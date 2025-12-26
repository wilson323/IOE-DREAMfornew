package net.lab1024.sa.video.edge.form;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.lab1024.sa.video.edge.model.EdgeCapability;
import net.lab1024.sa.video.edge.model.HardwareSpec;

/**
 * 边缘设备注册表单（video-service 边缘模块）
 * <p>
 * 说明：
 * - 与 common 的 EdgeDeviceRegisterForm 不同，此处补齐 hardwareSpec/capabilities 等边缘AI需要的字段
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
public class EdgeDeviceRegisterForm {

    @NotBlank(message = "设备ID不能为空")
    private String deviceId;

    @NotBlank(message = "设备名称不能为空")
    private String deviceName;

    @NotBlank(message = "设备类型不能为空")
    private String deviceType;

    private String location;

    @NotBlank(message = "IP地址不能为空")
    private String ipAddress;

    @NotNull(message = "端口不能为空")
    private Integer port;

    private HardwareSpec hardwareSpec;

    private List<EdgeCapability> capabilities;

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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public HardwareSpec getHardwareSpec() {
        return hardwareSpec;
    }

    public void setHardwareSpec(HardwareSpec hardwareSpec) {
        this.hardwareSpec = hardwareSpec;
    }

    public List<EdgeCapability> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<EdgeCapability> capabilities) {
        this.capabilities = capabilities;
    }
}


