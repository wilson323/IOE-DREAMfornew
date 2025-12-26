package net.lab1024.sa.common.auth.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.auth.service.ConcurrentLoginControlService;
import net.lab1024.sa.common.auth.service.JwtTokenBlacklistService;
import net.lab1024.sa.common.auth.service.UserLockService;

/**
 * 认证管理器
 * <p>
 * 纯Java类，不使用Spring注解
 * 通过构造函数注入依赖
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Slf4j
public class AuthManager {

    private final UserLockService userLockService;
    private final JwtTokenBlacklistService blacklistService;
    private final ConcurrentLoginControlService concurrentLoginService;

    /**
     * 构造函数注入依赖
     *
     * @param userLockService 用户锁定服务
     * @param blacklistService JWT黑名单服务
     * @param concurrentLoginService 并发登录控制服务
     */
    public AuthManager(UserLockService userLockService,
                      JwtTokenBlacklistService blacklistService,
                      ConcurrentLoginControlService concurrentLoginService) {
        this.userLockService = userLockService;
        this.blacklistService = blacklistService;
        this.concurrentLoginService = concurrentLoginService;
        log.debug("[认证管理器] 初始化完成");
    }

    /**
     * 检查用户是否被锁定
     *
     * @param username 用户名
     * @return 是否被锁定
     */
    public boolean isUserLocked(String username) {
        try {
            boolean locked = userLockService.isUserLocked(username);
            log.debug("[认证管理器] 用户锁定检查: username={}, locked={}", username, locked);
            return locked;
        } catch (Exception e) {
            log.error("[认证管理器] 用户锁定检查异常: username={}, error={}", username, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 检查并发登录是否超限
     *
     * @param userId 用户ID
     * @return 是否超限
     */
    public boolean isConcurrentLoginExceeded(Long userId) {
        try {
            // 使用默认检查（不限设备类型）
            boolean allowed = concurrentLoginService.allowLogin(userId, null);
            log.debug("[认证管理器] 并发登录检查: userId={}, allowed={}", userId, allowed);
            return !allowed;  // 不允许表示超限
        } catch (Exception e) {
            log.error("[认证管理器] 并发登录检查异常: userId={}, error={}", userId, e.getMessage(), e);
            return false;  // 异常时默认允许
        }
    }

    /**
     * 管理用户会话（创建新会话，如超限则踢出旧会话）
     *
     * @param userId 用户ID
     * @param token 访问令牌
     * @param deviceInfo 设备信息
     * @return 被踢出的令牌（如果没有则返回null）
     */
    public String manageUserSession(Long userId, String token, String deviceInfo) {
        try {
            // 解析设备类型
            String deviceType = parseDeviceType(deviceInfo);

            // 创建会话（带自动踢出）
            String evictedToken = concurrentLoginService.createSessionWithEviction(userId, token, deviceType, deviceInfo);

            if (evictedToken != null) {
                log.warn("[认证管理器] 会话超限，踢出旧会话: userId={}, evictedToken={}", userId, maskToken(evictedToken));
            } else {
                log.info("[认证管理器] 会话创建成功: userId={}, deviceType={}", userId, deviceType);
            }

            // 同时保存到JWT黑名单服务的会话管理
            blacklistService.saveUserSession(userId, token, deviceInfo);

            return evictedToken;
        } catch (Exception e) {
            log.error("[认证管理器] 管理用户会话异常: userId={}, error={}", userId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 清除登录失败记录（登录成功时调用）
     *
     * @param username 用户名
     */
    public void clearLoginFailure(String username) {
        try {
            userLockService.clearLoginFailure(username);
            log.debug("[认证管理器] 清除登录失败记录: username={}", username);
        } catch (Exception e) {
            log.error("[认证管理器] 清除登录失败记录异常: username={}, error={}", username, e.getMessage(), e);
        }
    }

    /**
     * 记录登录失败
     *
     * @param username 用户名
     * @return 是否触发锁定
     */
    public boolean recordLoginFailure(String username) {
        try {
            boolean locked = userLockService.recordLoginFailure(username);
            log.warn("[认证管理器] 记录登录失败: username={}, locked={}", username, locked);
            return locked;
        } catch (Exception e) {
            log.error("[认证管理器] 记录登录失败异常: username={}, error={}", username, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 将令牌加入黑名单
     *
     * @param token 令牌
     */
    public void blacklistToken(String token) {
        try {
            // 获取令牌剩余时间作为黑名单TTL
            long ttl = getTokenRemainingTime(token);
            blacklistService.blacklistToken(token, ttl);
            log.info("[认证管理器] 令牌已加入黑名单: token={}", maskToken(token));
        } catch (Exception e) {
            log.error("[认证管理器] 令牌加入黑名单异常: error={}", e.getMessage(), e);
        }
    }

    /**
     * 检查令牌是否在黑名单中
     *
     * @param token 令牌
     * @return 是否在黑名单中
     */
    public boolean isTokenBlacklisted(String token) {
        try {
            boolean blacklisted = blacklistService.isTokenBlacklisted(token);
            if (blacklisted) {
                log.warn("[认证管理器] 令牌在黑名单中: token={}", maskToken(token));
            }
            return blacklisted;
        } catch (Exception e) {
            log.error("[认证管理器] 令牌黑名单检查异常: error={}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 移除用户会话
     *
     * @param userId 用户ID
     * @param token 令牌
     */
    public void removeUserSession(Long userId, String token) {
        try {
            blacklistService.removeUserSession(userId, token);
            log.debug("[认证管理器] 移除用户会话: userId={}", userId);
        } catch (Exception e) {
            log.error("[认证管理器] 移除用户会话异常: userId={}, error={}", userId, e.getMessage(), e);
        }
    }

    /**
     * 验证用户会话是否有效
     *
     * @param userId 用户ID
     * @param token 令牌
     * @return 是否有效
     */
    public boolean isValidUserSession(Long userId, String token) {
        try {
            boolean valid = blacklistService.isValidUserSession(userId, token);
            log.debug("[认证管理器] 用户会话验证: userId={}, valid={}", userId, valid);
            return valid;
        } catch (Exception e) {
            log.error("[认证管理器] 用户会话验证异常: userId={}, error={}", userId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 更新会话最后访问时间
     *
     * @param userId 用户ID
     * @param token 令牌
     */
    public void updateSessionLastAccessTime(Long userId, String token) {
        try {
            concurrentLoginService.updateLastAccessTime(userId, token);
            log.trace("[认证管理器] 更新会话最后访问时间: userId={}, token={}", userId, maskToken(token));
        } catch (Exception e) {
            log.error("[认证管理器] 更新会话最后访问时间异常: userId={}, error={}", userId, e.getMessage(), e);
        }
    }

    /**
     * 解析设备类型
     *
     * @param deviceInfo 设备信息
     * @return 设备类型（pc/mobile/tablet/unknown）
     */
    private String parseDeviceType(String deviceInfo) {
        if (deviceInfo == null) {
            return "unknown";
        }

        String lowerInfo = deviceInfo.toLowerCase();

        // 移动设备
        if (lowerInfo.contains("iphone") || lowerInfo.contains("android") ||
            lowerInfo.contains("mobile") || lowerInfo.contains("phone")) {
            return "mobile";
        }

        // 平板设备
        if (lowerInfo.contains("ipad") || lowerInfo.contains("tablet")) {
            return "tablet";
        }

        // PC设备
        if (lowerInfo.contains("windows") || lowerInfo.contains("macintosh") ||
            lowerInfo.contains("linux") || lowerInfo.contains("pc")) {
            return "pc";
        }

        return "unknown";
    }

    /**
     * 获取令牌剩余时间（秒）
     * 辅助方法，用于设置黑名单TTL
     *
     * @param token JWT令牌
     * @return 剩余时间（秒）
     */
    private long getTokenRemainingTime(String token) {
        // 这里简化处理，实际应该解析JWT获取过期时间
        // 默认7天黑名单TTL
        return 7 * 24 * 60 * 60L;
    }

    /**
     * 遮盖令牌，只显示前20个字符
     * 用于日志记录
     *
     * @param token JWT令牌
     * @return 遮盖后的令牌
     */
    private String maskToken(String token) {
        if (token == null) {
            return "null";
        }
        int length = Math.min(token.length(), 20);
        return token.substring(0, length) + "...";
    }
}
