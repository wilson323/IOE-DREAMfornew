package net.lab1024.sa.consume.controller;

import java.util.Map;

import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.consume.report.domain.form.ReportParams;
import net.lab1024.sa.consume.service.ConsumeReportService;

/**
 * 报表管理控制器
 * <p>
 * 提供消费报表管理相关的REST API接口
 * 严格遵循CLAUDE.md规范：
 * - Controller层负责接收请求、参数验证、返回响应
 * - 使用@Resource注入Service（不直接注入Manager）
 * - 返回统一ResponseDTO格式
 * </p>
 * <p>
 * 业务场景：
 * - 报表生成
 * - 报表导出（Excel/PDF/CSV）
 * - 报表模板管理
 * - 报表统计分析
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/consume/report")
@Tag(name = "报表管理", description = "消费报表管理相关接口")
@PermissionCheck(value = "CONSUME_REPORT", description = "消费报表管理模块权限")
public class ReportController {

    @Resource
    private ConsumeReportService consumeReportService;

    /**
     * 生成消费报表
     * <p>
     * 根据模板ID和参数生成消费报表数据，支持多种报表类型（日消费报表、月消费报表、设备统计报表等）
     * </p>
     *
     * @param templateId 模板ID（必填）
     * @param params 报表参数（可选，包含时间范围、区域ID、用户ID等筛选条件）
     * @return 报表数据，包含统计数据和明细数据
     * @apiNote 示例请求：
     * <pre>
     * POST /api/v1/consume/report/generate?templateId=1
     * Body: {
     *   "startDate": "2025-01-01",
     *   "endDate": "2025-01-31",
     *   "areaId": "AREA001"
     * }
     * </pre>
     */
    @PostMapping("/generate")
    @Observed(name = "report.generateReport", contextualName = "report-generate-report")
    @Operation(
        summary = "生成消费报表",
        description = "根据模板ID和参数生成消费报表数据，支持多种报表类型（日消费报表、月消费报表、设备统计报表等）。",
        tags = {"报表管理"}
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "生成成功，返回报表数据",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Map.class)
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "模板不存在"
    )
    @PermissionCheck(value = "CONSUME_ACCOUNT_MANAGE", description = "账户管理操作")
    public ResponseDTO<Map<String, Object>> generateReport(
            @Parameter(description = "模板ID", required = true, example = "1")
            @RequestParam Long templateId,
            @Parameter(description = "报表参数（可选）")
            @RequestBody(required = false) ReportParams params) {
        log.info("[报表管理] 生成消费报表，templateId={}, params={}", templateId, params);
        try {
            ResponseDTO<Map<String, Object>> result = consumeReportService.generateReport(templateId, params);
            return result;
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[报表管理] 生成消费报表参数错误，templateId={}, error={}", templateId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[报表管理] 生成消费报表业务异常，templateId={}, code={}, message={}", templateId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[报表管理] 生成消费报表系统异常，templateId={}, code={}, message={}", templateId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("GENERATE_REPORT_SYSTEM_ERROR", "生成消费报表失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[报表管理] 生成消费报表未知异常，templateId={}", templateId, e);
            return ResponseDTO.error("GENERATE_REPORT_ERROR", "生成消费报表失败: " + e.getMessage());
        }
    }

    /**
     * 导出报表
     * <p>
     * 根据模板ID和参数导出报表，支持多种导出格式（Excel、PDF、CSV）
     * 返回导出文件的下载路径或文件ID
     * </p>
     *
     * @param templateId 模板ID（必填）
     * @param params 报表参数（可选，包含时间范围、区域ID、用户ID等筛选条件）
     * @param exportFormat 导出格式（必填：EXCEL/PDF/CSV）
     * @return 导出文件路径或文件ID，用于下载
     * @apiNote 示例请求：
     * <pre>
     * POST /api/v1/consume/report/export?templateId=1&exportFormat=EXCEL
     * Body: {
     *   "startDate": "2025-01-01",
     *   "endDate": "2025-01-31"
     * }
     * </pre>
     */
    @PostMapping("/export")
    @Observed(name = "report.exportReport", contextualName = "report-export-report")
    @Operation(
        summary = "导出报表",
        description = "根据模板ID和参数导出报表，支持多种导出格式（Excel、PDF、CSV）。返回导出文件的下载路径或文件ID，用于下载。",
        tags = {"报表管理"}
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "导出成功，返回文件路径或文件ID",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = String.class)
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "导出格式不支持或参数错误"
    )
    @PermissionCheck(value = "CONSUME_ACCOUNT_MANAGE", description = "账户管理操作")
    public ResponseDTO<String> exportReport(
            @Parameter(description = "模板ID", required = true, example = "1")
            @RequestParam Long templateId,
            @Parameter(description = "报表参数（可选）")
            @RequestBody(required = false) ReportParams params,
            @Parameter(description = "导出格式（EXCEL/PDF/CSV）", required = true, example = "EXCEL")
            @RequestParam String exportFormat) {
        log.info("[报表管理] 导出报表，templateId={}, exportFormat={}, params={}",
                templateId, exportFormat, params);
        try {
            ResponseDTO<String> result = consumeReportService.exportReport(templateId, params, exportFormat);
            return result;
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[报表管理] 导出报表参数错误，templateId={}, exportFormat={}, error={}", templateId, exportFormat, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[报表管理] 导出报表业务异常，templateId={}, exportFormat={}, code={}, message={}", templateId, exportFormat, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[报表管理] 导出报表系统异常，templateId={}, exportFormat={}, code={}, message={}", templateId, exportFormat, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("EXPORT_REPORT_SYSTEM_ERROR", "导出报表失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[报表管理] 导出报表未知异常，templateId={}, exportFormat={}", templateId, exportFormat, e);
            return ResponseDTO.error("EXPORT_REPORT_ERROR", "导出报表失败: " + e.getMessage());
        }
    }

    /**
     * 获取报表模板列表
     *
     * @param templateType 模板类型（可选）
     * @return 模板列表
     */
    @GetMapping("/templates")
    @Observed(name = "report.getReportTemplates", contextualName = "report-get-report-templates")
    @Operation(summary = "获取报表模板列表", description = "获取报表模板列表，支持按类型筛选")
    @PermissionCheck(value = "CONSUME_ACCOUNT_MANAGE", description = "账户管理操作")
    public ResponseDTO<?> getReportTemplates(
            @RequestParam(required = false) String templateType) {
        log.info("[报表管理] 获取报表模板列表，templateType={}", templateType);
        try {
            ResponseDTO<?> result = consumeReportService.getReportTemplates(templateType);
            return result;
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[报表管理] 获取报表模板列表参数错误，templateType={}, error={}", templateType, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[报表管理] 获取报表模板列表业务异常，templateType={}, code={}, message={}", templateType, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[报表管理] 获取报表模板列表系统异常，templateType={}, code={}, message={}", templateType, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("GET_TEMPLATES_SYSTEM_ERROR", "获取报表模板列表失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[报表管理] 获取报表模板列表未知异常，templateType={}", templateType, e);
            return ResponseDTO.error("GET_TEMPLATES_ERROR", "获取报表模板列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取报表统计数据
     *
     * @param startTime 开始时间（ISO格式）
     * @param endTime 结束时间（ISO格式）
     * @param dimensions 统计维度（可选，JSON格式）
     * @return 统计数据
     */
    @PostMapping("/statistics")
    @Observed(name = "report.getReportStatistics", contextualName = "report-get-report-statistics")
    @Operation(summary = "获取报表统计数据", description = "获取指定时间范围和维度的报表统计数据")
    @PermissionCheck(value = "CONSUME_ACCOUNT_MANAGE", description = "账户管理操作")
    public ResponseDTO<Map<String, Object>> getReportStatistics(
            @Parameter(description = "开始时间，ISO格式：yyyy-MM-ddTHH:mm:ss")
            @RequestParam String startTime,
            @Parameter(description = "结束时间，ISO格式：yyyy-MM-ddTHH:mm:ss")
            @RequestParam String endTime,
            @RequestBody(required = false) Map<String, Object> dimensions) {
        log.info("[报表管理] 获取报表统计数据，startTime={}, endTime={}, dimensions={}",
                startTime, endTime, dimensions);
        try {
            java.time.LocalDateTime start = java.time.LocalDateTime.parse(startTime);
            java.time.LocalDateTime end = java.time.LocalDateTime.parse(endTime);
            ResponseDTO<Map<String, Object>> result =
                    consumeReportService.getReportStatistics(start, end, dimensions);
            return result;
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[报表管理] 获取报表统计数据参数错误，startTime={}, endTime={}, error={}", startTime, endTime, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[报表管理] 获取报表统计数据业务异常，startTime={}, endTime={}, code={}, message={}", startTime, endTime, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[报表管理] 获取报表统计数据系统异常，startTime={}, endTime={}, code={}, message={}", startTime, endTime, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("GET_STATISTICS_SYSTEM_ERROR", "获取报表统计数据失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[报表管理] 获取报表统计数据未知异常，startTime={}, endTime={}", startTime, endTime, e);
            return ResponseDTO.error("GET_STATISTICS_ERROR", "获取报表统计数据失败: " + e.getMessage());
        }
    }
}




