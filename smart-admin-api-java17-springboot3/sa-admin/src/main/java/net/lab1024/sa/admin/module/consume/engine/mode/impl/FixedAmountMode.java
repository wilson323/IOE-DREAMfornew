package net.lab1024.sa.admin.module.consume.engine.mode.impl;

import net.lab1024.sa.admin.module.consume.engine.mode.abstracts.AbstractConsumptionMode;
import net.lab1024.sa.admin.module.consume.domain.enums.MealType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.Map;
import java.util.Arrays;
import java.util.List;

/**
 * 固定金额消费模式
 * 严格遵循repowiki规范：实现标准档位的固定金额消费模式
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Component("fixedAmountMode")
public class FixedAmountMode extends AbstractConsumptionMode {

    // 标准金额档位
    private static final BigDecimal[] STANDARD_AMOUNTS = {
        new BigDecimal("5"),
        new BigDecimal("8"),
        new BigDecimal("10"),
        new BigDecimal("12"),
        new BigDecimal("15"),
        new BigDecimal("18"),
        new BigDecimal("20"),
        new BigDecimal("25")
    };

    // 最大金额限制
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("100");

    public FixedAmountMode() {
        super("FIXED_AMOUNT", "固定金额模式", "使用预定义金额档位进行消费");
    }

    @Override
    public BigDecimal calculateAmount(Map<String, Object> params) {
        try {
            BigDecimal amount = getAmountFromParams(params);
            if (amount == null) {
                throw new IllegalArgumentException("无效的金额参数");
            }

            // 验证是否为标准档位金额
            if (!isStandardAmount(amount)) {
                throw new IllegalArgumentException("金额不在标准档位范围内: " + amount);
            }

            // 应用折扣和补贴
            amount = applyDiscountsAndSubsidies(amount, params);

            // 确保金额精度为2位小数
            return amount.setScale(2, RoundingMode.HALF_UP);

        } catch (Exception e) {
            throw new IllegalArgumentException("金额计算失败: " + e.getMessage());
        }
    }

    @Override
    protected boolean doValidateParameters(Map<String, Object> params) {
        // 验证必填参数
        if (!hasRequiredParams(params, "amount", "accountId")) {
            return false;
        }

        BigDecimal amount = getAmountFromParams(params);
        if (amount == null) {
            return false;
        }

        // 验证金额范围
        if (!isAmountInRange(amount)) {
            return false;
        }

        // 验证是否为标准档位
        if (!isStandardAmount(amount)) {
            return false;
        }

        // 验证餐别时间控制
        String mealTypeCode = getStringFromParams(params, "mealType");
        if (mealTypeCode != null) {
            MealType mealType = MealType.fromCode(mealTypeCode);
            if (mealType != null && !mealType.isCurrentTime()) {
                // 如果不是当前餐别时间，检查是否允许跨餐别消费
                Boolean allowCrossMeal = (Boolean) params.get("allowCrossMeal");
                if (!Boolean.TRUE.equals(allowCrossMeal)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    protected boolean doIsAllowed(Map<String, Object> params) {
        // 检查餐别时间限制
        String mealTypeCode = getStringFromParams(params, "mealType");
        if (mealTypeCode != null) {
            MealType mealType = MealType.fromCode(mealTypeCode);
            if (mealType != null && !mealType.isCurrentTime()) {
                Boolean allowCrossMeal = (Boolean) params.get("allowCrossMeal");
                if (!Boolean.TRUE.equals(allowCrossMeal)) {
                    return false;
                }
            }
        }

        // 检查自定义金额权限
        Boolean allowCustomAmount = (Boolean) params.get("allowCustomAmount");
        BigDecimal amount = getAmountFromParams(params);
        if (Boolean.TRUE.equals(allowCustomAmount) && amount != null && !isStandardAmount(amount)) {
            // 如果允许自定义金额，检查是否在合理范围内
            return amount.compareTo(getMinAmount()) >= 0 && amount.compareTo(getMaxAmount()) <= 0;
        }

        return true;
    }

    @Override
    protected Map<String, Object> doPreProcess(Map<String, Object> params) {
        Map<String, Object> result = new java.util.HashMap<>();

        // 确定当前餐别
        MealType currentMealType = MealType.getCurrentMealType();
        String mealTypeCode = getStringFromParams(params, "mealType");

        if (mealTypeCode == null) {
            // 如果没有指定餐别，使用当前餐别
            params.put("mealType", currentMealType.getCode());
            result.put("autoMealType", currentMealType.getCode());
        }

        // 获取推荐金额档位
        String selectedMealType = getStringFromParams(params, "mealType");
        MealType mealType = MealType.fromCode(selectedMealType != null ? selectedMealType : currentMealType.getCode());

        if (mealType != null) {
            BigDecimal[] recommendedAmounts = mealType.getRecommendedAmounts();
            result.put("recommendedAmounts", recommendedAmounts);
            result.put("mealTypeDesc", mealType.getDescription());
        }

        return result;
    }

    @Override
    protected Map<String, Object> doPostProcess(Map<String, Object> params, Map<String, Object> result) {
        Map<String, Object> postResult = new java.util.HashMap<>();

        BigDecimal finalAmount = (BigDecimal) result.get("amount");
        if (finalAmount != null) {
            postResult.put("formattedAmount", "¥" + finalAmount.setScale(2));
        }

        // 记录餐别信息
        String mealTypeCode = getStringFromParams(params, "mealType");
        if (mealTypeCode != null) {
            MealType mealType = MealType.fromCode(mealTypeCode);
            if (mealType != null) {
                postResult.put("mealTypeName", mealType.getName());
                postResult.put("mealTimeRange", mealType.getTimeRangeDesc());
            }
        }

        return postResult;
    }

    @Override
    protected BigDecimal getMinAmount() {
        return STANDARD_AMOUNTS[0];
    }

    @Override
    protected BigDecimal getMaxAmount() {
        return MAX_AMOUNT;
    }

    @Override
    protected String[] getSupportedFields() {
        return new String[]{"amount", "accountId", "deviceId", "mealType", "allowCrossMeal", "allowCustomAmount", "discountRate", "subsidyAmount"};
    }

    /**
     * 检查是否为标准档位金额
     */
    private boolean isStandardAmount(BigDecimal amount) {
        if (amount == null) {
            return false;
        }

        // 检查是否在标准档位中
        for (BigDecimal standardAmount : STANDARD_AMOUNTS) {
            if (amount.compareTo(standardAmount) == 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * 应用折扣和补贴
     */
    private BigDecimal applyDiscountsAndSubsidies(BigDecimal originalAmount, Map<String, Object> params) {
        BigDecimal amount = originalAmount;

        // 应用折扣率
        Object discountRateObj = params.get("discountRate");
        if (discountRateObj instanceof Number) {
            double discountRate = ((Number) discountRateObj).doubleValue();
            if (discountRate > 0 && discountRate < 1) {
                amount = amount.multiply(BigDecimal.valueOf(discountRate));
            }
        }

        // 应用补贴金额
        Object subsidyAmountObj = params.get("subsidyAmount");
        if (subsidyAmountObj instanceof Number) {
            BigDecimal subsidyAmount = BigDecimal.valueOf(((Number) subsidyAmountObj).doubleValue());
            amount = amount.add(subsidyAmount);
        }

        // 确保金额不为负数
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            amount = BigDecimal.ZERO;
        }

        return amount;
    }

    /**
     * 获取所有标准档位金额
     */
    public static List<BigDecimal> getStandardAmounts() {
        return Arrays.asList(STANDARD_AMOUNTS);
    }

    /**
     * 获取指定餐别的推荐金额
     */
    public static BigDecimal[] getRecommendedAmountsForMeal(String mealTypeCode) {
        MealType mealType = MealType.fromCode(mealTypeCode);
        return mealType != null ? mealType.getRecommendedAmounts() : STANDARD_AMOUNTS;
    }

    /**
     * 检查餐别是否允许消费
     */
    public static boolean isMealTimeAllowed(String mealTypeCode) {
        MealType mealType = MealType.fromCode(mealTypeCode);
        return mealType == null || mealType.isCurrentTime();
    }
}