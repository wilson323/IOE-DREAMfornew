package net.lab1024.sa.attendance.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.time.LocalDate;
import java.util.Map;

/**
 * 智能排班表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "智能排班表单")
public class SmartSchedulingForm {

    @NotNull(message = "部门ID不能为空")
    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    @NotNull(message = "开始日期不能为空")
    @Schema(description = "开始日期", example = "2025-01-01")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    @Schema(description = "结束日期", example = "2025-01-31")
    private LocalDate endDate;

    @Schema(description = "算法类型", example = "GENETIC", allowableValues = {"GENETIC", "SIMULATED_ANNEALING", "GREEDY"})
    private String algorithmType;

    @Schema(description = "约束条件", example = "{\"maxConsecutiveDays\": 6, \"minRestHours\": 12}")
    private Map<String, Object> constraints;

    @Min(value = 1, message = "优化迭代次数不能少于1次")
    @Max(value = 100, message = "优化迭代次数不能超过100次")
    @Schema(description = "优化迭代次数", example = "50")
    private Integer maxIterations;

    @Schema(description = "是否启用冲突检测", example = "true")
    private Boolean enableConflictDetection;

    @Schema(description = "是否自动应用排班", example = "false")
    private Boolean autoApply;

    @Schema(description = "创建人ID", example = "2001")
    private Long createUserId;
}