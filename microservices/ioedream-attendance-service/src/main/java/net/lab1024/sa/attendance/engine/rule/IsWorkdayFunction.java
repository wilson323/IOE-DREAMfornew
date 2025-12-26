package net.lab1024.sa.attendance.engine.rule;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;
import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;

@Slf4j
public class IsWorkdayFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "isWorkday";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        try {
            // 获取日期值（支持两种方式：直接传入LocalDate或传入变量名）
            Object dateObj = arg1.getValue(env);

            if (dateObj == null) {
                log.warn("[规则引擎] IsWorkdayFunction: 日期值为null");
                return AviatorBoolean.FALSE;
            }

            LocalDate date;
            if (dateObj instanceof LocalDate) {
                date = (LocalDate) dateObj;
            } else if (dateObj instanceof String) {
                // 如果是字符串，尝试解析为LocalDate
                String dateStr = (String) dateObj;
                try {
                    date = LocalDate.parse(dateStr);
                } catch (Exception e) {
                    log.warn("[规则引擎] IsWorkdayFunction: 无法解析日期字符串: {}", dateStr);
                    return AviatorBoolean.FALSE;
                }
            } else {
                log.warn("[规则引擎] IsWorkdayFunction: 不支持的日期类型: {}", dateObj.getClass().getName());
                return AviatorBoolean.FALSE;
            }

            // 判断是否为工作日（周一到周五）
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            boolean isWorkday = dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;

            log.debug("[规则引擎] IsWorkdayFunction: date={}, isWorkday={}", date, isWorkday);
            return AviatorBoolean.valueOf(isWorkday);

        } catch (Exception e) {
            log.error("[规则引擎] IsWorkdayFunction: 执行异常", e);
            return AviatorBoolean.FALSE;
        }
    }
}
