package net.lab1024.sa.common.dataanalysis.openapi.service;

import net.lab1024.sa.common.dataanalysis.openapi.domain.request.*;
import net.lab1024.sa.common.dataanalysis.openapi.domain.response.*;
import net.lab1024.sa.common.openapi.domain.response.PageResult;

/**
 * 数据分析开放API服务接口
 * 提供运营报表、趋势分析、智能预测等开放服务
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface DataAnalysisOpenApiService {

    /**
     * 获取综合运营报表
     *
     * @param reportType 报表类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param areaId 区域ID
     * @param includeComparison 是否包含对比数据
     * @param token 访问令牌
     * @return 综合运营报表
     */
    ComprehensiveOperationReportResponse getComprehensiveOperationReport(String reportType, String startDate,
                                                                        String endDate, Long areaId, Boolean includeComparison, String token);

    /**
     * 获取用户访问统计报表
     *
     * @param request 查询请求
     * @param token 访问令牌
     * @return 用户访问报表
     */
    UserAccessReportResponse getUserAccessReport(UserAccessQueryRequest request, String token);

    /**
     * 获取设备使用统计报表
     *
     * @param request 查询请求
     * @param token 访问令牌
     * @return 设备使用报表
     */
    DeviceUsageReportResponse getDeviceUsageReport(DeviceUsageQueryRequest request, String token);

    /**
     * 获取消费分析报表
     *
     * @param request 查询请求
     * @param token 访问令牌
     * @return 消费分析报表
     */
    ConsumptionAnalysisReportResponse getConsumptionAnalysisReport(ConsumptionAnalysisQueryRequest request, String token);

    /**
     * 获取安全风险分析报表
     *
     * @param request 查询请求
     * @param token 访问令牌
     * @return 安全风险报表
     */
    SecurityRiskReportResponse getSecurityRiskReport(SecurityRiskQueryRequest request, String token);

    /**
     * 获取趋势分析数据
     *
     * @param request 趋势分析请求
     * @param token 访问令牌
     * @param clientIp 客户端IP
     * @return 趋势分析结果
     */
    TrendAnalysisResponse getTrendAnalysis(TrendAnalysisRequest request, String token, String clientIp);

    /**
     * 获取异常检测结果
     *
     * @param request 异常检测请求
     * @param token 访问令牌
     * @param clientIp 客户端IP
     * @return 异常检测结果
     */
    AnomalyDetectionResponse detectAnomalies(AnomalyDetectionRequest request, String token, String clientIp);

    /**
     * 获取预测分析结果
     *
     * @param request 预测分析请求
     * @param token 访问令牌
     * @param clientIp 客户端IP
     * @return 预测分析结果
     */
    PredictionAnalysisResponse getPredictionAnalysis(PredictionAnalysisRequest request, String token, String clientIp);

    /**
     * 获取智能推荐建议
     *
     * @param request 推荐请求
     * @param token 访问令牌
     * @param clientIp 客户端IP
     * @return 智能推荐结果
     */
    IntelligentRecommendationResponse getIntelligentRecommendations(RecommendationRequest request, String token, String clientIp);

    /**
     * 获取数据洞察报告
     *
     * @param request 数据洞察请求
     * @param token 访问令牌
     * @param clientIp 客户端IP
     * @return 数据洞察报告
     */
    DataInsightResponse generateDataInsights(DataInsightRequest request, String token, String clientIp);

    /**
     * 获取实时监控仪表板数据
     *
     * @param request 仪表板查询请求
     * @param token 访问令牌
     * @return 实时仪表板数据
     */
    RealtimeDashboardResponse getRealtimeDashboard(DashboardQueryRequest request, String token);

    /**
     * 获取自定义报表数据
     *
     * @param request 自定义报表请求
     * @param token 访问令牌
     * @param clientIp 客户端IP
     * @return 自定义报表数据
     */
    CustomReportResponse getCustomReport(CustomReportRequest request, String token, String clientIp);

    /**
     * 导出报表数据
     *
     * @param request 导出请求
     * @param token 访问令牌
     * @param clientIp 客户端IP
     * @return 导出任务响应
     */
    ExportTaskResponse exportReport(ExportRequest request, String token, String clientIp);

    /**
     * 获取数据质量评估报告
     *
     * @param request 数据质量查询请求
     * @param token 访问令牌
     * @return 数据质量评估报告
     */
    DataQualityAssessmentResponse getDataQualityAssessment(DataQualityQueryRequest request, String token);

    /**
     * 获取分析任务列表
     *
     * @param request 分析任务查询请求
     * @param token 访问令牌
     * @return 分页分析任务列表
     */
    PageResult<AnalysisTaskResponse> getAnalysisTasks(AnalysisTaskQueryRequest request, String token);
}