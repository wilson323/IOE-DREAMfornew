package net.lab1024.sa.attendance.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置类
 * <p>
 * 配置STOMP协议的WebSocket消息代理
 * 支持点对点（/queue）和广播（/topic）消息
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单消息代理，用于发送消息到客户端
        // /queue: 点对点消息（私人队列）
        // /topic: 广播消息（公共主题）
        config.enableSimpleBroker("/topic", "/queue");

        // 设置应用目的地前缀
        config.setApplicationDestinationPrefixes("/app");

        // 设置用户目的地前缀
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册WebSocket端点
        // 允许跨域访问
        // 支持SockJS降级方案
        registry.addEndpoint("/ws/attendance")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
