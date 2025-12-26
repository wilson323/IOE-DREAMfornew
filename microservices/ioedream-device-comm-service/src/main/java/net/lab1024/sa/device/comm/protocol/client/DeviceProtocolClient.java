package net.lab1024.sa.device.comm.protocol.client;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.comm.protocol.domain.DeviceCommandRequest;
import net.lab1024.sa.device.comm.protocol.domain.ProtocolProcessResult;
import net.lab1024.sa.device.comm.protocol.exception.ProtocolProcessException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 设备协议客户端
 * <p>
 * 提供设备通讯协议的统一客户端接口，支持同步和异步调用
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
@Slf4j
@Component
public class DeviceProtocolClient {

    /**
     * 协议处理器映射
     */
    private final Map<String, ProtocolHandler> protocolHandlers = new ConcurrentHashMap<>();

    /**
     * 异步执行器
     */
    private final Executor asyncExecutor = Executors.newFixedThreadPool(10);

    /**
     * 构造函数，注入所有协议处理器
     * <p>
     * Spring 4.3+ 会自动识别单一构造函数并进行依赖注入，无需@Resource注解
     * </p>
     */
    public DeviceProtocolClient(Map<String, ProtocolHandler> handlerMap) {
        // 将所有实现了ProtocolHandler接口的Bean注册到处理器映射中
        for (Map.Entry<String, ProtocolHandler> entry : handlerMap.entrySet()) {
            String protocolType = extractProtocolType(entry.getKey());
            if (protocolType != null) {
                protocolHandlers.put(protocolType.toUpperCase(), entry.getValue());
                log.info("[设备协议客户端] 注册协议处理器: protocolType={}, handler={}",
                        protocolType, entry.getValue().getClass().getSimpleName());
            }
        }
    }

    /**
     * 同步执行设备命令
     *
     * @param protocolType 协议类型
     * @param request      设备命令请求
     * @return 协议处理结果
     * @throws ProtocolProcessException 处理失败时抛出异常
     */
    public ProtocolProcessResult executeCommand(String protocolType, DeviceCommandRequest request) {
        ProtocolHandler handler = getProtocolHandler(protocolType);
        try {
            log.debug("[设备协议客户端] 执行同步命令: protocolType={}, deviceId={}, commandType={}",
                    protocolType, request.getDeviceId(), request.getCommandType());

            return handler.handleCommand(request);
        } catch (Exception e) {
            log.error("[设备协议客户端] 同步命令执行失败: protocolType={}, deviceId={}",
                    protocolType, request.getDeviceId(), e);
            throw new ProtocolProcessException("COMMAND_EXECUTION_FAILED",
                    String.format("协议命令执行失败: %s", e.getMessage()),
                    request.getDeviceId(),
                    protocolType,
                    e);
        }
    }

    /**
     * 异步执行设备命令
     *
     * @param protocolType 协议类型
     * @param request      设备命令请求
     * @return 异步处理结果
     */
    public CompletableFuture<ProtocolProcessResult> executeCommandAsync(String protocolType, DeviceCommandRequest request) {
        ProtocolHandler handler = getProtocolHandler(protocolType);

        log.debug("[设备协议客户端] 执行异步命令: protocolType={}, deviceId={}, commandType={}",
                protocolType, request.getDeviceId(), request.getCommandType());

        return CompletableFuture.supplyAsync(() -> {
            try {
                return handler.handleCommand(request);
            } catch (Exception e) {
                log.error("[设备协议客户端] 异步命令执行失败: protocolType={}, deviceId={}",
                        protocolType, request.getDeviceId(), e);
                throw new ProtocolProcessException("ASYNC_COMMAND_EXECUTION_FAILED",
                        String.format("异步协议命令执行失败: %s", e.getMessage()),
                        request.getDeviceId(),
                        protocolType,
                        e);
            }
        }, asyncExecutor);
    }

