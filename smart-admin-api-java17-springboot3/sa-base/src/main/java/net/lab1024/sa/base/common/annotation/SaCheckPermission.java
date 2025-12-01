package net.lab1024.sa.base.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限检查注解（临时实现）
 *
 * @author SmartAdmin
 * @since 2025-11-13
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SaCheckPermission {
    String value() default "";
}