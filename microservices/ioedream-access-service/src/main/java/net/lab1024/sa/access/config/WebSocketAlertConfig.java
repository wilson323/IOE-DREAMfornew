package net.lab1024.sa.access.config;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.websocket.AlertWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import jakarta.annotation.Resource;

/**
 * WebSocket告警推送配置
 * <p>
 * 配置WebSocket端点，支持实时告警推送：
 * - 端点路径: /ws/alert
 * - 支持用户认证token连接
 * - 支持订阅特定告警类型
 * - 自动心跳检测
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Configuration
@EnableWebSocket
public class WebSocketAlertConfig implements WebSocketConfigurer {

    @Resource
    private AlertWebSocketHandler alertWebSocketHandler;

    /**
     * WebSocket端点路径
     */
    public static final String ALERT_ENDPOINT = "/ws/alert";

    /**
     * 允许的源地址（开发环境允许所有源，生产环境需要限制）
     */
    private static final String[] ALLOWED_ORIGINS = new String[]{"*"};

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        log.info("[WebSocket配置] 注册告警WebSocket端点: path={}", ALERT_ENDPOINT);

        registry.addHandler(alertWebSocketHandler, ALERT_ENDPOINT)
                .setAllowedOrigins(ALLOWED_ORIGINS)
                .withSockJS(); // 启用SockJS支持（降级方案）

        log.info("[WebSocket配置] WebSocket端点注册成功，支持SockJS降级");
    }

    /**
     * 如果使用@ServerEndpoint注解方式，需要注入ServerEndpointExporter
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        log.info("[WebSocket配置] 注入ServerEndpointExporter");
        return new ServerEndpointExporter();
    }
}
