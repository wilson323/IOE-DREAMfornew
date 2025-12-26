package net.lab1024.sa.attendance.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 仪表中心首页概览视图对象
 * <p>
 * 用于首页仪表中心展示
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
@Schema(description = "仪表中心首页概览视图对象")
public class DashboardOverviewVO {

    /**
     * 今日打卡人数
     */
    @Schema(description = "今日打卡人数", example = "120")
    private Integer todayPunchCount;

    /**
     * 今日出勤人数
     */
    @Schema(description = "今日出勤人数", example = "115")
    private Integer todayPresentCount;

    /**
     * 今日迟到人数
     */
    @Schema(description = "今日迟到人数", example = "8")
    private Integer todayLateCount;

    /**
     * 今日早退人数
     */
    @Schema(description = "今日早退人数", example = "3")
    private Integer todayEarlyCount;

    /**
     * 今日缺勤人数
     */
    @Schema(description = "今日缺勤人数", example = "5")
    private Integer todayAbsentCount;

    /**
     * 今日出勤率（百分比）
     */
    @Schema(description = "今日出勤率（百分比）", example = "95.8")
    private BigDecimal todayAttendanceRate;

    /**
     * 本月累计工作天数
     */
    @Schema(description = "本月累计工作天数", example = "22")
    private Integer monthWorkDays;

    /**
     * 本月平均出勤率（百分比）
     */
    @Schema(description = "本月平均出勤率（百分比）", example = "94.5")
    private BigDecimal monthAttendanceRate;

    /**
     * 待审批异常数
     */
    @Schema(description = "待审批异常数", example = "12")
    private Integer pendingApprovalCount;

    /**
     * 待处理补签数
     */
    @Schema(description = "待处理补签数", example = "5")
    private Integer pendingSupplementCount;

    /**
     * 待处理请假数
     */
    @Schema(description = "待处理请假数", example = "8")
    private Integer pendingLeaveCount;

    /**
     * 部门数量
     */
    @Schema(description = "部门数量", example = "10")
    private Integer departmentCount;

    /**
     * 员工总数
     */
    @Schema(description = "员工总数", example = "500")
    private Integer totalEmployees;

    /**
     * 今日打卡统计（按部门）
     */
    @Schema(description = "今日打卡统计（按部门）")
    private Map<String, Integer> departmentPunchStats;

    /**
     * 本周考勤趋势数据
     */
    @Schema(description = "本周考勤趋势数据")
    private Map<String, Object> weeklyTrend;

    /**
     * 本月考勤趋势数据
     */
    @Schema(description = "本月考勤趋势数据")
    private Map<String, Object> monthlyTrend;

    /**
     * 近7天出勤率变化
     */
    @Schema(description = "近7天出勤率变化")
    private java.util.List<BigDecimal> last7DaysRate;
}
