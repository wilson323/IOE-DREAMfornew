package net.lab1024.sa.admin.module.oa.document.domain.vo;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "文档版本VO")
public class DocumentVersionVO {
    private Long versionId;
    private Long documentId;
    private Integer versionNumber;
    private String changeType;
    private LocalDateTime createTime;
}


