package net.lab1024.sa.access.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 鐢ㄦ埛闂ㄧ鏉冮檺鍝嶅簲
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "鐢ㄦ埛闂ㄧ鏉冮檺鍝嶅簲")
public class UserAccessPermissionResponse {

    @Schema(description = "鐢ㄦ埛ID", example = "1001")
    private Long userId;

    @Schema(description = "鐢ㄦ埛鍚?, example = "admin")
    private String username;

    @Schema(description = "鐪熷疄濮撳悕", example = "绯荤粺绠＄悊鍛?)
    private String realName;

    @Schema(description = "閮ㄩ棬ID", example = "1")
    private Long departmentId;

    @Schema(description = "閮ㄩ棬鍚嶇О", example = "鎶€鏈儴")
    private String departmentName;

    @Schema(description = "鏉冮檺鎬绘暟", example = "25")
    private Integer totalPermissions;

    @Schema(description = "鏈夋晥鏉冮檺鏁?, example = "23")
    private Integer validPermissions;

    @Schema(description = "杩囨湡鏉冮檺鏁?, example = "2")
    private Integer expiredPermissions;

    @Schema(description = "鏉冮檺鍒楄〃")
    private List<AccessPermission> permissions;

    @Schema(description = "鏉冮檺缁熻淇℃伅")
    private PermissionStatistics statistics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "闂ㄧ鏉冮檺")
    public static class AccessPermission {

        @Schema(description = "鏉冮檺ID", example = "10001")
        private Long permissionId;

        @Schema(description = "璁惧ID", example = "ACCESS_001")
        private String deviceId;

        @Schema(description = "璁惧鍚嶇О", example = "涓婚棬绂?)
        private String deviceName;

        @Schema(description = "璁惧绫诲瀷", example = "access", allowableValues = {"access", "turnstile", "gate"})
        private String deviceType;

        @Schema(description = "璁惧绫诲瀷鍚嶇О", example = "闂ㄧ鎺у埗鍣?)
        private String deviceTypeName;

        @Schema(description = "鍖哄煙ID", example = "1")
        private Long areaId;

        @Schema(description = "鍖哄煙鍚嶇О", example = "涓€妤煎ぇ鍘?)
        private String areaName;

        @Schema(description = "鏉冮檺绫诲瀷", example = "permanent", allowableValues = {"permanent", "temporary", "scheduled"})
        private String permissionType;

        @Schema(description = "鏉冮檺绫诲瀷鍚嶇О", example = "姘镐箙鏉冮檺")
        private String permissionTypeName;

        @Schema(description = "鏉冮檺绾у埆", example = "normal", allowableValues = {"normal", "vip", "emergency"})
        private String permissionLevel;

        @Schema(description = "鏉冮檺绾у埆鍚嶇О", example = "鏅€?)
        private String permissionLevelName;

        @Schema(description = "鏉冮檺鐘舵€?, example = "1", allowableValues = {"0", "1", "2"})
        private Integer permissionStatus;

        @Schema(description = "鏉冮檺鐘舵€佸悕绉?, example = "鏈夋晥")
        private String permissionStatusName;

        @Schema(description = "鐢熸晥鏃堕棿", example = "2025-12-16T00:00:00")
        private LocalDateTime effectiveTime;

        @Schema(description = "澶辨晥鏃堕棿", example = "2025-12-31T23:59:59")
        private LocalDateTime expireTime;

        @Schema(description = "鍒涘缓鏃堕棿", example = "2025-12-01T10:30:00")
        private LocalDateTime createTime;

        @Schema(description = "鍒涘缓浜篒D", example = "1002")
        private Long creatorId;

        @Schema(description = "鍒涘缓浜哄鍚?, example = "绠＄悊鍛?)
        private String creatorName;

        @Schema(description = "瀹℃壒浜篒D", example = "1003")
        private Long approverId;

        @Schema(description = "瀹℃壒浜哄鍚?, example = "閮ㄩ棬缁忕悊")
        private String approverName;

        @Schema(description = "瀹℃壒鏃堕棿", example = "2025-12-01T11:00:00")
        private LocalDateTime approveTime;

        @Schema(description = "閫氳娆℃暟闄愬埗", example = "100")
        private Integer maxAccessCount;

        @Schema(description = "宸蹭娇鐢ㄦ鏁?, example = "25")
        private Integer usedAccessCount;

        @Schema(description = "鍓╀綑娆℃暟", example = "75")
        private Integer remainingAccessCount;

        @Schema(description = "姣忔棩閫氳娆℃暟闄愬埗", example = "10")
        private Integer maxDailyAccessCount;

        @Schema(description = "浠婃棩宸蹭娇鐢ㄦ鏁?, example = "3")
        private Integer todayUsedCount;

        @Schema(description = "浠婃棩鍓╀綑娆℃暟", example = "7")
        private Integer todayRemainingCount;

        @Schema(description = "鍏佽閫氳鐨勬椂闂存")
        private List<TimeSlot> allowedTimeSlots;

        @Schema(description = "閫氳鏂瑰悜", example = "both", allowableValues = {"in", "out", "both"})
        private String allowedDirection;

        @Schema(description = "楠岃瘉鏂瑰紡", example = "card,face", allowableValues = {"card", "face", "fingerprint", "password", "qr_code"})
        private String allowedVerifyMethods;

        @Schema(description = "鏉冮檺鍘熷洜", example = "鍔炲叕闇€瑕?)
        private String reason;

        @Schema(description = "澶囨敞", example = "")
        private String remark;

        @Schema(description = "鎵╁睍鍙傛暟", example = "{\"key1\":\"value1\"}")
        private String extendedParams;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "鏃堕棿娈?)
    public static class TimeSlot {

        @Schema(description = "鏄熸湡鍑狅紙1-7锛?涓哄懆涓€锛?, example = "1-5")
        private String dayOfWeek;

        @Schema(description = "鏄熸湡鍚嶇О", example = "鍛ㄤ竴鑷冲懆浜?)
        private String dayName;

        @Schema(description = "寮€濮嬫椂闂?, example = "08:00")
        private String startTime;

        @Schema(description = "缁撴潫鏃堕棿", example = "18:00")
        private String endTime;

        @Schema(description = "鏄惁鍚敤", example = "true")
        private Boolean enabled;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "鏉冮檺缁熻淇℃伅")
    public static class PermissionStatistics {

        @Schema(description = "鎬绘潈闄愭暟", example = "25")
        private Integer totalPermissions;

        @Schema(description = "姘镐箙鏉冮檺鏁?, example = "20")
        private Integer permanentPermissions;

        @Schema(description = "涓存椂鏉冮檺鏁?, example = "3")
        private Integer temporaryPermissions;

        @Schema(description = "瀹氭椂鏉冮檺鏁?, example = "2")
        private Integer scheduledPermissions;

        @Schema(description = "鏈夋晥鏉冮檺鏁?, example = "23")
        private Integer validPermissions;

        @Schema(description = "杩囨湡鏉冮檺鏁?, example = "2")
        private Integer expiredPermissions;

        @Schema(description = "鍗冲皢杩囨湡鏉冮檺鏁帮紙7澶╁唴锛?, example = "1")
        private Integer expiringSoonPermissions;

        @Schema(description = "楂樻潈闄愮骇鍒暟閲?, example = "5")
        private Integer highLevelPermissions;

        @Schema(description = "鏅€氭潈闄愮骇鍒暟閲?, example = "15")
        private Integer normalLevelPermissions;

        @Schema(description = "绱ф€ユ潈闄愮骇鍒暟閲?, example = "3")
        private Integer emergencyLevelPermissions;

        @Schema(description = "瑕嗙洊鍖哄煙鏁伴噺", example = "5")
        private Integer coveredAreas;

        @Schema(description = "瑕嗙洊璁惧鏁伴噺", example = "15")
        private Integer coveredDevices;

        @Schema(description = "鏈€杩戜娇鐢ㄦ椂闂?, example = "2025-12-16T08:30:00")
        private LocalDateTime lastUsedTime;

        @Schema(description = "鏈湀浣跨敤娆℃暟", example = "156")
        private Integer monthlyUsageCount;

        @Schema(description = "鏉冮檺鍒╃敤鐜?, example = "78.5")
        private Double utilizationRate;
    }
}