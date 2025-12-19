package net.lab1024.sa.visitor.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 审批请求
 *
 * @author IOE-DREAM团队
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@Schema(description = "审批请求")
public class ApprovalRequest {

    @Schema(description = "预约ID", required = true)
    @NotNull(message = "预约ID不能为空")
    private Long appointmentId;

    @Schema(description = "审批结果", required = true, allowableValues = {"APPROVED", "REJECTED"})
    @NotNull(message = "审批结果不能为空")
    private String approvalResult;

    @Schema(description = "审批意见")
    private String approvalComments;

    @Schema(description = "审批人ID")
    private Long approverId;

    @Schema(description = "审批人姓名")
    private String approverName;

    /**
     * 兼容历史字段名：approveResult
     *
     * @return 审批结果
     */
    public String getApproveResult() {
        return this.approvalResult;
    }
}
