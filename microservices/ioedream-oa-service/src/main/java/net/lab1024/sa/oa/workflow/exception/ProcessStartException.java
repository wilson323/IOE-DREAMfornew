package net.lab1024.sa.oa.workflow.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 流程启动异常
 * <p>
 * 专门用于流程启动过程中的异常处理
 * 提供详细的启动参数和错误上下文信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProcessStartException extends RuntimeException {

    /**
     * 流程Key
     */
    private String processKey;

    /**
     * 业务Key
     */
    private String businessKey;

    /**
     * 发起人ID
     */
    private Long initiatorId;

    /**
     * 流程变量
     */
    private Map<String, Object> variables;

    /**
     * 流程名称
     */
    private String processName;

    /**
     * 启动失败的具体步骤
     */
    private String failedStep;

    // ==================== 失败步骤常量 ====================

    public static final String STEP_VALIDATION = "VALIDATION"; // 参数验证步骤
    public static final String STEP_PROCESS_DEFINITION = "PROCESS_DEFINITION"; // 获取流程定义步骤
    public static final String STEP_VARIABLE_VALIDATION = "VARIABLE_VALIDATION"; // 变量验证步骤
    public static final String STEP_ENGINE_START = "ENGINE_START"; // 引擎启动步骤
    public static final String STEP_DATABASE_SAVE = "DATABASE_SAVE"; // 数据库保存步骤
    public static final String STEP_TASK_CREATION = "TASK_CREATION"; // 任务创建步骤
    public static final String STEP_PERMISSION_CHECK = "PERMISSION_CHECK"; // 权限检查步骤

    // ==================== 构造方法 ====================

    public ProcessStartException(String processKey, String businessKey, String failedStep, String message, Throwable cause) {
        super(message, cause);
        this.processKey = processKey;
        this.businessKey = businessKey;
        this.failedStep = failedStep;
    }

    public ProcessStartException(String processKey, String businessKey, Long initiatorId, String failedStep, String message, Throwable cause) {
        super(message, cause);
        this.processKey = processKey;
        this.businessKey = businessKey;
        this.initiatorId = initiatorId;
        this.failedStep = failedStep;
    }

    public ProcessStartException(String processKey, String businessKey, Long initiatorId, Map<String, Object> variables, String failedStep, String message, Throwable cause) {
        super(message, cause);
        this.processKey = processKey;
        this.businessKey = businessKey;
        this.initiatorId = initiatorId;
        this.variables = variables;
        this.failedStep = failedStep;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 参数验证失败
     */
    public static ProcessStartException validationError(String paramName, String reason) {
        return new ProcessStartException(null, null, STEP_VALIDATION,
                "参数验证失败: " + paramName + " - " + reason, null);
    }

    /**
     * 流程定义不存在
     */
    public static ProcessStartException processDefinitionNotFound(String processKey) {
        return new ProcessStartException(processKey, null, STEP_PROCESS_DEFINITION,
                "流程定义不存在: " + processKey, null);
    }

    /**
     * 流程定义已停用
     */
    public static ProcessStartException processDefinitionSuspended(String processKey) {
        return new ProcessStartException(processKey, null, STEP_PROCESS_DEFINITION,
                "流程定义已停用: " + processKey, null);
    }

    /**
     * 变量验证失败
     */
    public static ProcessStartException variableValidationError(String variableName, String reason) {
        return new ProcessStartException(null, null, STEP_VARIABLE_VALIDATION,
                "变量验证失败: " + variableName + " - " + reason, null);
    }

    /**
     * 必需变量缺失
     */
    public static ProcessStartException requiredVariableMissing(String variableName) {
        return new ProcessStartException(null, null, STEP_VARIABLE_VALIDATION,
                "必需变量缺失: " + variableName, null);
    }

    /**
     * 引擎启动失败
     */
    public static ProcessStartException engineStartError(String processKey, String businessKey, String error, Throwable cause) {
        return new ProcessStartException(processKey, businessKey, STEP_ENGINE_START,
                "流程引擎启动失败: " + error, cause);
    }

    /**
     * 数据库保存失败
     */
    public static ProcessStartException databaseSaveError(String processKey, String businessKey, String error, Throwable cause) {
        return new ProcessStartException(processKey, businessKey, STEP_DATABASE_SAVE,
                "数据库保存失败: " + error, cause);
    }

    /**
     * 任务创建失败
     */
    public static ProcessStartException taskCreationError(String processKey, String businessKey, String error, Throwable cause) {
        return new ProcessStartException(processKey, businessKey, STEP_TASK_CREATION,
                "初始任务创建失败: " + error, cause);
    }

    /**
     * 权限检查失败
     */
    public static ProcessStartException permissionDenied(String processKey, Long initiatorId, String reason) {
        return new ProcessStartException(processKey, null, initiatorId, STEP_PERMISSION_CHECK,
                "权限检查失败: " + reason, null);
    }

    /**
     * 业务Key重复
     */
    public static ProcessStartException businessKeyExists(String processKey, String businessKey) {
        return new ProcessStartException(processKey, businessKey, STEP_VALIDATION,
                "业务Key已存在: " + businessKey, null);
    }

    /**
     * 流程并发限制
     */
    public static ProcessStartException concurrencyLimitExceeded(String processKey, Long initiatorId, String reason) {
        return new ProcessStartException(processKey, null, initiatorId, STEP_VALIDATION,
                "流程并发限制超出: " + reason, null);
    }
}