package net.lab1024.sa.attendance.strategy.model;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工时时段模型
 * <p>
 * 表示一个连续的工作时段
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "工时时段")
public class WorkTimeSpan {

    @Schema(description = "时段ID", example = "1")
    private Integer spanId;

    @Schema(description = "开始时间", example = "2025-01-30 09:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2025-01-30 18:00:00")
    private LocalDateTime endTime;

    @Schema(description = "工作时长(分钟)", example = "540")
    private Integer workMinutes;

    @Schema(description = "休息时长(分钟)", example = "60")
    private Integer breakMinutes;

    @Schema(description = "是否有效时段", example = "true")
    private Boolean isValid;

    @Schema(description = "时段类型", example = "NORMAL", allowableValues = {"NORMAL", "OVERTIME", "BREAK", "FLEXIBLE"})
    private TimeSpanType timeSpanType;

    @Schema(description = "备注", example = "正常工作时段")
    private String remarks;

    /**
     * 时段类型枚举
     */
    public enum TimeSpanType {
        NORMAL("正常工作", "NORMAL"),
        OVERTIME("加班", "OVERTIME"),
        BREAK("休息", "BREAK"),
        FLEXIBLE("弹性", "FLEXIBLE");

        private final String description;
        private final String code;

        TimeSpanType(String description, String code) {
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
     * 计算工作时长（分钟）
     *
     * @return 工作时长（分钟）
     */
    public Integer calculateWorkMinutes() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        long minutes = java.time.Duration.between(startTime, endTime).toMinutes();
        return (int) minutes;
    }

    /**
     * 计算净工作时长（扣除休息时间）
     *
     * @return 净工作时长（分钟）
     */
    public Integer calculateNetWorkMinutes() {
        int workMinutes = calculateWorkMinutes();
        int breakMinutes = this.breakMinutes != null ? this.breakMinutes : 0;
        return Math.max(0, workMinutes - breakMinutes);
    }

    /**
     * 判断是否为有效时段
     *
     * @return 是否有效
     */
    public boolean isValidSpan() {
        return Boolean.TRUE.equals(isValid) && startTime != null && endTime != null && endTime.isAfter(startTime);
    }

    /**
     * 判断是否跨天
     *
     * @return 是否跨天
     */
    public boolean isOvernight() {
        if (startTime == null || endTime == null) {
            return false;
        }
        return endTime.toLocalDate().isAfter(startTime.toLocalDate());
    }

    /**
     * 判断是否包含指定时间
     *
     * @param time 指定时间
     * @return 是否包含
     */
    public boolean containsTime(LocalDateTime time) {
        if (time == null || startTime == null || endTime == null) {
            return false;
        }
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }
}
