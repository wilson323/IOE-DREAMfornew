package net.lab1024.sa.attendance.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 规则覆盖率报告生成表单
 * <p>
 * 用于生成规则覆盖率报告的请求参数
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "规则覆盖率报告生成表单")
public class RuleCoverageReportForm {

    /**
     * 报告类型：DAILY-日报/WEEKLY-周报/MONTHLY-月报/CUSTOM-自定义
     */
    @NotBlank(message = "报告类型不能为空")
    @Schema(description = "报告类型", example = "DAILY", required = true)
    private String reportType;

    /**
     * 报告日期（用于日报）
     */
    @Schema(description = "报告日期（用于日报）", example = "2025-12-26")
    private LocalDate reportDate;

    /**
     * 开始日期（用于周报、月报、自定义）
     */
    @Schema(description = "开始日期", example = "2025-12-20")
    private LocalDate startDate;

    /**
     * 结束日期（用于周报、月报、自定义）
     */
    @Schema(description = "结束日期", example = "2025-12-26")
    private LocalDate endDate;

    /**
     * 是否包含详细数据
     */
    @Schema(description = "是否包含详细数据", example = "true")
    private Boolean includeDetails;

    /**
     * 低覆盖率阈值（测试次数低于此值视为低覆盖率）
     */
    @Schema(description = "低覆盖率阈值", example = "5")
    private Integer lowCoverageThreshold;
}
