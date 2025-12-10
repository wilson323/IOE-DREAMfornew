package net.lab1024.sa.common.performance.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 系统统计信息
 * <p>
 * 操作系统和硬件资源的统计信息
 * 包含CPU、内存、磁盘、网络等统计
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Data
@Accessors(chain = true)
public class SystemStatistics {

    /**
     * 统计时间
     */
    private LocalDateTime statisticsTime;

    /**
     * 操作系统名称
     */
    private String osName;

    /**
     * 操作系统版本
     */
    private String osVersion;

    /**
     * 可用处理器数量
     */
    private int availableProcessors;

    /**
     * 系统负载平均值（1分钟）
     */
    private double systemLoadAverage;

    /**
     * CPU使用率
     */
    private double cpuUsage;

    /**
     * 系统总内存（字节）
     */
    private long totalMemory;

    /**
     * 系统可用内存（字节）
     */
    private long freeMemory;

    /**
     * 内存使用率
     */
    private double memoryUsagePercent;

    /**
     * JVM进程ID
     */
    private long processId;

    /**
     * JVM运行时间（毫秒）
     */
    private long uptime;

    /**
     * 系统启动时间
     */
    private LocalDateTime systemStartTime;
}