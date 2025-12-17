package net.lab1024.sa.device.comm.protocol.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备注册结果
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProtocolRegistrationResult {

    /**
     * 注册是否成功
     */
    private boolean success;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 注册码
     */
    private String registrationCode;

    /**
     * 消息
     */
    private String message;

    /**
     * 错误信息
     */
    private String errorMessage;

    public static ProtocolRegistrationResult success(Long deviceId, String registrationCode) {
        return ProtocolRegistrationResult.builder()
                .success(true)
                .deviceId(deviceId)
                .registrationCode(registrationCode)
                .message("设备注册成功")
                .build();
    }

    public static ProtocolRegistrationResult failure(String errorMessage) {
        return ProtocolRegistrationResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
