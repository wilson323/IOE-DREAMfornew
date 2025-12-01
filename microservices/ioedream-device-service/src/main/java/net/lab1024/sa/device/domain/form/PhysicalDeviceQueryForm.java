package net.lab1024.sa.device.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 物理设备查询表单
 */
@Data
@Schema(description = "物理设备查询表单")
public class PhysicalDeviceQueryForm {

    @Schema(description = "设备编号")
    private String deviceCode;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "设备类型")
    private String deviceType;

    @Schema(description = "设备状态")
    private Integer deviceStatus;

    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "20")
    @Min(value = 1, message = "每页数量不能小于1")
    private Integer pageSize = 20;
}