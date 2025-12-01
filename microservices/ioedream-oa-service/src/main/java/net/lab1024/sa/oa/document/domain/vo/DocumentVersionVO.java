package net.lab1024.sa.oa.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "文档版本VO")
public class DocumentVersionVO {

    @Schema(description = "版本ID")
    private Long versionId;

    @Schema(description = "文档ID")
    private Long documentId;

    @Schema(description = "版本号")
    private Integer versionNumber;

    @Schema(description = "版本名称")
    private String versionName;

    @Schema(description = "变更类型")
    private String changeType;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "创建人")
    private String createdByName;

    @Schema(description = "版本描述")
    private String versionDescription;

    @Schema(description = "是否为当前版本")
    private Integer isCurrentVersion;
}