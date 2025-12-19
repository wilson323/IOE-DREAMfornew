package net.lab1024.sa.attendance.engine.prediction.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 冲突预测结果模型
 * <p>
 * 封装冲突预测的结果数据 严格遵循CLAUDE.md全局架构规范
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
public class ConflictPredictionResult {
    private String predictionId;
    private LocalDateTime predictionTime;
    private ConflictPatternAnalysis patternAnalysis;
    private Map<String, Double> conflictProbabilities;
    private List<HighRiskPeriod> highRiskPeriods;
    private Map<String, ConflictSeverity> predictedSeverities;
    private List<ConflictPreventionSuggestion> preventionSuggestions;
    private Boolean predictionSuccessful;
    private Integer conflictCount;
    private List<Map<String, Object>> conflicts;
    private String riskLevel;
    private String errorMessage;
}
