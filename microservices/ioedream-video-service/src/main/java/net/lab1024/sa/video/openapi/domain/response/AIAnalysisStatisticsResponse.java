package net.lab1024.sa.video.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI分析统计响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI分析统计响应")
public class AIAnalysisStatisticsResponse {

    @Schema(description = "统计周期开始时间", example = "2025-12-16T00:00:00")
    private LocalDateTime periodStart;

    @Schema(description = "统计周期结束时间", example = "2025-12-16T23:59:59")
    private LocalDateTime periodEnd;

    @Schema(description = "分析请求总数", example = "10000")
    private Long totalAnalysisRequests;

    @Schema(description = "成功分析数", example = "9800")
    private Long successCount;

    @Schema(description = "失败分析数", example = "200")
    private Long failureCount;

    @Schema(description = "成功率(%)", example = "98")
    private Double successRate;

    @Schema(description = "平均处理时间(毫秒)", example = "500")
    private Double avgProcessingTime;

    @Schema(description = "按类型统计")
    private List<TypeStatistics> typeStatistics;

    @Schema(description = "按设备统计")
    private List<DeviceStatistics> deviceStatistics;

    @Schema(description = "按小时分布")
    private Map<Integer, Long> hourlyDistribution;

    @Schema(description = "热门告警类型")
    private List<AlarmTypeStatistics> topAlarmTypes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "类型统计")
    public static class TypeStatistics {
        @Schema(description = "分析类型", example = "face_detection")
        private String analysisType;

        @Schema(description = "分析类型名称", example = "人脸检测")
        private String typeName;

        @Schema(description = "请求数", example = "5000")
        private Long requestCount;

        @Schema(description = "成功数", example = "4900")
        private Long successCount;

        @Schema(description = "平均处理时间(毫秒)", example = "300")
        private Double avgProcessingTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "设备统计")
    public static class DeviceStatistics {
        @Schema(description = "设备ID", example = "CAM001")
        private String deviceId;

        @Schema(description = "设备名称", example = "前门摄像头")
        private String deviceName;

        @Schema(description = "请求数", example = "1000")
        private Long requestCount;

        @Schema(description = "告警数", example = "50")
        private Long alarmCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "告警类型统计")
    public static class AlarmTypeStatistics {
        @Schema(description = "告警类型", example = "intrusion")
        private String alarmType;

        @Schema(description = "告警类型名称", example = "入侵检测")
        private String typeName;

        @Schema(description = "告警数", example = "100")
        private Long alarmCount;

        @Schema(description = "占比(%)", example = "20")
        private Double percentage;
    }
}
