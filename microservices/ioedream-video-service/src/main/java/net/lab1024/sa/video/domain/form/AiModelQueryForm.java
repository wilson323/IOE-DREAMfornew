package net.lab1024.sa.video.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * AI模型查询表单
 * <p>
 * 用于查询AI模型列表的请求参数：
 * 1. 模型类型过滤
 * 2. 模型状态过滤
 * 3. 分页参数
 * </p>
 *
 * @author IOE-DREAM AI Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "AI模型查询表单")
public class AiModelQueryForm {

    @Schema(description = "模型类型（可选）", example = "YOLOv8")
    private String modelType;

    @Schema(description = "模型状态（0-草稿 1-已发布 2-已弃用）", example = "1")
    private Integer modelStatus;

    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "20")
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer pageSize = 20;
}
