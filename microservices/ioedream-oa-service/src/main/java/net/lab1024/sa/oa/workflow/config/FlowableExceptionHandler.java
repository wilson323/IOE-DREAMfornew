package net.lab1024.sa.oa.workflow.config;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.api.FlowableIllegalArgumentException;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.monitoring.ExceptionMetricsCollector;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Flowable工作流异常处理器（OA服务专用）
 * <p>
 * 专门处理Flowable 7.2.0工作流引擎的异常 严格遵循CLAUDE.md规范： - 使用@Order(1)确保优先于GlobalExceptionHandler处理Flowable异常 -
 * 仅处理oa.workflow包下的异常（basePackages限制） - 集成指标收集和监控
 * </p>
 * <p>
 * ⚠️ 注意：此异常处理器仅用于处理未被Service层捕获的Flowable异常 正常情况下，WorkflowEngineServiceImpl已捕获FlowableException并转换为SystemException
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-01-30
 */
@RestControllerAdvice(basePackages = "net.lab1024.sa.oa.workflow")
@Order(1)
public class FlowableExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger (FlowableExceptionHandler.class);

    @Resource
    private MeterRegistry meterRegistry;

    @Resource
    private ExceptionMetricsCollector exceptionMetricsCollector;

    private Counter flowableExceptionCounter;

    @PostConstruct
    public void init () {
        flowableExceptionCounter = Counter.builder ("workflow.exception.count").tag ("type", "flowable")
                .description ("Flowable异常计数").register (meterRegistry);
    }

    /**
     * 获取追踪ID
     */
    private String getTraceId () {
        String traceId = MDC.get ("traceId");
        if (traceId == null || traceId.isEmpty ()) {
            traceId = UUID.randomUUID ().toString ().replace ("-", "").substring (0, 16);
            MDC.put ("traceId", traceId);
        }
        return traceId;
    }

    /**
     * 记录异常指标
     */
    private void recordExceptionMetrics (Exception exception, long startTime) {
        long handlingTime = (System.nanoTime () - startTime) / 1_000_000; // 转换为毫秒
        exceptionMetricsCollector.recordException (exception, handlingTime);
    }

    // ==================== Flowable通用异常处理 ====================

    /**
     * 处理Flowable对象未找到异常
     * <p>
     * Flowable 7.x中统一使用此异常替代各种具体的NotFound异常
     * </p>
     */
    @ExceptionHandler(FlowableObjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseDTO<Void> handleFlowableObjectNotFoundException (FlowableObjectNotFoundException e) {
        long startTime = System.nanoTime ();
        String traceId = getTraceId ();

        // Flowable 7.x中getObjectType()和getObjectClass()可能已移除，改用getMessage()
        log.warn ("[Flowable对象不存在] traceId={}, message={}", traceId, e.getMessage ());

        flowableExceptionCounter.increment ();
        recordExceptionMetrics (e, startTime);
        recordExceptionDetailMetrics ("FLOWABLE_OBJECT_NOT_FOUND", "UNKNOWN");

        String errorMessage = determineErrorMessage (e);
        return ResponseDTO.error ("FLOWABLE_OBJECT_NOT_FOUND", errorMessage);
    }

    /**
     * 处理Flowable非法参数异常
     */
    @ExceptionHandler(FlowableIllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleFlowableIllegalArgumentException (FlowableIllegalArgumentException e) {
        long startTime = System.nanoTime ();
        String traceId = getTraceId ();

        log.warn ("[Flowable参数错误] traceId={}, message={}", traceId, e.getMessage ());

        flowableExceptionCounter.increment ();
        recordExceptionMetrics (e, startTime);
        recordExceptionDetailMetrics ("FLOWABLE_ILLEGAL_ARGUMENT", "ARGUMENT");

        return ResponseDTO.error ("FLOWABLE_ILLEGAL_ARGUMENT", "参数错误: " + e.getMessage ());
    }

    /**
     * 处理Flowable通用异常
     */
    @ExceptionHandler(FlowableException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleFlowableException (FlowableException e) {
        long startTime = System.nanoTime ();
        String traceId = getTraceId ();

        log.error ("[Flowable异常] traceId={}, message={}", traceId, e.getMessage (), e);

        flowableExceptionCounter.increment ();
        recordExceptionMetrics (e, startTime);
        recordExceptionDetailMetrics ("FLOWABLE_ERROR", "GENERAL");

        // 检查是否是乐观锁异常（通过消息内容判断）
        if (isOptimisticLockException (e)) {
            return ResponseDTO.error ("FLOWABLE_OPTIMISTIC_LOCK", "数据已被修改，请刷新后重试");
        }

        // 检查是否是任务已完成异常
        if (isTaskAlreadyCompletedException (e)) {
            return ResponseDTO.error ("FLOWABLE_TASK_COMPLETED", "任务已完成，无法重复操作");
        }

        return ResponseDTO.error ("FLOWABLE_ERROR", "工作流引擎错误: " + e.getMessage ());
    }

    // ==================== 私有方法 ====================

    /**
     * 根据异常消息确定错误消息
     * <p>
     * Flowable 7.x中FlowableObjectNotFoundException不再有getObjectType()方法， 改为从getMessage()中推断对象类型
     * </p>
     */
    private String determineErrorMessage (FlowableObjectNotFoundException e) {
        String message = e.getMessage ();
        if (message == null) {
            return "对象不存在";
        }
        String lowerMsg = message.toLowerCase ();
        if (lowerMsg.contains ("processdefinition") || lowerMsg.contains ("process definition")) {
            return "流程定义不存在";
        }
        if (lowerMsg.contains ("processinstance") || lowerMsg.contains ("process instance")) {
            return "流程实例不存在";
        }
        if (lowerMsg.contains ("task")) {
            return "任务不存在";
        }
        if (lowerMsg.contains ("execution")) {
            return "执行实例不存在";
        }
        if (lowerMsg.contains ("deployment")) {
            return "部署不存在";
        }
        if (lowerMsg.contains ("job")) {
            return "作业不存在";
        }
        if (lowerMsg.contains ("user")) {
            return "用户不存在";
        }
        if (lowerMsg.contains ("group")) {
            return "用户组不存在";
        }
        return "对象不存在: " + message;
    }

    /**
     * 判断是否是乐观锁异常
     */
    private boolean isOptimisticLockException (FlowableException e) {
        String message = e.getMessage ();
        return message != null
                && (message.contains ("optimistic locking") || message.contains ("was updated by another transaction")
                        || message.contains ("StaleObjectStateException"));
    }

    /**
     * 判断是否是任务已完成异常
     */
    private boolean isTaskAlreadyCompletedException (FlowableException e) {
        String message = e.getMessage ();
        return message != null && (message.contains ("task already completed")
                || message.contains ("Task does not exist") || message.contains ("Cannot find task"));
    }

    /**
     * 记录异常详细指标（用于记录错误码和类别）
     */
    private void recordExceptionDetailMetrics (String errorCode, String category) {
        meterRegistry.counter ("workflow.exception.detail", "error_code", errorCode, "category", category, "timestamp",
                LocalDateTime.now ().toString ()).increment ();
    }
}
