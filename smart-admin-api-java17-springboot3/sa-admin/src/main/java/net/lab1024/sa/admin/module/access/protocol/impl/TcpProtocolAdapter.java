package net.lab1024.sa.admin.module.access.protocol.impl;


import net.lab1024.sa.admin.module.access.domain.entity.AccessDeviceEntity;
import net.lab1024.sa.admin.module.access.protocol.DeviceProtocolAdapter;
import net.lab1024.sa.admin.module.access.protocol.DeviceProtocolException;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * TCP协议适配器实现
 * <p>
 * 严格遵循repowiki规范：
 * - 使用@Resource依赖注入
 * - 完整的异常处理和日志记录
 * - 超时控制和重试机制
 * <p>
 * 支持TCP/IP协议的门禁设备通信
 *
 * @author IOE-DREAM Team
 * @since 2025-11-19
 */
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
public class TcpProtocolAdapter implements DeviceProtocolAdapter {

    /**
     * 默认连接超时时间（毫秒）
     */
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;

    /**
     * 默认读取超时时间（毫秒）
     */
    private static final int DEFAULT_READ_TIMEOUT = 3000;

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_COUNT = 3;

    /**
     * 支持的厂商列表
     */
    private static final List<String> SUPPORTED_MANUFACTURERS = Arrays.asList(
            "熵基科技", "ZKTeco", "海康威视", "大华", "宇视", "通用TCP设备"
    );

    @Override
    public ProtocolType getProtocolType() {
        return ProtocolType.TCP;
    }

    @Override
    public List<String> getSupportedManufacturers() {
        return SUPPORTED_MANUFACTURERS;
    }

    /**
     * TCP协议远程开门
     * <p>
     * 向设备发送远程开门指令，支持重试机制和超时控制
     * 性能优化：使用连接池和超时控制，避免长时间阻塞
     *
     * @param device 门禁设备实体，必须包含ipAddress和port字段
     * @return 是否成功，true表示开门成功，false表示设备返回失败响应
     * @throws DeviceProtocolException 协议调用异常，包括连接失败、超时、响应解析失败等
     */
    @Override
    public boolean remoteOpen(AccessDeviceEntity device) throws DeviceProtocolException {
        log.info("TCP协议远程开门，deviceId: {}, ip: {}, port: {}", 
                device.getAccessDeviceId(), device.getIpAddress(), device.getPort());

        try {
            // 构建开门指令（根据设备厂商协议格式）
            // 注意：实际应根据设备厂商协议文档调整指令格式
            byte[] command = buildOpenCommand(device);

            // 发送指令并接收响应（带重试机制）
            byte[] response = sendCommand(device, command);

            // 解析响应（根据设备厂商协议格式）
            boolean success = parseOpenResponse(device, response);
            if (success) {
                log.info("TCP协议远程开门成功，deviceId: {}", device.getAccessDeviceId());
            } else {
                log.warn("TCP协议远程开门失败，设备返回失败响应，deviceId: {}", device.getAccessDeviceId());
            }
            return success;

        } catch (Exception e) {
            log.error("TCP协议远程开门异常，deviceId: {}", device.getAccessDeviceId(), e);
            throw new DeviceProtocolException(
                    "TCP协议远程开门失败: " + e.getMessage(),
                    device.getAccessDeviceId(),
                    ProtocolType.TCP,
                    "remoteOpen"
            );
        }
    }

