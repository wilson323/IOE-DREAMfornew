package net.lab1024.sa.oa.workflow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * 工作流WebSocket配置
 * 提供实时任务通知和状态更新功能
 * 使用STOMP协议进行消息传输
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 * @version 3.0.0
 */
@Configuration
@EnableWebSocketMessageBroker
@SuppressWarnings("null")
public class WorkflowWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 配置消息代理
     * 
     * @param config 消息代理配置
     */
    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        // 启用简单的内存消息代理，用于向客户端发送消息
        // 客户端订阅地址前缀：/topic（广播）和 /queue（点对点）
        config.enableSimpleBroker("/topic", "/queue");
        
        // 客户端向服务器发送消息的前缀
        config.setApplicationDestinationPrefixes("/app");
        
        // 点对点消息前缀（用户特定的消息）
        config.setUserDestinationPrefix("/user");
    }

    /**
     * 注册STOMP端点
     * 
     * @param registry STOMP端点注册表
     */
    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        // 注册WebSocket端点
        // 客户端连接地址：http://host:port/ws/workflow（使用SockJS时使用http/https协议）
        registry.addEndpoint("/ws/workflow")
                .setAllowedOriginPatterns("*")  // 开发环境允许所有来源，生产环境需要配置具体域名
                .withSockJS();  // 启用SockJS支持（提供降级方案，兼容性更好）
        
        // 也可以同时支持原生WebSocket（不通过SockJS）
        // 客户端连接地址：ws://host:port/ws/workflow-native
        registry.addEndpoint("/ws/workflow-native")
                .setAllowedOriginPatterns("*");
    }
}





