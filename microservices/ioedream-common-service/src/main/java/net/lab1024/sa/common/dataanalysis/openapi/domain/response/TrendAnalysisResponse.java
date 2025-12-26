package net.lab1024.sa.common.dataanalysis.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 趋势分析响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "趋势分析响应")
public class TrendAnalysisResponse {

    @Schema(description = "分析任务ID", example = "trend_analysis_001")
    private String analysisTaskId;

    @Schema(description = "分析时间", example = "2025-12-16T10:30:00")
    private LocalDateTime analysisTime;

    @Schema(description = "分析类型", example = "growth")
    private String analysisType;

    @Schema(description = "分析类型名称", example = "增长趋势分析")
    private String analysisTypeName;

    @Schema(description = "时间范围", example = "7d")
    private String timeRange;

    @Schema(description = "数据粒度", example = "hour")
    private String granularity;

    @Schema(description = "趋势分析结果列表")
    private List<TrendResult> trendResults;

    @Schema(description = "总体趋势摘要")
    private TrendSummary overallSummary;

    @Schema(description = "预测数据")
    private ForecastData forecastData;

    @Schema(description = "相关性分析结果")
    private List<CorrelationAnalysis> correlationAnalysis;

    @Schema(description = "异常点检测结果")
    private List<AnomalyPoint> anomalyPoints;

    @Schema(description = "关键洞察发现")
    private List<KeyInsight> keyInsights;

    @Schema(description = "数据质量指标")
    private DataQualityMetrics dataQualityMetrics;

