package net.lab1024.sa.common.organization.controller;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.organization.entity.AreaDeviceEntity;
import net.lab1024.sa.common.organization.service.AreaDeviceService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 区域设备管理控制器
 * <p>
 * 企业级区域设备管理REST API
 * 提供设备关联、状态监控、权限管理等接口
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/area-device")
@Tag(name = "区域设备管理", description = "区域设备管理相关接口")
@Validated
public class AreaDeviceController {

    @Resource
    private AreaDeviceService areaDeviceService;

    @Operation(summary = "添加设备到区域")
    @Observed(name = "areaDevice.addDeviceToArea", contextualName = "area-device-add")
    @PostMapping("/add")
    public ResponseDTO<AreaDeviceEntity> addDeviceToArea(
            @Parameter(description = "区域ID", required = true)
            @RequestParam @NotNull(message = "区域ID不能为空") Long areaId,
            @Parameter(description = "设备ID", required = true)
            @RequestParam @NotBlank(message = "设备ID不能为空") String deviceId,
            @Parameter(description = "设备编码", required = true)
            @RequestParam @NotBlank(message = "设备编码不能为空") String deviceCode,
            @Parameter(description = "设备名称", required = true)
            @RequestParam @NotBlank(message = "设备名称不能为空") String deviceName,
            @Parameter(description = "设备类型", required = true)
            @RequestParam @NotNull(message = "设备类型不能为空") Integer deviceType,
            @Parameter(description = "业务模块", required = true)
            @RequestParam @NotBlank(message = "业务模块不能为空") String businessModule) {

        log.info("[区域设备API] 添加设备到区域: areaId={}, deviceId={}, deviceType={}, module={}",
                areaId, deviceId, deviceType, businessModule);

        AreaDeviceEntity result = areaDeviceService.addDeviceToArea(areaId, deviceId, deviceCode, deviceName, deviceType, businessModule);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "移除区域中的设备")
    @Observed(name = "areaDevice.removeDeviceFromArea", contextualName = "area-device-remove")
    @DeleteMapping("/remove")
    public ResponseDTO<Void> removeDeviceFromArea(
            @Parameter(description = "区域ID", required = true)
            @RequestParam @NotNull(message = "区域ID不能为空") Long areaId,
            @Parameter(description = "设备ID", required = true)
            @RequestParam @NotBlank(message = "设备ID不能为空") String deviceId) {

        log.info("[区域设备API] 移除区域中的设备: areaId={}, deviceId={}", areaId, deviceId);

        boolean result = areaDeviceService.removeDeviceFromArea(areaId, deviceId);
        return result ? ResponseDTO.ok() : ResponseDTO.error("REMOVE_FAILED", "移除设备失败");
    }

    @Operation(summary = "获取区域中的所有设备")
    @Observed(name = "areaDevice.getAreaDevices", contextualName = "area-device-get-area-devices")
    @GetMapping("/area/{areaId}/devices")
    public ResponseDTO<List<AreaDeviceEntity>> getAreaDevices(
            @Parameter(description = "区域ID", required = true) @PathVariable Long areaId) {

        log.debug("[区域设备API] 获取区域设备: areaId={}", areaId);

        List<AreaDeviceEntity> devices = areaDeviceService.getAreaDevices(areaId);
        return ResponseDTO.ok(devices);
    }

    @Operation(summary = "获取区域中指定业务模块的设备")
    @Observed(name = "areaDevice.getAreaDevicesByModule", contextualName = "area-device-get-by-module")
    @GetMapping("/area/{areaId}/devices/module/{businessModule}")
    public ResponseDTO<List<AreaDeviceEntity>> getAreaDevicesByModule(
            @Parameter(description = "区域ID", required = true) @PathVariable Long areaId,
            @Parameter(description = "业务模块", required = true) @PathVariable String businessModule) {

        log.debug("[区域设备API] 获取区域业务模块设备: areaId={}, module={}", areaId, businessModule);

        List<AreaDeviceEntity> devices = areaDeviceService.getAreaDevicesByModule(areaId, businessModule);
        return ResponseDTO.ok(devices);
    }

