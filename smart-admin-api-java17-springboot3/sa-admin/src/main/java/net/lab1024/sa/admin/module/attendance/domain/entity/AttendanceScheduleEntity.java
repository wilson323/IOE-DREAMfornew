package net.lab1024.sa.admin.module.attendance.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import net.lab1024.sa.base.common.entity.BaseEntity;import java.time.LocalDateTime;import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import net.lab1024.sa.base.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 排班管理实体
 *
 * 严格遵循repowiki规范:
 * - 继承BaseEntity，包含审计字段
 * - 使用jakarta包，避免javax包
 * - 使用Lombok简化代码
 * - 字段命名规范：下划线分隔
 * - 完整的排班业务逻辑
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("t_attendance_schedule")
public class AttendanceScheduleEntity extends BaseEntity {

    /**
     * 排班ID
     */
    @TableId(value = "schedule_id", type = IdType.AUTO)
    private Long scheduleId;

    /**
     * 员工ID
     */
    @TableField("employee_id")
    private Long employeeId;

    /**
     * 排班日期
     */
    @TableField("schedule_date")
    private LocalDate scheduleDate;

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
     * 工作开始时间
     */
    @TableField("work_start_time")
    private LocalTime workStartTime;

    /**
     * 工作结束时间
     */
    @TableField("work_end_time")
    private LocalTime workEndTime;

    /**
     * 休息开始时间
     */
    @TableField("break_start_time")
    private LocalTime breakStartTime;

    /**
     * 休息结束时间
     */
    @TableField("break_end_time")
    private LocalTime breakEndTime;

    /**
     * 工作时长(小时)
     */
    @TableField("work_hours")
    private BigDecimal workHours;

    /**
     * 休息时长(小时)
     */
    @TableField("break_hours")
    private BigDecimal breakHours;

    /**
     * 是否节假日
     * 0-否, 1-是
     */
    @TableField("is_holiday")
    private Integer isHoliday;

    /**
     * 是否加班日
     * 0-否, 1-是
     */
    @TableField("is_overtime_day")
    private Integer isOvertimeDay;

    /**
     * 是否周末
     * 0-否, 1-是
     */
    @TableField("is_weekend")
    private Integer isWeekend;

    /**
     * 排班类型
     * NORMAL-正常, OVERTIME-加班, HOLIDAY-节假日, LEAVE-请假
     */
    @TableField("schedule_type")
    private String scheduleType;

    /**
     * 加班倍率
     */
    @TableField("overtime_rate")
    private BigDecimal overtimeRate;

    /**
     * 工作地点ID
     */
    @TableField("location_id")
    private Long locationId;

    /**
     * 工作地点名称
     */
    @TableField("location_name")
    private String locationName;

    /**
     * 主管ID
     */
    @TableField("supervisor_id")
    private Long supervisorId;

    /**
     * 主管姓名
     */
    @TableField("supervisor_name")
    private String supervisorName;

    /**
     * 备注
     */
    @TableField("remarks")
    private String remarks;

    /**
     * 检查是否为工作日
     *
     * @return 是否为工作日
     */
    public boolean isWorkDay() {
        return "NORMAL".equals(scheduleType) || "OVERTIME".equals(scheduleType);
    }

    /**
     * 检查是否为加班日
     *
     * @return 是否为加班日
     */
    public boolean isOvertimeDay() {
        return "OVERTIME".equals(scheduleType) || isOvertimeDay != null && isOvertimeDay == 1;
    }

    /**
     * 检查是否为节假日
     *
     * @return 是否为节假日
     */
    public boolean isHoliday() {
        return "HOLIDAY".equals(scheduleType) || isHoliday != null && isHoliday == 1;
    }

    /**
     * 检查是否为请假
     *
     * @return 是否为请假
     */
    public boolean isLeave() {
        return "LEAVE".equals(scheduleType);
    }

    /**
     * 检查是否为周末
     *
     * @return 是否为周末
     */
    public boolean isWeekend() {
        return isWeekend != null && isWeekend == 1;
    }

    /**
     * 计算实际工作时长（扣除休息时间）
     *
     * @return 实际工作时长(小时)
     */
    public BigDecimal calculateActualWorkHours() {
        if (workStartTime == null || workEndTime == null) {
            return BigDecimal.ZERO;
        }

        // 计算总时长
        long totalMinutes = java.time.Duration.between(workStartTime, workEndTime).toMinutes();
        BigDecimal totalHours = BigDecimal.valueOf(totalMinutes).divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP);

