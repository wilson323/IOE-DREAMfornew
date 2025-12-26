package net.lab1024.sa.consume.controller.report;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.entity.report.*;
import net.lab1024.sa.consume.service.report.ReportDefinitionService;
import net.lab1024.sa.consume.service.report.ReportGenerationService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 报表管理控制器 - 报表中心REST API完整实现
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Slf4j
@RestController
@RequestMapping("/api/report")
@Tag(name = "报表管理")
public class ReportDefinitionController {

    @Resource
    private ReportDefinitionService reportDefinitionService;

    @Resource
    private ReportGenerationService reportGenerationService;

    // ========== 报表定义管理 ==========

    @PostMapping("/definition")
    @Operation(summary = "新增报表定义")
    public ResponseDTO<Long> addReport(@RequestBody ReportDefinitionEntity report) {
        log.info("[报表管理] 新增报表定义: reportName={}", report.getReportName());
        Long reportId = reportDefinitionService.addReport(report);
        return ResponseDTO.ok(reportId);
    }

    @PutMapping("/definition")
    @Operation(summary = "更新报表定义")
    public ResponseDTO<Void> updateReport(@RequestBody ReportDefinitionEntity report) {
        log.info("[报表管理] 更新报表定义: reportId={}", report.getReportId());
        reportDefinitionService.updateReport(report);
        return ResponseDTO.ok();
    }

    @DeleteMapping("/definition/{reportId}")
    @Operation(summary = "删除报表定义")
    public ResponseDTO<Void> deleteReport(@PathVariable Long reportId) {
        log.info("[报表管理] 删除报表定义: reportId={}", reportId);
        reportDefinitionService.deleteReport(reportId);
        return ResponseDTO.ok();
    }

    @GetMapping("/definition/{reportId}")
    @Operation(summary = "查询报表详情")
    public ResponseDTO<ReportDefinitionEntity> getReportDetail(@PathVariable Long reportId) {
        log.info("[报表管理] 查询报表详情: reportId={}", reportId);
        ReportDefinitionEntity report = reportDefinitionService.getReportDetail(reportId);
        return ResponseDTO.ok(report);
    }

    @GetMapping("/definition/list")
    @Operation(summary = "分页查询报表列表")
    public ResponseDTO<PageResult<ReportDefinitionEntity>> queryReports(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String businessModule,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("[报表管理] 查询报表列表: categoryId={}, businessModule={}", categoryId, businessModule);

        Page<ReportDefinitionEntity> page = reportDefinitionService.queryReports(categoryId, businessModule, pageNum, pageSize);

        return ResponseDTO.ok(PageResult.of(page.getRecords(), page.getTotal(), pageNum, pageSize));
    }

    @GetMapping("/categories")
    @Operation(summary = "查询报表分类列表")
    public ResponseDTO<List<ReportCategoryEntity>> getReportCategories() {
        log.info("[报表管理] 查询报表分类列表");
        List<ReportCategoryEntity> categories = reportDefinitionService.getReportCategories();
        return ResponseDTO.ok(categories);
    }

    @GetMapping("/definition/{reportId}/parameters")
    @Operation(summary = "查询报表参数列表")
    public ResponseDTO<List<ReportParameterEntity>> getReportParameters(@PathVariable Long reportId) {
        log.info("[报表管理] 查询报表参数: reportId={}", reportId);
        List<ReportParameterEntity> parameters = reportDefinitionService.getReportParameters(reportId);
        return ResponseDTO.ok(parameters);
    }

    @PutMapping("/definition/{reportId}/enable")
    @Operation(summary = "启用报表")
    public ResponseDTO<Void> enableReport(@PathVariable Long reportId) {
        log.info("[报表管理] 启用报表: reportId={}", reportId);
        reportDefinitionService.enableReport(reportId);
        return ResponseDTO.ok();
    }

    @PutMapping("/definition/{reportId}/disable")
    @Operation(summary = "禁用报表")
    public ResponseDTO<Void> disableReport(@PathVariable Long reportId) {
        log.info("[报表管理] 禁用报表: reportId={}", reportId);
        reportDefinitionService.disableReport(reportId);
        return ResponseDTO.ok();
    }

    // ========== 报表生成管理 ==========

    @PostMapping("/definition/{reportId}/generate")
    @Operation(summary = "生成报表")
    public ResponseDTO<Long> generateReport(
            @PathVariable Long reportId,
            @RequestParam String parameters,
            @RequestParam(defaultValue = "1") Integer generateType,
            @RequestParam(defaultValue = "excel") String fileType) {
        log.info("[报表管理] 生成报表: reportId={}, fileType={}", reportId, fileType);
        Long generationId = reportDefinitionService.generateReport(reportId, parameters, generateType, fileType);
        return ResponseDTO.ok(generationId);
    }

    @GetMapping("/generation/list")
    @Operation(summary = "查询生成记录列表")
    public ResponseDTO<PageResult<ReportGenerationEntity>> queryGenerationRecords(
            @RequestParam(required = false) Long reportId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("[报表管理] 查询生成记录: reportId={}", reportId);

        List<ReportGenerationEntity> records = reportGenerationService.queryGenerationRecords(reportId, pageNum, pageSize);

        return ResponseDTO.ok(PageResult.of(records, (long) records.size(), pageNum, pageSize));
    }

    @GetMapping("/generation/{generationId}")
    @Operation(summary = "查询生成记录详情")
    public ResponseDTO<ReportGenerationEntity> getGenerationDetail(@PathVariable Long generationId) {
        log.info("[报表管理] 查询生成记录详情: generationId={}", generationId);
        ReportGenerationEntity generation = reportGenerationService.getGenerationDetail(generationId);
        return ResponseDTO.ok(generation);
    }

    @DeleteMapping("/generation/{generationId}")
    @Operation(summary = "删除生成记录")
    public ResponseDTO<Void> deleteGeneration(@PathVariable Long generationId) {
        log.info("[报表管理] 删除生成记录: generationId={}", generationId);
        reportGenerationService.deleteGeneration(generationId);
        return ResponseDTO.ok();
    }

    @GetMapping("/generation/my")
    @Operation(summary = "查询我的生成记录")
    public ResponseDTO<PageResult<ReportGenerationEntity>> queryMyGenerations(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("[报表管理] 查询我的生成记录: userId={}", userId);

        List<ReportGenerationEntity> records = reportGenerationService.queryUserGenerations(userId, pageNum, pageSize);

        return ResponseDTO.ok(PageResult.of(records, (long) records.size(), pageNum, pageSize));
    }
}
