package net.lab1024.sa.report.engine;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.report.domain.entity.ReportTemplateEntity;
import net.lab1024.sa.report.domain.vo.ReportRequestVO;
import net.lab1024.sa.report.domain.vo.ReportResponseVO;
import net.lab1024.sa.report.service.ChartGeneratorService;
import net.lab1024.sa.report.service.ExportService;
import net.lab1024.sa.report.service.ReportDataService;
import net.lab1024.sa.report.service.ReportTemplateService;

/**
 * 报表引擎核心类
 * 提供完整的报表生成、数据处理、图表生成和导出功能
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportEngine {

    private final ReportTemplateService reportTemplateService;
    private final ReportDataService reportDataService;
    private final ChartGeneratorService chartGeneratorService;
    private final ExportService exportService;

    /**
     * 生成报表数据
     */
    @Cacheable(value = "report-data", key = "#request.templateId + '_' + #request.params.hashCode()")
    public ReportResponseVO generateReport(ReportRequestVO request) {
        log.info("开始生成报表，模板ID: {}, 参数: {}", request.getTemplateId(), request.getParams());

        try {
            // 1. 获取报表模板
            ReportTemplateEntity template = reportTemplateService.getById(request.getTemplateId());
            if (template == null) {
                return ReportResponseVO.error("报表模板不存在");
            }

            // 2. 获取报表数据
            Map<String, Object> data = reportDataService.getReportData(template, request.getParams());

            // 3. 生成图表数据
            Map<String, Object> charts = null;
            if (request.getIncludeCharts() != null && request.getIncludeCharts()) {
                charts = chartGeneratorService.generateCharts(template, data);
            }

            // 4. 构建响应
            ReportResponseVO response = ReportResponseVO.success();
            response.setTemplateId(template.getTemplateId() != null ? template.getTemplateId().toString() : null);
            response.setTemplateName(template.getTemplateName());
            response.setData(data);
            response.setCharts(charts);
            response.setGeneratedTime(LocalDateTime.now());

            log.info("报表生成完成，模板ID: {}, 数据条数: {}", request.getTemplateId(),
                    data.get("totalCount") != null ? data.get("totalCount") : "未知");

            return response;

        } catch (Exception e) {
            log.error("报表生成失败，模板ID: {}", request.getTemplateId(), e);
            return ReportResponseVO.error("报表生成失败: " + e.getMessage());
        }
    }

    /**
     * 异步生成报表
     */
    public CompletableFuture<ReportResponseVO> generateReportAsync(ReportRequestVO request) {
        return CompletableFuture.supplyAsync(() -> generateReport(request));
    }

    /**
     * 批量生成报表
     */
    public Map<Long, ReportResponseVO> generateBatchReports(List<ReportRequestVO> requests) {
        Map<Long, ReportResponseVO> results = new HashMap<>();

        List<CompletableFuture<Void>> futures = requests.stream()
                .map(request -> CompletableFuture.runAsync(() -> {
                    ReportResponseVO response = generateReport(request);
                    results.put(request.getTemplateId(), response);
                }))
                .toList();

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return results;
    }

    /**
     * 导出报表
     */
    public ResponseDTO<String> exportReport(ReportRequestVO request, String exportType) {
        log.info("开始导出报表，模板ID: {}, 导出类型: {}", request.getTemplateId(), exportType);

        try {
            // 生成报表数据
            ReportResponseVO reportData = generateReport(request);
            if (!reportData.getSuccess()) {
                return ResponseDTO.error(reportData.getMessage());
            }

            // 导出报表
            String filePath = exportService.exportReport(reportData, exportType);

            log.info("报表导出完成，文件路径: {}", filePath);
            return ResponseDTO.ok(filePath);

        } catch (Exception e) {
            log.error("报表导出失败，模板ID: {}", request.getTemplateId(), e);
            return ResponseDTO.error("报表导出失败: " + e.getMessage());
        }
    }

    /**
     * 获取报表预览数据
     */
    @Cacheable(value = "report-preview", key = "#templateId + '_' + #params.hashCode()")
    public ReportResponseVO previewReport(Long templateId, Map<String, Object> params, Integer limit) {
        log.info("获取报表预览，模板ID: {}, 数据限制: {}", templateId, limit);

        try {
            ReportRequestVO request = new ReportRequestVO();
            request.setTemplateId(templateId);
            request.setParams(params);
            request.setIncludeCharts(false);

            // 设置数据限制
            if (limit != null && limit > 0) {
                params.put("limit", limit);
            }

            ReportResponseVO response = generateReport(request);

            // 标记为预览数据
            if (response.getSuccess()) {
                response.getData().put("isPreview", true);
                response.getData().put("previewLimit", limit);
            }

            return response;

        } catch (Exception e) {
            log.error("报表预览失败，模板ID: {}", templateId, e);
            return ReportResponseVO.error("报表预览失败: " + e.getMessage());
        }
    }

    /**
     * 验证报表模板
     */
    public ResponseDTO<Map<String, Object>> validateTemplate(Long templateId) {
        log.info("验证报表模板，模板ID: {}", templateId);

        try {
            ReportTemplateEntity template = reportTemplateService.getById(templateId);
            if (template == null) {
                return ResponseDTO.error("报表模板不存在");
            }

            Map<String, Object> validationResult = new HashMap<>();

            // 验证SQL语法
            if (template.getSqlQuery() != null && !template.getSqlQuery().trim().isEmpty()) {
                boolean sqlValid = reportDataService.validateSql(template.getSqlQuery());
                validationResult.put("sqlValid", sqlValid);
                if (!sqlValid) {
                    validationResult.put("sqlError", "SQL语法错误或无法执行");
                }
            }

            // 验证图表配置
            if (template.getChartConfig() != null && !template.getChartConfig().trim().isEmpty()) {
                boolean chartValid = chartGeneratorService.validateChartConfig(template.getChartConfig());
                validationResult.put("chartValid", chartValid);
                if (!chartValid) {
                    validationResult.put("chartError", "图表配置格式错误");
                }
            }

            // 验证参数配置
            boolean paramValid = validateParameters(template.getParameterConfig());
            validationResult.put("paramValid", paramValid);

            boolean overallValid = (boolean) validationResult.getOrDefault("sqlValid", true) &&
                    (boolean) validationResult.getOrDefault("chartValid", true) &&
                    paramValid;

            validationResult.put("overallValid", overallValid);
            validationResult.put("templateName", template.getTemplateName());

            return ResponseDTO.ok(validationResult);

        } catch (Exception e) {
            log.error("报表模板验证失败，模板ID: {}", templateId, e);
            return ResponseDTO.error("模板验证失败: " + e.getMessage());
        }
    }

    /**
     * 验证参数配置
     */
    private boolean validateParameters(String parameterConfig) {
        if (parameterConfig == null || parameterConfig.trim().isEmpty()) {
            return true; // 无参数配置是有效的
        }

        try {
            // 这里应该实现JSON格式验证
            // 简化实现，实际应该使用JSON解析器验证
            return parameterConfig.contains("{") && parameterConfig.contains("}");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取报表统计信息
     */
    @Cacheable(value = "report-stats", key = "'general'")
    public ResponseDTO<Map<String, Object>> getReportStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // 模板统计
            long templateCount = reportTemplateService.count();
            stats.put("templateCount", templateCount);

            // 今日生成报表数
            long todayReportCount = reportDataService.getTodayReportCount();
            stats.put("todayReportCount", todayReportCount);

            // 本月生成报表数
            long monthReportCount = reportDataService.getMonthReportCount();
            stats.put("monthReportCount", monthReportCount);

            // 最热门报表模板
            List<Map<String, Object>> popularTemplates = reportDataService.getPopularTemplates(10);
            stats.put("popularTemplates", popularTemplates);

            // 报表类型分布
            Map<String, Long> reportTypeDistribution = reportDataService.getReportTypeDistribution();
            stats.put("reportTypeDistribution", reportTypeDistribution);

            return ResponseDTO.ok(stats);

        } catch (Exception e) {
            log.error("获取报表统计信息失败", e);
            return ResponseDTO.error("获取统计信息失败: " + e.getMessage());
        }
    }
}
