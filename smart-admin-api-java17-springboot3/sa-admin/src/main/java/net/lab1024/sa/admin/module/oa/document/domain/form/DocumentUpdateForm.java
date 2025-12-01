package net.lab1024.sa.admin.module.oa.document.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "文档更新表单")
public class DocumentUpdateForm {
    @NotNull(message = "文档ID不能为空")
    private Long documentId;
    private String title;
    private String content;
    private String status;
}


