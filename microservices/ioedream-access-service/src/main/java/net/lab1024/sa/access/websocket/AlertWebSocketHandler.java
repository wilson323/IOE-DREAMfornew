package net.lab1024.sa.access.websocket;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.entity.access.DeviceAlertEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 告警WebSocket处理器
 * <p>
 * 管理WebSocket连接和消息推送：
 * - 连接管理：维护所有活跃的WebSocket会话
 * - 消息推送：向特定用户或所有用户推送告警消息
 * - 心跳检测：定期检测连接状态
 * - 异常处理：连接异常时的清理工作
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class AlertWebSocketHandler extends TextWebSocketHandler {

    /**
     * 存储所有WebSocket会话
     * Key: sessionId, Value: WebSocketSession
     */
    private static final Map<String, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();

    /**
     * 存储用户ID到会话ID的映射（用于向特定用户推送）
     * Key: userId, Value: sessionId
     */
    private static final Map<Long, String> USER_SESSION_MAP = new ConcurrentHashMap<>();

    /**
     * 连接建立成功
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        SESSIONS.put(sessionId, session);

        // 从session属性中获取userId（需要在握手时设置）
        Long userId = extractUserId(session);
        if (userId != null) {
            USER_SESSION_MAP.put(userId, sessionId);
            log.info("[WebSocket] 用户连接成功: userId={}, sessionId={}", userId, sessionId);
        } else {
            log.info("[WebSocket] 匿名连接成功: sessionId={}", sessionId);
        }

        // 发送欢迎消息
        sendWelcomeMessage(session);
    }

    /**
     * 接收到客户端消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("[WebSocket] 收到客户端消息: sessionId={}, message={}", session.getId(), payload);

        try {
            // 解析客户端消息（心跳、订阅等）
            Object messageObj = JSON.parse(payload);
            if (!(messageObj instanceof Map)) {
                log.warn("[WebSocket] 消息格式错误，应为Map类型: payload={}", payload);
                return;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> messageData = (Map<String, Object>) messageObj;
            String messageType = (String) messageData.get("type");

            switch (messageType) {
                case "ping":
                    // 心跳响应
                    sendPongMessage(session);
                    break;
                case "subscribe":
                    // 订阅特定告警类型（可扩展）
                    handleSubscribe(session, messageData);
                    break;
                case "unsubscribe":
                    // 取消订阅（可扩展）
                    handleUnsubscribe(session, messageData);
                    break;
                default:
                    log.warn("[WebSocket] 未知消息类型: type={}", messageType);
            }
        } catch (Exception e) {
            log.error("[WebSocket] 处理客户端消息异常: sessionId={}, error={}", session.getId(), e.getMessage(), e);
        }
    }

    /**
     * 连接关闭
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        SESSIONS.remove(sessionId);

        // 移除用户映射
        Long userId = extractUserId(session);
        if (userId != null) {
            USER_SESSION_MAP.remove(userId);
            log.info("[WebSocket] 用户连接关闭: userId={}, sessionId={}, status={}", userId, sessionId, status);
        } else {
            log.info("[WebSocket] 匿名连接关闭: sessionId={}, status={}", sessionId, status);
        }
    }

    /**
     * 传输异常
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String sessionId = session.getId();
        log.error("[WebSocket] 传输异常: sessionId={}, error={}", sessionId, exception.getMessage(), exception);

        // 清理会话
        SESSIONS.remove(sessionId);
        Long userId = extractUserId(session);
        if (userId != null) {
            USER_SESSION_MAP.remove(userId);
        }
    }

    /**
     * 向所有用户推送告警
     */
    public void broadcastAlert(DeviceAlertEntity alert) {
        log.info("[WebSocket] 广播告警消息: alertId={}, alertType={}, alertLevel={}",
                alert.getAlertId(), alert.getAlertType(), alert.getAlertLevel());

        Map<String, Object> message = buildAlertMessage(alert);
        String messageJson = JSON.toJSONString(message);

        int successCount = 0;
        int failCount = 0;

        for (WebSocketSession session : SESSIONS.values()) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(messageJson));
                    successCount++;
                } catch (IOException e) {
                    log.error("[WebSocket] 发送消息失败: sessionId={}, error={}",
                            session.getId(), e.getMessage());
                    failCount++;
                }
            }
        }

        log.info("[WebSocket] 广播完成: total={}, success={}, fail={}",
                SESSIONS.size(), successCount, failCount);
    }

    /**
     * 向指定用户推送告警
     */
    public void sendAlertToUser(Long userId, DeviceAlertEntity alert) {
        log.info("[WebSocket] 向指定用户推送告警: userId={}, alertId={}", userId, alert.getAlertId());

        String sessionId = USER_SESSION_MAP.get(userId);
        if (sessionId == null) {
            log.warn("[WebSocket] 用户未在线: userId={}", userId);
            return;
        }

        WebSocketSession session = SESSIONS.get(sessionId);
        if (session == null || !session.isOpen()) {
            log.warn("[WebSocket] 用户会话无效: userId={}, sessionId={}", userId, sessionId);
            USER_SESSION_MAP.remove(userId);
            SESSIONS.remove(sessionId);
            return;
        }

        Map<String, Object> message = buildAlertMessage(alert);
        String messageJson = JSON.toJSONString(message);

        try {
            session.sendMessage(new TextMessage(messageJson));
            log.info("[WebSocket] 推送成功: userId={}, alertId={}", userId, alert.getAlertId());
        } catch (IOException e) {
            log.error("[WebSocket] 推送失败: userId={}, error={}", userId, e.getMessage(), e);
        }
    }

    /**
     * 向所有用户推送系统消息
     */
    public void broadcastSystemMessage(String title, String content) {
        log.info("[WebSocket] 广播系统消息: title={}", title);

        Map<String, Object> message = new HashMap<>();
        message.put("type", "SYSTEM");
        message.put("title", title);
        message.put("content", content);
        message.put("timestamp", System.currentTimeMillis());

        String messageJson = JSON.toJSONString(message);

        for (WebSocketSession session : SESSIONS.values()) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(messageJson));
                } catch (IOException e) {
                    log.error("[WebSocket] 发送系统消息失败: sessionId={}, error={}",
                            session.getId(), e.getMessage());
                }
            }
        }
    }

    /**
     * 获取当前在线用户数
     */
    public int getOnlineUserCount() {
        return USER_SESSION_MAP.size();
    }

    /**
     * 获取当前连接数
     */
    public int getConnectionCount() {
        return SESSIONS.size();
    }

    // ==================== 私有方法 ====================

    /**
     * 从session中提取userId
     */
    private Long extractUserId(WebSocketSession session) {
        Object userIdObj = session.getAttributes().get("userId");
        if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        } else if (userIdObj instanceof String) {
            try {
                return Long.parseLong((String) userIdObj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 构建告警消息
     */
    private Map<String, Object> buildAlertMessage(DeviceAlertEntity alert) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "ALERT");
        message.put("alertId", alert.getAlertId());
        message.put("deviceId", alert.getDeviceId());
        message.put("deviceCode", alert.getDeviceCode());
        message.put("deviceName", alert.getDeviceName());
        message.put("alertType", alert.getAlertType());
        message.put("alertLevel", alert.getAlertLevel());
        message.put("alertTitle", alert.getAlertTitle());
        message.put("alertMessage", alert.getAlertMessage());
        message.put("alertOccurredTime", alert.getAlertOccurredTime());
        message.put("timestamp", System.currentTimeMillis());
        return message;
    }

    /**
     * 发送欢迎消息
     */
    private void sendWelcomeMessage(WebSocketSession session) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "WELCOME");
        message.put("content", "连接成功！您将实时接收告警消息。");
        message.put("timestamp", System.currentTimeMillis());

        try {
            session.sendMessage(new TextMessage(JSON.toJSONString(message)));
        } catch (IOException e) {
            log.error("[WebSocket] 发送欢迎消息失败: sessionId={}, error={}",
                    session.getId(), e.getMessage());
        }
    }

    /**
     * 发送Pong响应
     */
    private void sendPongMessage(WebSocketSession session) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "pong");
        message.put("timestamp", System.currentTimeMillis());

        try {
            session.sendMessage(new TextMessage(JSON.toJSONString(message)));
        } catch (IOException e) {
            log.error("[WebSocket] 发送Pong消息失败: sessionId={}, error={}",
                    session.getId(), e.getMessage());
        }
    }

    /**
     * 处理订阅请求（可扩展）
     */
    private void handleSubscribe(WebSocketSession session, Map<String, Object> messageData) {
        String alertType = (String) messageData.get("alertType");
        log.info("[WebSocket] 用户订阅告警类型: sessionId={}, alertType={}", session.getId(), alertType);

        // TODO: 实现订阅逻辑（存储用户的订阅偏好）
        Map<String, Object> response = new HashMap<>();
        response.put("type", "SUBSCRIBE_SUCCESS");
        response.put("alertType", alertType);
        response.put("timestamp", System.currentTimeMillis());

        try {
            session.sendMessage(new TextMessage(JSON.toJSONString(response)));
        } catch (IOException e) {
            log.error("[WebSocket] 发送订阅确认失败: sessionId={}, error={}",
                    session.getId(), e.getMessage());
        }
    }

    /**
     * 处理取消订阅请求（可扩展）
     */
    private void handleUnsubscribe(WebSocketSession session, Map<String, Object> messageData) {
        String alertType = (String) messageData.get("alertType");
        log.info("[WebSocket] 用户取消订阅: sessionId={}, alertType={}", session.getId(), alertType);

        // TODO: 实现取消订阅逻辑
        Map<String, Object> response = new HashMap<>();
        response.put("type", "UNSUBSCRIBE_SUCCESS");
        response.put("alertType", alertType);
        response.put("timestamp", System.currentTimeMillis());

        try {
            session.sendMessage(new TextMessage(JSON.toJSONString(response)));
        } catch (IOException e) {
            log.error("[WebSocket] 发送取消订阅确认失败: sessionId={}, error={}",
                    session.getId(), e.getMessage());
        }
    }
}
