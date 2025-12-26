package net.lab1024.sa.device.comm.protocol.exception;

import lombok.Getter;

/**
 * 协议处理异常
 * <p>
 * 设备协议处理过程中抛出的业务异常
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
@Getter
public class ProtocolProcessException extends RuntimeException {

    /**
     * 错误代码
     */
    private final String errorCode;

    /**
     * 设备ID
     */
    private final Long deviceId;

    /**
     * 协议类型
     */
    private final String protocolType;

    public ProtocolProcessException(String message) {
        super(message);
        this.errorCode = null;
        this.deviceId = null;
        this.protocolType = null;
    }

    public ProtocolProcessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
        this.deviceId = null;
        this.protocolType = null;
    }

    public ProtocolProcessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.deviceId = null;
        this.protocolType = null;
    }

    public ProtocolProcessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.deviceId = null;
        this.protocolType = null;
    }

    public ProtocolProcessException(String errorCode, String message, Long deviceId, String protocolType) {
        super(message);
        this.errorCode = errorCode;
        this.deviceId = deviceId;
        this.protocolType = protocolType;
    }

    public ProtocolProcessException(String errorCode, String message, Long deviceId, String protocolType, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.deviceId = deviceId;
        this.protocolType = protocolType;
    }

    /**
     * 创建协议处理失败异常
     */
    public static ProtocolProcessException processFailed(String message) {
        return new ProtocolProcessException("PROTOCOL_PROCESS_FAILED", message);
    }

    /**
     * 创建协议处理失败异常（带设备信息）
     */
    public static ProtocolProcessException processFailed(String message, Long deviceId, String protocolType) {
        return new ProtocolProcessException("PROTOCOL_PROCESS_FAILED", message, deviceId, protocolType);
    }

    /**
     * 创建协议处理失败异常（4参数版本，包含异常原因）
     */
    public static ProtocolProcessException processFailed(String message, Long deviceId, String protocolType, Exception cause) {
        ProtocolProcessException exception = new ProtocolProcessException("PROTOCOL_PROCESS_FAILED", message, deviceId, protocolType);
        exception.initCause(cause);
        return exception;
    }

    /**
     * 创建设备连接异常
     */
    public static ProtocolProcessException deviceConnectionFailed(String message, Long deviceId) {
        return new ProtocolProcessException("DEVICE_CONNECTION_FAILED", message, deviceId, null);
    }

    /**
     * 创建协议解析异常
     */
    public static ProtocolProcessException protocolParseFailed(String message, String protocolType) {
        return new ProtocolProcessException("PROTOCOL_PARSE_FAILED", message, null, protocolType);
    }

    /**
     * 创建命令执行异常
     */
    public static ProtocolProcessException commandExecuteFailed(String message, Long deviceId, String protocolType) {
        return new ProtocolProcessException("COMMAND_EXECUTE_FAILED", message, deviceId, protocolType);
    }
}