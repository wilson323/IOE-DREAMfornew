package net.lab1024.sa.admin.module.oa.document.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "版本创建表单")
public class DocumentVersionCreateForm {
    private String changeLog;
    private String changeType;
}


