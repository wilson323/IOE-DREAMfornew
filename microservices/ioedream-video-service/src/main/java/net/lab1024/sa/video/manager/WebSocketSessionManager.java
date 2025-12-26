package net.lab1024.sa.video.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket会话管理器
 * <p>
 * 管理WebSocket连接会话，提供会话查询、统计等功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Slf4j
@Component
public class WebSocketSessionManager {

    /**
     * 活跃会话映射（SessionId -> WebSocketSession）
     */
    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();

    /**
     * 用户会话映射（UserId -> Set<SessionId>）
     */
    private final Map<Long, Set<String>> userSessions = new ConcurrentHashMap<>();

    /**
     * 设备订阅映射（DeviceId -> Set<SessionId>）
     */
    private final Map<String, Set<String>> deviceSubscriptions = new ConcurrentHashMap<>();

    /**
     * 添加会话
     *
     * @param session WebSocket会话
     */
    public void addSession(WebSocketSession session) {
        String sessionId = session.getId();
        activeSessions.put(sessionId, session);

        // 提取用户ID（从session attributes中）
        Object userIdObj = session.getAttributes().get("userId");
        if (userIdObj instanceof Long) {
            Long userId = (Long) userIdObj;
            userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        }

        log.info("[WebSocket会话] 会话已添加: sessionId={}, 当前活跃数={}", sessionId, activeSessions.size());
    }

    /**
     * 移除会话
     *
     * @param sessionId 会话ID
     */
    public void removeSession(String sessionId) {
        WebSocketSession session = activeSessions.remove(sessionId);
        if (session == null) {
            return;
        }

        // 从用户会话映射中移除
        Object userIdObj = session.getAttributes().get("userId");
        if (userIdObj instanceof Long) {
            Long userId = (Long) userIdObj;
            Set<String> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
        }

        // 从设备订阅中移除
        for (Set<String> subscribers : deviceSubscriptions.values()) {
            subscribers.remove(sessionId);
        }

        log.info("[WebSocket会话] 会话已移除: sessionId={}, 剩余活跃数={}", sessionId, activeSessions.size());
    }

    /**
     * 订阅设备事件
     *
     * @param sessionId 会话ID
     * @param deviceId  设备ID
     */
    public void subscribeDevice(String sessionId, String deviceId) {
        deviceSubscriptions.computeIfAbsent(deviceId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        log.info("[WebSocket订阅] 会话订阅设备: sessionId={}, deviceId={}", sessionId, deviceId);
    }

    /**
     * 取消订阅设备事件
     *
     * @param sessionId 会话ID
     * @param deviceId  设备ID
     */
    public void unsubscribeDevice(String sessionId, String deviceId) {
        Set<String> subscribers = deviceSubscriptions.get(deviceId);
        if (subscribers != null) {
            subscribers.remove(sessionId);
            if (subscribers.isEmpty()) {
                deviceSubscriptions.remove(deviceId);
            }
        }
        log.info("[WebSocket订阅] 会话取消订阅: sessionId={}, deviceId={}", sessionId, deviceId);
    }

    /**
     * 获取会话
     *
     * @param sessionId 会话ID
     * @return WebSocket会话
     */
    public WebSocketSession getSession(String sessionId) {
        return activeSessions.get(sessionId);
    }

    /**
     * 获取用户的所有会话
     *
     * @param userId 用户ID
     * @return 会话ID列表
     */
    public Set<String> getUserSessionIds(Long userId) {
        return userSessions.getOrDefault(userId, Set.of());
    }

    /**
     * 获取设备的所有订阅者会话
     *
     * @param deviceId 设备ID
     * @return 会话ID列表
     */
    public Set<String> getDeviceSubscriberSessionIds(String deviceId) {
        return deviceSubscriptions.getOrDefault(deviceId, Set.of());
    }

    /**
     * 获取所有活跃会话
     *
     * @return 会话列表
     */
    public List<WebSocketSession> getAllActiveSessions() {
        return new ArrayList<>(activeSessions.values());
    }

    /**
     * 获取活跃会话数
     *
     * @return 会话数量
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }

    /**
     * 获取用户会话数
     *
     * @return 用户数量
     */
    public int getUserCount() {
        return userSessions.size();
    }

    /**
     * 获取设备订阅数
     *
     * @return 订阅数量
     */
    public int getDeviceSubscriptionCount() {
        return deviceSubscriptions.size();
    }

    /**
     * 检查会话是否存在
     *
     * @param sessionId 会话ID
     * @return 是否存在
     */
    public boolean hasSession(String sessionId) {
        return activeSessions.containsKey(sessionId);
    }

    /**
     * 检查用户是否在线
     *
     * @param userId 用户ID
     * @return 是否在线
     */
    public boolean isUserOnline(Long userId) {
        Set<String> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }
}
