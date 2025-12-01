package net.lab1024.sa.enterprise.oa.document.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 文档权限实体
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("t_document_permission")
public class DocumentPermissionEntity extends BaseEntity {

    /**
     * 权限ID
     */
    @TableId(type = IdType.AUTO)
    private Long permissionId;

    /**
     * 文档ID
     */
    private Long documentId;

    /**
     * 权限类型 (USER-用户, DEPT-部门, ROLE-角色, CUSTOM-自定义)
     */
    private String permissionType;

    /**
     * 权限对象ID (用户ID、部门ID或角色ID)
     */
    private Long targetId;

    /**
     * 权限对象名称
     */
    private String targetName;

    /**
     * 权限列表 (JSON数组格式: ["view", "edit", "download", "print", "share", "delete",
     * "comment"])
     */
    private String permissions;

    /**
     * 是否允许查看 (0-否, 1-是)
     */
    private Integer allowView;

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
     * 是否允许删除 (0-否, 1-是)
     */
    private Integer allowDelete;

    /**
     * 是否允许评论 (0-否, 1-是)
     */
    private Integer allowComment;

    /**
     * 是否允许管理权限 (0-否, 1-是)
     */
    private Integer allowManage;

    /**
     * 权限有效期开始时间
     */
    private LocalDateTime effectiveStartTime;

    /**
     * 权限有效期结束时间
     */
    private LocalDateTime effectiveEndTime;

    /**
     * 是否永久有效 (0-否, 1-是)
     */
    private Integer isPermanent;

    /**
     * 授权人ID
     */
    private Long grantedById;

    /**
     * 授权人姓名
     */
    private String grantedByName;

    /**
     * 访问级别
     */
    private String accessLevel;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 授权时间
     */
    private LocalDateTime grantedTime;

    /**
     * 权限状态 (ACTIVE-有效, EXPIRED-已过期, REVOKED-已撤销)
     */
    private String status;

    /**
     * 撤销时间
     */
    private LocalDateTime revokedTime;

    /**
     * 撤销人ID
     */
    private Long revokedById;

    /**
     * 撤销人姓名
     */
    private String revokedByName;

    /**
     * 撤销原因
     */
    private String revokeReason;

    /**
     * 最后访问时间
     */
    private LocalDateTime lastAccessTime;

    /**
     * 访问次数
     */
    private Integer accessCount;

    /**
     * 权限备注
     */
    private String permissionRemark;

    /**
     * 备注信息
     */
    private String remark;
}
