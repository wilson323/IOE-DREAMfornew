package net.lab1024.sa.admin.module.smart.access.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.smart.access.domain.dto.TimeSlotConfig;

/**
 * 时间段权限验证器
 * <p>
 * 严格遵循repowiki规范：
 * - 工具类设计，静态方法
 * - 完整的异常处理和日志记录
 * - 支持复杂时间段规则验证
 * <p>
 * 功能特性：
 * - 支持工作日/周末时间段配置
 * - 支持节假日时间段配置
 * - 支持特定日期时间段配置
 * - 支持多个时间段组合
 * - 时间段重叠处理
 *
 * @author IOE-DREAM Team
 * @since 2025-11-19
 */
@Slf4j
public class TimeSlotValidator {

    /**
     * JSON解析器
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 时间格式
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 验证当前时间是否在允许的时间段内
     *
     * @param timePeriodsJson 时间段配置JSON字符串
     * @param currentTime     当前时间
     * @return 是否在允许的时间段内
     */
    public static boolean validate(String timePeriodsJson, LocalDateTime currentTime) {
        if (!StringUtils.hasText(timePeriodsJson)) {
            // 如果没有配置时间段，默认允许
            log.debug("时间段配置为空，默认允许访问");
            return true;
        }

        try {
            // 解析JSON配置
            TimeSlotConfig config = parseTimeSlotConfig(timePeriodsJson);
            if (config == null || config.getTimeSlots() == null || config.getTimeSlots().isEmpty()) {
                log.debug("时间段配置为空，默认允许访问");
                return true;
            }

            // 获取当前日期和时间信息
            LocalDate currentDate = currentTime.toLocalDate();
            DayOfWeek dayOfWeek = currentTime.getDayOfWeek();
            LocalTime currentLocalTime = currentTime.toLocalTime();
            int dayOfWeekValue = dayOfWeek.getValue(); // 1=Monday, 7=Sunday

            // 检查每个时间段配置
            for (TimeSlotConfig.TimeSlot timeSlot : config.getTimeSlots()) {
                if (isTimeSlotMatch(timeSlot, currentDate, dayOfWeekValue, currentLocalTime)) {
                    log.debug("时间段验证通过，timeSlot: {}, currentTime: {}", timeSlot.getType(), currentTime);
                    return true;
                }
            }

            log.debug("时间段验证失败，当前时间不在任何允许的时间段内，currentTime: {}", currentTime);
            return false;

        } catch (Exception e) {
            log.error("时间段验证异常，timePeriodsJson: {}, currentTime: {}", timePeriodsJson, currentTime, e);
            // 验证异常时，为了安全起见，返回false（不允许访问）
            return false;
        }
    }

    /**
     * 解析时间段配置JSON
     *
     * @param timePeriodsJson JSON字符串
     * @return 时间段配置对象
     */
    private static TimeSlotConfig parseTimeSlotConfig(String timePeriodsJson) {
        try {
            return objectMapper.readValue(timePeriodsJson, TimeSlotConfig.class);
        } catch (Exception e) {
            log.error("解析时间段配置JSON失败，timePeriodsJson: {}", timePeriodsJson, e);
            return null;
        }
    }

    /**
     * 检查时间段是否匹配当前时间
     *
     * @param timeSlot         时间段配置
     * @param currentDate      当前日期
     * @param dayOfWeekValue   星期几 (1-7)
     * @param currentLocalTime 当前时间
     * @return 是否匹配
     */
    private static boolean isTimeSlotMatch(TimeSlotConfig.TimeSlot timeSlot,
            LocalDate currentDate,
            int dayOfWeekValue,
            LocalTime currentLocalTime) {
        String type = timeSlot.getType();
        if (type == null) {
            return false;
        }

        switch (type.toLowerCase()) {
            case "weekday":
                return isWeekdayMatch(timeSlot, dayOfWeekValue, currentLocalTime);
            case "weekend":
                return isWeekendMatch(timeSlot, dayOfWeekValue, currentLocalTime);
            case "holiday":
                return isHolidayMatch(timeSlot, currentDate, currentLocalTime);
            case "specific":
                return isSpecificDateMatch(timeSlot, currentDate, currentLocalTime);
            default:
                log.warn("未知的时间段类型: {}", type);
                return false;
        }
    }

    /**
     * 检查工作日时间段是否匹配
     *
     * @param timeSlot         时间段配置
     * @param dayOfWeekValue   星期几 (1-7)
     * @param currentLocalTime 当前时间
     * @return 是否匹配
     */
    private static boolean isWeekdayMatch(TimeSlotConfig.TimeSlot timeSlot,
            int dayOfWeekValue,
            LocalTime currentLocalTime) {
        // 检查星期几是否匹配
        List<Integer> days = timeSlot.getDays();
        if (days != null && !days.isEmpty()) {
            if (!days.contains(dayOfWeekValue)) {
                return false;
            }
        } else {
            // 如果没有指定星期，默认匹配工作日（周一到周五）
            if (dayOfWeekValue > 5) {
                return false;
            }
        }

        // 检查时间范围
        return isTimeInRanges(timeSlot.getTimeRanges(), currentLocalTime);
    }

