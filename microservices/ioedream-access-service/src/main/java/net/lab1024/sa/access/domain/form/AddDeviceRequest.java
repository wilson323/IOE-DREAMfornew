package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.time.LocalDateTime;

/**
 * 娣诲姞璁惧璇锋眰琛ㄥ崟
 * 鐢ㄤ簬绉诲姩绔坊鍔犳柊璁惧
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "娣诲姞璁惧璇锋眰琛ㄥ崟")
public class AddDeviceRequest {

    @NotBlank(message = "璁惧缂栫爜涓嶈兘涓虹┖")
    @Size(max = 50, message = "璁惧缂栫爜闀垮害涓嶈兘瓒呰繃50涓瓧绗?)
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "璁惧缂栫爜鍙兘鍖呭惈瀛楁瘝銆佹暟瀛椼€佷笅鍒掔嚎鍜屾í绾?)
    @Schema(description = "璁惧缂栫爜", requiredMode = Schema.RequiredMode.REQUIRED, example = "DEV_ACCESS_001")
    private String deviceCode;

    @NotBlank(message = "璁惧鍚嶇О涓嶈兘涓虹┖")
    @Size(max = 100, message = "璁惧鍚嶇О闀垮害涓嶈兘瓒呰繃100涓瓧绗?)
    @Schema(description = "璁惧鍚嶇О", requiredMode = Schema.RequiredMode.REQUIRED, example = "涓诲叆鍙ｉ棬绂佹帶鍒跺櫒")
    private String deviceName;

    @NotNull(message = "璁惧绫诲瀷涓嶈兘涓虹┖")
    @Schema(description = "璁惧绫诲瀷", requiredMode = Schema.RequiredMode.REQUIRED,
           allowableValues = {"1", "2", "3", "4", "5", "6", "7", "8"}, example = "1")
    private Integer deviceType;

    @Schema(description = "璁惧瀛愮被鍨?, example = "11")
    private Integer deviceSubType;

    @NotNull(message = "鎵€灞炲尯鍩熶笉鑳戒负绌?)
    @Schema(description = "鎵€灞炲尯鍩烮D", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    private Long areaId;

    @NotBlank(message = "璁惧浣嶇疆涓嶈兘涓虹┖")
    @Size(max = 200, message = "璁惧浣嶇疆闀垮害涓嶈兘瓒呰繃200涓瓧绗?)
    @Schema(description = "璁惧浣嶇疆", requiredMode = Schema.RequiredMode.REQUIRED, example = "A鏍?妤煎ぇ鍘呬富鍏ュ彛")
    private String location;

    @Schema(description = "璁惧IP鍦板潃", example = "192.168.1.100")
    @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
            message = "IP鍦板潃鏍煎紡涓嶆纭?)
    private String ipAddress;

    @Schema(description = "璁惧MAC鍦板潃", example = "AA:BB:CC:DD:EE:FF")
    @Pattern(regexp = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$",
            message = "MAC鍦板潃鏍煎紡涓嶆纭?)
    private String macAddress;

    @Schema(description = "璁惧鍨嬪彿", example = "IOE-ACCESS-2000")
    @Size(max = 50, message = "璁惧鍨嬪彿闀垮害涓嶈兘瓒呰繃50涓瓧绗?)
    private String deviceModel;

    @Schema(description = "鍥轰欢鐗堟湰", example = "v2.1.0")
    @Size(max = 20, message = "鍥轰欢鐗堟湰闀垮害涓嶈兘瓒呰繃20涓瓧绗?)
    private String firmwareVersion;

    @Schema(description = "璁惧鍘傚晢", example = "IOE绉戞妧")
    @Size(max = 100, message = "璁惧鍘傚晢闀垮害涓嶈兘瓒呰繃100涓瓧绗?)
    private String manufacturer;

    @Schema(description = "瀹夎鏃堕棿", example = "2024-01-15T10:30:00")
    private LocalDateTime installTime;

    @Schema(description = "璁惧鎻忚堪", example = "涓诲叆鍙ｉ棬绂佹帶鍒跺櫒锛屾敮鎸佷汉鑴歌瘑鍒拰鍒峰崱")
    @Size(max = 500, message = "璁惧鎻忚堪闀垮害涓嶈兘瓒呰繃500涓瓧绗?)
    private String description;

    @Schema(description = "鏄惁鏀寔杩滅▼鎺у埗", example = "true")
    private Boolean supportRemoteControl;

    @Schema(description = "鏄惁鏀寔鍥轰欢鍗囩骇", example = "true")
    private Boolean supportFirmwareUpgrade;

    @Schema(description = "璁惧鎵╁睍灞炴€?JSON鏍煎紡)",
           example = "{\"accessMode\":\"card+face\",\"antiPassback\":true,\"openTime\":3000}")
    private String extendedAttributes;

    @Schema(description = "鏉冮檺绾у埆", example = "1")
    @Min(value = 1, message = "鏉冮檺绾у埆涓嶈兘灏忎簬1")
    @Max(value = 10, message = "鏉冮檺绾у埆涓嶈兘澶т簬10")
    private Integer permissionLevel;

    @Schema(description = "涓氬姟妯″潡", example = "access")
    private String businessModule;

    // 瀹夎鐩稿叧鍙傛暟

    @Schema(description = "瀹夎璐熻矗浜?, example = "寮犲伐绋嬪笀")
    @Size(max = 50, message = "瀹夎璐熻矗浜洪暱搴︿笉鑳借秴杩?0涓瓧绗?)
    private String installer;

    @Schema(description = "瀹夎澶囨敞", example = "璁惧瀹夎鍦ㄦ爣鍑嗘満鏌滀腑锛岀數婧愬拰缃戠粶杩炴帴姝ｅ父")
    @Size(max = 300, message = "瀹夎澶囨敞闀垮害涓嶈兘瓒呰繃300涓瓧绗?)
    private String installNotes;

    @Schema(description = "瀹夎鍧愭爣-绾害", example = "39.9042")
    private Double latitude;

    @Schema(description = "瀹夎鍧愭爣-缁忓害", example = "116.4074")
    private Double longitude;

    @Schema(description = "瀹夎楂樺害(绫?", example = "1.5")
    @Min(value = 0, message = "瀹夎楂樺害涓嶈兘涓鸿礋鏁?)
    private Double installHeight;

    // 缃戠粶閰嶇疆

    @Schema(description = "缃戠粶绔彛", example = "8080")
    @Min(value = 1, message = "缃戠粶绔彛涓嶈兘灏忎簬1")
    @Max(value = 65535, message = "缃戠粶绔彛涓嶈兘澶т簬65535")
    private Integer networkPort;

    @Schema(description = "缃戠粶鍗忚", example = "TCP", allowableValues = {"TCP", "UDP", "HTTP", "HTTPS"})
    private String networkProtocol;

    @Schema(description = "鍔犲瘑瀵嗛挜", example = "")
    private String encryptionKey;

    // 楠岃瘉淇℃伅

    @Schema(description = "璁惧搴忓垪鍙?, example = "SN2024011500123")
    @Size(max = 50, message = "璁惧搴忓垪鍙烽暱搴︿笉鑳借秴杩?0涓瓧绗?)
    private String serialNumber;

    @Schema(description = "璁惧璁よ瘉鐮?, example = "AUTH20240115")
    @Size(max = 50, message = "璁惧璁よ瘉鐮侀暱搴︿笉鑳借秴杩?0涓瓧绗?)
    private String authCode;
}