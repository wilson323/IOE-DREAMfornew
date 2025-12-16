package net.lab1024.sa.oa.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 审批模板实体
 * <p>
 * 用于管理审批模板，支持快速创建相似的审批配置
 * 比钉钉更完善的模板管理功能
 * 严格遵循CLAUDE.md规范：
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
@TableName("t_common_approval_template")
public class ApprovalTemplateEntity extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("template_name")
    private String templateName;

    @TableField("template_code")
    private String templateCode;

    @TableField("category")
    private String category;

    @TableField("description")
    private String description;

    @TableField("icon")
    private String icon;

    @TableField("template_config")
    private String templateConfig;

    @TableField("form_design")
    private String formDesign;

    @TableField("usage_count")
    private Integer usageCount;

    @TableField("is_public")
    private Integer isPublic;

    @TableField("status")
    private String status;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("remark")
    private String remark;
}




