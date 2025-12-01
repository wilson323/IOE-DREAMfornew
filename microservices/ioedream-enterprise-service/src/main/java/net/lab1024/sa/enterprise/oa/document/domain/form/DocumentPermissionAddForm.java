package net.lab1024.sa.enterprise.oa.document.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 文档权限新增表单
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@Schema(description = "文档权限新增表单")
public class DocumentPermissionAddForm {

    /**
     * 权限类型 (USER-用户, DEPT-部门, ROLE-角色, CUSTOM-自定义)
     */
    @Schema(description = "权限类型", example = "USER")
    @NotNull(message = "权限类型不能为空")
    private String permissionType;

    /**
     * 权限对象ID (用户ID、部门ID或角色ID)
     */
    @Schema(description = "权限对象ID", example = "1")
    @NotNull(message = "权限对象ID不能为空")
    private Long targetId;

    /**
     * 访问级别 (VIEW-查看, EDIT-编辑, DOWNLOAD-下载, PRINT-打印, SHARE-分享, DELETE-删除,
     * COMMENT-评论)
     */
    @Schema(description = "访问级别", example = "VIEW")
    private String accessLevel;
}
