package net.lab1024.sa.common.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户详细信息响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户详细信息响应")
public class UserProfileResponse {

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

    @Schema(description = "性别", example = "1", allowableValues = {"0", "1", "2"})
    private Integer gender;

    @Schema(description = "生日", example = "1990-01-01")
    private String birthday;

    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    @Schema(description = "职位", example = "系统管理员")
    private String position;

    @Schema(description = "工号", example = "EMP001")
    private String employeeNo;

    @Schema(description = "用户状态", example = "1", allowableValues = {"0", "1"})
    private Integer status;

    @Schema(description = "账户锁定状态", example = "0", allowableValues = {"0", "1"})
    private Integer accountLocked;

    @Schema(description = "锁定原因", example = "密码错误次数过多")
    private String lockReason;

    @Schema(description = "最后登录时间", example = "2025-12-16T10:30:00")
    private LocalDateTime lastLoginTime;

    @Schema(description = "最后登录IP", example = "192.168.1.100")
    private String lastLoginIp;

    @Schema(description = "密码更新时间", example = "2025-12-01T10:30:00")
    private LocalDateTime passwordUpdateTime;

    @Schema(description = "账户过期时间", example = "2026-12-31T23:59:59")
    private LocalDateTime accountExpireTime;

    @Schema(description = "创建时间", example = "2025-01-01T10:30:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-12-16T10:30:00")
    private LocalDateTime updateTime;

    @Schema(description = "备注", example = "系统管理员")
    private String remark;

    @Schema(description = "用户权限列表")
    private List<PermissionInfo> permissions;

    @Schema(description = "用户角色列表")
    private List<RoleInfo> roles;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "权限信息")
    public static class PermissionInfo {

        @Schema(description = "权限ID", example = "1")
        private Long permissionId;

        @Schema(description = "权限编码", example = "user:view")
        private String permissionCode;

        @Schema(description = "权限名称", example = "查看用户")
        private String permissionName;

        @Schema(description = "权限类型", example = "menu", allowableValues = {"menu", "button", "api"})
        private String permissionType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "角色信息")
    public static class RoleInfo {

        @Schema(description = "角色ID", example = "1")
        private Long roleId;

        @Schema(description = "角色编码", example = "admin")
        private String roleCode;

        @Schema(description = "角色名称", example = "管理员")
        private String roleName;

        @Schema(description = "角色描述", example = "系统管理员角色")
        private String description;
    }
}