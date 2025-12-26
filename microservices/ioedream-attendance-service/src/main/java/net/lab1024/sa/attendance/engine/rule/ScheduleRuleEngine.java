package net.lab1024.sa.attendance.engine.rule;

import net.lab1024.sa.attendance.engine.rule.RuleExecutionContext;

public interface ScheduleRuleEngine {
    Object executeRule(String expression, RuleExecutionContext context);
    boolean validateRule(String expression);
    boolean testRule(String expression, RuleExecutionContext context);
}
