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
 * 点餐消费模式
 * 适用于餐厅、食堂等点餐消费场景
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
public class OrderingMode implements ConsumptionMode {

    private static final String MODE_CODE = "ORDERING";
    private static final String MODE_NAME = "点餐模式";

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
        return "适用于餐厅、食堂等点餐消费场景，支持菜单点餐、桌位管理和分单合单";
    }

    @Override
    public ConsumeModeResult process(ConsumeModeRequest request) {
        try {
            // 获取订单信息
            @SuppressWarnings("unchecked")
            List<OrderItem> orderItems = (List<OrderItem>) request.getModeParam("orderItems", List.class);
            if (orderItems == null || orderItems.isEmpty()) {
                return ConsumeModeResult.failure("NO_ORDER_ITEMS", "订单项为空");
            }

            // 获取桌位信息
            String tableId = request.getModeParam("tableId", String.class);
            Integer guestCount = request.getModeParam("guestCount", Integer.class, 1);

            // 计算菜品金额
            BigDecimal subtotal = calculateOrderSubtotal(orderItems);

            // 应用服务费
            BigDecimal serviceFee = BigDecimal.ZERO;
            Boolean hasServiceFee = request.getModeParam("hasServiceFee", Boolean.class, false);
            if (hasServiceFee) {
                serviceFee = calculateServiceFee(subtotal, guestCount, request);
            }

            // 应用餐位费
            BigDecimal tableFee = BigDecimal.ZERO;
            Boolean hasTableFee = request.getModeParam("hasTableFee", Boolean.class, false);
            if (hasTableFee) {
                tableFee = calculateTableFee(tableId, guestCount, request);
            }

            // 应用会员优惠
            BigDecimal memberDiscount = BigDecimal.ZERO;
            String memberLevel = request.getModeParam("memberLevel", String.class);
            if (memberLevel != null) {
                memberDiscount = calculateMemberDiscount(subtotal, memberLevel, request);
            }

            // 应用时段优惠
            BigDecimal timeDiscount = BigDecimal.ZERO;
            Boolean hasTimeDiscount = request.getModeParam("hasTimeDiscount", Boolean.class, false);
            if (hasTimeDiscount) {
                timeDiscount = calculateTimeDiscount(subtotal, request);
            }

            // 应用优惠券
            BigDecimal couponDiscount = BigDecimal.ZERO;
            String couponCode = request.getModeParam("couponCode", String.class);
            if (couponCode != null && !couponCode.trim().isEmpty()) {
                couponDiscount = calculateCouponDiscount(subtotal, couponCode, request);
            }

            // 计算最终金额
            BigDecimal finalAmount = subtotal.add(serviceFee).add(tableFee)
                    .subtract(memberDiscount).subtract(timeDiscount).subtract(couponDiscount);

            // 确保金额不为负数
            if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
                finalAmount = BigDecimal.ZERO;
            }

            // 构建结果
            ConsumeModeResult.ConsumeModeResultBuilder builder = ConsumeModeResult.builder()
                    .success(true)
                    .code("SUCCESS")
                    .message("点餐消费处理成功")
                    .modeCode(getModeCode())
                    .originalAmount(subtotal)
                    .finalAmount(finalAmount)
                    .discountAmount(memberDiscount.add(timeDiscount).add(couponDiscount))
                    .feeAmount(serviceFee.add(tableFee));

            // 添加模式特定数据
            builder.setModeData("tableId", tableId);
            builder.setModeData("guestCount", guestCount);
            builder.setModeData("itemCount", orderItems.size());
            builder.setModeData("subtotal", subtotal);
            builder.setModeData("serviceFee", serviceFee);
            builder.setModeData("tableFee", tableFee);
            builder.setModeData("memberDiscount", memberDiscount);
            builder.setModeData("timeDiscount", timeDiscount);
            builder.setModeData("couponDiscount", couponDiscount);
            if (memberLevel != null) {
                builder.setModeData("memberLevel", memberLevel);
            }
            if (couponCode != null) {
                builder.setModeData("couponCode", couponCode);
            }

            // 添加订单明细
            builder.setModeData("orderDetails", orderItems);

            return builder.build();

        } catch (Exception e) {
            log.error("点餐模式处理异常", e);
            return ConsumeModeResult.failure("PROCESS_ERROR", "点餐模式处理异常: " + e.getMessage());
        }
    }

    @Override
    public ConsumeModeValidationResult validate(ConsumeModeRequest request) {
        try {
            // 验证订单项
            @SuppressWarnings("unchecked")
            List<OrderItem> orderItems = (List<OrderItem>) request.getModeParam("orderItems", List.class);
            if (orderItems == null || orderItems.isEmpty()) {
                return ConsumeModeValidationResult.failure("NO_ORDER_ITEMS", "订单项不能为空");
            }

            // 验证订单详情
            for (OrderItem item : orderItems) {
                if (item.getMenuItemId() == null || item.getMenuItemId().trim().isEmpty()) {
                    return ConsumeModeValidationResult.failure("INVALID_MENU_ITEM", "菜品ID不能为空: " + item.getName());
                }
                if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                    return ConsumeModeValidationResult.failure("INVALID_PRICE", "菜品价格无效: " + item.getName());
                }
                if (item.getQuantity() == null || item.getQuantity() <= 0) {
                    return ConsumeModeValidationResult.failure("INVALID_QUANTITY", "菜品数量无效: " + item.getName());
                }
            }

            // 验证桌位信息
            String tableId = request.getModeParam("tableId", String.class);
            if (tableId != null && !isValidTableId(tableId)) {
                return ConsumeModeValidationResult.failure("INVALID_TABLE_ID", "无效的桌位ID: " + tableId);
            }

            // 验证客人数量
            Integer guestCount = request.getModeParam("guestCount", Integer.class, 1);
            if (guestCount <= 0 || guestCount > 20) {
                return ConsumeModeValidationResult.failure("INVALID_GUEST_COUNT", "客人数量无效: " + guestCount);
            }

            // 验证会员级别
            String memberLevel = request.getModeParam("memberLevel", String.class);
            if (memberLevel != null && !isValidMemberLevel(memberLevel)) {
                return ConsumeModeValidationResult.failure("INVALID_MEMBER_LEVEL", "无效的会员级别: " + memberLevel);
            }

            // 验证优惠券
            String couponCode = request.getModeParam("couponCode", String.class);
            if (couponCode != null && !isValidCouponCode(couponCode)) {
                return ConsumeModeValidationResult.failure("INVALID_COUPON_CODE", "无效的优惠券码: " + couponCode);
            }

            // 验证订单金额限制
            BigDecimal maxAmount = request.getModeParam("maxAmount", BigDecimal.class, new BigDecimal("99999.99"));
            BigDecimal subtotal = calculateOrderSubtotal(orderItems);
            if (subtotal.compareTo(maxAmount) > 0) {
                return ConsumeModeValidationResult.failure("AMOUNT_EXCEEDS_LIMIT",
                    "订单金额超过最大限制: " + maxAmount);
            }

            return ConsumeModeValidationResult.success();

        } catch (Exception e) {
            log.error("点餐模式验证异常", e);
            return ConsumeModeValidationResult.failure("VALIDATION_ERROR", "验证异常: " + e.getMessage());
        }
    }

    @Override
    public String getConfigTemplate() {
        return """
        {
          "restaurantInfo": {
            "name": "餐厅名称",
            "type": "CHINESE|WESTERN|BUFFET|CAFE",
            "cuisine": "菜系类型"
          },
          "tableManagement": {
            "enabled": true,
            "tableTypes": [
              {
                "type": "STANDARD",
                "minCapacity": 2,
                "maxCapacity": 4,
                "tableFee": 0.00
              },
              {
                "type": "VIP",
                "minCapacity": 2,
                "maxCapacity": 6,
                "tableFee": 50.00
              },
              {
                "type": "PRIVATE",
                "minCapacity": 8,
                "maxCapacity": 20,
                "tableFee": 200.00
              }
            ]
          },
          "serviceFee": {
            "enabled": false,
            "feeType": "PERCENTAGE",
            "feeRate": 0.10,
            "minFee": 5.00,
            "excludedCategories": ["BEVERAGE"]
          },
          "memberDiscount": {
            "enabled": true,
            "levels": {
              "BRONZE": {
                "discountRate": 0.95,
                "applicableCategories": ["FOOD"],
                "minAmount": 100.00
              },
              "SILVER": {
                "discountRate": 0.90,
                "applicableCategories": ["FOOD", "BEVERAGE"],
                "minAmount": 50.00
              },
              "GOLD": {
                "discountRate": 0.85,
                "applicableCategories": ["ALL"],
                "minAmount": 30.00
              },
              "PLATINUM": {
                "discountRate": 0.80,
                "applicableCategories": ["ALL"],
                "minAmount": 20.00
              }
            }
          },
          "timeDiscount": {
            "enabled": false,
            "periods": [
              {
                "name": "午餐特惠",
                "startTime": "11:00",
                "endTime": "14:00",
                "discountRate": 0.9,
                "applicableDays": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"]
              },
              {
                "name": "晚餐特惠",
                "startTime": "17:00",
                "endTime": "20:00",
                "discountRate": 0.95,
                "applicableDays": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "SUNDAY"]
              }
            ]
          },
          "couponSystem": {
            "enabled": true,
            "maxCouponsPerOrder": 3,
            "stackingRules": "ALLOWED|FORBIDDEN",
            "couponTypes": ["DISCOUNT", "CASH_VOUCHER", "FREE_ITEM", "BOGO"]
          },
          "orderRestrictions": {
            "maxItemsPerOrder": 50,
            "maxAmountPerOrder": 99999.99,
            "maxGuestsPerTable": 20,
            "minOrderAmount": 10.00
          }
        }
        """;
    }

    @Override
    public boolean isApplicableToDevice(Long deviceId) {
        // 点餐模式适用于餐饮设备
        return true; // 实际实现中可以根据设备类型判断
    }

    @Override
    public int getPriority() {
        return 90; // 高优先级
    }

    /**
     * 计算订单小计
     */
    private BigDecimal calculateOrderSubtotal(List<OrderItem> orderItems) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItem item : orderItems) {
            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(itemTotal);
        }
        return subtotal.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算服务费
     */
    private BigDecimal calculateServiceFee(BigDecimal subtotal, Integer guestCount, ConsumeModeRequest request) {
        String feeType = request.getModeParam("serviceFeeType", String.class, "PERCENTAGE");
        BigDecimal feeRate = request.getModeParam("serviceFeeRate", BigDecimal.class, new BigDecimal("0.10"));
        BigDecimal minFee = request.getModeParam("serviceMinFee", BigDecimal.class, new BigDecimal("5.00"));

        BigDecimal fee = BigDecimal.ZERO;

        switch (feeType) {
            case "PERCENTAGE":
                fee = subtotal.multiply(feeRate);
                break;
            case "PER_GUEST":
                BigDecimal perGuestFee = request.getModeParam("perGuestFee", BigDecimal.class, new BigDecimal("2.00"));
                fee = perGuestFee.multiply(BigDecimal.valueOf(guestCount));
                break;
            case "FIXED_AMOUNT":
                fee = request.getModeParam("fixedServiceFee", BigDecimal.class, BigDecimal.ZERO);
                break;
            case "TIERED":
                fee = calculateTieredServiceFee(subtotal, guestCount, request);
                break;
        }

        // 确保服务费不低于最低费用
        return fee.compareTo(minFee) >= 0 ? fee : minFee;
    }

    /**
     * 计算餐位费
     */
    private BigDecimal calculateTableFee(String tableId, Integer guestCount, ConsumeModeRequest request) {
        if (tableId == null) {
            return BigDecimal.ZERO;
        }

        String tableType = request.getModeParam("tableType", String.class, "STANDARD");
        BigDecimal baseFee = BigDecimal.ZERO;

        switch (tableType) {
            case "STANDARD":
                baseFee = BigDecimal.ZERO;
                break;
            case "VIP":
                baseFee = new BigDecimal("50.00");
                break;
            case "PRIVATE":
                baseFee = new BigDecimal("200.00");
                break;
        }

        // 根据客人数量调整费用
        if (guestCount > 6) {
            baseFee = baseFee.add(new BigDecimal("20.00"));
        }

        return baseFee.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算会员折扣
     */
    private BigDecimal calculateMemberDiscount(BigDecimal subtotal, String memberLevel, ConsumeModeRequest request) {
        BigDecimal minAmount = BigDecimal.ZERO;
        BigDecimal discountRate = BigDecimal.ZERO;
        String applicableCategories = request.getModeParam("memberDiscountCategories", String.class, "ALL");

        switch (memberLevel) {
            case "BRONZE":
                discountRate = new BigDecimal("0.05");
                minAmount = new BigDecimal("100.00");
                break;
            case "SILVER":
                discountRate = new BigDecimal("0.10");
                minAmount = new BigDecimal("50.00");
                break;
            case "GOLD":
                discountRate = new BigDecimal("0.15");
                minAmount = new BigDecimal("30.00");
                break;
            case "PLATINUM":
                discountRate = new BigDecimal("0.20");
                minAmount = new BigDecimal("20.00");
                break;
            default:
                return BigDecimal.ZERO;
        }

        // 只有当消费金额达到最低要求时才应用会员折扣
        if (subtotal.compareTo(minAmount) >= 0) {
            return subtotal.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
        }

        return BigDecimal.ZERO;
    }

    /**
     * 计算时段优惠
     */
    private BigDecimal calculateTimeDiscount(BigDecimal subtotal, ConsumeModeRequest request) {
        // 简化的时段优惠逻辑
        // 实际实现应该根据当前时间和配置计算
        return BigDecimal.ZERO;
    }

    /**
     * 计算优惠券折扣
     */
    private BigDecimal calculateCouponDiscount(BigDecimal subtotal, String couponCode, ConsumeModeRequest request) {
        // 简化的优惠券逻辑
        if (couponCode.startsWith("DISCOUNT_")) {
            String rateStr = couponCode.substring(9); // 去掉 "DISCOUNT_" 前缀
            try {
                BigDecimal discountRate = new BigDecimal(rateStr).divide(new BigDecimal("100"));
                return subtotal.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
            } catch (NumberFormatException e) {
                log.warn("无效的折扣率格式: {}", rateStr);
            }
        } else if (couponCode.startsWith("VOUCHER_")) {
            String amountStr = couponCode.substring(8); // 去掉 "VOUCHER_" 前缀
            try {
                BigDecimal voucherAmount = new BigDecimal(amountStr);
                return voucherAmount.compareTo(subtotal) >= 0 ? subtotal : voucherAmount;
            } catch (NumberFormatException e) {
                log.warn("无效的代金券金额格式: {}", amountStr);
            }
        }

        return BigDecimal.ZERO;
    }

    /**
     * 计算分级服务费
     */
    private BigDecimal calculateTieredServiceFee(BigDecimal subtotal, Integer guestCount, ConsumeModeRequest request) {
        // 简化的分级服务费计算
        if (subtotal.compareTo(new BigDecimal("1000")) >= 0) {
            return subtotal.multiply(new BigDecimal("0.15")); // 高消费15%服务费
        } else if (subtotal.compareTo(new BigDecimal("500")) >= 0) {
            return subtotal.multiply(new BigDecimal("0.12")); // 中消费12%服务费
        } else {
            return new BigDecimal("20.00"); // 低消费固定20元
        }
    }

    /**
     * 验证桌位ID是否有效
     */
    private boolean isValidTableId(String tableId) {
        if (tableId == null || tableId.trim().isEmpty()) {
            return false;
        }
        // 简化验证：桌位ID应该是字母数字组合
        return tableId.matches("^[A-Z0-9]+$");
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
     * 验证优惠券码是否有效
     */
    private boolean isValidCouponCode(String couponCode) {
        if (couponCode == null || couponCode.trim().isEmpty()) {
            return false;
        }
        // 简化验证：优惠券码应该是字母数字和下划线组合
        return couponCode.matches("^[A-Z0-9_]+$");
    }

    /**
     * 订单项内部类
     */
    public static class OrderItem {
        private String menuItemId;
        private String name;
        private String category;
        private BigDecimal price;
        private Integer quantity;
        private String specifications;
        private String remarks;

        // 构造函数
        public OrderItem() {}

        public OrderItem(String menuItemId, String name, String category, BigDecimal price, Integer quantity) {
            this.menuItemId = menuItemId;
            this.name = name;
            this.category = category;
            this.price = price;
            this.quantity = quantity;
        }

        // Getters and Setters
        public String getMenuItemId() { return menuItemId; }
        public void setMenuItemId(String menuItemId) { this.menuItemId = menuItemId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public String getSpecifications() { return specifications; }
        public void setSpecifications(String specifications) { this.specifications = specifications; }

        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
    }
}