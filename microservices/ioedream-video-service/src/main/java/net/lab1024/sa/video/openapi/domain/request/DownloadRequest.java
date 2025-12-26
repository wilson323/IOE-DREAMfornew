package net.lab1024.sa.video.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 录像下载请求
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "录像下载请求")
public class DownloadRequest {

    @NotBlank(message = "设备ID不能为空")
    @Schema(description = "设备ID", example = "CAM001", required = true)
    private String deviceId;

    @Schema(description = "录像ID", example = "REC001")
    private String recordingId;

    @Schema(description = "开始时间", example = "2025-12-16T10:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2025-12-16T11:00:00")
    private LocalDateTime endTime;

    @Schema(description = "下载格式", example = "mp4", allowableValues = {"mp4", "avi", "mkv"})
    private String format;

    @Schema(description = "视频质量", example = "high", allowableValues = {"original", "high", "medium", "low"})
    private String quality;

    @Schema(description = "是否包含音频", example = "true")
    private Boolean includeAudio;

    @Schema(description = "回调URL", example = "https://example.com/callback")
    private String callbackUrl;
}
