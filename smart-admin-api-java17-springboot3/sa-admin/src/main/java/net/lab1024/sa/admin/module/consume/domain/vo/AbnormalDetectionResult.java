/*
 * 异常检测结果
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 异常检测结果
 * 封装异常操作检测的结果信息
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbnormalDetectionResult {

    /**
     * 是否异常
     */
    private boolean isAbnormal;

    /**
     * 异常类型
     */
    private String anomalyType;

    /**
     * 异常级别（LOW/MEDIUM/HIGH/CRITICAL）
     */
    private String anomalyLevel;

    /**
     * 置信度（0-100）
     */
    private Double confidence;

    /**
     * 风险评分（0-100）
     */
    private Integer riskScore;

    /**
     * 检测消息
     */
    private String message;

    /**
     * 详细描述
     */
    private String detailDescription;

    /**
     * 检测到的异常特征
     */
    private List<String> detectedFeatures;

    /**
     * 异常指标
     */
    private Map<String, Object> anomalyMetrics;

    /**
     * 基线值
     */
    private Map<String, Object> baselineValues;

    /**
     * 当前值
     */
    private Map<String, Object> currentValues;

    /**
     * 偏离程度
     */
    private Map<String, Double> deviationDegrees;

    /**
     * 建议操作
     */
    private String suggestedAction;

    /**
     * 处理优先级
     */
    private Integer priority;

    /**
     * 是否需要人工审核
     */
    private Boolean requireManualReview;

    /**
     * 是否触发告警
     */
    private Boolean triggerAlert;

    /**
     * 告警类型
     */
    private String alertType;

    /**
     * 相关操作ID
     */
    private Long relatedOperationId;

    /**
     * 检测规则ID列表
     */
    private List<Long> triggeredRuleIds;

    /**
     * 检测时间
     */
    private LocalDateTime detectionTime;

    /**
     * 检测算法
     */
    private String detectionAlgorithm;

    /**
     * 模型版本
     */
    private String modelVersion;

    /**
     * 扩展数据（JSON格式）
     */
    private String extendData;

    /**
     * 创建正常结果
     */
    public static AbnormalDetectionResult normal() {
        return AbnormalDetectionResult.builder()
                .isAbnormal(false)
                .anomalyLevel("SAFE")
                .confidence(100.0)
                .riskScore(0)
                .message("操作正常，未检测到异常")
                .suggestedAction("继续正常操作")
                .requireManualReview(false)
                .triggerAlert(false)
                .detectionTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建异常结果
     */
    public static AbnormalDetectionResult abnormal(String anomalyType, String anomalyLevel, String message) {
        return AbnormalDetectionResult.builder()
                .isAbnormal(true)
                .anomalyType(anomalyType)
                .anomalyLevel(anomalyLevel)
                .confidence(80.0)
                .riskScore(calculateRiskScore(anomalyLevel))
                .message(message)
                .suggestedAction("建议进行人工审核")
                .requireManualReview(true)
                .triggerAlert(true)
                .detectionTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建低风险异常结果
     */
    public static AbnormalDetectionResult lowRiskAbnormal(String anomalyType, String message, Double confidence) {
        return AbnormalDetectionResult.builder()
                .isAbnormal(true)
                .anomalyType(anomalyType)
                .anomalyLevel("LOW")
                .confidence(confidence)
                .riskScore(30)
                .message(message)
                .suggestedAction("监控后续行为")
                .requireManualReview(false)
                .triggerAlert(false)
                .detectionTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建中风险异常结果
     */
    public static AbnormalDetectionResult mediumRiskAbnormal(String anomalyType, String message, Double confidence) {
        return AbnormalDetectionResult.builder()
                .isAbnormal(true)
                .anomalyType(anomalyType)
                .anomalyLevel("MEDIUM")
                .confidence(confidence)
                .riskScore(60)
                .message(message)
                .suggestedAction("增加监控频率")
                .requireManualReview(true)
                .triggerAlert(true)
                .alertType("WARNING")
                .detectionTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建高风险异常结果
     */
    public static AbnormalDetectionResult highRiskAbnormal(String anomalyType, String message, Double confidence) {
        return AbnormalDetectionResult.builder()
                .isAbnormal(true)
                .anomalyType(anomalyType)
                .anomalyLevel("HIGH")
                .confidence(confidence)
                .riskScore(80)
                .message(message)
                .suggestedAction("立即人工干预")
                .requireManualReview(true)
                .triggerAlert(true)
                .alertType("CRITICAL")
                .priority(1)
                .detectionTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建严重风险异常结果
     */
    public static AbnormalDetectionResult criticalRiskAbnormal(String anomalyType, String message, Double confidence) {
        return AbnormalDetectionResult.builder()
                .isAbnormal(true)
                .anomalyType(anomalyType)
                .anomalyLevel("CRITICAL")
                .confidence(confidence)
                .riskScore(100)
                .message(message)
                .suggestedAction("立即阻止操作并通知管理员")
                .requireManualReview(true)
                .triggerAlert(true)
                .alertType("EMERGENCY")
                .priority(0)
                .detectionTime(LocalDateTime.now())
                .build();
    }

    /**
     * 计算风险评分
     */
    private static Integer calculateRiskScore(String anomalyLevel) {
        switch (anomalyLevel.toUpperCase()) {
            case "LOW":
                return 30;
            case "MEDIUM":
                return 60;
            case "HIGH":
                return 80;
            case "CRITICAL":
                return 100;
            default:
                return 0;
        }
    }

    /**
     * 获取异常级别描述
     */
    public String getAnomalyLevelDescription() {
        if (anomalyLevel == null) {
            return "未知";
        }
        switch (anomalyLevel.toUpperCase()) {
            case "SAFE":
                return "安全";
            case "LOW":
                return "低风险";
            case "MEDIUM":
                return "中风险";
            case "HIGH":
                return "高风险";
            case "CRITICAL":
                return "严重风险";
            default:
                return anomalyLevel;
        }
    }

    /**
     * 获取异常类型描述
     */
    public String getAnomalyTypeDescription() {
        if (anomalyType == null) {
            return "未知";
        }
        switch (anomalyType.toUpperCase()) {
            case "FREQUENCY":
                return "频率异常";
            case "AMOUNT":
                return "金额异常";
            case "TIME":
                return "时间异常";
            case "LOCATION":
                return "位置异常";
            case "DEVICE":
                return "设备异常";
            case "SEQUENCE":
                return "序列异常";
            case "PATTERN":
                return "模式异常";
            default:
                return anomalyType;
        }
    }

    /**
     * 检查是否需要立即处理
     */
    public boolean needsImmediateAction() {
        return isAbnormal && ("HIGH".equals(anomalyLevel) || "CRITICAL".equals(anomalyLevel));
    }

    /**
     * 检查是否需要自动阻止
     */
    public boolean shouldBlockAutomatically() {
        return isAbnormal && "CRITICAL".equals(anomalyLevel) && riskScore >= 90;
    }

    /**
     * 获取处理建议
     */
    public String getProcessingSuggestion() {
        if (!isAbnormal) {
            return "无需处理";
        }

        switch (anomalyLevel.toUpperCase()) {
            case "LOW":
                return "建议记录并继续监控";
            case "MEDIUM":
                return "建议增加验证步骤";
            case "HIGH":
                return "建议人工审核";
            case "CRITICAL":
                return "建议立即阻止并报警";
            default:
                return "建议按标准流程处理";
        }
    }

    /**
     * 获取详细信息
     */
    public String getDetailInfo() {
        StringBuilder detail = new StringBuilder();
        detail.append("异常类型: ").append(getAnomalyTypeDescription());
        detail.append(", 异常级别: ").append(getAnomalyLevelDescription());
        detail.append(", 风险评分: ").append(riskScore);
        detail.append(", 置信度: ").append(confidence != null ? String.format("%.1f%%", confidence) : "未知");

        if (detectedFeatures != null && !detectedFeatures.isEmpty()) {
            detail.append(", 检测特征: ").append(String.join(", ", detectedFeatures));
        }

        if (suggestedAction != null) {
            detail.append(", 建议操作: ").append(suggestedAction);
        }

        return detail.toString();
    }

    /**
     * 添加异常指标
     */
    public void addAnomalyMetric(String key, Object value) {
        if (anomalyMetrics == null) {
            anomalyMetrics = new java.util.HashMap<>();
        }
        anomalyMetrics.put(key, value);
    }

    /**
     * 添加基线值
     */
    public void addBaselineValue(String key, Object value) {
        if (baselineValues == null) {
            baselineValues = new java.util.HashMap<>();
        }
        baselineValues.put(key, value);
    }

    /**
     * 添加当前值
     */
    public void addCurrentValue(String key, Object value) {
        if (currentValues == null) {
            currentValues = new java.util.HashMap<>();
        }
        currentValues.put(key, value);
    }

    /**
     * 计算偏离度
     */
    public void calculateDeviation(String metricKey) {
        if (baselineValues != null && currentValues != null) {
            Object baseline = baselineValues.get(metricKey);
            Object current = currentValues.get(metricKey);

            if (baseline instanceof Number && current instanceof Number) {
                double baselineValue = ((Number) baseline).doubleValue();
                double currentValue = ((Number) current).doubleValue();

                if (baselineValue != 0) {
                    double deviation = Math.abs((currentValue - baselineValue) / baselineValue) * 100;
                    if (deviationDegrees == null) {
                        deviationDegrees = new java.util.HashMap<>();
                    }
                    deviationDegrees.put(metricKey, deviation);
                }
            }
        }
    }
}