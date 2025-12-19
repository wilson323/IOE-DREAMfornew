package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 移动端今日考勤状态结果
 * <p>
 * 封装移动端今日考勤状态响应结果
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
@Schema(description = "移动端今日考勤状态结果")
public class MobileTodayStatusResult {

    /**
     * 员工ID
     */
    @Schema(description = "员工ID", example = "1001")
    private Long employeeId;

    /**
     * 今日日期（结构化字段）
     */
    @Schema(description = "今日日期", example = "2025-01-30")
    private LocalDate date;

    /**
     * 今日日期
     */
    @Schema(description = "今日日期", example = "2025-01-30")
    private String todayDate;

    /**
     * 上班打卡状态
     */
    @Schema(description = "上班打卡状态", example = "SUCCESS", allowableValues = {"NOT_CLOCKED_IN", "SUCCESS", "LATE"})
    private String clockInStatus;

    /**
     * 上班打卡时间
     */
    @Schema(description = "上班打卡时间", example = "2025-01-30T09:00:00")
    private LocalDateTime clockInTime;

    /**
     * 下班打卡状态
     */
    @Schema(description = "下班打卡状态", example = "NOT_CLOCKED_OUT", allowableValues = {"NOT_CLOCKED_OUT", "SUCCESS", "EARLY_LEAVE"})
    private String clockOutStatus;

    /**
     * 下班打卡时间
     */
    @Schema(description = "下班打卡时间", example = "2025-01-30T18:00:00")
    private LocalDateTime clockOutTime;

    /**
     * 工作时长（小时）
     */
    @Schema(description = "工作时长（小时）", example = "8.0")
    private Double workHours;

    /**
     * 考勤状态
     */
    @Schema(description = "考勤状态", example = "NORMAL", allowableValues = {"NORMAL", "LATE", "EARLY_LEAVE", "ABSENT"})
    private String attendanceStatus;

    /**
     * 当前班次信息
     */
    @Schema(description = "当前班次信息")
    private WorkShiftInfo currentShift;
}

