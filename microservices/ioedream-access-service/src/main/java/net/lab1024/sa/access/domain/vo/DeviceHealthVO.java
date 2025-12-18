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
 * 璁惧鍋ュ悍鐘舵€佽鍥惧璞?
 * <p>
 * 璁惧鍋ュ悍鐩戞帶缁撴灉鐨勬暟鎹紶杈撳璞?
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
@Schema(description = "璁惧鍋ュ悍鐘舵€佷俊鎭?)
public class DeviceHealthVO {

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
     * 璁惧缂栫爜
     */
    @Schema(description = "璁惧缂栫爜", example = "ACC-CTRL-001")
    private String deviceCode;

    /**
     * 璁惧绫诲瀷
     */
    @Schema(description = "璁惧绫诲瀷", example = "ACCESS_CONTROLLER")
    private String deviceType;

    /**
     * 鍋ュ悍璇勫垎锛?-100锛?
     */
    @Schema(description = "鍋ュ悍璇勫垎锛?-100锛?, example = "92.5")
    private BigDecimal healthScore;

    /**
     * 鍋ュ悍鐘舵€?
     * HEALTHY - 鍋ュ悍
     * WARNING - 浜氬仴搴?
     * CRITICAL - 鍗遍櫓
     * OFFLINE - 绂荤嚎
     */
    @Schema(description = "鍋ュ悍鐘舵€?, example = "HEALTHY")
    private String healthStatus;

    /**
     * 鍦ㄧ嚎鐘舵€?
     */
    @Schema(description = "鍦ㄧ嚎鐘舵€?, example = "true")
    private Boolean onlineStatus;

    /**
     * 鍝嶅簲鏃堕棿锛堟绉掞級
     */
    @Schema(description = "鍝嶅簲鏃堕棿锛堟绉掞級", example = "156")
    private Long responseTime;

    /**
     * CPU浣跨敤鐜囷紙%锛?
     */
    @Schema(description = "CPU浣跨敤鐜囷紙%锛?, example = "35.6")
    private BigDecimal cpuUsage;

    /**
     * 鍐呭瓨浣跨敤鐜囷紙%锛?
     */
    @Schema(description = "鍐呭瓨浣跨敤鐜囷紙%锛?, example = "42.3")
    private BigDecimal memoryUsage;

    /**
     * 纾佺洏浣跨敤鐜囷紙%锛?
     */
    @Schema(description = "纾佺洏浣跨敤鐜囷紙%锛?, example = "28.7")
    private BigDecimal diskUsage;

    /**
     * 缃戠粶杩炴帴璐ㄩ噺
     */
    @Schema(description = "缃戠粶杩炴帴璐ㄩ噺", example = "EXCELLENT")
    private String networkQuality;

    /**
     * 24灏忔椂鍐呴敊璇鏁?
     */
    @Schema(description = "24灏忔椂鍐呴敊璇鏁?, example = "2")
    private Integer errorCount24h;

    /**
     * 24灏忔椂鍐呮垚鍔熺巼锛?锛?
     */
    @Schema(description = "24灏忔椂鍐呮垚鍔熺巼锛?锛?, example = "99.8")
    private BigDecimal successRate24h;

    /**
     * 鏈€鍚庡湪绾挎椂闂?
     */
    @Schema(description = "鏈€鍚庡湪绾挎椂闂?, example = "2025-01-30T10:30:00")
    private LocalDateTime lastOnlineTime;

    /**
     * 杩炵画杩愯鏃堕暱锛堝皬鏃讹級
     */
    @Schema(description = "杩炵画杩愯鏃堕暱锛堝皬鏃讹級", example = "720")
    private Long uptimeHours;

    /**
     * 寮傚父鎸囨爣鍒楄〃
     */
    @Schema(description = "寮傚父鎸囨爣鍒楄〃")
    private List<AnomalousMetricVO> anomalousMetrics;

    /**
     * 鎬ц兘鎸囨爣璇︽儏
     */
    @Schema(description = "鎬ц兘鎸囨爣璇︽儏")
    private Map<String, Object> performanceMetrics;

    /**
     * 鍋ュ悍瓒嬪娍锛堟渶杩?澶╋級
     */
    @Schema(description = "鍋ュ悍瓒嬪娍锛堟渶杩?澶╋級")
    private List<HealthTrendVO> healthTrends;

    /**
     * 寤鸿鎿嶄綔
     */
    @Schema(description = "寤鸿鎿嶄綔", example = "寤鸿鍦ㄤ笅娆＄淮鎶ゆ椂妫€鏌ョ綉缁滆繛鎺ョǔ瀹氭€?)
    private String recommendation;

    /**
     * 鐩戞帶鏃堕棿
     */
    @Schema(description = "鐩戞帶鏃堕棿", example = "2025-01-30T15:45:00")
    private LocalDateTime monitorTime;

    /**
     * 寮傚父鎸囨爣鍐呴儴绫?
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "寮傚父鎸囨爣")
    public static class AnomalousMetricVO {

        @Schema(description = "鎸囨爣鍚嶇О", example = "CPU浣跨敤鐜?)
        private String metricName;

        @Schema(description = "褰撳墠鍊?, example = "85.6")
        private BigDecimal currentValue;

        @Schema(description = "姝ｅ父鑼冨洿", example = "0-70")
        private String normalRange;

        @Schema(description = "寮傚父绾у埆", example = "WARNING")
        private String anomalyLevel;

        @Schema(description = "寮傚父鎻忚堪", example = "CPU浣跨敤鐜囪秴杩囨甯搁槇鍊?)
        private String description;
    }

    /**
     * 鍋ュ悍瓒嬪娍鍐呴儴绫?
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "鍋ュ悍瓒嬪娍")
    public static class HealthTrendVO {

        @Schema(description = "鏃ユ湡", example = "2025-01-30")
        private String date;

        @Schema(description = "鍋ュ悍璇勫垎", example = "92.5")
        private BigDecimal healthScore;

        @Schema(description = "鐘舵€?, example = "HEALTHY")
        private String status;
    }
}