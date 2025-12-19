package net.lab1024.sa.attendance.report.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 考勤汇总报表
 * <p>
 * 封装考勤汇总报表的详细信息
 * 内存优化实现，使用基本数据类型和高效集合
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
public class AttendanceSummaryReport {

    /**
     * 报表ID
     */
    private String reportId;

    /**
     * 报表名称
     */
    private String reportName;

    /**
     * 报表类型
     */
    private ReportType reportType;

    /**
     * 报表期间开始日期
     */
    private LocalDate startDate;

    /**
     * 报表期间结束日期
     */
    private LocalDate endDate;

    /**
     * 生成时间
     */
    private LocalDateTime generatedTime;

    /**
     * 生成人
     */
    private Long generatedBy;

    /**
     * 生成人姓名
     */
    private String generatedByName;

    /**
     * 报表状态
     */
    private ReportStatus status;

    /**
     * 总员工数
     */
    private int totalEmployees;

    /**
     * 出勤员工数
     */
    private int presentEmployees;

    /**
     * 缺勤员工数
     */
    private int absentEmployees;

    /**
     * 请假员工数
     */
    private int leaveEmployees;

    /**
     * 出勤率（百分比）
     */
    private double attendanceRate;

    /**
     * 迟到员工数
     */
    private int lateEmployees;

    /**
     * 早退员工数
     */
    private int earlyLeaveEmployees;

    /**
     * 正常出勤员工数
     */
    private int normalEmployees;

    /**
     * 正常出勤率（百分比）
     */
    private double normalAttendanceRate;

    /**
     * 迟到率（百分比）
     */
    private double lateRate;

    /**
     * 早退率（百分比）
     */
    private double earlyLeaveRate;

    /**
     * 缺勤率（百分比）
     */
    private double absentRate;

    /**
     * 请假率（百分比）
     */
    private double leaveRate;

    /**
     * 总工作天数
     */
    private int totalWorkDays;

    /**
     * 平均出勤天数
     */
    private double averageAttendanceDays;

    /**
     * 总工作时长（小时）
     */
    private double totalWorkHours;

    /**
     * 人均工作时长（小时）
     */
    private double averageWorkHours;

    /**
     * 标准工作时长（小时）
     */
    private double standardWorkHours;

    /**
     * 工时完成率（百分比）
     */
    private double workHoursCompletionRate;

    /**
     * 加班员工数
     */
    private int overtimeEmployees;

    /**
     * 总加班时长（小时）
     */
    private double totalOvertimeHours;

    /**
     * 人均加班时长（小时）
     */
    private double averageOvertimeHours;

    /**
     * 加班率（百分比）
     */
    private double overtimeRate;

    /**
     * 部门统计数据
     */
    private List<DepartmentSummary> departmentSummaries;

    /**
     * 考勤异常统计
     */
    private AttendanceAnomalyStatistics anomalyStatistics;

    /**
     * 趋势分析数据
     */
    private TrendAnalysisData trendData;

    /**
     * 绩效指标
     */
    private PerformanceMetrics performanceMetrics;

    /**
     * 报表参数
     */
    private Map<String, Object> reportParameters;

    /**
     * 数据更新时间
     */
    private LocalDateTime dataUpdateTime;

    /**
     * 报表格式
     */
    private String reportFormat;

    /**
     * 报表文件路径
     */
    private String reportFilePath;

    /**
     * 报表文件大小（字节）
     */
    private long fileSize;

    /**
     * 生成耗时（毫秒）
     */
    private long generationTimeMs;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedAttributes;

    /**
     * 报表类型枚举
     */
    public enum ReportType {
        DAILY("日报"),
        WEEKLY("周报"),
        MONTHLY("月报"),
        QUARTERLY("季报"),
        ANNUAL("年报"),
        CUSTOM("自定义");

        private final String description;

