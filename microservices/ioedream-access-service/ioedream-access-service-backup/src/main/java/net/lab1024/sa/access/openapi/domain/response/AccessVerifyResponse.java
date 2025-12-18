package net.lab1024.sa.access.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 闂ㄧ閫氳楠岃瘉鍝嶅簲
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "闂ㄧ閫氳楠岃瘉鍝嶅簲")
public class AccessVerifyResponse {

    @Schema(description = "鏄惁鍏佽閫氳", example = "true")
    private Boolean allowAccess;

    @Schema(description = "閫氳鐘舵€佺爜", example = "200")
    private String statusCode;

    @Schema(description = "閫氳鐘舵€佹秷鎭?, example = "楠岃瘉鎴愬姛锛屽厑璁搁€氳")
    private String statusMessage;

    @Schema(description = "閫氳璁板綍ID", example = "100001")
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

    @Schema(description = "鍖哄煙ID", example = "1")
    private Long areaId;

    @Schema(description = "鍖哄煙鍚嶇О", example = "涓€妤煎ぇ鍘?)
    private String areaName;

    @Schema(description = "閫氳鏃堕棿", example = "2025-12-16T15:30:00")
    private LocalDateTime accessTime;

    @Schema(description = "閫氳鏂瑰悜", example = "in")
    private String direction;

    @Schema(description = "楠岃瘉鏂瑰紡", example = "face")
    private String verifyMethod;

    @Schema(description = "楠岃瘉缁撴灉璇︽儏", example = "浜鸿劯璇嗗埆鍖归厤搴︼細98.5%")
    private String verifyResultDetail;

    @Schema(description = "浣撴俯鏁版嵁", example = "36.5")
    private Double temperature;

    @Schema(description = "浣撴俯鐘舵€?, example = "normal", allowableValues = {"normal", "fever", "low"})
    private String temperatureStatus;

    @Schema(description = "鏄惁浣╂埓鍙ｇ僵", example = "true")
    private Boolean wearingMask;

    @Schema(description = "娲讳綋妫€娴嬬粨鏋?, example = "true")
    private Boolean livenessCheckResult;

    @Schema(description = "閫氳鏉冮檺绾у埆", example = "normal")
    private String permissionLevel;

    @Schema(description = "鍓╀綑閫氳娆℃暟", example = "95")
    private Integer remainingAccessCount;

    @Schema(description = "鏉冮檺杩囨湡鏃堕棿", example = "2025-12-31T23:59:59")
    private LocalDateTime permissionExpireTime;

    @Schema(description = "鍥剧墖URL", example = "https://example.com/access_photo.jpg")
    private String photoUrl;

    @Schema(description = "璇煶鎻愮ず", example = "娆㈣繋杩涘叆")
    private String voicePrompt;

    @Schema(description = "璀﹀憡淇℃伅", example = "")
    private String warningMessage;

    @Schema(description = "鎵╁睍淇℃伅", example = "{\"key1\":\"value1\"}")
    private String extendedInfo;

    @Schema(description = "鐢ㄦ埛鏉冮檺鍒楄〃")
    private List<PermissionInfo> permissions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "鏉冮檺淇℃伅")
    public static class PermissionInfo {

        @Schema(description = "璁惧ID", example = "ACCESS_001")
        private String deviceId;

        @Schema(description = "璁惧鍚嶇О", example = "涓婚棬绂?)
        private String deviceName;

        @Schema(description = "鏉冮檺绫诲瀷", example = "permanent")
        private String permissionType;

        @Schema(description = "鏉冮檺绾у埆", example = "normal")
        private String permissionLevel;

        @Schema(description = "鍏佽閫氳鏃堕棿", example = "鍏ㄥぉ")
        private String allowedTime;

        @Schema(description = "鏄惁鍦ㄦ湁鏁堟湡鍐?, example = "true")
        private Boolean isValid;
    }
}