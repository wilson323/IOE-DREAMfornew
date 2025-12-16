package net.lab1024.sa.common.organization.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.organization.manager.AreaPermissionManager;
import net.lab1024.sa.common.organization.service.AreaPermissionService;
import net.lab1024.sa.common.response.ResponseDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 区域权限管理控制器
 * <p>
 * 提供完整的区域权限管理API接口
 * 包括权限分配、撤销、检查、统计、同步等功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/organization/area-permission")
@RequiredArgsConstructor
@Tag(name = "区域权限管理", description = "区域权限管理相关接口")
@Validated
public class AreaPermissionManageController {

    private final AreaPermissionService areaPermissionService;

    @PostMapping("/grant")
    @Operation(summary = "分配区域权限", description = "为用户分配指定区域的访问权限")
    public ResponseDTO<String> grantAreaPermission(@Valid @RequestBody AreaPermissionGrantRequest request) {
        return ResponseDTO.ok(areaPermissionService.grantAreaPermission(
                request.getAreaId(),
                request.getUserId(),
                request.getUsername(),
                request.getRealName(),
                request.getRelationType(),
                request.getPermissionLevel(),
                request.getEffectiveTime(),
                request.getExpireTime(),
                request.getAllowStartTime(),
                request.getAllowEndTime(),
                request.getWorkdayOnly(),
                request.getAccessPermissions(),
                request.getExtendedAttributes(),
                request.getRemark()
        ));
    }

    @PostMapping("/revoke/{relationId}")
    @Operation(summary = "撤销区域权限", description = "撤销用户的区域访问权限")
    public ResponseDTO<Boolean> revokeAreaPermission(
            @Parameter(description = "权限关联ID", required = true)
            @PathVariable @NotBlank String relationId) {
        return ResponseDTO.ok(areaPermissionService.revokeAreaPermission(relationId));
    }

    @GetMapping("/check")
    @Operation(summary = "检查区域权限", description = "检查用户是否具有指定区域的访问权限")
    public ResponseDTO<AreaPermissionCheckResponse> checkAreaPermission(
            @Parameter(description = "用户ID", required = true)
            @RequestParam @NotNull Long userId,
            @Parameter(description = "区域ID", required = true)
            @RequestParam @NotNull Long areaId,
            @Parameter(description = "所需权限级别", required = false)
            @RequestParam(defaultValue = "1") Integer requiredPermissionLevel) {
        AreaPermissionManager.PermissionCheckResult result = areaPermissionService.checkAreaPermission(userId, areaId, requiredPermissionLevel);

        AreaPermissionCheckResponse response = new AreaPermissionCheckResponse();
        response.setGranted(result.isGranted());
        response.setReason(result.getReason());
        response.setPermission(result.getPermission());

        return ResponseDTO.ok(response);
    }

    @GetMapping("/user/{userId}/accessible-areas")
    @Operation(summary = "获取用户可访问区域", description = "获取用户具有访问权限的所有区域ID列表")
    public ResponseDTO<List<Long>> getUserAccessibleAreas(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId) {
        return ResponseDTO.ok(areaPermissionService.getUserAccessibleAreas(userId));
    }

    @GetMapping("/area/{areaId}/users")
    @Operation(summary = "获取区域用户权限列表", description = "获取指定区域的所有用户权限列表")
    public ResponseDTO<List<AreaPermissionService.AreaUserPermissionVO>> getAreaUserPermissions(
            @Parameter(description = "区域ID", required = true)
            @PathVariable @NotNull Long areaId,
            @Parameter(description = "权限级别（可选）", required = false)
            @RequestParam(required = false) Integer permissionLevel) {
        return ResponseDTO.ok(areaPermissionService.getAreaUserPermissions(areaId, permissionLevel));
    }

    @PostMapping("/batch-sync")
    @Operation(summary = "批量同步权限到设备", description = "将指定权限批量同步到相关设备")
    public ResponseDTO<AreaPermissionManager.BatchSyncResult> batchSyncToDevices(
            @Valid @RequestBody BatchSyncRequest request) {
        return ResponseDTO.ok(areaPermissionService.batchSyncToDevices(request.getRelationIds()));
    }

    @GetMapping("/area/{areaId}/statistics")
    @Operation(summary = "获取区域权限统计", description = "获取指定区域的权限统计信息")
    public ResponseDTO<AreaPermissionManager.AreaPermissionStatistics> getAreaPermissionStatistics(
            @Parameter(description = "区域ID", required = true)
            @PathVariable @NotNull Long areaId) {
        return ResponseDTO.ok(areaPermissionService.getAreaPermissionStatistics(areaId));
    }

    @PostMapping("/clean-expired")
    @Operation(summary = "清理过期权限", description = "清理所有已过期的区域权限")
    public ResponseDTO<Integer> cleanExpiredPermissions() {
        return ResponseDTO.ok(areaPermissionService.cleanExpiredPermissions());
    }

