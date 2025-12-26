package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 移动端心跳表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "移动端心跳表单")
public class MobileHeartbeatForm {

    @Schema(description = "设备ID", required = true, example = "MOBILE_001")
    @NotBlank(message = "设备ID不能为空")
    private String deviceId;

    @Schema(description = "设备状态", required = true, example = "online")
    @NotBlank(message = "设备状态不能为空")
    private String deviceStatus;

    @Schema(description = "电量百分比", example = "85")
    private Integer batteryLevel;

    @Schema(description = "网络类型", example = "wifi")
    private String networkType;

    @Schema(description = "信号强度", example = "strong")
    private String signalStrength;

    @Schema(description = "应用版本", example = "1.0.0")
    private String appVersion;

    @Schema(description = "位置信息")
    private LocationInfo location;

    @Data
    @Schema(description = "位置信息")
    public static class LocationInfo {
        @Schema(description = "纬度", example = "39.9042")
        private Double latitude;

        @Schema(description = "经度", example = "116.4074")
        private Double longitude;

        @Schema(description = "地址", example = "北京市朝阳区")
        private String address;
    }
}
