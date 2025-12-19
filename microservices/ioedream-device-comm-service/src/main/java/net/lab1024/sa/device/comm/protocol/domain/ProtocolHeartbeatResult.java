package net.lab1024.sa.device.comm.protocol.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 心跳处理结果
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProtocolHeartbeatResult {

    /**
     * 处理是否成功
     */
    private boolean success;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备在线状态
     */
    private boolean online;

    /**
     * 心跳时间
     */
    private LocalDateTime heartbeatTime;

    /**
     * 响应消息
     */
    private String responseMessage;

    public static ProtocolHeartbeatResult success(Long deviceId) {
        return ProtocolHeartbeatResult.builder()
                .success(true)
                .deviceId(deviceId)
                .online(true)
                .heartbeatTime(LocalDateTime.now())
                .build();
    }

    public static ProtocolHeartbeatResult failure(String message) {
        return ProtocolHeartbeatResult.builder()
                .success(false)
                .online(false)
                .responseMessage(message)
                .heartbeatTime(LocalDateTime.now())
                .build();
    }
}
