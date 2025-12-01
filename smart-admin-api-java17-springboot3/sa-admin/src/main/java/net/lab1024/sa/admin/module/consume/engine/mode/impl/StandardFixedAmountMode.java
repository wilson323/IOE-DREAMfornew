package net.lab1024.sa.admin.module.consume.engine.mode.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.engine.ConsumeRequest;
import net.lab1024.sa.admin.module.consume.engine.ConsumeResult;
import net.lab1024.sa.admin.module.consume.engine.mode.ConsumptionMode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 标准固定金额消费模式实现
 * 适用于餐厅、食堂等固定价格消费场景
 * <p>
 * 严格遵循repowiki规范：
 * - 实现ConsumptionMode接口
 * - 提供餐别控制、优惠折扣、手续费计算等功能
 * - 支持离线模式和基础设备
 * - 包含完整的参数验证和业务逻辑
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-27
 */
@Slf4j
public class StandardFixedAmountMode implements ConsumptionMode {

    private static final String MODE_CODE = "STANDARD_FIXED_AMOUNT";
    private static final String MODE_NAME = "标准固定金额模式";
    private static final String DESCRIPTION = "适用于餐厅、食堂等固定价格消费场景，支持餐别时段控制和价格配置";

    // 标准餐别价格映射
    private static final Map<String, BigDecimal> STANDARD_MEAL_PRICES = Map.of(
        "BREAKFAST", new BigDecimal("8.00"),
        "LUNCH", new BigDecimal("15.00"),
        "DINNER", new BigDecimal("18.00"),
        "STANDARD", new BigDecimal("15.00"),
        "PREMIUM", new BigDecimal("25.00"),
        "VIP", new BigDecimal("35.00")
    );

    @Override
    public String getModeCode() {
        return MODE_CODE;
    }

