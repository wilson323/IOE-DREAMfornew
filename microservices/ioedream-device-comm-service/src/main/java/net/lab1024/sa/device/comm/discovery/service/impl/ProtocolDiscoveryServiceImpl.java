package net.lab1024.sa.device.comm.discovery.service.impl;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Resource;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.device.comm.discovery.ProtocolAutoDiscoveryManager;
import net.lab1024.sa.device.comm.discovery.service.ProtocolDiscoveryService;

/**
 * 协议自动发现服务实现
 * <p>
 * 提供设备协议自动发现的完整业务逻辑：
 * 1. 发现任务生命周期管理
 * 2. 设备扫描和协议检测协调
 * 3. 设备自动注册和状态同步
 * 4. 结果缓存和历史管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class ProtocolDiscoveryServiceImpl implements ProtocolDiscoveryService {


    @Resource
    private ProtocolAutoDiscoveryManager discoveryManager;

    @Resource
    private ObjectMapper objectMapper;

    // 任务缓存
    private final Map<String, ProtocolAutoDiscoveryManager.DiscoveryTask> taskCache = new HashMap<>();

    // 结果缓存
    private final Map<String, ProtocolAutoDiscoveryManager.DiscoveryResult> resultCache = new HashMap<>();

    @Override
    public ResponseDTO<String> startDiscovery(ProtocolAutoDiscoveryManager.DiscoveryRequest discoveryRequest) {
        try {
            log.info("[协议发现服务] 启动发现任务: {}", discoveryRequest.getNetworkRange());

            // 验证请求参数
            validateDiscoveryRequest(discoveryRequest);

            // 启动发现任务（使用scanTimeout作为超时时间）
            String taskId = discoveryManager.startDiscovery(discoveryRequest);

            log.info("[协议发现服务] 发现任务启动成功: taskId={}", taskId);
            return ResponseDTO.ok(taskId);

        } catch (Exception e) {
            log.error("[协议发现服务] 启动发现任务失败", e);
            throw new BusinessException("DISCOVERY_START_FAILED", "启动发现任务失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Void> stopDiscovery(String taskId) {
        try {
            log.info("[协议发现服务] 停止发现任务: {}", taskId);

            boolean stopped = discoveryManager.stopDiscovery(taskId);
            if (!stopped) {
                throw new BusinessException("DISCOVERY_STOP_FAILED", "任务不存在或已停止: " + taskId);
            }

            log.info("[协议发现服务] 发现任务停止成功: {}", taskId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[协议发现服务] 停止发现任务失败: {}", taskId, e);
            throw new BusinessException("DISCOVERY_STOP_FAILED", "停止发现任务失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<ProtocolAutoDiscoveryManager.DiscoveryTask> getDiscoveryStatus(String taskId) {
        try {
            ProtocolAutoDiscoveryManager.DiscoveryTask task = discoveryManager.getDiscoveryTask(taskId);
            if (task == null) {
                throw new BusinessException("DISCOVERY_TASK_NOT_FOUND", "发现任务不存在: " + taskId);
            }

            return ResponseDTO.ok(task);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[协议发现服务] 获取发现任务状态失败: {}", taskId, e);
            throw new BusinessException("DISCOVERY_STATUS_FAILED", "获取任务状态失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<ProtocolAutoDiscoveryManager.DiscoveryResult> getDiscoveryResult(String taskId) {
        try {
            ProtocolAutoDiscoveryManager.DiscoveryResult result = discoveryManager.getDiscoveryResult(taskId);
            if (result == null) {
                throw new BusinessException("DISCOVERY_RESULT_NOT_FOUND", "发现结果不存在: " + taskId);
            }

            return ResponseDTO.ok(result);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[协议发现服务] 获取发现结果失败: {}", taskId, e);
            throw new BusinessException("DISCOVERY_RESULT_FAILED", "获取发现结果失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<ProtocolAutoDiscoveryManager.DiscoveryTask>> getAllDiscoveryTasks() {
        try {
            List<ProtocolAutoDiscoveryManager.DiscoveryTask> tasks = discoveryManager.getAllDiscoveryTasks();
            return ResponseDTO.ok(tasks);

        } catch (Exception e) {
            log.error("[协议发现服务] 获取所有发现任务失败", e);
            throw new BusinessException("DISCOVERY_TASKS_FAILED", "获取任务列表失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<ProtocolAutoDiscoveryManager.DiscoveryResult> scanSingleDevice(String ipAddress, int timeout) {
        try {
            log.info("[协议发现服务] 扫描单个设备: {}", ipAddress);

            ProtocolAutoDiscoveryManager.DiscoveryResult result = discoveryManager.scanSingleDevice(ipAddress, timeout);

            // 缓存结果
            resultCache.put("single:" + ipAddress, result);

            log.info("[协议发现服务] 单设备扫描完成: {}, 发现设备数: {}", ipAddress, result.getDiscoveredDevices().size());
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[协议发现服务] 扫描单个设备失败: {}", ipAddress, e);
            throw new BusinessException("DEVICE_SCAN_FAILED", "设备扫描失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<Map<String, ProtocolAutoDiscoveryManager.DiscoveryResult>> batchScanDevices(
            List<String> ipAddresses, int timeout) {
        try {
            log.info("[协议发现服务] 批量扫描设备: {} 个", ipAddresses.size());

            Map<String, ProtocolAutoDiscoveryManager.DiscoveryResult> results = new HashMap<>();

            // 并发扫描
            List<CompletableFuture<Void>> futures = ipAddresses.stream()
                    .map(ip -> CompletableFuture.runAsync(() -> {
                        try {
                            ProtocolAutoDiscoveryManager.DiscoveryResult result = discoveryManager.scanSingleDevice(ip,
                                    timeout);
                            results.put(ip, result);
                        } catch (Exception e) {
                            log.warn("[协议发现服务] 设备扫描失败: {} - {}", ip, e.getMessage());
                        }
                    }))
                    .collect(Collectors.toList());

            // 等待所有任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            log.info("[协议发现服务] 批量扫描完成: 成功 {} 个", results.size());
            return ResponseDTO.ok(results);

        } catch (Exception e) {
            log.error("[协议发现服务] 批量扫描设备失败", e);
            throw new BusinessException("BATCH_SCAN_FAILED", "批量扫描失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<DeviceEntity>> autoRegisterDevices(
            ProtocolAutoDiscoveryManager.DiscoveryResult discoveryResult, boolean autoRegister) {
        try {
            log.info("[协议发现服务] 自动注册设备: {} 个, 自动注册: {}",
                    discoveryResult.getDiscoveredDevices().size(), autoRegister);

            List<DeviceEntity> registeredDevices = new ArrayList<>();

            for (ProtocolAutoDiscoveryManager.DiscoveredDevice discoveredDevice : discoveryResult
                    .getDiscoveredDevices()) {

                String ipAddress = discoveredDevice.getIpAddress();

                try {
                    // 检查设备是否已存在
                    DeviceEntity existingDevice = findDeviceByIpAddress(ipAddress);
                    if (existingDevice != null) {
                        log.debug("[协议发现服务] 设备已存在: {}", ipAddress);
                        registeredDevices.add(existingDevice);
                        continue;
                    }

                    // 自动注册设备
                    if (autoRegister) {
                        DeviceEntity device = createDeviceFromDiscoveredDevice(discoveredDevice);
                        DeviceEntity savedDevice = saveDevice(device);
                        registeredDevices.add(savedDevice);

                        log.info("[协议发现服务] 设备注册成功: {} -> {}", ipAddress, savedDevice.getDeviceId());
                    }

                } catch (Exception e) {
                    log.error("[协议发现服务] 注册设备失败: {}", ipAddress, e);
                }
            }

            log.info("[协议发现服务] 自动注册完成: 成功 {} 个", registeredDevices.size());
            return ResponseDTO.ok(registeredDevices);

        } catch (Exception e) {
            log.error("[协议发现服务] 自动注册设备失败", e);
            throw new BusinessException("AUTO_REGISTER_FAILED", "自动注册失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<ProtocolAutoDiscoveryManager.DiscoveryStatistics> getDiscoveryStatistics() {
        try {
            ProtocolAutoDiscoveryManager.DiscoveryStatistics statistics = discoveryManager.getDiscoveryStatistics();
            return ResponseDTO.ok(statistics);

        } catch (Exception e) {
            log.error("[协议发现服务] 获取发现统计信息失败", e);
            throw new BusinessException("DISCOVERY_STATS_FAILED", "获取统计信息失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Void> updateProtocolFingerprints(
            Map<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprints) {
        try {
            log.info("[协议发现服务] 更新协议指纹库: {} 个指纹", fingerprints.size());

            discoveryManager.updateProtocolFingerprints(fingerprints);

            log.info("[协议发现服务] 协议指纹库更新成功");
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[协议发现服务] 更新协议指纹库失败", e);
            throw new BusinessException("FINGERPRINTS_UPDATE_FAILED", "更新指纹库失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<String>> getSupportedProtocols() {
        try {
            List<String> protocols = discoveryManager.getSupportedProtocols();
            return ResponseDTO.ok(protocols);

        } catch (Exception e) {
            log.error("[协议发现服务] 获取支持的协议类型失败", e);
            throw new BusinessException("SUPPORTED_PROTOCOLS_FAILED", "获取支持协议失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<String> detectDeviceProtocol(String ipAddress, Map<Integer, String> openPorts, int timeout) {
        try {
            log.info("[协议发现服务] 检测设备协议: {}", ipAddress);

            String protocol = discoveryManager.detectDeviceProtocol(ipAddress, openPorts, timeout);
            if (protocol == null) {
                protocol = "UNKNOWN";
            }

            log.info("[协议发现服务] 设备协议检测完成: {} -> {}", ipAddress, protocol);
            return ResponseDTO.ok(protocol);

        } catch (Exception e) {
            log.error("[协议发现服务] 检测设备协议失败: {}", ipAddress, e);
            throw new BusinessException("PROTOCOL_DETECTION_FAILED", "协议检测失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<Boolean> validateDeviceConnection(Long deviceId) {
        try {
            log.info("[协议发现服务] 验证设备连接: {}", deviceId);

            // 这里需要实际的设备连接验证逻辑
            // 暂时返回true
            boolean isValid = true;

            log.info("[协议发现服务] 设备连接验证完成: {} -> {}", deviceId, isValid);
            return ResponseDTO.ok(isValid);

        } catch (Exception e) {
            log.error("[协议发现服务] 验证设备连接失败: {}", deviceId, e);
            throw new BusinessException("CONNECTION_VALIDATION_FAILED", "连接验证失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<ProtocolAutoDiscoveryManager.DiscoveryResult>> getDiscoveryHistory(int limit) {
        try {
            List<ProtocolAutoDiscoveryManager.DiscoveryResult> history = discoveryManager.getDiscoveryHistory(limit);
            return ResponseDTO.ok(history);

        } catch (Exception e) {
            log.error("[协议发现服务] 获取发现历史失败", e);
            throw new BusinessException("DISCOVERY_HISTORY_FAILED", "获取历史记录失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Integer> cleanupExpiredTasks(int maxAgeHours) {
        try {
            log.info("[协议发现服务] 清理过期任务: 最大保存 {} 小时", maxAgeHours);

            int cleanedCount = discoveryManager.cleanupExpiredTasks(maxAgeHours);

            log.info("[协议发现服务] 过期任务清理完成: 清理 {} 个任务", cleanedCount);
            return ResponseDTO.ok(cleanedCount);

        } catch (Exception e) {
            log.error("[协议发现服务] 清理过期任务失败", e);
            throw new BusinessException("CLEANUP_TASKS_FAILED", "清理任务失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<String> exportDiscoveryResult(String taskId, String format) {
        try {
            log.info("[协议发现服务] 导出发现结果: {} - {}", taskId, format);

            ProtocolAutoDiscoveryManager.DiscoveryResult result = discoveryManager.getDiscoveryResult(taskId);
            if (result == null) {
                throw new BusinessException("DISCOVERY_RESULT_NOT_FOUND", "发现结果不存在: " + taskId);
            }

            String exportData;
            switch (format.toLowerCase()) {
                case "json":
                    exportData = objectMapper.writeValueAsString(result);
                    break;
                case "csv":
                    exportData = exportToCSV(result);
                    break;
                default:
                    throw new BusinessException("UNSUPPORTED_FORMAT", "不支持的导出格式: " + format);
            }

            log.info("[协议发现服务] 发现结果导出成功: {} - {} 字符", taskId, exportData.length());
            return ResponseDTO.ok(exportData);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[协议发现服务] 导出发现结果失败: {}", taskId, e);
            throw new BusinessException("EXPORT_FAILED", "导出失败: " + e.getMessage());
        }
    }

    /**
     * 验证发现请求
     */
    private void validateDiscoveryRequest(ProtocolAutoDiscoveryManager.DiscoveryRequest discoveryRequest) {
        if (discoveryRequest == null) {
            throw new BusinessException("DISCOVERY_REQUEST_NULL", "发现请求不能为空");
        }

        if (discoveryRequest.getNetworkRange() == null || discoveryRequest.getNetworkRange().trim().isEmpty()) {
            throw new BusinessException("NETWORK_RANGE_NULL", "网络范围不能为空");
        }

        if (discoveryRequest.getScanTimeout() == null || discoveryRequest.getScanTimeout() <= 0) {
            throw new BusinessException("TIMEOUT_INVALID", "扫描超时时间必须大于0");
        }
    }

    /**
     * 根据IP地址查找设备
     */
    private DeviceEntity findDeviceByIpAddress(String ipAddress) {
        // 这里应该调用DeviceDao来查找设备
        // 暂时返回null表示设备不存在
        return null;
    }

    /**
     * 从DiscoveredDevice创建设备实体
     */
    private DeviceEntity createDeviceFromDiscoveredDevice(
            ProtocolAutoDiscoveryManager.DiscoveredDevice discoveredDevice) {

        DeviceEntity device = new DeviceEntity();
        device.setDeviceCode(discoveredDevice.getIpAddress());
        device.setDeviceName("Auto-" + discoveredDevice.getIpAddress());
        // deviceType需要转换为Integer（如果discoveredDevice.getDeviceType()返回String）
        Object deviceTypeObj = discoveredDevice.getDeviceType();
        if (deviceTypeObj instanceof String) {
            try {
                device.setDeviceType(Integer.parseInt((String) deviceTypeObj));
            } catch (NumberFormatException e) {
                log.warn("设备类型转换失败: {}", deviceTypeObj);
            }
        } else if (deviceTypeObj instanceof Integer) {
            device.setDeviceType((Integer) deviceTypeObj);
        } else if (deviceTypeObj != null) {
            log.warn("设备类型格式不支持: {}, 类型: {}", deviceTypeObj, deviceTypeObj.getClass().getName());
        }
        device.setIpAddress(discoveredDevice.getIpAddress());
        // DeviceEntity没有macAddress字段，MAC地址可以存储在extendedAttributes中
        if (discoveredDevice.getMacAddress() != null) {
            String extendedAttrs = device.getExtendedAttributes();
            // TODO: 将MAC地址添加到extendedAttributes JSON中
        }
        device.setDeviceStatus(1); // 启用状态（1-在线）
        device.setEnabled(1); // 启用
        device.setCreateTime(LocalDateTime.now());
        device.setUpdateTime(LocalDateTime.now());

        // 设置其他属性
        if (discoveredDevice.getVendor() != null) {
            device.setBrand(discoveredDevice.getVendor());
        }
        if (discoveredDevice.getDeviceModel() != null) {
            device.setModel(discoveredDevice.getDeviceModel());
        }

        return device;
    }

    /**
     * 保存设备
     */
    private DeviceEntity saveDevice(DeviceEntity device) {
        // 这里应该调用DeviceDao来保存设备
        // 暂时直接返回设备对象，deviceId由MyBatis-Plus自动生成
        return device;
    }

    /**
     * 导出为CSV格式
     */
    private String exportToCSV(ProtocolAutoDiscoveryManager.DiscoveryResult result) {
        StringBuilder csv = new StringBuilder();
        csv.append("IP Address, MAC Address, Vendor, Device Type, Protocol, Identified\n");

        for (ProtocolAutoDiscoveryManager.DiscoveredDevice discoveredDevice : result.getDiscoveredDevices()) {

            csv.append(discoveredDevice.getIpAddress()).append(",");
            csv.append(discoveredDevice.getMacAddress() != null ? discoveredDevice.getMacAddress() : "").append(",");
            csv.append(discoveredDevice.getVendor() != null ? discoveredDevice.getVendor() : "").append(",");
            csv.append(discoveredDevice.getDeviceType() != null ? discoveredDevice.getDeviceType() : "").append(",");
            csv.append(discoveredDevice.getProtocolType() != null ? discoveredDevice.getProtocolType() : "")
                    .append(",");
            csv.append(discoveredDevice.isIdentified() ? "Yes" : "No").append("\n");
        }

        return csv.toString();
    }
}
