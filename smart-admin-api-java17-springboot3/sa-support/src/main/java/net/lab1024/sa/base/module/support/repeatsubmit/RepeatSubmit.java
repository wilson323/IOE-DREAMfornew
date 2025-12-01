package net.lab1024.sa.base.module.support.repeatsubmit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 防重复提交注解
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-27
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RepeatSubmit {

    /**
     * 是否启用
     */
    boolean enable() default true;

    /**
     * 锁定时间（秒）
     */
    long lockTime() default 10;
}