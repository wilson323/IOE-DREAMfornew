package net.lab1024.sa.common.performance;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.performance.model.*;

import java.lang.management.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * JVM性能管理器
 * <p>
 * 提供JVM性能监控、分析和优化建议
 * 支持内存、GC、线程、类加载等全方位监控
 * 提供性能告警和诊断功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Slf4j
public class JvmPerformanceManager {

    // JVM监控指标
    private final MemoryMXBean memoryMXBean;
    private final List<GarbageCollectorMXBean> gcMXBeans;
    private final ThreadMXBean threadMXBean;
    private final ClassLoadingMXBean classLoadingMXBean;
    private final CompilationMXBean compilationMXBean;
    private final OperatingSystemMXBean osMXBean;
    private final RuntimeMXBean runtimeMXBean;

    // 性能数据缓存
    private final Map<String, Object> performanceCache = new ConcurrentHashMap<>();

    // 性能历史数据
    private final Map<String, List<PerformanceSnapshot>> performanceHistory = new ConcurrentHashMap<>();

    // 监控配置
    private final JvmPerformanceConfig config;

    // 定时任务执行器
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    /**
     * 构造函数
     */
    public JvmPerformanceManager(JvmPerformanceConfig config) {
        this.config = config;

        // 获取JVM监控MXBeans
        this.memoryMXBean = ManagementFactory.getMemoryMXBean();
        this.gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        this.threadMXBean = ManagementFactory.getThreadMXBean();
        this.classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
        this.compilationMXBean = ManagementFactory.getCompilationMXBean();
        this.osMXBean = ManagementFactory.getOperatingSystemMXBean();
        this.runtimeMXBean = ManagementFactory.getRuntimeMXBean();

        // 启动性能监控
        startPerformanceMonitoring();

        log.info("[JVM性能管理] JVM性能管理器初始化完成");
    }

    /**
     * 获取JVM性能概览
     */
    public JvmPerformanceOverview getPerformanceOverview() {
        JvmPerformanceOverview overview = new JvmPerformanceOverview();

        try {
            // 内存信息
            MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
            MemoryUsage nonHeapUsage = memoryMXBean.getNonHeapMemoryUsage();

            overview.setHeapUsed(heapUsage.getUsed());
            overview.setHeapCommitted(heapUsage.getCommitted());
            overview.setHeapMax(heapUsage.getMax());
            overview.setHeapUsagePercent(getUsagePercent(heapUsage));

            overview.setNonHeapUsed(nonHeapUsage.getUsed());
            overview.setNonHeapCommitted(nonHeapUsage.getCommitted());
            overview.setNonHeapMax(nonHeapUsage.getMax());
            overview.setNonHeapUsagePercent(getUsagePercent(nonHeapUsage));

            // GC信息
            JvmPerformanceOverview.GcStatistics gcStats = calculateGcStatistics();
            overview.setGcStats(gcStats);

            // 线程信息
            JvmPerformanceOverview.ThreadStatistics threadStats = calculateThreadStatistics();
            overview.setThreadStats(threadStats);

            // 类加载信息
            JvmPerformanceOverview.ClassLoadingStatistics classStats = calculateClassLoadingStatistics();
            overview.setClassStats(classStats);

            // 编译信息
            JvmPerformanceOverview.CompilationStatistics compilationStats = calculateCompilationStatistics();
            overview.setCompilationStats(compilationStats);

            // 系统信息
            JvmPerformanceOverview.SystemStatistics systemStats = calculateSystemStatistics();
            overview.setSystemStats(systemStats);

            // 运行时信息
            JvmPerformanceOverview.RuntimeStatistics runtimeStats = calculateRuntimeStatistics();
            overview.setRuntimeStats(runtimeStats);

            // 性能评分
            overview.setPerformanceScore(calculatePerformanceScore(overview));

            // 检查性能告警
            overview.setPerformanceAlerts(checkPerformanceAlerts(overview));

        } catch (Exception e) {
            log.error("[JVM性能管理] 获取性能概览失败", e);
        }

        return overview;
    }

