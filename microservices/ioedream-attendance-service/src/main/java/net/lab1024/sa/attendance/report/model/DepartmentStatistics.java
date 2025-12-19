package net.lab1024.sa.attendance.report.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 部门统计数据
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentStatistics {

    private Long departmentId;

    private String departmentName;

    private int employeeCount;

    private double attendanceRate;

    private double punctualityRate;

    private double productivityRate;
}

