package net.lab1024.sa.admin.module.smart.monitor.websocket;

import jakarta.annotation.Resource;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.cache.RedisUtil;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket会话管理器
 * <p>
 * 严格遵循repowiki规范：
 * - 管理WebSocket连接会话
 * - 提供连接状态监控
 * - 实现心跳检测机制
 * - 支持消息广播和单发
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Slf4j
@Component
public class WebSocketSessionManager {

    // 活跃会话存储
    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();

    // 用户会话映射
    private final Map<String, String> userSessions = new ConcurrentHashMap<>();

    // 会话统计信息
    private final Map<String, Object> sessionStats = new ConcurrentHashMap<>();

    // 心跳检测任务
    private ScheduledExecutorService heartbeatExecutor;

    @Resource
    private RedisUtil redisUtil;

    // Redis存储前缀
    private static final String REDIS_SESSION_PREFIX = "smart:websocket:session:";
    private static final String REDIS_USER_PREFIX = "smart:websocket:user:";

    /**
     * WebSocket会话信息
     */
    public static class WebSocketSession {
        private final String sessionId;
        private final Session session;
        private final String userId;
        private final String userType;
        private final LocalDateTime connectTime;
        private LocalDateTime lastHeartbeatTime;
        private final String clientInfo;

        public WebSocketSession(String sessionId, Session session, String userId, String userType, String clientInfo) {
            this.sessionId = sessionId;
            this.session = session;
            this.userId = userId;
            this.userType = userType;
            this.clientInfo = clientInfo;
            this.connectTime = LocalDateTime.now();
            this.lastHeartbeatTime = LocalDateTime.now();
        }

        // Getters
        public String getSessionId() { return sessionId; }
        public Session getSession() { return session; }
        public String getUserId() { return userId; }
        public String getUserType() { return userType; }
        public LocalDateTime getConnectTime() { return connectTime; }
        public LocalDateTime getLastHeartbeatTime() { return lastHeartbeatTime; }
        public String getClientInfo() { return clientInfo; }

        public void updateHeartbeat() {
            this.lastHeartbeatTime = LocalDateTime.now();
        }

        public boolean isActive() {
            return session != null && session.isOpen();
        }

        public long getConnectedSeconds() {
            return java.time.Duration.between(connectTime, LocalDateTime.now()).getSeconds();
        }
    }

    /**
     * 启动心跳检测任务
     */
    public void startHeartbeatTask() {
        if (heartbeatExecutor != null) {
            return;
        }

        heartbeatExecutor = Executors.newScheduledThreadPool(2);

        // 心跳检测任务 - 每30秒执行一次
        heartbeatExecutor.scheduleAtFixedRate(this::checkHeartbeats, 30, 30, TimeUnit.SECONDS);

        // 会话统计任务 - 每60秒执行一次
        heartbeatExecutor.scheduleAtFixedRate(this::updateSessionStats, 60, 60, TimeUnit.SECONDS);

        log.info("WebSocket心跳检测任务已启动");
    }

    /**
     * 添加会话
     */
    public void addSession(String sessionId, Session session, String userId, String userType, String clientInfo) {
        WebSocketSession webSocketSession = new WebSocketSession(sessionId, session, userId, userType, clientInfo);

        // 存储到内存
        activeSessions.put(sessionId, webSocketSession);
        if (userId != null) {
            userSessions.put(userId, sessionId);
        }

        // 存储到Redis
        String sessionKey = REDIS_SESSION_PREFIX + sessionId;
        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("sessionId", sessionId);
        sessionData.put("userId", userId);
        sessionData.put("userType", userType);
        sessionData.put("clientInfo", clientInfo);
        sessionData.put("connectTime", webSocketSession.getConnectTime().toString());
        sessionData.put("lastHeartbeat", webSocketSession.getLastHeartbeatTime().toString());

        redisUtil.setBean(sessionKey, sessionData, 3600); // 1小时过期

        if (userId != null) {
            String userKey = REDIS_USER_PREFIX + userId;
            redisUtil.set(userKey, sessionId, 3600);
        }

        log.info("WebSocket会话已添加，sessionId: {}, userId: {}, userType: {}", sessionId, userId, userType);
    }

