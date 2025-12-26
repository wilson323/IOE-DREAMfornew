package net.lab1024.sa.common.entity.attendance;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤出差实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@TableName("t_attendance_travel")
@Schema(description = "考勤出差")
public class AttendanceTravelEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "出差ID")
    private Long travelId;

    @Schema(description = "出差申请编号")
    private String travelNo;

    @Schema(description = "员工ID")
    private Long employeeId;

    @Schema(description = "出差开始日期")
    private LocalDate startDate;

    @Schema(description = "出差结束日期")
    private LocalDate endDate;

    @Schema(description = "出差天数")
    private Double travelDays;

    @Schema(description = "出差地点")
    private String location;

    @Schema(description = "出差原因")
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
