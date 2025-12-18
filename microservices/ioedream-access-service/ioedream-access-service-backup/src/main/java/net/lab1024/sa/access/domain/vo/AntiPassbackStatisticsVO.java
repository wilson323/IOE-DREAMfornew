package net.lab1024.sa.access.domain.vo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 闂ㄧ鍙嶆綔鍥炵粺璁O
 * <p>
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛?
 * - 浣跨敤VO鍚庣紑鏍囪瘑瑙嗗浘瀵硅薄
 * - 浣跨敤@Data娉ㄨВ绠€鍖杇etter/setter
 * - 鍖呭惈Swagger娉ㄨВ渚夸簬API鏂囨。鐢熸垚
 * - 鎻愪緵璇︾粏鐨勫弽娼滃洖缁熻鏁版嵁
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
@Schema(description = "闂ㄧ鍙嶆綔鍥炵粺璁O")
public class AntiPassbackStatisticsVO {

    /**
     * 缁熻鏃堕棿
     */
    @Schema(description = "缁熻鏃堕棿", example = "2025-01-30T10:30:00")
    private LocalDateTime statisticsTime;

    /**
     * 鎬绘鏌ユ鏁?
     */
    @Schema(description = "鎬绘鏌ユ鏁?, example = "1500")
    private Long totalCheckCount;

    /**
     * 鎴愬姛閫氳繃娆℃暟
     */
    @Schema(description = "鎴愬姛閫氳繃娆℃暟", example = "1200")
    private Long successCount;

    /**
     * 澶辫触娆℃暟
     */
    @Schema(description = "澶辫触娆℃暟", example = "300")
    private Long failureCount;

    /**
     * 鎴愬姛鐜?
     */
    @Schema(description = "鎴愬姛鐜?, example = "80.0")
    private Double successRate;

    /**
     * 纭弽娼滃洖杩濊娆℃暟
     */
    @Schema(description = "纭弽娼滃洖杩濊娆℃暟", example = "50")
    private Long hardAntiPassbackViolations;

    /**
     * 杞弽娼滃洖寮傚父娆℃暟
     */
    @Schema(description = "杞弽娼滃洖寮傚父娆℃暟", example = "100")
    private Long softAntiPassbackExceptions;

    /**
     * 鍖哄煙鍙嶆綔鍥炶繚瑙勬鏁?
     */
    @Schema(description = "鍖哄煙鍙嶆綔鍥炶繚瑙勬鏁?, example = "30")
    private Long areaAntiPassbackViolations;

    /**
     * 鍏ㄥ眬鍙嶆綔鍥炶繚瑙勬鏁?
     */
    @Schema(description = "鍏ㄥ眬鍙嶆綔鍥炶繚瑙勬鏁?, example = "20")
    private Long globalAntiPassbackViolations;

    /**
     * 鍚勫尯鍩熺粺璁?
     */
    @Schema(description = "鍚勫尯鍩熺粺璁?)
    private List<AreaStatistics> areaStatisticsList;

    /**
     * 鍚勮澶囩粺璁?
     */
    @Schema(description = "鍚勮澶囩粺璁?)
    private List<DeviceStatistics> deviceStatisticsList;

    /**
     * 鏃堕棿鍒嗗竷缁熻
     */
    @Schema(description = "鏃堕棿鍒嗗竷缁熻")
    private Map<String, Long> timeDistribution;

    /**
     * 鍖哄煙缁熻鍐呴儴绫?
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AreaStatistics {

        @Schema(description = "鍖哄煙ID")
        private Long areaId;

        @Schema(description = "鍖哄煙鍚嶇О")
        private String areaName;

        @Schema(description = "妫€鏌ユ鏁?)
        private Long checkCount;

        @Schema(description = "杩濊娆℃暟")
        private Long violationCount;

        @Schema(description = "杩濊鐜?)
        private Double violationRate;
    }

    /**
     * 璁惧缁熻鍐呴儴绫?
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceStatistics {

        @Schema(description = "璁惧ID")
        private Long deviceId;

        @Schema(description = "璁惧鍚嶇О")
        private String deviceName;

        @Schema(description = "璁惧浣嶇疆")
        private String deviceLocation;

        @Schema(description = "妫€鏌ユ鏁?)
        private Long checkCount;

        @Schema(description = "杩濊娆℃暟")
        private Long violationCount;

        @Schema(description = "璁惧鐘舵€?)
        private String deviceStatus;
    }
}