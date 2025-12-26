package net.lab1024.sa.attendance.engine.rule.impl;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.StopWatch;

import net.lab1024.sa.attendance.engine.rule.AttendanceRuleEngine;
import net.lab1024.sa.attendance.engine.rule.cache.RuleCacheManager;
import net.lab1024.sa.attendance.engine.rule.evaluator.RuleEvaluatorFactory;
import net.lab1024.sa.attendance.engine.rule.executor.RuleExecutor;
import net.lab1024.sa.attendance.engine.rule.loader.RuleLoader;
import net.lab1024.sa.attendance.engine.model.CompiledAction;
import net.lab1024.sa.attendance.engine.model.CompiledRule;
import net.lab1024.sa.attendance.engine.model.RuleEvaluationResult;
import net.lab1024.sa.attendance.engine.model.RuleExecutionContext;
import net.lab1024.sa.attendance.engine.rule.model.RuleExecutionStatistics;
import net.lab1024.sa.attendance.engine.model.RuleValidationResult;
import net.lab1024.sa.attendance.engine.rule.validator.RuleValidator;

/**
 * 考勤规则引擎实现类
 * <p>
 * 考勤规则引擎的核心实现，支持灵活的规则配置和执行
 * 严格遵循CLAUDE.md全局架构规范，Manager类为纯Java类
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
public class AttendanceRuleEngineImpl implements AttendanceRuleEngine {


    private final RuleLoader ruleLoader;
    private final RuleValidator ruleValidator;
    private final RuleCacheManager cacheManager;
    private final RuleEvaluatorFactory evaluatorFactory;
    private final RuleExecutor ruleExecutor;

    // 规则执行统计
    private final Map<String, Long> executionStatistics = new ConcurrentHashMap<>();

    /**
     * 构造函数注入依赖
     */
    public AttendanceRuleEngineImpl(
            RuleLoader ruleLoader,
            RuleValidator ruleValidator,
            RuleCacheManager cacheManager,
            RuleEvaluatorFactory evaluatorFactory,
            RuleExecutor ruleExecutor) {
        this.ruleLoader = ruleLoader;
        this.ruleValidator = ruleValidator;
        this.cacheManager = cacheManager;
        this.evaluatorFactory = evaluatorFactory;
        this.ruleExecutor = ruleExecutor;
    }

    @Override
    public List<RuleEvaluationResult> evaluateRules(RuleExecutionContext context) {
        log.debug("[规则引擎] 开始执行规则评估, 用户ID: {}, 考勤日期: {}",
                context.getUserId(), context.getAttendanceDate());

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            // 1. 获取适用的规则
            List<Long> applicableRules = getApplicableRules(context);
            log.debug("[规则引擎] 找到适用规则数量: {}", applicableRules.size());

            // 2. 批量执行规则评估
            List<RuleEvaluationResult> results = new ArrayList<RuleEvaluationResult>();
            for (Long ruleId : applicableRules) {
                RuleEvaluationResult result = evaluateRule(ruleId, context);
                results.add(result);
            }

            // 3. 按优先级排序结果
            results.sort((r1, r2) -> r1.getRulePriority().compareTo(r2.getRulePriority()));

            // 4. 处理规则覆盖
            handleRuleOverrides(results);

            stopWatch.stop();
            log.debug("[规则引擎] 规则评估完成, 耗时: {}ms, 规则数量: {}",
                    stopWatch.getLastTaskTimeMillis(), results.size());

            return results;

        } catch (Exception e) {
            log.error("[规则引擎] 规则评估失败", e);
            return createErrorResult(e);
        }
    }

    @Override
    public List<RuleEvaluationResult> evaluateRulesByCategory(String ruleCategory, RuleExecutionContext context) {
        log.debug("[规则引擎] 执行分类规则评估, 分类: {}", ruleCategory);

        try {
            // 1. 获取分类适用的规则
            List<Long> categoryRules = ruleLoader.getRulesByCategory(ruleCategory);
            log.debug("[规则引擎] 分类 {} 规则数量: {}", ruleCategory, categoryRules.size());

            // 2. 执行规则评估
            List<RuleEvaluationResult> results = new ArrayList<RuleEvaluationResult>();
            for (Long ruleId : categoryRules) {
                RuleEvaluationResult result = evaluateRule(ruleId, context);
                results.add(result);
            }

            return results;

        } catch (Exception e) {
            log.error("[规则引擎] 分类规则评估失败, 分类: {}", ruleCategory, e);
            return createErrorResult(e);
        }
    }

    @Override
    public RuleEvaluationResult evaluateRule(Long ruleId, RuleExecutionContext context) {
        log.debug("[规则引擎] 执行单个规则评估, 规则ID: {}", ruleId);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            // 1. 验证规则有效性
            RuleValidationResult validation = validateRule(ruleId);
            if (!validation.isValid()) {
                return createValidationErrorResult(ruleId, validation);
            }

            // 2. 检查缓存
            RuleEvaluationResult cachedResult = cacheManager.getCachedResult(ruleId, context);
            if (cachedResult != null) {
                log.debug("[规则引擎] 使用缓存结果, 规则ID: {}", ruleId);
                return cachedResult;
            }

            // 3. 加载规则配置
            Map<String, Object> ruleConfig = ruleLoader.loadRuleConfig(ruleId);
            if (ruleConfig == null) {
                return createNotFoundResult(ruleId);
            }

            // 4. 创建规则评估器并执行规则评估
            RuleEvaluationResult result = evaluatorFactory.createEvaluator(ruleConfig.get("ruleType").toString())
                    .evaluate(ruleId, ruleConfig, context);

            // 6. 缓存结果
            cacheManager.cacheResult(ruleId, context, result);

            stopWatch.stop();
            result.setEvaluationDuration(stopWatch.getLastTaskTimeMillis());
            result.setExecutionTimestamp(java.time.LocalDateTime.now());

            log.debug("[规则引擎] 规则评估完成, 规则ID: {}, 结果: {}, 耗时: {}ms",
                    ruleId, result.getEvaluationResult(), result.getEvaluationDuration());

            return result;

        } catch (Exception e) {
            log.error("[规则引擎] 规则评估失败, 规则ID: {}", ruleId, e);
            return createErrorResult(ruleId, e);
        }
    }

    @Override
    public RuleValidationResult validateRule(Long ruleId) {
        log.debug("[规则引擎] 验证规则, 规则ID: {}", ruleId);
        return ruleValidator.validateRule(ruleId);
    }

    @Override
    public List<Long> getApplicableRules(RuleExecutionContext context) {
        log.debug("[规则引擎] 获取适用规则, 用户ID: {}, 部门ID: {}",
                context.getUserId(), context.getDepartmentId());

        try {
            // 1. 加载所有启用的规则
            List<Long> allRules = ruleLoader.loadAllActiveRules();

            // 2. 过滤适用的规则
            List<Long> applicableRules = new ArrayList<>();
            for (Long ruleId : allRules) {
                if (isRuleApplicable(ruleId, context)) {
                    applicableRules.add(ruleId);
                }
            }

            return applicableRules;

        } catch (Exception e) {
            log.error("[规则引擎] 获取适用规则失败", e);
            return new ArrayList<Long>();
        }
    }

    @Override
    public CompiledRule compileRuleCondition(String ruleCondition) {
        log.debug("[规则引擎] 编译规则条件: {}", ruleCondition);
        long startTime = System.currentTimeMillis();

        try {
            // 1. 验证条件表达式
            if (ruleCondition == null || ruleCondition.trim().isEmpty()) {
                return CompiledRule.failure(null, ruleCondition, "规则条件表达式不能为空");
            }

            // 2. 解析条件表达式
            CompiledCondition compiledCondition = parseCondition(ruleCondition);

            // 3. 构建编译结果
            CompiledRule result = CompiledRule.builder()
                    .ruleId(null)
                    .conditionExpression(ruleCondition)
                    .compiledCondition(compiledCondition)
                    .compiled(true)
                    .compileTime(java.time.LocalDateTime.now())
                    .compileDuration(System.currentTimeMillis() - startTime)
                    .compilerName("AttendanceRuleEngine")
                    .compilerVersion("1.0.0")
                    .needsRecompile(false)
                    .build();

            log.debug("[规则引擎] 规则条件编译成功, 耗时: {}ms", result.getCompileDuration());
            return result;

        } catch (Exception e) {
            log.error("[规则引擎] 编译规则条件失败", e);
            return CompiledRule.failure(null, ruleCondition, e.getMessage());
        }
    }

    @Override
    public CompiledAction compileRuleAction(String ruleAction) {
        log.debug("[规则引擎] 编译规则动作: {}", ruleAction);
        long startTime = System.currentTimeMillis();

        try {
            // 1. 验证动作表达式
            if (ruleAction == null || ruleAction.trim().isEmpty()) {
                return CompiledAction.failure(null, null, ruleAction, "规则动作表达式不能为空");
            }

            // 2. 解析动作表达式
            CompiledActionObject compiledAction = parseAction(ruleAction);

            // 3. 构建编译结果
            CompiledAction result = CompiledAction.builder()
                    .actionId(java.util.UUID.randomUUID().toString())
                    .actionType(compiledAction.getActionType())
                    .actionConfiguration(ruleAction)
                    .compiledAction(compiledAction)
                    .compiled(true)
                    .compileTime(java.time.LocalDateTime.now())
                    .compileDuration(System.currentTimeMillis() - startTime)
                    .compilerName("AttendanceRuleEngine")
                    .compilerVersion("1.0.0")
                    .executionPriority(compiledAction.getPriority())
                    .isCritical(compiledAction.isCritical())
                    .retryCount(compiledAction.getRetryCount())
                    .timeoutMillis(compiledAction.getTimeoutMillis())
                    .needsRecompile(false)
                    .build();

            log.debug("[规则引擎] 规则动作编译成功: actionType={}, 耗时: {}ms",
                    result.getActionType(), result.getCompileDuration());
            return result;

        } catch (Exception e) {
            log.error("[规则引擎] 编译规则动作失败", e);
            return CompiledAction.failure(null, null, ruleAction, e.getMessage());
        }
    }

    @Override
    public List<RuleEvaluationResult> batchEvaluateRules(List<RuleExecutionContext> contexts) {
        log.info("[规则引擎] 批量执行规则评估, 上下文数量: {}", contexts.size());

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        List<RuleEvaluationResult> allResults = new ArrayList<RuleEvaluationResult>();

        try {
            for (RuleExecutionContext context : contexts) {
                List<RuleEvaluationResult> results = evaluateRules(context);
                allResults.addAll(results);
            }

            stopWatch.stop();
            log.info("[规则引擎] 批量规则评估完成, 总结果数: {}, 总耗时: {}ms",
                    allResults.size(), stopWatch.getLastTaskTimeMillis());

            return allResults;

        } catch (Exception e) {
            log.error("[规则引擎] 批量规则评估失败", e);
            return createErrorResult(e);
        }
    }

    @Override
    public RuleExecutionStatistics getExecutionStatistics(long startTime, long endTime) {
        log.debug("[规则引擎] 获取执行统计");

        try {
            RuleExecutionStatistics statistics = new RuleExecutionStatistics();
            statistics.setStartTime(java.time.Instant.ofEpochMilli(startTime));
            statistics.setEndTime(java.time.Instant.ofEpochMilli(endTime));
            statistics.setTotalEvaluations(getStatisticsValue("total_evaluations"));
            statistics.setSuccessEvaluations(getStatisticsValue("success_evaluations"));
            statistics.setFailedEvaluations(getStatisticsValue("failed_evaluations"));
            statistics.setCacheHits(getStatisticsValue("cache_hits"));
            statistics.setAverageEvaluationTime(calculateAverageEvaluationTime());

            return statistics;

        } catch (Exception e) {
            log.error("[规则引擎] 获取执行统计失败", e);
            return new RuleExecutionStatistics();
        }
    }

    @Override
    public void clearRuleCache() {
        log.info("[规则引擎] 清除规则缓存");
        cacheManager.clearCache();
    }

    @Override
    public void warmUpRuleCache(List<Long> ruleIds) {
        log.info("[规则引擎] 预热规则缓存, 规则数量: {}", ruleIds.size());
        try {
            for (Long ruleId : ruleIds) {
                cacheManager.preloadRule(ruleId);
            }
        } catch (Exception e) {
            log.error("[规则引擎] 预热规则缓存失败", e);
        }
    }

    /**
     * 检查规则是否适用
     */
    private boolean isRuleApplicable(Long ruleId, RuleExecutionContext context) {
        try {
            // 1. 加载规则配置
            Map<String, Object> ruleConfig = ruleLoader.loadRuleConfig(ruleId);
            if (ruleConfig == null) {
                log.warn("[规则引擎] 规则配置不存在: ruleId={}", ruleId);
                return false;
            }

            // 2. 检查规则状态
            Object enabled = ruleConfig.get("enabled");
            if (enabled != null && Boolean.FALSE.equals(enabled)) {
                log.debug("[规则引擎] 规则已禁用: ruleId={}", ruleId);
                return false;
            }

            // 3. 检查部门范围
            if (!checkDepartmentScope(ruleConfig, context)) {
                log.debug("[规则引擎] 规则部门范围不匹配: ruleId={}, departmentId={}",
                        ruleId, context.getDepartmentId());
                return false;
            }

            // 4. 检查用户属性
            if (!checkUserAttributes(ruleConfig, context)) {
                log.debug("[规则引擎] 规则用户属性不匹配: ruleId={}, userId={}",
                        ruleId, context.getUserId());
                return false;
            }

            // 5. 检查时间范围
            if (!checkTimeScope(ruleConfig, context)) {
                log.debug("[规则引擎] 规则时间范围不匹配: ruleId={}, attendanceDate={}",
                        ruleId, context.getAttendanceDate());
                return false;
            }

            // 6. 检查规则过滤器
            if (!checkRuleFilters(ruleConfig, context)) {
                log.debug("[规则引擎] 规则过滤器不匹配: ruleId={}", ruleId);
                return false;
            }

            return true;

        } catch (Exception e) {
            log.error("[规则引擎] 检查规则适用性失败, 规则ID: {}", ruleId, e);
            return false;
        }
    }

    /**
     * 处理规则覆盖
     */
    private void handleRuleOverrides(List<RuleEvaluationResult> results) {
        if (results == null || results.isEmpty()) {
            return;
        }

        // 从高优先级到低优先级处理覆盖
        for (int i = 0; i < results.size(); i++) {
            RuleEvaluationResult current = results.get(i);

            // 检查是否被更高优先级的规则覆盖
            for (int j = 0; j < i; j++) {
                RuleEvaluationResult higherPriority = results.get(j);

                if (shouldOverride(higherPriority, current)) {
                    current.setOverridden(true);
                    current.setOverridingRuleId(higherPriority.getRuleId());
                    break;
                }
            }
        }
    }

    /**
     * 判断是否应该覆盖
     */
    private boolean shouldOverride(RuleEvaluationResult higher, RuleEvaluationResult lower) {
        // 相同规则类型，更高优先级应该覆盖
        return higher.getRuleCategory().equals(lower.getRuleCategory()) &&
                higher.getRulePriority() < lower.getRulePriority() &&
                "MATCH".equals(higher.getEvaluationResult());
    }

    /**
     * 创建错误结果
     */
    private List<RuleEvaluationResult> createErrorResult(Exception e) {
        RuleEvaluationResult errorResult = RuleEvaluationResult.builder()
                .evaluationResult("ERROR")
                .evaluationMessage("规则引擎执行异常: " + e.getMessage())
                .errorMessage(e.getMessage())
                .executionTimestamp(java.time.LocalDateTime.now())
                .build();

        return java.util.List.of(errorResult);
    }

    /**
     * 创建错误结果
     */
    private RuleEvaluationResult createErrorResult(Long ruleId, Exception e) {
        return RuleEvaluationResult.builder()
                .ruleId(ruleId)
                .evaluationResult("ERROR")
                .evaluationMessage("规则执行异常: " + e.getMessage())
                .errorMessage(e.getMessage())
                .executionTimestamp(java.time.LocalDateTime.now())
                .build();
    }

    /**
     * 创建验证错误结果
     */
    private RuleEvaluationResult createValidationErrorResult(Long ruleId, RuleValidationResult validation) {
        return RuleEvaluationResult.builder()
                .ruleId(ruleId)
                .evaluationResult("ERROR")
                .evaluationMessage("规则验证失败: " + validation.getErrorMessage())
                .executionTimestamp(java.time.LocalDateTime.now())
                .build();
    }

    /**
     * 创建未找到结果
     */
    private RuleEvaluationResult createNotFoundResult(Long ruleId) {
        return RuleEvaluationResult.builder()
                .ruleId(ruleId)
                .evaluationResult("NOT_FOUND")
                .evaluationMessage("规则不存在或已禁用")
                .executionTimestamp(java.time.LocalDateTime.now())
                .build();
    }

    /**
     * 获取统计值
     */
    private Long getStatisticsValue(String key) {
        return executionStatistics.getOrDefault(key, 0L);
    }

    /**
     * 设置统计值
     */
    private void setStatisticsValue(String key, Long value) {
        executionStatistics.put(key, value);
    }

    /**
     * 计算平均评估时间
     */
    private Double calculateAverageEvaluationTime() {
        Long totalTime = getStatisticsValue("total_evaluation_time");
        Long totalCount = getStatisticsValue("total_evaluations");

        if (totalCount > 0) {
            return totalTime.doubleValue() / totalCount;
        }
        return 0.0;
    }

    /**
     * 更新执行统计
     */
    private void updateExecutionStatistics(String resultType) {
        String key = "total_evaluations";
        executionStatistics.put(key, executionStatistics.getOrDefault(key, 0L) + 1);

        if ("SUCCESS".equals(resultType)) {
            key = "success_evaluations";
            executionStatistics.put(key, executionStatistics.getOrDefault(key, 0L) + 1);
        } else if ("FAILED".equals(resultType)) {
            key = "failed_evaluations";
            executionStatistics.put(key, executionStatistics.getOrDefault(key, 0L) + 1);
        }
    }

    // ==================== 规则编译辅助方法 ====================

    /**
     * 解析条件表达式
     */
    private CompiledCondition parseCondition(String conditionExpression) {
        CompiledCondition condition = new CompiledCondition();
        condition.setOriginalExpression(conditionExpression);

        // 解析操作符和操作数
        if (conditionExpression.contains("==")) {
            condition.setOperator("==");
            String[] parts = conditionExpression.split("==");
            condition.setLeftOperand(parts[0].trim());
            condition.setRightOperand(parts[1].trim());
        } else if (conditionExpression.contains("!=")) {
            condition.setOperator("!=");
            String[] parts = conditionExpression.split("!=");
            condition.setLeftOperand(parts[0].trim());
            condition.setRightOperand(parts[1].trim());
        } else if (conditionExpression.contains(">=")) {
            condition.setOperator(">=");
            String[] parts = conditionExpression.split(">=");
            condition.setLeftOperand(parts[0].trim());
            condition.setRightOperand(parts[1].trim());
        } else if (conditionExpression.contains("<=")) {
            condition.setOperator("<=");
            String[] parts = conditionExpression.split("<=");
            condition.setLeftOperand(parts[0].trim());
            condition.setRightOperand(parts[1].trim());
        } else if (conditionExpression.contains(">")) {
            condition.setOperator(">");
            String[] parts = conditionExpression.split(">");
            condition.setLeftOperand(parts[0].trim());
            condition.setRightOperand(parts[1].trim());
        } else if (conditionExpression.contains("<")) {
            condition.setOperator("<");
            String[] parts = conditionExpression.split("<");
            condition.setLeftOperand(parts[0].trim());
            condition.setRightOperand(parts[1].trim());
        } else if (conditionExpression.contains("&&")) {
            condition.setOperator("&&");
            String[] parts = conditionExpression.split("&&");
            condition.setLeftOperand(parts[0].trim());
            condition.setRightOperand(parts[1].trim());
        } else if (conditionExpression.contains("||")) {
            condition.setOperator("||");
            String[] parts = conditionExpression.split("\\|\\|");
            condition.setLeftOperand(parts[0].trim());
            condition.setRightOperand(parts[1].trim());
        } else {
            // 简单布尔值或变量引用
            condition.setOperator("REF");
            condition.setLeftOperand(conditionExpression.trim());
        }

        return condition;
    }

    /**
     * 解析动作表达式
     */
    private CompiledActionObject parseAction(String actionExpression) {
        CompiledActionObject action = new CompiledActionObject();
        action.setOriginalExpression(actionExpression);

        try {
            // 格式: ACTION_TYPE:param1=value1,param2=value2
            int colonIndex = actionExpression.indexOf(':');
            if (colonIndex > 0) {
                String actionType = actionExpression.substring(0, colonIndex).trim();
                String paramsString = actionExpression.substring(colonIndex + 1).trim();

                action.setActionType(actionType);

                // 解析参数
                if (!paramsString.isEmpty()) {
                    String[] params = paramsString.split(",");
                    for (String param : params) {
                        int eqIndex = param.indexOf('=');
                        if (eqIndex > 0) {
                            String key = param.substring(0, eqIndex).trim();
                            String value = param.substring(eqIndex + 1).trim();
                            action.addParameter(key, value);
                        }
                    }
                }

                // 设置默认值
                action.setPriority(0);
                action.setCritical(false);
                action.setRetryCount(0);
                action.setTimeoutMillis(5000L);
            } else {
                // 只有动作类型
                action.setActionType(actionExpression.trim());
                action.setPriority(0);
                action.setCritical(false);
                action.setRetryCount(0);
                action.setTimeoutMillis(5000L);
            }
        } catch (Exception e) {
            log.error("[规则引擎] 解析动作表达式失败: {}", actionExpression, e);
            action.setActionType("UNKNOWN");
        }

        return action;
    }

    /**
     * 检查部门范围
     */
    private boolean checkDepartmentScope(Map<String, Object> ruleConfig, RuleExecutionContext context) {
        Object departmentScope = ruleConfig.get("departmentScope");
        if (departmentScope == null) {
            // 未配置部门范围，适用于所有部门
            return true;
        }

        if (context.getDepartmentId() == null) {
            return false;
        }

        if (departmentScope instanceof java.util.List) {
            java.util.List<?> scopeList = (java.util.List<?>) departmentScope;
            return scopeList.contains(context.getDepartmentId());
        }

        return departmentScope.equals(context.getDepartmentId());
    }

    /**
     * 检查用户属性
     */
    private boolean checkUserAttributes(Map<String, Object> ruleConfig, RuleExecutionContext context) {
        Object userFilter = ruleConfig.get("userFilter");
        if (userFilter == null) {
            // 未配置用户过滤，适用于所有用户
            return true;
        }

        if (userFilter instanceof Map) {
            Map<?, ?> userFilters = (Map<?, ?>) userFilter;
            for (Map.Entry<?, ?> entry : userFilters.entrySet()) {
                String key = entry.getKey().toString();
                Object expectedValue = entry.getValue();

                Object actualValue = context.getUserAttribute(key);
                if (actualValue == null || !actualValue.equals(expectedValue)) {
                    return false;
                }
            }
            return true;
        }

        return true;
    }

    /**
     * 检查时间范围
     */
    private boolean checkTimeScope(Map<String, Object> ruleConfig, RuleExecutionContext context) {
        Object timeScope = ruleConfig.get("timeScope");
        if (timeScope == null) {
            // 未配置时间范围，适用于所有时间
            return true;
        }

        if (context.getAttendanceDate() == null) {
            return true;
        }

        if (timeScope instanceof Map) {
            Map<?, ?> scope = (Map<?, ?>) timeScope;
            Object startDate = scope.get("startDate");
            Object endDate = scope.get("endDate");

            if (startDate != null) {
                java.time.LocalDate start = java.time.LocalDate.parse(startDate.toString());
                if (context.getAttendanceDate().isBefore(start)) {
                    return false;
                }
            }

            if (endDate != null) {
                java.time.LocalDate end = java.time.LocalDate.parse(endDate.toString());
                if (context.getAttendanceDate().isAfter(end)) {
                    return false;
                }
            }

            return true;
        }

        return true;
    }

    /**
     * 检查规则过滤器
     */
    private boolean checkRuleFilters(Map<String, Object> ruleConfig, RuleExecutionContext context) {
        Object filters = ruleConfig.get("filters");
        if (filters == null) {
            // 未配置过滤器，通过
            return true;
        }

        if (context.getRuleFilters() == null || context.getRuleFilters().isEmpty()) {
            return true;
        }

        if (filters instanceof Map) {
            Map<?, ?> requiredFilters = (Map<?, ?>) filters;
            for (Map.Entry<?, ?> entry : requiredFilters.entrySet()) {
                String key = entry.getKey().toString();
                Object expectedValue = entry.getValue();

                Object actualValue = context.getRuleFilters().get(key);
                if (actualValue == null || !actualValue.equals(expectedValue)) {
                    return false;
                }
            }
            return true;
        }

        return true;
    }

    // ==================== 编译条件内部类 ====================

    /**
     * 编译后的条件对象
     */
    private static class CompiledCondition {
        private String originalExpression;
        private String operator;
        private String leftOperand;
        private String rightOperand;

        public String getOriginalExpression() {
            return originalExpression;
        }

        public void setOriginalExpression(String originalExpression) {
            this.originalExpression = originalExpression;
        }

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }

        public String getLeftOperand() {
            return leftOperand;
        }

        public void setLeftOperand(String leftOperand) {
            this.leftOperand = leftOperand;
        }

        public String getRightOperand() {
            return rightOperand;
        }

        public void setRightOperand(String rightOperand) {
            this.rightOperand = rightOperand;
        }
    }

    /**
     * 编译后的动作对象
     */
    private static class CompiledActionObject {
        private String originalExpression;
        private String actionType;
        private Map<String, String> parameters = new java.util.HashMap<>();
        private Integer priority;
        private Boolean critical;
        private Integer retryCount;
        private Long timeoutMillis;

        public String getOriginalExpression() {
            return originalExpression;
        }

        public void setOriginalExpression(String originalExpression) {
            this.originalExpression = originalExpression;
        }

        public String getActionType() {
            return actionType;
        }

        public void setActionType(String actionType) {
            this.actionType = actionType;
        }

        public Map<String, String> getParameters() {
            return parameters;
        }

        public void addParameter(String key, String value) {
            this.parameters.put(key, value);
        }

        public Integer getPriority() {
            return priority;
        }

        public void setPriority(Integer priority) {
            this.priority = priority;
        }

        public Boolean isCritical() {
            return critical;
        }

        public void setCritical(Boolean critical) {
            this.critical = critical;
        }

        public Integer getRetryCount() {
            return retryCount;
        }

        public void setRetryCount(Integer retryCount) {
            this.retryCount = retryCount;
        }

        public Long getTimeoutMillis() {
            return timeoutMillis;
        }

        public void setTimeoutMillis(Long timeoutMillis) {
            this.timeoutMillis = timeoutMillis;
        }
    }
}
