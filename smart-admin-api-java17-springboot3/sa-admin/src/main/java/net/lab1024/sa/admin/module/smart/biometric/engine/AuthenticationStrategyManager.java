package net.lab1024.sa.admin.module.smart.biometric.engine;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.smart.biometric.constant.BiometricTypeEnum;
import net.lab1024.sa.admin.module.smart.biometric.constant.SecurityLevelEnum;

/**
 * 认证策略管理器
 *
 * 负责管理不同的认证策略，包括安全级别控制、风险评估、自适应认证等功能， 根据环境因素和风险等级动态调整认证要求
 *
 * @author SmartAdmin Team
 * @date 2025-01-15
 */
@Slf4j
public class AuthenticationStrategyManager {

    private final Map<String, AuthenticationStrategy> strategyRegistry;
    private final AuthenticationContext context;
    private final RiskAssessmentEngine riskAssessmentEngine;
    private final AdaptivePolicyEngine adaptivePolicyEngine;
    private final AuthenticationStatistics statistics;
    private final AtomicLong strategyExecutionCounter;

    public AuthenticationStrategyManager(AuthenticationContext context) {
        this.strategyRegistry = new ConcurrentHashMap<>();
        this.context = context;
        this.riskAssessmentEngine = new RiskAssessmentEngine();
        this.adaptivePolicyEngine = new AdaptivePolicyEngine();
        this.statistics = new AuthenticationStatistics();
        this.strategyExecutionCounter = new AtomicLong(0);

        // 初始化默认策略
        initializeDefaultStrategies();

        log.info("认证策略管理器初始化完成");
    }

    /**
     * 注册认证策略
     *
     * @param strategy 认证策略
     * @return 注册是否成功
     */
    public boolean registerStrategy(AuthenticationStrategy strategy) {
        try {
            if (strategy == null || strategy.getStrategyId() == null) {
                log.error("策略注册失败：策略对象或策略ID为空");
                return false;
            }

            // 验证策略配置
            if (!validateStrategy(strategy)) {
                log.error("策略注册失败：策略配置验证失败 - {}", strategy.getStrategyId());
                return false;
            }

            strategyRegistry.put(strategy.getStrategyId(), strategy);
            log.info("认证策略注册成功: {}", strategy.getStrategyId());
            return true;

        } catch (Exception e) {
            log.error("策略注册异常", e);
            return false;
        }
    }

    /**
     * 获取认证策略
     *
     * @param strategyId 策略ID
     * @return 认证策略
     */
    public AuthenticationStrategy getStrategy(String strategyId) {
        return strategyRegistry.get(strategyId);
    }

    /**
     * 评估并选择最佳认证策略
     *
     * @param request 认证请求上下文
     * @return 策略评估结果
     */
    public StrategyEvaluationResult evaluateAndSelectStrategy(
            AuthenticationRequestContext request) {
        String executionId = generateExecutionId();
        long startTime = System.currentTimeMillis();

        try {
            log.info("开始认证策略评估: executionId={}, userId={}, location={}", executionId,
                    request.getUserId(), request.getLocation());

            // 1. 风险评估
            RiskAssessmentResult riskResult = riskAssessmentEngine.assessRisk(request);
            request.setRiskLevel(riskResult.getRiskLevel());
            request.setRiskScore(riskResult.getRiskScore());

            // 2. 环境因素分析
            EnvironmentAnalysisResult envResult = analyzeEnvironment(request);

            // 3. 策略候选筛选
            List<AuthenticationStrategy> candidateStrategies = filterCandidateStrategies(request);

            // 4. 策略评分和排序
            List<StrategyScore> scoredStrategies =
                    scoreAndRankStrategies(candidateStrategies, request, riskResult, envResult);

            // 5. 选择最佳策略
            AuthenticationStrategy selectedStrategy = selectBestStrategy(scoredStrategies, request);

            // 6. 自适应策略调整
            AuthenticationStrategy finalStrategy =
                    adaptivePolicyEngine.adjustStrategy(selectedStrategy, request, riskResult);

            // 7. 构建评估结果
            StrategyEvaluationResult result = new StrategyEvaluationResult();
            result.setExecutionId(executionId);
            result.setUserId(request.getUserId());
            result.setLocation(request.getLocation());
            result.setRiskLevel(riskResult.getRiskLevel());
            result.setRiskScore(riskResult.getRiskScore());
            result.setEnvironmentFactors(envResult.getFactors());
            result.setOriginalStrategy(selectedStrategy);
            result.setFinalStrategy(finalStrategy);
            result.setCandidateCount(candidateStrategies.size());
            result.setAllCandidateStrategies(candidateStrategies);
            result.setStrategyScores(scoredStrategies);
            result.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            result.setEvaluationTime(LocalDateTime.now());

            // 更新统计信息
            statistics.incrementStrategyEvaluations();
            if (finalStrategy != null) {
                statistics.incrementStrategySelections(finalStrategy.getStrategyId());
            }

            log.info(
                    "认证策略评估完成: executionId={}, selectedStrategy={}, riskLevel={}, processingTime={}ms",
                    executionId, finalStrategy != null ? finalStrategy.getStrategyId() : "NONE",
                    riskResult.getRiskLevel(), result.getProcessingTimeMs());

            return result;

        } catch (Exception e) {
            log.error("认证策略评估异常: executionId={}", executionId, e);
            statistics.incrementFailedEvaluations();

            StrategyEvaluationResult errorResult = new StrategyEvaluationResult();
            errorResult.setExecutionId(executionId);
            errorResult.setUserId(request.getUserId());
            errorResult.setSuccess(false);
            errorResult.setErrorMessage("策略评估异常: " + e.getMessage());
            errorResult.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            return errorResult;
        }
    }

