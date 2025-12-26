package net.lab1024.sa.video.exception;

import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.SystemException;

import lombok.extern.slf4j.Slf4j;

/**
 * 视频服务全局异常处理器
 * <p>
 * 专门处理视频服务的异常情况 提供详细的错误分类和标准化响应 集成异常监控和指标收集 支持视频流媒体、录像、AI分析、边缘计算等复杂场景异常处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
@Slf4j
@RestControllerAdvice(basePackages = "net.lab1024.sa.video")
public class GlobalExceptionHandler {

    /**
     * 处理视频业务异常
     */
    @ExceptionHandler(VideoBusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleVideoBusinessException (VideoBusinessException e) {
        log.warn ("[视频业务异常] code={}, message={}, businessId={}", e.getCode (), e.getMessage (), e.getBusinessId ());

        // 特殊处理视频设备异常
        if (isVideoDeviceException (e.getErrorCode ().getCode ())) {
            log.error ("[视频设备异常] 设备故障: code={}, businessId={}", e.getCode (), e.getBusinessId ());
        }

        // 特殊处理AI分析异常
        if (isAIAnalysisException (e.getErrorCode ().getCode ())) {
            log.warn ("[AI分析异常] AI处理失败: code={}, businessId={}", e.getCode (), e.getBusinessId ());
        }

        recordExceptionMetrics ("VideoBusinessException", e.getCode ());

        return ResponseDTO.error (e.getCode (), e.getMessage ());
    }

    /**
     * 处理通用业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleBusinessException (BusinessException e) {
        log.warn ("[业务异常] code={}, message={}", e.getCode (), e.getMessage ());

        recordExceptionMetrics ("BusinessException", e.getCode ());

        return ResponseDTO.error (e.getCode (), e.getMessage ());
    }

    /**
     * 处理系统异常
     */
    @ExceptionHandler(SystemException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleSystemException (SystemException e) {
        log.error ("[系统异常] code={}, message={}", e.getCode (), e.getMessage (), e);

        recordExceptionMetrics ("SystemException", e.getCode ());

        return ResponseDTO.error (e.getCode (), e.getMessage ());
    }

    /**
     * 处理参数验证异常 - @RequestBody参数验证失败
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleMethodArgumentNotValidException (MethodArgumentNotValidException e) {
        String message = e.getBindingResult ().getFieldErrors ().stream ().map (FieldError::getDefaultMessage)
                .collect (Collectors.joining (", "));

        log.warn ("[参数验证异常] @RequestBody验证失败: {}", message);

        recordExceptionMetrics ("MethodArgumentNotValidException", "VALIDATION_FAILED");

        return ResponseDTO.error ("PARAMETER_VALIDATION_ERROR", "参数验证失败: " + message);
    }

    /**
     * 处理参数验证异常 - 表单参数验证失败
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleBindException (BindException e) {
        String message = e.getBindingResult ().getFieldErrors ().stream ().map (FieldError::getDefaultMessage)
                .collect (Collectors.joining (", "));

        log.warn ("[参数验证异常] 表单参数验证失败: {}", message);

        recordExceptionMetrics ("BindException", "VALIDATION_FAILED");

        return ResponseDTO.error ("PARAMETER_VALIDATION_ERROR", "参数验证失败: " + message);
    }

    /**
     * 处理参数验证异常 - @RequestParam/@PathVariable参数验证失败
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleConstraintViolationException (ConstraintViolationException e) {
        Set<ConstraintViolation< ? >> violations = e.getConstraintViolations ();
        String message = violations.stream ().map (ConstraintViolation::getMessage).collect (Collectors.joining (", "));

        log.warn ("[参数验证异常] @RequestParam/@PathVariable验证失败: {}", message);

        recordExceptionMetrics ("ConstraintViolationException", "VALIDATION_FAILED");

        return ResponseDTO.error ("PARAMETER_VALIDATION_ERROR", "参数验证失败: " + message);
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleIllegalArgumentException (IllegalArgumentException e) {
        log.warn ("[非法参数异常] message={}", e.getMessage ());

        recordExceptionMetrics ("IllegalArgumentException", "ILLEGAL_ARGUMENT");

        return ResponseDTO.error ("ILLEGAL_ARGUMENT", e.getMessage ());
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleRuntimeException (RuntimeException e) {
        log.error ("[运行时异常] message={}", e.getMessage (), e);

        recordExceptionMetrics ("RuntimeException", "RUNTIME_ERROR");

        return ResponseDTO.error ("RUNTIME_ERROR", "系统运行时异常，请稍后重试");
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleException (Exception e) {
        log.error ("[系统异常] 未捕获的异常: {}", e.getMessage (), e);

        recordExceptionMetrics ("Exception", "UNKNOWN_ERROR");

        return ResponseDTO.error ("SYSTEM_ERROR", "系统繁忙，请稍后重试");
    }

    /**
     * 处理视频流异常
     */
    @ExceptionHandler({ VideoStreamException.class, VideoConnectionException.class })
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ResponseDTO<Void> handleVideoStreamException (Exception e) {
        log.error ("[视频流异常] 视频流处理失败: {}", e.getMessage (), e);

        String exceptionType = e.getClass ().getSimpleName ();
        recordExceptionMetrics (exceptionType, "VIDEO_STREAM_ERROR");

        return ResponseDTO.error ("VIDEO_STREAM_ERROR", String.format ("视频流异常: %s", e.getMessage ()));
    }

    /**
     * 处理录像存储异常
     */
    @ExceptionHandler(VideoStorageException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleVideoStorageException (VideoStorageException e) {
        log.error ("[录像存储异常] 存储类型={}, 容量={}, 错误={}", e.getStorageType (), e.getUsedCapacity (), e.getMessage (), e);

        recordExceptionMetrics ("VideoStorageException", "VIDEO_STORAGE_ERROR");

        return ResponseDTO.error ("VIDEO_STORAGE_ERROR", String.format ("录像存储异常: %s", e.getMessage ()));
    }

    /**
     * 处理AI分析异常
     */
    @ExceptionHandler(AIAnalysisException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleAIAnalysisException (AIAnalysisException e) {
        log.error ("[AI分析异常] 算法类型={}, 处理时长={}, 错误={}", e.getAlgorithmType (), e.getProcessingTime (), e.getMessage (),
                e);

        recordExceptionMetrics ("AIAnalysisException", "AI_ANALYSIS_ERROR");

        return ResponseDTO.error ("AI_ANALYSIS_ERROR", String.format ("AI分析异常: %s", e.getMessage ()));
    }

    /**
     * 处理边缘计算异常
     */
    @ExceptionHandler(EdgeComputingException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ResponseDTO<Void> handleEdgeComputingException (EdgeComputingException e) {
        log.error ("[边缘计算异常] 设备ID={}, 计算类型={}, 错误={}", e.getDeviceId (), e.getComputingType (), e.getMessage (), e);

        recordExceptionMetrics ("EdgeComputingException", "EDGE_COMPUTING_ERROR");

        return ResponseDTO.error ("EDGE_COMPUTING_ERROR", String.format ("边缘计算异常: %s", e.getMessage ()));
    }

    /**
     * 判断是否为视频设备相关异常
     *
     * @param errorCode
     *            错误码
     * @return 是否为视频设备异常
     */
    private boolean isVideoDeviceException (String errorCode) {
        return errorCode.equals (VideoBusinessException.ErrorCode.VIDEO_DEVICE_OFFLINE.getCode ())
                || errorCode.equals (VideoBusinessException.ErrorCode.VIDEO_DEVICE_MALFUNCTION.getCode ())
                || errorCode.equals (VideoBusinessException.ErrorCode.VIDEO_STREAM_CONNECTION_FAILED.getCode ())
                || errorCode.equals (VideoBusinessException.ErrorCode.VIDEO_DEVICE_NOT_FOUND.getCode ());
    }

    /**
     * 判断是否为AI分析相关异常
     *
     * @param errorCode
     *            错误码
     * @return 是否为AI分析异常
     */
    private boolean isAIAnalysisException (String errorCode) {
        return errorCode.equals (VideoBusinessException.ErrorCode.FACE_RECOGNITION_FAILED.getCode ())
                || errorCode.equals (VideoBusinessException.ErrorCode.OBJECT_DETECTION_FAILED.getCode ())
                || errorCode.equals (VideoBusinessException.ErrorCode.BEHAVIOR_ANALYSIS_FAILED.getCode ())
                || errorCode.equals (VideoBusinessException.ErrorCode.VIDEO_ANALYSIS_MODEL_NOT_FOUND.getCode ());
    }

    /**
     * 记录异常指标
     *
     * @param exceptionType
     *            异常类型
     * @param errorCode
     *            错误码
     */
    private void recordExceptionMetrics (String exceptionType, String errorCode) {
        try {
            // TODO: 集成ExceptionMetricsCollector
            // ExceptionMetricsCollector.recordException(exceptionType, errorCode);
            log.debug ("[异常指标] 记录异常指标: type={}, code={}", exceptionType, errorCode);
        } catch (Exception ex) {
            // 指标记录失败不应影响主流程
            log.warn ("[异常指标] 记录异常指标失败: {}", ex.getMessage ());
        }
    }

    /**
     * 视频流异常类（视频服务专用）
     */
    public static class VideoStreamException extends RuntimeException {
        private final String streamId;
        private final String deviceCode;

        public VideoStreamException (String streamId, String deviceCode, String message) {
            super (message);
            this.streamId = streamId;
            this.deviceCode = deviceCode;
        }

        public String getStreamId () {
            return streamId;
        }

        public String getDeviceCode () {
            return deviceCode;
        }
    }

    /**
     * 视频连接异常类（视频服务专用）
     */
    public static class VideoConnectionException extends RuntimeException {
        private final String deviceCode;
        private final String connectionType;

        public VideoConnectionException (String deviceCode, String connectionType, String message) {
            super (message);
            this.deviceCode = deviceCode;
            this.connectionType = connectionType;
        }

        public String getDeviceCode () {
            return deviceCode;
        }

        public String getConnectionType () {
            return connectionType;
        }
    }

    /**
     * 录像存储异常类（视频服务专用）
     */
    public static class VideoStorageException extends RuntimeException {
        private final String storageType;
        private final Long usedCapacity;

        public VideoStorageException (String storageType, Long usedCapacity, String message, Throwable cause) {
            super (message, cause);
            this.storageType = storageType;
            this.usedCapacity = usedCapacity;
        }

        public String getStorageType () {
            return storageType;
        }

        public Long getUsedCapacity () {
            return usedCapacity;
        }
    }

    /**
     * AI分析异常类（视频服务专用）
     */
    public static class AIAnalysisException extends RuntimeException {
        private final String algorithmType;
        private final Long processingTime;

        public AIAnalysisException (String algorithmType, Long processingTime, String message, Throwable cause) {
            super (message, cause);
            this.algorithmType = algorithmType;
            this.processingTime = processingTime;
        }

        public String getAlgorithmType () {
            return algorithmType;
        }

        public Long getProcessingTime () {
            return processingTime;
        }
    }

    /**
     * 边缘计算异常类（视频服务专用）
     */
    public static class EdgeComputingException extends RuntimeException {
        private final String deviceId;
        private final String computingType;

        public EdgeComputingException (String deviceId, String computingType, String message, Throwable cause) {
            super (message, cause);
            this.deviceId = deviceId;
            this.computingType = computingType;
        }

        public String getDeviceId () {
            return deviceId;
        }

        public String getComputingType () {
            return computingType;
        }
    }
}
