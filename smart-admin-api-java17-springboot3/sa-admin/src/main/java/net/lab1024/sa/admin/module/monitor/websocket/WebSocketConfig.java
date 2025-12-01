package net.lab1024.sa.admin.module.monitor.websocket;


import jakarta.annotation.Resource;
import jakarta.websocket.Endpoint;
import jakarta.websocket.server.ServerEndpointConfig;
import net.lab1024.sa.base.common.cache.RedisUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket配置类
 * <p>
 * 严格遵循repowiki规范：
 * - 配置WebSocket端点导出器
 * - 初始化会话管理器
 * - 配置心跳检测和清理任务
 * - 提供连接统计和监控功能
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
import lombok.extern.slf4j.Slf4j;
@Configuration
public class WebSocketConfig {

    @Resource
    private WebSocketSessionManager sessionManager;

    /**
     * WebSocket端点导出器
     * 用于自动注册所有带@ServerEndpoint注解的WebSocket端点
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        log.info("初始化WebSocket端点导出器");
        return new ServerEndpointExporter();
    }

    /**
     * WebSocket会话管理器
     */
    @Bean
    public WebSocketSessionManager webSocketSessionManager() {
        log.info("初始化WebSocket会话管理器");
        WebSocketSessionManager manager = new WebSocketSessionManager();

        // 启动心跳检测任务
        manager.startHeartbeatTask();

        return manager;
    }

    /**
     * 自定义WebSocket配置
     */
    @Bean
    public ServerEndpointConfig.Builder serverEndpointConfigBuilder() {
        // ServerEndpointConfig是抽象类，不能直接实例化
        // 只能通过Builder模式创建配置
        log.info("WebSocket配置构建器初始化完成");
        return ServerEndpointConfig.Builder.create(null, "/");
    }

    /**
     * WebSocket连接统计服务
     */
    @Bean
    public WebSocketConnectionStats connectionStats() {
        log.info("初始化WebSocket连接统计服务");
        return new WebSocketConnectionStats();
    }

    /**
     * WebSocket消息处理器
     */
    @Bean
    public WebSocketMessageHandler messageHandler() {
        log.info("初始化WebSocket消息处理器");
        return new WebSocketMessageHandler();
    }
}