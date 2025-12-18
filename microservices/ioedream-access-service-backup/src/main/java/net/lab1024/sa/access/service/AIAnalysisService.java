package net.lab1024.sa.access.service;

import java.time.LocalDateTime;
import java.util.List;

import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * AI鏅鸿兘鍒嗘瀽鏈嶅姟鎺ュ彛
 * <p>
 * 鎻愪緵鍩轰簬浜哄伐鏅鸿兘鐨勯棬绂佸垎鏋愬姛鑳斤細
 * - 鐢ㄦ埛琛屼负妯″紡鍒嗘瀽
 * - 寮傚父璁块棶琛屼负妫€娴?
 * - 璁惧棰勬祴鎬х淮鎶?
 * - 瀹夊叏椋庨櫓璇勪及鍜岄璀?
 * - 璁块棶瓒嬪娍棰勬祴
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛?
 * - Service鎺ュ彛瀹氫箟鍦ㄤ笟鍔℃湇鍔℃ā鍧椾腑
 * - 娓呮櫚鐨勬柟娉曟敞閲?
 * - 缁熶竴鐨勬暟鎹紶杈撳璞?
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AIAnalysisService {

    /**
     * 鐢ㄦ埛琛屼负妯″紡鍒嗘瀽
     * <p>
     * 鍒嗘瀽鐢ㄦ埛鐨勯棬绂佽闂涓烘ā寮忥細
     * - 璁块棶鏃堕棿瑙勫緥
     * - 璁惧鍋忓ソ鍒嗘瀽
     * - 璁块棶璺緞妯″紡
     * - 寮傚父琛屼负璇嗗埆
     * </p>
     *
     * @param userId 鐢ㄦ埛ID
     * @param analysisDays 鍒嗘瀽澶╂暟锛堥粯璁?0澶╋級
     * @return 琛屼负妯″紡鍒嗘瀽缁撴灉
     */
    ResponseDTO<UserBehaviorPatternVO> analyzeUserBehaviorPattern(Long userId, Integer analysisDays);

    /**
     * 寮傚父璁块棶琛屼负妫€娴?
     * <p>
     * 瀹炴椂妫€娴嬬敤鎴峰拰璁惧鐨勫紓甯歌闂涓猴細
     * - 鏃堕棿寮傚父妫€娴?
     * - 鍦扮悊浣嶇疆寮傚父
     * - 棰戠巼寮傚父妫€娴?
     * - 澶氳澶囧苟鍙戝紓甯?
     * </p>
     *
     * @param request 妫€娴嬭姹?
     * @return 寮傚父妫€娴嬬粨鏋?
     */
    ResponseDTO<AnomalyDetectionResult> detectAnomalousAccessBehavior(AnomalyDetectionRequest request);

    /**
     * 璁惧棰勬祴鎬х淮鎶ゅ垎鏋?
     * <p>
     * 鍩轰簬璁惧鏁版嵁杩涜棰勬祴鎬х淮鎶ゅ垎鏋愶細
     * - 鏁呴殰棰勬祴妯″瀷
     - 缁存姢璁″垝寤鸿
     - 璁惧鍋ュ悍璇勫垎
     - 鏇存崲鎴愭湰浼扮畻
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param predictionDays 棰勬祴澶╂暟锛堥粯璁?0澶╋級
     * @return 棰勬祴鎬х淮鎶ゅ垎鏋愮粨鏋?
     */
    ResponseDTO<PredictiveMaintenanceResult> performPredictiveMaintenanceAnalysis(Long deviceId, Integer predictionDays);

    /**
     * 瀹夊叏椋庨櫓璇勪及鍜岄璀?
     * <p>
     * 缁煎悎璇勪及闂ㄧ绯荤粺鐨勫畨鍏ㄩ闄╋細
     * - 鐢ㄦ埛椋庨櫓璇勪及
     * - 璁惧瀹夊叏璇勫垎
     * - 鍖哄煙瀹夊叏鍒嗘瀽
     * - 濞佽儊鎯呮姤鍒嗘瀽
     * </p>
     *
     * @param request 璇勪及璇锋眰
     * @return 瀹夊叏椋庨櫓璇勪及缁撴灉
     */
    ResponseDTO<SecurityRiskAssessmentVO> assessSecurityRisk(SecurityRiskAssessmentRequest request);

    /**
     * 璁块棶瓒嬪娍棰勬祴
     * <p>
     * 鍩轰簬鍘嗗彶鏁版嵁棰勬祴璁块棶瓒嬪娍锛?
     * - 璁块棶閲忛娴?
     - 宄板€兼椂娈甸娴?
     - 璁惧璐熻浇棰勬祴
     - 瀹归噺瑙勫垝寤鸿
     * </p>
     *
     * @param request 棰勬祴璇锋眰
     * @return 瓒嬪娍棰勬祴缁撴灉
     */
    ResponseDTO<AccessTrendPredictionVO> predictAccessTrends(AccessTrendPredictionRequest request);

    /**
     * 鏅鸿兘璁块棶鎺у埗浼樺寲
     * <p>
     * 鍩轰簬AI鍒嗘瀽缁撴灉浼樺寲璁块棶鎺у埗绛栫暐锛?
     * - 鏉冮檺浼樺寲寤鸿
     - 璁块棶瑙勫垯璋冩暣
     - 椋庨櫓鍒嗙骇绠＄悊
     - 涓€у寲璁剧疆
     * </p>
     *
     * @param userId 鐢ㄦ埛ID
     * @return 璁块棶鎺у埗浼樺寲寤鸿
     */
    ResponseDTO<AccessControlOptimizationVO> optimizeAccessControl(Long userId);

    /**
     * 璁惧鍒╃敤鐜囧垎鏋?
     * <p>
     * 鍒嗘瀽璁惧鐨勪娇鐢ㄦ晥鐜囧拰鍒╃敤鐜囷細
     * - 浣跨敤棰戠巼缁熻
     * - 绌洪棽鏃舵鍒嗘瀽
     * - 璐熻浇鍧囪　寤鸿
     * - 璁惧閰嶇疆浼樺寲
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param analysisPeriod 鍒嗘瀽鍛ㄦ湡锛堝ぉ锛?
     * @return 璁惧鍒╃敤鐜囧垎鏋愮粨鏋?
     */
    ResponseDTO<DeviceUtilizationAnalysisVO> analyzeDeviceUtilization(Long deviceId, Integer analysisPeriod);

    /**
     * 鏅鸿兘寮傚父浜嬩欢澶勭悊
     * <p>
     * 鑷姩澶勭悊鍜屽搷搴斿紓甯镐簨浠讹細
     * - 寮傚父浜嬩欢鍒嗙被
     * - 鑷姩鍝嶅簲绛栫暐
     * - 閫氱煡瑙勫垯閰嶇疆
     * - 澶勭悊鏁堟灉璇勪及
     * </p>
     *
     * @param request 寮傚父浜嬩欢澶勭悊璇锋眰
     * @return 澶勭悊缁撴灉
     */
    ResponseDTO<AnomalyEventProcessingResult> processAnomalyEvent(AnomalyEventProcessingRequest request);

    /**
     * AI妯″瀷璁粌鍜屾洿鏂?
     * <p>
     * 璁粌鍜屾洿鏂癆I鍒嗘瀽妯″瀷锛?
     * - 琛屼负妯″紡妯″瀷璁粌
     * - 寮傚父妫€娴嬫ā鍨嬩紭鍖?
     * - 棰勬祴鍑嗙‘鎬ц瘎浼?
     * - 妯″瀷鐗堟湰绠＄悊
     * </p>
     *
     * @param modelType 妯″瀷绫诲瀷
     * @param trainingData 璁粌鏁版嵁闆?
     * @return 妯″瀷璁粌缁撴灉
     */
    ResponseDTO<AIModelTrainingResult> trainAndUpdateAIModel(String modelType, Object trainingData);

    /**
     * 鑾峰彇AI鍒嗘瀽鎶ュ憡
     * <p>
     * 鐢熸垚缁煎悎鐨凙I鍒嗘瀽鎶ュ憡锛?
     * - 绯荤粺鍋ュ悍鎶ュ憡
     * - 瀹夊叏鎬佸娍鍒嗘瀽
     * - 鎬ц兘浼樺寲寤鸿
     * - 瓒嬪娍鍒嗘瀽鎬荤粨
     * </p>
     *
     * @param request 鎶ュ憡璇锋眰
     * @return AI鍒嗘瀽鎶ュ憡
     */
    ResponseDTO<AIAnalysisReportVO> generateAIAnalysisReport(AIAnalysisReportRequest request);

    // ==================== 鍐呴儴鏁版嵁浼犺緭瀵硅薄 ====================

    /**
     * 鐢ㄦ埛琛屼负妯″紡鍒嗘瀽缁撴灉
     */
    class UserBehaviorPatternVO {
        private Long userId;
        private String userName;
        private String analysisPeriod;      // 鍒嗘瀽鍛ㄦ湡
        private AccessTimePattern accessTimePattern;  // 璁块棶鏃堕棿妯″紡
        private DevicePreferencePattern devicePreferencePattern;  // 璁惧鍋忓ソ妯″紡
        private AccessPathPattern accessPathPattern;  // 璁块棶璺緞妯″紡
        private List<BehaviorAnomaly> detectedAnomalies;  // 妫€娴嬪埌鐨勫紓甯?
        private String riskLevel;           // 椋庨櫓绛夌骇
        private List<String> recommendations;  // 寤鸿鎺柦
        private Double confidenceScore;     // 缃俊搴﹁瘎鍒?
        private String lastAnalysisTime;   // 鏈€鍚庡垎鏋愭椂闂?
    }

    /**
     * 璁块棶鏃堕棿妯″紡
     */
    class AccessTimePattern {
        private List<String> peakHours;    // 楂樺嘲鏃舵
        private List<String> quietHours;   // 浣庤胺鏃舵
        private Integer avgDailyAccess;     // 鏃ュ潎璁块棶娆℃暟
        private Integer weeklyPattern;      // 鍛ㄨ闂ā寮?
        private Boolean weekendAccess;     // 鍛ㄦ湯璁块棶
        private String timeVariability;    // 鏃堕棿鍙樺紓鎬?
    }

    /**
     * 璁惧鍋忓ソ妯″紡
     */
    class DevicePreferencePattern {
        private List<DeviceUsageRanking> deviceRankings;  // 璁惧浣跨敤鎺掑悕
        private String preferredDeviceType;  // 鍋忓ソ璁惧绫诲瀷
        private Integer deviceDiversity;     // 璁惧澶氭牱鎬?
        private String switchingPattern;     // 璁惧鍒囨崲妯″紡
    }

    /**
     * 璁块棶璺緞妯″紡
     */
    class AccessPathPattern {
        private List<String> frequentPaths;  // 棰戠箒璁块棶璺緞
        private String typicalPathLength;   // 鍏稿瀷璺緞闀垮害
        private List<String> unusualPaths;   // 寮傚父璺緞
        private Double pathEfficiency;       // 璺緞鏁堢巼
    }

    /**
     * 琛屼负寮傚父
     */
    class BehaviorAnomaly {
        private String anomalyType;        // 寮傚父绫诲瀷
        private String description;         // 鎻忚堪
        private LocalDateTime detectionTime; // 妫€娴嬫椂闂?
        private String severity;            // 涓ラ噸绋嬪害
        private Double anomalyScore;        // 寮傚父璇勫垎
        private List<String> relatedFactors; // 鐩稿叧鍥犵礌
    }

    /**
     * 寮傚父妫€娴嬬粨鏋?
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
     * 妫€娴嬪埌鐨勫紓甯?
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
     * 棰勬祴鎬х淮鎶ゅ垎鏋愮粨鏋?
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
     * 缁存姢寤鸿
     */
    class MaintenanceRecommendation {
        private String recommendationType;  // 寤鸿绫诲瀷
        private String description;         // 鎻忚堪
        private String urgency;             // 绱ф€ョ▼搴?
        private Integer estimatedCost;       // 浼扮畻鎴愭湰
        private String timeFrame;           // 鏃堕棿妗嗘灦
        private List<String> requiredActions; // 蹇呰鎿嶄綔
    }

    /**
     * 瀹夊叏椋庨櫓璇勪及璇锋眰
     */
    class SecurityRiskAssessmentRequest {
        private Long userId;
        private Long deviceId;
        private Long areaId;
        private String assessmentScope;     // 璇勪及鑼冨洿
        private List<String> riskFactors;  // 椋庨櫓鍥犵礌
        private String assessmentPeriod;   // 璇勪及鍛ㄦ湡
    }

    /**
     * 瀹夊叏椋庨櫓璇勪及缁撴灉
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
     * 瀹夊叏椋庨櫓鍥犵礌
     */
    class SecurityRiskFactor {
        private String factorName;         // 鍥犵礌鍚嶇О
        private String category;            // 绫诲埆
        private Double riskWeight;          // 椋庨櫓鏉冮噸
        private String description;         // 鎻忚堪
        private String currentStatus;       // 褰撳墠鐘舵€?
    }

    /**
     * 椋庨櫓缂撹В鎺柦
     */
    class RiskMitigationMeasure {
        private String measureName;        // 鎺柦鍚嶇О
        private String description;         // 鎻忚堪
        private String implementationTime;  // 瀹炴柦鏃堕棿
        private Integer priority;          // 浼樺厛绾?
        private String expectedEffect;     // 棰勬湡鏁堟灉
    }

    /**
     * 璁块棶瓒嬪娍棰勬祴缁撴灉
     */
    class AccessTrendPredictionVO {
        private String predictionPeriod;    // 棰勬祴鍛ㄦ湡
        private List<AccessTrendData> predictedTrends; // 棰勬祴瓒嬪娍
        private List<PeakPrediction> peakPredictions;   // 宄板€奸娴?
        private List<DeviceLoadPrediction> deviceLoadPredictions; // 璁惧璐熻浇棰勬祴
        private List<CapacityRecommendation> capacityRecommendations; // 瀹归噺寤鸿
        private Double predictionAccuracy;  // 棰勬祴鍑嗙‘鎬?
        private String modelVersion;        // 妯″瀷鐗堟湰
    }

    /**
     * 璁块棶瓒嬪娍鏁版嵁
     */
    class AccessTrendData {
        private String date;               // 鏃ユ湡
        private Integer predictedVolume;   // 棰勬祴璁块棶閲?
        private Integer actualVolume;     // 瀹為檯璁块棶閲?
        private Integer variance;         // 宸紓
        private String confidenceLevel;    // 缃俊搴?
    }

    /**
     * 宄板€奸娴?
     */
    class PeakPrediction {
        private String peakTime;          // 宄板€兼椂闂?
        private Integer predictedVolume;  // 棰勬祴璁块棶閲?
        private Integer duration;          // 鎸佺画鏃堕棿
        private List<String> affectedDevices; // 褰卞搷璁惧
    }

    /**
     * 璁惧璐熻浇棰勬祴
     */
    class DeviceLoadPrediction {
        private Long deviceId;
        private String deviceName;
        private String predictionPeriod;
        private Integer predictedLoad;     // 棰勬祴璐熻浇
        private Integer maxCapacity;       // 鏈€澶у閲?
        private Integer loadPercentage;    // 璐熻浇鐧惧垎姣?
        private List<String> optimizationSuggestions; // 浼樺寲寤鸿
    }

    /**
     * 瀹归噺寤鸿
     */
    class CapacityRecommendation {
        private String recommendationType;  // 寤鸿绫诲瀷
        private String description;         // 鎻忚堪
        private String targetDate;         // 鐩爣鏃ユ湡
        private Integer estimatedCost;     // 浼扮畻鎴愭湰
        private String impactLevel;        // 褰卞搷绾у埆
    }

    /**
     * 璁块棶鎺у埗浼樺寲寤鸿
     */
    class AccessControlOptimizationVO {
        private Long userId;
        private String analysisTime;
        private String currentPolicy;     // 褰撳墠绛栫暐
        private List<OptimizationSuggestion> suggestions; // 浼樺寲寤鸿
        private Integer potentialRiskReduction; // 娼滃湪椋庨櫓闄嶄綆
        private Double efficiencyImprovement; // 鏁堢巼鎻愬崌
        private List<String> implementationSteps; // 瀹炴柦姝ラ
    }

    /**
     * 浼樺寲寤鸿
     */
    class OptimizationSuggestion {
        private String suggestionType;     // 寤鸿绫诲瀷
        private String description;        // 鎻忚堪
        private String impactLevel;        // 褰卞搷绾у埆
        private String implementationComplexity; // 瀹炴柦澶嶆潅搴?
        private String expectedBenefit;    // 棰勬湡鏀剁泭
    }

    /**
     * 璁惧鍒╃敤鐜囧垎鏋愮粨鏋?
     */
    class DeviceUtilizationAnalysisVO {
        private Long deviceId;
        private String deviceName;
        private String analysisPeriod;
        private Integer totalOperatingHours;  // 鎬昏繍琛屾椂闀?
        private Integer activeHours;         // 娲昏穬鏃堕暱
        private Double utilizationRate;      // 鍒╃敤鐜?
        private List<UsageStatistics> hourlyUsage; // 灏忔椂浣跨敤缁熻
        private List<PeakUsagePeriod> peakPeriods; // 楂樺嘲鏃舵
        private List<OptimizationOpportunity> optimizationOpportunities; // 浼樺寲鏈轰細
        private Integer potentialImprovement; // 娼滃湪鏀硅繘
    }

    /**
     * 浣跨敤缁熻
     */
    class UsageStatistics {
        private String hour;               // 灏忔椂
        private Integer accessCount;       // 璁块棶娆℃暟
        private Integer averageResponseTime; // 骞冲潎鍝嶅簲鏃堕棿
        private Integer successRate;        // 鎴愬姛鐜?
    }

    /**
     * 楂樺嘲鏃舵
     */
    class PeakUsagePeriod {
        private String startTime;          // 寮€濮嬫椂闂?
        private String endTime;            // 缁撴潫鏃堕棿
        private Integer maxConcurrentAccess; // 鏈€澶у苟鍙戣闂?
        private Double loadFactor;        // 璐熻浇鍥犲瓙
    }

    /**
     * 浼樺寲鏈轰細
     */
    class OptimizationOpportunity {
        private String opportunityType;    // 鏈轰細绫诲瀷
        private String description;         // 鎻忚堪
        private Integer potentialGain;     // 娼滃湪鏀剁泭
        private String implementationCost;  // 瀹炴柦鎴愭湰
    }

    /**
     * 寮傚父浜嬩欢澶勭悊缁撴灉
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
     * AI妯″瀷璁粌缁撴灉
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
     * AI鍒嗘瀽鎶ュ憡
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
     * 绯荤粺鍋ュ悍鎸囨爣
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
     * 瀹夊叏鎬佸娍鍒嗘瀽
     */
    class SecurityPostureAnalysis {
        private Integer securityScore;
        private Integer threatLevel;
        private List<SecurityTrend> securityTrends;
        private List<String> identifiedVulnerabilities;
        private List<String> remediationActions;
    }

    /**
     * 鎬ц兘鍒嗘瀽
     */
    class PerformanceAnalysis {
        private Double averageResponseTime;
        private Integer throughput;
        private List<PerformanceMetric> metrics;
        private List<BottleneckAnalysis> bottlenecks;
    }

    /**
     * 瓒嬪娍鍒嗘瀽
     */
    class TrendAnalysis {
        private String metricName;
        private String trendDirection;
        private Double changeRate;
        private String trendPeriod;
        private List<String> influencingFactors;
    }
}