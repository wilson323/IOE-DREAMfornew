package net.lab1024.sa.enterprise.oa.document.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "文档更新表单")
public class DocumentUpdateForm {

    @Schema(description = "文档ID")
    @NotNull(message = "文档ID不能为空")
    private Long documentId;

    @Schema(description = "文档标题", example = "2024年度工作总结（更新版）")
    @Size(max = 200, message = "文档标题长度不能超过200个字符")
    private String title;

    @Schema(description = "文档内容")
    private String content;

    @Schema(description = "文档摘要", example = "2024年度工作总结报告（更新版）")
    @Size(max = 500, message = "文档摘要长度不能超过500个字符")
    private String summary;

    @Schema(description = "标签", example = "总结,报告,2024,更新")
    @Size(max = 200, message = "标签长度不能超过200个字符")
    private String tags;

    @Schema(description = "关键词", example = "工作总结,年度报告")
    @Size(max = 300, message = "关键词长度不能超过300个字符")
    private String keywords;
}