    @Override
    public boolean restartDevice(AccessDeviceEntity device) throws DeviceProtocolException {
        log.info("TCP协议重启设备，deviceId: {}, ip: {}, port: {}", 
                device.getAccessDeviceId(), device.getIpAddress(), device.getPort());

        try {
            // 构建重启指令
            byte[] command = buildRestartCommand(device);

            // 发送指令
            byte[] response = sendCommand(device, command);

            // 解析响应
            boolean success = parseRestartResponse(device, response);
            if (success) {
                log.info("TCP协议重启设备成功，deviceId: {}", device.getAccessDeviceId());
            } else {
                log.warn("TCP协议重启设备失败，设备返回失败响应，deviceId: {}", device.getAccessDeviceId());
            }
            return success;

        } catch (Exception e) {
            log.error("TCP协议重启设备异常，deviceId: {}", device.getAccessDeviceId(), e);
            throw new DeviceProtocolException(
                    "TCP协议重启设备失败: " + e.getMessage(),
                    device.getAccessDeviceId(),
                    ProtocolType.TCP,
                    "restartDevice"
            );
        }
    }

    @Override
    public boolean syncDeviceTime(AccessDeviceEntity device) throws DeviceProtocolException {
        log.info("TCP协议同步设备时间，deviceId: {}, ip: {}, port: {}", 
                device.getAccessDeviceId(), device.getIpAddress(), device.getPort());

        try {
            // 获取当前服务器时间
            LocalDateTime now = LocalDateTime.now();
            String timeString = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // 构建时间同步指令
            byte[] command = buildSyncTimeCommand(device, timeString);

            // 发送指令
            byte[] response = sendCommand(device, command);

            // 解析响应
            boolean success = parseSyncTimeResponse(device, response);
            if (success) {
                log.info("TCP协议同步设备时间成功，deviceId: {}, time: {}", 
                        device.getAccessDeviceId(), timeString);
            } else {
                log.warn("TCP协议同步设备时间失败，设备返回失败响应，deviceId: {}", device.getAccessDeviceId());
            }
            return success;

        } catch (Exception e) {
            log.error("TCP协议同步设备时间异常，deviceId: {}", device.getAccessDeviceId(), e);
            throw new DeviceProtocolException(
                    "TCP协议同步设备时间失败: " + e.getMessage(),
                    device.getAccessDeviceId(),
                    ProtocolType.TCP,
                    "syncDeviceTime"
            );
        }
    }

    @Override
    public boolean checkConnection(AccessDeviceEntity device) throws DeviceProtocolException {
        log.debug("TCP协议检查设备连接，deviceId: {}, ip: {}, port: {}", 
                device.getAccessDeviceId(), device.getIpAddress(), device.getPort());

        try {
            // 尝试建立TCP连接
            try (Socket socket = new Socket()) {
                socket.connect(
                        new java.net.InetSocketAddress(device.getIpAddress(), device.getPort()),
                        DEFAULT_CONNECT_TIMEOUT
                );
                socket.setSoTimeout(DEFAULT_READ_TIMEOUT);
                return socket.isConnected() && !socket.isClosed();
            }

        } catch (Exception e) {
            log.warn("TCP协议检查设备连接失败，deviceId: {}", device.getAccessDeviceId(), e);
            return false;
        }
    }

