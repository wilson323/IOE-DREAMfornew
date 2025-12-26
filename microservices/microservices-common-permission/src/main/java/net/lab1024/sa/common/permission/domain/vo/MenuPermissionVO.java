package net.lab1024.sa.common.permission.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 菜单权限视图对象
 * <p>
 * 用于表示用户对菜单的权限信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "菜单权限视图对象")
public class MenuPermissionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "菜单ID", example = "1001")
    private Long menuId;

    @Schema(description = "菜单编码", example = "MENU_001")
    private String menuCode;

    @Schema(description = "菜单名称", example = "用户管理")
    private String menuName;

    @Schema(description = "菜单路径", example = "/user")
    private String menuPath;

    @Schema(description = "菜单图标", example = "user")
    private String menuIcon;

    @Schema(description = "菜单类型", example = "1")
    private Integer menuType;

    @Schema(description = "父菜单ID", example = "0")
    private Long parentId;

    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "权限标识列表", example = "[\"user:view\", \"user:edit\"]")
    private List<String> permissions;

    @Schema(description = "是否有权限", example = "true")
    private Boolean hasPermission;

    @Schema(description = "子菜单列表")
    private List<MenuPermissionVO> children;
}

