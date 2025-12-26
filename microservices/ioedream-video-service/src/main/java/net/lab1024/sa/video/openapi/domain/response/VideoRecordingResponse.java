package net.lab1024.sa.video.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 视频录像响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "视频录像响应")
public class VideoRecordingResponse {

    @Schema(description = "录像ID", example = "REC001")
    private String recordingId;

    @Schema(description = "设备ID", example = "CAM001")
    private String deviceId;

    @Schema(description = "设备名称", example = "前门摄像头")
    private String deviceName;

    @Schema(description = "开始时间", example = "2025-12-16T10:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2025-12-16T11:00:00")
    private LocalDateTime endTime;

    @Schema(description = "时长(秒)", example = "3600")
    private Long duration;

    @Schema(description = "文件大小(字节)", example = "1073741824")
    private Long fileSize;

    @Schema(description = "存储路径", example = "/recordings/2025/12/16/REC001.mp4")
    private String storagePath;

    @Schema(description = "录像状态", example = "completed", allowableValues = {"recording", "completed", "failed"})
    private String status;

    @Schema(description = "录像类型", example = "continuous", allowableValues = {"continuous", "event", "manual"})
    private String recordingType;

    @Schema(description = "扩展信息")
    private Map<String, Object> extendedInfo;
}

