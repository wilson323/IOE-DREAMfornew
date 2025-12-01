package net.lab1024.sa.admin.module.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 操作权限结果VO
 * 严格遵循repowiki规范：VO类用于数据传输
 *
 * @author SmartAdmin Team
 * @date 2025/11/21
 */




@Schema(description = "操作权限结果")
public class OperationPermissionResult {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "操作类型")
    private String operationType;

    @Schema(description = "是否有权限")
    private Boolean hasPermission;

    @Schema(description = "权限等级")
    private Integer permissionLevel;

    @Schema(description = "拒绝原因")
    private String rejectionReason;

    @Schema(description = "检查时间")
    private LocalDateTime checkTime;

    // 手动添加的getter/setter方法 (Lombok失效备用)




    public Boolean isHasPermission() {
        return hasPermission;
    }







}
