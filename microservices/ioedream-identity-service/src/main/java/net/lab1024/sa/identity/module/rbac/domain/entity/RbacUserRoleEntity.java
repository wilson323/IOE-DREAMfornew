package net.lab1024.sa.identity.module.rbac.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * RBAC用户角色关联实体类
 * 表名: t_rbac_user_role
 *
 * @author SmartAdmin Team
 * @date 2025/11/29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_rbac_user_role")
public class RbacUserRoleEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 生效时间
     */
    private LocalDateTime effectiveTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 状态(0-禁用,1-启用)
     */
    private Integer status;

    /**
     * 授权类型(MANUAL|AUTO)
     */
    private String grantType;

    /**
     * 授权来源
     */
    private String grantSource;
}