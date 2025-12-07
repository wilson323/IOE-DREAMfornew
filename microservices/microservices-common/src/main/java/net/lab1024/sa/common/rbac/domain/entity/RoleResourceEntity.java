package net.lab1024.sa.common.rbac.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 角色资源实体
 * <p>
 * 表示角色和资源的关联关系（权限分配）
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
@TableName("t_role_resource")
public class RoleResourceEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long roleResourceId;

    private Long roleId;
    private Long resourceId;
    private Integer status;
}
