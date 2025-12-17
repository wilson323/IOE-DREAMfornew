package net.lab1024.sa.device.comm.protocol.exception;

/**
 * 协议构建异常
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-17
 */
public class ProtocolBuildException extends Exception {

    private static final long serialVersionUID = 1L;

    private String errorCode;

    public ProtocolBuildException(String message) {
        super(message);
    }

    public ProtocolBuildException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProtocolBuildException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ProtocolBuildException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
