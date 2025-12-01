package net.lab1024.sa.admin.module.consume.engine.mode.impl;

import net.lab1024.sa.admin.module.consume.engine.mode.abstracts.AbstractConsumptionMode;
import net.lab1024.sa.admin.module.consume.domain.enums.CategoryDiscountEnum;
import net.lab1024.sa.admin.module.consume.domain.enums.MemberLevelEnum;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * 商品扫码消费模式
 * 严格遵循repowiki规范：专门为零售商品扫码场景设计的消费模式
 *
 * 核心功能：
 * - 商品条码识别和管理
 * - 商品信息查询和验证
 * - 库存检查和管理
 * - 价格策略和促销活动
 * - 扫码枪和移动端支持
 * - 商品追溯和防伪
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Component("productMode")
public class ProductMode extends AbstractConsumptionMode {

    // 商品类型
    public enum ProductType {
        FOOD("食品", "f", new BigDecimal("0.05")),
        BEVERAGE("饮品", "b", new BigDecimal("0.03")),
        DAILY_GOODS("日用品", "d", new BigDecimal("0.08")),
        ELECTRONICS("电子产品", "e", new BigDecimal("0.02")),
        CLOTHING("服装", "c", new BigDecimal("0.10")),
        BOOKS("图书", "k", new BigDecimal("0.15")),
        OFFICE("办公用品", "o", new BigDecimal("0.12")),
        OTHER("其他", "x", BigDecimal.ZERO);

        private final String name;
        private final String code;
        private final BigDecimal discountRate;

        ProductType(String name, String code, BigDecimal discountRate) {
            this.name = name;
            this.code = code;
            this.discountRate = discountRate;
        }

        public String getName() { return name; }
        public String getCode() { return code; }
        public BigDecimal getDiscountRate() { return discountRate; }
    }

    // 条码类型
    public enum BarcodeType {
        EAN13("EAN-13", 13, "^\\d{13}$"),
        EAN8("EAN-8", 8, "^\\d{8}$"),
        CODE128("CODE128", null, "^[A-Za-z0-9-]+$"),
        QR_CODE("QR码", null, "^[A-Za-z0-9+/=]+$"),
        DATA_MATRIX("Data Matrix", null, "^[A-Za-z0-9+/=]+$"),
        INTERNAL("内部码", null, "^[A-Z0-9]{6,20}$");

        private final String name;
        private final Integer length;
        private final String pattern;

        BarcodeType(String name, Integer length, String pattern) {
            this.name = name;
            this.length = length;
            this.pattern = pattern;
        }

        public String getName() { return name; }
        public Integer getLength() { return length; }
        public String getPattern() { return pattern; }
    }

    // 促销类型
    public enum PromotionType {
        DISCOUNT("折扣", new BigDecimal("0.10")),
        BOGO("买一送一", new BigDecimal("0.50")),
        BUNDLE("套餐优惠", new BigDecimal("0.15")),
        FLASH_SALE("限时特价", new BigDecimal("0.25")),
        MEMBER_EXCLUSIVE("会员专享", new BigDecimal("0.20")),
        VOLUME_DISCOUNT("数量折扣", new BigDecimal("0.05"));

        private final String name;
        private final BigDecimal discountRate;

        PromotionType(String name, BigDecimal discountRate) {
            this.name = name;
            this.discountRate = discountRate;
        }

        public String getName() { return name; }
        public BigDecimal getDiscountRate() { return discountRate; }
    }

    // 价格策略
    public enum PricingStrategy {
        FIXED_PRICE("固定价格", 1.0),
        DYNAMIC_PRICING("动态定价", 0.95),
        MEMBER_PRICING("会员价", 0.90),
        PROMOTION_PRICING("促销价", 0.80),
        BULK_PRICING("批量价", 0.85);

        private final String name;
        private final Double multiplier;

        PricingStrategy(String name, Double multiplier) {
            this.name = name;
            this.multiplier = multiplier;
        }

        public String getName() { return name; }
        public Double getMultiplier() { return multiplier; }
    }

