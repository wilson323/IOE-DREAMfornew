package net.lab1024.sa.consume.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 消费设备新增表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Schema(description = "消费设备新增表单")
public class ConsumeDeviceAddForm {

    @NotBlank(message = "设备编码不能为空")
    @Schema(description = "设备编码", example = "CONSUME_DEVICE_001", required = true)
    private String deviceCode;

    @NotBlank(message = "设备名称不能为空")
    @Schema(description = "设备名称", example = "餐厅POS机1号", required = true)
    private String deviceName;

    @NotNull(message = "设备类型不能为空")
    @Schema(description = "设备类型", example = "1", required = true)
    private Integer deviceType;

    @Schema(description = "设备位置", example = "餐厅一楼")
    private String deviceLocation;

    @Schema(description = "IP地址", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "MAC地址", example = "00:11:22:33:44:55")
    private String macAddress;

    @Schema(description = "设备型号", example = "POS-3000")
    private String deviceModel;

    @Schema(description = "设备厂商", example = "智慧设备科技")
    private String deviceManufacturer;

    @Schema(description = "固件版本", example = "V1.2.3")
    private String firmwareVersion;

    @Schema(description = "是否支持离线", example = "true")
    private Boolean supportOffline;

    @Schema(description = "设备描述", example = "用于餐厅消费支付的POS终端")
    private String deviceDescription;

    @Schema(description = "业务属性", example = "{}")
    private String businessAttributes;

    @Schema(description = "安装区域ID", example = "1001")
    private Long areaId;

    @Schema(description = "备注", example = "设备运行良好")
    private String remark;
}