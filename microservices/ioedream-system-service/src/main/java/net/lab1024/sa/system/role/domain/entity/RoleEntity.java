package net.lab1024.sa.system.role.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 角色实体
 *
 * @author IOE-DREAM Team
 * @date 2025/11/29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_rbac_role")
public class RoleEntity extends BaseEntity {

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
