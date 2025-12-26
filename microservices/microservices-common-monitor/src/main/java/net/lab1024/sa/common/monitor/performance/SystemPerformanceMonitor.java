package net.lab1024.sa.common.monitor.performance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 系统性能监控器
 * <p>
 * 严格遵循四层架构规范（Controller→Service→Manager→DAO）
 * 负责系统级别的性能监控和指标收集
 * </p>
 * <p>
 * 核心职责：
 * - CPU使用率监控
 * - 内存使用率监控
 * - 线程数监控
 * - GC监控
 * - 性能阈值告警
 * - 性能趋势分析
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Component
@Slf4j
public class SystemPerformanceMonitor {

    // JVM监控Bean
    private final OperatingSystemMXBean osMXBean;
    private final MemoryMXBean memoryMXBean;
    private final ThreadMXBean threadMXBean;

    // 定时任务线程池
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    // 性能指标缓存
    private final Map<String, PerformanceMetric> metricsCache = new ConcurrentHashMap<>();
    private final Queue<PerformanceSnapshot> snapshotHistory = new LinkedList<>();

    // 统计计数器
    private final AtomicLong totalCpuUsage = new AtomicLong(0);
    private final AtomicLong sampleCount = new AtomicLong(0);

    // 配置参数
    private static final long MONITOR_INTERVAL_SECONDS = 30;
    private static final int SNAPSHOT_HISTORY_SIZE = 144; // 保留72小时（30秒间隔）
    private static final double CPU_ALERT_THRESHOLD = 80.0; // CPU告警阈值80%
    private static final double MEMORY_ALERT_THRESHOLD = 85.0; // 内存告警阈值85%

    /**
     * 构造函数
     */
    public SystemPerformanceMonitor() {
        this.osMXBean = ManagementFactory.getOperatingSystemMXBean();
        this.memoryMXBean = ManagementFactory.getMemoryMXBean();
        this.threadMXBean = ManagementFactory.getThreadMXBean();

        log.info("[性能监控] 系统性能监控器初始化完成");

        // 启动定时监控任务
        startPeriodicMonitoring();
    }

    /**
     * 启动定时监控
     */
    public void startPeriodicMonitoring() {
        log.info("[性能监控] 启动定时监控任务: interval={}s", MONITOR_INTERVAL_SECONDS);

        // 定期收集性能指标
        scheduler.scheduleAtFixedRate(() -> {
            try {
                collectPerformanceMetrics();
            } catch (Exception e) {
                log.error("[性能监控] 收集性能指标失败", e);
            }
        }, MONITOR_INTERVAL_SECONDS, MONITOR_INTERVAL_SECONDS, TimeUnit.SECONDS);

        // 定期清理历史快照
        scheduler.scheduleAtFixedRate(() -> {
            try {
                cleanupOldSnapshots();
            } catch (Exception e) {
                log.error("[性能监控] 清理历史快照失败", e);
            }
        }, 1, 1, TimeUnit.HOURS);
    }

    /**
     * 收集性能指标
     */
    public PerformanceSnapshot collectPerformanceMetrics() {
        PerformanceSnapshot snapshot = new PerformanceSnapshot();
        snapshot.setTimestamp(LocalDateTime.now());

        try {
            // 1. CPU使用率
            double cpuUsage = getCpuUsage();
            snapshot.setCpuUsage(cpuUsage);
            totalCpuUsage.addAndGet((long) cpuUsage);
            sampleCount.incrementAndGet();

            // 2. 内存使用情况
            MemoryUsage heapMemory = memoryMXBean.getHeapMemoryUsage();
            MemoryUsage nonHeapMemory = memoryMXBean.getNonHeapMemoryUsage();

            snapshot.setHeapMemoryUsed(heapMemory.getUsed());
            snapshot.setHeapMemoryMax(heapMemory.getMax());
            snapshot.setHeapMemoryUsage(calculateUsagePercent(heapMemory));

            snapshot.setNonHeapMemoryUsed(nonHeapMemory.getUsed());
            snapshot.setNonHeapMemoryMax(nonHeapMemory.getMax());

            // 3. 线程信息
            snapshot.setThreadCount(threadMXBean.getThreadCount());
            snapshot.setPeakThreadCount(threadMXBean.getPeakThreadCount());
            snapshot.setDaemonThreadCount(threadMXBean.getDaemonThreadCount());

            // 4. 系统信息
            snapshot.setAvailableProcessors(osMXBean.getAvailableProcessors());
            snapshot.setSystemLoadAverage(osMXBean.getSystemLoadAverage());

            // 5. GC信息
            collectGCMetrics(snapshot);

            // 6. 保存快照
            addSnapshot(snapshot);

            // 7. 检查告警
            checkPerformanceAlerts(snapshot);

            log.debug("[性能监控] 收集性能指标成功: CPU={}%, Memory={}%, Threads={}",
                    String.format("%.2f", cpuUsage),
                    String.format("%.2f", snapshot.getHeapMemoryUsage()),
                    snapshot.getThreadCount());

            return snapshot;

        } catch (Exception e) {
            log.error("[性能监控] 收集性能指标异常", e);
            snapshot.setCollectError(e.getMessage());
            return snapshot;
        }
    }

