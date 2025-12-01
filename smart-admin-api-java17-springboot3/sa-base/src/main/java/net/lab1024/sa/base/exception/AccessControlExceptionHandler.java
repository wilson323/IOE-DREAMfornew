package net.lab1024.sa.base.exception;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.exception.SmartException;
import net.lab1024.sa.base.common.code.SystemErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 闂ㄧ绯荤粺异常处理鍣? *
 * @author SmartAdmin Team
 * @date 2025/01/13
 */
@Slf4j
@RestControllerAdvice(basePackages = "net.lab1024.sa.base.module.controller")
public class AccessControlExceptionHandler {

    /**
     * 处理SmartException涓氬姟异常
     */
    @ExceptionHandler(SmartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleSmartException(SmartException e) {
        log.error("涓氬姟异常: {}", e.getMessage(), e);
        return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, e.getMessage());
    }

    /**
     * 处理设备异常
     */
    @ExceptionHandler(AccessDeviceException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleAccessDeviceException(AccessDeviceException e) {
        log.error("设备异常: {}", e.getMessage(), e);
        return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "设备操作失败: " + e.getMessage());
    }

    /**
     * 处理权限异常
     */
    @ExceptionHandler(AccessPermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseDTO<Void> handleAccessPermissionException(AccessPermissionException e) {
        log.error("权限异常: {}", e.getMessage(), e);
        return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "权限楠岃瘉失败: " + e.getMessage());
    }

    /**
     * 处理迁移炴帴异常
     */
    @ExceptionHandler(AccessConnectionException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ResponseDTO<Void> handleAccessConnectionException(AccessConnectionException e) {
        log.error("迁移炴帴异常: {}", e.getMessage(), e);
        return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "设备迁移炴帴失败: " + e.getMessage());
    }

    /**
     * 处理参数鏍￠獙异常 - @Valid
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("参数鏍￠獙失败", e);

        List<String> errorMessages = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.toList());

        String message = "参数鏍￠獙失败: " + String.join(", ", errorMessages);
        return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, message);
    }

    /**
     * 处理参数缁戝畾异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleBindException(BindException e) {
        log.error("参数缁戝畾失败", e);

        List<String> errorMessages = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.toList());

        String message = "参数缁戝畾失败: " + String.join(", ", errorMessages);
        return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, message);
    }

    /**
     * 处理绾︽潫鏍￠獙异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("绾︽潫鏍￠獙失败", e);

        List<String> errorMessages = e.getConstraintViolations().stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.toList());

        String message = "绾︽潫鏍￠獙失败: " + String.join(", ", errorMessages);
        return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, message);
    }

    /**
     * 处理数据搴撳紓甯?     */
    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleDataAccessException(org.springframework.dao.DataAccessException e) {
        log.error("数据库异常", e);
        return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "数据库操作失败，请稍后重试");
    }

    /**
     * 处理运行鏃跺紓甯?     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常", e);
        return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "系统内部错误，请稍后重试");
    }

    /**
     * 处理鍏朵粬鏈煡异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleException(Exception e) {
        log.error("未知异常", e);
        return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "系统异常，请联系管理员");
    }

    /**
     * 闂ㄧ设备异常
     */
    public static class AccessDeviceException extends SmartException {
        public AccessDeviceException(String message) {
            super(message);
        }

        public AccessDeviceException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * 闂ㄧ权限异常
     */
    public static class AccessPermissionException extends SmartException {
        public AccessPermissionException(String message) {
            super(message);
        }

        public AccessPermissionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * 闂ㄧ迁移炴帴异常
     */
    public static class AccessConnectionException extends SmartException {
        public AccessConnectionException(String message) {
            super(message);
        }

        public AccessConnectionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