    /**
     * 执行认证策略
     *
     * @param strategyId 策略ID
     * @param request 认证请求
     * @return 策略执行结果
     */
    public StrategyExecutionResult executeStrategy(String strategyId,
            AuthenticationRequestContext request) {
        String executionId = generateExecutionId();

        try {
            log.info("开始执行认证策略: executionId={}, strategyId={}, userId={}", executionId, strategyId,
                    request.getUserId());

            AuthenticationStrategy strategy = strategyRegistry.get(strategyId);
            if (strategy == null) {
                return createExecutionFailure(executionId, request, "策略不存在: " + strategyId);
            }

            // 检查策略状态
            if (strategy.getStatus() != StrategyStatus.ACTIVE) {
                return createExecutionFailure(executionId, request,
                        "策略不可用: " + strategy.getStatus());
            }

            // 执行策略前置条件检查
            if (!strategy.checkPreconditions(request)) {
                return createExecutionFailure(executionId, request, "策略前置条件检查失败");
            }

            // 构建策略执行上下文
            StrategyExecutionContext executionContext = new StrategyExecutionContext();
            executionContext.setExecutionId(executionId);
            executionContext.setStrategy(strategy);
            executionContext.setRequest(request);
            executionContext.setContext(context);
            executionContext.setStartTime(System.currentTimeMillis());

            // 执行策略
            StrategyExecutionResult result = strategy.execute(executionContext);

            // 执行策略后置处理
            strategy.postProcess(executionContext, result);

            // 更新统计信息
            statistics.incrementStrategyExecutions(strategyId);
            if (result.isSuccess()) {
                statistics.incrementSuccessfulExecutions(strategyId);
            } else {
                statistics.incrementFailedExecutions(strategyId);
            }

            log.info("认证策略执行完成: executionId={}, strategyId={}, success={}, processingTime={}ms",
                    executionId, strategyId, result.isSuccess(), result.getProcessingTimeMs());

            return result;

        } catch (Exception e) {
            log.error("认证策略执行异常: executionId={}, strategyId={}", executionId, strategyId, e);
            statistics.incrementFailedExecutions(strategyId);
            return createExecutionFailure(executionId, request, "策略执行异常: " + e.getMessage());
        }
    }

    /**
     * 动态更新策略
     *
     * @param strategyId 策略ID
     * @param updates 更新内容
     * @return 更新是否成功
     */
    public boolean updateStrategy(String strategyId, Map<String, Object> updates) {
        try {
            AuthenticationStrategy strategy = strategyRegistry.get(strategyId);
            if (strategy == null) {
                log.error("策略更新失败：策略不存在 - {}", strategyId);
                return false;
            }

            // 备份原策略
            AuthenticationStrategy backup = strategy;

            // 应用更新
            if (strategy.updateConfiguration(updates)) {
                // 验证更新后的策略
                if (validateStrategy(strategy)) {
                    log.info("策略更新成功: {}", strategyId);
                    return true;
                } else {
                    // 回滚更新
                    strategyRegistry.put(strategyId, backup);
                    log.error("策略更新失败，验证未通过，已回滚: {}", strategyId);
                    return false;
                }
            } else {
                log.error("策略更新失败：更新应用失败 - {}", strategyId);
                return false;
            }

        } catch (Exception e) {
            log.error("策略更新异常: strategyId={}", strategyId, e);
            return false;
        }
    }

    /**
     * 获取策略统计信息
     *
     * @param strategyId 策略ID（可选，为空则返回所有策略统计）
     * @return 统计信息
     */
    public AuthenticationStatistics getStatistics(String strategyId) {
        if (strategyId == null) {
            return statistics;
        }

        AuthenticationStatistics specificStats = new AuthenticationStatistics();
        specificStats.setStrategyExecutionCount(statistics.getStrategyExecutions(strategyId));
        specificStats.setSuccessfulExecutionCount(statistics.getSuccessfulExecutions(strategyId));
        specificStats.setFailedExecutionCount(statistics.getFailedExecutions(strategyId));
        specificStats.setStrategySelectionCount(statistics.getStrategySelections(strategyId));

        return specificStats;
    }

