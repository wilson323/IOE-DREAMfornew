package net.lab1024.sa.base.common.exception;

import net.lab1024.sa.base.common.code.ErrorCode;

/**
 * Smart业务异常类
 *
 * @author SmartAdmin Team
 * @date 2025/01/13
 */
public class SmartException extends RuntimeException {

    private ErrorCode errorCode;

    public SmartException() {
        super();
    }

    public SmartException(String message) {
        super(message);
    }

    public SmartException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.errorCode = errorCode;
    }

    public SmartException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }

    public SmartException(String message, Throwable cause) {
        super(message, cause);
    }

    public SmartException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMsg(), cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
