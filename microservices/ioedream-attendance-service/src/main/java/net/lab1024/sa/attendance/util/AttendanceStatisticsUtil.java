package net.lab1024.sa.attendance.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 考勤统计工具类
 *
 * <p>
 * 专门用于考勤数据的统计分析，提供各种维度的统计计算
 * 严格遵循repowiki编码规范：使用jakarta包名、SLF4J日志
 * </p>
 *
 * <p>
 * 主要功能：
 * - 考勤率计算
 * - 出勤天数统计
 * - 迟到早退统计
 * - 加班时间统计
 * - 异常情况分析
 * - 趋势数据计算
 * </p>
 *
 * <p>
 * 使用示例：
 *
 * <pre>
 * &#64;Resource
 * private AttendanceStatisticsUtil statisticsUtil;
 *
 * // 计算月度考勤统计
 * MonthlyStats stats = statisticsUtil.calculateMonthlyStats(employeeId, year, month);
 *
 * // 计算考勤率
 * BigDecimal attendanceRate = statisticsUtil.calculateAttendanceRate(workDays, actualDays);
 * </pre>
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-27
 */
@Slf4j
@Component
public class AttendanceStatisticsUtil {

    /**
     * 考勤统计结果类
     */
    public static class AttendanceStats {
        private int totalWorkDays; // 应出勤天数
        private int actualWorkDays; // 实际出勤天数
        private int leaveDays; // 请假天数
        private int absentDays; // 缺勤天数
        private int lateCount; // 迟到次数
        private int earlyLeaveCount; // 早退次数
        private int overtimeHours; // 加班小时数
        private BigDecimal attendanceRate; // 出勤率
        private Map<String, Integer> details; // 详细统计

        // Getters and Setters
        public int getTotalWorkDays() {
            return totalWorkDays;
        }

        public void setTotalWorkDays(int totalWorkDays) {
            this.totalWorkDays = totalWorkDays;
        }

        public int getActualWorkDays() {
            return actualWorkDays;
        }

        public void setActualWorkDays(int actualWorkDays) {
            this.actualWorkDays = actualWorkDays;
        }

        public int getLeaveDays() {
            return leaveDays;
        }

        public void setLeaveDays(int leaveDays) {
            this.leaveDays = leaveDays;
        }

        public int getAbsentDays() {
            return absentDays;
        }

        public void setAbsentDays(int absentDays) {
            this.absentDays = absentDays;
        }

        public int getLateCount() {
            return lateCount;
        }

        public void setLateCount(int lateCount) {
            this.lateCount = lateCount;
        }

        public int getEarlyLeaveCount() {
            return earlyLeaveCount;
        }

        public void setEarlyLeaveCount(int earlyLeaveCount) {
            this.earlyLeaveCount = earlyLeaveCount;
        }

        public int getOvertimeHours() {
            return overtimeHours;
        }

        public void setOvertimeHours(int overtimeHours) {
            this.overtimeHours = overtimeHours;
        }

        public BigDecimal getAttendanceRate() {
            return attendanceRate;
        }

        public void setAttendanceRate(BigDecimal attendanceRate) {
            this.attendanceRate = attendanceRate;
        }

        public Map<String, Integer> getDetails() {
            return details;
        }

        public void setDetails(Map<String, Integer> details) {
            this.details = details;
        }
    }

    /**
     * 月度统计结果类
     */
    public static class MonthlyStats extends AttendanceStats {
        private int year;
        private int month;
        private Map<Integer, DailyStats> dailyStats; // 每日统计

        // Getters and Setters
        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public int getMonth() {
            return month;
        }

        public void setMonth(int month) {
            this.month = month;
        }

        public Map<Integer, DailyStats> getDailyStats() {
            return dailyStats;
        }

        public void setDailyStats(Map<Integer, DailyStats> dailyStats) {
            this.dailyStats = dailyStats;
        }
    }

    /**
     * 每日统计结果类
     */
    public static class DailyStats {
        private LocalDate date;
        private String status; // 状态：NORMAL, LATE, EARLY_LEAVE, ABSENT
        private LocalTime checkInTime; // 上班打卡时间
        private LocalTime checkOutTime; // 下班打卡时间
        private int workMinutes; // 工作分钟数
        private int overtimeMinutes; // 加班分钟数
        private boolean isHoliday; // 是否为假日
        private String remark; // 备注

