package net.lab1024.sa.device.comm.protocol.rs485;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * RS485设备初始化结果VO
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
@Schema(description = "RS485设备初始化结果")
public class RS485InitResultVO {
    @Schema(description = "是否成功")
    private Boolean success;
    
    @Schema(description = "消息")
    private String message;
    
    @Schema(description = "设备ID")
    private Long deviceId;
    
    @Schema(description = "设备序列号")
    private String serialNumber;
    
    @Schema(description = "协议版本")
    private String protocolVersion;
    
    @Schema(description = "初始化时间（时间戳）")
    private Long initTime;
    
    @Schema(description = "初始化状态")
    private String status;
}
