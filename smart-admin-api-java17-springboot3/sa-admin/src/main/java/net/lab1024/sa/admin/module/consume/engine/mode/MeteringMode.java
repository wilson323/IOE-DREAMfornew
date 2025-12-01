package net.lab1024.sa.admin.module.consume.engine.mode;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.engine.ConsumeMode;
import net.lab1024.sa.admin.module.consume.engine.ConsumeModeRequest;
import net.lab1024.sa.admin.module.consume.engine.ConsumeModeResult;
import net.lab1024.sa.admin.module.consume.engine.ConsumeModeValidationResult;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 计量消费模式
 * 适用于水、电、燃气等按使用量计费的消费场景
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
public class MeteringMode implements ConsumptionMode {

    private static final String MODE_CODE = "METERING";
    private static final String MODE_NAME = "计量模式";

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
        return "适用于水、电、燃气等按使用量计费的消费场景，支持阶梯定价和实时计量";
    }

    @Override
    public ConsumeModeResult process(ConsumeModeRequest request) {
        try {
            BigDecimal unitPrice = request.getModeParam("unitPrice", BigDecimal.class);
            BigDecimal usage = request.getModeParam("usage", BigDecimal.class);
            String meterType = request.getModeParam("meterType", String.class, "GENERAL");

            // 计算基础费用
            BigDecimal baseAmount = calculateBaseAmount(unitPrice, usage, meterType);

            // 应用阶梯定价
            BigDecimal tieredAmount = applyTieredPricing(baseAmount, usage, request);

            // 应用时段优惠
            BigDecimal timeDiscount = BigDecimal.ZERO;
            Boolean hasTimeDiscount = request.getModeParam("hasTimeDiscount", Boolean.class, false);
            if (hasTimeDiscount) {
                timeDiscount = calculateTimeDiscount(tieredAmount, request);
            }

            // 应用季节性调整
            BigDecimal seasonalAdjustment = BigDecimal.ZERO;
            Boolean hasSeasonalAdjustment = request.getModeParam("hasSeasonalAdjustment", Boolean.class, false);
            if (hasSeasonalAdjustment) {
                seasonalAdjustment = calculateSeasonalAdjustment(tieredAmount, request);
            }

            // 计算最终金额
            BigDecimal finalAmount = tieredAmount.subtract(timeDiscount).add(seasonalAdjustment);

            // 添加服务费（如果有）
            BigDecimal serviceFee = BigDecimal.ZERO;
            Boolean hasServiceFee = request.getModeParam("hasServiceFee", Boolean.class, false);
            if (hasServiceFee) {
                serviceFee = calculateServiceFee(finalAmount, request);
            }

            BigDecimal totalAmount = finalAmount.add(serviceFee);

            // 构建结果
            ConsumeModeResult.ConsumeModeResultBuilder builder = ConsumeModeResult.builder()
                    .success(true)
                    .code("SUCCESS")
                    .message("计量消费处理成功")
                    .modeCode(getModeCode())
                    .originalAmount(baseAmount)
                    .finalAmount(totalAmount)
                    .discountAmount(timeDiscount)
                    .feeAmount(serviceFee);

            // 添加模式特定数据
            builder.setModeData("meterType", meterType);
            builder.setModeData("unitPrice", unitPrice);
            builder.setModeData("usage", usage);
            builder.setModeData("baseAmount", baseAmount);
            builder.setModeData("tieredAmount", tieredAmount);
            if (hasTimeDiscount) {
                builder.setModeData("timeDiscountApplied", true);
                builder.setModeData("timeDiscountAmount", timeDiscount);
            }
            if (hasSeasonalAdjustment) {
                builder.setModeData("seasonalAdjustmentApplied", true);
                builder.setModeData("seasonalAdjustmentAmount", seasonalAdjustment);
            }

            return builder.build();

        } catch (Exception e) {
            log.error("计量模式处理异常", e);
            return ConsumeModeResult.failure("PROCESS_ERROR", "计量模式处理异常: " + e.getMessage());
        }
    }

    @Override
    public ConsumeModeValidationResult validate(ConsumeModeRequest request) {
        try {
            // 基础参数验证
            BigDecimal unitPrice = request.getModeParam("unitPrice", BigDecimal.class);
            if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                return ConsumeModeValidationResult.failure("INVALID_UNIT_PRICE", "单价必须大于0");
            }

            BigDecimal usage = request.getModeParam("usage", BigDecimal.class);
            if (usage == null || usage.compareTo(BigDecimal.ZERO) < 0) {
                return ConsumeModeValidationResult.failure("INVALID_USAGE", "使用量不能为负数");
            }

            // 验证计量类型
            String meterType = request.getModeParam("meterType", String.class);
            if (meterType != null && !isValidMeterType(meterType)) {
                return ConsumeModeValidationResult.failure("INVALID_METER_TYPE", "无效的计量类型: " + meterType);
            }

            // 验证阶梯定价配置
            Boolean hasTieredPricing = request.getModeParam("hasTieredPricing", Boolean.class, false);
            if (hasTieredPricing) {
                String tierConfig = request.getModeParam("tierConfig", String.class);
                if (tierConfig == null || tierConfig.trim().isEmpty()) {
                    return ConsumeModeValidationResult.failure("INVALID_TIER_CONFIG", "阶梯定价配置不能为空");
                }
            }

            // 验证使用量限制
            BigDecimal maxUsage = request.getModeParam("maxUsage", BigDecimal.class);
            if (maxUsage != null && usage.compareTo(maxUsage) > 0) {
                return ConsumeModeValidationResult.failure("USAGE_EXCEEDS_LIMIT",
                    "使用量超过最大限制: " + maxUsage);
            }

            // 验证时段优惠配置
            Boolean hasTimeDiscount = request.getModeParam("hasTimeDiscount", Boolean.class, false);
            if (hasTimeDiscount) {
                String timeDiscountConfig = request.getModeParam("timeDiscountConfig", String.class);
                if (timeDiscountConfig == null || timeDiscountConfig.trim().isEmpty()) {
                    return ConsumeModeValidationResult.failure("INVALID_TIME_DISCOUNT_CONFIG", "时段优惠配置不能为空");
                }
            }

            return ConsumeModeValidationResult.success();

        } catch (Exception e) {
            log.error("计量模式验证异常", e);
            return ConsumeModeValidationResult.failure("VALIDATION_ERROR", "验证异常: " + e.getMessage());
        }
    }

    @Override
    public String getConfigTemplate() {
        return """
        {
          "meterType": "ELECTRICITY",
          "meterTypes": ["ELECTRICITY", "WATER", "GAS", "HEAT", "GENERAL"],
          "unitPrice": 0.55,
          "unitPriceRange": {
            "minPrice": 0.01,
            "maxPrice": 99.99
          },
          "tieredPricing": {
            "enabled": true,
            "tiers": [
              {
                "minUsage": 0,
                "maxUsage": 100,
                "priceMultiplier": 1.0
              },
              {
                "minUsage": 101,
                "maxUsage": 300,
                "priceMultiplier": 1.2
              },
              {
                "minUsage": 301,
                "maxUsage": null,
                "priceMultiplier": 1.5
              }
            ]
          },
          "timeDiscount": {
            "enabled": false,
            "peakHours": {
              "start": "08:00",
              "end": "22:00",
              "surchargeRate": 0.2
            },
            "offPeakDiscount": {
              "rate": 0.1
            }
          },
          "seasonalAdjustment": {
            "enabled": false,
            "seasons": {
              "SUMMER": {
                "adjustmentRate": 0.15,
                "months": [6, 7, 8]
              },
              "WINTER": {
                "adjustmentRate": 0.10,
                "months": [12, 1, 2]
              }
            }
          },
          "serviceFee": {
            "enabled": false,
            "feeType": "PERCENTAGE",
            "feeRate": 0.02,
            "minFee": 0.50
          },
          "limits": {
            "maxUsage": 999999.99,
            "maxAmount": 99999.99
          }
        }
        """;
    }

    @Override
    public boolean isApplicableToDevice(Long deviceId) {
        // 计量模式适用于智能表计设备
        return true; // 实际实现中可以根据设备类型判断
    }

    @Override
    public int getPriority() {
        return 60; // 中等优先级
    }

    /**
     * 计算基础费用
     */
    private BigDecimal calculateBaseAmount(BigDecimal unitPrice, BigDecimal usage, String meterType) {
        BigDecimal baseAmount = unitPrice.multiply(usage);

        // 根据计量类型进行调整
        switch (meterType) {
            case "ELECTRICITY":
                // 电力通常按千瓦时计费，可能需要功率因数调整
                break;
            case "WATER":
                // 水费可能包含污水处理费
                BigDecimal sewageFee = baseAmount.multiply(new BigDecimal("0.3"));
                baseAmount = baseAmount.add(sewageFee);
                break;
            case "GAS":
                // 燃气费可能有热值调整
                break;
            case "HEAT":
                // 供热按平方米或吉焦计费
                break;
        }

        return baseAmount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 应用阶梯定价
     */
    private BigDecimal applyTieredPricing(BigDecimal baseAmount, BigDecimal usage, ConsumeModeRequest request) {
        Boolean hasTieredPricing = request.getModeParam("hasTieredPricing", Boolean.class, false);
        if (!hasTieredPricing) {
            return baseAmount;
        }

        // 简化的阶梯定价逻辑
        BigDecimal multiplier = BigDecimal.ONE;

        if (usage.compareTo(new BigDecimal("100")) <= 0) {
            multiplier = BigDecimal.ONE; // 第一档
        } else if (usage.compareTo(new BigDecimal("300")) <= 0) {
            multiplier = new BigDecimal("1.2"); // 第二档
        } else {
            multiplier = new BigDecimal("1.5"); // 第三档
        }

        return baseAmount.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算时段优惠
     */
    private BigDecimal calculateTimeDiscount(BigDecimal amount, ConsumeModeRequest request) {
        // 简化的时段优惠逻辑
        // 实际实现应该根据当前时间和配置计算
        return BigDecimal.ZERO;
    }

    /**
     * 计算季节性调整
     */
    private BigDecimal calculateSeasonalAdjustment(BigDecimal amount, ConsumeModeRequest request) {
        // 简化的季节性调整逻辑
        // 实际实现应该根据当前月份和配置计算
        return BigDecimal.ZERO;
    }

    /**
     * 计算服务费
     */
    private BigDecimal calculateServiceFee(BigDecimal amount, ConsumeModeRequest request) {
        String feeType = request.getModeParam("feeType", String.class, "PERCENTAGE");
        BigDecimal feeRate = request.getModeParam("feeRate", BigDecimal.class, new BigDecimal("0.02"));
        BigDecimal minFee = request.getModeParam("minFee", BigDecimal.class, new BigDecimal("0.50"));

        BigDecimal fee = BigDecimal.ZERO;

        switch (feeType) {
            case "PERCENTAGE":
                fee = amount.multiply(feeRate);
                break;
            case "FIXED_AMOUNT":
                fee = request.getModeParam("feeAmount", BigDecimal.class, BigDecimal.ZERO);
                break;
        }

        // 确保服务费不低于最低费用
        return fee.compareTo(minFee) >= 0 ? fee : minFee;
    }

    /**
     * 验证计量类型是否有效
     */
    private boolean isValidMeterType(String meterType) {
        return "ELECTRICITY".equals(meterType) ||
               "WATER".equals(meterType) ||
               "GAS".equals(meterType) ||
               "HEAT".equals(meterType) ||
               "GENERAL".equals(meterType);
    }
}