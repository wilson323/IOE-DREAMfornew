package net.lab1024.sa.platform.core.exception;

/**
 * 系统异常 - 重构版本
 * <p>
 * 解决原有SystemException的依赖混乱问题，提供简洁的系统异常处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-12-22
 */
public class SystemException extends RuntimeException {

    private final String code;
    private final Object details;

    public SystemException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.details = null;
    }

    public SystemException(String code, String message, Object details, Throwable cause) {
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
     * 预定义系统错误码
     */
    public static class Codes {
        public static final String SYSTEM_ERROR = "SYSTEM_ERROR";
        public static final String DATABASE_ERROR = "DATABASE_ERROR";
        public static final String NETWORK_ERROR = "NETWORK_ERROR";
        public static final String EXTERNAL_SERVICE_ERROR = "EXTERNAL_SERVICE_ERROR";
        public static final String CONFIGURATION_ERROR = "CONFIGURATION_ERROR";
    }

    /**
     * 创建系统异常的工厂方法
     */
    public static SystemException of(String code, String message, Throwable cause) {
        return new SystemException(code, message, cause);
    }

    public static SystemException of(String code, String message, Object details, Throwable cause) {
        return new SystemException(code, message, details, cause);
    }

    public static SystemException systemError(String message, Throwable cause) {
        return of(Codes.SYSTEM_ERROR, message, cause);
    }

    public static SystemException databaseError(String message, Throwable cause) {
        return of(Codes.DATABASE_ERROR, message, cause);
    }

    public static SystemException networkError(String message, Throwable cause) {
        return of(Codes.NETWORK_ERROR, message, cause);
    }
}