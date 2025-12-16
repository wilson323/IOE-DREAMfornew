package net.lab1024.sa.access.service;

import java.time.LocalDateTime;
import java.util.List;

import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * AI智能分析服务接口
 * <p>
 * 提供基于人工智能的门禁分析功能：
 * - 用户行为模式分析
 * - 异常访问行为检测
 * - 设备预测性维护
 * - 安全风险评估和预警
 * - 访问趋势预测
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义在业务服务模块中
 * - 清晰的方法注释
 * - 统一的数据传输对象
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AIAnalysisService {

    /**
     * 用户行为模式分析
     * <p>
     * 分析用户的门禁访问行为模式：
     * - 访问时间规律
     * - 设备偏好分析
     * - 访问路径模式
     * - 异常行为识别
     * </p>
     *
     * @param userId 用户ID
     * @param analysisDays 分析天数（默认30天）
     * @return 行为模式分析结果
     */
    ResponseDTO<UserBehaviorPatternVO> analyzeUserBehaviorPattern(Long userId, Integer analysisDays);

    /**
     * 异常访问行为检测
     * <p>
     * 实时检测用户和设备的异常访问行为：
     * - 时间异常检测
     * - 地理位置异常
     * - 频率异常检测
     * - 多设备并发异常
     * </p>
     *
     * @param request 检测请求
     * @return 异常检测结果
     */
    ResponseDTO<AnomalyDetectionResult> detectAnomalousAccessBehavior(AnomalyDetectionRequest request);

    /**
     * 设备预测性维护分析
     * <p>
     * 基于设备数据进行预测性维护分析：
     * - 故障预测模型
     - 维护计划建议
     - 设备健康评分
     - 更换成本估算
     * </p>
     *
     * @param deviceId 设备ID
     * @param predictionDays 预测天数（默认90天）
     * @return 预测性维护分析结果
     */
    ResponseDTO<PredictiveMaintenanceResult> performPredictiveMaintenanceAnalysis(Long deviceId, Integer predictionDays);

    /**
     * 安全风险评估和预警
     * <p>
     * 综合评估门禁系统的安全风险：
     * - 用户风险评估
     * - 设备安全评分
     * - 区域安全分析
     * - 威胁情报分析
     * </p>
     *
     * @param request 评估请求
     * @return 安全风险评估结果
     */
    ResponseDTO<SecurityRiskAssessmentVO> assessSecurityRisk(SecurityRiskAssessmentRequest request);

    /**
     * 访问趋势预测
     * <p>
     * 基于历史数据预测访问趋势：
     * - 访问量预测
     - 峰值时段预测
     - 设备负载预测
     - 容量规划建议
     * </p>
     *
     * @param request 预测请求
     * @return 趋势预测结果
     */
    ResponseDTO<AccessTrendPredictionVO> predictAccessTrends(AccessTrendPredictionRequest request);

    /**
     * 智能访问控制优化
     * <p>
     * 基于AI分析结果优化访问控制策略：
     * - 权限优化建议
     - 访问规则调整
     - 风险分级管理
     - 个性化设置
     * </p>
     *
     * @param userId 用户ID
     * @return 访问控制优化建议
     */
    ResponseDTO<AccessControlOptimizationVO> optimizeAccessControl(Long userId);

    /**
     * 设备利用率分析
     * <p>
     * 分析设备的使用效率和利用率：
     * - 使用频率统计
     * - 空闲时段分析
     * - 负载均衡建议
     * - 设备配置优化
     * </p>
     *
     * @param deviceId 设备ID
     * @param analysisPeriod 分析周期（天）
     * @return 设备利用率分析结果
     */
    ResponseDTO<DeviceUtilizationAnalysisVO> analyzeDeviceUtilization(Long deviceId, Integer analysisPeriod);

    /**
     * 智能异常事件处理
     * <p>
     * 自动处理和响应异常事件：
     * - 异常事件分类
     * - 自动响应策略
     * - 通知规则配置
     * - 处理效果评估
     * </p>
     *
     * @param request 异常事件处理请求
     * @return 处理结果
     */
    ResponseDTO<AnomalyEventProcessingResult> processAnomalyEvent(AnomalyEventProcessingRequest request);

    /**
     * AI模型训练和更新
     * <p>
     * 训练和更新AI分析模型：
     * - 行为模式模型训练
     * - 异常检测模型优化
     * - 预测准确性评估
     * - 模型版本管理
     * </p>
     *
     * @param modelType 模型类型
     * @param trainingData 训练数据集
     * @return 模型训练结果
     */
    ResponseDTO<AIModelTrainingResult> trainAndUpdateAIModel(String modelType, Object trainingData);

    /**
     * 获取AI分析报告
     * <p>
     * 生成综合的AI分析报告：
     * - 系统健康报告
     * - 安全态势分析
     * - 性能优化建议
     * - 趋势分析总结
     * </p>
     *
     * @param request 报告请求
     * @return AI分析报告
     */
    ResponseDTO<AIAnalysisReportVO> generateAIAnalysisReport(AIAnalysisReportRequest request);

    // ==================== 内部数据传输对象 ====================

    /**
     * 用户行为模式分析结果
     */
    class UserBehaviorPatternVO {
        private Long userId;
        private String userName;
        private String analysisPeriod;      // 分析周期
        private AccessTimePattern accessTimePattern;  // 访问时间模式
        private DevicePreferencePattern devicePreferencePattern;  // 设备偏好模式
        private AccessPathPattern accessPathPattern;  // 访问路径模式
        private List<BehaviorAnomaly> detectedAnomalies;  // 检测到的异常
        private String riskLevel;           // 风险等级
        private List<String> recommendations;  // 建议措施
        private Double confidenceScore;     // 置信度评分
        private String lastAnalysisTime;   // 最后分析时间
    }

    /**
     * 访问时间模式
     */
    class AccessTimePattern {
        private List<String> peakHours;    // 高峰时段
        private List<String> quietHours;   // 低谷时段
        private Integer avgDailyAccess;     // 日均访问次数
        private Integer weeklyPattern;      // 周访问模式
        private Boolean weekendAccess;     // 周末访问
        private String timeVariability;    // 时间变异性
    }

    /**
     * 设备偏好模式
     */
    class DevicePreferencePattern {
        private List<DeviceUsageRanking> deviceRankings;  // 设备使用排名
        private String preferredDeviceType;  // 偏好设备类型
        private Integer deviceDiversity;     // 设备多样性
        private String switchingPattern;     // 设备切换模式
    }

    /**
     * 访问路径模式
     */
    class AccessPathPattern {
        private List<String> frequentPaths;  // 频繁访问路径
        private String typicalPathLength;   // 典型路径长度
        private List<String> unusualPaths;   // 异常路径
        private Double pathEfficiency;       // 路径效率
    }

    /**
     * 行为异常
     */
    class BehaviorAnomaly {
        private String anomalyType;        // 异常类型
        private String description;         // 描述
        private LocalDateTime detectionTime; // 检测时间
        private String severity;            // 严重程度
        private Double anomalyScore;        // 异常评分
        private List<String> relatedFactors; // 相关因素
    }

    /**
     * 异常检测结果
     */
    class AnomalyDetectionResult {
        private Long userId;
        private Long deviceId;
        private String detectionTime;
        private List<DetectedAnomaly> anomalies;
        private Integer totalAnomalyCount;
        private Integer highRiskAnomalies;
        private String overallRiskLevel;
        private Boolean requiresAction;
        private List<String> recommendedActions;
    }

    /**
     * 检测到的异常
     */
    class DetectedAnomaly {
        private String anomalyId;
        private String anomalyType;
        private String description;
        private Double anomalyScore;
        private String severity;
        private LocalDateTime detectionTime;
        private String contextualInfo;
    }

    /**
     * 预测性维护分析结果
     */
    class PredictiveMaintenanceResult {
        private Long deviceId;
        private String deviceName;
        private String analysisDate;
        private Integer currentHealthScore;
        private Integer predictedHealthScore;
        private String maintenancePriority;
        private List<MaintenanceRecommendation> recommendations;
        private Integer estimatedFailureDays;
        private Double replacementProbability;
        private List<String> criticalComponents;
    }

    /**
     * 维护建议
     */
    class MaintenanceRecommendation {
        private String recommendationType;  // 建议类型
        private String description;         // 描述
        private String urgency;             // 紧急程度
        private Integer estimatedCost;       // 估算成本
        private String timeFrame;           // 时间框架
        private List<String> requiredActions; // 必要操作
    }

    /**
     * 安全风险评估请求
     */
    class SecurityRiskAssessmentRequest {
        private Long userId;
        private Long deviceId;
        private Long areaId;
        private String assessmentScope;     // 评估范围
        private List<String> riskFactors;  // 风险因素
        private String assessmentPeriod;   // 评估周期
    }

    /**
     * 安全风险评估结果
     */
    class SecurityRiskAssessmentVO {
        private Long userId;
        private Long deviceId;
        private Long areaId;
        private String assessmentTime;
        private Integer overallRiskScore;
        private String riskLevel;
        private List<SecurityRiskFactor> riskFactors;
        private List<RiskMitigationMeasure> mitigationMeasures;
        private String recommendationLevel;
        private List<String> immediateActions;
    }

    /**
     * 安全风险因素
     */
    class SecurityRiskFactor {
        private String factorName;         // 因素名称
        private String category;            // 类别
        private Double riskWeight;          // 风险权重
        private String description;         // 描述
        private String currentStatus;       // 当前状态
    }

    /**
     * 风险缓解措施
     */
    class RiskMitigationMeasure {
        private String measureName;        // 措施名称
        private String description;         // 描述
        private String implementationTime;  // 实施时间
        private Integer priority;          // 优先级
        private String expectedEffect;     // 预期效果
    }

    /**
     * 访问趋势预测结果
     */
    class AccessTrendPredictionVO {
        private String predictionPeriod;    // 预测周期
        private List<AccessTrendData> predictedTrends; // 预测趋势
        private List<PeakPrediction> peakPredictions;   // 峰值预测
        private List<DeviceLoadPrediction> deviceLoadPredictions; // 设备负载预测
        private List<CapacityRecommendation> capacityRecommendations; // 容量建议
        private Double predictionAccuracy;  // 预测准确性
        private String modelVersion;        // 模型版本
    }

    /**
     * 访问趋势数据
     */
    class AccessTrendData {
        private String date;               // 日期
        private Integer predictedVolume;   // 预测访问量
        private Integer actualVolume;     // 实际访问量
        private Integer variance;         // 差异
        private String confidenceLevel;    // 置信度
    }

    /**
     * 峰值预测
     */
    class PeakPrediction {
        private String peakTime;          // 峰值时间
        private Integer predictedVolume;  // 预测访问量
        private Integer duration;          // 持续时间
        private List<String> affectedDevices; // 影响设备
    }

    /**
     * 设备负载预测
     */
    class DeviceLoadPrediction {
        private Long deviceId;
        private String deviceName;
        private String predictionPeriod;
        private Integer predictedLoad;     // 预测负载
        private Integer maxCapacity;       // 最大容量
        private Integer loadPercentage;    // 负载百分比
        private List<String> optimizationSuggestions; // 优化建议
    }

    /**
     * 容量建议
     */
    class CapacityRecommendation {
        private String recommendationType;  // 建议类型
        private String description;         // 描述
        private String targetDate;         // 目标日期
        private Integer estimatedCost;     // 估算成本
        private String impactLevel;        // 影响级别
    }

    /**
     * 访问控制优化建议
     */
    class AccessControlOptimizationVO {
        private Long userId;
        private String analysisTime;
        private String currentPolicy;     // 当前策略
        private List<OptimizationSuggestion> suggestions; // 优化建议
        private Integer potentialRiskReduction; // 潜在风险降低
        private Double efficiencyImprovement; // 效率提升
        private List<String> implementationSteps; // 实施步骤
    }

    /**
     * 优化建议
     */
    class OptimizationSuggestion {
        private String suggestionType;     // 建议类型
        private String description;        // 描述
        private String impactLevel;        // 影响级别
        private String implementationComplexity; // 实施复杂度
        private String expectedBenefit;    // 预期收益
    }

    /**
     * 设备利用率分析结果
     */
    class DeviceUtilizationAnalysisVO {
        private Long deviceId;
        private String deviceName;
        private String analysisPeriod;
        private Integer totalOperatingHours;  // 总运行时长
        private Integer activeHours;         // 活跃时长
        private Double utilizationRate;      // 利用率
        private List<UsageStatistics> hourlyUsage; // 小时使用统计
        private List<PeakUsagePeriod> peakPeriods; // 高峰时段
        private List<OptimizationOpportunity> optimizationOpportunities; // 优化机会
        private Integer potentialImprovement; // 潜在改进
    }

    /**
     * 使用统计
     */
    class UsageStatistics {
        private String hour;               // 小时
        private Integer accessCount;       // 访问次数
        private Integer averageResponseTime; // 平均响应时间
        private Integer successRate;        // 成功率
    }

    /**
     * 高峰时段
     */
    class PeakUsagePeriod {
        private String startTime;          // 开始时间
        private String endTime;            // 结束时间
        private Integer maxConcurrentAccess; // 最大并发访问
        private Double loadFactor;        // 负载因子
    }

    /**
     * 优化机会
     */
    class OptimizationOpportunity {
        private String opportunityType;    // 机会类型
        private String description;         // 描述
        private Integer potentialGain;     // 潜在收益
        private String implementationCost;  // 实施成本
    }

    /**
     * 异常事件处理结果
     */
    class AnomalyEventProcessingResult {
        private String eventId;
        private String eventType;
        private String processingTime;
        private Boolean automaticResponse;
        private String responseAction;
        private Boolean issueResolved;
        private String resolutionTime;
        private List<String> followUpActions;
    }

    /**
     * AI模型训练结果
     */
    class AIModelTrainingResult {
        private String modelType;
        private String modelVersion;
        private String trainingStartTime;
        private String trainingEndTime;
        private Integer trainingDataSize;
        private Double trainingAccuracy;
        private Double validationAccuracy;
        private Boolean trainingSuccessful;
        private List<String> performanceMetrics;
        private String modelStatus;
    }

    /**
     * AI分析报告
     */
    class AIAnalysisReportVO {
        private String reportId;
        private String reportType;
        private String generationTime;
        private String reportPeriod;
        private ExecutiveSummary executiveSummary;
        private SystemHealthMetrics systemHealth;
        private SecurityPostureAnalysis securityPosture;
        private PerformanceAnalysis performance;
        private List<TrendAnalysis> trends;
        private List<String> recommendations;
        private String nextReviewDate;
    }

    /**
     * 系统健康指标
     */
    class SystemHealthMetrics {
        private Integer overallHealthScore;
        private Integer deviceHealthScore;
        private Integer networkHealthScore;
        private Integer securityHealthScore;
        private List<String> healthWarnings;
        private List<String> criticalIssues;
    }

    /**
     * 安全态势分析
     */
    class SecurityPostureAnalysis {
        private Integer securityScore;
        private Integer threatLevel;
        private List<SecurityTrend> securityTrends;
        private List<String> identifiedVulnerabilities;
        private List<String> remediationActions;
    }

    /**
     * 性能分析
     */
    class PerformanceAnalysis {
        private Double averageResponseTime;
        private Integer throughput;
        private List<PerformanceMetric> metrics;
        private List<BottleneckAnalysis> bottlenecks;
    }

    /**
     * 趋势分析
     */
    class TrendAnalysis {
        private String metricName;
        private String trendDirection;
        private Double changeRate;
        private String trendPeriod;
        private List<String> influencingFactors;
    }
}