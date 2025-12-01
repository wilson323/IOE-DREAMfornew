package net.lab1024.sa.admin.module.oa.document.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 批量分类更新DTO
 * 严格遵循repowiki规范：定义批量分类更新的数据传输对象
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Data
@Schema(description = "批量分类更新DTO")
public class BatchCategoryDTO {

    @Schema(description = "文档ID列表", required = true)
    @NotNull(message = "文档ID列表不能为空")
    private List<Long> documentIds;

    @Schema(description = "目标分类ID", example = "1", required = true)
    @NotNull(message = "目标分类ID不能为空")
    private Long categoryId;

    @Schema(description = "目标分类名称", example = "技术文档")
    private String categoryName;

    @Schema(description = "更新原因", example = "重新分类")
    private String reason;

    @Schema(description = "是否包含子分类", example = "true")
    private Boolean includeSubcategories;

    /**
     * 验证参数有效性
     *
     * @return 验证结果
     */
    public boolean isValid() {
        if (documentIds == null || documentIds.isEmpty()) {
            return false;
        }
        if (categoryId == null || categoryId <= 0) {
            return false;
        }
        return true;
    }
}