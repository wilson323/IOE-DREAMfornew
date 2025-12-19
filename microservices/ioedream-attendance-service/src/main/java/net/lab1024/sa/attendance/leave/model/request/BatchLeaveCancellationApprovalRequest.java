package net.lab1024.sa.attendance.leave.model.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量销假审批请求
 *
 * <p>用于批量审批销假申请。</p>
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchLeaveCancellationApprovalRequest {

    private List<String> cancellationIds;

    private Long approverId;
    private String approverName;

    /**
     * 审批结果枚举名
     */
    private String approvalResult;

    private String approvalComment;
}
