package net.lab1024.sa.attendance.report;

import net.lab1024.sa.attendance.report.model.ReportTemplate;
import net.lab1024.sa.attendance.report.model.request.AnnualReportRequest;
import net.lab1024.sa.attendance.report.model.request.AnomalyAnalysisReportRequest;
import net.lab1024.sa.attendance.report.model.request.AttendanceSummaryReportRequest;
import net.lab1024.sa.attendance.report.model.request.BatchReportGenerationRequest;
import net.lab1024.sa.attendance.report.model.request.DepartmentStatisticsReportRequest;
import net.lab1024.sa.attendance.report.model.request.EmployeeDetailReportRequest;
import net.lab1024.sa.attendance.report.model.request.MonthlyReportRequest;
import net.lab1024.sa.attendance.report.model.request.OvertimeStatisticsReportRequest;
import net.lab1024.sa.attendance.report.model.request.ReportCacheCleanupRequest;
import net.lab1024.sa.attendance.report.model.request.ReportCacheStatusRequest;
import net.lab1024.sa.attendance.report.model.request.ReportConfigurationRecommendationRequest;
import net.lab1024.sa.attendance.report.model.request.ReportExportRequest;
import net.lab1024.sa.attendance.report.model.request.ReportParameterValidationRequest;
import net.lab1024.sa.attendance.report.model.request.ReportPreviewRequest;
import net.lab1024.sa.attendance.report.model.request.ReportQueryParam;
import net.lab1024.sa.attendance.report.model.request.ReportStatisticsRequest;
import net.lab1024.sa.attendance.report.model.request.ReportTemplateQueryParam;
import net.lab1024.sa.attendance.report.model.request.TrendAnalysisReportRequest;
import net.lab1024.sa.attendance.report.model.result.AnnualReportResult;
import net.lab1024.sa.attendance.report.model.result.AnomalyAnalysisReportResult;
import net.lab1024.sa.attendance.report.model.result.AttendanceSummaryReportResult;
import net.lab1024.sa.attendance.report.model.result.BatchReportGenerationResult;
import net.lab1024.sa.attendance.report.model.result.DepartmentStatisticsReportResult;
import net.lab1024.sa.attendance.report.model.result.EmployeeDetailReportResult;
import net.lab1024.sa.attendance.report.model.result.MonthlyReportResult;
import net.lab1024.sa.attendance.report.model.result.OvertimeStatisticsReportResult;
import net.lab1024.sa.attendance.report.model.result.ReportCacheCleanupResult;
import net.lab1024.sa.attendance.report.model.result.ReportCacheStatusResult;
import net.lab1024.sa.attendance.report.model.result.ReportCancellationResult;
import net.lab1024.sa.attendance.report.model.result.ReportConfigurationRecommendation;
import net.lab1024.sa.attendance.report.model.result.ReportDetailResult;
import net.lab1024.sa.attendance.report.model.result.ReportExportResult;
import net.lab1024.sa.attendance.report.model.result.ReportGenerationProgress;
import net.lab1024.sa.attendance.report.model.result.ReportListResult;
import net.lab1024.sa.attendance.report.model.result.ReportParameterValidationResult;
import net.lab1024.sa.attendance.report.model.result.ReportPreviewResult;
import net.lab1024.sa.attendance.report.model.result.ReportStatisticsResult;
import net.lab1024.sa.attendance.report.model.result.ReportTemplateDeleteResult;
import net.lab1024.sa.attendance.report.model.result.ReportTemplateListResult;
import net.lab1024.sa.attendance.report.model.result.ReportTemplateSaveResult;
import net.lab1024.sa.attendance.report.model.result.TrendAnalysisReportResult;
import java.util.concurrent.CompletableFuture;

