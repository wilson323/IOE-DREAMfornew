package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * 璁惧鎺у埗璇锋眰琛ㄥ崟
 * 鐢ㄤ簬绉诲姩绔澶囨帶鍒舵搷浣?
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "璁惧鎺у埗璇锋眰琛ㄥ崟")
public class DeviceControlRequest {

    @NotNull(message = "璁惧ID涓嶈兘涓虹┖")
    @Schema(description = "璁惧ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    private Long deviceId;

    @NotBlank(message = "鎺у埗鍛戒护涓嶈兘涓虹┖")
    @Schema(description = "鎺у埗鍛戒护", requiredMode = Schema.RequiredMode.REQUIRED,
           example = "restart", allowableValues = {"restart", "shutdown", "maintenance", "calibrate", "unlock", "lock", "reset"})
    private String command;

    @Schema(description = "鎺у埗鍙傛暟(JSON鏍煎紡)",
           example = "{\"duration\":5,\"reason\":\"瀹氭湡閲嶅惎\"}")
    private String parameters;

    @Schema(description = "鎿嶄綔鍘熷洜", example = "瀹氭湡缁存姢閲嶅惎")
    private String reason;

    @Schema(description = "鏄惁寮哄埗鎵ц", example = "false")
    private Boolean forceExecute;

    @Schema(description = "鎵ц瓒呮椂鏃堕棿(绉?", example = "30")
    @Min(value = 5, message = "瓒呮椂鏃堕棿涓嶈兘灏戜簬5绉?)
    @Max(value = 300, message = "瓒呮椂鏃堕棿涓嶈兘瓒呰繃300绉?)
    private Integer timeoutSeconds;

    @Schema(description = "棰勬湡鎵ц鏃堕棿", example = "2025-12-16T15:00:00")
    private String scheduledTime;

    // 璁惧鐗瑰畾鎺у埗鍙傛暟

    @Schema(description = "閲嶅惎鏂瑰紡", example = "soft", allowableValues = {"soft", "hard"})
    private String restartType;

    @Schema(description = "缁存姢妯″紡鎸佺画鏃堕棿(灏忔椂)", example = "24")
    @Min(value = 1, message = "缁存姢鏃堕棿涓嶈兘灏戜簬1灏忔椂")
    @Max(value = 168, message = "缁存姢鏃堕棿涓嶈兘瓒呰繃168灏忔椂")
    private Integer maintenanceDuration;

    @Schema(description = "鏍″噯绫诲瀷", example = "face", allowableValues = {"face", "fingerprint", "card", "all"})
    private String calibrationType;

    @Schema(description = "鏍″噯绮惧害", example = "high", allowableValues = {"low", "medium", "high"})
    private String calibrationPrecision;

    // 闂ㄧ璁惧鐗规湁鍙傛暟

    @Schema(description = "闂ㄩ攣淇濇寔鏃堕棿(姣)", example = "3000")
    @Min(value = 1000, message = "闂ㄩ攣淇濇寔鏃堕棿涓嶈兘灏戜簬1绉?)
    @Max(value = 30000, message = "闂ㄩ攣淇濇寔鏃堕棿涓嶈兘瓒呰繃30绉?)
    private Integer doorOpenDuration;

    @Schema(description = "鏄惁鍙嶉攣", example = "false")
    private Boolean antiLock;

    // 楠岃瘉鍙傛暟

    @Schema(description = "鎿嶄綔鍛樼敤鎴峰悕", example = "admin")
    private String operatorUsername;

    @Schema(description = "鎿嶄綔鍛樺瘑鐮?鐢ㄤ簬鏁忔劅鎿嶄綔楠岃瘉)", example = "")
    private String operatorPassword;

    @Schema(description = "楠岃瘉鐮?, example = "123456")
    private String verificationCode;
}