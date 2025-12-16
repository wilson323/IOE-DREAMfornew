package net.lab1024.sa.common.performance.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 类加载统计信息
 * <p>
 * JVM类加载器的详细统计信息
 * 包含已加载类数、已卸载类数等统计
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Data
@Accessors(chain = true)
public class ClassLoadingStatistics {

    /**
     * 统计时间
     */
    private LocalDateTime statisticsTime;

    /**
     * 当前已加载类数量
     */
    private long loadedClassCount;

    /**
     * 已加载类总数（包括已卸载的）
     */
    private long totalLoadedClassCount;

    /**
     * 已卸载类数量
     */
    private long unloadedClassCount;

    /**
     * 类加载器数量
     */
    private int classLoaderCount;

    /**
     * 类定义内存使用量（字节）
     */
    private long classMemoryUsed;

    /**
     * 方法区内存使用量（字节）
     */
    private long metaspaceUsed;

    /**
     * 方法区内存总大小（字节）
     */
    private long metaspaceMax;

    /**
     * 方法区使用率
     */
    private double metaspaceUsagePercent;

    /**
     * 类加载速率（类/分钟）
     */
    private double classLoadingRate;
}