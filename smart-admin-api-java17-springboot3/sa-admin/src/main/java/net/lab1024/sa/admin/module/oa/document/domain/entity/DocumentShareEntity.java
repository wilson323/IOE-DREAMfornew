package net.lab1024.sa.admin.module.oa.document.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import net.lab1024.sa.base.common.entity.BaseEntity;

/**
 * 文档分享实体
 * <p>
 * 严格遵循repowiki规范：
 * - 继承BaseEntity，包含审计字段
 * - 完整的字段定义和注释
 * - 支持分享链接、权限控制、有效期管理
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
public class DocumentShareEntity extends BaseEntity {

    /**
     * 分享ID
     */
    private Long shareId;

    /**
     * 文档ID
     */
    private Long documentId;

    /**
     * 分享令牌（唯一标识）
     */
    private String shareToken;

    /**
     * 分享类型 (LINK-外链, USER-用户, ROLE-角色, DEPT-部门)
     */
    private String shareType;

    /**
     * 目标主体ID（当 shareType=USER/ROLE/DEPT 时必填）
     */
    private Long targetId;

    /**
     * 目标主体名称
     */
    private String targetName;

    /**
     * 分享权限 (view-查看, edit-编辑, download-下载, print-打印)
     */
    private String permission;

    /**
     * 分享链接（完整URL）
     */
    private String shareUrl;

    /**
     * 分享密码（可选，用于保护分享链接）
     */
    private String sharePassword;

    /**
     * 是否允许下载 (0-否, 1-是)
     */
    private Integer allowDownload;

    /**
     * 是否允许打印 (0-否, 1-是)
     */
    private Integer allowPrint;

    /**
     * 是否允许评论 (0-否, 1-是)
     */
    private Integer allowComment;

    /**
     * 访问次数
     */
    private Integer accessCount;

    /**
     * 下载次数
     */
    private Integer downloadCount;

    /**
     * 最后访问时间
     */
    private LocalDateTime lastAccessTime;

    /**
     * 生效时间
     */
    private LocalDateTime effectiveTime;

    /**
     * 过期时间（为空表示永久有效）
     */
    private LocalDateTime expireTime;

    /**
     * 是否永久有效 (0-否, 1-是)
     */
    private Integer isPermanent;

    /**
     * 分享状态 (ACTIVE-有效, EXPIRED-已过期, REVOKED-已撤销, DISABLED-已禁用)
     */
    private String status;

    /**
     * 分享人ID
     */
    private Long sharedById;

    /**
     * 分享人姓名
     */
    private String sharedByName;

    /**
     * 分享时间
     */
    private LocalDateTime sharedTime;

    /**
     * 备注
     */
    private String remark;

    // Getters and Setters
    public Long getShareId() {
        return shareId;
    }

    public void setShareId(Long shareId) {
        this.shareId = shareId;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getShareToken() {
        return shareToken;
    }

    public void setShareToken(String shareToken) {
        this.shareToken = shareToken;
    }

    public String getShareType() {
        return shareType;
    }

    public void setShareType(String shareType) {
        this.shareType = shareType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public String getSharePassword() {
        return sharePassword;
    }

    public void setSharePassword(String sharePassword) {
        this.sharePassword = sharePassword;
    }

    public Integer getAllowDownload() {
        return allowDownload;
    }

    public void setAllowDownload(Integer allowDownload) {
        this.allowDownload = allowDownload;
    }

    public Integer getAllowPrint() {
        return allowPrint;
    }

    public void setAllowPrint(Integer allowPrint) {
        this.allowPrint = allowPrint;
    }

    public Integer getAllowComment() {
        return allowComment;
    }

    public void setAllowComment(Integer allowComment) {
        this.allowComment = allowComment;
    }

    public Integer getAccessCount() {
        return accessCount;
    }

    public void setAccessCount(Integer accessCount) {
        this.accessCount = accessCount;
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    public LocalDateTime getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(LocalDateTime lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public LocalDateTime getEffectiveTime() {
        return effectiveTime;
    }

    public void setEffectiveTime(LocalDateTime effectiveTime) {
        this.effectiveTime = effectiveTime;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public Integer getIsPermanent() {
        return isPermanent;
    }

    public void setIsPermanent(Integer isPermanent) {
        this.isPermanent = isPermanent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getSharedById() {
        return sharedById;
    }

    public void setSharedById(Long sharedById) {
        this.sharedById = sharedById;
    }

    public String getSharedByName() {
        return sharedByName;
    }

    public void setSharedByName(String sharedByName) {
        this.sharedByName = sharedByName;
    }

    public LocalDateTime getSharedTime() {
        return sharedTime;
    }

    public void setSharedTime(LocalDateTime sharedTime) {
        this.sharedTime = sharedTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
