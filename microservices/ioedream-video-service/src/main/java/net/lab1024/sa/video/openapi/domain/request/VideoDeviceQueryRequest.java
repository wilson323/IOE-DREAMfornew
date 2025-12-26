package net.lab1024.sa.video.openapi.domain.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 视频设备查询请求
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "视频设备查询请求")
public class VideoDeviceQueryRequest {

    @Schema(description = "设备名称(模糊匹配)", example = "摄像头")
    private String deviceName;

    @Schema(description = "设备类型", example = "ipc", allowableValues = { "ipc", "nvr", "dvr", "ball" })
    private String deviceType;

    @Schema(description = "设备类型列表")
    private List<String> deviceTypes;

    @Schema(description = "设备厂商", example = "海康威视")
    private String manufacturer;

    @Schema(description = "在线状态", example = "online", allowableValues = { "online", "offline", "unknown" })
    private String status;

    @Schema(description = "区域ID（Long类型）", example = "1001")
    private Long areaId;

    @Schema(description = "区域ID（String类型，兼容字段）", example = "AREA001")
    private String areaIdStr;

    @Schema(description = "区域ID列表")
    private List<String> areaIds;

    @Schema(description = "是否支持AI", example = "true")
    private Boolean supportAI;

    @Schema(description = "是否支持云台", example = "true")
    private Boolean supportPTZ;

    @Schema(description = "IP地址(模糊匹配)", example = "192.168.1")
    private String ipAddress;

    @Schema(description = "页码", example = "1")
    private Integer pageNum;

    @Schema(description = "每页大小", example = "20")
    private Integer pageSize;

    @Schema(description = "排序字段", example = "deviceName")
    private String sortField;

    @Schema(description = "排序方向", example = "asc", allowableValues = { "asc", "desc" })
    private String sortOrder;
}
