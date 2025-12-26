package net.lab1024.sa.attendance.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 审批补卡申请请求
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "审批补卡申请请求")
public class ApproveSupplementRequest {

    @NotNull(message = "审批结果不能为空")
    @Schema(description = "审批结果", example = "approved", required = true,
            allowableValues = {"approved", "rejected"})
    private String approveResult;

    @Schema(description = "审批意见", example = "同意补卡申请，证明材料齐全")
    private String approveComment;

    @Schema(description = "拒绝原因", example = "证明材料不足，请重新提供")
    private String rejectReason;

    @Schema(description = "实际打卡时间", example = "2025-12-16T09:05:00")
    private LocalDateTime actualClockTime;

    @Schema(description = "调整打卡时间", example = "2025-12-16T09:05:00")
    private LocalDateTime adjustClockTime;

    @Schema(description = "是否修正考勤状态", example = "true")
    private Boolean correctAttendanceStatus = true;

    @Schema(description = "修正后的考勤状态", example = "normal")
    private String correctedStatus;

    @Schema(description = "是否需要重新计算工时", example = "true")
    private Boolean recalculateWorkHours = true;

    @Schema(description = "调整的工作时长（分钟）", example = "480")
    private Integer adjustWorkMinutes;

    @Schema(description = "是否影响薪资计算", example = "false")
    private Boolean affectSalaryCalculation = false;

    @Schema(description = "是否发送通知", example = "true")
    private Boolean sendNotification = true;

    @Schema(description = "通知内容", example = "您的补卡申请已批准")
    private String notificationMessage;

    @Schema(description = "扩展参数（JSON格式）", example = "{\"key1\":\"value1\"}")
    private String extendedParams;
}
