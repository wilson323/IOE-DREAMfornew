package net.lab1024.sa.access.domain.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 闂ㄧ鍙嶆綔鍥炵瓥鐣ラ厤缃〃鍗?
 * <p>
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛?
 * - 浣跨敤Form鍚庣紑鏍囪瘑琛ㄥ崟瀵硅薄
 * - 浣跨敤楠岃瘉娉ㄨВ纭繚鏁版嵁瀹屾暣鎬?
 * - 鍖呭惈Swagger娉ㄨВ渚夸簬API鏂囨。鐢熸垚
 * - 鏀寔鍥涚鍙嶆綔鍥炵瓥鐣ラ厤缃?
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "闂ㄧ鍙嶆綔鍥炵瓥鐣ラ厤缃〃鍗?)
public class AntiPassbackPolicyForm {

    /**
     * 璁惧ID
     */
    @NotNull(message = "璁惧ID涓嶈兘涓虹┖")
    @Schema(description = "璁惧ID", example = "1001")
    private Long deviceId;

    /**
     * 鍙嶆綔鍥炵被鍨?
     */
    @NotBlank(message = "鍙嶆綔鍥炵被鍨嬩笉鑳戒负绌?)
    @Pattern(regexp = "^(NONE|HARD|SOFT|AREA|GLOBAL)$", message = "鍙嶆綔鍥炵被鍨嬪繀椤绘槸NONE銆丠ARD銆丼OFT銆丄REA銆丟LOBAL涔嬩竴")
    @Schema(description = "鍙嶆綔鍥炵被鍨?, example = "HARD", allowableValues = {"NONE", "HARD", "SOFT", "AREA", "GLOBAL"})
    private String antiPassbackType;

    /**
     * 鏃堕棿绐楀彛锛堝垎閽燂級
     */
    @NotNull(message = "鏃堕棿绐楀彛涓嶈兘涓虹┖")
    @Schema(description = "鏃堕棿绐楀彛锛堝垎閽燂級", example = "5")
    private Integer timeWindowMinutes;

    /**
     * 鏄惁鍚敤鍖哄煙浜烘暟闄愬埗
     */
    @Schema(description = "鏄惁鍚敤鍖哄煙浜烘暟闄愬埗", example = "true")
    private Boolean areaCapacityLimitEnabled;

    /**
     * 鍖哄煙鏈€澶т汉鏁?
     */
    @Schema(description = "鍖哄煙鏈€澶т汉鏁?, example = "50")
    private Integer maxAreaCapacity;

    /**
     * 鏄惁鍚敤鍙屽悜閫氳
     */
    @Schema(description = "鏄惁鍚敤鍙屽悜閫氳", example = "true")
    private Boolean bidirectionalAccessEnabled;

    /**
     * 鏄惁璁板綍杞弽娼滃洖寮傚父
     */
    @Schema(description = "鏄惁璁板綍杞弽娼滃洖寮傚父", example = "true")
    private Boolean recordSoftExceptionsEnabled;

    /**
     * 鏄惁鍙戦€佸疄鏃跺憡璀?
     */
    @Schema(description = "鏄惁鍙戦€佸疄鏃跺憡璀?, example = "true")
    private Boolean realTimeAlertEnabled;

    /**
     * 鍛婅闃堝€硷紙杩濊娆℃暟锛?
     */
    @Schema(description = "鍛婅闃堝€硷紙杩濊娆℃暟锛?, example = "5")
    private Integer alertThreshold;

    /**
     * 绛栫暐鎻忚堪
     */
    @Schema(description = "绛栫暐鎻忚堪", example = "楂橀闄╁尯鍩熺‖鍙嶆綔鍥炵瓥鐣?)
    private String description;

    /**
     * 鐢熸晥鏃堕棿
     */
    @Schema(description = "鐢熸晥鏃堕棿", example = "2025-01-30T08:00:00")
    private String effectiveTime;

    /**
     * 澶辨晥鏃堕棿
     */
    @Schema(description = "澶辨晥鏃堕棿", example = "2025-12-31T23:59:59")
    private String expireTime;

    /**
     * 绛栫暐浼樺厛绾?
     */
    @Schema(description = "绛栫暐浼樺厛绾?, example = "1")
    private Integer priority;

    /**
     * 鎵╁睍閰嶇疆锛圝SON鏍煎紡锛?
     */
    @Schema(description = "鎵╁睍閰嶇疆锛圝SON鏍煎紡锛?, example = "{\"customRules\": {\"specialHours\": true}}")
    private String extendedConfig;
}