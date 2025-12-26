package net.lab1024.sa.attendance.engine.function;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorDouble;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;

/**
 * 获取休息天数函数
 * <p>
 * 使用示例：
 * - get_rest_days(employeeId, scheduleDate)
 * - get_rest_days(123, '2025-01-30')
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public class GetRestDaysFunction extends AbstractFunction {

    @Override
    public String getName() {
        return "get_rest_days";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        // 获取员工ID
        Number employeeIdObj = (Number) arg1.getValue(env);
        Long employeeId = employeeIdObj != null ? employeeIdObj.longValue() : null;

        // 获取当前日期
        Object dateObj = arg2.getValue(env);

        // 从上下文中获取休息天数
        @SuppressWarnings("unchecked")
        Map<String, Object> context = (Map<String, Object>) env.get("context");

        if (employeeId == null || context == null) {
            return AviatorDouble.valueOf(0);
        }

        // 构建缓存键
        String key = "rest_days_" + employeeId;

        // 从上下文中获取休息天数
        Object restDaysObj = context.get(key);
        int restDays = restDaysObj instanceof Number ? ((Number) restDaysObj).intValue() : 0;

        return AviatorDouble.valueOf(restDays);
    }
}
