package net.lab1024.sa.attendance.util;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 考勤时间计算工具类
 *
 * <p>
 * 专门用于考勤模块的时间计算工具，提供打卡时间验证、
 * 工作时长计算、迟到早退判断等核心功能
 * 严格遵循repowiki编码规范：使用jakarta包名、SLF4J日志
 * </p>
 *
 * <p>
 * 主要功能：
 * - 打卡时间有效性验证
 * - 工作时长精确计算（支持跨天）
 * - 迟到、早退、缺卡判断
 * - 加班时间计算
 * - 工作日历处理
 * - 法定假日判断
 * </p>
 *
 * <p>
 * 使用示例：
 *
 * <pre>
 * &#64;Resource
 * private AttendanceTimeUtil timeUtil;
 *
 * // 验证打卡时间
 * boolean isValid = timeUtil.isValidPunchTime(employeeId, punchTime);
 *
 * // 计算工作时长
 * Duration workHours = timeUtil.calculateWorkHours(checkInTime, checkOutTime);
 *
 * // 判断迟到
 * boolean isLate = timeUtil.isLate(checkInTime, shiftStartTime);
 * </pre>
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-27
 */
@Slf4j
@Component
public class AttendanceTimeUtil {

    /**
     * 时间格式化器
     */
    @SuppressWarnings("unused")
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 时间常量（分钟）
     */
    private static final int LATE_THRESHOLD = 5; // 迟到阈值：5分钟
    private static final int EARLY_LEAVE_THRESHOLD = 5; // 早退阈值：5分钟
    private static final int OVERTIME_THRESHOLD = 30; // 加班阈值：30分钟
    /**
     * 最小休息时间（分钟）
     * 预留配置项：用于未来休息时间验证
     */
    @SuppressWarnings("unused")
    private static final int MIN_REST_TIME = 30; // 最小休息时间：30分钟
    private static final int MAX_WORK_HOURS = 12 * 60; // 最大工作时长：12小时

    /**
     * 验证打卡时间是否有效
     *
     * @param punchTime        打卡时间
     * @param allowedTolerance 允许的时间容差（分钟）
     * @return 是否有效
     */
    public boolean isValidPunchTime(LocalDateTime punchTime, int allowedTolerance) {
        if (punchTime == null) {
            log.warn("[AttendanceTimeUtil] 打卡时间为空，验证失败");
            return false;
        }

        try {
            // 检查时间是否在合理范围内
            LocalDateTime now = LocalDateTime.now();
            long minutesDiff = Math.abs(ChronoUnit.MINUTES.between(punchTime, now));

            // 允许的时间容差检查（不能提前或推后超过限制）
            if (minutesDiff > allowedTolerance) {
                log.debug("[AttendanceTimeUtil] 打卡时间超出容差范围: {} 分钟", minutesDiff);
                return false;
            }

            // 检查时间是否为未来时间
            if (punchTime.isAfter(now.plusMinutes(5))) { // 允许5分钟时钟误差
                log.warn("[AttendanceTimeUtil] 打卡时间为未来时间: {}", punchTime);
                return false;
            }

            return true;

        } catch (Exception e) {
            log.error("[AttendanceTimeUtil] 验证打卡时间失败", e);
            return false;
        }
    }

    /**
     * 计算标准工作时长
     *
     * @param checkInTime  上班打卡时间
     * @param checkOutTime 下班打卡时间
     * @param breakTime    休息时间（分钟）
     * @return 工作时长（分钟）
     */
    public int calculateWorkMinutes(LocalDateTime checkInTime, LocalDateTime checkOutTime, int breakTime) {
        if (checkInTime == null || checkOutTime == null) {
            log.warn("[AttendanceTimeUtil] 上班或下班时间为空，无法计算工作时长");
            return 0;
        }

        try {
            // 处理跨天情况
            if (checkOutTime.isBefore(checkInTime)) {
                checkOutTime = checkOutTime.plusDays(1);
                log.debug("[AttendanceTimeUtil] 检测到跨天打卡，下班时间已调整");
            }

            // 计算总时长（分钟）
            long totalMinutes = ChronoUnit.MINUTES.between(checkInTime, checkOutTime);

            // 验证时长合理性
            if (totalMinutes < 0) {
                log.warn("[AttendanceTimeUtil] 计算出负工作时长，数据异常");
                return 0;
            }

            if (totalMinutes > MAX_WORK_HOURS) {
                log.warn("[AttendanceTimeUtil] 工作时长过长: {} 分钟，可能存在异常", totalMinutes);
                return MAX_WORK_HOURS;
            }

            // 减去休息时间
            int workMinutes = (int) Math.max(0, totalMinutes - breakTime);

            log.debug("[AttendanceTimeUtil] 计算工作时长: 总时长={}分钟, 休息={}分钟, 工作={}分钟",
                    totalMinutes, breakTime, workMinutes);

            return workMinutes;

        } catch (Exception e) {
            log.error("[AttendanceTimeUtil] 计算工作时长失败", e);
            return 0;
        }
    }

