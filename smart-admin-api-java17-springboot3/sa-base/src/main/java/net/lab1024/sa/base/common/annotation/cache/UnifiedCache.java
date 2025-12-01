package net.lab1024.sa.base.common.annotation.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import net.lab1024.sa.base.common.cache.CacheNamespace;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 统一缓存注解
 * <p>
 * 严格遵循repowiki注解规范：
 * - 简化缓存使用
 * - 统一缓存策略
 * - 支持多级缓存
 * - 自动失效和刷新
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface UnifiedCache {

    /**
     * 缓存命名空间
     */
    CacheNamespace namespace();

    /**
     * 缓存键表达式
     * 支持SpEL表达式，如：'#userId', '#user.id + ":" + #user.name'
     */
    @AliasFor("key")
    String value() default "";

    /**
     * 缓存键表达式
     */
    @AliasFor("value")
    String key() default "";

    /**
     * 缓存过期时间（单位：秒）
     * 如果不设置，使用命名空间的默认过期时间
     */
    long ttl() default -1;

    /**
     * 缓存过期时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 是否缓存null值
     */
    boolean cacheNull() default false;

    /**
     * 条件表达式，满足条件时才缓存
     * 支持SpEL表达式
     */
    String condition() default "";

    /**
     * 排除条件表达式，满足条件时不缓存
     * 支持SpEL表达式
     */
    String unless() default "";

    /**
     * 是否异步执行
     */
    boolean async() default false;

    /**
     * 是否启用缓存穿透保护
     */
    boolean penetrationProtection() default true;

    /**
     * 缓存穿透保护超时时间（毫秒）
     */
    long protectionTimeout() default 5000;

    /**
     * 缓存描述
     */
    String description() default "";

    /**
     * 缓存分组
     */
    String group() default "";
}