package net.lab1024.sa.device.comm.protocol.router;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.comm.protocol.domain.DeviceCommandRequest;
import net.lab1024.sa.device.comm.protocol.domain.ProtocolProcessResult;
import net.lab1024.sa.device.comm.protocol.exception.ProtocolProcessException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 消息路由器
 * <p>
 * 负责将设备协议消息路由到对应的处理器，支持动态路由和负载均衡
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
@Slf4j
@Component
public class MessageRouter {

    /**
     * 协议处理器映射
     */
    private final Map<String, ProtocolHandler> handlers = new ConcurrentHashMap<>();

    /**
     * 协议类型映射
     */
    private final Map<String, String> protocolTypeMapping = new ConcurrentHashMap<>();

    /**
     * 初始化路由器
     */
    public MessageRouter() {
        initializeDefaultRoutes();
    }

    /**
     * 初始化默认路由规则
     */
    private void initializeDefaultRoutes() {
        // 设备ID前缀到协议类型的映射
        registerProtocolTypeByPrefix("ACC_", "ACCESS");
        registerProtocolTypeByPrefix("ATT_", "ATTENDANCE");
        registerProtocolTypeByPrefix("CON_", "CONSUME");
        registerProtocolTypeByPrefix("VIS_", "VISITOR");
        registerProtocolTypeByPrefix("VID_", "VIDEO");

        // 命令类型到协议类型的映射
        registerProtocolTypeByCommand("OPEN_DOOR", "ACCESS");
        registerProtocolTypeByCommand("CLOSE_DOOR", "ACCESS");
        registerProtocolTypeByCommand("VERIFY_PERMISSION", "ACCESS");
        registerProtocolTypeByCommand("PUNCH_IN", "ATTENDANCE");
        registerProtocolTypeByCommand("PUNCH_OUT", "ATTENDANCE");
        registerProtocolTypeByCommand("CONSUME", "CONSUME");
        registerProtocolTypeByCommand("REFUND", "CONSUME");

        log.info("[消息路由器] 初始化完成，注册了{}个默认路由规则",
                protocolTypeMapping.size());
    }

    /**
     * 注册协议处理器
     *
     * @param protocolType 协议类型
     * @param handler      处理器
     */
    public void registerHandler(String protocolType, ProtocolHandler handler) {
        if (protocolType != null && handler != null) {
            String upperType = protocolType.toUpperCase();
            handlers.put(upperType, handler);
            log.info("[消息路由器] 注册处理器: protocolType={}, handler={}",
                    upperType, handler.getClass().getSimpleName());
        }
    }

    /**
     * 根据设备ID前缀注册协议类型
     *
     * @param deviceIdPrefix 设备ID前缀
     * @param protocolType    协议类型
     */
    public void registerProtocolTypeByPrefix(String deviceIdPrefix, String protocolType) {
        protocolTypeMapping.put("PREFIX:" + deviceIdPrefix.toUpperCase(), protocolType.toUpperCase());
    }

    /**
     * 根据命令类型注册协议类型
     *
     * @param commandType  命令类型
     * @param protocolType 协议类型
     */
    public void registerProtocolTypeByCommand(String commandType, String protocolType) {
        protocolTypeMapping.put("COMMAND:" + commandType.toUpperCase(), protocolType.toUpperCase());
    }

    /**
     * 路由消息到对应的处理器
     *
     * @param request 设备命令请求
     * @return 协议处理结果
     * @throws ProtocolProcessException 路由失败时抛出异常
     */
    public ProtocolProcessResult route(DeviceCommandRequest request) {
        if (request == null) {
            throw new ProtocolProcessException("REQUEST_NULL", "设备命令请求不能为空");
        }

        String protocolType = determineProtocolType(request);
        ProtocolHandler handler = getHandler(protocolType);

        log.debug("[消息路由器] 路由消息: deviceId={}, commandType={}, protocolType={}",
                request.getDeviceId(), request.getCommandType(), protocolType);

        try {
            return handler.handle(request);
        } catch (Exception e) {
            log.error("[消息路由器] 消息处理失败: deviceId={}, protocolType={}, error={}",
                    request.getDeviceId(), protocolType, e.getMessage(), e);
            throw new ProtocolProcessException("MESSAGE_ROUTING_FAILED",
                    String.format("消息路由处理失败: %s", e.getMessage()),
                    request.getDeviceId(),
                    protocolType,
                    e);
        }
    }

