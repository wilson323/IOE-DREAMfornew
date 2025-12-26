package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 移动端设备信息响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "移动端设备信息响应")
public class MobileDeviceInfoVO {

    @Schema(description = "设备ID", example = "ACCESS_CTRL_001")
    private String deviceId;

    @Schema(description = "设备名称", example = "主入口门禁")
    private String deviceName;

    @Schema(description = "设备类型", example = "access")
    private String deviceType;

    @Schema(description = "设备子类型", example = "11")
    private Integer deviceSubType;

    @Schema(description = "设备状态", example = "online")
    private String deviceStatus;

    @Schema(description = "区域ID", example = "1")
    private Long areaId;

    @Schema(description = "区域名称", example = "A栋1楼大厅")
    private String areaName;

    @Schema(description = "设备IP地址", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "设备端口", example = "8080")
    private Integer port;

    @Schema(description = "固件版本", example = "1.0.0")
    private String firmwareVersion;

    @Schema(description = "最后通信时间", example = "2025-01-30T12:00:00")
    private LocalDateTime lastCommunicateTime;

    @Schema(description = "扩展属性")
    private Map<String, Object> extendedAttributes;
}
