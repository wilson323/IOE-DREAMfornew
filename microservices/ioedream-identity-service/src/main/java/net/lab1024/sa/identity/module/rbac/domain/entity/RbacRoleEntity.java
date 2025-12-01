package net.lab1024.sa.identity.module.rbac.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * RBAC角色实体类
 * 表名: t_rbac_role
 *
 * @author SmartAdmin Team
 * @date 2025/11/29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_rbac_role")
public class RbacRoleEntity extends BaseEntity {

    /**
     * 角色ID
     */
    @TableId(value = "role_id", type = IdType.AUTO)
    private Long roleId;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色类型(SYSTEM|BUSINESS|CUSTOM)
     */
    private String roleType;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 是否系统角色
     */
    private Integer isSystem;

    /**
     * 状态(0-禁用,1-启用)
     */
    private Integer status;
}