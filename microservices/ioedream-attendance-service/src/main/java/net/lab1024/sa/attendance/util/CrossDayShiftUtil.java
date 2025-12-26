package net.lab1024.sa.attendance.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.extern.slf4j.Slf4j;

/**
 * 跨天班次工具类
 * <p>
 * 处理跨天班次的日期计算、打卡匹配、时长统计等逻辑
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class CrossDayShiftUtil {

    /**
     * 跨天归属规则常量
     */
    public static final String CROSS_DAY_RULE_START_DATE = "START_DATE"; // 以班次开始日期为准（推荐）
    public static final String CROSS_DAY_RULE_END_DATE = "END_DATE";   // 以班次结束日期为准
    public static final String CROSS_DAY_RULE_SPLIT = "SPLIT";         // 分别归属（不推荐）

    /**
     * 下班打卡缓冲期（分钟）
     * 在下班时间后此时间内的打卡仍归属到班次开始日期
     */
    private static final int PUNCH_OUT_BUFFER_MINUTES = 15; // 15分钟缓冲期，覆盖合理的延迟打卡

    /**
     * 计算打卡记录的考勤日期
     * <p>
     * 根据班次的跨天规则，确定打卡记录应该归属的考勤日期
     * </p>
     *
     * @param punchTime 打卡时间
     * @param workStartTime 上班时间
     * @param workEndTime 下班时间
     * @param crossDayRule 跨天归属规则
     * @param attendanceType 打卡类型（CHECK_IN/CHECK_OUT）
     * @return 考勤日期
     */
    public static LocalDate calculateAttendanceDate(
            LocalDateTime punchTime,
            LocalTime workStartTime,
            LocalTime workEndTime,
            String crossDayRule,
            String attendanceType) {

        // 参数校验：打卡时间不能为null
        if (punchTime == null) {
            throw new IllegalArgumentException("打卡时间不能为空");
        }

        LocalDate punchDate = punchTime.toLocalDate();
        LocalTime punchTimeOnly = punchTime.toLocalTime();

        // 判断是否为跨天班次
        boolean isCrossDay = workEndTime.isBefore(workStartTime);

        if (!isCrossDay) {
            // 非跨天班次，直接返回打卡日期
            return punchDate;
        }

        // 跨天班次，根据规则计算考勤日期
        switch (crossDayRule) {
            case CROSS_DAY_RULE_START_DATE:
                // 以班次开始日期为准
                // 下班打卡应用缓冲期：如果在下班时间后5分钟内，仍归属到班次开始日期
                if ("CHECK_OUT".equals(attendanceType)) {
                    LocalTime bufferedEndTime = workEndTime.plusMinutes(PUNCH_OUT_BUFFER_MINUTES);
                    if (!punchTimeOnly.isAfter(bufferedEndTime)) {
                        return punchDate.minusDays(1);
                    }
                } else {
                    // 上班打卡：如果打卡时间小于等于下班时间（第二天00:00-结束时间），归属到前一天（班次开始日期）
                    if (!punchTimeOnly.isAfter(workEndTime)) {
                        return punchDate.minusDays(1);
                    }
                }
                return punchDate;

            case CROSS_DAY_RULE_END_DATE:
                // 以班次结束日期为准
                // 如果打卡时间大于等于上班时间（开始时间-23:59），归属到后一天（班次结束日期）
                if (!punchTimeOnly.isBefore(workStartTime)) {
                    return punchDate.plusDays(1);
                }
                return punchDate;

            case CROSS_DAY_RULE_SPLIT:
                // 分别归属到各自日期
                // 上班打卡归属到当天，下班打卡归属到第二天
                if ("CHECK_IN".equals(attendanceType)) {
                    // 上班打卡：如果在班次开始时间之前（第二天），归属到前一天
                    if (!punchTimeOnly.isAfter(workEndTime)) {
                        return punchDate.minusDays(1);
                    }
                    return punchDate;
                } else if ("CHECK_OUT".equals(attendanceType)) {
                    // 下班打卡：不应用缓冲期，直接判断是否在下班时间之后
                    if (punchTimeOnly.isAfter(workEndTime)) {
                        return punchDate;
                    }
                    return punchDate.minusDays(1);
                }
                return punchDate;

            default:
                log.warn("[跨天班次] 未知的跨天规则: {}, 使用默认规则START_DATE", crossDayRule);
                // 默认使用START_DATE规则（应用缓冲期）
                if ("CHECK_OUT".equals(attendanceType)) {
                    LocalTime bufferedEndTime = workEndTime.plusMinutes(PUNCH_OUT_BUFFER_MINUTES);
                    if (!punchTimeOnly.isAfter(bufferedEndTime)) {
                        return punchDate.minusDays(1);
                    }
                } else {
                    if (!punchTimeOnly.isAfter(workEndTime)) {
                        return punchDate.minusDays(1);
                    }
                }
                return punchDate;
        }
    }

    /**
     * 判断打卡时间是否在班次有效范围内
     * <p>
     * 对于跨天班次，需要特别处理时间范围判断
     * </p>
     *
     * @param punchTime 打卡时间
     * @param workStartTime 上班时间
     * @param workEndTime 下班时间
     * @param shiftDate 班次日期
     * @return true-在有效范围内 false-不在有效范围内
     */
    public static boolean isValidPunchTime(
            LocalDateTime punchTime,
            LocalTime workStartTime,
            LocalTime workEndTime,
            LocalDate shiftDate) {

        LocalDate punchDate = punchTime.toLocalDate();
        LocalTime punchTimeOnly = punchTime.toLocalTime();

        // 判断是否为跨天班次
        boolean isCrossDay = workEndTime.isBefore(workStartTime);

        if (!isCrossDay) {
            // 非跨天班次：打卡时间必须在班次日期，且时间在上班和下班时间之间
            return punchDate.equals(shiftDate)
                    && !punchTimeOnly.isBefore(workStartTime)
                    && !punchTimeOnly.isAfter(workEndTime);
        }

        // 跨天班次：时间范围跨越两天
        // 班次开始：shiftDate  workStartTime（如：1月1日 22:00）
        // 班次结束：shiftDate+1 workEndTime（如：1月2日 06:00）

        LocalDateTime shiftStartDateTime = shiftDate.atTime(workStartTime);
        LocalDateTime shiftEndDateTime = shiftDate.plusDays(1).atTime(workEndTime);

        // 打卡时间必须在班次时间范围内（允许提前打卡和延后下班）
        return !punchTime.isBefore(shiftStartDateTime)
                && !punchTime.isAfter(shiftEndDateTime);
    }

    /**
     * 计算跨天班次的工作时长（分钟）
     * <p>
     * 跨天班次的工作时长计算：
     * 从班次开始日期的上班时间 到 班次结束日期的下班时间
     * </p>
     *
     * @param workStartTime 上班时间
     * @param workEndTime 下班时间
     * @param shiftDate 班次日期
     * @param actualStartTime 实际上班打卡时间
     * @param actualEndTime 实际下班打卡时间
     * @return 工作时长（分钟）
     */
    public static int calculateCrossDayWorkDuration(
            LocalTime workStartTime,
            LocalTime workEndTime,
            LocalDate shiftDate,
            LocalDateTime actualStartTime,
            LocalDateTime actualEndTime) {

        // 计算班次的开始和结束时间
        LocalDateTime shiftStart = shiftDate.atTime(workStartTime);
        LocalDateTime shiftEnd = shiftDate.plusDays(1).atTime(workEndTime);

        // 使用实际打卡时间，但限制在班次时间范围内
        LocalDateTime effectiveStart = actualStartTime.isBefore(shiftStart)
                ? shiftStart
                : actualStartTime;
        LocalDateTime effectiveEnd = actualEndTime.isAfter(shiftEnd)
                ? shiftEnd
                : actualEndTime;

        // 计算时长（分钟）
        long durationMinutes = java.time.Duration.between(effectiveStart, effectiveEnd).toMinutes();

        // 确保时长不为负数
        return (int) Math.max(durationMinutes, 0);
    }

    /**
     * 判断两个打卡时间是否属于同一个跨天班次
     * <p>
     * 用于判断上班打卡和下班打卡是否匹配到同一个跨天班次
     * </p>
     *
     * @param firstPunch 第一次打卡时间
     * @param secondPunch 第二次打卡时间
     * @param workStartTime 上班时间
     * @param workEndTime 下班时间
     * @return true-属于同一班次 false-不属于同一班次
     */
    public static boolean isSameCrossDayShift(
            LocalDateTime firstPunch,
            LocalDateTime secondPunch,
            LocalTime workStartTime,
            LocalTime workEndTime) {

        // 判断是否为跨天班次
        boolean isCrossDay = workEndTime.isBefore(workStartTime);
        if (!isCrossDay) {
            // 非跨天班次，判断是否在同一天
            return firstPunch.toLocalDate().equals(secondPunch.toLocalDate());
        }

        // 跨天班次：两次打卡的时间跨度应该大约等于班次时长
        // 上班打卡应该在班次开始时间附近，下班打卡应该在班次结束时间附近
        LocalTime firstTime = firstPunch.toLocalTime();
        LocalTime secondTime = secondPunch.toLocalTime();

        // 第一次打卡应该接近上班时间（22:00）
        // 第二次打卡应该接近下班时间（次日06:00）
        boolean isFirstNearStart = isNearTime(firstTime, workStartTime, 120); // 允许2小时偏差
        boolean isSecondNearEnd = isNearTime(secondTime, workEndTime, 120);

        // 或者第一次打卡接近下班时间，第二次打卡接近上班时间（打卡顺序相反）
        boolean isFirstNearEnd = isNearTime(firstTime, workEndTime, 120);
        boolean isSecondNearStart = isNearTime(secondTime, workStartTime, 120);

        return (isFirstNearStart && isSecondNearEnd) || (isFirstNearEnd && isSecondNearStart);
    }

    /**
     * 判断时间是否接近目标时间
     *
     * @param actualTime 实际时间
     * @param targetTime 目标时间
     * @param toleranceMinutes 容差（分钟）
     * @return true-接近 false-不接近
     */
    private static boolean isNearTime(LocalTime actualTime, LocalTime targetTime, int toleranceMinutes) {
        long diffMinutes = Math.abs(java.time.Duration.between(actualTime, targetTime).toMinutes());
        return diffMinutes <= toleranceMinutes;
    }
}
