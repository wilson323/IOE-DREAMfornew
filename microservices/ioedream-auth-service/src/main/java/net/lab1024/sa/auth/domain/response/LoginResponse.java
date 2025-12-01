package net.lab1024.sa.auth.domain.response;

import lombok.Builder;
import lombok.Data;

/**
 * 登录响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @date 2025-01-27
 */
@Data
@Builder
public class LoginResponse {

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * 令牌类型
     */
    private String tokenType = "Bearer";

    /**
     * 访问令牌过期时间（秒）
     */
    private Long expiresIn;

    /**
     * 刷新令牌过期时间（秒）
     */
    private Long refreshExpiresIn;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 权限列表
     */
    private java.util.List<String> permissions;

    /**
     * 角色列表
     */
    private java.util.List<String> roles;
}