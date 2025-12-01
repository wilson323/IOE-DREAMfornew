package net.lab1024.sa.admin.module.consume.domain.result;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 序列异常检测结果
 * 严格遵循repowiki规范：数据传输对象，包含序列异常检测的结果信息
 *
 * @author SmartAdmin Team
 * @date 2025/11/19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SequenceAnomalyResult {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 消费记录ID
     */
    private Long consumeRecordId;

    /**
     * 序列异常类型：OUT_OF_ORDER-顺序异常，MISSING-缺失记录，DUPLICATE-重复记录
     */
    private String anomalyType;

    /**
     * 异常置信度（0-100）
     */
    private BigDecimal anomalyConfidence;

    /**
     * 风险等级：LOW-低风险，MEDIUM-中风险，HIGH-高风险
     */
    private String riskLevel;

    /**
     * 期望的序列号
     */
    private Long expectedSequenceNumber;

    /**
     * 实际的序列号
     */
    private Long actualSequenceNumber;

    /**
     * 序列偏离值
     */
    private Long sequenceDeviation;

    /**
     * 相关的消费记录ID列表
     */
    private List<Long> relatedConsumeRecordIds;

    /**
     * 序列时间戳
     */
    private LocalDateTime sequenceTimestamp;

    /**
     * 前一条记录时间戳
     */
    private LocalDateTime previousRecordTimestamp;

    /**
     * 后一条记录时间戳
     */
    private LocalDateTime nextRecordTimestamp;

    /**
     * 时间间隔异常
     */
    private Boolean hasTimeIntervalAnomaly;

    /**
     * 时间间隔秒数
     */
    private Long intervalSeconds;

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
     * 序列完整性评分（0-100）
     */
    private BigDecimal sequenceIntegrityScore;

    /**
     * 序列一致性评分（0-100）
     */
    private BigDecimal sequenceConsistencyScore;

    /**
     * 影响的记录数量
     */
    private Integer affectedRecordCount;

    /**
     * 是否影响账务平衡
     */
    private Boolean affectsAccountBalance;

    /**
     * 序列异常详细信息
     */
    private Map<String, Object> anomalyDetails;

    /**
     * 检测模型版本
     */
    private String detectionModelVersion;

    /**
     * 扩展信息
     */
    private String extendedInfo;

    /**
     * 创建顺序异常结果
     */
    public static SequenceAnomalyResult outOfOrder(Long userId, Long consumeRecordId,
                                                   Long expectedSequence, Long actualSequence) {
        return SequenceAnomalyResult.builder()
                .userId(userId)
                .consumeRecordId(consumeRecordId)
                .anomalyType("OUT_OF_ORDER")
                .expectedSequenceNumber(expectedSequence)
                .actualSequenceNumber(actualSequence)
                .sequenceDeviation(actualSequence - expectedSequence)
                .anomalyConfidence(new BigDecimal("70.0"))
                .riskLevel("MEDIUM")
                .requiresManualReview(true)
                .detectionTime(LocalDateTime.now())
                .anomalyDescription("消费记录序列号不连续")
                .recommendedAction("检查相邻记录的完整性")
                .sequenceTimestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建缺失记录异常结果
     */
    public static SequenceAnomalyResult missingRecord(Long userId, Long expectedSequence,
                                                     List<Long> affectedRecordIds) {
        return SequenceAnomalyResult.builder()
                .userId(userId)
                .anomalyType("MISSING")
                .expectedSequenceNumber(expectedSequence)
                .actualSequenceNumber(null)
                .relatedConsumeRecordIds(affectedRecordIds)
                .affectedRecordCount(affectedRecordIds.size())
                .anomalyConfidence(new BigDecimal("90.0"))
                .riskLevel("HIGH")
                .requiresManualReview(true)
                .detectionTime(LocalDateTime.now())
                .anomalyDescription("检测到缺失的消费记录")
                .recommendedAction("立即调查缺失原因并补充记录")
                .affectsAccountBalance(true)
                .build();
    }

    /**
     * 创建重复记录异常结果
     */
    public static SequenceAnomalyResult duplicateRecord(Long userId, Long consumeRecordId,
                                                       List<Long> duplicateRecordIds) {
        return SequenceAnomalyResult.builder()
                .userId(userId)
                .consumeRecordId(consumeRecordId)
                .anomalyType("DUPLICATE")
                .relatedConsumeRecordIds(duplicateRecordIds)
                .affectedRecordCount(duplicateRecordIds.size())
                .anomalyConfidence(new BigDecimal("95.0"))
                .riskLevel("HIGH")
                .requiresManualReview(true)
                .detectionTime(LocalDateTime.now())
                .anomalyDescription("检测到重复的消费记录")
                .recommendedAction("立即删除重复记录并调查原因")
                .affectsAccountBalance(true)
                .build();
    }

    /**
     * 创建时间间隔异常结果
     */
    public static SequenceAnomalyResult timeIntervalAnomaly(Long userId, Long consumeRecordId,
                                                           Long intervalSeconds, LocalDateTime previousTime,
                                                           LocalDateTime nextTime) {
        return SequenceAnomalyResult.builder()
                .userId(userId)
                .consumeRecordId(consumeRecordId)
                .anomalyType("OUT_OF_ORDER")
                .hasTimeIntervalAnomaly(true)
                .intervalSeconds(intervalSeconds)
                .previousRecordTimestamp(previousTime)
                .nextRecordTimestamp(nextTime)
                .anomalyConfidence(new BigDecimal("60.0"))
                .riskLevel("LOW")
                .requiresManualReview(false)
                .detectionTime(LocalDateTime.now())
                .anomalyDescription("消费记录时间间隔异常")
                .recommendedAction("监控后续记录的时间序列")
                .sequenceTimestamp(LocalDateTime.now())
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
     * 检查是否影响账务
     */
    public boolean affectsAccounting() {
        return Boolean.TRUE.equals(affectsAccountBalance);
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
            case "OUT_OF_ORDER":
                return "顺序异常";
            case "MISSING":
                return "记录缺失";
            case "DUPLICATE":
                return "重复记录";
            default:
                return "未知异常";
        }
    }

    /**
     * 获取序列偏离描述
     */
    public String getSequenceDeviationDescription() {
        if (sequenceDeviation == null) {
            return "无偏离";
        }
        if (sequenceDeviation > 0) {
            return "偏离 +" + sequenceDeviation;
        } else if (sequenceDeviation < 0) {
            return "偏离 " + sequenceDeviation;
        } else {
            return "无偏离";
        }
    }

    /**
     * 获取格式化的时间间隔
     */
    public String getFormattedTimeInterval() {
        if (intervalSeconds == null) {
            return "0秒";
        }
        if (intervalSeconds < 60) {
            return intervalSeconds + "秒";
        } else if (intervalSeconds < 3600) {
            long minutes = intervalSeconds / 60;
            long seconds = intervalSeconds % 60;
            if (seconds == 0) {
                return minutes + "分钟";
            } else {
                return minutes + "分钟" + seconds + "秒";
            }
        } else {
            long hours = intervalSeconds / 3600;
            long minutes = (intervalSeconds % 3600) / 60;
            if (minutes == 0) {
                return hours + "小时";
            } else {
                return hours + "小时" + minutes + "分钟";
            }
        }
    }

    /**
     * 获取序列质量评分
     */
    public BigDecimal getSequenceQualityScore() {
        if (sequenceIntegrityScore == null || sequenceConsistencyScore == null) {
            return BigDecimal.ZERO;
        }
        return sequenceIntegrityScore.add(sequenceConsistencyScore).divide(new BigDecimal("2"), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 检查序列质量是否良好
     */
    public boolean isSequenceQualityGood() {
        BigDecimal qualityScore = getSequenceQualityScore();
        return qualityScore.compareTo(new BigDecimal("80")) >= 0;
    }

    /**
     * 获取严重程度描述
     */
    public String getSeverityDescription() {
        if (isHighRisk()) {
            return "严重";
        } else if ("MEDIUM".equals(riskLevel)) {
            return "中等";
        } else {
            return "轻微";
        }
    }
}