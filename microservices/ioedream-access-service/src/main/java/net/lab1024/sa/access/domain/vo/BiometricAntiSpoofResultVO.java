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
 * 鐢熺墿璇嗗埆闃蹭吉妫€娴嬬粨鏋滆鍥惧璞?
 * <p>
 * 鐢熺墿璇嗗埆闃蹭吉鍒嗘瀽缁撴灉鐨勬暟鎹紶杈撳璞?
 * 涓ユ牸閬靛惊CLADE.md瑙勮寖锛?
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
@Schema(description = "鐢熺墿璇嗗埆闃蹭吉妫€娴嬬粨鏋?)
public class BiometricAntiSpoofResultVO {

    /**
     * 鐢ㄦ埛ID
     */
    @Schema(description = "鐢ㄦ埛ID", example = "1001")
    private Long userId;

    /**
     * 鐢熺墿璇嗗埆绫诲瀷
     */
    @Schema(description = "鐢熺墿璇嗗埆绫诲瀷", example = "FACE")
    private String biometricType;

    /**
     * 娲讳綋妫€娴嬭瘎鍒嗭紙0-100锛?
     */
    @Schema(description = "娲讳綋妫€娴嬭瘎鍒嗭紙0-100锛?, example = "96.8")
    private BigDecimal livenessScore;

    /**
     * 娣卞害浼€犳娴嬭瘎鍒嗭紙0-100锛?
     */
    @Schema(description = "娣卞害浼€犳娴嬭瘎鍒嗭紙0-100锛?, example = "94.2")
    private BigDecimal deepfakeScore;

    /**
     * 璐ㄩ噺璇勪及璇勫垎锛?-100锛?
     */
    @Schema(description = "璐ㄩ噺璇勪及璇勫垎锛?-100锛?, example = "91.5")
    private BigDecimal qualityScore;

    /**
     * 3D缁撴瀯鍒嗘瀽璇勫垎锛?-100锛?
     */
    @Schema(description = "3D缁撴瀯鍒嗘瀽璇勫垎锛?-100锛?, example = "89.7")
    private BigDecimal structureScore;

    /**
     * 缁煎悎闃蹭吉璇勫垎锛?-100锛?
     */
    @Schema(description = "缁煎悎闃蹭吉璇勫垎锛?-100锛?, example = "92.8")
    private BigDecimal overallScore;

    /**
     * 鏄惁閫氳繃闃蹭吉妫€娴?
     */
    @Schema(description = "鏄惁閫氳繃闃蹭吉妫€娴?, example = "true")
    private Boolean passedAntiSpoofing;

    /**
     * 椋庨櫓绛夌骇
     * LOW - 浣庨闄?
     * MEDIUM - 涓瓑椋庨櫓
     * HIGH - 楂橀闄?
     * CRITICAL - 涓ラ噸椋庨櫓
     */
    @Schema(description = "椋庨櫓绛夌骇", example = "LOW")
    private String riskLevel;

    /**
     * 妫€娴嬭鎯?
     */
    @Schema(description = "妫€娴嬭鎯?)
    private Map<String, Object> detectionDetails;

    /**
     * 妫€娴嬪埌鐨勬敾鍑荤被鍨?
     */
    @Schema(description = "妫€娴嬪埌鐨勬敾鍑荤被鍨?)
    private List<String> detectedAttackTypes;

    /**
     * 淇′换搴﹀垎鏁?
     */
    @Schema(description = "淇′换搴﹀垎鏁?, example = "0.98")
    private BigDecimal trustScore;

    /**
     * 澶勭悊寤鸿
     */
    @Schema(description = "澶勭悊寤鸿", example = "鐢熺墿璇嗗埆鏁版嵁璐ㄩ噺鑹ソ锛屽彲浠ユ甯镐娇鐢?)
    private String recommendation;

    /**
     * 妫€娴嬭€楁椂锛堟绉掞級
     */
    @Schema(description = "妫€娴嬭€楁椂锛堟绉掞級", example = "156")
    private Long detectionDuration;

    /**
     * 妫€娴嬫椂闂?
     */
    @Schema(description = "妫€娴嬫椂闂?, example = "2025-01-30T15:45:00")
    private LocalDateTime detectionTime;

    /**
     * 璁惧淇℃伅
     */
    @Schema(description = "璁惧淇℃伅", example = "iPhone 13 Pro")
    private String deviceInfo;

    /**
     * 鐜鏉′欢
     */
    @Schema(description = "鐜鏉′欢", example = "瀹ゅ唴姝ｅ父鍏夌収")
    private String environmentalConditions;

    /**
     * 妫€娴嬫寚鏍囪鎯呭唴閮ㄧ被
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "妫€娴嬫寚鏍囪鎯?)
    public static class DetectionMetricVO {

        @Schema(description = "鎸囨爣鍚嶇О", example = "鐪ㄧ溂妫€娴?)
        private String metricName;

        @Schema(description = "鎸囨爣鍊?, example = "0.95")
        private BigDecimal metricValue;

        @Schema(description = "闃堝€?, example = "0.7")
        private BigDecimal threshold;

        @Schema(description = "鏄惁閫氳繃", example = "true")
        private Boolean passed;

        @Schema(description = "鎸囨爣鎻忚堪", example = "妫€娴嬪埌鑷劧鐨勭湪鐪艰涓?)
        private String description;

        @Schema(description = "鏉冮噸", example = "0.25")
        private BigDecimal weight;
    }

    /**
     * 鏀诲嚮绫诲瀷璇︽儏鍐呴儴绫?
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "鏀诲嚮绫诲瀷璇︽儏")
    public static class AttackTypeVO {

        @Schema(description = "鏀诲嚮绫诲瀷", example = "PHOTO_ATTACK")
        private String attackType;

        @Schema(description = "缃俊搴?, example = "0.15")
        private BigDecimal confidence;

        @Schema(description = "鎻忚堪", example = "妫€娴嬪埌鐓х墖鏀诲嚮鐨勫彲鑳芥€?)
        private String description;

        @Schema(description = "涓ラ噸绋嬪害", example = "LOW")
        private String severity;

        @Schema(description = "闃叉姢寤鸿", example = "瑕佹眰鐢ㄦ埛鎻愪緵鏇撮珮璐ㄩ噺鐨勭敓鐗╄瘑鍒暟鎹?)
        private String mitigationAdvice;
    }

    /**
     * 鐜鍒嗘瀽鍐呴儴绫?
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "鐜鍒嗘瀽")
    public static class EnvironmentalAnalysisVO {

        @Schema(description = "鍏夌収鏉′欢", example = "NORMAL")
        private String lightingCondition;

        @Schema(description = "鑳屾櫙澶嶆潅搴?, example = "LOW")
        private String backgroundComplexity;

        @Schema(description = "璁惧瑙掑害", example = "NORMAL")
        private String deviceAngle;

        @Schema(description = "閬尅鎯呭喌", example = "NONE")
        private String occlusionStatus;

        @Schema(description = "鐜璇勫垎", example = "92.5")
        private BigDecimal environmentalScore;
    }
}