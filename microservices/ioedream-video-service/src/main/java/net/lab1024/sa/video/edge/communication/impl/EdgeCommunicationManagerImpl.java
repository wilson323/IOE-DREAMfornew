package net.lab1024.sa.video.edge.communication.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.video.edge.communication.EdgeCommunicationManager;
import net.lab1024.sa.video.edge.model.EdgeDevice;
import net.lab1024.sa.video.edge.EdgeConfig;

/**
 * 边缘通信管理器实现类
 * <p>
 * 负责与边缘设备建立/断开连接，管理设备连接状态
 * 主要功能：
 * - 设备连接管理
 * - 心跳检测
 * - 断线重连
 * - 连接状态监控
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-20
 */
@Slf4j
public class EdgeCommunicationManagerImpl implements EdgeCommunicationManager {

    /**
     * 已连接的设备映射（deviceId -> EdgeDevice）
     */
    private final Map<String, EdgeDevice> connectedDevices = new ConcurrentHashMap<>();

    /**
     * 已知设备映射（deviceId -> EdgeDevice）
     * <p>
     * 用于支持“断开后仍可重连”的最小实现。
     * </p>
     */
    private final Map<String, EdgeDevice> knownDevices = new ConcurrentHashMap<>();

    /**
     * 设备连接时间映射（deviceId -> connectTime）
     */
    private final Map<String, Long> deviceConnectTime = new ConcurrentHashMap<>();

    /**
     * 设备最后心跳时间映射（deviceId -> lastHeartbeatTime）
     */
    private final Map<String, Long> deviceLastHeartbeat = new ConcurrentHashMap<>();

    /**
     * 边缘计算配置
     */
    private final EdgeConfig config;

    /**
     * 心跳检测调度器
     */
    private ScheduledExecutorService heartbeatScheduler;

    /**
     * 构造函数
     *
     * @param config 边缘计算配置
     */
    public EdgeCommunicationManagerImpl(EdgeConfig config) {
        this.config = config;
        startHeartbeatScheduler();
    }

