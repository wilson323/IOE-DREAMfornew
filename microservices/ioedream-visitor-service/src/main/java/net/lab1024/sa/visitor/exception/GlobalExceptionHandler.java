package net.lab1024.sa.visitor.exception;

import java.time.LocalDateTime;
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
 * 访客服务全局异常处理器
 * <p>
 * 专门处理访客服务的异常情况 提供详细的错误分类和标准化响应 集成异常监控和指标收集 支持访客预约、审批、出入管理、通行证、车辆等复杂业务场景异常处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
@Slf4j
@RestControllerAdvice(basePackages = "net.lab1024.sa.visitor")
public class GlobalExceptionHandler {

    /**
     * 处理访客业务异常
     */
    @ExceptionHandler(VisitorBusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleVisitorBusinessException (VisitorBusinessException e) {
        log.warn ("[访客业务异常] code={}, message={}, businessId={}", e.getCode (), e.getMessage (), e.getBusinessId ());

        // 特殊处理安全相关异常
        if (isSecurityException (e.getCode ())) {
            log.error ("[安全异常] 检测到访客安全违规: code={}, businessId={}", e.getCode (), e.getBusinessId ());
        }

        // 特殊处理审批工作流异常
        if (isApprovalException (e.getCode ())) {
            log.warn ("[审批异常] 审批流程异常: code={}, businessId={}", e.getCode (), e.getBusinessId ());
        }

        recordExceptionMetrics ("VisitorBusinessException", e.getCode ());

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
     * 处理访客预约异常
     */
    @ExceptionHandler(VisitorAppointmentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleVisitorAppointmentException (VisitorAppointmentException e) {
        log.warn ("[访客预约异常] 预约ID={}, 访客姓名={}, 预约时间={}", e.getAppointmentId (), e.getVisitorName (),
                e.getAppointmentTime ());

        recordExceptionMetrics ("VisitorAppointmentException", "APPOINTMENT_ERROR");

        return ResponseDTO.error ("APPOINTMENT_ERROR", String.format ("访客预约异常: %s", e.getMessage ()));
    }

    /**
     * 处理访客审批异常
     */
    @ExceptionHandler(VisitorApprovalException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleVisitorApprovalException (VisitorApprovalException e) {
        log.warn ("[访客审批异常] 审批ID={}, 审批人={}, 审批状态={}", e.getApprovalId (), e.getApproverName (),
                e.getApprovalStatus ());

        recordExceptionMetrics ("VisitorApprovalException", "APPROVAL_ERROR");

        return ResponseDTO.error ("APPROVAL_ERROR", String.format ("访客审批异常: %s", e.getMessage ()));
    }

    /**
     * 处理访客出入异常
     */
    @ExceptionHandler(VisitorAccessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleVisitorAccessException (VisitorAccessException e) {
        log.warn ("[访客出入异常] 访客ID={}, 访问区域={}, 出入时间={}", e.getVisitorId (), e.getAccessArea (), e.getAccessTime ());

        recordExceptionMetrics ("VisitorAccessException", "ACCESS_ERROR");

        return ResponseDTO.error ("ACCESS_ERROR", String.format ("访客出入异常: %s", e.getMessage ()));
    }

    /**
     * 处理访客通行证异常
     */
    @ExceptionHandler(VisitorPassException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleVisitorPassException (VisitorPassException e) {
        log.warn ("[访客通行证异常] 通行证ID={}, 访客姓名={}, 有效期={}", e.getPassId (), e.getVisitorName (), e.getExpiryTime ());

        recordExceptionMetrics ("VisitorPassException", "PASS_ERROR");

        return ResponseDTO.error ("PASS_ERROR", String.format ("访客通行证异常: %s", e.getMessage ()));
    }

    /**
     * 处理访客车辆异常
     */
    @ExceptionHandler(VisitorVehicleException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleVisitorVehicleException (VisitorVehicleException e) {
        log.warn ("[访客车辆异常] 车牌号={}, 访客姓名={}, 车辆类型={}", e.getLicensePlate (), e.getVisitorName (), e.getVehicleType ());

        recordExceptionMetrics ("VisitorVehicleException", "VEHICLE_ERROR");

        return ResponseDTO.error ("VEHICLE_ERROR", String.format ("访客车辆异常: %s", e.getMessage ()));
    }

    /**
     * 判断是否为安全相关异常
     *
     * @param errorCode
     *            错误码
     * @return 是否为安全异常
     */
    private boolean isSecurityException (String errorCode) {
        return errorCode.equals (VisitorBusinessException.ErrorCode.VISITOR_SECURITY_BREACH_DETECTED.getCode ())
                || errorCode.equals (VisitorBusinessException.ErrorCode.BLACKLIST_VISITOR_DETECTED.getCode ())
                || errorCode.equals (VisitorBusinessException.ErrorCode.SUSPICIOUS_BEHAVIOR_DETECTED.getCode ())
                || errorCode
                        .equals (VisitorBusinessException.ErrorCode.VISITOR_IDENTITY_VERIFICATION_FAILED.getCode ());
    }

    /**
     * 判断是否为审批相关异常
     *
     * @param errorCode
     *            错误码
     * @return 是否为审批异常
     */
    private boolean isApprovalException (String errorCode) {
        return errorCode.equals (VisitorBusinessException.ErrorCode.APPROVAL_WORKFLOW_TIMEOUT.getCode ())
                || errorCode.equals (VisitorBusinessException.ErrorCode.APPROVER_NOT_FOUND.getCode ())
                || errorCode.equals (VisitorBusinessException.ErrorCode.APPROVAL_CHAIN_ERROR.getCode ())
                || errorCode.equals (VisitorBusinessException.ErrorCode.VISITOR_APPROVAL_ALREADY_PROCESSED.getCode ());
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
     * 访客预约异常类（访客服务专用）
     */
    public static class VisitorAppointmentException extends RuntimeException {
        private final String appointmentId;
        private final String visitorName;
        private final LocalDateTime appointmentTime;

        public VisitorAppointmentException (String appointmentId, String visitorName, LocalDateTime appointmentTime,
                String message) {
            super (message);
            this.appointmentId = appointmentId;
            this.visitorName = visitorName;
            this.appointmentTime = appointmentTime;
        }

        public String getAppointmentId () {
            return appointmentId;
        }

        public String getVisitorName () {
            return visitorName;
        }

        public LocalDateTime getAppointmentTime () {
            return appointmentTime;
        }
    }

    /**
     * 访客审批异常类（访客服务专用）
     */
    public static class VisitorApprovalException extends RuntimeException {
        private final String approvalId;
        private final String approverName;
        private final String approvalStatus;

        public VisitorApprovalException (String approvalId, String approverName, String approvalStatus,
                String message) {
            super (message);
            this.approvalId = approvalId;
            this.approverName = approverName;
            this.approvalStatus = approvalStatus;
        }

        public String getApprovalId () {
            return approvalId;
        }

        public String getApproverName () {
            return approverName;
        }

        public String getApprovalStatus () {
            return approvalStatus;
        }
    }

    /**
     * 访客出入异常类（访客服务专用）
     */
    public static class VisitorAccessException extends RuntimeException {
        private final String visitorId;
        private final String accessArea;
        private final LocalDateTime accessTime;

        public VisitorAccessException (String visitorId, String accessArea, LocalDateTime accessTime, String message) {
            super (message);
            this.visitorId = visitorId;
            this.accessArea = accessArea;
            this.accessTime = accessTime;
        }

        public String getVisitorId () {
            return visitorId;
        }

        public String getAccessArea () {
            return accessArea;
        }

        public LocalDateTime getAccessTime () {
            return accessTime;
        }
    }

    /**
     * 访客通行证异常类（访客服务专用）
     */
    public static class VisitorPassException extends RuntimeException {
        private final String passId;
        private final String visitorName;
        private final LocalDateTime expiryTime;

        public VisitorPassException (String passId, String visitorName, LocalDateTime expiryTime, String message) {
            super (message);
            this.passId = passId;
            this.visitorName = visitorName;
            this.expiryTime = expiryTime;
        }

        public String getPassId () {
            return passId;
        }

        public String getVisitorName () {
            return visitorName;
        }

        public LocalDateTime getExpiryTime () {
            return expiryTime;
        }
    }

    /**
     * 访客车辆异常类（访客服务专用）
     */
    public static class VisitorVehicleException extends RuntimeException {
        private final String licensePlate;
        private final String visitorName;
        private final String vehicleType;

        public VisitorVehicleException (String licensePlate, String visitorName, String vehicleType, String message) {
            super (message);
            this.licensePlate = licensePlate;
            this.visitorName = visitorName;
            this.vehicleType = vehicleType;
        }

        public String getLicensePlate () {
            return licensePlate;
        }

        public String getVisitorName () {
            return visitorName;
        }

        public String getVehicleType () {
            return vehicleType;
        }
    }
}
