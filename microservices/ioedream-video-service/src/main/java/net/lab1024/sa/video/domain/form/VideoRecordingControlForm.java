package net.lab1024.sa.video.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 手动录像控制表单
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Data
@Schema(description = "手动录像控制表单")
public class VideoRecordingControlForm {

    @NotBlank(message = "设备ID不能为空")
    @Schema(description = "设备ID", example = "CAM001")
    private String deviceId;

    @Schema(description = "通道ID", example = "1")
    private Integer channelId;

    @NotNull(message = "操作类型不能为空")
    @Schema(description = "操作类型: 1-开始录像 2-停止录像", example = "1")
    private Integer operationType;

    @Schema(description = "录像质量: 1-低质量 2-中等质量 3-高质量 4-超清质量", example = "3")
    private Integer quality;

    @Schema(description = "最大录像时长（分钟）", example = "120")
    private Integer maxDurationMinutes;

    @Schema(description = "存储位置", example = "/recordings/manual/cam001/")
    private String storageLocation;

    @Schema(description = "录像原因（手动录像备注）", example = "特殊情况录像")
    private String recordingReason;

    @Schema(description = "任务ID（停止录像时必填）", example = "20001")
    private Long taskId;
}
