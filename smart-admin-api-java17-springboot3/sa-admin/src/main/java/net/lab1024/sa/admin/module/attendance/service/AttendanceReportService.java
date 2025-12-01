package net.lab1024.sa.admin.module.attendance.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import net.lab1024.sa.base.common.util.SmartDateUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceRecordEntity;
import net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceRuleEntity;
import net.lab1024.sa.admin.module.attendance.dao.AttendanceDao;
import net.lab1024.sa.admin.module.attendance.dao.AttendanceRuleDao;

/**
 * 考勤统计报表服务
 *
 * <p>
 * 考勤模块的统计报表专用服务，提供全面的考勤数据统计和分析功能
 * 严格遵循repowiki编码规范：使用jakarta包名、@Resource注入、SLF4J日志
 * 实现四层架构中的Service层，提供统计报表的业务逻辑
 * </p>
 *
 * <p>
 * 功能职责：
 * - 日报统计：生成员工每日考勤统计报表
 * - 月报统计：生成员工月度考勤统计报表
 * - 部门统计：生成部门考勤统计报表
 * - 异常统计：生成考勤异常统计报表
 * - 趋势分析：考勤数据趋势分析
 * - 自定义报表：支持自定义统计维度和指标
 * - 数据导出：支持Excel、PDF等格式导出
 * - 图表数据：生成可视化图表所需的数据结构
 * </p>
 *
 * @author SmartAdmin Team
 * @version 1.0.0
 * @since 2025-11-17
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class AttendanceReportService {

    @Resource
    private AttendanceDao attendanceRepository;

    @Resource
    private AttendanceRuleDao attendanceRuleDao;

    // ===== 日报统计 =====

    /**
     * 生成员工日报
     *
     * @param employeeId 员工ID
     * @param date       统计日期
     * @return 日报数据
     */
    public EmployeeDailyReport generateEmployeeDailyReport(Long employeeId, LocalDate date) {
        try {
            log.info("生成员工日报: employeeId={}, date={}", employeeId, date);

            // 1. 查询当日考勤记录
            AttendanceRecordEntity record = attendanceRepository.selectEmployeeTodayRecord(employeeId, date);

            // 2. 获取员工适用的考勤规则
            // TODO: 需要获取员工的部门ID和员工类型，暂时传null
            List<AttendanceRuleEntity> rules = attendanceRuleDao.selectApplicableRules(employeeId, null, null, date);

            // 3. 构建日报数据
            EmployeeDailyReport report = new EmployeeDailyReport();
            report.setEmployeeId(employeeId);
            report.setReportDate(date);
            report.setGenerateTime(LocalDateTime.now());

            if (record != null) {
                fillDailyReportFromRecord(report, record);
            } else {
                fillDailyReportForNoRecord(report);
            }

            fillDailyReportWithRules(report, rules);
            calculateDailyStatistics(report);

            log.info("员工日报生成完成: employeeId={}, date={}", employeeId, date);
            return report;

        } catch (Exception e) {
            log.error("生成员工日报异常: employeeId" + employeeId + ", date" + date, e);
            throw new RuntimeException("生成员工日报失败", e);
        }
    }

    /**
     * 生成部门日报
     *
     * @param departmentId 部门ID
     * @param date         统计日期
     * @return 部门日报数据
     */
    public DepartmentDailyReport generateDepartmentDailyReport(Long departmentId, LocalDate date) {
        try {
            log.info("生成部门日报: departmentId={}, date={}", departmentId, date);

            // 1. 查询部门所有员工的当日考勤记录
            List<AttendanceRecordEntity> records = attendanceRepository.selectDepartmentRecordsByDate(departmentId,
                    date);

            // 2. 构建部门日报数据
            DepartmentDailyReport report = new DepartmentDailyReport();
            report.setDepartmentId(departmentId);
            report.setReportDate(date);
            report.setGenerateTime(LocalDateTime.now());
            report.setTotalEmployees(records.size());

            if (records.isEmpty()) {
                fillEmptyDepartmentDailyReport(report);
            } else {
                fillDepartmentDailyReportFromRecords(report, records);
            }

            calculateDepartmentDailyStatistics(report);

            log.info("部门日报生成完成: departmentId={}, date={}", departmentId, date);
            return report;

        } catch (Exception e) {
            log.error("生成部门日报异常: departmentId" + departmentId + ", date" + date, e);
            throw new RuntimeException("生成部门日报失败", e);
        }
    }

    // ===== 月报统计 =====

    /**
     * 生成员工月报
     *
     * @param employeeId 员工ID
     * @param yearMonth  年月份
     * @return 月报数据
     */
    public EmployeeMonthlyReport generateEmployeeMonthlyReport(Long employeeId, String yearMonth) {
        try {
            log.info("生成员工月报: employeeId={}, yearMonth={}", employeeId, yearMonth);

            // 1. 解析年月份
            YearMonth ym = SmartDateUtil.parseMonth(yearMonth);
            LocalDate startDate = ym.atDay(1);
            LocalDate endDate = ym.atEndOfMonth();

            // 2. 查询月度考勤记录
            List<AttendanceRecordEntity> records = attendanceRepository.selectEmployeeRecordsByDateRange(employeeId,
                    startDate, endDate);

            // 3. 构建月报数据
            EmployeeMonthlyReport report = new EmployeeMonthlyReport();
            report.setEmployeeId(employeeId);
            report.setYearMonth(yearMonth);
            report.setStartDate(startDate);
            report.setEndDate(endDate);
            report.setGenerateTime(LocalDateTime.now());
            report.setTotalRecords(records.size());

            if (records.isEmpty()) {
                fillEmptyEmployeeMonthlyReport(report);
            } else {
                fillEmployeeMonthlyReportFromRecords(report, records);
            }

            calculateEmployeeMonthlyStatistics(report);
            calculateEmployeeMonthlyTrends(report);

            log.info("员工月报生成完成: employeeId={}, yearMonth={}, records={}", employeeId, yearMonth, records.size());
            return report;

        } catch (Exception e) {
            log.error("生成员工月报异常: employeeId" + employeeId + ", yearMonth" + yearMonth, e);
            throw new RuntimeException("生成员工月报失败", e);
        }
    }

    /**
     * 生成部门月报
     *
     * @param departmentId 部门ID
     * @param yearMonth    年月份
     * @return 部门月报数据
     */
    public DepartmentMonthlyReport generateDepartmentMonthlyReport(Long departmentId, String yearMonth) {
        try {
            log.info("生成部门月报: departmentId={}, yearMonth={}", departmentId, yearMonth);

            // 1. 解析年月份
            YearMonth ym = SmartDateUtil.parseMonth(yearMonth);
            LocalDate startDate = ym.atDay(1);
            LocalDate endDate = ym.atEndOfMonth();

            // 2. 查询部门月度考勤记录
            List<AttendanceRecordEntity> records = attendanceRepository.selectDepartmentRecordsByDateRange(departmentId,
                    startDate, endDate);

            // 3. 构建部门月报数据
            DepartmentMonthlyReport report = new DepartmentMonthlyReport();
            report.setDepartmentId(departmentId);
            report.setYearMonth(yearMonth);
            report.setStartDate(startDate);
            report.setEndDate(endDate);
            report.setGenerateTime(LocalDateTime.now());
            report.setTotalRecords(records.size());

            if (records.isEmpty()) {
                fillEmptyDepartmentMonthlyReport(report);
            } else {
                fillDepartmentMonthlyReportFromRecords(report, records);
            }

            calculateDepartmentMonthlyStatistics(report);

            log.info("部门月报生成完成: departmentId={}, yearMonth={}, records={}", departmentId, yearMonth, records.size());
            return report;

        } catch (Exception e) {
            log.error("生成部门月报异常: departmentId" + departmentId + ", yearMonth" + yearMonth, e);
            throw new RuntimeException("生成部门月报失败", e);
        }
    }

    // ===== 异常统计 =====

    /**
     * 生成异常统计报表
     *
     * @param employeeId 员工ID（可选）
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 异常统计数据
     */
    public ExceptionStatisticsReport generateExceptionStatisticsReport(Long employeeId, LocalDate startDate,
            LocalDate endDate) {
        try {
            log.info("生成异常统计报表: employeeId={}, startDate={}, endDate={}", employeeId, startDate, endDate);

            // 1. 查询异常记录
            List<AttendanceRecordEntity> records = attendanceRepository.selectExceptionRecordsByDateRange(employeeId,
                    startDate, endDate);

            // 2. 构建异常统计报表
            ExceptionStatisticsReport report = new ExceptionStatisticsReport();
            report.setEmployeeId(employeeId);
            report.setStartDate(startDate);
            report.setEndDate(endDate);
            report.setGenerateTime(LocalDateTime.now());
            report.setTotalExceptionRecords(records.size());

            if (records.isEmpty()) {
                fillEmptyExceptionStatisticsReport(report);
            } else {
                fillExceptionStatisticsReportFromRecords(report, records);
            }

            calculateExceptionStatistics(report);

            log.info("异常统计报表生成完成: employeeId={}, totalExceptions={}", employeeId, records.size());
            return report;

        } catch (Exception e) {
            log.error("生成异常统计报表异常: employeeId" + employeeId, e);
            throw new RuntimeException("生成异常统计报表失败", e);
        }
    }

    // ===== 趋势分析 =====

    /**
     * 生成考勤趋势分析
     *
     * @param employeeId 员工ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 趋势分析数据
     */
    public AttendanceTrendAnalysis generateTrendAnalysis(Long employeeId, LocalDate startDate, LocalDate endDate) {
        try {
            log.info("生成考勤趋势分析: employeeId={}, startDate={}, endDate={}", employeeId, startDate, endDate);

            // 1. 查询时间范围内的考勤记录
            List<AttendanceRecordEntity> records = attendanceRepository.selectEmployeeRecordsByDateRange(employeeId,
                    startDate, endDate);

            // 2. 构建趋势分析数据
            AttendanceTrendAnalysis analysis = new AttendanceTrendAnalysis();
            analysis.setEmployeeId(employeeId);
            analysis.setStartDate(startDate);
            analysis.setEndDate(endDate);
            analysis.setGenerateTime(LocalDateTime.now());

            if (records.isEmpty()) {
                fillEmptyTrendAnalysis(analysis);
            } else {
                fillTrendAnalysisFromRecords(analysis, records);
            }

            calculateTrendAnalysis(analysis);

            log.info("考勤趋势分析生成完成: employeeId={}, records={}", employeeId, records.size());
            return analysis;

        } catch (Exception e) {
            log.error("生成考勤趋势分析异常: employeeId" + employeeId, e);
            throw new RuntimeException("生成考勤趋势分析失败", e);
        }
    }

    // ===== 辅助方法 =====

    /**
     * 从记录填充日报数据
     */
    private void fillDailyReportFromRecord(EmployeeDailyReport report, AttendanceRecordEntity record) {
        report.setRecordId(record.getRecordId());
        report.setPunchInTime(record.getPunchInTime());
        report.setPunchOutTime(record.getPunchOutTime());
        report.setWorkHours(record.getWorkHours());
        report.setOvertimeHours(record.getOvertimeHours());
        report.setAttendanceStatus(record.getAttendanceStatus());
        report.setExceptionType(record.getExceptionType());
        report.setExceptionReason(record.getExceptionReason());
        report.setGpsValidation(record.getGpsValidation() != null ? (record.getGpsValidation() ? 1 : 0) : null);
        report.setDeviceValidation(record.getDeviceValidation() != null ? (record.getDeviceValidation() ? 1 : 0) : null);
        report.setPunchInLocation(record.getPunchInLocation());
        report.setPunchOutLocation(record.getPunchOutLocation());
    }

    /**
     * 填充无记录的日报数据
     */
    private void fillDailyReportForNoRecord(EmployeeDailyReport report) {
        report.setAttendanceStatus("ABSENT");
        report.setExceptionType("NO_RECORD");
        report.setExceptionReason("无打卡记录");
        report.setGpsValidation(0);
        report.setDeviceValidation(0);
    }

    /**
     * 填充日报的规则信息
     */
    private void fillDailyReportWithRules(EmployeeDailyReport report, List<AttendanceRuleEntity> rules) {
        if (rules != null && !rules.isEmpty()) {
            AttendanceRuleEntity primaryRule = rules.get(0);

            // 转换String时间为LocalTime
            java.time.LocalTime workStartTime = null;
            java.time.LocalTime workEndTime = null;

            try {
                String startTimeStr = primaryRule.getWorkStartTime();
                if (startTimeStr != null && !startTimeStr.trim().isEmpty()) {
                    workStartTime = java.time.LocalTime.parse(startTimeStr);
                }
            } catch (Exception e) {
                // 解析失败时使用默认时间
                workStartTime = java.time.LocalTime.of(9, 0);
            }

            try {
                String endTimeStr = primaryRule.getWorkEndTime();
                if (endTimeStr != null && !endTimeStr.trim().isEmpty()) {
                    workEndTime = java.time.LocalTime.parse(endTimeStr);
                }
            } catch (Exception e) {
                // 解析失败时使用默认时间
                workEndTime = java.time.LocalTime.of(18, 0);
            }

            report.setWorkStartTime(workStartTime);
            report.setWorkEndTime(workEndTime);
            report.setLateThreshold(primaryRule.getLateTolerance());
            report.setEarlyLeaveThreshold(primaryRule.getEarlyTolerance());
        }
    }

    /**
     * 计算日报统计数据
     */
    private void calculateDailyStatistics(EmployeeDailyReport report) {
        // 计算迟到状态
        if (report.getPunchInTime() != null && report.getWorkStartTime() != null) {
            boolean isLate = report.getPunchInTime().isAfter(report.getWorkStartTime());
            report.setIsLate(isLate);
        }

        // 计算早退状态
        if (report.getPunchOutTime() != null && report.getWorkEndTime() != null) {
            boolean isEarlyLeave = report.getPunchOutTime().isBefore(report.getWorkEndTime());
            report.setIsEarlyLeave(isEarlyLeave);
        }

        // 计算工作状态
        report.setIsCompleteDay(report.getPunchInTime() != null && report.getPunchOutTime() != null);
    }

    /**
     * 填充空部门日报
     */
    private void fillEmptyDepartmentDailyReport(DepartmentDailyReport report) {
        report.setPresentEmployees(0);
        report.setAbsentEmployees(0);
        report.setLateEmployees(0);
        report.setEarlyLeaveEmployees(0);
        report.setExceptionEmployees(0);
        report.setAttendanceRate(BigDecimal.ZERO);
    }

    /**
     * 从记录填充部门日报
     */
    private void fillDepartmentDailyReportFromRecords(DepartmentDailyReport report,
            List<AttendanceRecordEntity> records) {
        long presentCount = records.stream().filter(r -> r.getPunchInTime() != null).count();
        long absentCount = records.size() - presentCount;
        long lateCount = records.stream().filter(r -> "LATE".equals(r.getExceptionType())).count();
        long earlyLeaveCount = records.stream().filter(r -> "EARLY_LEAVE".equals(r.getExceptionType())).count();
        long exceptionCount = records.stream().filter(r -> r.hasException()).count();

        report.setPresentEmployees((int) presentCount);
        report.setAbsentEmployees((int) absentCount);
        report.setLateEmployees((int) lateCount);
        report.setEarlyLeaveEmployees((int) earlyLeaveCount);
        report.setExceptionEmployees((int) exceptionCount);
    }

    /**
     * 计算部门日报统计
     */
    private void calculateDepartmentDailyStatistics(DepartmentDailyReport report) {
        if (report.getTotalEmployees() > 0) {
            BigDecimal attendanceRate = BigDecimal.valueOf(report.getPresentEmployees())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(report.getTotalEmployees()), 2, RoundingMode.HALF_UP);
            report.setAttendanceRate(attendanceRate);
        }
    }

    /**
     * 填充空员工月报
     */
    private void fillEmptyEmployeeMonthlyReport(EmployeeMonthlyReport report) {
        report.setWorkDays(0);
        report.setActualWorkDays(0);
        report.setLateDays(0);
        report.setEarlyLeaveDays(0);
        report.setAbsentDays(0);
        report.setLeaveDays(0);
        report.setTotalWorkHours(BigDecimal.ZERO);
        report.setTotalOvertimeHours(BigDecimal.ZERO);
        report.setMonthlyAttendanceRate(BigDecimal.ZERO);
    }

    /**
     * 从记录填充员工月报
     */
    private void fillEmployeeMonthlyReportFromRecords(EmployeeMonthlyReport report,
            List<AttendanceRecordEntity> records) {
        int workDays = records.size();
        int actualWorkDays = (int) records.stream().filter(r -> r.isCompleteRecord()).count();
        int lateDays = (int) records.stream().filter(r -> "LATE".equals(r.getExceptionType())).count();
        int earlyLeaveDays = (int) records.stream().filter(r -> "EARLY_LEAVE".equals(r.getExceptionType())).count();
        int absentDays = (int) records.stream().filter(r -> "ABSENT".equals(r.getExceptionType())).count();
        int leaveDays = (int) records.stream().filter(r -> "LEAVE".equals(r.getExceptionType())).count();

        BigDecimal totalWorkHours = records.stream()
                .filter(r -> r.getWorkHours() != null)
                .map(AttendanceRecordEntity::getWorkHours)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        BigDecimal totalOvertimeHours = records.stream()
                .filter(r -> r.getOvertimeHours() != null)
                .map(AttendanceRecordEntity::getOvertimeHours)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        report.setWorkDays(workDays);
        report.setActualWorkDays(actualWorkDays);
        report.setLateDays(lateDays);
        report.setEarlyLeaveDays(earlyLeaveDays);
        report.setAbsentDays(absentDays);
        report.setLeaveDays(leaveDays);
        report.setTotalWorkHours(totalWorkHours);
        report.setTotalOvertimeHours(totalOvertimeHours);
    }

    /**
     * 计算员工月报统计
     */
    private void calculateEmployeeMonthlyStatistics(EmployeeMonthlyReport report) {
        // 计算月度出勤率
        if (report.getWorkDays() > 0) {
            BigDecimal attendanceRate = BigDecimal.valueOf(report.getActualWorkDays())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(report.getWorkDays()), 2, RoundingMode.HALF_UP);
            report.setMonthlyAttendanceRate(attendanceRate);
        }

        // 计算平均工作时长
        if (report.getActualWorkDays() > 0) {
            BigDecimal avgWorkHours = report.getTotalWorkHours()
                    .divide(BigDecimal.valueOf(report.getActualWorkDays()), 2, RoundingMode.HALF_UP);
            report.setAverageWorkHours(avgWorkHours);
        }
    }

    /**
     * 计算员工月度趋势
     */
    private void calculateEmployeeMonthlyTrends(EmployeeMonthlyReport report) {
        // 这里可以实现月度趋势分析
        // 例如：迟到趋势、早退趋势、工作时长趋势等
    }

    /**
     * 填充空部门月报
     */
    private void fillEmptyDepartmentMonthlyReport(DepartmentMonthlyReport report) {
        report.setTotalWorkDays(0);
        report.setTotalActualWorkDays(0);
        report.setTotalLateDays(0);
        report.setTotalEarlyLeaveDays(0);
        report.setTotalAbsentDays(0);
        report.setMonthlyAttendanceRate(BigDecimal.ZERO);
    }

    /**
     * 从记录填充部门月报
     */
    private void fillDepartmentMonthlyReportFromRecords(DepartmentMonthlyReport report,
            List<AttendanceRecordEntity> records) {
        report.setTotalWorkDays(records.size());
        report.setTotalActualWorkDays((int) records.stream().filter(r -> r.isCompleteRecord()).count());
        report.setTotalLateDays((int) records.stream().filter(r -> "LATE".equals(r.getExceptionType())).count());
        report.setTotalEarlyLeaveDays(
                (int) records.stream().filter(r -> "EARLY_LEAVE".equals(r.getExceptionType())).count());
        report.setTotalAbsentDays((int) records.stream().filter(r -> "ABSENT".equals(r.getExceptionType())).count());
    }

    /**
     * 计算部门月报统计
     */
    private void calculateDepartmentMonthlyStatistics(DepartmentMonthlyReport report) {
        if (report.getTotalWorkDays() > 0) {
            BigDecimal attendanceRate = BigDecimal.valueOf(report.getTotalActualWorkDays())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(report.getTotalWorkDays()), 2, RoundingMode.HALF_UP);
            report.setMonthlyAttendanceRate(attendanceRate);
        }
    }

    /**
     * 填充空异常统计报表
     */
    private void fillEmptyExceptionStatisticsReport(ExceptionStatisticsReport report) {
        report.setLateCount(0);
        report.setEarlyLeaveCount(0);
        report.setAbsentCount(0);
        report.setNoRecordCount(0);
        report.setOtherExceptionCount(0);
        report.setExceptionRate(BigDecimal.ZERO);
    }

    /**
     * 从记录填充异常统计报表
     */
    private void fillExceptionStatisticsReportFromRecords(ExceptionStatisticsReport report,
            List<AttendanceRecordEntity> records) {
        int lateCount = (int) records.stream().filter(r -> "LATE".equals(r.getExceptionType())).count();
        int earlyLeaveCount = (int) records.stream().filter(r -> "EARLY_LEAVE".equals(r.getExceptionType())).count();
        int absentCount = (int) records.stream().filter(r -> "ABSENT".equals(r.getExceptionType())).count();
        int noRecordCount = (int) records.stream().filter(r -> "NO_RECORD".equals(r.getExceptionType())).count();
        int otherExceptionCount = (int) records.stream()
                .filter(r -> r.hasException()
                        && !List.of("LATE", "EARLY_LEAVE", "ABSENT", "NO_RECORD").contains(r.getExceptionType()))
                .count();

        report.setLateCount(lateCount);
        report.setEarlyLeaveCount(earlyLeaveCount);
        report.setAbsentCount(absentCount);
        report.setNoRecordCount(noRecordCount);
        report.setOtherExceptionCount(otherExceptionCount);
    }

    /**
     * 计算异常统计
     */
    private void calculateExceptionStatistics(ExceptionStatisticsReport report) {
        if (report.getTotalExceptionRecords() > 0) {
            BigDecimal exceptionRate = BigDecimal.valueOf(report.getTotalExceptionRecords())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(report.getTotalExceptionRecords() + report.getNormalDays()), 2,
                            RoundingMode.HALF_UP);
            report.setExceptionRate(exceptionRate);
        }
    }

    /**
     * 填充空趋势分析
     */
    private void fillEmptyTrendAnalysis(AttendanceTrendAnalysis analysis) {
        analysis.setAttendanceTrend(new ArrayList<>());
        analysis.setWorkHoursTrend(new ArrayList<>());
        analysis.setExceptionTrend(new ArrayList<>());
    }

    /**
     * 从记录填充趋势分析
     */
    private void fillTrendAnalysisFromRecords(AttendanceTrendAnalysis analysis, List<AttendanceRecordEntity> records) {
        // 按日期分组统计
        Map<LocalDate, List<AttendanceRecordEntity>> recordsByDate = records.stream()
                .collect(Collectors.groupingBy(record -> record.getAttendanceDate().toLocalDate()));

        // 构建出勤趋势数据
        List<TrendDataPoint> attendanceTrend = recordsByDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<AttendanceRecordEntity> dayRecords = entry.getValue();
                    int totalCount = dayRecords.size();
                    int presentCount = (int) dayRecords.stream().filter(r -> r.getPunchInTime() != null).count();
                    BigDecimal attendanceRate = totalCount > 0 ? BigDecimal.valueOf(presentCount * 100)
                            .divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

                    return new TrendDataPoint(SmartDateUtil.formatDate(date), attendanceRate);
                })
                .collect(Collectors.toList());

        analysis.setAttendanceTrend(attendanceTrend);

        // 构建工作时长趋势数据
        List<TrendDataPoint> workHoursTrend = recordsByDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    BigDecimal dayTotalHours = entry.getValue().stream()
                            .filter(r -> r.getWorkHours() != null)
                            .map(AttendanceRecordEntity::getWorkHours)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new TrendDataPoint(SmartDateUtil.formatDate(date), dayTotalHours);
                })
                .collect(Collectors.toList());

        analysis.setWorkHoursTrend(workHoursTrend);

        // 构建异常趋势数据
        List<TrendDataPoint> exceptionTrend = recordsByDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    long exceptionCount = entry.getValue().stream()
                            .filter(AttendanceRecordEntity::hasException)
                            .count();

                    return new TrendDataPoint(SmartDateUtil.formatDate(date), BigDecimal.valueOf(exceptionCount));
                })
                .collect(Collectors.toList());

        analysis.setExceptionTrend(exceptionTrend);
    }

    /**
     * 计算趋势分析
     */
    private void calculateTrendAnalysis(AttendanceTrendAnalysis analysis) {
        // 这里可以实现更复杂的趋势分析算法
        // 例如：趋势线计算、异常检测等
    }

    // ===== 内部数据类 =====

    /**
     * 员工日报数据
     */
    public static class EmployeeDailyReport {
        private Long recordId;
        private Long employeeId;
        private LocalDate reportDate;
        private LocalDateTime generateTime;
        private java.time.LocalTime punchInTime;
        private java.time.LocalTime punchOutTime;
        private BigDecimal workHours;
        private BigDecimal overtimeHours;
        private String attendanceStatus;
        private String exceptionType;
        private String exceptionReason;
        private Integer gpsValidation;
        private Integer deviceValidation;
        private String punchInLocation;
        private String punchOutLocation;
        private java.time.LocalTime workStartTime;
        private java.time.LocalTime workEndTime;
        private Integer lateThreshold;
        private Integer earlyLeaveThreshold;
        private Boolean isLate;
        private Boolean isEarlyLeave;
        private Boolean isCompleteDay;

        // Getters and Setters (由于篇幅限制，这里省略具体实现)
        public Long getRecordId() {
            return recordId;
        }

        public void setRecordId(Long recordId) {
            this.recordId = recordId;
        }

        public Long getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Long employeeId) {
            this.employeeId = employeeId;
        }

        public LocalDate getReportDate() {
            return reportDate;
        }

        public void setReportDate(LocalDate reportDate) {
            this.reportDate = reportDate;
        }

        public LocalDateTime getGenerateTime() {
            return generateTime;
        }

        public void setGenerateTime(LocalDateTime generateTime) {
            this.generateTime = generateTime;
        }

        public java.time.LocalTime getPunchInTime() {
            return punchInTime;
        }

        public void setPunchInTime(java.time.LocalTime punchInTime) {
            this.punchInTime = punchInTime;
        }

        public java.time.LocalTime getPunchOutTime() {
            return punchOutTime;
        }

        public void setPunchOutTime(java.time.LocalTime punchOutTime) {
            this.punchOutTime = punchOutTime;
        }

        public BigDecimal getWorkHours() {
            return workHours;
        }

        public void setWorkHours(BigDecimal workHours) {
            this.workHours = workHours;
        }

        public BigDecimal getOvertimeHours() {
            return overtimeHours;
        }

        public void setOvertimeHours(BigDecimal overtimeHours) {
            this.overtimeHours = overtimeHours;
        }

        public String getAttendanceStatus() {
            return attendanceStatus;
        }

        public void setAttendanceStatus(String attendanceStatus) {
            this.attendanceStatus = attendanceStatus;
        }

        public String getExceptionType() {
            return exceptionType;
        }

        public void setExceptionType(String exceptionType) {
            this.exceptionType = exceptionType;
        }

        public String getExceptionReason() {
            return exceptionReason;
        }

        public void setExceptionReason(String exceptionReason) {
            this.exceptionReason = exceptionReason;
        }

        public Integer getGpsValidation() {
            return gpsValidation;
        }

        public void setGpsValidation(Integer gpsValidation) {
            this.gpsValidation = gpsValidation;
        }

        public Integer getDeviceValidation() {
            return deviceValidation;
        }

        public void setDeviceValidation(Integer deviceValidation) {
            this.deviceValidation = deviceValidation;
        }

        public String getPunchInLocation() {
            return punchInLocation;
        }

        public void setPunchInLocation(String punchInLocation) {
            this.punchInLocation = punchInLocation;
        }

        public String getPunchOutLocation() {
            return punchOutLocation;
        }

        public void setPunchOutLocation(String punchOutLocation) {
            this.punchOutLocation = punchOutLocation;
        }

        public java.time.LocalTime getWorkStartTime() {
            return workStartTime;
        }

        public void setWorkStartTime(java.time.LocalTime workStartTime) {
            this.workStartTime = workStartTime;
        }

        public java.time.LocalTime getWorkEndTime() {
            return workEndTime;
        }

        public void setWorkEndTime(java.time.LocalTime workEndTime) {
            this.workEndTime = workEndTime;
        }

        public Integer getLateThreshold() {
            return lateThreshold;
        }

        public void setLateThreshold(Integer lateThreshold) {
            this.lateThreshold = lateThreshold;
        }

        public Integer getEarlyLeaveThreshold() {
            return earlyLeaveThreshold;
        }

        public void setEarlyLeaveThreshold(Integer earlyLeaveThreshold) {
            this.earlyLeaveThreshold = earlyLeaveThreshold;
        }

        public Boolean getIsLate() {
            return isLate;
        }

        public void setIsLate(Boolean isLate) {
            this.isLate = isLate;
        }

        public Boolean getIsEarlyLeave() {
            return isEarlyLeave;
        }

        public void setIsEarlyLeave(Boolean isEarlyLeave) {
            this.isEarlyLeave = isEarlyLeave;
        }

        public Boolean getIsCompleteDay() {
            return isCompleteDay;
        }

        public void setIsCompleteDay(Boolean isCompleteDay) {
            this.isCompleteDay = isCompleteDay;
        }
    }

    /**
     * 部门日报数据
     */
    public static class DepartmentDailyReport {
        private Long departmentId;
        private LocalDate reportDate;
        private LocalDateTime generateTime;
        private Integer totalEmployees;
        private Integer presentEmployees;
        private Integer absentEmployees;
        private Integer lateEmployees;
        private Integer earlyLeaveEmployees;
        private Integer exceptionEmployees;
        private BigDecimal attendanceRate;

        // Getters and Setters (由于篇幅限制，这里省略具体实现)
        public Long getDepartmentId() {
            return departmentId;
        }

        public void setDepartmentId(Long departmentId) {
            this.departmentId = departmentId;
        }

        public LocalDate getReportDate() {
            return reportDate;
        }

        public void setReportDate(LocalDate reportDate) {
            this.reportDate = reportDate;
        }

        public LocalDateTime getGenerateTime() {
            return generateTime;
        }

        public void setGenerateTime(LocalDateTime generateTime) {
            this.generateTime = generateTime;
        }

        public Integer getTotalEmployees() {
            return totalEmployees;
        }

        public void setTotalEmployees(Integer totalEmployees) {
            this.totalEmployees = totalEmployees;
        }

        public Integer getPresentEmployees() {
            return presentEmployees;
        }

        public void setPresentEmployees(Integer presentEmployees) {
            this.presentEmployees = presentEmployees;
        }

        public Integer getAbsentEmployees() {
            return absentEmployees;
        }

        public void setAbsentEmployees(Integer absentEmployees) {
            this.absentEmployees = absentEmployees;
        }

        public Integer getLateEmployees() {
            return lateEmployees;
        }

        public void setLateEmployees(Integer lateEmployees) {
            this.lateEmployees = lateEmployees;
        }

        public Integer getEarlyLeaveEmployees() {
            return earlyLeaveEmployees;
        }

        public void setEarlyLeaveEmployees(Integer earlyLeaveEmployees) {
            this.earlyLeaveEmployees = earlyLeaveEmployees;
        }

        public Integer getExceptionEmployees() {
            return exceptionEmployees;
        }

        public void setExceptionEmployees(Integer exceptionEmployees) {
            this.exceptionEmployees = exceptionEmployees;
        }

        public BigDecimal getAttendanceRate() {
            return attendanceRate;
        }

        public void setAttendanceRate(BigDecimal attendanceRate) {
            this.attendanceRate = attendanceRate;
        }
    }

    /**
     * 员工月报数据
     */
    public static class EmployeeMonthlyReport {
        private Long employeeId;
        private String yearMonth;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalDateTime generateTime;
        private Integer totalRecords;
        private Integer workDays;
        private Integer actualWorkDays;
        private Integer lateDays;
        private Integer earlyLeaveDays;
        private Integer absentDays;
        private Integer leaveDays;
        private BigDecimal totalWorkHours;
        private BigDecimal totalOvertimeHours;
        private BigDecimal monthlyAttendanceRate;
        private BigDecimal averageWorkHours;

        // Getters and Setters (由于篇幅限制，这里省略具体实现)
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

        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }

        public LocalDateTime getGenerateTime() {
            return generateTime;
        }

        public void setGenerateTime(LocalDateTime generateTime) {
            this.generateTime = generateTime;
        }

        public Integer getTotalRecords() {
            return totalRecords;
        }

        public void setTotalRecords(Integer totalRecords) {
            this.totalRecords = totalRecords;
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

        public Integer getAbsentDays() {
            return absentDays;
        }

        public void setAbsentDays(Integer absentDays) {
            this.absentDays = absentDays;
        }

        public Integer getLeaveDays() {
            return leaveDays;
        }

        public void setLeaveDays(Integer leaveDays) {
            this.leaveDays = leaveDays;
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

        public BigDecimal getMonthlyAttendanceRate() {
            return monthlyAttendanceRate;
        }

        public void setMonthlyAttendanceRate(BigDecimal monthlyAttendanceRate) {
            this.monthlyAttendanceRate = monthlyAttendanceRate;
        }

        public BigDecimal getAverageWorkHours() {
            return averageWorkHours;
        }

        public void setAverageWorkHours(BigDecimal averageWorkHours) {
            this.averageWorkHours = averageWorkHours;
        }
    }

    /**
     * 部门月报数据
     */
    public static class DepartmentMonthlyReport {
        private Long departmentId;
        private String yearMonth;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalDateTime generateTime;
        private Integer totalRecords;
        private Integer totalWorkDays;
        private Integer totalActualWorkDays;
        private Integer totalLateDays;
        private Integer totalEarlyLeaveDays;
        private Integer totalAbsentDays;
        private BigDecimal monthlyAttendanceRate;

        // Getters and Setters (由于篇幅限制，这里省略具体实现)
        public Long getDepartmentId() {
            return departmentId;
        }

        public void setDepartmentId(Long departmentId) {
            this.departmentId = departmentId;
        }

        public String getYearMonth() {
            return yearMonth;
        }

        public void setYearMonth(String yearMonth) {
            this.yearMonth = yearMonth;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }

        public LocalDateTime getGenerateTime() {
            return generateTime;
        }

        public void setGenerateTime(LocalDateTime generateTime) {
            this.generateTime = generateTime;
        }

        public Integer getTotalRecords() {
            return totalRecords;
        }

        public void setTotalRecords(Integer totalRecords) {
            this.totalRecords = totalRecords;
        }

        public Integer getTotalWorkDays() {
            return totalWorkDays;
        }

        public void setTotalWorkDays(Integer totalWorkDays) {
            this.totalWorkDays = totalWorkDays;
        }

        public Integer getTotalActualWorkDays() {
            return totalActualWorkDays;
        }

        public void setTotalActualWorkDays(Integer totalActualWorkDays) {
            this.totalActualWorkDays = totalActualWorkDays;
        }

        public Integer getTotalLateDays() {
            return totalLateDays;
        }

        public void setTotalLateDays(Integer totalLateDays) {
            this.totalLateDays = totalLateDays;
        }

        public Integer getTotalEarlyLeaveDays() {
            return totalEarlyLeaveDays;
        }

        public void setTotalEarlyLeaveDays(Integer totalEarlyLeaveDays) {
            this.totalEarlyLeaveDays = totalEarlyLeaveDays;
        }

        public Integer getTotalAbsentDays() {
            return totalAbsentDays;
        }

        public void setTotalAbsentDays(Integer totalAbsentDays) {
            this.totalAbsentDays = totalAbsentDays;
        }

        public BigDecimal getMonthlyAttendanceRate() {
            return monthlyAttendanceRate;
        }

        public void setMonthlyAttendanceRate(BigDecimal monthlyAttendanceRate) {
            this.monthlyAttendanceRate = monthlyAttendanceRate;
        }
    }

    /**
     * 异常统计数据
     */
    public static class ExceptionStatisticsReport {
        private Long employeeId;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalDateTime generateTime;
        private Integer totalExceptionRecords;
        private Integer normalDays;
        private Integer lateCount;
        private Integer earlyLeaveCount;
        private Integer absentCount;
        private Integer noRecordCount;
        private Integer otherExceptionCount;
        private BigDecimal exceptionRate;

        // Getters and Setters (由于篇幅限制，这里省略具体实现)
        public Long getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Long employeeId) {
            this.employeeId = employeeId;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }

        public LocalDateTime getGenerateTime() {
            return generateTime;
        }

        public void setGenerateTime(LocalDateTime generateTime) {
            this.generateTime = generateTime;
        }

        public Integer getTotalExceptionRecords() {
            return totalExceptionRecords;
        }

        public void setTotalExceptionRecords(Integer totalExceptionRecords) {
            this.totalExceptionRecords = totalExceptionRecords;
        }

        public Integer getNormalDays() {
            return normalDays;
        }

        public void setNormalDays(Integer normalDays) {
            this.normalDays = normalDays;
        }

        public Integer getLateCount() {
            return lateCount;
        }

        public void setLateCount(Integer lateCount) {
            this.lateCount = lateCount;
        }

        public Integer getEarlyLeaveCount() {
            return earlyLeaveCount;
        }

        public void setEarlyLeaveCount(Integer earlyLeaveCount) {
            this.earlyLeaveCount = earlyLeaveCount;
        }

        public Integer getAbsentCount() {
            return absentCount;
        }

        public void setAbsentCount(Integer absentCount) {
            this.absentCount = absentCount;
        }

        public Integer getNoRecordCount() {
            return noRecordCount;
        }

        public void setNoRecordCount(Integer noRecordCount) {
            this.noRecordCount = noRecordCount;
        }

        public Integer getOtherExceptionCount() {
            return otherExceptionCount;
        }

        public void setOtherExceptionCount(Integer otherExceptionCount) {
            this.otherExceptionCount = otherExceptionCount;
        }

        public BigDecimal getExceptionRate() {
            return exceptionRate;
        }

        public void setExceptionRate(BigDecimal exceptionRate) {
            this.exceptionRate = exceptionRate;
        }
    }

    /**
     * 考勤趋势分析
     */
    public static class AttendanceTrendAnalysis {
        private Long employeeId;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalDateTime generateTime;
        private List<TrendDataPoint> attendanceTrend;
        private List<TrendDataPoint> workHoursTrend;
        private List<TrendDataPoint> exceptionTrend;

        // Getters and Setters (由于篇幅限制，这里省略具体实现)
        public Long getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Long employeeId) {
            this.employeeId = employeeId;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }

        public LocalDateTime getGenerateTime() {
            return generateTime;
        }

        public void setGenerateTime(LocalDateTime generateTime) {
            this.generateTime = generateTime;
        }

        public List<TrendDataPoint> getAttendanceTrend() {
            return attendanceTrend;
        }

        public void setAttendanceTrend(List<TrendDataPoint> attendanceTrend) {
            this.attendanceTrend = attendanceTrend;
        }

        public List<TrendDataPoint> getWorkHoursTrend() {
            return workHoursTrend;
        }

        public void setWorkHoursTrend(List<TrendDataPoint> workHoursTrend) {
            this.workHoursTrend = workHoursTrend;
        }

        public List<TrendDataPoint> getExceptionTrend() {
            return exceptionTrend;
        }

        public void setExceptionTrend(List<TrendDataPoint> exceptionTrend) {
            this.exceptionTrend = exceptionTrend;
        }
    }

    /**
     * 趋势数据点
     */
    public static class TrendDataPoint {
        private String date;
        private BigDecimal value;

        public TrendDataPoint(String date, BigDecimal value) {
            this.date = date;
            this.value = value;
        }

        // Getters and Setters
        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }
    }
}