package net.lab1024.sa.attendance.leave.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 销假审批请求
 *
 * <p>用于审批销假申请的请求参数。</p>
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveCancellationApprovalRequest {

    private String cancellationId;

    private String approvalStep;
    private Long approverId;
    private String approverName;

    /**
     * 审批结果枚举名（对应 {@code LeaveCancellationApplication.ApprovalResult} 的 valueOf）
     */
    private String approvalResult;

    private String approvalComment;

    private Boolean isProxyApproval;
    private String proxyApproverName;
}
