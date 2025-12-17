package net.lab1024.sa.common.permission.service;

/**
 * 认证服务接口
 * 提供用户认证相关功能
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-17
 */
public interface AuthService {

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID，未登录返回null
     */
    Long getCurrentUserId();

    /**
     * 获取当前登录用户名
     *
     * @return 用户名，未登录返回null
     */
    String getCurrentUsername();

    /**
     * 检查用户是否已登录
     *
     * @return 是否已登录
     */
    boolean isAuthenticated();

    /**
     * 获取当前用户Token
     *
     * @return Token字符串
     */
    String getCurrentToken();
}
