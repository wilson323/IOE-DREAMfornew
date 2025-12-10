package net.lab1024.sa.devicecomm.protocol.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.devicecomm.protocol.enums.ProtocolTypeEnum;
import net.lab1024.sa.devicecomm.protocol.router.MessageRouter;

/**
 * TCP推送服务器
 * <p>
 * 接收设备通过TCP推送的数据
 * 严格遵循CLAUDE.md规范：
 * - 使用@Component注解，由Spring管理
 * - 使用@Resource注入依赖
 * - 使用NIO实现非阻塞IO
 * </p>
 * <p>
 * 功能：
 * - 监听TCP端口接收设备推送
 * - 非阻塞IO处理
 * - 消息路由到协议处理器
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class TcpPushServer {

    /**
     * TCP服务器端口（从配置文件读取，默认18087）
     * 注意：使用18087避免与common-service的8088端口冲突
     */
    @Value("${device.protocol.tcp.port:18087}")
    private int tcpPort;

    /**
     * 消息路由器
     */
    @Resource
    private MessageRouter messageRouter;

    /**
     * 网关服务客户端（用于调用其他微服务）
     */
    @Resource
    private GatewayServiceClient gatewayServiceClient;

    /**
     * 服务器Socket通道
     */
    private ServerSocketChannel serverSocketChannel;

    /**
     * 选择器
     */
    private Selector selector;

    /**
     * 服务器运行标志
     */
    private volatile boolean running = false;

    /**
     * 服务器线程
     */
    private Thread serverThread;

    /**
     * 初始化TCP服务器
     * <p>
     * 在Spring容器启动后自动启动TCP服务器
     * </p>
     */
    @PostConstruct
    public void start() {
        log.info("[TCP服务器] 开始启动TCP推送服务器，端口={}", tcpPort);

        try {
            // 创建服务器Socket通道
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(tcpPort));

            // 创建选择器
            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            running = true;

            // 启动服务器线程
            serverThread = new Thread(this::run, "TcpPushServer-" + tcpPort);
            serverThread.setDaemon(true);
            serverThread.start();

            log.info("[TCP服务器] TCP推送服务器启动成功，端口={}", tcpPort);

        } catch (IOException e) {
            log.error("[TCP服务器] TCP推送服务器启动失败，端口={}, 错误={}", tcpPort, e.getMessage(), e);
            throw new RuntimeException("TCP推送服务器启动失败", e);
        }
    }

    /**
     * 停止TCP服务器
     * <p>
     * 在Spring容器关闭时停止TCP服务器
     * </p>
     */
    @PreDestroy
    public void stop() {
        log.info("[TCP服务器] 开始停止TCP推送服务器");

        running = false;

        try {
            if (selector != null) {
                selector.wakeup();
            }

            if (serverSocketChannel != null) {
                serverSocketChannel.close();
            }

            if (selector != null) {
                selector.close();
            }

            if (serverThread != null) {
                serverThread.interrupt();
                serverThread.join(5000);
            }

            log.info("[TCP服务器] TCP推送服务器已停止");

        } catch (Exception e) {
            log.error("[TCP服务器] TCP推送服务器停止异常，错误={}", e.getMessage(), e);
        }
    }

    /**
     * 服务器主循环
     * <p>
     * 使用NIO选择器处理连接和读写事件
     * </p>
     */
    private void run() {
        log.info("[TCP服务器] 服务器主循环开始运行");

        while (running) {
            try {
                // 选择就绪的通道
                int selectCount = selector.select(1000);
                if (selectCount == 0) {
                    continue;
                }

                // 处理就绪的通道
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    try {
                        if (key.isAcceptable()) {
                            handleAccept(key);
                        } else if (key.isReadable()) {
                            handleRead(key);
                        }
                    } catch (Exception e) {
                        log.error("[TCP服务器] 处理通道事件异常，错误={}", e.getMessage(), e);
                        if (key != null) {
                            key.cancel();
                            if (key.channel() != null) {
                                key.channel().close();
                            }
                        }
                    }
                }

            } catch (IOException e) {
                if (running) {
                    log.error("[TCP服务器] 服务器主循环异常，错误={}", e.getMessage(), e);
                }
            }
        }

        log.info("[TCP服务器] 服务器主循环结束");
    }

    /**
     * 处理连接接受事件
     *
     * @param key 选择键
     * @throws IOException IO异常
     */
    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);

        InetSocketAddress remoteAddress = (InetSocketAddress) clientChannel.getRemoteAddress();
        log.info("[TCP服务器] 新客户端连接，地址={}:{}", remoteAddress.getAddress().getHostAddress(), remoteAddress.getPort());
    }

    /**
     * 处理读取事件
     *
     * @param key 选择键
     * @throws IOException IO异常
     */
    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        InetSocketAddress remoteAddress = (InetSocketAddress) clientChannel.getRemoteAddress();

        try {
            // 读取数据
            ByteBuffer buffer = ByteBuffer.allocate(4096);
            int bytesRead = clientChannel.read(buffer);

            if (bytesRead == -1) {
                // 客户端关闭连接
                log.info("[TCP服务器] 客户端关闭连接，地址={}:{}", remoteAddress.getAddress().getHostAddress(), remoteAddress.getPort());
                key.cancel();
                clientChannel.close();
                return;
            }

            if (bytesRead > 0) {
                // 处理接收到的数据
                buffer.flip();
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);

                log.debug("[TCP服务器] 接收到数据，地址={}:{}, 长度={}", remoteAddress.getAddress().getHostAddress(), remoteAddress.getPort(), data.length);

                // 异步路由消息（根据协议头识别协议类型和设备ID）
                CompletableFuture.supplyAsync(() -> {
                    try {
                        // 根据协议头识别协议类型
                        ProtocolIdentifier identifier = identifyProtocol(data);
                        if (identifier == null) {
                            log.warn("[TCP服务器] 无法识别协议类型，数据长度={}, 前4字节={}", 
                                    data.length, bytesToHex(data, Math.min(4, data.length)));
                            return null;
                        }

                        // 从消息中提取设备ID（如果可能）
                        Long deviceId = extractDeviceId(data, identifier.getProtocolType());
                        if (deviceId == null) {
                            // 如果无法从消息中提取设备ID，尝试根据客户端IP查找设备
                            deviceId = findDeviceIdByClientIp(remoteAddress.getAddress().getHostAddress());
                            if (deviceId == null) {
                                log.warn("[TCP服务器] 无法获取设备ID，使用默认值1");
                                deviceId = 1L; // 默认设备ID
                            }
                        }

                        log.info("[TCP服务器] 识别协议类型={}, 设备ID={}", identifier.getProtocolType(), deviceId);
                        return messageRouter.route(identifier.getProtocolType(), data, deviceId);
                    } catch (Exception e) {
                        log.error("[TCP服务器] 消息路由异常，错误={}", e.getMessage(), e);
                        return null;
                    }
                });
            }

        } catch (IOException e) {
            log.error("[TCP服务器] 读取数据异常，地址={}:{}, 错误={}",
                    remoteAddress.getAddress().getHostAddress(), remoteAddress.getPort(), e.getMessage(), e);
            key.cancel();
            clientChannel.close();
        }
    }

    /**
     * 协议识别结果
     */
    @Getter
    @AllArgsConstructor
    private static class ProtocolIdentifier {
        /**
         * 协议类型代码
         */
        private final String protocolType;
    }

    /**
     * 根据协议头识别协议类型
     * <p>
     * 通过检查消息前几个字节的协议头来识别协议类型：
     * - 考勤协议（熵基科技 V4.0）：{0x55, 0xAA}
     * - 门禁协议（熵基科技 V4.8）：{0xAA, 0x55}
     * - 消费协议（中控智慧 V1.0）：{0x7E, 0x81}
     * </p>
     *
     * @param data 原始数据
     * @return 协议识别结果，如果无法识别返回null
     */
    private ProtocolIdentifier identifyProtocol(byte[] data) {
        if (data == null || data.length < 2) {
            return null;
        }

        // 检查考勤协议头（熵基科技 V4.0）：{0x55, 0xAA}
        if (data.length >= 2 && data[0] == 0x55 && (data[1] & 0xFF) == 0xAA) {
            return new ProtocolIdentifier(ProtocolTypeEnum.ATTENDANCE_ENTROPY_V4_0.getCode());
        }

        // 检查门禁协议头（熵基科技 V4.8）：{0xAA, 0x55}
        if (data.length >= 2 && (data[0] & 0xFF) == 0xAA && data[1] == 0x55) {
            return new ProtocolIdentifier(ProtocolTypeEnum.ACCESS_ENTROPY_V4_8.getCode());
        }

        // 检查消费协议头（中控智慧 V1.0）：{0x7E, 0x81}
        if (data.length >= 2 && data[0] == 0x7E && (data[1] & 0xFF) == 0x81) {
            return new ProtocolIdentifier(ProtocolTypeEnum.CONSUME_ZKTECO_V1_0.getCode());
        }

        return null;
    }

    /**
     * 从消息中提取设备ID
     * <p>
     * 根据协议类型从消息的特定位置提取设备ID
     * 注意：不同协议的设备ID位置可能不同，需要根据实际协议文档确定
     * </p>
     *
     * @param data 原始数据
     * @param protocolType 协议类型
     * @return 设备ID，如果无法提取返回null
     */
    private Long extractDeviceId(byte[] data, String protocolType) {
        if (data == null || data.length < 10 || protocolType == null) {
            return null;
        }

        try {
            ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
            buffer.position(2); // 跳过协议头（2字节）

            // 根据协议类型提取设备ID
            if (ProtocolTypeEnum.ATTENDANCE_ENTROPY_V4_0.getCode().equals(protocolType)) {
                // 考勤协议：设备编号通常在协议头后（假设4字节）
                if (data.length >= 6) {
                    int deviceId = buffer.getInt();
                    return (long) deviceId;
                }
            } else if (ProtocolTypeEnum.ACCESS_ENTROPY_V4_8.getCode().equals(protocolType)) {
                // 门禁协议：设备编号通常在协议头后（假设8字节字符串，需要解析）
                if (data.length >= 10) {
                    // 尝试解析设备编号（假设为8字节字符串）
                    byte[] deviceCodeBytes = new byte[8];
                    buffer.get(deviceCodeBytes);
                    String deviceCode = new String(deviceCodeBytes).trim();
                    try {
                        return Long.parseLong(deviceCode);
                    } catch (NumberFormatException e) {
                        log.debug("[TCP服务器] 设备编号无法转换为数字，deviceCode={}", deviceCode);
                    }
                }
            } else if (ProtocolTypeEnum.CONSUME_ZKTECO_V1_0.getCode().equals(protocolType)) {
                // 消费协议：设备编号通常在协议头后（假设4字节）
                if (data.length >= 6) {
                    int deviceId = buffer.getInt();
                    return (long) deviceId;
                }
            }
        } catch (Exception e) {
            log.debug("[TCP服务器] 提取设备ID异常，protocolType={}, error={}", protocolType, e.getMessage());
        }

        return null;
    }

    /**
     * 根据客户端IP查找设备ID
     * <p>
     * 通过网关调用公共服务，根据设备IP地址查找设备ID
     * </p>
     *
     * @param clientIp 客户端IP地址
     * @return 设备ID，如果找不到返回null
     */
    private Long findDeviceIdByClientIp(String clientIp) {
        if (clientIp == null || clientIp.isEmpty()) {
            return null;
        }

        try {
            // 通过网关调用公共服务查询设备
            ResponseDTO<DeviceEntity> response = gatewayServiceClient.callCommonService(
                    "/api/v1/devices/ip/" + clientIp,
                    HttpMethod.GET,
                    null,
                    DeviceEntity.class
            );

            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData().getId();
            }
        } catch (Exception e) {
            log.debug("[TCP服务器] 根据IP查找设备ID失败，clientIp={}, error={}", clientIp, e.getMessage());
        }

        return null;
    }

    /**
     * 字节数组转十六进制字符串（用于日志输出）
     *
     * @param bytes 字节数组
     * @param length 要转换的长度
     * @return 十六进制字符串
     */
    private String bytesToHex(byte[] bytes, int length) {
        if (bytes == null || length <= 0) {
            return "";
        }
        int actualLength = Math.min(length, bytes.length);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < actualLength; i++) {
            sb.append(String.format("%02X", bytes[i] & 0xFF));
        }
        return sb.toString();
    }
}

