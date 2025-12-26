package net.lab1024.sa.video.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 开始推流请求
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "开始推流请求")
public class StartStreamRequest {

    @Schema(description = "流类型", example = "main", allowableValues = {"main", "sub"})
    @NotBlank(message = "流类型不能为空")
    @Pattern(regexp = "^(main|sub)$", message = "流类型只能是main或sub")
    private String streamType;

    @Schema(description = "视频编码", example = "h264", allowableValues = {"h264", "h265"})
    private String videoCodec;

    @Schema(description = "音频编码", example = "aac", allowableValues = {"aac", "g711a", "g711u"})
    private String audioCodec;

    @Schema(description = "视频分辨率", example = "1920x1080")
    private String resolution;

    @Schema(description = "帧率", example = "25")
    private Integer frameRate;

    @Schema(description = "码率(kbps)", example = "2000")
    private Integer bitrate;

    @Schema(description = "是否录制", example = "true")
    private Boolean enableRecording;

    @Schema(description = "推流超时时间(秒)", example = "300")
    private Integer timeoutSeconds;

    @Schema(description = "推流参数", example = "")
    private String streamParams;
}