    @PutMapping("/update/{relationId}")
    @Operation(summary = "更新区域权限", description = "更新指定的区域权限配置")
    public ResponseDTO<Boolean> updateAreaPermission(
            @Parameter(description = "权限关联ID", required = true)
            @PathVariable @NotBlank String relationId,
            @Valid @RequestBody AreaPermissionUpdateRequest request) {
        return ResponseDTO.ok(areaPermissionService.updateAreaPermission(
                relationId,
                request.getPermissionLevel(),
                request.getEffectiveTime(),
                request.getExpireTime(),
                request.getAllowStartTime(),
                request.getAllowEndTime(),
                request.getWorkdayOnly(),
                request.getAccessPermissions(),
                request.getExtendedAttributes(),
                request.getRemark()
        ));
    }

    @GetMapping("/detail/{relationId}")
    @Operation(summary = "获取用户权限详情", description = "获取指定权限关联的详细信息")
    public ResponseDTO<AreaPermissionService.AreaUserPermissionVO> getUserPermissionDetail(
            @Parameter(description = "权限关联ID", required = true)
            @PathVariable @NotBlank String relationId) {
        return ResponseDTO.ok(areaPermissionService.getUserPermissionDetail(relationId));
    }

    @PostMapping("/batch-grant")
    @Operation(summary = "批量分配区域权限", description = "为多个用户批量分配指定区域的权限")
    public ResponseDTO<AreaPermissionService.BatchGrantResult> batchGrantAreaPermission(
            @Valid @RequestBody BatchGrantRequest request) {
        return ResponseDTO.ok(areaPermissionService.batchGrantAreaPermission(
                request.getAreaId(),
                request.getUserIds(),
                request.getPermissionLevel(),
                request.getRelationType()
        ));
    }

    @GetMapping("/user/{userId}/history")
    @Operation(summary = "获取用户权限历史", description = "获取用户在指定区域的权限变更历史")
    public ResponseDTO<List<AreaPermissionService.AreaPermissionHistoryVO>> getUserPermissionHistory(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId,
            @Parameter(description = "区域ID（可选）", required = false)
            @RequestParam(required = false) Long areaId) {
        return ResponseDTO.ok(areaPermissionService.getUserPermissionHistory(userId, areaId));
    }

    @GetMapping("/area/{areaId}/validate")
    @Operation(summary = "验证区域权限配置", description = "验证指定区域的权限配置是否正确")
    public ResponseDTO<AreaPermissionService.AreaPermissionValidationResult> validateAreaPermissionConfig(
            @Parameter(description = "区域ID", required = true)
            @PathVariable @NotNull Long areaId) {
        return ResponseDTO.ok(areaPermissionService.validateAreaPermissionConfig(areaId));
    }

    // ==================== 便捷查询接口 ====================

    @GetMapping("/user/{userId}/area/{areaId}/quick-check")
    @Operation(summary = "快速检查权限", description = "快速检查用户是否具有区域访问权限")
    public ResponseDTO<Boolean> quickCheckPermission(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId,
            @Parameter(description = "区域ID", required = true)
            @PathVariable @NotNull Long areaId) {
        AreaPermissionManager.PermissionCheckResult result = areaPermissionService.checkAreaPermission(userId, areaId, 1);
        return ResponseDTO.ok(result.isGranted());
    }

    @GetMapping("/area/{areaId}/count")
    @Operation(summary = "获取区域权限数量", description = "获取指定区域的权限数量统计")
    public ResponseDTO<Integer> getAreaPermissionCount(
            @Parameter(description = "区域ID", required = true)
            @PathVariable @NotNull Long areaId) {
        AreaPermissionManager.AreaPermissionStatistics statistics = areaPermissionService.getAreaPermissionStatistics(areaId);
        return ResponseDTO.ok(statistics != null ? statistics.getTotalUsers() : 0);
    }

    // ==================== 内部类 ====================

    /**
     * 区域权限分配请求
     */
    public static class AreaPermissionGrantRequest {
        @NotNull(message = "区域ID不能为空")
        private Long areaId;

        @NotNull(message = "用户ID不能为空")
        private Long userId;

        @NotBlank(message = "用户名不能为空")
        private String username;

        private String realName;

        @NotNull(message = "关联类型不能为空")
        private Integer relationType;

        @NotNull(message = "权限级别不能为空")
        private Integer permissionLevel;

        private LocalDateTime effectiveTime;
        private LocalDateTime expireTime;
        private String allowStartTime;
        private String allowEndTime;
        private Boolean workdayOnly;
        private String accessPermissions;
        private String extendedAttributes;
        private String remark;