    /**
     * 获取CPU使用率
     */
    private double getCpuUsage() {
        try {
            if (osMXBean instanceof com.sun.management.OperatingSystemMXBean) {
                com.sun.management.OperatingSystemMXBean sunOsMXBean =
                        (com.sun.management.OperatingSystemMXBean) osMXBean;
                return sunOsMXBean.getProcessCpuLoad() * 100;
            } else {
                // 兼容性处理：使用系统负载平均值
                double loadAverage = osMXBean.getSystemLoadAverage();
                int processors = osMXBean.getAvailableProcessors();
                return (loadAverage / processors) * 100;
            }
        } catch (Exception e) {
            log.warn("[性能监控] 获取CPU使用率失败，使用估算值", e);
            // 估算：系统负载 / CPU核心数
            double loadAverage = osMXBean.getSystemLoadAverage();
            int processors = osMXBean.getAvailableProcessors();
            return loadAverage > 0 ? (loadAverage / processors) * 100 : 0;
        }
    }

    /**
     * 收集GC指标
     */
    private void collectGCMetrics(PerformanceSnapshot snapshot) {
        try {
            // 获取所有GC Bean
            List<java.lang.management.GarbageCollectorMXBean> gcBeans =
                    ManagementFactory.getGarbageCollectorMXBeans();

            long totalGcCount = 0;
            long totalGcTime = 0;

            for (java.lang.management.GarbageCollectorMXBean gcBean : gcBeans) {
                totalGcCount += gcBean.getCollectionCount();
                totalGcTime += gcBean.getCollectionTime();
            }

            snapshot.setGcCount(totalGcCount);
            snapshot.setGcTime(totalGcTime);

        } catch (Exception e) {
            log.warn("[性能监控] 收集GC指标失败", e);
        }
    }

    /**
     * 检查性能告警
     */
    private void checkPerformanceAlerts(PerformanceSnapshot snapshot) {
        List<String> alerts = new ArrayList<>();

        // CPU告警
        if (snapshot.getCpuUsage() > CPU_ALERT_THRESHOLD) {
            String alert = String.format("CPU使用率过高: %.2f%% (阈值: %.2f%%)",
                    snapshot.getCpuUsage(), CPU_ALERT_THRESHOLD);
            alerts.add(alert);
            log.warn("[性能告警] {}", alert);
        }

        // 内存告警
        if (snapshot.getHeapMemoryUsage() > MEMORY_ALERT_THRESHOLD) {
            String alert = String.format("堆内存使用率过高: %.2f%% (阈值: %.2f%%)",
                    snapshot.getHeapMemoryUsage(), MEMORY_ALERT_THRESHOLD);
            alerts.add(alert);
            log.warn("[性能告警] {}", alert);
        }

        // 线程数告警
        if (snapshot.getThreadCount() > 500) {
            String alert = String.format("线程数过高: %d", snapshot.getThreadCount());
            alerts.add(alert);
            log.warn("[性能告警] {}", alert);
        }

        snapshot.setAlerts(alerts);
    }

    /**
     * 添加快照
     */
    private synchronized void addSnapshot(PerformanceSnapshot snapshot) {
        snapshotHistory.add(snapshot);

        // 限制历史大小
        while (snapshotHistory.size() > SNAPSHOT_HISTORY_SIZE) {
            snapshotHistory.poll();
        }
    }

