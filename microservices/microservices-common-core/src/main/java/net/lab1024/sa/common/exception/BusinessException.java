package net.lab1024.sa.common.exception;

/**
 * 业务异常
 * <p>
 * 用于处理业务逻辑中的异常情况
 * 严格遵循CLAUDE.md规范：
 * - 自定义异常类继承RuntimeException
 * - 包含错误码和错误信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final String code;

    /**
     * 错误参数
     */
    private final Object[] args;

    /**
     * 构造函数
     *
     * @param message 错误信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = "BUSINESS_ERROR";
        this.args = null;
    }

    /**
     * 构造函数
     *
     * @param code 错误码
     * @param message 错误信息
     */
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
        this.args = null;
    }

    /**
     * 构造函数
     *
     * @param code 错误码
     * @param message 错误信息
     * @param args 错误参数
     */
    public BusinessException(String code, String message, Object... args) {
        super(message);
        this.code = code;
        this.args = args;
    }

    /**
     * 构造函数
     *
     * @param code 错误码
     * @param message 错误信息
     * @param cause 原因
     * @param args 错误参数
     */
    public BusinessException(String code, String message, Throwable cause, Object... args) {
        super(message, cause);
        this.code = code;
        this.args = args;
    }

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取错误参数
     *
     * @return 错误参数
     */
    public Object[] getArgs() {
        return args;
    }
}

