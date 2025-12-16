package net.lab1024.sa.common.performance.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 线程性能分析
 * <p>
 * JVM线程使用情况的分析结果
 * 包含线程数量、状态、死锁检测等信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Data
@Accessors(chain = true)
public class ThreadPerformanceAnalysis {

    /**
     * 分析时间
     */
    private LocalDateTime analysisTime;

    /**
     * 活跃线程数
     */
    private int activeThreadCount;

    /**
     * 守护线程数
     */
    private int daemonThreadCount;

    /**
     * 峰值线程数
     */
    private int peakThreadCount;

    /**
     * 启动的线程总数
     */
    private long totalStartedThreadCount;

    /**
     * 阻塞线程数
     */
    private int blockedThreadCount;

    /**
     * 等待线程数
     */
    private int waitingThreadCount;

    /**
     * 线程池使用率
     */
    private double threadPoolUsage;

    /**
     * 死锁检测次数
     */
    private long deadlockCount;

    /**
     * 平均线程创建时间（毫秒）
     */
    private double averageThreadCreationTime;

    /**
     * 线程告警列表
     */
    private java.util.List<PerformanceAlert> alerts;

    /**
     * 当前线程数
     */
    private int currentThreadCount;

    /**
     * 线程状态统计
     */
    private java.util.Map<java.lang.Thread.State, Integer> threadStates;

    /**
     * 是否检测到死锁
     */
    private boolean deadlockDetected;

    /**
     * 线程优化建议
     */
    private java.util.List<String> threadRecommendations;
}