    /**
     * 清理旧快照
     */
    private void cleanupOldSnapshots() {
        log.debug("[性能监控] 清理旧快照: currentSize={}", snapshotHistory.size());

        // 已经在addSnapshot中限制了大小，这里可以定期清理
        LocalDateTime cutoff = LocalDateTime.now().minusHours(72);

        snapshotHistory.removeIf(snapshot ->
                snapshot.getTimestamp().isBefore(cutoff)
        );

        log.debug("[性能监控] 清理完成: size={}", snapshotHistory.size());
    }

    /**
     * 计算使用率百分比
     */
    private double calculateUsagePercent(MemoryUsage usage) {
        long used = usage.getUsed();
        long max = usage.getMax();
        return max > 0 ? (used * 100.0 / max) : 0;
    }

    /**
     * 获取当前性能快照
     */
    public PerformanceSnapshot getCurrentSnapshot() {
        return collectPerformanceMetrics();
    }

    /**
     * 获取性能摘要
     */
    public PerformanceSummary getPerformanceSummary() {
        PerformanceSnapshot latest = getCurrentSnapshot();

        PerformanceSummary summary = new PerformanceSummary();
        summary.setLatestSnapshot(latest);
        summary.setAverageCpuUsage(calculateAverageCpuUsage());
        summary.setSnapshotCount(snapshotHistory.size());

        // 计算趋势
        summary.setCpuTrend(calculateTrend("cpu"));
        summary.setMemoryTrend(calculateTrend("memory"));

        return summary;
    }

    /**
     * 计算平均CPU使用率
     */
    private double calculateAverageCpuUsage() {
        long count = sampleCount.get();
        return count > 0 ? (double) totalCpuUsage.get() / count : 0;
    }

    /**
     * 计算趋势
     */
    private String calculateTrend(String metricType) {
        if (snapshotHistory.size() < 2) {
            return "UNKNOWN";
        }

        PerformanceSnapshot[] snapshots = snapshotHistory.toArray(new PerformanceSnapshot[0]);
        int n = Math.min(snapshots.length, 10); // 最近10个样本

        double recent = 0;
        double older = 0;

        if ("cpu".equals(metricType)) {
            for (int i = 0; i < n / 2; i++) {
                older += snapshots[n - 1 - i].getCpuUsage();
            }
            for (int i = n / 2; i < n; i++) {
                recent += snapshots[n - 1 - i].getCpuUsage();
            }
        } else if ("memory".equals(metricType)) {
            for (int i = 0; i < n / 2; i++) {
                older += snapshots[n - 1 - i].getHeapMemoryUsage();
            }
            for (int i = n / 2; i < n; i++) {
                recent += snapshots[n - 1 - i].getHeapMemoryUsage();
            }
        }

        double recentAvg = recent / (n / 2);
        double olderAvg = older / (n / 2);

        if (recentAvg > olderAvg * 1.1) {
            return "RISING"; // 上升
        } else if (recentAvg < olderAvg * 0.9) {
            return "FALLING"; // 下降
        } else {
            return "STABLE"; // 稳定
        }
    }

    /**
     * 获取历史快照列表
     */
    public List<PerformanceSnapshot> getSnapshotHistory() {
        return new ArrayList<>(snapshotHistory);
    }