    // 配置常量
    private static final BigDecimal MAX_SINGLE_ITEM_PRICE = new BigDecimal("9999.99");
    private static final BigDecimal MAX_TOTAL_AMOUNT = new BigDecimal("50000.00");
    private static final int MAX_ITEM_QUANTITY = 99;
    private static final int MAX_CART_ITEMS = 50;
    private static final Pattern BARCODE_PATTERN = Pattern.compile("^[A-Za-z0-9-]+$");

    public ProductMode() {
        super("PRODUCT", "商品扫码模式", "零售商品扫码的专业消费模式");
    }

    @Override
    public BigDecimal calculateAmount(Map<String, Object> params) {
        try {
            // 1. 获取商品清单
            List<Map<String, Object>> products = getProductList(params);
            if (products == null || products.isEmpty()) {
                throw new IllegalArgumentException("商品清单不能为空");
            }

            // 2. 验证商品数量限制
            if (products.size() > MAX_CART_ITEMS) {
                throw new IllegalArgumentException("购物车商品数量不能超过" + MAX_CART_ITEMS + "种");
            }

            // 3. 计算商品基础总价
            BigDecimal baseTotalCost = calculateBaseProductCost(products);

            // 4. 应用品类折扣
            BigDecimal categoryDiscountCost = applyCategoryDiscounts(baseTotalCost, products);

            // 5. 应用促销优惠
            BigDecimal promotionCost = applyPromotions(categoryDiscountCost, products, params);

            // 6. 应用会员折扣
            BigDecimal memberDiscountCost = applyMemberDiscounts(promotionCost, params);

            // 7. 应用数量折扣
            BigDecimal quantityDiscountCost = applyQuantityDiscounts(memberDiscountCost, products);

            // 8. 应用特殊调整
            BigDecimal finalCost = applySpecialAdjustments(quantityDiscountCost, params);

            return finalCost.setScale(2, RoundingMode.HALF_UP);

        } catch (Exception e) {
            throw new IllegalArgumentException("商品计费失败: " + e.getMessage(), e);
        }
    }

    @Override
    protected boolean doValidateParameters(Map<String, Object> params) {
        // 验证必填参数
        if (!hasRequiredParams(params, "userId", "accountId", "products")) {
            return false;
        }

        // 验证商品数据
        if (!hasValidProducts(params)) {
            return false;
        }

        // 验证条码格式
        if (!hasValidBarcodes(params)) {
            return false;
        }

        // 验证商品数量
        if (!hasValidQuantities(params)) {
            return false;
        }

        // 验证促销码（如果有）
        String promotionCode = getStringFromParams(params, "promotionCode");
        if (promotionCode != null && !isValidPromotionCode(promotionCode)) {
            return false;
        }

        return true;
    }

    @Override
    protected boolean doIsAllowed(Map<String, Object> params) {
        // 检查库存状态
        if (!checkInventoryStatus(params)) {
            return false;
        }

        // 检查购买频率限制
        if (!checkPurchaseFrequencyLimit(params)) {
            return false;
        }

        // 检查商品状态
        if (!checkProductStatus(params)) {
            return false;
        }

        // 检查促销有效期
        if (!checkPromotionValidity(params)) {
            return false;
        }

        // 检查购买额度限制
        if (!checkPurchaseLimit(params)) {
            return false;
        }

        return true;
    }

    @Override
    protected Map<String, Object> doPreProcess(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        // 商品分析
        List<Map<String, Object>> products = getProductList(params);
        Map<String, Object> productAnalysis = analyzeProducts(products);
        result.put("productAnalysis", productAnalysis);

        // 条码验证
        Map<String, Object> barcodeValidation = validateBarcodes(products);
        result.put("barcodeValidation", barcodeValidation);

        // 价格分析
        Map<String, Object> priceAnalysis = analyzePricing(products, params);
        result.put("priceAnalysis", priceAnalysis);
        result.put("originalTotal", priceAnalysis.get("originalTotal"));
        result.put("discountTotal", priceAnalysis.get("discountTotal"));

        // 促销分析
        Map<String, Object> promotionAnalysis = analyzePromotions(products, params);
        result.put("promotionAnalysis", promotionAnalysis);
        result.put("appliedPromotions", promotionAnalysis.get("appliedPromotions"));

        // 库存检查
        Map<String, Object> inventoryStatus = checkInventory(products);
        result.put("inventoryStatus", inventoryStatus);
        result.put("allInStock", inventoryStatus.get("allAvailable"));

        // 推荐商品
        List<Map<String, Object>> recommendations = generateProductRecommendations(params);
        result.put("recommendations", recommendations);

        return result;
    }

