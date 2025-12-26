package net.lab1024.sa.access.exception;

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
 * 门禁服务全局异常处理器
 * <p>
 * 专门处理门禁服务的异常情况 提供详细的错误分类和标准化响应 集成异常监控和指标收集 支持多模态认证和反潜回规则异常处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
@Slf4j
@RestControllerAdvice(basePackages = "net.lab1024.sa.access")
public class GlobalExceptionHandler {

    /**
     * 处理门禁业务异常
     */
    @ExceptionHandler(AccessBusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleAccessBusinessException (AccessBusinessException e) {
        log.warn ("[门禁业务异常] code={}, message={}, businessId={}", e.getCode (), e.getMessage (), e.getBusinessId ());

        // 特殊处理安全相关异常
        if (isSecurityException (e.getErrorCode ().getCode ())) {
            log.error ("[安全异常] 检测到安全违规: code={}, businessId={}", e.getCode (), e.getBusinessId ());
        }

        recordExceptionMetrics ("AccessBusinessException", e.getCode ());

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
     * 处理门禁特殊异常
     */
    @ExceptionHandler({ AuthenticationException.class, AuthorizationException.class })
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseDTO<Void> handleAuthenticationException (Exception e) {
        log.error ("[认证授权异常] message={}", e.getMessage (), e);

        String exceptionType = e.getClass ().getSimpleName ();
        recordExceptionMetrics (exceptionType, "AUTHENTICATION_ERROR");

        return ResponseDTO.error ("AUTHENTICATION_ERROR", "认证授权失败: " + e.getMessage ());
    }

    /**
     * 处理生物特征识别异常
     */
    @ExceptionHandler(BiometricException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleBiometricException (BiometricException e) {
        log.warn ("[生物识别异常] type={}, message={}", e.getBiometricType (), e.getMessage ());

        recordExceptionMetrics ("BiometricException", "BIOMETRIC_ERROR");

        return ResponseDTO.error ("BIOMETRIC_ERROR", e.getMessage ());
    }

    /**
     * 判断是否为安全相关异常
     *
     * @param errorCode
     *            错误码
     * @return 是否为安全异常
     */
    private boolean isSecurityException (String errorCode) {
        return errorCode.equals (AccessBusinessException.ErrorCode.SECURITY_BREACH_DETECTED.getCode ())
                || errorCode.equals (AccessBusinessException.ErrorCode.ANTI_PASSBACK_VIOLATION.getCode ())
                || errorCode.equals (AccessBusinessException.ErrorCode.AREA_SECURITY_BREACH.getCode ())
                || errorCode.equals (AccessBusinessException.ErrorCode.ABNORMAL_ACCESS_PATTE.getCode ());
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
     * 生物特征识别异常类（门禁服务专用）
     */
    public static class BiometricException extends RuntimeException {
        private final String biometricType;

        public BiometricException (String biometricType, String message) {
            super (message);
            this.biometricType = biometricType;
        }

        public String getBiometricType () {
            return biometricType;
        }
    }

    /**
     * 认证异常类（门禁服务专用）
     */
    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException (String message) {
            super (message);
        }
    }

    /**
     * 授权异常类（门禁服务专用）
     */
    public static class AuthorizationException extends RuntimeException {
        public AuthorizationException (String message) {
            super (message);
        }
    }
}
