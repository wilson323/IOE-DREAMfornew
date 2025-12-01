package net.lab1024.sa.device.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物理设备详情VO
 */
@Data
@Schema(description = "物理设备详情VO")
public class PhysicalDeviceDetailVO {

    @Schema(description = "设备ID")
    private Long deviceId;

    @Schema(description = "设备编号")
    private String deviceCode;

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

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建人ID")
    private Long createUserId;

    @Schema(description = "更新人ID")
    private Long updateUserId;
}