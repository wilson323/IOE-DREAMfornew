package net.lab1024.sa.common.audit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 配置变更审批实体
 * <p>
 * 从ConfigChangeAuditEntity中拆分出来的审批专门实体
 * 遵循Entity设计原则：单一职责、字段精简
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_config_change_approval")
public class ConfigChangeApprovalEntity extends BaseEntity {

    /**
     * 审批ID（主键）
     * <p>
     * 统一使用id作为主键字段名，通过@TableId的value属性映射到数据库列approval_id
     * 符合实体类主键命名规范：统一使用id，避免方法引用错误
     * </p>
     */
    @TableId(value = "approval_id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 关联的审计ID
     */
    @TableField("audit_id")
    private Long auditId;

    /**
     * 变更批次ID
     */
    @TableField("change_batch_id")
    private String changeBatchId;

    /**
     * 是否需要审批
     */
    @TableField("require_approval")
    private Integer requireApproval;

    /**
     * 审批人ID
     */
    @TableField("approver_id")
    private Long approverId;

    /**
     * 审批人姓名
     */
    @TableField("approver_name")
    private String approverName;

    /**
     * 审批时间
     */
    @TableField("approval_time")
    private LocalDateTime approvalTime;

    /**
     * 审批状态
     * PENDING-待审批, APPROVED-已审批, REJECTED-已拒绝, SKIPPED-已跳过
     */
    @TableField("approval_status")
    private String approvalStatus;

    /**
     * 审批意见
     */
    @TableField("approval_comment")
    private String approvalComment;

    /**
     * 审批优先级
     * LOW-低, MEDIUM-中, HIGH-高, URGENT-紧急
     */
    @TableField("approval_priority")
    private String approvalPriority;

    /**
     * 审批截止时间
     */
    @TableField("approval_deadline")
    private LocalDateTime approvalDeadline;

    /**
     * 自动审批规则
     */
    @TableField("auto_approval_rule")
    private String autoApprovalRule;

    /**
     * 代理审批人ID
     */
    @TableField("delegate_approver_id")
    private Long delegateApproverId;

    /**
     * 审批流程ID
     */
    @TableField("approval_workflow_id")
    private String approvalWorkflowId;
}
