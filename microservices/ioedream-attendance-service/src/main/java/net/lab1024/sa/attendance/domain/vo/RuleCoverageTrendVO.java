package net.lab1024.sa.attendance.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 规则覆盖率趋势视图对象
 * <p>
 * 用于展示覆盖率的时间趋势数据
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
@Schema(description = "规则覆盖率趋势VO")
public class RuleCoverageTrendVO {

    /**
     * 日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "日期", example = "2025-12-26")
    private LocalDate date;

    /**
     * 覆盖率（百分比）
     */
    @Schema(description = "覆盖率（百分比）", example = "84.0")
    private Double coverageRate;

    /**
     * 已测试规则数量
     */
    @Schema(description = "已测试规则数量", example = "42")
    private Integer testedRules;

    /**
     * 总规则数量
     */
    @Schema(description = "总规则数量", example = "50")
    private Integer totalRules;

    /**
     * 测试总次数
     */
    @Schema(description = "测试总次数", example = "500")
    private Integer totalTests;

    /**
     * 测试成功率（百分比）
     */
    @Schema(description = "测试成功率（百分比）", example = "95.0")
    private Double successRate;
}
