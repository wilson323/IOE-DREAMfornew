package net.lab1024.sa.consume.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import net.lab1024.sa.consume.domain.form.ConsumeReportQueryForm;
import net.lab1024.sa.consume.service.ConsumeReportService;

/**
 * 消费报表管理控制器
 * <p>
 * 提供消费报表的管理功能，包括：
 * 1. 消费统计报表
 * 2. 设备运营报表
 * 3. 用户消费报表
 * 4. 财务收支报表
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Slf4j
@RestController
@PermissionCheck(value = "CONSUME_REPORT_MANAGE", description = "消费报表管理权限")
@RequestMapping("/api/v1/consume/reports")
@Tag(name = "消费报表管理", description = "消费统计报表、设备运营报表等功能")
public class ConsumeReportController {

    @Resource
    private ConsumeReportService consumeReportService;

    /**
     * 获取消费日报表
     *
     * @param date 日期（yyyy-MM-dd格式）
     * @return 日报表数据
     */
    @GetMapping("/daily/{date}")
    @Operation(summary = "获取消费日报表", description = "获取指定日期的消费日报表数据")
    public ResponseDTO<Map<String, Object>> getDailyReport(
            @Parameter(description = "日期", required = true) @PathVariable String date) {
        Map<String, Object> report = consumeReportService.getDailyReport(date);
        return ResponseDTO.ok(report);
    }

    /**
     * 获取消费月报表
     *
     * @param year  年份
     * @param month 月份
     * @return 月报表数据
     */
    @GetMapping("/monthly/{year}/{month}")
    @Operation(summary = "获取消费月报表", description = "获取指定年月的消费月报表数据")
    public ResponseDTO<Map<String, Object>> getMonthlyReport(
            @Parameter(description = "年份", required = true) @PathVariable Integer year,
            @Parameter(description = "月份", required = true) @PathVariable Integer month) {
        Map<String, Object> report = consumeReportService.getMonthlyReport(year, month);
        return ResponseDTO.ok(report);
    }

    /**
     * 获取消费年报表
     *
     * @param year 年份
     * @return 年报表数据
     */
    @GetMapping("/yearly/{year}")
    @Operation(summary = "获取消费年报表", description = "获取指定年份的消费年报表数据")
    public ResponseDTO<Map<String, Object>> getYearlyReport(
            @Parameter(description = "年份", required = true) @PathVariable Integer year) {
        Map<String, Object> report = consumeReportService.getYearlyReport(year);
        return ResponseDTO.ok(report);
    }

    /**
     * 获取自定义时间段报表
     *
     * @param queryForm 查询条件
     * @return 报表数据
     */
    @PostMapping("/custom")
    @Operation(summary = "获取自定义报表", description = "获取自定义时间段的消费报表数据")
    public ResponseDTO<Map<String, Object>> getCustomReport(@ModelAttribute ConsumeReportQueryForm queryForm) {
        Map<String, Object> report = consumeReportService.getCustomReport(queryForm);
        return ResponseDTO.ok(report);
    }

    /**
     * 获取设备运营报表
     *
     * @param deviceId  设备ID（可选）
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 设备运营报表
     */
    @GetMapping("/device-operation")
    @Operation(summary = "获取设备运营报表", description = "获取设备运营相关的报表数据")
    public ResponseDTO<Map<String, Object>> getDeviceOperationReport(
            @Parameter(description = "设备ID") @RequestParam(required = false) String deviceId,
            @Parameter(description = "开始日期") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) LocalDateTime endDate) {
        Map<String, Object> report = consumeReportService.getDeviceOperationReport(deviceId, startDate, endDate);
        return ResponseDTO.ok(report);
    }

    /**
     * 获取用户消费报表
     *
     * @param userId    用户ID（可选）
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 用户消费报表
     */
    @GetMapping("/user-consumption")
    @Operation(summary = "获取用户消费报表", description = "获取用户消费相关的报表数据")
    public ResponseDTO<Map<String, Object>> getUserConsumptionReport(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "开始日期") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) LocalDateTime endDate) {
        Map<String, Object> report = consumeReportService.getUserConsumptionReport(userId, startDate, endDate);
        return ResponseDTO.ok(report);
    }

    /**
     * 获取财务收支报表
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 财务收支报表
     */
    @GetMapping("/financial")
    @Operation(summary = "获取财务收支报表", description = "获取财务收支相关的报表数据")
    public ResponseDTO<Map<String, Object>> getFinancialReport(
            @Parameter(description = "开始日期") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) LocalDateTime endDate) {
        Map<String, Object> report = consumeReportService.getFinancialReport(startDate, endDate);
        return ResponseDTO.ok(report);
    }

    /**
     * 获取产品销售报表
     *
     * @param productId 产品ID（可选）
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 产品销售报表
     */
    @GetMapping("/product-sales")
    @Operation(summary = "获取产品销售报表", description = "获取产品销售相关的报表数据")
    public ResponseDTO<Map<String, Object>> getProductSalesReport(
            @Parameter(description = "产品ID") @RequestParam(required = false) Long productId,
            @Parameter(description = "开始日期") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) LocalDateTime endDate) {
        Map<String, Object> report = consumeReportService.getProductSalesReport(productId, startDate, endDate);
        return ResponseDTO.ok(report);
    }

    /**
     * 获取消费趋势分析
     *
     * @param type 分析类型（day/week/month）
     * @param days 天数
     * @return 趋势分析数据
     */
    @GetMapping("/trend-analysis")
    @Operation(summary = "获取消费趋势分析", description = "获取消费趋势分析数据")
    public ResponseDTO<Map<String, Object>> getTrendAnalysis(
            @Parameter(description = "分析类型", required = true) @RequestParam String type,
            @Parameter(description = "天数") @RequestParam(defaultValue = "30") Integer days) {
        Map<String, Object> analysis = consumeReportService.getTrendAnalysis(type, days);
        return ResponseDTO.ok(analysis);
    }

    /**
     * 获取对比分析报表
     *
     * @param currentPeriodStart  当前周期开始时间
     * @param currentPeriodEnd    当前周期结束时间
     * @param previousPeriodStart 上期周期开始时间
     * @param previousPeriodEnd   上期周期结束时间
     * @return 对比分析数据
     */
    @GetMapping("/comparison-analysis")
    @Operation(summary = "获取对比分析报表", description = "获取不同时间段的对比分析数据")
    public ResponseDTO<Map<String, Object>> getComparisonAnalysis(
            @Parameter(description = "当前周期开始时间", required = true) @RequestParam LocalDateTime currentPeriodStart,
            @Parameter(description = "当前周期结束时间", required = true) @RequestParam LocalDateTime currentPeriodEnd,
            @Parameter(description = "上期周期开始时间", required = true) @RequestParam LocalDateTime previousPeriodStart,
            @Parameter(description = "上期周期结束时间", required = true) @RequestParam LocalDateTime previousPeriodEnd) {
        Map<String, Object> analysis = consumeReportService.getComparisonAnalysis(
                currentPeriodStart, currentPeriodEnd, previousPeriodStart, previousPeriodEnd);
        return ResponseDTO.ok(analysis);
    }

    /**
     * 导出报表
     *
     * @param reportType 报表类型
     * @param format     导出格式（excel/pdf）
     * @param queryForm  查询条件
     * @return 导出文件下载链接
     */
    @PostMapping("/export")
    @Operation(summary = "导出报表", description = "导出指定类型的报表文件")
    public ResponseDTO<String> exportReport(
            @Parameter(description = "报表类型", required = true) @RequestParam String reportType,
            @Parameter(description = "导出格式", required = true) @RequestParam String format,
            @ModelAttribute ConsumeReportQueryForm queryForm) {
        String downloadUrl = consumeReportService.exportReport(reportType, format, queryForm);
        return ResponseDTO.ok(downloadUrl);
    }

    /**
     * 获取实时消费概览
     *
     * @return 实时概览数据
     */
    @GetMapping("/realtime-overview")
    @Operation(summary = "获取实时消费概览", description = "获取当前实时的消费概览数据")
    public ResponseDTO<Map<String, Object>> getRealtimeOverview() {
        Map<String, Object> overview = consumeReportService.getRealtimeOverview();
        return ResponseDTO.ok(overview);
    }

    /**
     * 获取异常消费分析
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 异常分析数据
     */
    @GetMapping("/abnormal-analysis")
    @Operation(summary = "获取异常消费分析", description = "获取异常消费的分析数据")
    public ResponseDTO<Map<String, Object>> getAbnormalAnalysis(
            @Parameter(description = "开始日期") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) LocalDateTime endDate) {
        String startDateStr = startDate != null ? startDate.toString() : null;
        String endDateStr = endDate != null ? endDate.toString() : null;
        Map<String, Object> analysis = consumeReportService.getAbnormalAnalysis(startDateStr, endDateStr);
        return ResponseDTO.ok(analysis);
    }

    /**
     * 获取消费排行榜
     *
     * @param type      排行榜类型（user/product/device）
     * @param limit     数量限制
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 排行榜数据
     */
    @GetMapping("/ranking")
    @Operation(summary = "获取消费排行榜", description = "获取消费相关的排行榜数据")
    public ResponseDTO<List<Map<String, Object>>> getConsumptionRanking(
            @Parameter(description = "排行榜类型", required = true) @RequestParam String type,
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "10") Integer limit,
            @Parameter(description = "开始日期") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) LocalDateTime endDate) {
        String startDateStr = startDate != null ? startDate.toString() : null;
        String endDateStr = endDate != null ? endDate.toString() : null;
        List<Map<String, Object>> ranking = consumeReportService.getConsumptionRanking(type, limit, startDateStr,
                endDateStr);
        return ResponseDTO.ok(ranking);
    }
}