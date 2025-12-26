package net.lab1024.sa.attendance.engine.rule.execution;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.RuleExecutor;
import net.lab1024.sa.attendance.engine.RuleEvaluatorFactory;
import net.lab1024.sa.attendance.engine.RuleLoader;
import net.lab1024.sa.attendance.engine.RuleValidator;
import net.lab1024.sa.attendance.engine.cache.RuleCacheManager;
import net.lab1024.sa.attendance.engine.model.RuleEvaluationResult;
import net.lab1024.sa.attendance.engine.model.RuleExecutionContext;
import net.lab1024.sa.attendance.engine.model.RuleValidationResult;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 规则执行服务
 * <p>
 * 负责规则评估的核心执行逻辑,包括单个规则评估、批量评估、分类评估等
 * 严格遵循CLAUDE.md全局架构规范,纯Java类
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
public class RuleExecutionService {

    private final RuleLoader ruleLoader;
    private final RuleValidator ruleValidator;
    private final RuleCacheManager cacheManager;
    private final RuleEvaluatorFactory evaluatorFactory;
    private final RuleExecutor ruleExecutor;

    /**
     * 构造函数注入依赖
     */
    public RuleExecutionService(
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

    /**
     * 评估单个规则
     *
     * @param ruleId  规则ID
     * @param context 规则执行上下文
     * @return 规则评估结果
     */
    public RuleEvaluationResult evaluateRule(Long ruleId, RuleExecutionContext context) {
        log.debug("[规则执行服务] 执行单个规则评估, 规则ID: {}", ruleId);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            // 1. 验证规则有效性
            RuleValidationResult validation = ruleValidator.validateRule(ruleId);
            if (!validation.isValid()) {
                return createValidationErrorResult(ruleId, validation);
            }

            // 2. 检查缓存
            RuleEvaluationResult cachedResult = cacheManager.getCachedResult(ruleId, context);
            if (cachedResult != null) {
                log.debug("[规则执行服务] 使用缓存结果, 规则ID: {}", ruleId);
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

            // 5. 缓存结果
            cacheManager.cacheResult(ruleId, context, result);

            stopWatch.stop();
            result.setEvaluationDuration(stopWatch.getLastTaskTimeMillis());
            result.setExecutionTimestamp(java.time.LocalDateTime.now());

            log.debug("[规则执行服务] 规则评估完成, 规则ID: {}, 结果: {}, 耗时: {}ms",
                    ruleId, result.getEvaluationResult(), result.getEvaluationDuration());

            return result;

        } catch (Exception e) {
            log.error("[规则执行服务] 规则评估失败, 规则ID: {}", ruleId, e);
            return createErrorResult(ruleId, e);
        }
    }

    /**
     * 评估多个规则
     *
     * @param ruleIds 规则ID列表
     * @param context 规则执行上下文
     * @return 规则评估结果列表
     */
    public List<RuleEvaluationResult> evaluateRules(List<Long> ruleIds, RuleExecutionContext context) {
        log.debug("[规则执行服务] 执行批量规则评估, 规则数量: {}", ruleIds.size());

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            List<RuleEvaluationResult> results = new ArrayList<>();
            for (Long ruleId : ruleIds) {
                RuleEvaluationResult result = evaluateRule(ruleId, context);
                results.add(result);
            }

            // 按优先级排序结果
            sortByPriority(results);

            stopWatch.stop();
            log.debug("[规则执行服务] 批量规则评估完成, 规则数量: {}, 耗时: {}ms",
                    results.size(), stopWatch.getLastTaskTimeMillis());

            return results;

        } catch (Exception e) {
            log.error("[规则执行服务] 批量规则评估失败", e);
            return createErrorResultList(e);
        }
    }