    /**
     * 异步路由消息
     *
     * @param request 设备命令请求
     * @return 异步处理结果
     */
    public java.util.concurrent.CompletableFuture<ProtocolProcessResult> routeAsync(DeviceCommandRequest request) {
        return java.util.concurrent.CompletableFuture.supplyAsync(() -> route(request));
    }

    /**
     * 带回调的路由消息
     *
     * @param request  设备命令请求
     * @param callback 回调函数
     * @return 处理结果
     */
    public ProtocolProcessResult routeWithCallback(DeviceCommandRequest request,
                                                   Function<ProtocolProcessResult, Void> callback) {
        ProtocolProcessResult result = route(request);

        if (callback != null) {
            try {
                callback.apply(result);
            } catch (Exception e) {
                log.warn("[消息路由器] 回调执行失败: deviceId={}, error={}",
                        request.getDeviceId(), e.getMessage());
            }
        }

        return result;
    }

    /**
     * 确定协议类型
     *
     * @param request 设备命令请求
     * @return 协议类型
     * @throws ProtocolProcessException 无法确定协议类型时抛出异常
     */
    private String determineProtocolType(DeviceCommandRequest request) {
        // 1. 优先使用请求中指定的协议类型
        if (request.getProtocolType() != null && !request.getProtocolType().trim().isEmpty()) {
            return request.getProtocolType().toUpperCase();
        }

        // 2. 根据命令类型确定协议类型
        String commandType = request.getCommandType();
        if (commandType != null) {
            String protocolType = protocolTypeMapping.get("COMMAND:" + commandType.toUpperCase());
            if (protocolType != null) {
                log.debug("[消息路由器] 根据命令类型确定协议: commandType={}, protocolType={}",
                        commandType, protocolType);
                return protocolType;
            }
        }

        // 3. 根据设备ID前缀确定协议类型
        if (request.getDeviceId() != null) {
            String deviceIdStr = String.valueOf(request.getDeviceId());
            for (Map.Entry<String, String> entry : protocolTypeMapping.entrySet()) {
                if (entry.getKey().startsWith("PREFIX:")) {
                    String prefix = entry.getKey().substring("PREFIX:".length());
                    if (deviceIdStr.startsWith(prefix)) {
                        String protocolType = entry.getValue();
                        log.debug("[消息路由器] 根据设备ID前缀确定协议: deviceId={}, prefix={}, protocolType={}",
                                deviceIdStr, prefix, protocolType);
                        return protocolType;
                    }
                }
            }
        }

        // 4. 无法确定协议类型
        String errorMessage = String.format("无法确定协议类型: deviceId=%s, commandType=%s",
                request.getDeviceId(), request.getCommandType());
        throw new ProtocolProcessException("PROTOCOL_TYPE_DETERMINATION_FAILED", errorMessage);
    }

    /**
     * 获取处理器
     *
     * @param protocolType 协议类型
     * @return 协议处理器
     * @throws ProtocolProcessException 处理器不存在时抛出异常
     */
    private ProtocolHandler getHandler(String protocolType) {
        if (protocolType == null || protocolType.trim().isEmpty()) {
            throw new ProtocolProcessException("PROTOCOL_TYPE_REQUIRED", "协议类型不能为空");
        }

        String upperType = protocolType.toUpperCase();
        ProtocolHandler handler = handlers.get(upperType);

        if (handler == null) {
            String errorMessage = String.format("未找到协议处理器: %s，已注册的处理器: %s",
                    protocolType, String.join(", ", handlers.keySet()));
            throw new ProtocolProcessException("PROTOCOL_HANDLER_NOT_FOUND", errorMessage);
        }

        return handler;
    }

    /**
     * 获取支持的协议类型
     *
     * @return 协议类型集合
     */
    public java.util.Set<String> getSupportedProtocols() {
        return handlers.keySet();
    }

    /**
     * 获取路由统计信息
     *
     * @return 路由统计信息
     */
    public Map<String, Object> getRoutingStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("registeredHandlers", handlers.size());
        stats.put("routingRules", protocolTypeMapping.size());
        stats.put("supportedProtocols", handlers.keySet());
        stats.put("protocolTypeMappings", protocolTypeMapping);
        return stats;
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
        ProtocolProcessResult handle(DeviceCommandRequest request);
    }
}