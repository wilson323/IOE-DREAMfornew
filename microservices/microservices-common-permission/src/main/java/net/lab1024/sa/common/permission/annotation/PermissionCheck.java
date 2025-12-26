package net.lab1024.sa.common.permission.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限检查注解
 * <p>
 * 用于标记需要权限验证的方法或类
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PermissionCheck {

    /**
     * 权限标识（单个或多个）
     *
     * @return 权限标识数组
     */
    String[] value() default {};

    /**
     * 权限描述
     *
     * @return 权限描述
     */
    String description() default "";
}