    /**
     * 获取所有活跃策略
     *
     * @return 活跃策略列表
     */
    public List<AuthenticationStrategy> getActiveStrategies() {
        return strategyRegistry.values().stream()
                .filter(strategy -> strategy.getStatus() == StrategyStatus.ACTIVE)
                .sorted(Comparator.comparing(AuthenticationStrategy::getPriority).reversed())
                .toList();
    }

    /**
     * 获取策略列表
     *
     * @param includeInactive 是否包含非活跃策略
     * @return 策略列表
     */
    public List<AuthenticationStrategy> getAllStrategies(boolean includeInactive) {
        if (includeInactive) {
            return new ArrayList<>(strategyRegistry.values());
        } else {
            return getActiveStrategies();
        }
    }

    // ================================
    // 私有辅助方法
    // ================================

    private void initializeDefaultStrategies() {
        // 低安全级别策略
        registerStrategy(createLowSecurityStrategy());

        // 中安全级别策略
        registerStrategy(createMediumSecurityStrategy());

        // 高安全级别策略
        registerStrategy(createHighSecurityStrategy());

        // 关键安全级别策略
        registerStrategy(createCriticalSecurityStrategy());

        log.info("默认认证策略初始化完成，共{}个策略", strategyRegistry.size());
    }

    private AuthenticationStrategy createLowSecurityStrategy() {
        AuthenticationStrategy strategy = new AuthenticationStrategy();
        strategy.setStrategyId("LOW_SECURITY");
        strategy.setName("低安全级别策略");
        strategy.setDescription("适用于一般办公区域的基础认证策略");
        strategy.setSecurityLevel(SecurityLevelEnum.LOW);
        strategy.setPriority(1);
        strategy.setStatus(StrategyStatus.ACTIVE);

        // 配置要求
        StrategyRequirements requirements = new StrategyRequirements();
        requirements.setRequiredBiometricTypes(
                Arrays.asList(BiometricTypeEnum.FACE, BiometricTypeEnum.FINGERPRINT));
        requirements.setMinBiometricTypes(1);
        requirements.setRequireLivenessDetection(false);
        requirements.setMaxRetryAttempts(3);
        requirements.setTimeoutSeconds(30);
        requirements.setConfidenceThreshold(0.70);
        strategy.setRequirements(requirements);

        // 配置条件
        StrategyConditions conditions = new StrategyConditions();
        conditions.setAllowedRiskLevels(Arrays.asList(RiskLevel.LOW, RiskLevel.MEDIUM));
        conditions.setAllowedTimeRanges(Arrays.asList("08:00-18:00"));
        conditions.setAllowedLocations(Arrays.asList("OFFICE", "MEETING_ROOM"));
        conditions.setMaxConcurrentSessions(5);
        strategy.setConditions(conditions);

        return strategy;
    }

    private AuthenticationStrategy createMediumSecurityStrategy() {
        AuthenticationStrategy strategy = new AuthenticationStrategy();
        strategy.setStrategyId("MEDIUM_SECURITY");
        strategy.setName("中安全级别策略");
        strategy.setDescription("适用于重要办公区域的标准认证策略");
        strategy.setSecurityLevel(SecurityLevelEnum.MEDIUM);
        strategy.setPriority(2);
        strategy.setStatus(StrategyStatus.ACTIVE);

        StrategyRequirements requirements = new StrategyRequirements();
        requirements.setRequiredBiometricTypes(Arrays.asList(BiometricTypeEnum.FACE,
                BiometricTypeEnum.FINGERPRINT, BiometricTypeEnum.IRIS));
        requirements.setMinBiometricTypes(1);
        requirements.setRequireLivenessDetection(true);
        requirements.setMaxRetryAttempts(2);
        requirements.setTimeoutSeconds(45);
        requirements.setConfidenceThreshold(0.85);
        strategy.setRequirements(requirements);

        StrategyConditions conditions = new StrategyConditions();
        conditions.setAllowedRiskLevels(Arrays.asList(RiskLevel.LOW, RiskLevel.MEDIUM));
        conditions.setAllowedTimeRanges(Arrays.asList("07:00-20:00"));
        conditions.setAllowedLocations(Arrays.asList("OFFICE", "LAB", "MEETING_ROOM"));
        conditions.setMaxConcurrentSessions(3);
        strategy.setConditions(conditions);

        return strategy;
    }

