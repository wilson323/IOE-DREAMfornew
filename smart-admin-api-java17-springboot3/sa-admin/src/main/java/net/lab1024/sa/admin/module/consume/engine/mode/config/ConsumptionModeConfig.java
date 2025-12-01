package net.lab1024.sa.admin.module.consume.engine.mode.config;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 消费模式配置
 * 定义消费模式的所有配置参数和行为规则
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Data
public class ConsumptionModeConfig {

    /**
     * 模式编码（唯一标识）
     */
    private String modeCode;

    /**
     * 模式名称
     */
    private String modeName;

    /**
     * 模式描述
     */
    private String description;

    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 优先级（数值越小优先级越高）
     */
    private int priority = 100;

    /**
     * 最大重试次数
     */
    private int maxRetryCount = 3;

    /**
     * 超时时间（秒）
     */
    private int timeoutSeconds = 30;

    /**
     * 是否支持降级
     */
    private boolean supportsFallback = true;

    /**
     * 模式特定配置
     */
    private Map<String, Object> modeSpecificConfig;

    /**
     * 时间限制配置
     */
    private TimeRestrictionConfig timeRestriction;

    /**
     * 金额限制配置
     */
    private AmountRestrictionConfig amountRestriction;

    /**
     * 频率限制配置
     */
    private FrequencyRestrictionConfig frequencyRestriction;

    /**
     * 补贴配置
     */
    private SubsidyConfig subsidyConfig;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private Long createUserId;

    /**
     * 更新人
     */
    private Long updateUserId;

    /**
     * 版本号
     */
    private Long version = 1L;

    /**
     * 时间限制配置
     */
    @Data
    public static class TimeRestrictionConfig {
        /**
         * 是否启用时间限制
         */
        private boolean enabled = false;

        /**
         * 允许的时间段（格式：HH:mm-HH:mm）
         */
        private String allowedTimeRange;

        /**
         * 允许的星期几（1-7，1表示周一）
         */
        private String allowedWeekdays;

        /**
         * 禁用的日期范围（格式：yyyy-MM-dd,yyyy-MM-dd）
         */
        private String disabledDateRanges;
    }

    /**
     * 金额限制配置
     */
    @Data
    public static class AmountRestrictionConfig {
        /**
         * 是否启用金额限制
         */
        private boolean enabled = false;

        /**
         * 最小消费金额
         */
        private BigDecimal minAmount;

        /**
         * 最大消费金额
         */
        private BigDecimal maxAmount;

        /**
         * 固定金额列表（用于固定金额模式）
         */
        private String fixedAmounts;

        /**
         * 金额递增步长
         */
        private BigDecimal amountStep;
    }

    /**
     * 频率限制配置
     */
    @Data
    public static class FrequencyRestrictionConfig {
        /**
         * 是否启用频率限制
         */
        private boolean enabled = false;

        /**
         * 时间窗口内最大消费次数
         */
        private int maxConsumptionsPerWindow;

        /**
         * 时间窗口长度（分钟）
         */
        private int windowSizeMinutes = 60;

        /**
         * 每日最大消费次数
         */
        private int maxConsumptionsPerDay;

        /**
         * 冷却时间（分钟）
         */
        private int cooldownMinutes = 0;
    }

    /**
     * 补贴配置
     */
    @Data
    public static class SubsidyConfig {
        /**
         * 是否启用补贴
         */
        private boolean enabled = false;

        /**
         * 补贴类型（FIXED_AMOUNT, PERCENTAGE, TIERED）
         */
        private String subsidyType;

        /**
         * 固定补贴金额
         */
        private BigDecimal fixedSubsidyAmount;

        /**
         * 补贴比例（百分比）
         */
        private BigDecimal subsidyPercentage;

        /**
         * 分级补贴配置
         */
        private String tieredSubsidyRules;

        /**
         * 每日最大补贴金额
         */
        private BigDecimal dailyMaxSubsidy;

        /**
         * 补贴开始时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime subsidyStartTime;

        /**
         * 补贴结束时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime subsidyEndTime;
    }

    /**
     * 获取模式特定配置值
     *
     * @param key 配置键
     * @param defaultValue 默认值
     * @param <T> 值类型
     * @return 配置值
     */
    @SuppressWarnings("unchecked")
    public <T> T getModeSpecificConfigValue(String key, T defaultValue) {
        if (modeSpecificConfig == null || !modeSpecificConfig.containsKey(key)) {
            return defaultValue;
        }
        try {
            return (T) modeSpecificConfig.get(key);
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    /**
     * 设置模式特定配置值
     *
     * @param key 配置键
     * @param value 配置值
     */
    public void setModeSpecificConfigValue(String key, Object value) {
        if (modeSpecificConfig == null) {
            modeSpecificConfig = new java.util.HashMap<>();
        }
        modeSpecificConfig.put(key, value);
    }

    /**
     * 检查配置是否在有效时间范围内
     *
     * @return 是否在有效时间内
     */
    public boolean isWithinValidTimeRange() {
        if (timeRestriction == null || !timeRestriction.isEnabled()) {
            return true;
        }

        // 这里应该实现具体的时间检查逻辑
        // 简化实现，实际项目中需要详细的时间解析和比较
        return true;
    }

    /**
     * 检查金额是否在限制范围内
     *
     * @param amount 消费金额
     * @return 是否符合金额限制
     */
    public boolean isAmountWithinRestriction(BigDecimal amount) {
        if (amountRestriction == null || !amountRestriction.isEnabled()) {
            return true;
        }

        // 检查最小金额
        if (amountRestriction.getMinAmount() != null &&
            amount.compareTo(amountRestriction.getMinAmount()) < 0) {
            return false;
        }

        // 检查最大金额
        if (amountRestriction.getMaxAmount() != null &&
            amount.compareTo(amountRestriction.getMaxAmount()) > 0) {
            return false;
        }

        // 检查固定金额列表
        if (amountRestriction.getFixedAmounts() != null &&
            !amountRestriction.getFixedAmounts().isEmpty()) {
            String[] amounts = amountRestriction.getFixedAmounts().split(",");
            for (String amountStr : amounts) {
                try {
                    BigDecimal fixedAmount = new BigDecimal(amountStr.trim());
                    if (amount.compareTo(fixedAmount) == 0) {
                        return true;
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
            }
            return false;
        }

        return true;
    }

    /**
     * 生成配置摘要
     *
     * @return 配置摘要字符串
     */
    public String generateSummary() {
        return String.format("消费模式配置: %s(%s) - 启用:%s, 优先级:%d, 支持降级:%s",
                modeName, modeCode, enabled, priority, supportsFallback);
    }
}