package net.lab1024.sa.video.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 视频设备状态响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "视频设备状态响应")
public class VideoDeviceStatusResponse {

    @Schema(description = "设备ID", example = "CAM001")
    private String deviceId;

    @Schema(description = "设备名称", example = "前门摄像头")
    private String deviceName;

    @Schema(description = "在线状态", example = "online", allowableValues = {"online", "offline", "unknown"})
    private String status;

    @Schema(description = "CPU使用率(%)", example = "35")
    private Integer cpuUsage;

    @Schema(description = "内存使用率(%)", example = "45")
    private Integer memoryUsage;

    @Schema(description = "存储使用率(%)", example = "50")
    private Integer storageUsage;

    @Schema(description = "网络带宽(Mbps)", example = "10")
    private Double networkBandwidth;

    @Schema(description = "网络延迟(ms)", example = "5")
    private Integer networkLatency;

    @Schema(description = "设备温度(°C)", example = "45")
    private Integer temperature;

    @Schema(description = "运行时长(秒)", example = "86400")
    private Long uptime;

    @Schema(description = "视频流状态", example = "active", allowableValues = {"active", "inactive", "error"})
    private String streamStatus;

    @Schema(description = "录像状态", example = "recording", allowableValues = {"recording", "stopped", "error"})
    private String recordingStatus;

    @Schema(description = "AI状态", example = "running", allowableValues = {"running", "stopped", "error"})
    private String aiStatus;

    @Schema(description = "最后心跳时间", example = "2025-12-16T10:00:00")
    private LocalDateTime lastHeartbeat;

    @Schema(description = "告警信息")
    private Map<String, String> alarms;

    @Schema(description = "扩展状态信息")
    private Map<String, Object> extendedStatus;
}
