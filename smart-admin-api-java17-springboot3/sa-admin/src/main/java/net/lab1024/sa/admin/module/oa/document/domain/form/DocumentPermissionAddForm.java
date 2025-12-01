package net.lab1024.sa.admin.module.oa.document.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "文档权限新增表单")
public class DocumentPermissionAddForm {
    @NotNull(message = "文档ID不能为空")
    private Long documentId;
    @NotNull(message = "角色编码不能为空")
    private String roleCode;
    private String accessLevel;
}


