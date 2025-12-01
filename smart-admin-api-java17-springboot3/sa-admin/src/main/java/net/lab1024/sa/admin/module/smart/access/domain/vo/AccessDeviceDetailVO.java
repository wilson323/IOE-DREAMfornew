package net.lab1024.sa.admin.module.smart.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 门禁设备详情VO
 * <p>
 * 用于展示门禁设备的完整信息
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@Schema(description = "门禁设备详情VO")
public class AccessDeviceDetailVO {

    /**
     * 门禁设备ID
     */
    @Schema(description = "门禁设备ID", example = "1")
    private Long accessDeviceId;

    /**
     * 设备ID（继承自SmartDeviceEntity）
     */
    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    /**
     * 设备编码
     */
    @Schema(description = "设备编码", example = "DEV_001")
    private String deviceCode;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称", example = "主门禁机")
    private String deviceName;

    /**
     * 设备类型
     */
    @Schema(description = "设备类型", example = "1")
    private Integer deviceType;

    /**
     * 设备状态
     */
    @Schema(description = "设备状态", example = "1")
    private Integer deviceStatus;

    /**
     * 设备状态名称
     */
    @Schema(description = "设备状态名称", example = "在线")
    private String statusName;

    /**
     * 所属区域ID
     */
    @Schema(description = "所属区域ID", example = "1")
    private Long areaId;

    /**
     * 所属区域名称
     */
    @Schema(description = "所属区域名称", example = "主园区")
    private String areaName;

    /**
     * 门禁设备类型
     */
    @Schema(description = "门禁设备类型", example = "1")
    private Integer accessDeviceType;

    /**
     * 门禁设备类型名称
     */
    @Schema(description = "门禁设备类型名称", example = "门禁机")
    private String accessDeviceTypeName;

    /**
     * 设备厂商
     */
    @Schema(description = "设备厂商", example = "海康威视")
    private String manufacturer;

    /**
     * 设备型号
     */
    @Schema(description = "设备型号", example = "DS-K2801")
    private String deviceModel;

    /**
     * 设备序列号
     */
    @Schema(description = "设备序列号", example = "DSK280120241116001")
    private String serialNumber;

    /**
     * 通信协议
     */
    @Schema(description = "通信协议", example = "TCP")
    private String protocol;

    /**
     * IP地址
     */
    @Schema(description = "IP地址", example = "192.168.1.100")
    private String ipAddress;

    /**
     * 端口号
     */
    @Schema(description = "端口号", example = "8000")
    private Integer port;

    /**
     * 设备方向
     */
    @Schema(description = "设备方向", example = "2")
    private Integer direction;

    /**
     * 设备方向名称
     */
    @Schema(description = "设备方向名称", example = "双向")
    private String directionName;

    /**
     * 开门方式
     */
    @Schema(description = "开门方式", example = "6")
    private Integer openMethod;

    /**
     * 开门方式名称
     */
    @Schema(description = "开门方式名称", example = "组合方式")
    private String openMethodName;

    /**
     * 开门延时时间
     */
    @Schema(description = "开门延时时间（秒）", example = "3")
    private Integer openDelay;

    /**
     * 有效开门时间
     */
    @Schema(description = "有效开门时间（秒）", example = "5")
    private Integer validTime;

    /**
     * 是否支持远程开门
     */
    @Schema(description = "是否支持远程开门", example = "1")
    private Integer remoteOpenEnabled;

    /**
     * 是否支持反潜回
     */
    @Schema(description = "是否支持反潜回", example = "1")
    private Integer antiPassbackEnabled;

    /**
     * 是否支持多人同时进入
     */
    @Schema(description = "是否支持多人同时进入", example = "0")
    private Integer multiPersonEnabled;

    /**
     * 是否支持门磁检测
     */
    @Schema(description = "是否支持门磁检测", example = "1")
    private Integer doorSensorEnabled;

    /**
     * 门磁状态
     */
    @Schema(description = "门磁状态", example = "0")
    private Integer doorSensorStatus;

    /**
     * 门磁状态名称
     */
    @Schema(description = "门磁状态名称", example = "关闭")
    private String doorSensorStatusName;

