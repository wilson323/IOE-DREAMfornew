package net.lab1024.sa.common.organization.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.organization.entity.AreaDeviceEntity;
import net.lab1024.sa.common.organization.service.AreaDeviceService;
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
public class AreaDeviceController {

    @Resource
    private AreaDeviceService areaDeviceService;

    @Operation(summary = "添加设备到区域")
    @PostMapping("/add")
    public ResponseDTO<AreaDeviceEntity> addDeviceToArea(
            @Parameter(description = "区域ID", required = true) @RequestParam Long areaId,
            @Parameter(description = "设备ID", required = true) @RequestParam String deviceId,
            @Parameter(description = "设备编码", required = true) @RequestParam String deviceCode,
            @Parameter(description = "设备名称", required = true) @RequestParam String deviceName,
            @Parameter(description = "设备类型", required = true) @RequestParam Integer deviceType,
            @Parameter(description = "业务模块", required = true) @RequestParam String businessModule) {

        log.info("[区域设备API] 添加设备到区域: areaId={}, deviceId={}, deviceType={}, module={}",
                areaId, deviceId, deviceType, businessModule);

        try {
            AreaDeviceEntity result = areaDeviceService.addDeviceToArea(areaId, deviceId, deviceCode, deviceName, deviceType, businessModule);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[区域设备API] 添加设备到区域失败: areaId={}, deviceId={}, error={}",
                    areaId, deviceId, e.getMessage(), e);
            return ResponseDTO.error("ADD_DEVICE_FAILED", "添加设备到区域失败: " + e.getMessage());
        }
    }

    @Operation(summary = "移除区域中的设备")
    @DeleteMapping("/remove")
    public ResponseDTO<Void> removeDeviceFromArea(
            @Parameter(description = "区域ID", required = true) @RequestParam Long areaId,
            @Parameter(description = "设备ID", required = true) @RequestParam String deviceId) {

        log.info("[区域设备API] 移除区域中的设备: areaId={}, deviceId={}", areaId, deviceId);

        try {
            boolean result = areaDeviceService.removeDeviceFromArea(areaId, deviceId);
            return result ? ResponseDTO.ok() : ResponseDTO.error("REMOVE_FAILED", "移除设备失败");
        } catch (Exception e) {
            log.error("[区域设备API] 移除区域中的设备失败: areaId={}, deviceId={}, error={}",
                    areaId, deviceId, e.getMessage(), e);
            return ResponseDTO.error("REMOVE_DEVICE_FAILED", "移除设备失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取区域中的所有设备")
    @GetMapping("/area/{areaId}/devices")
    public ResponseDTO<List<AreaDeviceEntity>> getAreaDevices(
            @Parameter(description = "区域ID", required = true) @PathVariable Long areaId) {

        log.debug("[区域设备API] 获取区域设备: areaId={}", areaId);

        try {
            List<AreaDeviceEntity> devices = areaDeviceService.getAreaDevices(areaId);
            return ResponseDTO.ok(devices);
        } catch (Exception e) {
            log.error("[区域设备API] 获取区域设备失败: areaId={}, error={}", areaId, e.getMessage(), e);
            return ResponseDTO.error("GET_DEVICES_FAILED", "获取区域设备失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取区域中指定业务模块的设备")
    @GetMapping("/area/{areaId}/devices/module/{businessModule}")
    public ResponseDTO<List<AreaDeviceEntity>> getAreaDevicesByModule(
            @Parameter(description = "区域ID", required = true) @PathVariable Long areaId,
            @Parameter(description = "业务模块", required = true) @PathVariable String businessModule) {

        log.debug("[区域设备API] 获取区域业务模块设备: areaId={}, module={}", areaId, businessModule);

        try {
            List<AreaDeviceEntity> devices = areaDeviceService.getAreaDevicesByModule(areaId, businessModule);
            return ResponseDTO.ok(devices);
        } catch (Exception e) {
            log.error("[区域设备API] 获取区域业务模块设备失败: areaId={}, module={}, error={}",
                    areaId, businessModule, e.getMessage(), e);
            return ResponseDTO.error("GET_MODULE_DEVICES_FAILED", "获取业务模块设备失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取用户可访问的设备")
    @GetMapping("/user/{userId}/devices")
    public ResponseDTO<List<AreaDeviceEntity>> getUserAccessibleDevices(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "业务模块") @RequestParam(required = false) String businessModule) {

        log.debug("[区域设备API] 获取用户可访问设备: userId={}, module={}", userId, businessModule);

        try {
            List<AreaDeviceEntity> devices = areaDeviceService.getUserAccessibleDevices(userId, businessModule);
            return ResponseDTO.ok(devices);
        } catch (Exception e) {
            log.error("[区域设备API] 获取用户可访问设备失败: userId={}, module={}, error={}",
                    userId, businessModule, e.getMessage(), e);
            return ResponseDTO.error("GET_USER_DEVICES_FAILED", "获取用户可访问设备失败: " + e.getMessage());
        }
    }

    @Operation(summary = "检查设备是否在区域中")
    @GetMapping("/area/{areaId}/device/{deviceId}/check")
    public ResponseDTO<Boolean> isDeviceInArea(
            @Parameter(description = "区域ID", required = true) @PathVariable Long areaId,
            @Parameter(description = "设备ID", required = true) @PathVariable String deviceId) {

        log.debug("[区域设备API] 检查设备是否在区域: areaId={}, deviceId={}", areaId, deviceId);

        try {
            boolean inArea = areaDeviceService.isDeviceInArea(areaId, deviceId);
            return ResponseDTO.ok(inArea);
        } catch (Exception e) {
            log.error("[区域设备API] 检查设备区域关联失败: areaId={}, deviceId={}, error={}",
                    areaId, deviceId, e.getMessage(), e);
            return ResponseDTO.error("CHECK_DEVICE_FAILED", "检查设备区域关联失败: " + e.getMessage());
        }
    }

    @Operation(summary = "设置设备业务属性")
    @PostMapping("/device/{deviceId}/area/{areaId}/attributes")
    public ResponseDTO<Void> setDeviceBusinessAttributes(
            @Parameter(description = "设备ID", required = true) @PathVariable String deviceId,
            @Parameter(description = "区域ID", required = true) @PathVariable Long areaId,
            @RequestBody Map<String, Object> businessAttributes) {

        log.info("[区域设备API] 设置设备业务属性: deviceId={}, areaId={}", deviceId, areaId);

        try {
            areaDeviceService.setDeviceBusinessAttributes(deviceId, areaId, businessAttributes);
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("[区域设备API] 设置设备业务属性失败: deviceId={}, areaId={}, error={}",
                    deviceId, areaId, e.getMessage(), e);
            return ResponseDTO.error("SET_ATTRIBUTES_FAILED", "设置设备业务属性失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取设备业务属性")
    @GetMapping("/device/{deviceId}/attributes")
    public ResponseDTO<Map<String, Object>> getDeviceBusinessAttributes(
            @Parameter(description = "设备ID", required = true) @PathVariable String deviceId,
            @Parameter(description = "区域ID") @RequestParam(required = false) Long areaId) {

        log.debug("[区域设备API] 获取设备业务属性: deviceId={}, areaId={}", deviceId, areaId);

        try {
            Map<String, Object> attributes = areaDeviceService.getDeviceBusinessAttributes(deviceId, areaId);
            return ResponseDTO.ok(attributes);
        } catch (Exception e) {
            log.error("[区域设备API] 获取设备业务属性失败: deviceId={}, areaId={}, error={}",
                    deviceId, areaId, e.getMessage(), e);
            return ResponseDTO.error("GET_ATTRIBUTES_FAILED", "获取设备业务属性失败: " + e.getMessage());
        }
    }

    @Operation(summary = "更新设备状态")
    @PutMapping("/relation/{relationId}/status")
    public ResponseDTO<Void> updateDeviceRelationStatus(
            @Parameter(description = "关联ID", required = true) @PathVariable String relationId,
            @Parameter(description = "关联状态", required = true) @RequestParam Integer relationStatus) {

        log.info("[区域设备API] 更新设备状态: relationId={}, status={}", relationId, relationStatus);

        try {
            areaDeviceService.updateDeviceRelationStatus(relationId, relationStatus);
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("[区域设备API] 更新设备状态失败: relationId={}, status={}, error={}",
                    relationId, relationStatus, e.getMessage(), e);
            return ResponseDTO.error("UPDATE_STATUS_FAILED", "更新设备状态失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取区域设备统计信息")
    @GetMapping("/area/{areaId}/statistics")
    public ResponseDTO<Map<String, Object>> getAreaDeviceStatistics(
            @Parameter(description = "区域ID", required = true) @PathVariable Long areaId) {

        log.debug("[区域设备API] 获取区域设备统计: areaId={}", areaId);

        try {
            Map<String, Object> statistics = areaDeviceService.getAreaDeviceStatistics(areaId);
            return ResponseDTO.ok(statistics);
        } catch (Exception e) {
            log.error("[区域设备API] 获取区域设备统计失败: areaId={}, error={}", areaId, e.getMessage(), e);
            return ResponseDTO.error("GET_STATISTICS_FAILED", "获取区域设备统计失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取设备属性模板")
    @GetMapping("/device/template")
    public ResponseDTO<Map<String, Object>> getDeviceAttributeTemplate(
            @Parameter(description = "设备类型", required = true) @RequestParam Integer deviceType,
            @Parameter(description = "设备子类型") @RequestParam(required = false) Integer deviceSubType) {

        log.debug("[区域设备API] 获取设备属性模板: deviceType={}, deviceSubType={}", deviceType, deviceSubType);

        try {
            Map<String, Object> template = areaDeviceService.getDeviceAttributeTemplate(deviceType, deviceSubType);
            return ResponseDTO.ok(template);
        } catch (Exception e) {
            log.error("[区域设备API] 获取设备属性模板失败: deviceType={}, deviceSubType={}, error={}",
                    deviceType, deviceSubType, e.getMessage(), e);
            return ResponseDTO.error("GET_TEMPLATE_FAILED", "获取设备属性模板失败: " + e.getMessage());
        }
    }

    @Operation(summary = "同步区域用户权限到设备")
    @PostMapping("/area/{areaId}/device/{deviceId}/sync-users")
    public ResponseDTO<Void> syncAreaUserPermissionsToDevice(
            @Parameter(description = "区域ID", required = true) @PathVariable Long areaId,
            @Parameter(description = "设备ID", required = true) @PathVariable String deviceId) {

        log.info("[区域设备API] 同步区域用户权限到设备: areaId={}, deviceId={}", areaId, deviceId);

        try {
            areaDeviceService.syncAreaUserPermissionsToDevice(areaId, deviceId);
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("[区域设备API] 同步区域用户权限到设备失败: areaId={}, deviceId={}, error={}",
                    areaId, deviceId, e.getMessage(), e);
            return ResponseDTO.error("SYNC_PERMISSIONS_FAILED", "同步用户权限失败: " + e.getMessage());
        }
    }

    @Operation(summary = "检查用户是否有设备访问权限")
    @GetMapping("/user/{userId}/area/{areaId}/device/{deviceId}/check-permission")
    public ResponseDTO<Boolean> hasDeviceAccessPermission(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "区域ID", required = true) @PathVariable Long areaId,
            @Parameter(description = "设备ID", required = true) @PathVariable String deviceId) {

        log.debug("[区域设备API] 检查设备访问权限: userId={}, areaId={}, deviceId={}", userId, areaId, deviceId);

        try {
            boolean hasPermission = areaDeviceService.hasDeviceAccessPermission(userId, areaId, deviceId);
            return ResponseDTO.ok(hasPermission);
        } catch (Exception e) {
            log.error("[区域设备API] 检查设备访问权限失败: userId={}, areaId={}, deviceId={}, error={}",
                    userId, areaId, deviceId, e.getMessage(), e);
            return ResponseDTO.error("CHECK_PERMISSION_FAILED", "检查设备访问权限失败: " + e.getMessage());
        }
    }
}
