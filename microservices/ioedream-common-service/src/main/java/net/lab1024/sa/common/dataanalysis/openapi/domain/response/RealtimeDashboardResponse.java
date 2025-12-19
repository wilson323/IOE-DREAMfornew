package net.lab1024.sa.common.dataanalysis.openapi.domain.response;

import lombok.Data;
import java.util.Map;

/**
 * 实时仪表板响应
 */
@Data
public class RealtimeDashboardResponse {
    private String dashboardType;
    private Map<String, Object> metrics;
    private Long lastUpdated;
}