    private AuthenticationStrategy createHighSecurityStrategy() {
        AuthenticationStrategy strategy = new AuthenticationStrategy();
        strategy.setStrategyId("HIGH_SECURITY");
        strategy.setName("高安全级别策略");
        strategy.setDescription("适用于机密区域的严格认证策略");
        strategy.setSecurityLevel(SecurityLevelEnum.HIGH);
        strategy.setPriority(3);
        strategy.setStatus(StrategyStatus.ACTIVE);

        StrategyRequirements requirements = new StrategyRequirements();
        requirements.setRequiredBiometricTypes(
                Arrays.asList(BiometricTypeEnum.FACE, BiometricTypeEnum.FINGERPRINT,
                        BiometricTypeEnum.IRIS, BiometricTypeEnum.PALMPRINT));
        requirements.setMinBiometricTypes(2);
        requirements.setRequireLivenessDetection(true);
        requirements.setMaxRetryAttempts(1);
        requirements.setTimeoutSeconds(60);
        requirements.setConfidenceThreshold(0.95);
        strategy.setRequirements(requirements);

        StrategyConditions conditions = new StrategyConditions();
        conditions.setAllowedRiskLevels(Arrays.asList(RiskLevel.LOW));
        conditions.setAllowedTimeRanges(Arrays.asList("00:00-23:59"));
        conditions.setAllowedLocations(Arrays.asList("DATA_CENTER", "SERVER_ROOM", "LAB"));
        conditions.setMaxConcurrentSessions(2);
        conditions.setRequireLocationVerification(true);
        strategy.setConditions(conditions);

        return strategy;
    }

    private AuthenticationStrategy createCriticalSecurityStrategy() {
        AuthenticationStrategy strategy = new AuthenticationStrategy();
        strategy.setStrategyId("CRITICAL_SECURITY");
        strategy.setName("关键安全级别策略");
        strategy.setDescription("适用于核心区域的最高级别认证策略");
        strategy.setSecurityLevel(SecurityLevelEnum.CRITICAL);
        strategy.setPriority(4);
        strategy.setStatus(StrategyStatus.ACTIVE);

        StrategyRequirements requirements = new StrategyRequirements();
        requirements.setRequiredBiometricTypes(
                Arrays.asList(BiometricTypeEnum.FACE, BiometricTypeEnum.FINGERPRINT,
                        BiometricTypeEnum.IRIS, BiometricTypeEnum.PALMPRINT));
        requirements.setMinBiometricTypes(3);
        requirements.setRequireLivenessDetection(true);
        requirements.setMaxRetryAttempts(1);
        requirements.setTimeoutSeconds(90);
        requirements.setConfidenceThreshold(0.98);
        strategy.setRequirements(requirements);

        StrategyConditions conditions = new StrategyConditions();
        conditions.setAllowedRiskLevels(Arrays.asList(RiskLevel.LOW));
        conditions.setAllowedTimeRanges(Arrays.asList("00:00-23:59"));
        conditions.setAllowedLocations(Arrays.asList("CORE_ROOM", "VAULT", "SECURITY_CENTER"));
        conditions.setMaxConcurrentSessions(1);
        conditions.setRequireLocationVerification(true);
        conditions.setRequireBehaviorAnalysis(true);
        strategy.setConditions(conditions);

        return strategy;
    }

    private boolean validateStrategy(AuthenticationStrategy strategy) {
        // 验证基本属性
        if (strategy.getStrategyId() == null || strategy.getStrategyId().trim().isEmpty()) {
            return false;
        }
        if (strategy.getSecurityLevel() == null) {
            return false;
        }
        if (strategy.getRequirements() == null) {
            return false;
        }

        // 验证要求配置
        StrategyRequirements req = strategy.getRequirements();
        if (req.getRequiredBiometricTypes() == null || req.getRequiredBiometricTypes().isEmpty()) {
            return false;
        }
        if (req.getMinBiometricTypes() <= 0
                || req.getMinBiometricTypes() > req.getRequiredBiometricTypes().size()) {
            return false;
        }
        if (req.getConfidenceThreshold() < 0.0 || req.getConfidenceThreshold() > 1.0) {
            return false;
        }

        return true;
    }

    private List<AuthenticationStrategy> filterCandidateStrategies(
            AuthenticationRequestContext request) {
        return strategyRegistry.values().stream()
                .filter(strategy -> strategy.getStatus() == StrategyStatus.ACTIVE)
                .filter(strategy -> strategy.isApplicable(request))
                .sorted(Comparator.comparing(AuthenticationStrategy::getPriority).reversed())
                .toList();
    }

    private List<StrategyScore> scoreAndRankStrategies(List<AuthenticationStrategy> strategies,
            AuthenticationRequestContext request, RiskAssessmentResult riskResult,
            EnvironmentAnalysisResult envResult) {

        List<StrategyScore> scores = new ArrayList<>();

        for (AuthenticationStrategy strategy : strategies) {
            double score = calculateStrategyScore(strategy, request, riskResult, envResult);
            StrategyScore strategyScore = new StrategyScore();
            strategyScore.setStrategy(strategy);
            strategyScore.setScore(score);
            scores.add(strategyScore);
        }

        // 按分数降序排序
        scores.sort(Comparator.comparing(StrategyScore::getScore).reversed());
        return scores;
    }

