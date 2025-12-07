package net.lab1024.sa.common.identity.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 权限树VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Data
@Schema(description = "权限树VO")
public class PermissionTreeVO {

    @Schema(description = "权限ID", example = "1")
    private Long permissionId;

    @Schema(description = "权限编码", example = "user:view")
    private String permissionCode;

    @Schema(description = "权限名称", example = "查看用户")
    private String permissionName;

    @Schema(description = "权限类型：1-菜单 2-按钮 3-接口 4-数据", example = "1")
    private Integer permissionType;

    @Schema(description = "父权限ID", example = "0")
    private Long parentId;

    @Schema(description = "路由路径", example = "/user")
    private String path;

    @Schema(description = "组件路径", example = "views/user/index")
    private String component;

    @Schema(description = "图标", example = "user")
    private String icon;

    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "状态：1-启用 2-禁用", example = "1")
    private Integer status;

    @Schema(description = "子权限列表")
    private List<PermissionTreeVO> children;
}