    /**
     * 在线状态
     */
    @Schema(description = "在线状态", example = "1")
    private Integer onlineStatus;

    /**
     * 在线状态名称
     */
    @Schema(description = "在线状态名称", example = "在线")
    private String onlineStatusName;

    /**
     * 最后通信时间
     */
    @Schema(description = "最后通信时间", example = "2025-11-16T10:30:00")
    private LocalDateTime lastCommTime;

    /**
     * 设备工作模式
     */
    @Schema(description = "设备工作模式", example = "1")
    private Integer workMode;

    /**
     * 设备工作模式名称
     */
    @Schema(description = "设备工作模式名称", example = "正常模式")
    private String workModeName;

    /**
     * 设备版本信息
     */
    @Schema(description = "设备版本信息", example = "V2.1.0")
    private String firmwareVersion;

    /**
     * 硬件版本信息
     */
    @Schema(description = "硬件版本信息", example = "V1.0.0")
    private String hardwareVersion;

    /**
     * 心跳间隔
     */
    @Schema(description = "心跳间隔（秒）", example = "60")
    private Integer heartbeatInterval;

    /**
     * 最后心跳时间
     */
    @Schema(description = "最后心跳时间", example = "2025-11-16T10:29:00")
    private LocalDateTime lastHeartbeatTime;

    /**
     * 是否启用
     */
    @Schema(description = "是否启用", example = "1")
    private Integer enabled;

    /**
     * 安装位置描述
     */
    @Schema(description = "安装位置描述", example = "主楼入口")
    private String installLocation;

    /**
     * 经度坐标
     */
    @Schema(description = "经度坐标", example = "116.397128")
    private Double longitude;

    /**
     * 纬度坐标
     */
    @Schema(description = "纬度坐标", example = "39.916527")
    private Double latitude;

    /**
     * 设备照片路径
     */
    @Schema(description = "设备照片路径", example = "/upload/device/photo_001.jpg")
    private String devicePhoto;

    /**
     * 维护人员
     */
    @Schema(description = "维护人员", example = "张三")
    private String maintenancePerson;

    /**
     * 维护联系电话
     */
    @Schema(description = "维护联系电话", example = "13800138000")
    private String maintenancePhone;

    /**
     * 上次维护时间
     */
    @Schema(description = "上次维护时间", example = "2025-10-16T10:00:00")
    private LocalDateTime lastMaintenanceTime;

    /**
     * 下次维护时间
     */
    @Schema(description = "下次维护时间", example = "2026-01-16T10:00:00")
    private LocalDateTime nextMaintenanceTime;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "主要入口设备，需要重点关注")
    private String remark;

    /**
     * 是否需要维护
     */
    @Schema(description = "是否需要维护", example = "false")
    private Boolean needMaintenance;

    /**
     * 连续离线天数
     */
    @Schema(description = "连续离线天数", example = "0")
    private Integer offlineDays;

    /**
     * 统计信息（非数据库字段，用于展示）
     */
    @Schema(description = "统计信息")
    private DeviceStatistics statistics;

    /**
     * 今日通行统计
     */
    @Schema(description = "今日通行统计")
    private Map<String, Object> todayStatistics;

    /**
     * 本周通行统计
     */
    @Schema(description = "本周通行统计")
    private Map<String, Object> weekStatistics;

    /**
     * 实时状态指标
     */
    @Schema(description = "实时状态指标")
    private List<Map<String, Object>> realTimeMetrics;

    /**
     * 设备统计数据内部类
     */
    @Data
    @Schema(description = "设备统计数据")
    public static class DeviceStatistics {
        @Schema(description = "今日通行次数", example = "156")
        private Long todayPassCount;

        @Schema(description = "今日通行成功次数", example = "152")
        private Long todaySuccessCount;

        @Schema(description = "今日通行失败次数", example = "4")
        private Long todayFailCount;

        @Schema(description = "今日报警次数", example = "1")
        private Long todayAlarmCount;

        @Schema(description = "平均响应时间（毫秒）", example = "150")
        private Long avgResponseTime;

        @Schema(description = "设备运行时长（小时）", example = "24")
        private Long runningHours;
    }
}