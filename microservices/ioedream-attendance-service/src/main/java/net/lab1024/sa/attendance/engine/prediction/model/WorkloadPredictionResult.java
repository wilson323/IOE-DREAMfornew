package net.lab1024.sa.attendance.engine.prediction.model;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工作量预测结果模型
 * <p>
 * 封装工作量预测的结果数据
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
public class WorkloadPredictionResult {

    /**
     * 预测ID
     */
    private String predictionId;

    /**
     * 时间范围
     */
    private TimeRange timeRange;

    private List<BusinessFactor> businessFactors;

    /**
     * 预测的工作量
     */
    private Double predictedWorkload;

    /**
     * 置信度
     */
    private Double confidence;

    /**
     * 工作量级别
     */
    private String workloadLevel;

    /**
     * 基础工作量
     */
    private Map<String, Double> baseWorkload;

    /**
     * 调整后工作量
     */
    private Map<String, Double> adjustedWorkload;

    /**
     * 高峰期
     */
    private List<PeakPeriod> peakPeriods;

    /**
     * 资源需求
     */
    private Map<String, ResourceRequirement> resourceRequirements;

    /**
     * 预测是否成功
     */
    private boolean predictionSuccessful;

    // 缺失的字段和方法
    private WorkloadPatternAnalysis patternAnalysis;

    public void setPatternAnalysis(WorkloadPatternAnalysis patternAnalysis) {
        this.patternAnalysis = patternAnalysis;
    }
}
