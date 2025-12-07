package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * 用户行为分析数据VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class ConsumeUserBehaviorAnalysisVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 消费偏好分析（旧版，保留兼容性）
     */
    private ConsumptionPreference preference;

    /**
     * 消费时段分析
     */
    private List<TimeSlotAnalysis> timeSlotAnalysis;

    /**
     * 消费频率
     */
    private Integer consumptionFrequency;

    /**
     * 时间范围
     */
    private String timeRange;

    /**
     * 平均消费金额
     */
    private BigDecimal avgConsumptionAmount;

    /**
     * 用户分群分析
     */
    private List<UserSegmentAnalysis> userSegments;

    /**
     * 消费习惯分析
     */
    private ConsumptionHabitAnalysis consumptionHabit;

    /**
     * 消费习惯分析（复数形式，与consumptionHabit相同）
     */
    private ConsumptionHabitAnalysis consumptionHabits;

    /**
     * 偏好分析
     */
    private PreferenceAnalysis preferenceAnalysis;

    /**
     * 消费偏好
     */
    @Data
    public static class ConsumptionPreference {
        /**
         * 偏好消费模式
         */
        private String preferredMode;

        /**
         * 偏好区域
         */
        private Long preferredAreaId;

        /**
         * 偏好时段
         */
        private String preferredTimeSlot;
    }

    /**
     * 时段分析
     */
    @Data
    public static class TimeSlotAnalysis {
        /**
         * 时段（如：08:00-10:00）
         */
        private String timeSlot;

        /**
         * 消费金额
         */
        private BigDecimal amount;

        /**
         * 消费笔数
         */
        private Integer count;
    }

    /**
     * 用户分群分析
     */
    @Data
    public static class UserSegmentAnalysis {
        /**
         * 分群名称
         */
        private String segmentName;

        /**
         * 用户数量
         */
        private Integer userCount;

        /**
         * 平均消费金额
         */
        private BigDecimal avgAmount;

        /**
         * 占比（百分比）
         */
        private BigDecimal percentage;
    }

    /**
     * 消费习惯分析
     */
    @Data
    public static class ConsumptionHabitAnalysis {
        /**
         * 习惯类型
         */
        private String habitType;

        /**
         * 习惯描述
         */
        private String description;

        /**
         * 高峰时段
         */
        private List<String> peakTimeSlots;

        /**
         * 常去地点
         */
        private List<String> frequentLocations;
    }

    /**
     * 偏好分析
     */
    @Data
    public static class PreferenceAnalysis {
        /**
         * 偏好类型
         */
        private String preferenceType;

        /**
         * 偏好值
         */
        private String preferenceValue;

        /**
         * 偏好类别
         */
        private List<String> preferredCategories;
    }
}
