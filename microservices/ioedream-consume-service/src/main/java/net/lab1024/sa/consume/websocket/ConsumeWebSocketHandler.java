package net.lab1024.sa.consume.websocket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消费模块WebSocket处理器
 * <p>
 * 处理WebSocket连接、消息接收和实时数据推送
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-24
 */
@Slf4j
@Component
public class ConsumeWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 存储用户ID到WebSocket会话的映射
    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    // 存储会话ID到用户ID的映射
    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("[WebSocket] 新连接建立: sessionId={}", session.getId());

        // 从URL参数中获取userId
        String uri = session.getUri().toString();
        Long userId = extractUserIdFromUri(uri);

        if (userId != null) {
            userSessions.put(userId, session);
            sessionUserMap.put(session.getId(), userId);
            log.info("[WebSocket] 用户连接成功: userId={}, sessionId={}", userId, session.getId());

            // 发送欢迎消息
            sendWelcomeMessage(session, userId);
        } else {
            log.warn("[WebSocket] 连接缺少userId参数: sessionId={}", session.getId());
            session.close();
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("[WebSocket] 收到消息: sessionId={}, message={}", session.getId(), payload);

        try {
            Map<String, Object> messageData = objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {});
            String action = (String) messageData.get("action");

            if ("ping".equals(action)) {
                // 心跳检测
                sendPongMessage(session);
            } else if ("subscribe".equals(action)) {
                // 订阅特定类型的消息
                handleSubscribe(session, messageData);
            }
        } catch (Exception e) {
            log.error("[WebSocket] 消息处理失败: sessionId={}, error={}", session.getId(), e.getMessage(), e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("[WebSocket] 连接关闭: sessionId={}, status={}", session.getId(), status);

        Long userId = sessionUserMap.remove(session.getId());
        if (userId != null) {
            userSessions.remove(userId);
            log.info("[WebSocket] 用户断开连接: userId={}, sessionId={}", userId, session.getId());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("[WebSocket] 传输错误: sessionId={}, error={}",
                session.getId(), exception.getMessage(), exception);

        Long userId = sessionUserMap.remove(session.getId());
        if (userId != null) {
            userSessions.remove(userId);
        }
    }

    /**
     * 向指定用户发送实时消费通知
     */
    public void sendConsumeNotification(Long userId, Map<String, Object> consumeData) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                Map<String, Object> message = Map.of(
                        "type", "consume_notification",
                        "data", consumeData,
                        "timestamp", System.currentTimeMillis()
                );

                String jsonMessage = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(jsonMessage));

                log.info("[WebSocket] 发送消费通知: userId={}, amount={}",
                        userId, consumeData.get("amount"));
            } catch (IOException e) {
                log.error("[WebSocket] 发送消息失败: userId={}, error={}",
                        userId, e.getMessage(), e);
            }
        }
    }

    /**
     * 向指定用户发送账户余额变动通知
     */
    public void sendBalanceUpdate(Long userId, Map<String, Object> balanceData) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                Map<String, Object> message = Map.of(
                        "type", "balance_update",
                        "data", balanceData,
                        "timestamp", System.currentTimeMillis()
                );

                String jsonMessage = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(jsonMessage));

                log.info("[WebSocket] 发送余额更新: userId={}, balance={}",
                        userId, balanceData.get("balance"));
            } catch (IOException e) {
                log.error("[WebSocket] 发送消息失败: userId={}, error={}",
                        userId, e.getMessage(), e);
            }
        }
    }

    /**
     * 广播系统公告
     */
    public void broadcastSystemNotification(String title, String content) {
        Map<String, Object> notification = Map.of(
                "type", "system_notification",
                "data", Map.of(
                        "title", title,
                        "content", content
                ),
                "timestamp", System.currentTimeMillis()
        );

        String jsonMessage;
        try {
            jsonMessage = objectMapper.writeValueAsString(notification);
        } catch (Exception e) {
            log.error("[WebSocket] 消息序列化失败", e);
            return;
        }

        userSessions.forEach((userId, session) -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(jsonMessage));
                } catch (IOException e) {
                    log.error("[WebSocket] 广播消息失败: userId={}", userId, e);
                }
            }
        });

        log.info("[WebSocket] 广播系统公告: title={}, receivers={}", title, userSessions.size());
    }

    /**
     * 发送欢迎消息
     */
    private void sendWelcomeMessage(WebSocketSession session, Long userId) {
        try {
            Map<String, Object> message = Map.of(
                    "type", "welcome",
                    "data", Map.of(
                            "userId", userId,
                            "message", "WebSocket连接成功"
                    ),
                    "timestamp", System.currentTimeMillis()
            );

            String jsonMessage = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(jsonMessage));
        } catch (Exception e) {
            log.error("[WebSocket] 发送欢迎消息失败: userId={}", userId, e);
        }
    }

    /**
     * 发送Pong响应
     */
    private void sendPongMessage(WebSocketSession session) {
        try {
            Map<String, Object> message = Map.of(
                    "type", "pong",
                    "timestamp", System.currentTimeMillis()
            );

            String jsonMessage = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(jsonMessage));
        } catch (Exception e) {
            log.error("[WebSocket] 发送Pong失败", e);
        }
    }

    /**
     * 处理订阅请求
     */
    private void handleSubscribe(WebSocketSession session, Map<String, Object> messageData) {
        String messageType = (String) messageData.get("messageType");
        log.info("[WebSocket] 用户订阅: sessionId={}, messageType={}", session.getId(), messageType);

        // 可以在这里实现更复杂的订阅逻辑
    }

    /**
     * 从URI中提取userId
     */
    private Long extractUserIdFromUri(String uri) {
        try {
            if (uri.contains("?userId=")) {
                String userIdStr = uri.substring(uri.indexOf("?userId=") + 8);
                if (userIdStr.contains("&")) {
                    userIdStr = userIdStr.substring(0, userIdStr.indexOf("&"));
                }
                return Long.parseLong(userIdStr);
            }
        } catch (Exception e) {
            log.error("[WebSocket] 提取userId失败: uri={}", uri, e);
        }
        return null;
    }

    /**
     * 获取在线用户数
     */
    public int getOnlineUserCount() {
        return userSessions.size();
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }
}
