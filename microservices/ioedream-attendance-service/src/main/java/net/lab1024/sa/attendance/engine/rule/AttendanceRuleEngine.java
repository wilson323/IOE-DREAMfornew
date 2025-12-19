package net.lab1024.sa.attendance.engine.rule;

import net.lab1024.sa.attendance.engine.rule.model.RuleExecutionContext;
import net.lab1024.sa.attendance.engine.rule.model.RuleEvaluationResult;
import net.lab1024.sa.attendance.engine.rule.model.RuleValidationResult;
import net.lab1024.sa.attendance.engine.rule.model.CompiledRule;
import net.lab1024.sa.attendance.engine.rule.model.CompiledAction;
import net.lab1024.sa.attendance.engine.rule.model.RuleExecutionStatistics;

import java.util.List;
import java.util.Map;

/**
 * 考勤规则引擎接口
 * <p>
 * 提供灵活的考勤规则执行引擎，支持多种规则类型和条件判断
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface AttendanceRuleEngine {

    /**
     * 执行规则评估
     *
     * @param context 规则执行上下文
     * @return 规则评估结果
     */
    List<RuleEvaluationResult> evaluateRules(RuleExecutionContext context);

    /**
     * 执行特定分类的规则
     *
     * @param ruleCategory 规则分类
     * @param context 规则执行上下文
     * @return 规则评估结果
     */
    List<RuleEvaluationResult> evaluateRulesByCategory(String ruleCategory, RuleExecutionContext context);

    /**
     * 执行单个规则
     *
     * @param ruleId 规则ID
     * @param context 规则执行上下文
     * @return 规则评估结果
     */
    RuleEvaluationResult evaluateRule(Long ruleId, RuleExecutionContext context);

    /**
     * 验证规则有效性
     *
     * @param ruleId 规则ID
     * @return 验证结果
     */
    RuleValidationResult validateRule(Long ruleId);

    /**
     * 获取适用的规则列表
     *
     * @param context 规则执行上下文
     * @return 适用的规则列表
     */
    List<Long> getApplicableRules(RuleExecutionContext context);

    /**
     * 编译规则条件
     *
     * @param ruleCondition 规则条件表达式
     * @return 编译后的规则
     */
    CompiledRule compileRuleCondition(String ruleCondition);

    /**
     * 编译规则动作
     *
     * @param ruleAction 规则动作配置
     * @return 编译后的动作
     */
    CompiledAction compileRuleAction(String ruleAction);

    /**
     * 批量执行规则
     *
     * @param contexts 批量规则执行上下文
     * @return 批量评估结果
     */
    List<RuleEvaluationResult> batchEvaluateRules(List<RuleExecutionContext> contexts);

    /**
     * 获取规则执行统计
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 执行统计信息
     */
    RuleExecutionStatistics getExecutionStatistics(long startTime, long endTime);

    /**
     * 清除规则缓存
     */
    void clearRuleCache();

    /**
     * 预热规则缓存
     *
     * @param ruleIds 规则ID列表
     */
    void warmUpRuleCache(List<Long> ruleIds);
}