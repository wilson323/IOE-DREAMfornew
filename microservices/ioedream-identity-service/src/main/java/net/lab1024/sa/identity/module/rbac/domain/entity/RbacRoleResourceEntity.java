package net.lab1024.sa.identity.module.rbac.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * RBAC角色资源关联实体类
 * 表名: t_rbac_role_resource
 *
 * @author SmartAdmin Team
 * @date 2025/11/29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_rbac_role_resource")
public class RbacRoleResourceEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 资源ID
     */
    private Long resourceId;

    /**
     * 操作动作(READ|WRITE|DELETE|APPROVE|*)
     */
    private String action;

    /**
     * RAC条件(JSON格式)
     */
    private String conditionJson;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 状态(0-禁用,1-启用)
     */
    private Integer status;
}