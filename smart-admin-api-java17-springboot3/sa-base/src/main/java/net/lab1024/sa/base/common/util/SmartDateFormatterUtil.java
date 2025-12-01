/*
 * Smart日期格式化工具类
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-19
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.base.common.util;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 * Smart日期格式化工具类
 * 提供常用的日期时间格式化和解析功能
 *
 * @author SmartAdmin Team
 * @date 2025/01/19
 */
public class SmartDateFormatterUtil {

    // 常用日期格式
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATETIME_FORMAT_WITH_MILLIS = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String COMPACT_DATETIME_FORMAT = "yyyyMMddHHmmss";

    // 对应的DateTimeFormatter
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_FORMAT);
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
    public static final DateTimeFormatter DATETIME_FORMATTER_WITH_MILLIS = DateTimeFormatter.ofPattern(DATETIME_FORMAT_WITH_MILLIS);
    public static final DateTimeFormatter COMPACT_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(COMPACT_DATETIME_FORMAT);

    /**
     * 格式化当前日期
     */
    public static String formatDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    /**
     * 格式化指定日期
     */
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }

    /**
     * 格式化当前时间
     */
    public static String formatTime() {
        return LocalTime.now().format(TIME_FORMATTER);
    }

    /**
     * 格式化指定时间
     */
    public static String formatTime(LocalTime time) {
        return time != null ? time.format(TIME_FORMATTER) : null;
    }

    /**
     * 格式化当前日期时间
     */
    public static String formatDateTime() {
        return LocalDateTime.now().format(DATETIME_FORMATTER);
    }

    /**
     * 格式化指定日期时间
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : null;
    }

    /**
     * 格式化日期时间（包含毫秒）
     */
    public static String formatDateTimeWithMillis(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER_WITH_MILLIS) : null;
    }

    /**
     * 格式化为紧凑的日期时间格式
     */
    public static String formatCompactDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(COMPACT_DATETIME_FORMATTER) : null;
    }

    /**
     * 格式化Date对象
     */
    public static String formatDate(Date date) {
        return date != null ? new java.sql.Date(date.getTime()).toString() : null;
    }

    /**
     * 解析日期字符串
     */
    public static LocalDate parseDate(String dateStr) {
        if (SmartStringUtil.isEmpty(dateStr)) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * 解析时间字符串
     */
    public static LocalTime parseTime(String timeStr) {
        if (SmartStringUtil.isEmpty(timeStr)) {
            return null;
        }
        try {
            return LocalTime.parse(timeStr, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * 解析日期时间字符串
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (SmartStringUtil.isEmpty(dateTimeStr)) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * 解析日期时间字符串（包含毫秒）
     */
    public static LocalDateTime parseDateTimeWithMillis(String dateTimeStr) {
        if (SmartStringUtil.isEmpty(dateTimeStr)) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER_WITH_MILLIS);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * 解析紧凑格式的日期时间字符串
     */
    public static LocalDateTime parseCompactDateTime(String dateTimeStr) {
        if (SmartStringUtil.isEmpty(dateTimeStr)) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, COMPACT_DATETIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * 智能解析，支持多种格式
     */
    public static LocalDateTime parseSmart(String dateTimeStr) {
        if (SmartStringUtil.isEmpty(dateTimeStr)) {
            return null;
        }

        // 尝试按优先级解析
        LocalDateTime result = parseDateTime(dateTimeStr);
        if (result != null) return result;

        result = parseDateTimeWithMillis(dateTimeStr);
        if (result != null) return result;

        result = parseCompactDateTime(dateTimeStr);
        if (result != null) return result;

        return null;
    }

    /**
     * 获取当前时间戳
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 获取当前秒级时间戳
     */
    public static long getCurrentSecondTimestamp() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 格式化时间戳为日期时间字符串
     */
    public static String formatTimestamp(long timestamp) {
        return new java.sql.Date(timestamp).toString();
    }

    /**
     * 获取友好的时间显示（如：刚刚、5分钟前、1小时前等）
     */
    public static String getFriendlyTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();
        long diffMinutes = java.time.Duration.between(dateTime, now).toMinutes();

        if (diffMinutes < 1) {
            return "刚刚";
        } else if (diffMinutes < 60) {
            return diffMinutes + "分钟前";
        } else if (diffMinutes < 1440) { // 24小时
            return (diffMinutes / 60) + "小时前";
        } else if (diffMinutes < 10080) { // 7天
            return (diffMinutes / 1440) + "天前";
        } else {
            return formatDateTime(dateTime);
        }
    }

    /**
     * 判断是否为今天
     */
    public static boolean isToday(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.toLocalDate().equals(LocalDate.now());
    }

    /**
     * 判断是否为昨天
     */
    public static boolean isYesterday(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.toLocalDate().equals(LocalDate.now().minusDays(1));
    }

    /**
     * 判断是否为本周
     */
    public static boolean isThisWeek(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.minusDays(now.getDayOfWeek().getValue() - 1);
        LocalDate weekEnd = weekStart.plusDays(6);
        return !dateTime.toLocalDate().isBefore(weekStart) && !dateTime.toLocalDate().isAfter(weekEnd);
    }
}