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
 * 部门考勤看板视图对象
 * <p>
 * 用于部门考勤数据展示
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
@Schema(description = "部门考勤看板视图对象")
public class DashboardDepartmentVO {

    /**
     * 部门ID
     */
    @Schema(description = "部门ID", example = "10")
    private Long departmentId;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    /**
     * 部门总人数
     */
    @Schema(description = "部门总人数", example = "50")
    private Integer totalCount;

    /**
     * 今日出勤人数
     */
    @Schema(description = "今日出勤人数", example = "48")
    private Integer todayPresentCount;

    /**
     * 今日缺勤人数
     */
    @Schema(description = "今日缺勤人数", example = "2")
    private Integer todayAbsentCount;

    /**
     * 今日迟到人数
     */
    @Schema(description = "今日迟到人数", example = "5")
    private Integer todayLateCount;

    /**
     * 今日早退人数
     */
    @Schema(description = "今日早退人数", example = "2")
    private Integer todayEarlyCount;

    /**
     * 今日出勤率（百分比）
     */
    @Schema(description = "今日出勤率（百分比）", example = "96.0")
    private BigDecimal todayAttendanceRate;

    /**
     * 本月平均出勤率（百分比）
     */
    @Schema(description = "本月平均出勤率（百分比）", example = "94.5")
    private BigDecimal monthAttendanceRate;

    /**
     * 本月平均工作时长（小时）
     */
    @Schema(description = "本月平均工作时长（小时）", example = "8.5")
    private BigDecimal avgWorkHours;

    /**
     * 本月总加班时长（小时）
     */
    @Schema(description = "本月总加班时长（小时）", example = "120.5")
    private BigDecimal totalOvertimeHours;

    /**
     * 本月人均加班时长（小时）
     */
    @Schema(description = "本月人均加班时长（小时）", example = "2.5")
    private BigDecimal avgOvertimeHours;

    /**
     * 待审批请假数
     */
    @Schema(description = "待审批请假数", example = "3")
    private Integer pendingLeaveCount;

    /**
     * 待审批补签数
     */
    @Schema(description = "待审批补签数", example = "2")
    private Integer pendingSupplementCount;

    /**
     * 近7天出勤趋势
     */
    @Schema(description = "近7天出勤趋势")
    private List<Map<String, Object>> last7DaysTrend;

    /**
     * 部门员工考勤排行TOP10
     */
    @Schema(description = "部门员工考勤排行TOP10")
    private List<Map<String, Object>> topEmployees;

    /**
     * 出勤异常员工列表
     */
    @Schema(description = "出勤异常员工列表")
    private List<Map<String, Object>> abnormalEmployees;
}