    @Schema(description = "分析参数")
    private Map<String, Object> analysisParams;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "趋势结果")
    public static class TrendResult {

        @Schema(description = "指标名称", example = "user_count")
        private String metricName;

        @Schema(description = "指标显示名称", example = "用户数量")
        private String metricDisplayName;

        @Schema(description = "趋势方向", example = "up", allowableValues = {"up", "down", "stable"})
        private String trendDirection;

        @Schema(description = "趋势方向名称", example = "上升趋势")
        private String trendDirectionName;

        @Schema(description = "变化率", example = "15.5")
        private BigDecimal changeRate;

        @Schema(description = "变化量", example = "1250")
        private BigDecimal changeValue;

        @Schema(description = "当前值", example = "12500")
        private BigDecimal currentValue;

        @Schema(description = "基准值", example = "11250")
        private BigDecimal baselineValue;

        @Schema(description = "趋势线数据点")
        private List<DataPoint> trendLineData;

        @Schema(description = "平均值", example = "11875")
        private BigDecimal averageValue;

        @Schema(description = "最大值", example = "13500")
        private BigDecimal maxValue;

        @Schema(description = "最小值", example = "10500")
        private BigDecimal minValue;

        @Schema(description = "趋势强度", example = "strong", allowableValues = {"strong", "moderate", "weak"})
        private String trendStrength;

        @Schema(description = "季节性指数", example = "1.2")
        private BigDecimal seasonalityIndex;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @Schema(description = "数据点")
        public static class DataPoint {

            @Schema(description = "时间戳", example = "2025-12-16T10:00:00")
            private LocalDateTime timestamp;

            @Schema(description = "数值", example = "12500")
            private BigDecimal value;

            @Schema(description = "预测值(如果有)", example = "12600")
            private BigDecimal predictedValue;

            @Schema(description = "置信区间上限", example = "12700")
            private BigDecimal upperBound;

            @Schema(description = "置信区间下限", example = "12500")
            private BigDecimal lowerBound;

            @Schema(description = "是否异常点", example = "false")
            private Boolean isAnomaly;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "趋势摘要")
    public static class TrendSummary {

        @Schema(description = "主要增长指标", example = "用户访问量增长15.5%")
        private String mainGrowthMetric;

        @Schema(description = "主要下降指标", example = "设备故障率下降8.2%")
        private String mainDeclineMetric;

        @Schema(description = "整体趋势评价", example = "整体呈良好增长态势")
        private String overallTrendAssessment;

        @Schema(description = "关键转折点", example = "第3天出现显著增长")
        private String keyTurningPoint;

        @Schema(description = "趋势稳定性", example = "stable", allowableValues = {"stable", "volatile", "seasonal"})
        private String trendStability;

        @Schema(description = "预测准确度", example = "0.92")
        private BigDecimal forecastAccuracy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "预测数据")
    public static class ForecastData {

        @Schema(description = "预测周期", example = "7d")
        private String forecastPeriod;

        @Schema(description = "预测方法", example = "arima")
        private String forecastMethod;

        @Schema(description = "预测准确度", example = "0.88")
        private BigDecimal accuracy;

        @Schema(description = "预测数据点")
        private List<TrendResult.DataPoint> forecastPoints;

        @Schema(description = "置信区间", example = "95%")
        private String confidenceInterval;

        @Schema(description = "预测摘要", example = "预计未来7天用户数将保持稳定增长")
        private String forecastSummary;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "相关性分析")
    public static class CorrelationAnalysis {

        @Schema(description = "指标1", example = "user_count")
        private String metric1;

        @Schema(description = "指标2", example = "access_times")
        private String metric2;

        @Schema(description = "相关系数", example = "0.85")
        private BigDecimal correlationCoefficient;

        @Schema(description = "相关性强度", example = "strong", allowableValues = {"strong", "moderate", "weak", "none"})
        private String correlationStrength;

        @Schema(description = "P值", example = "0.001")
        private BigDecimal pValue;

        @Schema(description = "相关性描述", example = "用户数量与访问次数呈强正相关")
        private String correlationDescription;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "异常点")
    public static class AnomalyPoint {

        @Schema(description = "时间戳", example = "2025-12-14T15:30:00")
        private LocalDateTime timestamp;

        @Schema(description = "指标名称", example = "user_count")
        private String metricName;

        @Schema(description = "异常值", example = "8500")
        private BigDecimal anomalyValue;

        @Schema(description = "预期值", example = "11500")
        private BigDecimal expectedValue;

        @Schema(description = "异常分数", example = "3.5")
        private BigDecimal anomalyScore;

        @Schema(description = "异常类型", example = "spike", allowableValues = {"spike", "drop", "trend_change", "pattern_break"})
        private String anomalyType;

        @Schema(description = "异常描述", example = "用户数量突然下降，可能由系统故障引起")
        private String anomalyDescription;

        @Schema(description = "严重程度", example = "high", allowableValues = {"low", "medium", "high", "critical"})
        private String severity;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "关键洞察")
    public static class KeyInsight {

        @Schema(description = "洞察类型", example = "pattern", allowableValues = {"pattern", "anomaly", "opportunity", "risk"})
        private String insightType;

        @Schema(description = "洞察标题", example = "工作日访问高峰模式")
        private String insightTitle;

        @Schema(description = "洞察描述", example = "发现用户访问在工作日上午9-11点达到峰值")
        private String insightDescription;

        @Schema(description = "业务影响", example = "影响系统资源调度策略")
        private String businessImpact;

        @Schema(description = "建议行动", example = "建议在高峰期增加服务器资源")
        private String recommendedAction;

        @Schema(description = "置信度", example = "0.92")
        private BigDecimal confidence;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "数据质量指标")
    public static class DataQualityMetrics {

        @Schema(description = "数据完整率", example = "0.98")
        private BigDecimal completenessRate;

        @Schema(description = "数据准确率", example = "0.95")
        private BigDecimal accuracyRate;

        @Schema(description = "数据及时性", example = "0.99")
        private BigDecimal timelinessRate;

        @Schema(description = "数据一致性", example = "0.97")
        private BigDecimal consistencyRate;

        @Schema(description = "缺失数据比例", example = "0.02")
        private BigDecimal missingDataRatio;

        @Schema(description = "异常数据比例", example = "0.01")
        private BigDecimal anomalyDataRatio;
    }
}
