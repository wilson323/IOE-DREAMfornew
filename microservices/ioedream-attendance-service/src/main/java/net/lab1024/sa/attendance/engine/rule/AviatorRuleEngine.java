package net.lab1024.sa.attendance.engine.rule;
import lombok.extern.slf4j.Slf4j;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class AviatorRuleEngine implements ScheduleRuleEngine {

    public AviatorRuleEngine() {
        registerCustomFunctions();
    }

    @Override
    public Object executeRule(String expression, RuleExecutionContext context) {
        try {
            Expression compiledExpression = AviatorEvaluator.compile(expression);
            Map<String, Object> env = buildEnvironment(context);
            return compiledExpression.execute(env);
        } catch (Exception e) {
            log.error("规则执行失败: {}", expression, e);
            return false;
        }
    }

    @Override
    public boolean validateRule(String expression) {
        try {
            AviatorEvaluator.compile(expression);
            return true;
        } catch (Exception e) {
            log.error("规则验证失败: {}", expression, e);
            return false;
        }
    }

    @Override
    public boolean testRule(String expression, RuleExecutionContext context) {
        try {
            Object result = executeRule(expression, context);
            if (result instanceof Boolean) {
                return (Boolean) result;
            }
            return result != null;
        } catch (Exception e) {
            log.error("规则测试失败: {}", expression, e);
            return false;
        }
    }

    private Map<String, Object> buildEnvironment(RuleExecutionContext context) {
        Map<String, Object> env = new HashMap<>();
        env.put("employeeId", context.getEmployeeId());
        env.put("date", context.getDate());
        env.put("shiftId", context.getShiftId());
        env.put("consecutiveWorkDays", context.getConsecutiveWorkDays());
        env.put("consecutiveRestDays", context.getConsecutiveRestDays());
        env.put("weeklyWorkDays", context.getWeeklyWorkDays());
        env.put("monthlyWorkDays", context.getMonthlyWorkDays());

        if (context.getCustomData() != null) {
            env.putAll(context.getCustomData());
        }

        return env;
    }

    private void registerCustomFunctions() {
        AviatorEvaluator.addFunction(new IsWorkdayFunction());
        AviatorEvaluator.addFunction(new IsWeekendFunction());
        AviatorEvaluator.addFunction(new DayOfWeekFunction());
    }
}
