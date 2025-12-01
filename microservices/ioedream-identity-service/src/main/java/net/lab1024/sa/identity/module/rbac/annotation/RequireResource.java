package net.lab1024.sa.identity.module.rbac.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 资源权限要求注解
 * <p>
 * 用于标记需要权限控制的方法，统一声明资源编码、动作和数据域范围
 * </p>
 *
 * @author SmartAdmin Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireResource {

    /**
     * 资源编码（必须）
     * 对应 t_rbac_resource.resource_code
     */
    String code();

    /**
     * 操作动作（可选）
     * 默认为 READ，可选值：READ|WRITE|DELETE|APPROVE|*
     */
    String action() default "READ";

    /**
     * 数据域范围（可选）
     * 默认为 AREA，可选值：AREA|DEPT|SELF|CUSTOM|ALL
     */
    String scope() default "AREA";

    /**
     * 权限描述（可选）
     */
    String description() default "";

    /**
     * 是否记录审计日志（可选）
     */
    boolean audit() default true;

    /**
     * 是否忽略权限检查（可选，用于调试）
     */
    boolean ignore() default false;

    /**
     * 权限检查失败时的错误码（可选）
     */
    String errorCode() default "AUTH_DENIED";
}