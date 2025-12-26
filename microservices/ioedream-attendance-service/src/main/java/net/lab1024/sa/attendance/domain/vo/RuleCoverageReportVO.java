package net.lab1024.sa.attendance.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 规则覆盖率报告视图对象
 * <p>
 * 用于展示规则覆盖率统计结果
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
@Schema(description = "规则覆盖率报告VO")
public class RuleCoverageReportVO {

    /**
     * 报告ID
     */
    @Schema(description = "报告ID")
    private Long reportId;

    /**
     * 报告日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "报告日期", example = "2025-12-26")
    private LocalDate reportDate;

    /**
     * 总规则数量
     */
    @Schema(description = "总规则数量", example = "50")
    private Integer totalRules;

    /**
     * 已测试规则数量
     */
    @Schema(description = "已测试规则数量", example = "42")
    private Integer testedRules;

    /**
     * 未测试规则数量
     */
    @Schema(description = "未测试规则数量", example = "8")
    private Integer untestedRules;

    /**
     * 覆盖率（百分比）
     */
    @Schema(description = "覆盖率（百分比）", example = "84.0")
    private Double coverageRate;

    /**
     * 测试总次数
     */
    @Schema(description = "测试总次数", example = "500")
    private Integer totalTests;

    /**
     * 测试成功次数
     */
    @Schema(description = "测试成功次数", example = "475")
    private Integer successTests;

    /**
     * 测试失败次数
     */
    @Schema(description = "测试失败次数", example = "25")
    private Integer failedTests;

    /**
     * 测试成功率（百分比）
     */
    @Schema(description = "测试成功率（百分比）", example = "95.0")
    private Double successRate;

    /**
     * 报告类型
     */
    @Schema(description = "报告类型", example = "DAILY")
    private String reportType;

    /**
     * 开始日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "开始日期", example = "2025-12-26")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "结束日期", example = "2025-12-26")
    private LocalDate endDate;

    /**
     * 报告状态
     */
    @Schema(description = "报告状态", example = "COMPLETED")
    private String reportStatus;

    /**
     * 生成耗时（毫秒）
     */
    @Schema(description = "生成耗时（毫秒）", example = "2500")
    private Long generationTimeMs;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间", example = "2025-12-26 10:30:00")
    private LocalDateTime createTime;
}
