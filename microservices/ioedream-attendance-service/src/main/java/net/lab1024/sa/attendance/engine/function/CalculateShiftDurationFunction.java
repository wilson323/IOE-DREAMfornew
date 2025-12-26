package net.lab1024.sa.attendance.engine.function;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorDouble;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.time.LocalTime;
import java.util.Map;

/**
 * 计算班次时长的函数
 * <p>
 * 使用示例：
 * - calculate_shift_duration(workStartTime, workEndTime, lunchDuration)
 * - calculate_shift_duration('08:00:00', '17:00:00', 60)
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public class CalculateShiftDurationFunction extends AbstractFunction {

    @Override
    public String getName() {
        return "calculate_shift_duration";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2, AviatorObject arg3) {
        // 获取上班时间
        Object startTimeObj = arg1.getValue(env);
        LocalTime startTime = parseTime(startTimeObj);

        // 获取下班时间
        Object endTimeObj = arg2.getValue(env);
        LocalTime endTime = parseTime(endTimeObj);

        // 获取午休时长（分钟）
        Number lunchDurationObj = (Number) arg3.getValue(env);
        int lunchDuration = lunchDurationObj != null ? lunchDurationObj.intValue() : 0;

        // 计算工作时长（分钟）
        int durationMinutes = (int) java.time.Duration.between(startTime, endTime).toMinutes() - lunchDuration;

        // 转换为小时
        double durationHours = durationMinutes / 60.0;

        return AviatorDouble.valueOf(durationHours);
    }

    /**
     * 解析时间
     */
    private LocalTime parseTime(Object timeObj) {
        if (timeObj instanceof LocalTime) {
            return (LocalTime) timeObj;
        } else if (timeObj instanceof String) {
            return LocalTime.parse((String) timeObj);
        } else {
            throw new IllegalArgumentException("calculate_shift_duration函数参数类型错误，需要LocalTime或String");
        }
    }
}
