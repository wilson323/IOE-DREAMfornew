package net.lab1024.sa.attendance.engine.rule;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;
import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;

@Slf4j
public class DayOfWeekFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "dayOfWeek";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        try {
            // 获取日期值（支持两种方式：直接传入LocalDate或传入变量名）
            Object dateObj = arg1.getValue(env);

            if (dateObj == null) {
                log.warn("[规则引擎] DayOfWeekFunction: 日期值为null，返回默认值0");
                return AviatorLong.valueOf(0);
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
                    log.warn("[规则引擎] DayOfWeekFunction: 无法解析日期字符串: {}, 返回默认值0", dateStr);
                    return AviatorLong.valueOf(0);
                }
            } else {
                log.warn("[规则引擎] DayOfWeekFunction: 不支持的日期类型: {}, 返回默认值0", dateObj.getClass().getName());
                return AviatorLong.valueOf(0);
            }

            // 获取星期几（1=周一，7=周日）
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            int dayOfWeekValue = dayOfWeek.getValue();

            log.debug("[规则引擎] DayOfWeekFunction: date={}, dayOfWeek={}", date, dayOfWeekValue);
            return AviatorLong.valueOf(dayOfWeekValue);

        } catch (Exception e) {
            log.error("[规则引擎] DayOfWeekFunction: 执行异常，返回默认值0", e);
            return AviatorLong.valueOf(0);
        }
    }
}
