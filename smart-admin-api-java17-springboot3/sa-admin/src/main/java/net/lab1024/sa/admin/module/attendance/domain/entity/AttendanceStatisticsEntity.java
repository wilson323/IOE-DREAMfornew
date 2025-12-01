package net.lab1024.sa.admin.module.attendance.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import net.lab1024.sa.base.common.entity.BaseEntity;import java.time.LocalDateTime;import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import net.lab1024.sa.base.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤统计实体
 *
 * 严格遵循repowiki规范:
 * - 继承BaseEntity，包含审计字段
 * - 使用jakarta包，避免javax包
 * - 使用Lombok简化代码
 * - 字段命名规范：下划线分隔
 * - 完整的统计计算逻辑
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("t_attendance_statistics")
public class AttendanceStatisticsEntity extends BaseEntity {

    /**
     * 统计ID
     */
    @TableId(value = "statistics_id", type = IdType.AUTO)
    private Long statisticsId;

    /**
     * 员工ID
     */
    @TableField("employee_id")
    private Long employeeId;

    /**
     * 部门ID
     */
    @TableField("department_id")
    private Long departmentId;

    /**
     * 公司ID
     */
    @TableField("company_id")
    private Long companyId;

    /**
     * 统计类型
     * DAILY-日报, WEEKLY-周报, MONTHLY-月报, QUARTERLY-季报, YEARLY-年报
     */
    @TableField("statistics_type")
    private String statisticsType;

    /**
     * 统计周期
     * 2024-01, 2024-W01
     */
    @TableField("statistics_period")
    private String statisticsPeriod;

    /**
     * 统计日期
     */
    @TableField("statistics_date")
    private LocalDate statisticsDate;

    /**
     * 年份
     */
    @TableField("year")
    private Integer year;

    /**
     * 月份
     */
    @TableField("month")
    private Integer month;

    /**
     * 季度
     */
    @TableField("quarter")
    private Integer quarter;

    /**
     * 周数
     */
    @TableField("week")
    private Integer week;

    /**
     * 总天数
     */
    @TableField("total_days")
    private Integer totalDays;

    /**
     * 工作天数
     */
    @TableField("work_days")
    private Integer workDays;

    /**
     * 实际工作天数
     */
    @TableField("actual_work_days")
    private Integer actualWorkDays;

    /**
     * 出勤天数
     */
    @TableField("present_days")
    private Integer presentDays;

    /**
     * 缺勤天数
     */
    @TableField("absent_days")
    private Integer absentDays;

    /**
     * 请假天数
     */
    @TableField("leave_days")
    private Integer leaveDays;

    /**
     * 节假日天数
     */
    @TableField("holiday_days")
    private Integer holidayDays;

    /**
     * 迟到天数
     */
    @TableField("late_days")
    private Integer lateDays;

    /**
     * 早退天数
     */
    @TableField("early_leave_days")
    private Integer earlyLeaveDays;

    /**
     * 加班天数
     */
    @TableId("overtime_days")
    private Integer overtimeDays;

    /**
     * 忘打卡天数
     */
    @TableField("forget_punch_days")
    private Integer forgetPunchDays;

    /**
     * 总工作时长(小时)
     */
    @TableField("total_work_hours")
    private BigDecimal totalWorkHours;

    /**
     * 实际工作时长(小时)
     */
    @TableField("actual_work_hours")
    private BigDecimal actualWorkHours;

    /**
     * 标准工作时长(小时)
     */
    @TableField("standard_work_hours")
    private BigDecimal standardWorkHours;

    /**
     * 加班时长(小时)
     */
    @TableField("overtime_hours")
    private BigDecimal overtimeHours;

    /**
     * 周末加班时长(小时)
     */
    @TableField("overtime_weekend_hours")
    private BigDecimal overtimeWeekendHours;

    /**
     * 节假日加班时长(小时)
     */
    @TableField("overtime_holiday_hours")
    private BigDecimal overtimeHolidayHours;

    /**
     * 迟到总分钟数
     */
    @TableField("late_total_minutes")
    private Integer lateTotalMinutes;

    /**
     * 早退总分钟数
     */
    @TableField("early_leave_total_minutes")
    private Integer earlyLeaveTotalMinutes;

    /**
     * 出勤率(%)
     */
    @TableField("attendance_rate")
    private BigDecimal attendanceRate;

    /**
     * 准时率(%)
     */
    @TableField("punctuality_rate")
    private BigDecimal punctualityRate;

    /**
     * 工作效率(%)
     */
    @TableField("efficiency_rate")
    private BigDecimal efficiencyRate;

    /**
     * 异常率(%)
     */
    @TableField("abnormal_rate")
    private BigDecimal abnormalRate;

    /**
     * 平均工作时长(小时)
     */
    @TableField("average_work_hours")
    private BigDecimal averageWorkHours;

    /**
     * 最长连续工作天数
     */
    @TableField("max_continuous_work_days")
    private Integer maxContinuousWorkDays;

    /**
     * 假期余额(天)
     */
    @TableField("leave_balance")
    private BigDecimal leaveBalance;

    /**
     * 已用病假(天)
     */
    @TableField("sick_leave_used")
    private BigDecimal sickLeaveUsed;

    /**
     * 已用年假(天)
     */
    @TableField("annual_leave_used")
    private BigDecimal annualLeaveUsed;

    /**
     * 已用事假(天)
     */
    @TableField("personal_leave_used")
    private BigDecimal personalLeaveUsed;