    /**
     * 检查周末时间段是否匹配
     *
     * @param timeSlot         时间段配置
     * @param dayOfWeekValue   星期几 (1-7)
     * @param currentLocalTime 当前时间
     * @return 是否匹配
     */
    private static boolean isWeekendMatch(TimeSlotConfig.TimeSlot timeSlot,
            int dayOfWeekValue,
            LocalTime currentLocalTime) {
        // 检查星期几是否匹配
        List<Integer> days = timeSlot.getDays();
        if (days != null && !days.isEmpty()) {
            if (!days.contains(dayOfWeekValue)) {
                return false;
            }
        } else {
            // 如果没有指定星期，默认匹配周末（周六和周日）
            if (dayOfWeekValue <= 5) {
                return false;
            }
        }

        // 检查时间范围
        return isTimeInRanges(timeSlot.getTimeRanges(), currentLocalTime);
    }

    /**
     * 检查节假日时间段是否匹配
     *
     * @param timeSlot         时间段配置
     * @param currentDate      当前日期
     * @param currentLocalTime 当前时间
     * @return 是否匹配
     */
    private static boolean isHolidayMatch(TimeSlotConfig.TimeSlot timeSlot,
            LocalDate currentDate,
            LocalTime currentLocalTime) {
        List<String> dates = timeSlot.getDates();
        if (dates == null || dates.isEmpty()) {
            return false;
        }

        String currentDateStr = currentDate.format(DATE_FORMATTER);
        if (!dates.contains(currentDateStr)) {
            return false;
        }

        // 检查时间范围
        return isTimeInRanges(timeSlot.getTimeRanges(), currentLocalTime);
    }

    /**
     * 检查特定日期时间段是否匹配
     *
     * @param timeSlot         时间段配置
     * @param currentDate      当前日期
     * @param currentLocalTime 当前时间
     * @return 是否匹配
     */
    private static boolean isSpecificDateMatch(TimeSlotConfig.TimeSlot timeSlot,
            LocalDate currentDate,
            LocalTime currentLocalTime) {
        List<String> dates = timeSlot.getDates();
        if (dates == null || dates.isEmpty()) {
            return false;
        }

        String currentDateStr = currentDate.format(DATE_FORMATTER);
        if (!dates.contains(currentDateStr)) {
            return false;
        }

        // 检查时间范围
        return isTimeInRanges(timeSlot.getTimeRanges(), currentLocalTime);
    }

    /**
     * 检查当前时间是否在时间范围内
     *
     * @param timeRanges       时间范围列表
     * @param currentLocalTime 当前时间
     * @return 是否在时间范围内
     */
    private static boolean isTimeInRanges(List<TimeSlotConfig.TimeRange> timeRanges,
            LocalTime currentLocalTime) {
        if (timeRanges == null || timeRanges.isEmpty()) {
            // 如果没有配置时间范围，默认允许
            return true;
        }

        for (TimeSlotConfig.TimeRange timeRange : timeRanges) {
            if (isTimeInRange(timeRange, currentLocalTime)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查当前时间是否在单个时间范围内
     *
     * @param timeRange        时间范围
     * @param currentLocalTime 当前时间
     * @return 是否在时间范围内
     */
    private static boolean isTimeInRange(TimeSlotConfig.TimeRange timeRange,
            LocalTime currentLocalTime) {
        try {
            String startStr = timeRange.getStart();
            String endStr = timeRange.getEnd();

            if (startStr == null || endStr == null) {
                return false;
            }

            LocalTime startTime = LocalTime.parse(startStr, TIME_FORMATTER);
            LocalTime endTime = LocalTime.parse(endStr, TIME_FORMATTER);

            // 处理跨天的情况（如 22:00-06:00）
            if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
                // 跨天：当前时间 >= 开始时间 或 当前时间 <= 结束时间
                return currentLocalTime.isAfter(startTime) || currentLocalTime.equals(startTime) ||
                        currentLocalTime.isBefore(endTime) || currentLocalTime.equals(endTime);
            } else {
                // 不跨天：开始时间 <= 当前时间 <= 结束时间
                return (currentLocalTime.isAfter(startTime) || currentLocalTime.equals(startTime)) &&
                        (currentLocalTime.isBefore(endTime) || currentLocalTime.equals(endTime));
            }

        } catch (Exception e) {
            log.error("解析时间范围失败，timeRange: {}", timeRange, e);
            return false;
        }
    }
}
