package net.lab1024.sa.admin.module.smart.device.controller;

import java.util.List;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import net.lab1024.sa.admin.module.smart.device.domain.entity.SmartDeviceEntity;
import net.lab1024.sa.admin.module.smart.device.service.SmartDeviceService;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartResponseUtil;
import net.lab1024.sa.base.module.support.rbac.RequireResource;
import net.lab1024.sa.base.module.support.operationlog.annotation.OperationLog;

/**
 * 智能设备控制器
 *
 * @author SmartAdmin Team
 * @date 2025-11-15
 */
@RestController
@RequestMapping("/api/smart/device")
@RequiredArgsConstructor
@Tag(name = "智能设备管理", description = "智能设备相关接口")
@Validated
public class SmartDeviceController {

    private final SmartDeviceService smartDeviceService;

    @Operation(summary = "分页查询设备列表", description = "根据条件分页查询智能设备列表")
    @GetMapping("/page")
    @RequireResource(code = "device:page", scope = "AREA")
    @SaCheckPermission("device:page")
    public ResponseDTO<PageResult<SmartDeviceEntity>> queryDevicePage(@Valid PageParam pageParam,
            @RequestParam(required = false) String deviceType,
            @RequestParam(required = false) String deviceStatus,
            @RequestParam(required = false) String deviceName) {

        PageResult<SmartDeviceEntity> pageResult =
                smartDeviceService.queryDevicePage(pageParam, deviceType, deviceStatus, deviceName);
        return SmartResponseUtil.success(pageResult);
    }

    @Operation(summary = "获取设备详情", description = "根据设备ID获取设备详细信息")
    @GetMapping("/{deviceId}")
    @RequireResource(code = "device:detail", scope = "AREA")
    @SaCheckPermission("device:detail")
    public ResponseDTO<SmartDeviceEntity> getDeviceDetail(@PathVariable @NotNull Long deviceId) {
        return smartDeviceService.getDeviceDetail(deviceId);
    }

    @Operation(summary = "注册新设备", description = "新增智能设备")
    @PostMapping
    @RequireResource(code = "device:add", scope = "AREA")
    @SaCheckPermission("device:add")
    @OperationLog(operationType = "CREATE", operationDesc = "注册新设备")
    public ResponseDTO<String> registerDevice(@RequestBody @Valid SmartDeviceEntity deviceEntity) {
        return smartDeviceService.registerDevice(deviceEntity);
    }

    @Operation(summary = "更新设备信息", description = "更新智能设备的基础信息")
    @PutMapping
    @RequireResource(code = "device:update", scope = "AREA")
    @SaCheckPermission("device:update")
    @OperationLog(operationType = "UPDATE", operationDesc = "更新设备淇℃伅")
    public ResponseDTO<String> updateDevice(@RequestBody @Valid SmartDeviceEntity deviceEntity) {
        return smartDeviceService.updateDevice(deviceEntity);
    }

    @Operation(summary = "删除设备", description = "删除指定的智能设备")
    @DeleteMapping("/{deviceId}")
    @RequireResource(code = "device:delete", scope = "AREA")
    @SaCheckPermission("device:delete")
    @OperationLog(operationType = "DELETE", operationDesc = "删除设备")
    public ResponseDTO<String> deleteDevice(@PathVariable @NotNull Long deviceId) {
        return smartDeviceService.deleteDevice(deviceId);
    }

    @Operation(summary = "启用设备", description = "启用指定设备")
    @PostMapping("/{deviceId}/enable")
    @RequireResource(code = "device:enable", scope = "AREA")
    @SaCheckPermission("device:enable")
    public ResponseDTO<String> enableDevice(@PathVariable @NotNull Long deviceId) {
        return smartDeviceService.enableDevice(deviceId);
    }

    @Operation(summary = "禁用设备", description = "禁用指定设备")
    @PostMapping("/{deviceId}/disable")
    @RequireResource(code = "device:disable", scope = "AREA")
    @SaCheckPermission("device:disable")
    public ResponseDTO<String> disableDevice(@PathVariable @NotNull Long deviceId) {
        return smartDeviceService.disableDevice(deviceId);
    }

    @Operation(summary = "批量操作设备", description = "批量启用、禁用或删除设备")
    @PostMapping("/batch")
    @RequireResource(code = "device:batch", scope = "AREA")
    @SaCheckPermission("device:batch")
    public ResponseDTO<String> batchOperateDevices(@RequestBody @NotNull List<Long> deviceIds,
            @RequestParam @NotNull String operation) {
        return smartDeviceService.batchOperateDevices(deviceIds, operation);
    }

