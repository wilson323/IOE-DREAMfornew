package net.lab1024.sa.common.exception;

/**
 * 系统异常
 * 用于处理系统级别的异常情况，通常是不可预期的错误
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
public class SystemException extends RuntimeException {

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
    public SystemException(String message) {
        super(message);
        this.code = "SYSTEM_ERROR";
        this.args = null;
    }

    /**
     * 构造函数
     *
     * @param code 错误码
     * @param message 错误信息
     */
    public SystemException(String code, String message) {
        super(message);
        this.code = code;
        this.args = null;
    }

    /**
     * 构造函数
     *
     * @param code    错误码
     * @param message 错误信息
     * @param cause   原因
     */
    public SystemException(String code, String message, Throwable cause) {
        super(message, cause);
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
    public SystemException(String code, String message, Object... args) {
        super(message);
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

