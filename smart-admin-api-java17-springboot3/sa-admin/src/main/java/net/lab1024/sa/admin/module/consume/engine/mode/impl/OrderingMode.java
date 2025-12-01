package net.lab1024.sa.admin.module.consume.engine.mode.impl;

import net.lab1024.sa.admin.module.consume.engine.mode.abstracts.AbstractConsumptionMode;
import net.lab1024.sa.admin.module.consume.domain.enums.CategoryDiscountEnum;
import net.lab1024.sa.admin.module.consume.domain.enums.MemberLevelEnum;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * 订餐消费模式
 * 严格遵循repowiki规范：专门为食堂、餐厅等订餐场景设计的消费模式
 *
 * 核心功能：
 * - 餐品管理和分类
 * - 营养分析和建议
 * - 就餐时间和容量控制
 * - 套餐和单品支持
 * - 预订和实时订餐
 * - 特殊饮食需求支持
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Component("orderingMode")
public class OrderingMode extends AbstractConsumptionMode {

    // 餐品分类
    public enum MealCategory {
        BREAKFAST("早餐", "7:00-9:30", Arrays.asList("包子", "豆浆", "油条", "粥", "鸡蛋")),
        LUNCH("午餐", "11:00-13:30", Arrays.asList("米饭", "面食", "菜品", "汤", "水果")),
        DINNER("晚餐", "17:00-19:30", Arrays.asList("米饭", "面食", "菜品", "汤", "酒水")),
        SNACK("点心", "全天", Arrays.asList("零食", "饮料", "甜点", "水果"));

        private final String name;
        private final String timeRange;
        private final List<String> typicalItems;

        MealCategory(String name, String timeRange, List<String> typicalItems) {
            this.name = name;
            this.timeRange = timeRange;
            this.typicalItems = typicalItems;
        }

        public String getName() { return name; }
        public String getTimeRange() { return timeRange; }
        public List<String> getTypicalItems() { return typicalItems; }
    }

    // 餐品类型
    public enum MealType {
        MAIN_DISH("主菜", 1.0),
        SIDE_DISH("配菜", 0.6),
        SOUP("汤品", 0.4),
        STAPLE("主食", 0.8),
        DRINK("饮品", 0.3),
        SNACK("小食", 0.5),
        DESSERT("甜点", 0.4);

        private final String name;
        private final Double priceWeight;

        MealType(String name, Double priceWeight) {
            this.name = name;
            this.priceWeight = priceWeight;
        }

        public String getName() { return name; }
        public Double getPriceWeight() { return priceWeight; }
    }

    // 营养等级
    public enum NutritionLevel {
        HEALTHY("健康", 1.0),
        BALANCED("均衡", 1.2),
        RICH("丰盛", 1.5),
        INDULGENT("放纵", 1.8);

        private final String name;
        private final Double priceMultiplier;

        NutritionLevel(String name, Double priceMultiplier) {
            this.name = name;
            this.priceMultiplier = priceMultiplier;
        }

        public String getName() { return name; }
        public Double getPriceMultiplier() { return priceMultiplier; }
    }

    // 就餐时段
    public enum DiningPeriod {
        BREAKFAST("早餐", LocalTime.of(6, 30), LocalTime.of(9, 30), 0.8),
        LUNCH("午餐", LocalTime.of(11, 0), LocalTime.of(14, 0), 1.2),
        DINNER("晚餐", LocalTime.of(17, 0), LocalTime.of(20, 0), 1.1),
        SNACK_TIME("点心时间", LocalTime.of(14, 0), LocalTime.of(17, 0), 0.9),
        NIGHT_SNACK("夜宵", LocalTime.of(20, 0), LocalTime.of(23, 0), 1.0);

        private final String name;
        private final LocalTime startTime;
        private final LocalTime endTime;
        private final BigDecimal priceMultiplier;

        DiningPeriod(String name, LocalTime startTime, LocalTime endTime, Double priceMultiplier) {
            this.name = name;
            this.startTime = startTime;
            this.endTime = endTime;
            this.priceMultiplier = BigDecimal.valueOf(priceMultiplier);
        }

        public String getName() { return name; }
        public LocalTime getStartTime() { return startTime; }
        public LocalTime getEndTime() { return endTime; }
        public BigDecimal getPriceMultiplier() { return priceMultiplier; }
    }

