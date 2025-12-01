package net.lab1024.sa.admin.common.exception;

// import lombok.extern.slf4j.Slf4j; // 使用手动logger定义，避免Lombok冲突
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;

import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.exception.BusinessException;
import net.lab1024.sa.base.common.exception.SmartException;
import net.lab1024.sa.base.common.util.SmartRequestUtil;
import net.lab1024.sa.base.common.util.SmartStringUtil;

/**
 * 全局异常处理器
 * 统一处理系统异常，消除Controller中的重复try-catch代码
 * 严格遵循repowiki四层架构规范：Controller层统一异常处理
 *
 * @author SmartAdmin Team
 * @since 2025-11-18
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 业务异常处理
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseDTO<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: {} - {}", request.getRequestURI(), e.getMessage());
        return ResponseDTO.error(e.getMessage());
    }

    /**
     * Smart系统异常处理
     */
    @ExceptionHandler(SmartException.class)
    public ResponseDTO<Void> handleSmartException(SmartException e, HttpServletRequest request) {
        log.warn("系统异常: {} - {}", request.getRequestURI(), e.getMessage());
        return ResponseDTO.error(e.getMessage());
    }

    /**
     * 参数验证异常处理 - @Valid
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseDTO<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.warn("参数验证失败: {} - {}", request.getRequestURI(), e.getMessage());

        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        StringBuilder errorMessage = new StringBuilder("参数验证失败: ");

        for (int i = 0; i < fieldErrors.size(); i++) {
            FieldError fieldError = fieldErrors.get(i);
            errorMessage.append(fieldError.getField()).append(" ").append(fieldError.getDefaultMessage());
            if (i < fieldErrors.size() - 1) {
                errorMessage.append("; ");
            }
        }

        return ResponseDTO.error(errorMessage.toString());
    }

    /**
     * 参数验证异常处理 - @Validated
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseDTO<Void> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        log.warn("参数约束验证失败: {} - {}", request.getRequestURI(), e.getMessage());

        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        StringBuilder errorMessage = new StringBuilder("参数约束验证失败: ");

        int i = 0;
        for (ConstraintViolation<?> violation : violations) {
            errorMessage.append(violation.getMessage());
            if (i < violations.size() - 1) {
                errorMessage.append("; ");
            }
            i++;
        }

        return ResponseDTO.error(errorMessage.toString());
    }

    /**
     * 参数绑定异常处理
     */
    @ExceptionHandler(BindException.class)
    public ResponseDTO<Void> handleBindException(BindException e, HttpServletRequest request) {
        log.warn("参数绑定失败: {} - {}", request.getRequestURI(), e.getMessage());

        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        StringBuilder errorMessage = new StringBuilder("参数绑定失败: ");

        for (int i = 0; i < fieldErrors.size(); i++) {
            FieldError fieldError = fieldErrors.get(i);
            errorMessage.append(fieldError.getField()).append(" ").append(fieldError.getDefaultMessage());
            if (i < fieldErrors.size() - 1) {
                errorMessage.append("; ");
            }
        }

        return ResponseDTO.error(errorMessage.toString());
    }

    /**
     * 缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseDTO<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        log.warn("缺少请求参数: {} - {}", request.getRequestURI(), e.getMessage());
        String message = String.format("缺少请求参数: %s", e.getParameterName());
        return ResponseDTO.error(message);
    }

    /**
     * 参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseDTO<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        log.warn("参数类型不匹配: {} - {}", request.getRequestURI(), e.getMessage());
        String message = String.format("参数类型不匹配: %s，期望类型: %s", e.getName(), e.getRequiredType().getSimpleName());
        return ResponseDTO.error(message);
    }

    /**
     * 请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseDTO<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.warn("请求方法不支持: {} - {}", request.getRequestURI(), e.getMessage());
        String message = String.format("请求方法不支持: %s", e.getMethod());
        return ResponseDTO.error(message);
    }

    /**
     * 404异常处理
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseDTO<Void> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        log.warn("请求路径不存在: {} - {}", request.getRequestURI(), e.getRequestURL());
        return ResponseDTO.error("请求路径不存在");
    }

    /**
     * 非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseDTO<Void> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("非法参数: {} - {}", request.getRequestURI(), e.getMessage());
        return ResponseDTO.error("非法参数: " + e.getMessage());
    }

    /**
     * 非法状态异常
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseDTO<Void> handleIllegalStateException(IllegalStateException e, HttpServletRequest request) {
        log.warn("非法状态: {} - {}", request.getRequestURI(), e.getMessage());
        return ResponseDTO.error("系统状态异常: " + e.getMessage());
    }

    /**
     * 空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseDTO<Void> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        log.error("空指针异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        return ResponseDTO.error("系统内部错误，请联系管理员");
    }

    /**
     * 数字格式异常
     */
    @ExceptionHandler(NumberFormatException.class)
    public ResponseDTO<Void> handleNumberFormatException(NumberFormatException e, HttpServletRequest request) {
        log.warn("数字格式错误: {} - {}", request.getRequestURI(), e.getMessage());
        return ResponseDTO.error("数字格式错误: " + e.getMessage());
    }

    /**
     * 运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseDTO<Void> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("运行时异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        return ResponseDTO.error("系统运行异常，请联系管理员");
    }

    /**
     * 通用异常处理
     */
    @ExceptionHandler(Exception.class)
    public ResponseDTO<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: {} - {}", request.getRequestURI(), e.getMessage(), e);

        // 避免敏感信息泄露
        String errorMessage = "系统异常，请联系管理员";

        // 如果是开发环境，可以显示详细错误信息
        // if (isDevEnvironment()) {
        //     errorMessage = e.getMessage();
        // }

        return ResponseDTO.error(errorMessage);
    }

    /**
     * 检查字符串是否为空或null
     */
    private boolean isEmpty(String str) {
        return SmartStringUtil.isEmpty(str);
    }
}