package net.lab1024.sa.attendance.manager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.Resource;
/**
 * 考勤统计管理器
 *
 * <p>
 * 考勤模块的统计分析和报表生成管理器，提供全面的考勤数据分析功能
 * 严格遵循repowiki编码规范：使用jakarta包名、@Resource注入、SLF4J日志
 * 实现四层架构中的Manager层，提供复杂的统计分析和数据聚合逻辑
 * </p>
 *
 * <p>
 * 功能职责：
 * - 统计计算：工作时长、出勤率、加班统计等
 * - 异常统计：迟到、早退、缺勤等异常情况统计
 * - 趋势分析：考勤数据的趋势变化分析
 * - 报表生成：各类考勤报表的生成和导出
 * - 数据聚合：多维度数据聚合和分析
 * - 异步处理：大数据量的异步统计分析
 * </p>
 *
 * @author SmartAdmin Team
 * @version 1.0.0
 * @since 2025-11-16
 */
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.dao.AttendanceDao;
import net.lab1024.sa.attendance.dao.AttendanceRuleDao;
import net.lab1024.sa.attendance.dao.AttendanceScheduleDao;
import net.lab1024.sa.attendance.domain.entity.AttendanceRecordEntity;
import net.lab1024.sa.attendance.domain.entity.AttendanceScheduleEntity;

@Slf4j
@Component
public class AttendanceStatisticsManager {

    @Resource
    private AttendanceDao attendanceRepository;

    @Resource
    private AttendanceRuleDao attendanceRuleDao;

    @Resource
    private AttendanceScheduleDao attendanceScheduleRepository;

    @Resource
    // TEMP: Cache functionality disabled

    // 线程池
    private final ExecutorService statisticsExecutor = Executors.newFixedThreadPool(3);

    // ===== 考勤统计结果类 =====

    /**
     * 员工月度考勤统计
     */
    public static class EmployeeMonthlyStatistics {
        private Long employeeId;
        private String yearMonth;
        private Integer workDays; // 工作天数
        private Integer actualWorkDays; // 实际出勤天数
        private Integer leaveDays; // 请假天数
        private Integer overtimeDays; // 加班天数
        private Integer lateDays; // 迟到天数
        private Integer earlyLeaveDays; // 早退天数
        private Integer absenceDays; // 缺勤天数
        private BigDecimal totalWorkHours; // 总工作时长
        private BigDecimal totalOvertimeHours; // 总加班时长
        private BigDecimal attendanceRate; // 出勤率
        private Map<String, Integer> exceptionTypeCount; // 异常类型统计

        // Getters and Setters
        public Long getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Long employeeId) {
            this.employeeId = employeeId;
        }

        public String getYearMonth() {
            return yearMonth;
        }

        public void setYearMonth(String yearMonth) {
            this.yearMonth = yearMonth;
        }

        public Integer getWorkDays() {
            return workDays;
        }

        public void setWorkDays(Integer workDays) {
            this.workDays = workDays;
        }

        public Integer getActualWorkDays() {
            return actualWorkDays;
        }

        public void setActualWorkDays(Integer actualWorkDays) {
            this.actualWorkDays = actualWorkDays;
        }

        public Integer getLeaveDays() {
            return leaveDays;
        }

        public void setLeaveDays(Integer leaveDays) {
            this.leaveDays = leaveDays;
        }

        public Integer getOvertimeDays() {
            return overtimeDays;
        }

        public void setOvertimeDays(Integer overtimeDays) {
            this.overtimeDays = overtimeDays;
        }

        public Integer getLateDays() {
            return lateDays;
        }

        public void setLateDays(Integer lateDays) {
            this.lateDays = lateDays;
        }

        public Integer getEarlyLeaveDays() {
            return earlyLeaveDays;
        }

        public void setEarlyLeaveDays(Integer earlyLeaveDays) {
            this.earlyLeaveDays = earlyLeaveDays;
        }

        public Integer getAbsenceDays() {
            return absenceDays;
        }

        public void setAbsenceDays(Integer absenceDays) {
            this.absenceDays = absenceDays;
        }

        public BigDecimal getTotalWorkHours() {
            return totalWorkHours;
        }

