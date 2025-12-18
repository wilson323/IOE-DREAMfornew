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
 * 门禁设备详情响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "门禁设备详情响应")
public class AccessDeviceDetailResponse {

    @Schema(description = "设备ID", example = "ACCESS_001")
    private String deviceId;

    @Schema(description = "设备名称", example = "主门禁")
    private String deviceName;

    @Schema(description = "设备类型", example = "access", allowableValues = {"access", "turnstile", "gate"})
    private String deviceType;

    @Schema(description = "设备类型名称", example = "门禁控制器")
    private String deviceTypeName;

    @Schema(description = "设备型号", example = "MODEL-A100")
    private String deviceModel;

    @Schema(description = "设备厂商", example = "海康威视")
    private String deviceManufacturer;

    @Schema(description = "设备序列号", example = "SN1234567890")
    private String serialNumber;

    @Schema(description = "设备状态", example = "1", allowableValues = {"0", "1", "2"})
    private Integer deviceStatus;

    @Schema(description = "设备状态名称", example = "在线")
    private String deviceStatusName;

    @Schema(description = "区域ID", example = "1")
    private Long areaId;

    @Schema(description = "区域名称", example = "一楼大厅")
    private String areaName;

    @Schema(description = "设备位置", example = "一楼大厅东侧")
    private String location;

    @Schema(description = "设备IP", example = "192.168.1.100")
    private String deviceIp;

    @Schema(description = "设备端口", example = "8000")
    private Integer devicePort;

    @Schema(description = "网关IP", example = "192.168.1.1")
    private String gatewayIp;

    @Schema(description = "子网掩码", example = "255.255.255.0")
    private String subnetMask;

    @Schema(description = "MAC地址", example = "AA:BB:CC:DD:EE:FF")
    private String macAddress;

    @Schema(description = "最后在线时间", example = "2025-12-16T15:30:00")
    private LocalDateTime lastOnlineTime;

    @Schema(description = "最后离线时间", example = "2025-12-16T10:30:00")
    private LocalDateTime lastOfflineTime;

    @Schema(description = "连续在线时长（小时）", example = "72.5")
    private Double onlineDuration;

    @Schema(description = "安装时间", example = "2025-01-01T10:30:00")
    private LocalDateTime installTime;

    @Schema(description = "固件版本", example = "v1.2.3")
    private String firmwareVersion;

    @Schema(description = "硬件版本", example = "v2.0")
    private String hardwareVersion;

    @Schema(description = "协议类型", example = "TCP", allowableValues = {"TCP", "UDP", "HTTP", "HTTPS"})
    private String protocolType;

    @Schema(description = "是否支持远程控制", example = "true")
    private Boolean supportRemoteControl;

    @Schema(description = "是否支持人脸识别", example = "true")
    private Boolean supportFaceRecognition;

    @Schema(description = "是否支持指纹识别", example = "true")
    private Boolean supportFingerprint;

    @Schema(description = "是否支持刷卡", example = "true")
    private Boolean supportCard;

    @Schema(description = "是否支持密码", example = "true")
    private Boolean supportPassword;

    @Schema(description = "是否支持二维码", example = "true")
    private Boolean supportQrCode;

    @Schema(description = "是否支持体温检测", example = "true")
    private Boolean supportTemperature;

    @Schema(description = "是否支持活体检测", example = "true")
    private Boolean supportLivenessCheck;

    @Schema(description = "是否支持口罩检测", example = "true")
    private Boolean supportMaskCheck;

    @Schema(description = "是否支持反潜回", example = "true")
    private Boolean supportAntiPassback;

    @Schema(description = "人脸识别配置")
    private FaceRecognitionConfig faceRecognitionConfig;

    @Schema(description = "指纹识别配置")
    private FingerprintConfig fingerprintConfig;

    @Schema(description = "门禁控制配置")
    private AccessControlConfig accessControlConfig;

    @Schema(description = "设备状态信息")
    private Map<String, Object> deviceStatusInfo;

    @Schema(description = "设备统计信息")
    private DeviceStatistics statistics;

    @Schema(description = "设备能力列表")
    private List<DeviceCapability> capabilities;

    @Schema(description = "设备描述", example = "一楼大厅主入口门禁设备")
    private String description;

    @Schema(description = "备注", example = "")
    private String remark;

