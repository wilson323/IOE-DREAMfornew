package net.lab1024.sa.common.performance.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 性能快照
 * <p>
 * JVM性能的时间快照
 * 包含特定时间点的性能概览和运行时间
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Data
@Accessors(chain = true)
public class PerformanceSnapshot {

    /**
     * 快照时间
     */
    private LocalDateTime timestamp;

    /**
     * JVM运行时间（毫秒）
     */
    private long uptime;

    /**
     * 性能概览
     */
    private JvmPerformanceOverview performanceOverview;

    /**
     * 获取性能评分
     */
    public double getPerformanceScore() {
        return performanceOverview != null ? performanceOverview.getPerformanceScore() : 0.0;
    }
}