package net.lab1024.sa.device.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 物理设备添加表单
 *
 * @author IOE-DREAM Team
 */
@Data
@Schema(description = "物理设备添加表单")
public class PhysicalDeviceAddForm {

    @NotBlank(message = "设备编号不能为空")
    @Schema(description = "设备编号（唯一标识）", example = "DEV001")
    private String deviceCode;

    @NotBlank(message = "设备名称不能为空")
    @Schema(description = "设备名称", example = "主门禁设备")
    private String deviceName;

    @NotBlank(message = "设备类型不能为空")
    @Schema(description = "设备类型", example = "ACCESS", allowableValues = {
            "ACCESS", "VIDEO", "SENSOR", "OTHER"
    })
    private String deviceType;

    @Schema(description = "设备型号", example = "HD-1000")
    private String deviceModel;

    @Schema(description = "制造商", example = "华为")
    private String manufacturer;

    @Schema(description = "IP地址", example = "192.168.1.100")
    private String ipAddress;

    @NotNull(message = "端口号不能为空")
    @Schema(description = "端口号", example = "8080")
    private Integer port;

    @Schema(description = "设备位置", example = "办公楼一楼大厅")
    private String location;

    @Schema(description = "安装区域ID", example = "1001")
    private Long areaId;

    @Schema(description = "协议类型", example = "TCP", allowableValues = {
            "TCP", "UDP", "HTTP", "MQTT", "MODBUS"
    })
    private String protocolType = "TCP";

    @Schema(description = "设备版本", example = "v1.0.0")
    private String deviceVersion;

    @Schema(description = "固件版本", example = "v2.1.5")
    private String firmwareVersion;

    @Schema(description = "配置参数（JSON格式）")
    private String configParameters;

    @Schema(description = "设备描述", example = "主要出入口门禁控制设备")
    private String description;

    @Schema(description = "备注", example = "需要定期维护")
    private String remarks;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled = true;
}