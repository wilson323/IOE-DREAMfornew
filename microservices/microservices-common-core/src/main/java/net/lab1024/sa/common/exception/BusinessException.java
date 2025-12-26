package net.lab1024.sa.common.exception;

/**
 * 业务异常
 * <p>
 * 用于处理业务逻辑中的异常情况
 * 严格遵循CLAUDE.md规范和ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求：
 * - 自定义异常类继承RuntimeException
 * - 包含错误码和错误信息
 * - 字段名为errorCode（文档要求）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     * <p>
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求：字段名为errorCode
     * </p>
     */
    private final String errorCode;

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
        this.errorCode = "BUSINESS_ERROR";
        this.args = null;
    }

    /**
     * 构造函数
     * <p>
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
     * </p>
     *
     * @param errorCode 错误码
     * @param message 错误信息
     */
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.args = null;
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param message 错误信息
     * @param args 错误参数
     */
    public BusinessException(String errorCode, String message, Object... args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args;
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param message 错误信息
     * @param cause 原因
     * @param args 错误参数
     */
    public BusinessException(String errorCode, String message, Throwable cause, Object... args) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = args;
    }

    /**
     * 获取错误码
     * <p>
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求：方法名为getErrorCode
     * </p>
     *
     * @return 错误码
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 获取错误码（向后兼容方法）
     * <p>
     * 为了保持向后兼容，保留getCode方法
     * </p>
     *
     * @return 错误码
     */
    public String getCode() {
        return errorCode;
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


