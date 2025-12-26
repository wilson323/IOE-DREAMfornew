package net.lab1024.sa.data.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.data.domain.DataAnalysisDomain.*;
import net.lab1024.sa.data.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据报表控制器
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "数据报表管理", description = "BI报表、数据查询、数据导出")
public class ReportController {

    @Autowired
    private ReportService reportService;

    // ==================== 报表管理 ====================

    @Operation(summary = "创建报表")
    @PostMapping
    public ResponseDTO<Long> createReport(@Valid @RequestBody ReportVO report) {
        log.info("[数据报表API] 创建报表: reportName={}", report.getReportName());

        Long reportId = reportService.createReport(report);

        log.info("[数据报表API] 报表创建成功: reportId={}", reportId);
        return ResponseDTO.ok(reportId);
    }

    @Operation(summary = "更新报表")
    @PutMapping("/{reportId}")
    public ResponseDTO<Void> updateReport(@PathVariable Long reportId,
                                          @Valid @RequestBody ReportVO report) {
        log.info("[数据报表API] 更新报表: reportId={}", reportId);

        reportService.updateReport(reportId, report);

        log.info("[数据报表API] 报表更新成功: reportId={}", reportId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除报表")
    @DeleteMapping("/{reportId}")
    public ResponseDTO<Void> deleteReport(@PathVariable Long reportId) {
        log.info("[数据报表API] 删除报表: reportId={}", reportId);

        reportService.deleteReport(reportId);

        log.info("[数据报表API] 报表删除成功: reportId={}", reportId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "获取报表详情")
    @GetMapping("/{reportId}")
    public ResponseDTO<ReportVO> getReportDetail(@PathVariable Long reportId) {
        log.info("[数据报表API] 获取报表详情: reportId={}", reportId);

        ReportVO report = reportService.getReportDetail(reportId);

        return ResponseDTO.ok(report);
    }

    @Operation(summary = "获取报表列表")
    @GetMapping
    public ResponseDTO<PageResult<ReportVO>> getReportList(
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String businessModule,
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) String status) {
        log.info("[数据报表API] 查询报表列表: pageNum={}, pageSize={}, module={}, type={}, status={}",
                 pageNum, pageSize, businessModule, reportType, status);

        PageResult<ReportVO> result = reportService.getReportList(
                pageNum != null ? pageNum : 1,
                pageSize != null ? pageSize : 20,
                businessModule,
                reportType,
                status
        );

        return ResponseDTO.ok(result);
    }

    @Operation(summary = "复制报表")
    @PostMapping("/{reportId}/copy")
    public ResponseDTO<Long> copyReport(@PathVariable Long reportId,
                                        @RequestParam String newReportName) {
        log.info("[数据报表API] 复制报表: reportId={}, newName={}", reportId, newReportName);

        Long newReportId = reportService.copyReport(reportId, newReportName);

        log.info("[数据报表API] 报表复制成功: newReportId={}", newReportId);
        return ResponseDTO.ok(newReportId);
    }

    // ==================== 报表查询 ====================

    @Operation(summary = "查询报表数据")
    @PostMapping("/{reportId}/query")
    public ResponseDTO<ReportQueryResult> queryReportData(@PathVariable Long reportId,
                                                         @Valid @RequestBody ReportQueryRequest request) {
        log.info("[数据报表API] 查询报表数据: reportId={}", reportId);

        request.setReportId(reportId);
        ReportQueryResult result = reportService.queryReportData(request);

        return ResponseDTO.ok(result);
    }

    @Operation(summary = "刷新报表数据")
    @PostMapping("/{reportId}/refresh")
    public ResponseDTO<ReportQueryResult> refreshReportData(@PathVariable Long reportId) {
        log.info("[数据报表API] 刷新报表数据: reportId={}", reportId);

        ReportQueryResult result = reportService.refreshReportData(reportId);

        return ResponseDTO.ok(result);
    }

    @Operation(summary = "获取报表预览")
    @GetMapping("/{reportId}/preview")
    public ResponseDTO<ReportQueryResult> getReportPreview(@PathVariable Long reportId) {
        log.info("[数据报表API] 获取报表预览: reportId={}", reportId);

        ReportQueryResult result = reportService.getReportPreview(reportId);

        return ResponseDTO.ok(result);
    }

    // ==================== 数据导出 ====================

    @Operation(summary = "导出报表数据")
    @PostMapping("/{reportId}/export")
    public ResponseDTO<String> exportReportData(@PathVariable Long reportId,
                                                 @Valid @RequestBody DataExportRequest request) {
        log.info("[数据报表API] 导出报表数据: reportId={}, format={}",
                 reportId, request.getExportFormat());

        request.setReportId(reportId);
        String exportTaskId = reportService.exportReportData(request);

        log.info("[数据报表API] 导出任务创建: taskId={}", exportTaskId);
        return ResponseDTO.ok(exportTaskId);
    }

    @Operation(summary = "获取导出状态")
    @GetMapping("/export/{exportTaskId}")
    public ResponseDTO<DataExportResult> getExportStatus(@PathVariable String exportTaskId) {
        log.info("[数据报表API] 查询导出状态: taskId={}", exportTaskId);

        DataExportResult result = reportService.getExportStatus(exportTaskId);

        return ResponseDTO.ok(result);
    }

    @Operation(summary = "批量导出")
    @PostMapping("/batch-export")
    public ResponseDTO<List<String>> batchExport(@RequestBody List<Long> reportIds,
                                                 @RequestParam String format) {
        log.info("[数据报表API] 批量导出: count={}, format={}", reportIds.size(), format);

        List<String> exportTaskIds = reportService.batchExport(reportIds, format);

        log.info("[数据报表API] 批量导出任务创建: count={}", exportTaskIds.size());
        return ResponseDTO.ok(exportTaskIds);
    }

    // ==================== 数据统计 ====================

    @Operation(summary = "获取统计数据")
    @GetMapping("/{reportId}/statistics")
    public ResponseDTO<List<DataStatisticsVO>> getStatistics(
            @PathVariable Long reportId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        log.info("[数据报表API] 获取统计数据: reportId={}, start={}, end={}",
                 reportId, startDate, endDate);

        List<DataStatisticsVO> statistics = reportService.getStatistics(reportId, startDate, endDate);

        return ResponseDTO.ok(statistics);
    }

    @Operation(summary = "获取趋势数据")
    @GetMapping("/{reportId}/trend")
    public ResponseDTO<ChartData> getTrendData(
            @PathVariable Long reportId,
            @RequestParam String field,
            @RequestParam(defaultValue = "day") String period,
            @RequestParam(defaultValue = "7") Integer periodCount) {
        log.info("[数据报表API] 获取趋势数据: reportId={}, field={}, period={}, count={}",
                 reportId, field, period, periodCount);

        ChartData chartData = reportService.getTrendData(reportId, field, period, periodCount);

        return ResponseDTO.ok(chartData);
    }

    @Operation(summary = "获取对比数据")
    @PostMapping("/{reportId}/compare")
    public ResponseDTO<Map<String, Object>> getCompareData(
            @PathVariable Long reportId,
            @RequestParam String compareField,
            @RequestBody List<String> comparePeriods) {
        log.info("[数据报表API] 获取对比数据: reportId={}, field={}, periods={}",
                 reportId, compareField, comparePeriods);

        Map<String, Object> compareData = reportService.getCompareData(reportId,
                                                                       compareField,
                                                                       comparePeriods);

        return ResponseDTO.ok(compareData);
    }

    // ==================== 报表缓存 ====================

    @Operation(summary = "清除报表缓存")
    @DeleteMapping("/{reportId}/cache")
    public ResponseDTO<Void> clearReportCache(@PathVariable Long reportId) {
        log.info("[数据报表API] 清除报表缓存: reportId={}", reportId);

        reportService.clearReportCache(reportId);

        log.info("[数据报表API] 缓存清除成功: reportId={}", reportId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "预加载报表数据")
    @PostMapping("/preload")
    public ResponseDTO<Void> preloadReportData(@RequestBody List<Long> reportIds) {
        log.info("[数据报表API] 预加载报表数据: count={}", reportIds.size());

        reportService.preloadReportData(reportIds);

        log.info("[数据报表API] 预加载完成: count={}", reportIds.size());
        return ResponseDTO.ok();
    }

    // ==================== 报表权限 ====================

    @Operation(summary = "设置报表权限")
    @PutMapping("/{reportId}/permission")
    public ResponseDTO<Void> setReportPermission(@PathVariable Long reportId,
                                                 @RequestBody ReportPermission permission) {
        log.info("[数据报表API] 设置报表权限: reportId={}", reportId);

        reportService.setReportPermission(reportId, permission);

        log.info("[数据报表API] 权限设置成功: reportId={}", reportId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "检查报表权限")
    @GetMapping("/{reportId}/permission/{permissionType}")
    public ResponseDTO<Boolean> checkReportPermission(@PathVariable Long reportId,
                                                       @PathVariable String permissionType) {
        log.info("[数据报表API] 检查报表权限: reportId={}, type={}", reportId, permissionType);

        Boolean hasPermission = reportService.checkReportPermission(reportId, permissionType);

        return ResponseDTO.ok(hasPermission);
    }

    @Operation(summary = "获取用户可见报表")
    @GetMapping("/user/{userId}/visible")
    public ResponseDTO<List<ReportVO>> getUserVisibleReports(@PathVariable Long userId) {
        log.info("[数据报表API] 获取用户可见报表: userId={}", userId);

        List<ReportVO> reports = reportService.getUserVisibleReports(userId);

        return ResponseDTO.ok(reports);
    }
}