    /**
     * 移除会话
     */
    public void removeSession(String sessionId) {
        WebSocketSession webSocketSession = activeSessions.remove(sessionId);

        if (webSocketSession != null) {
            String userId = webSocketSession.getUserId();
            if (userId != null) {
                userSessions.remove(userId);
                redisUtil.delete(REDIS_USER_PREFIX + userId);
            }

            // 关闭WebSocket连接
            try {
                if (webSocketSession.getSession() != null && webSocketSession.getSession().isOpen()) {
                    webSocketSession.getSession().close();
                }
            } catch (IOException e) {
                log.warn("关闭WebSocket连接失败，sessionId: {}", sessionId, e);
            }

            redisUtil.delete(REDIS_SESSION_PREFIX + sessionId);

            log.info("WebSocket会话已移除，sessionId: {}, userId: {}", sessionId, userId);
        }
    }

    /**
     * 获取会话
     */
    public WebSocketSession getSession(String sessionId) {
        return activeSessions.get(sessionId);
    }

    /**
     * 根据用户ID获取会话
     */
    public WebSocketSession getSessionByUserId(String userId) {
        String sessionId = userSessions.get(userId);
        return sessionId != null ? activeSessions.get(sessionId) : null;
    }

    /**
     * 更新心跳时间
     */
    public void updateHeartbeat(String sessionId) {
        WebSocketSession session = activeSessions.get(sessionId);
        if (session != null) {
            session.updateHeartbeat();

            // 更新Redis中的心跳时间
            String sessionKey = REDIS_SESSION_PREFIX + sessionId;
            Map<String, Object> sessionData = redisUtil.getBean(sessionKey, Map.class);
            if (sessionData != null) {
                sessionData.put("lastHeartbeat", session.getLastHeartbeatTime().toString());
                redisUtil.setBean(sessionKey, sessionData, 3600);
            }

            log.debug("WebSocket心跳已更新，sessionId: {}", sessionId);
        }
    }

    /**
     * 广播消息给所有会话
     */
    public void broadcast(String message) {
        broadcast(message, null);
    }

    /**
     * 广播消息给指定用户类型的会话
     */
    public void broadcast(String message, String userType) {
        List<String> failedSessionIds = new ArrayList<>();

        for (Map.Entry<String, WebSocketSession> entry : activeSessions.entrySet()) {
            WebSocketSession session = entry.getValue();

            // 检查用户类型过滤
            if (userType != null && !userType.equals(session.getUserType())) {
                continue;
            }

            try {
                if (session.isActive()) {
                    session.getSession().getAsyncRemote().sendText(message);
                } else {
                    failedSessionIds.add(entry.getKey());
                }
            } catch (Exception e) {
                log.error("发送WebSocket消息失败，sessionId: {}", entry.getKey(), e);
                failedSessionIds.add(entry.getKey());
            }
        }

        // 清理失败的会话
        for (String sessionId : failedSessionIds) {
            removeSession(sessionId);
        }

        log.info("WebSocket广播消息完成，消息长度: {}, 失败数量: {}", message.length(), failedSessionIds.size());
    }

    /**
     * 发送消息给指定用户
     */
    public boolean sendToUser(String userId, String message) {
        WebSocketSession session = getSessionByUserId(userId);
        if (session != null && session.isActive()) {
            try {
                session.getSession().getAsyncRemote().sendText(message);
                log.debug("WebSocket消息已发送给用户，userId: {}", userId);
                return true;
            } catch (Exception e) {
                log.error("发送WebSocket消息给用户失败，userId: {}", userId, e);
                return false;
            }
        }
        return false;
    }

