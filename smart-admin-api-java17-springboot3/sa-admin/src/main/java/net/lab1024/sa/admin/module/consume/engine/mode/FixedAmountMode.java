package net.lab1024.sa.admin.module.consume.engine.mode;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.engine.ConsumeRequest;
import net.lab1024.sa.admin.module.consume.engine.ConsumeResult;

/**
 * 固定金额消费模式
 * 适用于餐厅、食堂等固定价格消费场景
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
public class FixedAmountMode implements ConsumptionMode {

    private static final String MODE_CODE = "FIXED_AMOUNT";
    private static final String MODE_NAME = "固定金额模式";

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
        return "适用于餐厅、食堂等固定价格消费场景，支持餐别时段控制和价格配置";
    }

    @Override
    public ConsumeResult process(ConsumeRequest request) {
        try {
            BigDecimal amount = request.getAmount();
            if (amount == null) {
                return ConsumeResult.failure("INVALID_AMOUNT", "消费金额不能为空");
            }

            // 获取餐别类型配置
            Map<String, Object> modeConfig = request.getModeConfig();
            String mealType = getStringFromMap(modeConfig, "mealType", "STANDARD");
            BigDecimal actualAmount = calculateActualAmount(mealType, amount);

            // 如果有优惠折扣
            BigDecimal discountAmount = BigDecimal.ZERO;
            Boolean hasDiscount = getBooleanFromMap(modeConfig, "hasDiscount", false);
            if (hasDiscount) {
                BigDecimal discountRate = getBigDecimalFromMap(modeConfig, "discountRate", BigDecimal.ZERO);
                if (discountRate.compareTo(BigDecimal.ZERO) > 0) {
                    discountAmount = actualAmount.multiply(discountRate);
                    actualAmount = actualAmount.subtract(discountAmount);
                }
            }

            // 添加手续费（如果有）
            BigDecimal feeAmount = BigDecimal.ZERO;
            Boolean hasFee = getBooleanFromMap(modeConfig, "hasFee", false);
            if (hasFee) {
                BigDecimal feeRate = getBigDecimalFromMap(modeConfig, "feeRate", BigDecimal.ZERO);
                if (feeRate.compareTo(BigDecimal.ZERO) > 0) {
                    feeAmount = actualAmount.multiply(feeRate);
                }
            }

            BigDecimal finalAmount = actualAmount.add(feeAmount);

            // 构建结果
            return ConsumeResult.builder()
                    .success(true)
                    .resultCode("SUCCESS")
                    .message("固定金额消费处理成功")
                    .actualAmount(finalAmount)
                    .discountAmount(discountAmount)
                    .feeAmount(feeAmount)
                    .finalAmount(finalAmount)
                    .consumptionMode(getModeCode())
                    .build();

        } catch (Exception e) {
            log.error("固定金额模式处理异常", e);
            return ConsumeResult.failure("PROCESS_ERROR", "固定金额模式处理异常: " + e.getMessage());
        }
    }

    @Override
    public ConsumptionMode.ValidationResult validate(ConsumeRequest request) {
        try {
            // 基础参数验证
            if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return ConsumptionMode.ValidationResult.failure("INVALID_AMOUNT", "消费金额必须大于0");
            }

            // 验证餐别类型
            Map<String, Object> modeConfig = request.getModeConfig();
            String mealType = getStringFromMap(modeConfig, "mealType", null);
            if (mealType != null && !isValidMealType(mealType)) {
                return ConsumptionMode.ValidationResult.failure("INVALID_MEAL_TYPE", "无效的餐别类型: " + mealType);
            }

            // 验证折扣配置
            Boolean hasDiscount = getBooleanFromMap(modeConfig, "hasDiscount", false);
            if (hasDiscount) {
                BigDecimal discountRate = getBigDecimalFromMap(modeConfig, "discountRate", null);
                if (discountRate == null || discountRate.compareTo(BigDecimal.ZERO) < 0
                        || discountRate.compareTo(BigDecimal.ONE) > 0) {
                    return ConsumptionMode.ValidationResult.failure("INVALID_DISCOUNT_RATE", "折扣率必须在0-1之间");
                }
            }

            // 验证手续费配置
            Boolean hasFee = getBooleanFromMap(modeConfig, "hasFee", false);
            if (hasFee) {
                BigDecimal feeRate = getBigDecimalFromMap(modeConfig, "feeRate", null);
                if (feeRate == null || feeRate.compareTo(BigDecimal.ZERO) < 0
                        || feeRate.compareTo(BigDecimal.ONE) > 0) {
                    return ConsumptionMode.ValidationResult.failure("INVALID_FEE_RATE", "手续费率必须在0-1之间");
                }
            }

            return ConsumptionMode.ValidationResult.success();

        } catch (Exception e) {
            log.error("固定金额模式验证异常", e);
            return ConsumptionMode.ValidationResult.failure("VALIDATION_ERROR", "验证异常: " + e.getMessage());
        }
    }

    @Override
    public BigDecimal calculateAmount(ConsumeRequest request) {
        if (request.getAmount() == null) {
            return BigDecimal.ZERO;
        }
        Map<String, Object> modeConfig = request.getModeConfig();
        String mealType = getStringFromMap(modeConfig, "mealType", "STANDARD");
        return calculateActualAmount(mealType, request.getAmount());
    }

    @Override
    public Map<String, Object> getConfigTemplate() {
        Map<String, Object> template = new HashMap<>();
        template.put("mealType", "STANDARD");
        Map<String, BigDecimal> priceLevels = new HashMap<>();
        priceLevels.put("STANDARD", new BigDecimal("15.00"));
        priceLevels.put("PREMIUM", new BigDecimal("25.00"));
        priceLevels.put("VIP", new BigDecimal("35.00"));
        template.put("priceLevels", priceLevels);
        return template;
    }

    @Override
    public boolean validateConfig(Map<String, Object> config) {
        if (config == null) {
            return false;
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
        return true;
    }

    @Override
    public String[] getSupportedDeviceTypes() {
        return new String[] { "CONSUME_TERMINAL", "POS_TERMINAL", "MOBILE_APP" };
    }

    @Override
    public boolean supportsOfflineMode() {
        return true;
    }

    @Override
    public boolean requiresNetwork() {
        return false;
    }

    @Override
    public void initialize(Map<String, Object> config) {
        // 初始化模式配置
        log.info("初始化固定金额模式: config={}", config);
    }

    @Override
    public void destroy() {
        // 清理资源
        log.info("销毁固定金额模式");
    }

    @Override
    public int getPriority() {
        return 100; // 高优先级
    }

    /**
     * 计算实际消费金额
     *
     * @param mealType   餐别类型
     * @param baseAmount 基础金额
     * @return 实际金额
     */
    private BigDecimal calculateActualAmount(String mealType, BigDecimal baseAmount) {
        // 根据餐别类型调整价格
        switch (mealType) {
            case "BREAKFAST":
                return baseAmount.multiply(new BigDecimal("0.8")); // 早餐8折
            case "LUNCH":
                return baseAmount; // 午餐原价
            case "DINNER":
                return baseAmount.multiply(new BigDecimal("1.2")); // 晚餐1.2倍
            case "PREMIUM":
                return baseAmount.multiply(new BigDecimal("1.5")); // 高级餐1.5倍
            default:
                return baseAmount;
        }
    }

    /**
     * 验证餐别类型是否有效
     *
     * @param mealType 餐别类型
     * @return 是否有效
     */
    private boolean isValidMealType(String mealType) {
        if (mealType == null) {
            return true; // 默认餐别
        }

        return "STANDARD".equals(mealType) ||
                "BREAKFAST".equals(mealType) ||
                "LUNCH".equals(mealType) ||
                "DINNER".equals(mealType) ||
                "PREMIUM".equals(mealType) ||
                "VIP".equals(mealType);
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
        }
        return defaultValue;
    }
}