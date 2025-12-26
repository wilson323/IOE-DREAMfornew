package net.lab1024.sa.attendance.exception;

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
 * 考勤服务全局异常处理器
 * <p>
 * 专门处理考勤服务的异常情况 提供详细的错误分类和标准化响应 集成异常监控和指标收集 支持排班、请假、加班等复杂业务场景异常处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
@Slf4j
@RestControllerAdvice(basePackages = "net.lab1024.sa.attendance")
public class GlobalExceptionHandler {

    /**
     * 处理考勤业务异常
     */
    @ExceptionHandler(AttendanceBusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleAttendanceBusinessException (AttendanceBusinessException e) {
        log.warn ("[考勤业务异常] code={}, message={}, businessId={}", e.getCode (), e.getMessage (), e.getBusinessId ());

        // 特殊处理考勤规则异常
        if (isAttendanceRuleException (e.getCode ())) {
            log.warn ("[考勤规则异常] 违反考勤规则: code={}, businessId={}", e.getCode (), e.getBusinessId ());
        }

        recordExceptionMetrics ("AttendanceBusinessException", e.getCode ());

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
     * 处理考勤时间冲突异常
     */
    @ExceptionHandler(ScheduleConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseDTO<Void> handleScheduleConflictException (ScheduleConflictException e) {
        log.warn ("[排班冲突异常] 用户={}, 时间={}, 冲突类型={}", e.getUserId (), e.getConflictTime (), e.getConflictType ());

        recordExceptionMetrics ("ScheduleConflictException", "SCHEDULE_CONFLICT");

        return ResponseDTO.error ("SCHEDULE_CONFLICT", String.format ("排班冲突: %s", e.getMessage ()));
    }

    /**
     * 处理考勤数据同步异常
     */
    @ExceptionHandler(DataSyncException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleDataSyncException (DataSyncException e) {
        log.error ("[数据同步异常] 同步类型={}, 数据量={}, 错误={}", e.getSyncType (), e.getDataCount (), e.getMessage (), e);

        recordExceptionMetrics ("DataSyncException", "DATA_SYNC_ERROR");

        return ResponseDTO.error ("DATA_SYNC_ERROR", String.format ("数据同步失败: %s", e.getMessage ()));
    }

    /**
     * 处理移动端考勤异常
     */
    @ExceptionHandler(MobileAttendanceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleMobileAttendanceException (MobileAttendanceException e) {
        log.warn ("[移动端考勤异常] 用户={}, 设备={}, 位置={}", e.getUserId (), e.getDeviceInfo (), e.getLocationInfo ());

        recordExceptionMetrics ("MobileAttendanceException", "MOBILE_ATTENDANCE_ERROR");

        return ResponseDTO.error ("MOBILE_ATTENDANCE_ERROR", String.format ("移动端考勤异常: %s", e.getMessage ()));
    }

    /**
     * 判断是否为考勤规则异常
     */
    private boolean isAttendanceRuleException (String errorCode) {
        return errorCode.equals (AttendanceBusinessException.ErrorCode.CLOCK_IN_FAILED.getCode ())
                || errorCode.equals (AttendanceBusinessException.ErrorCode.CLOCK_OUT_FAILED.getCode ())
                || errorCode.equals (AttendanceBusinessException.ErrorCode.CLOCK_IN_OUT_MISMATCH.getCode ())
                || errorCode.equals (AttendanceBusinessException.ErrorCode.ATTENDANCE_LOCATION_INVALID.getCode ())
                || errorCode.equals (AttendanceBusinessException.ErrorCode.ATTENDANCE_TIME_INVALID.getCode ());
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
     * 排班冲突异常类（考勤服务专用）
     */
    public static class ScheduleConflictException extends RuntimeException {
        private final Long userId;
        private final LocalDateTime conflictTime;
        private final String conflictType;

        public ScheduleConflictException (Long userId, LocalDateTime conflictTime, String conflictType,
                String message) {
            super (message);
            this.userId = userId;
            this.conflictTime = conflictTime;
            this.conflictType = conflictType;
        }

        public Long getUserId () {
            return userId;
        }

        public LocalDateTime getConflictTime () {
            return conflictTime;
        }

        public String getConflictType () {
            return conflictType;
        }
    }

    /**
     * 数据同步异常类（考勤服务专用）
     */
    public static class DataSyncException extends RuntimeException {
        private final String syncType;
        private final Integer dataCount;

        public DataSyncException (String syncType, Integer dataCount, String message, Throwable cause) {
            super (message, cause);
            this.syncType = syncType;
            this.dataCount = dataCount;
        }

        public String getSyncType () {
            return syncType;
        }

        public Integer getDataCount () {
            return dataCount;
        }
    }

    /**
     * 移动端考勤异常类（考勤服务专用）
     */
    public static class MobileAttendanceException extends RuntimeException {
        private final Long userId;
        private final String deviceInfo;
        private final String locationInfo;

        public MobileAttendanceException (Long userId, String deviceInfo, String locationInfo, String message) {
            super (message);
            this.userId = userId;
            this.deviceInfo = deviceInfo;
            this.locationInfo = locationInfo;
        }

        public Long getUserId () {
            return userId;
        }

        public String getDeviceInfo () {
            return deviceInfo;
        }

        public String getLocationInfo () {
            return locationInfo;
        }
    }
}