    /**
     * 判断是否迟到
     *
     * @param actualTime    实际打卡时间
     * @param scheduledTime 规定上班时间
     * @param graceMinutes  宽限时间（分钟）
     * @return 是否迟到
     */
    public boolean isLate(LocalDateTime actualTime, LocalDateTime scheduledTime, int graceMinutes) {
        if (actualTime == null || scheduledTime == null) {
            return false;
        }

        try {
            // 计算迟到分钟数
            long lateMinutes = ChronoUnit.MINUTES.between(scheduledTime, actualTime);

            // 超过宽限时间即为迟到
            boolean isLate = lateMinutes > graceMinutes;

            if (isLate) {
                log.debug("[AttendanceTimeUtil] 检测到迟到: 迟到{}分钟, 宽限{}分钟", lateMinutes, graceMinutes);
            }

            return isLate;

        } catch (Exception e) {
            log.error("[AttendanceTimeUtil] 判断迟到状态失败", e);
            return false;
        }
    }

    /**
     * 判断是否早退
     *
     * @param actualTime    实际打卡时间
     * @param scheduledTime 规定下班时间
     * @param graceMinutes  宽限时间（分钟）
     * @return 是否早退
     */
    public boolean isEarlyLeave(LocalDateTime actualTime, LocalDateTime scheduledTime, int graceMinutes) {
        if (actualTime == null || scheduledTime == null) {
            return false;
        }

        try {
            // 计算早退分钟数
            long earlyLeaveMinutes = ChronoUnit.MINUTES.between(actualTime, scheduledTime);

            // 超过宽限时间即为早退
            boolean isEarlyLeave = earlyLeaveMinutes > graceMinutes;

            if (isEarlyLeave) {
                log.debug("[AttendanceTimeUtil] 检测到早退: 早退{}分钟, 宽限{}分钟", earlyLeaveMinutes, graceMinutes);
            }

            return isEarlyLeave;

        } catch (Exception e) {
            log.error("[AttendanceTimeUtil] 判断早退状态失败", e);
            return false;
        }
    }

    /**
     * 判断是否缺卡
     *
     * @param hasCheckIn  是否有上班打卡
     * @param hasCheckOut 是否有下班打卡
     * @return 缺卡类型
     */
    public AbsenceType getAbsenceType(boolean hasCheckIn, boolean hasCheckOut) {
        if (!hasCheckIn && !hasCheckOut) {
            return AbsenceType.BOTH_ABSENT; // 上下班都缺卡
        } else if (!hasCheckIn) {
            return AbsenceType.CHECK_IN_ABSENT; // 缺上班卡
        } else if (!hasCheckOut) {
            return AbsenceType.CHECK_OUT_ABSENT; // 缺下班卡
        } else {
            return AbsenceType.NONE; // 无缺卡
        }
    }

    /**
     * 计算加班时间
     *
     * @param actualEndTime    实际结束时间
     * @param scheduledEndTime 规定结束时间
     * @param minimumOvertime  最小加班时间（分钟）
     * @return 加班时间（分钟）
     */
    public int calculateOvertimeMinutes(LocalDateTime actualEndTime, LocalDateTime scheduledEndTime,
            int minimumOvertime) {
        if (actualEndTime == null || scheduledEndTime == null) {
            return 0;
        }

        try {
            // 计算超时分钟数
            long overtimeMinutes = ChronoUnit.MINUTES.between(scheduledEndTime, actualEndTime);

            // 只有超过最小加班时间才计算加班
            if (overtimeMinutes >= minimumOvertime) {
                return (int) overtimeMinutes;
            }

            return 0;

        } catch (Exception e) {
            log.error("[AttendanceTimeUtil] 计算加班时间失败", e);
            return 0;
        }
    }

    /**
     * 判断是否为工作日
     *
     * @param date     日期
     * @param holidays 假日列表
     * @return 是否为工作日
     */
    public boolean isWorkDay(LocalDate date, Set<LocalDate> holidays) {
        if (date == null) {
            return false;
        }

        try {
            // 检查是否为假日
            if (holidays != null && holidays.contains(date)) {
                return false;
            }

            // 检查是否为周末（周六、周日）
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;

        } catch (Exception e) {
            log.error("[AttendanceTimeUtil] 判断工作日失败", e);
            return false;
        }
    }

