package net.lab1024.sa.common.permission.annotation;

import java.lang.annotation.*;

/**
 * 字段级权限控制注解
 * <p>
 * 功能：控制对实体对象特定字段的访问权限
 * 使用场景：
 * 1. 敏感字段隐藏（如手机号、身份证号）
 * 2. 数据脱敏显示（如邮箱、地址）
 * 3. 角色字段限制（如仅管理员可见工资字段）
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FieldPermission {

    /**
     * 字段名称
     * <p>指定需要权限控制的字段名</p>
     */
    String field() default "";

    /**
     * 权限代码
     * <p>访问该字段所需的权限代码，支持多个权限（OR关系）</p>
     */
    String[] value() default {};

    /**
     * 权限角色
     * <p>访问该字段所需的角色代码，支持多个角色（OR关系）</p>
     */
    String[] roles() default {};

    /**
     * 脱敏策略
     * <p>当用户无权限时，对该字段执行的脱敏策略</p>
     */
    MaskStrategy maskStrategy() default MaskStrategy.HIDDEN;

    /**
     * 脱敏策略枚举
     */
    enum MaskStrategy {
        /**
         * 完全隐藏 - 返回null
         */
        HIDDEN,

        /**
         * 手机号脱敏 - 显示前3后4位（如：138****1234）
         */
        PHONE,

        /**
         * 邮箱脱敏 - 显示前2后@域名（如：ab****@example.com）
         */
        EMAIL,

        /**
         * 身份证号脱敏 - 显示前6后4位（如：110101********1234）
         */
        ID_CARD,

        /**
         * 银行卡号脱敏 - 显示前4后4位（如：6222********1234）
         */
        BANK_CARD,

        /**
         * 地址脱敏 - 显示前6个字符（如：北京市朝阳****）
         */
        ADDRESS,

        /**
         * 姓名脱敏 - 显示姓氏（如：张**）
         */
        NAME,

        /**
         * 部分隐藏 - 显示前半部分（如：显示前50%）
         */
        PARTIAL,

        /**
         * 替换为固定文本（如："***"）
         */
        REPLACE
    }

    /**
     * 替换文本
     * <p>当maskStrategy=REPLACE时，使用的替换文本</p>
     */
    String replaceText() default "***";

    /**
     * 部分显示比例
     * <p>当maskStrategy=PARTIAL时，显示前N%（0-100）</p>
     */
    int partialPercent() default 50;

    /**
     * 描述信息
     */
    String description() default "";
}
