package net.lab1024.sa.auth.service;

import net.lab1024.sa.auth.domain.request.LoginRequest;
import net.lab1024.sa.auth.domain.request.RefreshTokenRequest;
import net.lab1024.sa.auth.domain.response.LoginResponse;
import net.lab1024.sa.auth.domain.response.UserInfoResponse;

/**
 * 认证服务接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @date 2025-01-27
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponse login(LoginRequest request);

    /**
     * 刷新令牌
     *
     * @param request 刷新令牌请求
     * @return 新的令牌响应
     */
    LoginResponse refreshToken(RefreshTokenRequest request);

    /**
     * 用户登出
     *
     * @param token 令牌
     */
    void logout(String token);

    /**
     * 验证令牌
     *
     * @param token 令牌
     * @return 是否有效
     */
    boolean validateToken(String token);

    /**
     * 获取用户信息
     *
     * @param token 令牌
     * @return 用户信息
     */
    UserInfoResponse getUserInfo(String token);

    /**
     * 检查权限
     *
     * @param token 令牌
     * @param permission 权限编码
     * @return 是否有权限
     */
    boolean hasPermission(String token, String permission);

    /**
     * 检查角色
     *
     * @param token 令牌
     * @param role 角色编码
     * @return 是否有角色
     */
    boolean hasRole(String token, String role);
}