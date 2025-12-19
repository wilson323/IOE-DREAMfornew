package net.lab1024.sa.attendance.report.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.attendance.report.model.AttendanceSummaryReport;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤汇总报表请求
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSummaryReportRequest {

    private String reportName;

    private AttendanceSummaryReport.ReportType reportType;

    private LocalDate startDate;

    private LocalDate endDate;

    private List<Long> departmentIds;

    private Boolean includeDepartmentSummary;

    private Boolean includeTrendAnalysis;

    private Long generatedBy;

    private String generatedByName;
}
