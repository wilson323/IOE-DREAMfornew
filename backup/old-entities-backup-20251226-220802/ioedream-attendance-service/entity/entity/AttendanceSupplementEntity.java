package net.lab1024.sa.attendance.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤补卡实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@TableName("t_attendance_supplement")
@Schema(description = "考勤补卡")
public class AttendanceSupplementEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "补卡ID")
    private Long supplementId;

    @Schema(description = "补卡申请编号")
    private String supplementNo;

    @Schema(description = "员工ID")
    private Long employeeId;

    @Schema(description = "补卡日期")
    private LocalDate supplementDate;

    @Schema(description = "补卡时间")
    private String supplementTime;

    @Schema(description = "补卡类型：1-上班打卡 2-下班打卡")
    private Integer supplementType;

    @Schema(description = "补卡原因")
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
