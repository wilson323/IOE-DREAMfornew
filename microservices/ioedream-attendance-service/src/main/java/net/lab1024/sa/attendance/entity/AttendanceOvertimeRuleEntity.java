package net.lab1024.sa.attendance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * 加班规则配置实体类
 * <p>
 * 存储加班计算规则和补偿规则
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_attendance_overtime_rule")
public class AttendanceOvertimeRuleEntity extends BaseEntity {

    /**
     * 规则ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long ruleId;

    /**
     * 规则编码
     */
    @TableField("rule_code")
    private String ruleCode;

    /**
     * 规则名称
     */
    @TableField("rule_name")
    private String ruleName;

    /**
     * 规则分类：CALCULATION-计算规则 COMPENSATION-补偿规则 LIMITATION-限制规则
     */
    @TableField("rule_category")
    private String ruleCategory;

    /**
     * 规则类型
     */
    @TableField("rule_type")
    private String ruleType;

    /**
     * 应用范围：GLOBAL-全局 DEPARTMENT-部门 SHIFT-班次 EMPLOYEE-员工
     */
    @TableField("apply_scope")
    private String applyScope;

    /**
     * 适用部门ID列表（JSON）
     */
    @TableField("department_ids")
    private String departmentIds;

    /**
     * 适用班次ID列表（JSON）
     */
    @TableField("shift_ids")
    private String shiftIds;

    /**
     * 适用员工ID列表（JSON）
     */
    @TableField("user_ids")
    private String userIds;

    /**
     * 加班计算方式：1-按小时 2-按半小时 3-按15分钟
     */
    @TableField("overtime_calculation")
    private Integer overtimeCalculation;

    /**
     * 最小加班时长（分钟）
     */
    @TableField("min_overtime_duration")
    private Integer minOvertimeDuration;

    /**
     * 加班计算精度：1-分钟 2-5分钟 3-15分钟 4-30分钟
     */
    @TableField("overtime_precision")
    private Integer overtimePrecision;

    /**
     * 工作日加班开始阈值（超过此时间才算加班）
     */
    @TableField("workday_start_threshold")
    private LocalTime workdayStartThreshold;

    /**
     * 工作日加班结束阈值（超过此时间才算加班）
     */
    @TableField("workday_end_threshold")
    private LocalTime workdayEndThreshold;

    /**
     * 休息日加班开始阈值
     */
    @TableField("restday_start_threshold")
    private LocalTime restdayStartThreshold;

    /**
     * 休息日加班结束阈值
     */
    @TableField("restday_end_threshold")
    private LocalTime restdayEndThreshold;

    /**
     * 夜班开始时间
     */
    @TableField("night_shift_start")
    private LocalTime nightShiftStart;

    /**
     * 夜班结束时间
     */
    @TableField("night_shift_end")
    private LocalTime nightShiftEnd;

    /**
     * 夜班补贴（每小时）
     */
    @TableField("night_shift_allowance")
    private BigDecimal nightShiftAllowance;

    /**
     * 工作日加班倍率
     */
    @TableField("workday_multiplier")
    private BigDecimal workdayMultiplier;

    /**
     * 周末加班倍率
     */
    @TableField("weekend_multiplier")
    private BigDecimal weekendMultiplier;

    /**
     * 法定节假日加班倍率
     */
    @TableField("holiday_multiplier")
    private BigDecimal holidayMultiplier;

    /**
     * 最大加班时长（每天，分钟）
     */
    @TableField("max_daily_overtime")
    private Integer maxDailyOvertime;

    /**
     * 最大加班时长（每月，小时）
     */
    @TableField("max_monthly_overtime")
    private Integer maxMonthlyOvertime;

    /**
     * 最大连续加班天数
     */
    @TableField("max_continuous_days")
    private Integer maxContinuousDays;

    /**
     * 加班换休假率
     */
    @TableField("overtime_to_leave_rate")
    private BigDecimal overtimeToLeaveRate;

    /**
     * 调休有效期（天数）
     */
    @TableField("leave_valid_days")
    private Integer leaveValidDays;

    /**
     * 默认补偿方式：PAY-支付加班费 LEAVE-调休
     */
    @TableField("default_compensation")
    private String defaultCompensation;

    /**
     * 是否需要审批：0-否 1-是
     */
    @TableField("approval_required")
    private Integer approvalRequired;

    /**
     * 审批工作流ID
     */
    @TableField("approval_workflow_id")
    private Long approvalWorkflowId;

    /**
     * 自动批准时长阈值（小时）
     */
    @TableField("auto_approve_hours")
    private BigDecimal autoApproveHours;

    /**
     * 规则状态：1-启用 0-禁用
     */
    @TableField("rule_status")
    private Integer ruleStatus;

    /**
     * 执行顺序（数字越小优先级越高）
     */
    @TableField("execution_order")
    private Integer executionOrder;

    /**
     * 规则优先级（数字越小优先级越高）
     */
    @TableField("rule_priority")
    private Integer rulePriority;

    /**
     * 生效开始时间
     */
    @TableField("effective_start_time")
    private LocalDateTime effectiveStartTime;

    /**
     * 生效结束时间
     */
    @TableField("effective_end_time")
    private LocalDateTime effectiveEndTime;

    /**
     * 生效日期（如：1,2,3,4,5表示工作日）
     */
    @TableField("effective_days")
    private String effectiveDays;

    /**
     * 规则描述
     */
    @TableField("description")
    private String description;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;
}
