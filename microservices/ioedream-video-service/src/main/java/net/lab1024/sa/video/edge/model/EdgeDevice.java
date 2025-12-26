package net.lab1024.sa.video.edge.model;

import java.util.List;

import net.lab1024.sa.video.edge.EdgeConfig;

/**
 * 边缘设备模型
 * <p>
 * 说明：
 * - 该类型用于 video-service 的边缘计算模块内部使用
 * - 与 common 模块的 edge 模型解耦，避免在迭代早期引入跨模块强耦合
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
public class EdgeDevice {

    private String deviceId;
    private String deviceName;
    private String deviceType;
    private String ipAddress;
    private Integer port;
    private String location;
    private String status;
    private String aiCapabilities;
    private String remark;

    private List<EdgeCapability> capabilities;
    private HardwareSpec hardwareSpec;
    private EdgeConfig config;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAiCapabilities() {
        return aiCapabilities;
    }

    public void setAiCapabilities(String aiCapabilities) {
        this.aiCapabilities = aiCapabilities;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public List<EdgeCapability> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<EdgeCapability> capabilities) {
        this.capabilities = capabilities;
    }

    public HardwareSpec getHardwareSpec() {
        return hardwareSpec;
    }

    public void setHardwareSpec(HardwareSpec hardwareSpec) {
        this.hardwareSpec = hardwareSpec;
    }

    public EdgeConfig getConfig() {
        return config;
    }

    public void setConfig(EdgeConfig config) {
        this.config = config;
    }
}

