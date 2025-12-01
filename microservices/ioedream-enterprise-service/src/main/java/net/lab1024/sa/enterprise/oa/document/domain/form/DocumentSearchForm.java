package net.lab1024.sa.enterprise.oa.document.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.page.PageForm;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "文档搜索表单")
public class DocumentSearchForm extends PageForm {

    @Schema(description = "搜索关键词")
    private String keyword;

    @Schema(description = "文档分类ID")
    private Long categoryId;

    @Schema(description = "文档类型")
    private String documentType;

    @Schema(description = "标签")
    private String tags;

    @Schema(description = "开始时间")
    private java.time.LocalDateTime startDate;

    @Schema(description = "结束时间")
    private java.time.LocalDateTime endDate;

    @Schema(description = "搜索范围", example = "title,content,summary")
    private String searchScope;

    @Schema(description = "排序方式", example = "createTime,updateTime,title")
    private String sortBy;

    @Schema(description = "排序方向", example = "asc,desc")
    private String sortDirection;
}
