package net.lab1024.sa.attendance.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 异常审批表实体
 *
 * 异常审批管理的核心实体，记录完整的审批过程和结果
 *
 * @author IOE-DREAM Team
 * @since 2025-11-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_exception_approvals")
public class ExceptionApprovalsEntity extends BaseEntity {

    /**
     * 审批ID
     */
    private Long approvalId;

    /**
     * 申请ID
     */
    @TableField("`application_id`")
    private Long applicationId;

    /**
     * 审批人ID
     */
    @TableField("`approver_id`")
    private Long approverId;

    /**
     * 审批人姓名
     */
    @TableField("`approver_name`")
    private String approverName;

    /**
     * 审批层级
     */
    @TableField("`approval_level`")
    private Integer approvalLevel;

    /**
     * 审批结果: APPROVED-通过 REJECTED-拒绝 RETURNED-退回
     */
    @TableField("`approval_result`")
    private String approvalResult;

    /**
     * 审批意见
     */
    @TableField("`approval_comment`")
    private String approvalComment;

    /**
     * 审批时间
     */
    @TableField("`approval_time`")
    private LocalDateTime approvalTime;

    /**
     * 下一审批人ID
     */
    @TableField("`next_approver_id`")
    private Long nextApproverId;

    /**
     * 是否最终审批
     */
    @TableField("`is_final_approval`")
    private Boolean isFinalApproval;

    /**
     * 审批附件 JSON格式
     * [{"fileName":"审批附件.pdf","fileUrl":"/uploads/approval_attach.pdf"}]
     */
    @TableField("`attachments`")
    private String attachments;

    /**
     * 代理人信息 JSON格式
     * {"originalApproverId":1001,"originalApproverName":"张三","delegateReason":"出差"}
     */
    @TableField("`delegate_approvers`")
    private String delegateApprovers;

    /**
     * 申请编码（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String applicationCode;

    /**
     * 申请类型（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String exceptionType;

    /**
     * 申请员工姓名（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String employeeName;

    /**
     * 申请员工编号（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String employeeCode;

    /**
     * 审批人职位（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String approverPosition;

    /**
     * 审批结果描述（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String approvalResultDesc;

    /**
     * 下一审批人姓名（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String nextApproverName;

    /**
     * 审批时长（分钟，查询时填充，不存储）
     */
    @TableField(exist = false)
    private Long approvalDurationMinutes;

    /**
     * 是否超时审批（查询时填充，不存储）
     */
    @TableField(exist = false)
    private Boolean isTimeoutApproval;

    /**
     * 格式化审批时间（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String formattedApprovalTime;
}
