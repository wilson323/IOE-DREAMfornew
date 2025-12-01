package net.lab1024.sa.admin.module.oa.document.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import net.lab1024.sa.admin.module.oa.document.domain.enums.DocumentStatusEnum;
import net.lab1024.sa.admin.module.oa.document.domain.enums.DocumentTypeEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;

@Data
@Schema(description = "文档新增表单")
public class DocumentAddForm {

    @Schema(description = "文档标题", required = true, example = "2024年度工作总结")
    @NotBlank(message = "文档标题不能为空")
    @Size(max = 200, message = "文档标题长度不能超过200个字符")
    private String title;

    @Schema(description = "文档内容")
    private String content;

    @Schema(description = "文档摘要", example = "2024年度工作总结报告")
    @Size(max = 500, message = "文档摘要长度不能超过500个字符")
    private String summary;

    @Schema(description = "文档类型", required = true, example = "WORD")
    @NotBlank(message = "文档类型不能为空")
    @CheckEnum(value = DocumentTypeEnum.class, message = "文档类型无效")
    private String documentType;

    @Schema(description = "文档分类ID", required = true)
    @NotNull(message = "文档分类不能为空")
    private Long categoryId;

    @Schema(description = "文件路径", example = "/upload/2024/12/16/document.docx")
    private String filePath;

    @Schema(description = "文件名", example = "document.docx")
    private String fileName;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "文档状态", example = "DRAFT")
    @CheckEnum(value = DocumentStatusEnum.class, message = "文档状态无效")
    private String status = "DRAFT";

    @Schema(description = "优先级", example = "HIGH")
    private String priority = "MEDIUM";

    @Schema(description = "标签", example = "总结,报告,2024")
    @Size(max = 200, message = "标签长度不能超过200个字符")
    private String tags;

    @Schema(description = "关键词", example = "工作总结,年度报告")
    @Size(max = 300, message = "关键词长度不能超过300个字符")
    private String keywords;

    @Schema(description = "作者", example = "张三")
    @Size(max = 100, message = "作者长度不能超过100个字符")
    private String author;

    @Schema(description = "发布者", example = "李四")
    @Size(max = 100, message = "发布者长度不能超过100个字符")
    private String publisher;

    @Schema(description = "发布日期", example = "2024-12-16T10:00:00")
    private java.time.LocalDateTime publishDate;

    @Schema(description = "版本号", example = "1")
    private Integer version = 1;

    @Schema(description = "访问级别", example = "INTERNAL")
    private String accessLevel = "INTERNAL";

    @Schema(description = "过期时间", example = "2025-12-16T10:00:00")
    private java.time.LocalDateTime expireTime;

}