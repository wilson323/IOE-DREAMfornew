package net.lab1024.sa.attendance.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket配置类
 * <p>
 * 配置WebSocket消息代理，支持实时监控数据推送
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    /**
     * 配置消息代理
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        log.info("[WebSocket配置] 初始化消息代理");

        // 启用简单消息代理，用于向客户端发送消息
        config.enableSimpleBroker("/topic", "/queue");

        // 设置应用程序目的地前缀
        config.setApplicationDestinationPrefixes("/app");

        // 设置用户目的地前缀（用于点对点消息）
        config.setUserDestinationPrefix("/user");
    }

    /**
     * 配置STOMP端点
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        log.info("[WebSocket配置] 注册STOMP端点");

        // 注册WebSocket端点，允许跨域访问
        registry.addEndpoint("/ws/attendance")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // 启用SockJS支持（降级方案）
    }
}
