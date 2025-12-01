package net.lab1024.sa.oa.document.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "文档权限VO")
public class DocumentPermissionVO {

    @Schema(description = "权限ID")
    private Long permissionId;

    @Schema(description = "文档ID")
    private Long documentId;

    @Schema(description = "权限类型编码")
    private String roleCode;

    @Schema(description = "访问级别")
    private String accessLevel;

    @Schema(description = "权限对象ID")
    private Long targetId;

    @Schema(description = "权限对象名称")
    private String targetName;

    @Schema(description = "权限状态")
    private String status;

    @Schema(description = "授权时间")
    private LocalDateTime grantedTime;

    @Schema(description = "授权人")
    private String grantedByName;
}