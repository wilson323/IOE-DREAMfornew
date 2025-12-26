package net.lab1024.sa.attendance.strategy.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工时计算结果
 * <p>
 * 封装工时计算的所有结果数据
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "工时计算结果")
public class CalculateResult {

    @Schema(description = "员工ID", example = "1001")
    private Long employeeId;

    @Schema(description = "考勤日期", example = "2025-01-30")
    private LocalDate attendanceDate;

    @Schema(description = "排班ID", example = "10001")
    private Long scheduleId;

    @Schema(description = "班次ID", example = "101")
    private Long shiftId;

    @Schema(description = "班次名称", example = "正常班")
    private String shiftName;

    @Schema(description = "实际工作时长(小时)", example = "8.5")
    private Double actualWorkHours;

    @Schema(description = "标准工作时长(小时)", example = "8.0")
    private Double standardWorkHours;

    @Schema(description = "迟到时长(分钟)", example = "15")
    private Integer lateMinutes;

    @Schema(description = "早退时长(分钟)", example = "0")
    private Integer earlyLeaveMinutes;

    @Schema(description = "加班时长(小时)", example = "0.5")
    private Double overtimeHours;

    @Schema(description = "工作时段列表")
    private List<WorkTimeSpan> workTimeSpans;

    @Schema(description = "休息时长(分钟)", example = "60")
    private Integer breakMinutes;

    @Schema(description = "核心工作时长(小时)", example = "8.0")
    private Double coreWorkHours;

    @Schema(description = "弹性工作时长(小时)", example = "0.0")
    private Double flexibleWorkHours;

    @Schema(description = "是否跨天班次", example = "false")
    private Boolean isOvernight;

    @Schema(description = "第一次打卡时间", example = "2025-01-30 08:55:00")
    private LocalDateTime firstPunchTime;

    @Schema(description = "最后一次打卡时间", example = "2025-01-30 18:05:00")
    private LocalDateTime lastPunchTime;

    @Schema(description = "打卡次数", example = "4")
    private Integer punchCount;

    @Schema(description = "考勤状态", example = "NORMAL")
    private AttendanceStatus attendanceStatus;

    @Schema(description = "是否有效记录", example = "true")
    private Boolean isValid;

    @Schema(description = "验证错误消息", example = "打卡记录不足")
    private String validationErrorMessage;

    @Schema(description = "计算时间戳", example = "1706592000000")
    private Long calculatedAt;

    /**
     * 考勤状态枚举
     */
    public enum AttendanceStatus {
        NORMAL("正常", "NORMAL"),
        LATE("迟到", "LATE"),
        EARLY_LEAVE("早退", "EARLY_LEAVE"),
        LATE_AND_EARLY_LEAVE("迟到早退", "LATE_AND_EARLY_LEAVE"),
        ABSENT("缺勤", "ABSENT"),
        LEAVE("请假", "LEAVE"),
        OVERTIME("加班", "OVERTIME"),
        HOLIDAY("节假日", "HOLIDAY"),
        WEEKEND("周末", "WEEKEND"),
        INVALID("无效", "INVALID");

        private final String description;
        private final String code;

        AttendanceStatus(String description, String code) {
            this.description = description;
            this.code = code;
        }

        public String getDescription() {
            return description;
        }

        public String getCode() {
            return code;
        }
    }

    /**
     * 计算工作时长（小时）
     *
     * @param minutes 工作时长（分钟）
     * @return 工作时长（小时，保留2位小数）
     */
    public static Double calculateWorkHours(Integer minutes) {
        if (minutes == null || minutes <= 0) {
            return 0.0;
        }
        return BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * 判断是否迟到
     *
     * @return 是否迟到
     */
    public boolean isLate() {
        return lateMinutes != null && lateMinutes > 0;
    }

    /**
     * 判断是否早退
     *
     * @return 是否早退
     */
    public boolean isEarlyLeave() {
        return earlyLeaveMinutes != null && earlyLeaveMinutes > 0;
    }

    /**
     * 判断是否有加班
     *
     * @return 是否有加班
     */
    public boolean hasOvertime() {
        return overtimeHours != null && overtimeHours > 0;
    }

    /**
     * 判断是否有效
     *
     * @return 是否有效（打卡记录完整且符合规则）
     */
    public boolean isValidRecord() {
        return Boolean.TRUE.equals(isValid) && validationErrorMessage == null;
    }
}