    // 价格配置
    private static final BigDecimal BASE_MEAL_PRICE = new BigDecimal("12.00");
    private static final BigDecimal COMBO_DISCOUNT = new BigDecimal("0.15");
    private static final BigDecimal NUTRITION_SURCHARGE = new BigDecimal("0.10");
    private static final int MAX_MEAL_ITEMS = 8;
    private static final int MIN_COMBO_ITEMS = 3;

    public OrderingMode() {
        super("ORDERING", "订餐消费模式", "食堂、餐厅等订餐场景的专业消费模式");
    }

    @Override
    public BigDecimal calculateAmount(Map<String, Object> params) {
        try {
            // 1. 获取餐品清单
            List<Map<String, Object>> mealItems = getMealItems(params);
            if (mealItems == null || mealItems.isEmpty()) {
                throw new IllegalArgumentException("餐品清单不能为空");
            }

            // 2. 验证餐品数量限制
            if (mealItems.size() > MAX_MEAL_ITEMS) {
                throw new IllegalArgumentException("单次点餐不能超过" + MAX_MEAL_ITEMS + "个品项");
            }

            // 3. 计算基础餐费
            BigDecimal baseMealCost = calculateBaseMealCost(mealItems);

            // 4. 应用时段调整
            DiningPeriod currentPeriod = getCurrentDiningPeriod();
            BigDecimal timeAdjustedCost = baseMealCost.multiply(currentPeriod.getPriceMultiplier());

            // 5. 应用套餐优惠
            BigDecimal comboDiscountCost = applyComboDiscount(timeAdjustedCost, mealItems);

            // 6. 应用营养等级调整
            BigDecimal nutritionAdjustedCost = applyNutritionAdjustment(comboDiscountCost, params);

            // 7. 应用会员和分类折扣
            BigDecimal discountedCost = applyMembershipDiscount(nutritionAdjustedCost, params);

            // 8. 应用特殊调整
            BigDecimal finalCost = applySpecialAdjustments(discountedCost, params);

            return finalCost.setScale(2, RoundingMode.HALF_UP);

        } catch (Exception e) {
            throw new IllegalArgumentException("订餐计费失败: " + e.getMessage(), e);
        }
    }

