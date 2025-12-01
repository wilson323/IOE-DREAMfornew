package net.lab1024.sa.auth.service;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.auth.domain.entity.UserEntity;
import net.lab1024.sa.auth.domain.entity.UserRoleEntity;
import net.lab1024.sa.auth.domain.vo.LoginRequest;
import net.lab1024.sa.auth.domain.vo.LoginResponse;
import net.lab1024.sa.auth.domain.vo.RefreshTokenRequest;
import net.lab1024.sa.auth.repository.UserRepository;
import net.lab1024.sa.auth.repository.UserRoleRepository;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;

/**
 * 用户认证服务
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Value("${auth.password.max-attempts}")
    private int maxPasswordAttempts;

    @Value("${auth.password.lock-duration}")
    private int lockDurationMinutes;

    private Key signingKey;

    @PostConstruct
    public void init() {
        // 初始化JWT签名密钥
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    @Transactional
    public ResponseDTO<LoginResponse> login(LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();
        String loginIp = request.getLoginIp();

        log.info("用户登录尝试: username={}, ip={}", username, loginIp);

        try {
            // 查找用户
            UserEntity user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new BusinessException("用户名或密码错误"));

            // 检查用户状态
            validateUserStatus(user);

            // 验证密码
            if (!validatePassword(user, password)) {
                handleLoginFailure(user);
                throw new BusinessException("用户名或密码错误");
            }

            // 登录成功，重置失败次数
            resetLoginFailure(user);

            // 生成JWT令牌
            String accessToken = generateAccessToken(user);
            String refreshToken = generateRefreshToken(user);

            // 更新最后登录信息
            updateLastLoginInfo(user, loginIp);

            // 缓存用户会话
            cacheUserSession(user, accessToken);

            // 获取用户角色和权限
            List<String> roles = new ArrayList<>(getUserRoles(user.getUserId()));
            List<String> permissions = new ArrayList<>(getUserPermissions(user.getUserId()));

            // 构建登录响应
            LoginResponse response = LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtExpiration)
                    .userInfo(buildUserInfo(user, roles))
                    .roles(roles)
                    .permissions(permissions)
                    .build();

            log.info("用户登录成功: userId={}, username={}", user.getUserId(), username);
            return ResponseDTO.ok(response);

        } catch (BusinessException e) {
            log.warn("用户登录失败: username={}, error={}", username, e.getMessage());
            return ResponseDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("用户登录异常: username={}", username, e);
            return ResponseDTO.error("登录失败，请稍后重试");
        }
    }

    /**
     * 刷新令牌
     *
     * @param request 刷新令牌请求
     * @return 登录响应
     */
    @Transactional
    public ResponseDTO<LoginResponse> refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        try {
            // 验证刷新令牌
            Claims claims = parseRefreshToken(refreshToken);
            Long userId = Long.parseLong(claims.getSubject());

            // 查找用户
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException("用户不存在"));

            // 检查用户状态
            validateUserStatus(user);

            // 检查刷新令牌是否有效（从Redis中验证）
            if (!isValidRefreshToken(userId, refreshToken)) {
                throw new BusinessException("刷新令牌无效");
            }

            // 生成新的访问令牌
            String newAccessToken = generateAccessToken(user);
            String newRefreshToken = generateRefreshToken(user);

            // 更新缓存的用户会话
            updateUserSession(user.getUserId(), newAccessToken, newRefreshToken);

            // 获取用户角色和权限
            List<String> roles = new ArrayList<>(getUserRoles(user.getUserId()));
            List<String> permissions = new ArrayList<>(getUserPermissions(user.getUserId()));

            LoginResponse response = LoginResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtExpiration)
                    .userInfo(buildUserInfo(user, roles))
                    .roles(roles)
                    .permissions(permissions)
                    .build();

            log.info("令牌刷新成功: userId={}", userId);
            return ResponseDTO.ok(response);

        } catch (BusinessException e) {
            log.warn("令牌刷新失败: error={}", e.getMessage());
            return ResponseDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("令牌刷新异常", e);
            return ResponseDTO.error("令牌刷新失败，请重新登录");
        }
    }

    /**
     * 用户登出
     *
     * @param userId 用户ID
     * @return 响应结果
     */
    @CacheEvict(value = "user:session", key = "#userId")
    @Transactional
    public ResponseDTO<Void> logout(Long userId) {
        try {
            // 清除用户会话缓存
            String sessionKey = "user:session:" + userId;
            redisTemplate.delete(sessionKey);

            // 清除用户权限缓存
            String permissionKey = "user:permissions:" + userId;
            redisTemplate.delete(permissionKey);

            log.info("用户登出成功: userId={}", userId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("用户登出失败: userId={}", userId, e);
            return ResponseDTO.error("登出失败，请稍后重试");
        }
    }

    /**
     * 验证访问令牌
     *
     * @param token 访问令牌
     * @return 令牌声明
     */
    public Claims validateAccessToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.warn("访问令牌验证失败: token={}", token, e);
            throw new BusinessException("无效的访问令牌");
        }
    }

    /**
     * 根据令牌获取用户信息
     *
     * @param token 访问令牌
     * @return 用户实体
     */
    @Cacheable(value = "user:session", key = "#userId", unless = "#result == null")
    public UserEntity getUserByToken(String token) {
        try {
            Claims claims = validateAccessToken(token);
            Long userId = Long.parseLong(claims.getSubject());

            return userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException("用户不存在"));

        } catch (Exception e) {
            log.warn("根据令牌获取用户信息失败: token={}", token, e);
            throw new BusinessException("令牌无效");
        }
    }

    /**
     * 检查用户权限
     *
     * @param userId 用户ID
     * @return 权限集合
     */
    @Cacheable(value = "user:permissions", key = "#userId", unless = "#result == null")
    public Set<String> getUserPermissions(Long userId) {
        try {
            // 通过用户角色关联查询权限
            List<String> permissions = userRepository.findUserPermissions(userId);
            return new HashSet<>(permissions);
        } catch (Exception e) {
            log.error("获取用户权限失败: userId={}", userId, e);
            return new HashSet<>();
        }
    }

    /**
     * 检查用户是否具有指定权限
     *
     * @param userId     用户ID
     * @param permission 权限标识
     * @return 是否具有权限
     */
    public boolean hasPermission(Long userId, String permission) {
        Set<String> permissions = getUserPermissions(userId);
        return permissions.contains(permission);
    }

    /**
     * 检查用户是否具有指定角色
     *
     * @param userId 用户ID
     * @param role   角色标识
     * @return 是否具有角色
     */
    public boolean hasRole(Long userId, String role) {
        List<String> roles = getUserRoles(userId);
        return roles.contains(role);
    }

    // 私有方法

    /**
     * 验证用户状态
     *
     * @param user 用户实体
     */
    private void validateUserStatus(UserEntity user) {
        if (user.isLocked()) {
            throw new BusinessException("账户已被锁定，请稍后重试");
        }

        if (!user.isEnabled()) {
            throw new BusinessException("账户已被禁用");
        }
    }

    /**
     * 验证密码
     *
     * @param user     用户实体
     * @param password 密码
     * @return 是否匹配
     */
    private boolean validatePassword(UserEntity user, String password) {
        return passwordEncoder.matches(password, user.getPassword());
    }

    /**
     * 处理登录失败
     *
     * @param user 用户实体
     */
    private void handleLoginFailure(UserEntity user) {
        user.incrementLoginFailedCount();

        if (user.getLoginFailedCount() >= maxPasswordAttempts) {
            user.lockAccount(lockDurationMinutes);
            log.warn("用户账户被锁定: userId={}, username={}, 失败次数={}",
                    user.getUserId(), user.getUsername(), user.getLoginFailedCount());
        }

        userRepository.save(user);
    }

    /**
     * 重置登录失败次数
     *
     * @param user 用户实体
     */
    private void resetLoginFailure(UserEntity user) {
        if (user.getLoginFailedCount() != null && user.getLoginFailedCount() > 0) {
            user.resetLoginFailedCount();
            userRepository.save(user);
        }
    }

    /**
     * 更新最后登录信息
     *
     * @param user    用户实体
     * @param loginIp 登录IP
     */
    private void updateLastLoginInfo(UserEntity user, String loginIp) {
        user.updateLastLogin(loginIp);
        userRepository.save(user);
    }

    /**
     * 生成访问令牌
     *
     * @param user 用户实体
     * @return 访问令牌
     */
    private String generateAccessToken(UserEntity user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiration = now.plus(jwtExpiration, ChronoUnit.SECONDS);

        return Jwts.builder()
                .setSubject(user.getUserId().toString())
                .setIssuedAt(java.sql.Timestamp.valueOf(now))
                .setExpiration(java.sql.Timestamp.valueOf(expiration))
                .claim("username", user.getUsername())
                .claim("userType", "user")
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 生成刷新令牌
     *
     * @param user 用户实体
     * @return 刷新令牌
     */
    private String generateRefreshToken(UserEntity user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiration = now.plus(refreshExpiration, ChronoUnit.SECONDS);

        return Jwts.builder()
                .setSubject(user.getUserId().toString())
                .setIssuedAt(java.sql.Timestamp.valueOf(now))
                .setExpiration(java.sql.Timestamp.valueOf(expiration))
                .claim("tokenType", "refresh")
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 解析刷新令牌
     *
     * @param refreshToken 刷新令牌
     * @return 令牌声明
     */
    private Claims parseRefreshToken(String refreshToken) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();

        if (!"refresh".equals(claims.get("tokenType"))) {
            throw new BusinessException("无效的刷新令牌");
        }

        return claims;
    }

    /**
     * 缓存用户会话
     *
     * @param user        用户实体
     * @param accessToken 访问令牌
     */
    private void cacheUserSession(UserEntity user, String accessToken) {
        String sessionKey = "user:session:" + user.getUserId();
        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("user", user);
        sessionData.put("accessToken", accessToken);
        sessionData.put("loginTime", LocalDateTime.now());

        redisTemplate.opsForValue().set(sessionKey, sessionData, jwtExpiration, TimeUnit.SECONDS);
    }

    /**
     * 更新用户会话
     *
     * @param userId          用户ID
     * @param newAccessToken  新的访问令牌
     * @param newRefreshToken 新的刷新令牌
     */
    @SuppressWarnings("unchecked")
    private void updateUserSession(Long userId, String newAccessToken, String newRefreshToken) {
        String sessionKey = "user:session:" + userId;
        Map<String, Object> sessionData = (Map<String, Object>) redisTemplate.opsForValue().get(sessionKey);

        if (sessionData != null) {
            sessionData.put("accessToken", newAccessToken);
            sessionData.put("refreshToken", newRefreshToken);
            redisTemplate.opsForValue().set(sessionKey, sessionData, jwtExpiration, TimeUnit.SECONDS);
        }
    }

    /**
     * 验证刷新令牌是否有效
     *
     * @param userId       用户ID
     * @param refreshToken 刷新令牌
     * @return 是否有效
     */
    @SuppressWarnings("unchecked")
    private boolean isValidRefreshToken(Long userId, String refreshToken) {
        String sessionKey = "user:session:" + userId;
        Map<String, Object> sessionData = (Map<String, Object>) redisTemplate.opsForValue().get(sessionKey);

        return sessionData != null && refreshToken.equals(sessionData.get("refreshToken"));
    }

    /**
     * 获取用户角色
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    private List<String> getUserRoles(Long userId) {
        List<UserRoleEntity> userRoles = userRoleRepository.findByUserId(userId);
        return userRoles.stream()
                .filter(UserRoleEntity::isValid)
                .map(ur -> ur.getRole().getRoleCode())
                .collect(Collectors.toList());
    }

    /**
     * 构建用户信息
     *
     * @param user  用户实体
     * @param roles 角色列表
     * @return 用户信息
     */
    private LoginResponse.UserInfo buildUserInfo(UserEntity user, List<String> roles) {
        return LoginResponse.UserInfo.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .roles(roles)
                .lastLoginTime(user.getLastLoginTime())
                .build();
    }
}
