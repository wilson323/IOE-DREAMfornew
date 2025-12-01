package net.lab1024.sa.device.controller;

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.common.util.SmartVerificationUtil;
import net.lab1024.sa.device.domain.form.PhysicalDeviceAddForm;
import net.lab1024.sa.device.domain.form.PhysicalDeviceQueryForm;
import net.lab1024.sa.device.domain.form.PhysicalDeviceUpdateForm;
import net.lab1024.sa.device.domain.vo.PhysicalDeviceDetailVO;
import net.lab1024.sa.device.domain.vo.PhysicalDeviceVO;
import net.lab1024.sa.device.service.PhysicalDeviceService;

/**
 * 物理设备管理控制器
 *
 * 专注于物理设备的基础管理，包括：
 * - 设备注册与配置
 * - 设备状态监控
 * - 设备连接管理
 * - 设备数据采集
 * - 设备维护管理
 *
 * 注意：避免与System Service的统一设备控制功能重叠
 *
 * @author IOE-DREAM Team
 */
@Slf4j
@RestController
@RequestMapping("/api/device/physical")
@Tag(name = "物理设备管理", description = "专注于物理设备的基础管理功能")
@Validated
public class PhysicalDeviceController {

    @Resource
    private PhysicalDeviceService physicalDeviceService;

    @PostMapping("/register")
    @Operation(summary = "注册物理设备", description = "注册新的物理设备到系统")
    public ResponseDTO<Long> registerPhysicalDevice(@RequestBody @Valid PhysicalDeviceAddForm addForm) {
        log.info("注册物理设备，设备编号：{}，设备类型：{}", addForm.getDeviceCode(), addForm.getDeviceType());

        Long deviceId = physicalDeviceService.registerPhysicalDevice(addForm);

        log.info("物理设备注册成功，设备ID：{}", deviceId);

        return ResponseDTO.ok(deviceId);
    }

    @GetMapping("/query-page")
    @Operation(summary = "分页查询物理设备", description = "分页查询物理设备列表")
    public ResponseDTO<PageResult<PhysicalDeviceVO>> queryPhysicalDevicePage(@Valid PhysicalDeviceQueryForm queryForm) {
        log.info("分页查询物理设备，页码：{}，页大小：{}，设备类型：{}",
                queryForm.getPageNum(), queryForm.getPageSize(), queryForm.getDeviceType());

        PageResult<PhysicalDeviceVO> pageResult = physicalDeviceService.queryPhysicalDevicePage(queryForm);

        log.info("物理设备分页查询完成，记录数：{}", pageResult.getList() != null ? pageResult.getList().size() : 0);

        return ResponseDTO.ok(pageResult);
    }

    @GetMapping("/{deviceId}")
    @Operation(summary = "获取设备详情", description = "获取指定物理设备的详细信息")
    public ResponseDTO<PhysicalDeviceDetailVO> getPhysicalDeviceDetail(@PathVariable Long deviceId) {
        SmartVerificationUtil.verify(deviceId != null && deviceId > 0, "设备ID不能为空");

        log.info("获取物理设备详情，设备ID：{}", deviceId);

        PhysicalDeviceDetailVO deviceDetail = physicalDeviceService.getPhysicalDeviceDetail(deviceId);

        log.info("物理设备详情获取完成，设备名称：{}", deviceDetail.getDeviceName());

        return ResponseDTO.ok(deviceDetail);
    }

    @PutMapping("/{deviceId}")
    @Operation(summary = "更新设备信息", description = "更新物理设备的基本信息")
    public ResponseDTO<Void> updatePhysicalDevice(@PathVariable Long deviceId,
            @RequestBody @Valid PhysicalDeviceUpdateForm updateForm) {
        SmartVerificationUtil.verify(deviceId != null && deviceId > 0, "设备ID不能为空");
        updateForm.setDeviceId(deviceId);

        log.info("更新物理设备信息，设备ID：{}", deviceId);

        physicalDeviceService.updatePhysicalDevice(updateForm);

        log.info("物理设备信息更新完成");

        return ResponseDTO.ok();
    }

    @DeleteMapping("/{deviceId}")
    @Operation(summary = "注销设备", description = "注销物理设备（软删除）")
    public ResponseDTO<Void> unregisterPhysicalDevice(@PathVariable Long deviceId) {
        SmartVerificationUtil.verify(deviceId != null && deviceId > 0, "设备ID不能为空");

        log.info("注销物理设备，设备ID：{}", deviceId);

        physicalDeviceService.unregisterPhysicalDevice(deviceId);

        log.info("物理设备注销完成");

        return ResponseDTO.ok();
    }

    @PostMapping("/{deviceId}/connect")
    @Operation(summary = "连接设备", description = "建立与物理设备的连接")
    public ResponseDTO<Void> connectDevice(@PathVariable Long deviceId) {
        SmartVerificationUtil.verify(deviceId != null && deviceId > 0, "设备ID不能为空");

        log.info("连接物理设备，设备ID：{}", deviceId);

        physicalDeviceService.connectDevice(deviceId);

        log.info("物理设备连接成功");

        return ResponseDTO.ok();
    }

