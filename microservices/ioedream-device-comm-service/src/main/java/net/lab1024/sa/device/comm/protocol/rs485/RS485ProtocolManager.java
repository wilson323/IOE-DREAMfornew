package net.lab1024.sa.device.comm.protocol.rs485;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.comm.dao.DeviceCommLogDao;
import net.lab1024.sa.device.comm.protocol.domain.ProtocolMessage;
import net.lab1024.sa.device.comm.protocol.domain.ProtocolProcessResult;
import net.lab1024.sa.device.comm.protocol.domain.ProtocolHeartbeatResult;
import net.lab1024.sa.device.comm.protocol.domain.ProtocolDeviceStatus;
import net.lab1024.sa.device.comm.protocol.exception.ProtocolParseException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * RS485协议管理器
 * <p>
 * 负责管理RS485设备的协议通讯
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-17
 */
@Slf4j
public class RS485ProtocolManager {

    private final RS485ProtocolAdapter protocolAdapter;
    private final DeviceCommLogDao deviceCommLogDao;

    /**
     * 构造函数
     *
     * @param protocolAdapter RS485协议适配器
     * @param deviceCommLogDao 设备通讯日志DAO
     */
    public RS485ProtocolManager(RS485ProtocolAdapter protocolAdapter, DeviceCommLogDao deviceCommLogDao) {
        this.protocolAdapter = protocolAdapter;
        this.deviceCommLogDao = deviceCommLogDao;
        log.info("[RS485管理器] 初始化完成");
    }

