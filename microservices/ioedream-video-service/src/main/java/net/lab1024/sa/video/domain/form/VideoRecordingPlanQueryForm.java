package net.lab1024.sa.video.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.common.domain.form.PageForm;

import java.time.LocalDateTime;

/**
 * 查询录像计划表单
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Data
@Schema(description = "查询录像计划表单")
public class VideoRecordingPlanQueryForm extends PageForm {

    @Schema(description = "计划名称（模糊查询）", example = "主入口")
    private String planName;

    @Schema(description = "计划类型: 1-定时录像 2-事件录像 3-手动录像", example = "1")
    private Integer planType;

    @Schema(description = "设备ID", example = "CAM001")
    private String deviceId;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "录像类型: 1-全天录像 2-定时录像 3-事件触发录像", example = "1")
    private Integer recordingType;

    @Schema(description = "开始时间（起）", example = "2025-01-30 00:00:00")
    private LocalDateTime startTimeBegin;

    @Schema(description = "开始时间（止）", example = "2025-01-30 23:59:59")
    private LocalDateTime startTimeEnd;

    @Schema(description = "创建时间（起）", example = "2025-01-01 00:00:00")
    private LocalDateTime createTimeBegin;

    @Schema(description = "创建时间（止）", example = "2025-01-31 23:59:59")
    private LocalDateTime createTimeEnd;
}
