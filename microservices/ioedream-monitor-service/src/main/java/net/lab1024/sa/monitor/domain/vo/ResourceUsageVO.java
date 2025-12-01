package net.lab1024.sa.monitor.domain.vo;

import lombok.Data;

/**
 * 资源使用情况VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
public class ResourceUsageVO {

    /**
     * CPU使用率
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
     * 内存总量（GB）
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
     * 内存使用率
     */
    private Double memoryUsage;

    /**
     * 磁盘总量（GB）
     */
    private Double totalDisk;

    /**
     * 已用磁盘（GB）
     */
    private Double usedDisk;

    /**
     * 可用磁盘（GB）
     */
    private Double availableDisk;

    /**
     * 磁盘使用率
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
     * 活跃连接数
     */
    private Integer activeConnections;

    /**
     * 线程总数
     */
    private Integer totalThreads;

    /**
     * 活跃线程数
     */
    private Integer activeThreads;

    /**
     * JVM堆内存使用率
     */
    private Double heapMemoryUsage;

    /**
     * JVM非堆内存使用率
     */
    private Double nonHeapMemoryUsage;

    /**
     * GC次数
     */
    private Long gcCount;

    /**
     * GC时间（毫秒）
     */
    private Long gcTime;
}