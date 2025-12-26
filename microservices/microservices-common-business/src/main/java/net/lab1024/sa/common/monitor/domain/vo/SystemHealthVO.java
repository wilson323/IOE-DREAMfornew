package net.lab1024.sa.common.monitor.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 系统健康状态VO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemHealthVO {

    private String overallStatus;
    private List<Map<String, Object>> componentHealth;
    private List<Map<String, Object>> microservicesHealth;
    private Map<String, Object> databaseHealth;
    private Map<String, Object> cacheHealth;

    // 扩展字段
    private Integer healthScore;
    private Long systemUptime;
    private Integer totalServices;
    private Integer onlineServices;
    private Integer offlineServices;
    private Integer activeAlerts;
    private Integer criticalAlerts;
    private Integer warningAlerts;
    private Double cpuUsage;
    private Double memoryUsage;
    private Double diskUsage;
    private ResourceUsageVO resourceUsage;
    private List<ComponentHealthVO> componentHealthList;
    private Map<String, Object> performanceMetrics;
    private LocalDateTime lastCheckTime;
    private List<Map<String, Object>> healthTrends;
    private List<AlertSummaryVO> activeAlertList;
}

