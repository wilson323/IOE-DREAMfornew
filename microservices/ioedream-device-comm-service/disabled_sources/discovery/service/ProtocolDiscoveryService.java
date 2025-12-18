package net.lab1024.sa.device.comm.discovery.service;

import net.lab1024.sa.device.comm.discovery.ProtocolAutoDiscoveryManager;
import net.lab1024.sa.device.comm.discovery.scanner.NetworkScanner;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.organization.entity.DeviceEntity;

import java.util.List;
import java.util.Map;

/**
 * 协议自动发现服务接口
 * <p>
 * 提供设备协议自动发现的核心功能：
 * 1. 网络设备扫描和发现
 * 2. 协议自动识别和检测
 * 3. 设备自动注册和管理
 * 4. 发现任务管理和监控
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface ProtocolDiscoveryService {

    /**
     * 启动协议发现任务
     *
     * @param discoveryRequest 发现请求
     * @return 发现任务ID
     */
    ResponseDTO<String> startDiscovery(ProtocolAutoDiscoveryManager.DiscoveryRequest discoveryRequest);

    /**
     * 停止发现任务
     *
     * @param taskId 任务ID
     * @return 操作结果
     */
    ResponseDTO<Void> stopDiscovery(String taskId);

    /**
     * 获取发现任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态
     */
    ResponseDTO<ProtocolAutoDiscoveryManager.DiscoveryTask> getDiscoveryStatus(String taskId);

    /**
     * 获取发现任务结果
     *
     * @param taskId 任务ID
     * @return 发现结果
     */
    ResponseDTO<ProtocolAutoDiscoveryManager.DiscoveryResult> getDiscoveryResult(String taskId);

    /**
     * 获取所有发现任务
     *
     * @return 任务列表
     */
    ResponseDTO<List<ProtocolAutoDiscoveryManager.DiscoveryTask>> getAllDiscoveryTasks();

    /**
     * 扫描单个设备
     *
     * @param ipAddress 设备IP地址
     * @param timeout 扫描超时时间
     * @return 发现结果
     */
    ResponseDTO<ProtocolAutoDiscoveryManager.DiscoveryResult> scanSingleDevice(String ipAddress, int timeout);

    /**
     * 批量扫描设备
     *
     * @param ipAddresses IP地址列表
     * @param timeout 扫描超时时间
     * @return 批量发现结果
     */
    ResponseDTO<Map<String, ProtocolAutoDiscoveryManager.DiscoveryResult>> batchScanDevices(
            List<String> ipAddresses, int timeout);

    /**
     * 自动注册发现的设备
     *
     * @param discoveryResult 发现结果
     * @param autoRegister 是否自动注册
     * @return 注册结果
     */
    ResponseDTO<List<DeviceEntity>> autoRegisterDevices(
            ProtocolAutoDiscoveryManager.DiscoveryResult discoveryResult, boolean autoRegister);

    /**
     * 获取发现统计信息
     *
     * @return 统计信息
     */
    ResponseDTO<ProtocolAutoDiscoveryManager.DiscoveryStatistics> getDiscoveryStatistics();

    /**
     * 更新协议指纹库
     *
     * @param fingerprints 协议指纹
     * @return 更新结果
     */
    ResponseDTO<Void> updateProtocolFingerprints(Map<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprints);

    /**
     * 获取支持的协议类型
     *
     * @return 协议类型列表
     */
    ResponseDTO<List<String>> getSupportedProtocols();

    /**
     * 检测设备协议
     *
     * @param ipAddress 设备IP地址
     * @param openPorts 开放端口
     * @param timeout 检测超时时间
     * @return 检测结果
     */
    ResponseDTO<String> detectDeviceProtocol(String ipAddress, Map<Integer, String> openPorts, int timeout);

    /**
     * 验证设备连接
     *
     * @param deviceId 设备ID
     * @return 验证结果
     */
    ResponseDTO<Boolean> validateDeviceConnection(Long deviceId);

    /**
     * 获取发现历史
     *
     * @param limit 限制数量
     * @return 发现历史
     */
    ResponseDTO<List<ProtocolAutoDiscoveryManager.DiscoveryResult>> getDiscoveryHistory(int limit);

    /**
     * 清理过期任务
     *
     * @param maxAgeHours 最大保存时间（小时）
     * @return 清理结果
     */
    ResponseDTO<Integer> cleanupExpiredTasks(int maxAgeHours);

    /**
     * 导出发现结果
     *
     * @param taskId 任务ID
     * @param format 导出格式
     * @return 导出结果
     */
    ResponseDTO<String> exportDiscoveryResult(String taskId, String format);
}
