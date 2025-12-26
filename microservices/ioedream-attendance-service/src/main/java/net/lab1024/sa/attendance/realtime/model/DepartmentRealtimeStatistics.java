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
 * 部门实时统计数据
 * <p>
 * 封装部门的实时考勤统计信息
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
public class DepartmentRealtimeStatistics {

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 统计时间范围
     */
    private TimeRange timeRange;

    /**
     * 部门总员工数
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
     * 出勤率（百分比）
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
     * 平均工作时长（分钟）
     */
    private Double averageWorkDurationMinutes;

    /**
     * 部门总工作时长（分钟）
     */
    private Long totalWorkDurationMinutes;

    /**
     * 平均加班时长（分钟）
     */
    private Double averageOvertimeMinutes;

    /**
     * 部门总加班时长（分钟）
     */
    private Long totalOvertimeMinutes;

    /**
     * 按考勤状态分类统计
     */
    private Map<String, Integer> statusDistribution;

    /**
     * 按班次分类统计
     */
    private Map<String, Integer> shiftDistribution;

    /**
     * 按时间点出勤分布（每小时的在岗人数）
     */
    private Map<String, Integer> hourlyAttendanceDistribution;

    /**
     * 异常事件统计
     */
    private List<AnomalyEvent> anomalyEvents;

    /**
     * 实时异常数量
     */
    private Integer realtimeAnomalyCount;

    /**
     * 今日新增异常数量
     */
    private Integer todayNewAnomalyCount;

    /**
     * 预警信息统计
     */
    private List<AlertInfo> alertInfos;

    /**
     * 待处理预警数量
     */
    private Integer pendingAlertCount;

    /**
     * 部门效率评分（0-100）
     */
    private BigDecimal efficiencyScore;

    /**
     * 部门纪律评分（0-100）
     */
    private BigDecimal disciplineScore;

    /**
     * 部门综合评分（0-100）
     */
    private BigDecimal overallScore;

    /**
     * 昨日同期对比数据
     */
    private ComparisonData yesterdayComparison;

    /**
     * 上周同期对比数据
     */
    private ComparisonData lastWeekComparison;

    /**
     * 上月同期对比数据
     */
    private ComparisonData lastMonthComparison;

    /**
     * 统计生成时间
     */
    private LocalDateTime statisticsTime;

    /**
     * 数据更新频率（分钟）
     */
    private Integer updateFrequencyMinutes;

    /**
     * 下次更新时间
     */
    private LocalDateTime nextUpdateTime;

    /**
     * 数据状态
     */
    private DataStatus dataStatus;

    /**
     * 异常事件记录
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnomalyEvent {
        private String eventId;
        private String eventType;
        private String eventDescription;
        private Long employeeId;
        private String employeeName;
        private LocalDateTime eventTime;
        private String severity;
        private Boolean isResolved;
        private LocalDateTime resolvedTime;
    }

    /**
     * 预警信息记录
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertInfo {
        private String alertId;
        private String alertType;
        private String alertTitle;
        private String alertDescription;
        private String alertLevel;
        private LocalDateTime alertTime;
        private Boolean requiresAction;
        private String assignedTo;
        private LocalDateTime deadline;
        private Boolean isCompleted;
        private LocalDateTime completedTime;
    }

    /**
     * 对比数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonData {
        private BigDecimal attendanceRateChange;
        private BigDecimal lateRateChange;
        private BigDecimal earlyLeaveRateChange;
        private BigDecimal absentRateChange;
        private Double averageWorkDurationChange;
        private BigDecimal efficiencyScoreChange;
        private Double totalWorkDurationChange;
    }

    /**
     * 数据状态枚举
     */
    public enum DataStatus {
        FRESH("最新数据"),
        UPDATING("更新中"),
        STALE("数据过期"),
        ERROR("数据错误");

        private final String description;

        DataStatus(String description) {
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
     * 计算各项率指标
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
        if (attendanceEmployeeCount != null && attendanceEmployeeCount > 0 && totalWorkDurationMinutes != null) {
            this.averageWorkDurationMinutes = totalWorkDurationMinutes.doubleValue() / attendanceEmployeeCount;
        } else {
            this.averageWorkDurationMinutes = 0.0;
        }
    }

