package net.lab1024.sa.oa.document.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 文档权限更新表单
 *
 * @author IOE-DREAM Team
 * @since 2025-11-29
 */
@Data
@Schema(description = "文档权限更新表单")
public class DocumentPermissionUpdateForm {

    @Schema(description = "权限ID", required = true)
    @NotNull(message = "权限ID不能为空")
    private Long permissionId;

    /**
     * 访问级别 (VIEW-查看, EDIT-编辑, DOWNLOAD-下载, PRINT-打印, SHARE-分享, DELETE-删除,
     * COMMENT-评论)
     */
    @Schema(description = "访问级别", example = "VIEW")
    private String accessLevel;

    @Schema(description = "备注")
    private String remark;
}
