package net.lab1024.sa.common.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务异常类 - 企业级标准
 * 用于处理业务逻辑中的异常情况
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 错误数据
     */
    private Object data;

    /**
     * 构造函数
     *
     * @param message 错误消息
     */
    public BusinessException(String message) {
        this(500, message);
    }

    /**
     * 构造函数
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 构造函数
     *
     * @param code    错误码
     * @param message 错误消息
     * @param data    错误数据
     */
    public BusinessException(Integer code, String message, Object data) {
        super(message);
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 构造函数
     *
     * @param message 错误消息
     * @param cause   原因异常
     */
    public BusinessException(String message, Throwable cause) {
        this(500, message, cause);
    }

    /**
     * 构造函数
     *
     * @param code    错误码
     * @param message 错误消息
     * @param cause   原因异常
     */
    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 参数错误异常
     */
    public static BusinessException paramError(String message) {
        return new BusinessException(400, message);
    }

    /**
     * 认证错误异常
     */
    public static BusinessException authError(String message) {
        return new BusinessException(401, message);
    }

    /**
     * 权限错误异常
     */
    public static BusinessException permissionError(String message) {
        return new BusinessException(403, message);
    }

    /**
     * 资源不存在异常
     */
    public static BusinessException notFound(String message) {
        return new BusinessException(404, message);
    }

    /**
     * 服务器内部错误异常
     */
    public static BusinessException internalError(String message) {
        return new BusinessException(500, message);
    }

    /**
     * 服务不可用异常
     */
    public static BusinessException serviceUnavailable(String message) {
        return new BusinessException(503, message);
    }
}