    private double calculateStrategyScore(AuthenticationStrategy strategy,
            AuthenticationRequestContext request, RiskAssessmentResult riskResult,
            EnvironmentAnalysisResult envResult) {

        double score = 0.0;

        // 1. 安全级别匹配度 (30%)
        SecurityLevelEnum requiredLevel = strategy.getSecurityLevel();
        if (request.getRequiredSecurityLevel() != null) {
            if (requiredLevel.getLevel() >= request.getRequiredSecurityLevel().getLevel()) {
                score += 30.0;
            }
        } else {
            score += 20.0; // 基础分数
        }

        // 2. 风险等级适配度 (25%)
        if (strategy.getConditions().getAllowedRiskLevels().contains(riskResult.getRiskLevel())) {
            score += 25.0 - (riskResult.getRiskScore() * 10); // 风险越低分数越高
        }

        // 3. 环境因素匹配度 (20%)
        double envMatch = calculateEnvironmentMatch(strategy, envResult);
        score += envMatch * 20.0;

        // 4. 策略复杂度 (15%)
        double complexityScore = calculateComplexityScore(strategy);
        score += complexityScore * 15.0;

        // 5. 优先级 (10%)
        double priorityScore = (strategy.getPriority() / 4.0) * 10.0; // 假设最高优先级为4
        score += priorityScore;

        return Math.min(100.0, Math.max(0.0, score));
    }

    private double calculateEnvironmentMatch(AuthenticationStrategy strategy,
            EnvironmentAnalysisResult envResult) {
        double match = 0.0;
        int totalFactors = 0;

        StrategyConditions conditions = strategy.getConditions();

        // 时间匹配
        if (conditions.getAllowedTimeRanges() != null) {
            totalFactors++;
            String currentTime = java.time.LocalTime.now().toString().substring(0, 5);
            boolean timeMatch = conditions.getAllowedTimeRanges().stream()
                    .anyMatch(range -> isTimeInRange(currentTime, range));
            if (timeMatch)
                match += 1.0;
        }

        // 位置匹配
        if (conditions.getAllowedLocations() != null && envResult.getLocation() != null) {
            totalFactors++;
            if (conditions.getAllowedLocations().contains(envResult.getLocation())) {
                match += 1.0;
            }
        }

        return totalFactors > 0 ? match / totalFactors : 0.5; // 默认50%匹配度
    }

    private boolean isTimeInRange(String currentTime, String timeRange) {
        try {
            String[] parts = timeRange.split("-");
            String start = parts[0];
            String end = parts[1];

            int current = Integer.parseInt(currentTime.replace(":", ""));
            int startTime = Integer.parseInt(start.replace(":", ""));
            int endTime = Integer.parseInt(end.replace(":", ""));

            return current >= startTime && current <= endTime;
        } catch (Exception e) {
            return false;
        }
    }

    private double calculateComplexityScore(AuthenticationStrategy strategy) {
        StrategyRequirements req = strategy.getRequirements();
        double complexity = 0.0;

        // 生物识别类型数量
        complexity += req.getMinBiometricTypes() * 0.2;

        // 活体检测要求
        if (req.isRequireLivenessDetection()) {
            complexity += 0.3;
        }

        // 置信度阈值
        complexity += req.getConfidenceThreshold() * 0.3;

        // 超时时间（越短越复杂）
        complexity += (60.0 / Math.max(req.getTimeoutSeconds(), 30)) * 0.2;

        return Math.min(1.0, complexity);
    }

    private AuthenticationStrategy selectBestStrategy(List<StrategyScore> scoredStrategies,
            AuthenticationRequestContext request) {
        if (scoredStrategies.isEmpty()) {
            return null;
        }

        // 选择最高分且适用的策略
        for (StrategyScore score : scoredStrategies) {
            AuthenticationStrategy strategy = score.getStrategy();
            if (strategy.isApplicable(request)) {
                return strategy;
            }
        }

        return scoredStrategies.get(0).getStrategy(); // 如果都不可用，返回最高分的
    }

    private EnvironmentAnalysisResult analyzeEnvironment(AuthenticationRequestContext request) {
        EnvironmentAnalysisResult result = new EnvironmentAnalysisResult();
        result.setLocation(request.getLocation());
        result.setTime(java.time.LocalTime.now().toString());
        result.setDeviceType(request.getDeviceType());
        result.setNetworkType(request.getNetworkType());

        Map<String, Object> factors = new HashMap<>();
        factors.put("location", request.getLocation());
        factors.put("time", result.getTime());
        factors.put("deviceType", request.getDeviceType());
        factors.put("networkType", request.getNetworkType());
        result.setFactors(factors);

        return result;
    }

    private String generateExecutionId() {
        return "STRATEGY_EXEC_" + System.currentTimeMillis() + "_"
                + strategyExecutionCounter.incrementAndGet();
    }

