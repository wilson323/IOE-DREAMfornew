package net.lab1024.sa.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 资源权限要求注解
 * 严格遵循repowiki规范：用于方法级别的资源访问控制
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireResource {

    /**
     * 资源标识
     *
     * @return 资源标识符
     */
    String resource();

    /**
     * 操作类型
     *
     * @return 操作类型：READ, WRITE, DELETE, EXECUTE
     */
    String action() default "READ";

    /**
     * 数据范围
     *
     * @return 数据范围：ALL, DEPT, SELF
     */
    String dataScope() default "ALL";

    /**
     * 错误消息
     *
     * @return 权限不足时的提示消息
     */
    String message() default "您没有权限执行此操作";
}