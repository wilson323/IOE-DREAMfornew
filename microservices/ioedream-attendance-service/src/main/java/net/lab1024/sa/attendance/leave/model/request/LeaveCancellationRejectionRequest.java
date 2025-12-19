package net.lab1024.sa.attendance.leave.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 销假驳回请求
 *
 * <p>用于驳回销假申请的请求参数。</p>
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveCancellationRejectionRequest {

    private String cancellationId;
    private Long approverId;

    private String rejectionReason;
}
