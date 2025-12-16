package net.lab1024.sa.common.permission.annotation;

import java.lang.annotation.*;

/**
 * 权限验证注解
 * <p>
 * 企业级声明式权限验证注解，支持：
 * - 权限验证
 * - 角色验证
 * - 数据权限验证
 * - 复合条件验证
 * - 逻辑操作符
 * - 缓存控制
 * - 审计配置
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface PermissionCheck {

    /**
     * 权限标识
     * <p>
     * 支持多权限配置，多个权限之间用逗号分隔
     * 例如：{"USER_VIEW", "USER_EDIT"} 或 "USER_VIEW,USER_EDIT"
     * </p>
     */
    String[] value() default {};

    /**
     * 角色标识
     * <p>
     * 支持多角色配置，多个角色之间用逗号分隔
     * 例如：{"ADMIN", "MANAGER"} 或 "ADMIN,MANAGER"
     * </p>
     */
    String[] roles() default {};

    /**
     * 权限验证模式
     * <p>
     * ANY - 任一权限满足即可
     * ALL - 所有权限都必须满足
     * CUSTOM - 自定义验证逻辑
     * </p>
     */
    PermissionMode mode() default PermissionMode.ANY;

    /**
     * 逻辑操作符
     * <p>
     * 用于权限和角色之间的逻辑组合
     * AND - 权限和角色都必须满足
     * OR - 权限或角色满足其一即可
     * </p>
     */
    LogicOperator operator() default LogicOperator.OR;

    /**
     * 数据权限类型
     * <p>
     * 用于数据权限验证
     * DEPARTMENT - 部门数据权限
     * AREA - 区域数据权限
     * DEVICE - 设备数据权限
     * CUSTOM - 自定义数据权限
     * </p>
     */
    DataScopeType dataScope() default DataScopeType.NONE;

    /**
     * 数据权限参数
     * <p>
     * 指定方法参数名，用于数据权限验证
     * 例如：当dataScope = DEPARTMENT时，可以是 "departmentId"
     * </p>
     */
    String dataScopeParam() default "";

    /**
     * 区域权限参数
     * <p>
     * 指定方法参数名，用于区域权限验证
     * 例如：areaId
     * </p>
     */
    String areaParam() default "";

    /**
     * 设备权限参数
     * <p>
     * 指定方法参数名，用于设备权限验证
     * 例如：deviceId
     * </p>
     */
    String deviceParam() default "";

    /**
     * 模块权限参数
     * <p>
     * 指定方法参数名，用于模块权限验证
     * 例如：moduleCode
     * </p>
     */
    String moduleParam() default "";

    /**
     * 资源标识
     * <p>
     * 可选的资源标识，用于细粒度权限控制
     * 支持SpEL表达式：#{#resourceId}
     * </p>
     */
    String resource() default "";

    /**
     * 用户ID参数
     * <p>
     * 指定方法参数名作为用户ID
     * 默认从当前登录用户获取
     * </p>
     */
    String userParam() default "";

    /**
     * 缓存控制
     * <p>
     * ENABLE - 启用缓存
     * DISABLE - 禁用缓存
     * CUSTOM - 自定义缓存策略
     * </p>
     */
    CacheControl cache() default CacheControl.ENABLE;

    /**
     * 缓存时间（秒）
     * <p>
     * 权限验证结果的缓存时间
     * 默认使用系统配置
     * </p>
     */
    int cacheTime() default -1;

    /**
     * 审计控制
     * <p>
     * ENABLE - 启用审计
     * DISABLE - 禁用审计
     * CUSTOM - 自定义审计策略
     * </p>
     */
    AuditControl audit() default AuditControl.ENABLE;

    /**
     * 失败处理策略
     * <p>
     * EXCEPTION - 抛出异常
     * RETURN_NULL - 返回null
     * RETURN_DEFAULT - 返回默认值
     * CUSTOM - 自定义处理
     * </p>
     */
    FailureStrategy failureStrategy() default FailureStrategy.EXCEPTION;

    /**
     * 异常类型
     * <p>
     * 权限验证失败时抛出的异常类型
     * </p>
     */
    Class<? extends Exception> exceptionType() default PermissionDeniedException.class;

    /**
     * 异常消息
     * <p>
     * 权限验证失败时的错误消息
     * 支持国际化消息键
     * </p>
     */
    String message() default "权限不足";

    /**
     * 验证前置处理
     * <p>
     * 权限验证前的预处理逻辑
     * 指定处理类的方法名
     * </p>
     */
    String beforeHandler() default "";

    /**
     * 验证后置处理
     * <p>
     * 权限验证后的后处理逻辑
     * 指定处理类的方法名
     * </p>
     */
    String afterHandler() default "";

    /**
     * 描述信息
     * <p>
     * 权限验证的描述信息，用于文档和调试
     * </p>
     */
    String description() default "";

    /**
     * 权限验证模式
     */
    enum PermissionMode {
        ANY,    // 任一权限满足
        ALL,    // 所有权限满足
        CUSTOM  // 自定义验证
    }

    /**
     * 逻辑操作符
     */
    enum LogicOperator {
        AND,   // 逻辑与
        OR     // 逻辑或
    }

    /**
     * 数据权限类型
     */
    enum DataScopeType {
        NONE,        // 无数据权限
        DEPARTMENT,  // 部门数据权限
        AREA,        // 区域数据权限
        DEVICE,      // 设备数据权限
        CUSTOM       // 自定义数据权限
    }

    /**
     * 缓存控制
     */
    enum CacheControl {
        ENABLE,   // 启用缓存
        DISABLE,  // 禁用缓存
        CUSTOM    // 自定义缓存
    }

    /**
     * 审计控制
     */
    enum AuditControl {
        ENABLE,   // 启用审计
        DISABLE,  // 禁用审计
        CUSTOM    // 自定义审计
    }

    /**
     * 失败处理策略
     */
    enum FailureStrategy {
        EXCEPTION,      // 抛出异常
        RETURN_NULL,    // 返回null
        RETURN_DEFAULT, // 返回默认值
        CUSTOM          // 自定义处理
    }

    /**
     * 权限拒绝异常
     */
    class PermissionDeniedException extends RuntimeException {
        public PermissionDeniedException(String message) {
            super(message);
        }

        public PermissionDeniedException(String message, Throwable cause) {
            super(message, cause);
        }

        public PermissionDeniedException(String code, String message) {
            super(message);
            this.code = code;
        }

        private String code;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }
}