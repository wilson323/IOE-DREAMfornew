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
 * 实时视频流响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "实时视频流响应")
public class LiveStreamResponse {

    @Schema(description = "设备ID", example = "CAM001")
    private String deviceId;

    @Schema(description = "设备名称", example = "前门摄像头")
    private String deviceName;

    @Schema(description = "流ID", example = "stream_001")
    private String streamId;

    @Schema(description = "流类型", example = "main")
    private String streamType;

    @Schema(description = "播放协议", example = "hls")
    private String protocol;

    @Schema(description = "播放地址", example = "https://example.com/live/stream_001.m3u8")
    private String playUrl;

    @Schema(description = "备用播放地址列表")
    private List<String> backupPlayUrls;

    @Schema(description = "视频编码", example = "h264")
    private String videoCodec;

    @Schema(description = "音频编码", example = "aac")
    private String audioCodec;

    @Schema(description = "视频分辨率", example = "1920x1080")
    private String resolution;

    @Schema(description = "帧率", example = "25")
    private Integer frameRate;

    @Schema(description = "码率(kbps)", example = "2000")
    private Integer bitrate;

    @Schema(description = "流状态", example = "active", allowableValues = {"active", "inactive", "error"})
    private String streamStatus;

    @Schema(description = "流状态名称", example = "活跃")
    private String streamStatusName;

    @Schema(description = "开始时间", example = "2025-12-16T10:00:00")
    private LocalDateTime startTime;

    @Schema(description = "延迟(毫秒)", example = "2000")
    private Long latency;

    @Schema(description = "在线观看人数", example = "5")
    private Integer viewerCount;

    @Schema(description = "是否支持录制", example = "true")
    private Boolean supportRecording;

    @Schema(description = "是否支持云台控制", example = "true")
    private Boolean supportPTZ;

    @Schema(description = "扩展信息")
    private Map<String, Object> extendedInfo;
}
