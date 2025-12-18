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
 * 闂ㄧ璁惧璇︽儏鍝嶅簲
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "闂ㄧ璁惧璇︽儏鍝嶅簲")
public class AccessDeviceDetailResponse {

    @Schema(description = "璁惧ID", example = "ACCESS_001")
    private String deviceId;

    @Schema(description = "璁惧鍚嶇О", example = "涓婚棬绂?)
    private String deviceName;

    @Schema(description = "璁惧绫诲瀷", example = "access", allowableValues = {"access", "turnstile", "gate"})
    private String deviceType;

    @Schema(description = "璁惧绫诲瀷鍚嶇О", example = "闂ㄧ鎺у埗鍣?)
    private String deviceTypeName;

    @Schema(description = "璁惧鍨嬪彿", example = "MODEL-A100")
    private String deviceModel;

    @Schema(description = "璁惧鍘傚晢", example = "娴峰悍濞佽")
    private String deviceManufacturer;

    @Schema(description = "璁惧搴忓垪鍙?, example = "SN1234567890")
    private String serialNumber;

    @Schema(description = "璁惧鐘舵€?, example = "1", allowableValues = {"0", "1", "2"})
    private Integer deviceStatus;

    @Schema(description = "璁惧鐘舵€佸悕绉?, example = "鍦ㄧ嚎")
    private String deviceStatusName;

    @Schema(description = "鍖哄煙ID", example = "1")
    private Long areaId;

    @Schema(description = "鍖哄煙鍚嶇О", example = "涓€妤煎ぇ鍘?)
    private String areaName;

    @Schema(description = "璁惧浣嶇疆", example = "涓€妤煎ぇ鍘呬笢渚?)
    private String location;

    @Schema(description = "璁惧IP", example = "192.168.1.100")
    private String deviceIp;

    @Schema(description = "璁惧绔彛", example = "8000")
    private Integer devicePort;

    @Schema(description = "缃戝叧IP", example = "192.168.1.1")
    private String gatewayIp;

    @Schema(description = "瀛愮綉鎺╃爜", example = "255.255.255.0")
    private String subnetMask;

    @Schema(description = "MAC鍦板潃", example = "AA:BB:CC:DD:EE:FF")
    private String macAddress;

    @Schema(description = "鏈€鍚庡湪绾挎椂闂?, example = "2025-12-16T15:30:00")
    private LocalDateTime lastOnlineTime;

    @Schema(description = "鏈€鍚庣绾挎椂闂?, example = "2025-12-16T10:30:00")
    private LocalDateTime lastOfflineTime;

    @Schema(description = "杩炵画鍦ㄧ嚎鏃堕暱锛堝皬鏃讹級", example = "72.5")
    private Double onlineDuration;

    @Schema(description = "瀹夎鏃堕棿", example = "2025-01-01T10:30:00")
    private LocalDateTime installTime;

    @Schema(description = "鍥轰欢鐗堟湰", example = "v1.2.3")
    private String firmwareVersion;

    @Schema(description = "纭欢鐗堟湰", example = "v2.0")
    private String hardwareVersion;

    @Schema(description = "鍗忚绫诲瀷", example = "TCP", allowableValues = {"TCP", "UDP", "HTTP", "HTTPS"})
    private String protocolType;

    @Schema(description = "鏄惁鏀寔杩滅▼鎺у埗", example = "true")
    private Boolean supportRemoteControl;

    @Schema(description = "鏄惁鏀寔浜鸿劯璇嗗埆", example = "true")
    private Boolean supportFaceRecognition;

    @Schema(description = "鏄惁鏀寔鎸囩汗璇嗗埆", example = "true")
    private Boolean supportFingerprint;

    @Schema(description = "鏄惁鏀寔鍒峰崱", example = "true")
    private Boolean supportCard;

    @Schema(description = "鏄惁鏀寔瀵嗙爜", example = "true")
    private Boolean supportPassword;

    @Schema(description = "鏄惁鏀寔浜岀淮鐮?, example = "true")
    private Boolean supportQrCode;

    @Schema(description = "鏄惁鏀寔浣撴俯妫€娴?, example = "true")
    private Boolean supportTemperature;

    @Schema(description = "鏄惁鏀寔娲讳綋妫€娴?, example = "true")
    private Boolean supportLivenessCheck;

    @Schema(description = "鏄惁鏀寔鍙ｇ僵妫€娴?, example = "true")
    private Boolean supportMaskCheck;

    @Schema(description = "鏄惁鏀寔鍙嶆綔鍥?, example = "true")
    private Boolean supportAntiPassback;

    @Schema(description = "浜鸿劯璇嗗埆閰嶇疆")
    private FaceRecognitionConfig faceRecognitionConfig;

    @Schema(description = "鎸囩汗璇嗗埆閰嶇疆")
    private FingerprintConfig fingerprintConfig;

    @Schema(description = "闂ㄧ鎺у埗閰嶇疆")
    private AccessControlConfig accessControlConfig;

    @Schema(description = "璁惧鐘舵€佷俊鎭?)
    private Map<String, Object> deviceStatusInfo;

    @Schema(description = "璁惧缁熻淇℃伅")
    private DeviceStatistics statistics;

    @Schema(description = "璁惧鑳藉姏鍒楄〃")
    private List<DeviceCapability> capabilities;

    @Schema(description = "璁惧鎻忚堪", example = "涓€妤煎ぇ鍘呬富鍏ュ彛闂ㄧ璁惧")
    private String description;

    @Schema(description = "澶囨敞", example = "")
    private String remark;

    @Schema(description = "鍒涘缓鏃堕棿", example = "2025-01-01T10:30:00")
    private LocalDateTime createTime;

    @Schema(description = "鏇存柊鏃堕棿", example = "2025-12-16T15:30:00")
    private LocalDateTime updateTime;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "浜鸿劯璇嗗埆閰嶇疆")
    public static class FaceRecognitionConfig {

        @Schema(description = "鏄惁鍚敤", example = "true")
        private Boolean enabled;

        @Schema(description = "璇嗗埆闃堝€?, example = "80.0")
        private Double threshold;

        @Schema(description = "娲讳綋妫€娴嬮槇鍊?, example = "85.0")
        private Double livenessThreshold;

        @Schema(description = "浜鸿劯搴揑D", example = "FACE_LIB_001")
        private String faceLibId;

        @Schema(description = "姣斿瓒呮椂鏃堕棿锛堟绉掞級", example = "3000")
        private Integer timeout;

        @Schema(description = "鏈€澶т汉鑴告暟", example = "5")
        private Integer maxFaceCount;

        @Schema(description = "鏄惁淇濆瓨浜鸿劯鍥惧儚", example = "true")
        private Boolean saveFaceImage;

        @Schema(description = "鍥惧儚璐ㄩ噺瑕佹眰", example = "high", allowableValues = {"low", "medium", "high"})
        private String imageQuality;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "鎸囩汗璇嗗埆閰嶇疆")
    public static class FingerprintConfig {

        @Schema(description = "鏄惁鍚敤", example = "true")
        private Boolean enabled;

        @Schema(description = "璇嗗埆闃堝€?, example = "75.0")
        private Double threshold;

        @Schema(description = "鎸囩汗妯℃澘瀹归噺", example = "1000")
        private Integer templateCapacity;

        @Schema(description = "姣斿瓒呮椂鏃堕棿锛堟绉掞級", example = "2000")
        private Integer timeout;

        @Schema(description = "鎸囩汗璐ㄩ噺瑕佹眰", example = "medium", allowableValues = {"low", "medium", "high"})
        private String qualityLevel;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "闂ㄧ鎺у埗閰嶇疆")
    public static class AccessControlConfig {

        @Schema(description = "寮€闂ㄦ椂闀匡紙绉掞級", example = "30")
        private Integer openDuration;

        @Schema(description = "鏄惁闇€瑕佷簩娆＄‘璁?, example = "false")
        private Boolean requireConfirmation;

        @Schema(description = "纭瓒呮椂鏃堕棿锛堢锛?, example = "60")
        private Integer confirmationTimeout;

        @Schema(description = "鏄惁鏀寔杩滅▼寮€闂?, example = "true")
        private Boolean supportRemoteOpen;

        @Schema(description = "鏄惁鏀寔杩滅▼鍏抽棬", example = "true")
        private Boolean supportRemoteClose;

        @Schema(description = "鍙嶆綔鍥炴ā寮?, example = "area", allowableValues = {"none", "zone", "area"})
        private String antiPassbackMode;

        @Schema(description = "鏄惁鍏佽灏鹃殢", example = "false")
        private Boolean allowTailgating;

        @Schema(description = "鏈€澶у熬闅忎汉鏁?, example = "0")
        private Integer maxTailgatingCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "璁惧缁熻淇℃伅")
    public static class DeviceStatistics {

        @Schema(description = "浠婃棩閫氳娆℃暟", example = "156")
        private Integer todayAccessCount;

        @Schema(description = "鏈懆閫氳娆℃暟", example = "1024")
        private Integer weeklyAccessCount;

        @Schema(description = "鏈湀閫氳娆℃暟", example = "4096")
        private Integer monthlyAccessCount;

        @Schema(description = "鎬婚€氳娆℃暟", example = "98765")
        private Long totalAccessCount;

        @Schema(description = "浠婃棩鎴愬姛娆℃暟", example = "150")
        private Integer todaySuccessCount;

        @Schema(description = "浠婃棩澶辫触娆℃暟", example = "6")
        private Integer todayFailCount;

        @Schema(description = "鎴愬姛鐜?, example = "96.15")
        private Double successRate;

        @Schema(description = "骞冲潎鍝嶅簲鏃堕棿锛堟绉掞級", example = "450")
        private Long averageResponseTime;

        @Schema(description = "浠婃棩寮傚父娆℃暟", example = "2")
        private Integer todayAbnormalCount;

        @Schema(description = "涓婃缁存姢鏃堕棿", example = "2025-12-01T10:30:00")
        private LocalDateTime lastMaintenanceTime;

        @Schema(description = "涓嬫缁存姢鏃堕棿", example = "2025-12-31T10:30:00")
        private LocalDateTime nextMaintenanceTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "璁惧鑳藉姏")
    public static class DeviceCapability {

        @Schema(description = "鑳藉姏缂栫爜", example = "face_recognition")
        private String capabilityCode;

        @Schema(description = "鑳藉姏鍚嶇О", example = "浜鸿劯璇嗗埆")
        private String capabilityName;

        @Schema(description = "鑳藉姏绫诲瀷", example = "verify", allowableValues = {"verify", "control", "monitor", "alarm"})
        private String capabilityType;

        @Schema(description = "鏄惁鍚敤", example = "true")
        private Boolean enabled;

        @Schema(description = "鑳藉姏鐗堟湰", example = "v1.0")
        private String version;

        @Schema(description = "鑳藉姏鎻忚堪", example = "鏀寔浜鸿劯鐗瑰緛璇嗗埆楠岃瘉")
        private String description;

        @Schema(description = "閰嶇疆鍙傛暟", example = "{\"threshold\":80}")
        private String parameters;
    }
}