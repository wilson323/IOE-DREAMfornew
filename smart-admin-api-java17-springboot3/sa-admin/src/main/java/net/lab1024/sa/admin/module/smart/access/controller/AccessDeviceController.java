package net.lab1024.sa.admin.module.smart.access.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.controller.SupportBaseController;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import cn.dev33.satoken.annotation.SaCheckPermission;
import net.lab1024.sa.base.module.support.rbac.RequireResource;
import net.lab1024.sa.admin.module.smart.access.domain.entity.AccessDeviceEntity;
import net.lab1024.sa.admin.module.smart.access.domain.form.AccessDeviceForm;
import net.lab1024.sa.admin.module.smart.access.service.AccessDeviceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 门禁设备管理控制器
 * <p>
 * 严格遵循repowiki规范：
 * - 使用@Resource依赖注入（一级规范）
 * - 使用jakarta.*包名（一级规范）
 * - 完整的权限控制@SaCheckPermission
 * - RESTful API设计规范
 * - 统一响应格式ResponseDTO
 * - 完整的参数验证@Valid
 * - 详细的Swagger文档注解
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Slf4j
@RestController
@RequestMapping("/api/smart/access/device")
@Tag(name = "门禁设备管理", description = "门禁设备的增删改查和远程控制")
public class AccessDeviceController extends SupportBaseController {

    @Resource
    private AccessDeviceService accessDeviceService;

