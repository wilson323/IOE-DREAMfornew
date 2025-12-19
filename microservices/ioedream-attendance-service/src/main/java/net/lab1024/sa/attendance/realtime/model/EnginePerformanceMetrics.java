package net.lab1024.sa.attendance.realtime.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 引擎性能指标（简化）
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnginePerformanceMetrics {
    private String engineVersion;
    private long uptime;
    private long totalEventsProcessed;
    private long totalCalculationsPerformed;
    private long averageProcessingTime;
    private double cacheHitRate;
    private long memoryUsage;
    private double threadPoolUsage;
    private LocalDateTime lastUpdated;
}
