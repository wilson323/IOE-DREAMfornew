package net.lab1024.sa.report.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.report.domain.analytics.ReportGenerationRequest;
import net.lab1024.sa.report.domain.analytics.ReportGenerationResult;
import net.lab1024.sa.report.service.analytics.ReportGenerationService;

/**
 * 报表分析控制器
 * <p>
 * 整合了原analytics模块的报表生成功能
 * 提供跨服务数据聚合分析、报表生成、业务洞察功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/report/analytics")
@Tag(name = "报表分析", description = "跨服务数据聚合分析、报表生成、业务洞察API")
public class AnalyticsReportController {

    @Resource
    private ReportGenerationService reportGenerationService;

    @Operation(summary = "生成Excel报表", description = "根据请求生成Excel格式的报表")
    @PostMapping("/excel")
    public ResponseDTO<ReportGenerationResult> generateExcelReport(
            @Valid @RequestBody ReportGenerationRequest request) {
        return reportGenerationService.generateExcelReport(request);
    }

    @Operation(summary = "生成PDF报表", description = "根据请求生成PDF格式的报表")
    @PostMapping("/pdf")
    public ResponseDTO<ReportGenerationResult> generatePdfReport(
            @Valid @RequestBody ReportGenerationRequest request) {
        return reportGenerationService.generatePdfReport(request);
    }

    @Operation(summary = "生成图表报表", description = "根据请求生成图表格式的报表")
    @PostMapping("/chart")
    public ResponseDTO<ReportGenerationResult> generateChartReport(
            @Valid @RequestBody ReportGenerationRequest request) {
        return reportGenerationService.generateChartReport(request);
    }

    @Operation(summary = "获取报表生成状态", description = "根据任务ID获取报表生成状态")
    @GetMapping("/status/{taskId}")
    public ResponseDTO<Map<String, Object>> getReportGenerationStatus(
            @Parameter(description = "任务ID", required = true) @PathVariable String taskId) {
        return reportGenerationService.getReportGenerationStatus(taskId);
    }

    @Operation(summary = "下载报表文件", description = "根据文件ID下载报表文件")
    @GetMapping("/download/{fileId}")
    public void downloadReportFile(
            @Parameter(description = "文件ID", required = true) @PathVariable String fileId,
            HttpServletResponse response) {
        reportGenerationService.downloadReportFile(fileId, response);
    }

    @Operation(summary = "删除报表文件", description = "根据文件ID删除报表文件")
    @DeleteMapping("/file/{fileId}")
    public ResponseDTO<Boolean> deleteReportFile(
            @Parameter(description = "文件ID", required = true) @PathVariable String fileId) {
        return reportGenerationService.deleteReportFile(fileId);
    }

    @Operation(summary = "获取报表模板列表", description = "获取所有可用的报表模板")
    @GetMapping("/templates")
    public ResponseDTO<List<Map<String, Object>>> getReportTemplates() {
        return reportGenerationService.getReportTemplates();
    }

    @Operation(summary = "保存报表模板", description = "保存新的报表模板")
    @PostMapping("/template")
    public ResponseDTO<String> saveReportTemplate(@RequestBody Map<String, Object> templateData) {
        return reportGenerationService.saveReportTemplate(templateData);
    }

    @Operation(summary = "预览报表数据", description = "预览报表数据而不生成文件")
    @PostMapping("/preview")
    public ResponseDTO<Map<String, Object>> previewReportData(
            @Valid @RequestBody ReportGenerationRequest request) {
        return reportGenerationService.previewReportData(request);
    }

    @Operation(summary = "导出报表数据", description = "导出报表数据到指定格式")
    @PostMapping("/export/{format}")
    public ResponseDTO<String> exportReportData(
            @Valid @RequestBody ReportGenerationRequest request,
            @Parameter(description = "导出格式", required = true) @PathVariable String format) {
        return reportGenerationService.exportReportData(request, format);
    }

    @Operation(summary = "获取报表统计数据", description = "获取指定时间范围内的报表统计数据")
    @GetMapping("/statistics/{templateId}")
    public ResponseDTO<Map<String, Object>> getReportStatistics(
            @Parameter(description = "模板ID", required = true) @PathVariable String templateId,
            @Parameter(description = "开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) String endTime) {
        // TODO: 转换时间字符串为LocalDateTime
        return ResponseDTO.ok(Map.of());
    }

    @Operation(summary = "批量生成报表", description = "批量生成多个报表")
    @PostMapping("/batch")
    public ResponseDTO<List<ReportGenerationResult>> batchGenerateReports(
            @Valid @RequestBody List<ReportGenerationRequest> requests) {
        return reportGenerationService.batchGenerateReports(requests);
    }

    @Operation(summary = "调度报表生成", description = "创建定时报表生成任务")
    @PostMapping("/schedule/{templateId}")
    public ResponseDTO<String> scheduleReportGeneration(
            @Parameter(description = "模板ID", required = true) @PathVariable String templateId,
            @RequestBody Map<String, Object> scheduleConfig) {
        return reportGenerationService.scheduleReportGeneration(templateId, scheduleConfig);
    }

    @Operation(summary = "取消调度任务", description = "取消已创建的定时报表生成任务")
    @DeleteMapping("/schedule/{scheduleId}")
    public ResponseDTO<Boolean> cancelScheduledReport(
            @Parameter(description = "调度ID", required = true) @PathVariable String scheduleId) {
        return reportGenerationService.cancelScheduledReport(scheduleId);
    }

    @Operation(summary = "获取调度任务列表", description = "获取所有定时报表生成任务")
    @GetMapping("/schedules")
    public ResponseDTO<List<Map<String, Object>>> getScheduledReports() {
        return reportGenerationService.getScheduledReports();
    }

    @Operation(summary = "复制报表模板", description = "复制现有的报表模板")
    @PostMapping("/template/{templateId}/copy")
    public ResponseDTO<String> copyReportTemplate(
            @Parameter(description = "原模板ID", required = true) @PathVariable String templateId,
            @Parameter(description = "新模板名称", required = true) @RequestParam String newTemplateName) {
        return reportGenerationService.copyReportTemplate(templateId, newTemplateName);
    }

    @Operation(summary = "验证报表模板", description = "验证报表模板配置是否正确")
    @PostMapping("/template/validate")
    public ResponseDTO<Map<String, Object>> validateReportTemplate(@RequestBody Map<String, Object> templateData) {
        return reportGenerationService.validateReportTemplate(templateData);
    }

    @Operation(summary = "获取报表执行日志", description = "获取指定模板的报表执行日志")
    @GetMapping("/logs/{templateId}")
    public ResponseDTO<Map<String, Object>> getReportExecutionLogs(
            @Parameter(description = "模板ID", required = true) @PathVariable String templateId,
            @Parameter(description = "页码", required = false) @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小", required = false) @RequestParam(defaultValue = "20") Integer pageSize) {
        return reportGenerationService.getReportExecutionLogs(templateId, pageNum, pageSize);
    }
}
