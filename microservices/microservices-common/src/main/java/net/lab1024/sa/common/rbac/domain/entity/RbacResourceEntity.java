package net.lab1024.sa.common.rbac.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * RBAC资源实体
 * <p>
 * 表示系统中的资源（菜单、按钮、接口等）
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
@TableName("t_rbac_resource")
public class RbacResourceEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long resourceId;

    private String resourceCode;
    private String resourceName;
    private String resourceType;
    private String resourcePath;
    private Long parentId;
    private Integer sortOrder;
    private Integer status;
    private String remark;
}
