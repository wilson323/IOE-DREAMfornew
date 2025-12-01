package net.lab1024.sa.device.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.controller.SupportBaseController;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.common.util.SmartResponseUtil;
import net.lab1024.sa.device.domain.entity.SmartDeviceEntity;
import net.lab1024.sa.device.service.SmartDeviceService;

/**
 * 智能设备控制器 - 微服务版本
 * <p>
 * 严格遵循repowiki规范：
 * - 使用@Resource依赖注入
 * - 使用jakarta.*包名
 * - 完整的权限控制和参数验证
 * - RESTful API设计规范
 * - 统一响应格式ResponseDTO
 * - 详细的Swagger文档注解
 * - 完整的异常处理和日志记录
 *
 * @author IOE-DREAM Team
 * @date 2025-11-29
 */
@Slf4j
@RestController
@RequestMapping("/api/smart/device")
@Tag(name = "智能设备管理", description = "智能设备的增删改查和状态管理")
public class SmartDeviceController extends SupportBaseController {

    @Resource
    private SmartDeviceService smartDeviceService;

    @Operation(summary = "分页查询设备列表", description = "根据条件分页查询智能设备列表")
    @GetMapping("/page")
    public ResponseDTO<PageResult<SmartDeviceEntity>> queryDevicePage(
            @Valid PageParam pageParam,
            @Parameter(description = "设备类型") @RequestParam(required = false) String deviceType,
            @Parameter(description = "设备状态") @RequestParam(required = false) String deviceStatus,
            @Parameter(description = "设备名称") @RequestParam(required = false) String deviceName) {

        log.info("分页查询智能设备列表，deviceType: {}, deviceStatus: {}, deviceName: {}", deviceType, deviceStatus, deviceName);

        try {
            PageResult<SmartDeviceEntity> pageResult = smartDeviceService.queryDevicePage(pageParam, deviceType,
                    deviceStatus, deviceName);
            return SmartResponseUtil.success(pageResult);
        } catch (Exception e) {
            log.error("分页查询智能设备列表失败", e);
            return ResponseDTO.<PageResult<SmartDeviceEntity>>error("查询设备列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取设备详情", description = "根据设备ID获取设备详细信息")
    @GetMapping("/{deviceId}")
    public ResponseDTO<SmartDeviceEntity> getDeviceDetail(
            @Parameter(description = "设备ID", required = true) @PathVariable @NotNull Long deviceId) {

        log.info("获取智能设备详情，deviceId: {}", deviceId);

        try {
            return smartDeviceService.getDeviceDetail(deviceId);
        } catch (Exception e) {
            log.error("获取智能设备详情失败，deviceId: {}", deviceId, e);
            return ResponseDTO.<SmartDeviceEntity>error("获取设备详情失败: " + e.getMessage());
        }
    }

    @Operation(summary = "注册新设备", description = "新增智能设备")
    @PostMapping
    public ResponseDTO<String> registerDevice(
            @Parameter(description = "设备信息", required = true) @RequestBody @Valid SmartDeviceEntity deviceEntity) {

        log.info("注册新智能设备，deviceCode: {}, deviceName: {}", deviceEntity.getDeviceCode(), deviceEntity.getDeviceName());

        try {
            return smartDeviceService.registerDevice(deviceEntity);
        } catch (Exception e) {
            log.error("注册智能设备失败，deviceEntity: {}", deviceEntity, e);
            return ResponseDTO.<String>error("注册设备失败: " + e.getMessage());
        }
    }

    @Operation(summary = "更新设备信息", description = "更新智能设备的基础信息")
    @PutMapping
    public ResponseDTO<String> updateDevice(
            @Parameter(description = "设备信息", required = true) @RequestBody @Valid SmartDeviceEntity deviceEntity) {

        log.info("更新智能设备信息，deviceId: {}, deviceName: {}", deviceEntity.getDeviceId(), deviceEntity.getDeviceName());

        try {
            return smartDeviceService.updateDevice(deviceEntity);
        } catch (Exception e) {
            log.error("更新智能设备信息失败，deviceEntity: {}", deviceEntity, e);
            return ResponseDTO.<String>error("更新设备信息失败: " + e.getMessage());
        }
    }

    @Operation(summary = "删除设备", description = "删除指定的智能设备")
    @DeleteMapping("/{deviceId}")
    public ResponseDTO<String> deleteDevice(
            @Parameter(description = "设备ID", required = true) @PathVariable @NotNull Long deviceId) {

        log.info("删除智能设备，deviceId: {}", deviceId);

        try {
            return smartDeviceService.deleteDevice(deviceId);
        } catch (Exception e) {
            log.error("删除智能设备失败，deviceId: {}", deviceId, e);
            return ResponseDTO.<String>error("删除设备失败: " + e.getMessage());
        }
    }

    @Operation(summary = "启用设备", description = "启用指定设备")
    @PostMapping("/{deviceId}/enable")
    public ResponseDTO<String> enableDevice(
            @Parameter(description = "设备ID", required = true) @PathVariable @NotNull Long deviceId) {

        log.info("启用智能设备，deviceId: {}", deviceId);

        try {
            return smartDeviceService.enableDevice(deviceId);
        } catch (Exception e) {
            log.error("启用智能设备失败，deviceId: {}", deviceId, e);
            return ResponseDTO.<String>error("启用设备失败: " + e.getMessage());
        }
    }

    @Operation(summary = "禁用设备", description = "禁用指定设备")
    @PostMapping("/{deviceId}/disable")
    public ResponseDTO<String> disableDevice(
            @Parameter(description = "设备ID", required = true) @PathVariable @NotNull Long deviceId) {

        log.info("禁用智能设备，deviceId: {}", deviceId);

        try {
            return smartDeviceService.disableDevice(deviceId);
        } catch (Exception e) {
            log.error("禁用智能设备失败，deviceId: {}", deviceId, e);
            return ResponseDTO.<String>error("禁用设备失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量操作设备", description = "批量启用、禁用或删除设备")
    @PostMapping("/batch")
    public ResponseDTO<String> batchOperateDevices(
            @Parameter(description = "设备ID列表", required = true) @RequestBody @NotNull List<Long> deviceIds,
            @Parameter(description = "操作类型", required = true) @RequestParam @NotNull String operation) {

        log.info("批量操作智能设备，deviceIds: {}, operation: {}", deviceIds, operation);

        try {
            return smartDeviceService.batchOperateDevices(deviceIds, operation);
        } catch (Exception e) {
            log.error("批量操作智能设备失败，deviceIds: {}, operation: {}", deviceIds, operation, e);
            return ResponseDTO.<String>error("批量操作设备失败: " + e.getMessage());
        }
    }

    @Operation(summary = "设备心跳检测", description = "设备心跳检测接口")
    @PostMapping("/{deviceId}/heartbeat")
    public ResponseDTO<String> deviceHeartbeat(
            @Parameter(description = "设备ID", required = true) @PathVariable @NotNull Long deviceId) {

        log.debug("智能设备心跳检测，deviceId: {}", deviceId);

        try {
            boolean success = smartDeviceService.deviceHeartbeat(deviceId);
            return success ? SmartResponseUtil.success("心跳成功") : ResponseDTO.<String>error("心跳失败");
        } catch (Exception e) {
            log.error("智能设备心跳检测失败，deviceId: {}", deviceId, e);
            return ResponseDTO.<String>error("心跳检测失败: " + e.getMessage());
        }
    }

    @Operation(summary = "更新设备状态", description = "更新设备运行状态")
    @PostMapping("/{deviceId}/status")
    public ResponseDTO<String> updateDeviceStatus(
            @Parameter(description = "设备ID", required = true) @PathVariable @NotNull Long deviceId,
            @Parameter(description = "设备状态", required = true) @RequestParam @NotNull String deviceStatus) {

        log.info("更新智能设备状态，deviceId: {}, deviceStatus: {}", deviceId, deviceStatus);

        try {
            return smartDeviceService.updateDeviceStatus(deviceId, deviceStatus);
        } catch (Exception e) {
            log.error("更新智能设备状态失败，deviceId: {}, deviceStatus: {}", deviceId, deviceStatus, e);
            return ResponseDTO.<String>error("更新设备状态失败: " + e.getMessage());
        }
    }

    @Operation(summary = "远程控制设备", description = "远程控制智能设备")
    @PostMapping("/{deviceId}/control")
    public ResponseDTO<String> remoteControlDevice(
            @Parameter(description = "设备ID", required = true) @PathVariable @NotNull Long deviceId,
            @Parameter(description = "控制命令", required = true) @RequestParam @NotNull String command,
            @Parameter(description = "命令参数") @RequestBody(required = false) Map<String, Object> params) {

        log.info("远程控制智能设备，deviceId: {}, command: {}", deviceId, command);

        try {
            return smartDeviceService.remoteControlDevice(deviceId, command, params);
        } catch (Exception e) {
            log.error("远程控制智能设备失败，deviceId: {}, command: {}", deviceId, command, e);
            return ResponseDTO.<String>error("远程控制设备失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取设备配置", description = "获取设备的配置信息")
    @GetMapping("/{deviceId}/config")
    public ResponseDTO<Map<String, Object>> getDeviceConfig(
            @Parameter(description = "设备ID", required = true) @PathVariable @NotNull Long deviceId) {

        log.debug("获取智能设备配置，deviceId: {}", deviceId);

        try {
            return smartDeviceService.getDeviceConfig(deviceId);
        } catch (Exception e) {
            log.error("获取智能设备配置失败，deviceId: {}", deviceId, e);
            return ResponseDTO.<Map<String, Object>>error("获取设备配置失败: " + e.getMessage());
        }
    }

    @Operation(summary = "更新设备配置", description = "更新设备的配置信息")
    @PutMapping("/{deviceId}/config")
    public ResponseDTO<String> updateDeviceConfig(
            @Parameter(description = "设备ID", required = true) @PathVariable @NotNull Long deviceId,
            @Parameter(description = "配置信息", required = true) @RequestBody @NotNull Map<String, Object> config) {

        log.info("更新智能设备配置，deviceId: {}", deviceId);

        try {
            return smartDeviceService.updateDeviceConfig(deviceId, config);
        } catch (Exception e) {
            log.error("更新智能设备配置失败，deviceId: {}", deviceId, e);
            return ResponseDTO.<String>error("更新设备配置失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取在线设备列表", description = "获取所有在线的设备列表")
    @GetMapping("/online")
    public ResponseDTO<List<SmartDeviceEntity>> getOnlineDevices() {
        log.debug("获取在线智能设备列表");

        try {
            List<SmartDeviceEntity> devices = smartDeviceService.getOnlineDevices();
            return SmartResponseUtil.success(devices);
        } catch (Exception e) {
            log.error("获取在线智能设备列表失败", e);
            return ResponseDTO.<List<SmartDeviceEntity>>error("获取在线设备列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取离线设备列表", description = "获取所有离线的设备列表")
    @GetMapping("/offline")
    public ResponseDTO<List<SmartDeviceEntity>> getOfflineDevices() {
        log.debug("获取离线智能设备列表");

        try {
            List<SmartDeviceEntity> devices = smartDeviceService.getOfflineDevices();
            return SmartResponseUtil.success(devices);
        } catch (Exception e) {
            log.error("获取离线智能设备列表失败", e);
            return ResponseDTO.<List<SmartDeviceEntity>>error("获取离线设备列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取设备状态统计", description = "获取设备状态分布统计信息")
    @GetMapping("/statistics/status")
    public ResponseDTO<Map<String, Object>> getDeviceStatusStatistics() {
        log.debug("获取智能设备状态统计");

        try {
            Map<String, Object> statistics = smartDeviceService.getDeviceStatusStatistics();
            return SmartResponseUtil.success(statistics);
        } catch (Exception e) {
            log.error("获取智能设备状态统计失败", e);
            return ResponseDTO.<Map<String, Object>>error("获取设备状态统计失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取设备类型统计", description = "获取设备类型分布统计信息")
    @GetMapping("/statistics/type")
    public ResponseDTO<Map<String, Object>> getDeviceTypeStatistics() {
        log.debug("获取智能设备类型统计");

        try {
            Map<String, Object> statistics = smartDeviceService.getDeviceTypeStatistics();
            return SmartResponseUtil.success(statistics);
        } catch (Exception e) {
            log.error("获取智能设备类型统计失败", e);
            return ResponseDTO.<Map<String, Object>>error("获取设备类型统计失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取完整设备统计", description = "获取设备的完整统计信息")
    @GetMapping("/statistics/full")
    public ResponseDTO<Map<String, Object>> getFullDeviceStatistics() {
        log.debug("获取智能设备完整统计");

        try {
            Map<String, Object> statistics = smartDeviceService.getFullDeviceStatistics();
            return SmartResponseUtil.success(statistics);
        } catch (Exception e) {
            log.error("获取智能设备完整统计失败", e);
            return ResponseDTO.<Map<String, Object>>error("获取设备完整统计失败: " + e.getMessage());
        }
    }

    @Operation(summary = "根据设备编码查询", description = "根据设备编码获取设备信息")
    @GetMapping("/code/{deviceCode}")
    public ResponseDTO<SmartDeviceEntity> getDeviceByCode(
            @Parameter(description = "设备编码", required = true) @PathVariable @NotNull String deviceCode) {

        log.debug("根据设备编码查询智能设备，deviceCode: {}", deviceCode);

        try {
            SmartDeviceEntity device = smartDeviceService.getDeviceByCode(deviceCode);
            if (device == null) {
                return ResponseDTO.<SmartDeviceEntity>error("设备不存在");
            }
            return SmartResponseUtil.success(device);
        } catch (Exception e) {
            log.error("根据设备编码查询智能设备失败，deviceCode: {}", deviceCode, e);
            return ResponseDTO.<SmartDeviceEntity>error("查询设备失败: " + e.getMessage());
        }
    }

    @Operation(summary = "根据设备类型查询", description = "根据设备类型获取设备列表")
    @GetMapping("/type/{deviceType}")
    public ResponseDTO<List<SmartDeviceEntity>> getDevicesByType(
            @Parameter(description = "设备类型", required = true) @PathVariable @NotNull String deviceType) {

        log.debug("根据设备类型查询智能设备，deviceType: {}", deviceType);

        try {
            List<SmartDeviceEntity> devices = smartDeviceService.getDevicesByType(deviceType);
            return SmartResponseUtil.success(devices);
        } catch (Exception e) {
            log.error("根据设备类型查询智能设备失败，deviceType: {}", deviceType, e);
            return ResponseDTO.<List<SmartDeviceEntity>>error("查询设备列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "根据分组查询", description = "根据分组ID获取设备列表")
    @GetMapping("/group/{groupId}")
    public ResponseDTO<List<SmartDeviceEntity>> getDevicesByGroup(
            @Parameter(description = "分组ID", required = true) @PathVariable @NotNull Long groupId) {

        log.debug("根据分组查询智能设备，groupId: {}", groupId);

        try {
            List<SmartDeviceEntity> devices = smartDeviceService.getDevicesByGroup(groupId);
            return SmartResponseUtil.success(devices);
        } catch (Exception e) {
            log.error("根据分组查询智能设备失败，groupId: {}", groupId, e);
            return ResponseDTO.<List<SmartDeviceEntity>>error("查询设备列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "检查设备编码", description = "检查设备编码是否已存在")
    @GetMapping("/check/{deviceCode}")
    public ResponseDTO<Boolean> checkDeviceCodeExists(
            @Parameter(description = "设备编码", required = true) @PathVariable @NotNull String deviceCode) {

        log.debug("检查智能设备编码是否存在，deviceCode: {}", deviceCode);

        try {
            boolean exists = smartDeviceService.checkDeviceCodeExists(deviceCode);
            return SmartResponseUtil.success(exists);
        } catch (Exception e) {
            log.error("检查智能设备编码是否存在失败，deviceCode: {}", deviceCode, e);
            return ResponseDTO.<Boolean>error("检查设备编码失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量同步状态", description = "批量同步设备状态")
    @PostMapping("/sync/status")
    public ResponseDTO<String> batchSyncDeviceStatus(
            @Parameter(description = "设备ID列表", required = true) @RequestBody @NotNull List<Long> deviceIds) {

        log.info("批量同步智能设备状态，deviceIds: {}", deviceIds);

        try {
            return smartDeviceService.batchSyncDeviceStatus(deviceIds);
        } catch (Exception e) {
            log.error("批量同步智能设备状态失败，deviceIds: {}", deviceIds, e);
            return ResponseDTO.<String>error("批量同步设备状态失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取维护设备", description = "获取需要维护的设备列表")
    @GetMapping("/maintenance")
    public ResponseDTO<List<SmartDeviceEntity>> getMaintenanceDevices() {
        log.debug("获取需要维护的智能设备列表");

        try {
            List<SmartDeviceEntity> devices = smartDeviceService.getMaintenanceDevices();
            return SmartResponseUtil.success(devices);
        } catch (Exception e) {
            log.error("获取需要维护的智能设备列表失败", e);
            return ResponseDTO.<List<SmartDeviceEntity>>error("获取维护设备列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取故障设备", description = "获取故障设备列表")
    @GetMapping("/fault")
    public ResponseDTO<List<SmartDeviceEntity>> getFaultDevices() {
        log.debug("获取故障智能设备列表");

        try {
            List<SmartDeviceEntity> devices = smartDeviceService.getFaultDevices();
            return SmartResponseUtil.success(devices);
        } catch (Exception e) {
            log.error("获取故障智能设备列表失败", e);
            return ResponseDTO.<List<SmartDeviceEntity>>error("获取故障设备列表失败: " + e.getMessage());
        }
    }
}