    /**
     * 关闭监控器
     */
    public void shutdown() {
        log.info("[性能监控] 关闭性能监控器");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // ==================== 内部类 ====================

    /**
     * 性能快照
     */
    public static class PerformanceSnapshot {
        private LocalDateTime timestamp;
        private double cpuUsage;
        private long heapMemoryUsed;
        private long heapMemoryMax;
        private double heapMemoryUsage;
        private long nonHeapMemoryUsed;
        private long nonHeapMemoryMax;
        private int threadCount;
        private int peakThreadCount;
        private int daemonThreadCount;
        private int availableProcessors;
        private double systemLoadAverage;
        private long gcCount;
        private long gcTime;
        private List<String> alerts;
        private String collectError;

        // Getters and Setters
        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public double getCpuUsage() {
            return cpuUsage;
        }

        public void setCpuUsage(double cpuUsage) {
            this.cpuUsage = cpuUsage;
        }

        public long getHeapMemoryUsed() {
            return heapMemoryUsed;
        }

        public void setHeapMemoryUsed(long heapMemoryUsed) {
            this.heapMemoryUsed = heapMemoryUsed;
        }

        public long getHeapMemoryMax() {
            return heapMemoryMax;
        }

        public void setHeapMemoryMax(long heapMemoryMax) {
            this.heapMemoryMax = heapMemoryMax;
        }

        public double getHeapMemoryUsage() {
            return heapMemoryUsage;
        }

        public void setHeapMemoryUsage(double heapMemoryUsage) {
            this.heapMemoryUsage = heapMemoryUsage;
        }

        public long getNonHeapMemoryUsed() {
            return nonHeapMemoryUsed;
        }

        public void setNonHeapMemoryUsed(long nonHeapMemoryUsed) {
            this.nonHeapMemoryUsed = nonHeapMemoryUsed;
        }

        public long getNonHeapMemoryMax() {
            return nonHeapMemoryMax;
        }

        public void setNonHeapMemoryMax(long nonHeapMemoryMax) {
            this.nonHeapMemoryMax = nonHeapMemoryMax;
        }

        public int getThreadCount() {
            return threadCount;
        }

        public void setThreadCount(int threadCount) {
            this.threadCount = threadCount;
        }

        public int getPeakThreadCount() {
            return peakThreadCount;
        }

        public void setPeakThreadCount(int peakThreadCount) {
            this.peakThreadCount = peakThreadCount;
        }

        public int getDaemonThreadCount() {
            return daemonThreadCount;
        }

        public void setDaemonThreadCount(int daemonThreadCount) {
            this.daemonThreadCount = daemonThreadCount;
        }

        public int getAvailableProcessors() {
            return availableProcessors;
        }

        public void setAvailableProcessors(int availableProcessors) {
            this.availableProcessors = availableProcessors;
        }

        public double getSystemLoadAverage() {
            return systemLoadAverage;
        }

        public void setSystemLoadAverage(double systemLoadAverage) {
            this.systemLoadAverage = systemLoadAverage;
        }

        public long getGcCount() {
            return gcCount;
        }

        public void setGcCount(long gcCount) {
            this.gcCount = gcCount;
        }

        public long getGcTime() {
            return gcTime;
        }

        public void setGcTime(long gcTime) {
            this.gcTime = gcTime;
        }

        public List<String> getAlerts() {
            return alerts;
        }

        public void setAlerts(List<String> alerts) {
            this.alerts = alerts;
        }

        public String getCollectError() {
            return collectError;
        }

        public void setCollectError(String collectError) {
            this.collectError = collectError;
        }
    }

    /**
     * 性能摘要
     */
    public static class PerformanceSummary {
        private PerformanceSnapshot latestSnapshot;
        private double averageCpuUsage;
        private String cpuTrend;
        private String memoryTrend;
        private int snapshotCount;

        // Getters and Setters
        public PerformanceSnapshot getLatestSnapshot() {
            return latestSnapshot;
        }

        public void setLatestSnapshot(PerformanceSnapshot latestSnapshot) {
            this.latestSnapshot = latestSnapshot;
        }

        public double getAverageCpuUsage() {
            return averageCpuUsage;
        }

        public void setAverageCpuUsage(double averageCpuUsage) {
            this.averageCpuUsage = averageCpuUsage;
        }

        public String getCpuTrend() {
            return cpuTrend;
        }

        public void setCpuTrend(String cpuTrend) {
            this.cpuTrend = cpuTrend;
        }

        public String getMemoryTrend() {
            return memoryTrend;
        }

        public void setMemoryTrend(String memoryTrend) {
            this.memoryTrend = memoryTrend;
        }

        public int getSnapshotCount() {
            return snapshotCount;
        }

        public void setSnapshotCount(int snapshotCount) {
            this.snapshotCount = snapshotCount;
        }
    }

    /**
     * 性能指标
     */
    public static class PerformanceMetric {
        private String name;
        private double value;
        private String unit;
        private LocalDateTime timestamp;

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }
}
