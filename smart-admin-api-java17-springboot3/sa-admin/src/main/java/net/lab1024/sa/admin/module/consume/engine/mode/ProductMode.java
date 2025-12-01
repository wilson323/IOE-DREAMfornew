package net.lab1024.sa.admin.module.consume.engine.mode;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.engine.ConsumeMode;
import net.lab1024.sa.admin.module.consume.engine.ConsumeModeRequest;
import net.lab1024.sa.admin.module.consume.engine.ConsumeModeResult;
import net.lab1024.sa.admin.module.consume.engine.ConsumeModeValidationResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.ArrayList;

/**
 * 商品扫码消费模式
 * 适用于便利店、超市等商品扫码消费场景
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
public class ProductMode implements ConsumptionMode {

    private static final String MODE_CODE = "PRODUCT";
    private static final String MODE_NAME = "商品扫码模式";

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
        return "适用于便利店、超市等商品扫码消费场景，支持商品扫码、批量计算和促销优惠";
    }

    @Override
    public ConsumeModeResult process(ConsumeModeRequest request) {
        try {
            // 获取商品列表
            @SuppressWarnings("unchecked")
            List<ProductItem> products = (List<ProductItem>) request.getModeParam("products", List.class);
            if (products == null || products.isEmpty()) {
                return ConsumeModeResult.failure("NO_PRODUCTS", "商品列表为空");
            }

            // 计算商品总金额
            BigDecimal subtotal = calculateSubtotal(products);

            // 应用促销优惠
            BigDecimal promotionDiscount = BigDecimal.ZERO;
            Boolean hasPromotion = request.getModeParam("hasPromotion", Boolean.class, false);
            if (hasPromotion) {
                promotionDiscount = calculatePromotionDiscount(products, subtotal, request);
            }

            // 应用会员折扣
            BigDecimal memberDiscount = BigDecimal.ZERO;
            String memberLevel = request.getModeParam("memberLevel", String.class);
            if (memberLevel != null) {
                memberDiscount = calculateMemberDiscount(subtotal.subtract(promotionDiscount), memberLevel, request);
            }

            // 应用满减优惠
            BigDecimal fullReductionDiscount = BigDecimal.ZERO;
            Boolean hasFullReduction = request.getModeParam("hasFullReduction", Boolean.class, false);
            if (hasFullReduction) {
                fullReductionDiscount = calculateFullReductionDiscount(subtotal, request);
            }

            // 计算最终金额
            BigDecimal finalAmount = subtotal.subtract(promotionDiscount).subtract(memberDiscount).subtract(fullReductionDiscount);

            // 确保金额不为负数
            if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
                finalAmount = BigDecimal.ZERO;
            }

            // 添加包装费
            BigDecimal packagingFee = BigDecimal.ZERO;
            Boolean hasPackagingFee = request.getModeParam("hasPackagingFee", Boolean.class, false);
            if (hasPackagingFee) {
                packagingFee = calculatePackagingFee(products, request);
            }

            // 添加配送费
            BigDecimal deliveryFee = BigDecimal.ZERO;
            Boolean hasDeliveryFee = request.getModeParam("hasDeliveryFee", Boolean.class, false);
            if (hasDeliveryFee) {
                deliveryFee = request.getModeParam("deliveryFee", BigDecimal.class, BigDecimal.ZERO);
            }

            BigDecimal totalAmount = finalAmount.add(packagingFee).add(deliveryFee);

            // 构建结果
            ConsumeModeResult.ConsumeModeResultBuilder builder = ConsumeModeResult.builder()
                    .success(true)
                    .code("SUCCESS")
                    .message("商品扫码消费处理成功")
                    .modeCode(getModeCode())
                    .originalAmount(subtotal)
                    .finalAmount(totalAmount)
                    .discountAmount(promotionDiscount.add(memberDiscount).add(fullReductionDiscount))
                    .feeAmount(packagingFee.add(deliveryFee));

            // 添加模式特定数据
            builder.setModeData("productCount", products.size());
            builder.setModeData("subtotal", subtotal);
            builder.setModeData("promotionDiscount", promotionDiscount);
            builder.setModeData("memberDiscount", memberDiscount);
            builder.setModeData("fullReductionDiscount", fullReductionDiscount);
            builder.setModeData("packagingFee", packagingFee);
            builder.setModeData("deliveryFee", deliveryFee);
            if (memberLevel != null) {
                builder.setModeData("memberLevel", memberLevel);
            }

            // 添加商品明细
            builder.setModeData("productDetails", products);

            return builder.build();

        } catch (Exception e) {
            log.error("商品扫码模式处理异常", e);
            return ConsumeModeResult.failure("PROCESS_ERROR", "商品扫码模式处理异常: " + e.getMessage());
        }
    }

    @Override
    public ConsumeModeValidationResult validate(ConsumeModeRequest request) {
        try {
            // 验证商品列表
            @SuppressWarnings("unchecked")
            List<ProductItem> products = (List<ProductItem>) request.getModeParam("products", List.class);
            if (products == null || products.isEmpty()) {
                return ConsumeModeValidationResult.failure("NO_PRODUCTS", "商品列表不能为空");
            }

            // 验证商品信息
            for (ProductItem product : products) {
                if (product.getBarcode() == null || product.getBarcode().trim().isEmpty()) {
                    return ConsumeModeValidationResult.failure("INVALID_BARCODE", "商品条码不能为空: " + product.getName());
                }
                if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                    return ConsumeModeValidationResult.failure("INVALID_PRICE", "商品价格无效: " + product.getName());
                }
                if (product.getQuantity() == null || product.getQuantity() <= 0) {
                    return ConsumeModeValidationResult.failure("INVALID_QUANTITY", "商品数量无效: " + product.getName());
                }
            }

            // 验证会员级别
            String memberLevel = request.getModeParam("memberLevel", String.class);
            if (memberLevel != null && !isValidMemberLevel(memberLevel)) {
                return ConsumeModeValidationResult.failure("INVALID_MEMBER_LEVEL", "无效的会员级别: " + memberLevel);
            }

            // 验证促销配置
            Boolean hasPromotion = request.getModeParam("hasPromotion", Boolean.class, false);
            if (hasPromotion) {
                String promotionConfig = request.getModeParam("promotionConfig", String.class);
                if (promotionConfig == null || promotionConfig.trim().isEmpty()) {
                    return ConsumeModeValidationResult.failure("INVALID_PROMOTION_CONFIG", "促销配置不能为空");
                }
            }

            // 验证满减配置
            Boolean hasFullReduction = request.getModeParam("hasFullReduction", Boolean.class, false);
            if (hasFullReduction) {
                String fullReductionConfig = request.getModeParam("fullReductionConfig", String.class);
                if (fullReductionConfig == null || fullReductionConfig.trim().isEmpty()) {
                    return ConsumeModeValidationResult.failure("INVALID_FULL_REDUCTION_CONFIG", "满减配置不能为空");
                }
            }

            // 验证金额限制
            BigDecimal maxAmount = request.getModeParam("maxAmount", BigDecimal.class, new BigDecimal("99999.99"));
            BigDecimal subtotal = calculateSubtotal(products);
            if (subtotal.compareTo(maxAmount) > 0) {
                return ConsumeModeValidationResult.failure("AMOUNT_EXCEEDS_LIMIT",
                    "商品总金额超过最大限制: " + maxAmount);
            }

            return ConsumeModeValidationResult.success();

        } catch (Exception e) {
            log.error("商品扫码模式验证异常", e);
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
            "ELECTRONICS",
            "CLOTHING",
            "BOOKS",
            "OTHER"
          ],
          "promotions": {
            "enabled": false,
            "buyXGetY": {
              "enabled": false,
              "rules": [
                {
                  "buyQuantity": 2,
                  "getQuantity": 1,
                  "applicableCategories": ["FOOD", "BEVERAGE"]
                }
              ]
            },
            "discountedProducts": {
              "enabled": false,
              "products": [
                {
                  "productId": "P001",
                  "discountRate": 0.8,
                  "maxDiscountQuantity": 5
                }
              ]
            },
            "bundlePromotion": {
              "enabled": false,
              "bundles": [
                {
                  "name": "早餐套餐",
                  "products": ["P001", "P002"],
                  "bundlePrice": 15.00,
                  "originalPrice": 18.00
                }
              ]
            }
          },
          "memberDiscount": {
            "enabled": true,
            "levels": {
              "BRONZE": {
                "discountRate": 0.95,
                "minAmount": 50.00
              },
              "SILVER": {
                "discountRate": 0.90,
                "minAmount": 30.00
              },
              "GOLD": {
                "discountRate": 0.85,
                "minAmount": 20.00
              },
              "PLATINUM": {
                "discountRate": 0.80,
                "minAmount": 10.00
              }
            }
          },
          "fullReduction": {
            "enabled": false,
            "rules": [
              {
                "minAmount": 100.00,
                "reductionAmount": 10.00
              },
              {
                "minAmount": 200.00,
                "reductionAmount": 25.00
              },
              {
                "minAmount": 500.00,
                "reductionAmount": 60.00
              }
            ]
          },
          "packagingFee": {
            "enabled": false,
            "feeType": "PER_ITEM",
            "feeAmount": 0.50,
            "freePackagingThreshold": 5
          },
          "deliveryFee": {
            "enabled": false,
            "baseFee": 5.00,
            "freeDeliveryThreshold": 100.00
          },
          "limits": {
            "maxProductCount": 50,
            "maxAmount": 99999.99
          }
        }
        """;
    }

    @Override
    public boolean isApplicableToDevice(Long deviceId) {
        // 商品扫码模式适用于有扫码功能的设备
        return true; // 实际实现中可以根据设备类型判断
    }

    @Override
    public int getPriority() {
        return 70; // 中等优先级
    }

    /**
     * 计算商品小计
     */
    private BigDecimal calculateSubtotal(List<ProductItem> products) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (ProductItem product : products) {
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(product.getQuantity()));
            subtotal = subtotal.add(itemTotal);
        }
        return subtotal.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算促销优惠
     */
    private BigDecimal calculatePromotionDiscount(List<ProductItem> products, BigDecimal subtotal, ConsumeModeRequest request) {
        BigDecimal discount = BigDecimal.ZERO;

        // 买X送Y优惠
        Boolean buyXGetYEnabled = request.getModeParam("buyXGetYEnabled", Boolean.class, false);
        if (buyXGetYEnabled) {
            discount = discount.add(calculateBuyXGetYDiscount(products, request));
        }

        // 商品折扣
        Boolean productDiscountEnabled = request.getModeParam("productDiscountEnabled", Boolean.class, false);
        if (productDiscountEnabled) {
            discount = discount.add(calculateProductDiscount(products, request));
        }

        // 套餐优惠
        Boolean bundlePromotionEnabled = request.getModeParam("bundlePromotionEnabled", Boolean.class, false);
        if (bundlePromotionEnabled) {
            discount = discount.add(calculateBundleDiscount(products, request));
        }

        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算会员折扣
     */
    private BigDecimal calculateMemberDiscount(BigDecimal amount, String memberLevel, ConsumeModeRequest request) {
        BigDecimal minAmount = BigDecimal.ZERO;
        BigDecimal discountRate = BigDecimal.ZERO;

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
                discountRate = new BigDecimal("0.20");
                minAmount = new BigDecimal("10.00");
                break;
            default:
                return BigDecimal.ZERO;
        }

        // 只有当消费金额达到最低要求时才应用会员折扣
        if (amount.compareTo(minAmount) >= 0) {
            return amount.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
        }

        return BigDecimal.ZERO;
    }

    /**
     * 计算满减优惠
     */
    private BigDecimal calculateFullReductionDiscount(BigDecimal subtotal, ConsumeModeRequest request) {
        BigDecimal discount = BigDecimal.ZERO;

        // 简化的满减逻辑
        if (subtotal.compareTo(new BigDecimal("500")) >= 0) {
            discount = new BigDecimal("60"); // 满500减60
        } else if (subtotal.compareTo(new BigDecimal("200")) >= 0) {
            discount = new BigDecimal("25"); // 满200减25
        } else if (subtotal.compareTo(new BigDecimal("100")) >= 0) {
            discount = new BigDecimal("10"); // 满100减10
        }

        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算包装费
     */
    private BigDecimal calculatePackagingFee(List<ProductItem> products, ConsumeModeRequest request) {
        String feeType = request.getModeParam("packagingFeeType", String.class, "PER_ORDER");
        BigDecimal feeAmount = request.getModeParam("packagingFeeAmount", BigDecimal.class, new BigDecimal("0.50"));

        switch (feeType) {
            case "PER_ITEM":
                return BigDecimal.valueOf(products.size()).multiply(feeAmount);
            case "PER_ORDER":
                return feeAmount;
            default:
                return BigDecimal.ZERO;
        }
    }

    /**
     * 计算买X送Y优惠
     */
    private BigDecimal calculateBuyXGetYDiscount(List<ProductItem> products, ConsumeModeRequest request) {
        // 简化的买X送Y逻辑
        BigDecimal discount = BigDecimal.ZERO;

        for (ProductItem product : products) {
            if ("FOOD".equals(product.getCategory()) && product.getQuantity() >= 3) {
                // 买3送1，相当于4件只收3件的钱
                int freeItems = product.getQuantity() / 4;
                BigDecimal freeItemValue = product.getPrice().multiply(BigDecimal.valueOf(freeItems));
                discount = discount.add(freeItemValue);
            }
        }

        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算商品折扣
     */
    private BigDecimal calculateProductDiscount(List<ProductItem> products, ConsumeModeRequest request) {
        // 简化的商品折扣逻辑
        BigDecimal discount = BigDecimal.ZERO;

        for (ProductItem product : products) {
            if (product.getBarcode().startsWith("DISCOUNT_")) {
                // 折扣商品8折
                BigDecimal originalTotal = product.getPrice().multiply(BigDecimal.valueOf(product.getQuantity()));
                BigDecimal discountTotal = originalTotal.multiply(new BigDecimal("0.8"));
                discount = discount.add(originalTotal.subtract(discountTotal));
            }
        }

        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算套餐优惠
     */
    private BigDecimal calculateBundleDiscount(List<ProductItem> products, ConsumeModeRequest request) {
        // 简化的套餐优惠逻辑
        BigDecimal discount = BigDecimal.ZERO;

        // 检查是否有早餐套餐（面包+牛奶）
        boolean hasBread = false;
        boolean hasMilk = false;
        BigDecimal breadPrice = BigDecimal.ZERO;
        BigDecimal milkPrice = BigDecimal.ZERO;

        for (ProductItem product : products) {
            if ("BREAD".equals(product.getBarcode())) {
                hasBread = true;
                breadPrice = product.getPrice();
            } else if ("MILK".equals(product.getBarcode())) {
                hasMilk = true;
                milkPrice = product.getPrice();
            }
        }

        if (hasBread && hasMilk) {
            BigDecimal originalPrice = breadPrice.add(milkPrice);
            BigDecimal bundlePrice = new BigDecimal("15.00"); // 套餐价
            if (originalPrice.compareTo(bundlePrice) > 0) {
                discount = discount.add(originalPrice.subtract(bundlePrice));
            }
        }

        return discount.setScale(2, RoundingMode.HALF_UP);
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

    /**
     * 商品项内部类
     */
    public static class ProductItem {
        private String barcode;
        private String name;
        private String category;
        private BigDecimal price;
        private Integer quantity;
        private String unit;

        // 构造函数
        public ProductItem() {}

        public ProductItem(String barcode, String name, String category, BigDecimal price, Integer quantity) {
            this.barcode = barcode;
            this.name = name;
            this.category = category;
            this.price = price;
            this.quantity = quantity;
        }

        // Getters and Setters
        public String getBarcode() { return barcode; }
        public void setBarcode(String barcode) { this.barcode = barcode; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
    }
}