package net.lab1024.sa.attendance.engine.optimization;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.model.RuleEvaluationResult;
import net.lab1024.sa.attendance.engine.model.RuleExecutionContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 考勤规则链编排器
 * <p>
 * 支持复杂的规则链组合和执行策略优化
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-12-26
 */
@Slf4j
@Component
public class AttendanceRuleChainOrchestrator {

    /**
     * 执行策略枚举
     */
    public enum ExecutionStrategy {
        /** 全部执行（ALL）- 执行所有规则，收集所有结果 */
        ALL,
        /** 首个匹配（FIRST_MATCH）- 遇到第一个匹配规则即停止 */
        FIRST_MATCH,
        /** 优先级最高（HIGHEST_PRIORITY）- 只执行优先级最高的规则 */
        HIGHEST_PRIORITY,
        /** 权重组合（WEIGHTED_COMBINATION）- 根据权重组合多个规则结果 */
        WEIGHTED_COMBINATION,
        /** 短路执行（SHORT_CIRCUIT）- 遇到失败立即停止 */
        SHORT_CIRCUIT
    }

    /**
     * 规则链配置
     */
    public static class RuleChainConfig {
        private String chainName;
        private ExecutionStrategy strategy;
        private List<Long> ruleIds;
        private Map<Long, Integer> ruleWeights; // 规则权重
        private Boolean continueOnError;
        private Integer timeout; // 超时时间（毫秒）

        // Getters and Setters
        public String getChainName() { return chainName; }
        public void setChainName(String chainName) { this.chainName = chainName; }

        public ExecutionStrategy getStrategy() { return strategy; }
        public void setStrategy(ExecutionStrategy strategy) { this.strategy = strategy; }

        public List<Long> getRuleIds() { return ruleIds; }
        public void setRuleIds(List<Long> ruleIds) { this.ruleIds = ruleIds; }

        public Map<Long, Integer> getRuleWeights() { return ruleWeights; }
        public void setRuleWeights(Map<Long, Integer> ruleWeights) { this.ruleWeights = ruleWeights; }

        public Boolean getContinueOnError() { return continueOnError; }
        public void setContinueOnError(Boolean continueOnError) { this.continueOnError = continueOnError; }

        public Integer getTimeout() { return timeout; }
        public void setTimeout(Integer timeout) { this.timeout = timeout; }
    }

    /**
     * 规则链执行结果
     */
    public static class ChainExecutionResult {
        private String chainName;
        private List<RuleEvaluationResult> results;
        private Boolean success;
        private String errorMessage;
        private Long executionTime;
        private ExecutionStrategy strategy;

        // Getters and Setters
        public String getChainName() { return chainName; }
        public void setChainName(String chainName) { this.chainName = chainName; }

        public List<RuleEvaluationResult> getResults() { return results; }
        public void setResults(List<RuleEvaluationResult> results) { this.results = results; }

        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        public Long getExecutionTime() { return executionTime; }
        public void setExecutionTime(Long executionTime) { this.executionTime = executionTime; }

        public ExecutionStrategy getStrategy() { return strategy; }
        public void setStrategy(ExecutionStrategy strategy) { this.strategy = strategy; }
    }

    // 规则链缓存
    private final Map<String, RuleChainConfig> chainCache = new ConcurrentHashMap<>();

