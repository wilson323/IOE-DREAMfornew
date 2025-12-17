package net.lab1024.sa.device.comm.protocol.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 设备状态信息
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProtocolDeviceStatus {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备编码
     */
    private String deviceCode;

    /**
     * 设备型号
     */
    private String deviceModel;

    /**
     * 厂商
     */
    private String manufacturer;

    /**
     * 固件版本
     */
    private String firmwareVersion;

    /**
     * 在线状态
     */
    private boolean online;

    /**
     * 连接状态 (CONNECTED, DISCONNECTED, CONNECTING)
     */
    private String connectionStatus;

    /**
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeatTime;

    /**
     * CPU使用率 (%)
     */
    private Double cpuUsage;

    /**
     * 内存使用率 (%)
     */
    private Double memoryUsage;

    /**
     * 存储使用率 (%)
     */
    private Double storageUsage;

    /**
     * 支持的功能列表
     */
    private String[] supportedFeatures;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedProperties;
}
