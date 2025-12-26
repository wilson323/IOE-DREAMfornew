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
 * 考勤异常记录实体类
 * <p>
 * 用于记录员工考勤异常情况：
 * - 缺卡（忘打卡）
 * - 迟到
 * - 早退
 * - 旷工
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_attendance_anomaly")
public class AttendanceAnomalyEntity extends BaseEntity {

    /**
     * 异常记录ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long anomalyId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户姓名
     */
    @TableField("user_name")
    private String userName;

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
     * 考勤日期
     */
    @TableField("attendance_date")
    private LocalDate attendanceDate;

    /**
     * 异常类型
     * <p>
     * MISSING_CARD - 缺卡（忘打卡）
     * LATE - 迟到
     * EARLY - 早退
     * ABSENT - 旷工
     * </p>
     */
    @TableField("anomaly_type")
    private String anomalyType;

    /**
     * 异常程度
     * <p>
     * NORMAL - 一般异常
     * SERIOUS - 严重异常
     * CRITICAL - 重大异常
     * </p>
     */
    @TableField("severity_level")
    private String severityLevel;

    /**
     * 应打卡时间
     */
    @TableField("expected_punch_time")
    private LocalDateTime expectedPunchTime;

    /**
     * 实际打卡时间（缺卡时为空）
     */
    @TableField("actual_punch_time")
    private LocalDateTime actualPunchTime;

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
     * 异常时长（分钟）
     * <p>
     * 迟到：迟到多少分钟
     * 早退：早退多少分钟
     * 旷工：旷工多少分钟
     * </p>
     */
    @TableField("anomaly_duration")
    private Integer anomalyDuration;

    /**
     * 异常原因
     * <p>
     * 系统自动检测的异常原因
     * 例如：迟到超过15分钟、未在规定时间打卡
     * </p>
     */
    @TableField("anomaly_reason")
    private String anomalyReason;

    /**
     * 异常状态
     * <p>
     * PENDING - 待处理
     * APPLIED - 已申请（员工已提交申请）
     * APPROVED - 已批准（管理员已审批通过）
     * REJECTED - 已驳回（管理员已驳回）
     * IGNORED - 已忽略（管理员标记为忽略）
     * </p>
     */
    @TableField("anomaly_status")
    private String anomalyStatus;

    /**
     * 申请ID（关联到异常申请表）
     */
    @TableField("apply_id")
    private Long applyId;

    /**
     * 处理人ID（审批人）
     */
    @TableField("handler_id")
    private Long handlerId;

    /**
     * 处理人姓名
     */
    @TableField("handler_name")
    private String handlerName;

    /**
     * 处理时间
     */
    @TableField("handle_time")
    private LocalDateTime handleTime;

    /**
     * 处理意见
     */
    @TableField("handle_comment")
    private String handleComment;

    /**
     * 关联的考勤记录ID
     */
    @TableField("attendance_record_id")
    private Long attendanceRecordId;

    /**
     * 是否已修正
     * <p>
     * 0-未修正
     * 1-已修正（管理员手动修正或补卡成功）
     * </p>
     */
    @TableField("is_corrected")
    private Integer isCorrected;

    /**
     * 修正时间
     */
    @TableField("corrected_time")
    private LocalDateTime correctedTime;

    /**
     * 修正后打卡时间
     */
    @TableField("corrected_punch_time")
    private LocalDateTime correctedPunchTime;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}
