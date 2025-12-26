package net.lab1024.sa.attendance.engine.rule.compilation;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.model.CompiledAction;
import net.lab1024.sa.attendance.engine.model.CompiledRule;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 规则编译服务
 * <p>
 * 负责规则条件和动作的编译,包括表达式解析、验证和优化
 * 严格遵循CLAUDE.md全局架构规范,纯Java类
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
public class RuleCompilationService {

    /**
     * 编译规则条件
     *
     * @param ruleCondition 规则条件表达式
     * @return 编译后的规则
     */
    public CompiledRule compileRuleCondition(String ruleCondition) {
        log.debug("[规则编译服务] 编译规则条件: {}", ruleCondition);
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
                    .compileTime(LocalDateTime.now())
                    .compileDuration(System.currentTimeMillis() - startTime)
                    .compilerName("AttendanceRuleEngine")
                    .compilerVersion("1.0.0")
                    .needsRecompile(false)
                    .build();

            log.debug("[规则编译服务] 规则条件编译成功, 耗时: {}ms", result.getCompileDuration());
            return result;

        } catch (Exception e) {
            log.error("[规则编译服务] 编译规则条件失败", e);
            return CompiledRule.failure(null, ruleCondition, e.getMessage());
        }
    }

    /**
     * 编译规则动作
     *
     * @param ruleAction 规则动作表达式
     * @return 编译后的动作
     */
    public CompiledAction compileRuleAction(String ruleAction) {
        log.debug("[规则编译服务] 编译规则动作: {}", ruleAction);
        long startTime = System.currentTimeMillis();

        try {
            // 1. 验证动作表达式
            if (ruleAction == null || ruleAction.trim().isEmpty()) {
                return CompiledAction.failure(null, ruleAction, "规则动作表达式不能为空");
            }

            // 2. 解析动作表达式
            CompiledActionObject compiledAction = parseAction(ruleAction);

            // 3. 构建编译结果
            CompiledAction result = CompiledAction.builder()
                    .ruleId(null)
                    .actionExpression(ruleAction)
                    .compiledAction(compiledAction)
                    .compiled(true)
                    .compileTime(LocalDateTime.now())
                    .compileDuration(System.currentTimeMillis() - startTime)
                    .compilerName("AttendanceRuleEngine")
                    .compilerVersion("1.0.0")
                    .needsRecompile(false)
                    .build();

            log.debug("[规则编译服务] 规则动作编译成功, 耗时: {}ms", result.getCompileDuration());
            return result;

        } catch (Exception e) {
            log.error("[规则编译服务] 编译规则动作失败", e);
            return CompiledAction.failure(null, ruleAction, e.getMessage());
        }
    }

    /**
     * 解析条件表达式
     *
     * @param conditionExpression 条件表达式
     * @return 编译后的条件对象
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

        log.debug("[规则编译服务] 条件解析完成: operator={}, left={}, right={}",
                condition.getOperator(), condition.getLeftOperand(), condition.getRightOperand());

        return condition;
    }

    /**
     * 解析动作表达式
     *
     * @param actionExpression 动作表达式
     * @return 编译后的动作对象
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

            log.debug("[规则编译服务] 动作解析完成: type={}, params={}",
                    action.getActionType(), action.getParameters().size());

        } catch (Exception e) {
            log.error("[规则编译服务] 解析动作表达式失败: {}", actionExpression, e);
            action.setActionType("UNKNOWN");
        }

        return action;
    }

    // ==================== 内部类 ====================

    /**
     * 编译后的条件对象
     */
    public static class CompiledCondition {
        private String originalExpression;
        private String operator;
        private String leftOperand;
        private String rightOperand;

        public String getOriginalExpression() { return originalExpression; }
        public void setOriginalExpression(String originalExpression) { this.originalExpression = originalExpression; }

        public String getOperator() { return operator; }
        public void setOperator(String operator) { this.operator = operator; }

        public String getLeftOperand() { return leftOperand; }
        public void setLeftOperand(String leftOperand) { this.leftOperand = leftOperand; }

        public String getRightOperand() { return rightOperand; }
        public void setRightOperand(String rightOperand) { this.rightOperand = rightOperand; }
    }

    /**
     * 编译后的动作对象
     */
    @Data
    public static class CompiledActionObject {
        private String originalExpression;
        private String actionType;
        private Map<String, String> parameters = new java.util.HashMap<>();
        private Integer priority;
        private Boolean critical;
        private Integer retryCount;
        private Long timeoutMillis;
    }
}
