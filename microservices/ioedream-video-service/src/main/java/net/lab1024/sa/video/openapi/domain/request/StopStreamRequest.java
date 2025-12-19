package net.lab1024.sa.video.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 停止推流请求
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "停止推流请求")
public class StopStreamRequest {

    @Schema(description = "流类型", example = "main", allowableValues = {"main", "sub"})
    @NotBlank(message = "流类型不能为空")
    @Pattern(regexp = "^(main|sub)$", message = "流类型只能是main或sub")
    private String streamType;

    @Schema(description = "停止原因", example = "用户主动停止")
    private String reason;

    @Schema(description = "是否保存录制文件", example = "true")
    private Boolean saveRecording;
}