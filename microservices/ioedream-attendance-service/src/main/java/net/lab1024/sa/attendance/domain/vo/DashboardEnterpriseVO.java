package net.lab1024.sa.attendance.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 企业考勤看板视图对象
 * <p>
 * 用于企业级考勤数据展示
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "企业考勤看板视图对象")
public class DashboardEnterpriseVO {

    /**
     * 企业总人数
     */
    @Schema(description = "企业总人数", example = "500")
    private Integer totalEmployees;

    /**
     * 部门数量
     */
    @Schema(description = "部门数量", example = "10")
    private Integer departmentCount;

    /**
     * 今日出勤人数
     */
    @Schema(description = "今日出勤人数", example = "480")
    private Integer todayPresentCount;

    /**
     * 今日缺勤人数
     */
    @Schema(description = "今日缺勤人数", example = "20")
    private Integer todayAbsentCount;

    /**
     * 今日出勤率（百分比）
     */
    @Schema(description = "今日出勤率（百分比）", example = "96.0")
    private BigDecimal todayAttendanceRate;

    /**
     * 本月平均出勤率（百分比）
     */
    @Schema(description = "本月平均出勤率（百分比）", example = "94.8")
    private BigDecimal monthAttendanceRate;

    /**
     * 本月总加班时长（小时）
     */
    @Schema(description = "本月总加班时长（小时）", example = "1200.5")
    private BigDecimal totalOvertimeHours;

    /**
     * 本月人均加班时长（小时）
     */
    @Schema(description = "本月人均加班时长（小时）", example = "2.5")
    private BigDecimal avgOvertimeHours;

    /**
     * 待审批异常总数
     */
    @Schema(description = "待审批异常总数", example = "25")
    private Integer pendingApprovalCount;

    /**
     * 部门考勤排行
     */
    @Schema(description = "部门考勤排行")
    private List<Map<String, Object>> departmentRanking;

    /**
     * 出勤率最低的部门TOP5
     */
    @Schema(description = "出勤率最低的部门TOP5")
    private List<Map<String, Object>> lowRateDepartments;

    /**
     * 近30天企业出勤趋势
     */
    @Schema(description = "近30天企业出勤趋势")
    private List<Map<String, Object>> last30DaysTrend;

    /**
     * 近12月出勤趋势
     */
    @Schema(description = "近12月出勤趋势")
    private List<Map<String, Object>> last12MonthsTrend;

    /**
     * 部门出勤率分布
     */
    @Schema(description = "部门出勤率分布")
    private Map<String, Object> departmentRateDistribution;

    /**
     * 考勤异常统计
     */
    @Schema(description = "考勤异常统计")
    private Map<String, Object> exceptionStatistics;
}
