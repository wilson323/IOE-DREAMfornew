package net.lab1024.sa.attendance.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 加班申请更新表单
 * <p>
 * 用于更新已有的加班申请（仅允许草稿状态）
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "加班申请更新表单")
public class AttendanceOvertimeApplyUpdateForm {

    @Schema(description = "加班类型", allowableValues = {"WORKDAY", "OVERTIME", "HOLIDAY"})
    @Pattern(regexp = "^(WORKDAY|OVERTIME|HOLIDAY)$", message = "加班类型必须是WORKDAY、OVERTIME或HOLIDAY")
    private String overtimeType;

    @Schema(description = "加班日期")
    private LocalDate overtimeDate;

    @Schema(description = "加班开始时间")
    private LocalTime startTime;

    @Schema(description = "加班结束时间")
    private LocalTime endTime;

    @Schema(description = "计划加班时长（小时）")
    @DecimalMin(value = "0.5", message = "加班时长不能小于0.5小时")
    @DecimalMax(value = "24", message = "加班时长不能超过24小时")
    private BigDecimal plannedHours;

    @Schema(description = "加班原因")
    @Size(min = 5, max = 500, message = "加班原因长度必须在5-500个字符之间")
    private String overtimeReason;

    @Schema(description = "加班详细说明")
    @Size(max = 2000, message = "加班详细说明不能超过2000个字符")
    private String overtimeDescription;

    @Schema(description = "补偿方式", allowableValues = {"PAY", "LEAVE"})
    @Pattern(regexp = "^(PAY|LEAVE)$", message = "补偿方式必须是PAY或LEAVE")
    private String compensationType;

    @Schema(description = "调休日期（补偿方式为调休时必填）")
    private LocalDate leaveDate;
}
