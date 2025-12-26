package net.lab1024.sa.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一类型转换工具类
 * <p>
 * 提供安全的类型转换方法，统一处理类型转换逻辑
 * 避免直接类型转换导致的运行时异常
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
public class TypeUtils {

    private static final DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 安全的字符串转Long转换
     *
     * @param value 字符串值
     * @return 转换结果，失败时返回null
     */
    public static Long parseLong(String value) {
        return parseLong(value, null);
    }

    /**
     * 安全的字符串转Long转换，支持默认值
     *
     * @param value      字符串值
     * @param defaultVal 默认值
     * @return 转换结果，失败时返回默认值
     */
    public static Long parseLong(String value, Long defaultVal) {
        if (value == null || value.trim().isEmpty()) {
            return defaultVal;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            System.err.println("Long转换失败: " + value + ", 返回默认值: " + defaultVal);
            return defaultVal;
        }
    }

    /**
     * 安全的字符串转Integer转换
     *
     * @param value 字符串值
     * @return 转换结果，失败时返回null
     */
    public static Integer parseInt(String value) {
        return parseInt(value, null);
    }

    /**
     * 安全的字符串转Integer转换，支持默认值
     *
     * @param value      字符串值
     * @param defaultVal 默认值
     * @return 转换结果，失败时返回默认值
     */
    public static Integer parseInt(String value, Integer defaultVal) {
        if (value == null || value.trim().isEmpty()) {
            return defaultVal;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            System.err.println("Integer转换失败: " + value + ", 返回默认值: " + defaultVal);
            return defaultVal;
        }
    }

    /**
     * 安全的Long转字符串转换
     *
     * @param value Long值
     * @return 字符串表示，null值返回空字符串
     */
    public static String toString(Long value) {
        return value != null ? value.toString() : "";
    }

    /**
     * 安全的Integer转字符串转换
     *
     * @param value Integer值
     * @return 字符串表示，null值返回空字符串
     */
    public static String toString(Integer value) {
        return value != null ? value.toString() : "";
    }

    /**
     * 安全的日期时间字符串解析
     *
     * @param dateTimeStr 日期时间字符串
     * @return 解析结果，失败时返回null
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return parseDateTime(dateTimeStr, DEFAULT_DATE_FORMATTER);
    }

    /**
     * 安全的日期时间字符串解析，支持自定义格式
     *
     * @param dateTimeStr 日期时间字符串
     * @param formatter   日期格式
     * @return 解析结果，失败时返回null
     */
    public static LocalDateTime parseDateTime(String dateTimeStr, DateTimeFormatter formatter) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr.trim(), formatter);
        } catch (DateTimeParseException e) {
            System.err.println("日期时间解析失败: " + dateTimeStr);
            return null;
        }
    }

    /**
     * 安全的集合类型转换 - 将Object转为List
     *
     * @param obj 对象
     * @param <T> 类型参数
     * @return 转换后的List，失败时返回空List
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> toList(Object obj) {
        if (obj == null) {
            return new ArrayList<>();
        }
        if (obj instanceof List) {
            return (List<T>) obj;
        }
        return new ArrayList<>();
    }

    /**
     * 安全的集合类型转换 - 将Object转为Map
     *
     * @param obj 对象
     * @param <K> Key类型
     * @param <V> Value类型
     * @return 转换后的Map，失败时返回空Map
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> toMap(Object obj) {
        if (obj == null) {
            return new HashMap<>();
        }
        if (obj instanceof Map) {
            return (Map<K, V>) obj;
        }
        return new HashMap<>();
    }

    /**
     * 安全的Object类型转换
     *
     * @param obj        原始对象
     * @param targetClass 目标类型
     * @param <T>        类型参数
     * @return 转换结果，失败时返回null
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj, Class<T> targetClass) {
        if (obj == null) {
            return null;
        }
        if (targetClass.isInstance(obj)) {
            return (T) obj;
        }
        System.err.println("类型转换失败: " + obj.getClass().getName() + " -> " + targetClass.getName() + ", 返回null");
        return null;
    }

    /**
     * 检查对象是否为数字类型
     *
     * @param obj 对象
     * @return 是否为数字类型
     */
    public static boolean isNumber(Object obj) {
        return obj instanceof Number;
    }

    /**
     * 检查对象是否为字符串类型且有内容
     *
     * @param obj 对象
     * @return 是否为有效字符串
     */
    public static boolean hasText(Object obj) {
        return obj instanceof String && ((String) obj).trim().length() > 0;
    }

    /**
     * 安全的字符串值提取（处理null和空字符串）
     *
     * @param value 原始值
     * @return 处理后的字符串
     */
    public static String safeString(String value) {
        return value != null ? value : "";
    }

    /**
     * 安全的字符串值提取（处理null、空字符串和空白）
     *
     * @param value 原始值
     * @return 处理后的字符串
     */
    public static String safeStringTrim(String value) {
        return value != null ? value.trim() : "";
    }

  
    /**
     * 检查字符串是否为null或空
     *
     * @param value 字符串值
     * @return 是否为null或空
     */
    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * 检查字符串是否不为null且非空
     *
     * @param value 字符串值
     * @return 是否不为null且非空
     */
    public static boolean isNotEmpty(String value) {
        return !isEmpty(value);
    }
}
