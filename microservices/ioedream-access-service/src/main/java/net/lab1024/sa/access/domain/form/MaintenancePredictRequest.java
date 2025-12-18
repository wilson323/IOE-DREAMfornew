package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 缁存姢棰勬祴璇锋眰琛ㄥ崟
 * <p>
 * 鐢ㄤ簬璁惧棰勬祴鎬х淮鎶ょ殑璇锋眰鍙傛暟
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛?
 * - 浣跨敤@Data娉ㄨВ
 * - 瀹屾暣鐨勫瓧娈甸獙璇佹敞瑙?
 * - Swagger鏂囨。娉ㄨВ
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "缁存姢棰勬祴璇锋眰")
public class MaintenancePredictRequest {

    /**
     * 璁惧ID
     */
    @NotNull(message = "璁惧ID涓嶈兘涓虹┖")
    @Schema(description = "璁惧ID", example = "1001")
    private Long deviceId;

    /**
     * 棰勬祴澶╂暟
     */
    @Min(value = 1, message = "棰勬祴澶╂暟涓嶈兘灏忎簬1")
    @Max(value = 365, message = "棰勬祴澶╂暟涓嶈兘澶т簬365")
    @Schema(description = "棰勬祴澶╂暟", example = "30")
    private Integer predictionDays = 30;

    /**
     * 棰勬祴妯″瀷绫诲瀷
     * ML - 鏈哄櫒瀛︿範妯″瀷
     * STATISTICAL - 缁熻妯″瀷
     * HYBRID - 娣峰悎妯″瀷
     */
    @Schema(description = "棰勬祴妯″瀷绫诲瀷", example = "HYBRID", allowableValues = {"ML", "STATISTICAL", "HYBRID"})
    private String modelType = "HYBRID";

    /**
     * 椋庨櫓闃堝€?
     */
    @Min(value = 0, message = "椋庨櫓闃堝€间笉鑳藉皬浜?")
    @Max(value = 100, message = "椋庨櫓闃堝€间笉鑳藉ぇ浜?00")
    @Schema(description = "椋庨櫓闃堝€硷紙%锛?, example = "75")
    private Integer riskThreshold = 75;

    /**
     * 鏄惁鍖呭惈鎴愭湰棰勪及
     */
    @Schema(description = "鏄惁鍖呭惈鎴愭湰棰勪及", example = "true")
    private Boolean includeCostEstimation = true;

    /**
     * 鏄惁鎺ㄨ崘缁存姢鏂规
     */
    @Schema(description = "鏄惁鎺ㄨ崘缁存姢鏂规", example = "true")
    private Boolean includeMaintenancePlan = true;
}