    /**
     * 获取详细内存分析
     */
    public MemoryAnalysis getMemoryAnalysis() {
        MemoryAnalysis analysis = new MemoryAnalysis();

        try {
            MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
            MemoryUsage nonHeapUsage = memoryMXBean.getNonHeapMemoryUsage();

            // 堆内存分析
            analysis.setHeapUsed(heapUsage.getUsed());
            analysis.setHeapCommitted(heapUsage.getCommitted());
            analysis.setHeapMax(heapUsage.getMax());
            analysis.setHeapUsagePercent(getUsagePercent(heapUsage));
            analysis.setHeapEfficiency(calculateHeapEfficiency(heapUsage));

            // 非堆内存分析
            analysis.setNonHeapUsed(nonHeapUsage.getUsed());
            analysis.setNonHeapCommitted(nonHeapUsage.getCommitted());
            analysis.setNonHeapMax(nonHeapUsage.getMax());
            analysis.setNonHeapUsagePercent(getUsagePercent(nonHeapUsage));

            // 内存区域分析
            analysis.setMemoryAreas(calculateMemoryAreas());

            // 内存建议
            analysis.setMemoryRecommendations(generateMemoryRecommendations(analysis));

        } catch (Exception e) {
            log.error("[JVM性能管理] 内存分析失败", e);
        }

        return analysis;
    }

    /**
     * 获取GC性能分析
     */
    public GcPerformanceAnalysis getGcPerformanceAnalysis() {
        GcPerformanceAnalysis analysis = new GcPerformanceAnalysis();

        try {
            List<GcCollectorAnalysis> collectors = new ArrayList<>();
            long totalCollections = 0;
            long totalTime = 0;

            for (GarbageCollectorMXBean gcBean : gcMXBeans) {
                GcCollectorAnalysis collector = new GcCollectorAnalysis();

                collector.setName(gcBean.getName());
                collector.setCollectionCount(gcBean.getCollectionCount());
                collector.setCollectionTime(gcBean.getCollectionTime());
                collector.setAverageCollectionTime(gcBean.getCollectionCount() > 0 ?
                        (double) gcBean.getCollectionTime() / gcBean.getCollectionCount() : 0.0);

                // 计算GC效率
                collector.setEfficiency(calculateGcEfficiency(gcBean));

                collectors.add(collector);
                totalCollections += gcBean.getCollectionCount();
                totalTime += gcBean.getCollectionTime();
            }

            analysis.setCollectors(collectors);
            analysis.setTotalCollections(totalCollections);
            analysis.setTotalCollectionTime(totalTime);
            analysis.setAveragePauseTime(totalCollections > 0 ? (double) totalTime / totalCollections : 0.0);
            analysis.setGcThroughput(calculateGcThroughput());
            analysis.setGcRecommendations(generateGcRecommendations(analysis));

        } catch (Exception e) {
            log.error("[JVM性能管理] GC性能分析失败", e);
        }

        return analysis;
    }

    /**
     * 获取线程性能分析
     */
    public ThreadPerformanceAnalysis getThreadPerformanceAnalysis() {
        ThreadPerformanceAnalysis analysis = new ThreadPerformanceAnalysis();

        try {
            // 获取线程信息
            int threadCount = threadMXBean.getThreadCount();
            int peakThreadCount = threadMXBean.getPeakThreadCount();
            long totalStartedThreadCount = threadMXBean.getTotalStartedThreadCount();

            // 获取线程堆栈（如果启用）
            ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds());

            // 统计线程状态
            Map<Thread.State, Integer> threadStates = new HashMap<>();
            int blockedThreads = 0;
            int waitingThreads = 0;

            for (ThreadInfo threadInfo : threadInfos) {
                Thread.State state = threadInfo.getThreadState();
                threadStates.put(state, threadStates.getOrDefault(state, 0) + 1);

                if (state == Thread.State.BLOCKED) {
                    blockedThreads++;
                } else if (state == Thread.State.WAITING || state == Thread.State.TIMED_WAITING) {
                    waitingThreads++;
                }
            }

            analysis.setCurrentThreadCount(threadCount);
            analysis.setPeakThreadCount(peakThreadCount);
            analysis.setTotalStartedThreadCount(totalStartedThreadCount);
            analysis.setThreadStates(threadStates);
            analysis.setBlockedThreadCount(blockedThreads);
            analysis.setWaitingThreadCount(waitingThreads);
            analysis.setDeadlockDetected(threadMXBean.findDeadlockedThreads() != null);
            analysis.setThreadRecommendations(generateThreadRecommendations(analysis));

        } catch (Exception e) {
            log.error("[JVM性能管理] 线程性能分析失败", e);
        }

