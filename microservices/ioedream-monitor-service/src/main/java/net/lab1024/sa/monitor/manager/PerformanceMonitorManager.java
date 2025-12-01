package net.lab1024.sa.monitor.manager;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.monitor.dao.SystemMonitorDao;
import net.lab1024.sa.monitor.domain.entity.SystemMonitorEntity;

/**
 * 性能监控管理器
 *
 * 负责JVM性能监控、GC监控、线程监控等性能相关功能
 *
 * @author IOE-DREAM Team
 */
@Slf4j
@Component
public class PerformanceMonitorManager {

    @Resource
    private SystemMonitorDao systemMonitorDao;

    // JVM相关Bean
    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    private final List<GarbageCollectorMXBean> gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
    private final List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();

    // 定时任务执行器
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

    /**
     * 初始化性能监控
     */
    public void initialize() {
        log.info("初始化性能监控管理器");

        // 启动定时性能监控任务
        startPerformanceMonitoring();

        log.info("性能监控管理器初始化完成");
    }

    /**
     * 获取JVM性能指标
     *
     * @return JVM性能指标
     */
    public Map<String, Object> getJvmPerformanceMetrics() {
        log.debug("获取JVM性能指标");

        Map<String, Object> metrics = new HashMap<>();

        try {
            // 内存信息
            Map<String, Object> memoryMetrics = getMemoryMetrics();
            metrics.put("memory", memoryMetrics);

            // 线程信息
            Map<String, Object> threadMetrics = getThreadMetrics();
            metrics.put("thread", threadMetrics);

            // GC信息
            Map<String, Object> gcMetrics = getGcMetrics();
            metrics.put("gc", gcMetrics);

            // 运行时信息
            Map<String, Object> runtimeMetrics = getRuntimeMetrics();
            metrics.put("runtime", runtimeMetrics);

            // 内存池信息
            List<Map<String, Object>> memoryPoolMetrics = getMemoryPoolMetrics();
            metrics.put("memoryPools", memoryPoolMetrics);

            metrics.put("collectTime", LocalDateTime.now());

            log.debug("JVM性能指标获取完成");

        } catch (Exception e) {
            log.error("获取JVM性能指标失败", e);
        }

        return metrics;
    }

    /**
     * 获取应用程序性能指标
     *
     * @return 应用程序性能指标
     */
    public Map<String, Object> getApplicationPerformanceMetrics() {
        log.debug("获取应用程序性能指标");

        Map<String, Object> metrics = new HashMap<>();

        try {
            // 响应时间指标
            metrics.put("avgResponseTime", getAverageResponseTime());
            metrics.put("maxResponseTime", getMaxResponseTime());
            metrics.put("minResponseTime", getMinResponseTime());

            // 吞吐量指标
            metrics.put("throughput", getThroughput());
            metrics.put("requestRate", getRequestRate());

            // 错误率指标
            metrics.put("errorRate", getErrorRate());
            metrics.put("errorCount", getErrorCount());

            // 并发指标
            metrics.put("activeConnections", getActiveConnections());
            metrics.put("maxConnections", getMaxConnections());

            // 缓存指标
            metrics.put("cacheHitRate", getCacheHitRate());
            metrics.put("cacheSize", getCacheSize());

            metrics.put("collectTime", LocalDateTime.now());

            log.debug("应用程序性能指标获取完成");

        } catch (Exception e) {
            log.error("获取应用程序性能指标失败", e);
        }

        return metrics;
    }

    /**
     * 获取性能趋势数据
     *
     * @param metricName 指标名称
     * @param hours      时间范围（小时）
     * @return 趋势数据
     */
    public List<Map<String, Object>> getPerformanceTrends(String metricName, Integer hours) {
        log.debug("获取性能趋势数据，指标：{}，时间范围：{}小时", metricName, hours);

        try {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(hours);

            return systemMonitorDao.selectByMonitorType("PERFORMANCE", startTime, endTime)
                    .stream()
                    .filter(monitor -> metricName.equals(monitor.getMetricName()))
                    .map(this::convertToTrendData)
                    .toList();

        } catch (Exception e) {
            log.error("获取性能趋势数据失败，指标：{}", metricName, e);
            return new ArrayList<>();
        }
    }

    /**
     * 收集性能指标
     *
     * @param serviceName 服务名称
     * @param instanceId  实例ID
     */
    public void collectPerformanceMetrics(String serviceName, String instanceId) {
        log.debug("收集性能指标，服务：{}，实例：{}", serviceName, instanceId);

        try {
            // 收集JVM性能指标
            collectJvmMetrics(serviceName, instanceId);

            // 收集应用性能指标
            collectApplicationMetrics(serviceName, instanceId);

        } catch (Exception e) {
            log.error("收集性能指标失败", e);
        }
    }

