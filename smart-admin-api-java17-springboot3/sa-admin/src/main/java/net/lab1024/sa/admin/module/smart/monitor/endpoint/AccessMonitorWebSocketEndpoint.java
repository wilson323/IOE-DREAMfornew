package net.lab1024.sa.admin.module.smart.monitor.endpoint;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.smart.monitor.websocket.WebSocketSessionManager;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 门禁实时监控WebSocket端点
 * <p>
 * 严格遵循repowiki规范：
 * - 处理WebSocket连接生命周期
 * - 实现实时门禁事件推送
 * - 支持心跳检测和会话管理
 * - 提供消息格式化和验证
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Slf4j
@Component
@ServerEndpoint("/api/websocket/access-monitor/{userId}/{userType}")
public class AccessMonitorWebSocketEndpoint {

    // 使用静态变量注入Spring Bean
    private static WebSocketSessionManager sessionManager;

    @Resource
    public void setSessionManager(WebSocketSessionManager sessionManager) {
        AccessMonitorWebSocketEndpoint.sessionManager = sessionManager;
    }

    /**
     * 连接建立时调用
     */
    @OnOpen
    public void onOpen(Session session,
                      @PathParam("userId") String userId,
                      @PathParam("userType") String userType) {
        try {
            String sessionId = UUID.randomUUID().toString();

            // 获取客户端信息
            String clientInfo = buildClientInfo(session);

            // 添加会话
            sessionManager.addSession(sessionId, session, userId, userType, clientInfo);

            // 发送连接成功消息
            JSONObject welcomeMessage = new JSONObject();
            welcomeMessage.put("type", "connection");
            welcomeMessage.put("status", "connected");
            welcomeMessage.put("sessionId", sessionId);
            welcomeMessage.put("serverTime", LocalDateTime.now().toString());
            welcomeMessage.put("message", "门禁实时监控连接已建立");

            session.getAsyncRemote().sendText(welcomeMessage.toJSONString());

            // 推送初始数据
            pushInitialData(session, userId, userType);

            log.info("WebSocket连接已建立，sessionId: {}, userId: {}, userType: {}", sessionId, userId, userType);

        } catch (Exception e) {
            log.error("WebSocket连接建立失败，userId: {}, userType: {}", userId, userType, e);
            try {
                session.close();
            } catch (Exception ex) {
                log.error("关闭WebSocket连接失败", ex);
            }
        }
    }

    /**
     * 连接关闭时调用
     */
    @OnClose
    public void onClose(Session session,
                       @PathParam("userId") String userId,
                       @PathParam("userType") String userType) {
        log.info("WebSocket连接已关闭，userId: {}, userType: {}", userId, userType);
        // 会话清理在SessionManager中自动处理
    }

    /**
     * 收到客户端消息时调用
     */
    @OnMessage
    public void onMessage(String message,
                         Session session,
                         @PathParam("userId") String userId,
                         @PathParam("userType") String userType) {
        try {
            log.debug("收到WebSocket消息，userId: {}, message: {}", userId, message);

            JSONObject messageObj = JSON.parseObject(message);
            String messageType = messageObj.getString("type");

            switch (messageType) {
                case "heartbeat":
                    handleHeartbeat(session, userId);
                    break;
                case "subscribe":
                    handleSubscribe(session, userId, messageObj);
                    break;
                case "unsubscribe":
                    handleUnsubscribe(session, userId, messageObj);
                    break;
                case "device_status":
                    handleDeviceStatusQuery(session, userId, messageObj);
                    break;
                default:
                    log.warn("未知的WebSocket消息类型: {}, userId: {}", messageType, userId);
                    sendErrorMessage(session, "未知的消息类型: " + messageType);
            }

        } catch (Exception e) {
            log.error("处理WebSocket消息失败，userId: {}, message: {}", userId, message, e);
            sendErrorMessage(session, "消息处理失败: " + e.getMessage());
        }
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session,
                      Throwable error,
                      @PathParam("userId") String userId,
                      @PathParam("userType") String userType) {
        log.error("WebSocket连接发生错误，userId: {}, userType: {}", userId, userType, error);
    }

    /**
     * 处理心跳消息
     */
    private void handleHeartbeat(Session session, String userId) {
        // 更新心跳时间
        String sessionId = session.getId();
        sessionManager.updateHeartbeat(sessionId);

        // 发送心跳响应
        JSONObject heartbeatResponse = new JSONObject();
        heartbeatResponse.put("type", "heartbeat");
        heartbeatResponse.put("status", "alive");
        heartbeatResponse.put("timestamp", System.currentTimeMillis());

        session.getAsyncRemote().sendText(heartbeatResponse.toJSONString());
    }

    /**
     * 处理订阅请求
     */
    private void handleSubscribe(Session session, String userId, JSONObject message) {
        String subscribeType = message.getString("subscribeType");

        JSONObject response = new JSONObject();
        response.put("type", "subscribe");
        response.put("subscribeType", subscribeType);
        response.put("status", "success");
        response.put("message", "订阅成功");

        session.getAsyncRemote().sendText(response.toJSONString());

        log.info("用户订阅成功，userId: {}, subscribeType: {}", userId, subscribeType);
    }