    private StrategyExecutionResult createExecutionFailure(String executionId,
            AuthenticationRequestContext request, String message) {
        StrategyExecutionResult result = new StrategyExecutionResult();
        result.setExecutionId(executionId);
        result.setSuccess(false);
        result.setUserId(request.getUserId());
        result.setMessage(message);
        result.setProcessingTimeMs(0);
        return result;
    }

    // ================================
    // 内部类定义
    // ================================

    @Data
    public static class AuthenticationStrategy {
        private String strategyId;
        private String name;
        private String description;
        private SecurityLevelEnum securityLevel;
        private int priority;
        private StrategyStatus status;
        private StrategyRequirements requirements;
        private StrategyConditions conditions;
        private Map<String, Object> configuration;
        private LocalDateTime createdTime;
        private LocalDateTime updatedTime;

        public AuthenticationStrategy() {
            this.status = StrategyStatus.DRAFT;
            this.configuration = new HashMap<>();
            this.createdTime = LocalDateTime.now();
            this.updatedTime = LocalDateTime.now();
        }

        public boolean isApplicable(AuthenticationRequestContext request) {
            if (conditions == null)
                return true;

            // 检查风险等级
            if (conditions.getAllowedRiskLevels() != null && request.getRiskLevel() != null) {
                if (!conditions.getAllowedRiskLevels().contains(request.getRiskLevel())) {
                    return false;
                }
            }

            // 检查时间范围
            if (conditions.getAllowedTimeRanges() != null) {
                String currentTime = java.time.LocalTime.now().toString().substring(0, 5);
                boolean timeMatch = conditions.getAllowedTimeRanges().stream()
                        .anyMatch(range -> isTimeInRange(currentTime, range));
                if (!timeMatch)
                    return false;
            }

            // 检查位置
            if (conditions.getAllowedLocations() != null && request.getLocation() != null) {
                if (!conditions.getAllowedLocations().contains(request.getLocation())) {
                    return false;
                }
            }

            return true;
        }

        public boolean checkPreconditions(AuthenticationRequestContext request) {
            // 检查用户状态
            if (request.getUserStatus() != UserStatus.ACTIVE) {
                return false;
            }

            // 检查设备状态
            if (request.getDeviceStatus() != DeviceStatus.TRUSTED) {
                return false;
            }

            return true;
        }

        private boolean isTimeInRange(String currentTime, String timeRange) {
            try {
                String[] parts = timeRange.split("-");
                String start = parts[0];
                String end = parts[1];

                int current = Integer.parseInt(currentTime.replace(":", ""));
                int startTime = Integer.parseInt(start.replace(":", ""));
                int endTime = Integer.parseInt(end.replace(":", ""));

                return current >= startTime && current <= endTime;
            } catch (Exception e) {
                return false;
            }
        }

        public StrategyExecutionResult execute(StrategyExecutionContext context) {
            StrategyExecutionResult result = new StrategyExecutionResult();
            result.setExecutionId(context.getExecutionId());
            result.setStrategyId(strategyId);
            result.setUserId(context.getRequest().getUserId());
            result.setStartTime(System.currentTimeMillis());

            try {
                // 实现说明：此处应对接生物识别引擎（人脸/指纹/声纹等）完成实际认证，
                // 并根据策略配置执行多因子校验、风控规则与阈值判断。
                // 这里应该调用生物识别引擎进行实际认证

                // 模拟执行结果
                result.setSuccess(Math.random() > 0.1); // 90%成功率
                result.setConfidence(0.85 + Math.random() * 0.1);
                result.setMessage("策略执行完成");

            } catch (Exception e) {
                result.setSuccess(false);
                result.setMessage("策略执行异常: " + e.getMessage());
            } finally {
                result.setProcessingTimeMs(System.currentTimeMillis() - result.getStartTime());
            }

            return result;
        }

        public void postProcess(StrategyExecutionContext context, StrategyExecutionResult result) {
            // 后置处理逻辑，如记录日志、更新统计等
            log.info("策略后置处理: strategyId={}, executionId={}, success={}", strategyId,
                    context.getExecutionId(), result.isSuccess());
        }

        public boolean updateConfiguration(Map<String, Object> updates) {
            if (updates != null) {
                configuration.putAll(updates);
                updatedTime = LocalDateTime.now();
                return true;
            }
            return false;
        }
    }

    @Data
    public static class StrategyRequirements {
        private List<BiometricTypeEnum> requiredBiometricTypes;
        private int minBiometricTypes;
        private boolean requireLivenessDetection;
        private int maxRetryAttempts;
        private int timeoutSeconds;
        private double confidenceThreshold;
        private boolean requireLocationVerification;
        private boolean requireBehaviorAnalysis;
    }

    @Data
    public static class StrategyConditions {
        private List<RiskLevel> allowedRiskLevels;
        private List<String> allowedTimeRanges;
        private List<String> allowedLocations;
        private int maxConcurrentSessions;
        private boolean requireLocationVerification;
        private boolean requireBehaviorAnalysis;
    }

