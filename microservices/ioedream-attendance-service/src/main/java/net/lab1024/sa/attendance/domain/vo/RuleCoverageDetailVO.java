package net.lab1024.sa.attendance.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 规则覆盖详情视图对象
 * <p>
 * 展示单个规则的覆盖情况
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
@Schema(description = "规则覆盖详情VO")
public class RuleCoverageDetailVO {

    /**
     * 规则ID
     */
    @Schema(description = "规则ID", example = "1")
    private Long ruleId;

    /**
     * 规则名称
     */
    @Schema(description = "规则名称", example = "早高峰考勤规则")
    private String ruleName;

    /**
     * 规则类型
     */
    @Schema(description = "规则类型", example = "SHIFT")
    private String ruleType;

    /**
     * 测试次数
     */
    @Schema(description = "测试次数", example = "25")
    private Integer testCount;

    /**
     * 成功次数
     */
    @Schema(description = "成功次数", example = "24")
    private Integer successCount;

    /**
     * 失败次数
     */
    @Schema(description = "失败次数", example = "1")
    private Integer failedCount;

    /**
     * 成功率（百分比）
     */
    @Schema(description = "成功率（百分比）", example = "96.0")
    private Double successRate;

    /**
     * 最后测试时间
     */
    @Schema(description = "最后测试时间", example = "2025-12-26 10:30:00")
    private String lastTestTime;

    /**
     * 覆盖状态：COVERED-已覆盖/UNCOVERED-未覆盖/LOW_COVERAGE-低覆盖率
     */
    @Schema(description = "覆盖状态", example = "COVERED")
    private String coverageStatus;

    /**
     * 覆盖率等级：HIGH-高(≥10次)/MEDIUM-中(5-9次)/LOW-低(1-4次)/NONE-无(0次)
     */
    @Schema(description = "覆盖率等级", example = "HIGH")
    private String coverageLevel;
}
