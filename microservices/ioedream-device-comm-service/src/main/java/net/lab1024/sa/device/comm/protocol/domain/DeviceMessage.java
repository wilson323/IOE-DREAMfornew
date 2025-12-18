package net.lab1024.sa.device.comm.protocol.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 设备消息对象
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceMessage {

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备编码
     */
    private String deviceCode;

    /**
     * 消息类型
     */
    private String messageType;

    /**
     * 消息内容
     */
    private Map<String, Object> content;

    /**
     * 业务数据（别名，映射到content）
     */
    public Map<String, Object> getBusinessData() {
        return this.content;
    }

    public void setBusinessData(Map<String, Object> businessData) {
        this.content = businessData;
    }

    /**
     * 设备数据（别名，映射到content）
     */
    public Map<String, Object> getDeviceData() {
        return this.content;
    }

    public void setDeviceData(Map<String, Object> deviceData) {
        this.content = deviceData;
    }

    /**
     * 消息时间
     */
    private LocalDateTime messageTime;

    /**
     * 原始数据
     */
    private String rawData;
}
