package net.lab1024.sa.oa.workflow.exception;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.api.FlowableIllegalArgumentException;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.engine.FlowableOptimisticLockingException;
import org.flowable.engine.FlowableTaskAlreadyCompletedException;
import org.flowable.engine.repository.ProcessDefinitionNotFoundException;
import org.flowable.engine.runtime.ProcessInstanceNotFoundException;
import org.flowable.task.api.TaskNotFoundException;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.annotation.Resource;
import java.util.UUID;

/**
 * 工作流引擎异常处理器
 * <p>
 * 专门处理Flowable工作流引擎相关的异常，提供细化的异常分类和处理
 * 集成Micrometer指标收集，支持企业级监控和告警
 * </p>
 * <p>
 * 异常分类体系：
 * 1. FlowableException - Flowable引擎基础异常
 * 2. FlowableIllegalArgumentException - 参数非法异常
 * 3. FlowableObjectNotFoundException - 对象不存在异常
 * 4. FlowableOptimisticLockingException - 乐观锁异常
 * 5. FlowableTaskAlreadyCompletedException - 任务已完成异常
 * 6. ProcessDefinitionNotFoundException - 流程定义不存在异常
 * 7. ProcessInstanceNotFoundException - 流程实例不存在异常
 * 8. TaskNotFoundException - 任务不存在异常
 * 9. DataAccessException - 数据访问异常
 * 10. WorkflowBusinessException - 工作流业务异常
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@RestControllerAdvice
@Order(1) // 优先级高于GlobalExceptionHandler
public class WorkflowExceptionHandler {

    @Resource
    private MeterRegistry meterRegistry;

    // 工作流异常计数器
    private Counter workflowExceptionCounter;
    private Counter flowableExceptionCounter;
    private Counter dataAccessCounter;
    private Counter businessExceptionCounter;

    /**
     * 初始化指标收集器
     */
    @jakarta.annotation.PostConstruct
    public void initMetrics() {
        workflowExceptionCounter = Counter.builder("workflow.exception.count")
                .description("工作流异常总数")
                .register(meterRegistry);

        flowableExceptionCounter = Counter.builder("workflow.flowable.exception.count")
                .description("Flowable引擎异常数")
                .register(meterRegistry);

        dataAccessCounter = Counter.builder("workflow.dataaccess.exception.count")
                .description("数据访问异常数")
                .register(meterRegistry);

        businessExceptionCounter = Counter.builder("workflow.business.exception.count")
                .description("工作流业务异常数")
                .register(meterRegistry);
    }

    /**
     * 获取TraceId
     */
    private String getTraceId() {
        String traceId = MDC.get("traceId");
        if (traceId == null || traceId.trim().isEmpty()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
            MDC.put("traceId", traceId);
        }
        return traceId;
    }

    /**
     * 记录异常指标
     */
    private void recordExceptionMetrics(String exceptionType, String operation) {
        workflowExceptionCounter.increment(
                meterRegistry.counter("workflow.exception.count",
                        "type", exceptionType,
                        "operation", operation)
        );
    }

    // ==================== Flowable引擎异常处理 ====================

    /**
     * 处理Flowable基础异常
     */
    @ExceptionHandler(FlowableException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleFlowableException(FlowableException e) {
        String traceId = getTraceId();

        log.error("[Flowable引擎异常] traceId={}, errorCode={}, message={}",
                traceId, e.getErrorCode(), e.getMessage(), e);

        recordExceptionMetrics("FLOWABLE_EXCEPTION", "FLOWABLE_ENGINE");
        flowableExceptionCounter.increment();

        // 根据错误代码返回不同的错误信息
        String errorCode = "FLOWABLE_ENGINE_ERROR";
        String errorMessage = "工作流引擎内部错误";

        if (e.getErrorCode() != null) {
            switch (e.getErrorCode()) {
                case "no-deployments-for-key":
                    errorCode = "PROCESS_NOT_DEPLOYED";
                    errorMessage = "流程定义未部署";
                    break;
                case "process-instance-already-terminated":
                    errorCode = "PROCESS_TERMINATED";
                    errorMessage = "流程实例已终止";
                    break;
                case "unauthorized":
                    errorCode = "WORKFLOW_UNAUTHORIZED";
                    errorMessage = "工作流操作未授权";
                    break;
                default:
                    errorMessage = "工作流引擎错误: " + e.getErrorCode();
            }
        }

        return ResponseDTO.error(errorCode, errorMessage);
    }

    /**
     * 处理Flowable参数非法异常
     */
    @ExceptionHandler(FlowableIllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleFlowableIllegalArgumentException(FlowableIllegalArgumentException e) {
        String traceId = getTraceId();

        log.warn("[Flowable参数异常] traceId={}, errorCode={}, message={}",
                traceId, e.getErrorCode(), e.getMessage());

        recordExceptionMetrics("FLOWABLE_ILLEGAL_ARGUMENT", "FLOWABLE_ENGINE");
        flowableExceptionCounter.increment(
                meterRegistry.counter("workflow.flowable.exception.count",
                        "type", "ILLEGAL_ARGUMENT")
        );

        String errorMessage = "工作流参数错误";

        if (e.getErrorCode() != null) {
            switch (e.getErrorCode()) {
                case "invalid-xml":
                    errorMessage = "流程定义XML格式错误";
                    break;
                case "missing-required-field":
                    errorMessage = "缺少必需字段";
                    break;
                case "invalid-value":
                    errorMessage = "参数值无效";
                    break;
                default:
                    errorMessage = "参数错误: " + e.getErrorCode();
            }
        }

        return ResponseDTO.error("FLOWABLE_ILLEGAL_ARGUMENT", errorMessage);
    }

    /**
     * 处理Flowable对象不存在异常
     */
    @ExceptionHandler(FlowableObjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseDTO<Void> handleFlowableObjectNotFoundException(FlowableObjectNotFoundException e) {
        String traceId = getTraceId();

        log.warn("[Flowable对象不存在] traceId={}, objectType={}, objectId={}",
                traceId, e.getObjectType(), e.getObjectId());

        recordExceptionMetrics("FLOWABLE_OBJECT_NOT_FOUND", "FLOWABLE_ENGINE");
        flowableExceptionCounter.increment(
                meterRegistry.counter("workflow.flowable.exception.count",
                        "type", "OBJECT_NOT_FOUND")
        );

        String errorMessage = "工作流对象不存在";

        if (e.getObjectType() != null) {
            switch (e.getObjectType()) {
                case "process-definition":
                    errorMessage = "流程定义不存在";
                    break;
                case "process-instance":
                    errorMessage = "流程实例不存在";
                    break;
                case "task":
                    errorMessage = "任务不存在";
                    break;
                case "execution":
                    errorMessage = "执行实例不存在";
                    break;
                default:
                    errorMessage = e.getObjectType() + "不存在";
            }
        }

        return ResponseDTO.error("FLOWABLE_OBJECT_NOT_FOUND", errorMessage);
    }

    /**
     * 处理Flowable乐观锁异常
     */
    @ExceptionHandler(FlowableOptimisticLockingException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseDTO<Void> handleFlowableOptimisticLockingException(FlowableOptimisticLockingException e) {
        String traceId = getTraceId();

        log.warn("[Flowable乐观锁异常] traceId={}, entityType={}, entityId={}",
                traceId, e.getEntityType(), e.getEntityId());

        recordExceptionMetrics("FLOWABLE_OPTIMISTIC_LOCK", "FLOWABLE_ENGINE");
        flowableExceptionCounter.increment(
                meterRegistry.counter("workflow.flowable.exception.count",
                        "type", "OPTIMISTIC_LOCK")
        );

        return ResponseDTO.error("FLOWABLE_OPTIMISTIC_LOCK",
                "数据已被修改，请刷新后重试");
    }

    /**
     * 处理Flowable任务已完成异常
     */
    @ExceptionHandler(FlowableTaskAlreadyCompletedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseDTO<Void> handleFlowableTaskAlreadyCompletedException(FlowableTaskAlreadyCompletedException e) {
        String traceId = getTraceId();

        log.warn("[Flowable任务已完成] traceId={}, taskId={}", traceId, e.getTaskId());

        recordExceptionMetrics("FLOWABLE_TASK_COMPLETED", "FLOWABLE_ENGINE");
        flowableExceptionCounter.increment(
                meterRegistry.counter("workflow.flowable.exception.count",
                        "type", "TASK_ALREADY_COMPLETED")
        );

        return ResponseDTO.error("FLOWABLE_TASK_COMPLETED", "任务已完成，无法重复操作");
    }

    // ==================== Flowable具体异常处理 ====================

    /**
     * 处理流程定义不存在异常
     */
    @ExceptionHandler(ProcessDefinitionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseDTO<Void> handleProcessDefinitionNotFoundException(ProcessDefinitionNotFoundException e) {
        String traceId = getTraceId();

        log.warn("[流程定义不存在] traceId={}, processDefinitionId={}, processDefinitionKey={}",
                traceId, e.getProcessDefinitionId(), e.getProcessDefinitionKey());

        recordExceptionMetrics("PROCESS_DEFINITION_NOT_FOUND", "PROCESS_DEFINITION");
        flowableExceptionCounter.increment(
                meterRegistry.counter("workflow.flowable.exception.count",
                        "type", "PROCESS_DEFINITION_NOT_FOUND")
        );

        return ResponseDTO.error("PROCESS_DEFINITION_NOT_FOUND", "流程定义不存在");
    }

    /**
     * 处理流程实例不存在异常
     */
    @ExceptionHandler(ProcessInstanceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseDTO<Void> handleProcessInstanceNotFoundException(ProcessInstanceNotFoundException e) {
        String traceId = getTraceId();

        log.warn("[流程实例不存在] traceId={}, processInstanceId={}", traceId, e.getProcessInstanceId());

        recordExceptionMetrics("PROCESS_INSTANCE_NOT_FOUND", "PROCESS_INSTANCE");
        flowableExceptionCounter.increment(
                meterRegistry.counter("workflow.flowable.exception.count",
                        "type", "PROCESS_INSTANCE_NOT_FOUND")
        );

        return ResponseDTO.error("PROCESS_INSTANCE_NOT_FOUND", "流程实例不存在");
    }

    /**
     * 处理任务不存在异常
     */
    @ExceptionHandler(TaskNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseDTO<Void> handleTaskNotFoundException(TaskNotFoundException e) {
        String traceId = getTraceId();

        log.warn("[任务不存在] traceId={}, taskId={}", traceId, e.getTaskId());

        recordExceptionMetrics("TASK_NOT_FOUND", "TASK");
        flowableExceptionCounter.increment(
                meterRegistry.counter("workflow.flowable.exception.count",
                        "type", "TASK_NOT_FOUND")
        );

        return ResponseDTO.error("TASK_NOT_FOUND", "任务不存在");
    }

    // ==================== 数据访问异常处理 ====================

    /**
     * 处理数据访问异常
     */
    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleDataAccessException(DataAccessException e) {
        String traceId = getTraceId();

        log.error("[数据访问异常] traceId={}, message={}", traceId, e.getMessage(), e);

        recordExceptionMetrics("DATA_ACCESS", "DATABASE");
        dataAccessCounter.increment();

        // 根据异常类型细化错误信息
        String errorMessage = "数据访问失败";

        if (e.getCause() != null) {
            String causeMessage = e.getCause().getMessage().toLowerCase();
            if (causeMessage.contains("connection")) {
                errorMessage = "数据库连接失败";
            } else if (causeMessage.contains("timeout")) {
                errorMessage = "数据库访问超时";
            } else if (causeMessage.contains("constraint")) {
                errorMessage = "数据完整性约束违反";
            } else if (causeMessage.contains("duplicate")) {
                errorMessage = "数据重复";
            }
        }

        return ResponseDTO.error("WORKFLOW_DATA_ACCESS_ERROR", errorMessage);
    }

    // ==================== 工作流业务异常处理 ====================

    /**
     * 处理工作流业务异常
     */
    @ExceptionHandler(WorkflowBusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleWorkflowBusinessException(WorkflowBusinessException e) {
        String traceId = getTraceId();

        log.warn("[工作流业务异常] traceId={}, code={}, message={}, operation={}",
                traceId, e.getCode(), e.getMessage(), e.getOperation());

        recordExceptionMetrics("WORKFLOW_BUSINESS", e.getOperation());
        businessExceptionCounter.increment(
                meterRegistry.counter("workflow.business.exception.count",
                        "operation", e.getOperation())
        );

        return ResponseDTO.error(e.getCode(), e.getMessage());
    }

    // ==================== 特殊工作流场景异常处理 ====================

    /**
     * 处理流程部署异常
     */
    @ExceptionHandler(ProcessDeploymentException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleProcessDeploymentException(ProcessDeploymentException e) {
        String traceId = getTraceId();

        log.error("[流程部署异常] traceId={}, processKey={}, stage={}, error={}",
                traceId, e.getProcessKey(), e.getDeploymentStage(), e.getMessage(), e);

        recordExceptionMetrics("PROCESS_DEPLOYMENT", "DEPLOYMENT");
        flowableExceptionCounter.increment(
                meterRegistry.counter("workflow.flowable.exception.count",
                        "type", "DEPLOYMENT_ERROR")
        );

        return ResponseDTO.error("PROCESS_DEPLOYMENT_ERROR",
                "流程部署失败: " + e.getDeploymentStage());
    }

    /**
     * 处理流程启动异常
     */
    @ExceptionHandler(ProcessStartException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleProcessStartException(ProcessStartException e) {
        String traceId = getTraceId();

        log.error("[流程启动异常] traceId={}, processKey={}, businessKey={}, error={}",
                traceId, e.getProcessKey(), e.getBusinessKey(), e.getMessage(), e);

        recordExceptionMetrics("PROCESS_START", "START_PROCESS");
        flowableExceptionCounter.increment(
                meterRegistry.counter("workflow.flowable.exception.count",
                        "type", "START_PROCESS_ERROR")
        );

        return ResponseDTO.error("PROCESS_START_ERROR", "流程启动失败");
    }

    /**
     * 处理任务完成异常
     */
    @ExceptionHandler(TaskCompletionException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleTaskCompletionException(TaskCompletionException e) {
        String traceId = getTraceId();

        log.error("[任务完成异常] traceId={}, taskId={}, assigneeId={}, error={}",
                traceId, e.getTaskId(), e.getAssigneeId(), e.getMessage(), e);

        recordExceptionMetrics("TASK_COMPLETION", "COMPLETE_TASK");
        flowableExceptionCounter.increment(
                meterRegistry.counter("workflow.flowable.exception.count",
                        "type", "TASK_COMPLETION_ERROR")
        );

        return ResponseDTO.error("TASK_COMPLETION_ERROR", "任务完成失败");
    }

    /**
     * 处理流程实例查询异常
     */
    @ExceptionHandler(ProcessInstanceQueryException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleProcessInstanceQueryException(ProcessInstanceQueryException e) {
        String traceId = getTraceId();

        log.error("[流程实例查询异常] traceId={}, queryParams={}, error={}",
                traceId, e.getQueryParams(), e.getMessage(), e);

        recordExceptionMetrics("PROCESS_INSTANCE_QUERY", "QUERY");
        flowableExceptionCounter.increment(
                meterRegistry.counter("workflow.flowable.exception.count",
                        "type", "QUERY_ERROR")
        );

        return ResponseDTO.error("PROCESS_QUERY_ERROR", "流程实例查询失败");
    }
}