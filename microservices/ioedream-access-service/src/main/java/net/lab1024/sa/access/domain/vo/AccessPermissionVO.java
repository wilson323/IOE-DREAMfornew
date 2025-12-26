package net.lab1024.sa.access.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 门禁权限VO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-24
 */
@Data
@Schema(description = "门禁权限VO")
public class AccessPermissionVO {

    @Schema(description = "权限ID")
    private Long permissionId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "区域ID")
    private Long areaId;

    @Schema(description = "区域名称")
    private String areaName;

    @Schema(description = "区域编码")
    private String areaCode;

    @Schema(description = "权限类型：1-永久，2-临时，3-时段")
    private Integer permissionType;

    @Schema(description = "权限类型名称")
    private String permissionTypeName;

    @Schema(description = "权限状态：1-有效，2-即将过期，3-已过期，4-已冻结")
    private Integer permissionStatus;

    @Schema(description = "权限状态名称")
    private String permissionStatusName;

    @Schema(description = "权限级别")
    private Integer permissionLevel;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "是否永久")
    private Boolean permanent;

    @Schema(description = "允许进入时间段（开始）")
    private String allowStartTime;

    @Schema(description = "允许进入时间段（结束）")
    private String allowEndTime;

    @Schema(description = "仅工作日")
    private Boolean workdayOnly;

    @Schema(description = "可通行方式：人脸/指纹/IC卡/密码/二维码")
    private String accessPermissions;

    @Schema(description = "扩展属性（JSON）")
    private String extendedAttributes;

    @Schema(description = "通行次数")
    private Integer passCount;

    @Schema(description = "最后通行时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastPassTime;

    @Schema(description = "设备同步状态：0-未同步，1-同步成功，2-同步失败")
    private Integer deviceSyncStatus;

    @Schema(description = "最后同步时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastSyncTime;

    @Schema(description = "距离过期天数（负数表示已过期多少天）")
    private Long daysUntilExpiry;
}