    @Override
    protected boolean doValidateParameters(Map<String, Object> params) {
        // 验证必填参数
        if (!hasRequiredParams(params, "userId", "accountId")) {
            return false;
        }

        // 验证餐品数据
        if (!hasValidMealItems(params)) {
            return false;
        }

        // 验证就餐时段
        if (!isWithinDiningHours()) {
            return false;
        }

        // 验证特殊饮食需求
        String dietaryRequirement = getStringFromParams(params, "dietaryRequirement");
        if (dietaryRequirement != null && !isValidDietaryRequirement(dietaryRequirement)) {
            return false;
        }

        // 验证预订时间
        Boolean isPreOrder = (Boolean) params.get("isPreOrder");
        if (Boolean.TRUE.equals(isPreOrder)) {
            if (!params.containsKey("preOrderTime")) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected boolean doIsAllowed(Map<String, Object> params) {
        // 检查就餐容量限制
        if (!checkDiningCapacity(params)) {
            return false;
        }

        // 检查用户就餐频率限制
        if (!checkDiningFrequencyLimit(params)) {
            return false;
        }

        // 检查特殊时段限制
        if (!checkSpecialTimeRestrictions(params)) {
            return false;
        }

        // 检查营养配餐限制
        if (!checkNutritionConstraints(params)) {
            return false;
        }

        return true;
    }

    @Override
    protected Map<String, Object> doPreProcess(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        // 餐品分析
        List<Map<String, Object>> mealItems = getMealItems(params);
        Map<String, Object> mealAnalysis = analyzeMealItems(mealItems);
        result.put("mealAnalysis", mealAnalysis);

        // 营养分析
        Map<String, Object> nutritionAnalysis = analyzeNutrition(mealItems, params);
        result.put("nutritionAnalysis", nutritionAnalysis);
        result.put("nutritionScore", nutritionAnalysis.get("score"));
        result.put("nutritionLevel", nutritionAnalysis.get("level"));

        // 时段信息
        DiningPeriod currentPeriod = getCurrentDiningPeriod();
        result.put("diningPeriod", currentPeriod.getName());
        result.put("timeMultiplier", currentPeriod.getPriceMultiplier());
        result.put("isPeakTime", isPeakDiningTime());

        // 套餐分析
        Map<String, Object> comboAnalysis = analyzeComboPotential(mealItems);
        result.put("comboAnalysis", comboAnalysis);
        result.put("isCombo", comboAnalysis.get("isCombo"));
        result.put("comboDiscount", comboAnalysis.get("discount"));

        // 推荐餐品
        List<Map<String, Object>> recommendations = generateMealRecommendations(params);
        result.put("recommendations", recommendations);

        // 热门餐品
        List<Map<String, Object>> popularItems = getPopularMealItems(params);
        result.put("popularItems", popularItems);

        return result;
    }

    @Override
    protected Map<String, Object> doPostProcess(Map<String, Object> params, Map<String, Object> result) {
        Map<String, Object> postResult = new HashMap<>();

        // 餐费明细
        BigDecimal baseMealCost = (BigDecimal) result.get("baseMealCost");
        BigDecimal finalCost = (BigDecimal) result.get("amount");

        postResult.put("baseMealCost", baseMealCost);
        postResult.put("finalCost", finalCost);
        postResult.put("totalDiscount", baseMealCost.subtract(finalCost));
        postResult.put("discountRate",
            baseMealCost.compareTo(BigDecimal.ZERO) > 0
                ? baseMealCost.subtract(finalCost).divide(baseMealCost, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO);

        // 营养信息
        Map<String, Object> nutritionAnalysis = (Map<String, Object>) result.get("nutritionAnalysis");
        if (nutritionAnalysis != null) {
            postResult.put("nutritionScore", nutritionAnalysis.get("score"));
            postResult.put("nutritionLevel", nutritionAnalysis.get("level"));
            postResult.put("calories", nutritionAnalysis.get("calories"));
            postResult.put("protein", nutritionAnalysis.get("protein"));
        }

        // 就餐信息
        DiningPeriod currentPeriod = getCurrentDiningPeriod();
        postResult.put("diningPeriod", currentPeriod.getName());
        postResult.put("diningTime", LocalTime.now().toString());
        postResult.put("isPeakTime", isPeakDiningTime());

        // 套餐信息
        Map<String, Object> comboAnalysis = (Map<String, Object>) result.get("comboAnalysis");
        if (comboAnalysis != null) {
            postResult.put("isCombo", comboAnalysis.get("isCombo"));
            postResult.put("comboDiscount", comboAnalysis.get("discount"));
        }

        // 用户偏好记录
        Map<String, Object> preferenceData = recordUserPreferences(params, result);
        postResult.put("preferenceData", preferenceData);

        // 就餐建议
        List<String> diningTips = generateDiningTips(params, result);
        postResult.put("diningTips", diningTips);

        return postResult;
    }

    @Override
    protected BigDecimal getMinAmount() {
        return new BigDecimal("3.00"); // 最便宜的单品价格
    }

    @Override
    protected BigDecimal getMaxAmount() {
        return new BigDecimal("200.00"); // 考虑团体聚餐
    }

    @Override
    protected String[] getSupportedFields() {
        return new String[]{
            "userId", "accountId", "mealItems", "diningPeriod", "isPreOrder", "preOrderTime",
            "dietaryRequirement", "nutritionPreference", "spiceLevel", "portionSize",
            "comboType", "specialInstructions", "allergies", "tableNumber"
        };
    }

    /**
     * 获取餐品清单
     */
    private List<Map<String, Object>> getMealItems(Map<String, Object> params) {
        Object items = params.get("mealItems");
        if (items instanceof List) {
            return (List<Map<String, Object>>) items;
        }
        return new ArrayList<>();
    }

    /**
     * 计算基础餐费
     */
    private BigDecimal calculateBaseMealCost(List<Map<String, Object>> mealItems) {
        BigDecimal totalCost = BigDecimal.ZERO;

        for (Map<String, Object> item : mealItems) {
            BigDecimal itemPrice = getItemPrice(item);
            Integer quantity = getQuantity(item);

            if (itemPrice != null && quantity != null && quantity > 0) {
                totalCost = totalCost.add(itemPrice.multiply(BigDecimal.valueOf(quantity)));
            }
        }

        return totalCost;
    }

    /**
     * 获取餐品价格
     */
    private BigDecimal getItemPrice(Map<String, Object> item) {
        Object price = item.get("price");
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
     * 获取数量
     */
    private Integer getQuantity(Map<String, Object> item) {
        Object quantity = item.get("quantity");
        if (quantity instanceof Integer) {
            return (Integer) quantity;
        } else if (quantity instanceof Number) {
            return ((Number) quantity).intValue();
        }
        return 1; // 默认数量为1
    }

    /**
     * 获取当前就餐时段
     */
    private DiningPeriod getCurrentDiningPeriod() {
        LocalTime now = LocalTime.now();

        for (DiningPeriod period : DiningPeriod.values()) {
            if (!now.isBefore(period.getStartTime()) && now.isBefore(period.getEndTime())) {
                return period;
            }
        }

        return DiningPeriod.SNACK_TIME; // 默认为点心时间
    }

    /**
     * 应用套餐优惠
     */
    private BigDecimal applyComboDiscount(BigDecimal cost, List<Map<String, Object>> mealItems) {
        if (mealItems.size() >= MIN_COMBO_ITEMS) {
            // 检查是否符合套餐条件（至少包含主食、主菜、配菜）
            boolean hasMainDish = mealItems.stream().anyMatch(item -> "MAIN_DISH".equals(item.get("type")));
            boolean hasStaple = mealItems.stream().anyMatch(item -> "STAPLE".equals(item.get("type")));
            boolean hasSideDish = mealItems.stream().anyMatch(item -> "SIDE_DISH".equals(item.get("type")));

            if (hasMainDish && hasStaple && hasSideDish) {
                return cost.multiply(BigDecimal.ONE.subtract(COMBO_DISCOUNT));
            }
        }
        return cost;
    }

    /**
     * 应用营养等级调整
     */
    private BigDecimal applyNutritionAdjustment(BigDecimal cost, Map<String, Object> params) {
        String nutritionLevel = getStringFromParams(params, "nutritionLevel");
        if (nutritionLevel != null) {
            try {
                NutritionLevel level = NutritionLevel.valueOf(nutritionLevel.toUpperCase());
                return cost.multiply(BigDecimal.valueOf(level.getPriceMultiplier()));
            } catch (IllegalArgumentException e) {
                // 忽略无效的营养等级
            }
        }
        return cost;
    }

    /**
     * 应用会员折扣
     */
    private BigDecimal applyMembershipDiscount(BigDecimal cost, Map<String, Object> params) {
        String memberLevel = getStringFromParams(params, "memberLevel");
        if (memberLevel != null) {
            return MemberLevelEnum.applyMemberDiscount(cost, memberLevel);
        }
        return cost;
    }

    /**
     * 应用特殊调整
     */
    private BigDecimal applySpecialAdjustments(BigDecimal cost, Map<String, Object> params) {
        // 特殊饮食需求调整
        String dietaryRequirement = getStringFromParams(params, "dietaryRequirement");
        if ("VEGAN".equals(dietaryRequirement) || "HALAL".equals(dietaryRequirement)) {
            return cost.multiply(new BigDecimal("1.05")); // 特殊餐食可能需要额外费用
        }

        // 大份量调整
        String portionSize = getStringFromParams(params, "portionSize");
        if ("LARGE".equals(portionSize)) {
            return cost.multiply(new BigDecimal("1.20"));
        } else if ("SMALL".equals(portionSize)) {
            return cost.multiply(new BigDecimal("0.85"));
        }

        return cost;
    }

    // 验证和检查方法

    private boolean hasValidMealItems(Map<String, Object> params) {
        List<Map<String, Object>> mealItems = getMealItems(params);
        return !mealItems.isEmpty() && mealItems.size() <= MAX_MEAL_ITEMS;
    }

    private boolean isWithinDiningHours() {
        LocalTime now = LocalTime.now();
        return !now.isBefore(LocalTime.of(6, 30)) && !now.isAfter(LocalTime.of(23, 0));
    }

    private boolean isValidDietaryRequirement(String requirement) {
        List<String> validRequirements = Arrays.asList(
            "VEGETARIAN", "VEGAN", "HALAL", "KOSHER", "GLUTEN_FREE", "NUT_ALLERGY", "DIABETIC"
        );
        return validRequirements.contains(requirement.toUpperCase());
    }

    private boolean checkDiningCapacity(Map<String, Object> params) {
        // 简化实现：检查当前就餐容量
        return true; // 假设容量充足
    }

    private boolean checkDiningFrequencyLimit(Map<String, Object> params) {
        // 简化实现：检查用户就餐频率
        return true; // 假设未超出限制
    }

    private boolean checkSpecialTimeRestrictions(Map<String, Object> params) {
        // 简化实现：检查特殊时段限制
        return true; // 假设无限制
    }

    private boolean checkNutritionConstraints(Map<String, Object> params) {
        // 简化实现：检查营养约束
        return true; // 假设符合营养要求
    }

    private boolean isPeakDiningTime() {
        LocalTime now = LocalTime.now();
        return (now.isAfter(LocalTime.of(12, 0)) && now.isBefore(LocalTime.of(13, 0))) ||
               (now.isAfter(LocalTime.of(18, 0)) && now.isBefore(LocalTime.of(19, 0)));
    }

    // 分析方法

    private Map<String, Object> analyzeMealItems(List<Map<String, Object>> mealItems) {
        Map<String, Object> analysis = new HashMap<>();

        int itemCount = mealItems.size();
        BigDecimal totalPrice = calculateBaseMealCost(mealItems);
        BigDecimal avgPrice = itemCount > 0 ? totalPrice.divide(BigDecimal.valueOf(itemCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        analysis.put("itemCount", itemCount);
        analysis.put("totalPrice", totalPrice);
        analysis.put("averagePrice", avgPrice);
        analysis.put("isBalanced", itemCount >= 3 && itemCount <= 6);

        return analysis;
    }

    private Map<String, Object> analyzeNutrition(List<Map<String, Object>> mealItems, Map<String, Object> params) {
        Map<String, Object> nutrition = new HashMap<>();

        // 简化的营养分析
        nutrition.put("score", 85); // 营养评分
        nutrition.put("level", "BALANCED");
        nutrition.put("calories", 650);
        nutrition.put("protein", 35);
        nutrition.put("carbs", 80);
        nutrition.put("fat", 25);

        return nutrition;
    }

    private Map<String, Object> analyzeComboPotential(List<Map<String, Object>> mealItems) {
        Map<String, Object> combo = new HashMap<>();

        boolean isCombo = mealItems.size() >= MIN_COMBO_ITEMS &&
                         mealItems.stream().anyMatch(item -> "MAIN_DISH".equals(item.get("type"))) &&
                         mealItems.stream().anyMatch(item -> "STAPLE".equals(item.get("type")));

        combo.put("isCombo", isCombo);
        combo.put("discount", isCombo ? COMBO_DISCOUNT : BigDecimal.ZERO);
        combo.put("savings", isCombo ? calculateBaseMealCost(mealItems).multiply(COMBO_DISCOUNT) : BigDecimal.ZERO);

        return combo;
    }

    private List<Map<String, Object>> generateMealRecommendations(Map<String, Object> params) {
        List<Map<String, Object>> recommendations = new ArrayList<>();

        Map<String, Object> rec1 = new HashMap<>();
        rec1.put("name", "营养均衡套餐");
        rec1.put("description", "包含主食、主菜、蔬菜和汤品");
        rec1.put("price", new BigDecimal("18.00"));
        recommendations.add(rec1);

        return recommendations;
    }

    private List<Map<String, Object>> getPopularMealItems(Map<String, Object> params) {
        List<Map<String, Object>> popularItems = new ArrayList<>();

        Map<String, Object> item1 = new HashMap<>();
        item1.put("name", "红烧肉");
        item1.put("category", "MAIN_DISH");
        item1.put("price", new BigDecimal("15.00"));
        item1.put("popularity", 95);
        popularItems.add(item1);

        return popularItems;
    }

    private Map<String, Object> recordUserPreferences(Map<String, Object> params, Map<String, Object> result) {
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("timestamp", LocalDateTime.now().toString());
        preferences.put("diningPeriod", getCurrentDiningPeriod().getName());
        preferences.put("mealCount", ((List<?>) params.get("mealItems")).size());
        preferences.put("nutritionLevel", result.get("nutritionLevel"));
        return preferences;
    }

    private List<String> generateDiningTips(Map<String, Object> params, Map<String, Object> result) {
        List<String> tips = new ArrayList<>();

        tips.add("建议细嚼慢咽，有助消化");
        tips.add("餐后可以适量散步");

        if (Boolean.TRUE.equals(result.get("isPeakTime"))) {
            tips.add("当前为就餐高峰期，建议错峰就餐");
        }

        return tips;
    }

    /**
     * 获取所有支持的就餐时段
     */
    public static List<String> getSupportedDiningPeriods() {
        return Arrays.stream(DiningPeriod.values())
                .map(DiningPeriod::getName)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取所有支持的餐品类型
     */
    public static List<String> getSupportedMealTypes() {
        return Arrays.stream(MealType.values())
                .map(MealType::getName)
                .collect(java.util.stream.Collectors.toList());
    }
}