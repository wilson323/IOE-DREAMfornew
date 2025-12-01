package net.lab1024.sa.admin.module.oa.document.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "文档权限更新表单")
public class DocumentPermissionUpdateForm {
    @NotNull(message = "权限ID不能为空")
    private Long permissionId;
    private String accessLevel;
}