        ReportType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 报表状态枚举
     */
    public enum ReportStatus {
        GENERATING("生成中"),
        COMPLETED("已完成"),
        FAILED("生成失败"),
        CANCELLED("已取消"),
        EXPIRED("已过期");

        private final String description;

        ReportStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 部门汇总数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentSummary {
        private Long departmentId;
        private String departmentName;
        private int employeeCount;
        private int presentCount;
        private int absentCount;
        private int leaveCount;
        private int lateCount;
        private int earlyLeaveCount;
        private double attendanceRate;
        private double normalRate;
        private double workHours;
    }

    /**
     * 考勤异常统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceAnomalyStatistics {
        private int totalAnomalies;
        private int missingClockInAnomalies;
        private int missingClockOutAnomalies;
        private int duplicateRecordsAnomalies;
        private int invalidTimeAnomalies;
        private int locationAnomalies;
        private int deviceAnomalies;
        private Map<String, Integer> anomalyTypeDistribution;
    }

    /**
     * 趋势分析数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendAnalysisData {
        private List<DailyTrendPoint> dailyTrends;
        private List<WeeklyTrendPoint> weeklyTrends;
        private List<MonthlyTrendPoint> monthlyTrends;
        private TrendDirection attendanceTrend;
        private TrendDirection punctualityTrend;
        private TrendDirection productivityTrend;
    }

    /**
     * 每日趋势点
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyTrendPoint {
        private LocalDate date;
        private double attendanceRate;
        private double lateRate;
        private double absentRate;
        private double workHours;
    }

    /**
     * 每周趋势点
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklyTrendPoint {
        private String weekNumber;
        private LocalDate weekStart;
        private LocalDate weekEnd;
        private double attendanceRate;
        private double lateRate;
        private double absentRate;
        private double workHours;
    }

    /**
     * 每月趋势点
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyTrendPoint {
        private int year;
        private int month;
        private double attendanceRate;
        private double lateRate;
        private double absentRate;
        private double workHours;
    }

    /**
     * 趋势方向枚举
     */
    public enum TrendDirection {
        IMPROVING("改善"),
        STABLE("稳定"),
        DECLINING("下降"),
        FLUCTUATING("波动");

        private final String description;

