package net.lab1024.sa.oa.workflow.function;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;


import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorDouble;
import com.googlecode.aviator.runtime.type.AviatorObject;

/**
 * 计算工作时间函数
 * <p>
 * 工作流表达式引擎自定义函数，用于计算两个时间点之间的工作小时数
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Slf4j
public class CalculateWorkingHoursFunction extends AbstractFunction {

    @Override
    public String getName() {
        return "calculateWorkingHours";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        try {
            // 获取参数
            String startTimeStr = arg1.getValue(env).toString();
            String endTimeStr = arg2.getValue(env).toString();

            // 解析时间
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime startTime = LocalDateTime.parse(startTimeStr, formatter);
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr, formatter);

            // 计算工作时间
            double workingHours = calculateWorkingHours(startTime, endTime);

            log.debug("[工作时间计算] startTime={}, endTime={}, workingHours={}",
                    startTimeStr, endTimeStr, workingHours);

            return AviatorDouble.valueOf(workingHours);

        } catch (Exception e) {
            log.error("[工作时间计算] 执行异常: {}", e.getMessage(), e);
            return AviatorDouble.valueOf(0.0);
        }
    }

    /**
     * 计算工作时间
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 工作小时数
     */
    private double calculateWorkingHours(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return 0.0;
        }

        if (endTime.isBefore(startTime)) {
            return 0.0;
        }

        // 计算总时长（分钟）
        long totalMinutes = Duration.between(startTime, endTime).toMinutes();

        // 简化计算：总分钟数 / 60 = 小时数
        // 实际业务中可能需要排除午休、周末、节假日等
        double workingHours = totalMinutes / 60.0;

        // 保留两位小数
        return new BigDecimal(workingHours)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
