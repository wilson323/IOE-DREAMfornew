package net.lab1024.sa.attendance.engine.prediction.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 缺勤率预测结果模型
 * <p>
 * 封装缺勤率预测的结果数据
 * 严格遵循CLAUDE.md全局架构规范
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
public class AbsenteeismPredictionResult {
    private String predictionId;
    private TimeRange timeRange;
    private AbsenteeismPatternAnalysis patternAnalysis;
    private Double baseAbsenteeismRate;
    private List<AbsenteeismFactor> influencingFactors;
    private Double adjustedAbsenteeismRate;
    private Map<String, Double> departmentAbsenteeismRates;
    private ConfidenceInterval confidenceInterval;
    private Boolean predictionSuccessful;
    private String errorMessage;

    private Double predictedRate;
    private Double confidence;
    private String riskLevel;
}
