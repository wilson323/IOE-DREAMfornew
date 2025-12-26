package net.lab1024.sa.common.auth.service.impl;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import net.lab1024.sa.common.auth.dao.UserDao;
import net.lab1024.sa.common.auth.dao.UserSessionDao;
import net.lab1024.sa.common.auth.domain.dto.LoginRequestDTO;
import net.lab1024.sa.common.auth.domain.dto.RefreshTokenRequestDTO;
import net.lab1024.sa.common.auth.domain.vo.LoginResponseVO;
import net.lab1024.sa.common.auth.domain.vo.UserInfoVO;
import net.lab1024.sa.common.auth.manager.AuthManager;
import net.lab1024.sa.common.auth.service.AuthService;
import net.lab1024.sa.common.auth.util.JwtTokenUtil;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.organization.entity.UserEntity;

/**
 * 认证服务实现 整合自ioedream-auth-service
 *
 * 职责： - 用户登录认证 - JWT令牌管理 - 权限验证 - 会话管理
 *
 * 符合CLAUDE.md规范： - Service层处理核心业务逻辑 - 使用@Resource依赖注入 - 使用@Transactional事务管理 -
 * 调用Manager层处理复杂流程 - 实现企业级安全特性
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02（整合自auth-service）
 */
@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
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
     * 企业级特性： - 防暴力破解（登录失败计数） - 账户锁定机制 - 并发登录控制 - 会话管理 - 审计日志
     *
     * @param request
     *                登录请求
     * @return 登录响应
     */
    @Override
    @Observed(name = "auth.login", contextualName = "auth-login")
    public LoginResponseVO login(LoginRequestDTO request) {
        try {
            log.info("用户登录请求，用户名: {}, IP: {}", request.getUsername(), request.getLoginIp());

            // 1. 检查用户是否被锁定（防暴力破解）
            if (authManager.isUserLocked(request.getUsername())) {
                throw new BusinessException("AUTH_USER_LOCKED", "账户已被锁定，请稍后再试");
            }

            // 2. 验证用户名和密码
            UserEntity user = userDao.selectByUsername(request.getUsername());
            if (user == null) {
                authManager.recordLoginFailure(request.getUsername());
                throw new BusinessException("AUTH_INVALID_CREDENTIALS", "用户不存在或密码错误");
            }

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                authManager.recordLoginFailure(request.getUsername());
                throw new BusinessException("AUTH_INVALID_CREDENTIALS", "用户不存在或密码错误");
            }

            // 3. 检查用户状态
            if (user.getStatus() != 1) {
                throw new BusinessException("AUTH_USER_DISABLED", "用户已被禁用");
            }

            // 4. 检查并发登录限制
            if (authManager.isConcurrentLoginExceeded(user.getId())) {
                log.warn("用户并发登录超限，用户ID: {}", user.getId());
                // 企业级策略：可以选择拒绝登录或强制下线旧会话
            }

            // 5. 查询用户权限和角色
            List<String> permissionList = userDao.selectUserPermissions(user.getId());
            List<String> roleList = userDao.selectUserRoles(user.getId());
            Set<String> permissionSet = new HashSet<>(permissionList);
            Set<String> roleSet = new HashSet<>(roleList);
            List<String> permissions = new ArrayList<>(permissionSet);
            List<String> roles = new ArrayList<>(roleSet);

            // 6. 生成JWT令牌
            String accessToken = jwtTokenUtil.generateAccessToken(user.getId(), user.getUsername(), roles,
                    permissions);
            String refreshToken = jwtTokenUtil.generateRefreshToken(user.getId(), user.getUsername());

            // 7. 管理用户会话（Manager层处理复杂流程）
            authManager.manageUserSession(user.getId(), accessToken, request.getDeviceInfo());

            // 8. 清除登录失败记录
            authManager.clearLoginFailure(request.getUsername());

            // 9. 更新最后登录信息
            userDao.updateLastLogin(user.getId(), LocalDateTime.now(), request.getLoginIp());

            // 10. 构建响应
            LoginResponseVO response = LoginResponseVO.builder().accessToken(accessToken).refreshToken(refreshToken)
                    .tokenType("Bearer").expiresIn(jwtTokenUtil.getRemainingExpiration(accessToken))
                    .refreshExpiresIn(jwtTokenUtil.getRemainingExpiration(refreshToken)).userId(user.getId())
                    .username(user.getUsername()).nickname(user.getRealName()).avatarUrl(user.getAvatar())
                    .permissions(permissions).roles(roles).build();

            log.info("用户登录成功，用户名: {}, 用户ID: {}", request.getUsername(), user.getId());
            return response;

        } catch (BusinessException e) {
            log.warn("[用户登录] 业务异常，用户名: {}, error={}", request.getUsername(), e.getMessage());
            throw e;
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[用户登录] 参数异常，用户名: {}, error={}", request.getUsername(), e.getMessage());
            throw new ParamException("AUTH_INVALID_PARAM", "登录参数错误: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[用户登录] 系统异常，用户名: {}", request.getUsername(), e);
            throw new SystemException("AUTH_LOGIN_ERROR", "登录失败，请稍后重试", e);
        }
    }

    /**
     * 刷新令牌
     *
     * 企业级特性： - 令牌轮换（Token Rotation） - 旧令牌黑名单 - 安全令牌刷新
     *
     * @param request
     *                刷新令牌请求
     * @return 登录响应
     */
    @Override
    @Observed(name = "auth.refreshToken", contextualName = "auth-refresh-token")
    public LoginResponseVO refreshToken(RefreshTokenRequestDTO request) {
        try {
            String refreshToken = request.getRefreshToken();

            // 1. 验证刷新令牌
            if (!jwtTokenUtil.validateToken(refreshToken) || !jwtTokenUtil.isRefreshToken(refreshToken)) {
                throw new BusinessException("AUTH_INVALID_REFRESH_TOKEN", "无效的刷新令牌");
            }

            // 2. 检查令牌是否在黑名单中
            if (authManager.isTokenBlacklisted(refreshToken)) {
                throw new BusinessException("AUTH_TOKEN_BLACKLISTED", "令牌已失效");
            }

            // 3. 从刷新令牌中获取用户信息
            Long userId = jwtTokenUtil.getUserIdFromToken(refreshToken);
            String username = jwtTokenUtil.getUsernameFromToken(refreshToken);

            // 4. 查询用户信息
            UserEntity user = userDao.selectById(userId);
            if (user == null || user.getStatus() != 1) {
                throw new BusinessException("AUTH_USER_NOT_FOUND", "用户不存在或已被禁用");
            }

            // 5. 查询用户权限和角色
            List<String> permissionList = userDao.selectUserPermissions(user.getId());
            List<String> roleList = userDao.selectUserRoles(user.getId());
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
            LoginResponseVO response = LoginResponseVO.builder().accessToken(newAccessToken)
                    .refreshToken(newRefreshToken).tokenType("Bearer")
                    .expiresIn(jwtTokenUtil.getRemainingExpiration(newAccessToken))
                    .refreshExpiresIn(jwtTokenUtil.getRemainingExpiration(newRefreshToken)).userId(user.getId())
                    .username(user.getUsername()).nickname(user.getRealName()).avatarUrl(user.getAvatar())
                    .permissions(permissions).roles(roles).build();

            log.info("刷新令牌成功，用户名: {}", username);
            return response;

        } catch (BusinessException e) {
            log.warn("[刷新令牌] 业务异常，error={}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[刷新令牌] 参数异常，error={}", e.getMessage());
            throw new ParamException("AUTH_INVALID_PARAM", "刷新令牌参数错误: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[刷新令牌] 系统异常", e);
            throw new SystemException("AUTH_REFRESH_TOKEN_ERROR", "刷新令牌失败，请稍后重试", e);
        }
    }

    /**
     * 用户登出
     *
     * 企业级特性： - 令牌撤销 - 会话清理 - 审计日志
     *
     * @param token
     *              访问令牌
     */
    @Override
    @Observed(name = "auth.logout", contextualName = "auth-logout")
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
        } catch (BusinessException e) {
            log.warn("[用户登出] 业务异常，error={}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[用户登出] 系统异常", e);
            throw new SystemException("AUTH_LOGOUT_ERROR", "登出失败，请稍后重试", e);
        }
    }

    /**
     * 验证令牌
     *
     * 企业级特性： - 多重验证（格式+签名+黑名单+会话） - 安全令牌验证
     *
     * @param token
     *              访问令牌
     * @return 是否有效
     */
    @Override
    @Observed(name = "auth.validateToken", contextualName = "auth-validate-token")
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
                authManager.updateSessionLastAccessTime(userId, token);
            }

            return true;
        } catch (BusinessException | ParamException e) {
            log.debug("[令牌验证] 业务/参数异常，error={}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("[令牌验证] 系统异常", e);
            return false;
        }
    }

    /**
     * 获取用户信息
     *
     * @param token
     *              访问令牌
     * @return 用户信息响应
     */
    @Override
    @Observed(name = "auth.getUserInfo", contextualName = "auth-get-user-info")
    @Transactional(readOnly = true)
    public UserInfoVO getUserInfo(String token) {
        try {
            if (!validateToken(token)) {
                throw new BusinessException("AUTH_INVALID_TOKEN", "无效的令牌");
            }

            Long userId = jwtTokenUtil.getUserIdFromToken(token);
            UserEntity user = userDao.selectById(userId);

            if (user == null) {
                throw new BusinessException("AUTH_USER_NOT_FOUND", "获取用户信息失败");
            }

            return UserInfoVO.builder().userId(user.getId()).username(user.getUsername())
                    .realName(user.getRealName()).nickname(user.getRealName()).email(user.getEmail())
                    .phone(user.getPhone()).avatarUrl(user.getAvatar()).status(user.getStatus())
                    .lastLoginTime(user.getLastLoginTime()).build();
        } catch (BusinessException e) {
            log.warn("[获取用户信息] 业务异常，error={}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[获取用户信息] 参数异常，error={}", e.getMessage());
            throw new ParamException("AUTH_INVALID_PARAM", "获取用户信息参数错误: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[获取用户信息] 系统异常", e);
            throw new SystemException("AUTH_GET_USER_INFO_ERROR", "获取用户信息失败，请稍后重试", e);
        }
    }

    /**
     * 检查权限
     *
     * @param token
     *                   访问令牌
     * @param permission
     *                   权限标识
     * @return 是否具有权限
     */
    @Override
    @Observed(name = "auth.hasPermission", contextualName = "auth-has-permission")
    @Transactional(readOnly = true)
    public boolean hasPermission(String token, String permission) {
        try {
            if (!validateToken(token) || !StringUtils.hasText(permission)) {
                return false;
            }

            List<String> permissions = jwtTokenUtil.getPermissionsFromToken(token);
            return permissions != null && permissions.contains(permission);
        } catch (BusinessException | ParamException e) {
            log.debug("[权限检查] 业务/参数异常，权限: {}, error={}", permission, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("[权限检查] 系统异常，权限: {}", permission, e);
            return false;
        }
    }

    /**
     * 检查角色
     *
     * @param token
     *              访问令牌
     * @param role
     *              角色标识
     * @return 是否具有角色
     */
    @Override
    @Observed(name = "auth.hasRole", contextualName = "auth-has-role")
    @Transactional(readOnly = true)
    public boolean hasRole(String token, String role) {
        try {
            if (!validateToken(token) || !StringUtils.hasText(role)) {
                return false;
            }

            List<String> roles = jwtTokenUtil.getRolesFromToken(token);
            return roles != null && roles.contains(role);
        } catch (BusinessException | ParamException e) {
            log.debug("[角色检查] 业务/参数异常，角色: {}, error={}", role, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("[角色检查] 系统异常，角色: {}", role, e);
            return false;
        }
    }
}

