package net.lab1024.sa.attendance.engine.rule.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.rule.AttendanceRuleEngine;
import net.lab1024.sa.attendance.engine.rule.model.*;
import net.lab1024.sa.attendance.engine.rule.evaluator.RuleEvaluatorFactory;
import net.lab1024.sa.attendance.engine.rule.executor.RuleExecutor;
import net.lab1024.sa.attendance.engine.rule.cache.RuleCacheManager;
import net.lab1024.sa.attendance.engine.rule.loader.RuleLoader;
import net.lab1024.sa.attendance.engine.rule.validator.RuleValidator;

import org.springframework.util.StopWatch;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
            List<RuleEvaluationResult> results = new java.util.ArrayList<>();
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
            List<RuleEvaluationResult> results = new java.util.ArrayList<>();
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
            List<Long> applicableRules = new java.util.ArrayList<>();
            for (Long ruleId : allRules) {
                if (isRuleApplicable(ruleId, context)) {
                    applicableRules.add(ruleId);
                }
            }

            return applicableRules;

        } catch (Exception e) {
            log.error("[规则引擎] 获取适用规则失败", e);
            return new java.util.ArrayList<>();
        }
    }

    @Override
    public CompiledRule compileRuleCondition(String ruleCondition) {
        log.debug("[规则引擎] 编译规则条件: {}", ruleCondition);
        try {
            // TODO: 实现规则条件编译逻辑
            return new CompiledRule();
        } catch (Exception e) {
            log.error("[规则引擎] 编译规则条件失败", e);
            return null;
        }
    }

    @Override
    public CompiledAction compileRuleAction(String ruleAction) {
        log.debug("[规则引擎] 编译规则动作: {}", ruleAction);
        try {
            // TODO: 实现规则动作编译逻辑
            return new CompiledAction();
        } catch (Exception e) {
            log.error("[规则引擎] 编译规则动作失败", e);
            return null;
        }
    }

    @Override
    public List<RuleEvaluationResult> batchEvaluateRules(List<RuleExecutionContext> contexts) {
        log.info("[规则引擎] 批量执行规则评估, 上下文数量: {}", contexts.size());

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        List<RuleEvaluationResult> allResults = new java.util.ArrayList<>();

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
            // TODO: 实现规则适用性检查逻辑
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
}