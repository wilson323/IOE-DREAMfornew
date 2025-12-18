package net.lab1024.sa.device.comm.protocol.rs485;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * RS485消息处理结果VO
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
@Schema(description = "RS485消息处理结果")
public class RS485ProcessResultVO {
    @Schema(description = "是否成功")
    private Boolean success;
    
    @Schema(description = "消息")
    private String message;
    
    @Schema(description = "设备ID")
    private Long deviceId;
    
    @Schema(description = "消息类型")
    private String messageType;
    
    @Schema(description = "处理时间（时间戳）")
    private Long processTime;
    
    @Schema(description = "业务类型")
    private String businessType;
    
    @Schema(description = "业务数据")
    private Map<String, Object> businessData;
    
    @Schema(description = "响应数据")
    private Map<String, Object> responseData;
    
    @Schema(description = "处理状态")
    private String status;
}
