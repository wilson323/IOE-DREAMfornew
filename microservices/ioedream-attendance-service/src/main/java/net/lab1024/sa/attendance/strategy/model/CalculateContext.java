package net.lab1024.sa.attendance.strategy.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import net.lab1024.sa.common.entity.attendance.WorkShiftEntity;

/**
 * 计算上下文
 * <p>
 * 封装工时计算所需的所有上下文信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "工时计算上下文")
public class CalculateContext {

    @Schema(description = "员工ID", example = "1001")
    private Long employeeId;

    @Schema(description = "班次配置")
    private WorkShiftEntity workShift;

    @Schema(description = "考勤日期", example = "2025-01-30")
    private LocalDate attendanceDate;

    @Schema(description = "打卡记录列表")
    private List<PunchRecord> punchRecords;

    @Schema(description = "排班ID", example = "10001")
    private Long scheduleId;

    @Schema(description = "部门ID", example = "2001")
    private Long departmentId;

    @Schema(description = "是否启用灵活计算", example = "true")
    private Boolean flexibleCalculation;

    @Schema(description = "是否忽略休息时间", example = "false")
    private Boolean ignoreBreakTime;

    @Schema(description = "是否自动补卡", example = "false")
    private Boolean autoFillMissedPunch;

    @Schema(description = "最小工作时长(分钟)", example = "30")
    private Integer minWorkMinutes;

    @Schema(description = "最大工作时长(分钟)", example = "720")
    private Integer maxWorkMinutes;

    @Schema(description = "加班计算开始时间(分钟)", example = "0")
    private Integer overtimeStartMinutes;

    @Schema(description = "最小加班时长(分钟)", example = "30")
    private Integer minOvertimeMinutes;

    @Schema(description = "是否启用跨天计算", example = "true")
    private Boolean enableOvernightCalculation;

    @Schema(description = "扩展属性(JSON格式)")
    private String extendedAttributes;
}
