package net.lab1024.sa.attendance.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 考勤汇总表实体类
 * <p>
 * 持久化个人考勤月度汇总数据
 * 从AttendanceRecord聚合生成
 * 严格遵循CLAUDE.md规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_attendance_summary")
public class AttendanceSummaryEntity extends BaseEntity {

    /**
     * 汇总ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long summaryId;

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
     * 汇总月份（格式：2024-01）
     */
    @TableField("summary_month")
    private String summaryMonth;

    /**
     * 应工作天数
     */
    @TableField("work_days")
    private Integer workDays;

    /**
     * 实际出勤天数
     */
    @TableField("actual_days")
    private Integer actualDays;

    /**
     * 旷工天数
     */
    @TableField("absent_days")
    private Integer absentDays;

    /**
     * 迟到次数
     */
    @TableField("late_count")
    private Integer lateCount;

    /**
     * 早退次数
     */
    @TableField("early_count")
    private Integer earlyCount;

    /**
     * 加班时长（小时）
     */
    @TableField("overtime_hours")
    private BigDecimal overtimeHours;

    /**
     * 周末加班时长（小时）
     */
    @TableField("weekend_overtime_hours")
    private BigDecimal weekendOvertimeHours;

    /**
     * 请假天数
     */
    @TableField("leave_days")
    private BigDecimal leaveDays;

    /**
     * 出勤率（0-1之间的小数，如0.95表示95%）
     */
    @TableField("attendance_rate")
    private BigDecimal attendanceRate;

    /**
     * 状态：0-删除，1-正常
     */
    @TableField("status")
    private Integer status;
}
