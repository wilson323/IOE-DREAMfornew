package net.lab1024.sa.consume.domain.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 账户类别消费模式配置 - 对应POSID_ACCOUNTKIND.mode_config字段
 *
 * 支持6种消费模式：
 * - FIXED_AMOUNT: 固定金额模式（SIMPLE/KEYVALUE/SECTION子类型）
 * - FREE_AMOUNT: 自由金额模式
 * - METERED: 计量计费模式（TIMING/COUNTING子类型）
 * - PRODUCT: 商品模式
 * - ORDER: 订餐模式
 * - INTELLIGENCE: 智能模式
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
@Data
public class ModeConfig {

    /**
     * 消费模式类型
     */
    @JsonProperty("consumeMode")
    private String consumeMode;

    /**
     * 固定金额模式配置
     */
    @JsonProperty("FIXED_AMOUNT")
    private FixedAmountConfig fixedAmount;

    /**
     * 自由金额模式配置
     */
    @JsonProperty("FREE_AMOUNT")
    private FreeAmountConfig freeAmount;

    /**
     * 计量计费模式配置
     */
    @JsonProperty("METERED")
    private MeteredConfig metered;

    /**
     * 商品模式配置
     */
    @JsonProperty("PRODUCT")
    private ProductConfig product;

    /**
     * 订餐模式配置
     */
    @JsonProperty("ORDER")
    private OrderConfig order;

    /**
     * 智能模式配置
     */
    @JsonProperty("INTELLIGENCE")
    private IntelligenceConfig intelligence;

    /**
     * 固定金额模式配置
     */
    @Data
    public static class FixedAmountConfig {
        /**
         * 子类型：SIMPLE-简单固定, KEYVALUE-餐别映射, SECTION-时段区间
         */
        @JsonProperty("subType")
        private String subType;

        /**
         * 简单固定金额（SIMPLE子类型）
         */
        @JsonProperty("amount")
        private BigDecimal amount;

        /**
         * 餐别金额映射（KEYVALUE子类型）
         * key: 餐别编码（BREAKFAST/LUNCH/DINNER/SNACK）
         * value: 金额
         */
        @JsonProperty("mealAmounts")
        private Map<String, BigDecimal> mealAmounts;

        /**
         * 时段区间金额（SECTION子类型）
         * key: 时段编码（MORNING/AFTERNOON/EVENING/NIGHT）
         * value: 金额
         */
        @JsonProperty("timeSectionAmounts")
        private Map<String, BigDecimal> timeSectionAmounts;
    }

    /**
     * 自由金额模式配置
     */
    @Data
    public static class FreeAmountConfig {
        /**
         * 最大金额限制
         */
        @JsonProperty("maxAmount")
        private BigDecimal maxAmount;

        /**
         * 最小金额限制
         */
        @JsonProperty("minAmount")
        private BigDecimal minAmount;
    }

    /**
     * 计量计费模式配置
     */
    @Data
    public static class MeteredConfig {
        /**
         * 子类型：TIMING-计时, COUNTING-计数
         */
        @JsonProperty("subType")
        private String subType;

        /**
         * 单价（元/单位）
         */
        @JsonProperty("unitPrice")
        private BigDecimal unitPrice;

        /**
         * 单位名称（分钟/次/个等）
         */
        @JsonProperty("unitName")
        private String unitName;
    }

    /**
     * 商品模式配置
     */
    @Data
    public static class ProductConfig {
        /**
         * 是否支持多商品
         */
        @JsonProperty("supportMultiple")
        private Boolean supportMultiple;

        /**
         * 最大商品数量
         */
        @JsonProperty("maxProductCount")
        private Integer maxProductCount;
    }

    /**
     * 订餐模式配置
     */
    @Data
    public static class OrderConfig {
        /**
         * 提前订餐时长（小时）
         */
        @JsonProperty("advanceHours")
        private Integer advanceHours;

        /**
         * 取餐时段
         */
        @JsonProperty("pickupTimeRange")
        private String pickupTimeRange;

        /**
         * 扣款时间策略：PICKUP-取餐时扣款, ORDER-下单时扣款
         */
        @JsonProperty("deductTimeStrategy")
        private String deductTimeStrategy;
    }

    /**
     * 智能模式配置
     */
    @Data
    public static class IntelligenceConfig {
        /**
         * 推荐算法类型：HISTORY-历史偏好, POPULARITY-热门推荐, HYBRID-混合
         */
        @JsonProperty("algorithm")
        private String algorithm;

        /**
         * 推荐数量
         */
        @JsonProperty("recommendCount")
        private Integer recommendCount;
    }
}
