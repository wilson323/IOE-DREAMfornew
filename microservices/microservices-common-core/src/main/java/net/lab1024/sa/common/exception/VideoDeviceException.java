package net.lab1024.sa.common.exception;

import lombok.Getter;

/**
 * 视频设备异常
 * <p>
 * 用于视频设备相关操作的异常处理
 * 严格遵循CLAUDE.md规范：统一异常处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Getter
public class VideoDeviceException extends BusinessException {

    /**
     * 设备ID
     */
    private final Long deviceId;

    /**
     * 构造函数
     *
     * @param deviceId 设备ID
     * @param errorCode 错误码
     * @param message 错误消息
     */
    public VideoDeviceException(Long deviceId, String errorCode, String message) {
        super(errorCode, message);
        this.deviceId = deviceId;
    }

    /**
     * 构造函数（带原因）
     *
     * @param deviceId 设备ID
     * @param errorCode 错误码
     * @param message 错误消息
     * @param cause 原因
     */
    public VideoDeviceException(Long deviceId, String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
        this.deviceId = deviceId;
    }
}
