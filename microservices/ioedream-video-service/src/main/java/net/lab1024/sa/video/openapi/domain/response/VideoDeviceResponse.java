package net.lab1024.sa.video.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 视频设备响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "视频设备响应")
public class VideoDeviceResponse {

    @Schema(description = "设备ID", example = "CAM001")
    private String deviceId;

    @Schema(description = "设备名称", example = "前门摄像头")
    private String deviceName;

    @Schema(description = "设备类型", example = "ipc", allowableValues = {"ipc", "nvr", "dvr", "ball"})
    private String deviceType;

    @Schema(description = "设备型号", example = "DS-2CD2T45G1-I")
    private String model;

    @Schema(description = "设备厂商", example = "海康威视")
    private String manufacturer;

    @Schema(description = "IP地址", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "端口", example = "8000")
    private Integer port;

    @Schema(description = "在线状态", example = "online", allowableValues = {"online", "offline", "unknown"})
    private String status;

    @Schema(description = "区域ID", example = "AREA001")
    private String areaId;

    @Schema(description = "区域名称", example = "园区入口")
    private String areaName;

    @Schema(description = "支持的能力")
    private DeviceCapabilities capabilities;

    @Schema(description = "最后在线时间", example = "2025-12-16T10:00:00")
    private LocalDateTime lastOnlineTime;

    @Schema(description = "扩展信息")
    private Map<String, Object> extendedInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "设备能力")
    public static class DeviceCapabilities {
        @Schema(description = "是否支持实时流", example = "true")
        private Boolean supportLiveStream;

        @Schema(description = "是否支持录像", example = "true")
        private Boolean supportRecording;

        @Schema(description = "是否支持回放", example = "true")
        private Boolean supportPlayback;

        @Schema(description = "是否支持云台", example = "true")
        private Boolean supportPTZ;

        @Schema(description = "是否支持AI分析", example = "true")
        private Boolean supportAI;

        @Schema(description = "是否支持对讲", example = "false")
        private Boolean supportTalk;
    }
}
