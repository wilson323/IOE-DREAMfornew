package net.lab1024.sa.admin.module.consume.domain.result;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 异常趋势分析
 *
 * @author SmartAdmin Team
 * @since 2025-11-22
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "异常趋势分析")
public class AbnormalTrendAnalysis {

    @Schema(description = "分析ID")
    private Long analysisId;

    @Schema(description = "分析类型")
    private String analysisType;

    @Schema(description = "时间范围")
    private String timeRange;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "分析期间")
    private String analysisPeriod;

    @Schema(description = "趋势方向")
    private String trendDirection;

    @Schema(description = "趋势强度")
    private String trendIntensity;

    @Schema(description = "趋势描述")
    private String trendDescription;

    @Schema(description = "异常增长率")
    private Double growthRate;

    @Schema(description = "分析时间")
    private LocalDateTime analysisTime;

    @Schema(description = "趋势数据")
    private List<TrendDataPoint> trendData;

    @Schema(description = "预测结果")
    private List<TrendPrediction> predictions;

    
    
    
    
    public static class TrendDataPoint {
        @Schema(description = "时间点")
        private LocalDateTime timePoint;

        @Schema(description = "异常数量")
        private Integer anomalyCount;

        @Schema(description = "风险评分")
        private Double riskScore;
    }

    
    
    
    
    public static class TrendPrediction {
        @Schema(description = "预测时间")
        private LocalDateTime predictedTime;

        @Schema(description = "预测异常数")
        private Integer predictedAnomalies;

        @Schema(description = "预测置信度")
        private Double confidence;
    }

    public static AbnormalTrendAnalysis create(Long userId, String direction, double growthRate) {
        return AbnormalTrendAnalysis.builder()
                .analysisId(System.currentTimeMillis())
                .userId(userId)
                .trendDirection(direction)
                .growthRate(growthRate)
                .analysisTime(LocalDateTime.now())
                .build();
    }

}