    /**
     * 建立与边缘设备的连接
     *
     * @param device 边缘设备
     * @return 是否连接成功
     */
    @Override
    public boolean connectToDevice(EdgeDevice device) {
        if (device == null || device.getDeviceId() == null || device.getDeviceId().trim().isEmpty()) {
            log.warn("[边缘通信管理器] 设备信息无效，无法连接");
            return false;
        }

        String deviceId = device.getDeviceId().trim();
        // 记录为已知设备（用于后续重连）
        knownDevices.put(deviceId, device);

        // 检查设备是否已连接
        if (connectedDevices.containsKey(deviceId)) {
            log.info("[边缘通信管理器] 设备已连接，deviceId={}", deviceId);
            return true;
        }

        try {
            // 尝试连接设备（实际实现中应该通过设备通讯服务建立连接）
            boolean connected = performConnection(device);

            if (connected) {
                // 记录连接信息
                connectedDevices.put(deviceId, device);
                deviceConnectTime.put(deviceId, System.currentTimeMillis());
                deviceLastHeartbeat.put(deviceId, System.currentTimeMillis());

                log.info("[边缘通信管理器] 设备连接成功，deviceId={}, ipAddress={}, port={}",
                        deviceId, device.getIpAddress(), device.getPort());
                return true;
            } else {
                log.error("[边缘通信管理器] 设备连接失败，deviceId={}", deviceId);
                return false;
            }

        } catch (Exception e) {
            log.error("[边缘通信管理器] 设备连接异常，deviceId={}, error={}",
                    deviceId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 断开与边缘设备的连接
     *
     * @param device 边缘设备
     */
    @Override
    public void disconnectFromDevice(EdgeDevice device) {
        if (device == null || device.getDeviceId() == null || device.getDeviceId().trim().isEmpty()) {
            log.warn("[边缘通信管理器] 设备信息无效，无法断开连接");
            return;
        }

        String deviceId = device.getDeviceId().trim();
        // 保留为已知设备，便于后续重连
        knownDevices.put(deviceId, device);

        if (!connectedDevices.containsKey(deviceId)) {
            log.warn("[边缘通信管理器] 设备未连接，无需断开，deviceId={}", deviceId);
            return;
        }

        try {
            // 执行断开连接操作（实际实现中应该通过设备通讯服务断开连接）
            performDisconnection(device);

            // 清理连接信息
            connectedDevices.remove(deviceId);
            deviceConnectTime.remove(deviceId);
            deviceLastHeartbeat.remove(deviceId);

            log.info("[边缘通信管理器] 设备断开连接成功，deviceId={}", deviceId);

        } catch (Exception e) {
            log.error("[边缘通信管理器] 设备断开连接异常，deviceId={}, error={}",
                    deviceId, e.getMessage(), e);
        }
    }

    /**
     * 检查设备是否已连接
     *
     * @param deviceId 设备ID
     * @return 是否已连接
     */
    public boolean isConnected(String deviceId) {
        return connectedDevices.containsKey(deviceId);
    }

    /**
     * 发送心跳检测
     *
     * @param deviceId 设备ID
     * @return 心跳是否成功
     */
    public boolean sendHeartbeat(String deviceId) {
        if (!isConnected(deviceId)) {
            log.warn("[边缘通信管理器] 设备未连接，无法发送心跳，deviceId={}", deviceId);
            return false;
        }

        try {
            // 执行心跳检测（实际实现中应该通过设备通讯服务发送心跳）
            boolean success = performHeartbeat(deviceId);

            if (success) {
                deviceLastHeartbeat.put(deviceId, System.currentTimeMillis());
                log.debug("[边缘通信管理器] 心跳检测成功，deviceId={}", deviceId);
            } else {
                log.warn("[边缘通信管理器] 心跳检测失败，deviceId={}", deviceId);
            }

            return success;

        } catch (Exception e) {
            log.error("[边缘通信管理器] 心跳检测异常，deviceId={}, error={}",
                    deviceId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 同步模型到边缘设备
     *
     * @param deviceId  设备ID
     * @param modelType 模型类型
     * @param modelData 模型数据
     * @return 同步是否成功
     */
    public boolean syncModel(String deviceId, String modelType, byte[] modelData) {
        if (!isConnected(deviceId)) {
            log.warn("[边缘通信管理器] 设备未连接，无法同步模型，deviceId={}", deviceId);
            return false;
        }

        if (modelType == null || modelType.isEmpty()) {
            log.warn("[边缘通信管理器] 模型类型无效，无法同步模型");
            return false;
        }

        if (modelData == null || modelData.length == 0) {
            log.warn("[边缘通信管理器] 模型数据无效，无法同步模型");
            return false;
        }

        // 检查模型同步是否启用
        if (!Boolean.TRUE.equals(config.getModelSyncEnabled())) {
            log.warn("[边缘通信管理器] 模型同步未启用，跳过同步，deviceId={}", deviceId);
            return false;
        }

        try {
            // 执行模型同步（实际实现中应该通过设备通讯服务同步模型）
            boolean success = performModelSync(deviceId, modelType, modelData);

            if (success) {
                log.info("[边缘通信管理器] 模型同步成功，deviceId={}, modelType={}, modelSize={}MB",
                        deviceId, modelType, modelData.length / (1024 * 1024));
            } else {
                log.error("[边缘通信管理器] 模型同步失败，deviceId={}, modelType={}", deviceId, modelType);
            }

            return success;

        } catch (Exception e) {
            log.error("[边缘通信管理器] 模型同步异常，deviceId={}, modelType={}, error={}",
                    deviceId, modelType, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取连接统计信息
     *
     * @return 统计信息
     */
    public Map<String, Object> getConnectionStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("totalConnections", connectedDevices.size());
        stats.put("activeConnections", connectedDevices.size());
        stats.put("connectionDetails", connectedDevices.keySet());
        return stats;
    }

    /**
     * 重连设备
     *
     * @param deviceId 设备ID
     * @return 重连是否成功
     */
    public boolean reconnect(String deviceId) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            log.warn("[边缘通信管理器] deviceId为空，无法重连");
            return false;
        }

        String normalizedDeviceId = deviceId.trim();
        EdgeDevice device = knownDevices.get(normalizedDeviceId);
        if (device == null) {
            log.warn("[边缘通信管理器] 设备未连接过，无法重连，deviceId={}", deviceId);
            return false;
        }

        // 若仍处于连接状态，直接返回成功
        if (isConnected(normalizedDeviceId)) {
            return true;
        }

        // 重新连接
        return connectToDevice(device);
    }

    /**
     * 启动心跳检测调度器
     */
    private void startHeartbeatScheduler() {
        if (config.getHeartbeatInterval() == null || config.getHeartbeatInterval() <= 0) {
            log.warn("[边缘通信管理器] 心跳检测间隔未配置，跳过启动心跳调度器");
            return;
        }

        heartbeatScheduler = Executors.newScheduledThreadPool(1);
        heartbeatScheduler.scheduleAtFixedRate(
                this::performHeartbeatCheck,
                config.getHeartbeatInterval(),
                config.getHeartbeatInterval(),
                TimeUnit.MILLISECONDS);

        log.info("[边缘通信管理器] 心跳检测调度器已启动，间隔={}ms", config.getHeartbeatInterval());
    }

    /**
     * 执行心跳检测检查
     */
    private void performHeartbeatCheck() {
        for (String deviceId : connectedDevices.keySet()) {
            sendHeartbeat(deviceId);
        }
    }

    /**
     * 执行设备连接（实际实现中应该通过设备通讯服务）
     *
     * @param device 边缘设备
     * @return 连接是否成功
     */
    private boolean performConnection(EdgeDevice device) {
        // 实际实现中应该通过设备通讯服务建立连接
        // 这里返回 true 表示连接成功（占位实现）
        log.debug("[边缘通信管理器] 执行设备连接，deviceId={}, endpoint={}:{}",
                device.getDeviceId(), device.getIpAddress(), device.getPort());
        return true;
    }

    /**
     * 执行设备断开连接（实际实现中应该通过设备通讯服务）
     *
     * @param device 边缘设备
     */
    private void performDisconnection(EdgeDevice device) {
        // 实际实现中应该通过设备通讯服务断开连接
        log.debug("[边缘通信管理器] 执行设备断开连接，deviceId={}", device.getDeviceId());
    }

    /**
     * 执行心跳检测（实际实现中应该通过设备通讯服务）
     *
     * @param deviceId 设备ID
     * @return 心跳是否成功
     */
    private boolean performHeartbeat(String deviceId) {
        // 实际实现中应该通过设备通讯服务发送心跳
        log.debug("[边缘通信管理器] 执行心跳检测，deviceId={}", deviceId);
        return true;
    }

    /**
     * 执行模型同步（实际实现中应该通过设备通讯服务）
     *
     * @param deviceId  设备ID
     * @param modelType 模型类型
     * @param modelData 模型数据
     * @return 同步是否成功
     */
    private boolean performModelSync(String deviceId, String modelType, byte[] modelData) {
        // 实际实现中应该通过设备通讯服务同步模型
        log.debug("[边缘通信管理器] 执行模型同步，deviceId={}, modelType={}, modelSize={}MB",
                deviceId, modelType, modelData.length / (1024 * 1024));
        return true;
    }

    /**
     * 关闭通信管理器
     */
    public void shutdown() {
        if (heartbeatScheduler != null && !heartbeatScheduler.isShutdown()) {
            heartbeatScheduler.shutdown();
            try {
                if (!heartbeatScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    heartbeatScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                heartbeatScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // 断开所有设备连接
        for (EdgeDevice device : connectedDevices.values()) {
            disconnectFromDevice(device);
        }

        connectedDevices.clear();
        knownDevices.clear();
        deviceConnectTime.clear();
        deviceLastHeartbeat.clear();

        log.info("[边缘通信管理器] 通信管理器已关闭");
    }
}
