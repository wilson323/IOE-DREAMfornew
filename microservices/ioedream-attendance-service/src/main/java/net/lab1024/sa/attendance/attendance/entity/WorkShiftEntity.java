package net.lab1024.sa.attendance.attendance.entity;

import java.time.LocalTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 工作班次实体类
 * <p>
 * 考勤班次管理实体，支持多种班次类型和灵活排班
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_attendance_work_shift")
public class WorkShiftEntity extends BaseEntity {

    /**
     * 班次ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long shiftId;

    /**
     * 班次编码
     */
    @TableField("shift_code")
    private String shiftCode;

    /**
     * 班次名称
     */
    @TableField("shift_name")
    private String shiftName;

    /**
     * 班次类型：1-固定班次 2-弹性班次 3-轮班班次 4-临时班次
     */
    @TableField("shift_type")
    private Integer shiftType;

    /**
     * 工作日：1-周一 2-周二 3-周三 4-周四 5-周五 6-周六 7-周日（JSON数组）
     */
    @TableField("work_days")
    private String workDays;

    /**
     * 上班时间
     */
    @TableField("work_start_time")
    private LocalTime workStartTime;

    /**
     * 下班时间
     */
    @TableField("work_end_time")
    private LocalTime workEndTime;

    /**
     * 工作时长（分钟）
     */
    @TableField("work_duration")
    private Integer workDuration;

    /**
     * 午休开始时间
     */
    @TableField("lunch_start_time")
    private LocalTime lunchStartTime;

    /**
     * 午休结束时间
     */
    @TableField("lunch_end_time")
    private LocalTime lunchEndTime;

    /**
     * 午休时长（分钟）
     */
    @TableField("lunch_duration")
    private Integer lunchDuration;

    /**
     * 休息时间（分钟）
     */
    @TableField("break_time")
    private Integer breakTime;

    /**
     * 弹性上班时间（最早）
     */
    @TableField("flex_start_earliest")
    private LocalTime flexStartEarliest;

    /**
     * 弹性上班时间（最晚）
     */
    @TableField("flex_start_latest")
    private LocalTime flexStartLatest;

    /**
     * 弹性下班时间（最早）
     */
    @TableField("flex_end_earliest")
    private LocalTime flexEndEarliest;

    /**
     * 弹性下班时间（最晚）
     */
    @TableField("flex_end_latest")
    private LocalTime flexEndLatest;

    /**
     * 迟到宽限时间（分钟）
     */
    @TableField("late_tolerance")
    private Integer lateTolerance;

    /**
     * 早退宽限时间（分钟）
     */
    @TableField("early_tolerance")
    private Integer earlyTolerance;

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
     * 夜班补贴
     */
    @TableField("night_shift_allowance")
    private java.math.BigDecimal nightShiftAllowance;

    /**
     * 节假日三倍薪资：0-否 1-是
     */
    @TableField("holiday_triple_pay")
    private Integer holidayTriplePay;

    /**
     * 班次颜色（用于界面显示）
     */
    @TableField("shift_color")
    private String shiftColor;

    /**
     * 班次描述
     */
    @TableField("description")
    private String description;

    /**
     * 班次状态：1-启用 0-禁用
     */
    @TableField("shift_status")
    private Integer shiftStatus;

    /**
     * 创建人ID
     */
    @TableField("create_user_id")
    private Long createUserId;

    /**
     * 更新人ID
     */
    @TableField("update_user_id")
    private Long updateUserId;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extended_attributes")
    private String extendedAttributes;
}
