package net.lab1024.sa.admin.module.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 门禁设备统计信息VO
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Schema(description = "门禁设备统计信息VO")
public class AccessDeviceStatisticsVO {

    /**
     * 设备总数
     */
    @Schema(description = "设备总数", example = "150")
    private Integer totalDevices;

    /**
     * 在线设备数
     */
    @Schema(description = "在线设备数", example = "142")
    private Integer onlineDevices;

    /**
     * 离线设备数
     */
    @Schema(description = "离线设备数", example = "8")
    private Integer offlineDevices;

    /**
     * 告警设备数
     */
    @Schema(description = "告警设备数", example = "3")
    private Integer alertDevices;

    /**
     * 维护中设备数
     */
    @Schema(description = "维护中设备数", example = "2")
    private Integer maintenanceDevices;

    /**
     * 在线率
     */
    @Schema(description = "在线率（%）", example = "94.67")
    private Double onlineRate;

    // Getters and Setters
    public Integer getTotalDevices() {
        return totalDevices;
    }

    public void setTotalDevices(Integer totalDevices) {
        this.totalDevices = totalDevices;
    }

    public Integer getOnlineDevices() {
        return onlineDevices;
    }

    public void setOnlineDevices(Integer onlineDevices) {
        this.onlineDevices = onlineDevices;
    }

    public Integer getOfflineDevices() {
        return offlineDevices;
    }

    public void setOfflineDevices(Integer offlineDevices) {
        this.offlineDevices = offlineDevices;
    }

    public Integer getAlertDevices() {
        return alertDevices;
    }

    public void setAlertDevices(Integer alertDevices) {
        this.alertDevices = alertDevices;
    }

    public Integer getMaintenanceDevices() {
        return maintenanceDevices;
    }

    public void setMaintenanceDevices(Integer maintenanceDevices) {
        this.maintenanceDevices = maintenanceDevices;
    }

    public Double getOnlineRate() {
        return onlineRate;
    }

    public void setOnlineRate(Double onlineRate) {
        this.onlineRate = onlineRate;
    }
}