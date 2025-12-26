package net.lab1024.sa.attendance.realtime.rule;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.realtime.model.CalculationRule;
import net.lab1024.sa.attendance.realtime.model.RuleRegistrationResult;
import net.lab1024.sa.attendance.realtime.model.RuleUnregistrationResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 计算规则管理服务
 * <p>
 * 职责：
 * 1. 管理计算规则（注册、注销、查询）
 * 2. 验证规则有效性
 * 3. 提供规则执行接口
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-26
 */
@Slf4j
@Service
public class CalculationRuleManagementService {

    /**
     * 存储已注册的计算规则
     * Key: ruleId, Value: CalculationRule
     */
    private final Map<String, CalculationRule> calculationRules = new ConcurrentHashMap<>();

    /**
     * 注册计算规则
     * <p>
     * P0级核心功能：注册新的计算规则
     * </p>
     *
     * @param calculationRule 计算规则
     * @return 规则注册结果
     */
    public RuleRegistrationResult registerCalculationRule(CalculationRule calculationRule) {
        log.debug("[规则管理] 注册计算规则: {}", calculationRule.getRuleId());

        try {
            calculationRules.put(calculationRule.getRuleId(), calculationRule);

            // 验证规则
            if (!validateCalculationRule(calculationRule)) {
                calculationRules.remove(calculationRule.getRuleId());
                return RuleRegistrationResult.builder()
                        .ruleId(calculationRule.getRuleId())
                        .registrationSuccessful(false)
                        .errorMessage("规则验证失败")
                        .build();
            }

            return RuleRegistrationResult.builder()
                    .ruleId(calculationRule.getRuleId())
                    .registrationSuccessful(true)
                    .registrationTime(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("[规则管理] 注册计算规则失败", e);
            return RuleRegistrationResult.builder()
                    .ruleId(calculationRule.getRuleId())
                    .registrationSuccessful(false)
                    .errorMessage("注册失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 注销计算规则
     * <p>
     * P0级核心功能：注销已注册的计算规则
     * </p>
     *
     * @param ruleId 规则ID
     * @return 规则注销结果
     */
    public RuleUnregistrationResult unregisterCalculationRule(String ruleId) {
        log.debug("[规则管理] 注销计算规则: {}", ruleId);

        try {
            CalculationRule removedRule = calculationRules.remove(ruleId);

            if (removedRule != null) {
                return RuleUnregistrationResult.builder()
                        .ruleId(ruleId)
                        .unregistrationSuccessful(true)
                        .unregistrationTime(LocalDateTime.now())
                        .build();
            } else {
                return RuleUnregistrationResult.builder()
                        .ruleId(ruleId)
                        .unregistrationSuccessful(false)
                        .errorMessage("规则不存在")
                        .build();
            }

        } catch (Exception e) {
            log.error("[规则管理] 注销计算规则失败", e);
            return RuleUnregistrationResult.builder()
                    .ruleId(ruleId)
                    .unregistrationSuccessful(false)
                    .errorMessage("注销失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 获取已注册的规则列表
     *
     * @return 规则列表
     */
    public Map<String, CalculationRule> getRegisteredRules() {
        return new ConcurrentHashMap<>(calculationRules);
    }

    /**
     * 根据规则ID获取规则
     *
     * @param ruleId 规则ID
     * @return 计算规则，如果不存在则返回null
     */
    public CalculationRule getRule(String ruleId) {
        return calculationRules.get(ruleId);
    }

    /**
     * 检查规则是否存在
     *
     * @param ruleId 规则ID
     * @return true-存在，false-不存在
     */
    public boolean hasRule(String ruleId) {
        return calculationRules.containsKey(ruleId);
    }

    /**
     * 获取已注册规则数量
     *
     * @return 规则数量
     */
    public int getRuleCount() {
        return calculationRules.size();
    }

    /**
     * 清空所有规则
     * <p>
     * 警告：此操作将删除所有已注册的规则
     * </p>
     */
    public void clearAllRules() {
        log.warn("[规则管理] 清空所有规则，当前规则数量: {}", calculationRules.size());
        calculationRules.clear();
    }

    /**
     * 验证计算规则
     * <p>
     * P0级核心功能：验证规则的有效性
     * </p>
     *
     * @param rule 计算规则
     * @return true-有效，false-无效
     */
    private boolean validateCalculationRule(CalculationRule rule) {
        // 基础验证：规则ID和规则表达式不能为空
        if (rule.getRuleId() == null || rule.getRuleExpression() == null) {
            log.warn("[规则管理] 规则验证失败: ruleId或ruleExpression为空");
            return false;
        }

        // TODO: 添加更复杂的规则验证逻辑
        // 示例：
        // 1. 验证规则表达式语法
        // 2. 验证规则参数完整性
        // 3. 验证规则优先级合法性
        // 4. 验证规则执行条件

        return true;
    }
}
