package net.lab1024.sa.common.organization.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 区域用户权限VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Data
@Schema(description = "区域用户权限VO")
public class AreaUserPermissionVO {

    @Schema(description = "权限ID")
    private String relationId;

    @Schema(description = "区域ID")
    private Long areaId;

    @Schema(description = "区域编码")
    private String areaCode;

    @Schema(description = "区域名称")
    private String areaName;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "关联类型")
    private Integer relationType;

    @Schema(description = "关联类型名称")
    private String relationTypeName;

    @Schema(description = "权限级别")
    private Integer permissionLevel;

    @Schema(description = "权限级别名称")
    private String permissionLevelName;

    @Schema(description = "生效时间")
    private LocalDateTime effectiveTime;

    @Schema(description = "失效时间")
    private LocalDateTime expireTime;

    @Schema(description = "是否永久")
    private Boolean permanent;

    @Schema(description = "允许开始时间")
    private String allowStartTime;

    @Schema(description = "允许结束时间")
    private String allowEndTime;

    @Schema(description = "仅工作日有效")
    private Boolean workdayOnly;

    @Schema(description = "访问权限配置")
    private String accessPermissions;

    @Schema(description = "设备同步状态")
    private Integer deviceSyncStatus;

    @Schema(description = "设备同步状态名称")
    private String deviceSyncStatusName;

    @Schema(description = "最后同步时间")
    private LocalDateTime lastSyncTime;

    @Schema(description = "扩展属性")
    private String extendedAttributes;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}