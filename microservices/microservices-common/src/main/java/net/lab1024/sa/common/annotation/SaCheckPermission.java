package net.lab1024.sa.common.annotation;

import java.lang.annotation.*;

/**
 * 权限检查注解 - 企业级标准
 * 用于方法级别的权限控制
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-30
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SaCheckPermission {

    /**
     * 权限值
     */
    String value() default "";

    /**
     * 权限值列表
     */
    String[] permission() default {};

    /**
     * 是否需要全部权限
     * true: 需要所有权限都满足
     * false: 只需要一个权限满足即可
     */
    boolean all() default false;

    /**
     * 错误消息
     */
    String message() default "权限不足";
}