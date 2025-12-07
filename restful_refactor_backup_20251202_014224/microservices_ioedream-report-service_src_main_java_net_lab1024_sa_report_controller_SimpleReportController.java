package net.lab1024.sa.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.report.service.SimpleReportEngine;
import net.lab1024.sa.report.domain.vo.ReportRequestVO;
import net.lab1024.sa.report.domain.vo.ReportResponseVO;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.List;

/**
 * 企业级简化报表控制器
 *
 * 提供统一的报表生成、查询、导出等功能，支持多种报表格式和数据源
 * 严格遵循企业级开发规范，提供完整的API文档和错误处理
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-30
 */
@Slf4j
@RestController
@RequestMapping("/api/report/v1")
@RequiredArgsConstructor
@Validated
@Tag(name = "简化报表管理", description = "企业级简化报表生成、查询和导出相关接口")
public class SimpleReportController {

    private final SimpleReportEngine simpleReportEngine;

    /**
     * 生成简化报表
     *
     * 支持多种报表类型，包括数据报表、统计报表、分析报表等
     * 提供异步生成能力，适合大数据量报表处理
     *
     * @param request 报表生成请求参数
     * @return 报表生成结果，包含报表ID和状态信息
     */
    @Operation(
        summary = "生成简化报表",
        description = "根据指定参数生成简化版报表，支持多种数据源和格式",
        parameters = {
            @Parameter(name = "request", description = "报表生成请求参数", required = true)
        }
    )
    @PostMapping(value = "/generate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDTO<ReportResponseVO> generateReport(@Valid @RequestBody ReportRequestVO request) {
        try {
            log.info("开始生成简化报表: reportType={}, templateId={}, parameters={}",
                    request.getReportType(), request.getTemplateId(), request.getParameters());

            ReportResponseVO response = simpleReportEngine.generateReport(request);

            log.info("简化报表生成成功: reportId={}, status={}", response.getReportId(), response.getStatus());
            return ResponseDTO.ok(response, "报表生成成功");

        } catch (Exception e) {
            log.error("生成简化报表失败: reportType={}, error={}", request.getReportType(), e.getMessage(), e);
            return ResponseDTO.error("报表生成失败: " + e.getMessage());
        }
    }

