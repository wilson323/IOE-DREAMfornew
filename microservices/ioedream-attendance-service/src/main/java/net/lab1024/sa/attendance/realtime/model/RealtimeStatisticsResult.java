package net.lab1024.sa.attendance.realtime.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 实时统计结果（简化）
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealtimeStatisticsResult {
    private String queryId;
    private LocalDateTime queryTime;
    private StatisticsQueryParameters queryParameters;

    private boolean querySuccessful;
    private String errorMessage;

    private Map<String, Object> employeeStatistics;
    private Map<String, Object> departmentStatistics;
    private Map<String, Object> companyStatistics;
    private EnginePerformanceMetrics performanceMetrics;
}