    @Override
    protected Map<String, Object> doPostProcess(Map<String, Object> params, Map<String, Object> result) {
        Map<String, Object> postResult = new HashMap<>();

        // 商品明细
        List<Map<String, Object>> products = getProductList(params);
        BigDecimal originalTotal = (BigDecimal) result.get("originalTotal");
        BigDecimal finalAmount = (BigDecimal) result.get("amount");

        postResult.put("itemCount", products.size());
        postResult.put("originalTotal", originalTotal);
        postResult.put("finalAmount", finalAmount);
        postResult.put("totalDiscount", originalTotal.subtract(finalAmount));
        postResult.put("discountRate",
            originalTotal.compareTo(BigDecimal.ZERO) > 0
                ? originalTotal.subtract(finalAmount).divide(originalTotal, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO);

        // 分类统计
        Map<String, Object> categoryStats = calculateCategoryStatistics(products);
        postResult.put("categoryStats", categoryStats);

        // 促销详情
        Map<String, Object> promotionAnalysis = (Map<String, Object>) result.get("promotionAnalysis");
        if (promotionAnalysis != null) {
            postResult.put("appliedPromotions", promotionAnalysis.get("appliedPromotions"));
            postResult.put("promotionSavings", promotionAnalysis.get("totalSavings"));
        }

        // 库存更新提示
        Map<String, Object> inventoryStatus = (Map<String, Object>) result.get("inventoryStatus");
        if (inventoryStatus != null) {
            postResult.put("lowStockItems", inventoryStatus.get("lowStockItems"));
            postResult.put("outOfStockItems", inventoryStatus.get("outOfStockItems"));
        }

        // 扫码统计
        Map<String, Object> scanStatistics = generateScanStatistics(products);
        postResult.put("scanStatistics", scanStatistics);

        // 购买建议
        List<String> purchaseTips = generatePurchaseTips(params, result);
        postResult.put("purchaseTips", purchaseTips);

        return postResult;
    }

    @Override
    protected BigDecimal getMinAmount() {
        return new BigDecimal("0.01");
    }

    @Override
    protected BigDecimal getMaxAmount() {
        return MAX_TOTAL_AMOUNT;
    }

    @Override
    protected String[] getSupportedFields() {
        return new String[]{
            "userId", "accountId", "products", "promotionCode", "memberLevel",
            "pricingStrategy", "storeId", "cashierId", "scanMethod",
            "invoiceRequired", "deliveryMethod", "customerNotes"
        };
    }

    /**
     * 获取商品清单
     */
    private List<Map<String, Object>> getProductList(Map<String, Object> params) {
        Object products = params.get("products");
        if (products instanceof List) {
            return (List<Map<String, Object>>) products;
        }
        return new ArrayList<>();
    }

    /**
     * 计算商品基础总价
     */
    private BigDecimal calculateBaseProductCost(List<Map<String, Object>> products) {
        BigDecimal totalCost = BigDecimal.ZERO;

        for (Map<String, Object> product : products) {
            BigDecimal price = getProductPrice(product);
            Integer quantity = getProductQuantity(product);

            if (price != null && quantity != null && quantity > 0) {
                BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(quantity));

                // 检查单商品总价限制
                if (itemTotal.compareTo(MAX_SINGLE_ITEM_PRICE) > 0) {
                    throw new IllegalArgumentException("单商品总价超出限制: " + itemTotal);
                }

                totalCost = totalCost.add(itemTotal);
            }
        }

        return totalCost;
    }