    /**
     * 异步生成报表
     *
     * 适用于大数据量或复杂报表的异步处理场景
     * 返回任务ID，支持任务状态查询
     *
     * @param request 报表生成请求参数
     * @return 异步任务结果，包含任务ID
     */
    @Operation(
        summary = "异步生成报表",
        description = "异步生成报表，适用于大数据量或复杂报表处理",
        parameters = {
            @Parameter(name = "request", description = "报表生成请求参数", required = true)
        }
    )
    @PostMapping(value = "/generate-async", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDTO<String> generateReportAsync(@Valid @RequestBody ReportRequestVO request) {
        try {
            log.info("开始异步生成简化报表: reportType={}, templateId={}",
                    request.getReportType(), request.getTemplateId());

            String taskId = simpleReportEngine.generateReportAsync(request);

            log.info("异步报表生成任务创建成功: taskId={}", taskId);
            return ResponseDTO.ok(taskId, "异步报表生成任务已创建");

        } catch (Exception e) {
            log.error("异步生成简化报表失败: reportType={}, error={}", request.getReportType(), e.getMessage(), e);
            return ResponseDTO.error("异步报表生成失败: " + e.getMessage());
        }
    }

    /**
     * 查询报表状态
     *
     * 查询指定报表的生成状态和进度信息
     * 支持批量查询
     *
     * @param reportIds 报表ID列表，支持批量查询
     * @return 报表状态信息列表
     */
    @Operation(
        summary = "查询报表状态",
        description = "查询指定报表的生成状态和进度信息",
        parameters = {
            @Parameter(name = "reportIds", description = "报表ID列表，多个ID用逗号分隔", required = true)
        }
    )
    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDTO<List<ReportResponseVO>> getReportStatus(
            @RequestParam("reportIds") @NotNull(message = "报表ID不能为空") String reportIds) {
        try {
            log.info("查询报表状态: reportIds={}", reportIds);

            List<ReportResponseVO> statuses = simpleReportEngine.getReportStatus(reportIds);

            log.info("报表状态查询完成: count={}", statuses.size());
            return ResponseDTO.ok(statuses, "报表状态查询成功");

        } catch (Exception e) {
            log.error("查询报表状态失败: reportIds={}, error={}", reportIds, e.getMessage(), e);
            return ResponseDTO.error("报表状态查询失败: " + e.getMessage());
        }
    }

    /**
     * 下载报表文件
     *
     * 支持多种格式下载，包括PDF、Excel、Word等
     * 提供文件流下载，支持断点续传
     *
     * @param reportId 报表ID
     * @param format 文件格式，支持pdf、excel、word等
     * @return 报表文件流
     */
    @Operation(
        summary = "下载报表文件",
        description = "下载指定格式的报表文件",
        parameters = {
            @Parameter(name = "reportId", description = "报表ID", required = true),
            @Parameter(name = "format", description = "文件格式：pdf/excel/word", required = true)
        }
    )
    @GetMapping(value = "/download/{reportId}")
    public ResponseDTO<byte[]> downloadReport(
            @PathVariable @NotNull(message = "报表ID不能为空") String reportId,
            @RequestParam @NotNull(message = "文件格式不能为空") String format) {
        try {
            log.info("开始下载报表文件: reportId={}, format={}", reportId, format);

            byte[] fileData = simpleReportEngine.downloadReport(reportId, format);

            log.info("报表文件下载成功: reportId={}, size={}bytes", reportId, fileData.length);
            return ResponseDTO.ok(fileData, "报表文件下载成功");

        } catch (Exception e) {
            log.error("下载报表文件失败: reportId={}, format={}, error={}", reportId, format, e.getMessage(), e);
            return ResponseDTO.error("报表文件下载失败: " + e.getMessage());
        }
    }

    /**
     * 获取报表模板列表
     *
     * 返回所有可用的报表模板信息
     * 支持分页查询和条件筛选
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param templateType 模板类型（可选）
     * @return 报表模板分页结果
     */
    @Operation(
        summary = "获取报表模板列表",
        description = "获取所有可用的报表模板信息，支持分页查询",
        parameters = {
            @Parameter(name = "pageNum", description = "页码，默认为1"),
            @Parameter(name = "pageSize", description = "每页大小，默认为10"),
            @Parameter(name = "templateType", description = "模板类型，可选参数")
        }
    )
    @GetMapping(value = "/templates", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDTO<PageResult<Map<String, Object>>> getReportTemplates(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String templateType) {
        try {
            log.info("获取报表模板列表: pageNum={}, pageSize={}, templateType={}", pageNum, pageSize, templateType);

            PageResult<Map<String, Object>> templates = simpleReportEngine.getReportTemplates(pageNum, pageSize, templateType);

            log.info("报表模板列表获取成功: total={}, size={}", templates.getTotal(), templates.getRows().size());
            return ResponseDTO.ok(templates, "报表模板列表获取成功");

        } catch (Exception e) {
            log.error("获取报表模板列表失败: pageNum={}, pageSize={}, templateType={}, error={}",
                    pageNum, pageSize, templateType, e.getMessage(), e);
            return ResponseDTO.error("报表模板列表获取失败: " + e.getMessage());
        }
    }

    /**
     * 删除报表
     *
     * 删除指定的报表文件和相关数据
     * 支持批量删除
     *
     * @param reportIds 报表ID列表，支持批量删除
     * @return 删除结果
     */
    @Operation(
        summary = "删除报表",
        description = "删除指定的报表文件和相关数据",
        parameters = {
            @Parameter(name = "reportIds", description = "报表ID列表，多个ID用逗号分隔", required = true)
        }
    )
    @DeleteMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDTO<String> deleteReports(
            @RequestParam("reportIds") @NotNull(message = "报表ID不能为空") String reportIds) {
        try {
            log.info("开始删除报表: reportIds={}", reportIds);

            String result = simpleReportEngine.deleteReports(reportIds);

            log.info("报表删除成功: reportIds={}, result={}", reportIds, result);
            return ResponseDTO.ok(result, "报表删除成功");

        } catch (Exception e) {
            log.error("删除报表失败: reportIds={}, error={}", reportIds, e.getMessage(), e);
            return ResponseDTO.error("报表删除失败: " + e.getMessage());
        }
    }

    /**
     * 系统健康检查
     *
     * 检查报表引擎和相关服务的健康状态
     * 返回系统状态和性能指标
     *
     * @return 系统健康状态信息
     */
    @Operation(
        summary = "系统健康检查",
        description = "检查报表引擎和相关服务的健康状态"
    )
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDTO<Map<String, Object>> health() {
        try {
            log.debug("执行报表系统健康检查");

            Map<String, Object> healthInfo = simpleReportEngine.healthCheck();

            log.debug("健康检查完成: status={}", healthInfo.get("status"));
            return ResponseDTO.ok(healthInfo, "系统健康状态正常");

        } catch (Exception e) {
            log.error("系统健康检查失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("系统健康检查失败: " + e.getMessage());
        }
    }

    /**
     * 获取系统统计信息
     *
     * 返回报表系统的统计信息和性能指标
     * 包括报表数量、处理时间、成功率等
     *
     * @return 系统统计信息
     */
    @Operation(
        summary = "获取系统统计信息",
        description = "获取报表系统的统计信息和性能指标"
    )
    @GetMapping(value = "/statistics", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDTO<Map<String, Object>> getStatistics() {
        try {
            log.debug("获取报表系统统计信息");

            Map<String, Object> statistics = simpleReportEngine.getStatistics();

            log.debug("统计信息获取完成: reportCount={}, successRate={}",
                    statistics.get("reportCount"), statistics.get("successRate"));
            return ResponseDTO.ok(statistics, "统计信息获取成功");

        } catch (Exception e) {
            log.error("获取系统统计信息失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("统计信息获取失败: " + e.getMessage());
        }
    }
}