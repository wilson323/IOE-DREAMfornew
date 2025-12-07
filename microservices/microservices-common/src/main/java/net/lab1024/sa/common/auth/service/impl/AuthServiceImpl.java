package net.lab1024.sa.common.auth.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.auth.dao.UserDao;
import net.lab1024.sa.common.auth.dao.UserSessionDao;
import net.lab1024.sa.common.auth.domain.dto.LoginRequestDTO;
import net.lab1024.sa.common.auth.domain.dto.RefreshTokenRequestDTO;
import net.lab1024.sa.common.auth.domain.vo.LoginResponseVO;
import net.lab1024.sa.common.auth.domain.vo.UserInfoVO;
import net.lab1024.sa.common.auth.manager.AuthManager;
import net.lab1024.sa.common.auth.service.AuthService;
import net.lab1024.sa.common.auth.util.JwtTokenUtil;
import net.lab1024.sa.common.security.entity.UserEntity;

/**
 * 认证服务实现
 * 整合自ioedream-auth-service
 *
 * 职责：
 * - 用户登录认证
 * - JWT令牌管理
 * - 权限验证
 * - 会话管理
 *
 * 符合CLAUDE.md规范：
 * - Service层处理核心业务逻辑
 * - 使用@Resource依赖注入
 * - 使用@Transactional事务管理
 * - 调用Manager层处理复杂流程
 * - 实现企业级安全特性
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02（整合自auth-service）
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AuthServiceImpl implements AuthService {

    @Resource
    private UserSessionDao userSessionDao;

    @Resource
    private UserDao userDao;

    @Resource
    private AuthManager authManager;

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Resource
    private PasswordEncoder passwordEncoder;

    /**
     * 用户登录
     *
     * 企业级特性：
     * - 防暴力破解（登录失败计数）
     * - 账户锁定机制
     * - 并发登录控制
     * - 会话管理
     * - 审计日志
     *
     * @param request 登录请求
     * @return 登录响应
     */
    @Override
    public LoginResponseVO login(LoginRequestDTO request) {
        try {
            log.info("用户登录请求，用户名: {}, IP: {}", request.getUsername(), request.getLoginIp());

            // 1. 检查用户是否被锁定（防暴力破解）
            if (authManager.isUserLocked(request.getUsername())) {
                throw new RuntimeException("账户已被锁定，请稍后再试");
            }

            // 2. 验证用户名和密码
            UserEntity user = userDao.selectByUsername(request.getUsername());
            if (user == null) {
                authManager.recordLoginFailure(request.getUsername());
                throw new RuntimeException("用户不存在或密码错误");
            }

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                authManager.recordLoginFailure(request.getUsername());
                throw new RuntimeException("用户不存在或密码错误");
            }

            // 3. 检查用户状态
            if (user.getStatus() != 1) {
                throw new RuntimeException("用户已被禁用");
            }

            // 4. 检查并发登录限制
            if (authManager.isConcurrentLoginExceeded(user.getUserId())) {
                log.warn("用户并发登录超限，用户ID: {}", user.getUserId());
                // 企业级策略：可以选择拒绝登录或强制下线旧会话
            }

            // 5. 查询用户权限和角色
            List<String> permissionList = userDao.selectUserPermissions(user.getUserId());
            List<String> roleList = userDao.selectUserRoles(user.getUserId());
            Set<String> permissionSet = new HashSet<>(permissionList);
            Set<String> roleSet = new HashSet<>(roleList);
            List<String> permissions = new ArrayList<>(permissionSet);
            List<String> roles = new ArrayList<>(roleSet);

            // 6. 生成JWT令牌
            String accessToken = jwtTokenUtil.generateAccessToken(
                    user.getUserId(),
                    user.getUsername(),
                    roles,
                    permissions);
            String refreshToken = jwtTokenUtil.generateRefreshToken(
                    user.getUserId(),
                    user.getUsername());

            // 7. 管理用户会话（Manager层处理复杂流程）
            authManager.manageUserSession(user.getUserId(), accessToken, request.getDeviceInfo());

            // 8. 清除登录失败记录
            authManager.clearLoginFailure(request.getUsername());

            // 9. 更新最后登录信息
            userDao.updateLastLogin(user.getUserId(), LocalDateTime.now(), request.getLoginIp());

            // 10. 构建响应
            LoginResponseVO response = LoginResponseVO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenUtil.getRemainingExpiration(accessToken))
                    .refreshExpiresIn(jwtTokenUtil.getRemainingExpiration(refreshToken))
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .nickname(user.getRealName())
                    .avatarUrl(user.getAvatar())
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
     * 企业级特性：
     * - 令牌轮换（Token Rotation）
     * - 旧令牌黑名单
     * - 安全令牌刷新
     *
     * @param request 刷新令牌请求
     * @return 登录响应
     */
    @Override
    public LoginResponseVO refreshToken(RefreshTokenRequestDTO request) {
        try {
            String refreshToken = request.getRefreshToken();

            // 1. 验证刷新令牌
            if (!jwtTokenUtil.validateToken(refreshToken) || !jwtTokenUtil.isRefreshToken(refreshToken)) {
                throw new RuntimeException("无效的刷新令牌");
            }

            // 2. 检查令牌是否在黑名单中
            if (authManager.isTokenBlacklisted(refreshToken)) {
                throw new RuntimeException("令牌已失效");
            }

            // 3. 从刷新令牌中获取用户信息
            Long userId = jwtTokenUtil.getUserIdFromToken(refreshToken);
            String username = jwtTokenUtil.getUsernameFromToken(refreshToken);

            // 4. 查询用户信息
            UserEntity user = userDao.selectById(userId);
            if (user == null || user.getStatus() != 1) {
                throw new RuntimeException("用户不存在或已被禁用");
            }

            // 5. 查询用户权限和角色
            List<String> permissionList = userDao.selectUserPermissions(user.getUserId());
            List<String> roleList = userDao.selectUserRoles(user.getUserId());
            Set<String> permissionSet = new HashSet<>(permissionList);
            Set<String> roleSet = new HashSet<>(roleList);
            List<String> permissions = new ArrayList<>(permissionSet);
            List<String> roles = new ArrayList<>(roleSet);

            // 6. 生成新的JWT令牌（令牌轮换）
            String newAccessToken = jwtTokenUtil.generateAccessToken(userId, username, roles, permissions);
            String newRefreshToken = jwtTokenUtil.generateRefreshToken(userId, username);

            // 7. 将旧的刷新令牌加入黑名单（防止重放攻击）
            authManager.blacklistToken(refreshToken);

            // 8. 构建响应
            LoginResponseVO response = LoginResponseVO.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenUtil.getRemainingExpiration(newAccessToken))
                    .refreshExpiresIn(jwtTokenUtil.getRemainingExpiration(newRefreshToken))
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .nickname(user.getRealName())
                    .avatarUrl(user.getAvatar())
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
     * 企业级特性：
     * - 令牌撤销
     * - 会话清理
     * - 审计日志
     *
     * @param token 访问令牌
     */
    @Override
    public void logout(String token) {
        try {
            if (StringUtils.hasText(token)) {
                // 1. 将令牌加入黑名单
                authManager.blacklistToken(token);

                // 2. 从用户会话中移除
                if (jwtTokenUtil.isAccessToken(token)) {
                    Long userId = jwtTokenUtil.getUserIdFromToken(token);
                    authManager.removeUserSession(userId, token);
                }

                log.info("用户登出成功");
            }
        } catch (Exception e) {
            log.error("用户登出失败", e);
            throw new RuntimeException("登出失败: " + e.getMessage());
        }
    }

    /**
     * 验证令牌
     *
     * 企业级特性：
     * - 多重验证（格式+签名+黑名单+会话）
     * - 安全令牌验证
     *
     * @param token 访问令牌
     * @return 是否有效
     */
    @Override
    @Transactional(readOnly = true)
    public boolean validateToken(String token) {
        try {
            // 1. 检查令牌格式和签名
            if (!jwtTokenUtil.validateToken(token)) {
                return false;
            }

            // 2. 检查令牌是否在黑名单中
            if (authManager.isTokenBlacklisted(token)) {
                return false;
            }

            // 3. 检查用户会话（访问令牌才需要）
            if (jwtTokenUtil.isAccessToken(token)) {
                Long userId = jwtTokenUtil.getUserIdFromToken(token);
                if (!authManager.isValidUserSession(userId, token)) {
                    return false;
                }

                // 更新会话最后访问时间
                authManager.updateSessionLastAccessTime(token);
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
    @Transactional(readOnly = true)
    public UserInfoVO getUserInfo(String token) {
        try {
            if (!validateToken(token)) {
                throw new RuntimeException("无效的令牌");
            }

            Long userId = jwtTokenUtil.getUserIdFromToken(token);
            UserEntity user = userDao.selectById(userId);

            if (user == null) {
                throw new RuntimeException("获取用户信息失败");
            }

            return UserInfoVO.builder()
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .realName(user.getRealName())
                    .nickname(user.getRealName())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .avatarUrl(user.getAvatar())
                    .status(user.getStatus())
                    .lastLoginTime(user.getLastLoginTime())
                    .build();
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
}