    /**
     * 按分类评估规则
     *
     * @param ruleCategory 规则分类
     * @param context      规则执行上下文
     * @return 规则评估结果列表
     */
    public List<RuleEvaluationResult> evaluateRulesByCategory(String ruleCategory, RuleExecutionContext context) {
        log.debug("[规则执行服务] 执行分类规则评估, 分类: {}", ruleCategory);

        try {
            // 1. 获取分类适用的规则
            List<Long> categoryRules = ruleLoader.getRulesByCategory(ruleCategory);
            log.debug("[规则执行服务] 分类 {} 规则数量: {}", ruleCategory, categoryRules.size());

            // 2. 执行规则评估
            List<RuleEvaluationResult> results = new ArrayList<>();
            for (Long ruleId : categoryRules) {
                RuleEvaluationResult result = evaluateRule(ruleId, context);
                results.add(result);
            }

            return results;

        } catch (Exception e) {
            log.error("[规则执行服务] 分类规则评估失败, 分类: {}", ruleCategory, e);
            return createErrorResultList(e);
        }
    }

    /**
     * 批量评估规则(多个上下文)
     *
     * @param contexts 规则执行上下文列表
     * @return 规则评估结果列表(扁平化)
     */
    public List<RuleEvaluationResult> batchEvaluateRules(List<RuleExecutionContext> contexts) {
        log.debug("[规则执行服务] 批量评估规则, 上下文数量: {}", contexts.size());

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            List<RuleEvaluationResult> allResults = new ArrayList<>();

            for (RuleExecutionContext context : contexts) {
                // 获取适用的规则
                List<Long> applicableRules = ruleLoader.loadAllActiveRules();

                // 评估所有适用规则
                List<RuleEvaluationResult> contextResults = evaluateRules(applicableRules, context);
                allResults.addAll(contextResults);
            }

            stopWatch.stop();
            log.debug("[规则执行服务] 批量评估完成, 上下文数: {}, 结果数: {}, 耗时: {}ms",
                    contexts.size(), allResults.size(), stopWatch.getLastTaskTimeMillis());

            return allResults;

        } catch (Exception e) {
            log.error("[规则执行服务] 批量评估失败", e);
            return createErrorResultList(e);
        }
    }

    /**
     * 按优先级排序规则评估结果
     *
     * @param results 规则评估结果列表
     */
    private void sortByPriority(List<RuleEvaluationResult> results) {
        if (results == null || results.isEmpty()) {
            return;
        }

        results.sort(Comparator.comparing(RuleEvaluationResult::getRulePriority));
        log.debug("[规则执行服务] 规则结果已按优先级排序, 结果数量: {}", results.size());
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建验证错误结果
     */
    private RuleEvaluationResult createValidationErrorResult(Long ruleId, RuleValidationResult validation) {
        RuleEvaluationResult result = RuleEvaluationResult.builder()
                .ruleId(ruleId)
                .evaluationResult("VALIDATION_FAILED")
                .errorMessage(validation.getErrorMessage())
                .executionTimestamp(java.time.LocalDateTime.now())
                .build();

        log.debug("[规则执行服务] 创建验证错误结果: ruleId={}, error={}", ruleId, validation.getErrorMessage());
        return result;
    }

    /**
     * 创建未找到结果
     */
    private RuleEvaluationResult createNotFoundResult(Long ruleId) {
        RuleEvaluationResult result = RuleEvaluationResult.builder()
                .ruleId(ruleId)
                .evaluationResult("NOT_FOUND")
                .errorMessage("规则配置未找到: " + ruleId)
                .executionTimestamp(java.time.LocalDateTime.now())
                .build();

        log.debug("[规则执行服务] 创建未找到结果: ruleId={}", ruleId);
        return result;
    }

    /**
     * 创建错误结果
     */
    private RuleEvaluationResult createErrorResult(Long ruleId, Exception e) {
        RuleEvaluationResult result = RuleEvaluationResult.builder()
                .ruleId(ruleId)
                .evaluationResult("ERROR")
                .errorMessage(e.getMessage())
                .executionTimestamp(java.time.LocalDateTime.now())
                .build();

        log.debug("[规则执行服务] 创建错误结果: ruleId={}, error={}", ruleId, e.getMessage());
        return result;
    }

    /**
     * 创建错误结果列表
     */
    private List<RuleEvaluationResult> createErrorResultList(Exception e) {
        List<RuleEvaluationResult> errorResults = new ArrayList<>();
        RuleEvaluationResult errorResult = RuleEvaluationResult.builder()
                .evaluationResult("ERROR")
                .errorMessage(e.getMessage())
                .executionTimestamp(java.time.LocalDateTime.now())
                .build();

        errorResults.add(errorResult);
        return errorResults;
    }
}
