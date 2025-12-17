package net.lab1024.sa.common.monitor.service.impl;

import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.monitor.domain.entity.AlertEntity;
import net.lab1024.sa.common.monitor.domain.vo.AlertSummaryVO;
import net.lab1024.sa.common.monitor.domain.vo.ComponentHealthVO;
import net.lab1024.sa.common.monitor.domain.vo.ResourceUsageVO;
import net.lab1024.sa.common.monitor.domain.vo.SystemHealthVO;
import net.lab1024.sa.common.monitor.manager.HealthCheckManager;
import net.lab1024.sa.common.monitor.manager.PerformanceMonitorManager;
import net.lab1024.sa.common.monitor.manager.SystemMonitorManager;
import net.lab1024.sa.common.monitor.dao.AlertDao;
import net.lab1024.sa.common.monitor.service.SystemHealthService;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.exception.ParamException;

/**
 * 系统健康监控服务实现类
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@Service注解标识服务实现
 * - 使用@Resource依赖注入（符合架构规范）
 * - 使用@Transactional管理事务
 * - 完整的异常处理和日志记录
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
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
    @Observed(name = "system.health.overview", contextualName = "system-health-overview")
    @Transactional(readOnly = true)
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
                    .filter(service -> "UP".equals(service.get("status")))
                    .count());
            systemHealth.setOfflineServices(systemHealth.getTotalServices() - systemHealth.getOnlineServices());

            // 获取告警信息
            List<Map<String, Object>> activeAlerts = getActiveAlerts();
            systemHealth.setActiveAlerts(activeAlerts.size());
            systemHealth.setCriticalAlerts((int) activeAlerts.stream()
                    .filter(alert -> "CRITICAL".equals(alert.get("alertLevel")))
                    .count());
            systemHealth.setWarningAlerts((int) activeAlerts.stream()
                    .filter(alert -> "WARNING".equals(alert.get("alertLevel")))
                    .count());

            // 获取资源使用情况
            ResourceUsageVO resourceUsage = systemMonitorManager.getResourceUsage();
            systemHealth.setCpuUsage(resourceUsage.getCpuUsage());
            systemHealth.setMemoryUsage(resourceUsage.getMemoryUsage());
            systemHealth.setDiskUsage(resourceUsage.getDiskUsage());
            systemHealth.setResourceUsage(resourceUsage);

            // 获取组件状态
            List<Map<String, Object>> componentHealth = getComponentHealthStatus();
            List<ComponentHealthVO> componentHealthList = componentHealth.stream()
                    .map(this::convertToComponentHealthVO)
                    .collect(java.util.stream.Collectors.toList());
            systemHealth.setComponentHealthList(componentHealthList);

            // 获取系统指标
            Map<String, Object> systemMetrics = getSystemMetrics();
            systemHealth.setPerformanceMetrics(systemMetrics);

            systemHealth.setLastCheckTime(LocalDateTime.now());

            // 获取健康趋势
            List<Map<String, Object>> healthTrends = getHealthTrends(24);
            systemHealth.setHealthTrends(healthTrends);

            // 转换活跃告警为摘要列表
            List<AlertSummaryVO> alertSummaryList = activeAlerts.stream()
                    .map(this::convertToAlertSummaryVO)
                    .collect(java.util.stream.Collectors.toList());
            systemHealth.setActiveAlertList(alertSummaryList);

            log.debug("系统健康概览获取完成，整体状态：{}，健康评分：{}", overallStatus, healthScore);

        } catch (Exception e) {
            log.error("[系统健康服务] 获取系统健康概览系统异常", e);
            // 设置默认值
            systemHealth.setOverallStatus("UNKNOWN");
            systemHealth.setHealthScore(0);
            systemHealth.setLastCheckTime(LocalDateTime.now());
        }

        return systemHealth;
    }

    @Override
    @Observed(name = "system.health.component", contextualName = "system-health-component")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getComponentHealthStatus() {
        log.debug("获取组件健康状态");

        try {
            return healthCheckManager.getAllComponentHealthStatus();
        } catch (Exception e) {
            log.error("[系统健康服务] 获取组件健康状态系统异常", e);
            return new ArrayList<>();
        }
    }

    @Override
    @Observed(name = "system.health.metrics", contextualName = "system-health-metrics")
    @Transactional(readOnly = true)
    public Map<String, Object> getSystemMetrics() {
        log.debug("获取系统性能指标");

        try {
            return systemMonitorManager.getSystemMetrics();
        } catch (Exception e) {
            log.error("[系统健康服务] 获取系统性能指标系统异常", e);
            return new HashMap<>();
        }
    }

    @Override
    @Observed(name = "system.health.resource", contextualName = "system-health-resource")
    @Transactional(readOnly = true)
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
            log.error("[系统健康服务] 获取资源使用情况系统异常", e);
            return new HashMap<>();
        }
    }

    @Override
    @Observed(name = "system.health.microservices", contextualName = "system-health-microservices")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMicroservicesStatus() {
        log.debug("获取微服务状态");

        try {
            return healthCheckManager.getMicroservicesHealthStatus();
        } catch (Exception e) {
            log.error("[系统健康服务] 获取微服务状态系统异常", e);
            return new ArrayList<>();
        }
    }

    @Override
    @Observed(name = "system.health.database", contextualName = "system-health-database")
    @Transactional(readOnly = true)
    public Map<String, Object> getDatabaseStatus() {
        log.debug("获取数据库状态");

        try {
            return healthCheckManager.checkDatabaseHealth();
        } catch (Exception e) {
            log.error("[系统健康服务] 获取数据库状态系统异常", e);
            return new HashMap<>();
        }
    }

    @Override
    @Observed(name = "system.health.cache", contextualName = "system-health-cache")
    @Transactional(readOnly = true)
    public Map<String, Object> getCacheStatus() {
        log.debug("获取缓存状态");

        try {
            return healthCheckManager.checkCacheHealth();
        } catch (Exception e) {
            log.error("[系统健康服务] 获取缓存状态系统异常", e);
            return new HashMap<>();
        }
    }

    @Override
    @Observed(name = "system.health.check", contextualName = "system-health-check")
    @Transactional(readOnly = true)
    public Map<String, Object> performHealthCheck(String component) {
        log.debug("执行健康检查，组件：{}", component);

        try {
            return healthCheckManager.performHealthCheck(component);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[系统健康服务] 执行健康检查参数异常, component={}, error={}", component, e.getMessage());
            return Map.of("status", "ERROR", "message", "参数异常: " + e.getMessage());
        } catch (Exception e) {
            log.error("[系统健康服务] 执行健康检查系统异常, component={}", component, e);
            return Map.of("status", "ERROR", "message", "系统异常: " + e.getMessage());
        }
    }

    @Override
    @Observed(name = "system.health.activeAlerts", contextualName = "system-health-active-alerts")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getActiveAlerts() {
        log.debug("获取活跃告警");

        try {
            QueryWrapper<AlertEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", "ACTIVE");
            queryWrapper.eq("deleted_flag", 0);
            queryWrapper.orderByDesc("create_time");
            queryWrapper.last("LIMIT 100");

            List<AlertEntity> alerts = alertDao.selectList(queryWrapper);

            return alerts.stream()
                    .map(alert -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("alertId", alert.getId());
                        map.put("alertLevel", alert.getAlertLevel());
                        map.put("alertTitle", alert.getAlertTitle());
                        map.put("alertMessage", alert.getAlertMessage());
                        map.put("createTime", alert.getCreateTime());
                        return map;
                    })
                    .collect(java.util.stream.Collectors.toList());

        } catch (Exception e) {
            log.error("[系统健康服务] 获取活跃告警系统异常", e);
            return new ArrayList<>();
        }
    }

    @Override
    @Observed(name = "system.health.uptime", contextualName = "system-health-uptime")
    @Transactional(readOnly = true)
    public Map<String, Object> getSystemUptime() {
        log.debug("获取系统运行时间");

        try {
            long uptimeMillis = ManagementFactory.getRuntimeMXBean().getUptime();
            long uptimeSeconds = uptimeMillis / 1000;

            Map<String, Object> uptime = new HashMap<>();
            uptime.put("uptimeMillis", uptimeMillis);
            uptime.put("uptimeSeconds", uptimeSeconds);
            uptime.put("uptimeMinutes", uptimeSeconds / 60);
            uptime.put("uptimeHours", uptimeSeconds / 3600);
            uptime.put("uptimeDays", uptimeSeconds / 86400);

            return uptime;

        } catch (Exception e) {
            log.error("[系统健康服务] 获取系统运行时间系统异常", e);
            return new HashMap<>();
        }
    }

    @Override
    @Observed(name = "system.health.trends", contextualName = "system-health-trends")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getHealthTrends(Integer hours) {
        log.debug("获取健康趋势，小时数：{}", hours);

        try {
            return performanceMonitorManager.getHealthTrends(hours != null ? hours : 24);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[系统健康服务] 获取健康趋势参数异常, hours={}, error={}", hours, e.getMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("[系统健康服务] 获取健康趋势系统异常, hours={}", hours, e);
            return new ArrayList<>();
        }
    }

    @Override
    @Observed(name = "system.health.resolveAlert", contextualName = "system-health-resolve-alert")
    public void resolveAlert(Long alertId, String resolution) {
        log.info("解决告警，ID：{}，说明：{}", alertId, resolution);

        try {
            AlertEntity alert = new AlertEntity();
            alert.setId(alertId);
            alert.setStatus("RESOLVED");
            alert.setResolutionNotes(resolution);
            alert.setResolvedTime(LocalDateTime.now());

            alertDao.updateById(alert);

            log.info("告警解决成功，ID：{}", alertId);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[系统健康服务] 解决告警参数异常, alertId={}, error={}", alertId, e.getMessage());
            throw new ParamException("ALERT_RESOLVE_PARAM_ERROR", "解决告警参数异常: " + e.getMessage(), e);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[系统健康服务] 解决告警系统异常, alertId={}", alertId, e);
            throw new SystemException("ALERT_RESOLVE_SYSTEM_ERROR", "解决告警系统异常", e);
        }
    }

    /**
     * 计算健康评分
     */
    private Integer calculateHealthScore(String status) {
        switch (status) {
            case "HEALTHY":
                return 100;
            case "WARNING":
                return 70;
            case "CRITICAL":
                return 30;
            case "DOWN":
                return 0;
            default:
                return 50;
        }
    }

    /**
     * 转换为ComponentHealthVO
     */
    private ComponentHealthVO convertToComponentHealthVO(Map<String, Object> map) {
        ComponentHealthVO vo = new ComponentHealthVO();
        vo.setComponentName((String) map.get("componentName"));
        vo.setComponentType((String) map.get("componentType"));
        vo.setStatus((String) map.get("status"));
        vo.setResponseTime((Long) map.get("responseTime"));
        vo.setLastCheckTime((LocalDateTime) map.get("lastCheckTime"));
        vo.setErrorMessage((String) map.get("errorMessage"));
        return vo;
    }

    /**
     * 转换为AlertSummaryVO
     */
    private AlertSummaryVO convertToAlertSummaryVO(Map<String, Object> map) {
        AlertSummaryVO vo = new AlertSummaryVO();
        vo.setAlertId((Long) map.get("alertId"));
        vo.setAlertLevel((String) map.get("alertLevel"));
        vo.setAlertTitle((String) map.get("alertTitle"));
        vo.setAlertMessage((String) map.get("alertMessage"));
        vo.setCreateTime((LocalDateTime) map.get("createTime"));
        return vo;
    }
}

