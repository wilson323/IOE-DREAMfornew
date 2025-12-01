package net.lab1024.sa.base.common.util;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * 智能日期工具类
 * 严格遵循repowiki规范：工具类统一放在common.util包
 *
 * @author SmartAdmin Team
 * @date 2025/11/21
 */
public class SmartDateUtil {

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";
    public static final String DEFAULT_MONTH_FORMAT = "yyyy-MM";
    public static final String COMPACT_DATETIME_FORMAT = "yyyyMMdd_HHmmss";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATETIME_FORMAT);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT);
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_MONTH_FORMAT);
    private static final DateTimeFormatter COMPACT_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(COMPACT_DATETIME_FORMAT);

    /**
     * 格式化日期
     *
     * @param date 日期
     * @return 格式化后的字符串
     */
    public static String formatDate(LocalDateTime date) {
        if (date == null) {
            return null;
        }
        return date.format(DATE_FORMATTER);
    }

    /**
     * 格式化日期时间
     *
     * @param date 日期时间
     * @return 格式化后的字符串
     */
    public static String formatDateTime(LocalDateTime date) {
        if (date == null) {
            return null;
        }
        return date.format(DATETIME_FORMATTER);
    }

    /**
     * 格式化时间
     *
     * @param date 日期时间
     * @return 格式化后的字符串
     */
    public static String formatTime(LocalDateTime date) {
        if (date == null) {
            return null;
        }
        return date.format(TIME_FORMATTER);
    }

    /**
     * 解析日期字符串
     *
     * @param dateStr 日期字符串
     * @return LocalDateTime
     */
    public static LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateStr + " 00:00:00", DATETIME_FORMATTER);
    }

    /**
     * 解析日期时间字符串
     *
     * @param dateTimeStr 日期时间字符串
     * @return LocalDateTime
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
    }

    /**
     * 计算两个日期之间的天数差
     *
     * @param start 开始日期
     * @param end   结束日期
     * @return 天数差
     */
    public static long daysBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * 获取当前日期
     *
     * @return 当前日期的字符串形式
     */
    public static String getCurrentDate() {
        return formatDate(LocalDateTime.now());
    }

    /**
     * 获取当前日期时间
     *
     * @return 当前日期时间的字符串形式
     */
    public static String getCurrentDateTime() {
        return formatDateTime(LocalDateTime.now());
    }

    /**
     * 获取当前时间
     *
     * @return 当前时间的字符串形式
     */
    public static String getCurrentTime() {
        return formatTime(LocalDateTime.now());
    }

    /**
     * 日期增加天数
     *
     * @param date 日期
     * @param days 天数
     * @return 增加后的日期
     */
    public static LocalDateTime plusDays(LocalDateTime date, long days) {
        if (date == null) {
            return null;
        }
        return date.plusDays(days);
    }

    /**
     * 日期减少天数
     *
     * @param date 日期
     * @param days 天数
     * @return 减少后的日期
     */
    public static LocalDateTime minusDays(LocalDateTime date, long days) {
        if (date == null) {
            return null;
        }
        return date.minusDays(days);
    }

    /**
     * 检查日期是否在指定范围内
     *
     * @param date  要检查的日期
     * @param start 开始日期
     * @param end   结束日期
     * @return 是否在范围内
     */
    public static boolean isBetween(LocalDateTime date, LocalDateTime start, LocalDateTime end) {
        if (date == null || start == null || end == null) {
            return false;
        }
        return !date.isBefore(start) && !date.isAfter(end);
    }

    /**
     * LocalDateTime 转 Date
     *
     * @param localDateTime LocalDateTime
     * @return Date
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant());
    }

    /**
     * Date 转 LocalDateTime
     *
     * @param date Date
     * @return LocalDateTime
     */
    public static LocalDateTime fromDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * 格式化年月
     *
     * @param yearMonth 年月
     * @return 格式化后的字符串
     */
    public static String formatMonth(YearMonth yearMonth) {
        if (yearMonth == null) {
            return null;
        }
        return yearMonth.format(MONTH_FORMATTER);
    }

    /**
     * 格式化紧凑日期时间（用于文件名等）
     *
     * @param date 日期时间
     * @return 格式化后的字符串
     */
    public static String formatCompactDateTime(LocalDateTime date) {
        if (date == null) {
            return null;
        }
        return date.format(COMPACT_DATETIME_FORMATTER);
    }

    /**
     * 解析年月字符串
     *
     * @param monthStr 年月字符串 (yyyy-MM)
     * @return YearMonth
     */
    public static YearMonth parseMonth(String monthStr) {
        if (monthStr == null || monthStr.trim().isEmpty()) {
            return null;
        }
        return YearMonth.parse(monthStr, MONTH_FORMATTER);
    }

    /**
     * 格式化LocalDate
     *
     * @param date 日期
     * @return 格式化后的字符串
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DATE_FORMATTER);
    }

    /**
     * 解析日期字符串
     *
     * @param dateStr 日期字符串 (yyyy-MM-dd)
     * @return LocalDate
     */
    public static LocalDate parseLocalDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }
}