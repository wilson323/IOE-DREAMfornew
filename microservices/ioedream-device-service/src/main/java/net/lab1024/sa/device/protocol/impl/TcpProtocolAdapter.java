package net.lab1024.sa.device.protocol.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.domain.entity.AccessDeviceEntity;
import net.lab1024.sa.device.protocol.DeviceProtocolAdapter;
import net.lab1024.sa.device.protocol.DeviceProtocolException;

/**
 * TCP协议适配器实现 - 微服务版本
 * <p>
 * 严格遵循repowiki规范：
 * - 实现DeviceProtocolAdapter接口
 * - 完整的异常处理和日志记录
 * - 超时控制和重试机制
 * - 支持主流门禁设备厂商TCP协议
 *
 * @author IOE-DREAM Team
 * @date 2025-11-29
 */
@Slf4j
public class TcpProtocolAdapter implements DeviceProtocolAdapter {

    /**
     * 默认连接超时时间(毫秒)
     */
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;

    /**
     * 默认读取超时时间(毫秒)
     */
    private static final int DEFAULT_READ_TIMEOUT = 10000;

    /**
     * 重试次数
     */
    private static final int RETRY_COUNT = 3;

    /**
     * 支持的厂商列表
     */
    private static final List<String> SUPPORTED_MANUFACTURERS = Arrays.asList(
            "海康威视", "大华", "宇视", "雄迈", "中维世纪", "天地伟业");

    @Override
    public ProtocolType getProtocolType() {
        return ProtocolType.TCP;
    }

    @Override
    public List<String> getSupportedManufacturers() {
        return new ArrayList<>(SUPPORTED_MANUFACTURERS);
    }

    @Override
    public boolean remoteOpen(AccessDeviceEntity device) throws DeviceProtocolException {
        log.info("TCP协议远程开门，设备: {}", device.getDeviceName());

        String command = buildRemoteOpenCommand(device);

        return executeCommandWithRetry(device, command, "远程开门");
    }

    @Override
    public boolean restartDevice(AccessDeviceEntity device) throws DeviceProtocolException {
        log.info("TCP协议重启设备，设备: {}", device.getDeviceName());

        String command = buildRestartCommand(device);

        return executeCommandWithRetry(device, command, "重启设备");
    }

    @Override
    public boolean syncDeviceTime(AccessDeviceEntity device) throws DeviceProtocolException {
        log.info("TCP协议同步设备时间，设备: {}", device.getDeviceName());

        String command = buildSyncTimeCommand(device);

        return executeCommandWithRetry(device, command, "同步时间");
    }

    @Override
    public boolean checkConnection(AccessDeviceEntity device) throws DeviceProtocolException {
        log.debug("TCP协议检查设备连接，设备: {}", device.getDeviceName());

        try (Socket socket = createSocket(device)) {
            // 发送心跳检测命令
            String heartbeatCommand = buildHeartbeatCommand(device);
            String response = sendAndReceive(socket, heartbeatCommand);

            return isResponseSuccess(response);
        } catch (Exception e) {
            throw new DeviceProtocolException(
                    "设备连接检查失败: " + e.getMessage(),
                    e,
                    "CONNECTION_CHECK_FAILED",
                    device.getAccessDeviceId(),
                    "TCP");
        }
    }

    @Override
    public Map<String, Object> getDeviceStatus(AccessDeviceEntity device) throws DeviceProtocolException {
        log.info("TCP协议获取设备状态，设备: {}", device.getDeviceName());

        try (Socket socket = createSocket(device)) {
            String statusCommand = buildStatusCommand(device);
            String response = sendAndReceive(socket, statusCommand);

            return parseStatusResponse(response);
        } catch (Exception e) {
            throw new DeviceProtocolException(
                    "获取设备状态失败: " + e.getMessage(),
                    e,
                    "GET_STATUS_FAILED",
                    device.getAccessDeviceId(),
                    "TCP");
        }
    }

    @Override
    public Map<String, Object> sendCustomCommand(AccessDeviceEntity device, String command, Map<String, Object> params)
            throws DeviceProtocolException {
        log.info("TCP协议发送自定义命令，设备: {}, 命令: {}", device.getDeviceName(), command);

        try (Socket socket = createSocket(device)) {
            String fullCommand = buildCustomCommand(command, params);
            String response = sendAndReceive(socket, fullCommand);

            return parseCustomResponse(response);
        } catch (Exception e) {
            throw new DeviceProtocolException(
                    "发送自定义命令失败: " + e.getMessage(),
                    e,
                    "CUSTOM_COMMAND_FAILED",
                    device.getAccessDeviceId(),
                    "TCP");
        }
    }

