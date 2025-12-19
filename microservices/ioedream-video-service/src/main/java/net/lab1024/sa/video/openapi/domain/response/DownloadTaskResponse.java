package net.lab1024.sa.video.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 下载任务响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "下载任务响应")
public class DownloadTaskResponse {

    @Schema(description = "任务ID", example = "DL001")
    private String taskId;

    @Schema(description = "录像ID", example = "REC001")
    private String recordingId;

    @Schema(description = "设备ID", example = "CAM001")
    private String deviceId;

    @Schema(description = "任务状态", example = "processing", allowableValues = {"pending", "processing", "completed", "failed"})
    private String status;

    @Schema(description = "进度百分比", example = "50")
    private Integer progress;

    @Schema(description = "文件大小(字节)", example = "1073741824")
    private Long fileSize;

    @Schema(description = "下载地址", example = "https://example.com/download/DL001.mp4")
    private String downloadUrl;

    @Schema(description = "创建时间", example = "2025-12-16T10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "完成时间", example = "2025-12-16T10:30:00")
    private LocalDateTime completeTime;

    @Schema(description = "过期时间", example = "2025-12-17T10:00:00")
    private LocalDateTime expireTime;

    @Schema(description = "错误消息", example = "")
    private String errorMessage;
}