    @Operation(summary = "设备心跳检测", description = "设备心跳检测接口")
    @PostMapping("/{deviceId}/heartbeat")
    @RequireResource(code = "device:heartbeat", scope = "AREA")
    @SaCheckPermission("device:heartbeat")
    public ResponseDTO<String> deviceHeartbeat(@PathVariable @NotNull Long deviceId) {
        boolean success = smartDeviceService.deviceHeartbeat(deviceId);
        return success ? SmartResponseUtil.success("心跳成功") : SmartResponseUtil.error("心跳失败");
    }

    @Operation(summary = "更新设备状态", description = "更新设备运行状态")
    @PostMapping("/{deviceId}/status")
    @RequireResource(code = "device:status", scope = "AREA")
    @SaCheckPermission("device:status")
    public ResponseDTO<String> updateDeviceStatus(@PathVariable @NotNull Long deviceId,
            @RequestParam @NotNull String deviceStatus) {
        return smartDeviceService.updateDeviceStatus(deviceId, deviceStatus);
    }

    @Operation(summary = "远程控制设备", description = "远程控制智能设备")
    @PostMapping("/{deviceId}/control")
    @RequireResource(code = "device:control", scope = "AREA")
    @SaCheckPermission("device:control")
    @OperationLog(operationType = "CONTROL", operationDesc = "远程控制设备")
    public ResponseDTO<String> remoteControlDevice(@PathVariable @NotNull Long deviceId,
            @RequestParam @NotNull String command,
            @RequestBody(required = false) Map<String, Object> params) {
        return smartDeviceService.remoteControlDevice(deviceId, command, params);
    }

    @Operation(summary = "获取设备配置", description = "获取设备的配置信息")
    @GetMapping("/{deviceId}/config")
    @RequireResource(code = "device:config:get", scope = "AREA")
    @SaCheckPermission("device:config:get")
    public ResponseDTO<Map<String, Object>> getDeviceConfig(@PathVariable @NotNull Long deviceId) {
        return smartDeviceService.getDeviceConfig(deviceId);
    }

    @Operation(summary = "更新设备配置", description = "更新设备的配置信息")
    @PutMapping("/{deviceId}/config")
    @RequireResource(code = "device:config:update", scope = "AREA")
    @SaCheckPermission("device:config:update")
    public ResponseDTO<String> updateDeviceConfig(@PathVariable @NotNull Long deviceId,
            @RequestBody @NotNull Map<String, Object> config) {
        return smartDeviceService.updateDeviceConfig(deviceId, config);
    }

    @Operation(summary = "获取在线设备列表", description = "获取所有在线的设备列表")
    @GetMapping("/online")
    @RequireResource(code = "device:online", scope = "AREA")
    @SaCheckPermission("device:online")
    public ResponseDTO<List<SmartDeviceEntity>> getOnlineDevices() {
        List<SmartDeviceEntity> devices = smartDeviceService.getOnlineDevices();
        return SmartResponseUtil.success(devices);
    }

    @Operation(summary = "获取离线设备列表", description = "获取所有离线的设备列表")
    @GetMapping("/offline")
    @RequireResource(code = "device:offline", scope = "AREA")
    @SaCheckPermission("device:offline")
    public ResponseDTO<List<SmartDeviceEntity>> getOfflineDevices() {
        List<SmartDeviceEntity> devices = smartDeviceService.getOfflineDevices();
        return SmartResponseUtil.success(devices);
    }

    @Operation(summary = "获取设备状态统计", description = "获取设备状态分布统计信息")
    @GetMapping("/statistics/status")
    @RequireResource(code = "device:statistics:status", scope = "AREA")
    @SaCheckPermission("device:statistics:status")
    public ResponseDTO<Map<String, Object>> getDeviceStatusStatistics() {
        Map<String, Object> statistics = smartDeviceService.getDeviceStatusStatistics();
        return SmartResponseUtil.success(statistics);
    }

    @Operation(summary = "获取设备类型统计", description = "获取设备类型分布统计信息")
    @GetMapping("/statistics/type")
    @RequireResource(code = "device:statistics:type", scope = "AREA")
    @SaCheckPermission("device:statistics:type")
    public ResponseDTO<Map<String, Object>> getDeviceTypeStatistics() {
        Map<String, Object> statistics = smartDeviceService.getDeviceTypeStatistics();
        return SmartResponseUtil.success(statistics);
    }
}

