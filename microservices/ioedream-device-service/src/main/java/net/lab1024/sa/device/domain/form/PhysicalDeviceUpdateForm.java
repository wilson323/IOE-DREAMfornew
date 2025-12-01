package net.lab1024.sa.device.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 物理设备更新表单
 */
@Data
@Schema(description = "物理设备更新表单")
public class PhysicalDeviceUpdateForm {

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "设备类型")
    private String deviceType;

    @Schema(description = "设备型号")
    private String deviceModel;

    @Schema(description = "设备厂商")
    private String deviceManufacturer;

    @Schema(description = "设备描述")
    private String deviceDescription;

    @Schema(description = "设备状态")
    private Integer deviceStatus;
}
