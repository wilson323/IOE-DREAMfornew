package net.lab1024.sa.access.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 缁存姢棰勬祴瑙嗗浘瀵硅薄
 * <p>
 * 璁惧棰勬祴鎬х淮鎶ょ粨鏋滅殑鏁版嵁浼犺緭瀵硅薄
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛?
 * - 浣跨敤@Data娉ㄨВ
 * - 瀹屾暣鐨勫瓧娈垫枃妗ｆ敞瑙?
 * - 鏋勫缓鑰呮ā寮忔敮鎸?
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
@Schema(description = "缁存姢棰勬祴淇℃伅")
public class MaintenancePredictionVO {

    /**
     * 璁惧ID
     */
    @Schema(description = "璁惧ID", example = "1001")
    private Long deviceId;

    /**
     * 璁惧鍚嶇О
     */
    @Schema(description = "璁惧鍚嶇О", example = "涓诲叆鍙ｉ棬绂佹帶鍒跺櫒")
    private String deviceName;

    /**
     * 棰勬祴缁存姢绫诲瀷
     * PREVENTIVE - 棰勯槻鎬х淮鎶?
     * CORRECTIVE - 绾犳鎬х淮鎶?
     * PREDICTIVE - 棰勬祴鎬х淮鎶?
     * EMERGENCY - 绱ф€ョ淮鎶?
     */
    @Schema(description = "棰勬祴缁存姢绫诲瀷", example = "PREDICTIVE")
    private String maintenanceType;

    /**
     * 棰勬祴鏁呴殰姒傜巼锛?锛?
     */
    @Schema(description = "棰勬祴鏁呴殰姒傜巼锛?锛?, example = "23.5")
    private BigDecimal failureProbability;

    /**
     * 椋庨櫓绛夌骇
     * LOW - 浣庨闄?
     * MEDIUM - 涓瓑椋庨櫓
     * HIGH - 楂橀闄?
     * CRITICAL - 涓ラ噸椋庨櫓
     */
    @Schema(description = "椋庨櫓绛夌骇", example = "MEDIUM")
    private String riskLevel;

    /**
     * 寤鸿缁存姢鏃堕棿
     */
    @Schema(description = "寤鸿缁存姢鏃堕棿", example = "2025-02-15T09:00:00")
    private LocalDateTime recommendedMaintenanceTime;

    /**
     * 缁存姢浼樺厛绾?
     */
    @Schema(description = "缁存姢浼樺厛绾?, example = "2")
    private Integer priority;

    /**
     * 棰勮鍋滄満鏃堕暱锛堝皬鏃讹級
     */
    @Schema(description = "棰勮鍋滄満鏃堕暱锛堝皬鏃讹級", example = "2")
    private Integer estimatedDowntimeHours;

    /**
     * 棰勮缁存姢鎴愭湰
     */
    @Schema(description = "棰勮缁存姢鎴愭湰", example = "1200.00")
    private BigDecimal estimatedMaintenanceCost;

    /**
     * 棰勮鍑忓皯鎹熷け
     */
    @Schema(description = "棰勮鍑忓皯鎹熷け", example = "5800.00")
    private BigDecimal estimatedLossReduction;

    /**
     * 鏁呴殰鎻忚堪
     */
    @Schema(description = "鏁呴殰鎻忚堪", example = "鍩轰簬鍘嗗彶鏁版嵁鍒嗘瀽锛岃澶囩綉缁滄ā鍧楀彲鑳藉湪15澶╁悗鍑虹幇杩炴帴涓嶇ǔ瀹?)
    private String failureDescription;

    /**
     * 褰卞搷鍒嗘瀽
     */
    @Schema(description = "褰卞搷鍒嗘瀽", example = "鍙兘瀵艰嚧闂ㄧ鍝嶅簲寤惰繜锛屽奖鍝嶅憳宸ラ€氳鏁堢巼")
    private String impactAnalysis;

    /**
     * 缁存姢寤鸿
     */
    @Schema(description = "缁存姢寤鸿", example = "寤鸿妫€鏌ョ綉缁滆繛鎺ュ櫒锛屾洿鏂板浐浠剁増鏈?)
    private String maintenanceRecommendation;

    /**
     * 鎵€闇€澶囦欢
     */
    @Schema(description = "鎵€闇€澶囦欢")
    private List<RequiredPartVO> requiredParts;

    /**
     * 缁存姢姝ラ
     */
    @Schema(description = "缁存姢姝ラ")
    private List<MaintenanceStepVO> maintenanceSteps;

    /**
     * 棰勬祴缃俊搴︼紙%锛?
     */
    @Schema(description = "棰勬祴缃俊搴︼紙%锛?, example = "87.5")
    private BigDecimal confidenceLevel;

    /**
     * 棰勬祴妯″瀷
     */
    @Schema(description = "棰勬祴妯″瀷", example = "HYBRID_ML_STATISTICAL")
    private String predictionModel;

    /**
     * 鏁版嵁鏉ユ簮
     */
    @Schema(description = "鏁版嵁鏉ユ簮", example = "璁惧杩愯鏃ュ織銆佹晠闅滃巻鍙层€佺幆澧冩暟鎹?)
    private String dataSource;

    /**
     * 棰勬祴鐢熸垚鏃堕棿
     */
    @Schema(description = "棰勬祴鐢熸垚鏃堕棿", example = "2025-01-30T15:45:00")
    private LocalDateTime predictionTime;

    /**
     * 鎵€闇€澶囦欢鍐呴儴绫?
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "鎵€闇€澶囦欢")
    public static class RequiredPartVO {

        @Schema(description = "澶囦欢鍚嶇О", example = "缃戠粶妯″潡")
        private String partName;

        @Schema(description = "澶囦欢鍨嬪彿", example = "NM-2000A")
        private String partModel;

        @Schema(description = "澶囦欢鏁伴噺", example = "1")
        private Integer quantity;

        @Schema(description = "棰勮鎴愭湰", example = "450.00")
        private BigDecimal estimatedCost;

        @Schema(description = "渚涘簲鍟?, example = "璁惧鍘熷巶")
        private String supplier;
    }

    /**
     * 缁存姢姝ラ鍐呴儴绫?
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "缁存姢姝ラ")
    public static class MaintenanceStepVO {

        @Schema(description = "姝ラ搴忓彿", example = "1")
        private Integer stepNumber;

        @Schema(description = "姝ラ鎻忚堪", example = "鏂紑璁惧鐢垫簮")
        private String stepDescription;

        @Schema(description = "棰勮鑰楁椂锛堝垎閽燂級", example = "5")
        private Integer estimatedMinutes;

        @Schema(description = "鎶€鑳借姹?, example = "BASIC")
        private String skillRequirement;

        @Schema(description = "瀹夊叏娉ㄦ剰浜嬮」", example = "纭繚瀹屽叏鏂數鍚庡啀鎿嶄綔")
        private String safetyNote;
    }
}