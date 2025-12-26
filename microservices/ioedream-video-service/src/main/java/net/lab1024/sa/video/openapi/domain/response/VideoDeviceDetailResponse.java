package net.lab1024.sa.video.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 视频设备详情响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "视频设备详情响应")
public class VideoDeviceDetailResponse extends VideoDeviceResponse {

    @Schema(description = "固件版本", example = "V5.6.5")
    private String firmwareVersion;

    @Schema(description = "序列号", example = "DS-2CD2T45G1-I20210101")
    private String serialNumber;

    @Schema(description = "MAC地址", example = "00:11:22:33:44:55")
    private String macAddress;

    @Schema(description = "分辨率", example = "1920x1080")
    private String resolution;

    @Schema(description = "帧率", example = "25")
    private Integer frameRate;

    @Schema(description = "码率(kbps)", example = "4000")
    private Integer bitrate;

    @Schema(description = "存储容量(GB)", example = "1000")
    private Long storageCapacity;

    @Schema(description = "已用存储(GB)", example = "500")
    private Long storageUsed;

    @Schema(description = "通道列表")
    private List<Channel> channels;

    @Schema(description = "安装位置", example = "入口大厅天花板")
    private String installLocation;

    @Schema(description = "安装时间", example = "2025-01-01T10:00:00")
    private LocalDateTime installTime;

    @Schema(description = "最后维护时间", example = "2025-12-01T10:00:00")
    private LocalDateTime lastMaintenanceTime;

    @Schema(description = "配置信息")
    private Map<String, Object> configuration;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "设备通道")
    public static class Channel {
        @Schema(description = "通道ID", example = "1")
        private Integer channelId;

        @Schema(description = "通道名称", example = "通道1")
        private String channelName;

        @Schema(description = "通道状态", example = "active")
        private String status;

        @Schema(description = "视频源类型", example = "ipc")
        private String sourceType;
    }
}
