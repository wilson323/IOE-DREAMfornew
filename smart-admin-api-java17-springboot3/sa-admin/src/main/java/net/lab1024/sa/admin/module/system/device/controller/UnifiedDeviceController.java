package net.lab1024.sa.admin.module.system.device.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.system.device.domain.entity.UnifiedDeviceEntity;
import net.lab1024.sa.admin.module.system.device.domain.form.UnifiedDeviceAddForm;
import net.lab1024.sa.admin.module.system.device.domain.form.UnifiedDeviceQueryForm;
import net.lab1024.sa.admin.module.system.device.domain.form.UnifiedDeviceUpdateForm;
import net.lab1024.sa.admin.module.system.device.domain.vo.UnifiedDeviceVO;
import net.lab1024.sa.admin.module.system.device.service.UnifiedDeviceService;
import net.lab1024.sa.base.common.annotation.SaCheckLogin;
import net.lab1024.sa.base.common.annotation.SaCheckPermission;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import net.lab1024.sa.base.common.util.SmartResponseUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 统一设备管理控制器
 * <p>
 * 严格遵循repowiki四层架构规范：
 * - Controller层负责参数验证和调用Service层
 * - 禁止在Controller层编写业务逻辑
 * - 统一异常处理和响应格式
 * - 完整的权限控制
 * - RESTful API设计
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Slf4j
@RestController
@Tag(name = "统一设备管理", description = "统一设备管理接口")
@RequestMapping("/api/unified/device")
@SaCheckLogin
public class UnifiedDeviceController {

    @Resource
    private UnifiedDeviceService unifiedDeviceService;

    // ========== 基础CRUD操作 ==========

    @Operation(summary = "分页查询设备列表")
    @SaCheckPermission("unified:device:query")
    @GetMapping("/page")
    public ResponseDTO<PageResult<UnifiedDeviceVO>> queryDevicePage(
            @Parameter(description = "分页参数") PageParam pageParam,
            @Parameter(description = "设备类型") @RequestParam(required = false) String deviceType,
            @Parameter(description = "设备状态") @RequestParam(required = false) String deviceStatus,
            @Parameter(description = "设备名称") @RequestParam(required = false) String deviceName,
            @Parameter(description = "区域ID") @RequestParam(required = false) Long areaId) {

        try {
            log.info("分页查询设备列表，设备类型：{}，设备状态：{}，设备名称：{}，区域ID：{}",
                    deviceType, deviceStatus, deviceName, areaId);

            PageResult<UnifiedDeviceEntity> pageResult = unifiedDeviceService.queryDevicePage(
                    pageParam, deviceType, deviceStatus, deviceName, areaId);

            // 转换为VO对象
            PageResult<UnifiedDeviceVO> voPageResult = SmartPageUtil.convert2PageVO(pageResult, UnifiedDeviceVO.class);

            log.info("分页查询设备列表成功，总记录数：{}", voPageResult.getTotal());
            return SmartResponseUtil.ok(voPageResult);

        } catch (Exception e) {
            log.error("分页查询设备列表失败", e);
            return SmartResponseUtil.error("查询设备列表失败：" + e.getMessage());
        }
    }

    @Operation(summary = "根据ID查询设备详情")
    @SaCheckPermission("unified:device:query")
    @GetMapping("/{deviceId}")
    public ResponseDTO<UnifiedDeviceVO> getDeviceDetail(
            @Parameter(description = "设备ID") @PathVariable Long deviceId) {

        try {
            log.info("查询设备详情，设备ID：{}", deviceId);

            ResponseDTO<UnifiedDeviceEntity> result = unifiedDeviceService.getDeviceDetail(deviceId);
            if (!result.getOk()) {
                return SmartResponseUtil.error(result.getMsg());
            }

            UnifiedDeviceVO vo = SmartPageUtil.convert2VO(result.getData(), UnifiedDeviceVO.class);

            log.info("查询设备详情成功，设备ID：{}", deviceId);
            return SmartResponseUtil.ok(vo);

        } catch (Exception e) {
            log.error("查询设备详情失败，设备ID：{}", deviceId, e);
            return SmartResponseUtil.error("查询设备详情失败：" + e.getMessage());
        }
    }