    /**
     * 处理取消订阅请求
     */
    private void handleUnsubscribe(Session session, String userId, JSONObject message) {
        String subscribeType = message.getString("subscribeType");

        JSONObject response = new JSONObject();
        response.put("type", "unsubscribe");
        response.put("subscribeType", subscribeType);
        response.put("status", "success");
        response.put("message", "取消订阅成功");

        session.getAsyncRemote().sendText(response.toJSONString());

        log.info("用户取消订阅成功，userId: {}, subscribeType: {}", userId, subscribeType);
    }

    /**
     * 处理设备状态查询
     */
    private void handleDeviceStatusQuery(Session session, String userId, JSONObject message) {
        // TODO: 查询设备状态并返回
        JSONObject response = new JSONObject();
        response.put("type", "device_status");
        response.put("status", "success");
        response.put("data", new JSONObject()); // 实际设备数据

        session.getAsyncRemote().sendText(response.toJSONString());
    }

    /**
     * 推送初始数据
     */
    private void pushInitialData(Session session, String userId, String userType) {
        try {
            // 推送连接统计
            JSONObject statsMessage = new JSONObject();
            statsMessage.put("type", "stats");
            statsMessage.put("data", sessionManager.getSessionStats());
            session.getAsyncRemote().sendText(statsMessage.toJSONString());

            // 推送设备状态概览
            JSONObject deviceOverviewMessage = new JSONObject();
            deviceOverviewMessage.put("type", "device_overview");
            JSONObject deviceData = new JSONObject();
            deviceData.put("totalDevices", 0); // TODO: 从实际数据源获取
            deviceData.put("onlineDevices", 0);
            deviceData.put("offlineDevices", 0);
            deviceOverviewMessage.put("data", deviceData);
            session.getAsyncRemote().sendText(deviceOverviewMessage.toJSONString());

            log.debug("初始数据推送完成，userId: {}, userType: {}", userId, userType);

        } catch (Exception e) {
            log.error("推送初始数据失败，userId: {}", userId, e);
        }
    }

    /**
     * 发送错误消息
     */
    private void sendErrorMessage(Session session, String errorMessage) {
        try {
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("type", "error");
            errorResponse.put("status", "error");
            errorResponse.put("message", errorMessage);
            errorResponse.put("timestamp", LocalDateTime.now().toString());

            session.getAsyncRemote().sendText(errorResponse.toJSONString());

        } catch (Exception e) {
            log.error("发送错误消息失败", e);
        }
    }

    /**
     * 构建客户端信息
     */
    private String buildClientInfo(Session session) {
        JSONObject clientInfo = new JSONObject();
        clientInfo.put("remoteAddress", session.getUserProperties().get("jakarta.websocket.endpoint.remoteAddress"));
        clientInfo.put("localAddress", session.getUserProperties().get("jakarta.websocket.endpoint.localAddress"));
        clientInfo.put("userAgent", session.getUserProperties().get("jakarta.websocket.endpoint.remoteAddress"));
        return clientInfo.toJSONString();
    }

    /**
     * 推送门禁事件
     */
    public static void pushAccessEvent(String eventType, Object eventData) {
        try {
            if (sessionManager != null) {
                JSONObject message = new JSONObject();
                message.put("type", "access_event");
                message.put("eventType", eventType);
                message.put("timestamp", LocalDateTime.now().toString());
                message.put("data", eventData);

                sessionManager.broadcast(message.toJSONString());

                log.debug("门禁事件已推送，eventType: {}", eventType);
            }
        } catch (Exception e) {
            log.error("推送门禁事件失败，eventType: {}", eventType, e);
        }
    }

    /**
     * 推送设备状态变化
     */
    public static void pushDeviceStatusChange(String deviceId, String status, Object details) {
        try {
            if (sessionManager != null) {
                JSONObject message = new JSONObject();
                message.put("type", "device_status_change");
                message.put("deviceId", deviceId);
                message.put("status", status);
                message.put("timestamp", LocalDateTime.now().toString());
                message.put("details", details);

                sessionManager.broadcast(message.toJSONString());

                log.debug("设备状态变化已推送，deviceId: {}, status: {}", deviceId, status);
            }
        } catch (Exception e) {
            log.error("推送设备状态变化失败，deviceId: {}, status: {}", deviceId, status, e);
        }
    }

    /**
     * 推送系统告警
     */
    public static void pushSystemAlert(String alertLevel, String title, String message, Object details) {
        try {
            if (sessionManager != null) {
                JSONObject alertMessage = new JSONObject();
                alertMessage.put("type", "system_alert");
                alertMessage.put("alertLevel", alertLevel);
                alertMessage.put("title", title);
                alertMessage.put("message", message);
                alertMessage.put("timestamp", LocalDateTime.now().toString());
                alertMessage.put("details", details);

                sessionManager.broadcast(alertMessage.toJSONString());

                log.info("系统告警已推送，alertLevel: {}, title: {}", alertLevel, title);
            }
        } catch (Exception e) {
            log.error("推送系统告警失败，alertLevel: {}, title: {}", alertLevel, title, e);
        }
    }

    /**
     * 推送实时统计数据
     */
    public static void pushRealTimeStats(Map<String, Object> stats) {
        try {
            if (sessionManager != null) {
                JSONObject message = new JSONObject();
                message.put("type", "real_time_stats");
                message.put("timestamp", LocalDateTime.now().toString());
                message.put("data", stats);

                sessionManager.broadcast(message.toJSONString());

                log.debug("实时统计数据已推送");
            }
        } catch (Exception e) {
            log.error("推送实时统计数据失败", e);
        }
    }
}