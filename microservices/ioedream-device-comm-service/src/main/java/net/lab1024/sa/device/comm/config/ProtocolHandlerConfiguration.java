package net.lab1024.sa.device.comm.config;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.comm.protocol.handler.AccessProtocolHandler;
import net.lab1024.sa.device.comm.protocol.handler.AttendanceProtocolHandler;
import net.lab1024.sa.device.comm.protocol.handler.ConsumeProtocolHandler;
import net.lab1024.sa.device.comm.protocol.client.DeviceProtocolClient;
import net.lab1024.sa.device.comm.protocol.router.MessageRouter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 协议处理器配置类
 * <p>
 * 配置所有设备协议处理器的Bean注册和初始化
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
@Slf4j
@Configuration
public class ProtocolHandlerConfiguration {

    /**
     * 注册协议处理器映射Bean
     * <p>
     * 为DeviceProtocolClient提供协议处理器映射
     * </p>
     *
     * @return 协议处理器映射
     */
    @Bean
    public Map<String, DeviceProtocolClient.ProtocolHandler> protocolHandlerMap(
            AccessProtocolHandler accessProtocolHandler,
            AttendanceProtocolHandler attendanceProtocolHandler,
            ConsumeProtocolHandler consumeProtocolHandler) {

        Map<String, DeviceProtocolClient.ProtocolHandler> handlerMap = new HashMap<>();

        // 注册门禁协议处理器
        handlerMap.put("accessProtocolHandler", accessProtocolHandler::handleCommand);
        log.info("[协议处理器配置] 注册门禁协议处理器: {}", accessProtocolHandler.getClass().getSimpleName());

        // 注册考勤协议处理器
        handlerMap.put("attendanceProtocolHandler", attendanceProtocolHandler::handleCommand);
        log.info("[协议处理器配置] 注册考勤协议处理器: {}", attendanceProtocolHandler.getClass().getSimpleName());

        // 注册消费协议处理器
        handlerMap.put("consumeProtocolHandler", consumeProtocolHandler::handleCommand);
        log.info("[协议处理器配置] 注册消费协议处理器: {}", consumeProtocolHandler.getClass().getSimpleName());

        log.info("[协议处理器配置] 协议处理器映射注册完成，共{}个处理器", handlerMap.size());
        return handlerMap;
    }

    /**
     * 注册消息路由器协议处理器映射Bean
     * <p>
     * 为MessageRouter提供协议处理器映射
     * </p>
     *
     * @return 协议处理器映射
     */
    @Bean
    public Map<String, MessageRouter.ProtocolHandler> messageRouterHandlerMap(
            AccessProtocolHandler accessProtocolHandler,
            AttendanceProtocolHandler attendanceProtocolHandler,
            ConsumeProtocolHandler consumeProtocolHandler) {

        Map<String, MessageRouter.ProtocolHandler> handlerMap = new HashMap<>();

        // 注册门禁协议处理器
        handlerMap.put("ACCESS", accessProtocolHandler::handleCommand);
        log.info("[消息路由器配置] 注册门禁协议处理器: {}", accessProtocolHandler.getClass().getSimpleName());

        // 注册考勤协议处理器
        handlerMap.put("ATTENDANCE", attendanceProtocolHandler::handleCommand);
        log.info("[消息路由器配置] 注册考勤协议处理器: {}", attendanceProtocolHandler.getClass().getSimpleName());

        // 注册消费协议处理器
        handlerMap.put("CONSUME", consumeProtocolHandler::handleCommand);
        log.info("[消息路由器配置] 注册消费协议处理器: {}", consumeProtocolHandler.getClass().getSimpleName());

        log.info("[消息路由器配置] 消息路由器协议处理器映射注册完成，共{}个处理器", handlerMap.size());
        return handlerMap;
    }

    /**
     * 初始化消息路由器
     * <p>
     * 向MessageRouter注册所有协议处理器
     * </p>
     *
     * @param messageRouter           消息路由器
     * @param messageRouterHandlerMap 协议处理器映射
     */
    @Bean
    public void initializeMessageRouter(
            MessageRouter messageRouter,
            Map<String, MessageRouter.ProtocolHandler> messageRouterHandlerMap) {

        // 向消息路由器注册所有处理器
        for (Map.Entry<String, MessageRouter.ProtocolHandler> entry : messageRouterHandlerMap.entrySet()) {
            messageRouter.registerHandler(entry.getKey(), entry.getValue());
        }

        log.info("[消息路由器配置] 消息路由器初始化完成");
    }
}