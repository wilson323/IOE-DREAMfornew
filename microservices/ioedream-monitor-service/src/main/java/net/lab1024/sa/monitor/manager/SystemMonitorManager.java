package net.lab1024.sa.monitor.manager;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.monitor.dao.SystemMonitorDao;
import net.lab1024.sa.monitor.domain.entity.SystemMonitorEntity;
import net.lab1024.sa.monitor.domain.vo.ResourceUsageVO;

/**
 * 系统监控管理器
 *
 * 负责系统资源监控、性能数据收集、监控数据存储等功能
 *
 * @author IOE-DREAM Team
 */
@Slf4j
@Component
public class SystemMonitorManager {

    @Resource
    private SystemMonitorDao systemMonitorDao;

    // 异步执行器
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    // JVM相关Bean
    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private final OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();
    private final com.sun.management.OperatingSystemMXBean sunOsMXBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory
            .getOperatingSystemMXBean();

    /**
     * 收集系统监控数据
     *
     * @param serviceName 服务名称
     * @param instanceId  实例ID
     */
    public void collectSystemMetrics(String serviceName, String instanceId) {
        log.debug("开始收集系统监控数据，服务：{}，实例：{}", serviceName, instanceId);

        try {
            // 异步收集各种监控指标
            CompletableFuture<Void> cpuFuture = CompletableFuture
                    .runAsync(() -> collectCpuMetrics(serviceName, instanceId), executorService);
            CompletableFuture<Void> memoryFuture = CompletableFuture
                    .runAsync(() -> collectMemoryMetrics(serviceName, instanceId), executorService);
            CompletableFuture<Void> diskFuture = CompletableFuture
                    .runAsync(() -> collectDiskMetrics(serviceName, instanceId), executorService);
            CompletableFuture<Void> networkFuture = CompletableFuture
                    .runAsync(() -> collectNetworkMetrics(serviceName, instanceId), executorService);
            CompletableFuture<Void> jvmFuture = CompletableFuture
                    .runAsync(() -> collectJvmMetrics(serviceName, instanceId), executorService);

            // 等待所有指标收集完成
            CompletableFuture.allOf(cpuFuture, memoryFuture, diskFuture, networkFuture, jvmFuture)
                    .thenRun(() -> log.debug("系统监控数据收集完成"))
                    .exceptionally(throwable -> {
                        log.error("收集系统监控数据失败", throwable);
                        return null;
                    });

        } catch (Exception e) {
            log.error("收集系统监控数据异常，服务：{}，实例：{}", serviceName, instanceId, e);
        }
    }

    /**
     * 获取系统资源使用情况
     *
     * @return 资源使用情况
     */
    public ResourceUsageVO getResourceUsage() {
        log.debug("获取系统资源使用情况");

        ResourceUsageVO resourceUsage = new ResourceUsageVO();

        try {
            // CPU使用率
            double cpuUsage = getCpuUsage();
            resourceUsage.setCpuUsage(cpuUsage);

            // CPU核心数
            int cpuCores = osMXBean.getAvailableProcessors();
            resourceUsage.setCpuCores(cpuCores);

            // CPU负载
            double cpuLoad = getCpuLoad();
            resourceUsage.setCpuLoad(cpuLoad);

            // 内存使用情况
            long totalMemory = getTotalMemoryMB();
            long usedMemory = getUsedMemoryMB();
            long availableMemory = totalMemory - usedMemory;
            double memoryUsage = totalMemory > 0 ? (double) usedMemory / totalMemory * 100 : 0;

            resourceUsage.setTotalMemory(totalMemory / 1024.0); // 转换为GB
            resourceUsage.setUsedMemory(usedMemory / 1024.0);
            resourceUsage.setAvailableMemory(availableMemory / 1024.0);
            resourceUsage.setMemoryUsage(memoryUsage);

            // 磁盘使用情况
            double totalDisk = getTotalDiskSpaceGB();
            double usedDisk = getUsedDiskSpaceGB();
            double availableDisk = totalDisk - usedDisk;
            double diskUsage = totalDisk > 0 ? usedDisk / totalDisk * 100 : 0;

            resourceUsage.setTotalDisk(totalDisk);
            resourceUsage.setUsedDisk(usedDisk);
            resourceUsage.setAvailableDisk(availableDisk);
            resourceUsage.setDiskUsage(diskUsage);

            // 网络流量（模拟）
            resourceUsage.setNetworkInbound(getNetworkInboundMBps());
            resourceUsage.setNetworkOutbound(getNetworkOutboundMBps());

            // 线程信息
            resourceUsage.setTotalThreads(getTotalThreadCount());
            resourceUsage.setActiveThreads(getActiveThreadCount());

            // JVM内存信息
            double heapUsage = getHeapMemoryUsagePercent();
            double nonHeapUsage = getNonHeapMemoryUsagePercent();

            resourceUsage.setHeapMemoryUsage(heapUsage);
            resourceUsage.setNonHeapMemoryUsage(nonHeapUsage);

            // GC信息（模拟）
            resourceUsage.setGcCount(getGcCount());
            resourceUsage.setGcTime(getGcTime());

            log.debug("系统资源使用情况获取完成");

        } catch (Exception e) {
            log.error("获取系统资源使用情况失败", e);
        }

        return resourceUsage;
    }

