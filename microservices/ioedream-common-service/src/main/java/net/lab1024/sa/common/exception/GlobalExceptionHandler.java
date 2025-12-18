package net.lab1024.sa.common.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.monitoring.ExceptionMetricsCollector;
import org.slf4j.MDC;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.annotation.Resource;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.UUID;
import java.util.stream.Collectors;

// 视频相关异常类
import net.lab1024.sa.common.exception.VideoDeviceException;
import net.lab1024.sa.common.exception.VideoStreamException;
import net.lab1024.sa.common.exception.AIAnalysisException;
import net.lab1024.sa.common.exception.VideoRecordingException;

/**
 * 全局异常处理器（内存优化+指标收集版本）
 * <p>
 * 统一处理系统中的所有异常，返回标准的响应格式
 * 严格遵循CLAUDE.md规范：
 * - 统一异常处理，禁止多个异常处理器并存
 * - 包含TraceId追踪，便于分布式追踪和问题定位
 * - 集成企业级指标收集和监控
 * - 内存优化：减少对象创建和字符串拼接
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 2.0.0 - 企业级监控版本
 * @since 2025-12-16
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Resource
    private ExceptionMetricsCollector exceptionMetricsCollector;

    /**
     * 获取TraceId（从MDC获取，如果没有则生成新的）
     *
     * @return TraceId
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
     * 记录异常指标（内存优化版本）
     *
     * @param exception 异常对象
     * @param startTime 开始时间（纳秒）
     */
    private void recordExceptionMetrics(Exception exception, long startTime) {
        long handlingTime = (System.nanoTime() - startTime) / 1_000_000; // 转换为毫秒
        exceptionMetricsCollector.recordException(exception, handlingTime);
    }

    // ==================== 业务异常处理 ====================

    /**
     * 处理业务异常（带指标收集）
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseDTO<Void> handleBusinessException(BusinessException e) {
        long startTime = System.nanoTime();
        String traceId = getTraceId();

        // 内存优化：使用参数化日志而非字符串拼接
        log.warn("[业务异常] traceId={}, code={}, message={}", traceId, e.getCode(), e.getMessage());

        // 记录异常指标
        recordExceptionMetrics(e, startTime);

        return ResponseDTO.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理系统异常（带指标收集）
     */
    @ExceptionHandler(SystemException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleSystemException(SystemException e) {
        long startTime = System.nanoTime();
        String traceId = getTraceId();

        // 记录系统异常指标（关键指标）
        log.error("[系统异常] traceId={}, code={}, message={}", traceId, e.getCode(), e.getMessage(), e);

        // 记录异常指标
        recordExceptionMetrics(e, startTime);

        return ResponseDTO.error(e.getCode(), "系统繁忙，请稍后重试");
    }

    /**
     * 处理参数验证异常（带指标收集）
     */
    @ExceptionHandler(ParamException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleParamException(ParamException e) {
        long startTime = System.nanoTime();

        log.warn("[参数异常] code={}, message={}", e.getCode(), e.getMessage());

        // 记录异常指标
        recordExceptionMetrics(e, startTime);

        return ResponseDTO.error(e.getCode(), e.getMessage());
    }

    // ==================== 验证异常处理 ====================

    /**
     * 处理方法参数验证异常 (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("[参数验证异常] message={}", message);
        return ResponseDTO.error("VALIDATION_ERROR", message);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("[绑定异常] message={}", message);
        return ResponseDTO.error("BIND_ERROR", message);
    }

    /**
     * 处理约束验证异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.warn("[约束验证异常] message={}", message);
        return ResponseDTO.error("CONSTRAINT_VIOLATION", message);
    }

    // ==================== HTTP相关异常处理 ====================

    /**
     * 处理HTTP方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResponseDTO<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("[HTTP方法不支持] method={}", e.getMethod());
        return ResponseDTO.error("METHOD_NOT_ALLOWED", "不支持的请求方法: " + e.getMethod());
    }

    /**
     * 处理HTTP消息不可读异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("[HTTP消息不可读] message={}", e.getMessage());
        return ResponseDTO.error("MESSAGE_NOT_READABLE", "请求消息格式错误");
    }

    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.warn("[缺少请求参数] parameter={}", e.getParameterName());
        return ResponseDTO.error("MISSING_PARAMETER", "缺少必需参数: " + e.getParameterName());
    }

    /**
     * 处理方法参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("[参数类型不匹配] parameter={}, requiredType={}", e.getName(), e.getRequiredType());
        return ResponseDTO.error("TYPE_MISMATCH", "参数类型错误: " + e.getName());
    }

    /**
     * 处理404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseDTO<Void> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("[404异常] url={}, method={}", e.getRequestURL(), e.getHttpMethod());
        return ResponseDTO.error("NOT_FOUND", "请求的资源不存在");
    }

    // ==================== 数据库相关异常处理 ====================

    /**
     * 处理数据库唯一约束冲突异常
     */
    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseDTO<Void> handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("[数据库唯一约束冲突] message={}", e.getMessage());
        return ResponseDTO.error("DUPLICATE_KEY", "数据已存在，违反唯一约束");
    }

    /**
     * 处理数据库完整性约束违反异常
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseDTO<Void> handleSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException e) {
        log.warn("[数据库完整性约束违反] message={}", e.getMessage());
        return ResponseDTO.error("CONSTRAINT_VIOLATION", "数据完整性约束违反");
    }

    // ==================== 文件上传相关异常处理 ====================

    /**
     * 处理文件上传大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn("[文件上传大小超限] maxSize={}", e.getMaxUploadSize());
        return ResponseDTO.error("FILE_TOO_LARGE", "上传文件大小超过限制");
    }

    // ==================== 视频相关异常处理 ====================

    /**
     * 处理视频设备异常
     */
    @ExceptionHandler(VideoDeviceException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleVideoDeviceException(VideoDeviceException e) {
        long startTime = System.nanoTime();
        String traceId = getTraceId();

        log.error("[视频设备异常] traceId={}, deviceId={}, errorCode={}, message={}",
                traceId, e.getDeviceId(), e.getErrorCode(), e.getMessage(), e);

        recordExceptionMetrics(e, startTime);
        return ResponseDTO.error(e.getErrorCode(), e.getMessage());
    }

    /**
     * 处理视频流异常
     */
    @ExceptionHandler(VideoStreamException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleVideoStreamException(VideoStreamException e) {
        long startTime = System.nanoTime();
        String traceId = getTraceId();

        log.error("[视频流异常] traceId={}, streamId={}, errorCode={}, message={}",
                traceId, e.getStreamId(), e.getErrorCode(), e.getMessage(), e);

        recordExceptionMetrics(e, startTime);
        return ResponseDTO.error(e.getErrorCode(), e.getMessage());
    }

    /**
     * 处理AI分析异常
     */
    @ExceptionHandler(AIAnalysisException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleAIAnalysisException(AIAnalysisException e) {
        long startTime = System.nanoTime();
        String traceId = getTraceId();

        log.error("[AI分析异常] traceId={}, analysisType={}, errorCode={}, message={}",
                traceId, e.getAnalysisType(), e.getErrorCode(), e.getMessage(), e);

        recordExceptionMetrics(e, startTime);
        return ResponseDTO.error(e.getErrorCode(), e.getMessage());
    }

    /**
     * 处理录像异常
     */
    @ExceptionHandler(VideoRecordingException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleVideoRecordingException(VideoRecordingException e) {
        long startTime = System.nanoTime();
        String traceId = getTraceId();

        log.error("[录像异常] traceId={}, recordingId={}, errorCode={}, message={}",
                traceId, e.getRecordingId(), e.getErrorCode(), e.getMessage(), e);

        recordExceptionMetrics(e, startTime);
        return ResponseDTO.error(e.getErrorCode(), e.getMessage());
    }

    // ==================== Flowable工作流异常处理 ====================
    // 注意：Flowable异常处理已移除，因为common-service不依赖Flowable
    // Flowable异常应在oa-service中捕获并转换为SystemException或BusinessException
    // 如果FlowableException未被捕获，会被下面的handleException方法处理

    // ==================== 通用异常处理 ====================

    /**
     * 处理IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("[非法参数异常] message={}", e.getMessage());
        return ResponseDTO.error("ILLEGAL_ARGUMENT", e.getMessage());
    }

    /**
     * 处理IllegalStateException
     */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleIllegalStateException(IllegalStateException e) {
        log.error("[非法状态异常] message={}", e.getMessage(), e);
        return ResponseDTO.error("ILLEGAL_STATE", "系统状态异常");
    }

    /**
     * 处理NullPointerException
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleNullPointerException(NullPointerException e) {
        log.error("[空指针异常] ", e);
        return ResponseDTO.error("NULL_POINTER", "系统内部错误");
    }

    /**
     * 处理其他未捕获的异常（带指标收集）
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleException(Exception e) {
        long startTime = System.nanoTime();
        String traceId = getTraceId();

        // 内存优化：避免字符串拼接，使用参数化日志
        log.error("[未知异常] traceId={}, error={}", traceId, e.getMessage(), e);

        // 记录异常指标（所有异常都记录）
        recordExceptionMetrics(e, startTime);

        return ResponseDTO.error("SYSTEM_ERROR", "系统内部错误，请稍后重试");
    }

    /**
     * 处理Throwable（最严重错误，带指标收集）
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleThrowable(Throwable t) {
        long startTime = System.nanoTime();
        String traceId = getTraceId();

        // 记录严重错误
        log.error("[严重错误] traceId={}, error={}", traceId, t.getMessage(), t);

        // 转换为Exception记录指标
        Exception exception = t instanceof Exception ? (Exception) t : new RuntimeException(t);
        recordExceptionMetrics(exception, startTime);

        return ResponseDTO.error("CRITICAL_ERROR", "系统严重错误");
    }
}
