package net.lab1024.sa.attendance.memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 内存监控器
 *
 * 实时监控JVM内存使用情况，提供内存使用报告和预警
 * 支持内存泄漏检测、GC性能监控、内存趋势分析
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Component
public class MemoryMonitor {

    @Resource
    private MemoryOptimizer memoryOptimizer;

    // JVM内存管理器
    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

    // 内存使用历史记录
    private final AtomicReference<MemoryUsageHistory> memoryHistory = new AtomicReference<>(
        new MemoryUsageHistory(1000)
    );

    // 内存统计
    private final AtomicLong totalSamples = new AtomicLong(0);
    private final AtomicLong warningCount = new AtomicLong(0);
    private final AtomicLong criticalCount = new AtomicLong(0);
    private final AtomicLong gcCount = new AtomicLong(0);

    // 配置参数
    private static final long SAMPLE_INTERVAL_MS = 5000; // 5秒采样一次
    private static final double WARNING_RATIO = 0.7;   // 70%警告阈值
    private static final double CRITICAL_RATIO = 0.85;  // 85%临界阈值
    private static final int HISTORY_SIZE = 1000;       // 历史记录大小

    // 定时任务执行器
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, r -> {
        Thread t = new Thread(r, "MemoryMonitor-" + System.currentTimeMillis());
        t.setDaemon(true);
        return t;
    });

    /**
     * 内存使用历史记录
     */
    private static class MemoryUsageHistory {
        private final MemorySample[] samples;
        private volatile int index = 0;
        private volatile int size = 0;

        public MemoryUsageHistory(int capacity) {
            this.samples = new MemorySample[capacity];
        }

        public synchronized void addSample(MemorySample sample) {
            samples[index] = sample;
            index = (index + 1) % samples.length;
            if (size < samples.length) {
                size++;
            }
        }

        public synchronized List<MemorySample> getRecentSamples(int count) {
            List<MemorySample> result = new ArrayList<>();
            int start = Math.max(0, size - count);
            for (int i = start; i < size; i++) {
                int idx = (index - size + i + samples.length) % samples.length;
                result.add(samples[idx]);
            }
            return result;
        }

        public synchronized MemorySample getLatestSample() {
            if (size == 0) return null;
            int idx = (index - 1 + samples.length) % samples.length;
            return samples[idx];
        }

        public synchronized int getSize() {
            return size;
        }

        public synchronized void clear() {
            index = 0;
            size = 0;
        }
    }

    /**
     * 内存使用样本
     */
    public static class MemorySample {
        private final long timestamp;
        private final long heapUsed;
        private final long heapMax;
        private final long nonHeapUsed;
        private final long nonHeapMax;
        private final double heapUsageRatio;
        private final double nonHeapUsageRatio;
        private final int activeThreads;

        public MemorySample(long heapUsed, long heapMax, long nonHeapUsed, long nonHeapMax, int activeThreads) {
            this.timestamp = System.currentTimeMillis();
            this.heapUsed = heapUsed;
            this.heapMax = heapMax;
            this.nonHeapUsed = nonHeapUsed;
            this.nonHeapMax = nonHeapMax;
            this.heapUsageRatio = heapMax > 0 ? (double) heapUsed / heapMax : 0.0;
            this.nonHeapUsageRatio = nonHeapMax > 0 ? (double) nonHeapUsed / nonHeapMax : 0.0;
            this.activeThreads = activeThreads;
        }

        // Getters
        public long getTimestamp() { return timestamp; }
        public long getHeapUsed() { return heapUsed; }
        public long getHeapMax() { return heapMax; }
        public long getNonHeapUsed() { return nonHeapUsed; }
        public long getNonHeapMax() { return nonHeapMax; }
        public double getHeapUsageRatio() { return heapUsageRatio; }
        public double getNonHeapUsageRatio() { return nonHeapUsageRatio; }
        public int getActiveThreads() { return activeThreads; }

        public String getHeapUsedMB() {
            return String.format("%.1f", heapUsed / 1024.0 / 1024.0);
        }

        public String getHeapMaxMB() {
            return String.format("%.1f", heapMax / 1024.0 / 1024.0);
        }

        public String getHeapUsagePercent() {
            return String.format("%.1f%%", heapUsageRatio * 100);
        }
    }

    /**
     * 内存状态枚举
     */
    public enum MemoryStatus {
        NORMAL("正常", 0, 255, 0),
        WARNING("警告", 255, 255, 0),
        CRITICAL("临界", 255, 0, 0),
        OUT_OF_MEMORY("内存不足", 255, 0, 0);

        private final String description;
        private final int red;
        private final int green;
        private final int blue;

        MemoryStatus(String description, int red, int green, int blue) {
            this.description = description;
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        public String getDescription() { return description; }
        public int getRed() { return red; }
        public int getGreen() { return green; }
        public int getBlue() { return blue; }
    }

    /**
     * 启动内存监控
     */
    public void startMonitoring() {
        // 立即采样一次
        sampleMemoryUsage();

        // 定时采样
        scheduler.scheduleAtFixedRate(this::sampleMemoryUsage,
            0, SAMPLE_INTERVAL_MS, TimeUnit.MILLISECONDS);

        // 定时分析和报告
        scheduler.scheduleAtFixedRate(this::analyzeMemoryTrends,
            60, 60, TimeUnit.SECONDS);

        // 定时GC监控
        scheduler.scheduleAtFixedRate(this::monitorGCPauses,
            30, 30, TimeUnit.SECONDS);

        log.info("内存监控已启动，采样间隔: {}ms", SAMPLE_INTERVAL_MS);
    }

    /**
     * 停止内存监控
     */
    public void stopMonitoring() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                scheduler.shutdownNow();
            }
        }
        log.info("内存监控已停止");
    }

    /**
     * 采样内存使用情况
     */
    private void sampleMemoryUsage() {
        try {
            MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
            MemoryUsage nonHeapUsage = memoryMXBean.getNonHeapMemoryUsage();
            int activeThreads = Thread.activeCount();

            MemorySample sample = new MemorySample(
                heapUsage.getUsed(),
                heapUsage.getMax(),
                nonHeapUsage.getUsed(),
                nonHeapUsage.getMax(),
                activeThreads
            );

            memoryHistory.get().addSample(sample);
            totalSamples.incrementAndGet();

            // 检查内存状态
            MemoryStatus status = determineMemoryStatus(sample);
            if (status != MemoryStatus.NORMAL) {
                handleMemoryStatus(status, sample);
            }

            // 记录详细信息（仅在调试模式）
            if (log.isDebugEnabled()) {
                log.debug("内存采样 - 堆内存: {}MB/{}MB ({}%), 非堆内存: {}MB/{}MB ({}%), 活跃线程: {}",
                    sample.getHeapUsedMB(), sample.getHeapMaxMB(), sample.getHeapUsagePercent(),
                    formatMB(nonHeapUsage.getUsed()), formatMB(nonHeapUsage.getMax()),
                    formatPercent((double) nonHeapUsage.getUsed() / nonHeapUsage.getMax()),
                    sample.getActiveThreads());
            }

        } catch (Exception e) {
            log.error("内存采样失败", e);
        }
    }

    /**
     * 分析内存趋势
     */
    private void analyzeMemoryTrends() {
        try {
            MemoryUsageHistory history = memoryHistory.get();
            List<MemorySample> recentSamples = history.getRecentSamples(12); // 最近1分钟

            if (recentSamples.size() < 6) {
                return; // 样本不足，无法分析趋势
            }

            // 计算趋势
            MemoryTrend heapTrend = calculateTrend(recentSamples, MemorySample::getHeapUsageRatio);
            MemoryTrend nonHeapTrend = calculateTrend(recentSamples, MemorySample::getNonHeapUsageRatio);

            // 分析趋势并报告
            analyzeTrendAndReport(heapTrend, "堆内存");
            analyzeTrendAndReport(nonHeapTrend, "非堆内存");

        } catch (Exception e) {
            log.error("内存趋势分析失败", e);
        }
    }

    /**
     * 监控GC暂停
     */
    private void monitorGCPauses() {
        try {
            // 这里可以集成GC监控工具来获取更详细的GC信息
            // 当前实现使用简单的GC计数
            long gcCountBefore = gcCount.get();
            System.gc(); // 建议GC
            long gcCountAfter = gcCount.incrementAndGet();

            if (log.isInfoEnabled() && gcCountAfter > gcCountBefore) {
                log.info("触发GC，累计GC次数: {}", gcCountAfter);
            }

        } catch (Exception e) {
            log.error("GC监控失败", e);
        }
    }

    /**
     * 计算趋势
     */
    private MemoryTrend calculateTrend(List<MemorySample> samples,
                                     java.util.function.Function<MemorySample, Double> valueExtractor) {
        if (samples.size() < 2) {
            return MemoryTrend.STABLE;
        }

        double firstValue = valueExtractor.apply(samples.get(0));
        double lastValue = valueExtractor.apply(samples.get(samples.size() - 1));
        double change = lastValue - firstValue;
        double avgValue = samples.stream()
            .mapToDouble(valueExtractor)
            .average()
            .orElse(0.0);

        // 计算趋势
        double changeRatio = avgValue > 0 ? change / avgValue : 0.0;

        if (changeRatio > 0.05) {
            return MemoryTrend.INCREASING;
        } else if (changeRatio < -0.05) {
            return MemoryTrend.DECREASING;
        } else {
            return MemoryTrend.STABLE;
        }
    }

    /**
     * 分析趋势并报告
     */
    private void analyzeTrendAndReport(MemoryTrend trend, String memoryType) {
        switch (trend) {
            case INCREASING:
                log.warn("{}使用呈上升趋势，建议检查内存泄漏", memoryType);
                break;
            case DECREASING:
                log.info("{}使用呈下降趋势", memoryType);
                break;
            case STABLE:
                log.debug("{}使用稳定", memoryType);
                break;
            case VOLATILE:
                log.warn("{}使用波动较大，可能存在不稳定因素", memoryType);
                break;
        }
    }

    /**
     * 确定内存状态
     */
    private MemoryStatus determineMemoryStatus(MemorySample sample) {
        if (sample.getHeapUsageRatio() >= CRITICAL_RATIO) {
            return MemoryStatus.CRITICAL;
        } else if (sample.getHeapUsageRatio() >= WARNING_RATIO) {
            return MemoryStatus.WARNING;
        } else if (sample.getHeapUsageRatio() >= 0.95) {
            return MemoryStatus.OUT_OF_MEMORY;
        } else {
            return MemoryStatus.NORMAL;
        }
    }

    /**
     * 处理内存状态
     */
    private void handleMemoryStatus(MemoryStatus status, MemorySample sample) {
        switch (status) {
            case WARNING:
                warningCount.incrementAndGet();
                log.warn("内存使用警告 - 堆内存: {}%, 活跃线程: {}",
                    sample.getHeapUsagePercent(), sample.getActiveThreads());
                break;
            case CRITICAL:
                criticalCount.incrementAndGet();
                log.error("内存使用临界 - 堆内存: {}%, 活跃线程: {}",
                    sample.getHeapUsagePercent(), sample.getActiveThreads());
                triggerCriticalMemoryHandling();
                break;
            case OUT_OF_MEMORY:
                criticalCount.incrementAndGet();
                log.error("内存即将耗尽 - 堆内存: {}%, 活跃线程: {}",
                    sample.getHeapUsagePercent(), sample.getActiveThreads());
                triggerOutOfMemoryHandling();
                break;
        }
    }

    /**
     * 触发临界内存处理
     */
    private void triggerCriticalMemoryHandling() {
        try {
            log.warn("触发临界内存处理机制");

            // 通知内存优化器执行紧急优化
            if (memoryOptimizer != null) {
                memoryOptimizer.checkMemoryUsage();
            }

            // 可以添加其他处理逻辑，如：
            // - 发送告警通知
            // - 触发服务降级
            // - 清理缓存等

        } catch (Exception e) {
            log.error("临界内存处理失败", e);
        }
    }

    /**
     * 触发内存不足处理
     */
    private void triggerOutOfMemoryHandling() {
        try {
            log.error("触发内存不足处理机制");

            // 通知内存优化器执行紧急优化
            if (memoryOptimizer != null) {
                memoryOptimizer.checkMemoryUsage();
            }

            // 强制GC
            System.gc();
            Thread.sleep(200);

        } catch (Exception e) {
            log.error("内存不足处理失败", e);
        }
    }

    /**
     * 获取内存使用报告
     */
    public MemoryReport getMemoryReport() {
        MemorySample latest = memoryHistory.get().getLatestSample();
        if (latest == null) {
            return MemoryReport.builder()
                .status(MemoryStatus.NORMAL)
                .totalSamples(0)
                .warningCount(0)
                .criticalCount(0)
                .gcCount(gcCount.get())
                .build();
        }

        return MemoryReport.builder()
            .heapUsed(latest.getHeapUsed())
            .heapMax(latest.getHeapMax())
            .heapUsageRatio(latest.getHeapUsageRatio())
            .nonHeapUsed(latest.getNonHeapUsed())
            .nonHeapMax(latest.getNonHeapMax())
            .nonHeapUsageRatio(latest.getNonHeapUsageRatio())
            .activeThreads(latest.getActiveThreads())
            .status(determineMemoryStatus(latest))
            .totalSamples(totalSamples.get())
            .warningCount(warningCount.get())
            .criticalCount(criticalCount.get())
            .gcCount(gcCount.get())
            .memoryTrend(calculateMemoryTrend())
            .build();
    }

    /**
     * 计算内存趋势
     */
    private MemoryTrend calculateMemoryTrend() {
        MemoryUsageHistory history = memoryHistory.get();
        List<MemorySample> recentSamples = history.getRecentSamples(12);

        if (recentSamples.size() < 6) {
            return MemoryTrend.STABLE;
        }

        MemoryTrend heapTrend = calculateTrend(recentSamples, MemorySample::getHeapUsageRatio);
        MemoryTrend nonHeapTrend = calculateTrend(recentSamples, MemorySample::getNonHeapUsageRatio);

        // 综合趋势判断
        if (heapTrend == MemoryTrend.INCREASING || nonHeapTrend == MemoryTrend.INCREASING) {
            return MemoryTrend.INCREASING;
        } else if (heapTrend == MemoryTrend.DECREASING && nonHeapTrend == MemoryTrend.DECREASING) {
            return MemoryTrend.DECREASING;
        } else {
            return MemoryTrend.VOLATILE;
        }
    }

    /**
     * 内存报告类
     */
    @lombok.Builder
    @lombok.Data
    public static class MemoryReport {
        private long heapUsed;
        private long heapMax;
        private double heapUsageRatio;
        private long nonHeapUsed;
        private long nonHeapMax;
        private double nonHeapUsageRatio;
        private int activeThreads;
        private MemoryStatus status;
        private long totalSamples;
        private long warningCount;
        private long criticalCount;
        private long gcCount;
        private MemoryTrend memoryTrend;

        public String getHeapUsedMB() {
            return formatMB(heapUsed);
        }

        public String getHeapMaxMB() {
            return formatMB(heapMax);
        }

        public String getHeapUsagePercent() {
            return formatPercent(heapUsageRatio);
        }

        public String getNonHeapUsedMB() {
            return formatMB(nonHeapUsed);
        }

        public String getNonHeapMaxMB() {
            return formatMB(nonHeapMax);
        }

        public String getNonHeapUsagePercent() {
            return formatPercent(nonHeapUsageRatio);
        }

        public long getTimestamp() {
            return System.currentTimeMillis();
        }
    }

    /**
     * 内存趋势枚举
     */
    public enum MemoryTrend {
        INCREASING("上升"),
        DECREASING("下降"),
        STABLE("稳定"),
        VOLATILE("波动");

        private final String description;

        MemoryTrend(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 格式化MB
     */
    private static String formatMB(long bytes) {
        return String.format("%.1f MB", bytes / 1024.0 / 1024.0);
    }

    /**
     * 格式化百分比
     */
    private static String formatPercent(double ratio) {
        return String.format("%.1f%%", ratio * 100);
    }

    /**
     * 销毁资源
     */
    public void destroy() {
        stopMonitoring();
        if (memoryHistory != null) {
            memoryHistory.get().clear();
        }
        log.info("内存监控器已销毁");
    }
}