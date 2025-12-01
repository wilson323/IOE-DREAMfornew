package net.lab1024.sa.monitor.service.impl;

import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.monitor.dao.AlertDao;
import net.lab1024.sa.monitor.domain.entity.AlertEntity;
import net.lab1024.sa.monitor.domain.vo.AlertSummaryVO;
import net.lab1024.sa.monitor.domain.vo.ComponentHealthVO;
import net.lab1024.sa.monitor.domain.vo.ResourceUsageVO;
import net.lab1024.sa.monitor.domain.vo.SystemHealthVO;
import net.lab1024.sa.monitor.manager.HealthCheckManager;
import net.lab1024.sa.monitor.manager.PerformanceMonitorManager;
import net.lab1024.sa.monitor.manager.SystemMonitorManager;
import net.lab1024.sa.monitor.service.SystemHealthService;

/**
 * 系统健康监控服务实现类
 *
 * @author IOE-DREAM Team
 */
@Slf4j
@Service
public class SystemHealthServiceImpl implements SystemHealthService {

    @Resource
    private HealthCheckManager healthCheckManager;

    @Resource
    private SystemMonitorManager systemMonitorManager;

    @Resource
    private PerformanceMonitorManager performanceMonitorManager;

    @Resource
    private AlertDao alertDao;

    @Override
    public SystemHealthVO getSystemHealthOverview() {
        log.debug("获取系统健康概览");

        SystemHealthVO systemHealth = new SystemHealthVO();

        try {
            // 获取整体健康状态
            String overallStatus = healthCheckManager.calculateOverallHealthStatus();
            systemHealth.setOverallStatus(overallStatus);

            // 计算健康评分
            Integer healthScore = calculateHealthScore(overallStatus);
            systemHealth.setHealthScore(healthScore);

            // 获取系统运行时间
            Map<String, Object> uptimeData = getSystemUptime();
            systemHealth.setSystemUptime((Long) uptimeData.get("uptimeSeconds"));

            // 获取微服务状态
            List<Map<String, Object>> microservicesStatus = getMicroservicesStatus();
            systemHealth.setTotalServices(microservicesStatus.size());
            systemHealth.setOnlineServices((int) microservicesStatus.stream()
                    .mapToLong(service -> "UP".equals(service.get("status")) ? 1 : 0)
                    .sum());
            systemHealth.setOfflineServices(systemHealth.getTotalServices() - systemHealth.getOnlineServices());

            // 获取告警信息
            List<Map<String, Object>> activeAlerts = getActiveAlerts();
            systemHealth.setActiveAlerts(activeAlerts.size());
            systemHealth.setCriticalAlerts((int) activeAlerts.stream()
                    .mapToLong(alert -> "CRITICAL".equals(alert.get("alertLevel")) ? 1 : 0)
                    .sum());
            systemHealth.setWarningAlerts((int) activeAlerts.stream()
                    .mapToLong(alert -> "WARNING".equals(alert.get("alertLevel")) ? 1 : 0)
                    .sum());

            // 获取资源使用情况
            ResourceUsageVO resourceUsage = systemMonitorManager.getResourceUsage();
            systemHealth.setCpuUsage(resourceUsage.getCpuUsage());
            systemHealth.setMemoryUsage(resourceUsage.getMemoryUsage());
            systemHealth.setDiskUsage(resourceUsage.getDiskUsage());

            // 获取组件状态
            List<Map<String, Object>> componentHealth = getComponentHealthStatus();
            List<ComponentHealthVO> componentHealthList = convertToComponentHealthList(componentHealth);
            systemHealth.setComponentHealthList(componentHealthList);

            // 获取系统指标
            Map<String, Object> systemMetrics = getSystemMetrics();
            systemHealth.setPerformanceMetrics(systemMetrics);

            systemHealth.setResourceUsage(resourceUsage);
            systemHealth.setLastCheckTime(LocalDateTime.now());

            // 获取健康趋势
            List<Map<String, Object>> healthTrends = getHealthTrends(24);
            systemHealth.setHealthTrends(healthTrends);

            // 转换活跃告警为摘要列表
            List<AlertSummaryVO> alertSummaryList = convertToAlertSummaryList(activeAlerts);
            systemHealth.setActiveAlertList(alertSummaryList);

            log.debug("系统健康概览获取完成，整体状态：{}，健康评分：{}", overallStatus, healthScore);

        } catch (Exception e) {
            log.error("获取系统健康概览失败", e);
            // 设置默认值
            systemHealth.setOverallStatus("UNKNOWN");
            systemHealth.setHealthScore(0);
            systemHealth.setLastCheckTime(LocalDateTime.now());
        }

        return systemHealth;
    }

