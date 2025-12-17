package net.lab1024.sa.common.dataanalysis.openapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.openapi.domain.response.PageResult;
import net.lab1024.sa.common.dataanalysis.openapi.domain.request.*;
import net.lab1024.sa.common.dataanalysis.openapi.domain.response.*;
import net.lab1024.sa.common.dataanalysis.openapi.service.DataAnalysisOpenApiService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 开放平台数据分析API控制器
 * 提供运营报表、趋势分析、智能预测等开放接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@RestController
@RequestMapping("/open/api/v1/data-analysis")
@RequiredArgsConstructor
@Tag(name = "开放平台数据分析API", description = "提供运营报表、趋势分析、智能预测等功能")
@Validated
public class DataAnalysisOpenApiController {

    private final DataAnalysisOpenApiService dataAnalysisOpenApiService;

    /**
     * 获取综合运营报表
     */
    @GetMapping("/reports/operations/comprehensive")
    @Operation(summary = "获取综合运营报表", description = "获取园区综合运营分析报表")
    public ResponseDTO<ComprehensiveOperationReportResponse> getComprehensiveOperationReport(
            @Parameter(description = "报表类型") @RequestParam(defaultValue = "daily") String reportType,
            @Parameter(description = "开始日期") @RequestParam String startDate,
            @Parameter(description = "结束日期") @RequestParam String endDate,
            @Parameter(description = "区域ID") @RequestParam(required = false) Long areaId,
            @Parameter(description = "是否包含对比数据") @RequestParam(defaultValue = "false") Boolean includeComparison,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 获取综合运营报表: reportType={}, startDate={}, endDate={}, areaId={}",
                reportType, startDate, endDate, areaId);

