package net.lab1024.sa.attendance.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 考勤趋势数据视图对象
 * <p>
 * 用于考勤趋势图表展示
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "考勤趋势数据视图对象")
public class DashboardTrendVO {

    /**
     * 时间维度：DAY-天 WEEK-周 MONTH-月
     */
    @Schema(description = "时间维度", example = "WEEK")
    private String timeDimension;

    /**
     * 趋势类型：ATTENDANCE_RATE-出勤率 WORK_HOURS-工作时长 OVERTIME-加班
     */
    @Schema(description = "趋势类型", example = "ATTENDANCE_RATE")
    private String trendType;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间", example = "2025-12-01")
    private String startDate;

    /**
     * 结束时间
     */
    @Schema(description = "结束时间", example = "2025-12-23")
    private String endDate;

    /**
     * 趋势数据
     */
    @Schema(description = "趋势数据")
    private List<Map<String, Object>> trendData;

    /**
     * 平均值
     */
    @Schema(description = "平均值", example = "94.5")
    private Double averageValue;

    /**
     * 最大值
     */
    @Schema(description = "最大值", example = "98.0")
    private Double maxValue;

    /**
     * 最小值
     */
    @Schema(description = "最小值", example = "89.0")
    private Double minValue;

    /**
     * 同比增长率（百分比）
     */
    @Schema(description = "同比增长率（百分比）", example = "2.5")
    private Double yearOverYearGrowth;

    /**
     * 环比增长率（百分比）
     */
    @Schema(description = "环比增长率（百分比）", example = "1.2")
    private Double monthOverMonthGrowth;
}
