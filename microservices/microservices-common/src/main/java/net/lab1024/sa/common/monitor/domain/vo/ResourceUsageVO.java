package net.lab1024.sa.common.monitor.domain.vo;

import lombok.Data;

/**
 * 资源使用VO
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@Data注解自动生成getter/setter
 * - 完整的字段注释
 * - 支持CPU、内存、磁盘、网络等资源监控
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Data
public class ResourceUsageVO {

    /**
     * CPU使用率（百分比）
     */
    private Double cpuUsage;

    /**
     * CPU核心数
     */
    private Integer cpuCores;

    /**
     * CPU负载
     */
    private Double cpuLoad;

    /**
     * 总内存（GB）
     */
    private Double totalMemory;

    /**
     * 已用内存（GB）
     */
    private Double usedMemory;

    /**
     * 可用内存（GB）
     */
    private Double availableMemory;

    /**
     * 内存使用率（百分比）
     */
    private Double memoryUsage;

    /**
     * 总磁盘空间（GB）
     */
    private Double totalDisk;

    /**
     * 已用磁盘空间（GB）
     */
    private Double usedDisk;

    /**
     * 可用磁盘空间（GB）
     */
    private Double availableDisk;

    /**
     * 磁盘使用率（百分比）
     */
    private Double diskUsage;

    /**
     * 网络入站流量（MB/s）
     */
    private Double networkInbound;

    /**
     * 网络出站流量（MB/s）
     */
    private Double networkOutbound;

    /**
     * 总线程数
     */
    private Integer totalThreads;

    /**
     * 活跃线程数
     */
    private Integer activeThreads;

    /**
     * 堆内存使用率（百分比）
     */
    private Double heapMemoryUsage;

    /**
     * 非堆内存使用率（百分比）
     */
    private Double nonHeapMemoryUsage;

    /**
     * GC次数
     */
    private Long gcCount;

    /**
     * GC耗时（毫秒）
     */
    private Long gcTime;
}

