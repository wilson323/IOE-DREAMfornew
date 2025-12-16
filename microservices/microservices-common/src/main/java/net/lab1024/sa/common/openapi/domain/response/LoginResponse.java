package net.lab1024.sa.common.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户登录响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户登录响应")
public class LoginResponse {

    @Schema(description = "访问令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "刷新令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType;

    @Schema(description = "访问令牌过期时间（秒）", example = "7200")
    private Long expiresIn;

    @Schema(description = "访问令牌过期时间", example = "2025-12-16T15:30:00")
    private LocalDateTime accessTokenExpireTime;

    @Schema(description = "刷新令牌过期时间", example = "2025-12-30T15:30:00")
    private LocalDateTime refreshTokenExpireTime;

    @Schema(description = "用户信息")
    private UserInfo userInfo;

    @Schema(description = "用户权限列表")
    private List<String> permissions;

    @Schema(description = "用户角色列表")
    private List<String> roles;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户信息")
    public static class UserInfo {

        @Schema(description = "用户ID", example = "1001")
        private Long userId;

        @Schema(description = "用户名", example = "admin")
        private String username;

        @Schema(description = "真实姓名", example = "系统管理员")
        private String realName;

        @Schema(description = "邮箱", example = "admin@example.com")
        private String email;

        @Schema(description = "手机号", example = "13812345678")
        private String phone;

        @Schema(description = "头像", example = "https://example.com/avatar.jpg")
        private String avatar;

        @Schema(description = "部门ID", example = "1")
        private Long departmentId;

        @Schema(description = "部门名称", example = "技术部")
        private String departmentName;

        @Schema(description = "职位", example = "系统管理员")
        private String position;

        @Schema(description = "工号", example = "EMP001")
        private String employeeNo;
    }
}