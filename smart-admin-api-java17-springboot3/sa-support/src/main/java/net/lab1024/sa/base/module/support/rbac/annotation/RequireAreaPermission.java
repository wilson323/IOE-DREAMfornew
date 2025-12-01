package net.lab1024.sa.base.module.support.rbac.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 区域权限验证注解
 * <p>
 * 用于Controller方法，标识需要区域权限验证
 * 支持灵活的区域ID提取方式和权限控制
 *
 * @author SmartAdmin Team
 * @date 2025/11/25
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAreaPermission {

    /**
     * 区域ID参数名称
     * 从方法参数中根据参数名称提取区域ID
     */
    String paramName() default "";

    /**
     * 区域ID参数索引
     * 从方法参数中根据索引提取区域ID（0-based）
     */
    int paramIndex() default -1;

    /**
     * 区域ID字段路径
     * 从复杂对象中根据字段路径提取区域ID，支持嵌套对象访问
     * 例如："areaId"、"entity.areaId"、"request.data.areaId"
     */
    String fieldPath() default "";

    /**
     * 是否必需
     * true: 必须提供区域ID且有权限
     * false: 可以不提供区域ID，如果提供了则需要验证权限
     */
    boolean required() default true;

    /**
     * 权限检查模式
     */
    PermissionMode mode() default PermissionMode.CHECK;

    /**
     * 权限模式枚举
     */
    enum PermissionMode {
        /**
         * 检查模式：检查用户是否有权限
         */
        CHECK,

        /**
         * 过滤模式：过滤出用户有权限的数据（用于列表查询）
         */
        FILTER,

        /**
         * 授权模式：为用户授权区域权限（用于权限分配）
         */
        GRANT,

        /**
         * 撤销模式：撤销用户区域权限（用于权限回收）
         */
        REVOKE
    }

    /**
     * 错误消息
     * 权限验证失败时的自定义错误消息
     */
    String message() default "区域权限不足";

    /**
     * 是否检查父区域权限
     * true: 检查当前区域及父区域权限
     * false: 只检查当前区域权限
     */
    boolean checkParent() default false;

    /**
     * 是否检查子区域权限
     * true: 检查当前区域及子区域权限
     * false: 只检查当前区域权限
     */
    boolean checkChildren() default false;

    /**
     * 超级管理员豁免
     * true: 超级管理员跳过权限检查
     * false: 超级管理员也需要检查权限
     */
    boolean adminBypass() default true;
}