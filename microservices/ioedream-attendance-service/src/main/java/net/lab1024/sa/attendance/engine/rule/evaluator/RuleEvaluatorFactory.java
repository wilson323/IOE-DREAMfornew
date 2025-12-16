package net.lab1024.sa.attendance.engine.rule.evaluator;

import net.lab1024.sa.attendance.engine.rule.model.RuleEvaluationResult;
import net.lab1024.sa.attendance.engine.rule.model.RuleExecutionContext;

import java.util.Map;

/**
 * 规则评估器工厂接口
 * <p>
 * 负责创建和管理不同类型的规则评估器
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface RuleEvaluatorFactory {

    /**
     * 创建规则评估器
     *
     * @param ruleType 规则类型
     * @return 规则评估器实例
     */
    RuleEvaluator createEvaluator(String ruleType);

    /**
     * 创建规则评估器（带参数）
     *
     * @param ruleType 规则类型
     * @param parameters 规则参数
     * @return 规则评估器实例
     */
    RuleEvaluator createEvaluator(String ruleType, Map<String, Object> parameters);

    /**
     * 获取支持的规则类型列表
     *
     * @return 规则类型列表
     */
    java.util.List<String> getSupportedRuleTypes();

    /**
     * 检查是否支持指定的规则类型
     *
     * @param ruleType 规则类型
     * @return 是否支持
     */
    boolean isSupportedRuleType(String ruleType);

    /**
     * 注册规则评估器
     *
     * @param ruleType 规则类型
     * @param evaluator 评估器实例
     */
    void registerEvaluator(String ruleType, RuleEvaluator evaluator);

    /**
     * 注销规则评估器
     *
     * @param ruleType 规则类型
     */
    void unregisterEvaluator(String ruleType);

    /**
     * 获取评估器统计信息
     *
     * @return 评估器统计信息
     */
    EvaluatorStatistics getStatistics();

    /**
     * 重置评估器统计信息
     */
    void resetStatistics();

    /**
     * 预热评估器工厂
     */
    void warmUp();

    /**
     * 销毁评估器工厂
     */
    void destroy();

    /**
     * 评估器统计信息内部类
     */
    class EvaluatorStatistics {
        private int totalEvaluators;
        private int activeEvaluators;
        private Map<String, Long> usageCount;
        private Map<String, Long> averageExecutionTime;
        private long totalEvaluations;
        private long successfulEvaluations;
        private long failedEvaluations;

        public EvaluatorStatistics() {
            this.usageCount = new java.util.concurrent.ConcurrentHashMap<>();
            this.averageExecutionTime = new java.util.concurrent.ConcurrentHashMap<>();
        }

        // Getters and Setters
        public int getTotalEvaluators() { return totalEvaluators; }
        public void setTotalEvaluators(int totalEvaluators) { this.totalEvaluators = totalEvaluators; }

        public int getActiveEvaluators() { return activeEvaluators; }
        public void setActiveEvaluators(int activeEvaluators) { this.activeEvaluators = activeEvaluators; }

        public Map<String, Long> getUsageCount() { return usageCount; }
        public void setUsageCount(Map<String, Long> usageCount) { this.usageCount = usageCount; }

        public Map<String, Long> getAverageExecutionTime() { return averageExecutionTime; }
        public void setAverageExecutionTime(Map<String, Long> averageExecutionTime) { this.averageExecutionTime = averageExecutionTime; }

        public long getTotalEvaluations() { return totalEvaluations; }
        public void setTotalEvaluations(long totalEvaluations) { this.totalEvaluations = totalEvaluations; }

        public long getSuccessfulEvaluations() { return successfulEvaluations; }
        public void setSuccessfulEvaluations(long successfulEvaluations) { this.successfulEvaluations = successfulEvaluations; }

        public long getFailedEvaluations() { return failedEvaluations; }
        public void setFailedEvaluations(long failedEvaluations) { this.failedEvaluations = failedEvaluations; }

        @Override
        public String toString() {
            return String.format(
                "EvaluatorStatistics{total=%d, active=%d, totalEvals=%d, success=%d, failed=%d}",
                totalEvaluators, activeEvaluators, totalEvaluations, successfulEvaluations, failedEvaluations
            );
        }
    }

    /**
     * 规则评估器接口
     */
    interface RuleEvaluator {

        /**
         * 评估规则
         *
         * @param ruleId 规则ID
         * @param ruleConfig 规则配置
         * @param context 执行上下文
         * @return 评估结果
         */
        RuleEvaluationResult evaluate(Long ruleId, Map<String, Object> ruleConfig, RuleExecutionContext context);

        /**
         * 获取评估器类型
         *
         * @return 评估器类型
         */
        String getEvaluatorType();

        /**
         * 检查是否支持指定的规则类型
         *
         * @param ruleType 规则类型
         * @return 是否支持
         */
        boolean supports(String ruleType);

        /**
         * 初始化评估器
         *
         * @param parameters 初始化参数
         */
        void initialize(Map<String, Object> parameters);

        /**
         * 销毁评估器
         */
        void destroy();

        /**
         * 获取评估器状态
         *
         * @return 评估器状态
         */
        String getStatus();
    }
}