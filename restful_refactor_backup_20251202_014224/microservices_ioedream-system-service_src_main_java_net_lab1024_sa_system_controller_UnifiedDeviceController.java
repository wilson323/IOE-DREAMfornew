package net.lab1024.sa.system.controller;

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

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.common.util.SmartBeanUtil;
import net.lab1024.sa.common.util.SmartPageUtil;
import net.lab1024.sa.system.domain.entity.UnifiedDeviceEntity;
import net.lab1024.sa.system.domain.form.UnifiedDeviceAddForm;
import net.lab1024.sa.system.domain.form.UnifiedDeviceQueryForm;
import net.lab1024.sa.system.domain.form.UnifiedDeviceUpdateForm;
import net.lab1024.sa.system.domain.vo.UnifiedDeviceVO;
import net.lab1024.sa.system.service.UnifiedDeviceService;

/**
 * 统一设备管理控制器
 * 严格遵循四层架构：Controller → Service → Manager → DAO
 *
 * @author IOE-DREAM Team
 */
@Slf4j
@SaCheckLogin
@RestController
@RequestMapping("/api/system/device")
@Tag(name = "统一设备管理", description = "统一设备管理接口")
public class UnifiedDeviceController {

    @Resource
    private UnifiedDeviceService unifiedDeviceService;

    // ========== 基础CRUD操作 ==========

