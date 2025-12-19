package net.lab1024.sa.attendance.report.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 员工考勤明细报表
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDetailReport {

    private String reportId;

    private String reportName;

    private Long employeeId;

    private String employeeName;

    private LocalDate startDate;

    private LocalDate endDate;

    private List<EmployeeAttendanceDetail> details;

    private EmployeeReportSummary summary;

    private LocalDateTime generatedTime;

    private long generationTimeMs;
}