    /**
     * 检查心跳
     */
    private void checkHeartbeats() {
        List<String> timeoutSessionIds = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Map.Entry<String, WebSocketSession> entry : activeSessions.entrySet()) {
            WebSocketSession session = entry.getValue();

            // 检查连接是否活跃
            if (!session.isActive()) {
                timeoutSessionIds.add(entry.getKey());
                continue;
            }

            // 检查心跳超时 (90秒无心跳)
            if (session.getLastHeartbeatTime().isBefore(now.minusSeconds(90))) {
                timeoutSessionIds.add(entry.getKey());
                log.warn("WebSocket会话心跳超时，sessionId: {}", entry.getKey());
            }
        }

        // 清理超时会话
        for (String sessionId : timeoutSessionIds) {
            removeSession(sessionId);
        }

        if (!timeoutSessionIds.isEmpty()) {
            log.info("WebSocket心跳检查完成，清理超时会话数量: {}", timeoutSessionIds.size());
        }
    }

    /**
     * 更新会话统计信息
     */
    private void updateSessionStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSessions", activeSessions.size());
        stats.put("totalUsers", userSessions.size());

        // 按用户类型统计
        Map<String, Integer> userTypeStats = new HashMap<>();
        Map<String, Long> connectTimeStats = new HashMap<>();

        for (WebSocketSession session : activeSessions.values()) {
            // 用户类型统计
            String userType = session.getUserType();
            if (userType != null) {
                userTypeStats.put(userType, userTypeStats.getOrDefault(userType, 0) + 1);
            }

            // 连接时长统计
            long connectedSeconds = session.getConnectedSeconds();
            connectTimeStats.put(session.getSessionId(), connectedSeconds);
        }

        stats.put("userTypeStats", userTypeStats);
        stats.put("connectTimeStats", connectTimeStats);
        stats.put("updateTime", LocalDateTime.now().toString());

        sessionStats.putAll(stats);

        // 存储到Redis
        redisUtil.setBean("smart:websocket:stats", stats, 300); // 5分钟过期
    }

    /**
     * 获取会话统计信息
     */
    public Map<String, Object> getSessionStats() {
        Map<String, Object> stats = new HashMap<>(sessionStats);

        // 实时统计
        stats.put("realTimeTotalSessions", activeSessions.size());
        stats.put("realTimeTotalUsers", userSessions.size());
        stats.put("timestamp", LocalDateTime.now().toString());

        return stats;
    }

    /**
     * 获取所有活跃会话
     */
    public Collection<WebSocketSession> getAllSessions() {
        return new ArrayList<>(activeSessions.values());
    }

    /**
     * 获取活跃会话数量
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }

    /**
     * 获取活跃用户数量
     */
    public int getActiveUserCount() {
        return userSessions.size();
    }

    /**
     * 发送消息到指定会话
     *
     * @param sessionId 会话ID
     * @param message   消息内容
     */
    public void sendToSession(String sessionId, String message) {
        WebSocketSession webSocketSession = activeSessions.get(sessionId);
        if (webSocketSession != null && webSocketSession.isActive()) {
            try {
                webSocketSession.getSession().getAsyncRemote().sendText(message);
                log.debug("消息已发送到会话: {}", sessionId);
            } catch (Exception e) {
                log.error("发送消息到会话失败，sessionId: {}, message: {}", sessionId, message, e);
                // 发送失败，移除会话
                removeSession(sessionId);
            }
        } else {
            log.warn("会话不存在或已断开: {}", sessionId);
        }
    }

    /**
     * 关闭所有会话
     */
    public void closeAllSessions() {
        log.info("开始关闭所有WebSocket会话，当前会话数量: {}", activeSessions.size());

        List<String> sessionIds = new ArrayList<>(activeSessions.keySet());
        for (String sessionId : sessionIds) {
            removeSession(sessionId);
        }

        // 停止心跳检测任务
        if (heartbeatExecutor != null) {
            heartbeatExecutor.shutdown();
            try {
                if (!heartbeatExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    heartbeatExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                heartbeatExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            heartbeatExecutor = null;
        }

        log.info("所有WebSocket会话已关闭");
    }
}