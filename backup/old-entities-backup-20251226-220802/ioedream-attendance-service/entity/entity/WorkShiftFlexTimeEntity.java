package net.lab1024.sa.attendance.entity;

import java.time.LocalTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 工作班次弹性时间配置实体类
 * <p>
 * 考勤班次的弹性时间配置，包含弹性上下班时间、宽限时间等
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_attendance_work_shift_flex_time")
public class WorkShiftFlexTimeEntity extends BaseEntity {

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
     * 弹性时间模式：1-标准弹性 2-完全弹性 3-混合弹性
     */
    @TableField("flex_mode")
    private Integer flexMode;

    /**
     * 最少工作时长（分钟）
     */
    @TableField("min_work_duration")
    private Integer minWorkDuration;

    /**
     * 最多工作时长（分钟）
     */
    @TableField("max_work_duration")
    private Integer maxWorkDuration;

    /**
     * 是否启用跨天弹性：0-否 1-是
     */
    @TableField("cross_day_flex")
    private Integer crossDayFlex;

    /**
     * 弹性时间配置状态：1-启用 0-禁用
     */
    @TableField("flex_status")
    private Integer flexStatus;
}
