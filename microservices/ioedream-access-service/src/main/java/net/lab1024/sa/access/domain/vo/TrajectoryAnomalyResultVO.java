package net.lab1024.sa.access.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 杞ㄨ抗寮傚父妫€娴嬬粨鏋滆鍥惧璞?
 * <p>
 * 鐢ㄦ埛璁块棶杞ㄨ抗寮傚父鍒嗘瀽缁撴灉鐨勬暟鎹紶杈撳璞?
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
@Schema(description = "杞ㄨ抗寮傚父妫€娴嬬粨鏋?)
public class TrajectoryAnomalyResultVO {

    /**
     * 鐢ㄦ埛ID
     */
    @Schema(description = "鐢ㄦ埛ID", example = "1001")
    private Long userId;

    /**
     * 杞ㄨ抗ID
     */
    @Schema(description = "杞ㄨ抗ID", example = "TRAJ_20250130_001")
    private String trajectoryId;

    /**
     * 鍒嗘瀽鏃堕棿鑼冨洿锛堝皬鏃讹級
     */
    @Schema(description = "鍒嗘瀽鏃堕棿鑼冨洿锛堝皬鏃讹級", example = "24")
    private Integer analysisTimeRange;

    /**
     * 寮傚父璇勫垎锛?-100锛?
     */
    @Schema(description = "寮傚父璇勫垎锛?-100锛?, example = "78.5")
    private BigDecimal anomalyScore;

    /**
     * 鏄惁妫€娴嬪埌寮傚父
     */
    @Schema(description = "鏄惁妫€娴嬪埌寮傚父", example = "true")
    private Boolean anomalyDetected;

    /**
     * 寮傚父绛夌骇
     * LOW - 浣庣骇寮傚父
     * MEDIUM - 涓骇寮傚父
     * HIGH - 楂樼骇寮傚父
     * CRITICAL - 涓ラ噸寮傚父
     */
    @Schema(description = "寮傚父绛夌骇", example = "MEDIUM")
    private String anomalyLevel;

    /**
     * 寮傚父绫诲瀷
     */
    @Schema(description = "寮傚父绫诲瀷", example = "TIME_PATTERN_ANOMALY")
    private List<String> anomalyTypes;

    /**
     * 鏃堕棿妯″紡寮傚父
     */
    @Schema(description = "鏃堕棿妯″紡寮傚父")
    private TimePatternAnomalyVO timePatternAnomaly;

    /**
     * 绌洪棿妯″紡寮傚父
     */
    @Schema(description = "绌洪棿妯″紡寮傚父")
    private SpatialPatternAnomalyVO spatialPatternAnomaly;

    /**
     * 棰戠巼寮傚父
     */
    @Schema(description = "棰戠巼寮傚父")
    private FrequencyAnomalyVO frequencyAnomaly;

    /**
     * 琛屼负搴忓垪寮傚父
     */
    @Schema(description = "琛屼负搴忓垪寮傚父")
    private List<BehaviorSequenceAnomalyVO> behaviorAnomalies;

    /**
     * 椋庨櫓璇勪及
     */
    @Schema(description = "椋庨櫓璇勪及")
    private RiskAssessmentVO riskAssessment;

    /**
     * 澶勭悊寤鸿
     */
    @Schema(description = "澶勭悊寤鸿", example = "寤鸿杩涗竴姝ラ獙璇佺敤鎴疯韩浠斤紝骞剁洃鎺у悗缁闂涓?)
    private String recommendation;

    /**
     * 鍒嗘瀽鏃堕棿
     */
    @Schema(description = "鍒嗘瀽鏃堕棿", example = "2025-01-30T15:45:00")
    private LocalDateTime analysisTime;

    /**
     * 鏃堕棿妯″紡寮傚父鍐呴儴绫?
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "鏃堕棿妯″紡寮傚父")
    public static class TimePatternAnomalyVO {

        @Schema(description = "寮傚父绫诲瀷", example = "ABNORMAL_TIME_ACCESS")
        private String anomalyType;

        @Schema(description = "寮傚父鏃堕棿鍒楄〃", example = "02:30, 04:45")
        private List<String> abnormalTimes;

        @Schema(description = "鍋忕绋嬪害", example = "3.5")
        private BigDecimal deviationScore;

        @Schema(description = "棰戠巼", example = "HIGH")
        private String frequency;

        @Schema(description = "寮傚父鎻忚堪", example = "鐢ㄦ埛鍦ㄩ潪宸ヤ綔鏃堕棿娈垫湁澶氭璁块棶璁板綍")
        private String description;
    }

    /**
     * 绌洪棿妯″紡寮傚父鍐呴儴绫?
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "绌洪棿妯″紡寮傚父")
    public static class SpatialPatternAnomalyVO {

        @Schema(description = "寮傚父绫诲瀷", example = "UNUSUAL_AREA_PATTERN")
        private String anomalyType;

        @Schema(description = "寮傚父鍖哄煙", example = "鏈嶅姟鍣ㄦ満鎴? 楂樼骇绠＄悊鍖?)
        private List<String> unusualAreas;

        @Schema(description = "璁块棶棰戠巼", example = "FIRST_TIME_ACCESS")
        private String accessFrequency;

        @Schema(description = "鍋忕绋嬪害", example = "4.2")
        private BigDecimal deviationScore;

        @Schema(description = "寮傚父鎻忚堪", example = "鐢ㄦ埛棣栨璁块棶楂樺畨鍏ㄧ瓑绾у尯鍩?)
        private String description;
    }

    /**
     * 棰戠巼寮傚父鍐呴儴绫?
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "棰戠巼寮傚父")
    public static class FrequencyAnomalyVO {

        @Schema(description = "寮傚父绫诲瀷", example = "EXCESSIVE_FREQUENCY")
        private String anomalyType;

        @Schema(description = "姝ｅ父棰戠巼", example = "15")
        private BigDecimal normalFrequency;

        @Schema(description = "瀹為檯棰戠巼", example = "85")
        private BigDecimal actualFrequency;

        @Schema(description = "棰戠巼鍊嶆暟", example = "5.7")
        private BigDecimal frequencyMultiplier;

        @Schema(description = "鏃堕棿绐楀彛", example = "24灏忔椂")
        private String timeWindow;

        @Schema(description = "寮傚父鎻忚堪", example = "鐢ㄦ埛璁块棶棰戠巼杩滆秴姝ｅ父姘村钩")
        private String description;
    }

    /**
     * 琛屼负搴忓垪寮傚父鍐呴儴绫?
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "琛屼负搴忓垪寮傚父")
    public static class BehaviorSequenceAnomalyVO {

        @Schema(description = "搴忓垪ID", example = "SEQ_001")
        private String sequenceId;

        @Schema(description = "寮傚父绫诲瀷", example = "RAPID_SUCCESSIVE_ACCESS")
        private String anomalyType;

        @Schema(description = "璁块棶鐐瑰簭鍒?, example = "DEVICE_001->DEVICE_002->DEVICE_003")
        private List<String> accessSequence;

        @Schema(description = "鏃堕棿闂撮殧", example = "30绉? 45绉? 20绉?)
        private List<String> timeIntervals;

        @Schema(description = "寮傚父璇勫垎", example = "8.5")
        private BigDecimal anomalyScore;

        @Schema(description = "缃俊搴?, example = "0.92")
        private BigDecimal confidence;

        @Schema(description = "鎻忚堪", example = "妫€娴嬪埌蹇€熺殑杩炵画璁块棶妯″紡")
        private String description;
    }

    /**
     * 椋庨櫓璇勪及鍐呴儴绫?
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "椋庨櫓璇勪及")
    public static class RiskAssessmentVO {

        @Schema(description = "椋庨櫓绛夌骇", example = "MEDIUM")
        private String riskLevel;

        @Schema(description = "椋庨櫓璇勫垎", example = "65.5")
        private BigDecimal riskScore;

        @Schema(description = "娼滃湪濞佽儊", example = "鍙兘瀛樺湪韬唤鍐掔敤鎴栧紓甯歌闂涓?)
        private String potentialThreat;

        @Schema(description = "褰卞搷鑼冨洿", example = "鍖哄煙瀹夊叏銆佹暟鎹闂?)
        private String impactScope;

        @Schema(description = "寤鸿鎺柦", example = "澧炲姞浜屾楠岃瘉锛岀洃鎺у悗缁涓?)
        private List<String> recommendedMeasures;

        @Schema(description = "绱ф€ョ▼搴?, example = "MEDIUM")
        private String urgency;
    }
}