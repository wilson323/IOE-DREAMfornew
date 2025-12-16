package net.lab1024.sa.common.openapi.service;

import net.lab1024.sa.common.openapi.domain.request.*;
import net.lab1024.sa.common.openapi.domain.response.*;

/**
 * 用户管理开放API服务接口
 * 提供用户认证、用户信息查询、权限管理等开放服务
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface UserOpenApiService {

    /**
     * 用户认证
     *
     * @param request 登录请求
     * @param clientIp 客户端IP
     * @return 登录响应
     */
    LoginResponse authenticate(LoginRequest request, String clientIp);

    /**
     * 刷新访问令牌
     *
     * @param refreshToken 刷新令牌
     * @return 刷新令牌响应
     */
    RefreshTokenResponse refreshToken(String refreshToken);

    /**
     * 用户退出登录
     *
     * @param token 访问令牌
     * @param clientIp 客户端IP
     */
    void logout(String token, String clientIp);

    /**
     * 获取用户详细信息
     *
     * @param token 访问令牌
     * @return 用户详细信息
     */
    UserProfileResponse getUserProfile(String token);

    /**
     * 更新用户信息
     *
     * @param token 访问令牌
     * @param request 更新请求
     * @param clientIp 客户端IP
     * @return 更新后的用户信息
     */
    UserProfileResponse updateUserProfile(String token, UpdateUserProfileRequest request, String clientIp);

    /**
     * 修改密码
     *
     * @param token 访问令牌
     * @param request 修改密码请求
     * @param clientIp 客户端IP
     */
    void changePassword(String token, ChangePasswordRequest request, String clientIp);

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @param token 访问令牌
     * @return 用户信息
     */
    UserInfoResponse getUserById(Long userId, String token);

    /**
     * 获取用户列表
     *
     * @param request 查询请求
     * @param token 访问令牌
     * @return 分页用户列表
     */
    PageResult<UserInfoResponse> getUserList(UserQueryRequest request, String token);

    /**
     * 获取用户权限
     *
     * @param userId 用户ID
     * @param token 访问令牌
     * @return 用户权限响应
     */
    UserPermissionResponse getUserPermissions(Long userId, String token);

    /**
     * 获取用户角色
     *
     * @param userId 用户ID
     * @param token 访问令牌
     * @return 角色列表
     */
    java.util.List<String> getUserRoles(Long userId, String token);

    /**
     * 检查用户权限
     *
     * @param token 访问令牌
     * @param permission 权限编码
     * @return 是否有权限
     */
    boolean checkPermission(String token, String permission);

    /**
     * 验证访问令牌
     *
     * @param token 访问令牌
     * @return 令牌验证响应
     */
    TokenValidationResponse validateToken(String token);
}