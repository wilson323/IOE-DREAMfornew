package net.lab1024.sa.video.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置类
 * <p>
 * 配置STOMP协议支持，实现实时AI事件推送功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 配置消息代理
     * <p>
     * 使用简单内存消息代理（适用于单机部署）
     * 生产环境可切换到RabbitMQ/ActiveMQ等外部消息代理
     * </p>
     *
     * @param registry 消息代理注册器
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 启用简单消息代理
        // /topic: 广播消息（一对多）
        // /queue: 点对点消息（一对一）
        registry.enableSimpleBroker("/topic", "/queue");

        // 设置应用目标前缀（客户端发送消息的前缀）
        registry.setApplicationDestinationPrefixes("/app");

        // 设置用户目标前缀（用于点对点消息）
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * 注册STOMP端点
     * <p>
     * 客户端通过此端点建立WebSocket连接
     * </p>
     *
     * @param registry STOMP端点注册器
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/video-events")  // WebSocket端点路径
                .setAllowedOriginPatterns("*")   // 允许跨域（生产环境应限制具体域名）
                .withSockJS();                    // 启用SockJS支持（降级方案）
    }
}