    /**
     * 创建TCP连接
     */
    private Socket createSocket(AccessDeviceEntity device) throws IOException {
        Socket socket = new Socket();
        socket.connect(new java.net.InetSocketAddress(device.getIpAddress(), device.getPort()),
                DEFAULT_CONNECT_TIMEOUT);
        socket.setSoTimeout(DEFAULT_READ_TIMEOUT);
        return socket;
    }

    /**
     * 发送并接收数据
     */
    private String sendAndReceive(Socket socket, String command) throws IOException {
        try (OutputStream out = socket.getOutputStream();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

            // 发送命令
            out.write(command.getBytes(StandardCharsets.UTF_8));
            out.flush();

            // 接收响应
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line).append("\n");
                if (line.contains("END")) {
                    break;
                }
            }

            return response.toString();
        }
    }

    /**
     * 带重试的命令执行
     */
    private boolean executeCommandWithRetry(AccessDeviceEntity device, String command, String operation)
            throws DeviceProtocolException {
        Exception lastException = null;

        for (int i = 0; i < RETRY_COUNT; i++) {
            try {
                log.debug("TCP协议执行{}，第{}次尝试，设备: {}", operation, i + 1, device.getDeviceName());

                try (Socket socket = createSocket(device)) {
                    String response = sendAndReceive(socket, command);

                    if (isResponseSuccess(response)) {
                        log.info("TCP协议{}成功，设备: {}", operation, device.getDeviceName());
                        return true;
                    } else {
                        log.warn("TCP协议{}失败，响应不正确: {}", operation, response);
                    }
                }
            } catch (Exception e) {
                lastException = e;
                log.warn("TCP协议{}第{}次尝试失败: {}", operation, i + 1, e.getMessage());

                if (i < RETRY_COUNT - 1) {
                    try {
                        Thread.sleep(1000 * (i + 1)); // 递增延迟
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        throw new DeviceProtocolException(
                String.format("TCP协议%s失败，已重试%d次", operation, RETRY_COUNT),
                lastException,
                "EXECUTE_COMMAND_FAILED",
                device.getAccessDeviceId(),
                "TCP");
    }

    /**
     * 构建远程开门命令
     */
    private String buildRemoteOpenCommand(AccessDeviceEntity device) {
        return String.format("OPEN DOOR %s\nEND\n", device.getDeviceCode());
    }

    /**
     * 构建重启命令
     */
    private String buildRestartCommand(AccessDeviceEntity device) {
        return String.format("RESTART %s\nEND\n", device.getDeviceCode());
    }

    /**
     * 构建时间同步命令
     */
    private String buildSyncTimeCommand(AccessDeviceEntity device) {
        String currentTime = new Date().toString();
        return String.format("SYNC_TIME %s %s\nEND\n", device.getDeviceCode(), currentTime);
    }

    /**
     * 构建心跳命令
     */
    private String buildHeartbeatCommand(AccessDeviceEntity device) {
        return String.format("HEARTBEAT %s\nEND\n", device.getDeviceCode());
    }

    /**
     * 构建状态查询命令
     */
    private String buildStatusCommand(AccessDeviceEntity device) {
        return String.format("GET_STATUS %s\nEND\n", device.getDeviceCode());
    }

    /**
     * 构建自定义命令
     */
    private String buildCustomCommand(String command, Map<String, Object> params) {
        StringBuilder sb = new StringBuilder(command);

        if (params != null && !params.isEmpty()) {
            sb.append(" ");
            params.forEach((key, value) -> sb.append(key).append("=").append(value).append(" "));
        }

        sb.append("\nEND\n");
        return sb.toString();
    }

    /**
     * 判断响应是否成功
     */
    private boolean isResponseSuccess(String response) {
        return response != null &&
                (response.contains("SUCCESS") || response.contains("OK") || response.contains("DONE"));
    }

    /**
     * 解析状态响应
     */
    private Map<String, Object> parseStatusResponse(String response) {
        Map<String, Object> status = new HashMap<>();

        if (response != null) {
            String[] lines = response.split("\n");
            for (String line : lines) {
                if (line.contains("=")) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        status.put(parts[0].trim(), parts[1].trim());
                    }
                }
            }
        }

        return status;
    }

    /**
     * 解析自定义响应
     */
    private Map<String, Object> parseCustomResponse(String response) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", isResponseSuccess(response));
        result.put("response", response);
        return result;
    }
}
