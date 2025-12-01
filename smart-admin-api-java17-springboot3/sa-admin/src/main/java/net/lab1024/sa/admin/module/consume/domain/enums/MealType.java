package net.lab1024.sa.admin.module.consume.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

/**
 * 餐别枚举
 * 严格遵循repowiki规范：定义消费时间控制和餐别管理
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Getter
@AllArgsConstructor
public enum MealType {

    /**
     * 早餐
     */
    BREAKFAST("BREAKFAST", "早餐", "早餐时段",
            LocalTime.of(6, 0), LocalTime.of(9, 30)),

    /**
     * 午餐
     */
    LUNCH("LUNCH", "午餐", "午餐时段",
            LocalTime.of(11, 0), LocalTime.of(14, 30)),

    /**
     * 晚餐
     */
    DINNER("DINNER", "晚餐", "晚餐时段",
            LocalTime.of(17, 0), LocalTime.of(20, 30)),

    /**
     * 夜宵
     */
    SUPPER("SUPPER", "夜宵", "夜宵时段",
            LocalTime.of(21, 0), LocalTime.of(23, 30)),

    /**
     * 全天有效
     */
    ALL_DAY("ALL_DAY", "全天", "全天时段",
            LocalTime.of(0, 0), LocalTime.of(23, 59));

    private final String code;
    private final String name;
    private final String description;
    private final LocalTime startTime;
    private final LocalTime endTime;

    /**
     * 检查当前时间是否在该餐别时段内
     */
    public boolean isCurrentTime() {
        LocalTime now = LocalTime.now();
        return !now.isBefore(startTime) && !now.isAfter(endTime);
    }

    /**
     * 检查指定时间是否在该餐别时段内
     */
    public boolean isTimeInRange(LocalTime time) {
        if (this == ALL_DAY) {
            return true;
        }
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }

    /**
     * 根据当前时间获取餐别
     */
    public static MealType getCurrentMealType() {
        LocalTime now = LocalTime.now();

        for (MealType mealType : values()) {
            if (mealType != ALL_DAY && mealType.isTimeInRange(now)) {
                return mealType;
            }
        }

        return ALL_DAY;
    }

    /**
     * 根据代码获取餐别
     */
    public static MealType fromCode(String code) {
        if (code == null) {
            return null;
        }

        for (MealType mealType : values()) {
            if (mealType.getCode().equals(code)) {
                return mealType;
            }
        }

        return null;
    }

    /**
     * 获取所有有效的餐别（排除全天）
     */
    public static List<MealType> getValidMealTypes() {
        return Arrays.asList(BREAKFAST, LUNCH, DINNER, SUPPER);
    }

    /**
     * 检查代码是否有效
     */
    public static boolean isValidCode(String code) {
        return fromCode(code) != null;
    }

    /**
     * 获取时段描述
     */
    public String getTimeRangeDesc() {
        if (this == ALL_DAY) {
            return "全天有效";
        }
        return String.format("%s - %s", startTime, endTime);
    }

    /**
     * 检查是否为工作日餐别（早餐、午餐、晚餐）
     */
    public boolean isWorkingDayMeal() {
        return this == BREAKFAST || this == LUNCH || this == DINNER;
    }

    /**
     * 获取推荐的金额档位
     */
    public java.math.BigDecimal[] getRecommendedAmounts() {
        switch (this) {
            case BREAKFAST:
                return new java.math.BigDecimal[]{
                    new java.math.BigDecimal("5"),
                    new java.math.BigDecimal("8"),
                    new java.math.BigDecimal("10")
                };
            case LUNCH:
                return new java.math.BigDecimal[]{
                    new java.math.BigDecimal("12"),
                    new java.math.BigDecimal("15"),
                    new java.math.BigDecimal("18"),
                    new java.math.BigDecimal("20")
                };
            case DINNER:
                return new java.math.BigDecimal[]{
                    new java.math.BigDecimal("15"),
                    new java.math.BigDecimal("18"),
                    new java.math.BigDecimal("25"),
                    new java.math.BigDecimal("30")
                };
            case SUPPER:
                return new java.math.BigDecimal[]{
                    new java.math.BigDecimal("8"),
                    new java.math.BigDecimal("10"),
                    new java.math.BigDecimal("15")
                };
            default:
                return new java.math.BigDecimal[]{
                    new java.math.BigDecimal("5"),
                    new java.math.BigDecimal("10"),
                    new java.math.BigDecimal("15"),
                    new java.math.BigDecimal("20")
                };
        }
    }
}