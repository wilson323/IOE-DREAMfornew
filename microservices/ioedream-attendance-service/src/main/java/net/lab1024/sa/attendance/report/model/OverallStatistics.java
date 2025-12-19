package net.lab1024.sa.attendance.report.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 总体统计数据
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverallStatistics {

    private int totalDepartments;

    private double overallAttendanceRate;

    private double overallPunctualityRate;

    private double overallProductivityRate;
}

