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
 * 考勤统计视图对象
 * <p>
 * 用于考勤统计数据响应
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
@Schema(description = "考勤统计视图对象")
public class AttendanceStatisticsVO {

    /**
     * 统计类型：PERSONAL-个人 DEPARTMENT-部门 COMPANY-公司
     */
    @Schema(description = "统计类型", example = "DEPARTMENT")
    private String statisticsType;

    /**
     * 统计对象ID（员工ID/部门ID）
     */
    @Schema(description = "统计对象ID", example = "10")
    private Long targetId;

    /**
     * 统计对象名称
     */
    @Schema(description = "统计对象名称", example = "技术部")
    private String targetName;

    /**
     * 总人数
     */
    @Schema(description = "总人数", example = "50")
    private Integer totalCount;

    /**
     * 出勤人数
     */
    @Schema(description = "出勤人数", example = "48")
    private Integer attendanceCount;

    /**
     * 缺勤人数
     */
    @Schema(description = "缺勤人数", example = "2")
    private Integer absenceCount;

    /**
     * 出勤率（百分比）
     */
    @Schema(description = "出勤率（百分比）", example = "96.0")
    private BigDecimal attendanceRate;

    /**
     * 平均工作时长（小时）
     */
    @Schema(description = "平均工作时长（小时）", example = "8.5")
    private BigDecimal avgWorkHours;

    /**
     * 迟到总次数
     */
    @Schema(description = "迟到总次数", example = "15")
    private Integer lateCount;

    /**
     * 早退总次数
     */
    @Schema(description = "早退总次数", example = "8")
    private Integer earlyLeaveCount;

    /**
     * 加班总时长（小时）
     */
    @Schema(description = "加班总时长（小时）", example = "120.5")
    private BigDecimal totalOvertimeHours;

    /**
     * 人均加班时长（小时）
     */
    @Schema(description = "人均加班时长（小时）", example = "2.5")
    private BigDecimal avgOvertimeHours;

    /**
     * 每日统计数据
     */
    @Schema(description = "每日统计数据")
    private List<Map<String, Object>> dailyStatistics;

    /**
     * 每周统计数据
     */
    @Schema(description = "每周统计数据")
    private List<Map<String, Object>> weeklyStatistics;

    /**
     * 每月统计数据
     */
    @Schema(description = "每月统计数据")
    private List<Map<String, Object>> monthlyStatistics;
}
