package net.lab1024.sa.attendance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.lab1024.sa.attendance.domain.form.RuleCoverageReportForm;
import net.lab1024.sa.attendance.domain.vo.RuleCoverageReportVO;
import net.lab1024.sa.attendance.domain.vo.RuleCoverageTrendVO;
import net.lab1024.sa.attendance.domain.vo.RuleCoverageDetailVO;
import net.lab1024.sa.attendance.service.RuleCoverageReportService;
import net.lab1024.sa.common.dto.ResponseDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * 规则覆盖率报告控制器
 * <p>
 * 提供规则覆盖率统计和报告相关API接口
 * 支持覆盖率分析、趋势查询、详情查看等功能
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/attendance/rule-coverage-report")
@Tag(name = "规则覆盖率报告管理")
public class AttendanceRuleCoverageReportController {

    @Resource
    private RuleCoverageReportService ruleCoverageReportService;

    /**
     * 生成覆盖率报告
     *
     * @param form 报告参数
     * @return 报告结果
     */
    @Observed(name = "ruleCoverageReport.generate", contextualName = "generate-coverage-report")
    @PostMapping("/generate")
    @Operation(summary = "生成覆盖率报告", description = "生成规则覆盖率统计报告")
    public ResponseDTO<RuleCoverageReportVO> generateCoverageReport(
            @Valid @RequestBody RuleCoverageReportForm form) {
        log.info("[覆盖率报告] 生成覆盖率报告: type={}, startDate={}, endDate={}",
                form.getReportType(), form.getStartDate(), form.getEndDate());

        RuleCoverageReportVO result = ruleCoverageReportService.generateCoverageReport(form);

        log.info("[覆盖率报告] 报告生成成功: reportId={}, coverageRate={}%, totalRules={}, testedRules={}",
                result.getReportId(), result.getCoverageRate(),
                result.getTotalRules(), result.getTestedRules());
        return ResponseDTO.ok(result);
    }

    /**
     * 查询报告结果
     *
     * @param reportId 报告ID
     * @return 报告结果
     */
    @Observed(name = "ruleCoverageReport.getReport", contextualName = "get-coverage-report")
    @GetMapping("/{reportId}")
    @Operation(summary = "查询报告结果", description = "根据报告ID查询覆盖率报告")
    public ResponseDTO<RuleCoverageReportVO> getReport(
            @PathVariable @Parameter(description = "报告ID", required = true) Long reportId) {
        log.info("[覆盖率报告] 查询报告: reportId={}", reportId);

        RuleCoverageReportVO result = ruleCoverageReportService.getReport(reportId);

        if (result == null) {
            log.warn("[覆盖率报告] 报告不存在: reportId={}", reportId);
            return ResponseDTO.error("REPORT_NOT_FOUND", "报告不存在");
        }

        log.info("[覆盖率报告] 查询报告成功: reportId={}, coverageRate={}%", reportId, result.getCoverageRate());
        return ResponseDTO.ok(result);
    }

    /**
     * 查询指定日期的报告
     *
     * @param reportDate 报告日期
     * @return 报告结果
     */
    @Observed(name = "ruleCoverageReport.getReportByDate", contextualName = "get-report-by-date")
    @GetMapping("/date/{reportDate}")
    @Operation(summary = "查询指定日期的报告", description = "根据日期查询覆盖率报告")
    public ResponseDTO<RuleCoverageReportVO> getReportByDate(
            @PathVariable @Parameter(description = "报告日期(yyyy-MM-dd)", required = true) LocalDate reportDate) {
        log.info("[覆盖率报告] 查询日期报告: reportDate={}", reportDate);

        RuleCoverageReportVO result = ruleCoverageReportService.getReportByDate(reportDate);

        if (result == null) {
            log.warn("[覆盖率报告] 报告不存在: reportDate={}", reportDate);
            return ResponseDTO.error("REPORT_NOT_FOUND", "报告不存在");
        }

        log.info("[覆盖率报告] 查询报告成功: reportDate={}, coverageRate={}%", reportDate, result.getCoverageRate());
        return ResponseDTO.ok(result);
    }

