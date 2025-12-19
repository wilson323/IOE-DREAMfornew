package net.lab1024.sa.common.dataanalysis.openapi.domain.response;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 考勤报表响应
 */
@Data
public class AttendanceReportResponse {
    private Long totalEmployees;
    private Double attendanceRate;
    private List<Map<String, Object>> dailyStats;
}
