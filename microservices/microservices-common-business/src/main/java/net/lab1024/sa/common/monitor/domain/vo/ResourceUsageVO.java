package net.lab1024.sa.common.monitor.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 资源使用情况VO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceUsageVO {

    private Double cpuUsage;
    private Double memoryUsage;
    private Double diskUsage;
    private Double networkUsage;
    private Long totalMemory;
    private Long usedMemory;
    private Long freeMemory;
    private Long totalDisk;
    private Long usedDisk;
    private Long freeDisk;

    // 扩展字段
    private Integer cpuCores;
    private Double cpuLoad;
    private Long availableMemory;
    private Long availableDisk;
    private Long networkInbound;
    private Long networkOutbound;
}

