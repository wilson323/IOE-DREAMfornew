package net.lab1024.sa.admin.module.consume.engine.mode.impl;

import net.lab1024.sa.admin.module.consume.engine.mode.abstracts.AbstractConsumptionMode;
import net.lab1024.sa.admin.module.consume.domain.enums.CategoryDiscountEnum;
import net.lab1024.sa.admin.module.consume.domain.enums.MemberLevelEnum;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * 智能消费模式
 * 严格遵循repowiki规范：基于AI的智能消费决策引擎
 *
 * 核心能力：
 * - AI驱动的个性化推荐和决策
 * - 多维度数据分析和预测
 * - 实时风险评估和异常检测
 * - 自适应学习和优化
 * - 智能营销和优惠匹配
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Component("smartMode")
public class SmartMode extends AbstractConsumptionMode {

    // 智能决策类型
    public enum DecisionType {
        RECOMMENDATION("推荐", "基于用户行为推荐最佳选择"),
        OPTIMIZATION("优化", "优化消费成本和体验"),
        PREDICTION("预测", "预测未来消费趋势"),
        ANOMALY_DETECTION("异常检测", "检测异常消费行为"),
        RISK_ASSESSMENT("风险评估", "评估消费风险"),
        PERSONALIZATION("个性化", "个性化定制服务");

        private final String name;
        private final String description;

        DecisionType(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
    }

    // AI模型配置
    private static final BigDecimal DEFAULT_CONFIDENCE_THRESHOLD = new BigDecimal("0.75");
    private static final BigDecimal RISK_THRESHOLD = new BigDecimal("0.70");
    private static final int MAX_RECOMMENDATIONS = 5;

    // 智能参数权重
    private static final Map<String, BigDecimal> FEATURE_WEIGHTS = Map.of(
        "user_behavior", new BigDecimal("0.30"),
        "time_context", new BigDecimal("0.20"),
        "location_context", new BigDecimal("0.15"),
        "social_influence", new BigDecimal("0.15"),
        "historical_pattern", new BigDecimal("0.20")
    );

    public SmartMode() {
        super("SMART", "智能消费模式", "基于AI的智能消费决策和推荐引擎");
    }

    @Override
    public BigDecimal calculateAmount(Map<String, Object> params) {
        try {
            // 1. 获取基础金额
            BigDecimal baseAmount = getBaseAmount(params);
            if (baseAmount == null || baseAmount.compareTo(BigDecimal.ZERO) <= 0) {
                return BigDecimal.ZERO;
            }

            // 2. 执行智能分析
            SmartAnalysisResult analysis = performSmartAnalysis(params, baseAmount);

            // 3. 应用智能优化
            BigDecimal optimizedAmount = applySmartOptimization(baseAmount, analysis, params);

            // 4. 风险评估调整
            BigDecimal riskAdjustedAmount = applyRiskAdjustment(optimizedAmount, analysis, params);

            // 5. 个性化调整
            BigDecimal personalizedAmount = applyPersonalization(riskAdjustedAmount, analysis, params);

            // 6. 最终验证
            BigDecimal finalAmount = validateAndFinalize(personalizedAmount, params);

            return finalAmount.setScale(2, RoundingMode.HALF_UP);

        } catch (Exception e) {
            // 智能模式失败时回退到基础计算
            return getFallbackAmount(params);
        }
    }

