package net.lab1024.sa.oa.document.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 文档版本创建表单
 * 用于创建文档新版本的数据传输对象
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Data
@Schema(description = "文档版本创建表单")
public class DocumentVersionCreateForm {

    /**
     * 文档ID
     */
    @Schema(description = "文档ID", example = "1")
    @NotNull(message = "文档ID不能为空")
    private Long documentId;

    /**
     * 版本号
     */
    @Schema(description = "版本号", example = "1.0.1")
    @NotBlank(message = "版本号不能为空")
    @Size(max = 50, message = "版本号长度不能超过50个字符")
    private String versionNumber;

    /**
     * 版本标题
     */
    @Schema(description = "版本标题", example = "修订版")
    @NotBlank(message = "版本标题不能为空")
    @Size(max = 200, message = "版本标题长度不能超过200个字符")
    private String versionTitle;

    /**
     * 版本描述
     */
    @Schema(description = "版本描述", example = "修复了几个重要bug")
    @Size(max = 1000, message = "版本描述长度不能超过1000个字符")
    private String versionDescription;

    /**
     * 版本类型：1-新建 2-修订 3-重大更新 4-紧急修复
     */
    @Schema(description = "版本类型", example = "2")
    @NotNull(message = "版本类型不能为空")
    private Integer versionType;

    /**
     * 变更摘要
     */
    @Schema(description = "变更摘要", example = "性能优化、bug修复")
    @Size(max = 2000, message = "变更摘要长度不能超过2000个字符")
    private String changeSummary;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "重要版本")
    @Size(max = 1000, message = "备注长度不能超过1000个字符")
    private String remark;
}