    /**
     * 分析性能瓶颈
     *
     * @return 性能分析结果
     */
    public Map<String, Object> analyzePerformanceBottlenecks() {
        log.debug("分析性能瓶颈");

        Map<String, Object> analysis = new HashMap<>();

        try {
            List<String> bottlenecks = new ArrayList<>();

            // 内存分析
            if (isMemoryPressureHigh()) {
                bottlenecks.add("内存使用率过高");
            }

            // GC分析
            if (isGcPressureHigh()) {
                bottlenecks.add("GC压力过大");
            }

            // 线程分析
            if (isThreadCountHigh()) {
                bottlenecks.add("线程数过多");
            }

            // CPU分析
            if (isCpuUsageHigh()) {
                bottlenecks.add("CPU使用率过高");
            }

            analysis.put("bottlenecks", bottlenecks);
            analysis.put("hasBottleneck", !bottlenecks.isEmpty());
            analysis.put("analysisTime", LocalDateTime.now());

            // 性能建议
            List<String> recommendations = generateRecommendations(bottlenecks);
            analysis.put("recommendations", recommendations);

            log.debug("性能瓶颈分析完成，发现问题：{}", bottlenecks.size());

        } catch (Exception e) {
            log.error("性能瓶颈分析失败", e);
        }

        return analysis;
    }

    // 私有方法实现具体功能

    private void startPerformanceMonitoring() {
        // 每分钟收集一次性能指标
        scheduler.scheduleAtFixedRate(() -> {
            try {
                String serviceName = System.getProperty("spring.application.name", "unknown-service");
                String instanceId = getInstanceId();
                collectPerformanceMetrics(serviceName, instanceId);
            } catch (Exception e) {
                log.error("定时性能监控异常", e);
            }
        }, 60, 60, TimeUnit.SECONDS);
    }

    private void collectJvmMetrics(String serviceName, String instanceId) {
        // 堆内存
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        savePerformanceMetric(serviceName, instanceId, "heap_used", heapUsage.getUsed() / 1024.0 / 1024.0, "MB");
        savePerformanceMetric(serviceName, instanceId, "heap_max", heapUsage.getMax() / 1024.0 / 1024.0, "MB");
        savePerformanceMetric(serviceName, instanceId, "heap_usage_percent",
                heapUsage.getUsed() * 100.0 / heapUsage.getMax(), "%");

        // 非堆内存
        MemoryUsage nonHeapUsage = memoryMXBean.getNonHeapMemoryUsage();
        savePerformanceMetric(serviceName, instanceId, "nonheap_used", nonHeapUsage.getUsed() / 1024.0 / 1024.0, "MB");

        // GC统计
        long totalGcCount = 0;
        long totalGcTime = 0;
        for (GarbageCollectorMXBean gcBean : gcMXBeans) {
            totalGcCount += gcBean.getCollectionCount();
            totalGcTime += gcBean.getCollectionTime();
        }
        savePerformanceMetric(serviceName, instanceId, "gc_count", (double) totalGcCount, "count");
        savePerformanceMetric(serviceName, instanceId, "gc_time", (double) totalGcTime, "ms");

        // 线程统计
        savePerformanceMetric(serviceName, instanceId, "thread_count", (double) threadMXBean.getThreadCount(), "count");
        savePerformanceMetric(serviceName, instanceId, "daemon_thread_count",
                (double) threadMXBean.getDaemonThreadCount(), "count");
    }

    private void collectApplicationMetrics(String serviceName, String instanceId) {
        // 模拟应用性能指标收集
        savePerformanceMetric(serviceName, instanceId, "response_time_avg", Math.random() * 100, "ms");
        savePerformanceMetric(serviceName, instanceId, "response_time_max", Math.random() * 500, "ms");
        savePerformanceMetric(serviceName, instanceId, "throughput", Math.random() * 1000, "req/s");
        savePerformanceMetric(serviceName, instanceId, "error_rate", Math.random() * 5, "%");
        savePerformanceMetric(serviceName, instanceId, "active_connections", (double) ((int) (Math.random() * 200)),
                "count");
    }

    private void savePerformanceMetric(String serviceName, String instanceId, String metricName,
            Double metricValue, String metricUnit) {
        SystemMonitorEntity monitor = new SystemMonitorEntity();
        monitor.setServiceName(serviceName);
        monitor.setInstanceId(instanceId);
        monitor.setMonitorType("PERFORMANCE");
        monitor.setMetricName(metricName);
        monitor.setMetricValue(metricValue);
        monitor.setMetricUnit(metricUnit);
        monitor.setMonitorTime(LocalDateTime.now());
        monitor.setStatus("NORMAL");
        monitor.setCreateTime(LocalDateTime.now());
        monitor.setDeletedFlag(0);

        systemMonitorDao.insert(monitor);
    }

    private Map<String, Object> getMemoryMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryMXBean.getNonHeapMemoryUsage();

        metrics.put("heapUsed", heapUsage.getUsed() / 1024.0 / 1024.0);
        metrics.put("heapMax", heapUsage.getMax() / 1024.0 / 1024.0);
        metrics.put("heapUsagePercent", heapUsage.getUsed() * 100.0 / heapUsage.getMax());
        metrics.put("nonHeapUsed", nonHeapUsage.getUsed() / 1024.0 / 1024.0);
        metrics.put("nonHeapMax", nonHeapUsage.getMax() / 1024.0 / 1024.0);