    /**
     * 计算平均加班时长
     */
    public void calculateAverageOvertimeDuration() {
        if (overtimeEmployeeCount != null && overtimeEmployeeCount > 0 && totalOvertimeMinutes != null) {
            this.averageOvertimeMinutes = totalOvertimeMinutes.doubleValue() / overtimeEmployeeCount;
        } else {
            this.averageOvertimeMinutes = 0.0;
        }
    }

    /**
     * 计算效率评分
     */
    public void calculateEfficiencyScore() {
        if (attendanceRate == null || normalWorkRate == null) {
            this.efficiencyScore = BigDecimal.ZERO;
            return;
        }

        // 效率评分 = 出勤率 * 0.6 + 正常工作率 * 0.4
        BigDecimal attendanceScore = attendanceRate.multiply(BigDecimal.valueOf(0.6));
        BigDecimal normalWorkScore = normalWorkRate.multiply(BigDecimal.valueOf(0.4));
        this.efficiencyScore = attendanceScore.add(normalWorkScore);
    }

    /**
     * 计算纪律评分
     */
    public void calculateDisciplineScore() {
        if (lateRate == null || earlyLeaveRate == null || absentRate == null) {
            this.disciplineScore = BigDecimal.ZERO;
            return;
        }

        // 纪律评分 = 100 - (迟到率 + 早退率 + 缺勤率 * 2)
        BigDecimal violationScore = lateRate.add(earlyLeaveRate)
                .add(absentRate.multiply(BigDecimal.valueOf(2)));
        this.disciplineScore = BigDecimal.valueOf(100).subtract(violationScore);

        // 确保评分在0-100范围内
        if (this.disciplineScore.compareTo(BigDecimal.ZERO) < 0) {
            this.disciplineScore = BigDecimal.ZERO;
        } else if (this.disciplineScore.compareTo(BigDecimal.valueOf(100)) > 0) {
            this.disciplineScore = BigDecimal.valueOf(100);
        }
    }

    /**
     * 计算综合评分
     */
    public void calculateOverallScore() {
        if (efficiencyScore == null || disciplineScore == null) {
            this.overallScore = BigDecimal.ZERO;
            return;
        }

        // 综合评分 = 效率评分 * 0.7 + 纪律评分 * 0.3
        BigDecimal efficiencyWeight = efficiencyScore.multiply(BigDecimal.valueOf(0.7));
        BigDecimal disciplineWeight = disciplineScore.multiply(BigDecimal.valueOf(0.3));
        this.overallScore = efficiencyWeight.add(disciplineWeight);
    }

    /**
     * 获取部门健康状态描述
     */
    public String getDepartmentHealthStatus() {
        if (overallScore == null) {
            return "状态未知";
        }

        if (overallScore.compareTo(BigDecimal.valueOf(90)) >= 0) {
            return "优秀";
        } else if (overallScore.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return "良好";
        } else if (overallScore.compareTo(BigDecimal.valueOf(70)) >= 0) {
            return "一般";
        } else if (overallScore.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return "较差";
        } else {
            return "需要关注";
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

        if (lateRate != null && lateRate.compareTo(BigDecimal.ZERO) > 0) {
            summary.append(" | 迟到率: ").append(lateRate.setScale(1, RoundingMode.HALF_UP)).append("%");
        }

        if (absentRate != null && absentRate.compareTo(BigDecimal.ZERO) > 0) {
            summary.append(" | 缺勤率: ").append(absentRate.setScale(1, RoundingMode.HALF_UP)).append("%");
        }

        if (overtimeRate != null && overtimeRate.compareTo(BigDecimal.ZERO) > 0) {
            summary.append(" | 加班率: ").append(overtimeRate.setScale(1, RoundingMode.HALF_UP)).append("%");
        }

        if (overallScore != null) {
            summary.append(" | 综合评分: ").append(overallScore.setScale(1, RoundingMode.HALF_UP));
        }

        return summary.toString();
    }

    /**
     * 检查数据是否需要更新
     */
    public boolean needsUpdate() {
        if (nextUpdateTime == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(nextUpdateTime);
    }

    /**
     * 更新下次更新时间
     */
    public void updateNextUpdateTime() {
        if (updateFrequencyMinutes != null) {
            this.nextUpdateTime = LocalDateTime.now().plusMinutes(updateFrequencyMinutes);
        }
    }
}
