package net.lab1024.sa.common.organization.domain.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 区域设备健康统计
 * @author IOE-DREAM Team
 * @since 2025-12-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AreaDeviceHealthStatistics {
    private Integer totalDevices;
    private Integer normalDevices;
    private Integer maintenanceDevices;
    private Integer faultDevices;
    private Integer offlineDevices;
    private Integer disabledDevices;
    private Double healthRate;
}
