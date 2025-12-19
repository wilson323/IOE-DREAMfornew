package net.lab1024.sa.common.organization.entity;

/**
 * 测试用设备实体（最小实现）
 * <p>
 * 仅用于 consume-service 测试编译与单元测试场景，避免测试类路径缺失导致编译失败。
 * </p>
 */
public class DeviceEntity {

    private String deviceId;
    private String deviceCode;
    private String deviceName;
    private Integer deviceStatus;
    private String extendedAttributes;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Integer getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(Integer deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public void setDeviceStatus(String statusText) {
        if (statusText == null || statusText.trim().isEmpty()) {
            this.deviceStatus = null;
            return;
        }
        String normalized = statusText.trim().toUpperCase();
        if ("ONLINE".equals(normalized)) {
            this.deviceStatus = 1;
            return;
        }
        if ("OFFLINE".equals(normalized)) {
            this.deviceStatus = 2;
            return;
        }
        this.deviceStatus = null;
    }

    public String getExtendedAttributes() {
        return extendedAttributes;
    }

    public void setExtendedAttributes(String extendedAttributes) {
        this.extendedAttributes = extendedAttributes;
    }
}