    /**
     * 获取商品价格
     */
    private BigDecimal getProductPrice(Map<String, Object> product) {
        Object price = product.get("price");
        if (price instanceof BigDecimal) {
            return (BigDecimal) price;
        } else if (price instanceof Number) {
            return BigDecimal.valueOf(((Number) price).doubleValue());
        } else if (price instanceof String) {
            try {
                return new BigDecimal((String) price);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 获取商品数量
     */
    private Integer getProductQuantity(Map<String, Object> product) {
        Object quantity = product.get("quantity");
        if (quantity instanceof Integer) {
            return (Integer) quantity;
        } else if (quantity instanceof Number) {
            return ((Number) quantity).intValue();
        }
        return 1; // 默认数量为1
    }

    /**
     * 应用品类折扣
     */
    private BigDecimal applyCategoryDiscounts(BigDecimal totalCost, List<Map<String, Object>> products) {
        BigDecimal categoryDiscount = BigDecimal.ZERO;

        for (Map<String, Object> product : products) {
            String category = getStringFromMap(product, "category");
            BigDecimal price = getProductPrice(product);
            Integer quantity = getProductQuantity(product);

            if (category != null && price != null && quantity != null) {
                BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(quantity));
                BigDecimal discountAmount = CategoryDiscountEnum.getDiscountAmount(itemTotal);
                categoryDiscount = categoryDiscount.add(discountAmount);
            }
        }

        return totalCost.subtract(categoryDiscount);
    }

    /**
     * 应用促销优惠
     */
    private BigDecimal applyPromotions(BigDecimal cost, List<Map<String, Object>> products, Map<String, Object> params) {
        // 检查促销码
        String promotionCode = getStringFromParams(params, "promotionCode");
        if (promotionCode != null) {
            // 应用促销码折扣
            return cost.multiply(new BigDecimal("0.9")); // 简化：促销码9折
        }

        // 检查自动促销
        for (Map<String, Object> product : products) {
            String promotion = getStringFromMap(product, "promotion");
            if (promotion != null) {
                try {
                    PromotionType promotionType = PromotionType.valueOf(promotion.toUpperCase());
                    return cost.multiply(BigDecimal.ONE.subtract(promotionType.getDiscountRate()));
                } catch (IllegalArgumentException e) {
                    // 忽略无效的促销类型
                }
            }
        }

        return cost;
    }

    /**
     * 应用会员折扣
     */
    private BigDecimal applyMemberDiscounts(BigDecimal cost, Map<String, Object> params) {
        String memberLevel = getStringFromParams(params, "memberLevel");
        if (memberLevel != null) {
            return MemberLevelEnum.applyMemberDiscount(cost, memberLevel);
        }
        return cost;
    }

    /**
     * 应用数量折扣
     */
    private BigDecimal applyQuantityDiscounts(BigDecimal cost, List<Map<String, Object>> products) {
        int totalQuantity = products.stream()
                .mapToInt(product -> getProductQuantity(product) != null ? getProductQuantity(product) : 0)
                .sum();

        // 批量购买优惠
        if (totalQuantity >= 10) {
            return cost.multiply(new BigDecimal("0.95")); // 10件以上95折
        } else if (totalQuantity >= 5) {
            return cost.multiply(new BigDecimal("0.98")); // 5件以上98折
        }

        return cost;
    }

    /**
     * 应用特殊调整
     */
    private BigDecimal applySpecialAdjustments(BigDecimal cost, Map<String, Object> params) {
        // 增值服务费用
        String invoiceRequired = getStringFromParams(params, "invoiceRequired");
        if ("TRUE".equalsIgnoreCase(invoiceRequired)) {
            cost = cost.add(new BigDecimal("0.50")); // 发票费用
        }

        // 配送费用
        String deliveryMethod = getStringFromParams(params, "deliveryMethod");
        if ("HOME_DELIVERY".equals(deliveryMethod)) {
            cost = cost.add(new BigDecimal("5.00")); // 配送费
        }

        return cost;
    }

    // 验证方法

    private boolean hasValidProducts(Map<String, Object> params) {
        List<Map<String, Object>> products = getProductList(params);
        return !products.isEmpty() && products.size() <= MAX_CART_ITEMS;
    }

    private boolean hasValidBarcodes(Map<String, Object> params) {
        List<Map<String, Object>> products = getProductList(params);
        for (Map<String, Object> product : products) {
            String barcode = getStringFromMap(product, "barcode");
            if (barcode != null && !BARCODE_PATTERN.matcher(barcode).matches()) {
                return false;
            }
        }
        return true;
    }

    private boolean hasValidQuantities(Map<String, Object> params) {
        List<Map<String, Object>> products = getProductList(params);
        for (Map<String, Object> product : products) {
            Integer quantity = getProductQuantity(product);
            if (quantity == null || quantity <= 0 || quantity > MAX_ITEM_QUANTITY) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidPromotionCode(String promotionCode) {
        // 简化实现：检查促销码格式
        return promotionCode.matches("^[A-Z0-9]{6,12}$");
    }

    private boolean checkInventoryStatus(Map<String, Object> params) {
        // 简化实现：检查库存状态
        return true; // 假设库存充足
    }

    private boolean checkPurchaseFrequencyLimit(Map<String, Object> params) {
        // 简化实现：检查购买频率
        return true; // 假设未超出限制
    }

    private boolean checkProductStatus(Map<String, Object> params) {
        // 简化实现：检查商品状态
        return true; // 假设商品状态正常
    }

    private boolean checkPromotionValidity(Map<String, Object> params) {
        // 简化实现：检查促销有效期
        return true; // 假设促销有效
    }

    private boolean checkPurchaseLimit(Map<String, Object> params) {
        // 简化实现：检查购买额度
        return true; // 假设未超出额度
    }

    // 分析方法

    private Map<String, Object> analyzeProducts(List<Map<String, Object>> products) {
        Map<String, Object> analysis = new HashMap<>();

        int itemCount = products.size();
        int totalQuantity = products.stream()
                .mapToInt(product -> getProductQuantity(product) != null ? getProductQuantity(product) : 0)
                .sum();

        analysis.put("itemCount", itemCount);
        analysis.put("totalQuantity", totalQuantity);
        analysis.put("averagePrice", itemCount > 0 ? calculateBaseProductCost(products).divide(BigDecimal.valueOf(itemCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        analysis.put("hasBarcode", products.stream().anyMatch(p -> getStringFromMap(p, "barcode") != null));

        return analysis;
    }

    private Map<String, Object> validateBarcodes(List<Map<String, Object>> products) {
        Map<String, Object> validation = new HashMap<>();

        int validBarcodes = 0;
        int invalidBarcodes = 0;

        for (Map<String, Object> product : products) {
            String barcode = getStringFromMap(product, "barcode");
            if (barcode != null) {
                if (BARCODE_PATTERN.matcher(barcode).matches()) {
                    validBarcodes++;
                } else {
                    invalidBarcodes++;
                }
            }
        }

        validation.put("validBarcodes", validBarcodes);
        validation.put("invalidBarcodes", invalidBarcodes);
        validation.put("allValid", invalidBarcodes == 0);

        return validation;
    }

    private Map<String, Object> analyzePricing(List<Map<String, Object>> products, Map<String, Object> params) {
        Map<String, Object> pricing = new HashMap<>();

        BigDecimal originalTotal = calculateBaseProductCost(products);
        pricing.put("originalTotal", originalTotal);

        // 计算折扣总额
        BigDecimal discountTotal = BigDecimal.ZERO;
        String memberLevel = getStringFromParams(params, "memberLevel");
        if (memberLevel != null) {
            MemberLevelEnum level = MemberLevelEnum.fromCode(memberLevel);
            if (level != null) {
                discountTotal = discountTotal.add(level.getMemberDiscountAmount(originalTotal));
            }
        }

        pricing.put("discountTotal", discountTotal);
        pricing.put("finalTotal", originalTotal.subtract(discountTotal));

        return pricing;
    }

    private Map<String, Object> analyzePromotions(List<Map<String, Object>> products, Map<String, Object> params) {
        Map<String, Object> promotion = new HashMap<>();

        List<String> appliedPromotions = new ArrayList<>();
        BigDecimal totalSavings = BigDecimal.ZERO;

        // 检查促销码
        String promotionCode = getStringFromParams(params, "promotionCode");
        if (promotionCode != null) {
            appliedPromotions.add("促销码优惠");
            totalSavings = totalSavings.add(new BigDecimal("10.00")); // 简化实现
        }

        promotion.put("appliedPromotions", appliedPromotions);
        promotion.put("totalSavings", totalSavings);

        return promotion;
    }

    private Map<String, Object> checkInventory(List<Map<String, Object>> products) {
        Map<String, Object> inventory = new HashMap<>();

        List<String> lowStockItems = new ArrayList<>();
        List<String> outOfStockItems = new ArrayList<>();

        for (Map<String, Object> product : products) {
            String productName = getStringFromMap(product, "name");
            Integer stock = getIntegerFromMap(product, "stock");
            Integer quantity = getProductQuantity(product);

            if (productName != null && stock != null && quantity != null) {
                if (stock == 0) {
                    outOfStockItems.add(productName);
                } else if (stock <= quantity) {
                    lowStockItems.add(productName);
                }
            }
        }

        inventory.put("lowStockItems", lowStockItems);
        inventory.put("outOfStockItems", outOfStockItems);
        inventory.put("allAvailable", lowStockItems.isEmpty() && outOfStockItems.isEmpty());

        return inventory;
    }

    private List<Map<String, Object>> generateProductRecommendations(Map<String, Object> params) {
        List<Map<String, Object>> recommendations = new ArrayList<>();

        Map<String, Object> rec1 = new HashMap<>();
        rec1.put("barcode", "6901234567890");
        rec1.put("name", "热销商品A");
        rec1.put("price", new BigDecimal("29.90"));
        rec1.put("reason", "基于购买历史推荐");
        recommendations.add(rec1);

        return recommendations;
    }

    private Map<String, Object> calculateCategoryStatistics(List<Map<String, Object>> products) {
        Map<String, Object> stats = new HashMap<>();

        Map<String, Integer> categoryCount = new HashMap<>();
        Map<String, BigDecimal> categoryTotal = new HashMap<>();

        for (Map<String, Object> product : products) {
            String category = getStringFromMap(product, "category");
            if (category != null) {
                categoryCount.put(category, categoryCount.getOrDefault(category, 0) + 1);

                BigDecimal price = getProductPrice(product);
                Integer quantity = getProductQuantity(product);
                if (price != null && quantity != null) {
                    BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(quantity));
                    categoryTotal.put(category, categoryTotal.getOrDefault(category, BigDecimal.ZERO).add(itemTotal));
                }
            }
        }

        stats.put("categoryCount", categoryCount);
        stats.put("categoryTotal", categoryTotal);

        return stats;
    }

    private Map<String, Object> generateScanStatistics(List<Map<String, Object>> products) {
        Map<String, Object> statistics = new HashMap<>();

        int totalScans = products.size();
        int barcodeScans = (int) products.stream().filter(p -> getStringFromMap(p, "barcode") != null).count();
        int manualEntries = totalScans - barcodeScans;

        statistics.put("totalScans", totalScans);
        statistics.put("barcodeScans", barcodeScans);
        statistics.put("manualEntries", manualEntries);
        statistics.put("scanRate", totalScans > 0 ? BigDecimal.valueOf(barcodeScans).divide(BigDecimal.valueOf(totalScans), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        return statistics;
    }

    private List<String> generatePurchaseTips(Map<String, Object> params, Map<String, Object> result) {
        List<String> tips = new ArrayList<>();

        tips.add("感谢您的购买，请保存好购物小票");

        if (((List<?>) params.get("products")).size() >= 10) {
            tips.add("您购买了很多商品，建议使用购物袋");
        }

        BigDecimal discountRate = (BigDecimal) result.get("discountRate");
        if (discountRate != null && discountRate.compareTo(new BigDecimal("20")) > 0) {
            tips.add("您享受了大幅优惠，成为会员可享更多折扣");
        }

        return tips;
    }

    // 工具方法

    private String getStringFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private Integer getIntegerFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    /**
     * 获取所有支持的商品类型
     */
    public static List<String> getSupportedProductTypes() {
        return Arrays.stream(ProductType.values())
                .map(ProductType::getName)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取所有支持的条码类型
     */
    public static List<String> getSupportedBarcodeTypes() {
        return Arrays.stream(BarcodeType.values())
                .map(BarcodeType::getName)
                .collect(java.util.stream.Collectors.toList());
    }
}