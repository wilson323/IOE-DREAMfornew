package net.lab1024.sa.oa.document.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.common.domain.PageParam;

@Data
@Schema(description = "文档查询表单")
public class DocumentQueryForm extends PageParam {

    @Schema(description = "文档分类ID")
    private Long categoryId;

    @Schema(description = "文档类型")
    private String documentType;

    @Schema(description = "文档状态")
    private String status;

    @Schema(description = "关键词")
    private String keyword;

    @Schema(description = "标签")
    private String tags;

    @Schema(description = "开始时间")
    private java.time.LocalDateTime startDate;

    @Schema(description = "结束时间")
    private java.time.LocalDateTime endDate;
}
