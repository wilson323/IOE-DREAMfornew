package net.lab1024.sa.attendance.engine.prediction.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实时预测结果模型
 * <p>
 * 封装实时预测的结果数据 严格遵循CLAUDE.md全局架构规范
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
public class RealTimePredictionResult {
    private LocalDateTime updateTime;
    private Map<String, Double> currentPredictions;
    private DataChangeAnalysis changeAnalysis;
    private Map<String, Double> updatedPredictions;
    private List<PredictionImpact> predictionImpacts;
    private Boolean updateSuccessful;

    private LocalDateTime predictionTime;
    private Map<String, Object> predictions;
    private Double confidence;
}
