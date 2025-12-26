package net.lab1024.sa.video.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 对象追踪请求
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "对象追踪请求")
public class ObjectTrackingRequest {

    @NotBlank(message = "对象ID不能为空")
    @Schema(description = "对象ID", example = "OBJ001", required = true)
    private String objectId;

    @Schema(description = "对象类型", example = "person", allowableValues = {"person", "vehicle", "object"})
    private String objectType;

    @Schema(description = "起始设备ID", example = "CAM001")
    private String startDeviceId;

    @Schema(description = "目标设备ID列表")
    private List<String> targetDeviceIds;

    @Schema(description = "开始时间", example = "2025-12-16T10:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2025-12-16T11:00:00")
    private LocalDateTime endTime;

    @Schema(description = "追踪模式", example = "realtime", allowableValues = {"realtime", "historical"})
    private String trackingMode;

    @Schema(description = "对象特征(如人脸特征向量)")
    private String objectFeature;

    @Schema(description = "图片URL(用于以图搜图)", example = "https://example.com/image.jpg")
    private String imageUrl;

    @Schema(description = "置信度阈值", example = "0.8")
    private Double confidenceThreshold;

    @Schema(description = "是否实时推送", example = "true")
    private Boolean realtimePush;

    @Schema(description = "推送URL", example = "https://example.com/webhook")
    private String webhookUrl;

    @Schema(description = "扩展参数")
    private Map<String, Object> extendedParams;
}
