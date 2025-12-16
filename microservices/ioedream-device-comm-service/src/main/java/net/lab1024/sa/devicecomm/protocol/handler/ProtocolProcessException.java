package net.lab1024.sa.devicecomm.protocol.handler;

/**
 * 协议处理异常
 * <p>
 * 当协议消息处理失败时抛出此异常
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public class ProtocolProcessException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 构造函数
     *
     * @param message 错误信息
     */
    public ProtocolProcessException(String message) {
        super(message);
    }

    /**
     * 构造函数
     *
     * @param message 错误信息
     * @param cause 异常原因
     */
    public ProtocolProcessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param message 错误信息
     */
    public ProtocolProcessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

