package net.lab1024.sa.devicecomm.protocol.handler;

/**
 * 协议解析异常
 * <p>
 * 当协议数据解析失败时抛出此异常
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public class ProtocolParseException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 原始数据
     */
    private byte[] rawData;

    /**
     * 构造函数
     *
     * @param message 错误信息
     */
    public ProtocolParseException(String message) {
        super(message);
    }

    /**
     * 构造函数
     *
     * @param message 错误信息
     * @param cause 异常原因
     */
    public ProtocolParseException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param message 错误信息
     * @param rawData 原始数据
     */
    public ProtocolParseException(String errorCode, String message, byte[] rawData) {
        super(message);
        this.errorCode = errorCode;
        this.rawData = rawData;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public byte[] getRawData() {
        return rawData;
    }
}

