package net.lab1024.sa.base.common.annotation.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 缓存注解
 * <p>
 * 用于标记需要缓存的方法，支持多级缓存:
 * - L1: Caffeine本地缓存
 * - L2: Redis分布式缓存
 * <p>
 * 使用示例:
 * <pre>
 * &#64;Cacheable(key = "user:", expire = 30)
 * public User getUserById(Long userId) {
 *     return userDao.selectById(userId);
 * }
 * </pre>
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cacheable {

    /**
     * 缓存键前缀
     *
     * @return 键前缀，如 "user:", "device:"
     */
    String key() default "";

    /**
     * 缓存过期时间(分钟)
     *
     * @return 过期时间，默认30分钟
     */
    int expire() default 30;

    /**
     * 是否启用缓存
     *
     * @return true启用，false禁用
     */
    boolean enabled() default true;

    /**
     * 条件表达式
     * 只有满足条件时才缓存
     *
     * @return SpEL表达式
     */
    String condition() default "";

    /**
     * 排除条件表达式
     * 满足条件时不缓存
     *
     * @return SpEL表达式
     */
    String unless() default "";
}