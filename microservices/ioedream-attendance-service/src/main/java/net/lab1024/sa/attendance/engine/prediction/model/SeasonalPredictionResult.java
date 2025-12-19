package net.lab1024.sa.attendance.engine.prediction.model;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 季节性需求预测结果模型
 * <p>
 * 封装季节性需求预测的结果数据 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeasonalPredictionResult {
    private String predictionId;
    private SeasonalityPatternAnalysis patternAnalysis;
    private List<SeasonalPeak> seasonalPeaks;
    private List<SeasonalAdjustmentStrategy> adjustmentStrategies;
    private Boolean predictionSuccessful;

    private Map<String, Double> seasonalDemands;
    private Double confidence;
    private String seasonType;
}
