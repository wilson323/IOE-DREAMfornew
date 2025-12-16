package net.lab1024.sa.access.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 门禁实时状态响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "门禁实时状态响应")
public class AccessRealtimeStatusResponse {

    @Schema(description = "区域ID", example = "1")
    private Long areaId;

    @Schema(description = "区域名称", example = "一楼大厅")
    private String areaName;

    @Schema(description = "统计时间", example = "2025-12-16T15:30:00")
    private LocalDateTime statisticsTime;

    @Schema(description = "设备总数", example = "15")
    private Integer totalDevices;

    @Schema(description = "在线设备数", example = "14")
    private Integer onlineDevices;

    @Schema(description = "离线设备数", example = "1")
    private Integer offlineDevices;

    @Schema(description = "设备在线率", example = "93.33")
    private Double onlineRate;

    @Schema(description = "今日总通行次数", example = "456")
    private Integer todayTotalAccess;

    @Schema(description = "今日成功次数", example = "450")
    private Integer todaySuccessAccess;

    @Schema(description = "今日失败次数", example = "6")
    private Integer todayFailAccess;

    @Schema(description = "今日成功率", example = "98.68")
    private Double todaySuccessRate;

    @Schema(description = "当前在线人数", example = "128")
    private Integer currentOnlineCount;

    @Schema(description = "当前室内人数", example = "98")
    private Integer currentInsideCount;

    @Schema(description = "今日进入人数", example = "234")
    private Integer todayEnterCount;

    @Schema(description = "今日离开人数", example = "222")
    private Integer todayExitCount;

    @Schema(description = "最近通行时间", example = "2025-12-16T15:28:45")
    private LocalDateTime lastAccessTime;

    @Schema(description = "最近通行记录")
    private AccessRecordInfo lastAccessRecord;

    @Schema(description = "异常告警数量", example = "2")
    private Integer alarmCount;

    @Schema(description = "异常告警列表")
    private List<AlarmInfo> alarmList;

    @Schema(description = "设备状态列表")
    private List<DeviceStatusInfo> deviceStatusList;

    @Schema(description = "实时通行热力图数据")
    private List<HeatmapData> heatmapData;

    @Schema(description = "通行时段分布")
    private Map<String, Integer> timeDistribution;

    @Schema(description = "设备类型分布")
    private Map<String, Integer> deviceTypeDistribution;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "通行记录信息")
    public static class AccessRecordInfo {

        @Schema(description = "记录ID", example = "100001")
        private Long recordId;

        @Schema(description = "用户ID", example = "1001")
        private Long userId;

        @Schema(description = "用户名", example = "admin")
        private String username;

        @Schema(description = "真实姓名", example = "系统管理员")
        private String realName;

        @Schema(description = "设备ID", example = "ACCESS_001")
        private String deviceId;

        @Schema(description = "设备名称", example = "主门禁")
        private String deviceName;

        @Schema(description = "通行时间", example = "2025-12-16T15:28:45")
        private LocalDateTime accessTime;

        @Schema(description = "通行方向", example = "in")
        private String direction;

        @Schema(description = "通行状态", example = "1")
        private Integer accessStatus;

        @Schema(description = "验证方式", example = "face")
        private String verifyType;

        @Schema(description = "照片URL", example = "https://example.com/photo.jpg")
        private String photoUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "告警信息")
    public static class AlarmInfo {

        @Schema(description = "告警ID", example = "ALARM_001")
        private String alarmId;

        @Schema(description = "告警类型", example = "device_offline", allowableValues = {"device_offline", "abnormal_access", "high_temperature", "forced_open"})
        private String alarmType;

        @Schema(description = "告警类型名称", example = "设备离线")
        private String alarmTypeName;

        @Schema(description = "告警级别", example = "warning", allowableValues = {"info", "warning", "error", "critical"})
        private String alarmLevel;

        @Schema(description = "告警级别名称", example = "警告")
        private String alarmLevelName;

        @Schema(description = "设备ID", example = "ACCESS_003")
        private String deviceId;

        @Schema(description = "设备名称", example = "侧门禁")
        private String deviceName;

        @Schema(description = "告警时间", example = "2025-12-16T15:25:30")
        private LocalDateTime alarmTime;

        @Schema(description = "告警描述", example = "设备通信异常，可能已离线")
        private String description;

        @Schema(description = "是否已处理", example = "false")
        private Boolean handled;

        @Schema(description = "处理时间", example = "2025-12-16T15:26:00")
        private LocalDateTime handleTime;

        @Schema(description = "处理人ID", example = "1002")
        private Long handlerId;

        @Schema(description = "处理人姓名", example = "维护人员")
        private String handlerName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "设备状态信息")
    public static class DeviceStatusInfo {

        @Schema(description = "设备ID", example = "ACCESS_001")
        private String deviceId;

        @Schema(description = "设备名称", example = "主门禁")
        private String deviceName;

        @Schema(description = "设备类型", example = "access")
        private String deviceType;

        @Schema(description = "设备状态", example = "1")
        private Integer deviceStatus;

        @Schema(description = "设备状态名称", example = "在线")
        private String deviceStatusName;

        @Schema(description = "设备位置", example = "一楼大厅东侧")
        private String location;

        @Schema(description = "CPU使用率", example = "45.2")
        private Double cpuUsage;

        @Schema(description = "内存使用率", example = "67.8")
        private Double memoryUsage;

        @Schema(description = "磁盘使用率", example = "23.5")
        private Double diskUsage;

        @Schema(description = "网络延迟（毫秒）", example = "15")
        private Integer networkLatency;

        @Schema(description = "今日通行次数", example = "45")
        private Integer todayAccessCount;

        @Schema(description = "今日成功率", example = "97.78")
        private Double todaySuccessRate;

        @Schema(description = "温度（℃）", example = "42.5")
        private Double temperature;

        @Schema(description = "电压（V）", example = "12.1")
        private Double voltage;

        @Schema(description = "固件版本", example = "v1.2.3")
        private String firmwareVersion;

        @Schema(description = "最后心跳时间", example = "2025-12-16T15:29:30")
        private LocalDateTime lastHeartbeatTime;

        @Schema(description = "运行时长（小时）", example = "168.5")
        private Double uptime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "热力图数据")
    public static class HeatmapData {

        @Schema(description = "位置X坐标", example = "150")
        private Integer x;

        @Schema(description = "位置Y坐标", example = "200")
        private Integer y;

        @Schema(description = "位置标签", example = "主入口")
        private String label;

        @Schema(description = "通行频次", example = "45")
        private Integer frequency;

        @Schema(description = "热度值", example = "0.85")
        private Double heatValue;

        @Schema(description = "时间范围", example = "09:00-10:00")
        private String timeRange;
    }
}