package net.lab1024.sa.admin.module.video.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 批量人脸搜索DTO
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */

@Schema(description = "批量人脸搜索DTO")
public class BatchFaceSearchDTO {

    @Schema(description = "图片URL列表", required = true)
    @NotNull(message = "图片URL列表不能为空")
    private List<String> imageUrls;

    @Schema(description = "相似度阈值", example = "0.8", required = true)
    @NotNull(message = "相似度阈值不能为空")
    @Min(value = 0, message = "相似度阈值不能小于0")
    @Max(value = 1, message = "相似度阈值不能大于1")
    private Double threshold;

    @Schema(description = "返回结果数量限制", example = "10", required = true)
    @NotNull(message = "返回结果数量限制不能为空")
    @Min(value = 1, message = "返回结果数量限制不能小于1")
    @Max(value = 100, message = "返回结果数量限制不能大于100")
    private Integer limit;
}