    @Override
    public String getModeName() {
        return MODE_NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public ConsumeResult process(ConsumeRequest request) {
        try {
            log.info("开始处理标准固定金额消费: personId={}, amount={}, orderNo={}",
                    request.getPersonId(), request.getAmount(), request.getOrderNo());

            Map<String, Object> modeConfig = request.getModeConfig();
            String mealType = getStringFromMap(modeConfig, "mealType", "STANDARD");
            BigDecimal baseAmount = request.getAmount();

            // 如果未提供金额，使用标准餐别价格
            if (baseAmount == null || baseAmount.compareTo(BigDecimal.ZERO) <= 0) {
                baseAmount = getStandardMealPrice(mealType);
                log.debug("使用标准餐别价格: mealType={}, price={}", mealType, baseAmount);
            }

            // 根据餐别类型调整基础金额
            BigDecimal adjustedAmount = calculateMealTypeAdjustment(mealType, baseAmount);

            // 应用时段优惠
            BigDecimal timeDiscount = BigDecimal.ZERO;
            Boolean hasTimeDiscount = getBooleanFromMap(modeConfig, "hasTimeDiscount", false);
            if (hasTimeDiscount) {
                timeDiscount = calculateTimeDiscount(adjustedAmount, modeConfig);
            }

            // 应用折扣
            BigDecimal discountAmount = BigDecimal.ZERO;
            Boolean hasDiscount = getBooleanFromMap(modeConfig, "hasDiscount", false);
            if (hasDiscount) {
                discountAmount = calculateDiscount(adjustedAmount, modeConfig);
            }

            // 应用会员优惠
            BigDecimal memberDiscount = BigDecimal.ZERO;
            Boolean isMember = getBooleanFromMap(modeConfig, "isMember", false);
            if (isMember) {
                memberDiscount = calculateMemberDiscount(adjustedAmount, modeConfig);
            }

            // 应用优惠券
            BigDecimal couponAmount = BigDecimal.ZERO;
            Boolean hasCoupon = getBooleanFromMap(modeConfig, "hasCoupon", false);
            if (hasCoupon) {
                couponAmount = calculateCouponDiscount(adjustedAmount, modeConfig);
            }

            // 计算最终金额（扣除所有优惠）
            BigDecimal finalAmount = adjustedAmount
                    .subtract(timeDiscount)
                    .subtract(discountAmount)
                    .subtract(memberDiscount)
                    .subtract(couponAmount);

            // 添加手续费（如果有）
            BigDecimal serviceFee = BigDecimal.ZERO;
            Boolean hasServiceFee = getBooleanFromMap(modeConfig, "hasServiceFee", false);
            if (hasServiceFee) {
                serviceFee = calculateServiceFee(finalAmount, modeConfig);
            }

            BigDecimal totalAmount = finalAmount.add(serviceFee);

            // 计算积分
            Integer pointsEarned = calculatePointsEarned(totalAmount, modeConfig);

            // 构建消费结果
            ConsumeResult result = ConsumeResult.builder()
                    .success(true)
                    .resultCode("SUCCESS")
                    .message("固定金额消费处理成功")
                    .actualAmount(totalAmount)
                    .discountAmount(timeDiscount.add(discountAmount).add(memberDiscount).add(couponAmount))
                    .feeAmount(serviceFee)
                    .finalAmount(totalAmount)
                    .consumptionMode(getModeCode())
                    .orderNo(request.getOrderNo())
                    .consumeTime(LocalDateTime.now())
                    .build();

            // 添加模式特定数据到扩展字段
            Map<String, Object> modeData = new HashMap<>();
            modeData.put("mealType", mealType);
            modeData.put("baseAmount", baseAmount);
            modeData.put("adjustedAmount", adjustedAmount);
            modeData.put("timeDiscount", timeDiscount);
            modeData.put("discountAmount", discountAmount);
            modeData.put("memberDiscount", memberDiscount);
            modeData.put("couponAmount", couponAmount);
            modeData.put("serviceFee", serviceFee);
            modeData.put("pointsEarned", pointsEarned);
            modeData.put("totalDiscount", timeDiscount.add(discountAmount).add(memberDiscount).add(couponAmount));

            if (request.getExtendData() != null) {
                request.getExtendData().put("modeData", modeData);
            }

            log.info("固定金额消费处理完成: orderNo={}, finalAmount={}, pointsEarned={}",
                    request.getOrderNo(), totalAmount, pointsEarned);
            return result;

        } catch (Exception e) {
            log.error("固定金额模式处理异常", e);
            return ConsumeResult.failure("PROCESS_ERROR", "固定金额模式处理异常: " + e.getMessage());
        }
    }

    @Override
    public ValidationResult validate(ConsumeRequest request) {
        try {
            // 基础参数验证
            if (request.getPersonId() == null) {
                return ValidationResult.failure("INVALID_PERSON", "人员ID不能为空");
            }

            if (request.getDeviceId() == null) {
                return ValidationResult.failure("INVALID_DEVICE", "设备ID不能为空");
            }

            Map<String, Object> modeConfig = request.getModeConfig();
            if (modeConfig == null) {
                modeConfig = new HashMap<>();
            }

            // 验证餐别类型
            String mealType = getStringFromMap(modeConfig, "mealType", "STANDARD");
            if (!isValidMealType(mealType)) {
                return ValidationResult.failure("INVALID_MEAL_TYPE", "无效的餐别类型: " + mealType);
            }

            // 验证折扣配置
            Boolean hasDiscount = getBooleanFromMap(modeConfig, "hasDiscount", false);
            if (hasDiscount) {
                BigDecimal discountRate = getBigDecimalFromMap(modeConfig, "discountRate", null);
                if (discountRate == null || discountRate.compareTo(BigDecimal.ZERO) < 0
                        || discountRate.compareTo(BigDecimal.ONE) > 0) {
                    return ValidationResult.failure("INVALID_DISCOUNT_RATE", "折扣率必须在0-1之间");
                }
            }

            // 验证会员优惠配置
            Boolean isMember = getBooleanFromMap(modeConfig, "isMember", false);
            if (isMember) {
                BigDecimal memberDiscountRate = getBigDecimalFromMap(modeConfig, "memberDiscountRate", null);
                if (memberDiscountRate == null || memberDiscountRate.compareTo(BigDecimal.ZERO) < 0
                        || memberDiscountRate.compareTo(BigDecimal.ONE) > 0) {
                    return ValidationResult.failure("INVALID_MEMBER_DISCOUNT_RATE", "会员折扣率必须在0-1之间");
                }
            }

            // 验证手续费配置
            Boolean hasServiceFee = getBooleanFromMap(modeConfig, "hasServiceFee", false);
            if (hasServiceFee) {
                BigDecimal serviceFeeRate = getBigDecimalFromMap(modeConfig, "serviceFeeRate", null);
                if (serviceFeeRate == null || serviceFeeRate.compareTo(BigDecimal.ZERO) < 0
                        || serviceFeeRate.compareTo(BigDecimal.ONE) > 0) {
                    return ValidationResult.failure("INVALID_SERVICE_FEE_RATE", "手续费率必须在0-1之间");
                }
            }

            return ValidationResult.success();

        } catch (Exception e) {
            log.error("固定金额模式验证异常", e);
            return ValidationResult.failure("VALIDATION_ERROR", "验证异常: " + e.getMessage());
        }
    }

    @Override
    public BigDecimal calculateAmount(ConsumeRequest request) {
        try {
            Map<String, Object> modeConfig = request.getModeConfig() != null ? request.getModeConfig() : new HashMap<>();
            String mealType = getStringFromMap(modeConfig, "mealType", "STANDARD");
            BigDecimal baseAmount = request.getAmount();

            if (baseAmount == null || baseAmount.compareTo(BigDecimal.ZERO) <= 0) {
                baseAmount = getStandardMealPrice(mealType);
            }

            BigDecimal adjustedAmount = calculateMealTypeAdjustment(mealType, baseAmount);

            // 应用时段优惠
            BigDecimal timeDiscount = BigDecimal.ZERO;
            Boolean hasTimeDiscount = getBooleanFromMap(modeConfig, "hasTimeDiscount", false);
            if (hasTimeDiscount) {
                timeDiscount = calculateTimeDiscount(adjustedAmount, modeConfig);
            }

            // 应用折扣
            BigDecimal discountAmount = BigDecimal.ZERO;
            Boolean hasDiscount = getBooleanFromMap(modeConfig, "hasDiscount", false);
            if (hasDiscount) {
                discountAmount = calculateDiscount(adjustedAmount, modeConfig);
            }

            // 应用会员优惠
            BigDecimal memberDiscount = BigDecimal.ZERO;
            Boolean isMember = getBooleanFromMap(modeConfig, "isMember", false);
            if (isMember) {
                memberDiscount = calculateMemberDiscount(adjustedAmount, modeConfig);
            }

            // 应用优惠券
            BigDecimal couponAmount = BigDecimal.ZERO;
            Boolean hasCoupon = getBooleanFromMap(modeConfig, "hasCoupon", false);
            if (hasCoupon) {
                couponAmount = calculateCouponDiscount(adjustedAmount, modeConfig);
            }

            // 计算最终金额
            BigDecimal finalAmount = adjustedAmount
                    .subtract(timeDiscount)
                    .subtract(discountAmount)
                    .subtract(memberDiscount)
                    .subtract(couponAmount);

            // 添加手续费
            BigDecimal serviceFee = BigDecimal.ZERO;
            Boolean hasServiceFee = getBooleanFromMap(modeConfig, "hasServiceFee", false);
            if (hasServiceFee) {
                serviceFee = calculateServiceFee(finalAmount, modeConfig);
            }

            return finalAmount.add(serviceFee).setScale(2, RoundingMode.HALF_UP);

        } catch (Exception e) {
            log.error("固定金额模式金额计算异常", e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public Map<String, Object> getConfigTemplate() {
        Map<String, Object> template = new HashMap<>();

        template.put("mealType", "STANDARD");
        template.put("baseAmount", new BigDecimal("15.00"));
        template.put("hasTimeDiscount", false);
        template.put("hasDiscount", false);
        template.put("isMember", false);
        template.put("hasCoupon", false);
        template.put("hasServiceFee", false);

        // 餐别类型选项
        String[] mealTypes = {"BREAKFAST", "LUNCH", "DINNER", "STANDARD", "PREMIUM", "VIP"};
        template.put("mealTypes", mealTypes);

        // 标准餐别价格
        template.put("standardPrices", STANDARD_MEAL_PRICES);

        return template;
    }

    @Override
    public boolean validateConfig(Map<String, Object> config) {
        if (config == null) {
            return true; // 允许空配置，使用默认值
        }

        // 验证餐别类型
        Object mealType = config.get("mealType");
        if (mealType != null && !isValidMealType(mealType.toString())) {
            return false;
        }

        return true;
    }

    @Override
    public boolean isApplicableTo(String deviceType) {
        // 固定金额模式适用于所有支持基础消费的设备
        return "CONSUME_TERMINAL".equals(deviceType) ||
               "POS_TERMINAL".equals(deviceType) ||
               "MOBILE_APP".equals(deviceType) ||
               "CANTEEN_TERMINAL".equals(deviceType);
    }

    @Override
    public String[] getSupportedDeviceTypes() {
        return new String[] {
            "CONSUME_TERMINAL", "POS_TERMINAL", "MOBILE_APP",
            "CANTEEN_TERMINAL", "RESTAURANT_TERMINAL"
        };
    }

    @Override
    public boolean supportsOfflineMode() {
        return true; // 固定金额模式支持离线模式
    }

    @Override
    public boolean requiresNetwork() {
        return false; // 基础功能不需要网络，但可选功能可能需要
    }

    @Override
    public int getPriority() {
        return 100; // 最高优先级，这是最基础的消费模式
    }

    @Override
    public void initialize(Map<String, Object> config) {
        log.info("初始化标准固定金额模式: config={}", config);
        // 初始化餐别价格表等
    }

    @Override
    public void destroy() {
        log.info("销毁标准固定金额模式");
        // 清理资源
    }

    /**
     * 获取标准餐别价格
     */
    private BigDecimal getStandardMealPrice(String mealType) {
        return STANDARD_MEAL_PRICES.getOrDefault(mealType, STANDARD_MEAL_PRICES.get("STANDARD"));
    }

    /**
     * 根据餐别类型调整价格
     */
    private BigDecimal calculateMealTypeAdjustment(String mealType, BigDecimal baseAmount) {
        switch (mealType) {
            case "BREAKFAST":
                return baseAmount.multiply(new BigDecimal("0.8")).setScale(2, RoundingMode.HALF_UP); // 早餐8折
            case "LUNCH":
                return baseAmount; // 午餐原价
            case "DINNER":
                return baseAmount.multiply(new BigDecimal("1.2")).setScale(2, RoundingMode.HALF_UP); // 晚餐1.2倍
            case "PREMIUM":
                return baseAmount.multiply(new BigDecimal("1.5")).setScale(2, RoundingMode.HALF_UP); // 高级餐1.5倍
            case "VIP":
                return baseAmount.multiply(new BigDecimal("2.0")).setScale(2, RoundingMode.HALF_UP); // VIP餐2倍
            default:
                return baseAmount;
        }
    }

    /**
     * 计算时段优惠
     */
    private BigDecimal calculateTimeDiscount(BigDecimal amount, Map<String, Object> config) {
        LocalTime now = LocalTime.now();
        LocalTime offPeakStart = LocalTime.of(14, 0);
        LocalTime offPeakEnd = LocalTime.of(17, 0);

        // 下午时段优惠
        if (now.isAfter(offPeakStart) && now.isBefore(offPeakEnd)) {
            return amount.multiply(new BigDecimal("0.1")).setScale(2, RoundingMode.HALF_UP);
        }

        return BigDecimal.ZERO;
    }

    /**
     * 计算折扣
     */
    private BigDecimal calculateDiscount(BigDecimal amount, Map<String, Object> config) {
        BigDecimal discountRate = getBigDecimalFromMap(config, "discountRate", BigDecimal.ZERO);
        if (discountRate.compareTo(BigDecimal.ZERO) > 0) {
            return amount.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    /**
     * 计算会员优惠
     */
    private BigDecimal calculateMemberDiscount(BigDecimal amount, Map<String, Object> config) {
        BigDecimal memberDiscountRate = getBigDecimalFromMap(config, "memberDiscountRate", new BigDecimal("0.05"));
        return amount.multiply(memberDiscountRate).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算优惠券优惠
     */
    private BigDecimal calculateCouponDiscount(BigDecimal amount, Map<String, Object> config) {
        BigDecimal couponAmount = getBigDecimalFromMap(config, "couponAmount", BigDecimal.ZERO);
        BigDecimal maxCouponAmount = getBigDecimalFromMap(config, "maxCouponAmount", amount);

        BigDecimal actualCouponAmount = couponAmount.compareTo(maxCouponAmount) <= 0 ? couponAmount : maxCouponAmount;
        return actualCouponAmount.compareTo(amount) <= 0 ? actualCouponAmount : amount;
    }

    /**
     * 计算服务费
     */
    private BigDecimal calculateServiceFee(BigDecimal amount, Map<String, Object> config) {
        String feeType = getStringFromMap(config, "feeType", "PERCENTAGE");
        BigDecimal feeRate = getBigDecimalFromMap(config, "feeRate", new BigDecimal("0.01"));
        BigDecimal minFee = getBigDecimalFromMap(config, "minFee", new BigDecimal("0.50"));

        BigDecimal fee = BigDecimal.ZERO;

        switch (feeType) {
            case "PERCENTAGE":
                fee = amount.multiply(feeRate);
                break;
            case "FIXED_AMOUNT":
                fee = getBigDecimalFromMap(config, "feeAmount", BigDecimal.ZERO);
                break;
        }

        // 确保服务费不低于最低费用
        return fee.compareTo(minFee) >= 0 ? fee : minFee;
    }

    /**
     * 计算获得积分
     */
    private Integer calculatePointsEarned(BigDecimal amount, Map<String, Object> config) {
        BigDecimal pointsRate = getBigDecimalFromMap(config, "pointsRate", new BigDecimal("10"));
        Boolean isMember = getBooleanFromMap(config, "isMember", false);

        // 会员双倍积分
        BigDecimal rate = isMember ? pointsRate.multiply(new BigDecimal("2")) : pointsRate;

        return amount.multiply(rate).intValue();
    }

    /**
     * 验证餐别类型是否有效
     */
    private boolean isValidMealType(String mealType) {
        return mealType != null && STANDARD_MEAL_PRICES.containsKey(mealType);
    }

    /**
     * 从Map中获取字符串值
     */
    private String getStringFromMap(Map<String, Object> map, String key, String defaultValue) {
        if (map == null || !map.containsKey(key)) {
            return defaultValue;
        }
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    /**
     * 从Map中获取Boolean值
     */
    private Boolean getBooleanFromMap(Map<String, Object> map, String key, Boolean defaultValue) {
        if (map == null || !map.containsKey(key)) {
            return defaultValue;
        }
        Object value = map.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }

    /**
     * 从Map中获取BigDecimal值
     */
    private BigDecimal getBigDecimalFromMap(Map<String, Object> map, String key, BigDecimal defaultValue) {
        if (map == null || !map.containsKey(key)) {
            return defaultValue;
        }
        Object value = map.get(key);
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        } else if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        } else if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
}