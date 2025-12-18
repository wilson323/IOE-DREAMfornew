package net.lab1024.sa.common.exception;

import lombok.Getter;

/**
 * 录像异常
 * <p>
 * 用于录像相关操作的异常处理
 * 严格遵循CLAUDE.md规范：统一异常处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Getter
public class VideoRecordingException extends BusinessException {

    /**
     * 录像ID
     */
    private final String recordingId;

    /**
     * 构造函数
     *
     * @param recordingId 录像ID
     * @param errorCode 错误码
     * @param message 错误消息
     */
    public VideoRecordingException(String recordingId, String errorCode, String message) {
        super(errorCode, message);
        this.recordingId = recordingId;
    }

    /**
     * 构造函数（带原因）
     *
     * @param recordingId 录像ID
     * @param errorCode 错误Code
     * @param message 错误消息
     * @param cause 原因
     */
    public VideoRecordingException(String recordingId, String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
        this.recordingId = recordingId;
    }
}
