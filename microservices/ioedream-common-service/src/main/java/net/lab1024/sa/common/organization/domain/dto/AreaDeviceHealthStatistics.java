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

    // 手动添加setter/getter方法以确保编译通过
    public void setTotalDevices(Integer totalDevices) {
        this.totalDevices = totalDevices;
    }

    public void setNormalDevices(Integer normalDevices) {
        this.normalDevices = normalDevices;
    }

    public void setOfflineDevices(Integer offlineDevices) {
        this.offlineDevices = offlineDevices;
    }

    public void setHealthRate(Double healthRate) {
        this.healthRate = healthRate;
    }

    public Integer getTotalDevices() {
        return this.totalDevices;
    }

    public Integer getNormalDevices() {
        return this.normalDevices;
    }
}
