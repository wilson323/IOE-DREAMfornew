package net.lab1024.sa.analytics.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import jakarta.servlet.http.HttpServletResponse;
import net.lab1024.sa.analytics.domain.vo.ReportGenerationRequest;
import net.lab1024.sa.analytics.domain.vo.ReportGenerationResult;
import net.lab1024.sa.common.domain.ResponseDTO;

/**
 * 报表生成服务接口
 * 提供多种格式的报表生成功能
 *
 * @author IOE-DREAM Team
 */
public interface ReportGenerationService {

    /**
     * 生成Excel报表
     *
     * @param request 报表生成请求
     * @return 报表生成结果
     */
    ResponseDTO<ReportGenerationResult> generateExcelReport(ReportGenerationRequest request);

    /**
     * 生成PDF报表
     *
     * @param request 报表生成请求
     * @return 报表生成结果
     */
    ResponseDTO<ReportGenerationResult> generatePdfReport(ReportGenerationRequest request);

    /**
     * 生成图表报表
     *
     * @param request 报表生成请求
     * @return 报表生成结果（图表Base64）
     */
    ResponseDTO<ReportGenerationResult> generateChartReport(ReportGenerationRequest request);

    /**
     * 异步生成报表
     *
     * @param request 报表生成请求
     * @return 异步任务ID
     */
    CompletableFuture<ResponseDTO<String>> generateReportAsync(ReportGenerationRequest request);

    /**
     * 获取报表生成状态
     *
     * @param taskId 任务ID
     * @return 生成状态
     */
    ResponseDTO<Map<String, Object>> getReportGenerationStatus(String taskId);

    /**
     * 下载报表文件
     *
     * @param fileId   文件ID
     * @param response HTTP响应
     */
    void downloadReportFile(String fileId, HttpServletResponse response);

    /**
     * 删除报表文件
     *
     * @param fileId 文件ID
     * @return 删除结果
     */
    ResponseDTO<Boolean> deleteReportFile(String fileId);

    /**
     * 获取报表模板列表
     *
     * @return 报表模板列表
     */
    ResponseDTO<List<Map<String, Object>>> getReportTemplates();

    /**
     * 保存报表模板
     *
     * @param templateData 模板数据
     * @return 保存结果
     */
    ResponseDTO<String> saveReportTemplate(Map<String, Object> templateData);

    /**
     * 预览报表数据
     *
     * @param request 报表请求
     * @return 预览数据
     */
    ResponseDTO<Map<String, Object>> previewReportData(ReportGenerationRequest request);

    /**
     * 导出报表数据
     *
     * @param request 导出请求
     * @param format  导出格式（json, csv, xml）
     * @return 导出结果
     */
    ResponseDTO<String> exportReportData(ReportGenerationRequest request, String format);

    /**
     * 获取报表统计数据
     *
     * @param templateId 模板ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 统计数据
     */
    ResponseDTO<Map<String, Object>> getReportStatistics(String templateId,
            LocalDateTime startTime,
            LocalDateTime endTime);

    /**
     * 批量生成报表
     *
     * @param requests 批量请求
     * @return 批量结果
     */
    ResponseDTO<List<ReportGenerationResult>> batchGenerateReports(List<ReportGenerationRequest> requests);

    /**
     * 调度报表生成
     *
     * @param templateId     模板ID
     * @param scheduleConfig 调度配置
     * @return 调度ID
     */
    ResponseDTO<String> scheduleReportGeneration(String templateId, Map<String, Object> scheduleConfig);

    /**
     * 取消调度任务
     *
     * @param scheduleId 调度ID
     * @return 取消结果
     */
    ResponseDTO<Boolean> cancelScheduledReport(String scheduleId);

    /**
     * 获取调度任务列表
     *
     * @return 调度任务列表
     */
    ResponseDTO<List<Map<String, Object>>> getScheduledReports();

    /**
     * 复制报表模板
     *
     * @param templateId      原模板ID
     * @param newTemplateName 新模板名称
     * @return 新模板ID
     */
    ResponseDTO<String> copyReportTemplate(String templateId, String newTemplateName);

    /**
     * 验证报表模板
     *
     * @param templateData 模板数据
     * @return 验证结果
     */
    ResponseDTO<Map<String, Object>> validateReportTemplate(Map<String, Object> templateData);

    /**
     * 获取报表执行日志
     *
     * @param templateId 模板ID
     * @param pageNum    页码
     * @param pageSize   页大小
     * @return 执行日志
     */
    ResponseDTO<Map<String, Object>> getReportExecutionLogs(String templateId,
            Integer pageNum,
            Integer pageSize);
}