    @Override
    public List<Map<String, Object>> getComponentHealthStatus() {
        log.debug("获取组件健康状态");

        try {
            return healthCheckManager.getAllComponentHealthStatus();
        } catch (Exception e) {
            log.error("获取组件健康状态失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> getSystemMetrics() {
        log.debug("获取系统性能指标");

        try {
            return systemMonitorManager.getSystemMetrics();
        } catch (Exception e) {
            log.error("获取系统性能指标失败", e);
            return new HashMap<>();
        }
    }

    @Override
    public Map<String, Object> getResourceUsage() {
        log.debug("获取资源使用情况");

        try {
            ResourceUsageVO resourceUsage = systemMonitorManager.getResourceUsage();

            Map<String, Object> usage = new HashMap<>();
            usage.put("cpu", Map.of(
                    "usage", resourceUsage.getCpuUsage(),
                    "cores", resourceUsage.getCpuCores(),
                    "load", resourceUsage.getCpuLoad()));

            usage.put("memory", Map.of(
                    "usage", resourceUsage.getMemoryUsage(),
                    "total", resourceUsage.getTotalMemory(),
                    "used", resourceUsage.getUsedMemory(),
                    "available", resourceUsage.getAvailableMemory()));

            usage.put("disk", Map.of(
                    "usage", resourceUsage.getDiskUsage(),
                    "total", resourceUsage.getTotalDisk(),
                    "used", resourceUsage.getUsedDisk(),
                    "available", resourceUsage.getAvailableDisk()));

            usage.put("network", Map.of(
                    "inbound", resourceUsage.getNetworkInbound(),
                    "outbound", resourceUsage.getNetworkOutbound()));

            return usage;

        } catch (Exception e) {
            log.error("获取资源使用情况失败", e);
            return new HashMap<>();
        }
    }

    @Override
    public List<Map<String, Object>> getMicroservicesStatus() {
        log.debug("获取微服务状态");

        try {
            // 模拟微服务状态
            List<Map<String, Object>> services = new ArrayList<>();

            String[] serviceNames = {
                    "ioedream-auth-service",
                    "ioedream-identity-service",
                    "ioedream-device-service",
                    "ioedream-access-service",
                    "ioedream-consume-service",
                    "ioedream-attendance-service",
                    "ioedream-video-service",
                    "ioedream-monitor-service"
            };

            for (String serviceName : serviceNames) {
                Map<String, Object> service = new HashMap<>();
                service.put("serviceName", serviceName);
                service.put("status", Math.random() > 0.1 ? "UP" : "DOWN");
                service.put("responseTime", Math.random() * 100);
                service.put("uptime", Math.random() * 86400);
                service.put("version", "1.0.0");
                service.put("lastCheckTime", LocalDateTime.now());
                services.add(service);
            }

            return services;

        } catch (Exception e) {
            log.error("获取微服务状态失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> getDatabaseStatus() {
        log.debug("获取数据库状态");

        try {
            Map<String, Object> status = new HashMap<>();

            // 模拟数据库状态检查
            boolean connected = checkDatabaseConnection();
            status.put("status", connected ? "UP" : "DOWN");
            status.put("responseTime", connected ? Math.random() * 10 : -1);
            status.put("activeConnections", (int) (Math.random() * 100));
            status.put("maxConnections", 200);
            status.put("lastCheckTime", LocalDateTime.now());

            if (connected) {
                status.put("version", "MySQL 8.0.33");
                status.put("uptime", Math.random() * 86400);
            }

            return status;

        } catch (Exception e) {
            log.error("获取数据库状态失败", e);
            Map<String, Object> status = new HashMap<>();
            status.put("status", "DOWN");
            status.put("error", e.getMessage());
            status.put("lastCheckTime", LocalDateTime.now());
            return status;
        }
    }

    @Override
    public Map<String, Object> getCacheStatus() {
        log.debug("获取缓存状态");

        try {
            Map<String, Object> status = new HashMap<>();

            // 模拟Redis状态检查
            boolean connected = checkRedisConnection();
            status.put("status", connected ? "UP" : "DOWN");
            status.put("responseTime", connected ? Math.random() * 5 : -1);
            status.put("memoryUsage", Math.random() * 100);
            status.put("hitRate", Math.random() * 100);
            status.put("connectedClients", (int) (Math.random() * 50));
            status.put("lastCheckTime", LocalDateTime.now());

            if (connected) {
                status.put("version", "Redis 6.2.7");
                status.put("uptime", Math.random() * 86400);
            }

            return status;

        } catch (Exception e) {
            log.error("获取缓存状态失败", e);
            Map<String, Object> status = new HashMap<>();
            status.put("status", "DOWN");
            status.put("error", e.getMessage());
            status.put("lastCheckTime", LocalDateTime.now());
            return status;
        }
    }

    @Override
    public Map<String, Object> performHealthCheck(String component) {
        log.debug("执行健康检查，组件：{}", component);

        try {
            return healthCheckManager.performHealthCheck(component);
        } catch (Exception e) {
            log.error("执行健康检查失败，组件：{}", component, e);
            Map<String, Object> result = new HashMap<>();
            result.put("component", component);
            result.put("status", "DOWN");
            result.put("error", e.getMessage());
            result.put("checkTime", LocalDateTime.now());
            return result;
        }
    }

    @Override
    public List<Map<String, Object>> getActiveAlerts() {
        log.debug("获取活跃告警");

        try {
            List<AlertEntity> alertEntities = alertDao.selectActiveAlerts();

            return alertEntities.stream()
                    .map(this::convertAlertEntityToMap)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("获取活跃告警失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> getSystemUptime() {
        log.debug("获取系统运行时间");

        try {
            Map<String, Object> uptime = new HashMap<>();

            long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
            long uptimeSeconds = uptimeMs / 1000;
            long uptimeMinutes = uptimeSeconds / 60;
            long uptimeHours = uptimeMinutes / 60;
            long uptimeDays = uptimeHours / 24;

            uptime.put("uptimeMs", uptimeMs);
            uptime.put("uptimeSeconds", uptimeSeconds);
            uptime.put("uptimeMinutes", uptimeMinutes);
            uptime.put("uptimeHours", uptimeHours);
            uptime.put("uptimeDays", uptimeDays);
            uptime.put("startTime", ManagementFactory.getRuntimeMXBean().getStartTime());
            uptime.put("currentTime", System.currentTimeMillis());

            return uptime;

        } catch (Exception e) {
            log.error("获取系统运行时间失败", e);
            return new HashMap<>();
        }
    }

    @Override
    public List<Map<String, Object>> getHealthTrends(Integer hours) {
        log.debug("获取健康趋势，时间范围：{}小时", hours);

        try {
            List<Map<String, Object>> trends = new ArrayList<>();

            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(hours);

            // 模拟健康趋势数据
            for (int i = 0; i < hours; i++) {
                Map<String, Object> trend = new HashMap<>();
                trend.put("timestamp", startTime.plusHours(i));
                trend.put("healthScore", 80 + Math.random() * 20);
                trend.put("cpuUsage", Math.random() * 100);
                trend.put("memoryUsage", Math.random() * 100);
                trend.put("activeAlerts", (int) (Math.random() * 5));
                trends.add(trend);
            }

            return trends;

        } catch (Exception e) {
            log.error("获取健康趋势失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public void resolveAlert(Long alertId, String resolution) {
        log.info("解决告警，ID：{}，解决说明：{}", alertId, resolution);

        try {
            LocalDateTime resolveTime = LocalDateTime.now();
            Long resolveUserId = 1L; // 实际应该从当前用户获取

            alertDao.updateResolveInfo(alertId, resolveTime, resolveUserId, resolution);

            log.info("告警解决完成，ID：{}", alertId);

        } catch (Exception e) {
            log.error("解决告警失败，ID：{}", alertId, e);
        }
    }

    // 私有辅助方法

    private Integer calculateHealthScore(String overallStatus) {
        switch (overallStatus) {
            case "HEALTHY":
                return 90 + (int) (Math.random() * 10);
            case "WARNING":
                return 70 + (int) (Math.random() * 20);
            case "CRITICAL":
                return 30 + (int) (Math.random() * 40);
            case "UNKNOWN":
            default:
                return 50;
        }
    }

    private List<ComponentHealthVO> convertToComponentHealthList(List<Map<String, Object>> componentHealth) {
        return componentHealth.stream()
                .map(this::convertToComponentHealthVO)
                .collect(Collectors.toList());
    }

    private ComponentHealthVO convertToComponentHealthVO(Map<String, Object> health) {
        ComponentHealthVO vo = new ComponentHealthVO();
        vo.setComponentName((String) health.get("component"));
        vo.setComponentType(mapToComponentType((String) health.get("component")));
        vo.setStatus((String) health.get("status"));
        vo.setResponseTime(health.containsKey("responseTime") ? (Long) health.get("responseTime") : null);
        vo.setLastCheckTime((LocalDateTime) health.get("checkTime"));
        vo.setDetails(health);
        vo.setMetrics(health);
        return vo;
    }

    private String mapToComponentType(String component) {
        switch (component.toLowerCase()) {
            case "database":
                return "DATABASE";
            case "redis":
                return "CACHE";
            case "cpu":
            case "memory":
            case "disk":
            case "network":
                return "SYSTEM";
            case "application":
                return "SERVICE";
            default:
                return "UNKNOWN";
        }
    }

    private List<AlertSummaryVO> convertToAlertSummaryList(List<Map<String, Object>> alerts) {
        return alerts.stream()
                .map(this::convertToAlertSummaryVO)
                .collect(Collectors.toList());
    }

    private AlertSummaryVO convertToAlertSummaryVO(Map<String, Object> alert) {
        AlertSummaryVO vo = new AlertSummaryVO();
        vo.setAlertId((Long) alert.get("alertId"));
        vo.setAlertTitle((String) alert.get("alertTitle"));
        vo.setAlertDescription((String) alert.get("alertDescription"));
        vo.setAlertLevel((String) alert.get("alertLevel"));
        vo.setAlertType((String) alert.get("alertType"));
        vo.setServiceName((String) alert.get("serviceName"));
        vo.setInstanceId((String) alert.get("instanceId"));
        vo.setStatus((String) alert.get("status"));
        vo.setAlertSource((String) alert.get("alertSource"));
        vo.setAlertValue(alert.containsKey("alertValue") ? (Double) alert.get("alertValue") : null);
        vo.setThresholdValue(alert.containsKey("thresholdValue") ? (Double) alert.get("thresholdValue") : null);
        vo.setAlertTime((LocalDateTime) alert.get("alertTime"));
        vo.setNotificationStatus((String) alert.get("notificationStatus"));
        vo.setAcknowledged(false);
        return vo;
    }

    private Map<String, Object> convertAlertEntityToMap(AlertEntity alert) {
        Map<String, Object> map = new HashMap<>();
        map.put("alertId", alert.getAlertId());
        map.put("alertTitle", alert.getAlertTitle());
        map.put("alertDescription", alert.getAlertDescription());
        map.put("alertLevel", alert.getAlertLevel());
        map.put("alertType", alert.getAlertType());
        map.put("serviceName", alert.getServiceName());
        map.put("instanceId", alert.getInstanceId());
        map.put("status", alert.getStatus());
        map.put("alertSource", alert.getAlertSource());
        map.put("alertValue", alert.getAlertValue());
        map.put("thresholdValue", alert.getThresholdValue());
        map.put("alertTime", alert.getAlertTime());
        map.put("notificationStatus", alert.getNotificationStatus());
        return map;
    }

    // 模拟方法
    private boolean checkDatabaseConnection() {
        return Math.random() > 0.1; // 90%成功率
    }

    private boolean checkRedisConnection() {
        return Math.random() > 0.05; // 95%成功率
    }
}
