package net.lab1024.sa.attendance.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 个人考勤看板视图对象
 * <p>
 * 用于个人考勤数据展示
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
@Schema(description = "个人考勤看板视图对象")
public class DashboardPersonalVO {

    /**
     * 员工ID
     */
    @Schema(description = "员工ID", example = "1001")
    private Long employeeId;

    /**
     * 员工姓名
     */
    @Schema(description = "员工姓名", example = "张三")
    private String employeeName;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    /**
     * 今日打卡状态
     */
    @Schema(description = "今日打卡状态", example = "NORMAL")
    private String todayStatus;

    /**
     * 今日上班打卡时间
     */
    @Schema(description = "今日上班打卡时间", example = "2025-12-23T08:55:00")
    private LocalDateTime todayClockInTime;

    /**
     * 今日下班打卡时间
     */
    @Schema(description = "今日下班打卡时间", example = "2025-12-23T18:05:00")
    private LocalDateTime todayClockOutTime;

    /**
     * 本月出勤天数
     */
    @Schema(description = "本月出勤天数", example = "18")
    private Integer monthActualDays;

    /**
     * 本月迟到次数
     */
    @Schema(description = "本月迟到次数", example = "2")
    private Integer monthLateCount;

    /**
     * 本月早退次数
     */
    @Schema(description = "本月早退次数", example = "1")
    private Integer monthEarlyCount;

    /**
     * 本月缺勤天数
     */
    @Schema(description = "本月缺勤天数", example = "1")
    private Integer monthAbsentDays;

    /**
     * 本月加班时长（小时）
     */
    @Schema(description = "本月加班时长（小时）", example = "12.5")
    private BigDecimal monthOvertimeHours;

    /**
     * 本月出勤率（百分比）
     */
    @Schema(description = "本月出勤率（百分比）", example = "90.5")
    private BigDecimal monthAttendanceRate;

    /**
     * 本年累计出勤天数
     */
    @Schema(description = "本年累计出勤天数", example = "220")
    private Integer yearActualDays;

    /**
     * 本年累计加班时长（小时）
     */
    @Schema(description = "本年累计加班时长（小时）", example = "150.5")
    private BigDecimal yearOvertimeHours;

    /**
     * 近7天考勤记录
     */
    @Schema(description = "近7天考勤记录")
    private List<Map<String, Object>> last7DaysRecords;

    /**
     * 近30天出勤趋势
     */
    @Schema(description = "近30天出勤趋势")
    private List<Map<String, Object>> last30DaysTrend;

    /**
     * 待处理异常
     */
    @Schema(description = "待处理异常")
    private List<Map<String, Object>> pendingExceptions;

    /**
     * 快速操作权限
     */
    @Schema(description = "快速操作权限")
    private Map<String, Boolean> quickActionPermissions;

    /**
     * 下次排班信息
     */
    @Schema(description = "下次排班信息")
    private Map<String, Object> nextSchedule;
}