    /**
     * 查询最近的报告列表
     *
     * @param limit 限制数量
     * @return 报告列表
     */
    @Observed(name = "ruleCoverageReport.getRecent", contextualName = "get-recent-reports")
    @GetMapping("/recent")
    @Operation(summary = "查询最近的报告", description = "查询最近生成的覆盖率报告列表")
    public ResponseDTO<List<RuleCoverageReportVO>> getRecentReports(
            @RequestParam(defaultValue = "10") @Parameter(description = "限制数量", required = false) Integer limit) {
        log.info("[覆盖率报告] 查询最近报告: limit={}", limit);

        List<RuleCoverageReportVO> reports = ruleCoverageReportService.getRecentReports(limit);

        log.info("[覆盖率报告] 查询最近报告成功: count={}", reports.size());
        return ResponseDTO.ok(reports);
    }

    /**
     * 查询覆盖率趋势数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 趋势数据列表
     */
    @Observed(name = "ruleCoverageReport.getTrend", contextualName = "get-coverage-trend")
    @GetMapping("/trend")
    @Operation(summary = "查询覆盖率趋势", description = "查询指定时间范围内的覆盖率趋势数据")
    public ResponseDTO<List<RuleCoverageTrendVO>> getCoverageTrend(
            @RequestParam @Parameter(description = "开始日期(yyyy-MM-dd)", required = true) LocalDate startDate,
            @RequestParam @Parameter(description = "结束日期(yyyy-MM-dd)", required = true) LocalDate endDate) {
        log.info("[覆盖率报告] 查询趋势数据: startDate={}, endDate={}", startDate, endDate);

        List<RuleCoverageTrendVO> trend = ruleCoverageReportService.getCoverageTrend(startDate, endDate);

        log.info("[覆盖率报告] 查询趋势数据成功: count={}", trend.size());
        return ResponseDTO.ok(trend);
    }

    /**
     * 查询规则覆盖详情
     *
     * @param reportId 报告ID
     * @return 规则覆盖详情列表
     */
    @Observed(name = "ruleCoverageReport.getDetails", contextualName = "get-rule-coverage-details")
    @GetMapping("/{reportId}/details")
    @Operation(summary = "查询规则覆盖详情", description = "查询报告中每个规则的详细覆盖情况")
    public ResponseDTO<List<RuleCoverageDetailVO>> getRuleCoverageDetails(
            @PathVariable @Parameter(description = "报告ID", required = true) Long reportId) {
        log.info("[覆盖率报告] 查询规则详情: reportId={}", reportId);

        List<RuleCoverageDetailVO> details = ruleCoverageReportService.getRuleCoverageDetails(reportId);

        log.info("[覆盖率报告] 查询规则详情成功: reportId={}, count={}", reportId, details.size());
        return ResponseDTO.ok(details);
    }

    /**
     * 删除报告
     *
     * @param reportId 报告ID
     * @return 操作结果
     */
    @Observed(name = "ruleCoverageReport.delete", contextualName = "delete-coverage-report")
    @DeleteMapping("/{reportId}")
    @Operation(summary = "删除报告", description = "删除指定的覆盖率报告")
    public ResponseDTO<Void> deleteReport(
            @PathVariable @Parameter(description = "报告ID", required = true) Long reportId) {
        log.info("[覆盖率报告] 删除报告: reportId={}", reportId);

        ruleCoverageReportService.deleteReport(reportId);

        log.info("[覆盖率报告] 删除报告成功: reportId={}", reportId);
        return ResponseDTO.ok();
    }

    /**
     * 批量删除报告
     *
     * @param reportIds 报告ID列表
     * @return 操作结果
     */
    @Observed(name = "ruleCoverageReport.batchDelete", contextualName = "batch-delete-reports")
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除报告", description = "批量删除多条覆盖率报告")
    public ResponseDTO<Void> batchDeleteReports(
            @RequestBody @Parameter(description = "报告ID列表", required = true) List<Long> reportIds) {
        log.info("[覆盖率报告] 批量删除报告: count={}", reportIds.size());

        ruleCoverageReportService.batchDeleteReports(reportIds);

        log.info("[覆盖率报告] 批量删除报告成功: count={}", reportIds.size());
        return ResponseDTO.ok();
    }
}
