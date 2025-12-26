package net.lab1024.sa.device.comm.exception;

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
import net.lab1024.sa.device.comm.protocol.exception.ProtocolProcessException;
import net.lab1024.sa.device.exception.DeviceBusinessException;

import lombok.extern.slf4j.Slf4j;

/**
 * 设备通讯服务全局异常处理器
 * <p>
 * 专门处理设备通讯服务的异常情况 提供详细的错误分类和标准化响应 集成异常监控和指标收集
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
@Slf4j
@RestControllerAdvice(basePackages = "net.lab1024.sa.device.comm")
public class GlobalExceptionHandler {

    /**
     * 处理设备业务异常
     */
    @ExceptionHandler(DeviceBusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleDeviceBusinessException (DeviceBusinessException e) {
        log.warn ("[设备业务异常] code={}, message={}, businessId={}", e.getErrorCode (), e.getMessage (), e.getBusinessId ());

        // 记录异常指标
        recordExceptionMetrics ("DeviceBusinessException", e.getErrorCode ().name ());

        return ResponseDTO.error (e.getErrorCode ().name (), e.getMessage ());
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
     * 处理设备协议异常（特殊处理）
     */
    @ExceptionHandler({ ProtocolProcessException.class })
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ResponseDTO<Void> handleDeviceException (ProtocolProcessException e) {
        log.error ("[设备异常] 设备通信异常: {}", e.getMessage (), e);

        String exceptionType = e.getClass ().getSimpleName ();
        recordExceptionMetrics (exceptionType, "DEVICE_COMMUNICATION_ERROR");

        return ResponseDTO.error ("DEVICE_COMMUNICATION_ERROR", "设备通信异常: " + e.getMessage ());
    }

    /**
     * 处理设备连接异常
     */
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ResponseDTO<Void> handleDeviceConnectionException (DeviceBusinessException e) {
        if (e.getErrorCode () == DeviceBusinessException.ErrorCode.DEVICE_CONNECTION_FAILED
                || e.getErrorCode () == DeviceBusinessException.ErrorCode.DEVICE_CONNECTION_TIMEOUT) {
            log.error ("[设备连接异常] 设备连接失败: {}", e.getMessage (), e);
            recordExceptionMetrics ("DeviceConnectionException", "DEVICE_CONNECTION_ERROR");
            return ResponseDTO.error ("DEVICE_CONNECTION_ERROR", e.getMessage ());
        }
        // 其他设备业务异常委托给 handleDeviceBusinessException 处理
        return handleDeviceBusinessException (e);
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
     * 设备连接异常类（设备通讯服务专用）
     */
    public static class DeviceConnectionException extends RuntimeException {
        public DeviceConnectionException (String message) {
            super (message);
        }
    }

    /**
     * 设备超时异常类（设备通讯服务专用）
     */
    public static class DeviceTimeoutException extends RuntimeException {
        public DeviceTimeoutException (String message) {
            super (message);
        }
    }
}