        return analysis;
    }

    /**
     * 获取性能快照
     */
    public PerformanceSnapshot takePerformanceSnapshot() {
        PerformanceSnapshot snapshot = new PerformanceSnapshot();

        try {
            snapshot.setTimestamp(LocalDateTime.now());
            snapshot.setUptime(runtimeMXBean.getUptime());
            snapshot.setPerformanceOverview(getPerformanceOverview());

            // 存储快照到历史记录
            performanceHistory.computeIfAbsent("overview", k -> new ArrayList<>()).add(snapshot);

            // 限制历史记录数量
            List<PerformanceSnapshot> history = performanceHistory.get("overview");
            if (history.size() > 100) {
                history.remove(0);
            }

        } catch (Exception e) {
            log.error("[JVM性能管理] 性能快照获取失败", e);
        }

        return snapshot;
    }

    /**
     * 获取性能历史数据
     */
    public List<PerformanceSnapshot> getPerformanceHistory(String type, int limit) {
        List<PerformanceSnapshot> history = performanceHistory.get(type);
        if (history == null) {
            return new ArrayList<>();
        }

        return history.stream()
                .skip(Math.max(0, history.size() - limit))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * 启动性能监控
     */
    private void startPerformanceMonitoring() {
        if (!config.isMonitoringEnabled()) {
            return;
        }

        // 定时收集性能指标
        scheduler.scheduleAtFixedRate(this::collectPerformanceMetrics,
                60, config.getCollectionIntervalSeconds(), TimeUnit.SECONDS);

        // 定时生成性能报告
        scheduler.scheduleAtFixedRate(this::generatePerformanceReport,
                300, config.getReportIntervalSeconds(), TimeUnit.SECONDS);

        log.info("[JVM性能管理] 性能监控已启动，收集间隔: {}秒, 报告间隔: {}秒",
                config.getCollectionIntervalSeconds(), config.getReportIntervalSeconds());
    }

    /**
     * 收集性能指标
     */
    private void collectPerformanceMetrics() {
        try {
            PerformanceSnapshot snapshot = takePerformanceSnapshot();

            // 检查性能告警
            checkPerformanceAlerts(snapshot);

            // 缓存关键指标
            cacheKeyMetrics(snapshot);

            log.debug("[JVM性能管理] 性能指标收集完成");

        } catch (Exception e) {
            log.error("[JVM性能管理] 性能指标收集失败", e);
        }
    }

    /**
     * 生成性能报告
     */
    private void generatePerformanceReport() {
        try {
            JvmPerformanceOverview overview = getPerformanceOverview();

            // 生成性能评分报告
            PerformanceReport report = new PerformanceReport();
            report.setTimestamp(LocalDateTime.now());
            report.setOverview(overview);
            report.setScore(overview.getPerformanceScore());
            report.setAlerts(overview.getPerformanceAlerts());
            report.setRecommendations(generateOverallRecommendations(overview));

            log.info("[JVM性能管理] 性能报告生成完成, 评分: {}, 告警数: {}",
                    report.getScore(), report.getAlerts().size());

        } catch (Exception e) {
            log.error("[JVM性能管理] 性能报告生成失败", e);
        }
    }

    /**
     * 计算GC统计信息
     */
    private JvmPerformanceOverview.GcStatistics calculateGcStatistics() {
        JvmPerformanceOverview.GcStatistics stats = new JvmPerformanceOverview.GcStatistics();

        long totalCollections = 0;
        long totalTime = 0;

        for (GarbageCollectorMXBean gcBean : gcMXBeans) {
            totalCollections += gcBean.getCollectionCount();
            totalTime += gcBean.getCollectionTime();
        }

        stats.setTotalCollections(totalCollections);
        stats.setTotalCollectionTime(totalTime);
        stats.setAveragePauseTime(totalCollections > 0 ? (double) totalTime / totalCollections : 0.0);
        stats.setCollectionsPerSecond(calculateGcThroughput());

        return stats;
    }

    /**
     * 计算线程统计信息
     */
    private JvmPerformanceOverview.ThreadStatistics calculateThreadStatistics() {
        JvmPerformanceOverview.ThreadStatistics stats = new JvmPerformanceOverview.ThreadStatistics();

        stats.setThreadCount(threadMXBean.getThreadCount());
        stats.setPeakThreadCount(threadMXBean.getPeakThreadCount());
        stats.setTotalStartedThreadCount(threadMXBean.getTotalStartedThreadCount());
        stats.setDaemonThreadCount(threadMXBean.getDaemonThreadCount());

        // 计算线程状态分布
        ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds());
        Map<Thread.State, Integer> stateCounts = new HashMap<>();

        for (ThreadInfo threadInfo : threadInfos) {
            Thread.State state = threadInfo.getThreadState();
            stateCounts.put(state, stateCounts.getOrDefault(state, 0) + 1);
        }

        stats.setThreadStates(stateCounts);
        return stats;
    }

    /**
     * 计算类加载统计信息
     */
    private JvmPerformanceOverview.ClassLoadingStatistics calculateClassLoadingStatistics() {
        JvmPerformanceOverview.ClassLoadingStatistics stats = new JvmPerformanceOverview.ClassLoadingStatistics();

        stats.setLoadedClassCount(classLoadingMXBean.getLoadedClassCount());
        stats.setTotalLoadedClassCount((int) classLoadingMXBean.getTotalLoadedClassCount());
        stats.setUnloadedClassCount((int) classLoadingMXBean.getUnloadedClassCount());

        return stats;
    }

    /**
     * 计算编译统计信息
     */
    private JvmPerformanceOverview.CompilationStatistics calculateCompilationStatistics() {
        JvmPerformanceOverview.CompilationStatistics stats = new JvmPerformanceOverview.CompilationStatistics();

        stats.setCompilationTime(compilationMXBean.getTotalCompilationTime());
        stats.setCompilationTimeSupports(compilationMXBean.isCompilationTimeMonitoringSupported());

        return stats;
    }

    /**
     * 计算系统统计信息
     */
    private JvmPerformanceOverview.SystemStatistics calculateSystemStatistics() {
        JvmPerformanceOverview.SystemStatistics stats = new JvmPerformanceOverview.SystemStatistics();

        stats.setAvailableProcessors(osMXBean.getAvailableProcessors());
        stats.setSystemLoadAverage(osMXBean.getSystemLoadAverage());
        stats.setFreePhysicalMemorySize(getFreePhysicalMemorySize());
        stats.setTotalPhysicalMemorySize(getTotalPhysicalMemorySize());

        return stats;
    }

    /**
     * 计算运行时统计信息
     */
    private JvmPerformanceOverview.RuntimeStatistics calculateRuntimeStatistics() {
        JvmPerformanceOverview.RuntimeStatistics stats = new JvmPerformanceOverview.RuntimeStatistics();

        stats.setUptime(runtimeMXBean.getUptime());
        stats.setStartTime(runtimeMXBean.getStartTime());
        stats.setVmName(runtimeMXBean.getVmName());
        stats.setVmVersion(runtimeMXBean.getVmVersion());
        stats.setVmVendor(runtimeMXBean.getVmVendor());

        return stats;
    }

    /**
     * 计算使用率百分比
     */
    private double getUsagePercent(MemoryUsage usage) {
        if (usage.getMax() <= 0) {
            return 0.0;
        }
        return (double) usage.getUsed() / usage.getMax() * 100;
    }

    /**
     * 计算性能评分
     */
    private double calculatePerformanceScore(JvmPerformanceOverview overview) {
        double score = 100.0;

        // 内存评分 (40%)
        double memoryScore = calculateMemoryScore(overview);
        score = score * 0.4 + memoryScore * 0.6;

        // GC评分 (30%)
        double gcScore = calculateGcScore(overview);
        score = score * 0.7 + gcScore * 0.3;

        // 线程评分 (30%)
        double threadScore = calculateThreadScore(overview);
        score = score * 0.7 + threadScore * 0.3;

        return Math.max(0, Math.min(100, score));
    }

    private double calculateMemoryScore(JvmPerformanceOverview overview) {
        double heapScore = Math.max(0, 100 - overview.getHeapUsagePercent());
        double nonHeapScore = Math.max(0, 100 - overview.getNonHeapUsagePercent());
        return (heapScore + nonHeapScore) / 2;
    }

    private double calculateGcScore(JvmPerformanceOverview overview) {
        double pauseTimeScore = Math.max(0, 100 - overview.getGcStats().getAveragePauseTime() / 10);
        double frequencyScore = Math.max(0, 100 - overview.getGcStats().getCollectionsPerSecond());
        return (pauseTimeScore + frequencyScore) / 2;
    }

    private double calculateThreadScore(JvmPerformanceOverview overview) {
        int blockedThreads = overview.getThreadStats().getBlockedThreadCount();
        double blockedScore = Math.max(0, 100 - blockedThreads * 2);
        return blockedScore;
    }

    /**
     * 检查性能告警
     */
    private List<net.lab1024.sa.common.performance.model.PerformanceAlert> checkPerformanceAlerts(JvmPerformanceOverview overview) {
        List<net.lab1024.sa.common.performance.model.PerformanceAlert> alerts = new ArrayList<>();

        // 内存告警
        if (overview.getHeapUsagePercent() > config.getMemoryUsageThreshold()) {
            net.lab1024.sa.common.performance.model.PerformanceAlert alert = new net.lab1024.sa.common.performance.model.PerformanceAlert();
            alert.setType("MEMORY");
            alert.setLevel("WARNING");
            alert.setMessage(String.format("堆内存使用率过高: %.1f%%", overview.getHeapUsagePercent()));
            alerts.add(alert);
        }

        // GC告警
        if (overview.getGcStats().getAveragePauseTime() > config.getGcPauseTimeThreshold()) {
            net.lab1024.sa.common.performance.model.PerformanceAlert gcAlert = new net.lab1024.sa.common.performance.model.PerformanceAlert();
            gcAlert.setType("GC");
            gcAlert.setLevel("WARNING");
            gcAlert.setMessage(String.format("GC暂停时间过长: %.2fms", overview.getGcStats().getAveragePauseTime()));
            alerts.add(gcAlert);
        }

        // 线程告警
        if (overview.getThreadStats().getBlockedThreadCount() > config.getThreadBlockedCountThreshold()) {
            net.lab1024.sa.common.performance.model.PerformanceAlert threadAlert = new net.lab1024.sa.common.performance.model.PerformanceAlert();
            threadAlert.setType("THREAD");
            threadAlert.setLevel("WARNING");
            threadAlert.setMessage(String.format("阻塞线程数过多: %d", overview.getThreadStats().getBlockedThreadCount()));
            alerts.add(threadAlert);
        }

        return alerts;
    }

    private void checkPerformanceAlerts(PerformanceSnapshot snapshot) {
        List<net.lab1024.sa.common.performance.model.PerformanceAlert> alerts = checkPerformanceAlerts(snapshot.getPerformanceOverview());

        for (net.lab1024.sa.common.performance.model.PerformanceAlert alert : alerts) {
            if (alert.getLevel().equals("CRITICAL") || alert.getLevel().equals("ERROR")) {
                log.error("[JVM性能管理] 性能告警: {}", alert.getMessage());
            } else {
                log.warn("[JVM性能管理] 性能告警: {}", alert.getMessage());
            }
        }
    }

    /**
     * 缓存关键指标
     */
    private void cacheKeyMetrics(PerformanceSnapshot snapshot) {
        performanceCache.put("lastSnapshot", snapshot);
        performanceCache.put("lastUpdateTime", LocalDateTime.now());
        performanceCache.put("performanceScore", snapshot.getPerformanceOverview().getPerformanceScore());
    }

    /**
     * 关闭JVM性能管理器
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        log.info("[JVM性能管理] JVM性能管理器已关闭");
    }

    // 获取物理内存大小的方法需要根据操作系统实现
    // 注意：getFreePhysicalMemorySize()和getTotalPhysicalMemorySize()在Java 14+中已过时
    // 但这是获取物理内存的唯一方式，暂时保留使用并添加抑制警告
    @SuppressWarnings("deprecation")
    private long getFreePhysicalMemorySize() {
        try {
            if (osMXBean instanceof com.sun.management.OperatingSystemMXBean) {
                return ((com.sun.management.OperatingSystemMXBean) osMXBean).getFreePhysicalMemorySize();
            }
        } catch (Exception e) {
            log.warn("[JVM性能管理] 无法获取物理内存大小", e);
        }
        return 0;
    }

    @SuppressWarnings("deprecation")
    private long getTotalPhysicalMemorySize() {
        try {
            if (osMXBean instanceof com.sun.management.OperatingSystemMXBean) {
                return ((com.sun.management.OperatingSystemMXBean) osMXBean).getTotalPhysicalMemorySize();
            }
        } catch (Exception e) {
            log.warn("[JVM性能管理] 无法获取物理内存大小", e);
        }
        return 0;
    }

    // 其他私有方法...
    private double calculateHeapEfficiency(MemoryUsage usage) {
        return usage.getUsed() > 0 ? (double) usage.getCommitted() / usage.getUsed() : 1.0;
    }

    private Map<String, Object> calculateMemoryAreas() {
        Map<String, Object> areas = new HashMap<>();
        areas.put("heap", memoryMXBean.getHeapMemoryUsage());
        areas.put("nonHeap", memoryMXBean.getNonHeapMemoryUsage());
        return areas;
    }

    private List<String> generateMemoryRecommendations(MemoryAnalysis analysis) {
        List<String> recommendations = new ArrayList<>();

        if (analysis.getHeapUsagePercent() > 80) {
            recommendations.add("考虑增加堆内存大小或优化内存使用");
        }

        if (analysis.getHeapEfficiency() < 0.8) {
            recommendations.add("堆内存利用率较低，建议调整内存分配策略");
        }

        return recommendations;
    }

    private double calculateGcEfficiency(GarbageCollectorMXBean gcBean) {
        // 简化实现，实际应该更复杂
        return gcBean.getCollectionCount() > 0 ? 0.95 : 1.0;
    }

    private double calculateGcThroughput() {
        long uptime = runtimeMXBean.getUptime();
        long totalCollections = gcMXBeans.stream().mapToLong(GarbageCollectorMXBean::getCollectionCount).sum();

        return uptime > 0 ? (double) totalCollections / (uptime / 1000.0) : 0.0;
    }

    private List<String> generateGcRecommendations(GcPerformanceAnalysis analysis) {
        List<String> recommendations = new ArrayList<>();

        if (analysis.getAveragePauseTime() > 200) {
            recommendations.add("考虑使用G1GC或调整GC参数以减少暂停时间");
        }

        if (analysis.getTotalCollections() > 100) {
            recommendations.add("GC频率较高，考虑增加堆内存或优化对象创建");
        }

        return recommendations;
    }

    private List<String> generateThreadRecommendations(ThreadPerformanceAnalysis analysis) {
        List<String> recommendations = new ArrayList<>();

        if (analysis.isDeadlockDetected()) {
            recommendations.add("检测到线程死锁，请检查线程同步代码");
        }

        if (analysis.getBlockedThreadCount() > 10) {
            recommendations.add("阻塞线程数较多，检查锁竞争和资源访问");
        }

        return recommendations;
    }

    private List<String> generateOverallRecommendations(JvmPerformanceOverview overview) {
        List<String> recommendations = new ArrayList<>();

        double score = overview.getPerformanceScore();

        if (score < 60) {
            recommendations.add("JVM性能评分较低，建议进行全面优化");
        } else if (score < 80) {
            recommendations.add("JVM性能一般，建议针对性优化");
        }

        return recommendations;
    }

    /**
     * JVM性能配置类
     */
    public static class JvmPerformanceConfig {
        private boolean monitoringEnabled = true;
        private int collectionIntervalSeconds = 60;
        private int reportIntervalSeconds = 300;
        private double memoryUsageThreshold = 80.0;
        private double gcPauseTimeThreshold = 200.0;
        private int threadBlockedCountThreshold = 10;

        // getters and setters
        public boolean isMonitoringEnabled() { return monitoringEnabled; }
        public void setMonitoringEnabled(boolean monitoringEnabled) { this.monitoringEnabled = monitoringEnabled; }
        public int getCollectionIntervalSeconds() { return collectionIntervalSeconds; }
        public void setCollectionIntervalSeconds(int collectionIntervalSeconds) { this.collectionIntervalSeconds = collectionIntervalSeconds; }
        public int getReportIntervalSeconds() { return reportIntervalSeconds; }
        public void setReportIntervalSeconds(int reportIntervalSeconds) { this.reportIntervalSeconds = reportIntervalSeconds; }
        public double getMemoryUsageThreshold() { return memoryUsageThreshold; }
        public void setMemoryUsageThreshold(double memoryUsageThreshold) { this.memoryUsageThreshold = memoryUsageThreshold; }
        public double getGcPauseTimeThreshold() { return gcPauseTimeThreshold; }
        public void setGcPauseTimeThreshold(double gcPauseTimeThreshold) { this.gcPauseTimeThreshold = gcPauseTimeThreshold; }
        public int getThreadBlockedCountThreshold() { return threadBlockedCountThreshold; }
        public void setThreadBlockedCountThreshold(int threadBlockedCountThreshold) { this.threadBlockedCountThreshold = threadBlockedCountThreshold; }
    }

    /**
     * 性能告警类
     */
    public static class PerformanceAlert {
        private String type;
        private String level;
        private String message;
        private LocalDateTime timestamp;

        public PerformanceAlert() {
            this.timestamp = LocalDateTime.now();
        }

        // getters and setters
        public String getType() { return type; }
        public PerformanceAlert setType(String type) { this.type = type; return this; }
        public String getLevel() { return level; }
        public PerformanceAlert setLevel(String level) { this.level = level; return this; }
        public String getMessage() { return message; }
        public PerformanceAlert setMessage(String message) { this.message = message; return this; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public PerformanceAlert setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
    }

    /**
     * 性能报告类
     */
    public static class PerformanceReport {
        private LocalDateTime timestamp;
        private JvmPerformanceOverview overview;
        private double score;
        private List<net.lab1024.sa.common.performance.model.PerformanceAlert> alerts;
        private List<String> recommendations;

        // getters and setters
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public JvmPerformanceOverview getOverview() { return overview; }
        public void setOverview(JvmPerformanceOverview overview) { this.overview = overview; }
        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
        public List<net.lab1024.sa.common.performance.model.PerformanceAlert> getAlerts() { return alerts; }
        public void setAlerts(List<net.lab1024.sa.common.performance.model.PerformanceAlert> alerts) { this.alerts = alerts; }
        public List<String> getRecommendations() { return recommendations; }
        public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    }

    /**
     * 性能快照类
     */
    public static class PerformanceSnapshot {
        private LocalDateTime timestamp;
        private long uptime;
        private JvmPerformanceOverview performanceOverview;

        // getters and setters
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public long getUptime() { return uptime; }
        public void setUptime(long uptime) { this.uptime = uptime; }
        public JvmPerformanceOverview getPerformanceOverview() { return performanceOverview; }
        public void setPerformanceOverview(JvmPerformanceOverview performanceOverview) { this.performanceOverview = performanceOverview; }
        public double getPerformanceScore() { return performanceOverview != null ? performanceOverview.getPerformanceScore() : 0.0; }
    }

    /**
     * GC收集器分析
     */
    public static class GcCollectorAnalysis {
        private String name;
        private long collectionCount;
        private long collectionTime;
        private double averageCollectionTime;
        private double efficiency;

        // getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public long getCollectionCount() { return collectionCount; }
        public void setCollectionCount(long collectionCount) { this.collectionCount = collectionCount; }
        public long getCollectionTime() { return collectionTime; }
        public void setCollectionTime(long collectionTime) { this.collectionTime = collectionTime; }
        public double getAverageCollectionTime() { return averageCollectionTime; }
        public void setAverageCollectionTime(double averageCollectionTime) { this.averageCollectionTime = averageCollectionTime; }
        public double getEfficiency() { return efficiency; }
        public void setEfficiency(double efficiency) { this.efficiency = efficiency; }
    }
}
