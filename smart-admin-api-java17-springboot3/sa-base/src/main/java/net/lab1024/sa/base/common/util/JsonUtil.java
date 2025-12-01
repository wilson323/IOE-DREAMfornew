package net.lab1024.sa.base.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;

import org.springframework.util.StringUtils;

import net.lab1024.sa.base.common.exception.SmartException;

import java.util.List;
import java.util.Map;

/**
 * JSON工具类
 * <p>
 * 基于Jackson实现的统一JSON处理工具类
 * 提供序列化、反序列化、类型转换等常用功能
 *
 * @author SmartAdmin
 * @since 2025-11-25
 */
@Slf4j
public class JsonUtil {

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        // 注册Java 8时间模块
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        // 禁用将日期写为时间戳
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 忽略未知属性
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 忽略空值
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    /**
     * 获取ObjectMapper实例
     *
     * @return ObjectMapper实例
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * 对象转JSON字符串
     *
     * @param obj 对象
     * @return JSON字符串
     */
    public static String toJsonString(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("对象序列化为JSON失败: {}", e.getMessage(), e);
            throw new RuntimeException("JSON序列化失败: " + e.getMessage());
        }
    }

    /**
     * 对象转格式化的JSON字符串
     *
     * @param obj 对象
     * @return 格式化的JSON字符串
     */
    public static String toJsonStringPretty(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("对象序列化为格式化JSON失败: {}", e.getMessage(), e);
            throw new RuntimeException("JSON序列化失败: " + e.getMessage());
        }
    }

    /**
     * JSON字符串转对象
     *
     * @param jsonString JSON字符串
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 对象实例
     */
    public static <T> T parseObject(String jsonString, Class<T> clazz) {
        if (!StringUtils.hasText(jsonString)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(jsonString, clazz);
        } catch (JsonProcessingException e) {
            log.error("JSON反序列化失败: {}, 目标类型: {}", e.getMessage(), clazz.getSimpleName(), e);
            throw new RuntimeException("JSON反序列化失败: " + e.getMessage());
        }
    }

    /**
     * JSON字符串转对象（支持泛型）
     *
     * @param jsonString JSON字符串
     * @param typeReference 类型引用
     * @param <T> 泛型类型
     * @return 对象实例
     */
    public static <T> T parseObject(String jsonString, TypeReference<T> typeReference) {
        if (!StringUtils.hasText(jsonString)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(jsonString, typeReference);
        } catch (JsonProcessingException e) {
            log.error("JSON反序列化失败: {}, 目标类型: {}", e.getMessage(), typeReference.getType(), e);
            throw new RuntimeException("JSON反序列化失败: " + e.getMessage());
        }
    }

    /**
     * JSON字符串转List
     *
     * @param jsonString JSON字符串
     * @param elementClass 元素类型
     * @param <T> 泛型类型
     * @return List实例
     */
    public static <T> List<T> parseList(String jsonString, Class<T> elementClass) {
        if (!StringUtils.hasText(jsonString)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(jsonString,
                OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, elementClass));
        } catch (JsonProcessingException e) {
            log.error("JSON反序列化为List失败: {}, 元素类型: {}", e.getMessage(), elementClass.getSimpleName(), e);
            throw new RuntimeException("JSON反序列化失败: " + e.getMessage());
        }
    }

    /**
     * JSON字符串转Map
     *
     * @param jsonString JSON字符串
     * @return Map实例
     */
    public static Map<String, Object> parseMap(String jsonString) {
        if (!StringUtils.hasText(jsonString)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("JSON反序列化为Map失败: {}", e.getMessage(), e);
            throw new RuntimeException("JSON反序列化失败: " + e.getMessage());
        }
    }

    /**
     * 对象转换
     *
     * @param source 源对象
     * @param targetClass 目标类型
     * @param <T> 泛型类型
     * @return 转换后的对象
     */
    public static <T> T convertValue(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.convertValue(source, targetClass);
        } catch (IllegalArgumentException e) {
            log.error("对象转换失败: {}, 源类型: {}, 目标类型: {}",
                e.getMessage(), source.getClass().getSimpleName(), targetClass.getSimpleName(), e);
            throw new RuntimeException("对象转换失败: " + e.getMessage());
        }
    }

    /**
     * 解析JSON节点
     *
     * @param jsonString JSON字符串
     * @return JsonNode实例
     */
    public static JsonNode parseTree(String jsonString) {
        if (!StringUtils.hasText(jsonString)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readTree(jsonString);
        } catch (JsonProcessingException e) {
            log.error("JSON解析失败: {}", e.getMessage(), e);
            throw new RuntimeException("JSON解析失败: " + e.getMessage());
        }
    }

    /**
     * 验证JSON字符串格式
     *
     * @param jsonString JSON字符串
     * @return 是否为有效的JSON格式
     */
    public static boolean isValidJson(String jsonString) {
        if (!StringUtils.hasText(jsonString)) {
            return false;
        }
        try {
            OBJECT_MAPPER.readTree(jsonString);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /**
     * 安全的JSON序列化（不抛出异常）
     *
     * @param obj 对象
     * @return JSON字符串，失败时返回null
     */
    public static String safeToJsonString(Object obj) {
        try {
            return toJsonString(obj);
        } catch (Exception e) {
            log.warn("安全JSON序列化失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 安全的JSON反序列化（不抛出异常）
     *
     * @param jsonString JSON字符串
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 对象实例，失败时返回null
     */
    public static <T> T safeParseObject(String jsonString, Class<T> clazz) {
        try {
            return parseObject(jsonString, clazz);
        } catch (Exception e) {
            log.warn("安全JSON反序列化失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 将对象序列化为JSON字符串（便捷方法）
     *
     * @param obj 要序列化的对象
     * @return JSON字符串
     */
    public static String toJson(Object obj) {
        return toJsonString(obj);
    }

    /**
     * 将JSON字符串反序列化为指定类型对象（便捷方法）
     *
     * @param jsonString JSON字符串
     * @param typeReference 类型引用
     * @param <T> 泛型类型
     * @return 反序列化后的对象
     */
    public static <T> T fromJson(String jsonString, TypeReference<T> typeReference) {
        return parseObject(jsonString, typeReference);
    }
}