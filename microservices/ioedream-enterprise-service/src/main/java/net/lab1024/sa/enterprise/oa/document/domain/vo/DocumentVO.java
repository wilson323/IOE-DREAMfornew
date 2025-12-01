package net.lab1024.sa.enterprise.oa.document.domain.vo;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文档VO
 *
 * 用于展示文档的详细信息，包括文档基本信息、状态、权限等
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
@Schema(description = "文档VO")
public class DocumentVO {

    /**
     * 文档ID
     */
    @Schema(description = "文档ID", example = "1")
    private Long documentId;

    /**
     * 文档编号
     */
    @Schema(description = "文档编号", example = "DOC20250130001")
    private String documentNo;

    /**
     * 文档标题
     */
    @Schema(description = "文档标题", example = "项目需求文档")
    private String title;

    /**
     * 文档摘要
     */
    @Schema(description = "文档摘要", example = "本文档描述了项目的核心需求")
    private String summary;

    /**
     * 文档类型 (WORD-文档, EXCEL-表格, PPT-演示, PDF-文档, TXT-文本, OTHER-其他)
     */
    @Schema(description = "文档类型", example = "WORD")
    private String documentType;

    /**
     * 文档格式 (DOC, DOCX, XLS, XLSX, PPT, PPTX, PDF, TXT, MD, HTML)
     */
    @Schema(description = "文档格式", example = "DOCX")
    private String format;

    /**
     * 文件大小(字节)
     */
    @Schema(description = "文件大小(字节)", example = "1024000")
    private Long fileSize;

    /**
     * 文件URL
     */
    @Schema(description = "文件URL", example = "/files/document/1/doc.docx")
    private String fileUrl;

    /**
     * 文档分类ID
     */
    @Schema(description = "文档分类ID", example = "1")
    private Long categoryId;

    /**
     * 文档分类名称
     */
    @Schema(description = "文档分类名称", example = "项目文档")
    private String categoryName;

    /**
     * 文档标签 (JSON数组格式)
     */
    @Schema(description = "文档标签", example = "[\"重要\",\"项目\"]")
    private String tags;

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID", example = "1")
    private Long createdById;

    /**
     * 创建人姓名
     */
    @Schema(description = "创建人姓名", example = "张三")
    private String author;

    /**
     * 创建人部门ID
     */
    @Schema(description = "创建人部门ID", example = "1")
    private Long createdByDeptId;

    /**
     * 创建人部门名称
     */
    @Schema(description = "创建人部门名称", example = "技术部")
    private String createdByDeptName;

    /**
     * 最后修改人ID
     */
    @Schema(description = "最后修改人ID", example = "1")
    private Long lastModifiedById;

    /**
     * 最后修改人姓名
     */
    @Schema(description = "最后修改人姓名", example = "李四")
    private String lastModifiedByName;

    /**
     * 最后修改时间
     */
    @Schema(description = "最后修改时间", example = "2025-01-30T10:00:00")
    private LocalDateTime lastModifiedTime;

    /**
     * 审批状态 (DRAFT-草稿, PENDING-待审批, APPROVED-已审批, REJECTED-已驳回)
     */
    @Schema(description = "审批状态", example = "APPROVED")
    private String approvalStatus;

    /**
     * 审批人姓名
     */
    @Schema(description = "审批人姓名", example = "王五")
    private String approvedByName;

    /**
     * 审批时间
     */
    @Schema(description = "审批时间", example = "2025-01-30T10:00:00")
    private LocalDateTime approvedTime;

    /**
     * 文档状态 (ACTIVE-正常, ARCHIVED-已归档, DELETED-已删除)
     */
    @Schema(description = "文档状态", example = "ACTIVE")
    private String status;

    /**
     * 访问权限 (PUBLIC-公开, PRIVATE-私有, DEPT-部门, ROLE-角色, CUSTOM-自定义)
     */
    @Schema(description = "访问权限", example = "DEPT")
    private String accessPermission;

    /**
     * 浏览次数
     */
    @Schema(description = "浏览次数", example = "100")
    private Integer viewCount;

    /**
     * 下载次数
     */
    @Schema(description = "下载次数", example = "50")
    private Integer downloadCount;

    /**
     * 收藏次数
     */
    @Schema(description = "收藏次数", example = "10")
    private Integer favoriteCount;

    /**
     * 最后访问时间
     */
    @Schema(description = "最后访问时间", example = "2025-01-30T10:00:00")
    private LocalDateTime lastAccessTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-01-30T10:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2025-01-30T10:00:00")
    private LocalDateTime updateTime;
}
