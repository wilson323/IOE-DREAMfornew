package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 移动端设备信息VO
 * 用于移动端设备管理界面展示设备信息
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "移动端设备信息VO")
public class MobileDeviceVO {

    @Schema(description = "设备ID", example = "1001")
    private Long deviceId;

    @Schema(description = "设备编码", example = "DEV001")
    private String deviceCode;

    @Schema(description = "设备名称", example = "主入口门禁")
    private String deviceName;

    @Schema(description = "设备类型", example = "1")
    private Integer deviceType;

    @Schema(description = "设备类型名称", example = "门禁控制器")
    private String deviceTypeName;

    @Schema(description = "设备子类型", example = "11")
    private Integer deviceSubType;

    @Schema(description = "设备子类型名称", example = "人脸识别门禁")
    private String deviceSubTypeName;

    @Schema(description = "所属区域ID", example = "1001")
    private Long areaId;

    @Schema(description = "所属区域名称", example = "A栋1楼大厅")
    private String areaName;

    @Schema(description = "设备位置", example = "A栋1楼大厅主入口")
    private String location;

    @Schema(description = "设备状态", example = "1")
    private Integer status;

    @Schema(description = "设备状态名称", example = "在线")
    private String statusName;

    @Schema(description = "设备IP地址", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "设备MAC地址", example = "AA:BB:CC:DD:EE:FF")
    private String macAddress;

    @Schema(description = "设备型号", example = "IOE-ACCESS-2000")
    private String deviceModel;

    @Schema(description = "固件版本", example = "v2.1.0")
    private String firmwareVersion;

    @Schema(description = "设备厂商", example = "IOE科技")
    private String manufacturer;

    @Schema(description = "安装时间", example = "2024-01-15T10:30:00")
    private LocalDateTime installTime;

    @Schema(description = "最后维护时间", example = "2024-12-01T09:00:00")
    private LocalDateTime lastMaintenanceTime;

    @Schema(description = "最后在线时间", example = "2025-12-16T14:30:00")
    private LocalDateTime lastOnlineTime;

    @Schema(description = "设备描述", example = "主入口门禁控制器，支持人脸识别和刷卡")
    private String description;

    @Schema(description = "是否支持远程控制", example = "true")
    private Boolean supportRemoteControl;

    @Schema(description = "是否支持固件升级", example = "true")
    private Boolean supportFirmwareUpgrade;

    @Schema(description = "设备扩展属性(JSON格式)", example = "{\"accessMode\":\"card+face\",\"antiPassback\":true}")
    private String extendedAttributes;

    @Schema(description = "权限级别", example = "1")
    private Integer permissionLevel;

    @Schema(description = "业务模块", example = "access")
    private String businessModule;

    @Schema(description = "创建时间", example = "2024-01-15T10:30:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-12-16T14:30:00")
    private LocalDateTime updateTime;

    // 移动端特有字段

    @Schema(description = "信号强度", example = "-45")
    private Integer signalStrength;

    @Schema(description = "电池电量(百分比，适用于无线设备)", example = "85")
    private Integer batteryLevel;

    @Schema(description = "温度(摄氏度)", example = "25.5")
    private Double temperature;

    @Schema(description = "CPU使用率(百分比)", example = "15.2")
    private Double cpuUsage;

    @Schema(description = "内存使用率(百分比)", example = "32.8")
    private Double memoryUsage;

    @Schema(description = "存储使用率(百分比)", example = "45.6")
    private Double storageUsage;

    @Schema(description = "今日通行次数", example = "156")
    private Long todayAccessCount;

    @Schema(description = "今日异常次数", example = "2")
    private Long todayErrorCount;

    @Schema(description = "设备健康评分(0-100)", example = "95")
    private Integer healthScore;

    @Schema(description = "是否需要维护", example = "false")
    private Boolean needsMaintenance;

    @Schema(description = "距离上次维护天数", example = "15")
    private Integer daysSinceLastMaintenance;
}