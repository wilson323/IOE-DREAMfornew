package net.lab1024.sa.attendance.report.model.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 参数验证结果（占位）
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportParameterValidationResult {
    private boolean success;
    private String message;
    private String errorMessage;
    private String errorCode;
}

