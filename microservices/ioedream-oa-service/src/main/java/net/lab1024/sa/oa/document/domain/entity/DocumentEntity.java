package net.lab1024.sa.oa.document.domain.entity;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 文档实体
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class DocumentEntity extends BaseEntity {

    /**
     * 文档ID
     */
    private Long documentId;

    /**
     * 文档编号
     */
    private String documentNo;

    /**
     * 文档标题
     */
    private String title;

    /**
     * 文档内容
     */
    private String content;

    /**
     * 文档摘要
     */
    private String summary;

    /**
     * 文档类型 (WORD-文档, EXCEL-表格, PPT-演示, PDF-文档, TXT-文本, OTHER-其他)
     */
    private String documentType;

    /**
     * 文档格式 (DOC, DOCX, XLS, XLSX, PPT, PPTX, PDF, TXT, MD, HTML)
     */
    private String format;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 文档分类
     */
    private Long categoryId;

    /**
     * 文档分类名称
     */
    private String categoryName;

    /**
     * 文档标签 (JSON数组格式)
     */
    private String tags;

    /**
     * 关键词 (JSON数组格式)
     */
    private String keywords;

    /**
     * 创建人ID
     */
    private Long createdById;

    /**
     * 创建人姓名
     */
    private String createdByName;

    /**
     * 创建人部门ID
     */
    private Long createdByDeptId;

    /**
     * 创建人部门名称
     */
    private String createdByDeptName;

    /**
     * 最后修改人ID
     */
    private Long lastModifiedById;

    /**
     * 最后修改人姓名
     */
    private String lastModifiedByName;

    /**
     * 最后修改时间
     */
    private LocalDateTime lastModifiedTime;

    /**
     * 审批状态 (DRAFT-草稿, PENDING-待审批, APPROVED-已审批, REJECTED-已驳回)
     */
    private String approvalStatus;

    /**
     * 审批人ID
     */
    private Long approvedById;

    /**
     * 审批人姓名
     */
    private String approvedByName;

    /**
     * 审批时间
     */
    private LocalDateTime approvedTime;

    /**
     * 审批意见
     */
    private String approvalComment;

    /**
     * 文档状态 (ACTIVE-正常, ARCHIVED-已归档, DELETED-已删除)
     */
    private String status;

    /**
     * 归档时间
     */
    private LocalDateTime archivedTime;

    /**
     * 访问权限 (PUBLIC-公开, PRIVATE-私有, DEPT-部门, ROLE-角色, CUSTOM-自定义)
     */
    private String accessPermission;

    /**
     * 权限配置JSON
     */
    private String permissionConfig;

    /**
     * 主文档ID (用于版本管理)
     */
    private Long masterDocumentId;

    /**
     * 是否为最新版本 (0-否, 1-是)
     */
    private Integer isLatestVersion;

    /**
     * 是否允许编辑 (0-否, 1-是)
     */
    private Integer allowEdit;

    /**
     * 是否允许下载 (0-否, 1-是)
     */
    private Integer allowDownload;

    /**
     * 是否允许打印 (0-否, 1-是)
     */
    private Integer allowPrint;

    /**
     * 是否允许分享 (0-否, 1-是)
     */
    private Integer allowShare;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 下载次数
     */
    private Integer downloadCount;

    /**
     * 收藏次数
     */
    private Integer favoriteCount;

    /**
     * 评论次数
     */
    private Integer commentCount;

    /**
     * 最后访问时间
     */
    private LocalDateTime lastAccessTime;

    /**
     * 最后访问人ID
     */
    private Long lastAccessById;

    /**
     * 最后访问人姓名
     */
    private String lastAccessByName;

    /**
     * 协作模式 (NONE-无协作, VIEW-只读, EDIT-编辑, COMMENT-评论)
     */
    private String collaborationMode;

    /**
     * 协作者列表 (JSON格式)
     */
    private String collaborators;

    /**
     * 缩略图路径
     */
    private String thumbnailPath;

    /**
     * 预览URL
     */
    private String previewUrl;

    /**
     * 搜索索引内容
     */
    private String searchIndex;

    /**
     * 元数据JSON
     */
    private String metadata;

    /**
     * 备注
     */
    private String remark;
}
