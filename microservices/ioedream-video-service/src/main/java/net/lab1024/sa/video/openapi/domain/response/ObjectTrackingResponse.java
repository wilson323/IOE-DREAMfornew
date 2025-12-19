package net.lab1024.sa.video.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 对象追踪响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "对象追踪响应")
public class ObjectTrackingResponse {

    @Schema(description = "追踪ID", example = "TRK001")
    private String trackingId;

    @Schema(description = "对象ID", example = "OBJ001")
    private String objectId;

    @Schema(description = "对象类型", example = "person", allowableValues = {"person", "vehicle", "object"})
    private String objectType;

    @Schema(description = "追踪状态", example = "active", allowableValues = {"active", "lost", "completed"})
    private String status;

    @Schema(description = "开始时间", example = "2025-12-16T10:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2025-12-16T10:30:00")
    private LocalDateTime endTime;

    @Schema(description = "轨迹点列表")
    private List<TrackPoint> trackPoints;

    @Schema(description = "经过的设备列表")
    private List<String> passedDevices;

    @Schema(description = "追踪置信度", example = "0.90")
    private Double confidence;

    @Schema(description = "扩展信息")
    private Map<String, Object> extendedInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "轨迹点")
    public static class TrackPoint {
        @Schema(description = "时间戳", example = "2025-12-16T10:00:00")
        private LocalDateTime timestamp;

        @Schema(description = "设备ID", example = "CAM001")
        private String deviceId;

        @Schema(description = "设备名称", example = "前门摄像头")
        private String deviceName;

        @Schema(description = "X坐标", example = "500")
        private Integer x;

        @Schema(description = "Y坐标", example = "300")
        private Integer y;

        @Schema(description = "置信度", example = "0.95")
        private Double confidence;
    }
}
