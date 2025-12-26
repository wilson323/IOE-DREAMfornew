package net.lab1024.sa.common.entity.attendance;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 排班记录实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@TableName("t_attendance_schedule_record")
@Schema(description = "排班记录")
public class ScheduleRecordEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "排班ID")
    private Long scheduleId;

    @Schema(description = "员工ID")
    private Long employeeId;

    @Schema(description = "排班日期")
    private LocalDate scheduleDate;

    @Schema(description = "班次ID")
    private Long shiftId;

    @Schema(description = "排班类型：1-正常排班 2-临时排班 3-调班")
    private Integer scheduleType;

    @Schema(description = "是否临时排班：0-否 1-是")
    private Integer isTemporary;

    @Schema(description = "原因")
    private String reason;

    @Schema(description = "状态：1-生效中 2-已完成 3-已取消")
    private Integer status;

    @Schema(description = "工作时长（小时）")
    private Double workHours;

    @Schema(description = "创建人ID")
    private Long createUserId;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @Schema(description = "删除标记：0-未删除 1-已删除")
    private Integer deletedFlag;
}