        // getters and setters
        public Long getAreaId() { return areaId; }
        public void setAreaId(Long areaId) { this.areaId = areaId; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getRealName() { return realName; }
        public void setRealName(String realName) { this.realName = realName; }
        public Integer getRelationType() { return relationType; }
        public void setRelationType(Integer relationType) { this.relationType = relationType; }
        public Integer getPermissionLevel() { return permissionLevel; }
        public void setPermissionLevel(Integer permissionLevel) { this.permissionLevel = permissionLevel; }
        public LocalDateTime getEffectiveTime() { return effectiveTime; }
        public void setEffectiveTime(LocalDateTime effectiveTime) { this.effectiveTime = effectiveTime; }
        public LocalDateTime getExpireTime() { return expireTime; }
        public void setExpireTime(LocalDateTime expireTime) { this.expireTime = expireTime; }
        public String getAllowStartTime() { return allowStartTime; }
        public void setAllowStartTime(String allowStartTime) { this.allowStartTime = allowStartTime; }
        public String getAllowEndTime() { return allowEndTime; }
        public void setAllowEndTime(String allowEndTime) { this.allowEndTime = allowEndTime; }
        public Boolean getWorkdayOnly() { return workdayOnly; }
        public void setWorkdayOnly(Boolean workdayOnly) { this.workdayOnly = workdayOnly; }
        public String getAccessPermissions() { return accessPermissions; }
        public void setAccessPermissions(String accessPermissions) { this.accessPermissions = accessPermissions; }
        public String getExtendedAttributes() { return extendedAttributes; }
        public void setExtendedAttributes(String extendedAttributes) { this.extendedAttributes = extendedAttributes; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }

    /**
     * 区域权限更新请求
     */
    public static class AreaPermissionUpdateRequest {
        private Integer permissionLevel;
        private LocalDateTime effectiveTime;
        private LocalDateTime expireTime;
        private String allowStartTime;
        private String allowEndTime;
        private Boolean workdayOnly;
        private String accessPermissions;
        private String extendedAttributes;
        private String remark;

        // getters and setters
        public Integer getPermissionLevel() { return permissionLevel; }
        public void setPermissionLevel(Integer permissionLevel) { this.permissionLevel = permissionLevel; }
        public LocalDateTime getEffectiveTime() { return effectiveTime; }
        public void setEffectiveTime(LocalDateTime effectiveTime) { this.effectiveTime = effectiveTime; }
        public LocalDateTime getExpireTime() { return expireTime; }
        public void setExpireTime(LocalDateTime expireTime) { this.expireTime = expireTime; }
        public String getAllowStartTime() { return allowStartTime; }
        public void setAllowStartTime(String allowStartTime) { this.allowStartTime = allowStartTime; }
        public String getAllowEndTime() { return allowEndTime; }
        public void setAllowEndTime(String allowEndTime) { this.allowEndTime = allowEndTime; }
        public Boolean getWorkdayOnly() { return workdayOnly; }
        public void setWorkdayOnly(Boolean workdayOnly) { this.workdayOnly = workdayOnly; }
        public String getAccessPermissions() { return accessPermissions; }
        public void setAccessPermissions(String accessPermissions) { this.accessPermissions = accessPermissions; }
        public String getExtendedAttributes() { return extendedAttributes; }
        public void setExtendedAttributes(String extendedAttributes) { this.extendedAttributes = extendedAttributes; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }

    /**
     * 批量同步请求
     */
    public static class BatchSyncRequest {
        @NotNull(message = "关联ID列表不能为空")
        private List<String> relationIds;

        // getters and setters
        public List<String> getRelationIds() { return relationIds; }
        public void setRelationIds(List<String> relationIds) { this.relationIds = relationIds; }
    }

    /**
     * 批量分配请求
     */
    public static class BatchGrantRequest {
        @NotNull(message = "区域ID不能为空")
        private Long areaId;

        @NotNull(message = "用户ID列表不能为空")
        private List<Long> userIds;

        @NotNull(message = "权限级别不能为空")
        private Integer permissionLevel;

        @NotNull(message = "关联类型不能为空")
        private Integer relationType;

        // getters and setters
        public Long getAreaId() { return areaId; }
        public void setAreaId(Long areaId) { this.areaId = areaId; }
        public List<Long> getUserIds() { return userIds; }
        public void setUserIds(List<Long> userIds) { this.userIds = userIds; }
        public Integer getPermissionLevel() { return permissionLevel; }
        public void setPermissionLevel(Integer permissionLevel) { this.permissionLevel = permissionLevel; }
        public Integer getRelationType() { return relationType; }
        public void setRelationType(Integer relationType) { this.relationType = relationType; }
    }

    /**
     * 权限检查响应
     */
    public static class AreaPermissionCheckResponse {
        private boolean granted;
        private String reason;
        private AreaUserEntity permission;

        // getters and setters
        public boolean isGranted() { return granted; }
        public void setGranted(boolean granted) { this.granted = granted; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public AreaUserEntity getPermission() { return permission; }
        public void setPermission(AreaUserEntity permission) { this.permission = permission; }
    }
}