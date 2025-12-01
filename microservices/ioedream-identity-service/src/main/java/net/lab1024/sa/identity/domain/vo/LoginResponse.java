package net.lab1024.sa.identity.domain.vo;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录响应VO
 * 基于现有登录响应模式重构
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@Data
@Schema(description = "登录响应")
public class LoginResponse {

    /**
     * 访问令牌
     */
    @Schema(description = "访问令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    /**
     * 刷新令牌
     */
    @Schema(description = "刷新令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    /**
     * 令牌类型
     */
    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType;

    /**
     * 令牌过期时间（秒）
     */
    @Schema(description = "令牌过期时间（秒）", example = "7200")
    private Long expiresIn;

    /**
     * 用户信息
     */
    @Schema(description = "用户信息")
    private UserInfo user;

    /**
     * 用户角色列表
     */
    @Schema(description = "用户角色列表", example = "[\"ADMIN\", \"USER\"]")
    private List<String> roles;

    /**
     * 用户权限列表
     */
    @Schema(description = "用户权限列表", example = "[\"user:list\", \"user:add\"]")
    private List<String> permissions;

    /**
     * Spring Security权限列表
     */
    @Schema(description = "Spring Security权限列表", example = "[\"ROLE_ADMIN\", \"user:list\"]")
    private List<String> authorities;

    /**
     * 会话ID（基于原会话模式）
     */
    @Schema(description = "会话ID", example = "session_12345")
    private String sessionId;

    /**
     * 登录时间
     */
    @Schema(description = "登录时间", example = "2025-11-27T10:30:00")
    private LocalDateTime loginTime;

    /**
     * 默认构造函数
     */
    public LoginResponse() {
    }

    /**
     * 用户信息内部类
     */
    @Data
    @Schema(description = "用户信息")
    public static class UserInfo {

        /**
         * 用户ID（基于原employee_id）
         */
        @Schema(description = "用户ID", example = "1001")
        private Long userId;

        /**
         * 用户名
         */
        @Schema(description = "用户名", example = "admin")
        private String username;

        /**
         * 真实姓名（基于原employee_name）
         */
        @Schema(description = "真实姓名", example = "管理员")
        private String realName;

        /**
         * 邮箱
         */
        @Schema(description = "邮箱", example = "admin@example.com")
        private String email;

        /**
         * 手机号
         */
        @Schema(description = "手机号", example = "13800138000")
        private String phone;

        /**
         * 头像URL
         */
        @Schema(description = "头像URL", example = "/api/file/avatar/admin.jpg")
        private String avatarUrl;

        /**
         * 用户状态
         */
        @Schema(description = "用户状态", example = "1")
        private Integer status;

        /**
         * 用户类型（基于原user_type）
         */
        @Schema(description = "用户类型", example = "1")
        private Integer userType;

        /**
         * 部门ID
         */
        @Schema(description = "部门ID", example = "1")
        private Long departmentId;

        /**
         * 职位
         */
        @Schema(description = "职位", example = "系统管理员")
        private String position;

        /**
         * 最后登录时间
         */
        @Schema(description = "最后登录时间", example = "2025-11-27T10:30:00")
        private LocalDateTime lastLoginTime;

        // 兼容性字段，保持与原有员工模式的兼容

        /**
         * 获取员工ID（兼容性方法）
         */
        public Long getEmployeeId() {
            return userId;
        }

        /**
         * 设置员工ID（兼容性方法）
         */
        public void setEmployeeId(Long employeeId) {
            this.userId = employeeId;
        }

        /**
         * 获取员工姓名（兼容性方法）
         */
        public String getEmployeeName() {
            return realName;
        }

        /**
         * 设置员工姓名（兼容性方法）
         */
        public void setEmployeeName(String employeeName) {
            this.realName = employeeName;
        }
    }

    // 兼容性方法，保持与原有登录响应的兼容

    /**
     * 检查令牌是否有效
     */
    public boolean isTokenValid() {
        return accessToken != null && !accessToken.trim().isEmpty()
                && refreshToken != null && !refreshToken.trim().isEmpty();
    }

    /**
     * 检查用户是否有效
     */
    public boolean isUserValid() {
        return user != null && user.getUserId() != null;
    }

    /**
     * 是否为管理员用户
     */
    public boolean isAdminUser() {
        return roles != null && roles.contains("ADMIN");
    }

    /**
     * 是否有指定权限
     */
    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }

    /**
     * 是否有指定角色
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
}
