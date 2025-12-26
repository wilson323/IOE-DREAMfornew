package net.lab1024.sa.consume.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import net.lab1024.sa.consume.domain.form.ConsumeDeviceAddForm;
import net.lab1024.sa.consume.domain.form.ConsumeDeviceQueryForm;
import net.lab1024.sa.consume.domain.form.ConsumeDeviceUpdateForm;
import net.lab1024.sa.consume.domain.vo.ConsumeDeviceVO;
import net.lab1024.sa.consume.service.ConsumeDeviceService;

import lombok.extern.slf4j.Slf4j;

/**
 * 消费设备管理控制器
 * <p>
 * 提供消费设备的管理功能，包括：
 * 1. 设备基本信息管理
 * 2. 设备状态监控
 * 3. 设备配置管理
 * 4. 设备数据同步
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Slf4j
@RestController
@PermissionCheck(value = "CONSUME_DEVICE_MANAGE", description = "消费设备管理权限")
@RequestMapping("/api/v1/consume/devices")
@Tag(name = "消费设备管理", description = "消费设备管理、状态监控、配置等功能")
public class ConsumeDeviceController {

    @Resource
    private ConsumeDeviceService consumeDeviceService;

    /**
     * 分页查询消费设备列表
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    @GetMapping("/query")
    @Operation(summary = "分页查询消费设备", description = "根据条件分页查询消费设备列表")
    public ResponseDTO<PageResult<ConsumeDeviceVO>> queryDevices(@ModelAttribute ConsumeDeviceQueryForm queryForm) {
        log.info("[设备管理] 分页查询消费设备: queryForm={}", queryForm);
        PageResult<ConsumeDeviceVO> result = consumeDeviceService.queryDevices(queryForm);
        log.info("[设备管理] 分页查询消费设备成功: totalCount={}", result.getTotal());
        return ResponseDTO.ok(result);
    }

    /**
     * 获取消费设备详情
     *
     * @param deviceId 设备ID
     * @return 设备详情
     */
    @GetMapping("/{deviceId}")
    @Operation(summary = "获取消费设备详情", description = "根据设备ID获取详细的设备信息")
    public ResponseDTO<ConsumeDeviceVO> getDeviceDetail(
            @Parameter(description = "设备ID", required = true) @PathVariable Long deviceId) {
        ConsumeDeviceVO device = consumeDeviceService.getDeviceDetail(deviceId);
        return ResponseDTO.ok(device);
    }

    /**
     * 新增消费设备
     *
     * @param addForm 设备新增表单
     * @return 新增结果
     */
    @PostMapping("/add")
    @Operation(summary = "新增消费设备", description = "创建新的消费设备")
    public ResponseDTO<Long> addDevice(@Valid @RequestBody ConsumeDeviceAddForm addForm) {
        log.info("[设备管理] 新增消费设备: deviceName={}, deviceType={}",
                addForm.getDeviceName(), addForm.getDeviceType());
        Long deviceId = consumeDeviceService.createDevice(addForm);
        log.info("[设备管理] 新增消费设备成功: deviceId={}, deviceName={}", deviceId, addForm.getDeviceName());
        return ResponseDTO.ok(deviceId);
    }

    /**
     * 更新消费设备
     *
     * @param deviceId 设备ID
     * @param updateForm 更新表单
     * @return 更新结果
     */
    @PutMapping("/{deviceId}")
    @Operation(summary = "更新消费设备", description = "更新消费设备的基本信息")
    public ResponseDTO<Void> updateDevice(
            @Parameter(description = "设备ID", required = true) @PathVariable Long deviceId,
            @Valid @RequestBody ConsumeDeviceUpdateForm updateForm) {
        consumeDeviceService.updateDevice(deviceId, updateForm);
        return ResponseDTO.ok();
    }

    /**
     * 删除消费设备
     *
     * @param deviceId 设备ID
     * @return 删除结果
     */
    @DeleteMapping("/{deviceId}")
    @Operation(summary = "删除消费设备", description = "删除指定的消费设备")
    public ResponseDTO<Void> deleteDevice(
            @Parameter(description = "设备ID", required = true) @PathVariable Long deviceId) {
        log.info("[设备管理] 删除消费设备: deviceId={}", deviceId);
        consumeDeviceService.deleteDevice(deviceId);
        log.info("[设备管理] 删除消费设备成功: deviceId={}", deviceId);
        return ResponseDTO.ok();
    }

    /**
     * 获取设备状态
     *
     * @param deviceId 设备ID
     * @return 设备状态
     */
    @GetMapping("/{deviceId}/status")
    @Operation(summary = "获取设备状态", description = "获取指定设备的实时状态")
    public ResponseDTO<String> getDeviceStatus(
            @Parameter(description = "设备ID", required = true) @PathVariable Long deviceId) {
        String status = consumeDeviceService.getDeviceStatus(deviceId);
        return ResponseDTO.ok(status);
    }

    /**
     * 更新设备状态
     *
     * @param deviceId 设备ID
     * @param status 设备状态
     * @return 更新结果
     */
    @PutMapping("/{deviceId}/status")
    @Operation(summary = "更新设备状态", description = "更新指定设备的状态")
    public ResponseDTO<Void> updateDeviceStatus(
            @Parameter(description = "设备ID", required = true) @PathVariable Long deviceId,
            @Parameter(description = "设备状态", required = true) @RequestParam String status) {
        consumeDeviceService.updateDeviceStatus(deviceId, status);
        return ResponseDTO.ok();
    }

    /**
     * 获取在线设备列表
     *
     * @return 在线设备列表
     */
    @GetMapping("/online")
    @Operation(summary = "获取在线设备", description = "获取当前在线的消费设备列表")
    public ResponseDTO<List<ConsumeDeviceVO>> getOnlineDevices() {
        List<ConsumeDeviceVO> devices = consumeDeviceService.getOnlineDevices();
        return ResponseDTO.ok(devices);
    }

    /**
     * 获取离线设备列表
     *
     * @return 离线设备列表
     */
    @GetMapping("/offline")
    @Operation(summary = "获取离线设备", description = "获取当前离线的消费设备列表")
    public ResponseDTO<List<ConsumeDeviceVO>> getOfflineDevices() {
        List<ConsumeDeviceVO> devices = consumeDeviceService.getOfflineDevices();
        return ResponseDTO.ok(devices);
    }

    /**
     * 设备重启
     *
     * @param deviceId 设备ID
     * @return 重启结果
     */
    @PostMapping("/{deviceId}/restart")
    @Operation(summary = "重启设备", description = "重启指定的消费设备")
    public ResponseDTO<Void> restartDevice(
            @Parameter(description = "设备ID", required = true) @PathVariable Long deviceId) {
        consumeDeviceService.restartDevice(deviceId);
        return ResponseDTO.ok();
    }

    /**
     * 同步设备配置
     *
     * @param deviceId 设备ID
     * @return 同步结果
     */
    @PostMapping("/{deviceId}/sync")
    @Operation(summary = "同步设备配置", description = "同步设备配置到服务器")
    public ResponseDTO<Void> syncDeviceConfig(
            @Parameter(description = "设备ID", required = true) @PathVariable Long deviceId) {
        consumeDeviceService.syncDeviceConfig(deviceId);
        return ResponseDTO.ok();
    }

    /**
     * 获取设备统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取设备统计信息", description = "获取消费设备的统计信息")
    public ResponseDTO<java.util.Map<String, Object>> getDeviceStatistics() {
        java.util.Map<String, Object> statistics = consumeDeviceService.getDeviceStatistics();
        return ResponseDTO.ok(statistics);
    }

    /**
     * 批量操作设备
     *
     * @param deviceIds 设备ID列表
     * @param operation 操作类型
     * @return 操作结果
     */
    @PostMapping("/batch")
    @Operation(summary = "批量操作设备", description = "对多个设备执行批量操作")
    public ResponseDTO<Void> batchOperateDevices(
            @Parameter(description = "设备ID列表", required = true) @RequestParam List<Long> deviceIds,
            @Parameter(description = "操作类型", required = true) @RequestParam String operation) {
        consumeDeviceService.batchOperateDevices(deviceIds, operation);
        return ResponseDTO.ok();
    }
}