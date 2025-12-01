package net.lab1024.sa.device.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.domain.entity.DeviceEntity;
import net.lab1024.sa.device.service.DeviceService;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;

/**
 * 设备管理控制器
 * 基于现有SmartDeviceController重构，提供统一设备管理API
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27 (基于原SmartDeviceController重构)
 */
@Slf4j
@RestController
@RequestMapping("/api/device")
@RequiredArgsConstructor
@Validated
@Tag(name = "设备管理", description = "设备管理相关接口")
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping("/page")
    @Operation(summary = "分页查询设备列表", description = "根据条件分页查询设备列表")
    @PreAuthorize("hasAuthority('device:page')")
    public ResponseDTO<PageResult<DeviceEntity>> queryDevicePage(@Valid PageParam pageParam,
            @RequestParam(required = false) String deviceType,
            @RequestParam(required = false) String deviceStatus,
            @RequestParam(required = false) String deviceName,
            @RequestParam(required = false) Long areaId) {
        return ResponseDTO.ok(deviceService.queryDevicePage(pageParam, deviceType, deviceStatus, deviceName, areaId));
    }

    @GetMapping("/detail/{deviceId}")
    @Operation(summary = "获取设备详情", description = "根据设备ID获取详细信息")
    @PreAuthorize("hasAuthority('device:detail')")
    public ResponseDTO<DeviceEntity> getDeviceDetail(@PathVariable @NotNull Long deviceId) {
        return deviceService.getDeviceById(deviceId);
    }

    @PostMapping("/add")
    @Operation(summary = "新增设备", description = "新增设备信息")
    @PreAuthorize("hasAuthority('device:add')")
    public ResponseDTO<String> addDevice(@RequestBody @Valid DeviceEntity device) {
        return deviceService.addDevice(device);
    }

    @PutMapping("/update")
    @Operation(summary = "更新设备", description = "更新设备信息")
    @PreAuthorize("hasAuthority('device:update')")
    public ResponseDTO<String> updateDevice(@RequestBody @Valid DeviceEntity device) {
        return deviceService.updateDevice(device);
    }

    @DeleteMapping("/delete/{deviceId}")
    @Operation(summary = "删除设备", description = "根据设备ID删除设备")
    @PreAuthorize("hasAuthority('device:delete')")
    public ResponseDTO<String> deleteDevice(@PathVariable @NotNull Long deviceId) {
        return deviceService.deleteDevice(deviceId);
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除设备", description = "批量删除设备")
    @PreAuthorize("hasAuthority('device:delete')")
    public ResponseDTO<String> batchDeleteDevice(@RequestBody @NotNull List<Long> deviceIds) {
        return deviceService.batchDeleteDevice(deviceIds);
    }

    @PutMapping("/status/{deviceId}")
    @Operation(summary = "更新设备状态", description = "更新设备状态")
    @PreAuthorize("hasAuthority('device:update-status')")
    public ResponseDTO<String> updateDeviceStatus(@PathVariable @NotNull Long deviceId,
            @RequestParam @NotNull String deviceStatus) {
        return deviceService.updateDeviceStatus(deviceId, deviceStatus);
    }

    @PutMapping("/status/batch")
    @Operation(summary = "批量更新设备状态", description = "批量更新设备状态")
    @PreAuthorize("hasAuthority('device:update-status')")
    public ResponseDTO<String> batchUpdateDeviceStatus(@RequestBody @NotNull List<Long> deviceIds,
            @RequestParam @NotNull String deviceStatus) {
        return deviceService.batchUpdateDeviceStatus(deviceIds, deviceStatus);
    }

    @GetMapping("/code/{deviceCode}")
    @Operation(summary = "根据设备编码查询设备", description = "根据设备编码获取设备信息")
    @PreAuthorize("hasAuthority('device:detail')")
    public ResponseDTO<DeviceEntity> getDeviceByCode(@PathVariable @NotNull String deviceCode) {
        return deviceService.getDeviceByCode(deviceCode);
    }

    @GetMapping("/type/{deviceType}")
    @Operation(summary = "根据设备类型查询设备", description = "根据设备类型获取设备列表")
    @PreAuthorize("hasAuthority('device:list')")
    public ResponseDTO<List<DeviceEntity>> getDevicesByType(@PathVariable @NotNull String deviceType) {
        return deviceService.getDevicesByType(deviceType);
    }

    @GetMapping("/status/{deviceStatus}")
    @Operation(summary = "根据设备状态查询设备", description = "根据设备状态获取设备列表")
    @PreAuthorize("hasAuthority('device:list')")
    public ResponseDTO<List<DeviceEntity>> getDevicesByStatus(@PathVariable @NotNull String deviceStatus) {
        return deviceService.getDevicesByStatus(deviceStatus);
    }

    @GetMapping("/area/{areaId}")
    @Operation(summary = "根据区域查询设备", description = "根据区域ID获取设备列表")
    @PreAuthorize("hasAuthority('device:list')")
    public ResponseDTO<List<DeviceEntity>> getDevicesByAreaId(@PathVariable @NotNull Long areaId) {
        return deviceService.getDevicesByAreaId(areaId);
    }

    @GetMapping("/online")
    @Operation(summary = "获取在线设备列表", description = "获取所有在线设备")
    @PreAuthorize("hasAuthority('device:list')")
    public ResponseDTO<List<DeviceEntity>> getOnlineDevices() {
        return deviceService.getOnlineDevices();
    }

    @GetMapping("/offline")
    @Operation(summary = "获取离线设备列表", description = "获取所有离线设备")
    @PreAuthorize("hasAuthority('device:list')")
    public ResponseDTO<List<DeviceEntity>> getOfflineDevices() {
        return deviceService.getOfflineDevices();
    }

    @GetMapping("/maintenance")
    @Operation(summary = "获取需要维护的设备", description = "获取需要维护的设备列表")
    @PreAuthorize("hasAuthority('device:list')")
    public ResponseDTO<List<DeviceEntity>> getMaintenanceRequiredDevices() {
        return deviceService.getMaintenanceRequiredDevices();
    }

    @GetMapping("/heartbeat-timeout")
    @Operation(summary = "获取心跳超时的设备", description = "获取心跳超时的设备列表")
    @PreAuthorize("hasAuthority('device:list')")
    public ResponseDTO<List<DeviceEntity>> getHeartbeatTimeoutDevices() {
        return deviceService.getHeartbeatTimeoutDevices();
    }

    @GetMapping("/statistics/status")
    @Operation(summary = "设备状态统计", description = "获取设备状态统计信息")
    @PreAuthorize("hasAuthority('device:statistics')")
    public ResponseDTO<Map<String, Object>> getDeviceStatusStatistics() {
        return deviceService.getDeviceStatusStatistics();
    }

    @GetMapping("/statistics/type")
    @Operation(summary = "设备类型统计", description = "获取设备类型统计信息")
    @PreAuthorize("hasAuthority('device:statistics')")
    public ResponseDTO<Map<String, Object>> getDeviceTypeStatistics() {
        return deviceService.getDeviceTypeStatistics();
    }

    @GetMapping("/statistics/health")
    @Operation(summary = "设备健康统计", description = "获取设备健康状态统计")
    @PreAuthorize("hasAuthority('device:statistics')")
    public ResponseDTO<Map<String, Object>> getDeviceHealthStatistics() {
        return deviceService.getDeviceHealthStatistics();
    }

    @GetMapping("/statistics/area")
    @Operation(summary = "区域设备统计", description = "获取按区域分组的设备统计")
    @PreAuthorize("hasAuthority('device:statistics')")
    public ResponseDTO<Map<String, Object>> getAreaDeviceStatistics() {
        return deviceService.getAreaDeviceStatistics();
    }

    @GetMapping("/statistics/manufacturer")
    @Operation(summary = "制造商统计", description = "获取按制造商分组的设备统计")
    @PreAuthorize("hasAuthority('device:statistics')")
    public ResponseDTO<Map<String, Object>> getManufacturerStatistics() {
        return deviceService.getManufacturerStatistics();
    }

    @GetMapping("/search")
    @Operation(summary = "搜索设备", description = "根据条件搜索设备")
    @PreAuthorize("hasAuthority('device:list')")
    public ResponseDTO<List<DeviceEntity>> searchDevices(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) String deviceType,
            @RequestParam(required = false) String deviceStatus,
            @RequestParam(required = false) Long areaId) {
        return deviceService.searchDevices(keyword, deviceType, deviceStatus, areaId);
    }

    @PostMapping("/online/{deviceId}")
    @Operation(summary = "设备上线", description = "标记设备为在线状态")
    @PreAuthorize("hasAuthority('device:control')")
    public ResponseDTO<String> deviceOnline(@PathVariable @NotNull Long deviceId) {
        return deviceService.deviceOnline(deviceId);
    }

    @PostMapping("/offline/{deviceId}")
    @Operation(summary = "设备下线", description = "标记设备为离线状态")
    @PreAuthorize("hasAuthority('device:control')")
    public ResponseDTO<String> deviceOffline(@PathVariable @NotNull Long deviceId) {
        return deviceService.deviceOffline(deviceId);
    }

    @PostMapping("/heartbeat/{deviceId}")
    @Operation(summary = "更新设备心跳", description = "更新设备最后心跳时间")
    @PreAuthorize("hasAuthority('device:monitor')")
    public ResponseDTO<String> updateHeartbeat(@PathVariable @NotNull Long deviceId) {
        return deviceService.updateHeartbeat(deviceId);
    }

    @PostMapping("/fault/{deviceId}")
    @Operation(summary = "设备故障报告", description = "报告设备故障")
    @PreAuthorize("hasAuthority('device:monitor')")
    public ResponseDTO<String> reportDeviceFault(@PathVariable @NotNull Long deviceId,
            @RequestParam(required = false) String faultMessage) {
        return deviceService.reportDeviceFault(deviceId, faultMessage);
    }

    @PostMapping("/recovery/{deviceId}")
    @Operation(summary = "设备恢复报告", description = "报告设备恢复")
    @PreAuthorize("hasAuthority('device:monitor')")
    public ResponseDTO<String> reportDeviceRecovery(@PathVariable @NotNull Long deviceId,
            @RequestParam(required = false) String recoveryMessage) {
        return deviceService.reportDeviceRecovery(deviceId, recoveryMessage);
    }

    @GetMapping("/health")
    @Operation(summary = "设备服务健康检查", description = "检查设备服务是否正常运行")
    public ResponseDTO<String> health() {
        return ResponseDTO.ok("Device Service is running");
    }

    // 兼容性接口，保持与原有设备控制器的兼容

    /**
     * 智能设备列表（兼容性接口）
     */
    @GetMapping("/smart/list")
    @Operation(summary = "智能设备列表", description = "智能设备列表（兼容性接口）")
    public ResponseDTO<PageResult<DeviceEntity>> getSmartDeviceList(@Valid PageParam pageParam,
            @RequestParam(required = false) String deviceType,
            @RequestParam(required = false) String deviceStatus,
            @RequestParam(required = false) String deviceName) {
        return queryDevicePage(pageParam, deviceType, deviceStatus, deviceName, null);
    }

    /**
     * 智能设备详情（兼容性接口）
     */
    @GetMapping("/smart/detail/{deviceId}")
    @Operation(summary = "智能设备详情", description = "智能设备详情（兼容性接口）")
    public ResponseDTO<DeviceEntity> getSmartDeviceDetail(@PathVariable @NotNull Long deviceId) {
        return getDeviceDetail(deviceId);
    }

    /**
     * 门禁设备详情（兼容性接口）
     */
    @GetMapping("/access/detail/{accessDeviceId}")
    @Operation(summary = "门禁设备详情", description = "门禁设备详情（兼容性接口）")
    public ResponseDTO<DeviceEntity> getAccessDeviceDetail(@PathVariable @NotNull Long accessDeviceId) {
        return deviceService.getAccessDeviceById(accessDeviceId);
    }

    /**
     * 消费设备详情（兼容性接口）
     */
    @GetMapping("/consume/detail/{consumeDeviceId}")
    @Operation(summary = "消费设备详情", description = "消费设备详情（兼容性接口）")
    public ResponseDTO<DeviceEntity> getConsumeDeviceDetail(@PathVariable @NotNull Long consumeDeviceId) {
        return deviceService.getConsumeDeviceById(consumeDeviceId);
    }

    /**
     * 考勤设备详情（兼容性接口）
     */
    @GetMapping("/attendance/detail/{attendanceDeviceId}")
    @Operation(summary = "考勤设备详情", description = "考勤设备详情（兼容性接口）")
    public ResponseDTO<DeviceEntity> getAttendanceDeviceDetail(@PathVariable @NotNull Long attendanceDeviceId) {
        return deviceService.getAttendanceDeviceById(attendanceDeviceId);
    }

    /**
     * 视频设备详情（兼容性接口）
     */
    @GetMapping("/video/detail/{videoDeviceId}")
    @Operation(summary = "视频设备详情", description = "视频设备详情（兼容性接口）")
    public ResponseDTO<DeviceEntity> getVideoDeviceDetail(@PathVariable @NotNull Long videoDeviceId) {
        return deviceService.getVideoDeviceById(videoDeviceId);
    }
}
