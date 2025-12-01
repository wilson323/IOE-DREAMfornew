package net.lab1024.sa.monitor.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.monitor.websocket.AccessMonitorWebSocketHandler;

/**
 * WebSocket配置类
 *
 * 配置监控服务的WebSocket端点，支持实时数据推送
 *
 * @author IOE-DREAM Team
 */
@Slf4j
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final AccessMonitorWebSocketHandler accessMonitorWebSocketHandler;

    public WebSocketConfig(AccessMonitorWebSocketHandler accessMonitorWebSocketHandler) {
        this.accessMonitorWebSocketHandler = accessMonitorWebSocketHandler;
    }

    @PostConstruct
    public void init() {
        // 启动心跳检测任务
        accessMonitorWebSocketHandler.startHeartbeatTask();
        log.info("WebSocket配置初始化完成");
    }

    @PreDestroy
    public void destroy() {
        // 清理资源
        accessMonitorWebSocketHandler.destroy();
        log.info("WebSocket配置已销毁");
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册访问监控WebSocket处理器
        registry.addHandler(accessMonitorWebSocketHandler, "/ws/access-monitor")
                .setAllowedOrigins("*") // 允许所有来源，生产环境应配置具体域名
                .withSockJS(); // 支持SockJS回退方案

        // 注册系统监控WebSocket处理器
        registry.addHandler(accessMonitorWebSocketHandler, "/ws/system-monitor")
                .setAllowedOrigins("*")
                .withSockJS();

        // 注册告警监控WebSocket处理器
        registry.addHandler(accessMonitorWebSocketHandler, "/ws/alert-monitor")
                .setAllowedOrigins("*")
                .withSockJS();

        log.info("WebSocket处理器注册完成");
    }
}
