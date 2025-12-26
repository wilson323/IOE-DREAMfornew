package net.lab1024.sa.attendance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 加班申请实体类
 * <p>
 * 存储员工加班申请信息，包含申请、审批、补偿等完整流程
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_attendance_overtime_apply")
public class AttendanceOvertimeApplyEntity extends BaseEntity {

    /**
     * 申请ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long applyId;

    /**
     * 申请编号（业务主键）
     * 格式：OT-YYYYMMDD-001
     */
    @TableField("apply_no")
    private String applyNo;

    /**
     * 申请人ID
     */
    @TableField("applicant_id")
    private Long applicantId;

    /**
     * 申请人姓名
     */
    @TableField("applicant_name")
    private String applicantName;

    /**
     * 部门ID
     */
    @TableField("department_id")
    private Long departmentId;

    /**
     * 部门名称
     */
    @TableField("department_name")
    private String departmentName;

    /**
     * 岗位ID
     */
    @TableField("position_id")
    private Long positionId;

    /**
     * 岗位名称
     */
    @TableField("position_name")
    private String positionName;

    /**
     * 加班类型：WORKDAY-工作日 OVERTIME-休息日 HOLIDAY-法定节假日
     */
    @TableField("overtime_type")
    private String overtimeType;

    /**
     * 加班日期
     */
    @TableField("overtime_date")
    private LocalDate overtimeDate;

    /**
     * 加班开始时间
     */
    @TableField("start_time")
    private LocalTime startTime;

    /**
     * 加班结束时间
     */
    @TableField("end_time")
    private LocalTime endTime;

    /**
     * 计划加班时长（小时）
     */
    @TableField("planned_hours")
    private BigDecimal plannedHours;

    /**
     * 实际加班时长（小时）
     */
    @TableField("actual_hours")
    private BigDecimal actualHours;

    /**
     * 加班原因
     */
    @TableField("overtime_reason")
    private String overtimeReason;

    /**
     * 加班详细说明
     */
    @TableField("overtime_description")
    private String overtimeDescription;

    /**
     * 补偿方式：PAY-支付加班费 LEAVE-调休
     */
    @TableField("compensation_type")
    private String compensationType;

    /**
     * 调休日期（补偿方式为调休时）
     */
    @TableField("leave_date")
    private LocalDate leaveDate;

    /**
     * 申请状态：PENDING-待审批 APPROVED-已批准 REJECTED-已驳回 CANCELLED-已撤销
     */
    @TableField("apply_status")
    private String applyStatus;

    /**
     * 当前审批层级
     */
    @TableField("approval_level")
    private Integer approvalLevel;

    /**
     * 当前审批人ID
     */
    @TableField("approver_id")
    private Long approverId;

    /**
     * 当前审批人姓名
     */
    @TableField("approver_name")
    private String approverName;

    /**
     * 最终审批人ID
     */
    @TableField("final_approver_id")
    private Long finalApproverId;

    /**
     * 最终审批人姓名
     */
    @TableField("final_approver_name")
    private String finalApproverName;

    /**
     * 最终审批时间
     */
    @TableField("final_approval_time")
    private java.time.LocalDateTime finalApprovalTime;

    /**
     * 最终审批意见
     */
    @TableField("final_approval_comment")
    private String finalApprovalComment;

    /**
     * 工作流实例ID
     */
    @TableField("workflow_instance_id")
    private Long workflowInstanceId;
}
