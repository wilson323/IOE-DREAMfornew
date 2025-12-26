package net.lab1024.sa.video.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 更新录像计划表单
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Data
@Schema(description = "更新录像计划表单")
public class VideoRecordingPlanUpdateForm {

    @NotNull(message = "计划ID不能为空")
    @Schema(description = "计划ID", example = "10001")
    private Long planId;

    @Schema(description = "计划名称", example = "主入口全天录像")
    private String planName;

    @Schema(description = "录像类型: 1-全天录像 2-定时录像 3-事件触发录像", example = "1")
    private Integer recordingType;

    @Schema(description = "录像质量: 1-低质量 2-中等质量 3-高质量 4-超清质量", example = "3")
    private Integer quality;

    @Schema(description = "开始时间", example = "2025-01-30 00:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2025-01-30 23:59:59")
    private LocalDateTime endTime;

    @Schema(description = "星期设置（1-7，逗号分隔）", example = "1,2,3,4,5")
    private String weekdays;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "优先级（1最高）", example = "1")
    private Integer priority;

    @Schema(description = "前置录像时长（秒）", example = "5")
    private Integer preRecordSeconds;

    @Schema(description = "后置录像时长（秒）", example = "10")
    private Integer postRecordSeconds;

    @Schema(description = "事件类型列表（事件录像用）", example = "[\"MOTION_DETECTED\", \"FACE_DETECTED\"]")
    private List<String> eventTypes;

    @Schema(description = "存储位置", example = "/recordings/cam001/")
    private String storageLocation;

    @Schema(description = "最大录像时长（分钟）", example = "480")
    private Integer maxDurationMinutes;

    @Schema(description = "循环录像", example = "true")
    private Boolean loopRecording;

    @Schema(description = "检测区域（JSON格式）", example = "{\"polygon\":[[100,200],[300,400],[500,600]]}")
    private String detectionArea;

    @Schema(description = "备注", example = "主入口全天录像计划")
    private String remarks;
}