    @Operation(summary = "获取用户可访问的设备")
    @Observed(name = "areaDevice.getUserAccessibleDevices", contextualName = "area-device-get-user-devices")
    @GetMapping("/user/{userId}/devices")
    public ResponseDTO<List<AreaDeviceEntity>> getUserAccessibleDevices(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "业务模块") @RequestParam(required = false) String businessModule) {

        log.debug("[区域设备API] 获取用户可访问设备: userId={}, module={}", userId, businessModule);

        List<AreaDeviceEntity> devices = areaDeviceService.getUserAccessibleDevices(userId, businessModule);
        return ResponseDTO.ok(devices);
    }

    @Operation(summary = "检查设备是否在区域中")
    @Observed(name = "areaDevice.isDeviceInArea", contextualName = "area-device-check")
    @GetMapping("/area/{areaId}/device/{deviceId}/check")
    public ResponseDTO<Boolean> isDeviceInArea(
            @Parameter(description = "区域ID", required = true) @PathVariable Long areaId,
            @Parameter(description = "设备ID", required = true) @PathVariable String deviceId) {

        log.debug("[区域设备API] 检查设备是否在区域: areaId={}, deviceId={}", areaId, deviceId);

        boolean inArea = areaDeviceService.isDeviceInArea(areaId, deviceId);
        return ResponseDTO.ok(inArea);
    }

    @Operation(summary = "设置设备业务属性")
    @Observed(name = "areaDevice.setDeviceBusinessAttributes", contextualName = "area-device-set-attributes")
    @PostMapping("/device/{deviceId}/area/{areaId}/attributes")
    public ResponseDTO<Void> setDeviceBusinessAttributes(
            @Parameter(description = "设备ID", required = true) @PathVariable String deviceId,
            @Parameter(description = "区域ID", required = true) @PathVariable Long areaId,
            @Valid @RequestBody Map<String, Object> businessAttributes) {

        log.info("[区域设备API] 设置设备业务属性: deviceId={}, areaId={}", deviceId, areaId);

        areaDeviceService.setDeviceBusinessAttributes(deviceId, areaId, businessAttributes);
        return ResponseDTO.ok();
    }

    @Operation(summary = "获取设备业务属性")
    @Observed(name = "areaDevice.getDeviceBusinessAttributes", contextualName = "area-device-get-attributes")
    @GetMapping("/device/{deviceId}/attributes")
    public ResponseDTO<Map<String, Object>> getDeviceBusinessAttributes(
            @Parameter(description = "设备ID", required = true) @PathVariable String deviceId,
            @Parameter(description = "区域ID") @RequestParam(required = false) Long areaId) {

        log.debug("[区域设备API] 获取设备业务属性: deviceId={}, areaId={}", deviceId, areaId);

        Map<String, Object> attributes = areaDeviceService.getDeviceBusinessAttributes(deviceId, areaId);
        return ResponseDTO.ok(attributes);
    }

    @Operation(summary = "更新设备状态")
    @Observed(name = "areaDevice.updateDeviceRelationStatus", contextualName = "area-device-update-status")
    @PutMapping("/relation/{relationId}/status")
    public ResponseDTO<Void> updateDeviceRelationStatus(
            @Parameter(description = "关联ID", required = true) @PathVariable String relationId,
            @Parameter(description = "关联状态", required = true)
            @RequestParam @NotNull(message = "关联状态不能为空") Integer relationStatus) {

        log.info("[区域设备API] 更新设备状态: relationId={}, status={}", relationId, relationStatus);

        areaDeviceService.updateDeviceRelationStatus(relationId, relationStatus);
        return ResponseDTO.ok();
    }

    @Operation(summary = "获取区域设备统计信息")
    @Observed(name = "areaDevice.getAreaDeviceStatistics", contextualName = "area-device-get-statistics")
    @GetMapping("/area/{areaId}/statistics")
    public ResponseDTO<Map<String, Object>> getAreaDeviceStatistics(
            @Parameter(description = "区域ID", required = true) @PathVariable Long areaId) {

        log.debug("[区域设备API] 获取区域设备统计: areaId={}", areaId);

        Map<String, Object> statistics = areaDeviceService.getAreaDeviceStatistics(areaId);
        return ResponseDTO.ok(statistics);
    }

    @Operation(summary = "获取设备属性模板")
    @Observed(name = "areaDevice.getDeviceAttributeTemplate", contextualName = "area-device-get-template")
    @GetMapping("/device/template")
    public ResponseDTO<Map<String, Object>> getDeviceAttributeTemplate(
            @Parameter(description = "设备类型", required = true)
            @RequestParam @NotNull(message = "设备类型不能为空") Integer deviceType,
            @Parameter(description = "设备子类型") @RequestParam(required = false) Integer deviceSubType) {

        log.debug("[区域设备API] 获取设备属性模板: deviceType={}, deviceSubType={}", deviceType, deviceSubType);

        Map<String, Object> template = areaDeviceService.getDeviceAttributeTemplate(deviceType, deviceSubType);
        return ResponseDTO.ok(template);
    }

    @Operation(summary = "同步区域用户权限到设备")
    @Observed(name = "areaDevice.syncAreaUserPermissionsToDevice", contextualName = "area-device-sync-permissions")
    @PostMapping("/area/{areaId}/device/{deviceId}/sync-users")
    public ResponseDTO<Void> syncAreaUserPermissionsToDevice(
            @Parameter(description = "区域ID", required = true) @PathVariable Long areaId,
            @Parameter(description = "设备ID", required = true) @PathVariable String deviceId) {

        log.info("[区域设备API] 同步区域用户权限到设备: areaId={}, deviceId={}", areaId, deviceId);

        areaDeviceService.syncAreaUserPermissionsToDevice(areaId, deviceId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "检查用户是否有设备访问权限")
    @Observed(name = "areaDevice.hasDeviceAccessPermission", contextualName = "area-device-check-permission")
    @GetMapping("/user/{userId}/area/{areaId}/device/{deviceId}/check-permission")
    public ResponseDTO<Boolean> hasDeviceAccessPermission(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "区域ID", required = true) @PathVariable Long areaId,
            @Parameter(description = "设备ID", required = true) @PathVariable String deviceId) {

        log.debug("[区域设备API] 检查设备访问权限: userId={}, areaId={}, deviceId={}", userId, areaId, deviceId);

        boolean hasPermission = areaDeviceService.hasDeviceAccessPermission(userId, areaId, deviceId);
        return ResponseDTO.ok(hasPermission);
    }
}
