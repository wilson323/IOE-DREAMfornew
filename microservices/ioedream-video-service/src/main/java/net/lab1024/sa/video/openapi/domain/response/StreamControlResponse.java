package net.lab1024.sa.video.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 视频流控制响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "视频流控制响应")
public class StreamControlResponse {

    @Schema(description = "设备ID", example = "CAM001")
    private String deviceId;

    @Schema(description = "流ID", example = "stream_001")
    private String streamId;

    @Schema(description = "操作类型", example = "start", allowableValues = {"start", "stop", "pause", "resume"})
    private String action;

    @Schema(description = "操作结果", example = "true")
    private Boolean success;

    @Schema(description = "结果消息", example = "流已启动")
    private String message;

    @Schema(description = "播放地址", example = "https://example.com/live/stream_001.m3u8")
    private String playUrl;

    @Schema(description = "操作时间", example = "2025-12-16T10:00:00")
    private LocalDateTime operationTime;
}

