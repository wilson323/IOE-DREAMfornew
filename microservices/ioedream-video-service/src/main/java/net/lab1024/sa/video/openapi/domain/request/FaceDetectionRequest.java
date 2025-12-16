package net.lab1024.sa.video.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 人脸识别检测请求
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "人脸识别检测请求")
public class FaceDetectionRequest {

    @Schema(description = "设备ID", example = "CAM001")
    @NotBlank(message = "设备ID不能为空")
    private String deviceId;

    @Schema(description = "检测类型", example = "realtime", allowableValues = {"realtime", "image", "video"})
    @NotBlank(message = "检测类型不能为空")
    private String detectionType;

    @Schema(description = "图像URL或视频URL", example = "https://example.com/image.jpg")
    private String mediaUrl;

    @Schema(description = "检测区域坐标", example = "[{x:100,y:100,width:200,height:200}]")
    private List<DetectionRegion> detectionRegions;

    @Schema(description = "人脸库ID列表", example = "[1,2,3]")
    private List<Long> faceDatabaseIds;

    @Schema(description = "相似度阈值", example = "0.8")
    private Double similarityThreshold;

    @Schema(description = "最大检测人脸数", example = "10")
    private Integer maxFaces;

    @Schema(description = "是否返回人脸特征", example = "false")
    private Boolean returnFaceFeatures;

    @Schema(description = "是否进行活体检测", example = "true")
    private Boolean enableLivenessCheck;

    @Schema(description = "检测时长(秒)", example = "30")
    private Integer detectionDuration;

    @Schema(description = "截图间隔(秒)", example = "1")
    private Integer captureInterval;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "检测区域")
    public static class DetectionRegion {

        @Schema(description = "X坐标", example = "100")
        private Integer x;

        @Schema(description = "Y坐标", example = "100")
        private Integer y;

        @Schema(description = "宽度", example = "200")
        private Integer width;

        @Schema(description = "高度", example = "200")
        private Integer height;

        @Schema(description = "区域名称", example = "门口区域")
        private String regionName;
    }
}