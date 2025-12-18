package net.lab1024.sa.access.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 闂ㄧ璁惧鍝嶅簲
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "闂ㄧ璁惧鍝嶅簲")
public class AccessDeviceResponse {

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

    @Schema(description = "鏈€鍚庡湪绾挎椂闂?, example = "2025-12-16T15:30:00")
    private LocalDateTime lastOnlineTime;

    @Schema(description = "鏈€鍚庣绾挎椂闂?, example = "2025-12-16T10:30:00")
    private LocalDateTime lastOfflineTime;

    @Schema(description = "瀹夎鏃堕棿", example = "2025-01-01T10:30:00")
    private LocalDateTime installTime;

    @Schema(description = "鍥轰欢鐗堟湰", example = "v1.2.3")
    private String firmwareVersion;

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

    @Schema(description = "璁惧鎻忚堪", example = "涓€妤煎ぇ鍘呬富鍏ュ彛闂ㄧ璁惧")
    private String description;

    @Schema(description = "澶囨敞", example = "")
    private String remark;
}