package net.lab1024.sa.common.exception;

import lombok.Getter;

/**
 * 视频流异常
 * <p>
 * 用于视频流相关操作的异常处理
 * 严格遵循CLAUDE.md规范：统一异常处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Getter
public class VideoStreamException extends BusinessException {

    /**
     * 流ID
     */
    private final String streamId;

    /**
     * 构造函数
     *
     * @param streamId 流ID
     * @param errorCode 错误码
     * @param message 错误消息
     */
    public VideoStreamException(String streamId, String errorCode, String message) {
        super(errorCode, message);
        this.streamId = streamId;
    }

    /**
     * 构造函数（带原因）
     *
     * @param streamId 流ID
     * @param errorCode 错误码
     * @param message 错误消息
     * @param cause 原因
     */
    public VideoStreamException(String streamId, String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
        this.streamId = streamId;
    }
}
