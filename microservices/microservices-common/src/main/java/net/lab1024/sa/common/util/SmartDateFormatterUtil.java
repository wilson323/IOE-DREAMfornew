package net.lab1024.sa.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 智能日期格式化工具类
 * 提供常用的日期格式化方法
 *
 * @author SmartAdmin Team
 * @since 2025-11-30
 */
public class SmartDateFormatterUtil {

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * 格式化默认格式的日期时间
     *
     * @param dateTime 日期时间
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DEFAULT_FORMATTER);
    }

    /**
     * 格式化日期
     *
     * @param dateTime 日期时间
     * @return 格式化后的日期字符串
     */
    public static String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATE_FORMATTER);
    }

    /**
     * 格式化时间
     *
     * @param dateTime 日期时间
     * @return 格式化后的时间字符串
     */
    public static String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(TIME_FORMATTER);
    }

    /**
     * 使用指定格式格式化日期时间
     *
     * @param dateTime 日期时间
     * @param pattern 日期格式
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 获取当前时间的默认格式字符串
     *
     * @return 当前时间字符串
     */
    public static String now() {
        return LocalDateTime.now().format(DEFAULT_FORMATTER);
    }
}