    /**
     * 获取系统性能指标
     *
     * @return 性能指标
     */
    public Map<String, Object> getSystemMetrics() {
        log.debug("获取系统性能指标");

        Map<String, Object> metrics = new HashMap<>();

        try {
            // 系统信息
            metrics.put("osName", System.getProperty("os.name"));
            metrics.put("osVersion", System.getProperty("os.version"));
            metrics.put("osArch", System.getProperty("os.arch"));
            metrics.put("hostname", getHostname());
            metrics.put("javaVersion", System.getProperty("java.version"));
            metrics.put("javaVendor", System.getProperty("java.vendor"));

            // 运行时信息
            long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
            metrics.put("uptime", uptime);
            metrics.put("startTime", ManagementFactory.getRuntimeMXBean().getStartTime());

            // CPU信息
            metrics.put("cpuUsage", getCpuUsage());
            metrics.put("cpuLoad", getCpuLoad());
            metrics.put("cpuCores", osMXBean.getAvailableProcessors());

            // 内存信息
            ResourceUsageVO resourceUsage = getResourceUsage();
            metrics.put("memoryUsage", resourceUsage.getMemoryUsage());
            metrics.put("totalMemory", resourceUsage.getTotalMemory());
            metrics.put("usedMemory", resourceUsage.getUsedMemory());

            // JVM信息
            metrics.put("heapMemoryUsage", getHeapMemoryUsagePercent());
            metrics.put("nonHeapMemoryUsage", getNonHeapMemoryUsagePercent());
            metrics.put("gcCount", getGcCount());
            metrics.put("gcTime", getGcTime());

            // 线程信息
            metrics.put("totalThreads", getTotalThreadCount());
            metrics.put("activeThreads", getActiveThreadCount());

            log.debug("系统性能指标获取完成");

        } catch (Exception e) {
            log.error("获取系统性能指标失败", e);
        }

        return metrics;
    }