    @Operation(summary = "新增设备")
    @SaCheckPermission("unified:device:add")
    @PostMapping("/add")
    public ResponseDTO<String> addDevice(@Valid @RequestBody UnifiedDeviceAddForm addForm) {
        try {
            log.info("新增设备，设备编号：{}，设备名称：{}", addForm.getDeviceCode(), addForm.getDeviceName());

            // 转换Form到Entity
            UnifiedDeviceEntity deviceEntity = SmartPageUtil.convert2Entity(addForm, UnifiedDeviceEntity.class);

            ResponseDTO<String> result = unifiedDeviceService.registerDevice(deviceEntity);
            if (!result.getOk()) {
                return SmartResponseUtil.error(result.getMsg());
            }

            log.info("新增设备成功，设备编号：{}", addForm.getDeviceCode());
            return SmartResponseUtil.ok(result.getData());

        } catch (Exception e) {
            log.error("新增设备失败", e);
            return SmartResponseUtil.error("新增设备失败：" + e.getMessage());
        }
    }

    @Operation(summary = "更新设备")
    @SaCheckPermission("unified:device:update")
    @PutMapping("/update")
    public ResponseDTO<String> updateDevice(@Valid @RequestBody UnifiedDeviceUpdateForm updateForm) {
        try {
            log.info("更新设备，设备ID：{}", updateForm.getDeviceId());

            // 转换Form到Entity
            UnifiedDeviceEntity deviceEntity = SmartPageUtil.convert2Entity(updateForm, UnifiedDeviceEntity.class);

            ResponseDTO<String> result = unifiedDeviceService.updateDevice(deviceEntity);
            if (!result.getOk()) {
                return SmartResponseUtil.error(result.getMsg());
            }

            log.info("更新设备成功，设备ID：{}", updateForm.getDeviceId());
            return SmartResponseUtil.ok(result.getData());

        } catch (Exception e) {
            log.error("更新设备失败，设备ID：{}", updateForm.getDeviceId(), e);
            return SmartResponseUtil.error("更新设备失败：" + e.getMessage());
        }
    }

    @Operation(summary = "删除设备")
    @SaCheckPermission("unified:device:delete")
    @DeleteMapping("/{deviceId}")
    public ResponseDTO<String> deleteDevice(@Parameter(description = "设备ID") @PathVariable Long deviceId) {
        try {
            log.info("删除设备，设备ID：{}", deviceId);

            ResponseDTO<String> result = unifiedDeviceService.deleteDevice(deviceId);
            if (!result.getOk()) {
                return SmartResponseUtil.error(result.getMsg());
            }

            log.info("删除设备成功，设备ID：{}", deviceId);
            return SmartResponseUtil.ok(result.getData());

        } catch (Exception e) {
            log.error("删除设备失败，设备ID：{}", deviceId, e);
            return SmartResponseUtil.error("删除设备失败：" + e.getMessage());
        }
    }

    // ========== 设备状态管理 ==========

    @Operation(summary = "启用设备")
    @SaCheckPermission("unified:device:enable")
    @PutMapping("/{deviceId}/enable")
    public ResponseDTO<String> enableDevice(@Parameter(description = "设备ID") @PathVariable Long deviceId) {
        try {
            log.info("启用设备，设备ID：{}", deviceId);

            ResponseDTO<String> result = unifiedDeviceService.enableDevice(deviceId);
            if (!result.getOk()) {
                return SmartResponseUtil.error(result.getMsg());
            }

            log.info("启用设备成功，设备ID：{}", deviceId);
            return SmartResponseUtil.ok(result.getData());

        } catch (Exception e) {
            log.error("启用设备失败，设备ID：{}", deviceId, e);
            return SmartResponseUtil.error("启用设备失败：" + e.getMessage());
        }
    }

    @Operation(summary = "禁用设备")
    @SaCheckPermission("unified:device:disable")
    @PutMapping("/{deviceId}/disable")
    public ResponseDTO<String> disableDevice(@Parameter(description = "设备ID") @PathVariable Long deviceId) {
        try {
            log.info("禁用设备，设备ID：{}", deviceId);

            ResponseDTO<String> result = unifiedDeviceService.disableDevice(deviceId);
            if (!result.getOk()) {
                return SmartResponseUtil.error(result.getMsg());
            }

            log.info("禁用设备成功，设备ID：{}", deviceId);
            return SmartResponseUtil.ok(result.getData());

        } catch (Exception e) {
            log.error("禁用设备失败，设备ID：{}", deviceId, e);
            return SmartResponseUtil.error("禁用设备失败：" + e.getMessage());
        }
    }