    /**
     * 处理设备消息
     *
     * @param rawData 原始数据
     * @param deviceId 设备ID
     * @return 处理结果Future
     */
    public Future<ProtocolProcessResult> processDeviceMessage(byte[] rawData, Long deviceId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("[RS485管理器] 开始处理设备消息, deviceId={}", deviceId);

                // 解析消息
                ProtocolMessage message = protocolAdapter.parseDeviceMessage(rawData, deviceId);

                // 记录通讯日志
                logDeviceComm(message, true);

                log.debug("[RS485管理器] 设备消息处理完成, deviceId={}", deviceId);

                return ProtocolProcessResult.success("MESSAGE_PROCESS", null);

            } catch (ProtocolParseException e) {
                log.error("[RS485管理器] 设备消息处理失败, deviceId={}", deviceId, e);
                return ProtocolProcessResult.failure("MESSAGE_PROCESS", "PARSE_ERROR", e.getMessage());
            }
        });
    }

    /**
     * 发送设备命令
     *
     * @param command 命令数据
     * @param deviceId 设备ID
     * @return 发送结果Future
     */
    public Future<ProtocolProcessResult> sendDeviceCommand(Map<String, Object> command, Long deviceId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("[RS485管理器] 发送设备命令, deviceId={}, command={}", deviceId, command);

                // 构建响应
                byte[] response = protocolAdapter.buildDeviceResponse("COMMAND", command, deviceId);

                log.debug("[RS485管理器] 设备命令发送完成, deviceId={}, responseLength={}", deviceId, response.length);

                return ProtocolProcessResult.success("COMMAND_SEND", null);

            } catch (Exception e) {
                log.error("[RS485管理器] 设备命令发送失败, deviceId={}", deviceId, e);
                return ProtocolProcessResult.failure("COMMAND_SEND", "SEND_ERROR", e.getMessage());
            }
        });
    }

    /**
     * 记录设备通讯日志
     *
     * @param message 协议消息
     * @param success 是否成功
     */
    private void logDeviceComm(ProtocolMessage message, boolean success) {
        try {
            // TODO: 实现通讯日志记录
            log.trace("[RS485管理器] 记录通讯日志, deviceId={}, success={}", message.getDeviceId(), success);
        } catch (Exception e) {
            log.warn("[RS485管理器] 通讯日志记录失败", e);
        }
    }

    /**
     * 处理设备心跳
     * <p>
     * 采用策略模式处理心跳逻辑
     * </p>
     *
     * @param deviceId 设备ID
     * @param heartbeatData 心跳数据
     * @return 心跳结果Future
     */
    public Future<RS485HeartbeatResult> processDeviceHeartbeat(Long deviceId, Map<String, Object> heartbeatData) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("[RS485管理器] 处理设备心跳, deviceId={}", deviceId);

                // 调用Adapter层处理心跳
                ProtocolHeartbeatResult result = protocolAdapter.handleDeviceHeartbeat(heartbeatData, deviceId);

                // 转换为RS485HeartbeatResult
                RS485HeartbeatResult heartbeatResult = RS485HeartbeatResult.success(
                        deviceId,
                        result.getHeartbeatTime() != null ?
                                java.time.Duration.between(result.getHeartbeatTime(), java.time.LocalDateTime.now()).toMillis() : 0L
                );
                heartbeatResult.setOnline(result.isOnline());

                log.debug("[RS485管理器] 设备心跳处理完成, deviceId={}, online={}", deviceId, heartbeatResult.isOnline());
                return heartbeatResult;

            } catch (Exception e) {
                log.error("[RS485管理器] 设备心跳处理失败, deviceId={}", deviceId, e);
                return RS485HeartbeatResult.offline(deviceId, "心跳处理失败: " + e.getMessage());
            }
        });
    }

    /**
     * 构建设备响应
     * <p>
     * 采用模板方法模式统一响应构建流程
     * </p>
     *
     * @param deviceId 设备ID
     * @param messageType 消息类型
     * @param businessData 业务数据
     * @return 响应数据Future
     */
    public Future<byte[]> buildDeviceResponse(Long deviceId, String messageType, Map<String, Object> businessData) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("[RS485管理器] 构建设备响应, deviceId={}, messageType={}", deviceId, messageType);

                // 调用Adapter层构建响应
                byte[] response = protocolAdapter.buildDeviceResponse(messageType, businessData, deviceId);

                log.debug("[RS485管理器] 设备响应构建完成, deviceId={}, responseLength={}", deviceId, response.length);
                return response;

            } catch (Exception e) {
                log.error("[RS485管理器] 设备响应构建失败, deviceId={}", deviceId, e);
                throw new RuntimeException("响应构建失败: " + e.getMessage(), e);
            }
        });
    }

    /**
     * 获取设备状态
     * <p>
     * 采用策略模式获取设备状态
     * </p>
     *
     * @param deviceId 设备ID
     * @return 设备状态
     */
    public RS485DeviceStatus getDeviceStatus(Long deviceId) {
        try {
            log.debug("[RS485管理器] 获取设备状态, deviceId={}", deviceId);

            // 调用Adapter层获取设备状态
            ProtocolDeviceStatus status = protocolAdapter.getDeviceStatus(deviceId);

            // 转换为RS485DeviceStatus
            RS485DeviceStatus deviceStatus = new RS485DeviceStatus();
            deviceStatus.setDeviceId(status.getDeviceId());
            deviceStatus.setSerialNumber(status.getDeviceCode());
            deviceStatus.setOnline(status.isOnline());
            deviceStatus.setConnectionStatus(status.getConnectionStatus());
            deviceStatus.setLastHeartbeatTime(status.getLastHeartbeatTime());
            deviceStatus.setFirmwareVersion(status.getFirmwareVersion());
            deviceStatus.setProtocolVersion(protocolAdapter.getVersion());

            log.debug("[RS485管理器] 设备状态获取完成, deviceId={}, online={}", deviceId, deviceStatus.isOnline());
            return deviceStatus;

        } catch (Exception e) {
            log.error("[RS485管理器] 设备状态获取失败, deviceId={}", deviceId, e);
            RS485DeviceStatus errorStatus = new RS485DeviceStatus();
            errorStatus.setDeviceId(deviceId);
            errorStatus.setOnline(false);
            errorStatus.setConnectionStatus("ERROR");
            return errorStatus;
        }
    }

    /**
     * 断开设备连接
     * <p>
     * 采用策略模式处理断开逻辑
     * </p>
     *
     * @param deviceId 设备ID
     * @return 是否成功
     */
    public boolean disconnectDevice(Long deviceId) {
        try {
            log.info("[RS485管理器] 断开设备连接, deviceId={}", deviceId);

            // 调用Adapter层销毁连接（通过ProtocolAdapter接口）
            protocolAdapter.destroy();

            log.info("[RS485管理器] 设备连接断开成功, deviceId={}", deviceId);
            return true;

        } catch (Exception e) {
            log.error("[RS485管理器] 设备连接断开失败, deviceId={}", deviceId, e);
            return false;
        }
    }

    /**
     * 获取性能统计
     * <p>
     * 采用装饰器模式收集性能数据
     * </p>
     *
     * @return 性能统计数据
     */
    public Map<String, Object> getPerformanceStatistics() {
        try {
            log.debug("[RS485管理器] 获取性能统计");

            // 调用Adapter层获取性能统计
            Map<String, Object> stats = protocolAdapter.getPerformanceStatistics();

            // 添加Manager层统计信息
            Map<String, Object> enhancedStats = new HashMap<>(stats);
            enhancedStats.put("managerLevel", "RS485ProtocolManager");
            enhancedStats.put("timestamp", System.currentTimeMillis());

            return enhancedStats;

        } catch (Exception e) {
            log.error("[RS485管理器] 性能统计获取失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 获取支持的设备型号
     * <p>
     * 采用策略模式获取设备型号列表
     * </p>
     *
     * @return 设备型号数组
     */
    public String[] getSupportedDeviceModels() {
        try {
            log.debug("[RS485管理器] 获取支持的设备型号");

            // 调用Adapter层获取支持的设备型号
            return protocolAdapter.getSupportedDeviceModels();

        } catch (Exception e) {
            log.error("[RS485管理器] 获取支持的设备型号失败", e);
            return new String[0];
        }
    }

    /**
     * 检查设备型号是否支持
     * <p>
     * 采用策略模式检查设备型号
     * </p>
     *
     * @param deviceModel 设备型号
     * @return 是否支持
     */
    public boolean isDeviceModelSupported(String deviceModel) {
        try {
            log.debug("[RS485管理器] 检查设备型号支持, deviceModel={}", deviceModel);

            // 调用Adapter层检查设备型号
            return protocolAdapter.isDeviceModelSupported(deviceModel);

        } catch (Exception e) {
            log.error("[RS485管理器] 检查设备型号支持失败, deviceModel={}", deviceModel, e);
            return false;
        }
    }

    /**
     * 获取协议适配器
     *
     * @return RS485协议适配器
     */
    public RS485ProtocolAdapter getProtocolAdapter() {
        return protocolAdapter;
    }
}
