package net.lab1024.sa.video.config;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.video.manager.WebSocketSessionManager;

/**
 * WebSocket事件监听器
 * <p>
 * 监听WebSocket连接和断开事件，更新会话管理器
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Slf4j
@Component
public class WebSocketEventListener {

    private final WebSocketSessionManager sessionManager;

    /**
     * 构造函数
     *
     * @param sessionManager WebSocket会话管理器
     */
    public WebSocketEventListener(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * WebSocket连接建立事件
     *
     * @param event 连接事件
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        log.info("[WebSocket监听] 客户端已连接: sessionId={}", sessionId);

        // 从用户属性中提取用户ID
        Object userIdObj = headerAccessor.getSessionAttributes().get("userId");
        if (userIdObj instanceof Long) {
            Long userId = (Long) userIdObj;
            log.info("[WebSocket监听] 用户已连接: userId={}, sessionId={}", userId, sessionId);
        }
    }

    /**
     * WebSocket断开连接事件
     *
     * @param event 断开事件
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        log.info("[WebSocket监听] 客户端已断开: sessionId={}", sessionId);

        // 从会话管理器中移除
        sessionManager.removeSession(sessionId);
    }
}
