package net.lab1024.sa.enterprise.oa.document.domain.vo;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文档权限VO
 *
 * 用于展示文档权限的详细信息，包括权限类型、权限对象、权限范围等
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
@Schema(description = "文档权限VO")
public class DocumentPermissionVO {

    /**
     * 权限ID
     */
    @Schema(description = "权限ID", example = "1")
    private Long permissionId;

    /**
     * 文档ID
     */
    @Schema(description = "文档ID", example = "1")
    private Long documentId;

    /**
     * 权限类型 (USER-用户, DEPT-部门, ROLE-角色, CUSTOM-自定义)
     */
    @Schema(description = "权限类型", example = "USER")
    private String permissionType;

    /**
     * 权限对象ID (用户ID、部门ID或角色ID)
     */
    @Schema(description = "权限对象ID", example = "1")
    private Long targetId;

    /**
     * 权限对象名称
     */
    @Schema(description = "权限对象名称", example = "张三")
    private String targetName;

    /**
     * 权限列表 (JSON数组格式: ["view", "edit", "download", "print", "share", "delete",
     * "comment"])
     */
    @Schema(description = "权限列表", example = "[\"view\",\"download\"]")
    private String permissions;

    /**
     * 是否允许查看 (0-否, 1-是)
     */
    @Schema(description = "是否允许查看", example = "1")
    private Integer allowView;

    /**
     * 是否允许编辑 (0-否, 1-是)
     */
    @Schema(description = "是否允许编辑", example = "0")
    private Integer allowEdit;

    /**
     * 是否允许下载 (0-否, 1-是)
     */
    @Schema(description = "是否允许下载", example = "1")
    private Integer allowDownload;

    /**
     * 是否允许打印 (0-否, 1-是)
     */
    @Schema(description = "是否允许打印", example = "1")
    private Integer allowPrint;

    /**
     * 是否允许分享 (0-否, 1-是)
     */
    @Schema(description = "是否允许分享", example = "0")
    private Integer allowShare;

    /**
     * 是否允许删除 (0-否, 1-是)
     */
    @Schema(description = "是否允许删除", example = "0")
    private Integer allowDelete;

    /**
     * 是否允许评论 (0-否, 1-是)
     */
    @Schema(description = "是否允许评论", example = "1")
    private Integer allowComment;

    /**
     * 是否允许管理权限 (0-否, 1-是)
     */
    @Schema(description = "是否允许管理权限", example = "0")
    private Integer allowManage;

    /**
     * 权限有效期开始时间
     */
    @Schema(description = "权限有效期开始时间", example = "2025-01-30T10:00:00")
    private LocalDateTime effectiveStartTime;

    /**
     * 权限有效期结束时间
     */
    @Schema(description = "权限有效期结束时间", example = "2025-12-31T23:59:59")
    private LocalDateTime effectiveEndTime;

    /**
     * 是否永久有效 (0-否, 1-是)
     */
    @Schema(description = "是否永久有效", example = "1")
    private Integer isPermanent;

    /**
     * 授权人ID
     */
    @Schema(description = "授权人ID", example = "1")
    private Long grantedById;

    /**
     * 授权人姓名
     */
    @Schema(description = "授权人姓名", example = "管理员")
    private String grantedByName;

    /**
     * 访问级别
     */
    @Schema(description = "访问级别", example = "READ")
    private String accessLevel;

    /**
     * 授权时间
     */
    @Schema(description = "授权时间", example = "2025-01-30T10:00:00")
    private LocalDateTime grantedTime;

    /**
     * 权限状态 (ACTIVE-有效, EXPIRED-已过期, REVOKED-已撤销)
     */
    @Schema(description = "权限状态", example = "ACTIVE")
    private String status;

    /**
     * 撤销时间
     */
    @Schema(description = "撤销时间", example = "2025-01-30T10:00:00")
    private LocalDateTime revokedTime;

    /**
     * 撤销人姓名
     */
    @Schema(description = "撤销人姓名", example = "管理员")
    private String revokedByName;

    /**
     * 撤销原因
     */
    @Schema(description = "撤销原因", example = "权限调整")
    private String revokeReason;

    /**
     * 最后访问时间
     */
    @Schema(description = "最后访问时间", example = "2025-01-30T10:00:00")
    private LocalDateTime lastAccessTime;

    /**
     * 访问次数
     */
    @Schema(description = "访问次数", example = "10")
    private Integer accessCount;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-01-30T10:00:00")
    private LocalDateTime createTime;
}
