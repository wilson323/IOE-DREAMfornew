package net.lab1024.sa.attendance.report.model.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.attendance.report.model.AttendanceSummaryReport;

/**
 * 报表详情结果（占位）
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDetailResult {
    private boolean success;
    private AttendanceSummaryReport report;
    private String message;
    private String errorMessage;
    private String errorCode;
}
