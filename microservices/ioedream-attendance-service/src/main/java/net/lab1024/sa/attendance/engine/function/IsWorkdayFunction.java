package net.lab1024.sa.attendance.engine.function;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;

/**
 * 判断是否为工作日的函数
 * <p>
 * 使用示例：
 * - is_workday(scheduleDate)
 * - is_workday('2025-01-30')
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public class IsWorkdayFunction extends AbstractFunction {

    @Override
    public String getName() {
        return "is_workday";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        Object dateObj = arg1.getValue(env);

        LocalDate date;
        if (dateObj instanceof LocalDate) {
            date = (LocalDate) dateObj;
        } else if (dateObj instanceof String) {
            date = LocalDate.parse((String) dateObj);
        } else {
            throw new IllegalArgumentException("is_workday函数参数类型错误，需要LocalDate或String");
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        boolean isWorkday = dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;

        return AviatorBoolean.valueOf(isWorkday);
    }
}
