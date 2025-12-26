package net.lab1024.sa.video.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 录像回放响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "录像回放响应")
public class PlaybackResponse {

    @Schema(description = "回放会话ID", example = "PB001")
    private String sessionId;

    @Schema(description = "录像ID", example = "REC001")
    private String recordingId;

    @Schema(description = "设备ID", example = "CAM001")
    private String deviceId;

    @Schema(description = "播放地址", example = "https://example.com/playback/PB001.m3u8")
    private String playUrl;

    @Schema(description = "备用播放地址列表")
    private List<String> backupPlayUrls;

    @Schema(description = "播放协议", example = "hls")
    private String protocol;

    @Schema(description = "开始时间", example = "2025-12-16T10:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2025-12-16T11:00:00")
    private LocalDateTime endTime;

    @Schema(description = "当前播放位置(秒)", example = "0")
    private Long currentPosition;

    @Schema(description = "播放速度", example = "1.0")
    private Double playbackSpeed;

    @Schema(description = "会话状态", example = "active", allowableValues = {"active", "paused", "stopped"})
    private String sessionStatus;

    @Schema(description = "会话过期时间", example = "2025-12-16T12:00:00")
    private LocalDateTime expireTime;
}

