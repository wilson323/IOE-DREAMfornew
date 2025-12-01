package net.lab1024.sa.attendance.domain.query;

import java.io.Serializable;

import lombok.Data;

/**
 * 时间段查询条件类
 *
 * 提供灵活的时间段查询条件构建，支持多维度筛选
 * 严格遵循项目开发规范和编码标准
 *
 * @author IOE-DREAM Team
 * @since 2025-11-25
 */
@Data
public class TimePeriodsQuery extends Object implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 时间段编码（模糊查询）
     */
    private String periodCode;

    /**
     * 时间段名称（模糊查询）
     */
    private String periodName;

    /**
     * 工作类型
     * WORK-工作
     * BREAK-休息
     * OVERTIME-加班
     */
    private String workType;

    /**
     * 状态：1-启用 0-禁用
     */
    private Boolean status;

    /**
     * 是否跨日：0-否 1-是
     */
    private Boolean isCrossDay;

    /**
     * 最小持续时间（分钟）
     */
    private Integer minDurationMinutes;

    /**
     * 最大持续时间（分钟）
     */
    private Integer maxDurationMinutes;

    /**
     * 开始时间范围-最小
     */
    private String startTimeMin;

    /**
     * 开始时间范围-最大
     */
    private String startTimeMax;

    /**
     * 结束时间范围-最小
     */
    private String endTimeMin;

    /**
     * 结束时间范围-最大
     */
    private String endTimeMax;

    /**
     * 最小提前容忍时间（分钟）
     */
    private Integer minToleranceBefore;

    /**
     * 最大提前容忍时间（分钟）
     */
    private Integer maxToleranceBefore;

    /**
     * 最小延后容忍时间（分钟）
     */
    private Integer minToleranceAfter;

    /**
     * 最大延后容忍时间（分钟）
     */
    private Integer maxToleranceAfter;

    /**
     * 创建时间范围-开始
     */
    private String createTimeStart;

    /**
     * 创建时间范围-结束
     */
    private String createTimeEnd;

    /**
     * 更新时间范围-开始
     */
    private String updateTimeStart;

    /**
     * 更新时间范围-结束
     */
    private String updateTimeEnd;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序方向
     */
    private String sortOrder;

    /**
     * 关键词搜索（时间段编码、名称、描述）
     */
    private String keyword;
}