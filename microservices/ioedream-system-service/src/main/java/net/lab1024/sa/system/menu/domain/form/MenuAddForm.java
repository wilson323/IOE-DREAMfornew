package net.lab1024.sa.system.menu.domain.form;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 菜单新增表单
 * <p>
 * 用于菜单新增的数据验证和传输
 *
 * @author SmartAdmin Team
 * @since 2025-11-29
 */
@Data
@Schema(description = "菜单新增表单")
public class MenuAddForm implements Serializable {

    /**
     * 父菜单ID
     */
    @NotNull(message = "父菜单ID不能为空")
    @Schema(description = "父菜单ID", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long parentId;

    /**
     * 菜单名称
     */
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50, message = "菜单名称长度不能超过50个字符")
    @Schema(description = "菜单名称", example = "用户管理", requiredMode = Schema.RequiredMode.REQUIRED)
    private String menuName;

    /**
     * 菜单编码
     */
    @Size(max = 50, message = "菜单编码长度不能超过50个字符")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "菜单编码必须以大写字母开头，只能包含大写字母、数字和下划线")
    @Schema(description = "菜单编码", example = "USER_MANAGE")
    private String menuCode;

    /**
     * 菜单类型：1-目录 2-菜单 3-按钮
     */
    @NotNull(message = "菜单类型不能为空")
    @Schema(description = "菜单类型", example = "2", allowableValues = { "1", "2",
            "3" }, requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer menuType;

    /**
     * 菜单图标
     */
    @Size(max = 100, message = "菜单图标长度不能超过100个字符")
    @Schema(description = "菜单图标", example = "user")
    private String icon;

    /**
     * 菜单路径
     */
    @Size(max = 200, message = "菜单路径长度不能超过200个字符")
    @Schema(description = "菜单路径", example = "/system/user")
    private String path;

    /**
     * 组件路径
     */
    @Size(max = 200, message = "组件路径长度不能超过200个字符")
    @Schema(description = "组件路径", example = "system/user/index")
    private String component;

    /**
     * 权限标识
     */
    @Size(max = 100, message = "权限标识长度不能超过100个字符")
    @Schema(description = "权限标识", example = "system:user:list")
    private String permission;

    /**
     * 路由参数
     */
    @Size(max = 255, message = "路由参数长度不能超过255个字符")
    @Schema(description = "路由参数", example = "{id: 1}")
    private String queryParam;

    /**
     * 是否缓存（0-否 1-是）
     */
    @Schema(description = "是否缓存", example = "1", allowableValues = { "0", "1" })
    private Integer isCache;

    /**
     * 是否外链（0-否 1-是）
     */
    @Schema(description = "是否外链", example = "0", allowableValues = { "0", "1" })
    private Integer isFrame;

    /**
     * 是否显示（0-否 1-是）
     */
    @Schema(description = "是否显示", example = "1", allowableValues = { "0", "1" })
    private Integer visible;

    /**
     * 状态（0-禁用 1-启用）
     */
    @Schema(description = "状态", example = "1", allowableValues = { "0", "1" })
    private Integer status;

    /**
     * 排序
     */
    @Schema(description = "排序", example = "1")
    private Integer sortOrder;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Schema(description = "备注", example = "用户管理菜单")
    private String remark;
}