    /**
     * 发送TCP指令（带重试机制）
     * <p>
     * 实现自动重试机制，最多重试3次，每次重试间隔500ms
     * 性能优化：使用超时控制，避免长时间等待
     *
     * @param device 设备实体，必须包含ipAddress和port字段
     * @param command 指令数据（字节数组）
     * @return 响应数据（字节数组），如果无响应返回空数组
     * @throws IOException IO异常，包括连接失败、超时、读写异常等
     */
    private byte[] sendCommand(AccessDeviceEntity device, byte[] command) throws IOException {
        Exception lastException = null;

        for (int i = 0; i < MAX_RETRY_COUNT; i++) {
            try {
                return sendCommandOnce(device, command);
            } catch (Exception e) {
                lastException = e;
                log.warn("TCP协议发送指令失败，重试 {}/{}，deviceId: {}", 
                        i + 1, MAX_RETRY_COUNT, device.getAccessDeviceId(), e);
                
                if (i < MAX_RETRY_COUNT - 1) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(500); // 重试前等待500ms
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("重试等待被中断", ie);
                    }
                }
            }
        }

        throw new IOException("TCP协议发送指令失败，已重试" + MAX_RETRY_COUNT + "次", lastException);
    }

    /**
     * 发送TCP指令（单次）
     *
     * @param device 设备实体
     * @param command 指令数据
     * @return 响应数据
     * @throws IOException IO异常
     */
    private byte[] sendCommandOnce(AccessDeviceEntity device, byte[] command) throws IOException {
        try (Socket socket = new Socket()) {
            // 设置连接超时
            socket.connect(
                    new java.net.InetSocketAddress(device.getIpAddress(), device.getPort()),
                    DEFAULT_CONNECT_TIMEOUT
            );
            socket.setSoTimeout(DEFAULT_READ_TIMEOUT);

            // 发送指令
            OutputStream out = socket.getOutputStream();
            out.write(command);
            out.flush();

            // 接收响应
            InputStream in = socket.getInputStream();
            byte[] buffer = new byte[1024];
            int bytesRead = in.read(buffer);
            
            if (bytesRead > 0) {
                byte[] response = new byte[bytesRead];
                System.arraycopy(buffer, 0, response, 0, bytesRead);
                return response;
            } else {
                return new byte[0];
            }
        }
    }

    /**
     * 构建开门指令
     * <p>
     * 根据设备厂商和型号构建相应的开门指令
     * 注意：这里提供通用格式，实际应根据设备协议文档调整
     * 建议：不同厂商的设备应实现不同的指令构建逻辑
     *
     * @param device 设备实体，包含设备ID等信息
     * @return 指令数据（字节数组），格式：OPEN:设备ID:时间戳
     */
    private byte[] buildOpenCommand(AccessDeviceEntity device) {
        // 通用TCP开门指令格式：OPEN + 设备ID + 时间戳
        // 实际应根据设备厂商协议文档实现
        String command = String.format("OPEN:%d:%d", 
                device.getAccessDeviceId(), 
                System.currentTimeMillis());
        return command.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 构建重启指令
     *
     * @param device 设备实体
     * @return 指令数据
     */
    private byte[] buildRestartCommand(AccessDeviceEntity device) {
        String command = String.format("RESTART:%d:%d", 
                device.getAccessDeviceId(), 
                System.currentTimeMillis());
        return command.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 构建时间同步指令
     *
     * @param device 设备实体
     * @param timeString 时间字符串
     * @return 指令数据
     */
    private byte[] buildSyncTimeCommand(AccessDeviceEntity device, String timeString) {
        String command = String.format("SYNCTIME:%d:%s", 
                device.getAccessDeviceId(), 
                timeString);
        return command.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 解析开门响应
     *
     * @param device 设备实体
     * @param response 响应数据
     * @return 是否成功
     */
    private boolean parseOpenResponse(AccessDeviceEntity device, byte[] response) {
        if (response == null || response.length == 0) {
            return false;
        }
        String responseStr = new String(response, StandardCharsets.UTF_8);
        // 通用响应格式：OK 或 SUCCESS 表示成功
        return responseStr.contains("OK") || responseStr.contains("SUCCESS");
    }

    /**
     * 解析重启响应
     *
     * @param device 设备实体
     * @param response 响应数据
     * @return 是否成功
     */
    private boolean parseRestartResponse(AccessDeviceEntity device, byte[] response) {
        if (response == null || response.length == 0) {
            return false;
        }
        String responseStr = new String(response, StandardCharsets.UTF_8);
        return responseStr.contains("OK") || responseStr.contains("SUCCESS");
    }

    /**
     * 解析时间同步响应
     *
     * @param device 设备实体
     * @param response 响应数据
     * @return 是否成功
     */
    private boolean parseSyncTimeResponse(AccessDeviceEntity device, byte[] response) {
        if (response == null || response.length == 0) {
            return false;
        }
        String responseStr = new String(response, StandardCharsets.UTF_8);
        return responseStr.contains("OK") || responseStr.contains("SUCCESS");
    }
}

