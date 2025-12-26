package net.lab1024.sa.attendance.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤结果视图对象
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 用于考勤处理模板方法的输出结果
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Getter
@Setter
@Data
@Schema(description = "考勤结果视图对象")
public class AttendanceResultVO {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 考勤日期
     */
    @Schema(description = "考勤日期", example = "2025-01-30")
    private LocalDate date;

    /**
     * 考勤状态（NORMAL-正常，LATE-迟到，EARLY_LEAVE-早退，ABSENT-缺勤等）
     */
    @Schema(description = "考勤状态", example = "NORMAL")
    private String status;

    /**
     * 迟到时长（分钟）
     */
    @Schema(description = "迟到时长（分钟）", example = "15")
    private Long lateDuration;

    /**
     * 早退时长（分钟）
     */
    @Schema(description = "早退时长（分钟）", example = "30")
    private Long earlyDuration;

    /**
     * 加班时长（分钟）
     */
    @Schema(description = "加班时长（分钟）", example = "60")
    private Long overtimeDuration;

    /**
     * 工作时长（分钟）
     */
    @Schema(description = "工作时长（分钟）", example = "480")
    private Long workingMinutes;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "正常出勤")
    private String remark;
}