    @Data
    public static class AuthenticationRequestContext {
        private Long userId;
        private String deviceId;
        private String location;
        private String deviceType;
        private String networkType;
        private SecurityLevelEnum requiredSecurityLevel;
        private RiskLevel riskLevel;
        private double riskScore;
        private UserStatus userStatus;
        private DeviceStatus deviceStatus;
        private Map<String, Object> additionalContext;
    }

    @Data
    public static class StrategyExecutionContext {
        private String executionId;
        private AuthenticationStrategy strategy;
        private AuthenticationRequestContext request;
        private AuthenticationContext context;
        private long startTime;
    }

    @Data
    public static class StrategyEvaluationResult {
        private String executionId;
        private Long userId;
        private String location;
        private RiskLevel riskLevel;
        private double riskScore;
        private Map<String, Object> environmentFactors;
        private AuthenticationStrategy originalStrategy;
        private AuthenticationStrategy finalStrategy;
        private int candidateCount;
        private List<AuthenticationStrategy> allCandidateStrategies;
        private List<StrategyScore> strategyScores;
        private long processingTimeMs;
        private LocalDateTime evaluationTime;
        private boolean success = true;
        private String errorMessage;
    }

    @Data
    public static class StrategyExecutionResult {
        private String executionId;
        private String strategyId;
        private Long userId;
        private boolean success;
        private double confidence;
        private String message;
        private long startTime;
        private long processingTimeMs;
        private Map<String, Object> resultDetails;
    }

    @Data
    public static class StrategyScore {
        private AuthenticationStrategy strategy;
        private double score;
        private Map<String, Object> scoreDetails;
    }

    @Data
    public static class RiskAssessmentResult {
        private RiskLevel riskLevel;
        private double riskScore;
        private Map<String, Object> riskFactors;
        private String assessmentDetails;
    }

    @Data
    public static class EnvironmentAnalysisResult {
        private String location;
        private String time;
        private String deviceType;
        private String networkType;
        private Map<String, Object> factors;
    }

    @Data
    public static class AuthenticationContext {
        private String contextId;
        private Map<String, Object> globalSettings;
        private Map<String, Object> securityPolicies;
    }

    @Data
    public static class AuthenticationStatistics {
        private long strategyEvaluations;
        private long failedEvaluations;
        private Map<String, Long> strategyExecutions = new HashMap<>();
        private Map<String, Long> successfulExecutions = new HashMap<>();
        private Map<String, Long> failedExecutions = new HashMap<>();
        private Map<String, Long> strategySelections = new HashMap<>();

        public void incrementStrategyEvaluations() {
            strategyEvaluations++;
        }

        public void incrementFailedEvaluations() {
            failedEvaluations++;
        }

        public void incrementStrategyExecutions(String strategyId) {
            strategyExecutions.merge(strategyId, 1L,
                    (a, b) -> Long.valueOf((a == null ? 0L : a) + (b == null ? 0L : b)));
        }

        public void incrementSuccessfulExecutions(String strategyId) {
            successfulExecutions.merge(strategyId, 1L,
                    (a, b) -> Long.valueOf((a == null ? 0L : a) + (b == null ? 0L : b)));
        }

        public void incrementFailedExecutions(String strategyId) {
            failedExecutions.merge(strategyId, 1L,
                    (a, b) -> Long.valueOf((a == null ? 0L : a) + (b == null ? 0L : b)));
        }

        public void incrementStrategySelections(String strategyId) {
            strategySelections.merge(strategyId, 1L,
                    (a, b) -> Long.valueOf((a == null ? 0L : a) + (b == null ? 0L : b)));
        }

        public void setStrategyExecutionCount(long count) { /* Implementation needed */ }

        public void setSuccessfulExecutionCount(long count) { /* Implementation needed */ }

        public void setFailedExecutionCount(long count) { /* Implementation needed */ }

        public void setStrategySelectionCount(long count) { /* Implementation needed */ }

        public long getStrategyExecutions(String strategyId) {
            return strategyExecutions.getOrDefault(strategyId, 0L);
        }

        public long getSuccessfulExecutions(String strategyId) {
            return successfulExecutions.getOrDefault(strategyId, 0L);
        }

        public long getFailedExecutions(String strategyId) {
            return failedExecutions.getOrDefault(strategyId, 0L);
        }

        public long getStrategySelections(String strategyId) {
            return strategySelections.getOrDefault(strategyId, 0L);
        }
    }

    // ================================
    // 枚举定义
    // ================================

    public enum StrategyStatus {
        DRAFT, // 草稿
        ACTIVE, // 活跃
        INACTIVE, // 非活跃
        DISABLED // 禁用
    }

    public enum RiskLevel {
        LOW, // 低风险
        MEDIUM, // 中风险
        HIGH, // 高风险
        CRITICAL // 关键风险
    }

    public enum UserStatus {
        ACTIVE, // 活跃
        INACTIVE, // 非活跃
        SUSPENDED, // 暂停
        BLACKLISTED // 黑名单
    }

