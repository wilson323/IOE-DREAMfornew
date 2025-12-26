package net.lab1024.sa.common.entity.attendance;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 规则覆盖率报告实体
 * <p>
 * 存储规则测试覆盖率统计结果
 * 支持按日期维度的覆盖率追踪
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
@TableName("t_attendance_rule_coverage_report")
@Schema(description = "规则覆盖率报告实体")
public class RuleCoverageReportEntity {

    /**
     * 报告ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "报告ID")
    private Long reportId;

    /**
     * 报告日期（用于趋势分析）
     */
    @TableField("report_date")
    @Schema(description = "报告日期")
    private LocalDate reportDate;

    /**
     * 总规则数量
     */
    @TableField("total_rules")
    @Schema(description = "总规则数量")
    private Integer totalRules;

    /**
     * 已测试规则数量
     */
    @TableField("tested_rules")
    @Schema(description = "已测试规则数量")
    private Integer testedRules;

    /**
     * 覆盖率（百分比）
     */
    @TableField("coverage_rate")
    @Schema(description = "覆盖率（百分比）")
    private Double coverageRate;

    /**
     * 测试总次数
     */
    @TableField("total_tests")
    @Schema(description = "测试总次数")
    private Integer totalTests;

    /**
     * 测试成功次数
     */
    @TableField("success_tests")
    @Schema(description = "测试成功次数")
    private Integer successTests;

    /**
     * 测试失败次数
     */
    @TableField("failed_tests")
    @Schema(description = "测试失败次数")
    private Integer failedTests;

    /**
     * 测试成功率（百分比）
     */
    @TableField("success_rate")
    @Schema(description = "测试成功率（百分比）")
    private Double successRate;

    /**
     * 覆盖率详情（JSON格式）
     * 存储每个规则的覆盖情况：规则ID、测试次数、成功次数、失败次数
     */
    @TableField("coverage_details")
    @Schema(description = "覆盖率详情（JSON）")
    private String coverageDetails;

    /**
     * 未覆盖规则列表（JSON格式）
     * 存储从未测试过的规则ID列表
     */
    @TableField("uncovered_rules")
    @Schema(description = "未覆盖规则列表（JSON）")
    private String uncoveredRules;

    /**
     * 低覆盖率规则列表（JSON格式）
     * 存储测试次数<5次的规则ID列表
     */
    @TableField("low_coverage_rules")
    @Schema(description = "低覆盖率规则列表（JSON）")
    private String lowCoverageRules;

    /**
     * 报告类型：DAILY-日报/WEEKLY-周报/MONTHLY-月报/CUSTOM-自定义
     */
    @TableField("report_type")
    @Schema(description = "报告类型")
    private String reportType;

    /**
     * 开始日期（用于周报、月报）
     */
    @TableField("start_date")
    @Schema(description = "开始日期")
    private LocalDate startDate;

    /**
     * 结束日期（用于周报、月报）
     */
    @TableField("end_date")
    @Schema(description = "结束日期")
    private LocalDate endDate;

    /**
     * 报告状态：GENERATING-生成中/COMPLETED-已完成/ERROR-错误
     */
    @TableField("report_status")
    @Schema(description = "报告状态")
    private String reportStatus;

    /**
     * 生成耗时（毫秒）
     */
    @TableField("generation_time_ms")
    @Schema(description = "生成耗时（毫秒）")
    private Long generationTimeMs;

    /**
     * 错误信息
     */
    @TableField("error_message")
    @Schema(description = "错误信息")
    private String errorMessage;

    /**
     * 审计字段
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted_flag")
    @Schema(description = "删除标记")
    private Integer deletedFlag;
}
