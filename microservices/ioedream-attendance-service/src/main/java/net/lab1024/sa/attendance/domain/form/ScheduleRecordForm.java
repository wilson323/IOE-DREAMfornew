package net.lab1024.sa.attendance.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * 排班记录表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "排班记录表单")
public class ScheduleRecordForm {

    @Schema(description = "排班记录ID", example = "1001")
    private Long scheduleId;

    @NotNull(message = "员工ID不能为空")
    @Schema(description = "员工ID", example = "1001")
    private Long employeeId;

    @NotNull(message = "排班日期不能为空")
    @Schema(description = "排班日期", example = "2025-01-30")
    private LocalDate scheduleDate;

    @NotNull(message = "班次ID不能为空")
    @Schema(description = "班次ID", example = "101")
    private Long shiftId;

    @Size(max = 50)
    @Schema(description = "排班类型", example = "正常排班")
    private String scheduleType;

    @Schema(description = "是否临时排班", example = "false")
    private Boolean isTemporary;

    @Size(max = 500)
    @Schema(description = "排班原因", example = "项目需要")
    private String reason;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "优先级", example = "1")
    private Integer priority;

    @Schema(description = "创建人ID", example = "2001")
    private Long createUserId;
}