    /**
     * 带超时的同步执行设备命令
     *
     * @param protocolType 协议类型
     * @param request      设备命令请求
     * @param timeout      超时时间
     * @param timeUnit     时间单位
     * @return 协议处理结果
     * @throws ProtocolProcessException 处理失败或超时时抛出异常
     */
    public ProtocolProcessResult executeCommandWithTimeout(String protocolType, DeviceCommandRequest request,
                                                            long timeout, TimeUnit timeUnit) {
        try {
            log.debug("[设备协议客户端] 执行带超时命令: protocolType={}, deviceId={}, timeout={} {}",
                    protocolType, request.getDeviceId(), timeout, timeUnit);

            ProtocolProcessResult result = executeCommandAsync(protocolType, request)
                    .get(timeout, timeUnit);

            log.debug("[设备协议客户端] 带超时命令执行成功: protocolType={}, deviceId={}",
                    protocolType, request.getDeviceId());

            return result;
        } catch (Exception e) {
            log.error("[设备协议客户端] 带超时命令执行失败: protocolType={}, deviceId={}, timeout={} {}",
                    protocolType, request.getDeviceId(), timeout, timeUnit, e);

            String errorMessage = String.format("协议命令执行超时或失败: %s", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            throw new ProtocolProcessException("COMMAND_TIMEOUT_OR_FAILED",
                    errorMessage,
                    request.getDeviceId(),
                    protocolType,
                    e);
        }
    }

    /**
     * 批量执行设备命令
     *
     * @param protocolType 协议类型
     * @param requests     设备命令请求列表
     * @return 处理结果映射（requestId -> result）
     */
    public Map<String, ProtocolProcessResult> executeBatchCommands(String protocolType, DeviceCommandRequest... requests) {
        Map<String, ProtocolProcessResult> results = new ConcurrentHashMap<>();

        if (requests == null || requests.length == 0) {
            return results;
        }

        log.info("[设备协议客户端] 执行批量命令: protocolType={}, count={}", protocolType, requests.length);

        // 并行执行所有命令
        CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                java.util.Arrays.stream(requests)
                        .map(request -> {
                            String requestId = generateRequestId(request);
                            return executeCommandAsync(protocolType, request)
                                    .thenAccept(result -> results.put(requestId, result))
                                    .exceptionally(throwable -> {
                                        log.error("[设备协议客户端] 批量命令执行失败: requestId={}, deviceId={}",
                                                requestId, request.getDeviceId(), throwable);

                                        ProtocolProcessResult errorResult = ProtocolProcessResult.builder()
                                                .success(false)
                                                .businessType(protocolType)
                                                .errorCode("BATCH_COMMAND_FAILED")
                                                .message("批量命令执行失败: " + throwable.getMessage())
                                                .build();

                                        results.put(requestId, errorResult);
                                        return null;
                                    });
                        })
                        .toArray(CompletableFuture[]::new)
        );

        try {
            // 等待所有任务完成
            allTasks.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("[设备协议客户端] 批量命令执行超时: protocolType={}", protocolType, e);
            throw new ProtocolProcessException("BATCH_COMMAND_TIMEOUT",
                    "批量命令执行超时",
                    null,
                    protocolType,
                    e);
        }

        log.info("[设备协议客户端] 批量命令执行完成: protocolType={}, total={}, success={}",
                protocolType, requests.length, results.values().stream().mapToInt(r -> r.isSuccess() ? 1 : 0).sum());

        return results;
    }

    /**
     * 检查协议类型是否支持
     *
     * @param protocolType 协议类型
     * @return 是否支持
     */
    public boolean isProtocolSupported(String protocolType) {
        return protocolHandlers.containsKey(protocolType.toUpperCase());
    }

    /**
     * 获取支持的协议类型列表
     *
     * @return 协议类型列表
     */
    public java.util.Set<String> getSupportedProtocols() {
        return protocolHandlers.keySet();
    }

    /**
     * 获取协议处理器
     *
     * @param protocolType 协议类型
     * @return 协议处理器
     * @throws ProtocolProcessException 协议类型不支持时抛出异常
     */
    private ProtocolHandler getProtocolHandler(String protocolType) {
        if (protocolType == null || protocolType.trim().isEmpty()) {
            throw new ProtocolProcessException("PROTOCOL_TYPE_REQUIRED", "协议类型不能为空");
        }

        String upperProtocolType = protocolType.toUpperCase();
        ProtocolHandler handler = protocolHandlers.get(upperProtocolType);

        if (handler == null) {
            String errorMessage = String.format("不支持的协议类型: %s，支持的类型: %s",
                    protocolType, String.join(", ", getSupportedProtocols()));
            throw new ProtocolProcessException("PROTOCOL_TYPE_NOT_SUPPORTED", errorMessage);
        }

        return handler;
    }

    /**
     * 从Bean名称中提取协议类型
     *
     * @param beanName Bean名称
     * @return 协议类型
     */
    private String extractProtocolType(String beanName) {
        if (beanName == null || beanName.trim().isEmpty()) {
            return null;
        }

        // 例如: "accessProtocolHandler" -> "ACCESS"
        if (beanName.toLowerCase().endsWith("protocolhandler")) {
            return beanName.substring(0, beanName.length() - "protocolhandler".length());
        }

        return null;
    }

    /**
     * 生成请求ID
     *
     * @param request 设备命令请求
     * @return 请求ID
     */
    private String generateRequestId(DeviceCommandRequest request) {
        return String.format("%s_%s_%s_%d",
                request.getDeviceId(),
                request.getCommandType(),
                request.getProtocolType() != null ? request.getProtocolType() : "UNKNOWN",
                System.currentTimeMillis());
    }

    /**
     * 协议处理器接口
     */
    public interface ProtocolHandler {
        /**
         * 处理设备命令
         *
         * @param request 设备命令请求
         * @return 协议处理结果
         * @throws ProtocolProcessException 处理失败时抛出异常
         */
        ProtocolProcessResult handleCommand(DeviceCommandRequest request);
    }
}