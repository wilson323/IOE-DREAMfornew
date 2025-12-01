package net.lab1024.sa.base.authz.rac.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 资源权限配置注解
 * 用于详细配置资源权限参数
 *
 * @author IOE-DREAM System
 * @version 1.0
 * @since 2025-11-18
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ResourcePermission {

    /**
     * 权限检查模式
     */
    CheckMode checkMode() default CheckMode.ANY;

    /**
     * 权限级别
     */
    Level level() default Level.NORMAL;

    /**
     * 超级管理员豁免
     */
    boolean adminBypass() default true;

    /**
     * 权限检查模式
     */
    enum CheckMode {
        /**
         * 满足任意权限即可
         */
        ANY,

        /**
         * 必须满足所有权限
         */
        ALL,

        /**
         * 自定义检查逻辑
         */
        CUSTOM
    }

    /**
     * 权限级别
     */
    enum Level {
        /**
         * 普通级别
         */
        NORMAL,

        /**
         * 重要级别
         */
        IMPORTANT,

        /**
         * 关键级别
         */
        CRITICAL
    }
}