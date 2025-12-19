package net.lab1024.sa.attendance.realtime.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实时统计查询参数（简化）
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsQueryParameters {
    private StatisticsType statisticsType;

    public enum StatisticsType {
        EMPLOYEE_REALTIME,
        DEPARTMENT_REALTIME,
        COMPANY_REALTIME,
        PERFORMANCE_METRICS
    }
}

