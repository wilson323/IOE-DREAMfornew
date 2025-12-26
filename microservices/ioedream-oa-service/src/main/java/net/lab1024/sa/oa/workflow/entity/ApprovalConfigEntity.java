package net.lab1024.sa.oa.workflow.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 审批配置实体
 * <p>
 * 用于管理审批配置，支持自定义审批类型和流程配置
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
@TableName("t_oa_approval_config")
public class ApprovalConfigEntity extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("business_type")
    private String businessType;

    @TableField("business_type_name")
    private String businessTypeName;

    @TableField("module")
    private String module;

    @TableField("definition_id")
    private Long definitionId;

    @TableField("process_key")
    private String processKey;

    @TableField("approval_rules")
    private String approvalRules;

    @TableField("post_approval_handler")
    private String postApprovalHandler;

    @TableField("timeout_config")
    private String timeoutConfig;

    @TableField("notification_config")
    private String notificationConfig;

    @TableField("applicable_scope")
    private String applicableScope;

    @TableField("status")
    private String status;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("remark")
    private String remark;

    @TableField("effective_time")
    private LocalDateTime effectiveTime;

    @TableField("expire_time")
    private LocalDateTime expireTime;
}