        // Getters and Setters
        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalTime getCheckInTime() {
            return checkInTime;
        }

        public void setCheckInTime(LocalTime checkInTime) {
            this.checkInTime = checkInTime;
        }

        public LocalTime getCheckOutTime() {
            return checkOutTime;
        }

        public void setCheckOutTime(LocalTime checkOutTime) {
            this.checkOutTime = checkOutTime;
        }

        public int getWorkMinutes() {
            return workMinutes;
        }

        public void setWorkMinutes(int workMinutes) {
            this.workMinutes = workMinutes;
        }

        public int getOvertimeMinutes() {
            return overtimeMinutes;
        }

        public void setOvertimeMinutes(int overtimeMinutes) {
            this.overtimeMinutes = overtimeMinutes;
        }

        public boolean isHoliday() {
            return isHoliday;
        }

        public void setHoliday(boolean holiday) {
            isHoliday = holiday;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }

    /**
     * 计算考勤率
     *
     * @param workDays   应出勤天数
     * @param actualDays 实际出勤天数
     * @return 考勤率（百分比，保留两位小数）
     */
    public BigDecimal calculateAttendanceRate(int workDays, int actualDays) {
        if (workDays <= 0) {
            return BigDecimal.ZERO;
        }

        try {
            BigDecimal rate = new BigDecimal(actualDays)
                    .divide(new BigDecimal(workDays), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));

            return rate.setScale(2, RoundingMode.HALF_UP);

        } catch (Exception e) {
            log.error("[AttendanceStatisticsUtil] 计算考勤率失败", e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 计算月度考勤统计
     *
     * @param attendanceRecords 考勤记录列表
     * @param holidays          假日列表
     * @return 月度统计结果
     */
    public MonthlyStats calculateMonthlyStats(List<Map<String, Object>> attendanceRecords, Set<LocalDate> holidays) {
        MonthlyStats monthlyStats = new MonthlyStats();

        if (attendanceRecords == null || attendanceRecords.isEmpty()) {
            return monthlyStats;
        }

        try {
            log.debug("[AttendanceStatisticsUtil] 开始计算月度考勤统计，记录数: {}", attendanceRecords.size());

            // 获取年月信息
            LocalDate firstDate = attendanceRecords.stream()
                    .map(record -> (LocalDate) record.get("attendanceDate"))
                    .filter(Objects::nonNull)
                    .min(LocalDate::compareTo)
                    .orElse(LocalDate.now());

            monthlyStats.setYear(firstDate.getYear());
            monthlyStats.setMonth(firstDate.getMonthValue());

            // 计算应出勤天数
            Set<LocalDate> workDays = calculateWorkDays(firstDate.getYear(), firstDate.getMonthValue(), holidays);
            monthlyStats.setTotalWorkDays(workDays.size());

            // 按日期分组统计
            Map<LocalDate, List<Map<String, Object>>> recordsByDate = attendanceRecords.stream()
                    .filter(record -> record.get("attendanceDate") != null)
                    .collect(Collectors.groupingBy(record -> (LocalDate) record.get("attendanceDate")));

            Map<Integer, DailyStats> dailyStatsMap = new HashMap<>();
            int actualWorkDays = 0;
            int leaveDays = 0;
            int lateCount = 0;
            int earlyLeaveCount = 0;
            int totalOvertimeMinutes = 0;

            for (LocalDate date : workDays) {
                List<Map<String, Object>> dayRecords = recordsByDate.getOrDefault(date, new ArrayList<>());
                DailyStats dailyStat = calculateDailyStats(date, dayRecords, holidays.contains(date));
                dailyStatsMap.put(date.getDayOfMonth(), dailyStat);

                // 累计统计
                if ("NORMAL".equals(dailyStat.getStatus()) || "LATE".equals(dailyStat.getStatus())
                        || "EARLY_LEAVE".equals(dailyStat.getStatus())) {
                    actualWorkDays++;
                }
                if ("LEAVE".equals(dailyStat.getStatus())) {
                    leaveDays++;
                }
                if ("LATE".equals(dailyStat.getStatus())) {
                    lateCount++;
                }
                if ("EARLY_LEAVE".equals(dailyStat.getStatus())) {
                    earlyLeaveCount++;
                }
                totalOvertimeMinutes += dailyStat.getOvertimeMinutes();
            }

            // 设置统计结果
            monthlyStats.setDailyStats(dailyStatsMap);
            monthlyStats.setActualWorkDays(actualWorkDays);
            monthlyStats.setLeaveDays(leaveDays);
            monthlyStats.setAbsentDays(monthlyStats.getTotalWorkDays() - actualWorkDays - leaveDays);
            monthlyStats.setLateCount(lateCount);
            monthlyStats.setEarlyLeaveCount(earlyLeaveCount);
            monthlyStats.setOvertimeHours(totalOvertimeMinutes / 60);
            monthlyStats.setAttendanceRate(calculateAttendanceRate(monthlyStats.getTotalWorkDays(), actualWorkDays));

            log.debug("[AttendanceStatisticsUtil] 月度考勤统计完成: 应出勤{}天, 实际{}天, 考勤率{}%",
                    monthlyStats.getTotalWorkDays(), monthlyStats.getActualWorkDays(),
                    monthlyStats.getAttendanceRate());

        } catch (Exception e) {
            log.error("[AttendanceStatisticsUtil] 计算月度考勤统计失败", e);
        }

        return monthlyStats;
    }

    /**
     * 计算每日考勤统计
     */
    private DailyStats calculateDailyStats(LocalDate date, List<Map<String, Object>> dayRecords, boolean isHoliday) {
        DailyStats dailyStat = new DailyStats();
        dailyStat.setDate(date);
        dailyStat.setHoliday(isHoliday);

        if (dayRecords.isEmpty()) {
            dailyStat.setStatus(isHoliday ? "HOLIDAY" : "ABSENT");
            return dailyStat;
        }

        try {
            // 解析打卡记录
            LocalTime checkInTime = null;
            LocalTime checkOutTime = null;
            int totalWorkMinutes = 0;
            int totalOvertimeMinutes = 0;

            for (Map<String, Object> record : dayRecords) {
                String punchType = (String) record.get("punchType");
                LocalDateTime punchTime = (LocalDateTime) record.get("punchTime");

                if (punchTime != null) {
                    LocalTime time = punchTime.toLocalTime();

                    if ("IN".equals(punchType) && (checkInTime == null || time.isBefore(checkInTime))) {
                        checkInTime = time;
                    } else if ("OUT".equals(punchType) && (checkOutTime == null || time.isAfter(checkOutTime))) {
                        checkOutTime = time;
                    }

                    // 统计加班时间
                    Integer overtimeMinutes = (Integer) record.get("overtimeMinutes");
                    if (overtimeMinutes != null && overtimeMinutes > 0) {
                        totalOvertimeMinutes += overtimeMinutes;
                    }
                }
            }

            dailyStat.setCheckInTime(checkInTime);
            dailyStat.setCheckOutTime(checkOutTime);

            // 计算工作时长
            if (checkInTime != null && checkOutTime != null) {
                totalWorkMinutes = (int) ChronoUnit.MINUTES.between(checkInTime, checkOutTime);
                // 处理跨天情况
                if (totalWorkMinutes < 0) {
                    totalWorkMinutes += 24 * 60;
                }
            }
            dailyStat.setWorkMinutes(totalWorkMinutes);
            dailyStat.setOvertimeMinutes(totalOvertimeMinutes);

            // 判断当日状态
            String status = determineDailyStatus(checkInTime, checkOutTime, dayRecords, isHoliday);
            dailyStat.setStatus(status);

        } catch (Exception e) {
            log.error("[AttendanceStatisticsUtil] 计算每日统计失败, 日期: {}", date, e);
            dailyStat.setStatus("ERROR");
            dailyStat.setRemark("计算异常: " + e.getMessage());
        }

        return dailyStat;
    }

    /**
     * 判断每日考勤状态
     */
    private String determineDailyStatus(LocalTime checkInTime, LocalTime checkOutTime,
            List<Map<String, Object>> dayRecords, boolean isHoliday) {
        if (isHoliday) {
            return "HOLIDAY";
        }

        if (checkInTime == null && checkOutTime == null) {
            return "ABSENT";
        }

        // 检查是否有请假记录
        boolean hasLeave = dayRecords.stream()
                .anyMatch(record -> "LEAVE".equals(record.get("recordType")));
        if (hasLeave) {
            return "LEAVE";
        }

        // 判断迟到早退
        boolean isLate = false;
        boolean isEarlyLeave = false;

        for (Map<String, Object> record : dayRecords) {
            Boolean lateFlag = (Boolean) record.get("isLate");
            Boolean earlyLeaveFlag = (Boolean) record.get("isEarlyLeave");

            if (Boolean.TRUE.equals(lateFlag)) {
                isLate = true;
            }
            if (Boolean.TRUE.equals(earlyLeaveFlag)) {
                isEarlyLeave = true;
            }
        }

        if (isLate && isEarlyLeave) {
            return "LATE_EARLY_LEAVE";
        } else if (isLate) {
            return "LATE";
        } else if (isEarlyLeave) {
            return "EARLY_LEAVE";
        }

        return "NORMAL";
    }

    /**
     * 计算工作日
     */
    private Set<LocalDate> calculateWorkDays(int year, int month, Set<LocalDate> holidays) {
        Set<LocalDate> workDays = new HashSet<>();

        try {
            LocalDate firstDay = LocalDate.of(year, month, 1);
            LocalDate lastDay = firstDay.with(TemporalAdjusters.lastDayOfMonth());

            for (LocalDate date = firstDay; !date.isAfter(lastDay); date = date.plusDays(1)) {
                if (isWorkDay(date, holidays)) {
                    workDays.add(date);
                }
            }

        } catch (Exception e) {
            log.error("[AttendanceStatisticsUtil] 计算工作日失败", e);
        }

        return workDays;
    }

    /**
     * 判断是否为工作日
     */
    private boolean isWorkDay(LocalDate date, Set<LocalDate> holidays) {
        if (holidays != null && holidays.contains(date)) {
            return false;
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    /**
     * 计算时间段统计
     *
     * @param startDate         开始日期
     * @param endDate           结束日期
     * @param attendanceRecords 考勤记录
     * @return 统计结果
     */
    public AttendanceStats calculatePeriodStats(LocalDate startDate, LocalDate endDate,
            List<Map<String, Object>> attendanceRecords, Set<LocalDate> holidays) {
        AttendanceStats stats = new AttendanceStats();

        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            log.warn("[AttendanceStatisticsUtil] 无效的日期范围");
            return stats;
        }

        try {
            log.debug("[AttendanceStatisticsUtil] 计算时间段统计: {} 到 {}", startDate, endDate);

            // 过滤指定时间段的记录
            List<Map<String, Object>> periodRecords = attendanceRecords.stream()
                    .filter(record -> {
                        LocalDate recordDate = (LocalDate) record.get("attendanceDate");
                        return recordDate != null &&
                                !recordDate.isBefore(startDate) &&
                                !recordDate.isAfter(endDate);
                    })
                    .collect(Collectors.toList());

            // 计算应出勤天数
            int totalWorkDays = 0;
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                if (isWorkDay(date, holidays)) {
                    totalWorkDays++;
                }
            }
            stats.setTotalWorkDays(totalWorkDays);

            // 按日期分组统计
            Map<LocalDate, List<Map<String, Object>>> recordsByDate = periodRecords.stream()
                    .filter(record -> record.get("attendanceDate") != null)
                    .collect(Collectors.groupingBy(record -> (LocalDate) record.get("attendanceDate")));

            int actualWorkDays = 0;
            int leaveDays = 0;
            int lateCount = 0;
            int earlyLeaveCount = 0;
            int totalOvertimeMinutes = 0;

            for (Map.Entry<LocalDate, List<Map<String, Object>>> entry : recordsByDate.entrySet()) {
                LocalDate date = entry.getKey();
                List<Map<String, Object>> dayRecords = entry.getValue();

                if (isWorkDay(date, holidays)) {
                    DailyStats dailyStat = calculateDailyStats(date, dayRecords, holidays.contains(date));

                    // 累计统计
                    String status = dailyStat.getStatus();
                    if ("NORMAL".equals(status) || "LATE".equals(status) || "EARLY_LEAVE".equals(status)) {
                        actualWorkDays++;
                    } else if ("LEAVE".equals(status)) {
                        leaveDays++;
                    }

                    if ("LATE".equals(status) || "LATE_EARLY_LEAVE".equals(status)) {
                        lateCount++;
                    }
                    if ("EARLY_LEAVE".equals(status) || "LATE_EARLY_LEAVE".equals(status)) {
                        earlyLeaveCount++;
                    }

                    totalOvertimeMinutes += dailyStat.getOvertimeMinutes();
                }
            }

            // 设置统计结果
            stats.setActualWorkDays(actualWorkDays);
            stats.setLeaveDays(leaveDays);
            stats.setAbsentDays(stats.getTotalWorkDays() - actualWorkDays - leaveDays);
            stats.setLateCount(lateCount);
            stats.setEarlyLeaveCount(earlyLeaveCount);
            stats.setOvertimeHours(totalOvertimeMinutes / 60);
            stats.setAttendanceRate(calculateAttendanceRate(stats.getTotalWorkDays(), actualWorkDays));

            // 计算详细统计
            Map<String, Integer> details = calculateDetailStats(recordsByDate);
            stats.setDetails(details);

            log.debug("[AttendanceStatisticsUtil] 时间段统计完成: 应出勤{}天, 实际{}天, 考勤率{}%",
                    stats.getTotalWorkDays(), stats.getActualWorkDays(), stats.getAttendanceRate());

        } catch (Exception e) {
            log.error("[AttendanceStatisticsUtil] 计算时间段统计失败", e);
        }

        return stats;
    }

    /**
     * 计算详细统计
     */
    private Map<String, Integer> calculateDetailStats(Map<LocalDate, List<Map<String, Object>>> recordsByDate) {
        Map<String, Integer> details = new HashMap<>();

        try {
            int normalDays = 0;
            int lateDays = 0;
            int earlyLeaveDays = 0;
            int lateEarlyLeaveDays = 0;
            int leaveDays = 0;
            int absentDays = 0;
            int holidayDays = 0;

            for (Map.Entry<LocalDate, List<Map<String, Object>>> entry : recordsByDate.entrySet()) {
                LocalDate date = entry.getKey();
                List<Map<String, Object>> dayRecords = entry.getValue();

                DailyStats dailyStat = calculateDailyStats(date, dayRecords, false);
                String status = dailyStat.getStatus();

                switch (status) {
                    case "NORMAL":
                        normalDays++;
                        break;
                    case "LATE":
                        lateDays++;
                        break;
                    case "EARLY_LEAVE":
                        earlyLeaveDays++;
                        break;
                    case "LATE_EARLY_LEAVE":
                        lateEarlyLeaveDays++;
                        break;
                    case "LEAVE":
                        leaveDays++;
                        break;
                    case "ABSENT":
                        absentDays++;
                        break;
                    case "HOLIDAY":
                        holidayDays++;
                        break;
                }
            }

            details.put("正常天数", normalDays);
            details.put("迟到天数", lateDays);
            details.put("早退天数", earlyLeaveDays);
            details.put("迟到早退天数", lateEarlyLeaveDays);
            details.put("请假天数", leaveDays);
            details.put("缺勤天数", absentDays);
            details.put("假日天数", holidayDays);

        } catch (Exception e) {
            log.error("[AttendanceStatisticsUtil] 计算详细统计失败", e);
        }

        return details;
    }

    /**
     * 计算员工考勤排名
     *
     * @param employeeStats 员工统计数据列表
     * @param sortBy        排序字段
     * @param limit         返回数量限制
     * @return 排名结果
     */
    public List<Map<String, Object>> calculateEmployeeRanking(List<Map<String, Object>> employeeStats,
            String sortBy, int limit) {
        if (employeeStats == null || employeeStats.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            log.debug("[AttendanceStatisticsUtil] 计算员工考勤排名，排序字段: {}", sortBy);

            // 根据排序字段进行排序
            Comparator<Map<String, Object>> comparator = getRankingComparator(sortBy);

            List<Map<String, Object>> sortedStats = employeeStats.stream()
                    .sorted(comparator)
                    .limit(limit)
                    .collect(Collectors.toList());

            // 添加排名
            for (int i = 0; i < sortedStats.size(); i++) {
                sortedStats.get(i).put("rank", i + 1);
            }

            return sortedStats;

        } catch (Exception e) {
            log.error("[AttendanceStatisticsUtil] 计算员工考勤排名失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取排序比较器
     */
    private Comparator<Map<String, Object>> getRankingComparator(String sortBy) {
        switch (sortBy.toLowerCase()) {
            case "attendance_rate":
                return (a, b) -> compareBigDecimal((BigDecimal) a.get("attendanceRate"),
                        (BigDecimal) b.get("attendanceRate"));
            case "overtime_hours":
                return (a, b) -> compareInteger((Integer) b.get("overtimeHours"),
                        (Integer) a.get("overtimeHours")); // 降序
            case "late_count":
                return (a, b) -> compareInteger((Integer) a.get("lateCount"),
                        (Integer) b.get("lateCount"));
            case "actual_work_days":
                return (a, b) -> compareInteger((Integer) b.get("actualWorkDays"),
                        (Integer) a.get("actualWorkDays")); // 降序
            default:
                return (a, b) -> compareBigDecimal((BigDecimal) a.get("attendanceRate"),
                        (BigDecimal) b.get("attendanceRate"));
        }
    }

    /**
     * 比较BigDecimal（降序）
     */
    private int compareBigDecimal(BigDecimal a, BigDecimal b) {
        if (a == null)
            return -1;
        if (b == null)
            return 1;
        return b.compareTo(a);
    }

    /**
     * 比较Integer（降序）
     */
    private int compareInteger(Integer a, Integer b) {
        if (a == null)
            return -1;
        if (b == null)
            return 1;
        return b.compareTo(a);
    }

    /**
     * 计算考勤趋势数据
     *
     * @param startDate         开始日期
     * @param endDate           结束日期
     * @param attendanceRecords 考勤记录
     * @return 趋势数据
     */
    public Map<String, Object> calculateAttendanceTrend(LocalDate startDate, LocalDate endDate,
            List<Map<String, Object>> attendanceRecords) {
        Map<String, Object> trendData = new HashMap<>();

        try {
            log.debug("[AttendanceStatisticsUtil] 计算考勤趋势数据: {} 到 {}", startDate, endDate);

            // 按周统计
            Map<Integer, AttendanceStats> weeklyStats = new HashMap<>();
            List<String> weeklyLabels = new ArrayList<>();
            List<BigDecimal> weeklyRates = new ArrayList<>();

            // 计算周统计数据
            LocalDate currentWeekStart = startDate;
            while (!currentWeekStart.isAfter(endDate)) {
                // 将变量复制为final，以便在lambda表达式中使用
                final LocalDate weekStart = currentWeekStart;
                LocalDate currentWeekEnd = currentWeekStart.plusDays(6);
                if (currentWeekEnd.isAfter(endDate)) {
                    currentWeekEnd = endDate;
                }
                final LocalDate weekEnd = currentWeekEnd;

                List<Map<String, Object>> weekRecords = attendanceRecords.stream()
                        .filter(record -> {
                            LocalDate recordDate = (LocalDate) record.get("attendanceDate");
                            return recordDate != null &&
                                    !recordDate.isBefore(weekStart) &&
                                    !recordDate.isAfter(weekEnd);
                        })
                        .collect(Collectors.toList());

                AttendanceStats weekStats = calculatePeriodStats(currentWeekStart, currentWeekEnd, weekRecords,
                        new HashSet<>());
                int weekNumber = currentWeekStart.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
                weeklyStats.put(weekNumber, weekStats);

                weeklyLabels.add("第" + weekNumber + "周");
                weeklyRates.add(weekStats.getAttendanceRate());

                currentWeekStart = currentWeekStart.plusWeeks(1);
            }

            trendData.put("weeklyLabels", weeklyLabels);
            trendData.put("weeklyRates", weeklyRates);
            trendData.put("weeklyStats", weeklyStats);

            log.debug("[AttendanceStatisticsUtil] 考勤趋势数据计算完成，共 {} 周数据", weeklyLabels.size());

        } catch (Exception e) {
            log.error("[AttendanceStatisticsUtil] 计算考勤趋势数据失败", e);
        }

        return trendData;
    }
}
