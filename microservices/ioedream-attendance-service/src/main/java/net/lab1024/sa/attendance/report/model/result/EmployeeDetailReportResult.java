package net.lab1024.sa.attendance.report.model.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.attendance.report.model.EmployeeDetailReport;

/**
 * 员工明细报表结果
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDetailReportResult {

    private boolean success;

    private EmployeeDetailReport report;

    private String message;

    private int totalRecords;

    private long generationTime;

    private String errorMessage;

    private String errorCode;
}

