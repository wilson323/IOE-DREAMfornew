package net.lab1024.sa.common.permission.annotation;

import java.lang.annotation.*;

/**
 * API级权限控制注解
 * <p>
 * 功能：控制对REST API接口的访问权限
 * 使用场景：
 * 1. Controller类或方法级别的权限控制
 * 2. 支持基于角色和权限代码的双重验证
 * 3. 支持IP白名单限制
 * 4. 支持时间窗口限制
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiPermission {

    /**
     * 权限代码
     * <p>访问该API所需的权限代码，支持多个权限（OR关系）</p>
     */
    String[] value() default {};

    /**
     * 权限角色
     * <p>访问该API所需的角色代码，支持多个角色（OR关系）</p>
     */
    String[] roles() default {};

    /**
     * API模块
     * <p>API所属模块，如：user、device、access</p>
     */
    String module() default "";

    /**
     * API操作
     * <p>API操作类型，如：query、create、update、delete</p>
     */
    String operation() default "";

    /**
     * IP白名单
     * <p>允许访问该API的IP地址列表，支持通配符（如：192.168.1.*）</p>
     */
    String[] ipWhitelist() default {};

    /**
     * 时间窗口限制
     * <p>允许访问该API的时间窗口，格式：HH:mm-HH:mm（如：09:00-18:00）</p>
     */
    String timeWindow() default "";

    /**
     * 是否需要认证
     * <p>true=需要用户认证，false=公开接口</p>
     */
    boolean requireAuth() default true;

    /**
     * 是否启用频率限制
     * <p>true=启用接口限流，false=不限流</p>
     */
    boolean rateLimit() default false;

    /**
     * 频率限制（每分钟请求数）
     * <p>当rateLimit=true时生效，默认60次/分钟</p>
     */
    int rateLimitPerMinute() default 60;

    /**
     * 描述信息
     */
    String description() default "";
}
