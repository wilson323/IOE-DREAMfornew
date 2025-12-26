package net.lab1024.sa.common.exception;

/**
 * 参数异常
 * 用于处理参数验证相关的异常情况
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
public class ParamException extends RuntimeException {

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
    public ParamException(String message) {
        super(message);
        this.code = "PARAM_ERROR";
        this.args = null;
    }

    /**
     * 构造函数
     *
     * @param code 错误码
     * @param message 错误信息
     */
    public ParamException(String code, String message) {
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
    public ParamException(String code, String message, Throwable cause) {
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
    public ParamException(String code, String message, Object... args) {
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


