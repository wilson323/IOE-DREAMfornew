package net.lab1024.sa.visitor.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批响应
 *
 * @author IOE-DREAM团队
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@Schema(description = "审批响应")
public class ApprovalResponse {

    @Schema(description = "预约ID")
    private Long appointmentId;

    @Schema(description = "审批结果")
    private String approvalResult;

    @Schema(description = "审批结果描述")
    private String approvalResultDesc;

    @Schema(description = "审批意见")
    private String approvalComments;

    @Schema(description = "审批人姓名")
    private String approverName;

    @Schema(description = "审批时间")
    private LocalDateTime approvalTime;

    @Schema(description = "处理结果")
    private Boolean success;

    @Schema(description = "处理消息")
    private String message;
}
