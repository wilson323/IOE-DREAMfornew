package net.lab1024.sa.video.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * AI模型上传表单
 * <p>
 * 用于上传AI模型的请求参数：
 * 1. 模型文件
 * 2. 模型基础信息
 * 3. 模型类型和版本
 * 4. 支持的事件类型
 * </p>
 *
 * @author IOE-DREAM AI Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "AI模型上传表单")
public class AiModelUploadForm {

    @Schema(description = "模型文件（最大500MB）")
    @NotNull(message = "模型文件不能为空")
    private MultipartFile file;

    @Schema(description = "模型名称", example = "fall_detection_v2")
    @NotBlank(message = "模型名称不能为空")
    @Size(max = 100, message = "模型名称长度不能超过100个字符")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "模型名称只能包含字母、数字、下划线和连字符")
    private String modelName;

    @Schema(description = "模型版本", example = "2.0.0")
    @NotBlank(message = "模型版本不能为空")
    @Size(max = 50, message = "模型版本长度不能超过50个字符")
    @Pattern(regexp = "^\\d+\\.\\d+\\.\\d+$", message = "模型版本格式应为x.y.z")
    private String modelVersion;

    @Schema(description = "模型类型", example = "YOLOv8", allowableValues = {"YOLOv8", "YOLOv7", "ResNet", "Custom"})
    @NotBlank(message = "模型类型不能为空")
    @Size(max = 50, message = "模型类型长度不能超过50个字符")
    private String modelType;

    @Schema(description = "支持的事件类型（JSON数组）", example = "[\"FALL_DETECTION\", \"ABNORMAL_GAIT\"]")
    @Size(max = 500, message = "支持的事件类型长度不能超过500个字符")
    private String supportedEvents;
}