    @Operation(summary = "分页查询设备列表")
    @SaCheckPermission("system:device:query")
    @GetMapping("/page")
    public ResponseDTO<PageResult<UnifiedDeviceVO>> queryDevicePage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "设备类型") @RequestParam(required = false) String deviceType,
            @Parameter(description = "设备状态") @RequestParam(required = false) String deviceStatus,
            @Parameter(description = "设备名称") @RequestParam(required = false) String deviceName,
            @Parameter(description = "区域ID") @RequestParam(required = false) Long areaId) {
        try {
            log.info("分页查询设备列表，设备类型：{}，设备状态：{}，设备名称：{}，区域ID：{}",
                    deviceType, deviceStatus, deviceName, areaId);

            UnifiedDeviceQueryForm queryForm = new UnifiedDeviceQueryForm();
            // 使用反射设置分页参数（Lombok @Data 会生成 setter 方法）
            try {
                java.lang.reflect.Method setPageNumMethod = queryForm.getClass().getMethod("setPageNum", Integer.class);
                setPageNumMethod.invoke(queryForm, pageNum);

                java.lang.reflect.Method setPageSizeMethod = queryForm.getClass().getMethod("setPageSize",
                        Integer.class);
                setPageSizeMethod.invoke(queryForm, pageSize);
            } catch (Exception e) {
                log.warn("无法通过反射设置分页参数", e);
            }
            queryForm.setDeviceType(deviceType);
            queryForm.setDeviceStatus(deviceStatus);
            queryForm.setDeviceName(deviceName);
            queryForm.setAreaId(areaId);

            PageResult<UnifiedDeviceEntity> pageResult = unifiedDeviceService.queryDevicePage(queryForm);
            PageResult<UnifiedDeviceVO> devicePageResultVO = SmartPageUtil.convert2PageResult(pageResult,
                    UnifiedDeviceVO.class);

            log.info("分页查询设备列表成功，总记录数：{}", devicePageResultVO.getTotal());
            return ResponseDTO.ok(devicePageResultVO);

        } catch (Exception e) {
            log.error("分页查询设备列表失败", e);
            return ResponseDTO.userErrorParam("查询设备列表失败：" + e.getMessage());
        }
    }

    @Operation(summary = "根据ID查询设备详情")
    @SaCheckPermission("system:device:query")
    @GetMapping("/{deviceId}")
    public ResponseDTO<UnifiedDeviceVO> getDeviceDetail(
            @Parameter(description = "设备ID") @PathVariable Long deviceId) {
        try {
            log.info("查询设备详情，设备ID：{}", deviceId);

            UnifiedDeviceEntity device = unifiedDeviceService.getDeviceDetail(deviceId);
            UnifiedDeviceVO deviceVO = new UnifiedDeviceVO();
            SmartBeanUtil.copyProperties(device, deviceVO);

            log.info("查询设备详情成功，设备ID：{}", deviceId);
            return ResponseDTO.ok(deviceVO);

        } catch (Exception e) {
            log.error("查询设备详情失败，设备ID：{}", deviceId, e);
            return ResponseDTO.userErrorParam("查询设备详情失败：" + e.getMessage());
        }
    }

    @Operation(summary = "新增设备")
    @SaCheckPermission("system:device:add")
    @PostMapping("/add")
    public ResponseDTO<Long> addDevice(@Valid @RequestBody UnifiedDeviceAddForm addForm) {
        try {
            log.info("新增设备，设备编号：{}，设备名称：{}", addForm.getDeviceCode(), addForm.getDeviceName());

            Long deviceId = unifiedDeviceService.addDevice(addForm);
            log.info("新增设备成功，设备ID：{}", deviceId);
            return ResponseDTO.ok(deviceId);

        } catch (Exception e) {
            log.error("新增设备失败", e);
            return ResponseDTO.userErrorParam("新增设备失败：" + e.getMessage());
        }
    }

    @Operation(summary = "更新设备")
    @SaCheckPermission("system:device:update")
    @PutMapping("/update")
    public ResponseDTO<String> updateDevice(@Valid @RequestBody UnifiedDeviceUpdateForm updateForm) {
        try {
            log.info("更新设备，设备ID：{}", updateForm.getDeviceId());

            unifiedDeviceService.updateDevice(updateForm);
            log.info("更新设备成功，设备ID：{}", updateForm.getDeviceId());
            return ResponseDTO.ok("更新成功");

        } catch (Exception e) {
            log.error("更新设备失败，设备ID：{}", updateForm.getDeviceId(), e);
            return ResponseDTO.userErrorParam("更新设备失败：" + e.getMessage());
        }
    }

    @Operation(summary = "删除设备")
    @SaCheckPermission("system:device:delete")
    @DeleteMapping("/{deviceId}")
    public ResponseDTO<String> deleteDevice(@Parameter(description = "设备ID") @PathVariable Long deviceId) {
        try {
            log.info("删除设备，设备ID：{}", deviceId);

            unifiedDeviceService.deleteDevice(deviceId);
            log.info("删除设备成功，设备ID：{}", deviceId);
            return ResponseDTO.ok("删除成功");

        } catch (Exception e) {
            log.error("删除设备失败，设备ID：{}", deviceId, e);
            return ResponseDTO.userErrorParam("删除设备失败：" + e.getMessage());
        }
    }

    // ========== 设备状态管理==========

    @Operation(summary = "启用设备")
    @SaCheckPermission("system:device:enable")
    @PutMapping("/{deviceId}/enable")
    public ResponseDTO<String> enableDevice(@Parameter(description = "设备ID") @PathVariable Long deviceId) {
        try {
            log.info("启用设备，设备ID：{}", deviceId);

            unifiedDeviceService.enableDevice(deviceId);
            log.info("启用设备成功，设备ID：{}", deviceId);
            return ResponseDTO.ok("启用成功");

        } catch (Exception e) {
            log.error("启用设备失败，设备ID：{}", deviceId, e);
            return ResponseDTO.userErrorParam("启用设备失败：" + e.getMessage());
        }
    }

    @Operation(summary = "禁用设备")
    @SaCheckPermission("system:device:disable")
    @PutMapping("/{deviceId}/disable")
    public ResponseDTO<String> disableDevice(@Parameter(description = "设备ID") @PathVariable Long deviceId) {
        try {
            log.info("禁用设备，设备ID：{}", deviceId);

            unifiedDeviceService.disableDevice(deviceId);
            log.info("禁用设备成功，设备ID：{}", deviceId);
            return ResponseDTO.ok("禁用成功");

        } catch (Exception e) {
            log.error("禁用设备失败，设备ID：{}", deviceId, e);
            return ResponseDTO.userErrorParam("禁用设备失败：" + e.getMessage());
        }
    }

    @Operation(summary = "更新设备状态")
    @SaCheckPermission("system:device:status:update")
    @PutMapping("/{deviceId}/status")
    public ResponseDTO<String> updateDeviceStatus(
            @Parameter(description = "设备ID") @PathVariable Long deviceId,
            @Parameter(description = "设备状态") @RequestParam String deviceStatus) {
        try {
            log.info("更新设备状态，设备ID：{}，新状态：{}", deviceId, deviceStatus);

            unifiedDeviceService.updateDeviceStatus(deviceId, deviceStatus);
            log.info("更新设备状态成功，设备ID：{}", deviceId);
            return ResponseDTO.ok("状态更新成功");

        } catch (Exception e) {
            log.error("更新设备状态失败，设备ID：{}", deviceId, e);
            return ResponseDTO.userErrorParam("更新设备状态失败：" + e.getMessage());
        }
    }

    // ========== 设备心跳 ==========

    @Operation(summary = "设备心跳上报")
    @PostMapping("/{deviceId}/heartbeat")
    public ResponseDTO<String> reportDeviceHeartbeat(@Parameter(description = "设备ID") @PathVariable Long deviceId) {
        try {
            log.debug("设备心跳上报，设备ID：{}", deviceId);

            boolean success = unifiedDeviceService.reportDeviceHeartbeat(deviceId);
            if (success) {
                return ResponseDTO.ok("心跳上报成功");
            } else {
                return ResponseDTO.userErrorParam("心跳上报失败");
            }

        } catch (Exception e) {
            log.error("设备心跳上报失败，设备ID：{}", deviceId, e);
            return ResponseDTO.userErrorParam("心跳上报失败：" + e.getMessage());
        }
    }

    // ========== 设备控制 ==========

    @Operation(summary = "远程控制设备")
    @SaCheckPermission("system:device:control")
    @PostMapping("/{deviceId}/control")
    public ResponseDTO<String> remoteControlDevice(
            @Parameter(description = "设备ID") @PathVariable Long deviceId,
            @Parameter(description = "控制命令") @RequestParam String command,
            @Parameter(description = "控制参数") @RequestBody(required = false) Map<String, Object> params) {
        try {
            log.info("远程控制设备，设备ID：{}，命令：{}", deviceId, command);

            String result = unifiedDeviceService.remoteControlDevice(deviceId, command, params);
            log.info("远程控制设备成功，设备ID：{}", deviceId);
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("远程控制设备失败，设备ID：{}，命令：{}", deviceId, command, e);
            return ResponseDTO.userErrorParam("设备控制失败：" + e.getMessage());
        }
    }

    // ========== 门禁设备专用接口 ==========

    @Operation(summary = "远程开门")
    @SaCheckPermission("system:device:access:open")
    @PostMapping("/{deviceId}/remote-open-door")
    public ResponseDTO<String> remoteOpenDoor(@Parameter(description = "设备ID") @PathVariable Long deviceId) {
        try {
            log.info("远程开门，设备ID：{}", deviceId);

            String result = unifiedDeviceService.remoteOpenDoor(deviceId);
            log.info("远程开门成功，设备ID：{}", deviceId);
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("远程开门失败，设备ID：{}", deviceId, e);
            return ResponseDTO.userErrorParam("远程开门失败：" + e.getMessage());
        }
    }

    // ========== 视频设备专用接口 ==========

    @Operation(summary = "云台控制")
    @SaCheckPermission("system:device:video:ptz")
    @PostMapping("/{deviceId}/ptz-control")
    public ResponseDTO<String> controlPtzDevice(
            @Parameter(description = "设备ID") @PathVariable Long deviceId,
            @Parameter(description = "云台命令") @RequestParam String command,
            @Parameter(description = "控制速度") @RequestParam(defaultValue = "5") Integer speed) {
        try {
            log.info("云台控制，设备ID：{}，命令：{}，速度：{}", deviceId, command, speed);

            String result = unifiedDeviceService.controlPtzDevice(deviceId, command, speed);
            log.info("云台控制成功，设备ID：{}", deviceId);
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("云台控制失败，设备ID：{}，命令：{}", deviceId, command, e);
            return ResponseDTO.userErrorParam("云台控制失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取实时视频流")
    @SaCheckPermission("system:device:video:stream")
    @GetMapping("/{deviceId}/live-stream")
    public ResponseDTO<String> getLiveStream(@Parameter(description = "设备ID") @PathVariable Long deviceId) {
        try {
            log.info("获取实时视频流，设备ID：{}", deviceId);

            String streamUrl = unifiedDeviceService.getLiveStream(deviceId);
            log.info("获取实时视频流成功，设备ID：{}", deviceId);
            return ResponseDTO.ok(streamUrl);

        } catch (Exception e) {
            log.error("获取实时视频流失败，设备ID：{}", deviceId, e);
            return ResponseDTO.userErrorParam("获取视频流失败：" + e.getMessage());
        }
    }

    // ========== 统计信息 ==========

    @Operation(summary = "获取设备状态统计")
    @SaCheckPermission("system:device:statistics")
    @GetMapping("/statistics/status")
    public ResponseDTO<Map<String, Object>> getDeviceStatusStatistics(
            @Parameter(description = "设备类型") @RequestParam(required = false) String deviceType) {
        try {
            log.info("获取设备状态统计，设备类型：{}", deviceType);

            Map<String, Object> statistics = unifiedDeviceService.getDeviceStatusStatistics(deviceType);
            log.info("获取设备状态统计成功");
            return ResponseDTO.ok(statistics);

        } catch (Exception e) {
            log.error("获取设备状态统计失败", e);
            return ResponseDTO.userErrorParam("获取设备状态统计失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取设备类型统计")
    @SaCheckPermission("system:device:statistics")
    @GetMapping("/statistics/type")
    public ResponseDTO<Map<String, Object>> getDeviceTypeStatistics() {
        try {
            log.info("获取设备类型统计");

            Map<String, Object> statistics = unifiedDeviceService.getDeviceTypeStatistics();
            log.info("获取设备类型统计成功");
            return ResponseDTO.ok(statistics);

        } catch (Exception e) {
            log.error("获取设备类型统计失败", e);
            return ResponseDTO.userErrorParam("获取设备类型统计失败：" + e.getMessage());
        }
    }

    // ========== 设备列表查询 ==========

    @Operation(summary = "获取在线设备列表")
    @SaCheckPermission("system:device:list")
    @GetMapping("/online")
    public ResponseDTO<List<UnifiedDeviceVO>> getOnlineDevices(
            @Parameter(description = "设备类型") @RequestParam(required = false) String deviceType) {
        try {
            log.info("获取在线设备列表，设备类型：{}", deviceType);

            List<UnifiedDeviceEntity> devices = unifiedDeviceService.getOnlineDevices(deviceType);
            List<UnifiedDeviceVO> deviceVOs = SmartBeanUtil.copyList(devices, UnifiedDeviceVO.class);

            log.info("获取在线设备列表成功，数量：{}", deviceVOs.size());
            return ResponseDTO.ok(deviceVOs);

        } catch (Exception e) {
            log.error("获取在线设备列表失败", e);
            return ResponseDTO.userErrorParam("获取在线设备列表失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取离线设备列表")
    @SaCheckPermission("system:device:list")
    @GetMapping("/offline")
    public ResponseDTO<List<UnifiedDeviceVO>> getOfflineDevices(
            @Parameter(description = "设备类型") @RequestParam(required = false) String deviceType) {
        try {
            log.info("获取离线设备列表，设备类型：{}", deviceType);

            List<UnifiedDeviceEntity> devices = unifiedDeviceService.getOfflineDevices(deviceType);
            List<UnifiedDeviceVO> deviceVOs = SmartBeanUtil.copyList(devices, UnifiedDeviceVO.class);

            log.info("获取离线设备列表成功，数量：{}", deviceVOs.size());
            return ResponseDTO.ok(deviceVOs);

        } catch (Exception e) {
            log.error("获取离线设备列表失败", e);
            return ResponseDTO.userErrorParam("获取离线设备列表失败：" + e.getMessage());
        }
    }

    // ========== 批量操作 ==========

    @Operation(summary = "批量操作设备")
    @SaCheckPermission("system:device:batch")
    @PostMapping("/batch")
    public ResponseDTO<String> batchOperateDevices(
            @Parameter(description = "设备ID列表") @RequestParam List<Long> deviceIds,
            @Parameter(description = "操作类型") @RequestParam String operation) {
        try {
            log.info("批量操作设备，设备数量：{}，操作类型：{}", deviceIds.size(), operation);

            String result = unifiedDeviceService.batchOperateDevices(deviceIds, operation);
            log.info("批量操作设备成功");
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("批量操作设备失败", e);
            return ResponseDTO.userErrorParam("批量操作设备失败：" + e.getMessage());
        }
    }
}
