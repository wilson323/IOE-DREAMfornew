package net.lab1024.sa.enterprise.oa.document.domain.vo;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文档版本VO
 *
 * 用于展示文档版本的详细信息，包括版本号、变更内容、创建信息等
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
@Schema(description = "文档版本VO")
public class DocumentVersionVO {

    /**
     * 版本ID
     */
    @Schema(description = "版本ID", example = "1")
    private Long versionId;

    /**
     * 文档ID
     */
    @Schema(description = "文档ID", example = "1")
    private Long documentId;

    /**
     * 版本号
     */
    @Schema(description = "版本号", example = "1")
    private Integer versionNumber;

    /**
     * 版本名称
     */
    @Schema(description = "版本名称", example = "v1.0")
    private String versionName;

    /**
     * 标题
     */
    @Schema(description = "标题", example = "项目需求文档")
    private String title;

    /**
     * 版本描述
     */
    @Schema(description = "版本描述", example = "初始版本")
    private String versionDescription;

    /**
     * 修改内容摘要
     */
    @Schema(description = "修改内容摘要", example = "更新了需求描述")
    private String changeSummary;

    /**
     * 文件大小(字节)
     */
    @Schema(description = "文件大小(字节)", example = "1024000")
    private Long fileSize;

    /**
     * 文件URL
     */
    @Schema(description = "文件URL", example = "/files/document/1/version/1/doc.docx")
    private String fileUrl;

    /**
     * 变更类型 (CREATE-创建, UPDATE-更新, MAJOR-主要变更, MINOR-次要变更)
     */
    @Schema(description = "变更类型", example = "UPDATE")
    private String changeType;

    /**
     * 是否为主要版本 (0-否, 1-是)
     */
    @Schema(description = "是否为主要版本", example = "0")
    private Integer isMajorVersion;

    /**
     * 是否为当前版本 (0-否, 1-是)
     */
    @Schema(description = "是否为当前版本", example = "1")
    private Integer isCurrentVersion;

    /**
     * 上一个版本ID
     */
    @Schema(description = "上一个版本ID", example = "0")
    private Long previousVersionId;

    /**
     * 版本标签 (如: v1.0, v2.0-beta)
     */
    @Schema(description = "版本标签", example = "v1.0")
    private String versionTag;

    /**
     * 变更日志
     */
    @Schema(description = "变更日志", example = "修复了已知问题")
    private String changelog;

    /**
     * 审批状态 (PENDING-待审批, APPROVED-已审批, REJECTED-已驳回)
     */
    @Schema(description = "审批状态", example = "APPROVED")
    private String approvalStatus;

    /**
     * 审批人姓名
     */
    @Schema(description = "审批人姓名", example = "管理员")
    private String approvedByName;

    /**
     * 审批时间
     */
    @Schema(description = "审批时间", example = "2025-01-30T10:00:00")
    private LocalDateTime approvedTime;

    /**
     * 审批意见
     */
    @Schema(description = "审批意见", example = "同意")
    private String approvalComment;

    /**
     * 版本状态 (ACTIVE-正常, ARCHIVED-已归档, DELETED-已删除)
     */
    @Schema(description = "版本状态", example = "ACTIVE")
    private String status;

    /**
     * 归档时间
     */
    @Schema(description = "归档时间", example = "2025-01-30T10:00:00")
    private LocalDateTime archivedTime;

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID", example = "1")
    private Long createdBy;

    /**
     * 创建人姓名
     */
    @Schema(description = "创建人姓名", example = "张三")
    private String createdByName;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-01-30T10:00:00")
    private LocalDateTime createdTime;

    /**
     * 创建时间（兼容字段）
     */
    @Schema(description = "创建时间", example = "2025-01-30T10:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2025-01-30T10:00:00")
    private LocalDateTime updateTime;
}
