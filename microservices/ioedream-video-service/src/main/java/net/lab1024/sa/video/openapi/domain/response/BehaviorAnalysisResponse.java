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
 * 行为分析响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "行为分析响应")
public class BehaviorAnalysisResponse {

    @Schema(description = "分析ID", example = "BA001")
    private String analysisId;

    @Schema(description = "设备ID", example = "CAM001")
    private String deviceId;

    @Schema(description = "设备名称", example = "前门摄像头")
    private String deviceName;

    @Schema(description = "分析类型", example = "intrusion", allowableValues = {"intrusion", "loitering", "crowd", "fight", "fall"})
    private String analysisType;

    @Schema(description = "分析状态", example = "completed", allowableValues = {"pending", "processing", "completed", "failed"})
    private String status;

    @Schema(description = "检测时间", example = "2025-12-16T10:00:00")
    private LocalDateTime detectionTime;

    @Schema(description = "检测结果列表")
    private List<DetectionResult> detectionResults;

    @Schema(description = "置信度", example = "0.95")
    private Double confidence;

    @Schema(description = "截图URL", example = "https://example.com/snapshot/BA001.jpg")
    private String snapshotUrl;

    @Schema(description = "视频片段URL", example = "https://example.com/clip/BA001.mp4")
    private String videoClipUrl;

    @Schema(description = "扩展信息")
    private Map<String, Object> extendedInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "检测结果")
    public static class DetectionResult {
        @Schema(description = "对象ID", example = "OBJ001")
        private String objectId;

        @Schema(description = "对象类型", example = "person")
        private String objectType;

        @Schema(description = "边界框")
        private BoundingBox boundingBox;

        @Schema(description = "置信度", example = "0.95")
        private Double confidence;

        @Schema(description = "行为标签", example = "intrusion")
        private String behaviorLabel;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "边界框")
    public static class BoundingBox {
        @Schema(description = "左上角X坐标", example = "100")
        private Integer x;

        @Schema(description = "左上角Y坐标", example = "200")
        private Integer y;

        @Schema(description = "宽度", example = "150")
        private Integer width;

        @Schema(description = "高度", example = "300")
        private Integer height;
    }
}