        // 扣除休息时间
        if (breakStartTime != null && breakEndTime != null) {
            long breakMinutes = java.time.Duration.between(breakStartTime, breakEndTime).toMinutes();
            BigDecimal breakHours = BigDecimal.valueOf(breakMinutes).divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP);
            totalHours = totalHours.subtract(breakHours);
        }

        return totalHours;
    }

    /**
     * 获取排班类型描述
     *
     * @return 排班类型描述
     */
    public String getScheduleTypeDescription() {
        if (scheduleType == null) {
            return "未知";
        }

        switch (scheduleType) {
            case "NORMAL":
                return "正常";
            case "OVERTIME":
                return "加班";
            case "HOLIDAY":
                return "节假日";
            case "LEAVE":
                return "请假";
            default:
                return scheduleType;
        }
    }

    /**
     * 检查排班是否有效
     *
     * @return 是否有效
     */
    public boolean isValidSchedule() {
        return workStartTime != null && workEndTime != null &&
               workHours != null && workHours.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 检查排班是否跨天
     *
     * @return 是否跨天
     */
    public boolean isCrossDay() {
        if (workStartTime != null && workEndTime != null) {
            return workEndTime.isBefore(workStartTime);
        }
        return false;
    }

    /**
     * 检查是否有休息时间
     *
     * @return 是否有休息时间
     */
    public boolean hasBreakTime() {
        return breakStartTime != null && breakEndTime != null &&
               breakEndTime.isAfter(breakStartTime);
    }

    /**
     * 获取工作时间段字符串
     *
     * @return 时间段字符串，格式: 09:00-18:00
     */
    public String getWorkTimeRange() {
        if (workStartTime == null || workEndTime == null) {
            return "";
        }
        return workStartTime + "-" + workEndTime;
    }

    /**
     * 获取休息时间段字符串
     *
     * @return 休息时间段字符串，格式: 12:00-13:00
     */
    public String getBreakTimeRange() {
        if (!hasBreakTime()) {
            return "无休息";
        }
        return breakStartTime + "-" + breakEndTime;
    }

    /**
     * 比较排班日期
     *
     * @param other 另一个排班
     * @return 比较结果
     */
    public int compareDate(AttendanceScheduleEntity other) {
        return this.scheduleDate.compareTo(other.getScheduleDate());
    }

    // ==================== 手动添加的getter/setter方法（解决Lombok编译问题） ====================

    public LocalDate getScheduleDate() {
        return scheduleDate;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public LocalTime getWorkStartTime() {
        return workStartTime;
    }

    public LocalTime getWorkEndTime() {
        return workEndTime;
    }

    public LocalTime getBreakStartTime() {
        return breakStartTime;
    }

    public LocalTime getBreakEndTime() {
        return breakEndTime;
    }

    public BigDecimal getWorkHours() {
        return workHours;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public String getShiftName() {
        return shiftName;
    }

    public void setScheduleDate(LocalDate scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public void setWorkStartTime(LocalTime workStartTime) {
        this.workStartTime = workStartTime;
    }

    public void setWorkEndTime(LocalTime workEndTime) {
        this.workEndTime = workEndTime;
    }

    public void setBreakStartTime(LocalTime breakStartTime) {
        this.breakStartTime = breakStartTime;
    }

    public void setBreakEndTime(LocalTime breakEndTime) {
        this.breakEndTime = breakEndTime;
    }

    public void setWorkHours(BigDecimal workHours) {
        this.workHours = workHours;
    }

    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }

    public void setShiftName(String shiftName) {
        this.shiftName = shiftName;
    }

    // ==================== 兼容性getter方法 ====================

    /**
     * 获取开始时间（兼容性方法）
     * 返回工作开始时间
     *
     * @return 开始时间
     */
    public LocalTime getStartTime() {
        return this.workStartTime;
    }

    /**
     * 获取结束时间（兼容性方法）
     * 返回工作结束时间
     *
     * @return 结束时间
     */
    public LocalTime getEndTime() {
        return this.workEndTime;
    }

    /**
     * 设置开始时间（兼容性方法）
     *
     * @param startTime 开始时间
     */
    public void setStartTime(LocalTime startTime) {
        this.workStartTime = startTime;
    }

    /**
     * 设置结束时间（兼容性方法）
     *
     * @param endTime 结束时间
     */
    public void setEndTime(LocalTime endTime) {
        this.workEndTime = endTime;
    }
}