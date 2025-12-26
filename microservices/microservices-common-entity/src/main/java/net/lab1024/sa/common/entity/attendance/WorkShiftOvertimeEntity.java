package net.lab1024.sa.common.entity.attendance;

import java.math.BigDecimal;
import java.time.LocalTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 工作班次加班配置实体类
 * <p>
 * 考勤班次的加班配置，包含加班计算方式、夜班补贴等
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_attendance_work_shift_overtime")
public class WorkShiftOvertimeEntity extends BaseEntity {

    /**
     * 配置ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long configId;

    /**
     * 关联班次ID
     */
    @TableField("shift_id")
    private Long shiftId;

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
     * 加班开始时间（超出此时间才算加班）
     */
    @TableField("overtime_start_threshold")
    private LocalTime overtimeStartThreshold;

    /**
     * 加班计算精度：1-分钟 2-5分钟 3-15分钟 4-30分钟
     */
    @TableField("overtime_precision")
    private Integer overtimePrecision;

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
     * 周末加班倍率
     */
    @TableField("weekend_multiplier")
    private BigDecimal weekendMultiplier;

    /**
     * 节假日加班倍率
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
     * 加班换休假率
     */
    @TableField("overtime_to_leave_rate")
    private BigDecimal overtimeToLeaveRate;

    /**
     * 加班配置状态：1-启用 0-禁用
     */
    @TableField("overtime_status")
    private Integer overtimeStatus;

    /**
     * 加班配置描述
     */
    @TableField("overtime_description")
    private String overtimeDescription;
}
