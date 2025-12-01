package net.lab1024.sa.common.page;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分页查询基类（非泛型版本）
 * <p>
 * 用于查询表单继承，包含分页参数
 * 严格遵循repowiki规范：统一的分页查询基类
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Data
@Schema(description = "分页查询参数")
public class PageForm {

    /**
     * 当前页
     */
    @Schema(description = "当前页", example = "1")
    private Integer pageNum = 1;

    /**
     * 每页的数量
     */
    @Schema(description = "每页的数量", example = "20")
    private Integer pageSize = 20;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段", example = "createTime")
    private String sortField;

    /**
     * 排序方式：asc/desc
     */
    @Schema(description = "排序方式", example = "desc", allowableValues = { "asc", "desc" })
    private String sortOrder;
}
