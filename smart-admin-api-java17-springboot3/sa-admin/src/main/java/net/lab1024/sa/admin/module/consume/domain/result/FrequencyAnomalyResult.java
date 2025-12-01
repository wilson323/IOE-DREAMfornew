package net.lab1024.sa.admin.module.consume.domain.result;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 频率异常检测结果
 * 严格遵循repowiki规范：数据传输对象，包含频率异常检测的结果信息
 *
 * @author SmartAdmin Team
 * @date 2025/11/19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrequencyAnomalyResult {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 消费记录ID
     */
    private Long consumeRecordId;

    /**
     * 检测时间窗口（分钟）
     */
    private Integer timeWindowMinutes;

    /**
     * 时间窗口内消费次数
     */
    private Integer consumeCount;

    /**
     * 历史平均频率（次/小时）
     */
    private BigDecimal historicalAverageFrequency;

    /**
     * 历史最高频率（次/小时）
     */
    private BigDecimal historicalMaxFrequency;

    /**
     * 当前频率（次/小时）
     */
    private BigDecimal currentFrequency;

    /**
     * 频率偏离率（百分比）
     */
    private BigDecimal deviationRate;

    /**
     * 异常置信度（0-100）
     */
    private BigDecimal anomalyConfidence;

    /**
     * 风险等级：LOW-低风险，MEDIUM-中风险，HIGH-高风险
     */
    private String riskLevel;

    /**
     * 异常类型：HIGH_FREQUENCY-高频，BURST-突发，SUSPICIOUS_PATTERN-可疑模式
     */
    private String anomalyType;

    /**
     * 最小消费间隔（秒）
     */
    private Integer minIntervalSeconds;

    /**
     * 平均消费间隔（秒）
     */
    private Integer averageIntervalSeconds;

    /**
     * 最大消费间隔（秒）
     */
    private Integer maxIntervalSeconds;

    /**
     * 24小时内消费次数
     */
    private Integer consumeCount24h;

    /**
     * 1小时内消费次数
     */
    private Integer consumeCount1h;

    /**
     * 30分钟内消费次数
     */
    private Integer consumeCount30m;

    /**
     * 连续消费次数
     */
    private Integer consecutiveConsumeCount;

    /**
     * 消费模式：REGULAR-规律，IRREGULAR-不规律，BURST-突发，CLUSTERED-聚集
     */
    private String consumePattern;

    /**
     * 检测时间
     */
    private LocalDateTime detectionTime;

    /**
     * 异常描述
     */
    private String anomalyDescription;

    /**
     * 建议处理方式
     */
    private String recommendedAction;

    /**
     * 是否需要人工审核
     */
    private Boolean requiresManualReview;

    /**
     * 匹配的规则ID
     */
    private Long ruleId;

    /**
     * 匹配的规则名称
     */
    private String ruleName;

    /**
     * 频率可信度评分（0-100）
     */
    private BigDecimal frequencyTrustScore;

    /**
     * 是否为机器行为特征
     */
    private Boolean isMachineLike;

    /**
     * 相同位置消费次数
     */
    private Integer sameLocationCount;

    /**
     * 相同设备消费次数
     */
    private Integer sameDeviceCount;

    /**
     * 相同金额消费次数
     */
    private Integer sameAmountCount;

    /**
     * 检测模型版本
     */
    private String detectionModelVersion;

    /**
     * 扩展信息
     */
    private String extendedInfo;

    /**
     * 创建高频消费异常结果
     */
    public static FrequencyAnomalyResult highFrequency(Long userId, Long consumeRecordId, Integer consumeCount1h,
                                                     BigDecimal historicalAverage, BigDecimal deviationRate) {
        return FrequencyAnomalyResult.builder()
                .userId(userId)
                .consumeRecordId(consumeRecordId)
                .timeWindowMinutes(60)
                .consumeCount(consumeCount1h)
                .historicalAverageFrequency(historicalAverage)
                .currentFrequency(new BigDecimal(consumeCount1h))
                .deviationRate(deviationRate)
                .anomalyType("HIGH_FREQUENCY")
                .anomalyConfidence(new BigDecimal("80.0"))
                .riskLevel("MEDIUM")
                .requiresManualReview(true)
                .detectionTime(LocalDateTime.now())
                .anomalyDescription("消费频率明显高于历史水平")
                .recommendedAction("建议监控后续消费行为")
                .consumePattern("BURST")
                .build();
    }

    /**
     * 创建突发消费异常结果
     */
    public static FrequencyAnomalyResult burstConsume(Long userId, Long consumeRecordId, Integer consumeCount30m,
                                                    Integer consecutiveCount) {
        return FrequencyAnomalyResult.builder()
                .userId(userId)
                .consumeRecordId(consumeRecordId)
                .timeWindowMinutes(30)
                .consumeCount(consumeCount30m)
                .consecutiveConsumeCount(consecutiveCount)
                .anomalyType("BURST")
                .anomalyConfidence(new BigDecimal("85.0"))
                .riskLevel("HIGH")
                .requiresManualReview(true)
                .detectionTime(LocalDateTime.now())
                .anomalyDescription("短时间内突发密集消费")
                .recommendedAction("建议暂停消费权限并联系用户")
                .consumePattern("BURST")
                .isMachineLike(true)
                .build();
    }

    /**
     * 创建可疑模式异常结果
     */
    public static FrequencyAnomalyResult suspiciousPattern(Long userId, Long consumeRecordId,
                                                          Integer sameLocationCount, Integer sameDeviceCount,
                                                          Integer sameAmountCount) {
        return FrequencyAnomalyResult.builder()
                .userId(userId)
                .consumeRecordId(consumeRecordId)
                .sameLocationCount(sameLocationCount)
                .sameDeviceCount(sameDeviceCount)
                .sameAmountCount(sameAmountCount)
                .anomalyType("SUSPICIOUS_PATTERN")
                .anomalyConfidence(new BigDecimal("75.0"))
                .riskLevel("MEDIUM")
                .requiresManualReview(true)
                .detectionTime(LocalDateTime.now())
                .anomalyDescription("检测到可疑的消费模式")
                .recommendedAction("建议进行详细的行为分析")
                .consumePattern("CLUSTERED")
                .build();
    }

    /**
     * 创建机器行为异常结果
     */
    public static FrequencyAnomalyResult machineLikeBehavior(Long userId, Long consumeRecordId, Integer minInterval,
                                                           Integer consumeCount1h) {
        return FrequencyAnomalyResult.builder()
                .userId(userId)
                .consumeRecordId(consumeRecordId)
                .minIntervalSeconds(minInterval)
                .consumeCount1h(consumeCount1h)
                .anomalyType("HIGH_FREQUENCY")
                .anomalyConfidence(new BigDecimal("95.0"))
                .riskLevel("HIGH")
                .requiresManualReview(true)
                .detectionTime(LocalDateTime.now())
                .anomalyDescription("消费行为模式类似机器操作")
                .recommendedAction("立即暂停账户并调查")
                .consumePattern("REGULAR")
                .isMachineLike(true)
                .build();
    }

    /**
     * 检查是否为高风险
     */
    public boolean isHighRisk() {
        return "HIGH".equals(riskLevel);
    }

    /**
     * 检查是否需要人工审核
     */
    public boolean needsReview() {
        return Boolean.TRUE.equals(requiresManualReview);
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
            default:
                return "未知风险";
        }
    }

    /**
     * 获取异常类型描述
     */
    public String getAnomalyTypeDescription() {
        switch (anomalyType) {
            case "HIGH_FREQUENCY":
                return "高频消费";
            case "BURST":
                return "突发消费";
            case "SUSPICIOUS_PATTERN":
                return "可疑模式";
            default:
                return "未知异常";
        }
    }

    /**
     * 获取消费模式描述
     */
    public String getConsumePatternDescription() {
        switch (consumePattern) {
            case "REGULAR":
                return "规律消费";
            case "IRREGULAR":
                return "不规律消费";
            case "BURST":
                return "突发消费";
            case "CLUSTERED":
                return "聚集消费";
            default:
                return "未知模式";
        }
    }

    /**
     * 获取格式化的频率偏离率
     */
    public String getFormattedDeviationRate() {
        if (deviationRate == null) {
            return "0%";
        }
        return deviationRate.setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
    }

    /**
     * 获取格式化的时间间隔
     */
    public String getFormattedMinInterval() {
        if (minIntervalSeconds == null) {
            return "0秒";
        }
        if (minIntervalSeconds < 60) {
            return minIntervalSeconds + "秒";
        } else {
            int minutes = minIntervalSeconds / 60;
            int seconds = minIntervalSeconds % 60;
            if (seconds == 0) {
                return minutes + "分钟";
            } else {
                return minutes + "分钟" + seconds + "秒";
            }
        }
    }

    /**
     * 检查是否存在可疑的重复行为
     */
    public boolean hasSuspiciousRepetition() {
        return (sameLocationCount != null && sameLocationCount >= 5) ||
               (sameDeviceCount != null && sameDeviceCount >= 5) ||
               (sameAmountCount != null && sameAmountCount >= 5);
    }

    /**
     * 检查是否可能为机器行为
     */
    public boolean isLikelyMachineBehavior() {
        return Boolean.TRUE.equals(isMachineLike) ||
               (minIntervalSeconds != null && minIntervalSeconds < 10) ||
               (consecutiveConsumeCount != null && consecutiveConsumeCount >= 10);
    }
}