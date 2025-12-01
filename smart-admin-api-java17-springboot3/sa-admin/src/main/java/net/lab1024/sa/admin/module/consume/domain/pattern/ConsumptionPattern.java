package net.lab1024.sa.admin.module.consume.domain.pattern;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 消费模式
 * 严格遵循repowiki规范：数据传输对象，包含用户消费行为的模式特征
 *
 * @author SmartAdmin Team
 * @date 2025/11/19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumptionPattern {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 模式ID
     */
    private Long patternId;

    /**
     * 模式名称
     */
    private String patternName;

    /**
     * 模式类型：REGULAR-规律型，IRREGULAR-不规律型，BINGE-暴饮暴食型，FRUGAL-节约型
     */
    private String patternType;

    /**
     * 主要消费金额范围（最小值）
     */
    private BigDecimal primaryAmountMin;

    /**
     * 主要消费金额范围（最大值）
     */
    private BigDecimal primaryAmountMax;

    /**
     * 平均消费金额
     */
    private BigDecimal averageAmount;

    /**
     * 消费频率（次/天）
     */
    private BigDecimal consumeFrequency;

    /**
     * 主要消费时段：MORNING-早晨，NOON-中午，AFTERNOON-下午，EVENING-晚上
     */
    private String primaryTimeSlot;

    /**
     * 主要消费地点类型：CANTEEN-食堂，RESTAURANT-餐厅，SUPERMARKET-超市，ONLINE-线上
     */
    private String primaryLocationType;

    /**
     * 偏好商品类别
     */
    private String preferredCategory;

    /**
     * 模式稳定性评分（0-100）
     */
    private BigDecimal stabilityScore;

    /**
     * 模式预测性评分（0-100）
     */
    private BigDecimal predictabilityScore;

    /**
     * 模式复杂度评分（0-100）
     */
    private BigDecimal complexityScore;

    /**
     * 数据样本量
     */
    private Integer sampleSize;

    /**
     * 分析窗口期（天）
     */
    private Integer analysisWindow;

    /**
     * 模式创建时间
     */
    private LocalDateTime createTime;

    /**
     * 模式更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 模式有效期
     */
    private LocalDateTime validUntil;

    /**
     * 是否为当前有效模式
     */
    private Boolean isActive;

    /**
     * 季节性特征
     */
    private Map<String, Object> seasonalFeatures;

    /**
     * 周期性特征
     */
    private Map<String, Object> periodicFeatures;

    /**
     * 异常行为特征
     */
    private List<String> anomalyFeatures;

    /**
     * 消费偏好特征
     */
    private Map<String, Object> preferenceFeatures;

    /**
     * 时间分布特征
     */
    private Map<String, BigDecimal> timeDistribution;

    /**
     * 金额分布特征
     */
    private Map<String, BigDecimal> amountDistribution;

    /**
     * 地点分布特征
     */
    private Map<String, BigDecimal> locationDistribution;

    /**
     * 品类分布特征
     */
    private Map<String, BigDecimal> categoryDistribution;

    /**
     * 模式置信度
     */
    private BigDecimal confidence;

    /**
     * 预测准确率
     */
    private BigDecimal predictionAccuracy;

    /**
     * 模式版本
     */
    private String patternVersion;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedAttributes;

    /**
     * 创建规律型消费模式
     */
    public static ConsumptionPattern regularPattern(Long userId, BigDecimal averageAmount,
                                                   BigDecimal frequency, String timeSlot) {
        return ConsumptionPattern.builder()
                .userId(userId)
                .patternType("REGULAR")
                .averageAmount(averageAmount)
                .primaryAmountMin(averageAmount.multiply(new BigDecimal("0.8")))
                .primaryAmountMax(averageAmount.multiply(new BigDecimal("1.2")))
                .consumeFrequency(frequency)
                .primaryTimeSlot(timeSlot)
                .stabilityScore(new BigDecimal("85.0"))
                .predictabilityScore(new BigDecimal("90.0"))
                .complexityScore(new BigDecimal("30.0"))
                .isActive(true)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusDays(30))
                .confidence(new BigDecimal("88.0"))
                .patternVersion("1.0")
                .build();
    }

    /**
     * 创建不规律型消费模式
     */
    public static ConsumptionPattern irregularPattern(Long userId, BigDecimal averageAmount,
                                                     BigDecimal frequency) {
        return ConsumptionPattern.builder()
                .userId(userId)
                .patternType("IRREGULAR")
                .averageAmount(averageAmount)
                .consumeFrequency(frequency)
                .stabilityScore(new BigDecimal("40.0"))
                .predictabilityScore(new BigDecimal("35.0"))
                .complexityScore(new BigDecimal("75.0"))
                .isActive(true)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusDays(7))
                .confidence(new BigDecimal("65.0"))
                .patternVersion("1.0")
                .build();
    }

    /**
     * 创建节约型消费模式
     */
    public static ConsumptionPattern frugalPattern(Long userId, BigDecimal averageAmount,
                                                  BigDecimal frequency) {
        return ConsumptionPattern.builder()
                .userId(userId)
                .patternType("FRUGAL")
                .averageAmount(averageAmount)
                .primaryAmountMin(BigDecimal.ZERO)
                .primaryAmountMax(averageAmount.multiply(new BigDecimal("1.5")))
                .consumeFrequency(frequency)
                .stabilityScore(new BigDecimal("75.0"))
                .predictabilityScore(new BigDecimal("70.0"))
                .complexityScore(new BigDecimal("45.0"))
                .isActive(true)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusDays(21))
                .confidence(new BigDecimal("80.0"))
                .patternVersion("1.0")
                .build();
    }

    /**
     * 检查模式是否有效
     */
    public boolean isValid() {
        return Boolean.TRUE.equals(isActive) && validUntil != null && validUntil.isAfter(LocalDateTime.now());
    }

    /**
     * 检查是否为稳定模式
     */
    public boolean isStable() {
        return stabilityScore != null && stabilityScore.compareTo(new BigDecimal("70")) >= 0;
    }

    /**
     * 检查是否可预测
     */
    public boolean isPredictable() {
        return predictabilityScore != null && predictabilityScore.compareTo(new BigDecimal("70")) >= 0;
    }

    /**
     * 获取模式类型描述
     */
    public String getPatternTypeDescription() {
        switch (patternType) {
            case "REGULAR":
                return "规律型";
            case "IRREGULAR":
                return "不规律型";
            case "BINGE":
                return "暴饮暴食型";
            case "FRUGAL":
                return "节约型";
            default:
                return "未知模式";
        }
    }

    /**
     * 获取时段描述
     */
    public String getTimeSlotDescription() {
        switch (primaryTimeSlot) {
            case "MORNING":
                return "早晨";
            case "NOON":
                return "中午";
            case "AFTERNOON":
                return "下午";
            case "EVENING":
                return "晚上";
            default:
                return "全天";
        }
    }

    /**
     * 获取地点类型描述
     */
    public String getLocationTypeDescription() {
        switch (primaryLocationType) {
            case "CANTEEN":
                return "食堂";
            case "RESTAURANT":
                return "餐厅";
            case "SUPERMARKET":
                return "超市";
            case "ONLINE":
                return "线上";
            default:
                return "其他";
        }
    }

    /**
     * 获取格式化的平均金额
     */
    public String getFormattedAverageAmount() {
        if (averageAmount == null) {
            return "￥0.00";
        }
        return "￥" + averageAmount.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * 获取格式化的消费频率
     */
    public String getFormattedFrequency() {
        if (consumeFrequency == null) {
            return "0次/天";
        }
        return consumeFrequency.setScale(2, BigDecimal.ROUND_HALF_UP) + "次/天";
    }

    /**
     * 检查金额是否在主要范围内
     */
    public boolean isInPrimaryAmountRange(BigDecimal amount) {
        if (amount == null || primaryAmountMin == null || primaryAmountMax == null) {
            return false;
        }
        return amount.compareTo(primaryAmountMin) >= 0 && amount.compareTo(primaryAmountMax) <= 0;
    }

    /**
     * 更新模式
     */
    public void updatePattern() {
        this.updateTime = LocalDateTime.now();
        // 根据业务逻辑更新其他属性
        if (this.validUntil != null) {
            this.validUntil = LocalDateTime.now().plusDays(30);
        }
    }

    /**
     * 获取模式质量评分
     */
    public BigDecimal getPatternQualityScore() {
        if (stabilityScore == null || predictabilityScore == null) {
            return BigDecimal.ZERO;
        }
        return stabilityScore.add(predictabilityScore).divide(new BigDecimal("2"), 2, BigDecimal.ROUND_HALF_UP);
    }
}