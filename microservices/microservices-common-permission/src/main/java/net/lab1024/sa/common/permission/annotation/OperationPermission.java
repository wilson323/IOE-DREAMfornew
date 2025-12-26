package net.lab1024.sa.common.permission.annotation;

import java.lang.annotation.*;

/**
 * 操作级权限控制注解
 * <p>
 * 功能：控制用户对特定操作的执行权限
 * 使用场景：
 * 1. 敏感操作控制（如删除、批量删除）
 * 2. 审批操作控制（如通过、拒绝）
 * 3. 导入导出控制（如导出Excel、导出PDF）
 * 4. 数据修改控制（如修改状态、修改权限）
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationPermission {

    /**
     * 操作类型
     */
    OperationType value() default OperationType.EXECUTE;

    /**
     * 权限代码
     * <p>执行该操作所需的权限代码，支持多个权限（OR关系）</p>
     */
    String[] permission() default {};

    /**
     * 权限角色
     * <p>执行该操作所需的角色代码，支持多个角色（OR关系）</p>
     */
    String[] roles() default {};

    /**
     * 资源类型
     * <p>操作的资源类型，如：user、device、access_record</p>
     */
    String resourceType() default "";

    /**
     * 是否需要二次确认
     * <p>true=需要用户确认才能执行操作，false=直接执行</p>
     */
    boolean requireConfirmation() default false;

    /**
     * 确认提示信息
     * <p>当requireConfirmation=true时，显示的确认提示</p>
     */
    String confirmMessage() default "确定要执行此操作吗？";

    /**
     * 是否记录操作日志
     * <p>true=记录操作日志，false=不记录</p>
     */
    boolean logOperation() default true;

    /**
     * 操作类型枚举
     */
    enum OperationType {
        /**
         * 查询操作
         */
        QUERY,

        /**
         * 新增操作
         */
        CREATE,

        /**
         * 更新操作
         */
        UPDATE,

        /**
         * 删除操作
         */
        DELETE,

        /**
         * 批量删除操作
         */
        BATCH_DELETE,

        /**
         * 导入操作
         */
        IMPORT,

        /**
         * 导出操作
         */
        EXPORT,

        /**
         * 审批通过
         */
        APPROVE,

        /**
         * 审批拒绝
         */
        REJECT,

        /**
         * 启用操作
         */
        ENABLE,

        /**
         * 禁用操作
         */
        DISABLE,

        /**
         * 重置操作
         */
        RESET,

        /**
         * 同步操作
         */
        SYNC,

        /**
         * 发送操作
         */
        SEND,

        /**
         * 执行操作（通用）
         */
        EXECUTE
    }

    /**
     * 描述信息
     */
    String description() default "";
}
