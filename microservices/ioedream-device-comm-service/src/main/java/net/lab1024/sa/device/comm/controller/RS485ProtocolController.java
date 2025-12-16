package net.lab1024.sa.device.comm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.response.ResponseDTO;
import net.lab1024.sa.device.comm.service.RS485ProtocolService;
import net.lab1024.sa.device.comm.service.impl.RS485ProtocolServiceImpl;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * RS485协议控制器
 * <p>
 * 严格遵循四层架构规范的Controller层实现：
 * - 位于Controller层，负责HTTP接口暴露和参数验证
 * - 使用@Resource注解进行依赖注入
 * - 使用@Valid进行参数验证
 * - 调用Service层处理业务逻辑
 * - 统一异常处理和响应封装
 * </p>
 * <p>
 * 提供RS485工业设备协议的RESTful API：
 * 1. 设备初始化和配置管理
 * 2. 设备消息处理和响应构建
 * 3. 设备状态监控和心跳管理
 * 4. 性能统计和设备管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/rs485")
@Tag(name = "RS485协议管理", description = "RS485工业设备协议管理接口")
public class RS485ProtocolController {

    @Resource
    private RS485ProtocolService rs485ProtocolService;

    @Operation(summary = "初始化RS485设备", description = "初始化RS485工业设备连接和配置")
    @PostMapping("/device/{deviceId}/initialize")
    public ResponseDTO<RS485ProtocolServiceImpl.RS485InitResultVO> initializeDevice(
            @Parameter(description = "设备ID", required = true) @PathVariable Long deviceId,
            @Parameter(description = "设备信息", required = true) @RequestBody Map<String, Object> deviceInfo,
            @Parameter(description = "配置参数", required = true) @RequestBody Map<String, Object> config) {
        return rs485ProtocolService.initializeDevice(deviceId, deviceInfo, config);
    }

    @Operation(summary = "处理RS485设备消息", description = "处理来自RS485设备的原始协议消息")
    @PostMapping("/device/{deviceId}/message")
    public ResponseDTO<RS485ProtocolServiceImpl.RS485ProcessResultVO> processDeviceMessage(
            @Parameter(description = "设备ID", required = true) @PathVariable Long deviceId,
            @Parameter(description = "协议类型", required = true) @RequestParam String protocolType,
            @Parameter(description = "原始消息数据", required = true) @RequestBody byte[] rawData) {
        return rs485ProtocolService.processDeviceMessage(deviceId, rawData, protocolType);
    }

    @Operation(summary = "处理RS485设备心跳", description = "处理RS485设备心跳消息")
    @PostMapping("/device/{deviceId}/heartbeat")
    public ResponseDTO<RS485ProtocolServiceImpl.RS485HeartbeatResultVO> processDeviceHeartbeat(
            @Parameter(description = "设备ID", required = true) @PathVariable Long deviceId,
            @Parameter(description = "心跳数据", required = true) @RequestBody Map<String, Object> heartbeatData) {
        return rs485ProtocolService.processDeviceHeartbeat(deviceId, heartbeatData);
    }

    @Operation(summary = "构建设备响应", description = "为RS485设备构建响应消息")
    @PostMapping("/device/{deviceId}/response")
    public ResponseDTO<byte[]> buildDeviceResponse(
            @Parameter(description = "设备ID", required = true) @PathVariable Long deviceId,
            @Parameter(description = "消息类型", required = true) @RequestParam String messageType,
            @Parameter(description = "业务数据", required = true) @RequestBody Map<String, Object> businessData) {
        return rs485ProtocolService.buildDeviceResponse(deviceId, messageType, businessData);
    }

    @Operation(summary = "获取设备状态", description = "获取RS485设备的当前状态信息")
    @GetMapping("/device/{deviceId}/status")
    public ResponseDTO<RS485ProtocolServiceImpl.RS485DeviceStatusVO> getDeviceStatus(
            @Parameter(description = "设备ID", required = true) @PathVariable Long deviceId) {
        return rs485ProtocolService.getDeviceStatus(deviceId);
    }

    @Operation(summary = "断开设备连接", description = "断开与RS485设备的连接")
    @PostMapping("/device/{deviceId}/disconnect")
    public ResponseDTO<Boolean> disconnectDevice(
            @Parameter(description = "设备ID", required = true) @PathVariable Long deviceId) {
        return rs485ProtocolService.disconnectDevice(deviceId);
    }

    @Operation(summary = "获取性能统计", description = "获取RS485协议处理的性能统计信息")
    @GetMapping("/statistics")
    public ResponseDTO<Map<String, Object>> getPerformanceStatistics() {
        return rs485ProtocolService.getPerformanceStatistics();
    }

    @Operation(summary = "获取支持的设备型号", description = "获取RS485协议支持的所有设备型号")
    @GetMapping("/supported-models")
    public ResponseDTO<String[]> getSupportedDeviceModels() {
        return rs485ProtocolService.getSupportedDeviceModels();
    }

    @Operation(summary = "检查设备型号支持", description = "检查指定设备型号是否被RS485协议支持")
    @GetMapping("/supported-models/check")
    public ResponseDTO<Boolean> isDeviceModelSupported(
            @Parameter(description = "设备型号", required = true) @RequestParam String deviceModel) {
        return rs485ProtocolService.isDeviceModelSupported(deviceModel);
    }

