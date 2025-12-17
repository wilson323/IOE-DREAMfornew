package net.lab1024.sa.device.comm.protocol.exception;

/**
 * 协议解析异常
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-17
 */
public class ProtocolParseException extends Exception {

    private static final long serialVersionUID = 1L;

    private String errorCode;

    public ProtocolParseException(String message) {
        super(message);
    }

    public ProtocolParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProtocolParseException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ProtocolParseException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
