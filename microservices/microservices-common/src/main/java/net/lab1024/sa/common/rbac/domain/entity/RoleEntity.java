package net.lab1024.sa.common.rbac.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * RBAC角色实体
 * <p>
 * 对应数据库表：t_role
 * 严格遵循CLAUDE.md规范:
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_role")
public class RoleEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long roleId;

    private String roleCode;
    private String roleName;
    private String roleDesc;
    private Integer dataScope; // 1-全部 2-自定义 3-本部门 4-本部门及子部门 5-仅本人
    private Integer status; // 1-启用 2-禁用
    private Integer isSystem; // 0-否 1-是
    private Integer sortOrder;
    private String remark;
}
