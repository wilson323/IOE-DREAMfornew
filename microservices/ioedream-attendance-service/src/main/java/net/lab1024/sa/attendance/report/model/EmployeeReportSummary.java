package net.lab1024.sa.attendance.report.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 员工报表汇总
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeReportSummary {

    private int totalRecords;

    private int attendanceDays;

    private int presentDays;

    private int absentDays;

    private int lateCount;

    private int earlyLeaveCount;
}