/**
 * 考勤报表服务接口
 * <p>
 * 定义考勤报表生成和查询的标准接口
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface AttendanceReportService {

    /**
     * 生成考勤汇总报表
     *
     * @param request 报表生成请求
     * @return 报表生成结果
     */
    CompletableFuture<AttendanceSummaryReportResult> generateSummaryReport(AttendanceSummaryReportRequest request);

    /**
     * 生成员工考勤明细报表
     *
     * @param request 报表生成请求
     * @return 报表生成结果
     */
    CompletableFuture<EmployeeDetailReportResult> generateEmployeeDetailReport(EmployeeDetailReportRequest request);

    /**
     * 生成部门考勤统计报表
     *
     * @param request 报表生成请求
     * @return 报表生成结果
     */
    CompletableFuture<DepartmentStatisticsReportResult> generateDepartmentStatisticsReport(DepartmentStatisticsReportRequest request);

    /**
     * 生成考勤异常分析报表
     *
     * @param request 报表生成请求
     * @return 报表生成结果
     */
    CompletableFuture<AnomalyAnalysisReportResult> generateAnomalyAnalysisReport(AnomalyAnalysisReportRequest request);

    /**
     * 生成加班统计报表
     *
     * @param request 报表生成请求
     * @return 报表生成结果
     */
    CompletableFuture<OvertimeStatisticsReportResult> generateOvertimeStatisticsReport(OvertimeStatisticsReportRequest request);

    /**
     * 生成考勤趋势分析报表
     *
     * @param request 报表生成请求
     * @return 报表生成结果
     */
    CompletableFuture<TrendAnalysisReportResult> generateTrendAnalysisReport(TrendAnalysisReportRequest request);

    /**
     * 生成考勤月报
     *
     * @param request 报表生成请求
     * @return 报表生成结果
     */
    CompletableFuture<MonthlyReportResult> generateMonthlyReport(MonthlyReportRequest request);

    /**
     * 生成考勤年报
     *
     * @param request 报表生成请求
     * @return 报表生成结果
     */
    CompletableFuture<AnnualReportResult> generateAnnualReport(AnnualReportRequest request);

    /**
     * 查询报表列表
     *
     * @param queryParam 查询参数
     * @return 报表列表
     */
    CompletableFuture<ReportListResult> getReportList(ReportQueryParam queryParam);

    /**
     * 获取报表详情
     *
     * @param reportId 报表ID
     * @return 报表详情
     */
    CompletableFuture<ReportDetailResult> getReportDetail(String reportId);

    /**
     * 导出报表
     *
     * @param exportRequest 导出请求
     * @return 导出结果
     */
    CompletableFuture<ReportExportResult> exportReport(ReportExportRequest exportRequest);

    /**
     * 预览报表数据
     *
     * @param previewRequest 预览请求
     * @return 预览结果
     */
    CompletableFuture<ReportPreviewResult> previewReport(ReportPreviewRequest previewRequest);

    /**
     * 获取报表模板列表
     *
     * @param templateQuery 模板查询参数
     * @return 模板列表
     */
    CompletableFuture<ReportTemplateListResult> getReportTemplates(ReportTemplateQueryParam templateQuery);

    /**
     * 保存报表模板
     *
     * @param template 报表模板
     * @return 保存结果
     */
    CompletableFuture<ReportTemplateSaveResult> saveReportTemplate(ReportTemplate template);

    /**
     * 删除报表模板
     *
     * @param templateId 模板ID
     * @return 删除结果
     */
    CompletableFuture<ReportTemplateDeleteResult> deleteReportTemplate(String templateId);

    /**
     * 获取报表生成进度
     *
     * @param taskId 任务ID
     * @return 生成进度
     */
    CompletableFuture<ReportGenerationProgress> getReportGenerationProgress(String taskId);

    /**
     * 取消报表生成
     *
     * @param taskId 任务ID
     * @return 取消结果
     */
    CompletableFuture<ReportCancellationResult> cancelReportGeneration(String taskId);

    /**
     * 获取报表统计数据
     *
     * @param statisticsRequest 统计请求
     * @return 统计数据
     */
    CompletableFuture<ReportStatisticsResult> getReportStatistics(ReportStatisticsRequest statisticsRequest);

    /**
     * 验证报表参数
     *
     * @param validationRequest 验证请求
     * @return 验证结果
     */
    CompletableFuture<ReportParameterValidationResult> validateReportParameters(ReportParameterValidationRequest validationRequest);

    /**
     * 获取推荐的报表配置
     *
     * @param recommendationRequest 推荐请求
     * @return 推荐结果
     */
    CompletableFuture<ReportConfigurationRecommendation> getRecommendedConfiguration(ReportConfigurationRecommendationRequest recommendationRequest);

    /**
     * 批量生成报表
     *
     * @param batchRequest 批量生成请求
     * @return 批量生成结果
     */
    CompletableFuture<BatchReportGenerationResult> generateBatchReports(BatchReportGenerationRequest batchRequest);

    /**
     * 获取报表缓存状态
     *
     * @param cacheRequest 缓存请求
     * @return 缓存状态
     */
    CompletableFuture<ReportCacheStatusResult> getReportCacheStatus(ReportCacheStatusRequest cacheRequest);

    /**
     * 清理报表缓存
     *
     * @param cleanupRequest 清理请求
     * @return 清理结果
     */
    CompletableFuture<ReportCacheCleanupResult> cleanupReportCache(ReportCacheCleanupRequest cleanupRequest);
}
