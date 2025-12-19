package net.lab1024.sa.video.edge.vo;

import java.time.LocalDateTime;

import net.lab1024.sa.video.edge.EdgeDeviceStatus;

/**
 * 边缘设备状态VO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
public class EdgeDeviceStatusVO {

    private String deviceId;
    private EdgeDeviceStatus status;
    private String statusDescription;
    private LocalDateTime updateTime;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public EdgeDeviceStatus getStatus() {
        return status;
    }

    public void setStatus(EdgeDeviceStatus status) {
        this.status = status;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}