    /**
     * 获取监控数据统计
     *
     * @param serviceName 服务名称
     * @param monitorType 监控类型
     * @param hours       时间范围（小时）
     * @return 统计结果
     */
    public Map<String, Object> getMonitoringStatistics(String serviceName, String monitorType, Integer hours) {
        log.debug("获取监控数据统计，服务：{}，类型：{}，时间范围：{}小时", serviceName, monitorType, hours);

        try {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(hours);

            return systemMonitorDao.selectMetricStats(serviceName, monitorType, startTime, endTime);

        } catch (Exception e) {
            log.error("获取监控数据统计失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 清理历史监控数据
     *
     * @param days 保留天数
     */
    public void cleanHistoryMonitoringData(Integer days) {
        log.info("开始清理{}天前的历史监控数据", days);

        try {
            LocalDateTime beforeTime = LocalDateTime.now().minusDays(days);
            int deletedCount = systemMonitorDao.deleteHistoryData(beforeTime);

            log.info("历史监控数据清理完成，删除记录数：{}", deletedCount);

        } catch (Exception e) {
            log.error("清理历史监控数据失败", e);
        }
    }

    // 以下为私有方法，实现具体的监控指标收集

    private void collectCpuMetrics(String serviceName, String instanceId) {
        try {
            double cpuUsage = getCpuUsage();
            double cpuLoad = getCpuLoad();

            saveMonitorData(serviceName, instanceId, "CPU", "usage", cpuUsage, "%");
            saveMonitorData(serviceName, instanceId, "CPU", "load", cpuLoad, null);

        } catch (Exception e) {
            log.error("收集CPU监控数据失败", e);
        }
    }

    private void collectMemoryMetrics(String serviceName, String instanceId) {
        try {
            double memoryUsage = getMemoryUsagePercent();
            long totalMemory = getTotalMemoryMB();
            long usedMemory = getUsedMemoryMB();

            saveMonitorData(serviceName, instanceId, "MEMORY", "usage", memoryUsage, "%");
            saveMonitorData(serviceName, instanceId, "MEMORY", "total", (double) totalMemory, "MB");
            saveMonitorData(serviceName, instanceId, "MEMORY", "used", (double) usedMemory, "MB");

        } catch (Exception e) {
            log.error("收集内存监控数据失败", e);
        }
    }

    private void collectDiskMetrics(String serviceName, String instanceId) {
        try {
            double diskUsage = getDiskUsagePercent();
            double totalDisk = getTotalDiskSpaceGB();
            double usedDisk = getUsedDiskSpaceGB();

            saveMonitorData(serviceName, instanceId, "DISK", "usage", diskUsage, "%");
            saveMonitorData(serviceName, instanceId, "DISK", "total", totalDisk, "GB");
            saveMonitorData(serviceName, instanceId, "DISK", "used", usedDisk, "GB");

        } catch (Exception e) {
            log.error("收集磁盘监控数据失败", e);
        }
    }

    private void collectNetworkMetrics(String serviceName, String instanceId) {
        try {
            double inbound = getNetworkInboundMBps();
            double outbound = getNetworkOutboundMBps();

            saveMonitorData(serviceName, instanceId, "NETWORK", "inbound", inbound, "MB/s");
            saveMonitorData(serviceName, instanceId, "NETWORK", "outbound", outbound, "MB/s");

        } catch (Exception e) {
            log.error("收集网络监控数据失败", e);
        }
    }

    private void collectJvmMetrics(String serviceName, String instanceId) {
        try {
            double heapUsage = getHeapMemoryUsagePercent();
            double nonHeapUsage = getNonHeapMemoryUsagePercent();
            long gcCount = getGcCount();
            long gcTime = getGcTime();

            saveMonitorData(serviceName, instanceId, "JVM", "heap_usage", heapUsage, "%");
            saveMonitorData(serviceName, instanceId, "JVM", "nonheap_usage", nonHeapUsage, "%");
            saveMonitorData(serviceName, instanceId, "JVM", "gc_count", (double) gcCount, "count");
            saveMonitorData(serviceName, instanceId, "JVM", "gc_time", (double) gcTime, "ms");

        } catch (Exception e) {
            log.error("收集JVM监控数据失败", e);
        }
    }

    private void saveMonitorData(String serviceName, String instanceId, String monitorType,
            String metricName, Double metricValue, String metricUnit) {
        SystemMonitorEntity monitor = new SystemMonitorEntity();
        monitor.setServiceName(serviceName);
        monitor.setInstanceId(instanceId);
        monitor.setMonitorType(monitorType);
        monitor.setMetricName(metricName);
        monitor.setMetricValue(metricValue);
        monitor.setMetricUnit(metricUnit);
        monitor.setMonitorTime(LocalDateTime.now());
        monitor.setStatus("NORMAL");
        monitor.setCreateTime(LocalDateTime.now());
        monitor.setDeletedFlag(0);

        systemMonitorDao.insert(monitor);
    }

    // 以下为模拟方法，实际项目中需要真实的实现
    private double getCpuUsage() {
        return Math.random() * 100;
    }

    private double getCpuLoad() {
        return Math.random() * osMXBean.getAvailableProcessors();
    }

    @SuppressWarnings("deprecation")
    private long getTotalMemoryMB() {
        if (sunOsMXBean != null) {
            return sunOsMXBean.getTotalPhysicalMemorySize() / (1024 * 1024);
        }
        // 如果无法获取物理内存，返回JVM最大内存
        return Runtime.getRuntime().maxMemory() / (1024 * 1024);
    }

    private long getUsedMemoryMB() {
        return (long) (getTotalMemoryMB() * (Math.random() * 0.8 + 0.1));
    }

    private double getMemoryUsagePercent() {
        long total = getTotalMemoryMB();
        long used = getUsedMemoryMB();
        return total > 0 ? (double) used / total * 100 : 0;
    }

    private double getTotalDiskSpaceGB() {
        return 500.0; // 模拟500GB
    }

    private double getUsedDiskSpaceGB() {
        return getTotalDiskSpaceGB() * (Math.random() * 0.7 + 0.1);
    }

    private double getDiskUsagePercent() {
        double total = getTotalDiskSpaceGB();
        double used = getUsedDiskSpaceGB();
        return total > 0 ? used / total * 100 : 0;
    }

    private double getNetworkInboundMBps() {
        return Math.random() * 100;
    }

    private double getNetworkOutboundMBps() {
        return Math.random() * 80;
    }

    private int getTotalThreadCount() {
        return Thread.activeCount();
    }

    private int getActiveThreadCount() {
        return (int) (getTotalThreadCount() * (Math.random() * 0.8 + 0.2));
    }

    private double getHeapMemoryUsagePercent() {
        return memoryMXBean.getHeapMemoryUsage().getUsed() * 100.0 / memoryMXBean.getHeapMemoryUsage().getMax();
    }

    private double getNonHeapMemoryUsagePercent() {
        long max = memoryMXBean.getNonHeapMemoryUsage().getMax();
        return max > 0 ? memoryMXBean.getNonHeapMemoryUsage().getUsed() * 100.0 / max : 0;
    }

    private long getGcCount() {
        return (long) (Math.random() * 1000);
    }

    private long getGcTime() {
        return (long) (Math.random() * 5000);
    }

    private String getHostname() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
