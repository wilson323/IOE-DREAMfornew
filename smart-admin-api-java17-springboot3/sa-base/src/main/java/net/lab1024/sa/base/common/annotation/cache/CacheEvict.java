package net.lab1024.sa.base.common.annotation.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 缓存清除注解
 * <p>
 * 用于标记需要清除缓存的方法，支持:
 * - 单个缓存清除
 * - 批量缓存清除
 * - 模式匹配清除
 * <p>
 * 使用示例:
 * <pre>
 * &#64;CacheEvict(key = "user:")
 * public void updateUser(User user) {
 *     userDao.updateById(user);
 * }
 *
 * &#64;CacheEvict(key = "user:", pattern = "*")
 * public void clearAllUserCache() {
 *     // 清除所有用户缓存
 * }
 * </pre>
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheEvict {

    /**
     * 缓存键前缀
     *
     * @return 键前缀，如 "user:", "device:"
     */
    String key() default "";

    /**
     * 模式匹配
     * 用于批量清除缓存，如 "*:list", ":detail*"
     *
     * @return 匹配模式
     */
    String pattern() default "";

    /**
     * 是否启用缓存清除
     *
     * @return true启用，false禁用
     */
    boolean enabled() default true;

    /**
     * 是否全部清除
     *
     * @return true清除所有缓存
     */
    boolean allEntries() default false;

    /**
     * 条件表达式
     * 只有满足条件时才清除缓存
     *
     * @return SpEL表达式
     */
    String condition() default "";

    /**
     * 排除条件表达式
     * 满足条件时不清除缓存
     *
     * @return SpEL表达式
     */
    String unless() default "";
}