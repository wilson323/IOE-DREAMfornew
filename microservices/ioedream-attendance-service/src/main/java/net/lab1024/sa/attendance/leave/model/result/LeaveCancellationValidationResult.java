package net.lab1024.sa.attendance.leave.model.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 销假校验结果（占位）
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveCancellationValidationResult {
    private boolean success;
    private String message;
    private String errorMessage;
    private String errorCode;
}

