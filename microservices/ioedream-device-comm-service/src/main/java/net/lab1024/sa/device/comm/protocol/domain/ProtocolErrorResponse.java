package net.lab1024.sa.device.comm.protocol.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 协议错误响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProtocolErrorResponse {

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 错误级别 (WARNING, ERROR, FATAL)
     */
    private String errorLevel;

    /**
     * 错误时间
     */
    private LocalDateTime errorTime;

    /**
     * 是否可恢复
     */
    private boolean recoverable;

    /**
     * 建议操作
     */
    private String suggestedAction;

    public static ProtocolErrorResponse create(String errorCode, String errorMessage, Long deviceId) {
        return ProtocolErrorResponse.builder()
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .deviceId(deviceId)
                .errorTime(LocalDateTime.now())
                .build();
    }
}
