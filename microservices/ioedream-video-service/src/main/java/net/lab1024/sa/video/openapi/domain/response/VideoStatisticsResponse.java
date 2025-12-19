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
 * 视频统计响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "视频统计响应")
public class VideoStatisticsResponse {

    @Schema(description = "统计周期开始时间", example = "2025-12-16T00:00:00")
    private LocalDateTime periodStart;

    @Schema(description = "统计周期结束时间", example = "2025-12-16T23:59:59")
    private LocalDateTime periodEnd;

    @Schema(description = "设备统计")
    private DeviceStatistics deviceStats;

    @Schema(description = "流统计")
    private StreamStatistics streamStats;

    @Schema(description = "录像统计")
    private RecordingStatistics recordingStats;

    @Schema(description = "存储统计")
    private StorageStatistics storageStats;

    @Schema(description = "区域统计列表")
    private List<AreaStatistics> areaStats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "设备统计")
    public static class DeviceStatistics {
        @Schema(description = "设备总数", example = "100")
        private Integer totalDevices;

        @Schema(description = "在线设备数", example = "95")
        private Integer onlineDevices;

        @Schema(description = "离线设备数", example = "5")
        private Integer offlineDevices;

        @Schema(description = "设备在线率(%)", example = "95")
        private Double onlineRate;

        @Schema(description = "按类型设备数")
        private Map<String, Integer> devicesByType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "流统计")
    public static class StreamStatistics {
        @Schema(description = "活跃流数", example = "50")
        private Integer activeStreams;

        @Schema(description = "流请求总数", example = "1000")
        private Long totalStreamRequests;

        @Schema(description = "平均流时长(分钟)", example = "15")
        private Double avgStreamDuration;

        @Schema(description = "峰值并发数", example = "80")
        private Integer peakConcurrent;

        @Schema(description = "总带宽(Mbps)", example = "500")
        private Double totalBandwidth;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "录像统计")
    public static class RecordingStatistics {
        @Schema(description = "录像总数", example = "5000")
        private Long totalRecordings;

        @Schema(description = "录像总时长(小时)", example = "10000")
        private Long totalDurationHours;

        @Schema(description = "回放请求总数", example = "500")
        private Long totalPlaybackRequests;

        @Schema(description = "下载请求总数", example = "100")
        private Long totalDownloadRequests;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "存储统计")
    public static class StorageStatistics {
        @Schema(description = "总容量(TB)", example = "100")
        private Double totalCapacity;

        @Schema(description = "已用容量(TB)", example = "60")
        private Double usedCapacity;

        @Schema(description = "可用容量(TB)", example = "40")
        private Double availableCapacity;

        @Schema(description = "使用率(%)", example = "60")
        private Double usageRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "区域统计")
    public static class AreaStatistics {
        @Schema(description = "区域ID", example = "AREA001")
        private String areaId;

        @Schema(description = "区域名称", example = "园区入口")
        private String areaName;

        @Schema(description = "设备数", example = "10")
        private Integer deviceCount;

        @Schema(description = "在线设备数", example = "9")
        private Integer onlineCount;

        @Schema(description = "告警数", example = "2")
        private Integer alarmCount;
    }
}
