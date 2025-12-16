package net.lab1024.sa.common.attendance.entity;

import java.time.LocalTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 工作班次休息时间配置实体类
 * <p>
 * 考勤班次的休息时间配置，包含午休、休息时间等
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_attendance_work_shift_break_time")
public class WorkShiftBreakTimeEntity extends BaseEntity {

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
     * 休息类型：1-午休 2-茶歇 3-晚餐 4-其他休息
     */
    @TableField("break_type")
    private Integer breakType;

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
     * 休息时长（分钟）
     */
    @TableField("break_duration")
    private Integer breakDuration;

    /**
     * 是否计薪：0-不计薪 1-计薪
     */
    @TableField("is_paid")
    private Integer isPaid;

    /**
     * 是否强制休息：0-否 1-是
     */
    @TableField("is_mandatory")
    private Integer isMandatory;

    /**
     * 休息次数限制
     */
    @TableField("break_count_limit")
    private Integer breakCountLimit;

    /**
     * 休息描述
     */
    @TableField("break_description")
    private String breakDescription;

    /**
     * 配置状态：1-启用 0-禁用
     */
    @TableField("config_status")
    private Integer configStatus;
}