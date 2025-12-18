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
 * 璁惧鎬ц兘鍒嗘瀽瑙嗗浘瀵硅薄
 * <p>
 * 璁惧鎬ц兘鍒嗘瀽缁撴灉鐨勬暟鎹紶杈撳璞?
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
@Schema(description = "璁惧鎬ц兘鍒嗘瀽淇℃伅")
public class DevicePerformanceAnalyticsVO {

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
     * 鍒嗘瀽鏃堕棿鑼冨洿
     */
    @Schema(description = "鍒嗘瀽鏃堕棿鑼冨洿", example = "30澶?)
    private String analysisPeriod;

    /**
     * 骞冲潎鍝嶅簲鏃堕棿锛堟绉掞級
     */
    @Schema(description = "骞冲潎鍝嶅簲鏃堕棿锛堟绉掞級", example = "156")
    private Long averageResponseTime;

    /**
     * 鏈€澶у搷搴旀椂闂达紙姣锛?
     */
    @Schema(description = "鏈€澶у搷搴旀椂闂达紙姣锛?, example = "1250")
    private Long maxResponseTime;

    /**
     * 鏈€灏忓搷搴旀椂闂达紙姣锛?
     */
    @Schema(description = "鏈€灏忓搷搴旀椂闂达紙姣锛?, example = "45")
    private Long minResponseTime;

    /**
     * P95鍝嶅簲鏃堕棿锛堟绉掞級
     */
    @Schema(description = "P95鍝嶅簲鏃堕棿锛堟绉掞級", example = "320")
    private Long p95ResponseTime;

    /**
     * P99鍝嶅簲鏃堕棿锛堟绉掞級
     */
    @Schema(description = "P99鍝嶅簲鏃堕棿锛堟绉掞級", example = "580")
    private Long p99ResponseTime;

    /**
     * 鎬昏姹傛鏁?
     */
    @Schema(description = "鎬昏姹傛鏁?, example = "125680")
    private Long totalRequests;

    /**
     * 鎴愬姛璇锋眰鏁?
     */
    @Schema(description = "鎴愬姛璇锋眰鏁?, example = "125432")
    private Long successfulRequests;

    /**
     * 鎴愬姛鐜囷紙%锛?
     */
    @Schema(description = "鎴愬姛鐜囷紙%锛?, example = "99.81")
    private BigDecimal successRate;

    /**
     * 骞冲潎鍚炲悙閲忥紙TPS锛?
     */
    @Schema(description = "骞冲潎鍚炲悙閲忥紙TPS锛?, example = "45.3")
    private BigDecimal averageThroughput;

    /**
     * 宄板€煎悶鍚愰噺锛圱PS锛?
     */
    @Schema(description = "宄板€煎悶鍚愰噺锛圱PS锛?, example = "126.8")
    private BigDecimal peakThroughput;

    /**
     * 閿欒鍒嗗竷
     */
    @Schema(description = "閿欒鍒嗗竷")
    private Map<String, Long> errorDistribution;

    /**
     * 鎬ц兘瓒嬪娍鏁版嵁
     */
    @Schema(description = "鎬ц兘瓒嬪娍鏁版嵁")
    private List<PerformanceTrendVO> performanceTrends;

    /**
     * 璐熻浇鍒嗘瀽
     */
    @Schema(description = "璐熻浇鍒嗘瀽")
    private LoadAnalysisVO loadAnalysis;

    /**
     * 鐡堕鍒嗘瀽
     */
    @Schema(description = "鐡堕鍒嗘瀽")
    private List<BottleneckAnalysisVO> bottlenecks;

    /**
     * 浼樺寲寤鸿
     */
    @Schema(description = "浼樺寲寤鸿")
    private List<OptimizationRecommendationVO> recommendations;

    /**
     * 鍒嗘瀽鐢熸垚鏃堕棿
     */
    @Schema(description = "鍒嗘瀽鐢熸垚鏃堕棿", example = "2025-01-30T15:45:00")
    private LocalDateTime analysisTime;

    /**
     * 鎬ц兘瓒嬪娍鍐呴儴绫?
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "鎬ц兘瓒嬪娍")
    public static class PerformanceTrendVO {

        @Schema(description = "鏃堕棿鐐?, example = "2025-01-30T10:00:00")
        private LocalDateTime timestamp;

        @Schema(description = "鍝嶅簲鏃堕棿", example = "156")
        private Long responseTime;

        @Schema(description = "鍚炲悙閲?, example = "45.3")
        private BigDecimal throughput;

        @Schema(description = "閿欒鐜?, example = "0.2")
        private BigDecimal errorRate;
    }

    /**
     * 璐熻浇鍒嗘瀽鍐呴儴绫?
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "璐熻浇鍒嗘瀽")
    public static class LoadAnalysisVO {

        @Schema(description = "骞冲潎CPU浣跨敤鐜?, example = "35.6")
        private BigDecimal averageCpuUsage;

        @Schema(description = "宄板€糃PU浣跨敤鐜?, example = "78.2")
        private BigDecimal peakCpuUsage;

        @Schema(description = "骞冲潎鍐呭瓨浣跨敤鐜?, example = "42.3")
        private BigDecimal averageMemoryUsage;

        @Schema(description = "宄板€煎唴瀛樹娇鐢ㄧ巼", example = "85.7")
        private BigDecimal peakMemoryUsage;

        @Schema(description = "骞冲潎缃戠粶甯﹀浣跨敤", example = "2.3")
        private BigDecimal averageNetworkUsage;

        @Schema(description = "宄板€肩綉缁滃甫瀹戒娇鐢?, example = "8.9")
        private BigDecimal peakNetworkUsage;
    }

    /**
     * 鐡堕鍒嗘瀽鍐呴儴绫?
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "鐡堕鍒嗘瀽")
    public static class BottleneckAnalysisVO {

        @Schema(description = "鐡堕绫诲瀷", example = "RESPONSE_TIME")
        private String bottleneckType;

        @Schema(description = "褰卞搷绋嬪害", example = "HIGH")
        private String impactLevel;

        @Schema(description = "鍑虹幇棰戠巼", example = "15.3%")
        private BigDecimal frequency;

        @Schema(description = "鐡堕鎻忚堪", example = "楂樺嘲鏈熷搷搴旀椂闂磋秴杩?00ms")
        private String description;

        @Schema(description = "寤鸿瑙ｅ喅鏂规", example = "澧炲姞缂撳瓨灞傛垨浼樺寲鏁版嵁搴撴煡璇?)
        private String solution;
    }

    /**
     * 浼樺寲寤鸿鍐呴儴绫?
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "浼樺寲寤鸿")
    public static class OptimizationRecommendationVO {

        @Schema(description = "寤鸿绫诲瀷", example = "PERFORMANCE")
        private String recommendationType;

        @Schema(description = "浼樺厛绾?, example = "HIGH")
        private String priority;

        @Schema(description = "棰勮鏀瑰杽骞呭害", example = "30%")
        private BigDecimal expectedImprovement;

        @Schema(description = "寤鸿鎻忚堪", example = "浼樺寲鏁版嵁搴撶储寮曞彲鍑忓皯鏌ヨ鏃堕棿")
        private String description;

        @Schema(description = "瀹炴柦闅惧害", example = "LOW")
        private String implementationDifficulty;
    }
}