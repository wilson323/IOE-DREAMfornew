package net.lab1024.sa.common.organization.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 区域权限历史VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Data
@Schema(description = "区域权限历史VO")
public class AreaPermissionHistoryVO {

    @Schema(description = "历史ID")
    private String historyId;

    @Schema(description = "关联ID")
    private String relationId;

    @Schema(description = "区域ID")
    private Long areaId;

    @Schema(description = "区域名称")
    private String areaName;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "操作类型")
    private String operationType;

    @Schema(description = "操作类型名称")
    private String operationTypeName;

    @Schema(description = "旧权限级别")
    private Integer oldPermissionLevel;

    @Schema(description = "新权限级别")
    private Integer newPermissionLevel;

    @Schema(description = "变更原因")
    private String changeReason;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "操作时间")
    private LocalDateTime operationTime;
}