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
 * 闂ㄧ瀹炴椂鐘舵€佸搷搴?
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "闂ㄧ瀹炴椂鐘舵€佸搷搴?)
public class AccessRealtimeStatusResponse {

    @Schema(description = "鍖哄煙ID", example = "1")
    private Long areaId;

    @Schema(description = "鍖哄煙鍚嶇О", example = "涓€妤煎ぇ鍘?)
    private String areaName;

    @Schema(description = "缁熻鏃堕棿", example = "2025-12-16T15:30:00")
    private LocalDateTime statisticsTime;

    @Schema(description = "璁惧鎬绘暟", example = "15")
    private Integer totalDevices;

    @Schema(description = "鍦ㄧ嚎璁惧鏁?, example = "14")
    private Integer onlineDevices;

    @Schema(description = "绂荤嚎璁惧鏁?, example = "1")
    private Integer offlineDevices;

    @Schema(description = "璁惧鍦ㄧ嚎鐜?, example = "93.33")
    private Double onlineRate;

    @Schema(description = "浠婃棩鎬婚€氳娆℃暟", example = "456")
    private Integer todayTotalAccess;

    @Schema(description = "浠婃棩鎴愬姛娆℃暟", example = "450")
    private Integer todaySuccessAccess;

    @Schema(description = "浠婃棩澶辫触娆℃暟", example = "6")
    private Integer todayFailAccess;

    @Schema(description = "浠婃棩鎴愬姛鐜?, example = "98.68")
    private Double todaySuccessRate;

    @Schema(description = "褰撳墠鍦ㄧ嚎浜烘暟", example = "128")
    private Integer currentOnlineCount;

    @Schema(description = "褰撳墠瀹ゅ唴浜烘暟", example = "98")
    private Integer currentInsideCount;

    @Schema(description = "浠婃棩杩涘叆浜烘暟", example = "234")
    private Integer todayEnterCount;

    @Schema(description = "浠婃棩绂诲紑浜烘暟", example = "222")
    private Integer todayExitCount;

    @Schema(description = "鏈€杩戦€氳鏃堕棿", example = "2025-12-16T15:28:45")
    private LocalDateTime lastAccessTime;

    @Schema(description = "鏈€杩戦€氳璁板綍")
    private AccessRecordInfo lastAccessRecord;

    @Schema(description = "寮傚父鍛婅鏁伴噺", example = "2")
    private Integer alarmCount;

    @Schema(description = "寮傚父鍛婅鍒楄〃")
    private List<AlarmInfo> alarmList;

    @Schema(description = "璁惧鐘舵€佸垪琛?)
    private List<DeviceStatusInfo> deviceStatusList;

    @Schema(description = "瀹炴椂閫氳鐑姏鍥炬暟鎹?)
    private List<HeatmapData> heatmapData;

    @Schema(description = "閫氳鏃舵鍒嗗竷")
    private Map<String, Integer> timeDistribution;

    @Schema(description = "璁惧绫诲瀷鍒嗗竷")
    private Map<String, Integer> deviceTypeDistribution;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "閫氳璁板綍淇℃伅")
    public static class AccessRecordInfo {

        @Schema(description = "璁板綍ID", example = "100001")
        private Long recordId;

        @Schema(description = "鐢ㄦ埛ID", example = "1001")
        private Long userId;

        @Schema(description = "鐢ㄦ埛鍚?, example = "admin")
        private String username;

        @Schema(description = "鐪熷疄濮撳悕", example = "绯荤粺绠＄悊鍛?)
        private String realName;

        @Schema(description = "璁惧ID", example = "ACCESS_001")
        private String deviceId;

        @Schema(description = "璁惧鍚嶇О", example = "涓婚棬绂?)
        private String deviceName;

        @Schema(description = "閫氳鏃堕棿", example = "2025-12-16T15:28:45")
        private LocalDateTime accessTime;

        @Schema(description = "閫氳鏂瑰悜", example = "in")
        private String direction;

        @Schema(description = "閫氳鐘舵€?, example = "1")
        private Integer accessStatus;

        @Schema(description = "楠岃瘉鏂瑰紡", example = "face")
        private String verifyType;

        @Schema(description = "鐓х墖URL", example = "https://example.com/photo.jpg")
        private String photoUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "鍛婅淇℃伅")
    public static class AlarmInfo {

        @Schema(description = "鍛婅ID", example = "ALARM_001")
        private String alarmId;

        @Schema(description = "鍛婅绫诲瀷", example = "device_offline", allowableValues = {"device_offline", "abnormal_access", "high_temperature", "forced_open"})
        private String alarmType;

        @Schema(description = "鍛婅绫诲瀷鍚嶇О", example = "璁惧绂荤嚎")
        private String alarmTypeName;

        @Schema(description = "鍛婅绾у埆", example = "warning", allowableValues = {"info", "warning", "error", "critical"})
        private String alarmLevel;

        @Schema(description = "鍛婅绾у埆鍚嶇О", example = "璀﹀憡")
        private String alarmLevelName;

        @Schema(description = "璁惧ID", example = "ACCESS_003")
        private String deviceId;

        @Schema(description = "璁惧鍚嶇О", example = "渚ч棬绂?)
        private String deviceName;

        @Schema(description = "鍛婅鏃堕棿", example = "2025-12-16T15:25:30")
        private LocalDateTime alarmTime;

        @Schema(description = "鍛婅鎻忚堪", example = "璁惧閫氫俊寮傚父锛屽彲鑳藉凡绂荤嚎")
        private String description;

        @Schema(description = "鏄惁宸插鐞?, example = "false")
        private Boolean handled;

        @Schema(description = "澶勭悊鏃堕棿", example = "2025-12-16T15:26:00")
        private LocalDateTime handleTime;

        @Schema(description = "澶勭悊浜篒D", example = "1002")
        private Long handlerId;

        @Schema(description = "澶勭悊浜哄鍚?, example = "缁存姢浜哄憳")
        private String handlerName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "璁惧鐘舵€佷俊鎭?)
    public static class DeviceStatusInfo {

        @Schema(description = "璁惧ID", example = "ACCESS_001")
        private String deviceId;

        @Schema(description = "璁惧鍚嶇О", example = "涓婚棬绂?)
        private String deviceName;

        @Schema(description = "璁惧绫诲瀷", example = "access")
        private String deviceType;

        @Schema(description = "璁惧鐘舵€?, example = "1")
        private Integer deviceStatus;

        @Schema(description = "璁惧鐘舵€佸悕绉?, example = "鍦ㄧ嚎")
        private String deviceStatusName;

        @Schema(description = "璁惧浣嶇疆", example = "涓€妤煎ぇ鍘呬笢渚?)
        private String location;

        @Schema(description = "CPU浣跨敤鐜?, example = "45.2")
        private Double cpuUsage;

        @Schema(description = "鍐呭瓨浣跨敤鐜?, example = "67.8")
        private Double memoryUsage;

        @Schema(description = "纾佺洏浣跨敤鐜?, example = "23.5")
        private Double diskUsage;

        @Schema(description = "缃戠粶寤惰繜锛堟绉掞級", example = "15")
        private Integer networkLatency;

        @Schema(description = "浠婃棩閫氳娆℃暟", example = "45")
        private Integer todayAccessCount;

        @Schema(description = "浠婃棩鎴愬姛鐜?, example = "97.78")
        private Double todaySuccessRate;

        @Schema(description = "娓╁害锛堚剝锛?, example = "42.5")
        private Double temperature;

        @Schema(description = "鐢靛帇锛圴锛?, example = "12.1")
        private Double voltage;

        @Schema(description = "鍥轰欢鐗堟湰", example = "v1.2.3")
        private String firmwareVersion;

        @Schema(description = "鏈€鍚庡績璺虫椂闂?, example = "2025-12-16T15:29:30")
        private LocalDateTime lastHeartbeatTime;

        @Schema(description = "杩愯鏃堕暱锛堝皬鏃讹級", example = "168.5")
        private Double uptime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "鐑姏鍥炬暟鎹?)
    public static class HeatmapData {

        @Schema(description = "浣嶇疆X鍧愭爣", example = "150")
        private Integer x;

        @Schema(description = "浣嶇疆Y鍧愭爣", example = "200")
        private Integer y;

        @Schema(description = "浣嶇疆鏍囩", example = "涓诲叆鍙?)
        private String label;

        @Schema(description = "閫氳棰戞", example = "45")
        private Integer frequency;

        @Schema(description = "鐑害鍊?, example = "0.85")
        private Double heatValue;

        @Schema(description = "鏃堕棿鑼冨洿", example = "09:00-10:00")
        private String timeRange;
    }
}