package net.lab1024.sa.base.module.support.rbac.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

/**
 * RBAC资源实体类
 * 表名: t_rbac_resource
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_rbac_resource")
public class RbacResourceEntity extends BaseEntity {

    /**
     * 资源ID
     */
    @TableId(value = "resource_id", type = IdType.AUTO)
    private Long resourceId;

    /**
     * 资源编码
     */
    private String resourceCode;

    /**
     * 资源名称
     */
    private String resourceName;

    /**
     * 资源类型(API|MENU|BUTTON|DATA)
     */
    private String resourceType;

    /**
     * 模块编码
     */
    private String moduleCode;

    /**
     * 资源描述
     */
    private String description;

    /**
     * 父资源ID
     */
    private Long parentId;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态(0-禁用,1-启用)
     */
    private Integer status;
}
