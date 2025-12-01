package net.lab1024.sa.enterprise.oa.document.domain.form;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文档版本创建表单
 * 严格遵循repowiki规范
 */
@Data
@EqualsAndHashCode(callSuper = false)
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
     * 文件ID列表
     */
    @Schema(description = "文件ID列表", example = "[1, 2, 3]")
    @NotNull(message = "文件ID列表不能为空")
    private List<Long> fileIds;

    /**
     * 是否主版本
     */
    @Schema(description = "是否主版本", example = "false")
    private Boolean isMainVersion = false;

    /**
     * 是否发布版本
     */
    @Schema(description = "是否发布版本", example = "true")
    private Boolean isReleasedVersion = true;

    /**
     * 生效时间
     */
    @Schema(description = "生效时间", example = "2023-12-01 00:00:00")
    private String effectiveTime;

    /**
     * 失效时间
     */
    @Schema(description = "失效时间", example = "2024-12-01 00:00:00")
    private String expireTime;

    /**
     * 版本标签
     */
    @Schema(description = "版本标签", example = "stable")
    @Size(max = 100, message = "版本标签长度不能超过100个字符")
    private String versionTag;

    /**
     * 变更摘要
     */
    @Schema(description = "变更摘要", example = "性能优化、bug修复")
    @Size(max = 2000, message = "变更摘要长度不能超过2000个字符")
    private String changeSummary;

    /**
     * 兼容性说明
     */
    @Schema(description = "兼容性说明", example = "向前兼容")
    @Size(max = 500, message = "兼容性说明长度不能超过500个字符")
    private String compatibilityInfo;

    /**
     * 相关文档ID列表
     */
    @Schema(description = "相关文档ID列表", example = "[1, 2]")
    private List<Long> relatedDocumentIds;

    /**
     * 审批人ID列表
     */
    @Schema(description = "审批人ID列表", example = "[1, 2]")
    private List<Long> approverIds;

    /**
     * 通知人员ID列表
     */
    @Schema(description = "通知人员ID列表", example = "[1, 2, 3]")
    private List<Long> notifyUserIds;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "重要版本")
    @Size(max = 1000, message = "备注长度不能超过1000个字符")
    private String remark;

    /**
     * 获取版本类型描述
     */
    public String getVersionTypeDesc() {
        switch (versionType) {
            case 1:
                return "新建";
            case 2:
                return "修订";
            case 3:
                return "重大更新";
            case 4:
                return "紧急修复";
            default:
                return "未知类型";
        }
    }

    /**
     * 是否为正式版本
     */
    public boolean isOfficialVersion() {
        return isReleasedVersion != null && isReleasedVersion;
    }

    /**
     * 是否有生效时间
     */
    public boolean hasEffectiveTime() {
        return effectiveTime != null && !effectiveTime.trim().isEmpty();
    }

    /**
     * 是否有失效时间
     */
    public boolean hasExpireTime() {
        return expireTime != null && !expireTime.trim().isEmpty();
    }

    /**
     * 是否需要审批
     */
    public boolean needsApproval() {
        return approverIds != null && !approverIds.isEmpty();
    }

    /**
     * 获取文件数量
     */
    public int getFileCount() {
        return fileIds != null ? fileIds.size() : 0;
    }

    /**
     * 获取变更摘要摘要（截取前100字符）
     */
    public String getChangeSummaryAbstract() {
        if (changeSummary == null || changeSummary.isEmpty()) {
            return "";
        }
        return changeSummary.length() > 100 ? changeSummary.substring(0, 100) + "..." : changeSummary;
    }
}