        ComprehensiveOperationReportResponse response = dataAnalysisOpenApiService.getComprehensiveOperationReport(
                reportType, startDate, endDate, areaId, includeComparison, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取用户访问统计报表
     */
    @GetMapping("/reports/user-access")
    @Operation(summary = "获取用户访问统计报表", description = "获取用户访问统计和趋势分析")
    public ResponseDTO<UserAccessReportResponse> getUserAccessReport(
            @Parameter(description = "统计维度") @RequestParam(defaultValue = "daily") String dimension,
            @Parameter(description = "开始日期") @RequestParam String startDate,
            @Parameter(description = "结束日期") @RequestParam String endDate,
            @Parameter(description = "用户类型") @RequestParam(required = false) String userType,
            @Parameter(description = "区域ID") @RequestParam(required = false) Long areaId,
            @Parameter(description = "是否包含详细信息") @RequestParam(defaultValue = "true") Boolean includeDetail,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);

        UserAccessQueryRequest queryRequest = UserAccessQueryRequest.builder()
                .dimension(dimension)
                .startDate(startDate)
                .endDate(endDate)
                .userType(userType)
                .areaId(areaId)
                .includeDetail(includeDetail)
                .build();

        log.info("[开放API] 获取用户访问报表: dimension={}, startDate={}, endDate={}, userType={}",
                dimension, startDate, endDate, userType);

        UserAccessReportResponse response = dataAnalysisOpenApiService.getUserAccessReport(queryRequest, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取设备使用统计报表
     */
    @GetMapping("/reports/device-usage")
    @Operation(summary = "获取设备使用统计报表", description = "获取设备使用率、故障率等统计信息")
    public ResponseDTO<DeviceUsageReportResponse> getDeviceUsageReport(
            @Parameter(description = "设备类型") @RequestParam(required = false) String deviceType,
            @Parameter(description = "开始日期") @RequestParam String startDate,
            @Parameter(description = "结束日期") @RequestParam String endDate,
            @Parameter(description = "区域ID") @RequestParam(required = false) Long areaId,
            @Parameter(description = "统计指标") @RequestParam(required = false) List<String> metrics,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);

        DeviceUsageQueryRequest queryRequest = DeviceUsageQueryRequest.builder()
                .deviceType(deviceType)
                .startDate(startDate)
                .endDate(endDate)
                .areaId(areaId)
                .metrics(metrics)
                .build();

        log.info("[开放API] 获取设备使用报表: deviceType={}, startDate={}, endDate={}, areaId={}",
                deviceType, startDate, endDate, areaId);

        DeviceUsageReportResponse response = dataAnalysisOpenApiService.getDeviceUsageReport(queryRequest, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取消费分析报表
     */
    @GetMapping("/reports/consumption-analysis")
    @Operation(summary = "获取消费分析报表", description = "获取消费行为分析和统计报表")
    public ResponseDTO<ConsumptionAnalysisReportResponse> getConsumptionAnalysisReport(
            @Parameter(description = "分析维度") @RequestParam(defaultValue = "daily") String dimension,
            @Parameter(description = "开始日期") @RequestParam String startDate,
            @Parameter(description = "结束日期") @RequestParam String endDate,
            @Parameter(description = "消费类型") @RequestParam(required = false) String consumptionType,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "部门ID") @RequestParam(required = false) Long departmentId,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);

        ConsumptionAnalysisQueryRequest queryRequest = ConsumptionAnalysisQueryRequest.builder()
                .dimension(dimension)
                .startDate(startDate)
                .endDate(endDate)
                .consumptionType(consumptionType)
                .userId(userId)
                .departmentId(departmentId)
                .build();

        log.info("[开放API] 获取消费分析报表: dimension={}, startDate={}, endDate={}, consumptionType={}",
                dimension, startDate, endDate, consumptionType);

        ConsumptionAnalysisReportResponse response = dataAnalysisOpenApiService.getConsumptionAnalysisReport(queryRequest, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取安全风险分析报表
     */
    @GetMapping("/reports/security-risk")
    @Operation(summary = "获取安全风险分析报表", description = "获取安全风险分析和预警信息")
    public ResponseDTO<SecurityRiskReportResponse> getSecurityRiskReport(
            @Parameter(description = "风险类型") @RequestParam(required = false) String riskType,
            @Parameter(description = "风险级别") @RequestParam(required = false) String riskLevel,
            @Parameter(description = "开始日期") @RequestParam String startDate,
            @Parameter(description = "结束日期") @RequestParam String endDate,
            @Parameter(description = "区域ID") @RequestParam(required = false) Long areaId,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);

        SecurityRiskQueryRequest queryRequest = SecurityRiskQueryRequest.builder()
                .riskType(riskType)
                .riskLevel(riskLevel)
                .startDate(startDate)
                .endDate(endDate)
                .areaId(areaId)
                .build();

        log.info("[开放API] 获取安全风险报表: riskType={}, riskLevel={}, startDate={}, endDate={}",
                riskType, riskLevel, startDate, endDate);

        SecurityRiskReportResponse response = dataAnalysisOpenApiService.getSecurityRiskReport(queryRequest, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取趋势分析数据
     */
    @PostMapping("/trends/analysis")
    @Operation(summary = "获取趋势分析数据", description = "获取指定指标的趋势分析数据")
    public ResponseDTO<TrendAnalysisResponse> getTrendAnalysis(
            @Valid @RequestBody TrendAnalysisRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 趋势分析: metrics={}, timeRange={}, analysisType={}, clientIp={}",
                request.getMetrics(), request.getTimeRange(), request.getAnalysisType(), clientIp);

        TrendAnalysisResponse response = dataAnalysisOpenApiService.getTrendAnalysis(request, token, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取异常检测结果
     */
    @PostMapping("/anomaly/detection")
    @Operation(summary = "获取异常检测结果", description = "AI异常检测和分析结果")
    public ResponseDTO<AnomalyDetectionResponse> detectAnomalies(
            @Valid @RequestBody AnomalyDetectionRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 异常检测: dataSource={}, detectionType={}, sensitivity={}, clientIp={}",
                request.getDataSource(), request.getDetectionType(), request.getSensitivity(), clientIp);

        AnomalyDetectionResponse response = dataAnalysisOpenApiService.detectAnomalies(request, token, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取预测分析结果
     */
    @PostMapping("/prediction/analysis")
    @Operation(summary = "获取预测分析结果", description = "AI智能预测和趋势预测")
    public ResponseDTO<PredictionAnalysisResponse> getPredictionAnalysis(
            @Valid @RequestBody PredictionAnalysisRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 预测分析: predictionType={}, targetField={}, predictionPeriod={}, clientIp={}",
                request.getPredictionType(), request.getTargetField(), request.getPredictionPeriod(), clientIp);

        PredictionAnalysisResponse response = dataAnalysisOpenApiService.getPredictionAnalysis(request, token, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取智能推荐建议
     */
    @PostMapping("/recommendations/intelligent")
    @Operation(summary = "获取智能推荐建议", description = "基于数据分析的智能推荐建议")
    public ResponseDTO<IntelligentRecommendationResponse> getIntelligentRecommendations(
            @Valid @RequestBody RecommendationRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 智能推荐: recommendationType={}, targetObject={}, context={}, clientIp={}",
                request.getRecommendationType(), request.getTargetObject(), request.getContext(), clientIp);

        IntelligentRecommendationResponse response = dataAnalysisOpenApiService.getIntelligentRecommendations(request, token, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取数据洞察报告
     */
    @PostMapping("/insights/generate")
    @Operation(summary = "获取数据洞察报告", description = "自动生成数据洞察分析报告")
    public ResponseDTO<DataInsightResponse> generateDataInsights(
            @Valid @RequestBody DataInsightRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 数据洞察生成: insightType={}, dataSource={}, analysisDepth={}, clientIp={}",
                request.getInsightType(), request.getDataSource(), request.getAnalysisDepth(), clientIp);

        DataInsightResponse response = dataAnalysisOpenApiService.generateDataInsights(request, token, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取实时监控仪表板数据
     */
    @GetMapping("/dashboard/realtime")
    @Operation(summary = "获取实时监控仪表板数据", description = "获取实时监控仪表板的关键指标数据")
    public ResponseDTO<RealtimeDashboardResponse> getRealtimeDashboard(
            @Parameter(description = "仪表板类型") @RequestParam(defaultValue = "comprehensive") String dashboardType,
            @Parameter(description = "刷新间隔(秒)") @RequestParam(defaultValue = "30") Integer refreshInterval,
            @Parameter(description = "区域ID") @RequestParam(required = false) Long areaId,
            @Parameter(description = "指标列表") @RequestParam(required = false) List<String> metrics,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);

        DashboardQueryRequest queryRequest = DashboardQueryRequest.builder()
                .dashboardType(dashboardType)
                .refreshInterval(refreshInterval)
                .areaId(areaId)
                .metrics(metrics)
                .build();

        log.info("[开放API] 获取实时仪表板: dashboardType={}, refreshInterval={}, areaId={}",
                dashboardType, refreshInterval, areaId);

        RealtimeDashboardResponse response = dataAnalysisOpenApiService.getRealtimeDashboard(queryRequest, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取自定义报表数据
     */
    @PostMapping("/reports/custom")
    @Operation(summary = "获取自定义报表数据", description = "根据自定义配置生成报表数据")
    public ResponseDTO<CustomReportResponse> getCustomReport(
            @Valid @RequestBody CustomReportRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 自定义报表: reportName={}, dataSource={}, dimensions={}, measures={}, clientIp={}",
                request.getReportName(), request.getDataSource(), request.getDimensions(), request.getMeasures(), clientIp);

        CustomReportResponse response = dataAnalysisOpenApiService.getCustomReport(request, token, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 导出报表数据
     */
    @PostMapping("/reports/export")
    @Operation(summary = "导出报表数据", description = "导出各类报表数据到指定格式")
    public ResponseDTO<ExportTaskResponse> exportReport(
            @Valid @RequestBody ExportRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 导出报表: reportType={}, exportFormat={}, exportRange={}, clientIp={}",
                request.getReportType(), request.getExportFormat(), request.getExportRange(), clientIp);

        ExportTaskResponse response = dataAnalysisOpenApiService.exportReport(request, token, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取数据质量评估报告
     */
    @GetMapping("/quality/assessment")
    @Operation(summary = "获取数据质量评估报告", description = "评估数据质量和完整性")
    public ResponseDTO<DataQualityAssessmentResponse> getDataQualityAssessment(
            @Parameter(description = "评估范围") @RequestParam(required = false) String assessmentScope,
            @Parameter(description = "数据源") @RequestParam(required = false) String dataSource,
            @Parameter(description = "评估日期") @RequestParam(required = false) String assessmentDate,
            @Parameter(description = "是否包含详细信息") @RequestParam(defaultValue = "false") Boolean includeDetail,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);

        DataQualityQueryRequest queryRequest = DataQualityQueryRequest.builder()
                .assessmentScope(assessmentScope)
                .dataSource(dataSource)
                .assessmentDate(assessmentDate)
                .includeDetail(includeDetail)
                .build();

        log.info("[开放API] 数据质量评估: assessmentScope={}, dataSource={}, assessmentDate={}",
                assessmentScope, dataSource, assessmentDate);

        DataQualityAssessmentResponse response = dataAnalysisOpenApiService.getDataQualityAssessment(queryRequest, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取分析任务列表
     */
    @GetMapping("/tasks/analysis")
    @Operation(summary = "获取分析任务列表", description = "获取数据分析任务列表和状态")
    public ResponseDTO<PageResult<AnalysisTaskResponse>> getAnalysisTasks(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "任务类型") @RequestParam(required = false) String taskType,
            @Parameter(description = "任务状态") @RequestParam(required = false) String taskStatus,
            @Parameter(description = "创建人") @RequestParam(required = false) String creator,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);

        AnalysisTaskQueryRequest queryRequest = AnalysisTaskQueryRequest.builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .taskType(taskType)
                .taskStatus(taskStatus)
                .creator(creator)
                .build();

        log.info("[开放API] 查询分析任务: taskType={}, taskStatus={}, creator={}",
                taskType, taskStatus, creator);

        PageResult<AnalysisTaskResponse> result = dataAnalysisOpenApiService.getAnalysisTasks(queryRequest, token);
        return ResponseDTO.ok(result);
    }

    /**
     * 从Authorization头中提取访问令牌
     */
    private String extractTokenFromAuthorization(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        throw new IllegalArgumentException("Invalid Authorization header format");
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
