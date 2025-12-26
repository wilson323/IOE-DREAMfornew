package net.lab1024.sa.common.organization.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 区域实体
 * <p>
 * 对应数据库表: t_common_area
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_common_area")
public class AreaEntity extends BaseEntity {

    @TableId(value = "area_id", type = IdType.AUTO)
    private Long areaId;

    @TableField("area_name")
    private String areaName;

    @TableField("area_code")
    private String areaCode;

    @TableField("area_type")
    private String areaType; // 区域类型：CAMPUS-园区 BUILDING-建筑 FLOOR-楼层 ROOM-房间

    @TableField("parent_id")
    private Long parentId;

    @TableField("level")
    private Integer level; // 层级

    @TableField("sort_order")
    private Integer sortOrder; // 排序

    @TableField("manager_id")
    private Long managerId; // 管理员ID

    @TableField("capacity")
    private Integer capacity; // 容量

    @TableField("description")
    private String description; // 描述

    @TableField("status")
    private Integer status; // 状态：1-正常 2-禁用

    /**
     * 子区域列表（用于树结构构建，不映射到数据库）
     */
    @TableField(exist = false)
    private List<AreaEntity> children;

    /**
     * 获取区域ID（兼容BaseEntity的getId方法）
     * <p>
     * 由于AreaEntity使用areaId作为主键，需要提供getId()方法以兼容BaseEntity
     * </p>
     *
     * @return 区域ID
     */
    public Long getId() {
        return areaId;
    }

    /**
     * 设置区域ID（兼容BaseEntity的setId方法）
     *
     * @param id 区域ID
     */
    public void setId(Long id) {
        this.areaId = id;
    }

    /**
     * 获取子区域列表（如果为null则初始化为空列表）
     *
     * @return 子区域列表
     */
    public List<AreaEntity> getChildren() {
        if (children == null) {
            children = new ArrayList<>();
        }
        return children;
    }

    /**
     * 设置子区域列表
     *
     * @param children 子区域列表
     */
    public void setChildren(List<AreaEntity> children) {
        this.children = children;
    }
}

