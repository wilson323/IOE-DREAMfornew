package net.lab1024.sa.oa.document.domain.vo;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "文档VO")
public class DocumentVO {
    private Long documentId;
    private String title;
    private String documentType;
    private String status;
    private Long categoryId;
    private String author;
    private LocalDateTime createTime;
}