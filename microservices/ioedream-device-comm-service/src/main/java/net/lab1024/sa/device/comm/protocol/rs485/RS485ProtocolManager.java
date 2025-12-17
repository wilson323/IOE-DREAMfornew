package net.lab1024.sa.device.comm.protocol.rs485;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.comm.dao.DeviceCommLogDao;
import net.lab1024.sa.device.comm.protocol.domain.ProtocolMessage;
import net.lab1024.sa.device.comm.protocol.domain.ProtocolProcessResult;
import net.lab1024.sa.device.comm.protocol.exception.ProtocolParseException;

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
     * 获取协议适配器
     *
     * @return RS485协议适配器
     */
    public RS485ProtocolAdapter getProtocolAdapter() {
        return protocolAdapter;
    }
}
