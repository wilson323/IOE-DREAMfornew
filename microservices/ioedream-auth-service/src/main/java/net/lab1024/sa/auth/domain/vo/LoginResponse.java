package net.lab1024.sa.auth.domain.vo;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import net.lab1024.sa.auth.domain.entity.UserEntity;

/**
 * 登录响应VO
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@Data
@Builder
public class LoginResponse {

    /**
     * 创建带有用户信息的构建器
     */
    public static LoginResponseBuilder user(UserInfo userInfo) {
        return new LoginResponseBuilder().userInfo(userInfo);
    }

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
    private List<String> permissions;

    /**
     * 角色列表
     */
    private List<String> roles;

    /**
     * 用户信息
     */
    private UserInfo userInfo;

    /**
     * 用户信息静态内部类
     */
    @Data
    @Builder
    public static class UserInfo {
        private Long userId;
        private String username;
        private String realName;
        private String email;
        private String phone;
        private String avatarUrl;
        private Integer status;
        private String departmentName;
        private String position;
        private List<String> roles;
        private List<String> permissions;
        private java.time.LocalDateTime lastLoginTime;

        // 为了兼容LoginResponse builder，添加user方法
        public static UserInfoBuilder user(UserEntity user) {
            return UserInfo.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .lastLoginTime(user.getLastLoginTime());
        }
    }
}