        TrendDirection(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 绩效指标
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceMetrics {
        private double efficiencyScore;
        private double punctualityScore;
        private double complianceScore;
        private double productivityScore;
        private double overallScore;
    }

    /**
     * 计算出勤率
     */
    public void calculateAttendanceRate() {
        if (totalEmployees > 0) {
            this.attendanceRate = (double) presentEmployees / totalEmployees * 100;
        } else {
            this.attendanceRate = 0.0;
        }
    }

    /**
     * 计算正常出勤率
     */
    public void calculateNormalAttendanceRate() {
        if (totalEmployees > 0) {
            this.normalAttendanceRate = (double) normalEmployees / totalEmployees * 100;
        } else {
            this.normalAttendanceRate = 0.0;
        }
    }

    /**
     * 计算迟到率
     */
    public void calculateLateRate() {
        if (totalEmployees > 0) {
            this.lateRate = (double) lateEmployees / totalEmployees * 100;
        } else {
            this.lateRate = 0.0;
        }
    }

    /**
     * 计算早退率
     */
    public void calculateEarlyLeaveRate() {
        if (totalEmployees > 0) {
            this.earlyLeaveRate = (double) earlyLeaveEmployees / totalEmployees * 100;
        } else {
            this.earlyLeaveRate = 0.0;
        }
    }

    /**
     * 计算缺勤率
     */
    public void calculateAbsentRate() {
        if (totalEmployees > 0) {
            this.absentRate = (double) absentEmployees / totalEmployees * 100;
        } else {
            this.absentRate = 0.0;
        }
    }

    /**
     * 计算请假率
     */
    public void calculateLeaveRate() {
        if (totalEmployees > 0) {
            this.leaveRate = (double) leaveEmployees / totalEmployees * 100;
        } else {
            this.leaveRate = 0.0;
        }
    }

    /**
     * 计算加班率
     */
    public void calculateOvertimeRate() {
        if (totalEmployees > 0) {
            this.overtimeRate = (double) overtimeEmployees / totalEmployees * 100;
        } else {
            this.overtimeRate = 0.0;
        }
    }

    /**
     * 计算平均出勤天数
     */
    public void calculateAverageAttendanceDays() {
        if (totalEmployees > 0) {
            this.averageAttendanceDays = (double) (presentEmployees + leaveEmployees) / totalEmployees;
        } else {
            this.averageAttendanceDays = 0.0;
        }
    }

    /**
     * 计算平均工作时长
     */
    public void calculateAverageWorkHours() {
        if (totalEmployees > 0) {
            this.averageWorkHours = totalWorkHours / totalEmployees;
        } else {
            this.averageWorkHours = 0.0;
        }
    }

    /**
     * 计算平均加班时长
     */
    public void calculateAverageOvertimeHours() {
        if (overtimeEmployees > 0) {
            this.averageOvertimeHours = totalOvertimeHours / overtimeEmployees;
        } else {
            this.averageOvertimeHours = 0.0;
        }
    }

    /**
     * 计算工时完成率
     */
    public void calculateWorkHoursCompletionRate() {
        if (standardWorkHours > 0) {
            this.workHoursCompletionRate = (totalWorkHours / standardWorkHours) * 100;
        } else {
            this.workHoursCompletionRate = 0.0;
        }
    }

    /**
     * 计算所有比率
     */
    public void calculateAllRates() {
        calculateAttendanceRate();
        calculateNormalAttendanceRate();
        calculateLateRate();
        calculateEarlyLeaveRate();
        calculateAbsentRate();
        calculateLeaveRate();
        calculateOvertimeRate();
        calculateAverageAttendanceDays();
        calculateAverageWorkHours();
        calculateAverageOvertimeHours();
        calculateWorkHoursCompletionRate();
    }

    /**
     * 获取健康状态评估
     */
    public String getHealthAssessment() {
        double overallScore = calculateOverallScore();

        if (overallScore >= 90) {
            return "优秀";
        } else if (overallScore >= 80) {
            return "良好";
        } else if (overallScore >= 70) {
            return "一般";
        } else if (overallScore >= 60) {
            return "较差";
        } else {
            return "需要关注";
        }
    }

    /**
     * 计算综合评分
     */
    private double calculateOverallScore() {
        // 出勤率权重40%
        double attendanceScore = attendanceRate * 0.4;

        // 准时率权重30%（正常出勤率体现准守程度）
        double punctualityScore = normalAttendanceRate * 0.3;

        // 工时完成率权重20%
        double workHoursScore = Math.min(workHoursCompletionRate, 100) * 0.2;

        // 其他因素权重10%
        double otherFactorsScore = 100.0 - (lateRate + earlyLeaveRate);
        if (otherFactorsScore < 0) {
            otherFactorsScore = 0;
        }

        return attendanceScore + punctualityScore + workHoursScore + (otherFactorsScore * 0.1);
    }

    /**
     * 获取关键指标摘要
     */
    public String getKeyMetricsSummary() {
        StringBuilder summary = new StringBuilder();

        summary.append("出勤率: ").append(String.format("%.1f%%", attendanceRate));
        summary.append(" | 正常率: ").append(String.format("%.1f%%", normalAttendanceRate));
        summary.append(" | 迟到率: ").append(String.format("%.1f%%", lateRate));

        if (absentRate > 0) {
            summary.append(" | 缺勤率: ").append(String.format("%.1f%%", absentRate));
        }

        if (overtimeRate > 0) {
            summary.append(" | 加班率: ").append(String.format("%.1f%%", overtimeRate));
        }

        if (totalWorkHours > 0) {
            summary.append(" | 总工时: ").append(String.format("%.1f", totalWorkHours));
        }

        return summary.toString();
    }

    /**
     * 获取报表期间描述
     */
    public String getPeriodDescription() {
        if (startDate == null && endDate == null) {
            return "未定义期间";
        }

        if (startDate != null && endDate != null) {
            if (startDate.equals(endDate)) {
                return startDate.toString();
            } else {
                return startDate.toString() + " 至 " + endDate.toString();
            }
        }

        if (startDate != null) {
            return "从 " + startDate.toString() + " 起";
        }

        return "截至 " + endDate.toString() + " 止";
    }

    /**
     * 获取文件大小描述
     */
    public String getFileSizeDescription() {
        if (fileSize <= 0) {
            return "0 B";
        }

        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * 获取生成耗时描述
     */
    public String getGenerationTimeDescription() {
        if (generationTimeMs <= 0) {
            return "0 ms";
        }

        if (generationTimeMs < 1000) {
            return generationTimeMs + " ms";
        } else if (generationTimeMs < 60000) {
            return String.format("%.1f s", generationTimeMs / 1000.0);
        } else {
            return String.format("%.1f min", generationTimeMs / 60000.0);
        }
    }

    /**
     * 检查报表是否有效
     */
    public boolean isValid() {
        return reportId != null && !reportId.trim().isEmpty() &&
               reportName != null && !reportName.trim().isEmpty() &&
               reportType != null &&
               status != null &&
               totalEmployees >= 0 &&
               startDate != null && endDate != null &&
               !startDate.isAfter(endDate);
    }

    /**
     * 检查报表是否为最新数据
     */
    public boolean isLatestData() {
        return dataUpdateTime != null &&
               LocalDateTime.now().minusHours(24).isBefore(dataUpdateTime);
    }

    /**
     * 内存优化：重置大数据字段
     */
    public void resetLargeFields() {
        departmentSummaries = null;
        trendData = null;
        reportParameters = null;
        extendedAttributes = null;
    }

    /**
     * 内存优化：轻量级克隆
     */
    public AttendanceSummaryReport lightClone() {
        AttendanceSummaryReport clone = AttendanceSummaryReport.builder()
                .reportId(this.reportId)
                .reportName(this.reportName)
                .reportType(this.reportType)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .generatedTime(this.generatedTime)
                .generatedBy(this.generatedBy)
                .generatedByName(this.generatedByName)
                .status(this.status)
                .totalEmployees(this.totalEmployees)
                .presentEmployees(this.presentEmployees)
                .absentEmployees(this.absentEmployees)
                .leaveEmployees(this.leaveEmployees)
                .attendanceRate(this.attendanceRate)
                .lateEmployees(this.lateEmployees)
                .earlyLeaveEmployees(this.earlyLeaveEmployees)
                .normalEmployees(this.normalEmployees)
                .normalAttendanceRate(this.normalAttendanceRate)
                .lateRate(this.lateRate)
                .earlyLeaveRate(this.earlyLeaveRate)
                .absentRate(this.absentRate)
                .leaveRate(this.leaveRate)
                .totalWorkDays(this.totalWorkDays)
                .averageAttendanceDays(this.averageAttendanceDays)
                .totalWorkHours(this.totalWorkHours)
                .averageWorkHours(this.averageWorkHours)
                .standardWorkHours(this.standardWorkHours)
                .workHoursCompletionRate(this.workHoursCompletionRate)
                .overtimeEmployees(this.overtimeEmployees)
                .totalOvertimeHours(this.totalOvertimeHours)
                .averageOvertimeHours(this.averageOvertimeHours)
                .overtimeRate(this.overtimeRate)
                .dataUpdateTime(this.dataUpdateTime)
                .reportFormat(this.reportFormat)
                .reportFilePath(this.reportFilePath)
                .fileSize(this.fileSize)
                .generationTimeMs(this.generationTimeMs)
                .build();

        // 不复制大字段以节省内存
        return clone;
    }
}