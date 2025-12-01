package net.lab1024.sa.admin.module.oa.document.domain.form;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.oa.document.domain.enums.DocumentStatusEnum;
import net.lab1024.sa.admin.module.oa.document.domain.enums.DocumentTypeEnum;
import net.lab1024.sa.base.common.page.PageForm;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;

/**
 * 文档查询表单
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@Schema(description = "文档查询表单")
public class DocumentQueryForm extends PageForm {

    @Schema(description = "文档分类ID")
    private Long categoryId;

    @Schema(description = "文档类型", example = "WORD,PDF,IMAGE")
    @CheckEnum(value = DocumentTypeEnum.class, message = "文档类型无效")
    private String documentType;

    @Schema(description = "文档状态", example = "DRAFT,PUBLISHED")
    @CheckEnum(value = DocumentStatusEnum.class, message = "文档状态无效")
    private String status;

    @Schema(description = "关键词搜索（标题、内容、关键词）")
    private String keyword;

    @Schema(description = "标签搜索")
    private String tags;

    @Schema(description = "开始时间")
    private LocalDateTime startDate;

    @Schema(description = "结束时间")
    private LocalDateTime endDate;

    @Schema(description = "创建人ID")
    private Long createdById;

    @Schema(description = "作者")
    private String author;

    @Schema(description = "优先级", example = "HIGH,MEDIUM,LOW")
    private String priority;

    @Schema(description = "访问级别", example = "PUBLIC,INTERNAL,PRIVATE")
    private String accessLevel;

    @Schema(description = "是否包含过期文档")
    private Boolean includeExpired = false;

    @Schema(description = "排序字段", example = "createTime,viewCount,downloadCount")
    private String sortBy = "createTime";

    @Schema(description = "排序方向", example = "asc,desc")
    private String sortDirection = "desc";

}
