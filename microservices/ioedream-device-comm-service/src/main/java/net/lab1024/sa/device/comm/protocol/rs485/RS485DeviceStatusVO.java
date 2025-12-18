package net.lab1024.sa.device.comm.protocol.rs485;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * RS485设备状态VO
 * <p>
 * 采用Builder模式设计，提高可读性和可维护性
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "RS485设备状态")
public class RS485DeviceStatusVO {
    @Schema(description = "设备ID")
    private Long deviceId;
    
    @Schema(description = "设备序列号")
    private String serialNumber;
    
    @Schema(description = "在线状态")
    private Boolean online;
    
    @Schema(description = "连接状态")
    private String status;
    
    @Schema(description = "消息")
    private String message;
    
    @Schema(description = "检查时间（时间戳）")
    private Long checkTime;
    
    @Schema(description = "设备数据")
    private Map<String, Object> deviceData;
    
    @Schema(description = "连接状态")
    private String connectionStatus;
    
    @Schema(description = "最后心跳时间（时间戳）")
    private Long lastHeartbeatTime;
    
    @Schema(description = "固件版本")
    private String firmwareVersion;
}
