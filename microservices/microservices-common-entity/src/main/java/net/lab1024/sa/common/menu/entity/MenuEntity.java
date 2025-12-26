package net.lab1024.sa.common.menu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.util.List;

/**
 * 菜单实体
 * <p>
 * 对应数据库表: t_menu
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_menu")
public class MenuEntity extends BaseEntity {

    @TableId(value = "menu_id", type = IdType.AUTO)
    private Long menuId;

    @TableField("menu_name")
    private String menuName;

    @TableField("menu_code")
    private String menuCode;

    @TableField("parent_id")
    private Long parentId;

    @TableField("menu_type")
    private Integer menuType;

    @TableField("path")
    private String path;

    @TableField("component")
    private String component;

    @TableField("icon")
    private String icon;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("visible")
    private Integer visible;

    @TableField("status")
    private Integer status;

    @TableField("permission")
    private String permission;

    @TableField("remark")
    private String remark;

    /**
     * 子菜单列表（用于构建菜单树，不映射到数据库）
     */
    @TableField(exist = false)
    private List<MenuEntity> children;

    /**
     * 是否可见（兼容字段，映射到visible字段）
     * <p>
     * 注意：这是一个计算属性，不映射到数据库
     * </p>
     */
    public Integer getIsVisible() {
        return visible;
    }

    /**
     * 是否禁用（兼容字段，根据status判断）
     * <p>
     * 注意：这是一个计算属性，不映射到数据库
     * </p>
     */
    public Integer getIsDisabled() {
        return status != null && status == 0 ? 1 : 0;
    }

    /**
     * 获取ID（兼容BaseEntity的getId方法）
     * <p>
     * 由于使用menuId作为主键，需要提供getId方法以兼容代码中的entity.getId()调用
     * </p>
     *
     * @return 菜单ID
     */
    public Long getId() {
        return menuId;
    }

    /**
     * 设置ID（兼容BaseEntity的setId方法）
     *
     * @param id 菜单ID
     */
    public void setId(Long id) {
        this.menuId = id;
    }
}

