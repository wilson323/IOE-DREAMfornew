package net.lab1024.sa.video.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 云台控制请求
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "云台控制请求")
public class PTZControlRequest {

    @NotBlank(message = "设备ID不能为空")
    @Schema(description = "设备ID", example = "CAM001", required = true)
    private String deviceId;

    @Schema(description = "通道ID", example = "1")
    private Integer channelId;

    @NotNull(message = "操作类型不能为空")
    @Schema(description = "操作类型", example = "pan_left", required = true,
            allowableValues = {"pan_left", "pan_right", "tilt_up", "tilt_down", 
                               "zoom_in", "zoom_out", "focus_near", "focus_far", 
                               "iris_open", "iris_close", "preset_set", "preset_call", "stop"})
    private String action;

    @Schema(description = "速度(1-10)", example = "5")
    private Integer speed;

    @Schema(description = "预置位编号", example = "1")
    private Integer presetNumber;

    @Schema(description = "预置位名称", example = "入口位置")
    private String presetName;

    @Schema(description = "水平角度(-180到180)", example = "45")
    private Double panAngle;

    @Schema(description = "垂直角度(-90到90)", example = "30")
    private Double tiltAngle;

    @Schema(description = "变焦倍数", example = "10")
    private Double zoomLevel;

    @Schema(description = "持续时间(毫秒)", example = "1000")
    private Integer duration;
}
