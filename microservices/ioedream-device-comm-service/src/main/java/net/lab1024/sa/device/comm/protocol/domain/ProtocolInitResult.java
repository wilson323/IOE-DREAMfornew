package net.lab1024.sa.device.comm.protocol.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 协议初始化结果
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProtocolInitResult {

    /**
     * 初始化是否成功
     */
    private boolean success;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 连接ID
     */
    private String connectionId;

    /**
     * 初始化消息
     */
    private String message;

    /**
     * 设备信息
     */
    private Map<String, Object> deviceInfo;

    /**
     * 错误信息
     */
    private String errorMessage;

    public static ProtocolInitResult success(Long deviceId, String connectionId) {
        return ProtocolInitResult.builder()
                .success(true)
                .deviceId(deviceId)
                .connectionId(connectionId)
                .message("设备初始化成功")
                .build();
    }

    public static ProtocolInitResult failure(String errorMessage) {
        return ProtocolInitResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
