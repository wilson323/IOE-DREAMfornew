package net.lab1024.sa.admin.module.consume.engine.mode;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.engine.ConsumeMode;
import net.lab1024.sa.admin.module.consume.engine.ConsumeModeRequest;
import net.lab1024.sa.admin.module.consume.engine.ConsumeModeResult;
import net.lab1024.sa.admin.module.consume.engine.ConsumeModeValidationResult;

import java.math.BigDecimal;

/**
 * 自由金额消费模式
 * 适用于超市、商店等自由购物消费场景
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
public class FreeAmountMode implements ConsumptionMode {

    private static final String MODE_CODE = "FREE_AMOUNT";
    private static final String MODE_NAME = "自由金额模式";

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
        return "适用于超市、商店等自由购物消费场景，支持金额范围检查和动态定价";
    }

    @Override
    public ConsumeModeResult process(ConsumeModeRequest request) {
        try {
            BigDecimal amount = request.getAmount();

            // 应用商品折扣（如果有）
            BigDecimal discountAmount = BigDecimal.ZERO;
            Boolean hasDiscount = request.getModeParam("hasDiscount", Boolean.class, false);
            if (hasDiscount) {
                discountAmount = calculateDiscount(request, amount);
            }

            // 应用会员折扣（如果有）
            BigDecimal memberDiscountAmount = BigDecimal.ZERO;
            String memberLevel = request.getModeParam("memberLevel", String.class);
            if (memberLevel != null) {
                memberDiscountAmount = calculateMemberDiscount(memberLevel, amount);
            }

            // 计算最终金额
            BigDecimal actualAmount = amount.subtract(discountAmount).subtract(memberDiscountAmount);

            // 添加手续费（如果有）
            BigDecimal feeAmount = BigDecimal.ZERO;
            Boolean hasFee = request.getModeParam("hasFee", Boolean.class, false);
            if (hasFee) {
                feeAmount = calculateFee(actualAmount, request);
            }

            BigDecimal finalAmount = actualAmount.add(feeAmount);

            // 检查金额范围限制
            if (!isAmountInRange(finalAmount, request)) {
                return ConsumeModeResult.failure("AMOUNT_OUT_OF_RANGE", "消费金额超出允许范围");
            }

            // 构建结果
            ConsumeModeResult.ConsumeModeResultBuilder builder = ConsumeModeResult.builder()
                    .success(true)
                    .code("SUCCESS")
                    .message("自由金额消费处理成功")
                    .modeCode(getModeCode())
                    .originalAmount(amount)
                    .finalAmount(finalAmount)
                    .discountAmount(discountAmount.add(memberDiscountAmount))
                    .feeAmount(feeAmount);

            // 添加模式特定数据
            builder.setModeData("hasDiscount", hasDiscount);
            builder.setModeData("hasMemberDiscount", memberLevel != null);
            builder.setModeData("totalDiscountAmount", discountAmount.add(memberDiscountAmount));
            if (memberLevel != null) {
                builder.setModeData("memberLevel", memberLevel);
            }

            return builder.build();

        } catch (Exception e) {
            log.error("自由金额模式处理异常", e);
            return ConsumeModeResult.failure("PROCESS_ERROR", "自由金额模式处理异常: " + e.getMessage());
        }
    }

    @Override
    public ConsumeModeValidationResult validate(ConsumeModeRequest request) {
        try {
            // 基础参数验证
            if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return ConsumeModeValidationResult.failure("INVALID_AMOUNT", "消费金额必须大于0");
            }

            // 验证商品分类（如果有）
            String productCategory = request.getModeParam("productCategory", String.class);
            if (productCategory != null && !isValidProductCategory(productCategory)) {
                return ConsumeModeValidationResult.failure("INVALID_CATEGORY", "无效的商品分类: " + productCategory);
            }

            // 验证折扣配置
            Boolean hasDiscount = request.getModeParam("hasDiscount", Boolean.class, false);
            if (hasDiscount) {
                String discountType = request.getModeParam("discountType", String.class);
                if (discountType != null && !isValidDiscountType(discountType)) {
                    return ConsumeModeValidationResult.failure("INVALID_DISCOUNT_TYPE", "无效的折扣类型: " + discountType);
                }
            }

            // 验证会员级别
            String memberLevel = request.getModeParam("memberLevel", String.class);
            if (memberLevel != null && !isValidMemberLevel(memberLevel)) {
                return ConsumeModeValidationResult.failure("INVALID_MEMBER_LEVEL", "无效的会员级别: " + memberLevel);
            }

            // 验证金额范围
            BigDecimal minAmount = request.getModeParam("minAmount", BigDecimal.class, BigDecimal.ONE);
            BigDecimal maxAmount = request.getModeParam("maxAmount", BigDecimal.class, new BigDecimal("99999.99"));

            if (request.getAmount().compareTo(minAmount) < 0) {
                return ConsumeModeValidationResult.failure("AMOUNT_BELOW_MIN",
                    "消费金额不能低于最小限制: " + minAmount);
            }

            if (request.getAmount().compareTo(maxAmount) > 0) {
                return ConsumeModeValidationResult.failure("AMOUNT_EXCEEDS_MAX",
                    "消费金额超过最大限制: " + maxAmount);
            }

            return ConsumeModeValidationResult.success();

        } catch (Exception e) {
            log.error("自由金额模式验证异常", e);
            return ConsumeModeValidationResult.failure("VALIDATION_ERROR", "验证异常: " + e.getMessage());
        }
    }

    @Override
    public String getConfigTemplate() {
        return """
        {
          "productCategories": [
            "FOOD",
            "BEVERAGE",
            "DAILY_GOODS",
            "CLOTHING",
            "ELECTRONICS",
            "BOOKS",
            "OTHER"
          ],
          "amountRange": {
            "minAmount": 0.01,
            "maxAmount": 99999.99,
            "defaultMin": 1.00,
            "defaultMax": 1000.00
          },
          "discountConfig": {
            "enabled": false,
            "discountTypes": ["PERCENTAGE", "FIXED_AMOUNT", "QUANTITY_BASED"],
            "categoryDiscounts": {
              "FOOD": 0.05,
              "BEVERAGE": 0.10,
              "DAILY_GOODS": 0.02
            }
          },
          "memberConfig": {
            "enabled": true,
            "memberLevels": {
              "BRONZE": {
                "discountRate": 0.05,
                "minAmount": 50.00
              },
              "SILVER": {
                "discountRate": 0.10,
                "minAmount": 30.00
              },
              "GOLD": {
                "discountRate": 0.15,
                "minAmount": 20.00
              },
              "PLATINUM": {
                "discountRate": 0.20,
                "minAmount": 10.00
              }
            }
          },
          "feeConfig": {
            "enabled": false,
            "feeType": "PERCENTAGE",
            "feeRate": 0.01,
            "minFee": 0.00
          },
          "inventoryCheck": {
            "enabled": false,
            "checkOutOfStock": true,
            "reserveItems": false
          }
        }
        """;
    }

    @Override
    public boolean isApplicableToDevice(Long deviceId) {
        // 自由金额模式适用于支持商品选择的设备
        return true; // 实际实现中可以根据设备类型判断
    }

    @Override
    public int getPriority() {
        return 80; // 中等优先级
    }

    /**
     * 计算商品折扣
     */
    private BigDecimal calculateDiscount(ConsumeModeRequest request, BigDecimal amount) {
        String discountType = request.getModeParam("discountType", String.class);
        String productCategory = request.getModeParam("productCategory", String.class);

        BigDecimal discountRate = BigDecimal.ZERO;

        // 按折扣类型计算
        switch (discountType) {
            case "PERCENTAGE":
                // 按百分比折扣
                discountRate = request.getModeParam("discountRate", BigDecimal.class);
                break;
            case "FIXED_AMOUNT":
                // 固定金额折扣
                return request.getModeParam("discountAmount", BigDecimal.class);
            case "QUANTITY_BASED":
                // 基于数量的折扣
                Integer quantity = request.getModeParam("quantity", Integer.class, 1);
                discountRate = calculateQuantityDiscount(quantity);
                break;
            default:
                log.debug("未知的折扣类型: {}", discountType);
                return BigDecimal.ZERO;
        }

        // 按商品分类应用折扣
        if (productCategory != null) {
            BigDecimal categoryDiscountRate = request.getModeParam("categoryDiscountRate", BigDecimal.class);
            if (categoryDiscountRate != null) {
                discountRate = discountDiscountRate;
            }
        }

        return amount.multiply(discountRate);
    }

    /**
     * 计算会员折扣
     */
    private BigDecimal calculateMemberDiscount(String memberLevel, BigDecimal amount) {
        BigDecimal discountRate = BigDecimal.ZERO;
        BigDecimal minAmount = BigDecimal.ZERO;

        switch (memberLevel) {
            case "BRONZE":
                discountRate = new BigDecimal("0.05");
                minAmount = new BigDecimal("50.00");
                break;
            case "SILVER":
                discountRate = new BigDecimal("0.10");
                minAmount = new BigDecimal("30.00");
                break;
            case "GOLD":
                discountRate = new BigDecimal("0.15");
                minAmount = new BigDecimal("20.00");
                break;
            case "PLATINUM":
                discountRate = new BigDecimal("20");
                minAmount = new BigDecimal("10.00");
                break;
            default:
                log.debug("未知的会员级别: {}", memberLevel);
                return BigDecimal.ZERO;
        }

        // 只有当消费金额达到最低要求时才应用会员折扣
        if (amount.compareTo(minAmount) >= 0) {
            return amount.multiply(discountRate);
        }

        return BigDecimal.ZERO;
    }

    /**
     * 计算基于数量的折扣
     */
    private BigDecimal calculateQuantityDiscount(Integer quantity) {
        // 基于数量的折扣逻辑
        if (quantity >= 10) {
            return new BigDecimal("0.10"); // 10件以上9折
        } else if (quantity >= 5) {
            return new BigDecimal("0.05"); // 5-9件95折
        } else if (quantity >= 3) {
            return new BigDecimal("0.02"); // 3-4件98折
        }
        return BigDecimal.ZERO;
    }

    /**
     * 计算手续费
     */
    private BigDecimal calculateFee(BigDecimal amount, ConsumeModeRequest request) {
        String feeType = request.getModeParam("feeType", String.class, "PERCENTAGE");
        BigDecimal feeRate = request.getModeParam("feeRate", BigDecimal.class, new BigDecimal("0.01"));
        BigDecimal minFee = request.getModeParam("minFee", BigDecimal.class, BigDecimal.ZERO);

        BigDecimal fee = BigDecimal.ZERO;

        switch (feeType) {
            case "PERCENTAGE":
                fee = amount.multiply(feeRate);
                break;
            case "FIXED_AMOUNT":
                fee = request.getModeParam("feeAmount", BigDecimal.class, BigDecimal.ZERO);
                break;
            case "TIERED":
                fee = calculateTieredFee(amount, request);
                break;
            default:
                log.debug("未知的费用类型: {}", feeType);
                return BigDecimal.ZERO;
        }

        // 确保手续费不低于最低费用
        return fee.compareTo(minFee) >= 0 ? fee : minFee;
    }

    /**
     * 计算分级费用
     */
    private BigDecimal calculateTieredFee(BigDecimal amount, ConsumeModeRequest request) {
        // 分级费用计算逻辑
        if (amount.compareTo(new BigDecimal("100")) <= 0) {
            return BigDecimal.ZERO; // 100以下免费
        } else if (amount.compareTo(new BigDecimal("500")) <= 0) {
            return new BigDecimal("2.00"); // 100-500收费2元
        } else if (amount.compareTo(new BigDecimal("1000")) <= 0) {
            return new BigDecimal("5.00"); // 500-1000收费5元
        } else {
            return amount.multiply(new BigDecimal("0.005")); // 1000以上收0.5%
        }
    }

    /**
     * 检查金额是否在范围内
     */
    private boolean isAmountInRange(BigDecimal amount, ConsumeModeRequest request) {
        BigDecimal minAmount = request.getModeParam("minAmount", BigDecimal.class, BigDecimal.ONE);
        BigDecimal maxAmount = request.getModeParam("maxAmount", BigDecimal.class, new BigDecimal("99999.99"));

        return amount.compareTo(minAmount) >= 0 && amount.compareTo(maxAmount) <= 0;
    }

    /**
     * 验证商品分类是否有效
     */
    private boolean isValidProductCategory(String category) {
        return "FOOD".equals(category) ||
               "BEVERAGE".equals(category) ||
               "DAILY_GOODS".equals(category) ||
               "CLOTHING".equals(category) ||
               "ELECTRONICS".equals(category) ||
               "BOOKS".equals(category) ||
               "OTHER".equals(category);
    }

    /**
     * 验证折扣类型是否有效
     */
    private boolean isValidDiscountType(String discountType) {
        return "PERCENTAGE".equals(discountType) ||
               "FIXED_AMOUNT".equals(discountType) ||
               "QUANTITY_BASED".equals(discountType);
    }

    /**
     * 验证会员级别是否有效
     */
    private boolean isValidMemberLevel(String memberLevel) {
        return "BRONZE".equals(memberLevel) ||
               "SILVER".equals(memberLevel) ||
               "GOLD".equals(memberLevel) ||
               "PLATINUM".equals(memberLevel);
    }
}