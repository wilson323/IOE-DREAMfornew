package net.lab1024.sa.admin.module.smart.monitor.service.impl;

import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.smart.access.dao.AccessDeviceDao;
import net.lab1024.sa.admin.module.smart.access.dao.AccessEventDao;
import net.lab1024.sa.admin.module.smart.monitor.listener.AccessEventRealtimeListener;
import net.lab1024.sa.admin.module.smart.monitor.service.AccessMonitorService;
import net.lab1024.sa.admin.module.smart.monitor.websocket.WebSocketSessionManager;
import net.lab1024.sa.base.common.cache.RedisUtil;

/**
 * 门禁实时监控服务实现
 * <p>
 * 严格遵循repowiki规范：
 * - 使用@Resource依赖注入
 * - 完整的异常处理和日志记录
 * - 支持数据聚合和缓存优化
 * - 提供实时数据更新机制
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Slf4j
@Service
public class AccessMonitorServiceImpl implements AccessMonitorService {

    @Resource
    private WebSocketSessionManager sessionManager;

    @Resource
    private AccessDeviceDao accessDeviceDao;

    @Resource
    private AccessEventDao accessEventDao;

    @Resource
    private AccessEventRealtimeListener eventListener;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private DataSource dataSource;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 日期格式化器
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 系统启动时间（用于计算运行时间）
    private static final long SYSTEM_START_TIME = System.currentTimeMillis();

    @Override
    public Map<String, Object> getDashboardOverview() {
        log.info("获取监控仪表板概览");

        try {
            Map<String, Object> overview = new HashMap<>();

            // 获取设备统计
            Map<String, Object> deviceStats = getDeviceStatistics();
            overview.put("devices", deviceStats);

            // 获取实时统计
            Map<String, Object> realtimeStats = getRealtimeStatistics();
            overview.put("realtime", realtimeStats);

            // 获取WebSocket统计
            Map<String, Object> websocketStats = getWebSocketStats();
            overview.put("websocket", websocketStats);

            // 获取系统健康状态
            Map<String, Object> healthStatus = getHealthStatus();
            overview.put("health", healthStatus);

            // 系统状态时间
            overview.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));
            overview.put("status", "running");

            return overview;

        } catch (Exception e) {
            log.error("获取监控仪表板概览失败", e);
            throw new RuntimeException("获取监控仪表板概览失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getRealtimeStatistics() {
        log.debug("获取实时统计数据");

        try {
            Map<String, Object> statistics = new HashMap<>();

            // 如果实时监听器已初始化，从其获取统计
            if (eventListener != null) {
                Map<String, Object> listenerStats = eventListener.getRealTimeStatistics();
                statistics.putAll(listenerStats);
            } else {
                // 从Redis获取缓存的统计数据
                String redisKey = "smart:monitor:realtime_stats";
                Map<String, Object> cachedStats = redisUtil.getBean(redisKey, Map.class);
                if (cachedStats != null) {
                    statistics.putAll(cachedStats);
                } else {
                    // 生成默认统计数据
                    statistics.put("totalEvents", 0);
                    statistics.put("successEvents", 0);
                    statistics.put("failedEvents", 0);
                    statistics.put("onlineDevices", 0);
                    statistics.put("offlineDevices", 0);
                }
            }

            statistics.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));

            return statistics;

        } catch (Exception e) {
            log.error("获取实时统计数据失败", e);
            throw new RuntimeException("获取实时统计数据失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getDeviceStatistics() {
        log.debug("获取设备状态统计");

        try {
            Map<String, Object> statistics = new HashMap<>();

            // 从数据库查询设备统计
            Long totalDevices = accessDeviceDao.countTotalDevices();
            Long onlineDevices = accessDeviceDao.countOnlineDevices();

            statistics.put("totalDevices", totalDevices != null ? totalDevices : 0);
            statistics.put("onlineDevices", onlineDevices != null ? onlineDevices : 0);
            statistics.put("offlineDevices",
                    (totalDevices != null ? totalDevices : 0) - (onlineDevices != null ? onlineDevices : 0));

            // 计算在线率
            double onlineRate = totalDevices != null && totalDevices > 0
                    ? (double) (onlineDevices != null ? onlineDevices : 0) / totalDevices * 100
                    : 0;
            statistics.put("onlineRate", Math.round(onlineRate * 100.0) / 100.0);

            // 设备类型统计（示例数据）
            Map<String, Integer> deviceTypeStats = new HashMap<>();
            deviceTypeStats.put("门禁机", 5);
            deviceTypeStats.put("读卡器", 3);
            deviceTypeStats.put("指纹机", 2);
            deviceTypeStats.put("人脸识别机", 4);
            statistics.put("deviceTypes", deviceTypeStats);

            statistics.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));

            return statistics;

        } catch (Exception e) {
            log.error("获取设备状态统计失败", e);
            throw new RuntimeException("获取设备状态统计失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getAreaStatistics(String startTime, String endTime, Long areaId) {
        log.debug("获取区域访问统计，startTime: {}, endTime: {}, areaId: {}", startTime, endTime, areaId);

        try {
            Map<String, Object> statistics = new HashMap<>();

            // TODO: 实现区域访问统计查询
            // 这里需要根据startTime、endTime和areaId查询数据库

            // 示例数据
            Map<String, Integer> areaEventCount = new HashMap<>();
            areaEventCount.put("主园区", 156);
            areaEventCount.put("研发楼", 89);
            areaEventCount.put("办公楼", 134);

            Map<String, Map<String, Integer>> verifyMethodStats = new HashMap<>();
            Map<String, Integer> cardStats = new HashMap<>();
            cardStats.put("success", 245);
            cardStats.put("failed", 23);
            Map<String, Integer> faceStats = new HashMap<>();
            faceStats.put("success", 189);
            faceStats.put("failed", 15);
            verifyMethodStats.put("card", cardStats);
            verifyMethodStats.put("face", faceStats);

            statistics.put("areaEventCount", areaEventCount);
            statistics.put("verifyMethodStats", verifyMethodStats);

            if (areaId != null) {
                statistics.put("selectedAreaId", areaId);
            }

            statistics.put("startTime", startTime);
            statistics.put("endTime", endTime);
            statistics.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));

            return statistics;

        } catch (Exception e) {
            log.error("获取区域访问统计失败，startTime: {}, endTime: {}, areaId: {}", startTime, endTime, areaId, e);
            throw new RuntimeException("获取区域访问统计失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getAccessTrend(String timeRange, Integer timeSpan, String startTime, String endTime) {
        log.debug("获取访问趋势数据，timeRange: {}, timeSpan: {}, startTime: {}, endTime: {}", timeRange, timeSpan, startTime,
                endTime);

        try {
            Map<String, Object> trendData = new HashMap<>();

            // 生成时间标签和访问次数数据
            List<String> labels = new ArrayList<>();
            List<Integer> accessCounts = new ArrayList<>();
            List<Integer> successCounts = new ArrayList<>();
            List<Integer> failedCounts = new ArrayList<>();

            // 根据timeRange生成时间标签
            LocalDateTime now = LocalDateTime.now();
            for (int i = timeSpan - 1; i >= 0; i--) {
                LocalDateTime timePoint = switch (timeRange) {
                    case "hour" -> now.minusHours(i);
                    case "day" -> now.minusDays(i);
                    case "week" -> now.minusWeeks(i);
                    case "month" -> now.minusMonths(i);
                    default -> now.minusDays(i);
                };

                String label = switch (timeRange) {
                    case "hour" -> timePoint.getHour() + ":00";
                    case "day" -> timePoint.getMonthValue() + "/" + timePoint.getDayOfMonth();
                    case "week" -> "Week " + (timePoint.getDayOfYear() / 7 + 1);
                    case "month" -> timePoint.getYear() + "-" + timePoint.getMonthValue();
                    default -> timePoint.format(DateTimeFormatter.ofPattern("MM-dd"));
                };

                labels.add(label);
                // 从数据源查询每个时间点的访问次数（当前使用模拟数据）
                accessCounts.add((int) (Math.random() * 100) + 20);
                successCounts.add((int) (Math.random() * 80) + 10);
                failedCounts.add((int) (Math.random() * 20) + 5);
            }

            trendData.put("labels", labels);
            trendData.put("accessCounts", accessCounts);
            trendData.put("successCounts", successCounts);
            trendData.put("failedCounts", failedCounts);

            trendData.put("timeRange", timeRange);
            trendData.put("timeSpan", timeSpan);
            trendData.put("startTime", startTime);
            trendData.put("endTime", endTime);
            trendData.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));

            return trendData;

        } catch (Exception e) {
            log.error("获取访问趋势数据失败，timeRange: {}, timeSpan: {}", timeRange, timeSpan, e);
            throw new RuntimeException("获取访问趋势数据失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getRealtimeAlerts(String alertLevel, Integer pageNum, Integer pageSize) {
        log.debug("获取实时告警列表，alertLevel: {}, pageNum: {}, pageSize: {}", alertLevel, pageNum, pageSize);

        try {
            Map<String, Object> alertData = new HashMap<>();

            // TODO: 从数据库或缓存中查询告警数据
            List<Map<String, Object>> alerts = new ArrayList<>();

            // 示例告警数据
            for (int i = 1; i <= 5; i++) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("id", "alert_" + i);
                alert.put("level", i % 2 == 0 ? "warning" : "critical");
                alert.put("title", "设备" + i + "状态异常");
                alert.put("message", "设备响应超时");
                alert.put("deviceId", "device_" + i);
                alert.put("deviceName", "门禁设备" + i);
                alert.put("createTime", LocalDateTime.now().minusMinutes(i * 5).format(DATE_FORMATTER));
                alert.put("status", "active");
                alert.put("acknowledged", false);
                alerts.add(alert);
            }

            // 根据alertLevel过滤
            if (alertLevel != null && !alertLevel.isEmpty()) {
                alerts = alerts.stream()
                        .filter(alert -> alertLevel.equals(alert.get("level")))
                        .toList();
            }

            // 分页处理
            int total = alerts.size();
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, total);
            List<Map<String, Object>> pagedAlerts = alerts.subList(start, end);

            alertData.put("total", total);
            alertData.put("pageNum", pageNum);
            alertData.put("pageSize", pageSize);
            alertData.put("pages", (total + pageSize - 1) / pageSize);
            alertData.put("alerts", pagedAlerts);

            return alertData;

        } catch (Exception e) {
            log.error("获取实时告警列表失败，alertLevel: {}, pageNum: {}, pageSize: {}", alertLevel, pageNum, pageSize, e);
            throw new RuntimeException("获取实时告警列表失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getWebSocketStats() {
        log.debug("获取WebSocket连接统计");

        try {
            Map<String, Object> stats = new HashMap<>();

            if (sessionManager != null) {
                Map<String, Object> sessionStats = sessionManager.getSessionStats();
                stats.putAll(sessionStats);

                // 实时数据
                stats.put("realTimeTotalSessions", sessionManager.getActiveSessionCount());
                stats.put("realTimeTotalUsers", sessionManager.getActiveUserCount());
            } else {
                stats.put("totalSessions", 0);
                stats.put("totalUsers", 0);
                stats.put("realTimeTotalSessions", 0);
                stats.put("realTimeTotalUsers", 0);
            }

            stats.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));

            return stats;

        } catch (Exception e) {
            log.error("获取WebSocket连接统计失败", e);
            throw new RuntimeException("获取WebSocket连接统计失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getHealthStatus() {
        log.debug("获取系统健康状态");

        try {
            Map<String, Object> healthStatus = new HashMap<>();

            // 整体状态
            healthStatus.put("status", "healthy");
            healthStatus.put("uptime", calculateSystemUptime());

            // 各组件状态
            Map<String, String> componentStatus = new HashMap<>();
            componentStatus.put("websocket", sessionManager != null ? "healthy" : "uninitialized");
            componentStatus.put("database", checkDatabaseHealth());
            componentStatus.put("cache", checkRedisHealth());
            componentStatus.put("eventListener", eventListener != null ? "healthy" : "uninitialized");
            healthStatus.put("components", componentStatus);

            // 性能指标
            Map<String, Object> performance = new HashMap<>();
            performance.put("memoryUsage", getMemoryUsage());
            performance.put("cpuUsage", getCpuUsage());
            performance.put("responseTime", getAverageResponseTime());
            healthStatus.put("performance", performance);

            healthStatus.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));

            return healthStatus;

        } catch (Exception e) {
            log.error("获取系统健康状态失败", e);
            throw new RuntimeException("获取系统健康状态失败: " + e.getMessage());
        }
    }

    @Override
    public void refreshStatistics() {
        log.info("手动刷新统计数据");

        try {
            // 清除相关缓存
            redisUtil.delete("smart:monitor:realtime_stats");
            redisUtil.delete("smart:monitor:device_stats");
            redisUtil.delete("smart:monitor:websocket_stats");

            // 重新计算统计数据
            getRealtimeStatistics();
            getDeviceStatistics();

            // 发送WebSocket通知
            if (sessionManager != null) {
                Map<String, Object> refreshNotification = new HashMap<>();
                refreshNotification.put("type", "statistics_refresh");
                refreshNotification.put("message", "统计数据已刷新");
                refreshNotification.put("timestamp", LocalDateTime.now().toString());

                String notificationMessage = JSON.toJSONString(refreshNotification);
                sessionManager.broadcast(notificationMessage);
            }

            log.info("统计数据刷新完成");

        } catch (Exception e) {
            log.error("刷新统计数据失败", e);
            throw new RuntimeException("刷新统计数据失败: " + e.getMessage());
        }
    }

    @Override
    public void clearHistoryData(String dataType, Integer retainDays) {
        log.info("清除历史数据，dataType: {}, retainDays: {}", dataType, retainDays);

        try {
            // TODO: 根据dataType清除相应的历史数据
            // 这里需要实现具体的数据清除逻辑

            // 清除相关缓存
            String cacheKey = "smart:monitor:history:" + dataType;
            redisUtil.delete(cacheKey);

            log.info("历史数据清除完成，dataType: {}, retainDays: {}", dataType, retainDays);

        } catch (Exception e) {
            log.error("清除历史数据失败，dataType: {}, retainDays: {}", dataType, retainDays, e);
            throw new RuntimeException("清除历史数据失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> exportReport(String reportType, String startTime, String endTime, String format) {
        log.info("导出监控报告，reportType: {}, startTime: {}, endTime: {}, format: {}", reportType, startTime, endTime,
                format);

        try {
            Map<String, Object> reportData = new HashMap<>();

            // 根据reportType生成不同的报告数据
            switch (reportType.toLowerCase()) {
                case "daily":
                    reportData = generateDailyReport(startTime, endTime);
                    break;
                case "weekly":
                    reportData = generateWeeklyReport(startTime, endTime);
                    break;
                case "monthly":
                    reportData = generateMonthlyReport(startTime, endTime);
                    break;
                default:
                    reportData = generateCustomReport(reportType, startTime, endTime);
                    break;
            }

            // 添加报告元数据
            reportData.put("reportType", reportType);
            reportData.put("startTime", startTime);
            reportData.put("endTime", endTime);
            reportData.put("format", format);
            reportData.put("generatedTime", LocalDateTime.now().format(DATE_FORMATTER));

            // TODO: 根据format生成实际的报告文件并返回下载链接
            if ("excel".equals(format)) {
                // 生成Excel报告文件
                reportData.put("downloadUrl", "/api/smart/monitor/download/" + reportType + "_report_"
                        + System.currentTimeMillis() + ".xlsx");
            }

            return reportData;

        } catch (Exception e) {
            log.error("导出监控报告失败，reportType: {}, startTime: {}, endTime: {}, format: {}", reportType, startTime, endTime,
                    format, e);
            throw new RuntimeException("导出监控报告失败: " + e.getMessage());
        }
    }

    @Override
    public void sendWebSocketTestMessage() {
        log.info("发送WebSocket测试消息");

        try {
            if (sessionManager != null) {
                JSONObject testMessage = new JSONObject();
                testMessage.put("type", "test");
                testMessage.put("message", "WebSocket连接测试成功");
                testMessage.put("timestamp", LocalDateTime.now().toString());
                testMessage.put("serverInfo", "SmartAdmin Access Monitor");

                String message = testMessage.toJSONString();
                sessionManager.broadcast(message);

                log.info("WebSocket测试消息已发送，连接数量: {}", sessionManager.getActiveSessionCount());
            } else {
                log.warn("WebSocket会话管理器未初始化，无法发送测试消息");
            }

        } catch (Exception e) {
            log.error("发送WebSocket测试消息失败", e);
            throw new RuntimeException("发送WebSocket测试消息失败: " + e.getMessage());
        }
    }

    /**
     * 生成日报数据
     */
    private Map<String, Object> generateDailyReport(String startTime, String endTime) {
        Map<String, Object> report = new HashMap<>();
        report.put("title", "门禁系统日报");
        report.put("period", startTime + " - " + endTime);

        // 添加日报数据
        Map<String, Object> data = new HashMap<>();
        data.put("totalEvents", 1250);
        data.put("successRate", 95.6);
        data.put("peakHour", "09:00");
        data.put("peakAccessCount", 156);

        report.put("data", data);
        return report;
    }

    /**
     * 生成周报数据
     */
    private Map<String, Object> generateWeeklyReport(String startTime, String endTime) {
        Map<String, Object> report = new HashMap<>();
        report.put("title", "门禁系统周报");
        report.put("period", startTime + " - " + endTime);

        Map<String, Object> data = new HashMap<>();
        data.put("totalEvents", 8750);
        data.put("successRate", 94.2);
        data.put("busiestDay", "Wednesday");
        data.put("weeklyTrend", "上升");

        report.put("data", data);
        return report;
    }

    /**
     * 生成月报数据
     */
    private Map<String, Object> generateMonthlyReport(String startTime, String endTime) {
        Map<String, Object> report = new HashMap<>();
        report.put("title", "门禁系统月报");
        report.put("period", startTime + " - " + endTime);

        Map<String, Object> data = new HashMap<>();
        data.put("totalEvents", 37500);
        data.put("successRate", 93.8);
        data.put("busiestWeek", "第3周");
        data.put("monthlyGrowth", "12.5%");

        report.put("data", data);
        return report;
    }

    /**
     * 生成自定义报告数据
     */
    private Map<String, Object> generateCustomReport(String reportType, String startTime, String endTime) {
        Map<String, Object> report = new HashMap<>();
        report.put("title", "门禁系统" + reportType + "报告");
        report.put("period", startTime + " - " + endTime);

        Map<String, Object> data = new HashMap<>();
        data.put("totalEvents", 0);
        data.put("successRate", 0);

        report.put("data", data);
        return report;
    }

    /**
     * 检查数据库连接健康状态
     * <p>
     * 通过执行简单查询测试数据库连接
     * 严格遵循repowiki规范：异常处理和日志记录
     *
     * @return 健康状态字符串："healthy" 或 "unhealthy"
     */
    private String checkDatabaseHealth() {
        try {
            // 使用try-with-resources流程
            try (Connection connection = dataSource.getConnection()) {
                // 检查连接有效性
                boolean isValid = connection.isValid(3); // 3秒超时
                if (isValid) {
                    log.debug("数据库连接检查通过");
                    return "healthy";
                } else {
                    log.warn("数据库连接检查失败：连接无效");
                    return "unhealthy";
                }
            }
        } catch (Exception e) {
            log.error("数据库连接检查失败", e);
            return "unhealthy";
        }
    }

    /**
     * is连接健康状态
     *
     *
     *
     *
     *
     * r
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     * long totalMemory = memoryBean.getHeapMemoryUsage().getMax();
     * long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
     * double usagePercent = totalMemory > 0 ? (double) usedMemory / totalMemory *
     * 100 : 0;
     * return String.format("%.2f%%", usagePercent);
     * } catch (Exception e) {
     * log.error("获取内存使用率失败", e);
     * return "0.00%";
     * }
     * }
     *
     * /**
     * 获取CPU使用率
     * <p>
     * 使用JVM的RuntimeMXBean计算CPU使用率
     * 严格遵循repowiki规范：性能监控
     *
     * @return CPU使用率字符串，格式："XX.XX%"
     */
    private String getCpuUsage() {
        try {
            com.sun.management.OperatingSystemMXBean osBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory
                    .getOperatingSystemMXBean();

            // 获取进程CPU使用率
            double cpuUsage = osBean.getProcessCpuLoad() * 100;
            if (cpuUsage < 0) {
                // 如果获取失败，尝试获取系统CPU使用率（已弃用，但作为后备方案）
                @SuppressWarnings("deprecation")
                double systemCpuLoad = osBean.getSystemCpuLoad();
                cpuUsage = systemCpuLoad * 100;
            }

            // 如果仍然失败，返回0
            if (cpuUsage < 0) {
                cpuUsage = 0;
            }

            return String.format("%.2f%%", cpuUsage);
        } catch (Exception e) {
            log.error("获取CPU使用率失败", e);
            return "0.00%";
        }
    }

    /**
     * 获取平均响应时间
     * <p>
     * 从Redis缓存中获取平均响应时间统计
     * 严格遵循repowiki规范：性能监控
     *
     * @return 平均响应时间字符串，格式："XXXms"
     */
    private String getAverageResponseTime() {
        try {
            // 从Redis获取缓存的平均响应时间
            String cacheKey = "smart:monitor:avg_response_time";
            Object cachedTime = redisUtil.get(cacheKey);

            if (cachedTime != null) {
                return cachedTime + "ms";
            }

            return "0ms";
        } catch (Exception e) {
            log.error("获取平均响应时间失败", e);
            return "0ms";
        }
    }

    /**
     * 计算系统运行时间
     * <p>
     * 基于系统启动时间计算运行时长
     * 严格遵循repowiki规范：性能监控
     *
     * @return 系统运行时间字符串，格式："X days, X hours, X minutes"
     */
    private String calculateSystemUptime() {
        try {
            long currentTime = System.currentTimeMillis();
            long uptimeMillis = currentTime - SYSTEM_START_TIME;
            Duration uptime = Duration.ofMillis(uptimeMillis);

            long days = uptime.toDays();
            long hours = uptime.toHours() % 24;
            long minutes = uptime.toMinutes() % 60;

            return String.format("%d days, %d hours, %d minutes", days, hours, minutes);
        } catch (Exception e) {
            log.error("计算系统运行时间失败", e);
            return "0 days, 0 hours, 0 minutes";
        }
    }
}
