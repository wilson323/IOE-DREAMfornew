package net.lab1024.sa.admin.module.consume.domain.result;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 金额异常检测结果
 * 严格遵循repowiki规范：数据传输对象，包含金额异常检测的结果信息
 *
 * @author SmartAdmin Team
 * @date 2025/11/19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmountAnomalyResult {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 消费记录ID
     */
    private Long consumeRecordId;

    /**
     * 消费金额
     */
    private BigDecimal consumeAmount;

    /**
     * 历史平均金额
     */
    private BigDecimal historicalAverage;

    /**
     * 历史最高金额
     */
    private BigDecimal historicalMax;

    /**
     * 历史最低金额
     */
    private BigDecimal historicalMin;

    /**
     * 金额偏离率（百分比）
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
     * 金额异常类型：HIGH-金额过高，LOW-金额过低，FREQUENT_HIGH-频繁高额，ZERO-零金额
     */
    private String anomalyType;

    /**
     * 是否首次高额消费
     */
    private Boolean isFirstHighAmount;

    /**
     * 历史高额消费次数
     */
    private Integer highAmountCount;

    /**
     * 当月累计消费金额
     */
    private BigDecimal monthlyTotalAmount;

    /**
     * 当月消费次数
     */
    private Integer monthlyConsumeCount;

    /**
     * 剩余余额
     */
    private BigDecimal remainingBalance;

    /**
     * 余额占比（消费金额/余额）
     */
    private BigDecimal balanceRatio;

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
     * 金额可信度评分（0-100）
     */
    private BigDecimal amountTrustScore;

    /**
     * 24小时内累计消费
     */
    private BigDecimal dailyTotalAmount;

    /**
     * 单日最高消费限额
     */
    private BigDecimal dailyLimit;

    /**
     * 单次最高消费限额
     */
    private BigDecimal singleLimit;

    /**
     * 检测模型版本
     */
    private String detectionModelVersion;

    /**
     * 扩展信息
     */
    private String extendedInfo;

    /**
     * 创建高额消费异常结果
     */
    public static AmountAnomalyResult highAmount(Long userId, Long consumeRecordId, BigDecimal consumeAmount,
                                               BigDecimal historicalAverage, BigDecimal deviationRate) {
        return AmountAnomalyResult.builder()
                .userId(userId)
                .consumeRecordId(consumeRecordId)
                .consumeAmount(consumeAmount)
                .historicalAverage(historicalAverage)
                .deviationRate(deviationRate)
                .anomalyType("HIGH")
                .anomalyConfidence(new BigDecimal("80.0"))
                .riskLevel("MEDIUM")
                .requiresManualReview(true)
                .detectionTime(LocalDateTime.now())
                .anomalyDescription("消费金额明显高于历史平均水平")
                .recommendedAction("建议确认消费场景")
                .build();
    }

    /**
     * 创建超额消费异常结果
     */
    public static AmountAnomalyResult exceedLimit(Long userId, Long consumeRecordId, BigDecimal consumeAmount,
                                                 BigDecimal dailyLimit, BigDecimal dailyTotalAmount) {
        return AmountAnomalyResult.builder()
                .userId(userId)
                .consumeRecordId(consumeRecordId)
                .consumeAmount(consumeAmount)
                .dailyLimit(dailyLimit)
                .dailyTotalAmount(dailyTotalAmount)
                .anomalyType("HIGH")
                .anomalyConfidence(new BigDecimal("95.0"))
                .riskLevel("HIGH")
                .requiresManualReview(true)
                .detectionTime(LocalDateTime.now())
                .anomalyDescription("单日累计消费超过限额")
                .recommendedAction("立即暂停消费权限并联系用户")
                .build();
    }

    /**
     * 创建余额不足异常结果
     */
    public static AmountAnomalyResult insufficientBalance(Long userId, Long consumeRecordId, BigDecimal consumeAmount,
                                                        BigDecimal remainingBalance) {
        return AmountAnomalyResult.builder()
                .userId(userId)
                .consumeRecordId(consumeRecordId)
                .consumeAmount(consumeAmount)
                .remainingBalance(remainingBalance)
                .anomalyType("HIGH")
                .anomalyConfidence(new BigDecimal("100.0"))
                .riskLevel("HIGH")
                .requiresManualReview(true)
                .detectionTime(LocalDateTime.now())
                .anomalyDescription("账户余额不足")
                .recommendedAction("拒绝交易并提醒用户充值")
                .build();
    }

    /**
     * 创建频繁高额消费异常结果
     */
    public static AmountAnomalyResult frequentHighAmount(Long userId, Long consumeRecordId, BigDecimal consumeAmount,
                                                       Integer highAmountCount, BigDecimal dailyTotalAmount) {
        return AmountAnomalyResult.builder()
                .userId(userId)
                .consumeRecordId(consumeRecordId)
                .consumeAmount(consumeAmount)
                .highAmountCount(highAmountCount)
                .dailyTotalAmount(dailyTotalAmount)
                .anomalyType("FREQUENT_HIGH")
                .anomalyConfidence(new BigDecimal("85.0"))
                .riskLevel("HIGH")
                .requiresManualReview(true)
                .detectionTime(LocalDateTime.now())
                .anomalyDescription("24小时内频繁高额消费")
                .recommendedAction("建议暂时冻结账户并联系用户")
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
            case "HIGH":
                return "金额过高";
            case "LOW":
                return "金额过低";
            case "FREQUENT_HIGH":
                return "频繁高额";
            case "ZERO":
                return "零金额";
            default:
                return "未知异常";
        }
    }

    /**
     * 获取格式化的金额偏离率
     */
    public String getFormattedDeviationRate() {
        if (deviationRate == null) {
            return "0%";
        }
        return deviationRate.setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
    }

    /**
     * 获取格式化的余额占比
     */
    public String getFormattedBalanceRatio() {
        if (balanceRatio == null) {
            return "0%";
        }
        return balanceRatio.multiply(new BigDecimal("100")).setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
    }

    /**
     * 获取格式化的消费金额
     */
    public String getFormattedConsumeAmount() {
        if (consumeAmount == null) {
            return "￥0.00";
        }
        return "￥" + consumeAmount.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * 获取格式化的历史平均金额
     */
    public String getFormattedHistoricalAverage() {
        if (historicalAverage == null) {
            return "￥0.00";
        }
        return "￥" + historicalAverage.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }
}