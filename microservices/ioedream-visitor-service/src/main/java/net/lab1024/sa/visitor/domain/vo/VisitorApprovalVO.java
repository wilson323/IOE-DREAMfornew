package net.lab1024.sa.visitor.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 访客审批VO
 */
@Data
@Schema(description = "访客审批VO")
public class VisitorApprovalVO {

    @Schema(description = "访客ID")
    private Long visitorId;

    @Schema(description = "是否通过")
    private Boolean approved;

    @Schema(description = "审批人ID")
    private Long approverId;

    @Schema(description = "审批意见")
    private String approvalComment;
}
