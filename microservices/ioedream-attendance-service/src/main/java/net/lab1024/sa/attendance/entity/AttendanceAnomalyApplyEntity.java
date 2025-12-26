package net.lab1024.sa.attendance.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 考勤异常申请实体类
 * <p>
 * 员工提交的异常申请：
 * - 补卡申请（缺卡申请）
 * - 迟到/早退说明
 * - 旷工申诉
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_attendance_anomaly_apply")
public class AttendanceAnomalyApplyEntity extends BaseEntity {

    /**
     * 申请ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long applyId;

    /**
     * 申请单号（自动生成，格式：APPLY-YYYYMMDD-序号）
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
     * 申请类型
     * <p>
     * SUPPLEMENT_CARD - 补卡申请
     * LATE_EXPLANATION - 迟到说明
     * EARLY_EXPLANATION - 早退说明
     * ABSENT_APPEAL - 旷工申诉
     * </p>
     */
    @TableField("apply_type")
    private String applyType;

    /**
     * 异常记录ID（关联到异常记录表）
     */
    @TableField("anomaly_id")
    private Long anomalyId;

    /**
     * 考勤日期
     */
    @TableField("attendance_date")
    private LocalDate attendanceDate;

    /**
     * 班次ID
     */
    @TableField("shift_id")
    private Long shiftId;

    /**
     * 班次名称
     */
    @TableField("shift_name")
    private String shiftName;

    /**
     * 打卡类型
     * <p>
     * CHECK_IN - 上班打卡
     * CHECK_OUT - 下班打卡
     * </p>
     */
    @TableField("punch_type")
    private String punchType;

    /**
     * 原打卡时间（补卡时为空）
     */
    @TableField("original_punch_time")
    private LocalDateTime originalPunchTime;

    /**
     * 申请打卡时间（补卡申请时填写）
     */
    @TableField("applied_punch_time")
    private LocalDateTime appliedPunchTime;

    /**
     * 申请原因
     */
    @TableField("apply_reason")
    private String applyReason;

    /**
     * 详细说明
     */
    @TableField("description")
    private String description;

    /**
     * 附件路径（证明材料）
     */
    @TableField("attachment_path")
    private String attachmentPath;

    /**
     * 申请状态
     * <p>
     * PENDING - 待审批
     * APPROVED - 已批准
     * REJECTED - 已驳回
     * CANCELLED - 已撤销
     * </p>
     */
    @TableField("apply_status")
    private String applyStatus;

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
    @TableField("approve_time")
    private LocalDateTime approveTime;

    /**
     * 审批意见
     */
    @TableField("approve_comment")
    private String approveComment;

    /**
     * 申请时间
     */
    @TableField("apply_time")
    private LocalDateTime applyTime;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}