    @Operation(summary = "更新设备状态")
    @SaCheckPermission("unified:device:status:update")
    @PutMapping("/{deviceId}/status")
    public ResponseDTO<String> updateDeviceStatus(
            @Parameter(description = "设备ID") @PathVariable Long deviceId,
            @Parameter(description = "设备状态") @RequestParam String deviceStatus) {

        try {
            log.info("更新设备状态，设备ID：{}，新状态：{}", deviceId, deviceStatus);

            ResponseDTO<String> result = unifiedDeviceService.updateDeviceStatus(deviceId, deviceStatus);
            if (!result.getOk()) {
                return SmartResponseUtil.error(result.getMsg());
            }

            log.info("更新设备状态成功，设备ID：{}", deviceId);
            return SmartResponseUtil.ok(result.getData());

        } catch (Exception e) {
            log.error("更新设备状态失败，设备ID：{}", deviceId, e);
            return SmartResponseUtil.error("更新设备状态失败：" + e.getMessage());
        }
    }

    // ========== 设备心跳 ==========

    @Operation(summary = "设备心跳上报")
    @PostMapping("/{deviceId}/heartbeat")
    public ResponseDTO<String> deviceHeartbeat(@Parameter(description = "设备ID") @PathVariable Long deviceId) {
        try {
            log.debug("设备心跳上报，设备ID：{}", deviceId);

            boolean success = unifiedDeviceService.deviceHeartbeat(deviceId);
            if (success) {
                return SmartResponseUtil.ok("心跳上报成功");
            } else {
                return SmartResponseUtil.error("心跳上报失败");
            }

        } catch (Exception e) {
            log.error("设备心跳上报失败，设备ID：{}", deviceId, e);
            return SmartResponseUtil.error("心跳上报失败：" + e.getMessage());
        }
    }

    // ========== 设备控制 ==========

    @Operation(summary = "远程控制设备")
    @SaCheckPermission("unified:device:control")
    @PostMapping("/{deviceId}/control")
    public ResponseDTO<String> remoteControlDevice(
            @Parameter(description = "设备ID") @PathVariable Long deviceId,
            @Parameter(description = "控制命令") @RequestParam String command,
            @Parameter(description = "控制参数") @RequestBody(required = false) Map<String, Object> params) {

        try {
            log.info("远程控制设备，设备ID：{}，命令：{}", deviceId, command);

            ResponseDTO<String> result = unifiedDeviceService.remoteControlDevice(deviceId, command, params);
            if (!result.getOk()) {
                return SmartResponseUtil.error(result.getMsg());
            }

            log.info("远程控制设备成功，设备ID：{}", deviceId);
            return SmartResponseUtil.ok(result.getData());

        } catch (Exception e) {
            log.error("远程控制设备失败，设备ID：{}，命令：{}", deviceId, command, e);
            return SmartResponseUtil.error("设备控制失败：" + e.getMessage());
        }
    }

    // ========== 门禁设备专用接口 ==========

    @Operation(summary = "远程开门")
    @SaCheckPermission("unified:device:access:open")
    @PostMapping("/{deviceId}/remote-open-door")
    public ResponseDTO<String> remoteOpenDoor(@Parameter(description = "设备ID") @PathVariable Long deviceId) {
        try {
            log.info("远程开门，设备ID：{}", deviceId);

            ResponseDTO<String> result = unifiedDeviceService.remoteOpenDoor(deviceId);
            if (!result.getOk()) {
                return SmartResponseUtil.error(result.getMsg());
            }

            log.info("远程开门成功，设备ID：{}", deviceId);
            return SmartResponseUtil.ok(result.getData());

        } catch (Exception e) {
            log.error("远程开门失败，设备ID：{}", deviceId, e);
            return SmartResponseUtil.error("远程开门失败：" + e.getMessage());
        }
    }

    // ========== 视频设备专用接口 ==========

