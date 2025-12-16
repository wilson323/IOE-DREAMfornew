package net.lab1024.sa.common.performance.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * GC性能分析
 * <p>
 * 垃圾回收器的性能分析结果
 * 包含GC次数、GC时间、GC效率等指标
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Data
@Accessors(chain = true)
public class GcPerformanceAnalysis {

    /**
     * 分析时间
     */
    private LocalDateTime analysisTime;

    /**
     * 年轻代GC次数
     */
    private long youngGenGcCount;

    /**
     * 年轻代GC时间（毫秒）
     */
    private long youngGenGcTime;

    /**
     * 老年代GC次数
     */
    private long oldGenGcCount;

    /**
     * 老年代GC时间（毫秒）
     */
    private long oldGenGcTime;

    /**
     * 总GC次数
     */
    private long totalGcCount;

    /**
     * 总GC时间（毫秒）
     */
    private long totalGcTime;

    /**
     * GC平均频率（次/分钟）
     */
    private double gcFrequency;

    /**
     * GC平均耗时（毫秒）
     */
    private double averageGcTime;

    /**
     * GC停顿时间占比
     */
    private double gcPauseRatio;

    /**
     * GC效率评分
     */
    private double gcEfficiencyScore;

    /**
     * GC告警列表
     */
    private java.util.List<PerformanceAlert> alerts;

    /**
     * GC收集器详细信息列表
     */
    private java.util.List<net.lab1024.sa.common.performance.JvmPerformanceManager.GcCollectorAnalysis> collectors;

    /**
     * 总收集次数（所有收集器）
     */
    private long totalCollections;

    /**
     * 总收集时间（所有收集器，毫秒）
     */
    private long totalCollectionTime;

    /**
     * 平均暂停时间（毫秒）
     */
    private double averagePauseTime;

    /**
     * GC吞吐量（次/分钟）
     */
    private double gcThroughput;

    /**
     * GC优化建议
     */
    private java.util.List<String> gcRecommendations;
}