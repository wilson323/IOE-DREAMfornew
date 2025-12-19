package net.lab1024.sa.attendance.report.model.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.attendance.report.model.DepartmentStatisticsReport;

/**
 * 部门统计报表结果
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentStatisticsReportResult {

    private boolean success;

    private DepartmentStatisticsReport report;

    private String message;

    private int totalDepartments;

    private long generationTime;

    private String errorMessage;

    private String errorCode;
}

