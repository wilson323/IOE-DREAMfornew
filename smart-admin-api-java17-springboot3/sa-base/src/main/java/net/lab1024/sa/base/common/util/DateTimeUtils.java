/*
 * 日期时间工具类
 *
 * 统一项目中LocalDateTime和LocalDate的使用
 * 提供格式化和转换方法，确保时间处理的一致性
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */

package net.lab1024.sa.base.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日期时间工具类
 * <p>
 * 统一项目中LocalDateTime和LocalDate的使用策略：
 * 1. 实体类统一使用LocalDateTime存储时间信息
 * 2. 业务操作中使用LocalDateTime
 * 3. 需要日期显示时通过格式化处理
 * 4. 提供便捷的转换和查询方法
 * </p>
 */
public class DateTimeUtils {

    private static final Logger log = LoggerFactory.getLogger(DateTimeUtils.class);

    // 常用日期时间格式
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter DATETIME_MILLIS_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // 中文日期时间格式
    public static final DateTimeFormatter CHINESE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
    public static final DateTimeFormatter CHINESE_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH时mm分");

    /**
     * 当前时间（LocalDateTime）
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * 当前日期（LocalDate）
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /**
     * LocalDateTime转LocalDate
     */
    public static LocalDate toLocalDate(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toLocalDate() : null;
    }

    /**
     * LocalDate转LocalDateTime（当天开始时间）
     */
    public static LocalDateTime toDateTime(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }

    /**
     * LocalDate转LocalDateTime（指定时间）
     */
    public static LocalDateTime toDateTime(LocalDate date, LocalTime time) {
        return date != null && time != null ? date.atTime(time) : null;
    }

    /**
     * Date转LocalDateTime
     */
    public static LocalDateTime toDateTime(Date date) {
        return date != null ? date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null;
    }

    /**
     * LocalDateTime转Date
     */
    public static Date toDate(LocalDateTime dateTime) {
        return dateTime != null ? Date.from(dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()) : null;
    }

    /**
     * 格式化日期（yyyy-MM-dd）
     */
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }

    /**
     * 格式化日期时间（yyyy-MM-dd HH:mm:ss）
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : null;
    }

    /**
     * 格式化时间（HH:mm:ss）
     */
    public static String formatTime(LocalTime time) {
        return time != null ? time.format(TIME_FORMATTER) : null;
    }

    /**
     * 格式化中文日期（yyyy年MM月dd日）
     */
    public static String formatDateChinese(LocalDate date) {
        return date != null ? date.format(CHINESE_DATE_FORMATTER) : null;
    }

    /**
     * 格式化中文日期时间（yyyy年MM月dd日 HH时mm分）
     */
    public static String formatDateTimeChinese(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(CHINESE_DATETIME_FORMATTER) : null;
    }

    /**
     * 解析日期字符串（yyyy-MM-dd）
     */
    public static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("解析日期字符串失败: {}, 格式应为 yyyy-MM-dd", dateString);
            return null;
        }
    }

    /**
     * 解析日期时间字符串（yyyy-MM-dd HH:mm:ss）
     */
    public static LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeString, DATETIME_FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("解析日期时间字符串失败: {}, 格式应为 yyyy-MM-dd HH:mm:ss", dateTimeString);
            return null;
        }
    }

    /**
     * 智能解析日期时间（支持多种格式）
     */
    public static LocalDateTime parseSmart(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        // 尝试不同的解析格式
        String[] patterns = {
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy-MM-dd",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy/MM/dd",
            "MM/dd/yyyy HH:mm:ss",
            "MM/dd/yyyy"
        };

        for (String pattern : patterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                return LocalDateTime.parse(dateString, formatter);
            } catch (DateTimeParseException e) {
                // 继续尝试下一个格式
            }
        }

        log.warn("无法解析日期时间字符串: {}", dateString);
        return null;
    }

    /**
     * 获取日期的开始时间（00:00:00）
     */
    public static LocalDateTime startOfDay(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }

    /**
     * 获取日期的结束时间（23:59:59）
     */
    public static LocalDateTime endOfDay(LocalDate date) {
        return date != null ? date.atTime(LocalTime.MAX) : null;
    }

    /**
     * 获取日期时间的开始时间（00:00:00）
     */
    public static LocalDateTime startOfDay(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toLocalDate().atStartOfDay() : null;
    }

    /**
     * 获取日期时间的结束时间（23:59:59）
     */
    public static LocalDateTime endOfDay(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toLocalDate().atTime(LocalTime.MAX) : null;
    }

    /**
     * 计算两个日期之间的天数差
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(start, end);
    }

    /**
     * 计算两个日期时间之间的分钟差
     */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.MINUTES.between(start, end);
    }

    /**
     * 计算两个日期时间之间的小时差
     */
    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.HOURS.between(start, end);
    }

    /**
     * 判断是否是同一天
     */
    public static boolean isSameDay(LocalDateTime dt1, LocalDateTime dt2) {
        if (dt1 == null || dt2 == null) {
            return false;
        }
        return dt1.toLocalDate().equals(dt2.toLocalDate());
    }

    /**
     * 判断是否是今天
     */
    public static boolean isToday(LocalDateTime dateTime) {
        return dateTime != null && dateTime.toLocalDate().equals(LocalDate.now());
    }

    /**
     * 判断是否是昨天
     */
    public static boolean isYesterday(LocalDateTime dateTime) {
        return dateTime != null && dateTime.toLocalDate().equals(LocalDate.now().minusDays(1));
    }

    /**
     * 判断是否是明天
     */
    public static boolean isTomorrow(LocalDateTime dateTime) {
        return dateTime != null && dateTime.toLocalDate().equals(LocalDate.now().plusDays(1));
    }

    /**
     * 获取指定日期的开始时间戳（当天的00:00:00）
     */
    public static long getTimestamp(LocalDate date) {
        return date != null ? date.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() : 0;
    }

    /**
     * 获取指定日期时间的时间戳
     */
    public static long getTimestamp(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() : 0;
    }

    /**
     * 时间戳转LocalDateTime
     */
    public static LocalDateTime fromTimestamp(long timestamp) {
        return LocalDateTime.ofEpochSecond(timestamp / 1000, 0, java.time.ZoneOffset.UTC);
    }

    /**
     * 毫秒时间戳转LocalDateTime
     */
    public static LocalDateTime fromTimestampMillis(long timestampMillis) {
        return LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(timestampMillis), java.time.ZoneId.systemDefault());
    }

    /**
     * 创建统一的日期时间对象（方便测试）
     */
    public static LocalDateTime of(int year, int month, int day, int hour, int minute, int second) {
        return LocalDateTime.of(year, month, day, hour, minute, second);
    }

    /**
     * 创建统一的日期对象（方便测试）
     */
    public static LocalDate of(int year, int month, int day) {
        return LocalDate.of(year, month, day);
    }
}