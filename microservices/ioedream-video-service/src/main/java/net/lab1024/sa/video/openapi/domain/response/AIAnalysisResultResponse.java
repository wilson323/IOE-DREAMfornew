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
 * AI分析结果响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI分析结果响应")
public class AIAnalysisResultResponse {

    @Schema(description = "分析ID", example = "AI001")
    private String analysisId;

    @Schema(description = "设备ID", example = "CAM001")
    private String deviceId;

    @Schema(description = "设备名称", example = "前门摄像头")
    private String deviceName;

    @Schema(description = "分析类型", example = "face_detection", allowableValues = {"face_detection", "object_detection", "behavior_analysis", "plate_recognition"})
    private String analysisType;

    @Schema(description = "分析状态", example = "completed", allowableValues = {"pending", "processing", "completed", "failed"})
    private String status;

    @Schema(description = "分析时间", example = "2025-12-16T10:00:00")
    private LocalDateTime analysisTime;

    @Schema(description = "完成时间", example = "2025-12-16T10:00:05")
    private LocalDateTime completeTime;

    @Schema(description = "分析结果列表")
    private List<AnalysisResult> results;

    @Schema(description = "处理耗时(毫秒)", example = "500")
    private Long processingTime;

    @Schema(description = "截图URL", example = "https://example.com/snapshot/AI001.jpg")
    private String snapshotUrl;

    @Schema(description = "扩展信息")
    private Map<String, Object> extendedInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "分析结果")
    public static class AnalysisResult {
        @Schema(description = "结果ID", example = "RES001")
        private String resultId;

        @Schema(description = "对象类型", example = "face")
        private String objectType;

        @Schema(description = "对象标签", example = "person")
        private String label;

        @Schema(description = "置信度", example = "0.95")
        private Double confidence;

        @Schema(description = "边界框")
        private BehaviorAnalysisResponse.BoundingBox boundingBox;

        @Schema(description = "属性信息")
        private Map<String, Object> attributes;
    }
}
