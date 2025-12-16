package net.lab1024.sa.common.system.area.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 系统区域实体
 * <p>
 * 兼容 Smart-Admin 管理后台的 /system/area 接口契约。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_area")
public class SystemAreaEntity extends BaseEntity {

    @TableId(value = "area_id", type = IdType.AUTO)
    private Long areaId;

    @TableField("area_name")
    private String areaName;

    @TableField("area_code")
    private String areaCode;

    @TableField("area_type")
    private String areaType;

    @TableField("parent_id")
    private Long parentId;

    @TableField("level")
    private Integer level;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("manager_id")
    private Long managerId;

    @TableField("capacity")
    private Integer capacity;

    @TableField("description")
    private String description;

    @TableField("status")
    private Integer status;
}