    /**
     * 绩效得分
     */
    @TableField("performance_score")
    private BigDecimal performanceScore;

    /**
     * 排名
     */
    @TableField("ranking")
    private Integer ranking;

    /**
     * 排名百分位
     */
    @TableField("ranking_percentile")
    private BigDecimal rankingPercentile;

    /**
     * 环比增长率(%)
     */
    @TableField("trend_month_over_month")
    private BigDecimal trendMonthOverMonth;

    /**
     * 同比增长率(%)
     */
    @TableField("trend_year_over_year")
    private BigDecimal trendYearOverYear;

    /**
     * 考勤质量得分
     */
    @TableField("quality_score")
    private BigDecimal qualityScore;

    /**
     * 风险等级
     * LOW-低, MEDIUM-中, HIGH-高
     */
    @TableField("risk_level")
    private String riskLevel;

    /**
     * 改进建议(JSON格式)
     */
    @TableField("recommendations")
    private String recommendations;

    /**
     * 详细指标数据(JSON格式)
     */
    @TableField("metrics_data")
    private String metricsData;

    /**
     * 是否系统生成
     * 0-手动, 1-系统
     */
    @TableField("created_by_system")
    private Integer createdBySystem;

    /**
     * 最后计算时间
     */
    @TableField("last_calculated_time")
    private LocalDateTime lastCalculatedTime;

    /**
     * 计算状态
     * PENDING-待计算, CALCULATING-计算中, COMPLETED-已完成, FAILED-失败
     */
    @TableField("calculation_status")
    private String calculationStatus;

    /**
     * 检查统计是否已完成
     *
     * @return 是否已完成
     */
    public boolean isCalculationCompleted() {
        return "COMPLETED".equals(calculationStatus);
    }

    /**
     * 检查统计是否正在计算
     *
     * @return 是否正在计算
     */
    public boolean isCalculationInProgress() {
        return "CALCULATING".equals(calculationStatus);
    }

    /**
     * 检查统计是否需要计算
     *
     * @return 是否需要计算
     */
    public boolean needsCalculation() {
        return "PENDING".equals(calculationStatus) || "FAILED".equals(calculationStatus);
    }

    /**
     * 计算出勤率
     *
     * @return 出勤率百分比
     */
    public BigDecimal calculateAttendanceRate() {
        if (workDays == null || workDays == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(presentDays)
                .divide(BigDecimal.valueOf(workDays), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * 计算准时率
     *
     * @return 准时率百分比
     */
    public BigDecimal calculatePunctualityRate() {
        if (presentDays == null || presentDays == 0) {
            return BigDecimal.ZERO;
        }

        int onTimeDays = presentDays - (lateDays + earlyLeaveDays);
        return BigDecimal.valueOf(onTimeDays)
                .divide(BigDecimal.valueOf(presentDays), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * 计算异常率
     *
     * @return 异常率百分比
     */
    public BigDecimal calculateAbnormalRate() {
        if (workDays == null || workDays == 0) {
            return BigDecimal.ZERO;
        }

        int abnormalDays = lateDays + earlyLeaveDays + forgetPunchDays;
        return BigDecimal.valueOf(abnormalDays)
                .divide(BigDecimal.valueOf(workDays), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * 计算工作效率
     *
     * @return 工作效率百分比
     */
    public BigDecimal calculateEfficiencyRate() {
        if (standardWorkHours == null || standardWorkHours.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal efficiency = actualWorkHours != null ? actualWorkHours : BigDecimal.ZERO;
        return efficiency
                .divide(standardWorkHours, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * 计算平均工作时长
     *
     * @return 平均工作时长(小时)
     */
    public BigDecimal calculateAverageWorkHours() {
        if (actualWorkDays == null || actualWorkDays == 0) {
            return BigDecimal.ZERO;
        }

        return actualWorkHours != null ?
                actualWorkHours.divide(BigDecimal.valueOf(actualWorkDays), 2, BigDecimal.ROUND_HALF_UP) :
                BigDecimal.ZERO;
    }

    /**
     * 检查绩效是否良好
     *
     * @return 是否绩效良好(得分>=80)
     */
    public boolean isGoodPerformance() {
        return performanceScore != null && performanceScore.compareTo(BigDecimal.valueOf(80)) >= 0;
    }

    /**
     * 检查是否存在风险
     *
     * @return 是否存在风险
     */
    public boolean hasRisk() {
        return "HIGH".equals(riskLevel) || "MEDIUM".equals(riskLevel);
    }

    /**
     * 获取统计类型描述
     *
     * @return 统计类型描述
     */
    public String getStatisticsTypeDescription() {
        if (statisticsType == null) {
            return "未知类型";
        }

        switch (statisticsType) {
            case "DAILY":
                return "日报";
            case "WEEKLY":
                return "周报";
            case "MONTHLY":
                return "月报";
            case "QUARTERLY":
                return "季报";
            case "YEARLY":
                return "年报";
            default:
                return statisticsType;
        }
    }

    /**
     * 获取风险等级描述
     *
     * @return 风险等级描述
     */
    public String getRiskLevelDescription() {
        if (riskLevel == null) {
            return "未知风险";
        }

        switch (riskLevel) {
            case "LOW":
                return "低风险";
            case "MEDIUM":
                return "中风险";
            case "HIGH":
                return "高风险";
            default:
                return riskLevel;
        }
    }
}