package net.lab1024.sa.system.menu.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单视图对象
 *
 * @author SmartAdmin Team
 * @since 2025-11-29
 */
@Data
@Schema(description = "菜单视图对象")
public class MenuVO {

    /**
     * 菜单ID
     */
    @Schema(description = "菜单ID", example = "1")
    private Long menuId;

    /**
     * 父菜单ID
     */
    @Schema(description = "父菜单ID", example = "0")
    private Long parentId;

    /**
     * 父菜单名称
     */
    @Schema(description = "父菜单名称", example = "系统管理")
    private String parentName;

    /**
     * 菜单名称
     */
    @Schema(description = "菜单名称", example = "用户管理")
    private String menuName;

    /**
     * 菜单编码
     */
    @Schema(description = "菜单编码", example = "USER_MANAGE")
    private String menuCode;

    /**
     * 菜单类型-目录 2-菜单 3-按钮
     */
    @Schema(description = "菜单类型", example = "2", allowableValues = {"1", "2", "3"})
    private Integer menuType;

    /**
     * 菜单类型名称
     */
    @Schema(description = "菜单类型名称", example = "菜单")
    private String menuTypeName;

    /**
     * 菜单图标
     */
    @Schema(description = "菜单图标", example = "user")
    private String icon;

    /**
     * 菜单路径
     */
    @Schema(description = "菜单路径", example = "/system/user")
    private String path;

    /**
     * 组件路径
     */
    @Schema(description = "组件路径", example = "system/user/index")
    private String component;

    /**
     * 权限标识
     */
    @Schema(description = "权限标识", example = "system:user:list")
    private String permission;

    /**
     * 路由参数
     */
    @Schema(description = "路由参数", example = "{id: 1}")
    private String queryParam;

    /**
     * 是否缓存（0-否 1-是）
     */
    @Schema(description = "是否缓存", example = "1", allowableValues = {"0", "1"})
    private Integer isCache;

    /**
     * 是否外链（0-否 1-是）
     */
    @Schema(description = "是否外链", example = "0", allowableValues = {"0", "1"})
    private Integer isFrame;

    /**
     * 是否显示（0-否 1-是）
     */
    @Schema(description = "是否显示", example = "1", allowableValues = {"0", "1"})
    private Integer visible;

    /**
     * 状态（0-禁用 1-启用）
     */
    @Schema(description = "状态", example = "1", allowableValues = {"0", "1"})
    private Integer status;

    /**
     * 排序
     */
    @Schema(description = "排序", example = "1")
    private Integer sortOrder;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "用户管理菜单")
    private String remark;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-11-29T10:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2025-11-29T10:00:00")
    private LocalDateTime updateTime;

    /**
     * 子菜单列表
     */
    @Schema(description = "子菜单列表")
    private List<MenuVO> children;
}