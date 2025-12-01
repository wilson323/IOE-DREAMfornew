package net.lab1024.sa.device.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物理设备视图对象
 *
 * @author IOE-DREAM Team
 */
@Data
@Schema(description = "物理设备信息")
public class PhysicalDeviceVO {

    @Schema(description = "设备ID", example = "1001")
    private Long deviceId;

    @Schema(description = "设备编号", example = "DEV001")
    private String deviceCode;

    @Schema(description = "设备名称", example = "主门禁设备")
    private String deviceName;

    @Schema(description = "设备类型", example = "ACCESS")
    private String deviceType;

    @Schema(description = "设备类型描述", example = "门禁设备")
    private String deviceTypeDesc;

    @Schema(description = "设备型号", example = "HD-1000")
    private String deviceModel;

    @Schema(description = "制造商", example = "华为")
    private String manufacturer;

    @Schema(description = "设备状态", example = "ONLINE")
    private String deviceStatus;

    @Schema(description = "设备状态描述", example = "在线")
    private String deviceStatusDesc;

    @Schema(description = "IP地址", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "端口号", example = "8080")
    private Integer port;

    @Schema(description = "设备位置", example = "办公楼一楼大厅")
    private String location;

    @Schema(description = "安装区域ID", example = "1001")
    private Long areaId;

    @Schema(description = "区域名称", example = "办公区域")
    private String areaName;

    @Schema(description = "协议类型", example = "TCP")
    private String protocolType;

    @Schema(description = "设备版本", example = "v1.0.0")
    private String deviceVersion;

    @Schema(description = "固件版本", example = "v2.1.5")
    private String firmwareVersion;

    @Schema(description = "最后心跳时间", example = "2024-11-27T10:30:00")
    private LocalDateTime lastHeartbeatTime;

    @Schema(description = "连接状态", example = "CONNECTED")
    private String connectionStatus;

    @Schema(description = "是否在线", example = "true")
    private Boolean isOnline;

    @Schema(description = "心跳延迟（秒）", example = "30")
    private Long heartbeatAge;

    @Schema(description = "心跳状态", example = "normal")
    private String heartbeatStatus;

    @Schema(description = "设备描述", example = "主要出入口门禁控制设备")
    private String description;

    @Schema(description = "备注", example = "需要定期维护")
    private String remarks;

    @Schema(description = "创建时间", example = "2024-11-27T09:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2024-11-27T10:30:00")
    private LocalDateTime updateTime;

    @Schema(description = "创建用户名", example = "admin")
    private String createUserName;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;
}