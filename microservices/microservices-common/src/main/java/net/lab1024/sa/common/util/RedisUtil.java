package net.lab1024.sa.common.util;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;

/**
 * Redis工具类
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
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@SuppressWarnings("null")
public class RedisUtil {

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
     * 获取值
     *
     * @param key 键
     * @return 值
     */
    public static Object get(String key) {
        if (redisTemplate == null) {
            throw new IllegalStateException("RedisTemplate未初始化，请先调用setRedisTemplate方法");
        }
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 设置值
     *
     * @param key 键
     * @param value 值
     */
    public static void set(String key, Object value) {
        if (redisTemplate == null) {
            throw new IllegalStateException("RedisTemplate未初始化，请先调用setRedisTemplate方法");
        }
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置值（带过期时间）
     *
     * @param key 键
     * @param value 值
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    public static void set(String key, Object value, long timeout, TimeUnit unit) {
        if (redisTemplate == null) {
            throw new IllegalStateException("RedisTemplate未初始化，请先调用setRedisTemplate方法");
        }
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 删除键
     *
     * @param key 键
     * @return 是否成功
     */
    public static Boolean delete(String key) {
        if (redisTemplate == null) {
            throw new IllegalStateException("RedisTemplate未初始化，请先调用setRedisTemplate方法");
        }
        return redisTemplate.delete(key);
    }

    /**
     * 判断键是否存在
     *
     * @param key 键
     * @return 是否存在
     */
    public static Boolean hasKey(String key) {
        if (redisTemplate == null) {
            throw new IllegalStateException("RedisTemplate未初始化，请先调用setRedisTemplate方法");
        }
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置过期时间
     *
     * @param key 键
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return 是否成功
     */
    public static Boolean expire(String key, long timeout, TimeUnit unit) {
        if (redisTemplate == null) {
            throw new IllegalStateException("RedisTemplate未初始化，请先调用setRedisTemplate方法");
        }
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 递增
     *
     * @param key 键
     * @return 递增后的值
     */
    public static Long increment(String key) {
        if (redisTemplate == null) {
            throw new IllegalStateException("RedisTemplate未初始化，请先调用setRedisTemplate方法");
        }
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * 递增（指定增量）
     *
     * @param key 键
     * @param delta 增量
     * @return 递增后的值
     */
    public static Long increment(String key, long delta) {
        if (redisTemplate == null) {
            throw new IllegalStateException("RedisTemplate未初始化，请先调用setRedisTemplate方法");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减
     *
     * @param key 键
     * @return 递减后的值
     */
    public static Long decrement(String key) {
        if (redisTemplate == null) {
            throw new IllegalStateException("RedisTemplate未初始化，请先调用setRedisTemplate方法");
        }
        return redisTemplate.opsForValue().decrement(key);
    }
}
