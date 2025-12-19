package net.lab1024.sa.device.comm.discovery.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.device.comm.discovery.ProtocolAutoDiscoveryManager;
import net.lab1024.sa.device.comm.discovery.service.ProtocolDiscoveryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 协议自动发现控制器
 * <p>
 * 提供设备协议自动发现的REST API接口：
 * 1. 发现任务管理接口
 * 2. 设备扫描和检测接口
 * 3. 结果查询和导出接口
 * 4. 统计和监控接口
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/discovery")
@Tag(name = "协议自动发现", description = "设备协议自动发现相关接口")
@Validated
public class ProtocolDiscoveryController {

    @Resource
    private ProtocolDiscoveryService protocolDiscoveryService;

    @Operation(summary = "启动发现任务", description = "启动网络设备协议自动发现任务")
    @PostMapping("/start")
    public ResponseDTO<String> startDiscovery(@Valid @RequestBody ProtocolAutoDiscoveryManager.DiscoveryRequest request) {
        return protocolDiscoveryService.startDiscovery(request);
    }

    @Operation(summary = "停止发现任务", description = "停止正在运行的发现任务")
    @PostMapping("/stop/{taskId}")
    public ResponseDTO<Void> stopDiscovery(
            @Parameter(description = "任务ID", required = true)
            @PathVariable @NotBlank String taskId) {
        return protocolDiscoveryService.stopDiscovery(taskId);
    }

    @Operation(summary = "获取发现任务状态", description = "查询指定发现任务的当前状态")
    @GetMapping("/status/{taskId}")
    public ResponseDTO<ProtocolAutoDiscoveryManager.DiscoveryTask> getDiscoveryStatus(
            @Parameter(description = "任务ID", required = true)
            @PathVariable @NotBlank String taskId) {
        return protocolDiscoveryService.getDiscoveryStatus(taskId);
    }

    @Operation(summary = "获取发现结果", description = "获取指定发现任务的扫描结果")
    @GetMapping("/result/{taskId}")
    public ResponseDTO<ProtocolAutoDiscoveryManager.DiscoveryResult> getDiscoveryResult(
            @Parameter(description = "任务ID", required = true)
            @PathVariable @NotBlank String taskId) {
        return protocolDiscoveryService.getDiscoveryResult(taskId);
    }

    @Operation(summary = "获取所有发现任务", description = "获取所有发现任务的列表")
    @GetMapping("/tasks")
    public ResponseDTO<List<ProtocolAutoDiscoveryManager.DiscoveryTask>> getAllDiscoveryTasks() {
        return protocolDiscoveryService.getAllDiscoveryTasks();
    }

    @Operation(summary = "扫描单个设备", description = "对指定IP地址的设备进行协议检测")
    @PostMapping("/scan/single")
    public ResponseDTO<ProtocolAutoDiscoveryManager.DiscoveryResult> scanSingleDevice(
            @Parameter(description = "设备IP地址", required = true)
            @RequestParam @NotBlank String ipAddress,
            @Parameter(description = "扫描超时时间（毫秒）")
            @RequestParam(defaultValue = "3000") @Min(1000) @Max(60000) int timeout) {
        return protocolDiscoveryService.scanSingleDevice(ipAddress, timeout);
    }

    @Operation(summary = "批量扫描设备", description = "对多个IP地址的设备进行并发扫描")
    @PostMapping("/scan/batch")
    public ResponseDTO<Map<String, ProtocolAutoDiscoveryManager.DiscoveryResult>> batchScanDevices(
            @Parameter(description = "IP地址列表", required = true)
            @RequestParam @NotNull List<String> ipAddresses,
            @Parameter(description = "扫描超时时间（毫秒）")
            @RequestParam(defaultValue = "3000") @Min(1000) @Max(60000) int timeout) {
        return protocolDiscoveryService.batchScanDevices(ipAddresses, timeout);
    }

    @Operation(summary = "自动注册设备", description = "将发现的设备自动注册到系统中")
    @PostMapping("/register")
    public ResponseDTO<List<DeviceEntity>> autoRegisterDevices(
            @Parameter(description = "发现结果", required = true)
            @NotNull ProtocolAutoDiscoveryManager.DiscoveryResult discoveryResult,
            @Parameter(description = "是否自动注册")
            @RequestParam(defaultValue = "true") boolean autoRegister) {
        return protocolDiscoveryService.autoRegisterDevices(discoveryResult, autoRegister);
    }

    @Operation(summary = "获取发现统计", description = "获取协议自动发现的统计信息")
    @GetMapping("/statistics")
    public ResponseDTO<ProtocolAutoDiscoveryManager.DiscoveryStatistics> getDiscoveryStatistics() {
        return protocolDiscoveryService.getDiscoveryStatistics();
    }

    @Operation(summary = "更新协议指纹", description = "更新协议自动发现的指纹库")
    @PostMapping("/fingerprints")
    public ResponseDTO<Void> updateProtocolFingerprints(
            @Parameter(description = "协议指纹映射", required = true)
            @RequestBody @NotNull Map<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprints) {
        return protocolDiscoveryService.updateProtocolFingerprints(fingerprints);
    }

    @Operation(summary = "获取支持的协议", description = "获取系统支持的协议类型列表")
    @GetMapping("/protocols")
    public ResponseDTO<List<String>> getSupportedProtocols() {
        return protocolDiscoveryService.getSupportedProtocols();
    }

