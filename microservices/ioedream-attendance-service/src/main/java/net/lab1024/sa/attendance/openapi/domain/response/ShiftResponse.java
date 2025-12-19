package net.lab1024.sa.attendance.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 班次信息响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "班次信息响应")
public class ShiftResponse {

    @Schema(description = "班次ID", example = "1")
    private Long shiftId;

    @Schema(description = "班次名称", example = "标准班")
    private String shiftName;

    @Schema(description = "班次类型", example = "normal", allowableValues = {"normal", "flexible", "night", "overtime"})
    private String shiftType;

    @Schema(description = "上班时间", example = "09:00")
    private String workStartTime;

    @Schema(description = "下班时间", example = "18:00")
    private String workEndTime;

    @Schema(description = "午休开始时间", example = "12:00")
    private String breakStartTime;

    @Schema(description = "午休结束时间", example = "13:00")
    private String breakEndTime;

    @Schema(description = "工作时长（分钟）", example = "480")
    private Integer workMinutes;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;
}

