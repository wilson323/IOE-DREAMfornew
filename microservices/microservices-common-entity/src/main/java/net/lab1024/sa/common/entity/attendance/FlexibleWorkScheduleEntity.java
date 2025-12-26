package net.lab1024.sa.common.entity.attendance;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * 弹性工作制配置实体类
 * <p>
 * 企业级弹性工作制配置管理
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-12-26
 */
@Data
@TableName("t_attendance_flexible_work_schedule")
public class FlexibleWorkScheduleEntity {

    /**
     * 配置ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long scheduleId;

    /**
     * 配置名称
     */
    @TableField("schedule_name")
    private String scheduleName;

    /**
     * 配置编码
     */
    @TableField("schedule_code")
    private String scheduleCode;

    /**
     * 弹性模式：STANDARD-标准弹性 FLEXIBLE-完全弹性 HYBRID-混合弹性
     */
    @TableField("flex_mode")
    private String flexMode;

    /**
     * 描述
     */
    @TableField("description")
    private String description;

    // ==================== 弹性上下班时间 ====================

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

    // ==================== 核心工作时间（标准弹性模式） ====================

    /**
     * 核心工作开始时间
     */
    @TableField("core_start_time")
    private LocalTime coreStartTime;

    /**
     * 核心工作结束时间
     */
    @TableField("core_end_time")
    private LocalTime coreEndTime;

    // ==================== 工作时长要求 ====================

    /**
     * 最少工作时长（分钟）
     */
    @TableField("min_work_duration")
    private Integer minWorkDuration;

    /**
     * 标准工作时长（分钟）
     */
    @TableField("standard_work_duration")
    private Integer standardWorkDuration;

    /**
     * 最多工作时长（分钟）
     */
    @TableField("max_work_duration")
    private Integer maxWorkDuration;

    // ==================== 宽限时间 ====================

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

    // ==================== 考勤规则 ====================

    /**
     * 必须打卡核心时间：0-否 1-是
     */
    @TableField("require_core_time_check")
    private Integer requireCoreTimeCheck;

    /**
     * 允许弹性加班：0-否 1-是
     */
    @TableField("allow_flexible_overtime")
    private Integer allowFlexibleOvertime;

    /**
     * 跨天弹性：0-否 1-是
     */
    @TableField("cross_day_flex")
    private Integer crossDayFlex;

    // ==================== 午休时间 ====================

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
     * 午休是否计入工作时长：0-否 1-是
     */
    @TableField("lunch_count_as_work")
    private Integer lunchCountAsWork;

    // ==================== 高级规则 ====================

    /**
     * 弹性周最小工作天数
     */
    @TableField("min_work_days_per_week")
    private Integer minWorkDaysPerWeek;

    /**
     * 弹性周最大工作天数
     */
    @TableField("max_work_days_per_week")
    private Integer maxWorkDaysPerWeek;

    /**
     * 允许远程工作：0-否 1-是
     */
    @TableField("allow_remote_work")
    private Integer allowRemoteWork;

    /**
     * 远程工作需要审批：0-否 1-是
     */
    @TableField("remote_work_requires_approval")
    private Integer remoteWorkRequiresApproval;

    // ==================== 状态信息 ====================

    /**
     * 配置状态：1-启用 0-禁用
     */
    @TableField("status")
    private Integer status;

    /**
     * 生效时间
     */
    @TableField("effective_time")
    private LocalDateTime effectiveTime;

    /**
     * 失效时间
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;

    // ==================== 审计字段 ====================

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

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

    // ==================== 扩展字段 ====================

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extended_attributes")
    private String extendedAttributes;
}
