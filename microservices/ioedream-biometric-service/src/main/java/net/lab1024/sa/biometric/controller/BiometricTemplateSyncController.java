package net.lab1024.sa.biometric.controller;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.lab1024.sa.biometric.domain.vo.TemplateSyncResultVO;
import net.lab1024.sa.biometric.service.BiometricTemplateSyncService;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 生物模板同步控制器
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@RestController注解
 * - 使用@Resource依赖注入
 * - RESTful API设计
 * - 统一的ResponseDTO响应格式
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@RestController
@RequestMapping("/api/v1/biometric/sync")
@Tag(name = "生物模板同步", description = "生物模板同步到设备API")
@Validated
@Slf4j
public class BiometricTemplateSyncController {

    @Resource
    private BiometricTemplateSyncService biometricTemplateSyncService;

    /**
     * 同步用户模板到设备
     */
    @PostMapping("/device/{deviceId}/user/{userId}")
    @Operation(summary = "同步模板到设备", description = "将用户的所有生物模板同步到指定设备")
    public ResponseDTO<TemplateSyncResultVO> syncTemplateToDevice(
            @PathVariable @NotNull Long userId,
            @PathVariable @NotBlank String deviceId) {
        return biometricTemplateSyncService.syncTemplateToDevice(userId, deviceId);
    }

    /**
     * 同步用户模板到多个设备
     */
    @PostMapping("/devices/user/{userId}")
    @Operation(summary = "批量同步模板到设备", description = "将用户的所有生物模板同步到多个设备")
    public ResponseDTO<TemplateSyncResultVO> syncTemplateToDevices(
            @PathVariable @NotNull Long userId,
            @RequestBody @NotNull List<String> deviceIds) {
        return biometricTemplateSyncService.syncTemplateToDevices(userId, deviceIds);
    }

    /**
     * 从设备删除用户模板
     */
    @DeleteMapping("/device/{deviceId}/user/{userId}")
    @Operation(summary = "从设备删除模板", description = "从指定设备删除用户的所有生物模板")
    public ResponseDTO<Void> deleteTemplateFromDevice(
            @PathVariable @NotNull Long userId,
            @PathVariable @NotBlank String deviceId) {
        return biometricTemplateSyncService.deleteTemplateFromDevice(userId, deviceId);
    }

    /**
     * 从所有设备删除用户模板
     */
    @DeleteMapping("/all-devices/user/{userId}")
    @Operation(summary = "从所有设备删除模板", description = "用户离职时，从所有设备删除模板")
    public ResponseDTO<Void> deleteTemplateFromAllDevices(@PathVariable @NotNull Long userId) {
        return biometricTemplateSyncService.deleteTemplateFromAllDevices(userId);
    }

    /**
     * 权限新增时同步模板
     */
    @PostMapping("/permission/added")
    @Operation(summary = "权限新增时同步模板", description = "用户新增区域权限时，同步模板到该区域设备")
    public ResponseDTO<TemplateSyncResultVO> syncOnPermissionAdded(
            @RequestParam @NotNull Long userId,
            @RequestParam @NotNull Long areaId) {
        return biometricTemplateSyncService.syncOnPermissionAdded(userId, areaId);
    }

    /**
     * 权限移除时删除模板
     */
    @DeleteMapping("/permission/removed")
    @Operation(summary = "权限移除时删除模板", description = "用户移除区域权限时，从该区域设备删除模板")
    public ResponseDTO<Void> deleteOnPermissionRemoved(
            @RequestParam @NotNull Long userId,
            @RequestParam @NotNull Long areaId) {
        return biometricTemplateSyncService.deleteOnPermissionRemoved(userId, areaId);
    }

    /**
     * 批量同步模板到设备
     */
    @PostMapping("/device/{deviceId}/users")
    @Operation(summary = "批量同步模板到设备", description = "将多个用户的所有生物模板同步到指定设备")
    public ResponseDTO<TemplateSyncResultVO> batchSyncTemplatesToDevice(
            @PathVariable @NotBlank String deviceId,
            @RequestBody @NotNull List<Long> userIds) {
        return biometricTemplateSyncService.batchSyncTemplatesToDevice(userIds, deviceId);
    }
}