    public enum DeviceStatus {
        TRUSTED, // 可信设备
        UNKNOWN, // 未知设备
        SUSPICIOUS, // 可疑设备
        BLACKLISTED // 黑名单设备
    }

    // ================================
    // 内部引擎类
    // ================================

    private static class RiskAssessmentEngine {
        public RiskAssessmentResult assessRisk(AuthenticationRequestContext request) {
            RiskAssessmentResult result = new RiskAssessmentResult();

            // 简单的风险评估逻辑
            double riskScore = calculateRiskScore(request);
            RiskLevel riskLevel = determineRiskLevel(riskScore);

            result.setRiskScore(riskScore);
            result.setRiskLevel(riskLevel);
            result.setAssessmentDetails("风险评估完成");

            return result;
        }

        private double calculateRiskScore(AuthenticationRequestContext request) {
            double score = 0.0;

            // 设备风险评估
            if (request.getDeviceStatus() == DeviceStatus.UNKNOWN) {
                score += 0.3;
            } else if (request.getDeviceStatus() == DeviceStatus.SUSPICIOUS) {
                score += 0.6;
            } else if (request.getDeviceStatus() == DeviceStatus.BLACKLISTED) {
                score += 1.0;
            }

            // 网络风险评估
            if ("PUBLIC_WIFI".equals(request.getNetworkType())) {
                score += 0.2;
            }

            // 时间风险评估
            int currentHour = java.time.LocalTime.now().getHour();
            if (currentHour < 6 || currentHour > 22) {
                score += 0.1; // 非正常工作时间
            }

            return Math.min(1.0, score);
        }

        private RiskLevel determineRiskLevel(double score) {
            if (score >= 0.8)
                return RiskLevel.CRITICAL;
            if (score >= 0.5)
                return RiskLevel.HIGH;
            if (score >= 0.2)
                return RiskLevel.MEDIUM;
            return RiskLevel.LOW;
        }
    }

    private static class AdaptivePolicyEngine {
        public AuthenticationStrategy adjustStrategy(AuthenticationStrategy strategy,
                AuthenticationRequestContext request, RiskAssessmentResult riskResult) {
            // 根据风险等级动态调整策略
            if (riskResult.getRiskLevel() == RiskLevel.HIGH
                    || riskResult.getRiskLevel() == RiskLevel.CRITICAL) {
                // 提高安全要求
                AuthenticationStrategy adjustedStrategy = cloneStrategy(strategy);
                StrategyRequirements req = adjustedStrategy.getRequirements();

                // 增加生物识别类型要求
                req.setMinBiometricTypes(Math.min(req.getMinBiometricTypes() + 1,
                        req.getRequiredBiometricTypes().size()));

                // 提高置信度阈值
                req.setConfidenceThreshold(Math.min(req.getConfidenceThreshold() + 0.05, 0.99));

                // 强制活体检测
                req.setRequireLivenessDetection(true);

                return adjustedStrategy;
            }

            return strategy;
        }

        private AuthenticationStrategy cloneStrategy(AuthenticationStrategy original) {
            // 简单的策略克隆实现
            AuthenticationStrategy clone = new AuthenticationStrategy();
            clone.setStrategyId(original.getStrategyId() + "_ADAPTIVE");
            clone.setName(original.getName() + " (自适应)");
            clone.setDescription(original.getDescription());
            clone.setSecurityLevel(original.getSecurityLevel());
            clone.setPriority(original.getPriority());
            clone.setStatus(original.getStatus());

            // 深拷贝配置
            if (original.getRequirements() != null) {
                StrategyRequirements req = new StrategyRequirements();
                req.setRequiredBiometricTypes(
                        new ArrayList<>(original.getRequirements().getRequiredBiometricTypes()));
                req.setMinBiometricTypes(original.getRequirements().getMinBiometricTypes());
                req.setRequireLivenessDetection(
                        original.getRequirements().isRequireLivenessDetection());
                req.setMaxRetryAttempts(original.getRequirements().getMaxRetryAttempts());
                req.setTimeoutSeconds(original.getRequirements().getTimeoutSeconds());
                req.setConfidenceThreshold(original.getRequirements().getConfidenceThreshold());
                clone.setRequirements(req);
            }

            if (original.getConditions() != null) {
                StrategyConditions cond = new StrategyConditions();
                cond.setAllowedRiskLevels(
                        new ArrayList<>(original.getConditions().getAllowedRiskLevels()));
                cond.setAllowedTimeRanges(
                        new ArrayList<>(original.getConditions().getAllowedTimeRanges()));
                cond.setAllowedLocations(
                        new ArrayList<>(original.getConditions().getAllowedLocations()));
                cond.setMaxConcurrentSessions(original.getConditions().getMaxConcurrentSessions());
                clone.setConditions(cond);
            }

            return clone;
        }
    }
}
