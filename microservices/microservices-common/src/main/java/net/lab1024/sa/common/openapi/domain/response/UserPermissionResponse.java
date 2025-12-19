package net.lab1024.sa.common.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户权限响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户权限响应")
public class UserPermissionResponse {

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "真实姓名", example = "系统管理员")
    private String realName;

    @Schema(description = "权限列表")
    private List<PermissionDetail> permissions;

    @Schema(description = "角色列表")
    private List<RoleDetail> roles;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "权限详情")
    public static class PermissionDetail {

        @Schema(description = "权限ID", example = "1")
        private Long permissionId;

        @Schema(description = "权限编码", example = "user:view")
        private String permissionCode;

        @Schema(description = "权限名称", example = "查看用户")
        private String permissionName;

        @Schema(description = "权限类型", example = "menu", allowableValues = {"menu", "button", "api"})
        private String permissionType;

        @Schema(description = "父级权限ID", example = "0")
        private Long parentId;

        @Schema(description = "权限路径", example = "/user")
        private String path;

        @Schema(description = "权限图标", example = "user")
        private String icon;

        @Schema(description = "排序号", example = "1")
        private Integer sort;

        @Schema(description = "是否启用", example = "1", allowableValues = {"0", "1"})
        private Integer status;

        @Schema(description = "权限描述", example = "查看用户列表和详情")
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "角色详情")
    public static class RoleDetail {

        @Schema(description = "角色ID", example = "1")
        private Long roleId;

        @Schema(description = "角色编码", example = "admin")
        private String roleCode;

        @Schema(description = "角色名称", example = "管理员")
        private String roleName;

        @Schema(description = "角色描述", example = "系统管理员角色")
        private String description;

        @Schema(description = "角色类型", example = "system", allowableValues = {"system", "business", "custom"})
        private String roleType;

        @Schema(description = "是否启用", example = "1", allowableValues = {"0", "1"})
        private Integer status;

        @Schema(description = "排序号", example = "1")
        private Integer sort;

        @Schema(description = "创建时间", example = "2025-01-01T10:30:00")
        private String createTime;
    }
}