package net.lab1024.sa.common.performance.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * GC统计信息
 * <p>
 * 垃圾回收器的详细统计信息
 * 包含GC次数、耗时、内存回收情况等
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Data
@Accessors(chain = true)
public class GcStatistics {

    /**
     * 统计时间
     */
    private LocalDateTime statisticsTime;

    /**
     * 年轻代GC名称
     */
    private String youngGenCollectorName;

    /**
     * 年轻代GC次数
     */
    private long youngGenCollectionCount;

    /**
     * 年轻代GC总时间（毫秒）
     */
    private long youngGenCollectionTime;

    /**
     * 老年代GC名称
     */
    private String oldGenCollectorName;

    /**
     * 老年代GC次数
     */
    private long oldGenCollectionCount;

    /**
     * 老年代GC总时间（毫秒）
     */
    private long oldGenCollectionTime;

    /**
     * 总GC次数
     */
    private long totalCollectionCount;

    /**
     * 总GC时间（毫秒）
     */
    private long totalCollectionTime;

    /**
     * 最近一次GC时间（毫秒）
     */
    private long lastGcDuration;

    /**
     * GC效率（回收内存字节数/毫秒）
     */
    private double gcEfficiency;
}