package net.lab1024.sa.attendance.engine.rule.executor;

import net.lab1024.sa.attendance.engine.model.RuleExecutionContext;
import net.lab1024.sa.attendance.engine.model.RuleEvaluationResult;

import java.util.Map;

/**
 * 规则执行器接口
 * <p>
 * 负责执行规则的动作部分，处理业务逻辑
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface RuleExecutor {

    /**
     * 执行规则动作
     *
     * @param ruleId 规则ID
     * @param ruleAction 规则动作配置
     * @param context 执行上下文
     * @param evaluationResult 规则评估结果
     * @return 执行结果
     */
    RuleExecutionResult execute(Long ruleId, Map<String, Object> ruleAction,
                                RuleExecutionContext context, RuleEvaluationResult evaluationResult);

    /**
     * 批量执行规则动作
     *
     * @param executions 执行请求列表
     * @return 批量执行结果
     */
    BatchExecutionResult executeBatch(java.util.List<RuleExecutionRequest> executions);

    /**
     * 异步执行规则动作
     *
     * @param ruleId 规则ID
     * @param ruleAction 规则动作配置
     * @param context 执行上下文
     * @param evaluationResult 规则评估结果
     * @param callback 执行回调
     */
    void executeAsync(Long ruleId, Map<String, Object> ruleAction,
                      RuleExecutionContext context, RuleEvaluationResult evaluationResult,
                      ExecutionCallback callback);

    /**
     * 检查是否支持指定的动作类型
     *
     * @param actionType 动作类型
     * @return 是否支持
     */
    boolean supportsActionType(String actionType);

    /**
     * 获取支持的动作类型列表
     *
     * @return 动作类型列表
     */
    java.util.List<String> getSupportedActionTypes();

    /**
     * 验证规则动作配置
     *
     * @param ruleAction 规则动作配置
     * @return 验证结果
     */
    RuleExecutionValidationResult validateAction(Map<String, Object> ruleAction);

    /**
     * 获取执行器统计信息
     *
     * @return 执行器统计信息
     */
    ExecutorStatistics getStatistics();

    /**
     * 重置执行器统计信息
     */
    void resetStatistics();

    /**
     * 获取执行器状态
     *
     * @return 执行器状态
     */
    String getStatus();

    /**
     * 规则执行结果类
     */
    class RuleExecutionResult {
        private boolean success;
        private String message;
        private String errorCode;
        private Map<String, Object> resultData;
        private Long executionDuration;
        private java.time.LocalDateTime executionTime;
        private java.util.List<ExecutedAction> executedActions;

        // Constructors
        public RuleExecutionResult() {
            this.executedActions = new java.util.ArrayList<>();
        }

        public RuleExecutionResult(boolean success, String message) {
            this();
            this.success = success;
            this.message = message;
            this.executionTime = java.time.LocalDateTime.now();
        }

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

        public Map<String, Object> getResultData() { return resultData; }
        public void setResultData(Map<String, Object> resultData) { this.resultData = resultData; }

        public Long getExecutionDuration() { return executionDuration; }
        public void setExecutionDuration(Long executionDuration) { this.executionDuration = executionDuration; }

        public java.time.LocalDateTime getExecutionTime() { return executionTime; }
        public void setExecutionTime(java.time.LocalDateTime executionTime) { this.executionTime = executionTime; }

        public java.util.List<ExecutedAction> getExecutedActions() { return executedActions; }
        public void setExecutedActions(java.util.List<ExecutedAction> executedActions) { this.executedActions = executedActions; }

        public void addExecutedAction(ExecutedAction action) {
            this.executedActions.add(action);
        }
    }

    /**
     * 已执行动作类
     */
    class ExecutedAction {
        private String actionId;
        private String actionType;
        private String actionDescription;
        private boolean success;
        private String errorMessage;
        private Long executionDuration;

        // Constructors
        public ExecutedAction() {}

        public ExecutedAction(String actionId, String actionType, boolean success) {
            this.actionId = actionId;
            this.actionType = actionType;
            this.success = success;
        }

        // Getters and Setters
        public String getActionId() { return actionId; }
        public void setActionId(String actionId) { this.actionId = actionId; }

        public String getActionType() { return actionType; }
        public void setActionType(String actionType) { this.actionType = actionType; }

        public String getActionDescription() { return actionDescription; }
        public void setActionDescription(String actionDescription) { this.actionDescription = actionDescription; }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        public Long getExecutionDuration() { return executionDuration; }
        public void setExecutionDuration(Long executionDuration) { this.executionDuration = executionDuration; }
    }

    /**
     * 规则执行请求类
     */
    class RuleExecutionRequest {
        private Long ruleId;
        private Map<String, Object> ruleAction;
        private RuleExecutionContext context;
        private RuleEvaluationResult evaluationResult;

        // Getters and Setters
        public Long getRuleId() { return ruleId; }
        public void setRuleId(Long ruleId) { this.ruleId = ruleId; }

        public Map<String, Object> getRuleAction() { return ruleAction; }
        public void setRuleAction(Map<String, Object> ruleAction) { this.ruleAction = ruleAction; }

        public RuleExecutionContext getContext() { return context; }
        public void setContext(RuleExecutionContext context) { this.context = context; }

        public RuleEvaluationResult getEvaluationResult() { return evaluationResult; }
        public void setEvaluationResult(RuleEvaluationResult evaluationResult) { this.evaluationResult = evaluationResult; }
    }

    /**
     * 批量执行结果类
     */
    class BatchExecutionResult {
        private int totalRequests;
        private int successCount;
        private int failureCount;
        private java.util.List<RuleExecutionResult> results;
        private Map<String, Object> batchStatistics;

        // Constructors
        public BatchExecutionResult() {
            this.results = new java.util.ArrayList<>();
            this.batchStatistics = new java.util.HashMap<>();
        }

        // Getters and Setters
        public int getTotalRequests() { return totalRequests; }
        public void setTotalRequests(int totalRequests) { this.totalRequests = totalRequests; }

        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }

        public int getFailureCount() { return failureCount; }
        public void setFailureCount(int failureCount) { this.failureCount = failureCount; }

        public java.util.List<RuleExecutionResult> getResults() { return results; }
        public void setResults(java.util.List<RuleExecutionResult> results) { this.results = results; }

        public Map<String, Object> getBatchStatistics() { return batchStatistics; }
        public void setBatchStatistics(Map<String, Object> batchStatistics) { this.batchStatistics = batchStatistics; }

        public void addResult(RuleExecutionResult result) {
            this.results.add(result);
            if (result.isSuccess()) {
                this.successCount++;
            } else {
                this.failureCount++;
            }
            this.totalRequests++;
        }
    }

    /**
     * 执行回调接口
     */
    @FunctionalInterface
    interface ExecutionCallback {
        void onComplete(RuleExecutionResult result);
    }

    /**
     * 规则执行验证结果类
     */
    class RuleExecutionValidationResult {
        private boolean valid;
        private String errorMessage;
        private java.util.List<String> warnings;
        private Map<String, Object> validationDetails;

        // Constructors
        public RuleExecutionValidationResult() {
            this.warnings = new java.util.ArrayList<>();
            this.validationDetails = new java.util.HashMap<>();
        }

        public RuleExecutionValidationResult(boolean valid, String errorMessage) {
            this();
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        // Getters and Setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        public java.util.List<String> getWarnings() { return warnings; }
        public void setWarnings(java.util.List<String> warnings) { this.warnings = warnings; }

        public Map<String, Object> getValidationDetails() { return validationDetails; }
        public void setValidationDetails(Map<String, Object> validationDetails) { this.validationDetails = validationDetails; }

        public void addWarning(String warning) {
            this.warnings.add(warning);
        }

        public void addValidationDetail(String key, Object value) {
            this.validationDetails.put(key, value);
        }
    }

    /**
     * 执行器统计信息类
     */
    class ExecutorStatistics {
        private long totalExecutions;
        private long successfulExecutions;
        private long failedExecutions;
        private long averageExecutionTime;
        private long maxExecutionTime;
        private long minExecutionTime;
        private Map<String, Long> actionTypeUsage;
        private java.time.LocalDateTime lastExecutionTime;

        // Constructors
        public ExecutorStatistics() {
            this.actionTypeUsage = new java.util.concurrent.ConcurrentHashMap<>();
            this.minExecutionTime = Long.MAX_VALUE;
        }

        // Getters and Setters
        public long getTotalExecutions() { return totalExecutions; }
        public void setTotalExecutions(long totalExecutions) { this.totalExecutions = totalExecutions; }

        public long getSuccessfulExecutions() { return successfulExecutions; }
        public void setSuccessfulExecutions(long successfulExecutions) { this.successfulExecutions = successfulExecutions; }

        public long getFailedExecutions() { return failedExecutions; }
        public void setFailedExecutions(long failedExecutions) { this.failedExecutions = failedExecutions; }

        public long getAverageExecutionTime() { return averageExecutionTime; }
        public void setAverageExecutionTime(long averageExecutionTime) { this.averageExecutionTime = averageExecutionTime; }

        public long getMaxExecutionTime() { return maxExecutionTime; }
        public void setMaxExecutionTime(long maxExecutionTime) { this.maxExecutionTime = maxExecutionTime; }

        public long getMinExecutionTime() { return minExecutionTime; }
        public void setMinExecutionTime(long minExecutionTime) { this.minExecutionTime = minExecutionTime; }

        public Map<String, Long> getActionTypeUsage() { return actionTypeUsage; }
        public void setActionTypeUsage(Map<String, Long> actionTypeUsage) { this.actionTypeUsage = actionTypeUsage; }

        public java.time.LocalDateTime getLastExecutionTime() { return lastExecutionTime; }
        public void setLastExecutionTime(java.time.LocalDateTime lastExecutionTime) { this.lastExecutionTime = lastExecutionTime; }

        public double getSuccessRate() {
            if (totalExecutions == 0) return 0.0;
            return (double) successfulExecutions / totalExecutions * 100;
        }

        @Override
        public String toString() {
            return String.format(
                "ExecutorStatistics{total=%d, success=%d, failed=%d, successRate=%.2f%%, avgTime=%dms}",
                totalExecutions, successfulExecutions, failedExecutions, getSuccessRate(), averageExecutionTime
            );
        }
    }
}
