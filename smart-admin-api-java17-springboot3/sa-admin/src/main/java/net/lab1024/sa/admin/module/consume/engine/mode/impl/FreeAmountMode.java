package net.lab1024.sa.admin.module.consume.engine.mode.impl;

import net.lab1024.sa.admin.module.consume.engine.mode.abstracts.AbstractConsumptionMode;
import net.lab1024.sa.admin.module.consume.domain.enums.CategoryDiscountEnum;
import net.lab1024.sa.admin.module.consume.domain.enums.MemberLevelEnum;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;

/**
 * 自由金额消费模式
 * 严格遵循repowiki规范：实现超市等自由购物场景的消费模式
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Component("freeAmountMode")
public class FreeAmountMode extends AbstractConsumptionMode {

    // 金额范围配置
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("0.01");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("9999.99");

    // 小数精度限制
    private static final int DECIMAL_SCALE = 2;

    public FreeAmountMode() {
        super("FREE_AMOUNT", "自由金额模式", "超市等自由购物场景，支持任意金额消费");
    }

    @Override
    public BigDecimal calculateAmount(Map<String, Object> params) {
        try {
            BigDecimal amount = getAmountFromParams(params);
            if (amount == null) {
                throw new IllegalArgumentException("无效的金额参数");
            }

            // 验证金额范围和精度
            validateAmountRange(amount);
            validateAmountPrecision(amount);

            // 应用折扣逻辑
            amount = applyDiscountsAndSubsidies(amount, params);

            // 确保金额精度为2位小数
            return amount.setScale(DECIMAL_SCALE, RoundingMode.HALF_UP);

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
        if (!validateAmountRange(amount)) {
            return false;
        }

        // 验证金额精度
        if (!validateAmountPrecision(amount)) {
            return false;
        }

        // 验证商品分类（如果有）
        String categoryCode = getStringFromParams(params, "category");
        if (categoryCode != null && !CategoryDiscountEnum.isValidCode(categoryCode)) {
            return false;
        }

        // 验证会员等级（如果有）
        String memberLevelCode = getStringFromParams(params, "memberLevel");
        if (memberLevelCode != null && !MemberLevelEnum.isValidCode(memberLevelCode)) {
            return false;
        }

        return true;
    }

    @Override
    protected boolean doIsAllowed(Map<String, Object> params) {
        BigDecimal amount = getAmountFromParams(params);
        if (amount == null) {
            return false;
        }

        // 检查特殊金额限制
        String specialLimitType = getStringFromParams(params, "specialLimitType");
        if (specialLimitType != null) {
            return checkSpecialLimit(amount, specialLimitType);
        }

        // 检查会员等级限制
        String memberLevelCode = getStringFromParams(params, "memberLevel");
        if (memberLevelCode != null) {
            MemberLevelEnum memberLevel = MemberLevelEnum.fromCode(memberLevelCode);
            if (memberLevel != null && memberLevel.getLevel() > getRequiredMemberLevel(params)) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected Map<String, Object> doPreProcess(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        // 预处理商品分类信息
        String categoryCode = getStringFromParams(params, "category");
        if (categoryCode != null) {
            CategoryDiscountEnum category = CategoryDiscountEnum.fromCode(categoryCode);
            if (category != null) {
                result.put("categoryInfo", category.getCategoryInfo());
                result.put("categoryDiscount", category.getDiscountPercentage());
            }
        }

        // 预处理会员等级信息
        String memberLevelCode = getStringFromParams(params, "memberLevel");
        if (memberLevelCode != null) {
            MemberLevelEnum memberLevel = MemberLevelEnum.fromCode(memberLevelCode);
            if (memberLevel != null) {
                result.put("memberLevelInfo", memberLevel.getMemberBenefits());
                result.put("memberDiscount", memberLevel.getDiscountPercentage());
            }
        }

        // 预处理金额信息
        BigDecimal amount = getAmountFromParams(params);
        if (amount != null) {
            result.put("formattedAmount", "¥" + amount.setScale(2));
            result.put("amountWords", convertAmountToWords(amount));
        }

        return result;
    }

    @Override
    protected Map<String, Object> doPostProcess(Map<String, Object> params, Map<String, Object> result) {
        Map<String, Object> postResult = new HashMap<>();

        BigDecimal originalAmount = (BigDecimal) params.get("amount");
        BigDecimal finalAmount = (BigDecimal) result.get("amount");

        if (originalAmount != null && finalAmount != null) {
            BigDecimal totalDiscount = originalAmount.subtract(finalAmount);
            postResult.put("originalAmount", "¥" + originalAmount.setScale(2));
            postResult.put("finalAmount", "¥" + finalAmount.setScale(2));
            postResult.put("totalDiscount", "¥" + totalDiscount.setScale(2));
            postResult.put("totalDiscountPercentage",
                originalAmount.compareTo(BigDecimal.ZERO) > 0
                    ? totalDiscount.divide(originalAmount, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")) + "%"
                    : "0%");
        }

        // 记录折扣详情
        String categoryCode = getStringFromParams(params, "category");
        String memberLevelCode = getStringFromParams(params, "memberLevel");

        if (categoryCode != null) {
            postResult.put("appliedCategoryDiscount", CategoryDiscountEnum.getDiscountPercentage(categoryCode));
        }

        if (memberLevelCode != null) {
            postResult.put("appliedMemberDiscount", MemberLevelEnum.fromCode(memberLevelCode).getDiscountPercentage());
        }

        return postResult;
    }

    @Override
    protected BigDecimal getMinAmount() {
        return MIN_AMOUNT;
    }

    @Override
    protected BigDecimal getMaxAmount() {
        return MAX_AMOUNT;
    }

    @Override
    protected String[] getSupportedFields() {
        return new String[]{
            "amount", "accountId", "deviceId", "category", "memberLevel",
            "allowCustomDiscount", "specialLimitType", "notes"
        };
    }

    /**
     * 验证金额范围
     */
    private boolean validateAmountRange(BigDecimal amount) {
        return amount.compareTo(MIN_AMOUNT) >= 0 && amount.compareTo(MAX_AMOUNT) <= 0;
    }

    /**
     * 验证金额精度
     */
    private boolean validateAmountPrecision(BigDecimal amount) {
        try {
            // 检查小数位数是否超过2位
            String amountStr = amount.toPlainString();
            int decimalIndex = amountStr.indexOf('.');
            if (decimalIndex != -1) {
                int decimalPlaces = amountStr.length() - decimalIndex - 1;
                return decimalPlaces <= DECIMAL_SCALE;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 应用折扣和补贴
     */
    private BigDecimal applyDiscountsAndSubsidies(BigDecimal originalAmount, Map<String, Object> params) {
        BigDecimal amount = originalAmount;

        // 获取参数中的折扣信息
        String categoryCode = getStringFromParams(params, "category");
        String memberLevelCode = getStringFromParams(params, "memberLevel");
        Boolean allowCustomDiscount = (Boolean) params.get("allowCustomDiscount");
        Object customDiscountRateObj = params.get("customDiscountRate");
        BigDecimal subsidyAmount = getBigDecimalFromParams(params, "subsidyAmount");

        // 应用折扣
        if (Boolean.TRUE.equals(allowCustomDiscount) && customDiscountRateObj instanceof Number) {
            // 自定义折扣率
            BigDecimal customDiscountRate = BigDecimal.valueOf(((Number) customDiscountRateObj).doubleValue());
            if (customDiscountRate.compareTo(BigDecimal.ZERO) > 0 && customDiscountRate.compareTo(BigDecimal.ONE) <= 1) {
                amount = amount.multiply(customDiscountRate);
            }
        } else {
            // 应用会员折扣和分类折扣
            if (memberLevelCode != null && categoryCode != null) {
                amount = CategoryDiscountEnum.applyMultipleDiscounts(amount, categoryCode,
                    MemberLevelEnum.getDiscountRate(memberLevelCode));
            } else if (memberLevelCode != null) {
                amount = MemberLevelEnum.applyMemberDiscount(amount, memberLevelCode);
            } else if (categoryCode != null) {
                amount = CategoryDiscountEnum.applyCategoryDiscount(amount, categoryCode);
            }
        }

        // 应用补贴
        if (subsidyAmount != null && subsidyAmount.compareTo(BigDecimal.ZERO) > 0) {
            amount = amount.add(subsidyAmount);
        }

        // 确保金额不为负数
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            amount = BigDecimal.ZERO;
        }

        return amount;
    }

    /**
     * 检查特殊金额限制
     */
    private boolean checkSpecialLimit(BigDecimal amount, String limitType) {
        switch (limitType) {
            case "DAILY_LIMIT":
                return amount.compareTo(new BigDecimal("500")) <= 0;
            case "MONTHLY_LIMIT":
                return amount.compareTo(new BigDecimal("2000")) <= 0;
            case "LOW_VALUE":
                return amount.compareTo(new BigDecimal("10")) <= 0;
            case "HIGH_VALUE":
                return amount.compareTo(new BigDecimal("1000")) <= 0;
            default:
                return true;
        }
    }

    /**
     * 获取所需会员等级
     */
    private int getRequiredMemberLevel(Map<String, Object> params) {
        String specialLimitType = getStringFromParams(params, "specialLimitType");
        if ("HIGH_VALUE".equals(specialLimitType)) {
            return 2; // 金卡会员以上
        }
        return 0; // 无限制
    }

    /**
     * 从参数中获取BigDecimal
     */
    private BigDecimal getBigDecimalFromParams(Map<String, Object> params, String key) {
        Object value = params.get(key);
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        } else if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        } else if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 将金额转换为中文大写
     */
    private String convertAmountToWords(BigDecimal amount) {
        if (amount == null) {
            return "零元整";
        }

        // 简化实现，实际应该使用完整的数字转中文算法
        return amount.setScale(2).toString() + "元";
    }

    /**
     * 获取所有支持的商品分类
     */
    public static List<String> getSupportedCategories() {
        return CategoryDiscountEnum.getAllCategoryCodes();
    }

    /**
     * 获取所有支持的会员等级
     */
    public static List<String> getSupportedMemberLevels() {
        return Arrays.stream(MemberLevelEnum.values())
                .map(MemberLevelEnum::getCode)
                .collect(java.util.stream.Collectors.toList());
    }
}