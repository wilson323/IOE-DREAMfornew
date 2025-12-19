package net.lab1024.sa.device.comm.protocol.rs485;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * RS485心跳检测结果
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
public class RS485HeartbeatResult {
    /** 是否成功 */
    private boolean success;
    /** 设备ID */
    private Long deviceId;
    /** 设备在线状态 */
    private boolean online;
    /** 响应延迟(ms) */
    private long latency;
    /** 心跳时间 */
    private LocalDateTime heartbeatTime;
    /** 错误消息 */
    private String errorMessage;

    public static RS485HeartbeatResult success(Long deviceId, long latency) {
        RS485HeartbeatResult result = new RS485HeartbeatResult();
        result.setSuccess(true);
        result.setDeviceId(deviceId);
        result.setOnline(true);
        result.setLatency(latency);
        result.setHeartbeatTime(LocalDateTime.now());
        return result;
    }

    public static RS485HeartbeatResult offline(Long deviceId, String errorMessage) {
        RS485HeartbeatResult result = new RS485HeartbeatResult();
        result.setSuccess(false);
        result.setDeviceId(deviceId);
        result.setOnline(false);
        result.setErrorMessage(errorMessage);
        return result;
    }
}