    @Operation(summary = "检测设备协议", description = "检测指定设备的协议类型")
    @PostMapping("/detect")
    public ResponseDTO<String> detectDeviceProtocol(
            @Parameter(description = "设备IP地址", required = true)
            @RequestParam @NotBlank String ipAddress,
            @Parameter(description = "开放端口信息", required = true)
            @RequestBody @NotNull Map<Integer, String> openPorts,
            @Parameter(description = "检测超时时间（毫秒）")
            @RequestParam(defaultValue = "3000") @Min(1000) @Max(30000) int timeout) {
        return protocolDiscoveryService.detectDeviceProtocol(ipAddress, openPorts, timeout);
    }

    @Operation(summary = "验证设备连接", description = "验证指定设备的连接状态")
    @GetMapping("/validate/{deviceId}")
    public ResponseDTO<Boolean> validateDeviceConnection(
            @Parameter(description = "设备ID", required = true)
            @PathVariable @NotNull Long deviceId) {
        return protocolDiscoveryService.validateDeviceConnection(deviceId);
    }

    @Operation(summary = "获取发现历史", description = "获取设备发现的历史记录")
    @GetMapping("/history")
    public ResponseDTO<List<ProtocolAutoDiscoveryManager.DiscoveryResult>> getDiscoveryHistory(
            @Parameter(description = "返回记录数量限制")
            @RequestParam(defaultValue = "100") @Min(1) @Max(1000) int limit) {
        return protocolDiscoveryService.getDiscoveryHistory(limit);
    }

    @Operation(summary = "清理过期任务", description = "清理过期的发现任务数据")
    @PostMapping("/cleanup")
    public ResponseDTO<Integer> cleanupExpiredTasks(
            @Parameter(description = "最大保存时间（小时）")
            @RequestParam(defaultValue = "168") @Min(1) @Max(720) int maxAgeHours) {
        return protocolDiscoveryService.cleanupExpiredTasks(maxAgeHours);
    }

    @Operation(summary = "导出发现结果", description = "导出发现结果到指定格式")
    @GetMapping("/export/{taskId}")
    public ResponseDTO<String> exportDiscoveryResult(
            @Parameter(description = "任务ID", required = true)
            @PathVariable @NotBlank String taskId,
            @Parameter(description = "导出格式", required = true)
            @RequestParam @NotBlank String format) {
        return protocolDiscoveryService.exportDiscoveryResult(taskId, format);
    }

    // ==================== 设备发现快捷接口 ====================

    @Operation(summary = "快速发现设备", description = "使用默认参数快速发现指定网络范围的设备")
    @PostMapping("/quick-discover")
    public ResponseDTO<String> quickDiscover(
            @Parameter(description = "网络范围", required = true)
            @RequestParam @NotBlank String networkRange) {

        ProtocolAutoDiscoveryManager.DiscoveryRequest request = new ProtocolAutoDiscoveryManager.DiscoveryRequest();
        request.setNetworkRange(networkRange);
        request.setTimeout(5000);
        request.setMaxConcurrentTasks(10);
        request.setAutoRegister(false);
        request.setEnablePingScan(true);
        request.setEnablePortScan(true);
        request.setEnableProtocolDetection(true);

        return protocolDiscoveryService.startDiscovery(request);
    }

    @Operation(summary = "快速扫描局域网", description = "快速扫描局域网内的设备")
    @PostMapping("/quick-scan")
    public ResponseDTO<ProtocolAutoDiscoveryManager.DiscoveryResult> quickScanNetwork() {

        // 自动检测本地网络段
        String networkRange = detectLocalNetworkRange();

        return protocolDiscoveryService.scanSingleDevice(networkRange, 5000);
    }

    // ==================== 监控和管理接口 ====================

    @Operation(summary = "获取系统状态", description = "获取协议发现服务的系统状态")
    @GetMapping("/system/status")
    public ResponseDTO<Map<String, Object>> getSystemStatus() {
        Map<String, Object> status = new java.util.HashMap<>();

        // 获取统计信息
        ResponseDTO<ProtocolAutoDiscoveryManager.DiscoveryStatistics> statsResponse =
                protocolDiscoveryService.getDiscoveryStatistics();
        status.put("statistics", statsResponse.getData());

        // 获取支持的协议
        ResponseDTO<List<String>> protocolsResponse = protocolDiscoveryService.getSupportedProtocols();
        status.put("supportedProtocols", protocolsResponse.getData());

        // 系统健康状态
        status.put("healthy", true);
        status.put("timestamp", System.currentTimeMillis());

        return ResponseDTO.ok(status);
    }

    @Operation(summary = "重置发现服务", description = "重置协议发现服务的状态")
    @PostMapping("/reset")
    public ResponseDTO<Void> resetService() {
        // 清理所有任务
        protocolDiscoveryService.cleanupExpiredTasks(0);

        return ResponseDTO.ok();
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 自动检测本地网络范围
     */
    private String detectLocalNetworkRange() {
        try {
            // 获取本机IP地址并推导网络范围
            // 这里简化实现，实际应该通过网络接口获取
            return "192.168.1.0/24";
        } catch (Exception e) {
            log.warn("[协议发现控制器] 自动检测网络范围失败，使用默认值", e);
            return "192.168.1.0/24";
        }
    }
}
