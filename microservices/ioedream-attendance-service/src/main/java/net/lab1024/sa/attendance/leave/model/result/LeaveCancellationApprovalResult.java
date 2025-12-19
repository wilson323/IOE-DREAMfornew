package net.lab1024.sa.attendance.leave.model.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 销假审批结果
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveCancellationApprovalResult {
    private boolean success;
    private String cancellationId;
    private String newStatus;
    private String message;
    private LocalDateTime approvalTime;
    private String errorMessage;
    private String errorCode;
}

