package net.lab1024.sa.oa.workflow.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工作流业务异常
 * <p>
 * 专门用于工作流模块的业务异常处理
 * 提供细化的异常分类和操作上下文信息
 * 支持企业级错误处理和监控指标收集
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WorkflowBusinessException extends RuntimeException {

    /**
     * 异常代码（符合统一规范）
     */
    private String errorCode;

    /**
     * 操作类型
     */
    private String operation;

    /**
     * 业务对象ID
     */
    private String businessId;

    /**
     * 额外的上下文信息
     */
    private Object context;

    // ==================== 常用异常代码常量 ====================

    // 流程定义相关异常 (1000-1099)
    public static final String PROCESS_DEFINITION_NOT_FOUND = "WF_1001";
    public static final String PROCESS_DEFINITION_ALREADY_DEPLOYED = "WF_1002";
    public static final String PROCESS_DEFINITION_INVALID = "WF_1003";
    public static final String PROCESS_DEFINITION_VERSION_CONFLICT = "WF_1004";
    public static final String PROCESS_DEFINITION_SUSPENDED = "WF_1005";

    // 流程实例相关异常 (1100-1199)
    public static final String PROCESS_INSTANCE_NOT_FOUND = "WF_1101";
    public static final String PROCESS_INSTANCE_ALREADY_STARTED = "WF_1102";
    public static final String PROCESS_INSTANCE_ALREADY_TERMINATED = "WF_1103";
    public static final String PROCESS_INSTANCE_ALREADY_SUSPENDED = "WF_1104";
    public static final String PROCESS_INSTANCE_CANNOT_START = "WF_1105";
    public static final String PROCESS_INSTANCE_CANNOT_TERMINATE = "WF_1106";
    public static final String PROCESS_INSTANCE_CANNOT_SUSPEND = "WF_1107";
    public static final String PROCESS_INSTANCE_CANNOT_RESUME = "WF_1108";

    // 任务相关异常 (1200-1299)
    public static final String TASK_NOT_FOUND = "WF_1201";
    public static final String TASK_ALREADY_COMPLETED = "WF_1202";
    public static final String TASK_ALREADY_ASSIGNED = "WF_1203";
    public static final String TASK_NOT_ASSIGNED = "WF_1204";
    public static final String TASK_CANNOT_COMPLETE = "WF_1205";
    public static final String TASK_CANNOT_CLAIM = "WF_1206";
    public static final String TASK_CANNOT_DELEGATE = "WF_1207";
    public static final String TASK_CANNOT_ASSIGN = "WF_1208";
    public static final String TASK_PERMISSION_DENIED = "WF_1209";

    // 审批相关异常 (1300-1399)
    public static final String APPROVAL_NOT_FOUND = "WF_1301";
    public static final String APPROVAL_ALREADY_PROCESSED = "WF_1302";
    public static final String APPROVAL_OUTDATED = "WF_1303";
    public static final String APPROVAL_INVALID_ACTION = "WF_1304";
    public static final String APPROVAL_REQUIRED_FIELDS_MISSING = "WF_1305";
    public static final String APPROVAL_COMMENT_REQUIRED = "WF_1306";
    public static final String APPROVAL_ATTACHMENT_REQUIRED = "WF_1307";
    public static final String APPROVAL_CONDITION_NOT_MET = "WF_1308";

    // 权限相关异常 (1400-1499)
    public static final String WORKFLOW_PERMISSION_DENIED = "WF_1401";
    public static final String WORKFLOW_ROLE_NOT_FOUND = "WF_1402";
    public static final String WORKFLOW_USER_NOT_FOUND = "WF_1403";
    public static final String WORKFLOW_DEPARTMENT_NOT_FOUND = "WF_1404";
    public static final String WORKFLOW_ACCESS_DENIED = "WF_1405";

    // 数据相关异常 (1500-1599)
    public static final String WORKFLOW_DATA_NOT_FOUND = "WF_1501";
    public static final String WORKFLOW_DATA_INVALID = "WF_1502";
    public static final String WORKFLOW_DATA_CONFLICT = "WF_1503";
    public static final String WORKFLOW_DATA_INTEGRITY_ERROR = "WF_1504";

    // 配置相关异常 (1600-1699)
    public static final String WORKFLOW_CONFIG_NOT_FOUND = "WF_1601";
    public static final String WORKFLOW_CONFIG_INVALID = "WF_1602";
    public static final String WORKFLOW_NODE_CONFIG_ERROR = "WF_1603";
    public static final String WORKFLOW_GATEWAY_CONFIG_ERROR = "WF_1604";
    public static final String WORKFLOW_TIMER_CONFIG_ERROR = "WF_1605";

    // ==================== 操作类型常量 ====================

    public static final String OPERATION_DEPLOY_PROCESS = "DEPLOY_PROCESS";
    public static final String OPERATION_START_PROCESS = "START_PROCESS";
    public static final String OPERATION_TERMINATE_PROCESS = "TERMINATE_PROCESS";
    public static final String OPERATION_SUSPEND_PROCESS = "SUSPEND_PROCESS";
    public static final String OPERATION_RESUME_PROCESS = "RESUME_PROCESS";
    public static final String OPERATION_COMPLETE_TASK = "COMPLETE_TASK";
    public static final String OPERATION_CLAIM_TASK = "CLAIM_TASK";
    public static final String OPERATION_DELEGATE_TASK = "DELEGATE_TASK";
    public static final String OPERATION_ASSIGN_TASK = "ASSIGN_TASK";
    public static final String OPERATION_REJECT_TASK = "REJECT_TASK";
    public static final String OPERATION_APPROVE = "APPROVE";
    public static final String OPERATION_QUERY_PROCESS = "QUERY_PROCESS";
    public static final String OPERATION_QUERY_TASK = "QUERY_TASK";
    public static final String OPERATION_QUERY_HISTORY = "QUERY_HISTORY";

    // ==================== 构造方法 ====================

    /**
     * 构造方法
     *
     * @param code      异常代码
     * @param message   异常消息
     * @param operation 操作类型
     */
    public WorkflowBusinessException(String code, String message, String operation) {
        super(message);
        this.errorCode = code;
        this.operation = operation;
    }

    /**
     * 构造方法
     *
     * @param code      异常代码
     * @param message   异常消息
     * @param operation 操作类型
     * @param businessId 业务对象ID
     */
    public WorkflowBusinessException(String code, String message, String operation, String businessId) {
        super(message);
        this.errorCode = code;
        this.operation = operation;
        this.businessId = businessId;
    }

    /**
     * 构造方法
     *
     * @param code      异常代码
     * @param message   异常消息
     * @param operation 操作类型
     * @param businessId 业务对象ID
     * @param context   上下文信息
     */
    public WorkflowBusinessException(String code, String message, String operation, String businessId, Object context) {
        super(message);
        this.errorCode = code;
        this.operation = operation;
        this.businessId = businessId;
        this.context = context;
    }

    /**
     * 构造方法
     *
     * @param code      异常代码
     * @param message   异常消息
     * @param operation 操作类型
     * @param cause     原因异常
     */
    public WorkflowBusinessException(String code, String message, String operation, Throwable cause) {
        super(message, cause);
        this.errorCode = code;
        this.operation = operation;
    }

    // ==================== 静态工厂方法 ====================

    // 流程定义异常工厂方法
    public static WorkflowBusinessException processDefinitionNotFound(String processKey) {
        return new WorkflowBusinessException(PROCESS_DEFINITION_NOT_FOUND,
                "流程定义不存在: " + processKey, OPERATION_DEPLOY_PROCESS, processKey);
    }

    public static WorkflowBusinessException processDefinitionAlreadyDeployed(String processKey) {
        return new WorkflowBusinessException(PROCESS_DEFINITION_ALREADY_DEPLOYED,
                "流程定义已部署: " + processKey, OPERATION_DEPLOY_PROCESS, processKey);
    }

    // 流程实例异常工厂方法
    public static WorkflowBusinessException processInstanceNotFound(String instanceId) {
        return new WorkflowBusinessException(PROCESS_INSTANCE_NOT_FOUND,
                "流程实例不存在: " + instanceId, OPERATION_QUERY_PROCESS, instanceId);
    }

    public static WorkflowBusinessException processInstanceAlreadyStarted(String instanceId) {
        return new WorkflowBusinessException(PROCESS_INSTANCE_ALREADY_STARTED,
                "流程实例已启动: " + instanceId, OPERATION_START_PROCESS, instanceId);
    }

    // 任务异常工厂方法
    public static WorkflowBusinessException taskNotFound(String taskId) {
        return new WorkflowBusinessException(TASK_NOT_FOUND,
                "任务不存在: " + taskId, OPERATION_QUERY_TASK, taskId);
    }

    public static WorkflowBusinessException taskAlreadyCompleted(String taskId) {
        return new WorkflowBusinessException(TASK_ALREADY_COMPLETED,
                "任务已完成: " + taskId, OPERATION_COMPLETE_TASK, taskId);
    }

    public static WorkflowBusinessException taskPermissionDenied(String taskId, Long userId) {
        return new WorkflowBusinessException(TASK_PERMISSION_DENIED,
                "无权限处理任务: " + taskId, OPERATION_COMPLETE_TASK, taskId, userId);
    }

    public static WorkflowBusinessException taskNotAssigned(String taskId) {
        return new WorkflowBusinessException(TASK_NOT_ASSIGNED,
                "任务未分配: " + taskId, OPERATION_COMPLETE_TASK, taskId);
    }

    // 审批异常工厂方法
    public static WorkflowBusinessException approvalNotFound(String approvalId) {
        return new WorkflowBusinessException(APPROVAL_NOT_FOUND,
                "审批记录不存在: " + approvalId, OPERATION_APPROVE, approvalId);
    }

    public static WorkflowBusinessException approvalAlreadyProcessed(String approvalId) {
        return new WorkflowBusinessException(APPROVAL_ALREADY_PROCESSED,
                "审批记录已处理: " + approvalId, OPERATION_APPROVE, approvalId);
    }

    public static WorkflowBusinessException approvalCommentRequired() {
        return new WorkflowBusinessException(APPROVAL_COMMENT_REQUIRED,
                "审批意见不能为空", OPERATION_APPROVE);
    }

    // 权限异常工厂方法
    public static WorkflowBusinessException workflowPermissionDenied(Long userId, String operation) {
        return new WorkflowBusinessException(WORKFLOW_PERMISSION_DENIED,
                "用户无权限执行工作流操作: " + operation, operation, userId.toString());
    }

    public static WorkflowBusinessException workflowAccessDenied(String resource, String operation) {
        return new WorkflowBusinessException(WORKFLOW_ACCESS_DENIED,
                "访问被拒绝: " + resource + " 操作: " + operation, operation);
    }

    // 数据异常工厂方法
    public static WorkflowBusinessException workflowDataNotFound(String dataType, String dataId) {
        return new WorkflowBusinessException(WORKFLOW_DATA_NOT_FOUND,
                "工作流数据不存在: " + dataType + " - " + dataId, OPERATION_QUERY_PROCESS, dataId);
    }

    public static WorkflowBusinessException workflowDataInvalid(String dataType, String reason) {
        return new WorkflowBusinessException(WORKFLOW_DATA_INVALID,
                "工作流数据无效: " + dataType + " - " + reason, OPERATION_START_PROCESS);
    }

    // 配置异常工厂方法
    public static WorkflowBusinessException workflowConfigNotFound(String configType) {
        return new WorkflowBusinessException(WORKFLOW_CONFIG_NOT_FOUND,
                "工作流配置不存在: " + configType, OPERATION_DEPLOY_PROCESS);
    }

    public static WorkflowBusinessException workflowNodeConfigError(String nodeId, String error) {
        return new WorkflowBusinessException(WORKFLOW_NODE_CONFIG_ERROR,
                "节点配置错误: " + nodeId + " - " + error, OPERATION_DEPLOY_PROCESS, nodeId);
    }

    // ==================== 标准化方法 ====================

    /**
     * 获取错误代码（符合统一规范）
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 获取详细信息（符合统一规范）
     */
    public Object getDetails() {
        return context;
    }

    /**
     * 获取操作类型
     */
    public String getOperation() {
        return operation;
    }

    /**
     * 获取业务对象ID
     */
    public String getBusinessId() {
        return businessId;
    }

    /**
     * 获取上下文信息
     */
    public Object getContext() {
        return context;
    }
}
