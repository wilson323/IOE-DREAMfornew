package net.lab1024.sa.video.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.response.ResponseDTO;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 视频服务统一异常处理器
 * <p>
 * 提供统一的异常处理机制：
 * 1. 业务异常处理
 * 2. 参数验证异常处理
 * 3. 系统异常处理
 * 4. 视频相关特定异常处理
 * 5. 错误日志记录
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@RestControllerAdvice
public class VideoExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ResponseDTO<Void>> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("[业务异常] code={}, message={}, path={}", e.getCode(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.ok(ResponseDTO.error(e.getCode(), e.getMessage()));
    }

    /**
     * 处理系统异常
     */
    @ExceptionHandler(SystemException.class)
    public ResponseEntity<ResponseDTO<Void>> handleSystemException(SystemException e, HttpServletRequest request) {
        log.error("[系统异常] code={}, message={}, path={}", e.getCode(), e.getMessage(), request.getRequestURI(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDTO.error(e.getCode(), e.getMessage()));
    }

    /**
     * 处理参数验证异常（@Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDTO<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e, HttpServletRequest request) {

        log.warn("[参数验证异常] path={}, errors={}", request.getRequestURI(), e.getMessage());

        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest()
                .body(ResponseDTO.error("VALIDATION_ERROR", errorMessage));
    }

    /**
     * 处理参数验证异常（@Validated）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseDTO<Void>> handleConstraintViolationException(
            ConstraintViolationException e, HttpServletRequest request) {

        log.warn("[约束验证异常] path={}, errors={}", request.getRequestURI(), e.getMessage());

        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest()
                .body(ResponseDTO.error("CONSTRAINT_VIOLATION", errorMessage));
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ResponseDTO<Void>> handleBindException(BindException e, HttpServletRequest request) {
        log.warn("[绑定异常] path={}, errors={}", request.getRequestURI(), e.getMessage());

        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest()
                .body(ResponseDTO.error("BIND_ERROR", errorMessage));
    }

    /**
     * 处理方法参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseDTO<Void>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {

        log.warn("[参数类型不匹配] path={}, parameter={}, expectedType={}",
                request.getRequestURI(), e.getName(), e.getRequiredType().getSimpleName());

        String errorMessage = String.format("参数 '%s' 类型错误，期望类型: %s",
                e.getName(), e.getRequiredType().getSimpleName());

        return ResponseEntity.badRequest()
                .body(ResponseDTO.error("TYPE_MISMATCH", errorMessage));
    }

    /**
     * 处理文件上传大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ResponseDTO<Void>> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException e, HttpServletRequest request) {

        log.warn("[文件上传超限] path={}, maxSize={}", request.getRequestURI(), e.getMaxUploadSize());

        String errorMessage = String.format("上传文件大小超过限制，最大允许: %d bytes", e.getMaxUploadSize());
        return ResponseEntity.badRequest()
                .body(ResponseDTO.error("FILE_SIZE_EXCEEDED", errorMessage));
    }

    /**
     * 处理视频设备相关异常
     */
    @ExceptionHandler(VideoDeviceException.class)
    public ResponseEntity<ResponseDTO<Void>> handleVideoDeviceException(
            VideoDeviceException e, HttpServletRequest request) {

        log.error("[视频设备异常] deviceId={}, errorCode={}, message={}, path={}",
                e.getDeviceId(), e.getErrorCode(), e.getMessage(), request.getRequestURI(), e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDTO.error(e.getErrorCode(), e.getMessage()));
    }

    /**
     * 处理视频流相关异常
     */
    @ExceptionHandler(VideoStreamException.class)
    public ResponseEntity<ResponseDTO<Void>> handleVideoStreamException(
            VideoStreamException e, HttpServletRequest request) {

        log.error("[视频流异常] streamId={}, errorCode={}, message={}, path={}",
                e.getStreamId(), e.getErrorCode(), e.getMessage(), request.getRequestURI(), e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDTO.error(e.getErrorCode(), e.getMessage()));
    }

    /**
     * 处理AI分析相关异常
     */
    @ExceptionHandler(AIAnalysisException.class)
    public ResponseEntity<ResponseDTO<Void>> handleAIAnalysisException(
            AIAnalysisException e, HttpServletRequest request) {

        log.error("[AI分析异常] analysisType={}, errorCode={}, message={}, path={}",
                e.getAnalysisType(), e.getErrorCode(), e.getMessage(), request.getRequestURI(), e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDTO.error(e.getErrorCode(), e.getMessage()));
    }

    /**
     * 处理录像相关异常
     */
    @ExceptionHandler(VideoRecordingException.class)
    public ResponseEntity<ResponseDTO<Void>> handleVideoRecordingException(
            VideoRecordingException e, HttpServletRequest request) {

        log.error("[录像异常] recordingId={}, errorCode={}, message={}, path={}",
                e.getRecordingId(), e.getErrorCode(), e.getMessage(), request.getRequestURI(), e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDTO.error(e.getErrorCode(), e.getMessage()));
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO<Void>> handleException(Exception e, HttpServletRequest request) {
        log.error("[系统异常] path={}, error={}", request.getRequestURI(), e.getMessage(), e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDTO.error("SYSTEM_ERROR", "系统内部错误，请稍后重试"));
    }

    // ========== 自定义异常类 ==========

    /**
     * 视频设备异常
     */
    public static class VideoDeviceException extends RuntimeException {
        private final Long deviceId;
        private final String errorCode;

        public VideoDeviceException(Long deviceId, String errorCode, String message) {
            super(message);
            this.deviceId = deviceId;
            this.errorCode = errorCode;
        }

        public Long getDeviceId() {
            return deviceId;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }

    /**
     * 视频流异常
     */
    public static class VideoStreamException extends RuntimeException {
        private final String streamId;
        private final String errorCode;

        public VideoStreamException(String streamId, String errorCode, String message) {
            super(message);
            this.streamId = streamId;
            this.errorCode = errorCode;
        }

        public String getStreamId() {
            return streamId;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }

    /**
     * AI分析异常
     */
    public static class AIAnalysisException extends RuntimeException {
        private final String analysisType;
        private final String errorCode;

        public AIAnalysisException(String analysisType, String errorCode, String message) {
            super(message);
            this.analysisType = analysisType;
            this.errorCode = errorCode;
        }

        public String getAnalysisType() {
            return analysisType;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }

    /**
     * 录像异常
     */
    public static class VideoRecordingException extends RuntimeException {
        private final String recordingId;
        private final String errorCode;

        public VideoRecordingException(String recordingId, String errorCode, String message) {
            super(message);
            this.recordingId = recordingId;
            this.errorCode = errorCode;
        }

        public String getRecordingId() {
            return recordingId;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }

    // ========== 异常创建工具方法 ==========

    /**
     * 创建视频设备异常
     */
    public static VideoDeviceException createVideoDeviceException(Long deviceId, String errorCode, String message) {
        return new VideoDeviceException(deviceId, errorCode, message);
    }

    /**
     * 创建视频流异常
     */
    public static VideoStreamException createVideoStreamException(String streamId, String errorCode, String message) {
        return new VideoStreamException(streamId, errorCode, message);
    }

    /**
     * 创建AI分析异常
     */
    public static AIAnalysisException createAIAnalysisException(String analysisType, String errorCode, String message) {
        return new AIAnalysisException(analysisType, errorCode, message);
    }

    /**
     * 创建录像异常
     */
    public static VideoRecordingException createVideoRecordingException(String recordingId, String errorCode, String message) {
        return new VideoRecordingException(recordingId, errorCode, message);
    }
}