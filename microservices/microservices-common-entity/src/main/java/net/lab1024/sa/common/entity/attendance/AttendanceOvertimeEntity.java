package net.lab1024.sa.common.entity.attendance;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤加班实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@TableName("t_attendance_overtime")
@Schema(description = "考勤加班")
public class AttendanceOvertimeEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "加班ID")
    private Long overtimeId;

    @Schema(description = "加班申请编号")
    private String overtimeNo;

    @Schema(description = "员工ID")
    private Long employeeId;

    @Schema(description = "加班开始日期")
    private LocalDate startDate;

    @Schema(description = "加班开始时间")
    private String startTime;

    @Schema(description = "加班结束日期")
    private LocalDate endDate;

    @Schema(description = "加班结束时间")
    private String endTime;

    @Schema(description = "加班时长（小时）")
    private Double overtimeHours;

    @Schema(description = "加班原因")
    private String reason;

    @Schema(description = "审批状态：0-待审批 1-已通过 2-已拒绝 3-已撤销")
    private String approvalStatus;

    @Schema(description = "审批意见")
    private String approvalComment;

    @Schema(description = "工作流实例ID")
    private String workflowInstanceId;

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
