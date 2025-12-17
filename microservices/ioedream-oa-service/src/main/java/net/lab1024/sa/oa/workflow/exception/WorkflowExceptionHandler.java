package net.lab1024.sa.oa.workflow.exception;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.api.FlowableIllegalArgumentException;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Flowable 7.2.0 工作流异常统一处理器
 * <p>
 * 适配Flowable 7.x API变化，移除已废弃的特定异常类，
 * 使用通用异常FlowableObjectNotFoundException统一处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-01-17
 */
@Slf4j
@RestControllerAdvice(basePackages = "net.lab1024.sa.oa.workflow")
@Order(1)
public class WorkflowExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(WorkflowExceptionHandler.class);

    @Resource
    private MeterRegistry meterRegistry;

    private Counter flowableExceptionCounter;
    private Counter businessExceptionCounter;

    @PostConstruct
    public void init() {
        flowableExceptionCounter = Counter.builder("workflow.exception.count")
                .tag("type", "flowable")
                .description("Flowable异常计数")
                .register(meterRegistry);

        businessExceptionCounter = Counter.builder("workflow.exception.count")
                .tag("type", "business")
                .description("业务异常计数")
                .register(meterRegistry);
    }

    // ==================== Flowable通用异常处理 ====================

    /**
     * 处理Flowable对象未找到异常
     * Flowable 7.x中统一使用此异常替代各种具体的NotFound异常
     */
    @ExceptionHandler(FlowableObjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseDTO<Void> handleFlowableObjectNotFoundException(FlowableObjectNotFoundException e) {
        String traceId = getTraceId();

        // Flowable 7.x中getObjectType()和getObjectClass()可能已移除，改用getMessage()
        log.warn("[Flowable对象不存在] traceId={}, message={}", traceId, e.getMessage());

        flowableExceptionCounter.increment();
        recordExceptionMetrics("FLOWABLE_OBJECT_NOT_FOUND", "UNKNOWN");

        String errorMessage = determineErrorMessage(e);
        return ResponseDTO.error("FLOWABLE_OBJECT_NOT_FOUND", errorMessage);
    }

    /**
     * 处理Flowable非法参数异常
     */
    @ExceptionHandler(FlowableIllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleFlowableIllegalArgumentException(FlowableIllegalArgumentException e) {
        String traceId = getTraceId();

        log.warn("[Flowable参数错误] traceId={}, message={}", traceId, e.getMessage());

        flowableExceptionCounter.increment();
        recordExceptionMetrics("FLOWABLE_ILLEGAL_ARGUMENT", "ARGUMENT");

        return ResponseDTO.error("FLOWABLE_ILLEGAL_ARGUMENT", "参数错误: " + e.getMessage());
    }

    /**
     * 处理Flowable通用异常
     */
    @ExceptionHandler(FlowableException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleFlowableException(FlowableException e) {
        String traceId = getTraceId();

        log.error("[Flowable异常] traceId={}, message={}", traceId, e.getMessage(), e);

        flowableExceptionCounter.increment();
        recordExceptionMetrics("FLOWABLE_ERROR", "GENERAL");

        // 检查是否是乐观锁异常（通过消息内容判断）
        if (isOptimisticLockException(e)) {
            return ResponseDTO.error("FLOWABLE_OPTIMISTIC_LOCK", "数据已被修改，请刷新后重试");
        }

        // 检查是否是任务已完成异常
        if (isTaskAlreadyCompletedException(e)) {
            return ResponseDTO.error("FLOWABLE_TASK_COMPLETED", "任务已完成，无法重复操作");
        }

        return ResponseDTO.error("FLOWABLE_ERROR", "工作流引擎错误: " + e.getMessage());
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

        businessExceptionCounter.increment();
        recordExceptionMetrics("DATA_ACCESS_ERROR", "DATABASE");

        return ResponseDTO.error("DATA_ACCESS_ERROR", "数据访问异常，请稍后重试");
    }

    // ==================== 通用异常处理 ====================

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleRuntimeException(RuntimeException e) {
        String traceId = getTraceId();

        log.error("[运行时异常] traceId={}, message={}", traceId, e.getMessage(), e);

        businessExceptionCounter.increment();
        recordExceptionMetrics("RUNTIME_ERROR", "GENERAL");

        return ResponseDTO.error("SYSTEM_ERROR", "系统错误，请稍后重试");
    }

    // ==================== 私有方法 ====================

    /**
     * 根据异常消息确定错误消息
     * <p>Flowable 7.x中FlowableObjectNotFoundException不再有getObjectType()方法，
     * 改为从getMessage()中推断对象类型</p>
     */
    private String determineErrorMessage(FlowableObjectNotFoundException e) {
        String message = e.getMessage();
        if (message == null) {
            return "对象不存在";
        }
        String lowerMsg = message.toLowerCase();
        if (lowerMsg.contains("processdefinition") || lowerMsg.contains("process definition")) return "流程定义不存在";
        if (lowerMsg.contains("processinstance") || lowerMsg.contains("process instance")) return "流程实例不存在";
        if (lowerMsg.contains("task")) return "任务不存在";
        if (lowerMsg.contains("execution")) return "执行实例不存在";
        if (lowerMsg.contains("deployment")) return "部署不存在";
        if (lowerMsg.contains("job")) return "作业不存在";
        if (lowerMsg.contains("user")) return "用户不存在";
        if (lowerMsg.contains("group")) return "用户组不存在";
        return "对象不存在: " + message;
    }

    /**
     * 判断是否是乐观锁异常
     */
    private boolean isOptimisticLockException(FlowableException e) {
        String message = e.getMessage();
        return message != null && (
                message.contains("optimistic locking") ||
                message.contains("was updated by another transaction") ||
                message.contains("StaleObjectStateException")
        );
    }

    /**
     * 判断是否是任务已完成异常
     */
    private boolean isTaskAlreadyCompletedException(FlowableException e) {
        String message = e.getMessage();
        return message != null && (
                message.contains("task already completed") ||
                message.contains("Task does not exist") ||
                message.contains("Cannot find task")
        );
    }

    /**
     * 获取追踪ID
     */
    private String getTraceId() {
        String traceId = MDC.get("traceId");
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            MDC.put("traceId", traceId);
        }
        return traceId;
    }

    /**
     * 记录异常指标
     */
    private void recordExceptionMetrics(String errorCode, String category) {
        meterRegistry.counter("workflow.exception.detail",
                "error_code", errorCode,
                "category", category,
                "timestamp", LocalDateTime.now().toString()
        ).increment();
    }
}
