package net.lab1024.sa.attendance.engine.rule.validator;

import net.lab1024.sa.attendance.engine.rule.model.RuleValidationResult;

/**
 * 规则验证器接口
 * <p>
 * 负责验证规则的有效性、完整性和正确性
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface RuleValidator {

    /**
     * 验证规则有效性
     *
     * @param ruleId 规则ID
     * @return 验证结果
     */
    RuleValidationResult validateRule(Long ruleId);

    /**
     * 验证规则条件表达式
     *
     * @param ruleCondition 规则条件表达式
     * @return 验证结果
     */
    RuleValidationResult validateRuleCondition(String ruleCondition);

    /**
     * 验证规则动作配置
     *
     * @param ruleAction 规则动作配置
     * @return 验证结果
     */
    RuleValidationResult validateRuleAction(String ruleAction);

    /**
     * 验证规则语法
     *
     * @param ruleCondition 规则条件
     * @param ruleAction 规则动作
     * @return 验证结果
     */
    RuleValidationResult validateRuleSyntax(String ruleCondition, String ruleAction);

    /**
     * 验证规则参数
     *
     * @param ruleType 规则类型
     * @param parameters 参数Map
     * @return 验证结果
     */
    RuleValidationResult validateRuleParameters(String ruleType, java.util.Map<String, Object> parameters);

    /**
     * 验证规则权限
     *
     * @param ruleId 规则ID
     * @param userId 用户ID
     * @return 验证结果
     */
    RuleValidationResult validateRulePermission(Long ruleId, Long userId);

    /**
     * 验证规则时间范围
     *
     * @param ruleId 规则ID
     * @param checkTime 检查时间
     * @return 验证结果
     */
    RuleValidationResult validateRuleTimeRange(Long ruleId, java.time.LocalDateTime checkTime);

    /**
     * 验证规则依赖
     *
     * @param ruleId 规则ID
     * @return 验证结果
     */
    RuleValidationResult validateRuleDependencies(Long ruleId);
}
