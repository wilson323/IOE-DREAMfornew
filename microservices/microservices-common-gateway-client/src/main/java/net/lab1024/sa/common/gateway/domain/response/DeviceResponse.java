package net.lab1024.sa.common.gateway.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 设备信息响应对象
 * <p>
 * 用于跨服务传递设备信息，避免直接使用Entity
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "设备信息响应")
public class DeviceResponse {

    @Schema(description = "设备ID", example = "1001")
    private Long deviceId;

    @Schema(description = "设备编号", example = "DEV001")
    private String deviceCode;

    @Schema(description = "设备名称", example = "主门禁控制器")
    private String deviceName;

    @Schema(description = "设备类型", example = "ACCESS_CONTROLLER")
    private String deviceType;

    @Schema(description = "设备子类型", example = "ACCESS_CONTROLLER_MAIN")
    private String deviceSubType;

    @Schema(description = "设备状态", example = "1")
    private Integer status;

    @Schema(description = "区域ID", example = "1001")
    private Long areaId;

    @Schema(description = "区域名称", example = "A栋1楼大厅")
    private String areaName;

    @Schema(description = "设备位置", example = "A栋1楼大厅入口")
    private String location;

    @Schema(description = "IP地址", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "端口", example = "8080")
    private Integer port;

    @Schema(description = "厂商", example = "Hikvision")
    private String manufacturer;

    @Schema(description = "型号", example = "DS-K2801")
    private String model;

    @Schema(description = "固件版本", example = "V1.0.0")
    private String firmwareVersion;

    @Schema(description = "是否在线", example = "true")
    private Boolean online;

    @Schema(description = "最后心跳时间", example = "2025-12-21T10:30:00")
    private LocalDateTime lastHeartbeatTime;

    @Schema(description = "创建时间", example = "2025-01-01T10:30:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-12-21T10:30:00")
    private LocalDateTime updateTime;

    @Schema(description = "设备配置信息")
    private Object configuration;

    @Schema(description = "扩展属性")
    private Object extendedAttributes;
}