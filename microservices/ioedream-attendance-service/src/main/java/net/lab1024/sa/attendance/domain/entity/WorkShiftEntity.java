package net.lab1024.sa.attendance.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import net.lab1024.sa.common.entity.BaseEntity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * 班次配置实体
 * 存储班次的详细配置信息
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_attendance_work_shift")
@Schema(description = "班次配置实体")
public class WorkShiftEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "班次ID", example = "101")
    private Long shiftId;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "班次名称", example = "正常班")
    private String shiftName;

    @NotNull
    @Schema(description = "班次类型", example = "1", allowableValues = {"1", "2", "3"})
    private Integer shiftType;

    @NotNull
    @Schema(description = "开始时间", example = "09:00:00")
    private LocalTime startTime;

    @NotNull
    @Schema(description = "结束时间", example = "18:00:00")
    private LocalTime endTime;

    @Schema(description = "工作时长(小时)", example = "8.0")
    private Double workHours;

    @Schema(description = "休息时长(分钟)", example = "60")
    private Integer breakMinutes;

    @Schema(description = "休息开始时间", example = "12:00:00")
    private LocalTime breakStartTime;

    @Schema(description = "休息结束时间", example = "13:00:00")
    private LocalTime breakEndTime;

    @Schema(description = "是否跨天", example = "false")
    private Boolean isOvernight;

    @Schema(description = "是否弹性时间", example = "false")
    private Boolean isFlexible;

    @Schema(description = "弹性开始时间(分钟)", example = "30")
    private Integer flexibleStartTime;

    @Schema(description = "弹性结束时间(分钟)", example = "30")
    private Integer flexibleEndTime;

    @Schema(description = "加班计算开始时间", example = "18:00:00")
    private LocalTime overtimeStartTime;

    @Schema(description = "最小加班时长(分钟)", example = "30")
    private Integer minOvertimeMinutes;

    @Schema(description = "颜色标识", example = "#FF5722")
    private String colorCode;

    @Schema(description = "备注", example = "正常工作日班次")
    private String remarks;

    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    // 枚举定义
    public enum ShiftType {
        DAY_SHIFT(1, "白班"),
        NIGHT_SHIFT(2, "夜班"),
        ROTATING_SHIFT(3, "轮班"),
        FLEXIBLE_SHIFT(4, "弹性班"),
        PART_TIME_SHIFT(5, "兼职班"),
        SPECIAL_SHIFT(6, "特殊班");

        private final int code;
        private final String description;

        ShiftType(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static ShiftType fromCode(int code) {
            for (ShiftType type : values()) {
                if (type.code == code) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid shift type code: " + code);
        }
    }
}