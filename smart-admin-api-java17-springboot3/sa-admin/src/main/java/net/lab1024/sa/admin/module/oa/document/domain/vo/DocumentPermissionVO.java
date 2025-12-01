package net.lab1024.sa.admin.module.oa.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "文档权限VO")
public class DocumentPermissionVO {
    private Long permissionId;
    private Long documentId;
    private String roleCode;
    private String accessLevel;
}


