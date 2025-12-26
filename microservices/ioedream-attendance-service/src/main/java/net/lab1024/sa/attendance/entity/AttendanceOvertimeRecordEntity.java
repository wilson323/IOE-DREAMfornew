package net.lab1024.sa.attendance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 加班记录实体类
 * <p>
 * 存储实际加班记录，可由申请生成或系统自动记录
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_attendance_overtime_record")
public class AttendanceOvertimeRecordEntity extends BaseEntity {

    /**
     * 记录ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long recordId;

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
     * 员工ID
     */
    @TableField("employee_id")
    private Long employeeId;

    /**
     * 员工姓名
     */
    @TableField("employee_name")
    private String employeeName;

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
     * 加班日期
     */
    @TableField("overtime_date")
    private LocalDate overtimeDate;

    /**
     * 实际加班开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 实际加班结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 实际加班时长（小时）
     */
    @TableField("overtime_hours")
    private BigDecimal overtimeHours;

    /**
     * 加班类型：WORKDAY-工作日 OVERTIME-休息日 HOLIDAY-法定节假日
     */
    @TableField("overtime_type")
    private String overtimeType;

    /**
     * 是否夜班：0-否 1-是
     */
    @TableField("is_night_shift")
    private Integer isNightShift;

    /**
     * 是否周末：0-否 1-是
     */
    @TableField("is_weekend")
    private Integer isWeekend;

    /**
     * 是否法定节假日：0-否 1-是
     */
    @TableField("is_holiday")
    private Integer isHoliday;

    /**
     * 加班倍率：1.0-1.5倍 2.0-3.0倍
     */
    @TableField("overtime_multiplier")
    private BigDecimal overtimeMultiplier;

    /**
     * 正常加班时长（小时）
     */
    @TableField("normal_hours")
    private BigDecimal normalHours;

    /**
     * 折算后时长（小时）
     */
    @TableField("multiplied_hours")
    private BigDecimal multipliedHours;

    /**
     * 补偿方式：PAY-支付加班费 LEAVE-调休
     */
    @TableField("compensation_type")
    private String compensationType;

    /**
     * 补偿状态：PENDING-待补偿 COMPLETED-已补偿
     */
    @TableField("compensation_status")
    private String compensationStatus;

    /**
     * 加班计算规则ID
     */
    @TableField("calculation_rule_id")
    private Long calculationRuleId;

    /**
     * 计算依据：ACTUAL-实际打卡 SCHEDULE-排班时间
     */
    @TableField("calculation_base")
    private String calculationBase;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 详细说明
     */
    @TableField("description")
    private String description;
}
