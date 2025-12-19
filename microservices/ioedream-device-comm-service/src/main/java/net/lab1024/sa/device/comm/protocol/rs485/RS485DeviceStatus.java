package net.lab1024.sa.device.comm.protocol.rs485;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * RS485设备状态
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
public class RS485DeviceStatus {
    /** 设备ID */
    private Long deviceId;
    /** 设备序列号 */
    private String serialNumber;
    /** 在线状态 */
    private boolean online;
    /** 连接状态 */
    private String connectionStatus;
    /** 最后心跳时间 */
    private LocalDateTime lastHeartbeatTime;
    /** 最后通讯时间 */
    private LocalDateTime lastCommunicationTime;
    /** 固件版本 */
    private String firmwareVersion;
    /** 协议版本 */
    private String protocolVersion;
    /** 错误计数 */
    private int errorCount;
}
