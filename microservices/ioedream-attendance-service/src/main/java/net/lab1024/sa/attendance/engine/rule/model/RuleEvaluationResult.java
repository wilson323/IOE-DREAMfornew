package net.lab1024.sa.attendance.engine.rule.model;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 规则评估结果模型
 * <p>
 * 规则引擎执行单个规则的评估结果
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleEvaluationResult {

    /**
     * 规则ID
     */
    private Long ruleId;

    /**
     * 规则编码
     */
    private String ruleCode;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 规则分类
     */
    private String ruleCategory;

    /**
     * 评估结果：MATCH-匹配 NOT_MATCH-不匹配 ERROR-错误
     */
    private String evaluationResult;

    /**
     * 评估消息
     */
    private String evaluationMessage;

    /**
     * 是否执行动作
     */
    private Boolean actionExecuted;

    /**
     * 执行的动作列表
     */
    private java.util.List<ExecutedAction> executedActions;

    /**
     * 规则优先级
     */
    private Integer rulePriority;

    /**
     * 条件匹配详情
     */
    private Map<String, Object> conditionMatchDetails;

    /**
     * 动作执行详情
     */
    private Map<String, Object> actionExecutionDetails;

    /**
     * 评估耗时（毫秒）
     */
    private Long evaluationDuration;

    /**
     * 执行时间戳
     */
    private LocalDateTime executionTimestamp;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 规则版本
     */
    private String ruleVersion;

    /**
     * 是否被后续规则覆盖
     */
    private Boolean overridden;

    /**
     * 覆盖规则的ID
     */
    private Long overridingRuleId;

    /**
     * 异常信息
     */
    private String errorMessage;

    /**
     * 异常堆栈（调试用）
     */
    private String errorStack;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedAttributes;

    /**
     * 执行动作内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutedAction {

        /**
         * 动作ID
         */
        private String actionId;

        /**
         * 动作类型
         */
        private String actionType;

        /**
         * 动作描述
         */
        private String actionDescription;

        /**
         * 动作参数
         */
        private Map<String, Object> actionParameters;

        /**
         * 执行结果：SUCCESS-成功 FAILED-失败 SKIPPED-跳过
         */
        private String executionResult;

        /**
         * 执行消息
         */
        private String executionMessage;

        /**
         * 执行耗时（毫秒）
         */
        private Long executionDuration;

        /**
         * 执行时间戳
         */
        private LocalDateTime executionTimestamp;

        /**
         * 是否关键动作
         */
        private Boolean isCritical;
    }
}
