package net.lab1024.sa.auth.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.auth.domain.entity.UserEntity;
import net.lab1024.sa.auth.domain.request.LoginRequest;
import net.lab1024.sa.auth.domain.request.RefreshTokenRequest;
import net.lab1024.sa.auth.domain.response.LoginResponse;
import net.lab1024.sa.auth.domain.response.UserInfoResponse;
import net.lab1024.sa.auth.service.AuthService;
import net.lab1024.sa.auth.service.UserService;
import net.lab1024.sa.auth.util.JwtTokenUtil;
import net.lab1024.sa.common.domain.ResponseDTO;

/**
 * 认证服务实现
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @date 2025-01-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    @Value("${auth.login.max-sessions:3}")
    private Integer maxSessions;

    @Value("${auth.login.session-timeout:3600}")
    private Integer sessionTimeout;

    private static final String TOKEN_BLACKLIST_PREFIX = "auth:token:blacklist:";
    private static final String USER_SESSION_PREFIX = "auth:user:session:";

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        try {
            log.info("用户登录请求，用户名: {}", request.getUsername());

            // 1. 验证用户名和密码
            UserEntity user = userService.getUserByUsername(request.getUsername());
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new RuntimeException("密码错误");
            }

            // 2. 检查用户状态
            if (user.getStatus() != 1) {
                throw new RuntimeException("用户已被禁用");
            }

            // 3. 查询用户权限和角色
            Set<String> permissionSet = userService.getUserPermissions(user.getUserId());
            Set<String> roleSet = userService.getUserRoles(user.getUserId());
            List<String> permissions = new ArrayList<>(permissionSet);
            List<String> roles = new ArrayList<>(roleSet);

            // 4. 生成JWT令牌
            String accessToken = jwtTokenUtil.generateAccessToken(
                    user.getUserId(),
                    user.getUsername(),
                    roles,
                    permissions);
            String refreshToken = jwtTokenUtil.generateRefreshToken(
                    user.getUserId(),
                    user.getUsername());

            // 5. 管理用户会话
            manageUserSessions(user.getUserId(), accessToken);

            // 6. 更新最后登录信息
            userService.updateLastLogin(user.getUserId(), request.getDeviceInfo());

            // 7. 构建响应
            LoginResponse response = LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenUtil.getRemainingExpiration(accessToken))
                    .refreshExpiresIn(jwtTokenUtil.getRemainingExpiration(refreshToken))
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .nickname(user.getRealName())
                    .avatarUrl(user.getAvatarUrl())
                    .permissions(permissions)
                    .roles(roles)
                    .build();

            log.info("用户登录成功，用户名: {}, 用户ID: {}", request.getUsername(), user.getUserId());
            return response;

        } catch (Exception e) {
            log.error("用户登录失败，用户名: {}", request.getUsername(), e);
            throw new RuntimeException("登录失败: " + e.getMessage());
        }
    }

    /**
     * 刷新令牌
     *
     * @param request 刷新令牌请求
     * @return 登录响应
     */
    @Override
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        try {
            String refreshToken = request.getRefreshToken();

            // 1. 验证刷新令牌
            if (!jwtTokenUtil.validateToken(refreshToken) || !jwtTokenUtil.isRefreshToken(refreshToken)) {
                throw new RuntimeException("无效的刷新令牌");
            }

            // 2. 从刷新令牌中获取用户信息
            Long userId = jwtTokenUtil.getUserIdFromToken(refreshToken);
            String username = jwtTokenUtil.getUsernameFromToken(refreshToken);

            // 3. 查询用户信息
            ResponseDTO<UserEntity> userResponse = userService.getUserById(userId);
            if (userResponse == null || userResponse.getOk() == null || !userResponse.getOk()
                    || userResponse.getData() == null || userResponse.getData().getStatus() != 1) {
                throw new RuntimeException("用户不存在或已被禁用");
            }

            UserEntity user = userResponse.getData();

            // 4. 查询用户权限和角色
            Set<String> permissionSet = userService.getUserPermissions(user.getUserId());
            Set<String> roleSet = userService.getUserRoles(user.getUserId());
            List<String> permissions = new ArrayList<>(permissionSet);
            List<String> roles = new ArrayList<>(roleSet);

            // 5. 生成新的JWT令牌
            String newAccessToken = jwtTokenUtil.generateAccessToken(userId, username, roles, permissions);
            String newRefreshToken = jwtTokenUtil.generateRefreshToken(userId, username);

            // 6. 将旧的刷新令牌加入黑名单
            blacklistToken(refreshToken);

            // 7. 构建响应
            LoginResponse response = LoginResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenUtil.getRemainingExpiration(newAccessToken))
                    .refreshExpiresIn(jwtTokenUtil.getRemainingExpiration(newRefreshToken))
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .nickname(user.getRealName())
                    .avatarUrl(user.getAvatarUrl())
                    .permissions(permissions)
                    .roles(roles)
                    .build();

            log.info("刷新令牌成功，用户名: {}", username);
            return response;

        } catch (Exception e) {
            log.error("刷新令牌失败", e);
            throw new RuntimeException("刷新令牌失败: " + e.getMessage());
        }
    }

    /**
     * 用户登出
     *
     * @param token 访问令牌
     */
    @Override
    public void logout(String token) {
        try {
            if (StringUtils.hasText(token)) {
                // 1. 将令牌加入黑名单
                blacklistToken(token);

                // 2. 从用户会话中移除
                if (jwtTokenUtil.isAccessToken(token)) {
                    Long userId = jwtTokenUtil.getUserIdFromToken(token);
                    removeUserSession(userId, token);
                }

                log.info("用户登出成功");
            }
        } catch (Exception e) {
            log.error("用户登出失败", e);
        }
    }

    /**
     * 验证令牌
     *
     * @param token 访问令牌
     * @return 是否有效
     */
    @Override
    public boolean validateToken(String token) {
        try {
            // 1. 检查令牌格式和签名
            if (!jwtTokenUtil.validateToken(token)) {
                return false;
            }

            // 2. 检查令牌是否在黑名单中
            if (isTokenBlacklisted(token)) {
                return false;
            }

            // 3. 检查用户会话
            if (jwtTokenUtil.isAccessToken(token)) {
                Long userId = jwtTokenUtil.getUserIdFromToken(token);
                if (!isValidUserSession(userId, token)) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            log.error("令牌验证失败", e);
            return false;
        }
    }

    /**
     * 获取用户信息
     *
     * @param token 访问令牌
     * @return 用户信息响应
     */
    @Override
    public UserInfoResponse getUserInfo(String token) {
        try {
            if (!validateToken(token)) {
                throw new RuntimeException("无效的令牌");
            }

            Long userId = jwtTokenUtil.getUserIdFromToken(token);
            ResponseDTO<UserEntity> userResponse = userService.getUserById(userId);

            if (userResponse == null || userResponse.getOk() == null || !userResponse.getOk()
                    || userResponse.getData() == null) {
                throw new RuntimeException("获取用户信息失败");
            }

            UserEntity user = userResponse.getData();

            // 手动转换为UserInfoResponse
            UserInfoResponse userInfoResponse = UserInfoResponse.builder()
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .realName(user.getRealName())
                    .nickname(user.getRealName())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .avatarUrl(user.getAvatarUrl())
                    .status(user.getStatus())
                    .lastLoginTime(user.getLastLoginTime())
                    .build();

            return userInfoResponse;
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            throw new RuntimeException("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 检查权限
     *
     * @param token      访问令牌
     * @param permission 权限标识
     * @return 是否具有权限
     */
    @Override
    public boolean hasPermission(String token, String permission) {
        try {
            if (!validateToken(token) || !StringUtils.hasText(permission)) {
                return false;
            }

            List<String> permissions = jwtTokenUtil.getPermissionsFromToken(token);
            return permissions != null && permissions.contains(permission);
        } catch (Exception e) {
            log.error("权限检查失败，权限: {}", permission, e);
            return false;
        }
    }

    /**
     * 检查角色
     *
     * @param token 访问令牌
     * @param role  角色标识
     * @return 是否具有角色
     */
    @Override
    public boolean hasRole(String token, String role) {
        try {
            if (!validateToken(token) || !StringUtils.hasText(role)) {
                return false;
            }

            List<String> roles = jwtTokenUtil.getRolesFromToken(token);
            return roles != null && roles.contains(role);
        } catch (Exception e) {
            log.error("角色检查失败，角色: {}", role, e);
            return false;
        }
    }

    /**
     * 管理用户会话
     *
     * @param userId 用户ID
     * @param token  令牌
     */
    private void manageUserSessions(Long userId, String token) {
        try {
            String sessionKey = USER_SESSION_PREFIX + userId;

            // 检查当前会话数量
            Long sessionCount = redisTemplate.opsForSet().size(sessionKey);
            if (sessionCount != null && sessionCount >= maxSessions) {
                // 移除最旧的会话
                Set<String> tokenSet = redisTemplate.opsForSet().members(sessionKey);
                List<String> tokens = tokenSet != null ? new ArrayList<>(tokenSet) : new ArrayList<>();
                if (!tokens.isEmpty()) {
                    String oldestToken = tokens.get(0);
                    redisTemplate.opsForSet().remove(sessionKey, oldestToken);
                    blacklistToken(oldestToken);
                }
            }

            // 添加新会话
            redisTemplate.opsForSet().add(sessionKey, token);
            redisTemplate.expire(sessionKey, sessionTimeout, TimeUnit.SECONDS);

        } catch (Exception e) {
            log.error("管理用户会话失败，用户ID: {}", userId, e);
        }
    }

    /**
     * 移除用户会话
     *
     * @param userId 用户ID
     * @param token  令牌
     */
    private void removeUserSession(Long userId, String token) {
        try {
            String sessionKey = USER_SESSION_PREFIX + userId;
            redisTemplate.opsForSet().remove(sessionKey, token);
        } catch (Exception e) {
            log.error("移除用户会话失败，用户ID: {}", userId, e);
        }
    }

    /**
     * 验证用户会话是否有效
     *
     * @param userId 用户ID
     * @param token  令牌
     * @return 是否有效
     */
    private boolean isValidUserSession(Long userId, String token) {
        try {
            String sessionKey = USER_SESSION_PREFIX + userId;
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(sessionKey, token));
        } catch (Exception e) {
            log.error("验证用户会话失败，用户ID: {}", userId, e);
            return false;
        }
    }

    /**
     * 将令牌加入黑名单
     *
     * @param token 令牌
     */
    private void blacklistToken(String token) {
        try {
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(blacklistKey, "true", 7, TimeUnit.DAYS);
        } catch (Exception e) {
            log.error("将令牌加入黑名单失败", e);
        }
    }

    /**
     * 检查令牌是否在黑名单中
     *
     * @param token 令牌
     * @return 是否在黑名单中
     */
    private boolean isTokenBlacklisted(String token) {
        try {
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
            return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
        } catch (Exception e) {
            log.error("检查令牌黑名单失败", e);
            return false;
        }
    }
}
