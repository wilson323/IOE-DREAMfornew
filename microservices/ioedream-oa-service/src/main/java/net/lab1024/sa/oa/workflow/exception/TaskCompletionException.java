package net.lab1024.sa.oa.workflow.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 任务完成异常
 * <p>
 * 专门用于任务完成过程中的异常处理
 * 提供详细的任务信息和处理上下文
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskCompletionException extends RuntimeException {

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 处理人ID
     */
    private Long assigneeId;

    /**
     * 处理结果
     */
    private String outcome;

    /**
     * 处理意见
     */
    private String comment;

    /**
     * 任务变量
     */
    private Map<String, Object> variables;

    /**
     * 表单数据
     */
    private Object formData;

    /**
     * 失败的具体步骤
     */
    private String failedStep;

    // ==================== 失败步骤常量 ====================

    public static final String STEP_VALIDATION = "VALIDATION"; // 参数验证步骤
    public static final String STEP_PERMISSION_CHECK = "PERMISSION_CHECK"; // 权限检查步骤
    public static final String STEP_TASK_STATUS_CHECK = "TASK_STATUS_CHECK"; // 任务状态检查步骤
    public static final String STEP_VARIABLE_VALIDATION = "VARIABLE_VALIDATION"; // 变量验证步骤
    public static final String STEP_FORM_VALIDATION = "FORM_VALIDATION"; // 表单验证步骤
    public static final String STEP_FLOWABLE_COMPLETION = "FLOWABLE_COMPLETION"; // Flowable引擎完成步骤
    public static final String STEP_DATABASE_UPDATE = "DATABASE_UPDATE"; // 数据库更新步骤
    public static final String STEP_NEXT_TASK_CREATION = "NEXT_TASK_CREATION"; // 下一任务创建步骤
    public static final String STEP_NOTIFICATION = "NOTIFICATION"; // 通知发送步骤

    // ==================== 构造方法 ====================

    public TaskCompletionException(Long taskId, String failedStep, String message, Throwable cause) {
        super(message, cause);
        this.taskId = taskId;
        this.failedStep = failedStep;
    }

    public TaskCompletionException(Long taskId, String taskName, Long assigneeId, String failedStep, String message, Throwable cause) {
        super(message, cause);
        this.taskId = taskId;
        this.taskName = taskName;
        this.assigneeId = assigneeId;
        this.failedStep = failedStep;
    }

    public TaskCompletionException(Long taskId, String taskName, Long assigneeId, String outcome, String comment, String failedStep, String message, Throwable cause) {
        super(message, cause);
        this.taskId = taskId;
        this.taskName = taskName;
        this.assigneeId = assigneeId;
        this.outcome = outcome;
        this.comment = comment;
        this.failedStep = failedStep;
    }

    public TaskCompletionException(Long taskId, String taskName, Long assigneeId, String outcome, String comment, Map<String, Object> variables, Object formData, String failedStep, String message, Throwable cause) {
        super(message, cause);
        this.taskId = taskId;
        this.taskName = taskName;
        this.assigneeId = assigneeId;
        this.outcome = outcome;
        this.comment = comment;
        this.variables = variables;
        this.formData = formData;
        this.failedStep = failedStep;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 参数验证失败
     */
    public static TaskCompletionException validationError(Long taskId, String paramName, String reason) {
        return new TaskCompletionException(taskId, STEP_VALIDATION,
                "参数验证失败: " + paramName + " - " + reason, null);
    }

    /**
     * 任务不存在
     */
    public static TaskCompletionException taskNotFound(Long taskId) {
        return new TaskCompletionException(taskId, STEP_VALIDATION,
                "任务不存在: " + taskId, null);
    }

    /**
     * 权限检查失败
     */
    public static TaskCompletionException permissionDenied(Long taskId, Long assigneeId, String reason) {
        return new TaskCompletionException(taskId, null, assigneeId, STEP_PERMISSION_CHECK,
                "权限检查失败: " + reason, null);
    }

    /**
     * 任务状态检查失败
     */
    public static TaskCompletionException invalidTaskStatus(Long taskId, String currentStatus, String requiredStatus) {
        return new TaskCompletionException(taskId, STEP_TASK_STATUS_CHECK,
                "任务状态无效: 当前状态 " + currentStatus + ", 要求状态 " + requiredStatus, null);
    }

    /**
     * 任务已完成
     */
    public static TaskCompletionException taskAlreadyCompleted(Long taskId) {
        return new TaskCompletionException(taskId, STEP_TASK_STATUS_CHECK,
                "任务已完成: " + taskId, null);
    }

    /**
     * 任务未分配
     */
    public static TaskCompletionException taskNotAssigned(Long taskId) {
        return new TaskCompletionException(taskId, STEP_TASK_STATUS_CHECK,
                "任务未分配: " + taskId, null);
    }

    /**
     * 处理结果无效
     */
    public static TaskCompletionException invalidOutcome(Long taskId, String outcome, String validOutcomes) {
        return new TaskCompletionException(taskId, STEP_VALIDATION,
                "处理结果无效: " + outcome + ", 有效值: " + validOutcomes, null);
    }

    /**
     * 必需字段缺失
     */
    public static TaskCompletionException requiredFieldMissing(Long taskId, String fieldName) {
        return new TaskCompletionException(taskId, STEP_VALIDATION,
                "必需字段缺失: " + fieldName, null);
    }

    /**
     * 表单验证失败
     */
    public static TaskCompletionException formValidationError(Long taskId, String formError) {
        return new TaskCompletionException(taskId, STEP_FORM_VALIDATION,
                "表单验证失败: " + formError, null);
    }

    /**
     * 流程变量验证失败
     */
    public static TaskCompletionException variableValidationError(Long taskId, String variableName, String reason) {
        return new TaskCompletionException(taskId, STEP_VARIABLE_VALIDATION,
                "变量验证失败: " + variableName + " - " + reason, null);
    }

    /**
     * Flowable引擎完成失败
     */
    public static TaskCompletionException flowableCompletionError(Long taskId, String flowableTaskId, String error, Throwable cause) {
        return new TaskCompletionException(taskId, null, null, STEP_FLOWABLE_COMPLETION,
                "Flowable任务完成失败: " + error, cause);
    }

    /**
     * 数据库更新失败
     */
    public static TaskCompletionException databaseUpdateError(Long taskId, String error, Throwable cause) {
        return new TaskCompletionException(taskId, null, null, STEP_DATABASE_UPDATE,
                "数据库更新失败: " + error, cause);
    }

    /**
     * 下一任务创建失败
     */
    public static TaskCompletionException nextTaskCreationError(Long taskId, String error, Throwable cause) {
        return new TaskCompletionException(taskId, null, null, STEP_NEXT_TASK_CREATION,
                "下一任务创建失败: " + error, cause);
    }

    /**
     * 通知发送失败
     */
    public static TaskCompletionException notificationError(Long taskId, String error, Throwable cause) {
        return new TaskCompletionException(taskId, null, null, STEP_NOTIFICATION,
                "通知发送失败: " + error, cause);
    }

    /**
     * 业务规则验证失败
     */
    public static TaskCompletionException businessRuleViolation(Long taskId, String ruleName, String reason) {
        return new TaskCompletionException(taskId, STEP_VALIDATION,
                "业务规则违反: " + ruleName + " - " + reason, null);
    }

    /**
     * 并发处理冲突
     */
    public static TaskCompletionException concurrentModification(Long taskId, Long currentAssignee, String conflictReason) {
        return new TaskCompletionException(taskId, null, currentAssignee, STEP_VALIDATION,
                "并发处理冲突: " + conflictReason, null);
    }
}