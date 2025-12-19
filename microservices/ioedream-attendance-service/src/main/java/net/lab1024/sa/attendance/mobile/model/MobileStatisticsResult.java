package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 移动端考勤统计结果
 * <p>
 * 封装移动端考勤统计响应结果
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
@Schema(description = "移动端考勤统计结果")
public class MobileStatisticsResult {

    /**
     * 员工ID
     */
    @Schema(description = "员工ID", example = "1001")
    private Long employeeId;

    /**
     * 开始日期
     */
    @Schema(description = "开始日期", example = "2025-01-01")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @Schema(description = "结束日期", example = "2025-01-31")
    private LocalDate endDate;

    /**
     * 总工作日
     */
    @Schema(description = "总工作日", example = "22")
    private Integer totalWorkDays;

    /**
     * 出勤天数
     */
    @Schema(description = "出勤天数", example = "20")
    private Integer attendanceDays;

    /**
     * 请假天数
     */
    @Schema(description = "请假天数", example = "2")
    private Integer leaveDays;

    /**
     * 迟到天数
     */
    @Schema(description = "迟到天数", example = "1")
    private Integer lateDays;

    /**
     * 早退天数
     */
    @Schema(description = "早退天数", example = "0")
    private Integer earlyLeaveDays;

    /**
     * 加班时长（小时）
     */
    @Schema(description = "加班时长（小时）", example = "10.5")
    private Double overtimeHours;
}
