package net.lab1024.sa.video.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 行为分析请求
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "行为分析请求")
public class BehaviorAnalysisRequest {

    @NotBlank(message = "设备ID不能为空")
    @Schema(description = "设备ID", example = "CAM001", required = true)
    private String deviceId;

    @Schema(description = "分析类型列表", example = "[\"intrusion\", \"loitering\"]", 
            allowableValues = {"intrusion", "loitering", "crowd", "fight", "fall"})
    private List<String> analysisTypes;

    @Schema(description = "检测区域")
    private List<DetectionRegion> detectionRegions;

    @Schema(description = "置信度阈值", example = "0.8")
    private Double confidenceThreshold;

    @Schema(description = "是否实时推送", example = "true")
    private Boolean realtimePush;

    @Schema(description = "推送URL", example = "https://example.com/webhook")
    private String webhookUrl;

    @Schema(description = "扩展参数")
    private Map<String, Object> extendedParams;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "检测区域")
    public static class DetectionRegion {
        @Schema(description = "区域ID", example = "REGION001")
        private String regionId;

        @Schema(description = "区域名称", example = "禁区1")
        private String regionName;

        @Schema(description = "区域类型", example = "polygon")
        private String regionType;

        @Schema(description = "坐标点列表")
        private List<Point> points;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "坐标点")
    public static class Point {
        @Schema(description = "X坐标", example = "100")
        private Integer x;

        @Schema(description = "Y坐标", example = "200")
        private Integer y;
    }
}
