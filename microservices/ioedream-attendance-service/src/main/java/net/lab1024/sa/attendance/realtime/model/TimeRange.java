package net.lab1024.sa.attendance.realtime.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 时间范围
 * <p>
 * 封装考勤统计的时间范围信息
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeRange {

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 时间范围类型
     */
    private TimeRangeType timeRangeType;

    /**
     * 时间范围描述
     */
    private String description;

    /**
     * 时区（默认为系统时区）
     */
    private String timezone;

    /**
     * 时间范围长度（分钟）
     */
    private Long durationMinutes;

    /**
     * 是否包含边界
     */
    private Boolean inclusiveBoundaries;

    /**
     * 时间范围类型枚举
     */
    public enum TimeRangeType {
        TODAY("今天"),
        YESTERDAY("昨天"),
        THIS_WEEK("本周"),
        LAST_WEEK("上周"),
        THIS_MONTH("本月"),
        LAST_MONTH("上月"),
        THIS_QUARTER("本季度"),
        LAST_QUARTER("上季度"),
        THIS_YEAR("今年"),
        LAST_YEAR("去年"),
        CUSTOM_RANGE("自定义范围"),
        REAL_TIME("实时"),
        LAST_24_HOURS("最近24小时"),
        LAST_7_DAYS("最近7天"),
        LAST_30_DAYS("最近30天"),
        WORKING_HOURS("工作时间"),
        NON_WORKING_HOURS("非工作时间");

        private final String description;

        TimeRangeType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 计算时间范围长度（分钟）
     */
    public void calculateDurationMinutes() {
        if (startTime != null && endTime != null) {
            this.durationMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
        } else {
            this.durationMinutes = null;
        }
    }

    /**
     * 检查指定时间是否在范围内
     */
    public boolean contains(LocalDateTime dateTime) {
        if (startTime == null || endTime == null) {
            return false;
        }

        if (inclusiveBoundaries != null && inclusiveBoundaries) {
            return !dateTime.isBefore(startTime) && !dateTime.isAfter(endTime);
        } else {
            return dateTime.isAfter(startTime) && dateTime.isBefore(endTime);
        }
    }

    /**
     * 检查当前时间是否在范围内
     */
    public boolean containsNow() {
        return contains(LocalDateTime.now());
    }

    /**
     * 获取格式化的时间范围描述
     */
    public String getFormattedRange() {
        if (startTime == null && endTime == null) {
            return "无限制";
        }

        if (startTime == null) {
            return "截至 " + endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }

        if (endTime == null) {
            return startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + " 起";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");
        return startTime.format(formatter) + " ~ " + endTime.format(formatter);
    }

    /**
     * 获取完整格式化的时间范围描述
     */
    public String getFullFormattedRange() {
        if (startTime == null && endTime == null) {
            return "无限制";
        }

        if (startTime == null) {
            return "截至 " + endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        if (endTime == null) {
            return startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " 起";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return startTime.format(formatter) + " ~ " + endTime.format(formatter);
    }

    /**
     * 创建今天的时间范围
     */
    public static TimeRange today() {
        LocalDateTime now = LocalDateTime.now();
        return TimeRange.builder()
                .startTime(now.withHour(0).withMinute(0).withSecond(0).withNano(0))
                .endTime(now.withHour(23).withMinute(59).withSecond(59).withNano(999999999))
                .timeRangeType(TimeRangeType.TODAY)
                .description("今天")
                .inclusiveBoundaries(true)
                .build();
    }

    /**
     * 创建昨天的时间范围
     */
    public static TimeRange yesterday() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        return TimeRange.builder()
                .startTime(yesterday.withHour(0).withMinute(0).withSecond(0).withNano(0))
                .endTime(yesterday.withHour(23).withMinute(59).withSecond(59).withNano(999999999))
                .timeRangeType(TimeRangeType.YESTERDAY)
                .description("昨天")
                .inclusiveBoundaries(true)
                .build();
    }

    /**
     * 创建本周的时间范围
     */
    public static TimeRange thisWeek() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekStart = now.minusDays(now.getDayOfWeek().getValue() - 1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime weekEnd = weekStart.plusDays(6)
                .withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        return TimeRange.builder()
                .startTime(weekStart)
                .endTime(weekEnd)
                .timeRangeType(TimeRangeType.THIS_WEEK)
                .description("本周")
                .inclusiveBoundaries(true)
                .build();
    }

    /**
     * 创建本月的时间范围
     */
    public static TimeRange thisMonth() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime monthEnd = monthStart.plusMonths(1).minusDays(1)
                .withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        return TimeRange.builder()
                .startTime(monthStart)
                .endTime(monthEnd)
                .timeRangeType(TimeRangeType.THIS_MONTH)
                .description("本月")
                .inclusiveBoundaries(true)
                .build();
    }

    /**
     * 创建最近24小时的时间范围
     */
    public static TimeRange last24Hours() {
        LocalDateTime now = LocalDateTime.now();
        return TimeRange.builder()
                .startTime(now.minusHours(24))
                .endTime(now)
                .timeRangeType(TimeRangeType.LAST_24_HOURS)
                .description("最近24小时")
                .inclusiveBoundaries(true)
                .build();
    }

    /**
     * 创建最近7天的时间范围
     */
    public static TimeRange last7Days() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(7).withHour(0).withMinute(0).withSecond(0).withNano(0);

        return TimeRange.builder()
                .startTime(start)
                .endTime(now)
                .timeRangeType(TimeRangeType.LAST_7_DAYS)
                .description("最近7天")
                .inclusiveBoundaries(true)
                .build();
    }

    /**
     * 创建最近30天的时间范围
     */
    public static TimeRange last30Days() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(30).withHour(0).withMinute(0).withSecond(0).withNano(0);

        return TimeRange.builder()
                .startTime(start)
                .endTime(now)
                .timeRangeType(TimeRangeType.LAST_30_DAYS)
                .description("最近30天")
                .inclusiveBoundaries(true)
                .build();
    }

    /**
     * 创建实时时间范围（当前时间前后15分钟）
     */
    public static TimeRange realtime() {
        LocalDateTime now = LocalDateTime.now();
        return TimeRange.builder()
                .startTime(now.minusMinutes(15))
                .endTime(now.plusMinutes(15))
                .timeRangeType(TimeRangeType.REAL_TIME)
                .description("实时")
                .inclusiveBoundaries(true)
                .build();
    }

    /**
     * 创建自定义时间范围
     */
    public static TimeRange custom(LocalDateTime startTime, LocalDateTime endTime) {
        return TimeRange.builder()
                .startTime(startTime)
                .endTime(endTime)
                .timeRangeType(TimeRangeType.CUSTOM_RANGE)
                .description("自定义范围")
                .inclusiveBoundaries(true)
                .build();
    }

    /**
     * 创建工作时间范围（例如：9:00-18:00）
     */
    public static TimeRange workingHours(LocalDateTime date) {
        LocalDateTime workStart = date.withHour(9).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime workEnd = date.withHour(18).withMinute(0).withSecond(0).withNano(0);

        return TimeRange.builder()
                .startTime(workStart)
                .endTime(workEnd)
                .timeRangeType(TimeRangeType.WORKING_HOURS)
                .description("工作时间")
                .inclusiveBoundaries(true)
                .build();
    }

    /**
     * 检查时间范围是否有效
     */
    public boolean isValid() {
        if (startTime == null && endTime == null) {
            return true; // 无限制范围
        }

        if (startTime != null && endTime != null) {
            return !startTime.isAfter(endTime);
        }

        return true; // 单边界范围
    }

    /**
     * 获取时间范围的中间点
     */
    public LocalDateTime getMidpoint() {
        if (startTime == null && endTime == null) {
            return LocalDateTime.now();
        }

        if (startTime == null) {
            return endTime;
        }

        if (endTime == null) {
            return startTime;
        }

        long totalMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
        return startTime.plusMinutes(totalMinutes / 2);
    }

    /**
     * 扩展时间范围
     */
    public TimeRange expand(long minutesToExpand) {
        LocalDateTime newStart = startTime != null ? startTime.minusMinutes(minutesToExpand) : null;
        LocalDateTime newEnd = endTime != null ? endTime.plusMinutes(minutesToExpand) : null;

        return TimeRange.builder()
                .startTime(newStart)
                .endTime(newEnd)
                .timeRangeType(this.timeRangeType)
                .description(this.description + " (扩展)")
                .timezone(this.timezone)
                .inclusiveBoundaries(this.inclusiveBoundaries)
                .build();
    }

    /**
     * 获取时间范围在指定日期的交集
     */
    public TimeRange getIntersectionWithDate(LocalDateTime date) {
        LocalDateTime dayStart = date.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime dayEnd = date.withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        LocalDateTime intersectStart = startTime != null && startTime.isAfter(dayStart) ? startTime : dayStart;
        LocalDateTime intersectEnd = endTime != null && endTime.isBefore(dayEnd) ? endTime : dayEnd;

        if (intersectStart.isBefore(intersectEnd) || intersectStart.equals(intersectEnd)) {
            return TimeRange.builder()
                    .startTime(intersectStart)
                    .endTime(intersectEnd)
                    .timeRangeType(TimeRangeType.CUSTOM_RANGE)
                    .description("日期交集")
                    .timezone(this.timezone)
                    .inclusiveBoundaries(this.inclusiveBoundaries)
                    .build();
        }

        return null; // 无交集
    }
}