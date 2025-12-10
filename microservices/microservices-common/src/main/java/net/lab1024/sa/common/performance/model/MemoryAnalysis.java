package net.lab1024.sa.common.performance.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 内存分析
 * <p>
 * JVM内存使用情况的分析结果
 * 包含堆内存、非堆内存、直接内存等详细信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Data
@Accessors(chain = true)
public class MemoryAnalysis {

    /**
     * 分析时间
     */
    private LocalDateTime analysisTime;

    /**
     * 堆内存总大小（字节）
     */
    private long heapMax;

    /**
     * 堆内存已使用（字节）
     */
    private long heapUsed;

    /**
     * 堆内存使用率
     */
    private double heapUsagePercent;

    /**
     * 堆内存已提交量（字节）
     */
    private long heapCommitted;

    /**
     * 堆内存效率（使用效率百分比）
     */
    private double heapEfficiency;

    /**
     * 非堆内存总大小（字节）
     */
    private long nonHeapMax;

    /**
     * 非堆内存已使用（字节）
     */
    private long nonHeapUsed;

    /**
     * 非堆内存使用率
     */
    private double nonHeapUsagePercent;

    /**
     * 非堆内存已提交量（字节）
     */
    private long nonHeapCommitted;

    /**
     * 直接内存大小（字节）
     */
    private long directMemoryUsed;

    /**
     * 内存区域详细信息
     */
    private java.util.Map<String, Object> memoryAreas;

    /**
     * 内存使用趋势（上升/下降/稳定）
     */
    private String memoryTrend;

    /**
     * 内存告警列表
     */
    private java.util.List<PerformanceAlert> alerts;

    /**
     * 内存优化建议
     */
    private java.util.List<String> memoryRecommendations;
}