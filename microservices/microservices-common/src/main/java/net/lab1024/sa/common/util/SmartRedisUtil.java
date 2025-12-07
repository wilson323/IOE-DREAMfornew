package net.lab1024.sa.common.util;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;

/**
 * SmartRedis工具类（增强版Redis工具类）
 * <p>
 * 提供Redis操作的静态方法封装
 * 严格遵循CLAUDE.md规范：
 * - 工具类在microservices-common中
 * - 提供静态方法
 * - 统一的Redis操作接口
 * </p>
 * <p>
 * 注意：此类需要在使用前通过setRedisTemplate方法设置RedisTemplate实例
 * 建议在配置类中初始化
 * </p>
 * <p>
 * 与RedisUtil的关系：
 * - RedisUtil: 基础Redis工具类
 * - SmartRedisUtil: 增强版Redis工具类（提供更多便捷方法）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@SuppressWarnings("null")
public class SmartRedisUtil {

    /**
     * Redis模板（需要在使用前设置）
     */
    private static RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置Redis模板
     * <p>
     * 在配置类中调用此方法初始化RedisTemplate
     * </p>
     *
     * @param template Redis模板
     */
    public static void setRedisTemplate(RedisTemplate<String, Object> template) {
        redisTemplate = template;
        // 同时设置RedisUtil的模板（保持兼容）
        RedisUtil.setRedisTemplate(template);
    }

    /**
     * 获取Redis模板
     *
     * @return Redis模板
     */
    public static RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }

    /**
     * 获取值（带默认值）
     *
     * @param key 键
     * @param defaultValue 默认值
     * @return 值
     */
    public static Object getOrDefault(String key, Object defaultValue) {
        if (redisTemplate == null) {
            throw new IllegalStateException("RedisTemplate未初始化，请先调用setRedisTemplate方法");
        }
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * 根据模式获取键集合
     *
     * @param pattern 模式
     * @return 键集合
     */
    public static Set<String> keys(String pattern) {
        if (redisTemplate == null) {
            throw new IllegalStateException("RedisTemplate未初始化，请先调用setRedisTemplate方法");
        }
        return redisTemplate.keys(pattern);
    }

    /**
     * 批量删除键
     *
     * @param keys 键集合
     * @return 删除的数量
     */
    public static Long delete(Set<String> keys) {
        if (redisTemplate == null) {
            throw new IllegalStateException("RedisTemplate未初始化，请先调用setRedisTemplate方法");
        }
        if (keys == null || keys.isEmpty()) {
            return 0L;
        }
        return redisTemplate.delete(keys);
    }

    /**
     * 设置值（带过期时间，单位：秒）
     *
     * @param key 键
     * @param value 值
     * @param timeoutSeconds 过期时间（秒）
     */
    public static void set(String key, Object value, long timeoutSeconds) {
        if (redisTemplate == null) {
            throw new IllegalStateException("RedisTemplate未初始化，请先调用setRedisTemplate方法");
        }
        redisTemplate.opsForValue().set(key, value, timeoutSeconds, TimeUnit.SECONDS);
    }
}