        public void setTotalWorkHours(BigDecimal totalWorkHours) {
            this.totalWorkHours = totalWorkHours;
        }

        public BigDecimal getTotalOvertimeHours() {
            return totalOvertimeHours;
        }

        public void setTotalOvertimeHours(BigDecimal totalOvertimeHours) {
            this.totalOvertimeHours = totalOvertimeHours;
        }

        public BigDecimal getAttendanceRate() {
            return attendanceRate;
        }

        public void setAttendanceRate(BigDecimal attendanceRate) {
            this.attendanceRate = attendanceRate;
        }

        public Map<String, Integer> getExceptionTypeCount() {
            return exceptionTypeCount;
        }

        public void setExceptionTypeCount(Map<String, Integer> exceptionTypeCount) {
            this.exceptionTypeCount = exceptionTypeCount;
        }
    }

    /**
     * 部门月度考勤统计
     */
    public static class DepartmentMonthlyStatistics {
        private Long departmentId;
        private String departmentName;
        private String yearMonth;
        private Integer totalEmployees; // 员工总数
        private Integer workDays; // 工作天数
        private BigDecimal averageAttendanceRate; // 平均出勤率
        private BigDecimal totalWorkHours; // 总工作时长
        private BigDecimal totalOvertimeHours; // 总加班时长
        private Map<String, Integer> summaryStatistics; // 汇总统计
        private List<EmployeeMonthlyStatistics> employeeStatistics; // 员工明细

        // Getters and Setters
        public Long getDepartmentId() {
            return departmentId;
        }

        public void setDepartmentId(Long departmentId) {
            this.departmentId = departmentId;
        }

        public String getDepartmentName() {
            return departmentName;
        }

        public void setDepartmentName(String departmentName) {
            this.departmentName = departmentName;
        }

        public String getYearMonth() {
            return yearMonth;
        }

        public void setYearMonth(String yearMonth) {
            this.yearMonth = yearMonth;
        }

        public Integer getTotalEmployees() {
            return totalEmployees;
        }

        public void setTotalEmployees(Integer totalEmployees) {
            this.totalEmployees = totalEmployees;
        }

        public Integer getWorkDays() {
            return workDays;
        }

        public void setWorkDays(Integer workDays) {
            this.workDays = workDays;
        }

        public BigDecimal getAverageAttendanceRate() {
            return averageAttendanceRate;
        }

        public void setAverageAttendanceRate(BigDecimal averageAttendanceRate) {
            this.averageAttendanceRate = averageAttendanceRate;
        }

        public BigDecimal getTotalWorkHours() {
            return totalWorkHours;
        }

        public void setTotalWorkHours(BigDecimal totalWorkHours) {
            this.totalWorkHours = totalWorkHours;
        }

        public BigDecimal getTotalOvertimeHours() {
            return totalOvertimeHours;
        }

        public void setTotalOvertimeHours(BigDecimal totalOvertimeHours) {
            this.totalOvertimeHours = totalOvertimeHours;
        }

        public Map<String, Integer> getSummaryStatistics() {
            return summaryStatistics;
        }

        public void setSummaryStatistics(Map<String, Integer> summaryStatistics) {
            this.summaryStatistics = summaryStatistics;
        }

        public List<EmployeeMonthlyStatistics> getEmployeeStatistics() {
            return employeeStatistics;
        }

        public void setEmployeeStatistics(List<EmployeeMonthlyStatistics> employeeStatistics) {
            this.employeeStatistics = employeeStatistics;
        }
    }

    // ===== 核心统计方法 =====

    /**
     * 生成员工月度考勤统计
     *
     * @param employeeId 员工ID
     * @param yearMonth  年月份（格式：2025-01）
     * @return 月度统计结果
     */
    public EmployeeMonthlyStatistics generateEmployeeMonthlyStatistics(Long employeeId, String yearMonth) {
        if (employeeId == null || yearMonth == null) {
            log.warn("生成员工月度考勤统计失败：参数为空");
            return new EmployeeMonthlyStatistics();
        }

        try {
            // 1. 检查缓存
            // TEMP: Cache functionality disabled
            // String cacheKey = String.format("attendance:statistics:employee:%s:%s",
            // employeeId, yearMonth);
            // EmployeeMonthlyStatistics cachedResult = cacheManager.get(cacheKey, () ->
            // null, EmployeeMonthlyStatistics.class);
            // if (cachedResult != null) {
            // return cachedResult;
            // }

            // 2. 解析年月份
            YearMonth ym = YearMonth.parse(yearMonth);
            LocalDate startDate = ym.atDay(1);
            LocalDate endDate = ym.atEndOfMonth();

            // 3. 查询考勤记录
            List<AttendanceRecordEntity> records = attendanceRepository.selectEmployeeMonthlyRecords(employeeId,
                    yearMonth);
            if (CollectionUtils.isEmpty(records)) {
                return createEmptyEmployeeStatistics(employeeId, yearMonth);
            }

            // 4. 生成统计结果
            EmployeeMonthlyStatistics statistics = new EmployeeMonthlyStatistics();
            statistics.setEmployeeId(employeeId);
            statistics.setYearMonth(yearMonth);

            // 5. 计算各项统计数据
            calculateEmployeeStatistics(employeeId, startDate, endDate, records, statistics);

            // 6. 缓存结果
            // TEMP: Cache functionality disabled

            log.debug("员工月度考勤统计生成完成：员工ID={}, 月份={}, 出勤率={}%",
                    employeeId, yearMonth,
                    statistics.getAttendanceRate() != null
                            ? statistics.getAttendanceRate().multiply(BigDecimal.valueOf(100))
                            : BigDecimal.ZERO);

            return statistics;

        } catch (Exception e) {
            log.error("生成员工月度考勤统计异常：员工ID={}, 月份={}", employeeId, yearMonth, e);
            return new EmployeeMonthlyStatistics();
        }
    }

    /**
     * 批量生成员工月度考勤统计
     *
     * @param employeeIds 员工ID列表
     * @param yearMonth   年月份
     * @return 统计结果列表
     */
    public List<EmployeeMonthlyStatistics> batchGenerateEmployeeStatistics(List<Long> employeeIds, String yearMonth) {
        if (CollectionUtils.isEmpty(employeeIds) || yearMonth == null) {
            log.warn("批量生成员工月度考勤统计失败：参数为空");
            return Collections.emptyList();
        }

        try {
            log.info("开始批量生成员工月度考勤统计：员工数={}, 月份={}", employeeIds.size(), yearMonth);

            // 异步批量处理
            List<CompletableFuture<EmployeeMonthlyStatistics>> futures = employeeIds.stream()
                    .map(employeeId -> CompletableFuture.supplyAsync(
                            () -> generateEmployeeMonthlyStatistics(employeeId, yearMonth), statisticsExecutor))
                    .toList();

            // 等待所有任务完成
            List<EmployeeMonthlyStatistics> results = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

            log.info("批量生成员工月度考勤统计完成：员工数={}, 月份={}, 成功数={}",
                    employeeIds.size(), yearMonth, results.size());

            return results;

        } catch (Exception e) {
            log.error("批量生成员工月度考勤统计异常：月份={}", yearMonth, e);
            return Collections.emptyList();
        }
    }

    /**
     * 生成部门月度考勤统计
     *
     * @param departmentId 部门ID
     * @param yearMonth    年月份
     * @param employeeIds  部门员工ID列表
     * @return 部门统计结果
     */
    public DepartmentMonthlyStatistics generateDepartmentMonthlyStatistics(Long departmentId, String yearMonth,
            List<Long> employeeIds) {
        if (departmentId == null || yearMonth == null || CollectionUtils.isEmpty(employeeIds)) {
            log.warn("生成部门月度考勤统计失败：参数为空");
            return new DepartmentMonthlyStatistics();
        }

        try {
            // 1. 检查缓存
            // TEMP: Cache functionality disabled
            // String cacheKey = String.format("attendance:statistics:department:%s:%s",
            // departmentId, yearMonth);
            // DepartmentMonthlyStatistics cachedResult = cacheManager.get(cacheKey, () ->
            // null, DepartmentMonthlyStatistics.class);
            // if (cachedResult != null) {
            // return cachedResult;
            // }

            // 2. 生成员工统计
            List<EmployeeMonthlyStatistics> employeeStatistics = batchGenerateEmployeeStatistics(employeeIds,
                    yearMonth);

            // 3. 生成部门统计
            DepartmentMonthlyStatistics departmentStatistics = new DepartmentMonthlyStatistics();
            departmentStatistics.setDepartmentId(departmentId);
            departmentStatistics.setYearMonth(yearMonth);
            departmentStatistics.setTotalEmployees(employeeIds.size());
            departmentStatistics.setEmployeeStatistics(employeeStatistics);

            // 4. 计算部门汇总数据
            calculateDepartmentStatistics(departmentStatistics, employeeStatistics);

            // 5. 缓存结果
            // TEMP: Cache functionality disabled

            log.debug("部门月度考勤统计生成完成：部门ID={}, 月份={}, 平均出勤率={}%",
                    departmentId, yearMonth,
                    departmentStatistics.getAverageAttendanceRate() != null
                            ? departmentStatistics.getAverageAttendanceRate().multiply(BigDecimal.valueOf(100))
                            : BigDecimal.ZERO);

            return departmentStatistics;

        } catch (Exception e) {
            log.error("生成部门月度考勤统计异常：部门ID={}, 月份={}", departmentId, yearMonth, e);
            return new DepartmentMonthlyStatistics();
        }
    }

    // ===== 统计计算方法 =====

    /**
     * 计算员工统计数据
     *
     * @param employeeId 员工ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @param records    考勤记录
     * @param statistics 统计结果
     */
    private void calculateEmployeeStatistics(Long employeeId, LocalDate startDate, LocalDate endDate,
            List<AttendanceRecordEntity> records, EmployeeMonthlyStatistics statistics) {
        // 1. 计算工作天数
        int workDays = 0;
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            if (attendanceScheduleRepository.selectByEmployeeAndDate(employeeId, currentDate) != null) {
                workDays++;
            }
            currentDate = currentDate.plusDays(1);
        }
        statistics.setWorkDays(workDays);

        // 2. 初始化统计变量
        int actualWorkDays = 0;
        int lateDays = 0;
        int earlyLeaveDays = 0;
        int absenceDays = 0;
        int overtimeDays = 0;
        BigDecimal totalWorkHours = BigDecimal.ZERO;
        BigDecimal totalOvertimeHours = BigDecimal.ZERO;
        Map<String, Integer> exceptionTypeCount = new HashMap<>();

        // 3. 遍历考勤记录进行统计
        for (AttendanceRecordEntity record : records) {
            if (record.getAttendanceStatus() != null) {
                switch (record.getAttendanceStatus()) {
                    case "NORMAL":
                        actualWorkDays++;
                        break;
                    case "LATE":
                        actualWorkDays++;
                        lateDays++;
                        break;
                    case "EARLY_LEAVE":
                        actualWorkDays++;
                        earlyLeaveDays++;
                        break;
                    case "LATE_EARLY_LEAVE":
                        actualWorkDays++;
                        lateDays++;
                        earlyLeaveDays++;
                        break;
                    case "ABSENCE":
                        absenceDays++;
                        break;
                    case "INSUFFICIENT_HOURS":
                        actualWorkDays++;
                        break;
                }
            }

            // 统计异常类型
            if (record.getExceptionType() != null) {
                // 使用null安全的方式合并统计
                Integer currentCount = exceptionTypeCount.getOrDefault(record.getExceptionType(), 0);
                exceptionTypeCount.put(record.getExceptionType(), currentCount + 1);
            }

            // 累计工作时长
            if (record.getWorkHours() != null) {
                totalWorkHours = totalWorkHours.add(record.getWorkHours());
            }

            // 累计加班时长
            if (record.getOvertimeHours() != null && record.getOvertimeHours().compareTo(BigDecimal.ZERO) > 0) {
                totalOvertimeHours = totalOvertimeHours.add(record.getOvertimeHours());
                overtimeDays++;
            }
        }

        // 4. 计算出勤率
        BigDecimal attendanceRate = workDays > 0
                ? BigDecimal.valueOf(actualWorkDays).divide(BigDecimal.valueOf(workDays), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 5. 设置统计结果
        statistics.setActualWorkDays(actualWorkDays);
        statistics.setLateDays(lateDays);
        statistics.setEarlyLeaveDays(earlyLeaveDays);
        statistics.setAbsenceDays(absenceDays);
        statistics.setOvertimeDays(overtimeDays);
        statistics.setTotalWorkHours(totalWorkHours);
        statistics.setTotalOvertimeHours(totalOvertimeHours);
        statistics.setAttendanceRate(attendanceRate);
        statistics.setExceptionTypeCount(exceptionTypeCount);

        log.debug("员工统计数据计算完成：员工ID={}, 实际出勤={}, 迟到={}, 早退={}, 缺勤={}",
                employeeId, actualWorkDays, lateDays, earlyLeaveDays, absenceDays);
    }

    /**
     * 计算部门统计数据
     *
     * @param departmentStatistics 部门统计结果
     * @param employeeStatistics   员工统计列表
     */
    private void calculateDepartmentStatistics(DepartmentMonthlyStatistics departmentStatistics,
            List<EmployeeMonthlyStatistics> employeeStatistics) {
        if (CollectionUtils.isEmpty(employeeStatistics)) {
            return;
        }

        // 1. 初始化汇总变量
        BigDecimal totalAttendanceRate = BigDecimal.ZERO;
        BigDecimal totalWorkHours = BigDecimal.ZERO;
        BigDecimal totalOvertimeHours = BigDecimal.ZERO;
        Map<String, Integer> summaryStatistics = new HashMap<>();

        // 2. 汇总员工数据
        for (EmployeeMonthlyStatistics empStats : employeeStatistics) {
            // 累计出勤率
            if (empStats.getAttendanceRate() != null) {
                totalAttendanceRate = totalAttendanceRate.add(empStats.getAttendanceRate());
            }

            // 累计工作时长
            if (empStats.getTotalWorkHours() != null) {
                totalWorkHours = totalWorkHours.add(empStats.getTotalWorkHours());
            }

            // 累计加班时长
            if (empStats.getTotalOvertimeHours() != null) {
                totalOvertimeHours = totalOvertimeHours.add(empStats.getTotalOvertimeHours());
            }

            // 汇总异常统计
            if (empStats.getExceptionTypeCount() != null) {
                empStats.getExceptionTypeCount()
                        .forEach((exceptionType, count) -> {
                            // 使用null安全的方式合并统计
                            if (exceptionType != null && count != null) {
                                Integer currentCount = summaryStatistics.getOrDefault(exceptionType, 0);
                                summaryStatistics.put(exceptionType, currentCount + count);
                            }
                        });
            }
        }

        // 3. 计算平均出勤率
        BigDecimal averageAttendanceRate = employeeStatistics.size() > 0
                ? totalAttendanceRate.divide(BigDecimal.valueOf(employeeStatistics.size()), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 4. 设置汇总结果
        departmentStatistics.setAverageAttendanceRate(averageAttendanceRate);
        departmentStatistics.setTotalWorkHours(totalWorkHours);
        departmentStatistics.setTotalOvertimeHours(totalOvertimeHours);
        departmentStatistics.setSummaryStatistics(summaryStatistics);

        log.debug("部门统计数据计算完成：部门ID={}, 平均出勤率={}, 总工作时长={}小时",
                departmentStatistics.getDepartmentId(), averageAttendanceRate, totalWorkHours);
    }

    // ===== 辅助方法 =====

    /**
     * 创建空的员工统计数据
     *
     * @param employeeId 员工ID
     * @param yearMonth  年月份
     * @return 空的统计结果
     */
    private EmployeeMonthlyStatistics createEmptyEmployeeStatistics(Long employeeId, String yearMonth) {
        EmployeeMonthlyStatistics statistics = new EmployeeMonthlyStatistics();
        statistics.setEmployeeId(employeeId);
        statistics.setYearMonth(yearMonth);
        statistics.setWorkDays(0);
        statistics.setActualWorkDays(0);
        statistics.setLateDays(0);
        statistics.setEarlyLeaveDays(0);
        statistics.setAbsenceDays(0);
        statistics.setOvertimeDays(0);
        statistics.setTotalWorkHours(BigDecimal.ZERO);
        statistics.setTotalOvertimeHours(BigDecimal.ZERO);
        statistics.setAttendanceRate(BigDecimal.ZERO);
        statistics.setExceptionTypeCount(new HashMap<>());
        return statistics;
    }

    /**
     * 生成日期范围的工作日统计
     *
     * @param employeeId 员工ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 工作日统计
     */
    public Map<LocalDate, Boolean> generateWorkdayStatistics(Long employeeId, LocalDate startDate, LocalDate endDate) {
        if (employeeId == null || startDate == null || endDate == null) {
            log.warn("生成工作日统计失败：参数为空");
            return Collections.emptyMap();
        }

        try {
            Map<LocalDate, Boolean> workdayMap = new HashMap<>();
            LocalDate currentDate = startDate;

            while (!currentDate.isAfter(endDate)) {
                AttendanceScheduleEntity schedule = attendanceScheduleRepository.selectByEmployeeAndDate(employeeId,
                        currentDate);
                boolean isWorkday = schedule != null && schedule.isWorkDay();
                workdayMap.put(currentDate, isWorkday);
                currentDate = currentDate.plusDays(1);
            }

            log.debug("工作日统计生成完成：员工ID={}, 日期范围={} 到 {}, 工作日数={}",
                    employeeId, startDate, endDate,
                    workdayMap.values().stream().mapToInt(workday -> workday ? 1 : 0).sum());

            return workdayMap;

        } catch (Exception e) {
            log.error("生成工作日统计异常：员工ID={}, 日期范围={} 到 {}", employeeId, startDate, endDate, e);
            return Collections.emptyMap();
        }
    }

    /**
     * 考勤异常趋势分析
     *
     * @param employeeId 员工ID
     * @param months     月份数
     * @return 异常趋势数据
     */
    public Map<String, Map<String, Integer>> analyzeExceptionTrends(Long employeeId, int months) {
        if (employeeId == null || months <= 0) {
            log.warn("考勤异常趋势分析失败：参数无效");
            return Collections.emptyMap();
        }

        try {
            Map<String, Map<String, Integer>> trendData = new HashMap<>();
            LocalDate currentDate = LocalDate.now();

            for (int i = months - 1; i >= 0; i--) {
                YearMonth ym = YearMonth.from(currentDate.minusMonths(i));
                String yearMonth = ym.toString();

                // 生成该月统计
                EmployeeMonthlyStatistics statistics = generateEmployeeMonthlyStatistics(employeeId, yearMonth);

                // 提取异常数据
                Map<String, Integer> monthData = new HashMap<>();
                monthData.put("LATE", statistics.getLateDays());
                monthData.put("EARLY_LEAVE", statistics.getEarlyLeaveDays());
                monthData.put("ABSENCE", statistics.getAbsenceDays());
                monthData.put("OVERTIME", statistics.getOvertimeDays());

                trendData.put(yearMonth, monthData);
            }

            log.debug("考勤异常趋势分析完成：员工ID={}, 月数={}", employeeId, months);
            return trendData;

        } catch (Exception e) {
            log.error("考勤异常趋势分析异常：员工ID={}, 月数={}", employeeId, months, e);
            return Collections.emptyMap();
        }
    }

    /**
     * 清除统计数据缓存
     *
     * @param employeeId 员工ID（可选）
     * @param yearMonth  年月份（可选）
     */
    public void clearStatisticsCache(Long employeeId, String yearMonth) {
        // TEMP: Cache functionality disabled
        try {
            if (employeeId != null && yearMonth != null) {
                // 清除指定员工指定月份的缓存
                // TEMP: Cache functionality disabled
                // cacheManager.evict(String.format("attendance:statistics:employee:%s:%s",
                // employeeId, yearMonth));
            } else if (yearMonth != null) {
                // 清除指定月份的所有缓存
                // TEMP: Cache functionality disabled
                // cacheManager.evictPattern(String.format("attendance:statistics:*:%s",
                // yearMonth));
            } else {
                // 清除所有统计缓存
                // TEMP: Cache functionality disabled
                // cacheManager.evictPattern("attendance:statistics:*");
            }

            log.info("统计数据缓存清除完成：员工ID={}, 月份={}", employeeId, yearMonth);

        } catch (Exception e) {
            log.error("清除统计数据缓存异常：员工ID={}, 月份={}", employeeId, yearMonth, e);
        }
    }
}
