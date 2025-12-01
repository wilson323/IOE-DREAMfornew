package net.lab1024.sa.oa.document.domain.entity;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 文档版本实体
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class DocumentVersionEntity extends BaseEntity {

    /**
     * 版本ID
     */
    private Long versionId;

    /**
     * 文档ID
     */
    private Long documentId;

    /**
     * 版本号
     */
    private Integer versionNumber;

    /**
     * 版本名称
     */
    private String versionName;

    /**
     * 版本描述
     */
    private String versionDescription;

    /**
     * 修改内容摘要
     */
    private String changeSummary;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 创建人姓名
     */
    private String createdByName;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 文档内容
     */
    private String content;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 变更类型 (CREATE-创建, UPDATE-更新, MAJOR-主要变更, MINOR-次要变更)
     */
    private String changeType;

    /**
     * 是否为主要版本 (0-否, 1-是)
     */
    private Integer isMajorVersion;

    /**
     * 是否为当前版本 (0-否, 1-是)
     */
    private Integer isCurrentVersion;

    /**
     * 上一个版本ID
     */
    private Long previousVersionId;

    /**
     * 版本标签 (如: v1.0, v2.0-beta)
     */
    private String versionTag;

    /**
     * 变更日志
     */
    private String changelog;

    /**
     * 差异对比数据 (JSON格式)
     */
    private String diffData;

    /**
     * 审批状态 (PENDING-待审批, APPROVED-已审批, REJECTED-已驳回)
     */
    private String approvalStatus;

    /**
     * 审批人ID
     */
    private Long approvedBy;

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
     * 版本状态 (ACTIVE-正常, ARCHIVED-已归档, DELETED-已删除)
     */
    private String status;

    /**
     * 归档时间
     */
    private LocalDateTime archivedTime;

    /**
     * 缩略图路径
     */
    private String thumbnailPath;

    /**
     * 预览URL
     */
    private String previewUrl;

    /**
     * 备注信息
     */
    private String remark;
}
