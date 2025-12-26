package net.lab1024.sa.attendance.realtime.monitor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.annotation.Resource;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.attendance.realtime.cache.RealtimeCacheManager;
import net.lab1024.sa.attendance.realtime.model.EnginePerformanceMetrics;

/**
 * 实时计算引擎性能监控服务
 * <p>
 * 负责引擎的性能指标收集、监控、报告等功能
 * </p>
 * <p>
 * 职责范围：
 * <ul>
 *   <li>收集性能指标（事件处理、计算次数、处理时间等）</li>
 *   <li>监控引擎性能（线程池使用率、内存使用、缓存命中率等）</li>
 *   <li>生成性能报告</li>
 *   <li>性能异常检测</li>
 * </ul>
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Service
public class EnginePerformanceMonitorService {

    /**
     * 事件处理线程池
     */
    @Resource(name = "eventProcessingExecutor")
    private ThreadPoolTaskExecutor eventProcessingExecutor;

    /**
     * 计算线程池
     */
    @Resource(name = "calculationExecutor")
    private ThreadPoolTaskExecutor calculationExecutor;

    /**
     * 缓存管理器（用于获取缓存统计）
     */
    @Resource
    private RealtimeCacheManager cacheManager;

    /**
     * 监控指标
     */
    private final Map<String, Object> monitoringMetrics = new ConcurrentHashMap<>();

    /**
     * 性能指标：总事件处理数
     */
    private final AtomicLong totalEventsProcessed = new AtomicLong(0);

    /**
     * 性能指标：总计算执行次数
     */
    private final AtomicLong totalCalculationsPerformed = new AtomicLong(0);

    /**
     * 性能指标：平均处理时间（毫秒）
     */
    private final AtomicLong averageProcessingTime = new AtomicLong(0);

    /**
     * 获取性能指标
     * <p>
     * P0级核心功能：收集并返回所有性能指标
     * </p>
     *
     * @return 性能指标对象
     */
    public EnginePerformanceMetrics getPerformanceMetrics() {
        try {
            return EnginePerformanceMetrics.builder()
                    .engineVersion("1.0.0")
                    .uptime(calculateUptime())
                    .totalEventsProcessed(totalEventsProcessed.get())
                    .totalCalculationsPerformed(totalCalculationsPerformed.get())
                    .averageProcessingTime(averageProcessingTime.get())
                    .cacheHitRate(calculateCacheHitRate())
                    .memoryUsage(calculateMemoryUsage())
                    .threadPoolUsage(calculateThreadPoolUsage())
                    .lastUpdated(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("[性能监控] 获取性能指标失败", e);
            return EnginePerformanceMetrics.builder()
                    .lastUpdated(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * 记录事件处理
     * <p>
     * 每次处理事件时调用，更新性能指标
     * </p>
     *
     * @param processingTime 处理时间（毫秒）
     */
    public void recordEventProcessing(long processingTime) {
        totalEventsProcessed.incrementAndGet();

        // 更新平均处理时间（简单移动平均）
        long currentAvg = averageProcessingTime.get();
        if (currentAvg == 0) {
            averageProcessingTime.set(processingTime);
        } else {
            // 使用加权平均：新值权重30%，旧值权重70%
            long newAvg = (long) (processingTime * 0.3 + currentAvg * 0.7);
            averageProcessingTime.set(newAvg);
        }

        // 更新监控指标
        monitoringMetrics.put("monitoring.eventProcessingTime.total",
                (Long) monitoringMetrics.getOrDefault("monitoring.eventProcessingTime.total", 0L) + processingTime);
        monitoringMetrics.put("monitoring.eventProcessingTime.count",
                (Integer) monitoringMetrics.getOrDefault("monitoring.eventProcessingTime.count", 0) + 1);
    }

    /**
     * 记录计算执行
     * <p>
     * 每次执行计算时调用，更新性能指标
     * </p>
     */
    public void recordCalculation() {
        totalCalculationsPerformed.incrementAndGet();
    }

    /**
     * 初始化监控
     * <p>
     * 引擎启动时调用，初始化监控指标
     * </p>
     */
    public void initializeMonitoring() {
        log.info("[性能监控] 开始初始化监控");

        // 设置性能监控初始值
        monitoringMetrics.put("monitoring.startTime", System.currentTimeMillis());
        monitoringMetrics.put("monitoring.eventProcessingTime.total", 0L);
        monitoringMetrics.put("monitoring.eventProcessingTime.count", 0);
        monitoringMetrics.put("monitoring.calculationTime.total", 0L);
        monitoringMetrics.put("monitoring.calculationTime.count", 0);
        monitoringMetrics.put("monitoring.errorCount", 0);
        monitoringMetrics.put("monitoring.warningCount", 0);

        log.info("[性能监控] 初始化监控完成");
    }

    /**
     * 记录错误
     * <p>
     * 每次发生错误时调用
     * </p>
     */
    public void recordError() {
        monitoringMetrics.put("monitoring.errorCount",
                (Integer) monitoringMetrics.getOrDefault("monitoring.errorCount", 0) + 1);
    }

    /**
     * 记录警告
     * <p>
     * 每次发生警告时调用
     * </p>
     */
    public void recordWarning() {
        monitoringMetrics.put("monitoring.warningCount",
                (Integer) monitoringMetrics.getOrDefault("monitoring.warningCount", 0) + 1);
    }

    /**
     * 计算运行时间（秒）
     * <p>
     * P1级功能：计算引擎从启动到现在的运行时间
     * </p>
     *
     * @return 运行时间（秒）
     */
    private long calculateUptime() {
        Long startTime = (Long) monitoringMetrics.get("monitoring.startTime");
        if (startTime == null) {
            // 如果没有记录启动时间，返回0
            return 0L;
        }

        long currentTime = System.currentTimeMillis();
        long uptimeMillis = currentTime - startTime;
        long uptimeSeconds = uptimeMillis / 1000; // 转换为秒

        log.trace("[性能监控] 计算运行时间: uptime={}秒", uptimeSeconds);
        return uptimeSeconds;
    }

    /**
     * 计算缓存命中率
     * <p>
     * P2级性能监控：计算缓存的命中率百分比
     * </p>
     *
     * @return 缓存命中率（0-100）
     */
    private double calculateCacheHitRate() {
        if (cacheManager == null) {
            return 0.0;
        }

        Map<String, Object> cacheStats = cacheManager.getCacheStatistics();
        if (cacheStats == null || !cacheStats.containsKey("cache.hitRate")) {
            return 0.0;
        }

        double hitRate = (Double) cacheStats.get("cache.hitRate");
        return hitRate * 100.0; // 转换为百分比
    }

    /**
     * 计算线程池使用率
     * <p>
     * P2级性能监控：计算线程池的使用率百分比
     * </p>
     *
     * @return 线程池使用率（0-100）
     */
    private double calculateThreadPoolUsage() {
        try {
            // 计算事件处理线程池使用率
            int eventActiveCount = eventProcessingExecutor.getActiveCount();
            int eventMaxPoolSize = eventProcessingExecutor.getMaxPoolSize();
            double eventPoolUsage = (double) eventActiveCount / eventMaxPoolSize * 100.0;

            // 计算计算线程池使用率
            int calcActiveCount = calculationExecutor.getActiveCount();
            int calcMaxPoolSize = calculationExecutor.getMaxPoolSize();
            double calcPoolUsage = (double) calcActiveCount / calcMaxPoolSize * 100.0;

            // 取两个线程池的最大值作为整体使用率
            double overallUsage = Math.max(eventPoolUsage, calcPoolUsage);

            log.trace("[性能监控] 计算线程池使用率: eventPool={}%, calcPool={}%, overall={}%",
                    String.format("%.2f", eventPoolUsage),
                    String.format("%.2f", calcPoolUsage),
                    String.format("%.2f", overallUsage));

            return overallUsage;

        } catch (Exception e) {
            log.error("[性能监控] 计算线程池使用率失败", e);
            return 0.0;
        }
    }

    /**
     * 计算内存使用量（字节）
     *
     * @return 内存使用量（字节）
     */
    private long calculateMemoryUsage() {
        try {
            Runtime runtime = Runtime.getRuntime();
            // 已使用的内存 = 总内存 - 空闲内存
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();

            log.trace("[性能监控] 计算内存使用量: used={}MB, total={}MB, max={}MB",
                    String.format("%.2f", usedMemory / 1024.0 / 1024.0),
                    String.format("%.2f", runtime.totalMemory() / 1024.0 / 1024.0),
                    String.format("%.2f", runtime.maxMemory() / 1024.0 / 1024.0));

            return usedMemory;

        } catch (Exception e) {
            log.error("[性能监控] 计算内存使用量失败", e);
            return 0L;
        }
    }

    /**
     * 获取总事件处理数
     *
     * @return 总事件处理数
     */
    public long getTotalEventsProcessed() {
        return totalEventsProcessed.get();
    }

    /**
     * 获取总计算执行次数
     *
     * @return 总计算执行次数
     */
    public long getTotalCalculationsPerformed() {
        return totalCalculationsPerformed.get();
    }

    /**
     * 获取平均处理时间
     *
     * @return 平均处理时间（毫秒）
     */
    public long getAverageProcessingTime() {
        return averageProcessingTime.get();
    }

    /**
     * 获取监控指标
     *
     * @return 监控指标映射
     */
    public Map<String, Object> getMonitoringMetrics() {
        return new ConcurrentHashMap<>(monitoringMetrics);
    }

    /**
     * 检测性能异常
     * <p>
     * P2级功能：检测是否存在性能异常（如内存过高、线程池满载等）
     * </p>
     *
     * @return true-存在异常，false-正常
     */
    public boolean detectPerformanceAnomaly() {
        try {
            // 检查内存使用率
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            double memoryUsageRate = (double) usedMemory / runtime.maxMemory();
            if (memoryUsageRate > 0.9) {
                log.warn("[性能监控] 检测到内存使用率过高: {}%", String.format("%.2f", memoryUsageRate * 100));
                return true;
            }

            // 检查线程池使用率
            double poolUsage = calculateThreadPoolUsage();
            if (poolUsage > 90.0) {
                log.warn("[性能监控] 检测到线程池使用率过高: {}%", String.format("%.2f", poolUsage));
                return true;
            }

            return false;

        } catch (Exception e) {
            log.error("[性能监控] 性能异常检测失败", e);
            return false;
        }
    }

    /**
     * 生成性能报告
     * <p>
     * P2级功能：生成详细的性能报告
     * </p>
     *
     * @return 性能报告字符串
     */
    public String generatePerformanceReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== 实时计算引擎性能报告 ===\n");
        report.append(String.format("引擎版本: %s\n", "1.0.0"));
        report.append(String.format("运行时间: %d秒\n", calculateUptime()));
        report.append(String.format("总事件处理数: %d\n", totalEventsProcessed.get()));
        report.append(String.format("总计算执行次数: %d\n", totalCalculationsPerformed.get()));
        report.append(String.format("平均处理时间: %dms\n", averageProcessingTime.get()));
        report.append(String.format("缓存命中率: %.2f%%\n", calculateCacheHitRate()));
        report.append(String.format("线程池使用率: %.2f%%\n", calculateThreadPoolUsage()));

        long memoryUsage = calculateMemoryUsage();
        report.append(String.format("内存使用: %.2fMB\n", memoryUsage / 1024.0 / 1024.0));

        int errorCount = (Integer) monitoringMetrics.getOrDefault("monitoring.errorCount", 0);
        int warningCount = (Integer) monitoringMetrics.getOrDefault("monitoring.warningCount", 0);
        report.append(String.format("错误数: %d\n", errorCount));
        report.append(String.format("警告数: %d\n", warningCount));

        return report.toString();
    }
}
