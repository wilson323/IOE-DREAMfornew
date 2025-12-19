package net.lab1024.sa.common.edge.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 边缘设备注册表单
 *
 * @author IOE-DREAM Team
 * @since 2025-12-18
 */
@Data
public class EdgeDeviceRegisterForm implements Serializable {

    private static final long serialVersionUID = 1L;

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

    private String aiCapabilities;

    private String remark;
}
