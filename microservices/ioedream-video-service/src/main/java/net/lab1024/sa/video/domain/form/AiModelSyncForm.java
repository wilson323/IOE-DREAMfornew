package net.lab1024.sa.video.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * AI模型设备同步表单
 * <p>
 * 用于将AI模型同步到设备的请求参数：
 * 1. 模型ID
 * 2. 设备ID列表
 * </p>
 *
 * @author IOE-DREAM AI Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "AI模型设备同步表单")
public class AiModelSyncForm {

    @Schema(description = "模型ID", example = "1")
    @NotNull(message = "模型ID不能为空")
    private Long modelId;

    @Schema(description = "设备ID列表", example = "[\"device_001\", \"device_002\"]")
    @NotEmpty(message = "设备ID列表不能为空")
    private List<String> deviceIds;
}
