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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 排班记录实体
 * 存储员工排班安排的详细信息
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_attendance_schedule_record")
@Schema(description = "排班记录实体")
public class ScheduleRecordEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "排班记录ID", example = "1001")
    private Long scheduleId;

    @NotNull
    @Schema(description = "员工ID", example = "1001")
    private Long employeeId;

    @NotNull
    @Schema(description = "排班日期", example = "2025-01-30")
    private LocalDate scheduleDate;

    @NotNull
    @Schema(description = "班次ID", example = "101")
    private Long shiftId;

    @NotBlank
    @Size(max = 50)
    @Schema(description = "排班类型", example = "正常排班")
    private String scheduleType;

    @Schema(description = "是否临时排班", example = "false")
    private Boolean isTemporary;

    @Size(max = 500)
    @Schema(description = "排班原因", example = "项目需要")
    private String reason;

    @NotNull
    @Schema(description = "状态", example = "1", allowableValues = {"0", "1"})
    private Integer status;

    @Schema(description = "工作时长(小时)", example = "8.0")
    private Double workHours;

    @Schema(description = "是否跨天", example = "false")
    private Boolean isOvernight;

    @Schema(description = "实际开始时间", example = "2025-01-30T09:00:00")
    private LocalDateTime actualStartTime;

    @Schema(description = "实际结束时间", example = "2025-01-30T18:00:00")
    private LocalDateTime actualEndTime;

    @Schema(description = "加班时长(小时)", example = "2.0")
    private Double overtimeHours;

    @Schema(description = "优先级", example = "1")
    private Integer priority;

    @Schema(description = "创建人ID", example = "2001")
    private Long createUserId;

    @Schema(description = "更新人ID", example = "2002")
    private Long updateUserId;

    // 枚举定义
    public enum ScheduleType {
        NORMAL("正常排班"),
        TEMPORARY("临时调班"),
        OVERTIME("加班排班"),
        HOLIDAY("节假日排班"),
        SPECIAL("特殊排班");

        private final String description;

        ScheduleType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum ScheduleStatus {
        CANCELLED(0, "取消"),
        NORMAL(1, "正常"),
        COMPLETED(2, "已完成"),
        ABSENT(3, "缺勤");

        private final int code;
        private final String description;

        ScheduleStatus(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static ScheduleStatus fromCode(int code) {
            for (ScheduleStatus status : values()) {
                if (status.code == code) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid schedule status code: " + code);
        }
    }
}