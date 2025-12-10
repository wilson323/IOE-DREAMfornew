package net.lab1024.sa.common.performance.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * JVM性能概览
 * <p>
 * JVM性能指标的综合概览
 * 包含内存、GC、线程、类加载等关键指标
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Data
@Accessors(chain = true)
public class JvmPerformanceOverview {

    /**
     * 堆内存使用量（字节）
     */
    private long heapUsed;

    /**
     * 堆内存已提交量（字节）
     */
    private long heapCommitted;

    /**
     * 堆内存最大量（字节）
     */
    private long heapMax;

    /**
     * 堆内存使用率（百分比）
     */
    private double heapUsagePercent;

    /**
     * 非堆内存使用量（字节）
     */
    private long nonHeapUsed;

    /**
     * 非堆内存已提交量（字节）
     */
    private long nonHeapCommitted;

    /**
     * 非堆内存最大量（字节）
     */
    private long nonHeapMax;

    /**
     * 非堆内存使用率（百分比）
     */
    private double nonHeapUsagePercent;

    /**
     * GC统计信息
     */
    private GcStatistics gcStats;

    /**
     * 线程统计信息
     */
    private ThreadStatistics threadStats;

    /**
     * 类加载统计信息
     */
    private ClassLoadingStatistics classStats;

    /**
     * 编译统计信息
     */
    private CompilationStatistics compilationStats;

    /**
     * 系统统计信息
     */
    private SystemStatistics systemStats;

    /**
     * 运行时统计信息
     */
    private RuntimeStatistics runtimeStats;

    /**
     * 性能评分（0-100）
     */
    private double performanceScore;

    /**
     * 性能告警列表
     */
    private List<PerformanceAlert> performanceAlerts;

    /**
     * GC统计信息
     */
    @Data
    @Accessors(chain = true)
    public static class GcStatistics {
        private long totalCollections;
        private long totalCollectionTime;
        private double averagePauseTime;
        private double collectionsPerSecond;
    }

    /**
     * 线程统计信息
     */
    @Data
    @Accessors(chain = true)
    public static class ThreadStatistics {
        private int threadCount;
        private int peakThreadCount;
        private long totalStartedThreadCount;
        private int daemonThreadCount;
        private java.util.Map<Thread.State, Integer> threadStates;
        private int blockedThreadCount;
        private int waitingThreadCount;
    }

    /**
     * 类加载统计信息
     */
    @Data
    @Accessors(chain = true)
    public static class ClassLoadingStatistics {
        private int loadedClassCount;
        private int totalLoadedClassCount;
        private int unloadedClassCount;
    }

    /**
     * 编译统计信息
     */
    @Data
    @Accessors(chain = true)
    public static class CompilationStatistics {
        private long compilationTime;
        private boolean compilationTimeSupports;
    }

    /**
     * 系统统计信息
     */
    @Data
    @Accessors(chain = true)
    public static class SystemStatistics {
        private int availableProcessors;
        private double systemLoadAverage;
        private long freePhysicalMemorySize;
        private long totalPhysicalMemorySize;
    }

    /**
     * 运行时统计信息
     */
    @Data
    @Accessors(chain = true)
    public static class RuntimeStatistics {
        private long uptime;
        private long startTime;
        private String vmName;
        private String vmVersion;
        private String vmVendor;
    }
}
