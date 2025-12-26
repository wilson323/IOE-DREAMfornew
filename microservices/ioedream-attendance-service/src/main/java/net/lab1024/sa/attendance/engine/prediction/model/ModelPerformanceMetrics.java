package net.lab1024.sa.attendance.engine.prediction.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模型性能指标模型
 * <p>
 * 封装模型性能指标信息
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
public class ModelPerformanceMetrics {
    private String predictionType;
    private Integer totalPredictions;
    private Double averageAccuracy;
    private Double averagePredictionTime;
    private LocalDateTime lastUpdated;
    private Map<String, Double> specificMetrics;

    private Double accuracy;
    private Double precision;
    private Double recall;
    private String modelVersion;

    public void setSpecificMetric(String key, Double value) {
        if (specificMetrics == null) {
            specificMetrics = new HashMap<>();
        }
        specificMetrics.put(key, value);
    }
}
