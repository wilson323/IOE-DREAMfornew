package net.lab1024.sa.devicecomm.protocol.router;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.devicecomm.monitor.ProtocolMetricsCollector;
import net.lab1024.sa.devicecomm.protocol.adapter.ProtocolAdapterFactory;
import net.lab1024.sa.devicecomm.protocol.enums.ProtocolTypeEnum;
import net.lab1024.sa.devicecomm.protocol.handler.ProtocolHandler;
import net.lab1024.sa.devicecomm.protocol.handler.ProtocolParseException;
import net.lab1024.sa.devicecomm.protocol.handler.ProtocolProcessException;
import net.lab1024.sa.devicecomm.protocol.message.ProtocolMessage;

/**
 * 消息路由器
 * <p>
 * 负责将设备推送的消息路由到对应的协议处理器
 * 严格遵循CLAUDE.md规范：
 * - 使用@Component注解，由Spring管理
 * - 使用@Resource注入依赖
 * - 异步处理消息，不阻塞主线程
 * </p>
 * <p>
 * 功能：
 * - 消息路由到对应的协议处理器
 * - 异步消息处理
 * - 错误处理和日志记录
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class MessageRouter {

    /**
     * 协议适配器工厂
     */
    @Resource
    private ProtocolAdapterFactory protocolAdapterFactory;

    /**
     * 动态线程池（从配置类注入）
     * <p>
     * 使用动态线程池替代固定线程池，支持根据配置和负载动态调整
     * </p>
     */
    @Resource(name = "protocolMessageExecutor")
    private AsyncTaskExecutor executorService;

    /**
     * 协议监控指标收集器
     */
    @Resource
    private ProtocolMetricsCollector metricsCollector;

    /**
     * 销毁线程池
     * <p>
     * 在Spring容器关闭时关闭线程池
     * </p>
     */
    @PreDestroy
    public void destroy() {
        if (executorService instanceof ThreadPoolTaskExecutor) {
            ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executorService;
            ThreadPoolExecutor threadPoolExecutor = taskExecutor.getThreadPoolExecutor();
            
            if (threadPoolExecutor != null && !threadPoolExecutor.isShutdown()) {
                log.info("[消息路由] 开始关闭消息路由器线程池，当前活跃线程数={}, 队列大小={}",
                        threadPoolExecutor.getActiveCount(), threadPoolExecutor.getQueue().size());
                taskExecutor.shutdown();
                log.info("[消息路由] 消息路由器线程池已关闭");
            }
        }
    }

    /**
     * 路由消息（根据协议类型）
     * <p>
     * 根据协议类型将消息路由到对应的协议处理器
     * </p>
     *
     * @param protocolType 协议类型代码
     * @param rawData 原始数据（字节数组）
     * @param deviceId 设备ID
     * @return CompletableFuture，异步处理结果
     */
    public CompletableFuture<ProtocolMessage> route(String protocolType, byte[] rawData, Long deviceId) {
        log.debug("[消息路由] 开始路由消息，协议类型={}, 设备ID={}, 数据长度={}",
                protocolType, deviceId, rawData != null ? rawData.length : 0);

        long startTime = System.currentTimeMillis();
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 获取协议处理器
                ProtocolHandler handler = protocolAdapterFactory.getHandler(protocolType);
                if (handler == null) {
                    throw new ProtocolProcessException("HANDLER_NOT_FOUND", "未找到协议处理器：" + protocolType);
                }

                // 解析消息
                ProtocolMessage message = handler.parseMessage(rawData);

                // 处理消息
                handler.processMessage(message, deviceId);

                long duration = System.currentTimeMillis() - startTime;
                log.info("[消息路由] 消息路由成功，协议类型={}, 设备ID={}, 消息类型={}, duration={}ms",
                        protocolType, deviceId, message.getMessageType(), duration);
                
                // 记录监控指标
                metricsCollector.recordSuccess(protocolType, duration);
                
                return message;

            } catch (ProtocolParseException e) {
                long duration = System.currentTimeMillis() - startTime;
                log.error("[消息路由] 消息解析失败，协议类型={}, 设备ID={}, 错误={}, duration={}ms",
                        protocolType, deviceId, e.getMessage(), duration, e);
                metricsCollector.recordError(protocolType, "PARSE_ERROR");
                throw new RuntimeException("消息解析失败：" + e.getMessage(), e);
            } catch (ProtocolProcessException e) {
                long duration = System.currentTimeMillis() - startTime;
                log.error("[消息路由] 消息处理失败，协议类型={}, 设备ID={}, 错误={}, duration={}ms",
                        protocolType, deviceId, e.getMessage(), duration, e);
                metricsCollector.recordError(protocolType, "PROCESS_ERROR");
                throw new RuntimeException("消息处理失败：" + e.getMessage(), e);
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                log.error("[消息路由] 消息路由异常，协议类型={}, 设备ID={}, 错误={}, duration={}ms",
                        protocolType, deviceId, e.getMessage(), duration, e);
                metricsCollector.recordError(protocolType, "ROUTE_ERROR");
                throw new RuntimeException("消息路由异常：" + e.getMessage(), e);
            }
        }, executorService);
    }

    /**
     * 路由消息（根据设备类型和厂商）
     * <p>
     * 根据设备类型和厂商将消息路由到对应的协议处理器
     * </p>
     *
     * @param deviceType 设备类型（ATTENDANCE、ACCESS、CONSUME）
     * @param manufacturer 厂商名称（熵基科技、中控智慧）
     * @param rawData 原始数据（字节数组）
     * @param deviceId 设备ID
     * @return CompletableFuture，异步处理结果
     */
    public CompletableFuture<ProtocolMessage> route(String deviceType, String manufacturer, byte[] rawData, Long deviceId) {
        log.debug("[消息路由] 开始路由消息，设备类型={}, 厂商={}, 设备ID={}, 数据长度={}",
                deviceType, manufacturer, deviceId, rawData != null ? rawData.length : 0);

        try {
            // 获取协议处理器
            ProtocolHandler handler = protocolAdapterFactory.getHandler(deviceType, manufacturer);
            if (handler == null) {
                throw new ProtocolProcessException("HANDLER_NOT_FOUND",
                        "未找到协议处理器：设备类型=" + deviceType + ", 厂商=" + manufacturer);
            }

            // 使用协议类型路由
            return route(handler.getProtocolType(), rawData, deviceId);

        } catch (Exception e) {
            log.error("[消息路由] 消息路由失败，设备类型={}, 厂商={}, 设备ID={}, 错误={}",
                    deviceType, manufacturer, deviceId, e.getMessage(), e);
            CompletableFuture<ProtocolMessage> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    /**
     * 路由消息（根据协议类型，字符串格式）
     * <p>
     * 根据协议类型将文本格式的消息路由到对应的协议处理器
     * 用于HTTP文本协议（如门禁、考勤、消费协议）
     * </p>
     *
     * @param protocolType 协议类型代码
     * @param rawData 原始数据（字符串）
     * @param deviceId 设备ID
     * @return CompletableFuture，异步处理结果
     */
    public CompletableFuture<ProtocolMessage> route(String protocolType, String rawData, Long deviceId) {
        log.debug("[消息路由] 开始路由文本消息，协议类型={}, 设备ID={}, 数据长度={}",
                protocolType, deviceId, rawData != null ? rawData.length() : 0);

        long startTime = System.currentTimeMillis();
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 获取协议处理器
                ProtocolHandler handler = protocolAdapterFactory.getHandler(protocolType);
                if (handler == null) {
                    throw new ProtocolProcessException("HANDLER_NOT_FOUND", "未找到协议处理器：" + protocolType);
                }

                // 解析消息（字符串格式）
                ProtocolMessage message = handler.parseMessage(rawData);

                // 处理消息
                handler.processMessage(message, deviceId);

                long duration = System.currentTimeMillis() - startTime;
                log.info("[消息路由] 文本消息路由成功，协议类型={}, 设备ID={}, 消息类型={}, duration={}ms",
                        protocolType, deviceId, message.getMessageType(), duration);
                
                // 记录监控指标
                metricsCollector.recordSuccess(protocolType, duration);
                
                return message;

            } catch (ProtocolParseException e) {
                long duration = System.currentTimeMillis() - startTime;
                log.error("[消息路由] 文本消息解析失败，协议类型={}, 设备ID={}, 错误={}, duration={}ms",
                        protocolType, deviceId, e.getMessage(), duration, e);
                metricsCollector.recordError(protocolType, "PARSE_ERROR");
                throw new RuntimeException("消息解析失败：" + e.getMessage(), e);
            } catch (ProtocolProcessException e) {
                long duration = System.currentTimeMillis() - startTime;
                log.error("[消息路由] 文本消息处理失败，协议类型={}, 设备ID={}, 错误={}, duration={}ms",
                        protocolType, deviceId, e.getMessage(), duration, e);
                metricsCollector.recordError(protocolType, "PROCESS_ERROR");
                throw new RuntimeException("消息处理失败：" + e.getMessage(), e);
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                log.error("[消息路由] 文本消息路由异常，协议类型={}, 设备ID={}, 错误={}, duration={}ms",
                        protocolType, deviceId, e.getMessage(), duration, e);
                metricsCollector.recordError(protocolType, "ROUTE_ERROR");
                throw new RuntimeException("消息路由异常：" + e.getMessage(), e);
            }
        }, executorService);
    }

    /**
     * 路由消息（根据设备类型和厂商，字符串格式）
     * <p>
     * 根据设备类型和厂商将文本格式的消息路由到对应的协议处理器
     * </p>
     *
     * @param deviceType 设备类型（ATTENDANCE、ACCESS、CONSUME）
     * @param manufacturer 厂商名称（熵基科技、中控智慧）
     * @param rawData 原始数据（字符串）
     * @param deviceId 设备ID
     * @return CompletableFuture，异步处理结果
     */
    public CompletableFuture<ProtocolMessage> route(String deviceType, String manufacturer, String rawData, Long deviceId) {
        log.debug("[消息路由] 开始路由文本消息，设备类型={}, 厂商={}, 设备ID={}, 数据长度={}",
                deviceType, manufacturer, deviceId, rawData != null ? rawData.length() : 0);

        try {
            // 获取协议处理器
            ProtocolHandler handler = protocolAdapterFactory.getHandler(deviceType, manufacturer);
            if (handler == null) {
                throw new ProtocolProcessException("HANDLER_NOT_FOUND",
                        "未找到协议处理器：设备类型=" + deviceType + ", 厂商=" + manufacturer);
            }

            // 使用协议类型路由
            return route(handler.getProtocolType(), rawData, deviceId);

        } catch (Exception e) {
            log.error("[消息路由] 文本消息路由失败，设备类型={}, 厂商={}, 设备ID={}, 错误={}",
                    deviceType, manufacturer, deviceId, e.getMessage(), e);
            CompletableFuture<ProtocolMessage> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    /**
     * 路由消息（根据协议类型枚举）
     * <p>
     * 根据ProtocolTypeEnum枚举将消息路由到对应的协议处理器
     * </p>
     *
     * @param protocolType 协议类型枚举
     * @param rawData 原始数据（字节数组）
     * @param deviceId 设备ID
     * @return CompletableFuture，异步处理结果
     */
    public CompletableFuture<ProtocolMessage> route(ProtocolTypeEnum protocolType, byte[] rawData, Long deviceId) {
        if (protocolType == null) {
            log.warn("[消息路由] 协议类型枚举为空");
            CompletableFuture<ProtocolMessage> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("协议类型枚举为空"));
            return future;
        }

        return route(protocolType.getCode(), rawData, deviceId);
    }

    /**
     * 路由消息（字符串格式）
     * <p>
     * 路由字符串格式的协议消息
     * </p>
     *
     * @param protocolType 协议类型代码
     * @param rawData 原始数据（字符串，十六进制格式）
     * @param deviceId 设备ID
     * @return CompletableFuture，异步处理结果
     */
    public CompletableFuture<ProtocolMessage> routeString(String protocolType, String rawData, Long deviceId) {
        log.debug("[消息路由] 开始路由字符串消息，协议类型={}, 设备ID={}, 数据长度={}",
                protocolType, deviceId, rawData != null ? rawData.length() : 0);

        // 将十六进制字符串转换为字节数组后路由
        // 注意：rawData应该是十六进制字符串格式（如"AA55..."），需要转换为字节数组
        byte[] bytes = hexStringToBytes(rawData);
        return route(protocolType, bytes, deviceId);
    }

    /**
     * 十六进制字符串转字节数组
     * <p>
     * 将十六进制字符串（如"AA55..."）转换为字节数组
     * </p>
     *
     * @param hex 十六进制字符串
     * @return 字节数组
     */
    private byte[] hexStringToBytes(String hex) {
        if (hex == null || hex.isEmpty()) {
            return new byte[0];
        }
        // 移除可能的空格和分隔符
        hex = hex.replaceAll("\\s+", "").replaceAll("-", "");
        int len = hex.length();
        // 如果长度为奇数，前面补0
        if (len % 2 != 0) {
            hex = "0" + hex;
            len = hex.length();
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}

