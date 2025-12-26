package net.lab1024.sa.platform.core.exception;

/**
 * 业务异常 - 重构版本
 * <p>
 * 解决原有BusinessException的依赖混乱问题，提供简洁的业务异常处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-12-22
 */
public class BusinessException extends RuntimeException {

    private final String code;
    private final Object details;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
        this.details = null;
    }

    public BusinessException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.details = null;
    }

    public BusinessException(String code, String message, Object details) {
        super(message);
        this.code = code;
        this.details = details;
    }

    public BusinessException(String code, String message, Object details, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.details = details;
    }

    /**
     * 获取错误码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取错误详情
     */
    public Object getDetails() {
        return details;
    }

    /**
     * 兼容原有方法
     */
    public String getErrorCode() {
        return code;
    }

    /**
     * 预定义业务错误码
     */
    public static class Codes {
        public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
        public static final String PARAM_ERROR = "PARAM_ERROR";
        public static final String DATA_NOT_FOUND = "DATA_NOT_FOUND";
        public static final String PERMISSION_DENIED = "PERMISSION_DENIED";
        public static final String BUSINESS_ERROR = "BUSINESS_ERROR";
    }

    /**
     * 创建业务异常的工厂方法
     */
    public static BusinessException of(String code, String message) {
        return new BusinessException(code, message);
    }

    public static BusinessException of(String code, String message, Object details) {
        return new BusinessException(code, message, details);
    }

    public static BusinessException userNotFound(String message) {
        return of(Codes.USER_NOT_FOUND, message);
    }

    public static BusinessException paramError(String message) {
        return of(Codes.PARAM_ERROR, message);
    }

    public static BusinessException dataNotFound(String message) {
        return of(Codes.DATA_NOT_FOUND, message);
    }

    public static BusinessException permissionDenied(String message) {
        return of(Codes.PERMISSION_DENIED, message);
    }
}