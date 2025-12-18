package net.lab1024.sa.access.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 闂ㄧ璁板綍鍝嶅簲
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "闂ㄧ璁板綍鍝嶅簲")
public class AccessRecordResponse {

    @Schema(description = "璁板綍ID", example = "100001")
    private Long recordId;

    @Schema(description = "鐢ㄦ埛ID", example = "1001")
    private Long userId;

    @Schema(description = "鐢ㄦ埛鍚?, example = "admin")
    private String username;

    @Schema(description = "鐪熷疄濮撳悕", example = "绯荤粺绠＄悊鍛?)
    private String realName;

    @Schema(description = "閮ㄩ棬鍚嶇О", example = "鎶€鏈儴")
    private String departmentName;

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

    @Schema(description = "娲讳綋妫€娴嬬粨鏋?, example = "true")
    private Boolean livenessCheckResult;

    @Schema(description = "閫氳鐓х墖URL", example = "https://example.com/access_photo.jpg")
    private String photoUrl;

    @Schema(description = "鍗″彿", example = "1234567890")
    private String cardNumber;

    @Schema(description = "璁惧IP", example = "192.168.1.100")
    private String deviceIp;

    @Schema(description = "澶勭悊鑰楁椂锛堟绉掞級", example = "500")
    private Long processTime;

    @Schema(description = "鍖归厤搴?, example = "98.5")
    private Double matchScore;

    @Schema(description = "寮傚父鍘熷洜", example = "")
    private String abnormalReason;

    @Schema(description = "鏄惁寮傚父璁板綍", example = "false")
    private Boolean isAbnormal;

    @Schema(description = "澶囨敞", example = "")
    private String remark;
}