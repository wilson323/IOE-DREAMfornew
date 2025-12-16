package net.lab1024.sa.attendance.realtime.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 公司实时考勤概览
 * <p>
 * 封装整个公司的实时考勤概览信息
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRealtimeOverview {

    /**
     * 公司ID
     */
    private Long companyId;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 统计时间范围
     */
    private TimeRange timeRange;

    /**
     * 公司总员工数
     */
    private Integer totalEmployeeCount;

    /**
     * 在岗员工数
     */
    private Integer onDutyEmployeeCount;

    /**
     * 出勤员工数
     */
    private Integer attendanceEmployeeCount;

    /**
     * 公司出勤率（百分比）
     */
    private BigDecimal attendanceRate;

    /**
     * 正常工作员工数
     */
    private Integer normalWorkEmployeeCount;

    /**
     * 正常工作率（百分比）
     */
    private BigDecimal normalWorkRate;

    /**
     * 迟到员工数
     */
    private Integer lateEmployeeCount;

    /**
     * 迟到率（百分比）
     */
    private BigDecimal lateRate;

    /**
     * 早退员工数
     */
    private Integer earlyLeaveEmployeeCount;

    /**
     * 早退率（百分比）
     */
    private BigDecimal earlyLeaveRate;

    /**
     * 缺勤员工数
     */
    private Integer absentEmployeeCount;

    /**
     * 缺勤率（百分比）
     */
    private BigDecimal absentRate;

    /**
     * 请假员工数
     */
    private Integer leaveEmployeeCount;

    /**
     * 请假率（百分比）
     */
    private BigDecimal leaveRate;

    /**
     * 出差员工数
     */
    private Integer businessTripEmployeeCount;

    /**
     * 出差率（百分比）
     */
    private BigDecimal businessTripRate;

    /**
     * 加班员工数
     */
    private Integer overtimeEmployeeCount;

    /**
     * 加班率（百分比）
     */
    private BigDecimal overtimeRate;

    /**
     * 公司总工作时长（小时）
     */
    private Double totalWorkDurationHours;

    /**
     * 人均工作时长（小时）
     */
    private Double averageWorkDurationHours;

    /**
     * 公司总加班时长（小时）
     */
    private Double totalOvertimeHours;

    /**
     * 人均加班时长（小时）
     */
    private Double averageOvertimeHours;

    /**
     * 今日预计产出工时
     */
    private Double expectedWorkHours;

    /**
     * 实际完成工时
     */
    private Double actualWorkHours;

    /**
     * 工时完成率（百分比）
     */
    private BigDecimal workHourCompletionRate;

    /**
     * 部门统计列表
     */
    private List<DepartmentStatistics> departmentStatistics;

    /**
     * 按部门出勤率分布
     */
    private Map<String, BigDecimal> departmentAttendanceRates;

    /**
     * 按部门纪律评分分布
     */
    private Map<String, BigDecimal> departmentDisciplineScores;

    /**
     * 按时间段出勤分布（每小时的出勤人数）
     */
    private Map<String, Integer> hourlyAttendanceTrend;

    /**
     * 今日异常事件总数
     */
    private Integer todayTotalAnomalies;

    /**
     * 实时未处理异常数
     */
    private Integer pendingAnomalyCount;

    /**
     * 严重异常事件数
     */
    private Integer severeAnomalyCount;

    /**
     * 今日预警总数
     */
    private Integer todayTotalAlerts;

    /**
     * 待处理预警数
     */
    private Integer pendingAlertCount;

    /**
     * 紧急预警数
     */
    private Integer urgentAlertCount;

    /**
     * 公司运营效率评分（0-100）
     */
    private BigDecimal operationalEfficiencyScore;

    /**
     * 公司纪律评分（0-100）
     */
    private BigDecimal disciplineScore;

    /**
     * 公司综合评分（0-100）
     */
    private BigDecimal overallPerformanceScore;

    /**
     * 运营状态
     */
    private OperationalStatus operationalStatus;

    /**
     * 关键指标趋势（最近7天）
     */
    private List<DailyTrend> dailyTrends;

    /**
     * 与昨日对比数据
     */
    private DayOverDayComparison dayOverDayComparison;

    /**
     * 与上周同期对比数据
     */
    private WeekOverWeekComparison weekOverWeekComparison;

    /**
     * 与上月同期对比数据
     */
    private MonthOverMonthComparison monthOverMonthComparison;

    /**
     * 实时监控指标
     */
    private RealtimeMonitoringMetrics realtimeMonitoringMetrics;

    /**
     * 数据生成时间
     */
    private LocalDateTime overviewTime;

    /**
     * 数据刷新间隔（分钟）
     */
    private Integer refreshIntervalMinutes;

    /**
     * 下次刷新时间
     */
    private LocalDateTime nextRefreshTime;

    /**
     * 数据质量评分（0-100）
     */
    private BigDecimal dataQualityScore;

    /**
     * 系统健康状态
     */
    private SystemHealthStatus systemHealthStatus;

    /**
     * 部门统计数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentStatistics {
        private Long departmentId;
        private String departmentName;
        private Integer employeeCount;
        private Integer attendanceCount;
        private BigDecimal attendanceRate;
        private Integer lateCount;
        private Integer absentCount;
        private Integer overtimeCount;
        private BigDecimal performanceScore;
    }

    /**
     * 每日趋势数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyTrend {
        private LocalDateTime date;
        private BigDecimal attendanceRate;
        private BigDecimal lateRate;
        private BigDecimal absentRate;
        private Double averageWorkHours;
        private BigDecimal performanceScore;
    }

    /**
     * 日环比数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayOverDayComparison {
        private BigDecimal attendanceRateChange;
        private BigDecimal lateRateChange;
        private BigDecimal absentRateChange;
        private Double averageWorkHoursChange;
        private BigDecimal performanceScoreChange;
        private String trendDirection;
    }

    /**
     * 周环比数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeekOverWeekComparison {
        private BigDecimal attendanceRateChange;
        private BigDecimal lateRateChange;
        private BigDecimal absentRateChange;
        private Double averageWorkHoursChange;
        private BigDecimal performanceScoreChange;
        private String trendDirection;
    }

    /**
     * 月环比数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthOverMonthComparison {
        private BigDecimal attendanceRateChange;
        private BigDecimal lateRateChange;
        private BigDecimal absentRateChange;
        private Double averageWorkHoursChange;
        private BigDecimal performanceScoreChange;
        private String trendDirection;
    }

    /**
     * 实时监控指标
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RealtimeMonitoringMetrics {
        private Integer activeEmployeeCount;
        private Integer currentProcessingEvents;
        private Double systemResponseTime;
        private Double throughputPerMinute;
        private Integer errorRate;
        private Integer systemLoadPercentage;
        private Integer memoryUsagePercentage;
        private Integer cacheHitRate;
    }

    /**
     * 运营状态枚举
     */
    public enum OperationalStatus {
        EXCELLENT("运营优秀"),
        GOOD("运营良好"),
        NORMAL("运营正常"),
        ATTENTION("需要关注"),
        CRITICAL("需要紧急处理");

        private final String description;

        OperationalStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 系统健康状态枚举
     */
    public enum SystemHealthStatus {
        HEALTHY("系统健康"),
        WARNING("系统警告"),
        DEGRADED("性能降级"),
        ERROR("系统错误"),
        CRITICAL("系统严重故障");

        private final String description;

        SystemHealthStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 计算出勤率
     */
    public void calculateAttendanceRate() {
        if (totalEmployeeCount != null && totalEmployeeCount > 0 && attendanceEmployeeCount != null) {
            BigDecimal attendance = BigDecimal.valueOf(attendanceEmployeeCount);
            BigDecimal total = BigDecimal.valueOf(totalEmployeeCount);
            this.attendanceRate = attendance.divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        } else {
            this.attendanceRate = BigDecimal.ZERO;
        }
    }

    /**
     * 计算所有率指标
     */
    public void calculateAllRates() {
        calculateAttendanceRate();

        if (totalEmployeeCount != null && totalEmployeeCount > 0) {
            BigDecimal total = BigDecimal.valueOf(totalEmployeeCount);

            if (normalWorkEmployeeCount != null) {
                this.normalWorkRate = BigDecimal.valueOf(normalWorkEmployeeCount)
                        .divide(total, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }

            if (lateEmployeeCount != null) {
                this.lateRate = BigDecimal.valueOf(lateEmployeeCount)
                        .divide(total, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }

            if (earlyLeaveEmployeeCount != null) {
                this.earlyLeaveRate = BigDecimal.valueOf(earlyLeaveEmployeeCount)
                        .divide(total, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }

            if (absentEmployeeCount != null) {
                this.absentRate = BigDecimal.valueOf(absentEmployeeCount)
                        .divide(total, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }

            if (leaveEmployeeCount != null) {
                this.leaveRate = BigDecimal.valueOf(leaveEmployeeCount)
                        .divide(total, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }

            if (businessTripEmployeeCount != null) {
                this.businessTripRate = BigDecimal.valueOf(businessTripEmployeeCount)
                        .divide(total, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }

            if (overtimeEmployeeCount != null) {
                this.overtimeRate = BigDecimal.valueOf(overtimeEmployeeCount)
                        .divide(total, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }
        } else {
            this.normalWorkRate = BigDecimal.ZERO;
            this.lateRate = BigDecimal.ZERO;
            this.earlyLeaveRate = BigDecimal.ZERO;
            this.absentRate = BigDecimal.ZERO;
            this.leaveRate = BigDecimal.ZERO;
            this.businessTripRate = BigDecimal.ZERO;
            this.overtimeRate = BigDecimal.ZERO;
        }
    }

    /**
     * 计算平均工作时长
     */
    public void calculateAverageWorkDuration() {
        if (attendanceEmployeeCount != null && attendanceEmployeeCount > 0 && totalWorkDurationHours != null) {
            this.averageWorkDurationHours = totalWorkDurationHours / attendanceEmployeeCount;
        } else {
            this.averageWorkDurationHours = 0.0;
        }
    }

    /**
     * 计算平均加班时长
     */
    public void calculateAverageOvertimeDuration() {
        if (overtimeEmployeeCount != null && overtimeEmployeeCount > 0 && totalOvertimeHours != null) {
            this.averageOvertimeHours = totalOvertimeHours / overtimeEmployeeCount;
        } else {
            this.averageOvertimeHours = 0.0;
        }
    }

    /**
     * 计算工时完成率
     */
    public void calculateWorkHourCompletionRate() {
        if (expectedWorkHours != null && expectedWorkHours > 0 && actualWorkHours != null) {
            BigDecimal actual = BigDecimal.valueOf(actualWorkHours);
            BigDecimal expected = BigDecimal.valueOf(expectedWorkHours);
            this.workHourCompletionRate = actual.divide(expected, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        } else {
            this.workHourCompletionRate = BigDecimal.ZERO;
        }
    }

    /**
     * 计算运营效率评分
     */
    public void calculateOperationalEfficiencyScore() {
        if (attendanceRate == null || workHourCompletionRate == null) {
            this.operationalEfficiencyScore = BigDecimal.ZERO;
            return;
        }

        // 运营效率评分 = 出勤率 * 0.5 + 工时完成率 * 0.3 + 正常工作率 * 0.2
        BigDecimal attendanceScore = attendanceRate.multiply(BigDecimal.valueOf(0.5));
        BigDecimal workHourScore = workHourCompletionRate.multiply(BigDecimal.valueOf(0.3));
        BigDecimal normalWorkScore = normalWorkRate != null ?
                normalWorkRate.multiply(BigDecimal.valueOf(0.2)) : BigDecimal.ZERO;

        this.operationalEfficiencyScore = attendanceScore.add(workHourScore).add(normalWorkScore);
    }

    /**
     * 计算纪律评分
     */
    public void calculateDisciplineScore() {
        if (lateRate == null || earlyLeaveRate == null || absentRate == null) {
            this.disciplineScore = BigDecimal.ZERO;
            return;
        }

        // 纪律评分 = 100 - (迟到率 + 早退率 + 缺勤率 * 2 + 异常率 * 0.5)
        BigDecimal violationScore = lateRate.add(earlyLeaveRate)
                .add(absentRate.multiply(BigDecimal.valueOf(2)));

        // 考虑异常影响
        if (todayTotalAnomalies != null && totalEmployeeCount != null && totalEmployeeCount > 0) {
            BigDecimal anomalyRate = BigDecimal.valueOf(todayTotalAnomalies)
                    .divide(BigDecimal.valueOf(totalEmployeeCount), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            violationScore = violationScore.add(anomalyRate.multiply(BigDecimal.valueOf(0.5)));
        }

        this.disciplineScore = BigDecimal.valueOf(100).subtract(violationScore);

        // 确保评分在0-100范围内
        if (this.disciplineScore.compareTo(BigDecimal.ZERO) < 0) {
            this.disciplineScore = BigDecimal.ZERO;
        } else if (this.disciplineScore.compareTo(BigDecimal.valueOf(100)) > 0) {
            this.disciplineScore = BigDecimal.valueOf(100);
        }
    }

    /**
     * 计算综合绩效评分
     */
    public void calculateOverallPerformanceScore() {
        if (operationalEfficiencyScore == null || disciplineScore == null) {
            this.overallPerformanceScore = BigDecimal.ZERO;
            return;
        }

        // 综合评分 = 运营效率 * 0.6 + 纪律评分 * 0.4
        BigDecimal efficiencyWeight = operationalEfficiencyScore.multiply(BigDecimal.valueOf(0.6));
        BigDecimal disciplineWeight = disciplineScore.multiply(BigDecimal.valueOf(0.4));
        this.overallPerformanceScore = efficiencyWeight.add(disciplineWeight);
    }

    /**
     * 确定运营状态
     */
    public void determineOperationalStatus() {
        if (overallPerformanceScore == null) {
            this.operationalStatus = OperationalStatus.ATTENTION;
            return;
        }

        if (overallPerformanceScore.compareTo(BigDecimal.valueOf(90)) >= 0) {
            this.operationalStatus = OperationalStatus.EXCELLENT;
        } else if (overallPerformanceScore.compareTo(BigDecimal.valueOf(80)) >= 0) {
            this.operationalStatus = OperationalStatus.GOOD;
        } else if (overallPerformanceScore.compareTo(BigDecimal.valueOf(70)) >= 0) {
            this.operationalStatus = OperationalStatus.NORMAL;
        } else if (overallPerformanceScore.compareTo(BigDecimal.valueOf(50)) >= 0) {
            this.operationalStatus = OperationalStatus.ATTENTION;
        } else {
            this.operationalStatus = OperationalStatus.CRITICAL;
        }
    }

    /**
     * 获取关键指标摘要
     */
    public String getKeyMetricsSummary() {
        StringBuilder summary = new StringBuilder();

        if (attendanceRate != null) {
            summary.append("出勤率: ").append(attendanceRate.setScale(1, RoundingMode.HALF_UP)).append("%");
        }

        if (operationalEfficiencyScore != null) {
            summary.append(" | 运营效率: ").append(operationalEfficiencyScore.setScale(1, RoundingMode.HALF_UP));
        }

        if (disciplineScore != null) {
            summary.append(" | 纪律评分: ").append(disciplineScore.setScale(1, RoundingMode.HALF_UP));
        }

        if (todayTotalAnomalies != null && todayTotalAnomalies > 0) {
            summary.append(" | 异常: ").append(todayTotalAnomalies).append("个");
        }

        if (pendingAlertCount != null && pendingAlertCount > 0) {
            summary.append(" | 待处理: ").append(pendingAlertCount).append("项");
        }

        return summary.toString();
    }

    /**
     * 获取运营状态描述
     */
    public String getOperationalStatusDescription() {
        if (operationalStatus == null) {
            return "状态未知";
        }
        return operationalStatus.getDescription() + " (评分: " +
                (overallPerformanceScore != null ? overallPerformanceScore.setScale(1, RoundingMode.HALF_UP) : "N/A") + ")";
    }

    /**
     * 检查是否需要刷新数据
     */
    public boolean needsRefresh() {
        if (nextRefreshTime == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(nextRefreshTime);
    }

    /**
     * 更新下次刷新时间
     */
    public void updateNextRefreshTime() {
        if (refreshIntervalMinutes != null) {
            this.nextRefreshTime = LocalDateTime.now().plusMinutes(refreshIntervalMinutes);
        }
    }

    /**
     * 执行所有计算
     */
    public void performAllCalculations() {
        calculateAllRates();
        calculateAverageWorkDuration();
        calculateAverageOvertimeDuration();
        calculateWorkHourCompletionRate();
        calculateOperationalEfficiencyScore();
        calculateDisciplineScore();
        calculateOverallPerformanceScore();
        determineOperationalStatus();
        updateNextRefreshTime();
    }
}