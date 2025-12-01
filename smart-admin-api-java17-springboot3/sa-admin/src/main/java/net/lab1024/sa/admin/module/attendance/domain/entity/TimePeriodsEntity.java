package net.lab1024.sa.admin.module.attendance.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.time.LocalTime;

/**
 * 时间段表实体
 *
 * 时间段管理的基础实体，支持跨日时间段和休息时间配置
 *
 * @author IOE-DREAM Team
 * @since 2025-11-25
 */
@Data
@TableName("t_time_periods")
public class TimePeriodsEntity extends BaseEntity {

    /**
     * 时间段ID
     */
    private Long periodId;

    /**
     * 时间段编码
     */
    @TableField("`period_code`")
    private String periodCode;

    /**
     * 时间段名称
     */
    @TableField("`period_name`")
    private String periodName;

    /**
     * 开始时间
     */
    @TableField("`start_time`")
    private LocalTime startTime;

    /**
     * 结束时间
     */
    @TableField("`end_time`")
    private LocalTime endTime;

    /**
     * 持续时间(分钟)
     */
    @TableField("`duration_minutes`")
    private Integer durationMinutes;

    /**
     * 是否跨日 0-否 1-是
     */
    @TableField("`is_cross_day`")
    private Boolean isCrossDay;

    /**
     * 休息时间(分钟)
     */
    @TableField("`break_time`")
    private Integer breakTime;

    /**
     * 工作类型: WORK-工作 BREAK-休息 OVERTIME-加班
     */
    @TableField("`work_type`")
    private String workType;

    /**
     * 提前容忍时间(分钟)
     */
    @TableField("`tolerance_before`")
    private Integer toleranceBefore;

    /**
     * 延后容忍时间(分钟)
     */
    @TableField("`tolerance_after`")
    private Integer toleranceAfter;

    /**
     * 时间段描述
     */
    @TableField("`description`")
    private String description;

    /**
     * 排序
     */
    @TableField("`sort_order`")
    private Integer sortOrder;

    /**
     * 状态 1-启用 0-禁用
     */
    @TableField("`status`")
    private Boolean status;

    /**
     * 工作类型描述（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String workTypeDesc;

    /**
     * 是否工作时间段（查询时填充，不存储）
     */
    @TableField(exist = false)
    private Boolean isWorkPeriod;

    /**
     * 格式化时间范围（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String formattedTimeRange;
}