    @PostMapping("/{deviceId}/disconnect")
    @Operation(summary = "断开设备连接", description = "断开与物理设备的连接")
    public ResponseDTO<Void> disconnectDevice(@PathVariable Long deviceId) {
        SmartVerificationUtil.verify(deviceId != null && deviceId > 0, "设备ID不能为空");

        log.info("断开物理设备连接，设备ID：{}", deviceId);

        physicalDeviceService.disconnectDevice(deviceId);

        log.info("物理设备连接断开完成");

        return ResponseDTO.ok();
    }

    @PostMapping("/{deviceId}/heartbeat")
    @Operation(summary = "设备心跳", description = "处理设备心跳请求")
    public ResponseDTO<Boolean> deviceHeartbeat(@PathVariable Long deviceId,
            @RequestBody Map<String, Object> heartbeatData) {
        SmartVerificationUtil.verify(deviceId != null && deviceId > 0, "设备ID不能为空");

        log.debug("设备心跳，设备ID：{}", deviceId);

        boolean result = physicalDeviceService.handleDeviceHeartbeat(deviceId, heartbeatData);

        return ResponseDTO.ok(result);
    }

    @GetMapping("/{deviceId}/status")
    @Operation(summary = "获取设备状态", description = "获取物理设备的当前状态")
    public ResponseDTO<Map<String, Object>> getDeviceStatus(@PathVariable Long deviceId) {
        SmartVerificationUtil.verify(deviceId != null && deviceId > 0, "设备ID不能为空");

        log.info("获取设备状态，设备ID：{}", deviceId);

        Map<String, Object> deviceStatus = physicalDeviceService.getDeviceStatus(deviceId);

        log.info("设备状态获取完成，状态：{}", deviceStatus.get("status"));

        return ResponseDTO.ok(deviceStatus);
    }

    @GetMapping("/online/list")
    @Operation(summary = "获取在线设备列表", description = "获取当前在线的物理设备列表")
    public ResponseDTO<List<PhysicalDeviceVO>> getOnlineDeviceList(@RequestParam(required = false) String deviceType) {
        log.info("获取在线设备列表，设备类型：{}", deviceType);

        List<PhysicalDeviceVO> onlineDevices = physicalDeviceService.getOnlineDeviceList(deviceType);

        log.info("在线设备列表获取完成，设备数量：{}", onlineDevices.size());

        return ResponseDTO.ok(onlineDevices);
    }

    @GetMapping("/offline/list")
    @Operation(summary = "获取离线设备列表", description = "获取当前离线的物理设备列表")
    public ResponseDTO<List<PhysicalDeviceVO>> getOfflineDeviceList(@RequestParam(required = false) String deviceType) {
        log.info("获取离线设备列表，设备类型：{}", deviceType);

        List<PhysicalDeviceVO> offlineDevices = physicalDeviceService.getOfflineDeviceList(deviceType);

        log.info("离线设备列表获取完成，设备数量：{}", offlineDevices.size());

        return ResponseDTO.ok(offlineDevices);
    }

    @PostMapping("/batch/connect")
    @Operation(summary = "批量连接设备", description = "批量连接多个物理设备")
    public ResponseDTO<Map<String, Integer>> batchConnectDevices(@RequestBody List<Long> deviceIds) {
        SmartVerificationUtil.verify(deviceIds != null && !deviceIds.isEmpty(), "设备ID列表不能为空");
        int deviceCount = deviceIds != null ? deviceIds.size() : 0;
        SmartVerificationUtil.verify(deviceCount <= 100, "批量连接最多支持100个设备");

        log.info("批量连接设备，设备数量：{}", deviceCount);

        Map<String, Integer> result = physicalDeviceService.batchConnectDevices(deviceIds);

        log.info("批量设备连接完成，成功：{}，失败：{}", result.get("success"), result.get("failed"));

        return ResponseDTO.ok(result);
    }

    @PostMapping("/{deviceId}/maintenance/record")
    @Operation(summary = "记录设备维护", description = "记录设备维护信息")
    public ResponseDTO<Void> recordMaintenance(@PathVariable Long deviceId,
            @RequestBody @Valid Map<String, Object> maintenanceData) {
        SmartVerificationUtil.verify(deviceId != null && deviceId > 0, "设备ID不能为空");

        log.info("记录设备维护，设备ID：{}", deviceId);

        physicalDeviceService.recordMaintenance(deviceId, maintenanceData);

        log.info("设备维护记录完成");

        return ResponseDTO.ok();
    }

    @GetMapping("/{deviceId}/maintenance/history")
    @Operation(summary = "获取维护历史", description = "获取设备的维护历史记录")
    public ResponseDTO<List<Map<String, Object>>> getMaintenanceHistory(@PathVariable Long deviceId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        SmartVerificationUtil.verify(deviceId != null && deviceId > 0, "设备ID不能为空");

        log.info("获取设备维护历史，设备ID：{}", deviceId);

        List<Map<String, Object>> maintenanceHistory = physicalDeviceService.getMaintenanceHistory(deviceId, pageNum,
                pageSize);

        log.info("设备维护历史获取完成，记录数：{}", maintenanceHistory.size());

        return ResponseDTO.ok(maintenanceHistory);
    }
}
