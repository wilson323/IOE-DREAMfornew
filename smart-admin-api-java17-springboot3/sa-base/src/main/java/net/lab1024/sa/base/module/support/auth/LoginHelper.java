package net.lab1024.sa.base.module.support.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 登录助手类
 * 严格遵循repowiki规范：提供登录用户信息获取和验证的工具方法
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Slf4j
@Component
public class LoginHelper {

    private static final ThreadLocal<Long> USER_ID_THREAD_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME_THREAD_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<String> TOKEN_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 设置当前登录用户信息
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param token 登录令牌
     */
    public static void setCurrentLoginUser(Long userId, String username, String token) {
        USER_ID_THREAD_LOCAL.set(userId);
        USERNAME_THREAD_LOCAL.set(username);
        TOKEN_THREAD_LOCAL.set(token);
        log.debug("设置当前登录用户: userId={}, username={}", userId, username);
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
     */
    public static Long getLoginUserId() {
        Long userId = USER_ID_THREAD_LOCAL.get();
        if (userId == null) {
            log.warn("当前线程未设置用户ID");
            return null;
        }
        return userId;
    }

    /**
     * 获取当前登录用户名
     *
     * @return 用户名
     */
    public static String getLoginUsername() {
        String username = USERNAME_THREAD_LOCAL.get();
        if (username == null) {
            log.warn("当前线程未设置用户名");
            return null;
        }
        return username;
    }

    /**
     * 获取当前登录令牌
     *
     * @return 登录令牌
     */
    public static String getLoginToken() {
        String token = TOKEN_THREAD_LOCAL.get();
        if (token == null) {
            log.warn("当前线程未设置登录令牌");
            return null;
        }
        return token;
    }

    /**
     * 获取当前登录用户ID（带默认值）
     *
     * @param defaultUserId 默认用户ID
     * @return 用户ID
     */
    public static Long getLoginUserId(Long defaultUserId) {
        return Optional.ofNullable(getLoginUserId()).orElse(defaultUserId);
    }

    /**
     * 获取当前登录用户名（带默认值）
     *
     * @param defaultUsername 默认用户名
     * @return 用户名
     */
    public static String getLoginUsername(String defaultUsername) {
        return Optional.ofNullable(getLoginUsername()).orElse(defaultUsername);
    }

    /**
     * 检查是否已登录
     *
     * @return 是否已登录
     */
    public static boolean isLogin() {
        return getLoginUserId() != null;
    }

    /**
     * 清除当前登录用户信息
     */
    public static void clearCurrentLoginUser() {
        log.debug("清除当前登录用户信息");
        USER_ID_THREAD_LOCAL.remove();
        USERNAME_THREAD_LOCAL.remove();
        TOKEN_THREAD_LOCAL.remove();
    }

    /**
     * 获取用户信息摘要
     *
     * @return 用户信息摘要
     */
    public static String getUserInfoSummary() {
        Long userId = getLoginUserId();
        String username = getLoginUsername();
        if (userId == null && username == null) {
            return "未登录";
        }
        return String.format("userId=%d, username=%s", userId, username);
    }
}