    @Operation(summary = "分页查询设备列表", description = "支持多条件组合查询")
    @SaCheckPermission("smart:access:device:query")
    @RequireResource(code = "smart:access:device:query", scope = "AREA")
    @PostMapping("/page")
    public ResponseDTO<PageResult<AccessDeviceEntity>> getDevicePage(@Valid @RequestBody PageParam pageParam,
                                                                    @Parameter(description = "设备ID") @RequestParam(required = false) Long deviceId,
                                                                    @Parameter(description = "区域ID") @RequestParam(required = false) Long areaId,
                                                                    @Parameter(description = "设备名称") @RequestParam(required = false) String deviceName,
                                                                    @Parameter(description = "门禁设备类型") @RequestParam(required = false) Integer accessDeviceType,
                                                                    @Parameter(description = "在线状态") @RequestParam(required = false) Integer onlineStatus,
                                                                    @Parameter(description = "启用状态") @RequestParam(required = false) Integer enabled) {
        log.info("分页查询门禁设备，deviceId: {}, areaId: {}, deviceName: {}, accessDeviceType: {}, onlineStatus: {}, enabled: {}",
                deviceId, areaId, deviceName, accessDeviceType, onlineStatus, enabled);

        try {
            PageResult<AccessDeviceEntity> result = accessDeviceService.getDevicePage(pageParam, deviceId, areaId, deviceName, accessDeviceType, onlineStatus, enabled);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("分页查询门禁设备失败", e);
            return ResponseDTO.error("查询设备列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "根据ID获取设备详情", description = "获取指定设备的详细信息")
    @SaCheckPermission("smart:access:device:query")
    @GetMapping("/{accessDeviceId}")
    public ResponseDTO<AccessDeviceEntity> getDeviceById(@Parameter(description = "设备ID", required = true) @PathVariable Long accessDeviceId) {
        log.info("获取门禁设备详情，accessDeviceId: {}", accessDeviceId);

        try {
            AccessDeviceEntity device = accessDeviceService.getDeviceById(accessDeviceId);
            return ResponseDTO.ok(device);
        } catch (Exception e) {
            log.error("获取门禁设备详情失败，accessDeviceId: {}", accessDeviceId, e);
            return ResponseDTO.error("获取设备详情失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取区域下的设备列表", description = "获取指定区域内的所有设备")
    @SaCheckPermission("smart:access:device:query")
    @GetMapping("/area/{areaId}")
    public ResponseDTO<List<AccessDeviceEntity>> getDevicesByAreaId(@Parameter(description = "区域ID", required = true) @PathVariable Long areaId) {
        log.info("获取区域设备列表，areaId: {}", areaId);

        try {
            List<AccessDeviceEntity> devices = accessDeviceService.getDevicesByAreaId(areaId);
            return ResponseDTO.ok(devices);
        } catch (Exception e) {
            log.error("获取区域设备列表失败，areaId: {}", areaId, e);
            return ResponseDTO.error("获取区域设备列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取在线设备列表", description = "获取所有当前在线的设备")
    @SaCheckPermission("smart:access:device:query")
    @GetMapping("/online")
    public ResponseDTO<List<AccessDeviceEntity>> getOnlineDevices() {
        log.info("获取在线设备列表");

        try {
            List<AccessDeviceEntity> devices = accessDeviceService.getOnlineDevices();
            return ResponseDTO.ok(devices);
        } catch (Exception e) {
            log.error("获取在线设备列表失败", e);
            return ResponseDTO.error("获取在线设备列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取离线设备列表", description = "获取所有当前离线的设备")
    @SaCheckPermission("smart:access:device:query")
    @GetMapping("/offline")
    public ResponseDTO<List<AccessDeviceEntity>> getOfflineDevices() {
        log.info("获取离线设备列表");

        try {
            List<AccessDeviceEntity> devices = accessDeviceService.getOfflineDevices();
            return ResponseDTO.ok(devices);
        } catch (Exception e) {
            log.error("获取离线设备列表失败", e);
            return ResponseDTO.error("获取离线设备列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "创建门禁设备", description = "新增门禁设备信息")
    @SaCheckPermission("smart:access:device:add")
    @PostMapping("/add")
    public ResponseDTO<String> addDevice(@Valid @RequestBody AccessDeviceForm deviceForm) {
        log.info("创建门禁设备，deviceForm: {}", deviceForm);

        try {
            // 转换为Entity并设置基础信息
            AccessDeviceEntity device = new AccessDeviceEntity();
            net.lab1024.sa.base.common.util.SmartBeanUtil.copyProperties(deviceForm, device);

            accessDeviceService.addDevice(device);
            return ResponseDTO.ok("设备创建成功");
        } catch (Exception e) {
            log.error("创建门禁设备失败，deviceForm: {}", deviceForm, e);
            return ResponseDTO.error("创建设备失败: " + e.getMessage());
        }
    }

    @Operation(summary = "更新门禁设备", description = "更新门禁设备信息")
    @SaCheckPermission("smart:access:device:update")
    @PostMapping("/update")
    public ResponseDTO<String> updateDevice(@Valid @RequestBody AccessDeviceForm deviceForm) {
        log.info("更新门禁设备，deviceForm: {}", deviceForm);

        try {
            // 转换为Entity
            AccessDeviceEntity device = new AccessDeviceEntity();
            net.lab1024.sa.base.common.util.SmartBeanUtil.copyProperties(deviceForm, device);

            accessDeviceService.updateDevice(device);
            return ResponseDTO.ok("设备更新成功");
        } catch (Exception e) {
            log.error("更新门禁设备失败，deviceForm: {}", deviceForm, e);
            return ResponseDTO.error("更新设备失败: " + e.getMessage());
        }
    }

    @Operation(summary = "删除门禁设备", description = "根据ID删除门禁设备")
    @SaCheckPermission("smart:access:device:delete")
    @PostMapping("/delete/{accessDeviceId}")
    public ResponseDTO<String> deleteDevice(@Parameter(description = "设备ID", required = true) @PathVariable Long accessDeviceId) {
        log.info("删除门禁设备，accessDeviceId: {}", accessDeviceId);

        try {
            accessDeviceService.deleteDevice(accessDeviceId);
            return ResponseDTO.ok("设备删除成功");
        } catch (Exception e) {
            log.error("删除门禁设备失败，accessDeviceId: {}", accessDeviceId, e);
            return ResponseDTO.error("删除设备失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量删除门禁设备", description = "根据ID列表批量删除设备")
    @SaCheckPermission("smart:access:device:delete")
    @PostMapping("/batchDelete")
    public ResponseDTO<String> batchDeleteDevices(@Parameter(description = "设备ID列表", required = true) @RequestBody List<Long> accessDeviceIds) {
        log.info("批量删除门禁设备，accessDeviceIds: {}", accessDeviceIds);

        try {
            accessDeviceService.batchDeleteDevices(accessDeviceIds);
            return ResponseDTO.ok("批量删除成功");
        } catch (Exception e) {
            log.error("批量删除门禁设备失败，accessDeviceIds: {}", accessDeviceIds, e);
            return ResponseDTO.error("批量删除失败: " + e.getMessage());
        }
    }

    @Operation(summary = "更新设备在线状态", description = "更新设备的在线状态")
    @SaCheckPermission("smart:access:device:update")
    @PostMapping("/updateOnlineStatus/{accessDeviceId}/{onlineStatus}")
    public ResponseDTO<String> updateDeviceOnlineStatus(@Parameter(description = "设备ID", required = true) @PathVariable Long accessDeviceId,
                                                        @Parameter(description = "在线状态 0:离线 1:在线", required = true) @PathVariable Integer onlineStatus) {
        log.info("更新设备在线状态，accessDeviceId: {}, onlineStatus: {}", accessDeviceId, onlineStatus);

        try {
            accessDeviceService.updateDeviceOnlineStatus(accessDeviceId, onlineStatus);
            return ResponseDTO.ok("设备状态更新成功");
        } catch (Exception e) {
            log.error("更新设备在线状态失败，accessDeviceId: {}, onlineStatus: {}", accessDeviceId, onlineStatus, e);
            return ResponseDTO.error("更新设备状态失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量更新设备在线状态", description = "批量更新设备的在线状态")
    @SaCheckPermission("smart:access:device:update")
    @PostMapping("/batchUpdateOnlineStatus")
    public ResponseDTO<String> batchUpdateDeviceOnlineStatus(@Parameter(description = "设备ID列表", required = true) @RequestBody List<Long> accessDeviceIds,
                                                            @Parameter(description = "在线状态 0:离线 1:在线", required = true) @RequestParam Integer onlineStatus) {
        log.info("批量更新设备在线状态，accessDeviceIds: {}, onlineStatus: {}", accessDeviceIds, onlineStatus);

        try {
            accessDeviceService.batchUpdateDeviceOnlineStatus(accessDeviceIds, onlineStatus);
            return ResponseDTO.ok("批量更新设备状态成功");
        } catch (Exception e) {
            log.error("批量更新设备在线状态失败，accessDeviceIds: {}, onlineStatus: {}", accessDeviceIds, onlineStatus, e);
            return ResponseDTO.error("批量更新设备状态失败: " + e.getMessage());
        }
    }

    @Operation(summary = "远程开门", description = "远程控制设备开门")
    @SaCheckPermission("smart:access:device:control")
    @RequireResource(code = "smart:access:device:control", scope = "AREA")
    @PostMapping("/remoteOpen/{accessDeviceId}")
    public ResponseDTO<String> remoteOpenDoor(@Parameter(description = "设备ID", required = true) @PathVariable Long accessDeviceId) {
        log.info("远程开门，accessDeviceId: {}", accessDeviceId);

        try {
            boolean result = accessDeviceService.remoteOpenDoor(accessDeviceId);
            if (result) {
                return ResponseDTO.ok("远程开门成功");
            } else {
                return ResponseDTO.error("远程开门失败");
            }
        } catch (Exception e) {
            log.error("远程开门失败，accessDeviceId: {}", accessDeviceId, e);
            return ResponseDTO.error("远程开门失败: " + e.getMessage());
        }
    }

    @Operation(summary = "重启设备", description = "远程重启门禁设备")
    @SaCheckPermission("smart:access:device:control")
    @PostMapping("/restart/{accessDeviceId}")
    public ResponseDTO<String> restartDevice(@Parameter(description = "设备ID", required = true) @PathVariable Long accessDeviceId) {
        log.info("重启设备，accessDeviceId: {}", accessDeviceId);

        try {
            boolean result = accessDeviceService.restartDevice(accessDeviceId);
            if (result) {
                return ResponseDTO.ok("设备重启成功");
            } else {
                return ResponseDTO.error("设备重启失败");
            }
        } catch (Exception e) {
            log.error("重启设备失败，accessDeviceId: {}", accessDeviceId, e);
            return ResponseDTO.error("设备重启失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取设备统计信息", description = "获取设备相关的统计数据")
    @SaCheckPermission("smart:access:device:query")
    @GetMapping("/statistics")
    public ResponseDTO<Map<String, Object>> getDeviceStatistics() {
        log.info("获取设备统计信息");

        try {
            Map<String, Object> statistics = accessDeviceService.getDeviceStatistics();
            return ResponseDTO.ok(statistics);
        } catch (Exception e) {
            log.error("获取设备统计信息失败", e);
            return ResponseDTO.error("获取设备统计信息失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取设备健康状态", description = "获取指定设备的健康状态信息")
    @SaCheckPermission("smart:access:device:query")
    @GetMapping("/health/{accessDeviceId}")
    public ResponseDTO<Map<String, Object>> getDeviceHealthStatus(@Parameter(description = "设备ID", required = true) @PathVariable Long accessDeviceId) {
        log.info("获取设备健康状态，accessDeviceId: {}", accessDeviceId);

        try {
            Map<String, Object> healthStatus = accessDeviceService.getDeviceHealthStatus(accessDeviceId);
            return ResponseDTO.ok(healthStatus);
        } catch (Exception e) {
            log.error("获取设备健康状态失败，accessDeviceId: {}", accessDeviceId, e);
            return ResponseDTO.error("获取设备健康状态失败: " + e.getMessage());
        }
    }

    @Operation(summary = "设备心跳上报", description = "设备向服务器上报心跳信息")
    @PostMapping("/heartbeat/{accessDeviceId}")
    public ResponseDTO<String> deviceHeartbeat(@Parameter(description = "设备ID", required = true) @PathVariable Long accessDeviceId,
                                              @Parameter(description = "心跳数据", required = true) @RequestBody Map<String, Object> heartbeatData) {
        log.info("设备心跳上报，accessDeviceId: {}, heartbeatData: {}", accessDeviceId, heartbeatData);

        try {
            accessDeviceService.deviceHeartbeat(accessDeviceId, heartbeatData);
            return ResponseDTO.ok("心跳上报成功");
        } catch (Exception e) {
            log.error("设备心跳上报失败，accessDeviceId: {}", accessDeviceId, e);
            return ResponseDTO.error("心跳上报失败: " + e.getMessage());
        }
    }

    @Operation(summary = "同步设备时间", description = "同步服务器时间到门禁设备")
    @SaCheckPermission("smart:access:device:control")
    @PostMapping("/syncTime/{accessDeviceId}")
    public ResponseDTO<String> syncDeviceTime(@Parameter(description = "设备ID", required = true) @PathVariable Long accessDeviceId) {
        log.info("同步设备时间，accessDeviceId: {}", accessDeviceId);

        try {
            boolean result = accessDeviceService.syncDeviceTime(accessDeviceId);
            if (result) {
                return ResponseDTO.ok("设备时间同步成功");
            } else {
                return ResponseDTO.error("设备时间同步失败");
            }
        } catch (Exception e) {
            log.error("同步设备时间失败，accessDeviceId: {}", accessDeviceId, e);
            return ResponseDTO.error("设备时间同步失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取需要维护的设备", description = "获取所有需要维护的设备列表")
    @SaCheckPermission("smart:access:device:query")
    @GetMapping("/maintenance")
    public ResponseDTO<List<AccessDeviceEntity>> getDevicesNeedingMaintenance() {
        log.info("获取需要维护的设备列表");

        try {
            List<AccessDeviceEntity> devices = accessDeviceService.getDevicesNeedingMaintenance();
            return ResponseDTO.ok(devices);
        } catch (Exception e) {
            log.error("获取需要维护的设备列表失败", e);
            return ResponseDTO.error("获取需要维护的设备列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取心跳超时设备", description = "获取心跳超时的设备列表")
    @SaCheckPermission("smart:access:device:query")
    @GetMapping("/heartbeatTimeout")
    public ResponseDTO<List<AccessDeviceEntity>> getHeartbeatTimeoutDevices() {
        log.info("获取心跳超时设备列表");

        try {
            List<AccessDeviceEntity> devices = accessDeviceService.getHeartbeatTimeoutDevices();
            return ResponseDTO.ok(devices);
        } catch (Exception e) {
            log.error("获取心跳超时设备列表失败", e);
            return ResponseDTO.error("获取心跳超时设备列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "更新设备工作模式", description = "更新设备的工作模式")
    @SaCheckPermission("smart:access:device:update")
    @PostMapping("/updateWorkMode/{accessDeviceId}/{workMode}")
    public ResponseDTO<String> updateDeviceWorkMode(@Parameter(description = "设备ID", required = true) @PathVariable Long accessDeviceId,
                                                    @Parameter(description = "工作模式 1:正常模式 2:维护模式 3:紧急模式 4:锁闭模式", required = true) @PathVariable Integer workMode) {
        log.info("更新设备工作模式，accessDeviceId: {}, workMode: {}", accessDeviceId, workMode);

        try {
            accessDeviceService.updateDeviceWorkMode(accessDeviceId, workMode);
            return ResponseDTO.ok("设备工作模式更新成功");
        } catch (Exception e) {
            log.error("更新设备工作模式失败，accessDeviceId: {}, workMode: {}", accessDeviceId, workMode, e);
            return ResponseDTO.error("更新设备工作模式失败: " + e.getMessage());
        }
    }
}