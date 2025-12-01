package net.lab1024.sa.admin.module.consume.domain.analysis;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 行为分析
 * 严格遵循repowiki规范：数据传输对象，包含用户消费行为的综合分析结果
 *
 * @author SmartAdmin Team
 * @date 2025/11/19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorAnalysis {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 分析ID
     */
    private Long analysisId;

    /**
     * 分析类型：COMPREHENSIVE-综合分析，ANOMALY_DETECTION-异常检测，PATTERN_RECOGNITION-模式识别，RISK_ASSESSMENT-风险评估
     */
    private String analysisType;

    /**
     * 行为类型：NORMAL-正常，ABNORMAL-异常，HIGH_RISK-高风险，SUSPICIOUS-可疑，CHANGED-行为变化
     */
    private String behaviorType;

    /**
     * 风险等级：LOW-低风险，MEDIUM-中风险，HIGH-高风险，CRITICAL-严重风险
     */
    private String riskLevel;

    /**
     * 风险评分（0-100）
     */
    private BigDecimal riskScore;

    /**
     * 异常置信度（0-100）
     */
    private BigDecimal anomalyConfidence;

    /**
     * 行为偏离度（0-100）
     */
    private BigDecimal behaviorDeviation;

    /**
     * 行为稳定性评分（0-100）
     */
    private BigDecimal stabilityScore;

    /**
     * 行为可预测性评分（0-100）
     */
    private BigDecimal predictabilityScore;

    /**
     * 行为一致性评分（0-100）
     */
    private BigDecimal consistencyScore;

    /**
     * 异常行为特征列表
     */
    private List<String> anomalyFeatures;

    /**
     * 行为变化指标
     */
    private Map<String, BigDecimal> behaviorChangeMetrics;

    /**
     * 时间相关分析
     */
    private Map<String, Object> timeAnalysis;

    /**
     * 金额相关分析
     */
    private Map<String, Object> amountAnalysis;

    /**
     * 位置相关分析
     */
    private Map<String, Object> locationAnalysis;

    /**
     * 设备相关分析
     */
    private Map<String, Object> deviceAnalysis;

    /**
     * 频率相关分析
     */
    private Map<String, Object> frequencyAnalysis;

    /**
     * 序列相关分析
     */
    private Map<String, Object> sequenceAnalysis;

    /**
     * 分析时间范围开始
     */
    private LocalDateTime analysisStartTime;

    /**
     * 分析时间范围结束
     */
    private LocalDateTime analysisEndTime;

    /**
     * 分析时间
     */
    private LocalDateTime analysisTime;

    /**
     * 数据样本量
     */
    private Integer sampleSize;

    /**
     * 分析窗口期（天）
     */
    private Integer analysisWindow;

    /**
     * 建议处理方式
     */
    private List<String> recommendedActions;

    /**
     * 需要关注的指标
     */
    private List<String> attentionMetrics;

    /**
     * 行为描述
     */
    private String behaviorDescription;

    /**
     * 业务影响评估
     */
    private String businessImpact;

    /**
     * 预测下一步行为
     */
    private String nextBehaviorPrediction;

    /**
     * 监控建议
     */
    private List<String> monitoringSuggestions;

    /**
     * 是否需要人工审核
     */
    private Boolean requiresManualReview;

    /**
     * 是否触发预警
     */
    private Boolean triggersAlert;

    /**
     * 预警级别：INFO-信息，WARNING-警告，ERROR-错误，CRITICAL-严重
     */
    private String alertLevel;

    /**
     * 检测到的异常数量
     */
    private Integer detectedAnomalyCount;

    /**
     * 高风险异常数量
     */
    private Integer highRiskAnomalyCount;

    /**
     * 分析模型版本
     */
    private String analysisModelVersion;

    /**
     * 分析算法类型
     */
    private String algorithmType;

    /**
     * 扩展信息
     */
    private Map<String, Object> extendedInfo;

    /**
     * 创建正常行为分析结果
     */
    public static BehaviorAnalysis normalBehavior(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        return BehaviorAnalysis.builder()
                .userId(userId)
                .analysisType("COMPREHENSIVE")
                .behaviorType("NORMAL")
                .riskLevel("LOW")
                .riskScore(new BigDecimal("15.0"))
                .anomalyConfidence(new BigDecimal("5.0"))
                .behaviorDeviation(new BigDecimal("10.0"))
                .stabilityScore(new BigDecimal("85.0"))
                .predictabilityScore(new BigDecimal("80.0"))
                .consistencyScore(new BigDecimal("90.0"))
                .analysisStartTime(startTime)
                .analysisEndTime(endTime)
                .analysisTime(LocalDateTime.now())
                .requiresManualReview(false)
                .triggersAlert(false)
                .alertLevel("INFO")
                .detectedAnomalyCount(0)
                .highRiskAnomalyCount(0)
                .behaviorDescription("用户消费行为正常，无明显异常")
                .businessImpact("无负面影响")
                .nextBehaviorPrediction("预计保持当前消费模式")
                .analysisModelVersion("1.0")
                .algorithmType("ENSEMBLE")
                .build();
    }

    /**
     * 创建异常行为分析结果
     */
    public static BehaviorAnalysis abnormalBehavior(Long userId, String behaviorType, BigDecimal riskScore,
                                                   List<String> anomalyFeatures, List<String> recommendedActions) {
        return BehaviorAnalysis.builder()
                .userId(userId)
                .analysisType("ANOMALY_DETECTION")
                .behaviorType(behaviorType)
                .riskLevel(riskScore.compareTo(new BigDecimal("70")) >= 0 ? "HIGH" : "MEDIUM")
                .riskScore(riskScore)
                .anomalyConfidence(new BigDecimal("80.0"))
                .behaviorDeviation(new BigDecimal("65.0"))
                .stabilityScore(new BigDecimal("35.0"))
                .predictabilityScore(new BigDecimal("30.0"))
                .consistencyScore(new BigDecimal("40.0"))
                .anomalyFeatures(anomalyFeatures)
                .analysisTime(LocalDateTime.now())
                .requiresManualReview(true)
                .triggersAlert(true)
                .alertLevel(riskScore.compareTo(new BigDecimal("85")) >= 0 ? "CRITICAL" : "WARNING")
                .detectedAnomalyCount(anomalyFeatures.size())
                .highRiskAnomalyCount(riskScore.compareTo(new BigDecimal("70")) >= 0 ? anomalyFeatures.size() : 0)
                .recommendedActions(recommendedActions)
                .behaviorDescription("检测到异常消费行为模式")
                .businessImpact("可能存在安全风险或异常使用")
                .nextBehaviorPrediction("需要密切监控后续行为")
                .analysisModelVersion("1.0")
                .algorithmType("MACHINE_LEARNING")
                .build();
    }

    /**
     * 创建行为变化分析结果
     */
    public static BehaviorAnalysis behaviorChange(Long userId, Map<String, BigDecimal> changeMetrics,
                                                 List<String> attentionMetrics) {
        return BehaviorAnalysis.builder()
                .userId(userId)
                .analysisType("PATTERN_RECOGNITION")
                .behaviorType("CHANGED")
                .riskLevel("MEDIUM")
                .riskScore(new BigDecimal("55.0"))
                .anomalyConfidence(new BigDecimal("70.0"))
                .behaviorDeviation(new BigDecimal("60.0"))
                .stabilityScore(new BigDecimal("45.0"))
                .predictabilityScore(new BigDecimal("40.0"))
                .consistencyScore(new BigDecimal("50.0"))
                .behaviorChangeMetrics(changeMetrics)
                .attentionMetrics(attentionMetrics)
                .analysisTime(LocalDateTime.now())
                .requiresManualReview(true)
                .triggersAlert(false)
                .alertLevel("WARNING")
                .detectedAnomalyCount(changeMetrics.size())
                .highRiskAnomalyCount(0)
                .recommendedActions(List.of("观察行为变化趋势", "更新用户行为基线"))
                .behaviorDescription("用户消费行为模式发生显著变化")
                .businessImpact("可能影响用户体验或风险控制")
                .nextBehaviorPrediction("需要重新建立行为基线")
                .analysisModelVersion("1.0")
                .algorithmType("STATISTICAL")
                .build();
    }

    /**
     * 创建高风险行为分析结果
     */
    public static BehaviorAnalysis highRiskBehavior(Long userId, BigDecimal riskScore,
                                                   List<String> anomalyFeatures,
                                                   List<String> recommendedActions) {
        return BehaviorAnalysis.builder()
                .userId(userId)
                .analysisType("RISK_ASSESSMENT")
                .behaviorType("HIGH_RISK")
                .riskLevel("CRITICAL")
                .riskScore(riskScore)
                .anomalyConfidence(new BigDecimal("95.0"))
                .behaviorDeviation(new BigDecimal("85.0"))
                .stabilityScore(new BigDecimal("20.0"))
                .predictabilityScore(new BigDecimal("15.0"))
                .consistencyScore(new BigDecimal("25.0"))
                .anomalyFeatures(anomalyFeatures)
                .analysisTime(LocalDateTime.now())
                .requiresManualReview(true)
                .triggersAlert(true)
                .alertLevel("CRITICAL")
                .detectedAnomalyCount(anomalyFeatures.size())
                .highRiskAnomalyCount(anomalyFeatures.size())
                .recommendedActions(recommendedActions)
                .monitoringSuggestions(List.of("实时监控用户行为", "考虑暂时限制账户权限", "联系用户确认"))
                .behaviorDescription("检测到高风险消费行为，存在严重安全隐患")
                .businessImpact("可能造成资金损失或安全风险")
                .nextBehaviorPrediction("需要立即采取风险控制措施")
                .analysisModelVersion("1.0")
                .algorithmType("RULE_BASED")
                .build();
    }

    /**
     * 检查是否为高风险
     */
    public boolean isHighRisk() {
        return "HIGH".equals(riskLevel) || "CRITICAL".equals(riskLevel);
    }

    /**
     * 检查是否需要人工审核
     */
    public boolean needsReview() {
        return Boolean.TRUE.equals(requiresManualReview);
    }

    /**
     * 检查是否触发预警
     */
    public boolean triggersAlert() {
        return Boolean.TRUE.equals(triggersAlert);
    }

    /**
     * 获取风险等级描述
     */
    public String getRiskLevelDescription() {
        switch (riskLevel) {
            case "LOW":
                return "低风险";
            case "MEDIUM":
                return "中风险";
            case "HIGH":
                return "高风险";
            case "CRITICAL":
                return "严重风险";
            default:
                return "未知风险";
        }
    }

    /**
     * 获取行为类型描述
     */
    public String getBehaviorTypeDescription() {
        switch (behaviorType) {
            case "NORMAL":
                return "正常行为";
            case "ABNORMAL":
                return "异常行为";
            case "HIGH_RISK":
                return "高风险行为";
            case "SUSPICIOUS":
                return "可疑行为";
            case "CHANGED":
                return "行为变化";
            default:
                return "未知行为";
        }
    }

    /**
     * 获取分析类型描述
     */
    public String getAnalysisTypeDescription() {
        switch (analysisType) {
            case "COMPREHENSIVE":
                return "综合分析";
            case "ANOMALY_DETECTION":
                return "异常检测";
            case "PATTERN_RECOGNITION":
                return "模式识别";
            case "RISK_ASSESSMENT":
                return "风险评估";
            default:
                return "未知分析";
        }
    }

    /**
     * 获取预警级别描述
     */
    public String getAlertLevelDescription() {
        switch (alertLevel) {
            case "INFO":
                return "信息";
            case "WARNING":
                return "警告";
            case "ERROR":
                return "错误";
            case "CRITICAL":
                return "严重";
            default:
                return "未知级别";
        }
    }

    /**
     * 获取综合行为评分
     */
    public BigDecimal getOverallBehaviorScore() {
        if (stabilityScore == null || predictabilityScore == null || consistencyScore == null) {
            return BigDecimal.ZERO;
        }
        return stabilityScore.add(predictabilityScore).add(consistencyScore)
                .divide(new BigDecimal("3"), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 检查行为质量是否良好
     */
    public boolean isBehaviorQualityGood() {
        BigDecimal overallScore = getOverallBehaviorScore();
        return overallScore.compareTo(new BigDecimal("70")) >= 0;
    }

    /**
     * 获取风险程度描述
     */
    public String getRiskSeverityDescription() {
        if (riskScore == null) {
            return "未知";
        }

        if (riskScore.compareTo(new BigDecimal("20")) <= 0) {
            return "极低风险";
        } else if (riskScore.compareTo(new BigDecimal("40")) <= 0) {
            return "低风险";
        } else if (riskScore.compareTo(new BigDecimal("60")) <= 0) {
            return "中等风险";
        } else if (riskScore.compareTo(new BigDecimal("80")) <= 0) {
            return "高风险";
        } else {
            return "极高风险";
        }
    }

    /**
     * 检查是否包含特定异常特征
     */
    public boolean containsAnomalyFeature(String feature) {
        return anomalyFeatures != null && anomalyFeatures.contains(feature);
    }

    /**
     * 获取异常特征数量
     */
    public int getAnomalyFeatureCount() {
        return anomalyFeatures != null ? anomalyFeatures.size() : 0;
    }

    /**
     * 检查是否建议采取紧急措施
     */
    public boolean recommendsUrgentAction() {
        return "CRITICAL".equals(riskLevel) || (recommendedActions != null &&
               recommendedActions.stream().anyMatch(action ->
                   action.contains("立即") || action.contains("暂停") || action.contains("限制")));
    }
}