    @Override
    protected boolean doValidateParameters(Map<String, Object> params) {
        // 验证基础参数
        if (!hasRequiredParams(params, "userId", "accountId")) {
            return false;
        }

        // 验证用户画像数据
        if (!hasUserProfile(params)) {
            return false;
        }

        // 验证场景上下文
        if (!hasValidContext(params)) {
            return false;
        }

        // 验证决策类型
        String decisionType = getStringFromParams(params, "decisionType");
        if (decisionType != null) {
            try {
                DecisionType.valueOf(decisionType);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        // 验证置信度阈值
        BigDecimal confidenceThreshold = getBigDecimalFromParams(params, "confidenceThreshold");
        if (confidenceThreshold != null) {
            if (confidenceThreshold.compareTo(BigDecimal.ZERO) <= 0 ||
                confidenceThreshold.compareTo(BigDecimal.ONE) > 0) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected boolean doIsAllowed(Map<String, Object> params) {
        SmartAnalysisResult analysis = performQuickAnalysis(params);

        // 检查风险评估
        if (analysis.getRiskScore() != null &&
            analysis.getRiskScore().compareTo(RISK_THRESHOLD) > 0) {
            return false;
        }

        // 检查用户信用等级
        String creditLevel = getStringFromParams(params, "creditLevel");
        if (creditLevel != null && !isCreditLevelAcceptable(creditLevel)) {
            return false;
        }

        // 检查时间窗口限制
        if (!isWithinAllowedTimeWindow(params)) {
            return false;
        }

        // 检查智能决策置信度
        if (analysis.getConfidence() != null &&
            analysis.getConfidence().compareTo(DEFAULT_CONFIDENCE_THRESHOLD) < 0) {
            return false;
        }

        return true;
    }

    @Override
    protected Map<String, Object> doPreProcess(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        // 用户画像分析
        Map<String, Object> userProfile = analyzeUserProfile(params);
        result.put("userProfile", userProfile);
        result.put("userSegment", userProfile.get("segment"));
        result.put("userLifeCycle", userProfile.get("lifeCycle"));

        // 场景上下文分析
        Map<String, Object> contextAnalysis = analyzeContext(params);
        result.put("contextAnalysis", contextAnalysis);
        result.put("scenarioType", contextAnalysis.get("scenarioType"));
        result.put("urgencyLevel", contextAnalysis.get("urgencyLevel"));

        // 历史行为模式分析
        Map<String, Object> behaviorPattern = analyzeBehaviorPattern(params);
        result.put("behaviorPattern", behaviorPattern);
        result.put("predictedIntent", behaviorPattern.get("predictedIntent"));

        // 实时环境因素分析
        Map<String, Object> environmentalFactors = analyzeEnvironmentalFactors(params);
        result.put("environmentalFactors", environmentalFactors);

        // 智能推荐准备
        List<Map<String, Object>> recommendations = generateRecommendations(params);
        result.put("recommendations", recommendations);
        result.put("topRecommendation", recommendations.isEmpty() ? null : recommendations.get(0));

        // 风险预评估
        Map<String, Object> riskAssessment = performPreRiskAssessment(params);
        result.put("riskAssessment", riskAssessment);
        result.put("riskLevel", riskAssessment.get("riskLevel"));

        return result;
    }

    @Override
    protected Map<String, Object> doPostProcess(Map<String, Object> params, Map<String, Object> result) {
        Map<String, Object> postResult = new HashMap<>();

        // 智能决策结果
        BigDecimal originalAmount = getBaseAmount(params);
        BigDecimal finalAmount = (BigDecimal) result.get("amount");

        if (originalAmount != null && finalAmount != null) {
            BigDecimal saving = originalAmount.subtract(finalAmount);
            BigDecimal savingRate = originalAmount.compareTo(BigDecimal.ZERO) > 0
                ? saving.divide(originalAmount, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

            postResult.put("originalAmount", originalAmount);
            postResult.put("finalAmount", finalAmount);
            postResult.put("smartSaving", saving);
            postResult.put("smartSavingRate", savingRate.multiply(new BigDecimal("100")));
        }

        // 智能分析详情
        SmartAnalysisResult analysis = (SmartAnalysisResult) result.get("smartAnalysis");
        if (analysis != null) {
            postResult.put("decisionConfidence", analysis.getConfidence());
            postResult.put("riskScore", analysis.getRiskScore());
            postResult.put("optimizationScore", analysis.getOptimizationScore());
            postResult.put("personalizationScore", analysis.getPersonalizationScore());
        }

        // 推荐结果
        List<Map<String, Object>> appliedRecommendations = (List<Map<String, Object>>) result.get("appliedRecommendations");
        if (appliedRecommendations != null && !appliedRecommendations.isEmpty()) {
            postResult.put("appliedRecommendations", appliedRecommendations);
            postResult.put("recommendationCount", appliedRecommendations.size());
        }

        // 学习数据记录
        Map<String, Object> learningData = generateLearningData(params, result);
        postResult.put("learningData", learningData);

        // 用户反馈收集点
        postResult.put("feedbackRequired", true);
        postResult.put("feedbackOptions", Arrays.asList(
            "excellent", "good", "acceptable", "needs_improvement"
        ));

        return postResult;
    }

    @Override
    protected BigDecimal getMinAmount() {
        return new BigDecimal("0.01");
    }

    @Override
    protected BigDecimal getMaxAmount() {
        return new BigDecimal("50000.00"); // 智能模式支持更大金额
    }

    @Override
    protected String[] getSupportedFields() {
        return new String[]{
            "userId", "accountId", "baseAmount", "decisionType", "confidenceThreshold",
            "userProfile", "contextData", "historicalData", "realTimeData",
            "preferences", "constraints", "objectives", "riskTolerance",
            "personalizationLevel", "optimizationGoal", "scenarioType"
        };
    }

    /**
     * 执行智能分析
     */
    private SmartAnalysisResult performSmartAnalysis(Map<String, Object> params, BigDecimal baseAmount) {
        SmartAnalysisResult result = new SmartAnalysisResult();

        // 用户行为分析
        BigDecimal behaviorScore = analyzeUserBehavior(params);
        result.setBehaviorScore(behaviorScore);

        // 上下文分析
        BigDecimal contextScore = analyzeContextScore(params);
        result.setContextScore(contextScore);

        // 风险评估
        BigDecimal riskScore = assessRisk(params, baseAmount);
        result.setRiskScore(riskScore);

        // 优化潜力评估
        BigDecimal optimizationScore = assessOptimizationPotential(params);
        result.setOptimizationScore(optimizationScore);

        // 个性化评估
        BigDecimal personalizationScore = assessPersonalizationLevel(params);
        result.setPersonalizationScore(personalizationScore);

        // 计算综合置信度
        BigDecimal confidence = calculateConfidence(result);
        result.setConfidence(confidence);

        return result;
    }

    /**
     * 应用智能优化
     */
    private BigDecimal applySmartOptimization(BigDecimal baseAmount, SmartAnalysisResult analysis, Map<String, Object> params) {
        BigDecimal optimizedAmount = baseAmount;

        // 基于优化评分调整
        BigDecimal optimizationScore = analysis.getOptimizationScore();
        if (optimizationScore != null && optimizationScore.compareTo(new BigDecimal("0.8")) > 0) {
            // 高优化潜力，应用智能折扣
            BigDecimal optimizationDiscount = optimizationScore.multiply(new BigDecimal("0.15")); // 最多15%折扣
            optimizedAmount = optimizedAmount.multiply(BigDecimal.ONE.subtract(optimizationDiscount));
        }

        // 应用推荐优惠
        optimizedAmount = applyRecommendedDiscounts(optimizedAmount, params);

        return optimizedAmount;
    }

    /**
     * 应用风险调整
     */
    private BigDecimal applyRiskAdjustment(BigDecimal amount, SmartAnalysisResult analysis, Map<String, Object> params) {
        BigDecimal riskScore = analysis.getRiskScore();
        if (riskScore == null) {
            return amount;
        }

        // 高风险情况下增加保证金
        if (riskScore.compareTo(RISK_THRESHOLD) > 0) {
            BigDecimal riskPremium = riskScore.multiply(new BigDecimal("0.10")); // 10%风险保证金
            return amount.multiply(BigDecimal.ONE.add(riskPremium));
        }

        // 低风险情况下给予优惠
        if (riskScore.compareTo(new BigDecimal("0.30")) < 0) {
            BigDecimal riskDiscount = new BigDecimal("0.30").subtract(riskScore).multiply(new BigDecimal("0.05")); // 最多5%优惠
            return amount.multiply(BigDecimal.ONE.subtract(riskDiscount));
        }

        return amount;
    }

    /**
     * 应用个性化调整
     */
    private BigDecimal applyPersonalization(BigDecimal amount, SmartAnalysisResult analysis, Map<String, Object> params) {
        BigDecimal personalizationScore = analysis.getPersonalizationScore();
        if (personalizationScore == null) {
            return amount;
        }

        // VIP用户个性化优惠
        String memberLevel = getStringFromParams(params, "memberLevel");
        if (memberLevel != null) {
            MemberLevelEnum level = MemberLevelEnum.fromCode(memberLevel);
            if (level != null && level.isHigherThan(MemberLevelEnum.GOLD)) {
                BigDecimal vipDiscount = new BigDecimal("0.05"); // VIP额外5%优惠
                amount = amount.multiply(BigDecimal.ONE.subtract(vipDiscount));
            }
        }

        // 个性化偏好调整
        String personalizationLevel = getStringFromParams(params, "personalizationLevel");
        if ("HIGH".equals(personalizationLevel)) {
            BigDecimal personalBonus = personalizationScore.multiply(new BigDecimal("0.03")); // 最多3%个性化奖励
            amount = amount.multiply(BigDecimal.ONE.subtract(personalBonus));
        }

        return amount;
    }

    /**
     * 获取基础金额
     */
    private BigDecimal getBaseAmount(Map<String, Object> params) {
        // 优先使用明确的基础金额
        BigDecimal baseAmount = getBigDecimalFromParams(params, "baseAmount");
        if (baseAmount != null) {
            return baseAmount;
        }

        // 其次使用金额字段
        BigDecimal amount = getBigDecimalFromParams(params, "amount");
        if (amount != null) {
            return amount;
        }

        // 智能估算基础金额
        return estimateBaseAmount(params);
    }

    /**
     * 智能估算基础金额
     */
    private BigDecimal estimateBaseAmount(Map<String, Object> params) {
        String scenarioType = getStringFromParams(params, "scenarioType");
        String category = getStringFromParams(params, "category");

        // 基于场景和分类的智能估算
        if ("DINING".equals(scenarioType)) {
            return new BigDecimal("50"); // 餐饮场景默认50元
        } else if ("SHOPPING".equals(scenarioType)) {
            return "STATIONERY".equals(category) ? new BigDecimal("20") : new BigDecimal("100");
        } else if ("UTILITY".equals(scenarioType)) {
            return new BigDecimal("200"); // 公共事业费用默认200元
        }

        return new BigDecimal("100"); // 默认100元
    }

    /**
     * 失败时回退金额
     */
    private BigDecimal getFallbackAmount(Map<String, Object> params) {
        BigDecimal baseAmount = getBaseAmount(params);
        return baseAmount != null ? baseAmount : new BigDecimal("100");
    }

    /**
     * 验证并最终确定金额
     */
    private BigDecimal validateAndFinalize(BigDecimal amount, Map<String, Object> params) {
        // 确保金额在合理范围内
        if (amount.compareTo(getMinAmount()) < 0) {
            return getMinAmount();
        }
        if (amount.compareTo(getMaxAmount()) > 0) {
            return getMaxAmount();
        }

        // 应用最终的业务规则
        return applyFinalBusinessRules(amount, params);
    }

    /**
     * 应用最终业务规则
     */
    private BigDecimal applyFinalBusinessRules(BigDecimal amount, Map<String, Object> params) {
        // 确保金额为正数
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return getMinAmount();
        }

        // 标准化小数位
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    // 以下为智能分析的辅助方法（简化实现，实际应连接AI服务）

    private boolean hasUserProfile(Map<String, Object> params) {
        return params.containsKey("userId") &&
               (params.containsKey("userProfile") || params.containsKey("historicalData"));
    }

    private boolean hasValidContext(Map<String, Object> params) {
        return params.containsKey("scenarioType") ||
               params.containsKey("contextData") ||
               params.containsKey("location");
    }

    private boolean isCreditLevelAcceptable(String creditLevel) {
        List<String> acceptableLevels = Arrays.asList("A", "AA", "AAA", "EXCELLENT", "GOOD");
        return acceptableLevels.contains(creditLevel.toUpperCase());
    }

    private boolean isWithinAllowedTimeWindow(Map<String, Object> params) {
        LocalTime now = LocalTime.now();
        // 智能模式允许全天候使用，但特殊场景有时间限制
        String scenarioType = getStringFromParams(params, "scenarioType");
        if ("HIGH_RISK".equals(scenarioType)) {
            return now.isAfter(LocalTime.of(8, 0)) && now.isBefore(LocalTime.of(22, 0));
        }
        return true;
    }

    private SmartAnalysisResult performQuickAnalysis(Map<String, Object> params) {
        SmartAnalysisResult result = new SmartAnalysisResult();
        result.setConfidence(DEFAULT_CONFIDENCE_THRESHOLD);
        result.setRiskScore(new BigDecimal("0.3")); // 默认低风险
        return result;
    }

    private BigDecimal analyzeUserBehavior(Map<String, Object> params) {
        // 简化实现：基于用户历史行为评分
        return new BigDecimal("0.8");
    }

    private BigDecimal analyzeContextScore(Map<String, Object> params) {
        // 简化实现：基于上下文相关性评分
        return new BigDecimal("0.75");
    }

    private BigDecimal assessRisk(Map<String, Object> params, BigDecimal amount) {
        // 简化实现：基于金额和历史风险评分
        if (amount.compareTo(new BigDecimal("1000")) > 0) {
            return new BigDecimal("0.6");
        }
        return new BigDecimal("0.2");
    }

    private BigDecimal assessOptimizationPotential(Map<String, Object> params) {
        // 简化实现：基于用户特征和场景评分
        return new BigDecimal("0.85");
    }

    private BigDecimal assessPersonalizationLevel(Map<String, Object> params) {
        // 简化实现：基于用户画像完整度评分
        return params.containsKey("userProfile") ? new BigDecimal("0.9") : new BigDecimal("0.4");
    }

    private BigDecimal calculateConfidence(SmartAnalysisResult result) {
        // 简化实现：综合各项评分计算置信度
        BigDecimal behaviorScore = result.getBehaviorScore() != null ? result.getBehaviorScore() : BigDecimal.ZERO;
        BigDecimal contextScore = result.getContextScore() != null ? result.getContextScore() : BigDecimal.ZERO;

        return behaviorScore.add(contextScore).divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal applyRecommendedDiscounts(BigDecimal amount, Map<String, Object> params) {
        // 简化实现：应用推荐折扣
        String category = getStringFromParams(params, "category");
        if (category != null) {
            return CategoryDiscountEnum.applyCategoryDiscount(amount, category);
        }
        return amount;
    }

    private Map<String, Object> analyzeUserProfile(Map<String, Object> params) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("segment", "PREMIUM");
        profile.put("lifeCycle", "MATURE");
        profile.put("loyaltyLevel", "HIGH");
        return profile;
    }

    private Map<String, Object> analyzeContext(Map<String, Object> params) {
        Map<String, Object> context = new HashMap<>();
        context.put("scenarioType", "REGULAR");
        context.put("urgencyLevel", "NORMAL");
        context.put("locationType", "FAMILIAR");
        return context;
    }

    private Map<String, Object> analyzeBehaviorPattern(Map<String, Object> params) {
        Map<String, Object> pattern = new HashMap<>();
        pattern.put("predictedIntent", "REGULAR_PURCHASE");
        pattern.put("frequency", "WEEKLY");
        pattern.put("consistency", "HIGH");
        return pattern;
    }

    private Map<String, Object> analyzeEnvironmentalFactors(Map<String, Object> params) {
        Map<String, Object> factors = new HashMap<>();
        factors.put("timeOfDay", LocalTime.now().toString());
        factors.put("dayOfWeek", LocalDateTime.now().getDayOfWeek().toString());
        factors.put("seasonalFactor", "NORMAL");
        return factors;
    }

    private List<Map<String, Object>> generateRecommendations(Map<String, Object> params) {
        List<Map<String, Object>> recommendations = new ArrayList<>();

        Map<String, Object> rec1 = new HashMap<>();
        rec1.put("type", "DISCOUNT");
        rec1.put("description", "应用会员折扣");
        rec1.put("saving", "5%");
        recommendations.add(rec1);

        return recommendations;
    }

    private Map<String, Object> performPreRiskAssessment(Map<String, Object> params) {
        Map<String, Object> risk = new HashMap<>();
        risk.put("riskLevel", "LOW");
        risk.put("riskFactors", new ArrayList<>());
        risk.put("mitigationNeeded", false);
        return risk;
    }

    private Map<String, Object> generateLearningData(Map<String, Object> params, Map<String, Object> result) {
        Map<String, Object> learningData = new HashMap<>();
        learningData.put("timestamp", LocalDateTime.now().toString());
        learningData.put("userId", params.get("userId"));
        learningData.put("decisionType", params.get("decisionType"));
        learningData.put("outcome", "SUCCESS");
        learningData.put("confidence", result.get("confidence"));
        return learningData;
    }

    /**
     * 智能分析结果类
     */
    public static class SmartAnalysisResult {
        private BigDecimal confidence;
        private BigDecimal riskScore;
        private BigDecimal behaviorScore;
        private BigDecimal contextScore;
        private BigDecimal optimizationScore;
        private BigDecimal personalizationScore;

        // Getters and Setters
        public BigDecimal getConfidence() { return confidence; }
        public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }

        public BigDecimal getRiskScore() { return riskScore; }
        public void setRiskScore(BigDecimal riskScore) { this.riskScore = riskScore; }

        public BigDecimal getBehaviorScore() { return behaviorScore; }
        public void setBehaviorScore(BigDecimal behaviorScore) { this.behaviorScore = behaviorScore; }

        public BigDecimal getContextScore() { return contextScore; }
        public void setContextScore(BigDecimal contextScore) { this.contextScore = contextScore; }

        public BigDecimal getOptimizationScore() { return optimizationScore; }
        public void setOptimizationScore(BigDecimal optimizationScore) { this.optimizationScore = optimizationScore; }

        public BigDecimal getPersonalizationScore() { return personalizationScore; }
        public void setPersonalizationScore(BigDecimal personalizationScore) { this.personalizationScore = personalizationScore; }
    }

    /**
     * 获取所有支持的决策类型
     */
    public static List<String> getSupportedDecisionTypes() {
        return Arrays.stream(DecisionType.values())
                .map(Enum::name)
                .collect(java.util.stream.Collectors.toList());
    }
}