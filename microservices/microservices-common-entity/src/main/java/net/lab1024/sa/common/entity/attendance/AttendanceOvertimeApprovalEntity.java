package net.lab1024.sa.common.entity.attendance;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 加班审批记录实体类
 * <p>
 * 存储加班申请的审批记录，支持多级审批
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_attendance_overtime_approval")
public class AttendanceOvertimeApprovalEntity extends BaseEntity {

    /**
     * 审批ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long approvalId;

    /**
     * 关联申请ID
     */
    @TableField("apply_id")
    private Long applyId;

    /**
     * 申请编号
     */
    @TableField("apply_no")
    private String applyNo;

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
     * 审批人岗位ID
     */
    @TableField("approver_position_id")
    private Long approverPositionId;

    /**
     * 审批人岗位名称
     */
    @TableField("approver_position_name")
    private String approverPositionName;

    /**
     * 审批层级
     */
    @TableField("approval_level")
    private Integer approvalLevel;

    /**
     * 审批操作：APPROVE-批准 REJECT-驳回 CANCEL-撤销
     */
    @TableField("approval_action")
    private String approvalAction;

    /**
     * 审批意见
     */
    @TableField("approval_comment")
    private String approvalComment;

    /**
     * 审批时间
     */
    @TableField("approval_time")
    private LocalDateTime approvalTime;

    /**
     * 审批附件URL
     */
    @TableField("attachment_url")
    private String attachmentUrl;
}
