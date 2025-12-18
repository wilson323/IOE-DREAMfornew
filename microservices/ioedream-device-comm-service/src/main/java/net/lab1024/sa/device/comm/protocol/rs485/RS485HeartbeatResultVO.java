package net.lab1024.sa.device.comm.protocol.rs485;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * RS485心跳检测结果VO
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
@Schema(description = "RS485心跳检测结果")
public class RS485HeartbeatResultVO {
    @Schema(description = "是否成功")
    private Boolean success;
    
    @Schema(description = "消息")
    private String message;
    
    @Schema(description = "设备ID")
    private Long deviceId;
    
    @Schema(description = "在线状态")
    private Boolean online;
    
    @Schema(description = "响应延迟(ms)")
    private Long latency;
    
    @Schema(description = "心跳时间（时间戳）")
    private Long heartbeatTime;
    
    @Schema(description = "设备状态")
    private Map<String, Object> deviceStatus;
    
    @Schema(description = "健康状态")
    private String healthStatus;
}
