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
 * 闂ㄧ璁板綍璇︽儏鍝嶅簲
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "闂ㄧ璁板綍璇︽儏鍝嶅簲")
public class AccessRecordDetailResponse {

    @Schema(description = "璁板綍ID", example = "100001")
    private Long recordId;

    @Schema(description = "鐢ㄦ埛ID", example = "1001")
    private Long userId;

    @Schema(description = "鐢ㄦ埛鍚?, example = "admin")
    private String username;

    @Schema(description = "鐪熷疄濮撳悕", example = "绯荤粺绠＄悊鍛?)
    private String realName;

    @Schema(description = "鎬у埆", example = "1")
    private Integer gender;

    @Schema(description = "鎵嬫満鍙?, example = "13812345678")
    private String phone;

    @Schema(description = "閭", example = "admin@example.com")
    private String email;

    @Schema(description = "閮ㄩ棬ID", example = "1")
    private Long departmentId;

    @Schema(description = "閮ㄩ棬鍚嶇О", example = "鎶€鏈儴")
    private String departmentName;

    @Schema(description = "鑱屼綅", example = "杞欢宸ョ▼甯?)
    private String position;

    @Schema(description = "宸ュ彿", example = "EMP001")
    private String employeeNo;

    @Schema(description = "澶村儚", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "璁惧ID", example = "ACCESS_001")
    private String deviceId;

    @Schema(description = "璁惧鍚嶇О", example = "涓婚棬绂?)
    private String deviceName;

    @Schema(description = "璁惧绫诲瀷", example = "access", allowableValues = {"access", "turnstile", "gate"})
    private String deviceType;

    @Schema(description = "璁惧鍨嬪彿", example = "MODEL-A100")
    private String deviceModel;

    @Schema(description = "璁惧鍘傚晢", example = "娴峰悍濞佽")
    private String deviceManufacturer;

    @Schema(description = "璁惧IP", example = "192.168.1.100")
    private String deviceIp;

    @Schema(description = "鍖哄煙ID", example = "1")
    private Long areaId;

    @Schema(description = "鍖哄煙鍚嶇О", example = "涓€妤煎ぇ鍘?)
    private String areaName;

    @Schema(description = "鍖哄煙绫诲瀷", example = "entrance", allowableValues = {"entrance", "exit", "office", "factory"})
    private String areaType;

    @Schema(description = "閫氳鏃堕棿", example = "2025-12-16T15:30:00")
    private LocalDateTime accessTime;

    @Schema(description = "閫氳鐘舵€?, example = "1", allowableValues = {"0", "1"})
    private Integer accessStatus;

    @Schema(description = "閫氳鐘舵€佸悕绉?, example = "鎴愬姛")
    private String accessStatusName;

    @Schema(description = "閫氳鏂瑰悜", example = "in", allowableValues = {"in", "out"})
    private String direction;

    @Schema(description = "閫氳鏂瑰悜鍚嶇О", example = "杩涘叆")
    private String directionName;

    @Schema(description = "楠岃瘉鏂瑰紡", example = "face", allowableValues = {"card", "face", "fingerprint", "password", "qr_code"})
    private String verifyType;

    @Schema(description = "楠岃瘉鏂瑰紡鍚嶇О", example = "浜鸿劯璇嗗埆")
    private String verifyTypeName;

    @Schema(description = "浣撴俯鏁版嵁", example = "36.5")
    private Double temperature;

    @Schema(description = "浣撴俯鐘舵€?, example = "normal", allowableValues = {"normal", "fever", "low"})
    private String temperatureStatus;

    @Schema(description = "鏄惁浣╂埓鍙ｇ僵", example = "true")
    private Boolean wearingMask;

    @Schema(description = "鍙ｇ僵妫€娴嬬疆淇″害", example = "95.2")
    private Double maskConfidence;

    @Schema(description = "娲讳綋妫€娴嬬粨鏋?, example = "true")
    private Boolean livenessCheckResult;

    @Schema(description = "娲讳綋妫€娴嬬疆淇″害", example = "98.7")
    private Double livenessConfidence;

    @Schema(description = "閫氳鐓х墖URL", example = "https://example.com/access_photo.jpg")
    private String photoUrl;

    @Schema(description = "閫氳鐓х墖缂╃暐鍥綰RL", example = "https://example.com/access_photo_thumb.jpg")
    private String photoThumbUrl;

    @Schema(description = "鍗″彿", example = "1234567890")
    private String cardNumber;

    @Schema(description = "鎸囩汗ID", example = "FINGER_001")
    private String fingerprintId;

    @Schema(description = "浜鸿劯ID", example = "FACE_001")
    private String faceId;

    @Schema(description = "澶勭悊鑰楁椂锛堟绉掞級", example = "500")
    private Long processTime;

    @Schema(description = "鍖归厤搴?, example = "98.5")
    private Double matchScore;

    @Schema(description = "鍖归厤闃堝€?, example = "80.0")
    private Double matchThreshold;

    @Schema(description = "寮傚父鍘熷洜", example = "")
    private String abnormalReason;

    @Schema(description = "鏄惁寮傚父璁板綍", example = "false")
    private Boolean isAbnormal;

    @Schema(description = "閫氳鏉冮檺绾у埆", example = "normal")
    private String permissionLevel;

    @Schema(description = "鏉冮檺鐘舵€?, example = "valid", allowableValues = {"valid", "expired", "revoked"})
    private String permissionStatus;

    @Schema(description = "鏉冮檺杩囨湡鏃堕棿", example = "2025-12-31T23:59:59")
    private LocalDateTime permissionExpireTime;

    @Schema(description = "楠岃瘉璇︽儏鍒楄〃")
    private List<VerifyDetail> verifyDetails;

    @Schema(description = "璁惧鐘舵€佷俊鎭?)
    private Map<String, Object> deviceStatusInfo;

    @Schema(description = "鎿嶄綔鏃ュ織")
    private List<OperationLog> operationLogs;

    @Schema(description = "澶囨敞", example = "")
    private String remark;

    @Schema(description = "鍒涘缓鏃堕棿", example = "2025-12-16T15:30:00")
    private LocalDateTime createTime;

    @Schema(description = "鏇存柊鏃堕棿", example = "2025-12-16T15:30:00")
    private LocalDateTime updateTime;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "楠岃瘉璇︽儏")
    public static class VerifyDetail {

        @Schema(description = "楠岃瘉姝ラ", example = "1")
        private Integer step;

        @Schema(description = "楠岃瘉绫诲瀷", example = "face")
        private String verifyType;

        @Schema(description = "楠岃瘉绫诲瀷鍚嶇О", example = "浜鸿劯璇嗗埆")
        private String verifyTypeName;

        @Schema(description = "楠岃瘉缁撴灉", example = "success", allowableValues = {"success", "fail", "skip"})
        private String verifyResult;

        @Schema(description = "楠岃瘉缁撴灉鍚嶇О", example = "鎴愬姛")
        private String verifyResultName;

        @Schema(description = "鍖归厤搴?, example = "98.5")
        private Double matchScore;

        @Schema(description = "鑰楁椂锛堟绉掞級", example = "200")
        private Long duration;

        @Schema(description = "璇︽儏淇℃伅", example = "浜鸿劯鐗瑰緛鍖归厤鎴愬姛")
        private String detail;

        @Schema(description = "鍥剧墖URL", example = "https://example.com/verify_photo.jpg")
        private String photoUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "鎿嶄綔鏃ュ織")
    public static class OperationLog {

        @Schema(description = "鎿嶄綔鏃堕棿", example = "2025-12-16T15:30:00")
        private LocalDateTime operationTime;

        @Schema(description = "鎿嶄綔绫诲瀷", example = "verify", allowableValues = {"verify", "open", "close", "alarm"})
        private String operationType;

        @Schema(description = "鎿嶄綔绫诲瀷鍚嶇О", example = "楠岃瘉")
        private String operationTypeName;

        @Schema(description = "鎿嶄綔缁撴灉", example = "success", allowableValues = {"success", "fail", "pending"})
        private String operationResult;

        @Schema(description = "鎿嶄綔缁撴灉鍚嶇О", example = "鎴愬姛")
        private String operationResultName;

        @Schema(description = "鎿嶄綔鎻忚堪", example = "鐢ㄦ埛楠岃瘉閫氳繃")
        private String description;

        @Schema(description = "鎿嶄綔浜篒D", example = "1001")
        private Long operatorId;

        @Schema(description = "鎿嶄綔浜哄鍚?, example = "绯荤粺")
        private String operatorName;
    }
}