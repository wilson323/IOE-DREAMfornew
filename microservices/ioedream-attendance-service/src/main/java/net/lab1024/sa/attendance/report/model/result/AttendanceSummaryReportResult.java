package net.lab1024.sa.attendance.report.model.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.attendance.report.model.AttendanceSummaryReport;

/**
 * 汇总报表结果
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSummaryReportResult {

    private boolean success;

    private AttendanceSummaryReport report;

    private String message;

    private long generationTime;

    private boolean cacheHit;

    private String errorMessage;

    private String errorCode;
}