    @Schema(description = "创建时间", example = "2025-01-01T10:30:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-12-16T15:30:00")
    private LocalDateTime updateTime;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "人脸识别配置")
    public static class FaceRecognitionConfig {

        @Schema(description = "是否启用", example = "true")
        private Boolean enabled;

        @Schema(description = "识别阈值", example = "80.0")
        private Double threshold;

        @Schema(description = "活体检测阈值", example = "85.0")
        private Double livenessThreshold;

        @Schema(description = "人脸库ID", example = "FACE_LIB_001")
        private String faceLibId;

        @Schema(description = "比对超时时间（毫秒）", example = "3000")
        private Integer timeout;

        @Schema(description = "最大人脸数", example = "5")
        private Integer maxFaceCount;

        @Schema(description = "是否保存人脸图像", example = "true")
        private Boolean saveFaceImage;

        @Schema(description = "图像质量要求", example = "high", allowableValues = {"low", "medium", "high"})
        private String imageQuality;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "指纹识别配置")
    public static class FingerprintConfig {

        @Schema(description = "是否启用", example = "true")
        private Boolean enabled;

        @Schema(description = "识别阈值", example = "75.0")
        private Double threshold;

        @Schema(description = "指纹模板容量", example = "1000")
        private Integer templateCapacity;

        @Schema(description = "比对超时时间（毫秒）", example = "2000")
        private Integer timeout;

        @Schema(description = "指纹质量要求", example = "medium", allowableValues = {"low", "medium", "high"})
        private String qualityLevel;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "门禁控制配置")
    public static class AccessControlConfig {

        @Schema(description = "开门时长（秒）", example = "30")
        private Integer openDuration;

        @Schema(description = "是否需要二次确认", example = "false")
        private Boolean requireConfirmation;

        @Schema(description = "确认超时时间（秒）", example = "60")
        private Integer confirmationTimeout;

        @Schema(description = "是否支持远程开门", example = "true")
        private Boolean supportRemoteOpen;

        @Schema(description = "是否支持远程关门", example = "true")
        private Boolean supportRemoteClose;

        @Schema(description = "反潜回模式", example = "area", allowableValues = {"none", "zone", "area"})
        private String antiPassbackMode;

        @Schema(description = "是否允许尾随", example = "false")
        private Boolean allowTailgating;

        @Schema(description = "最大尾随人数", example = "0")
        private Integer maxTailgatingCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "设备统计信息")
    public static class DeviceStatistics {

        @Schema(description = "今日通行次数", example = "156")
        private Integer todayAccessCount;

        @Schema(description = "本周通行次数", example = "1024")
        private Integer weeklyAccessCount;

        @Schema(description = "本月通行次数", example = "4096")
        private Integer monthlyAccessCount;

        @Schema(description = "总通行次数", example = "98765")
        private Long totalAccessCount;

        @Schema(description = "今日成功次数", example = "150")
        private Integer todaySuccessCount;

        @Schema(description = "今日失败次数", example = "6")
        private Integer todayFailCount;

        @Schema(description = "成功率", example = "96.15")
        private Double successRate;

        @Schema(description = "平均响应时间（毫秒）", example = "450")
        private Long averageResponseTime;

        @Schema(description = "今日异常次数", example = "2")
        private Integer todayAbnormalCount;

        @Schema(description = "上次维护时间", example = "2025-12-01T10:30:00")
        private LocalDateTime lastMaintenanceTime;

        @Schema(description = "下次维护时间", example = "2025-12-31T10:30:00")
        private LocalDateTime nextMaintenanceTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "设备能力")
    public static class DeviceCapability {

        @Schema(description = "能力编码", example = "face_recognition")
        private String capabilityCode;

        @Schema(description = "能力名称", example = "人脸识别")
        private String capabilityName;

        @Schema(description = "能力类型", example = "verify", allowableValues = {"verify", "control", "monitor", "alarm"})
        private String capabilityType;

        @Schema(description = "是否启用", example = "true")
        private Boolean enabled;

        @Schema(description = "能力版本", example = "v1.0")
        private String version;

        @Schema(description = "能力描述", example = "支持人脸特征识别验证")
        private String description;

        @Schema(description = "配置参数", example = "{\"threshold\":80}")
        private String parameters;
    }
}
