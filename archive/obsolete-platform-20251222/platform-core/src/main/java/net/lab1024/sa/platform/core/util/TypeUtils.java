package net.lab1024.sa.platform.core.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 类型转换工具类 - 重构版本
 * <p>
 * 解决原有TypeUtils的依赖混乱问题，提供简洁的类型转换功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-12-22
 */
@Slf4j
public class TypeUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 安全的长整型转换
     */
    public static Long parseLong(String value) {
        return parseLong(value, null);
    }

    public static Long parseLong(String value, Long defaultValue) {
        if (!hasText(value)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            log.debug("长整型转换失败: value={}, 使用默认值={}", value, defaultValue);
            return defaultValue;
        }
    }

    /**
     * 安全的整型转换
     */
    public static Integer parseInt(String value) {
        return parseInt(value, null);
    }

    public static Integer parseInt(String value, Integer defaultValue) {
        if (!hasText(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.debug("整型转换失败: value={}, 使用默认值={}", value, defaultValue);
            return defaultValue;
        }
    }

    /**
     * 安全的布尔型转换
     */
    public static Boolean parseBoolean(String value) {
        return parseBoolean(value, null);
    }

    public static Boolean parseBoolean(String value, Boolean defaultValue) {
        if (!hasText(value)) {
            return defaultValue;
        }
        try {
            return Boolean.parseBoolean(value.trim());
        } catch (Exception e) {
            log.debug("布尔型转换失败: value={}, 使用默认值={}", value, defaultValue);
            return defaultValue;
        }
    }

    /**
     * 安全的字符串转换
     */
    public static String toString(Object value) {
        return toString(value, "");
    }

    public static String toString(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return String.valueOf(value);
    }

    public static String safeString(Object value) {
        return toString(value, "");
    }

    /**
     * 检查字符串是否有内容
     */
    public static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * 安全的日期时间解析
     */
    public static LocalDateTime parseDateTime(String value) {
        return parseDateTime(value, null);
    }

    public static LocalDateTime parseDateTime(String value, LocalDateTime defaultValue) {
        if (!hasText(value)) {
            return defaultValue;
        }
        try {
            return LocalDateTime.parse(value.trim(), DATE_TIME_FORMATTER);
        } catch (Exception e) {
            log.debug("日期时间转换失败: value={}, 使用默认值={}", value, defaultValue);
            return defaultValue;
        }
    }

    /**
     * 安全的对象转Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(Object obj) {
        if (obj == null) {
            return new HashMap<>();
        }
        if (obj instanceof Map) {
            try {
                return (Map<String, Object>) obj;
            } catch (ClassCastException e) {
                log.debug("类型转换异常: 无法将对象转换为Map", e);
                return new HashMap<>();
            }
        }
        return new HashMap<>();
    }

    /**
     * 安全的对象转List
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> toList(Object obj) {
        if (obj == null) {
            return new ArrayList<>();
        }
        if (obj instanceof List) {
            try {
                return (List<T>) obj;
            } catch (ClassCastException e) {
                log.debug("类型转换异常: 无法将对象转换为List", e);
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }

    /**
     * JSON序列化
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("JSON序列化失败", e);
            return null;
        }
    }

    /**
     * JSON反序列化
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (!hasText(json)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            log.error("JSON反序列化失败: json={}, class={}", json, clazz.getName(), e);
            return null;
        }
    }

    /**
     * JSON反序列化（复杂类型）
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (!hasText(json)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (Exception e) {
            log.error("JSON反序列化失败: json={}, type={}", json, typeReference.getType(), e);
            return null;
        }
    }
}