    @Operation(summary = "批量初始化设备", description = "批量初始化多个RS485设备")
    @PostMapping("/devices/batch-initialize")
    public ResponseDTO<Map<String, RS485ProtocolServiceImpl.RS485InitResultVO>> batchInitializeDevices(
            @Parameter(description = "批量设备配置", required = true) @RequestBody BatchDeviceConfig config) {
        Map<String, RS485ProtocolServiceImpl.RS485InitResultVO> results = new HashMap<>();

        for (BatchDeviceConfig.DeviceConfig deviceConfig : config.getDevices()) {
            try {
                ResponseDTO<RS485ProtocolServiceImpl.RS485InitResultVO> result = rs485ProtocolService.initializeDevice(
                        deviceConfig.getDeviceId(),
                        deviceConfig.getDeviceInfo(),
                        deviceConfig.getConfig()
                );
                results.put(deviceConfig.getDeviceId().toString(), result.getData());
            } catch (Exception e) {
                log.error("[RS485控制器] 批量初始化设备失败, deviceId={}", deviceConfig.getDeviceId(), e);
                RS485ProtocolServiceImpl.RS485InitResultVO errorResult = new RS485ProtocolServiceImpl.RS485InitResultVO();
                errorResult.setSuccess(false);
                errorResult.setMessage("初始化失败: " + e.getMessage());
                results.put(deviceConfig.getDeviceId().toString(), errorResult);
            }
        }

        return ResponseDTO.ok(results);
    }

    @Operation(summary = "批量断开设备", description = "批量断开多个RS485设备连接")
    @PostMapping("/devices/batch-disconnect")
    public ResponseDTO<Map<String, Boolean>> batchDisconnectDevices(
            @Parameter(description = "设备ID列表", required = true) @RequestBody BatchDisconnectRequest request) {
        Map<String, Boolean> results = new HashMap<>();

        for (Long deviceId : request.getDeviceIds()) {
            try {
                ResponseDTO<Boolean> result = rs485ProtocolService.disconnectDevice(deviceId);
                results.put(deviceId.toString(), result.getData());
            } catch (Exception e) {
                log.error("[RS485控制器] 批量断开设备失败, deviceId={}", deviceId, e);
                results.put(deviceId.toString(), false);
            }
        }

        return ResponseDTO.ok(results);
    }

    @Operation(summary = "获取所有设备状态", description = "获取所有已连接RS485设备的状态信息")
    @GetMapping("/devices/status")
    public ResponseDTO<Map<String, Object>> getAllDeviceStatus() {
        Map<String, Object> result = new HashMap<>();

        try {
            Map<String, Object> statistics = rs485ProtocolService.getPerformanceStatistics().getData();
            if (statistics != null) {
                result.put("statistics", statistics);
            }
        } catch (Exception e) {
            log.error("[RS485控制器] 获取性能统计失败", e);
            result.put("statistics_error", e.getMessage());
        }

        return ResponseDTO.ok(result);
    }

    @Operation(summary = "重置设备连接", description = "重置指定设备的连接状态")
    @PostMapping("/device/{deviceId}/reset")
    public ResponseDTO<Boolean> resetDeviceConnection(
            @Parameter(description = "设备ID", required = true) @PathVariable Long deviceId) {
        try {
            // 先断开连接
            ResponseDTO<Boolean> disconnectResult = rs485ProtocolService.disconnectDevice(deviceId);

            if (disconnectResult.getData() != null && disconnectResult.getData()) {
                log.info("[RS485控制器] 设备重置成功, deviceId={}", deviceId);
                return ResponseDTO.ok(true);
            } else {
                log.warn("[RS485控制器] 设备断开失败, deviceId={}", deviceId);
                return ResponseDTO.error("RESET_FAILED", "设备重置失败");
            }
        } catch (Exception e) {
            log.error("[RS485控制器] 重置设备连接异常, deviceId={}", deviceId, e);
            return ResponseDTO.error("SYSTEM_ERROR", "设备重置失败: " + e.getMessage());
        }
    }

    // ==================== 内部类 ====================

    /**
     * 批量设备配置
     */
    @Schema(description = "批量设备配置")
    public static class BatchDeviceConfig {
        @Schema(description = "设备配置列表")
        private java.util.List<DeviceConfig> devices;

        // getters and setters
        public java.util.List<DeviceConfig> getDevices() { return devices; }
        public void setDevices(java.util.List<DeviceConfig> devices) { this.devices = devices; }

        @Schema(description = "单个设备配置")
        public static class DeviceConfig {
            @Schema(description = "设备ID")
            private Long deviceId;

            @Schema(description = "设备信息")
            private Map<String, Object> deviceInfo;

            @Schema(description = "配置参数")
            private Map<String, Object> config;

            // getters and setters
            public Long getDeviceId() { return deviceId; }
            public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }
            public Map<String, Object> getDeviceInfo() { return deviceInfo; }
            public void setDeviceInfo(Map<String, Object> deviceInfo) { this.deviceInfo = deviceInfo; }
            public Map<String, Object> getConfig() { return config; }
            public void setConfig(Map<String, Object> config) { this.config = config; }
        }
    }

    /**
     * 批量断开请求
     */
    @Schema(description = "批量断开请求")
    public static class BatchDisconnectRequest {
        @Schema(description = "设备ID列表")
        private java.util.List<Long> deviceIds;

        // getters and setters
        public java.util.List<Long> getDeviceIds() { return deviceIds; }
        public void setDeviceIds(java.util.List<Long> deviceIds) { this.deviceIds = deviceIds; }
    }
}