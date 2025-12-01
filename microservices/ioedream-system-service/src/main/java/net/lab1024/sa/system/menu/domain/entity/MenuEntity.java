package net.lab1024.sa.system.menu.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 菜单实体
 *
 * @author IOE-DREAM Team
 * @date 2025/11/29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_menu")
public class MenuEntity extends BaseEntity {

    /**
     * 菜单ID
     */
    @TableId(value = "menu_id", type = IdType.AUTO)
    private Long menuId;

    /**
     * 父菜单ID
     */
    private Long parentId;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 菜单编码
     */
    private String menuCode;

    /**
     * 菜单类型(1-目录 2-菜单 3-按钮)
     */
    private Integer menuType;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 菜单路径
     */
    private String path;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 权限标识
     */
    private String permission;

    /**
     * 路由参数
     */
    private String queryParam;

    /**
     * 是否缓存(0-否,1-是)
     */
    private Integer isCache;

    /**
     * 是否外链(0-否,1-是)
     */
    private Integer isFrame;

    /**
     * 是否显示(0-否,1-是)
     */
    private Integer visible;

    /**
     * 状态（0-禁用 1-启用）
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 备注
     */
    private String remark;
}