    /**
     * 执行规则链
     *
     * @param chainConfig 规则链配置
     * @param context 规则执行上下文
     * @return 规则链执行结果
     */
    public ChainExecutionResult executeChain(RuleChainConfig chainConfig, RuleExecutionContext context) {
        log.info("[规则链编排器] 开始执行规则链: chainName={}, strategy={}, ruleCount={}",
                chainConfig.getChainName(), chainConfig.getStrategy(), chainConfig.getRuleIds().size());

        long startTime = System.currentTimeMillis();
        ChainExecutionResult result = new ChainExecutionResult();
        result.setChainName(chainConfig.getChainName());
        result.setStrategy(chainConfig.getStrategy());

        try {
            List<RuleEvaluationResult> results = new ArrayList<>();

            // 根据执行策略执行规则链
            switch (chainConfig.getStrategy()) {
                case ALL:
                    results = executeAllRules(chainConfig, context);
                    break;

                case FIRST_MATCH:
                    results = executeFirstMatch(chainConfig, context);
                    break;

                case HIGHEST_PRIORITY:
                    results = executeHighestPriority(chainConfig, context);
                    break;

                case WEIGHTED_COMBINATION:
                    results = executeWeightedCombination(chainConfig, context);
                    break;

                case SHORT_CIRCUIT:
                    results = executeShortCircuit(chainConfig, context);
                    break;

                default:
                    log.warn("[规则链编排器] 未知的执行策略: {}", chainConfig.getStrategy());
                    results = executeAllRules(chainConfig, context);
            }

            result.setResults(results);
            result.setSuccess(true);
            result.setExecutionTime(System.currentTimeMillis() - startTime);

            log.info("[规则链编排器] 规则链执行成功: chainName={}, resultCount={}, executionTime={}ms",
                    chainConfig.getChainName(), results.size(), result.getExecutionTime());

            return result;

        } catch (Exception e) {
            log.error("[规则链编排器] 规则链执行失败: chainName={}", chainConfig.getChainName(), e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            result.setExecutionTime(System.currentTimeMillis() - startTime);
            return result;
        }
    }

    /**
     * 执行所有规则（ALL策略）
     */
    private List<RuleEvaluationResult> executeAllRules(RuleChainConfig config, RuleExecutionContext context) {
        List<RuleEvaluationResult> results = new ArrayList<>();

        for (Long ruleId : config.getRuleIds()) {
            try {
                // TODO: 调用规则引擎执行规则
                // RuleEvaluationResult result = ruleEngine.evaluateRule(ruleId, context);
                // results.add(result);

                if (!config.getContinueOnError()) {
                    // 如果不继续执行错误，检查最后一个结果
                    if (!results.isEmpty()) {
                        RuleEvaluationResult lastResult = results.get(results.size() - 1);
                        if ("ERROR".equals(lastResult.getEvaluationResult())) {
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                log.error("[规则链编排器] 规则执行异常: ruleId={}", ruleId, e);
                if (!config.getContinueOnError()) {
                    break;
                }
            }
        }

        return results;
    }

    /**
     * 执行首个匹配规则（FIRST_MATCH策略）
     */
    private List<RuleEvaluationResult> executeFirstMatch(RuleChainConfig config, RuleExecutionContext context) {
        List<RuleEvaluationResult> results = new ArrayList<>();

        for (Long ruleId : config.getRuleIds()) {
            try {
                // TODO: 调用规则引擎执行规则
                // RuleEvaluationResult result = ruleEngine.evaluateRule(ruleId, context);
                // results.add(result);

                // 检查是否匹配
                if (!results.isEmpty()) {
                    RuleEvaluationResult result = results.get(results.size() - 1);
                    if ("MATCH".equals(result.getEvaluationResult())) {
                        log.info("[规则链编排器] 找到首个匹配规则: ruleId={}", ruleId);
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("[规则链编排器] 规则执行异常: ruleId={}", ruleId, e);
                if (!config.getContinueOnError()) {
                    break;
                }
            }
        }

        return results;
    }

    /**
     * 执行优先级最高的规则（HIGHEST_PRIORITY策略）
     */
    private List<RuleEvaluationResult> executeHighestPriority(RuleChainConfig config, RuleExecutionContext context) {
        // TODO: 找到优先级最高的规则并执行
        log.warn("[规则链编排器] HIGHEST_PRIORITY策略尚未实现");
        return new ArrayList<>();
    }

    /**
     * 执行权重组合规则（WEIGHTED_COMBINATION策略）
     */
    private List<RuleEvaluationResult> executeWeightedCombination(RuleChainConfig config, RuleExecutionContext context) {
        // TODO: 根据权重组合规则结果
        log.warn("[规则链编排器] WEIGHTED_COMBINATION策略尚未实现");
        return new ArrayList<>();
    }

    /**
     * 执行短路规则（SHORT_CIRCUIT策略）
     */
    private List<RuleEvaluationResult> executeShortCircuit(RuleChainConfig config, RuleExecutionContext context) {
        List<RuleEvaluationResult> results = new ArrayList<>();

        for (Long ruleId : config.getRuleIds()) {
            try {
                // TODO: 调用规则引擎执行规则
                // RuleEvaluationResult result = ruleEngine.evaluateRule(ruleId, context);
                // results.add(result);

                // 检查是否失败
                if (!results.isEmpty()) {
                    RuleEvaluationResult result = results.get(results.size() - 1);
                    if ("ERROR".equals(result.getEvaluationResult())) {
                        log.warn("[规则链编排器] 规则执行失败，停止执行: ruleId={}", ruleId);
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("[规则链编排器] 规则执行异常，停止执行: ruleId={}", ruleId, e);
                break;
            }
        }

        return results;
    }

    /**
     * 注册规则链
     *
     * @param chainName 规则链名称
     * @param config 规则链配置
     */
    public void registerRuleChain(String chainName, RuleChainConfig config) {
        log.info("[规则链编排器] 注册规则链: chainName={}, ruleCount={}", chainName, config.getRuleIds().size());
        config.setChainName(chainName);
        chainCache.put(chainName, config);
    }

    /**
     * 获取规则链配置
     *
     * @param chainName 规则链名称
     * @return 规则链配置
     */
    public RuleChainConfig getRuleChain(String chainName) {
        return chainCache.get(chainName);
    }

    /**
     * 移除规则链
     *
     * @param chainName 规则链名称
     */
    public void unregisterRuleChain(String chainName) {
        log.info("[规则链编排器] 移除规则链: chainName={}", chainName);
        chainCache.remove(chainName);
    }

    /**
     * 获取所有规则链名称
     *
     * @return 规则链名称列表
     */
    public List<String> getAllRuleChainNames() {
        return new ArrayList<>(chainCache.keySet());
    }
}
