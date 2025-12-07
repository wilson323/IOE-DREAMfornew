package net.lab1024.sa.common.auth.service;

import net.lab1024.sa.common.auth.domain.dto.LoginRequestDTO;
import net.lab1024.sa.common.auth.domain.dto.RefreshTokenRequestDTO;
import net.lab1024.sa.common.auth.domain.vo.LoginResponseVO;
import net.lab1024.sa.common.auth.domain.vo.UserInfoVO;

/**
 * 认证服务接口
 * 整合自ioedream-auth-service
 *
 * 功能职责：
 * - 用户登录认证
 * - JWT令牌管理
 * - 会话管理
 * - 权限验证
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02（整合自auth-service）
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponseVO login(LoginRequestDTO request);

    /**
     * 刷新令牌
     *
     * @param request 刷新令牌请求
     * @return 新的令牌响应
     */
    LoginResponseVO refreshToken(RefreshTokenRequestDTO request);

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
    UserInfoVO getUserInfo(String token);

    /**
     * 检查权限
     *
     * @param token      令牌
     * @param permission 权限编码
     * @return 是否有权限
     */
    boolean hasPermission(String token, String permission);

    /**
     * 检查角色
     *
     * @param token 令牌
     * @param role  角色编码
     * @return 是否有角色
     */
    boolean hasRole(String token, String role);
}