    @Operation(summary = "云台控制")
    @SaCheckPermission("unified:device:video:ptz")
    @PostMapping("/{deviceId}/ptz-control")
    public ResponseDTO<String> ptzControl(
            @Parameter(description = "设备ID") @PathVariable Long deviceId,
            @Parameter(description = "云台命令") @RequestParam String command,
            @Parameter(description = "控制速度") @RequestParam(required = false, defaultValue = "5") Integer speed) {

        try {
            log.info("云台控制，设备ID：{}，命令：{}，速度：{}", deviceId, command, speed);

            ResponseDTO<String> result = unifiedDeviceService.ptzControl(deviceId, command, speed);
            if (!result.getOk()) {
                return SmartResponseUtil.error(result.getMsg());
            }

            log.info("云台控制成功，设备ID：{}", deviceId);
            return SmartResponseUtil.ok(result.getData());

        } catch (Exception e) {
            log.error("云台控制失败，设备ID：{}，命令：{}", deviceId, command, e);
            return SmartResponseUtil.error("云台控制失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取实时视频流")
    @SaCheckPermission("unified:device:video:stream")
    @GetMapping("/{deviceId}/live-stream")
    public ResponseDTO<String> getLiveStream(@Parameter(description = "设备ID") @PathVariable Long deviceId) {
        try {
            log.info("获取实时视频流，设备ID：{}", deviceId);

            ResponseDTO<String> result = unifiedDeviceService.getLiveStream(deviceId);
            if (!result.getOk()) {
                return SmartResponseUtil.error(result.getMsg());
            }

            log.info("获取实时视频流成功，设备ID：{}", deviceId);
            return SmartResponseUtil.ok(result.getData());

        } catch (Exception e) {
            log.error("获取实时视频流失败，设备ID：{}", deviceId, e);
            return SmartResponseUtil.error("获取视频流失败：" + e.getMessage());
        }
    }

    // ========== 配置管理 ==========

    @Operation(summary = "获取设备配置")
    @SaCheckPermission("unified:device:config:query")
    @GetMapping("/{deviceId}/config")
    public ResponseDTO<Map<String, Object>> getDeviceConfig(@Parameter(description = "设备ID") @PathVariable Long deviceId) {
        try {
            log.info("获取设备配置，设备ID：{}", deviceId);

            ResponseDTO<Map<String, Object>> result = unifiedDeviceService.getDeviceConfig(deviceId);
            if (!result.getOk()) {
                return SmartResponseUtil.error(result.getMsg());
            }

            log.info("获取设备配置成功，设备ID：{}", deviceId);
            return SmartResponseUtil.ok(result.getData());

        } catch (Exception e) {
            log.error("获取设备配置失败，设备ID：{}", deviceId, e);
            return SmartResponseUtil.error("获取设备配置失败：" + e.getMessage());
        }
    }

    @Operation(summary = "更新设备配置")
    @SaCheckPermission("unified:device:config:update")
    @PutMapping("/{deviceId}/config")
    public ResponseDTO<String> updateDeviceConfig(
            @Parameter(description = "设备ID") @PathVariable Long deviceId,
            @Parameter(description = "设备配置") @RequestBody Map<String, Object> config) {

        try {
            log.info("更新设备配置，设备ID：{}", deviceId);

            ResponseDTO<String> result = unifiedDeviceService.updateDeviceConfig(deviceId, config);
            if (!result.getOk()) {
                return SmartResponseUtil.error(result.getMsg());
            }

            log.info("更新设备配置成功，设备ID：{}", deviceId);
            return SmartResponseUtil.ok(result.getData());

        } catch (Exception e) {
            log.error("更新设备配置失败，设备ID：{}", deviceId, e);
            return SmartResponseUtil.error("更新设备配置失败：" + e.getMessage());
        }
    }

    // ========== 统计信息 ==========

    @Operation(summary = "获取设备状态统计")
    @SaCheckPermission("unified:device:statistics")
    @GetMapping("/statistics/status")
    public ResponseDTO<Map<String, Object>> getDeviceStatusStatistics(
            @Parameter(description = "设备类型") @RequestParam(required = false) String deviceType) {

        try {
            log.info("获取设备状态统计，设备类型：{}", deviceType);

            Map<String, Object> statistics = unifiedDeviceService.getDeviceStatusStatistics(deviceType);

            log.info("获取设备状态统计成功");
            return SmartResponseUtil.ok(statistics);

        } catch (Exception e) {
            log.error("获取设备状态统计失败", e);
            return SmartResponseUtil.error("获取设备状态统计失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取设备类型统计")
    @SaCheckPermission("unified:device:statistics")
    @GetMapping("/statistics/type")
    public ResponseDTO<Map<String, Object>> getDeviceTypeStatistics() {
        try {
            log.info("获取设备类型统计");

            Map<String, Object> statistics = unifiedDeviceService.getDeviceTypeStatistics();

            log.info("获取设备类型统计成功");
            return SmartResponseUtil.ok(statistics);

        } catch (Exception e) {
            log.error("获取设备类型统计失败", e);
            return SmartResponseUtil.error("获取设备类型统计失败：" + e.getMessage());
        }
    }

    // ========== 设备列表查询 ==========

    @Operation(summary = "获取在线设备列表")
    @SaCheckPermission("unified:device:list")
    @GetMapping("/online")
    public ResponseDTO<List<UnifiedDeviceVO>> getOnlineDevices(
            @Parameter(description = "设备类型") @RequestParam(required = false) String deviceType) {

        try {
            log.info("获取在线设备列表，设备类型：{}", deviceType);

            List<UnifiedDeviceEntity> devices = unifiedDeviceService.getOnlineDevices(deviceType);
            List<UnifiedDeviceVO> voList = SmartPageUtil.convert2ListVO(devices, UnifiedDeviceVO.class);

            log.info("获取在线设备列表成功，数量：{}", voList.size());
            return SmartResponseUtil.ok(voList);

        } catch (Exception e) {
            log.error("获取在线设备列表失败", e);
            return SmartResponseUtil.error("获取在线设备列表失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取离线设备列表")
    @SaCheckPermission("unified:device:list")
    @GetMapping("/offline")
    public ResponseDTO<List<UnifiedDeviceVO>> getOfflineDevices(
            @Parameter(description = "设备类型") @RequestParam(required = false) String deviceType) {

        try {
            log.info("获取离线设备列表，设备类型：{}", deviceType);

            List<UnifiedDeviceEntity> devices = unifiedDeviceService.getOfflineDevices(deviceType);
            List<UnifiedDeviceVO> voList = SmartPageUtil.convert2ListVO(devices, UnifiedDeviceVO.class);

            log.info("获取离线设备列表成功，数量：{}", voList.size());
            return SmartResponseUtil.ok(voList);

        } catch (Exception e) {
            log.error("获取离线设备列表失败", e);
            return SmartResponseUtil.error("获取离线设备列表失败：" + e.getMessage());
        }
    }

    // ========== 高级功能 ==========

    @Operation(summary = "获取设备健康状态")
    @SaCheckPermission("unified:device:health")
    @GetMapping("/{deviceId}/health")
    public ResponseDTO<Map<String, Object>> getDeviceHealthStatus(@Parameter(description = "设备ID") @PathVariable Long deviceId) {
        try {
            log.info("获取设备健康状态，设备ID：{}", deviceId);

            ResponseDTO<Map<String, Object>> result = unifiedDeviceService.getDeviceHealthStatus(deviceId);
            if (!result.getOk()) {
                return SmartResponseUtil.error(result.getMsg());
            }

            log.info("获取设备健康状态成功，设备ID：{}", deviceId);
            return SmartResponseUtil.ok(result.getData());

        } catch (Exception e) {
            log.error("获取设备健康状态失败，设备ID：{}", deviceId, e);
            return SmartResponseUtil.error("获取设备健康状态失败：" + e.getMessage());
        }
    }

    // ========== 批量操作 ==========

    @Operation(summary = "批量操作设备")
    @SaCheckPermission("unified:device:batch")
    @PostMapping("/batch")
    public ResponseDTO<String> batchOperateDevices(
            @Parameter(description = "设备ID列表") @RequestParam List<Long> deviceIds,
            @Parameter(description = "操作类型") @RequestParam String operation) {

        try {
            log.info("批量操作设备，设备数量：{}，操作类型：{}", deviceIds.size(), operation);

            ResponseDTO<String> result = unifiedDeviceService.batchOperateDevices(deviceIds, operation);
            if (!result.getOk()) {
                return SmartResponseUtil.error(result.getMsg());
            }

            log.info("批量操作设备成功");
            return SmartResponseUtil.ok(result.getData());

        } catch (Exception e) {
            log.error("批量操作设备失败", e);
            return SmartResponseUtil.error("批量操作设备失败：" + e.getMessage());
        }
    }
}