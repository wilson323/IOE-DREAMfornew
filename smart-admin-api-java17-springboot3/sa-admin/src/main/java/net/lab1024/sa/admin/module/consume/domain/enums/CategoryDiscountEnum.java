package net.lab1024.sa.admin.module.consume.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 商品分类折扣枚举
 * 严格遵循repowiki规范：定义不同商品分类的折扣策略
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Getter
@AllArgsConstructor
public enum CategoryDiscountEnum {

    /**
     * 餐饮类 - 9折
     */
    FOOD_BEVERAGE("FOOD_BEVERAGE", "餐饮类", new BigDecimal("0.90"), "餐厅、咖啡、零食等"),

    /**
     * 文具类 - 8折
     */
    STATIONERY("STATIONERY", "文具类", new BigDecimal("0.80"), "笔、本子、文具等"),

    /**
     * 图书类 - 7.5折
     */
    BOOKS("BOOKS", "图书类", new BigDecimal("0.75"), "教材、小说、杂志等"),

    /**
     * 日用品 - 9.5折
     */
    DAILY_NECESSITIES("DAILY_NECESSITIES", "日用品", new BigDecimal("0.95"), "洗护用品、清洁用品等"),

    /**
     * 电子产品 - 无折扣
     */
    ELECTRONICS("ELECTRONICS", "电子产品", BigDecimal.ONE, "手机、电脑、配件等"),

    /**
     * 服装类 - 8.5折
     */
    CLOTHING("CLOTHING", "服装类", new BigDecimal("0.85"), "服装、鞋帽、配饰等"),

    /**
     * 运动健身 - 8折
     */
    SPORTS_FITNESS("SPORTS_FITNESS", "运动健身", new BigDecimal("0.80"), "体育用品、健身器材等"),

    /**
     * 其他 - 无折扣
     */
    OTHER("OTHER", "其他", BigDecimal.ONE, "其他未分类商品");

    private final String code;
    private final String name;
    private final BigDecimal discountRate;
    private final String description;

    /**
     * 获取折扣后的价格
     */
    public BigDecimal getDiscountedPrice(BigDecimal originalPrice) {
        if (originalPrice == null) {
            return null;
        }
        return originalPrice.multiply(discountRate).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 计算折扣金额
     */
    public BigDecimal getDiscountAmount(BigDecimal originalPrice) {
        if (originalPrice == null) {
            return null;
        }
        return originalPrice.subtract(getDiscountedPrice(originalPrice));
    }

    /**
     * 检查是否有折扣
     */
    public boolean hasDiscount() {
        return discountRate.compareTo(BigDecimal.ONE) < 0;
    }

    /**
     * 获取折扣百分比字符串
     */
    public String getDiscountPercentage() {
        if (!hasDiscount()) {
            return "无折扣";
        }
        BigDecimal percentage = BigDecimal.ONE.subtract(discountRate).multiply(new BigDecimal("100"));
        return percentage.setScale(0, BigDecimal.ROUND_HALF_UP) + "%";
    }

    /**
     * 根据代码获取分类
     */
    public static CategoryDiscountEnum fromCode(String code) {
        if (code == null) {
            return null;
        }

        for (CategoryDiscountEnum category : values()) {
            if (category.getCode().equals(code)) {
                return category;
            }
        }

        return null;
    }

    /**
     * 检查代码是否有效
     */
    public static boolean isValidCode(String code) {
        return fromCode(code) != null;
    }

    /**
     * 获取所有有折扣的分类
     */
    public static List<CategoryDiscountEnum> getDiscountedCategories() {
        return Arrays.stream(values())
                .filter(CategoryDiscountEnum::hasDiscount)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有分类代码
     */
    public static List<String> getAllCategoryCodes() {
        return Arrays.stream(values())
                .map(CategoryDiscountEnum::getCode)
                .collect(Collectors.toList());
    }

    /**
     * 获取分类代码到折扣率的映射
     */
    public static Map<String, BigDecimal> getDiscountRateMap() {
        return Arrays.stream(values())
                .collect(Collectors.toMap(
                        CategoryDiscountEnum::getCode,
                        CategoryDiscountEnum::getDiscountRate
                ));
    }

    /**
     * 检查分类是否支持折扣
     */
    public static boolean supportsDiscount(String categoryCode) {
        CategoryDiscountEnum category = fromCode(categoryCode);
        return category != null && category.hasDiscount();
    }

    /**
     * 获取分类的折扣率
     */
    public static BigDecimal getDiscountRate(String categoryCode) {
        CategoryDiscountEnum category = fromCode(categoryCode);
        return category != null ? category.getDiscountRate() : BigDecimal.ONE;
    }

    /**
     * 应用分类折扣到价格
     */
    public static BigDecimal applyCategoryDiscount(BigDecimal originalPrice, String categoryCode) {
        BigDecimal discountRate = getDiscountRate(categoryCode);
        return originalPrice.multiply(discountRate).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 应用多重折扣（会员折扣 + 分类折扣）
     * 注意：多重折扣通常不是简单相乘，而是取最大折扣或特定规则
     */
    public static BigDecimal applyMultipleDiscounts(BigDecimal originalPrice, String categoryCode, BigDecimal memberDiscountRate) {
        BigDecimal categoryDiscountRate = getDiscountRate(categoryCode);

        // 这里采用优惠力度更大的折扣
        BigDecimal finalDiscountRate = categoryDiscountRate.compareTo(memberDiscountRate) <= 0
                ? categoryDiscountRate
                : memberDiscountRate;

        return originalPrice.multiply(finalDiscountRate).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 获取分类描述信息
     */
    public static String getCategoryDescription(String categoryCode) {
        CategoryDiscountEnum category = fromCode(categoryCode);
        return category != null ? category.getDescription() : "未知分类";
    }

    /**
     * 获取分类的完整信息
     */
    public Map<String, Object> getCategoryInfo() {
        return Map.of(
                "code", code,
                "name", name,
                "description", description,
                "discountRate", discountRate,
                "discountPercentage", getDiscountPercentage(),
                "hasDiscount", hasDiscount()
        );
    }
}