        return metrics;
    }

    private Map<String, Object> getThreadMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        metrics.put("threadCount", threadMXBean.getThreadCount());
        metrics.put("daemonThreadCount", threadMXBean.getDaemonThreadCount());
        metrics.put("peakThreadCount", threadMXBean.getPeakThreadCount());
        metrics.put("totalStartedThreadCount", threadMXBean.getTotalStartedThreadCount());

        return metrics;
    }

    private Map<String, Object> getGcMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        long totalGcCount = 0;
        long totalGcTime = 0;
        Map<String, Long> gcCountByCollector = new HashMap<>();
        Map<String, Long> gcTimeByCollector = new HashMap<>();

        for (GarbageCollectorMXBean gcBean : gcMXBeans) {
            long count = gcBean.getCollectionCount();
            long time = gcBean.getCollectionTime();
            String name = gcBean.getName();

            totalGcCount += count;
            totalGcTime += time;
            gcCountByCollector.put(name, count);
            gcTimeByCollector.put(name, time);
        }

        metrics.put("totalGcCount", totalGcCount);
        metrics.put("totalGcTime", totalGcTime);
        metrics.put("gcCountByCollector", gcCountByCollector);
        metrics.put("gcTimeByCollector", gcTimeByCollector);

        return metrics;
    }

    private Map<String, Object> getRuntimeMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        metrics.put("uptime", runtimeMXBean.getUptime());
        metrics.put("startTime", runtimeMXBean.getStartTime());
        metrics.put("jvmName", runtimeMXBean.getVmName());
        metrics.put("jvmVersion", runtimeMXBean.getVmVersion());
        metrics.put("jvmVendor", runtimeMXBean.getVmVendor());

        return metrics;
    }

    private List<Map<String, Object>> getMemoryPoolMetrics() {
        List<Map<String, Object>> poolMetrics = new ArrayList<>();

        for (MemoryPoolMXBean poolBean : memoryPoolMXBeans) {
            Map<String, Object> pool = new HashMap<>();
            pool.put("name", poolBean.getName());
            pool.put("type", poolBean.getType().toString());

            MemoryUsage usage = poolBean.getUsage();
            if (usage != null) {
                pool.put("used", usage.getUsed() / 1024.0 / 1024.0);
                pool.put("max", usage.getMax() / 1024.0 / 1024.0);
                pool.put("usagePercent", usage.getUsed() * 100.0 / usage.getMax());
            }

            poolMetrics.add(pool);
        }

        return poolMetrics;
    }

    private Map<String, Object> convertToTrendData(SystemMonitorEntity monitor) {
        Map<String, Object> data = new HashMap<>();
        data.put("timestamp", monitor.getMonitorTime());
        data.put("value", monitor.getMetricValue());
        data.put("unit", monitor.getMetricUnit());
        return data;
    }

    // 以下为模拟方法，实际项目中需要真实的实现
    private double getAverageResponseTime() {
        return Math.random() * 100;
    }

    private double getMaxResponseTime() {
        return Math.random() * 500;
    }

    private double getMinResponseTime() {
        return Math.random() * 10;
    }

    private double getThroughput() {
        return Math.random() * 1000;
    }

    private double getRequestRate() {
        return Math.random() * 100;
    }

    private double getErrorRate() {
        return Math.random() * 5;
    }

    private long getErrorCount() {
        return (long) (Math.random() * 10);
    }

    private int getActiveConnections() {
        return (int) (Math.random() * 200);
    }

    private int getMaxConnections() {
        return 500;
    }

    private double getCacheHitRate() {
        return Math.random() * 100;
    }

    private int getCacheSize() {
        return (int) (Math.random() * 10000);
    }

    private boolean isMemoryPressureHigh() {
        return Math.random() > 0.8;
    }

    private boolean isGcPressureHigh() {
        return Math.random() > 0.85;
    }

    private boolean isThreadCountHigh() {
        return Math.random() > 0.9;
    }

    private boolean isCpuUsageHigh() {
        return Math.random() > 0.7;
    }

    private List<String> generateRecommendations(List<String> bottlenecks) {
        List<String> recommendations = new ArrayList<>();
        for (String bottleneck : bottlenecks) {
            if (bottleneck.contains("内存")) {
                recommendations.add("考虑增加堆内存大小或优化内存使用");
            } else if (bottleneck.contains("GC")) {
                recommendations.add("考虑调整GC参数或优化对象创建");
            } else if (bottleneck.contains("线程")) {
                recommendations.add("检查线程泄漏或优化线程池配置");
            } else if (bottleneck.contains("CPU")) {
                recommendations.add("优化算法或增加计算资源");
            }
        }
        return recommendations;
    }

    private String getInstanceId() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName() + "-" + System.currentTimeMillis();
        } catch (Exception e) {
            return "unknown-instance";
        }
    }
}
