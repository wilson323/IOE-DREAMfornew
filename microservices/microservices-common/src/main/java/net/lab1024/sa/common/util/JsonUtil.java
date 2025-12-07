package net.lab1024.sa.common.util;

import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * JSON工具类
 * <p>
 * 提供统一的ObjectMapper实例，避免重复创建
 * ObjectMapper是线程安全的，设计用于复用
 * 严格遵循CLAUDE.md规范：
 * - 使用静态常量复用ObjectMapper实例
 * - 提供便捷的JSON序列化/反序列化方法
 * - 支持Java 8时间类型
 * </p>
 * <p>
 * 性能优化：
 * - ObjectMapper是线程安全的，可以安全地在多线程环境中共享
 * - 避免每次调用都创建新实例，减少99%+的对象创建
 * - 减少GC压力，提升系统性能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public class JsonUtil {

    /**
     * 统一的ObjectMapper实例（线程安全，可复用）
     * <p>
     * ObjectMapper是线程安全的，设计用于复用
     * 配置了Java 8时间模块，支持LocalDateTime等类型
     * 禁用了将日期写入为时间戳的默认行为
     * </p>
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /**
     * 私有构造函数，防止实例化
     */
    private JsonUtil() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 获取ObjectMapper实例
     * <p>
     * 返回统一的ObjectMapper实例，所有调用都复用同一个实例
     * </p>
     *
     * @return ObjectMapper实例
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * 将对象转换为JSON字符串
     * <p>
     * 使用统一的ObjectMapper实例进行序列化
     * </p>
     *
     * @param obj 待序列化的对象
     * @return JSON字符串
     * @throws RuntimeException 序列化失败时抛出
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("JSON序列化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将JSON字符串转换为对象
     * <p>
     * 使用统一的ObjectMapper实例进行反序列化
     * </p>
     *
     * @param json JSON字符串
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 反序列化后的对象
     * @throws RuntimeException 反序列化失败时抛出
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("JSON反序列化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将JSON字符串转换为Map
     * <p>
     * 使用TypeReference确保类型安全
     * </p>
     *
     * @param json JSON字符串
     * @return Map对象
     * @throws RuntimeException 反序列化失败时抛出
     */
    public static Map<String, Object> toMap(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException("JSON转Map失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将对象转换为Map
     * <p>
     * 先序列化为JSON，再反序列化为Map
     * </p>
     *
     * @param obj 待转换的对象
     * @return Map对象
     * @throws RuntimeException 转换失败时抛出
     */
    public static Map<String, Object> toMap(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            String json = OBJECT_MAPPER.writeValueAsString(obj);
            return OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException("对象转Map失败: " + e.getMessage(), e);
        }
    }

    /**
     * 使用TypeReference进行类型安全的JSON反序列化
     * <p>
     * 适用于复杂泛型类型，如List&lt;UserEntity&gt;、Map&lt;String, Object&gt;等
     * </p>
     *
     * @param json JSON字符串
     * @param typeReference 类型引用
     * @param <T> 泛型类型
     * @return 反序列化后的对象
     * @throws RuntimeException 反序列化失败时抛出
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (Exception e) {
            throw new RuntimeException("JSON反序列化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查JSON字符串是否有效
     *
     * @param json JSON字符串
     * @return true表示有效，false表示无效
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        try {
            OBJECT_MAPPER.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