    /**
     * 获取指定日期的工作时间段
     *
     * @param date           日期
     * @param shiftStartTime 班次开始时间
     * @param shiftEndTime   班次结束时间
     * @return 工作时间段
     */
    public WorkTimePeriod getWorkTimePeriod(LocalDate date, LocalTime shiftStartTime, LocalTime shiftEndTime) {
        if (date == null || shiftStartTime == null || shiftEndTime == null) {
            return null;
        }

        try {
            LocalDateTime startDateTime = LocalDateTime.of(date, shiftStartTime);
            LocalDateTime endDateTime = LocalDateTime.of(date, shiftEndTime);

            // 处理跨天班次
            if (shiftEndTime.isBefore(shiftStartTime)) {
                endDateTime = endDateTime.plusDays(1);
            }

            return new WorkTimePeriod(startDateTime, endDateTime);

        } catch (Exception e) {
            log.error("[AttendanceTimeUtil] 获取工作时间区间失败", e);
            return null;
        }
    }

    /**
     * 验证打卡时间顺序
     *
     * @param checkInTime  上班打卡时间
     * @param checkOutTime 下班打卡时间
     * @return 是否有效
     */
    public boolean isValidTimeSequence(LocalDateTime checkInTime, LocalDateTime checkOutTime) {
        if (checkInTime == null || checkOutTime == null) {
            return false;
        }

        try {
            // 下班时间应该晚于上班时间（允许跨天）
            if (checkOutTime.isBefore(checkInTime)) {
                // 检查是否为合理的跨天情况（下班时间在第二天）
                LocalDateTime nextDayCheckIn = checkInTime.plusDays(1);
                if (!checkOutTime.isBefore(nextDayCheckIn)) {
                    log.debug("[AttendanceTimeUtil] 检测到合理的跨天打卡");
                    return true;
                }
                log.warn("[AttendanceTimeUtil] 打卡时间顺序异常: 下班时间早于上班时间");
                return false;
            }

            // 检查时间间隔是否合理
            long hoursBetween = ChronoUnit.HOURS.between(checkInTime, checkOutTime);
            if (hoursBetween > 24) {
                log.warn("[AttendanceTimeUtil] 打卡时间间隔过长: {} 小时", hoursBetween);
                return false;
            }

            return true;

        } catch (Exception e) {
            log.error("[AttendanceTimeUtil] 验证打卡时间顺序失败", e);
            return false;
        }
    }

    /**
     * 格式化时间显示
     *
     * @param dateTime 日期时间
     * @return 格式化字符串
     */
    public String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        try {
            return dateTime.format(DATE_TIME_FORMATTER);
        } catch (Exception e) {
            log.error("[AttendanceTimeUtil] 格式化时间失败", e);
            return "";
        }
    }

    /**
     * 格式化时长显示
     *
     * @param minutes 分钟数
     * @return 格式化字符串（HH:mm格式）
     */
    public String formatDuration(int minutes) {
        if (minutes <= 0) {
            return "00:00";
        }

        try {
            int hours = minutes / 60;
            int mins = minutes % 60;
            return String.format("%02d:%02d", hours, mins);
        } catch (Exception e) {
            log.error("[AttendanceTimeUtil] 格式化时长失败", e);
            return "00:00";
        }
    }

    /**
     * 缺卡类型枚举
     */
    public enum AbsenceType {
        NONE("无缺卡"),
        CHECK_IN_ABSENT("缺上班卡"),
        CHECK_OUT_ABSENT("缺下班卡"),
        BOTH_ABSENT("上下班都缺卡");

        private final String description;

        AbsenceType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 工作时间段类
     */
    public static class WorkTimePeriod {
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;

        public WorkTimePeriod(LocalDateTime startTime, LocalDateTime endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public Duration getDuration() {
            return Duration.between(startTime, endTime);
        }

        @Override
        public String toString() {
            return String.format("[%s - %s]",
                    startTime.format(DATE_TIME_FORMATTER),
                    endTime.format(DATE_TIME_FORMATTER));
        }
    }

    // ==================== 常用方法快捷入口 ====================

    /**
     * 使用默认宽限时间判断迟到（5分钟）
     */
    public boolean isLate(LocalDateTime actualTime, LocalDateTime scheduledTime) {
        return isLate(actualTime, scheduledTime, LATE_THRESHOLD);
    }

    /**
     * 使用默认宽限时间判断早退（5分钟）
     */
    public boolean isEarlyLeave(LocalDateTime actualTime, LocalDateTime scheduledTime) {
        return isEarlyLeave(actualTime, scheduledTime, EARLY_LEAVE_THRESHOLD);
    }

    /**
     * 使用默认最小加班时间计算加班（30分钟）
     */
    public int calculateOvertimeMinutes(LocalDateTime actualEndTime, LocalDateTime scheduledEndTime) {
        return calculateOvertimeMinutes(actualEndTime, scheduledEndTime, OVERTIME_THRESHOLD);
    }

    /**
     * 判断是否为工作日（不指定假日）
     */
    public boolean isWorkDay(LocalDate date) {
        return isWorkDay(date, null);
    }
}
