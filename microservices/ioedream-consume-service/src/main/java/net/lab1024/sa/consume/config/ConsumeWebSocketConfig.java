package net.lab1024.sa.consume.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import jakarta.annotation.Resource;
import net.lab1024.sa.consume.websocket.ConsumeWebSocketHandler;

/**
 * 消费模块WebSocket配置
 * <p>
 * 配置WebSocket端点，支持实时消费数据推送
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-24
 */
@Configuration
@EnableWebSocket
public class ConsumeWebSocketConfig implements WebSocketConfigurer {

    @Resource
    private ConsumeWebSocketHandler consumeWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(consumeWebSocketHandler, "/ws/consume")
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .setAllowedOrigins("*");
    }
}
