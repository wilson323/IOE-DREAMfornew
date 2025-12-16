package net.lab1024.sa.common.performance.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 线程统计信息
 * <p>
 * JVM线程的详细统计信息
 * 包含线程数量、状态、死锁等统计
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Data
@Accessors(chain = true)
public class ThreadStatistics {

    /**
     * 统计时间
     */
    private LocalDateTime statisticsTime;

    /**
     * 当前活跃线程数
     */
    private int currentThreadCount;

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
     * 运行状态线程数
     */
    private int runnableThreadCount;

    /**
     * 终止状态线程数
     */
    private int terminatedThreadCount;

    /**
     * 超时等待线程数
     */
    private int timedWaitingThreadCount;

    /**
     * 死锁线程数
     */
    private int deadlockedThreadCount;
}