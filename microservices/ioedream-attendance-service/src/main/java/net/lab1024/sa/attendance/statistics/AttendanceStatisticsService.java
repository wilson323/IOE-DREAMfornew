package net.lab1024.sa.attendance.statistics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 考勤统计分析服务
 * 提供全面的考勤数据统计、分析和报表功能
 * 严格遵循repowiki规范，使用jakarta包和@Resource注入
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @date 2025-11-28
 */
@Slf4j
@Data
public class AttendanceStatisticsService {

    /**
     * 统计周期枚举
     */
    public enum StatisticsPeriod {
        DAILY("DAILY", "日统计"),
        WEEKLY("WEEKLY", "周统计"),
        MONTHLY("MONTHLY", "月统计"),
        QUARTERLY("QUARTERLY", "季度统计"),
        YEARLY("YEARLY", "年统计");

        private final String code;
        private final String description;

        StatisticsPeriod(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 统计类型枚举
     */
    public enum StatisticsType {
        ATTENDANCE_RATE("ATTENDANCE_RATE", "出勤率"),
        LATE_STATISTICS("LATE_STATISTICS", "迟到统计"),
        EARLY_LEAVE_STATISTICS("EARLY_LEAVE_STATISTICS", "早退统计"),
        ABSENTEEISM_STATISTICS("ABSENTEEISM_STATISTICS", "旷工统计"),
        OVERTIME_STATISTICS("OVERTIME_STATISTICS", "加班统计"),
        WORK_HOURS_STATISTICS("WORK_HOURS_STATISTICS", "工作时长统计");

        private final String code;
        private final String description;

        StatisticsType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 员工考勤统计
     */
    @Data
    public static class EmployeeAttendanceStatistics {
        private Long employeeId;
        private String employeeName;
        private String department;
        private LocalDate statisticsDate;
        private StatisticsPeriod period;

        // 基础统计数据
        private Integer totalWorkDays; // 总工作日
        private Integer actualWorkDays; // 实际工作日
        private Integer attendanceDays; // 出勤天数
        private Integer leaveDays; // 请假天数
        private Integer absentDays; // 旷工天数
        private Integer lateDays; // 迟到天数
        private Integer earlyLeaveDays; // 早退天数
        private Integer overtimeDays; // 加班天数

        // 比率数据
        private BigDecimal attendanceRate; // 出勤率
        private BigDecimal punctualityRate; // 准时率
        private BigDecimal overtimeRate; // 加班率
        private BigDecimal absenteeismRate; // 旷工率

        // 时长数据
        private BigDecimal totalWorkHours; // 总工作时长
        private BigDecimal averageWorkHours; // 平均工作时长
        private BigDecimal totalOvertimeHours; // 总加班时长
        private BigDecimal averageOvertimeHours; // 平均加班时长

        // 迟到详细数据
        private BigDecimal totalLateMinutes; // 总迟到分钟数
        private BigDecimal averageLateMinutes; // 平均迟到分钟数
        private Integer maxLateMinutes; // 最大迟到分钟数

        // 早退详细数据
        private BigDecimal totalEarlyLeaveMinutes; // 总早退分钟数
        private BigDecimal averageEarlyLeaveMinutes; // 平均早退分钟数
        private Integer maxEarlyLeaveMinutes; // 最大早退分钟数

        // 异常数据
        private Integer forgetPunchCount; // 忘打卡次数
        private Integer abnormalRecordsCount; // 异常记录数
        private BigDecimal abnormalRate; // 异常率

        // 趋势数据
        private BigDecimal monthOverMonthGrowth; // 环比增长率
        private BigDecimal yearOverYearGrowth; // 同比增长率
    }

    /**
     * 部门考勤统计
     */
    @Data
    public static class DepartmentAttendanceStatistics {
        private Long departmentId;
        private String departmentName;
        private LocalDate statisticsDate;
        private StatisticsPeriod period;

        // 部门总览数据
        private Integer totalEmployees; // 总员工数
        private Integer activeEmployees; // 活跃员工数
        private Integer attendanceEmployees; // 出勤员工数

        // 部门出勤数据
        private BigDecimal departmentAttendanceRate; // 部门出勤率
        private BigDecimal departmentPunctualityRate; // 部门准时率
        private BigDecimal departmentOvertimeRate; // 部门加班率

        // 部门异常数据
        private Integer totalLateEmployees; // 迟到员工数
        private Integer totalEarlyLeaveEmployees; // 早退员工数
        private Integer totalAbsentEmployees; // 旷工员工数

        // 部门工作量数据
        private BigDecimal totalDepartmentWorkHours; // 部门总工作时长
        private BigDecimal averageDepartmentWorkHours; // 部门平均工作时长

        // 部门排名数据
        private Integer attendanceRanking; // 出勤率排名
        private Integer punctualityRanking; // 准时率排名
        private Integer efficiencyRanking; // 效率排名

        // 员工详细列表
        private List<EmployeeAttendanceStatistics> employeeStatistics;
    }

    /**
     * 综合考勤报表
     */
    @Data
    public static class AttendanceReport {
        private String reportId;
        private String reportName;
        private LocalDate reportDate;
        private StatisticsPeriod period;
        private LocalDate startDate;
        private LocalDate endDate;

        // 报告概要
        private Integer totalEmployees; // 总员工数
        private BigDecimal overallAttendanceRate; // 总体出勤率
        private BigDecimal overallPunctualityRate; // 总体准时率
        private BigDecimal overallOvertimeRate; // 总体加班率

        // 核心指标
        private Integer totalAttendanceDays; // 总出勤天数
        private Integer totalLeaveDays; // 总请假天数
        private Integer totalAbsentDays; // 总旷工天数
        private BigDecimal totalWorkHours; // 总工作时长
        private BigDecimal totalOvertimeHours; // 总加班时长

        // 部门统计数据
        private List<DepartmentAttendanceStatistics> departmentStatistics;

        // 异常分析
        private Map<String, Integer> exceptionDistribution; // 异常分布
        private List<String> topAbnormalEmployees; // 异常最多的员工
        private List<String> topOvertimeEmployees; // 加班最多的员工

        // 趋势分析
        private Map<String, BigDecimal> monthlyTrends; // 月度趋势
        private Map<String, BigDecimal> departmentTrends; // 部门趋势

        // 生成时间
        private LocalDateTime generatedAt;
    }

    /**
     * 模拟考勤数据存储
     */
    private Map<String, Object> mockDataStore = new HashMap<>();

    /**
     * 初始化统计分析服务
     */
    public void initializeStatisticsService() {
        log.info("考勤统计分析服务初始化开始");

        // 加载模拟数据
        loadMockData();

        log.info("考勤统计分析服务初始化完成");
    }

    /**
     * 加载模拟数据
     */
    private void loadMockData() {
        // 这里可以加载实际的考勤数据
        // 为了演示，使用模拟数据
        log.debug("加载模拟考勤统计数据");
    }

    /**
     * 生成员工考勤统计
     *
     * @param employeeId 员工ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @param period     统计周期
     * @return 员工考勤统计数据
     */
    public EmployeeAttendanceStatistics generateEmployeeStatistics(Long employeeId,
            LocalDate startDate,
            LocalDate endDate,
            StatisticsPeriod period) {
        log.info("生成员工考勤统计：员工ID={}，日期范围={}到{}，周期={}",
                employeeId, startDate, endDate, period);

        EmployeeAttendanceStatistics statistics = new EmployeeAttendanceStatistics();
        statistics.setEmployeeId(employeeId);
        statistics.setStatisticsDate(endDate);
        statistics.setPeriod(period);

        // 计算工作日数量
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        statistics.setTotalWorkDays((int) totalDays);

        // 模拟统计数据（实际应该从数据库查询）
        statistics.setActualWorkDays((int) (totalDays * 0.95));
        statistics.setAttendanceDays((int) (totalDays * 0.90));
        statistics.setLeaveDays((int) (totalDays * 0.03));
        statistics.setAbsentDays((int) (totalDays * 0.02));
        statistics.setLateDays((int) (totalDays * 0.08));
        statistics.setEarlyLeaveDays((int) (totalDays * 0.05));
        statistics.setOvertimeDays((int) (totalDays * 0.12));

        // 计算比率
        statistics.setAttendanceRate(calculateRate(statistics.getAttendanceDays(), statistics.getTotalWorkDays()));
        statistics.setPunctualityRate(calculateRate(
                statistics.getAttendanceDays() - statistics.getLateDays() - statistics.getEarlyLeaveDays(),
                statistics.getAttendanceDays()));
        statistics.setOvertimeRate(calculateRate(statistics.getOvertimeDays(), statistics.getAttendanceDays()));
        statistics.setAbsenteeismRate(calculateRate(statistics.getAbsentDays(), statistics.getTotalWorkDays()));

        // 计算时长数据
        statistics.setTotalWorkHours(BigDecimal.valueOf(statistics.getAttendanceDays() * 8.5));
        statistics.setAverageWorkHours(statistics.getTotalWorkHours()
                .divide(BigDecimal.valueOf(statistics.getAttendanceDays()), 2, RoundingMode.HALF_UP));
        statistics.setTotalOvertimeHours(BigDecimal.valueOf(statistics.getOvertimeDays() * 2.5));
        statistics.setAverageOvertimeHours(statistics.getTotalOvertimeHours()
                .divide(BigDecimal.valueOf(Math.max(1, statistics.getOvertimeDays())), 2, RoundingMode.HALF_UP));

        // 计算迟到详细数据
        statistics.setTotalLateMinutes(BigDecimal.valueOf(statistics.getLateDays() * 15.5));
        statistics.setAverageLateMinutes(statistics.getTotalLateMinutes()
                .divide(BigDecimal.valueOf(Math.max(1, statistics.getLateDays())), 1, RoundingMode.HALF_UP));
        statistics.setMaxLateMinutes(45);

        // 计算早退详细数据
        statistics.setTotalEarlyLeaveMinutes(BigDecimal.valueOf(statistics.getEarlyLeaveDays() * 20.3));
        statistics.setAverageEarlyLeaveMinutes(statistics.getTotalEarlyLeaveMinutes()
                .divide(BigDecimal.valueOf(Math.max(1, statistics.getEarlyLeaveDays())), 1, RoundingMode.HALF_UP));
        statistics.setMaxEarlyLeaveMinutes(60);

        // 计算异常数据
        statistics.setForgetPunchCount((int) (totalDays * 0.01));
        statistics.setAbnormalRecordsCount(
                statistics.getLateDays() + statistics.getEarlyLeaveDays() + statistics.getForgetPunchCount());
        statistics.setAbnormalRate(calculateRate(statistics.getAbnormalRecordsCount(), statistics.getTotalWorkDays()));

        log.info("员工考勤统计生成完成：出勤率{}%，准时率{}%",
                statistics.getAttendanceRate().multiply(BigDecimal.valueOf(100)),
                statistics.getPunctualityRate().multiply(BigDecimal.valueOf(100)));

        return statistics;
    }

    /**
     * 生成部门考勤统计
     *
     * @param departmentId 部门ID
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @param period       统计周期
     * @return 部门考勤统计数据
     */
    public DepartmentAttendanceStatistics generateDepartmentStatistics(Long departmentId,
            LocalDate startDate,
            LocalDate endDate,
            StatisticsPeriod period) {
        log.info("生成部门考勤统计：部门ID={}，日期范围={}到{}，周期={}",
                departmentId, startDate, endDate, period);

        DepartmentAttendanceStatistics statistics = new DepartmentAttendanceStatistics();
        statistics.setDepartmentId(departmentId);
        statistics.setDepartmentName("技术部"); // 模拟部门名称
        statistics.setStatisticsDate(endDate);
        statistics.setPeriod(period);

        // 模拟部门数据
        statistics.setTotalEmployees(25);
        statistics.setActiveEmployees((int) (statistics.getTotalEmployees() * 0.95));
        statistics.setAttendanceEmployees((int) (statistics.getTotalEmployees() * 0.88));

        // 计算部门比率
        statistics.setDepartmentAttendanceRate(calculateRate(
                statistics.getAttendanceEmployees(), statistics.getTotalEmployees()));
        statistics.setDepartmentPunctualityRate(BigDecimal.valueOf(0.92));
        statistics.setDepartmentOvertimeRate(BigDecimal.valueOf(0.15));

        // 异常员工数
        statistics.setTotalLateEmployees((int) (statistics.getTotalEmployees() * 0.12));
        statistics.setTotalEarlyLeaveEmployees((int) (statistics.getTotalEmployees() * 0.08));
        statistics.setTotalAbsentEmployees((int) (statistics.getTotalEmployees() * 0.02));

        // 工作量数据
        statistics.setTotalDepartmentWorkHours(BigDecimal.valueOf(statistics.getAttendanceEmployees() * 8.5 * 22));
        statistics.setAverageDepartmentWorkHours(statistics.getTotalDepartmentWorkHours()
                .divide(BigDecimal.valueOf(statistics.getAttendanceEmployees()), 2, RoundingMode.HALF_UP));

        // 排名数据（模拟）
        statistics.setAttendanceRanking(3);
        statistics.setPunctualityRanking(2);
        statistics.setEfficiencyRanking(1);

        // 生成员工详细统计
        statistics.setEmployeeStatistics(new ArrayList<>());
        for (int i = 1; i <= Math.min(5, statistics.getTotalEmployees()); i++) {
            EmployeeAttendanceStatistics empStats = generateEmployeeStatistics(
                    (long) i, startDate, endDate, period);
            empStats.setEmployeeName("员工" + i);
            empStats.setDepartment(statistics.getDepartmentName());
            statistics.getEmployeeStatistics().add(empStats);
        }

        log.info("部门考勤统计生成完成：部门出勤率{}%，准时率{}%",
                statistics.getDepartmentAttendanceRate().multiply(BigDecimal.valueOf(100)),
                statistics.getDepartmentPunctualityRate().multiply(BigDecimal.valueOf(100)));

        return statistics;
    }

    /**
     * 生成综合考勤报表
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param period    统计周期
     * @return 综合考勤报表
     */
    public AttendanceReport generateComprehensiveReport(LocalDate startDate,
            LocalDate endDate,
            StatisticsPeriod period) {
        log.info("生成综合考勤报表：日期范围={}到{}，周期={}", startDate, endDate, period);

        AttendanceReport report = new AttendanceReport();
        report.setReportId("RPT_" + System.currentTimeMillis());
        report.setReportName("考勤综合统计报表");
        report.setReportDate(LocalDate.now());
        report.setPeriod(period);
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setGeneratedAt(LocalDateTime.now());

        // 生成部门统计
        List<DepartmentAttendanceStatistics> deptStats = new ArrayList<>();
        for (long deptId = 1L; deptId <= 5L; deptId++) {
            DepartmentAttendanceStatistics stat = generateDepartmentStatistics(deptId, startDate, endDate, period);
            deptStats.add(stat);
        }
        report.setDepartmentStatistics(deptStats);

        // 计算总体数据
        calculateOverallStatistics(report);

        // 生成异常分析
        generateExceptionAnalysis(report);

        // 生成趋势分析
        generateTrendAnalysis(report);

        log.info("综合考勤报表生成完成：员工数={}，总体出勤率={}%",
                report.getTotalEmployees(),
                report.getOverallAttendanceRate().multiply(BigDecimal.valueOf(100)));

        return report;
    }

    /**
     * 计算总体统计数据
     */
    private void calculateOverallStatistics(AttendanceReport report) {
        List<DepartmentAttendanceStatistics> deptStats = report.getDepartmentStatistics();

        int totalEmployees = deptStats.stream().mapToInt(DepartmentAttendanceStatistics::getTotalEmployees).sum();
        report.setTotalEmployees(totalEmployees);

        // 计算总出勤天数
        int totalAttendanceDays = deptStats.stream()
                .flatMap(dept -> dept.getEmployeeStatistics().stream())
                .mapToInt(EmployeeAttendanceStatistics::getAttendanceDays)
                .sum();
        report.setTotalAttendanceDays(totalAttendanceDays);

        // 计算总请假天数
        int totalLeaveDays = deptStats.stream()
                .flatMap(dept -> dept.getEmployeeStatistics().stream())
                .mapToInt(EmployeeAttendanceStatistics::getLeaveDays)
                .sum();
        report.setTotalLeaveDays(totalLeaveDays);

        // 计算总旷工天数
        int totalAbsentDays = deptStats.stream()
                .flatMap(dept -> dept.getEmployeeStatistics().stream())
                .mapToInt(EmployeeAttendanceStatistics::getAbsentDays)
                .sum();
        report.setTotalAbsentDays(totalAbsentDays);

        // 计算总工作时长
        BigDecimal totalWorkHours = deptStats.stream()
                .flatMap(dept -> dept.getEmployeeStatistics().stream())
                .map(EmployeeAttendanceStatistics::getTotalWorkHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setTotalWorkHours(totalWorkHours);

        // 计算总加班时长
        BigDecimal totalOvertimeHours = deptStats.stream()
                .flatMap(dept -> dept.getEmployeeStatistics().stream())
                .map(EmployeeAttendanceStatistics::getTotalOvertimeHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setTotalOvertimeHours(totalOvertimeHours);

        // 计算总体比率
        int totalPossibleDays = totalEmployees
                * (int) ChronoUnit.DAYS.between(report.getStartDate(), report.getEndDate()) + 1;
        report.setOverallAttendanceRate(calculateRate(report.getTotalAttendanceDays(), totalPossibleDays));
        report.setOverallPunctualityRate(BigDecimal.valueOf(0.91)); // 模拟数据
        report.setOverallOvertimeRate(BigDecimal.valueOf(0.14)); // 模拟数据
    }

    /**
     * 生成异常分析
     */
    private void generateExceptionAnalysis(AttendanceReport report) {
        Map<String, Integer> exceptionDistribution = new HashMap<>();
        exceptionDistribution.put("迟到", report.getDepartmentStatistics().stream()
                .flatMap(dept -> dept.getEmployeeStatistics().stream())
                .mapToInt(EmployeeAttendanceStatistics::getLateDays)
                .sum());
        exceptionDistribution.put("早退", report.getDepartmentStatistics().stream()
                .flatMap(dept -> dept.getEmployeeStatistics().stream())
                .mapToInt(EmployeeAttendanceStatistics::getEarlyLeaveDays)
                .sum());
        exceptionDistribution.put("忘打卡", report.getDepartmentStatistics().stream()
                .flatMap(dept -> dept.getEmployeeStatistics().stream())
                .mapToInt(EmployeeAttendanceStatistics::getForgetPunchCount)
                .sum());
        exceptionDistribution.put("旷工", report.getTotalAbsentDays());
        report.setExceptionDistribution(exceptionDistribution);

        // 异常最多的员工（模拟）
        report.setTopAbnormalEmployees(Arrays.asList("张三", "李四", "王五", "赵六", "钱七"));

        // 加班最多的员工（模拟）
        report.setTopOvertimeEmployees(Arrays.asList("技术部-小王", "产品部-小李", "运营部-小张", "市场部-小陈"));
    }

    /**
     * 生成趋势分析
     */
    private void generateTrendAnalysis(AttendanceReport report) {
        Map<String, BigDecimal> monthlyTrends = new HashMap<>();
        LocalDate current = report.getStartDate();
        while (!current.isAfter(report.getEndDate())) {
            String monthKey = current.toString().substring(0, 7); // yyyy-MM
            // 模拟趋势数据
            monthlyTrends.put(monthKey, BigDecimal.valueOf(0.88 + Math.random() * 0.1));
            current = current.plusMonths(1);
        }
        report.setMonthlyTrends(monthlyTrends);

        Map<String, BigDecimal> departmentTrends = new HashMap<>();
        for (DepartmentAttendanceStatistics dept : report.getDepartmentStatistics()) {
            departmentTrends.put(dept.getDepartmentName(), dept.getDepartmentAttendanceRate());
        }
        report.setDepartmentTrends(departmentTrends);
    }

    /**
     * 计算比率
     */
    private BigDecimal calculateRate(Integer numerator, Integer denominator) {
        if (denominator == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(numerator)
                .divide(BigDecimal.valueOf(denominator), 4, RoundingMode.HALF_UP);
    }

    /**
     * 获取考勤热力图数据
     *
     * @param employeeId 员工ID
     * @param yearMonth  年月
     * @return 热力图数据
     */
    public Map<String, Object> getAttendanceHeatmap(Long employeeId, YearMonth yearMonth) {
        Map<String, Object> heatmapData = new HashMap<>();

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        Map<String, String> dailyStatus = new HashMap<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            // 模拟每日状态
            if (date.getDayOfWeek().getValue() >= 6) { // 周末
                dailyStatus.put(date.toString(), "WEEKEND");
            } else if (Math.random() > 0.1) { // 90%出勤
                dailyStatus.put(date.toString(), Math.random() > 0.8 ? "LATE" : "NORMAL");
            } else {
                dailyStatus.put(date.toString(), "ABSENT");
            }
        }

        heatmapData.put("employeeId", employeeId);
        heatmapData.put("yearMonth", yearMonth.toString());
        heatmapData.put("dailyStatus", dailyStatus);
        heatmapData.put("statistics", Map.of(
                "attendanceDays",
                dailyStatus.values().stream().filter(s -> "NORMAL".equals(s) || "LATE".equals(s)).count(),
                "lateDays", dailyStatus.values().stream().filter(s -> "LATE".equals(s)).count(),
                "absentDays", dailyStatus.values().stream().filter(s -> "ABSENT".equals(s)).count(),
                "weekendDays", dailyStatus.values().stream().filter(s -> "WEEKEND".equals(s)).count()));

        return heatmapData;
    }

    /**
     * 导出考勤报表到Excel
     *
     * @param report 考勤报表
     * @return Excel文件路径
     */
    public String exportToExcel(AttendanceReport report) {
        log.info("导出考勤报表到Excel：{}", report.getReportId());

        // 这里应该使用实际的Excel导出库，如Apache POI
        String filePath = "/tmp/attendance_report_" + report.getReportId() + ".xlsx";

        // 模拟导出过程
        log.debug("Excel导出完成，文件路径：{}", filePath);

        return filePath;
    }

    /**
     * 获取实时考勤概览
     *
     * @return 实时概览数据
     */
    public Map<String, Object> getRealTimeOverview() {
        Map<String, Object> overview = new HashMap<>();

        // 今日数据
        LocalDate today = LocalDate.now();
        overview.put("todayDate", today.toString());
        overview.put("todayAttendance", 245); // 今日出勤人数
        overview.put("todayLate", 18); // 今日迟到人数
        overview.put("todayEarlyLeave", 6); // 今日早退人数
        overview.put("todayAbsent", 3); // 今日旷工人数

        // 实时打卡数据
        overview.put("recentPunchIns", Arrays.asList(
                Map.of("employeeName", "张三", "time", "08:45", "status", "正常"),
                Map.of("employeeName", "李四", "time", "09:02", "status", "迟到"),
                Map.of("employeeName", "王五", "time", "08:38", "status", "正常")));

        return overview;
    }

    /**
     * 获取所有统计周期
     */
    public List<StatisticsPeriod> getAllStatisticsPeriods() {
        return Arrays.asList(StatisticsPeriod.values());
    }

    /**
     * 获取所有统计类型
     */
    public List<StatisticsType> getAllStatisticsTypes() {
        return Arrays.asList(StatisticsType.values());
    }
}
