package net.lab1024.sa.common.menu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * 菜单实体类
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_menu")
@Schema(description = "菜单实体")
public class MenuEntity extends BaseEntity {

    /**
     * 菜单ID（主键）
     * <p>
     * 统一使用id作为主键字段名，通过@TableId的value属性映射到数据库列menu_id
     * 符合实体类主键命名规范：统一使用id，避免方法引用错误
     * </p>
     */
    @TableId(value = "menu_id", type = IdType.AUTO)
    @Schema(description = "菜单ID", example = "1")
    private Long id;

    /**
     * 菜单名称
     */
    @TableField("menu_name")
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 100, message = "菜单名称长度不能超过100个字符")
    @Schema(description = "菜单名称", example = "用户管理")
    private String menuName;

    /**
     * 菜单类型：1-目录 2-菜单 3-功能
     */
    @TableField("menu_type")
    @NotNull(message = "菜单类型不能为空")
    @Min(value = 1, message = "菜单类型值无效")
    @Max(value = 3, message = "菜单类型值无效")
    @Schema(description = "菜单类型", example = "2", allowableValues = {"1", "2", "3"})
    private Integer menuType;

    /**
     * 父菜单ID
     */
    @TableField("parent_id")
    @Schema(description = "父菜单ID", example = "0")
    private Long parentId;

    /**
     * 菜单排序
     */
    @TableField("sort_order")
    @Min(value = 0, message = "菜单排序不能小于0")
    @Schema(description = "菜单排序", example = "1")
    private Integer sortOrder;

    /**
     * 菜单路径
     */
    @TableField("menu_path")
    @Size(max = 200, message = "菜单路径长度不能超过200个字符")
    @Schema(description = "菜单路径", example = "/system/user")
    private String menuPath;

    /**
     * 组件路径
     */
    @TableField("component_path")
    @Size(max = 200, message = "组件路径长度不能超过200个字符")
    @Schema(description = "组件路径", example = "system/user/index")
    private String componentPath;

    /**
     * 菜单图标
     */
    @TableField("menu_icon")
    @Size(max = 100, message = "菜单图标长度不能超过100个字符")
    @Schema(description = "菜单图标", example = "UserOutlined")
    private String menuIcon;

    /**
     * 权限标识
     */
    @TableField("permission")
    @Size(max = 200, message = "权限标识长度不能超过200个字符")
    @Schema(description = "权限标识", example = "system:user:list")
    private String permission;

    /**
     * 路由参数
     */
    @TableField("route_params")
    @Schema(description = "路由参数（JSON格式）", example = "{\"id\": \"123\"}")
    private String routeParams;

    /**
     * 是否外链：0-否 1-是
     */
    @TableField("is_external")
    @Min(value = 0, message = "是否外链值无效")
    @Max(value = 1, message = "是否外链值无效")
    @Schema(description = "是否外链", example = "0")
    private Integer isExternal;

    /**
     * 是否缓存：0-否 1-是
     */
    @TableField("is_cache")
    @Min(value = 0, message = "是否缓存值无效")
    @Max(value = 1, message = "是否缓存值无效")
    @Schema(description = "是否缓存", example = "1")
    private Integer isCache;

    /**
     * 是否显示：0-否 1-是
     */
    @TableField("is_visible")
    @Min(value = 0, message = "是否显示值无效")
    @Max(value = 1, message = "是否显示值无效")
    @Schema(description = "是否显示", example = "1")
    private Integer isVisible;

    /**
     * 是否禁用：0-否 1-是
     */
    @TableField("is_disabled")
    @Min(value = 0, message = "是否禁用值无效")
    @Max(value = 1, message = "是否禁用值无效")
    @Schema(description = "是否禁用", example = "0")
    private Integer isDisabled;

    /**
     * 备注
     */
    @TableField("remark")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Schema(description = "备注", example = "系统菜单")
    private String remark;

    /**
     * 菜单等级：根据层级自动计算
     */
    @TableField("menu_level")
    @Schema(description = "菜单等级", example = "1")
    private Integer menuLevel;

    /**
     * 菜单编码：系统唯一编码
     */
    @TableField("menu_code")
    @Size(max = 50, message = "菜单编码长度不能超过50个字符")
    @Schema(description = "菜单编码", example = "SYSTEM_USER_MANAGE")
    private String menuCode;

    /**
     * 业务模块：所属业务模块
     */
    @TableField("module")
    @Size(max = 50, message = "业务模块长度不能超过50个字符")
    @Schema(description = "业务模块", example = "system")
    private String module;

    /**
     * 子菜单列表（非数据库字段，仅用于树形结构展示）
     */
    @TableField(exist = false)
    @Schema(description = "子菜单列表")
    private List<MenuEntity> children;
}
