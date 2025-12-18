package net.lab1024.sa.access.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 闂ㄧ鎺у埗鍝嶅簲
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "闂ㄧ鎺у埗鍝嶅簲")
public class AccessControlResponse {

    @Schema(description = "鏄惁鎴愬姛", example = "true")
    private Boolean success;

    @Schema(description = "鎺у埗缁撴灉鐮?, example = "200")
    private String resultCode;

    @Schema(description = "鎺у埗缁撴灉娑堟伅", example = "鎿嶄綔鎴愬姛")
    private String resultMessage;

    @Schema(description = "璁惧ID", example = "ACCESS_001")
    private String deviceId;

    @Schema(description = "璁惧鍚嶇О", example = "涓婚棬绂?)
    private String deviceName;

    @Schema(description = "鎿嶄綔绫诲瀷", example = "open")
    private String action;

    @Schema(description = "鎿嶄綔鏃堕棿", example = "2025-12-16T15:30:00")
    private LocalDateTime operationTime;

    @Schema(description = "鎵ц鏃堕棿", example = "2025-12-16T15:30:01")
    private LocalDateTime executeTime;

    @Schema(description = "鎿嶄綔鐘舵€?, example = "completed", allowableValues = {"pending", "executing", "completed", "failed"})
    private String operationStatus;

    @Schema(description = "鎿嶄綔浜篒D", example = "1001")
    private Long operatorId;

    @Schema(description = "鎿嶄綔浜哄鍚?, example = "寮犱笁")
    private String operatorName;

    @Schema(description = "鎿嶄綔鍘熷洜", example = "璁垮閫氳")
    private String reason;

    @Schema(description = "褰撳墠璁惧鐘舵€?, example = "open", allowableValues = {"open", "close", "lock", "unlock", "fault"})
    private String deviceStatus;

    @Schema(description = "璁惧鍝嶅簲娑堟伅", example = "闂ㄥ凡寮€鍚?)
    private String deviceResponse;

    @Schema(description = "寮€闂ㄦ椂闀匡紙绉掞級", example = "30")
    private Integer openDuration;

    @Schema(description = "棰勮鍏抽棴鏃堕棿", example = "2025-12-16T15:30:30")
    private LocalDateTime estimatedCloseTime;

    @Schema(description = "鎿嶄綔璁板綍ID", example = "100001")
    private Long operationRecordId;

    @Schema(description = "鏄惁闇€瑕佺‘璁?, example = "false")
    private Boolean needConfirmation;

    @Schema(description = "纭瓒呮椂鏃堕棿锛堢锛?, example = "60")
    private Integer confirmationTimeout;

    @Schema(description = "璀﹀憡淇℃伅", example = "")
    private String warningMessage;

    @Schema(description = "閿欒浠ｇ爜", example = "")
    private String errorCode;

    @Schema(description = "閿欒璇︽儏", example = "")
    private String errorDetail;

    @Schema(description = "鎵╁睍淇℃伅", example = "{\"key1\":\"value1\"}")
    private String extendedInfo;
}