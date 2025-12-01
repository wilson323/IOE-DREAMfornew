package net.lab1024.sa.monitor.websocket;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import lombok.extern.slf4j.Slf4j;

/**
 * 访问监控WebSocket处理器
 *
 * 负责处理监控相关的WebSocket连接，支持实时数据推送
 * 包括：访问监控、系统监控、告警监控等
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Slf4j
@Component
public class AccessMonitorWebSocketHandler extends TextWebSocketHandler {

    /**
     * 存储所有活跃的WebSocket会话
     * Key: sessionId, Value: WebSocketSession
     */
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * 心跳检测调度器
     */
    private final ScheduledExecutorService heartbeatScheduler = Executors.newScheduledThreadPool(1);

    /**
     * 初始化心跳检测任务
     */
    public void startHeartbeatTask() {
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            try {
                sendHeartbeat();
            } catch (Exception e) {
                log.error("发送心跳失败", e);
            }
        }, 30, 30, TimeUnit.SECONDS);
        log.info("WebSocket心跳检测任务已启动");
    }

    /**
     * 连接建立后调用
     *
     * @param session WebSocket会话
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String sessionId = session.getId();
        sessions.put(sessionId, session);

        log.info("WebSocket连接已建立，sessionId: {}, remoteAddress: {}",
                sessionId, session.getRemoteAddress());

        // 发送欢迎消息
        sendWelcomeMessage(session);
    }

    /**
     * 处理接收到的文本消息
     *
     * @param session WebSocket会话
     * @param message 接收到的消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String sessionId = session.getId();
        String payload = message.getPayload();

        log.debug("收到WebSocket消息，sessionId: {}, message: {}", sessionId, payload);

        try {
            JSONObject messageObj = JSON.parseObject(payload);
            String messageType = messageObj.getString("type");

            switch (messageType) {
                case "heartbeat":
                    handleHeartbeat(session, messageObj);
                    break;
                case "subscribe":
                    handleSubscribe(session, messageObj);
                    break;
                case "unsubscribe":
                    handleUnsubscribe(session, messageObj);
                    break;
                default:
                    log.warn("未知的消息类型: {}, sessionId: {}", messageType, sessionId);
                    sendErrorMessage(session, "未知的消息类型: " + messageType);
            }

        } catch (Exception e) {
            log.error("处理WebSocket消息失败，sessionId: {}, message: {}", sessionId, payload, e);
            sendErrorMessage(session, "消息处理失败: " + e.getMessage());
        }
    }

    /**
     * 连接关闭后调用
     *
     * @param session WebSocket会话
     * @param status  关闭状态
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = session.getId();
        sessions.remove(sessionId);

        log.info("WebSocket连接已关闭，sessionId: {}, status: {}", sessionId, status);
    }

    /**
     * 传输错误处理
     *
     * @param session   WebSocket会话
     * @param exception 异常
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        String sessionId = session.getId();
        log.error("WebSocket传输错误，sessionId: {}", sessionId, exception);

        try {
            session.close(CloseStatus.SERVER_ERROR);
        } catch (IOException e) {
            log.error("关闭WebSocket连接失败", e);
        } finally {
            sessions.remove(sessionId);
        }
    }

    /**
     * 发送欢迎消息
     *
     * @param session WebSocket会话
     */
    private void sendWelcomeMessage(WebSocketSession session) {
        try {
            JSONObject welcomeMessage = new JSONObject();
            welcomeMessage.put("type", "connection");
            welcomeMessage.put("status", "connected");
            welcomeMessage.put("sessionId", session.getId());
            welcomeMessage.put("serverTime", LocalDateTime.now().toString());
            welcomeMessage.put("message", "监控WebSocket连接已建立");

            session.sendMessage(new TextMessage(welcomeMessage.toJSONString()));
        } catch (Exception e) {
            log.error("发送欢迎消息失败", e);
        }
    }

    /**
     * 处理心跳消息
     *
     * @param session    WebSocket会话
     * @param messageObj 消息对象
     */
    private void handleHeartbeat(WebSocketSession session, JSONObject messageObj) {
        try {
            JSONObject response = new JSONObject();
            response.put("type", "heartbeat");
            response.put("status", "ok");
            response.put("serverTime", LocalDateTime.now().toString());
            response.put("timestamp", messageObj.getLong("timestamp"));

            session.sendMessage(new TextMessage(response.toJSONString()));
        } catch (Exception e) {
            log.error("处理心跳消息失败", e);
        }
    }

    /**
     * 处理订阅请求
     *
     * @param session    WebSocket会话
     * @param messageObj 消息对象
     */
    private void handleSubscribe(WebSocketSession session, JSONObject messageObj) {
        try {
            String topic = messageObj.getString("topic");
            log.info("收到订阅请求，sessionId: {}, topic: {}", session.getId(), topic);

            JSONObject response = new JSONObject();
            response.put("type", "subscribe");
            response.put("status", "ok");
            response.put("topic", topic);
            response.put("message", "订阅成功");

            session.sendMessage(new TextMessage(response.toJSONString()));
        } catch (Exception e) {
            log.error("处理订阅请求失败", e);
        }
    }

    /**
     * 处理取消订阅请求
     *
     * @param session    WebSocket会话
     * @param messageObj 消息对象
     */
    private void handleUnsubscribe(WebSocketSession session, JSONObject messageObj) {
        try {
            String topic = messageObj.getString("topic");
            log.info("收到取消订阅请求，sessionId: {}, topic: {}", session.getId(), topic);

            JSONObject response = new JSONObject();
            response.put("type", "unsubscribe");
            response.put("status", "ok");
            response.put("topic", topic);
            response.put("message", "取消订阅成功");

            session.sendMessage(new TextMessage(response.toJSONString()));
        } catch (Exception e) {
            log.error("处理取消订阅请求失败", e);
        }
    }

    /**
     * 发送错误消息
     *
     * @param session      WebSocket会话
     * @param errorMessage 错误消息
     */
    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        try {
            JSONObject error = new JSONObject();
            error.put("type", "error");
            error.put("message", errorMessage);
            error.put("serverTime", LocalDateTime.now().toString());

            session.sendMessage(new TextMessage(error.toJSONString()));
        } catch (Exception e) {
            log.error("发送错误消息失败", e);
        }
    }

    /**
     * 发送心跳消息给所有连接的客户端
     */
    private void sendHeartbeat() {
        if (sessions.isEmpty()) {
            return;
        }

        JSONObject heartbeat = new JSONObject();
        heartbeat.put("type", "heartbeat");
        heartbeat.put("serverTime", LocalDateTime.now().toString());

        TextMessage message = new TextMessage(heartbeat.toJSONString());
        sessions.values().removeIf(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(message);
                    return false;
                }
            } catch (Exception e) {
                log.debug("发送心跳失败，sessionId: {}", session.getId(), e);
            }
            return true; // 移除已关闭或出错的会话
        });
    }

    /**
     * 广播消息给所有连接的客户端
     *
     * @param message 消息内容
     */
    public void broadcast(String message) {
        if (sessions.isEmpty()) {
            return;
        }

        TextMessage textMessage = new TextMessage(message);
        sessions.values().removeIf(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                    return false;
                }
            } catch (Exception e) {
                log.debug("广播消息失败，sessionId: {}", session.getId(), e);
            }
            return true; // 移除已关闭或出错的会话
        });
    }

    /**
     * 获取当前连接的会话数量
     *
     * @return 会话数量
     */
    public int getSessionCount() {
        return sessions.size();
    }

    /**
     * 关闭所有连接
     */
    public void closeAllSessions() {
        sessions.values().forEach(session -> {
            try {
                session.close(CloseStatus.GOING_AWAY);
            } catch (Exception e) {
                log.error("关闭会话失败，sessionId: {}", session.getId(), e);
            }
        });
        sessions.clear();
        log.info("所有WebSocket连接已关闭");
    }

    /**
     * 销毁资源
     */
    public void destroy() {
        closeAllSessions();
        heartbeatScheduler.shutdown();
        log.info("WebSocket处理器已销毁");
    }
}
