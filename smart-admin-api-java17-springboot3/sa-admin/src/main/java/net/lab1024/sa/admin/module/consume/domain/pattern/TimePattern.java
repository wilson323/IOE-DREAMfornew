package net.lab1024.sa.admin.module.consume.domain.pattern;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * 时间模式
 * 严格遵循repowiki规范：数据传输对象，包含用户消费行为的时间模式特征
 *
 * @author SmartAdmin Team
 * @date 2025/11/19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimePattern {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 模式ID
     */
    private Long patternId;

    /**
     * 时间模式类型：FIXED-固定时间，FLEXIBLE-灵活时间，IRREGULAR-不规律，MEAL_TIME-用餐时间
     */
    private String timePatternType;

    /**
     * 主要消费时段1开始时间
     */
    private LocalTime primarySlot1Start;

    /**
     * 主要消费时段1结束时间
     */
    private LocalTime primarySlot1End;

    /**
     * 主要消费时段2开始时间
     */
    private LocalTime primarySlot2Start;

    /**
     * 主要消费时段2结束时间
     */
    private LocalTime primarySlot2End;

    /**
     * 主要消费时段3开始时间
     */
    private LocalTime primarySlot3Start;

    /**
     * 主要消费时段3结束时间
     */
    private LocalTime primarySlot3End;

    /**
     * 平均消费间隔（分钟）
     */
    private Integer averageIntervalMinutes;

    /**
     * 最小消费间隔（分钟）
     */
    private Integer minIntervalMinutes;

    /**
     * 最大消费间隔（分钟）
     */
    private Integer maxIntervalMinutes;

    /**
     * 消费时间标准差（分钟）
     */
    private BigDecimal intervalStdDeviation;

    /**
     * 早晨消费概率（0-100）
     */
    private BigDecimal morningProbability;

    /**
     * 中午消费概率（0-100）
     */
    private BigDecimal noonProbability;

    /**
     * 下午消费概率（0-100）
     */
    private BigDecimal afternoonProbability;

    /**
     * 晚上消费概率（0-100）
     */
    private BigDecimal eveningProbability;

    /**
     * 深夜消费概率（0-100）
     */
    private BigDecimal nightProbability;

    /**
     * 工作日消费概率（0-100）
     */
    private BigDecimal weekdayProbability;

    /**
     * 周末消费概率（0-100）
     */
    private BigDecimal weekendProbability;

    /**
     * 时间规律性评分（0-100）
     */
    private BigDecimal regularityScore;

    /**
     * 时间预测性评分（0-100）
     */
    private BigDecimal predictabilityScore;

    /**
     * 数据样本量
     */
    private Integer sampleSize;

    /**
     * 分析窗口期（天）
     */
    private Integer analysisWindow;

    /**
     * 模式创建时间
     */
    private LocalDateTime createTime;

    /**
     * 模式更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 模式有效期
     */
    private LocalDateTime validUntil;

    /**
     * 是否为当前有效模式
     */
    private Boolean isActive;

    /**
     * 小时分布统计（0-23时）
     */
    private Map<Integer, Integer> hourlyDistribution;

    /**
     * 星期分布统计（1-7，周一到周日）
     */
    private Map<Integer, Integer> weeklyDistribution;

    /**
     * 特殊日期消费模式
     */
    private Map<String, Object> specialDatePatterns;

    /**
     * 节假日消费模式
     */
    private Map<String, Object> holidayPatterns;

    /**
     * 季节性时间变化
     */
    private Map<String, Object> seasonalTimeChanges;

    /**
     * 时间漂移趋势
     */
    private List<BigDecimal> timeDriftTrend;

    /**
     * 模式置信度
     */
    private BigDecimal confidence;

    /**
     * 模式版本
     */
    private String patternVersion;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedAttributes;

    /**
     * 创建固定时间模式
     */
    public static TimePattern fixedTimePattern(Long userId, LocalTime slot1Start, LocalTime slot1End) {
        return TimePattern.builder()
                .userId(userId)
                .timePatternType("FIXED")
                .primarySlot1Start(slot1Start)
                .primarySlot1End(slot1End)
                .regularityScore(new BigDecimal("90.0"))
                .predictabilityScore(new BigDecimal("85.0"))
                .isActive(true)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusDays(30))
                .confidence(new BigDecimal("88.0"))
                .patternVersion("1.0")
                .build();
    }

    /**
     * 创建用餐时间模式
     */
    public static TimePattern mealTimePattern(Long userId) {
        return TimePattern.builder()
                .userId(userId)
                .timePatternType("MEAL_TIME")
                .primarySlot1Start(LocalTime.of(7, 30))   // 早餐
                .primarySlot1End(LocalTime.of(9, 0))
                .primarySlot2Start(LocalTime.of(11, 30))  // 午餐
                .primarySlot2End(LocalTime.of(13, 30))
                .primarySlot3Start(LocalTime.of(17, 30))  // 晚餐
                .primarySlot3End(LocalTime.of(19, 30))
                .morningProbability(new BigDecimal("25.0"))
                .noonProbability(new BigDecimal("45.0"))
                .afternoonProbability(new BigDecimal("15.0"))
                .eveningProbability(new BigDecimal("15.0"))
                .nightProbability(new BigDecimal("0.0"))
                .regularityScore(new BigDecimal("85.0"))
                .predictabilityScore(new BigDecimal("80.0"))
                .isActive(true)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusDays(30))
                .confidence(new BigDecimal("83.0"))
                .patternVersion("1.0")
                .build();
    }

    /**
     * 创建灵活时间模式
     */
    public static TimePattern flexibleTimePattern(Long userId, Integer avgInterval) {
        return TimePattern.builder()
                .userId(userId)
                .timePatternType("FLEXIBLE")
                .averageIntervalMinutes(avgInterval)
                .minIntervalMinutes(avgInterval / 3)
                .maxIntervalMinutes(avgInterval * 3)
                .intervalStdDeviation(new BigDecimal(avgInterval / 2))
                .regularityScore(new BigDecimal("60.0"))
                .predictabilityScore(new BigDecimal("55.0"))
                .isActive(true)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusDays(21))
                .confidence(new BigDecimal("70.0"))
                .patternVersion("1.0")
                .build();
    }

    /**
     * 创建不规律时间模式
     */
    public static TimePattern irregularTimePattern(Long userId) {
        return TimePattern.builder()
                .userId(userId)
                .timePatternType("IRREGULAR")
                .regularityScore(new BigDecimal("30.0"))
                .predictabilityScore(new BigDecimal("25.0"))
                .isActive(true)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusDays(7))
                .confidence(new BigDecimal("50.0"))
                .patternVersion("1.0")
                .build();
    }

    /**
     * 检查模式是否有效
     */
    public boolean isValid() {
        return Boolean.TRUE.equals(isActive) && validUntil != null && validUntil.isAfter(LocalDateTime.now());
    }

    /**
     * 检查是否为规律模式
     */
    public boolean isRegular() {
        return regularityScore != null && regularityScore.compareTo(new BigDecimal("70")) >= 0;
    }

    /**
     * 检查是否可预测
     */
    public boolean isPredictable() {
        return predictabilityScore != null && predictabilityScore.compareTo(new BigDecimal("70")) >= 0;
    }

    /**
     * 检查时间是否在主要时段内
     */
    public boolean isInPrimarySlot(LocalTime time) {
        if (time == null) {
            return false;
        }

        return isInSlot(time, primarySlot1Start, primarySlot1End) ||
               isInSlot(time, primarySlot2Start, primarySlot2End) ||
               isInSlot(time, primarySlot3Start, primarySlot3End);
    }

    /**
     * 检查时间是否在指定时段内
     */
    private boolean isInSlot(LocalTime time, LocalTime slotStart, LocalTime slotEnd) {
        if (time == null || slotStart == null || slotEnd == null) {
            return false;
        }

        if (slotStart.isBefore(slotEnd)) {
            return !time.isBefore(slotStart) && !time.isAfter(slotEnd);
        } else {
            // 跨日期的情况（如22:00-02:00）
            return !time.isBefore(slotStart) || !time.isAfter(slotEnd);
        }
    }

    /**
     * 获取时间模式类型描述
     */
    public String getTimePatternTypeDescription() {
        switch (timePatternType) {
            case "FIXED":
                return "固定时间";
            case "FLEXIBLE":
                return "灵活时间";
            case "IRREGULAR":
                return "不规律";
            case "MEAL_TIME":
                return "用餐时间";
            default:
                return "未知模式";
        }
    }

    /**
     * 获取最高概率时段
     */
    public String getHighestProbabilityTimeSlot() {
        if (morningProbability == null || noonProbability == null ||
            afternoonProbability == null || eveningProbability == null || nightProbability == null) {
            return "未知";
        }

        BigDecimal maxProb = morningProbability;
        String maxSlot = "早晨";

        if (noonProbability.compareTo(maxProb) > 0) {
            maxProb = noonProbability;
            maxSlot = "中午";
        }
        if (afternoonProbability.compareTo(maxProb) > 0) {
            maxProb = afternoonProbability;
            maxSlot = "下午";
        }
        if (eveningProbability.compareTo(maxProb) > 0) {
            maxProb = eveningProbability;
            maxSlot = "晚上";
        }
        if (nightProbability.compareTo(maxProb) > 0) {
            maxSlot = "深夜";
        }

        return maxSlot;
    }

    /**
     * 获取格式化的平均间隔时间
     */
    public String getFormattedAverageInterval() {
        if (averageIntervalMinutes == null) {
            return "0分钟";
        }
        if (averageIntervalMinutes < 60) {
            return averageIntervalMinutes + "分钟";
        } else {
            int hours = averageIntervalMinutes / 60;
            int minutes = averageIntervalMinutes % 60;
            if (minutes == 0) {
                return hours + "小时";
            } else {
                return hours + "小时" + minutes + "分钟";
            }
        }
    }

    /**
     * 获取时段概率分布
     */
    public Map<String, BigDecimal> getTimeSlotProbabilities() {
        return Map.of(
            "早晨", morningProbability != null ? morningProbability : BigDecimal.ZERO,
            "中午", noonProbability != null ? noonProbability : BigDecimal.ZERO,
            "下午", afternoonProbability != null ? afternoonProbability : BigDecimal.ZERO,
            "晚上", eveningProbability != null ? eveningProbability : BigDecimal.ZERO,
            "深夜", nightProbability != null ? nightProbability : BigDecimal.ZERO
        );
    }

    /**
     * 更新模式
     */
    public void updatePattern() {
        this.updateTime = LocalDateTime.now();
        if (this.validUntil != null) {
            this.validUntil = LocalDateTime.now().plusDays(30);
        }
    }

    /**
     * 获取模式质量评分
     */
    public BigDecimal getPatternQualityScore() {
        if (regularityScore == null || predictabilityScore == null) {
            return BigDecimal.ZERO;
        }
        return regularityScore.add(predictabilityScore).divide(new BigDecimal("2"), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 检查是否为用餐时间模式
     */
    public boolean isMealTimePattern() {
        return "MEAL_TIME".equals(timePatternType);
    }

    /**
     * 获取主要时段描述
     */
    public String getPrimarySlotsDescription() {
        StringBuilder description = new StringBuilder();

        if (primarySlot1Start != null && primarySlot1End != null) {
            description.append(formatTimeSlot(primarySlot1Start, primarySlot1End));
        }

        if (primarySlot2Start != null && primarySlot2End != null) {
            if (description.length() > 0) {
                description.append(", ");
            }
            description.append(formatTimeSlot(primarySlot2Start, primarySlot2End));
        }

        if (primarySlot3Start != null && primarySlot3End != null) {
            if (description.length() > 0) {
                description.append(", ");
            }
            description.append(formatTimeSlot(primarySlot3Start, primarySlot3End));
        }

        return description.length() > 0 ? description.toString() : "无固定时段";
    }

    /**
     * 格式化时段
     */
    private String formatTimeSlot(LocalTime start, LocalTime end) {
        return start.toString() + "-" + end.toString();
    }
}