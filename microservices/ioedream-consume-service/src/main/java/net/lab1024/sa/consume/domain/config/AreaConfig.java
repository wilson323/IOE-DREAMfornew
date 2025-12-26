package net.lab1024.sa.consume.domain.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 区域权限配置 - 对应POSID_AREA.area_config字段
 *
 * 存储区域的权限配置信息，包括：
 * - 子区域权限
 * - 餐别权限
 * - 时间段权限
 * - 消费限额
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
@Data
public class AreaConfig {

    /**
     * 子区域权限配置
     */
    @JsonProperty("subAreas")
    private SubAreaConfig subAreas;

    /**
     * 餐别权限配置
     */
    @JsonProperty("mealPermissions")
    private MealPermissionConfig mealPermissions;

    /**
     * 时间段权限配置
     */
    @JsonProperty("timePermissions")
    private TimePermissionConfig timePermissions;

    /**
     * 消费限额配置
     */
    @JsonProperty("consumeLimits")
    private ConsumeLimitConfig consumeLimits;

    /**
     * 子区域权限配置
     */
    @Data
    public static class SubAreaConfig {
        /**
         * 是否包含所有子区域
         */
        @JsonProperty("includeSubAreas")
        private Boolean includeSubAreas;

        /**
         * 允许的子区域列表
         * 为空时表示允许所有子区域
         */
        @JsonProperty("allowedAreas")
        private List<String> allowedAreas;

        /**
         * 禁止的子区域列表
         * 优先级高于allowedAreas
         */
        @JsonProperty("deniedAreas")
        private List<String> deniedAreas;
    }

    /**
     * 餐别权限配置
     */
    @Data
    public static class MealPermissionConfig {
        /**
         * 允许的餐别列表
         * 示例：["BREAKFAST", "LUNCH", "DINNER", "SNACK"]
         */
        @JsonProperty("allowedMeals")
        private List<String> allowedMeals;

        /**
         * 禁止的餐别列表
         * 优先级高于allowedMeals
         */
        @JsonProperty("deniedMeals")
        private List<String> deniedMeals;

        /**
         * 餐别消费次数限制
         * key: 餐别编码
         * value: 每日最大消费次数（0表示不限制）
         */
        @JsonProperty("mealCountLimits")
        private Map<String, Integer> mealCountLimits;
    }

    /**
     * 时间段权限配置
     */
    @Data
    public static class TimePermissionConfig {
        /**
         * 允许消费的时间段
         * 格式：HH:mm-HH:mm
         * 示例：["07:00-09:00", "11:00-13:00", "17:00-19:00"]
         */
        @JsonProperty("allowedTimeRanges")
        private List<String> allowedTimeRanges;

        /**
         * 禁止消费的时间段
         * 优先级高于allowedTimeRanges
         */
        @JsonProperty("deniedTimeRanges")
        private List<String> deniedTimeRanges;
    }

    /**
     * 消费限额配置
     */
    @Data
    public static class ConsumeLimitConfig {
        /**
         * 单笔消费限额
         */
        @JsonProperty("singleAmountLimit")
        private Double singleAmountLimit;

        /**
         * 每日消费限额
         */
        @JsonProperty("dailyAmountLimit")
        private Double dailyAmountLimit;

        /**
         * 每日消费次数限制
         */
        @JsonProperty("dailyCountLimit")
        private Integer dailyCountLimit;
    }
}
