package net.lab1024.sa.attendance.engine.prediction.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 成本预测结果模型
 * <p>
 * 封装成本预测的结果数据
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
public class CostPredictionResult {

    /**
     * 预测ID
     */
    private String predictionId;

    private TimeRange timeRange;
    private List<CostFactor> costFactors;
    private List<CostOptimizationSuggestion> optimizationSuggestions;
    private Boolean predictionSuccessful;

    /**
     * 基础人工成本
     */
    private double baseLaborCost;

    /**
     * 预测加班成本
     */
    private double predictedOvertimeCost;

    /**
     * 预测缺勤成本
     */
    private double predictedAbsenteeismCost;

    /**
     * 管理成本
     */
    private double managementCost;

    /**
     * 总预测成本
     */
    private double totalPredictedCost;

    /**
     * 预测成本
     */
    private BigDecimal predictedCost;

    /**
     * 置信度
     */
    private Double confidence;

    /**
     * 成本级别
     */
    private String costLevel;
}
