package net.lab1024.sa.common.rbac.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 用户角色实体
 * <p>
 * 表示用户和角色的关联关系
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
@TableName("t_user_role")
public class UserRoleEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long userRoleId;

    private Long userId;
    private Long roleId;
    private Integer status;
}
