package net.lab1024.sa.attendance.leave.model.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 自动审批销假请求
 *
 * <p>用于触发自动审批策略。</p>
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoLeaveCancellationApprovalRequest {

    private List<String> cancellationIds;
    private String policyCode;
    private Long operatorId;
}
