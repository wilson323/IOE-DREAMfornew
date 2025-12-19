package net.lab1024.sa.attendance.leave.model.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 销假申请结果
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveCancellationApplicationResult {
    private boolean success;
    private String cancellationId;
    private String message;
    private LocalDateTime applicationTime;
    private LocalDateTime estimatedApprovalTime;
    private String errorMessage;
    private String errorCode;
}

