package net.lab1024.sa.video.controller;

import java.util.List;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.video.constant.WebSocketConstants;
import net.lab1024.sa.video.domain.dto.EdgeAIEventDTO;
import net.lab1024.sa.video.manager.EventCacheManager;
import net.lab1024.sa.video.manager.WebSocketSessionManager;

/**
 * WebSocket事件控制器
 * <p>
 * 处理WebSocket订阅请求，返回初始数据和会话信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/video/websocket")
@Tag(name = "WebSocket事件推送", description = "WebSocket实时事件推送接口")
public class EventWebSocketController {

    @Resource
    private WebSocketSessionManager sessionManager;

    @Resource
    private EventCacheManager eventCacheManager;

    @Resource
    @Qualifier("eventCacheManager")
    private CacheManager springCacheManager;

    /**
     * 获取最近的视频事件（REST API）
     * <p>
     * 提供REST接口用于获取最近的事件，可用于WebSocket断线重连后获取历史数据
     * </p>
     *
     * @param count 数量
     * @return 事件列表
     */
    @GetMapping("/recent-events")
    @Operation(summary = "获取最近的视频事件", description = "获取最近的AI事件列表")
    public ResponseDTO<List<EdgeAIEventDTO>> getRecentEvents(
            @RequestParam(defaultValue = "10") int count) {
        log.info("[WebSocket事件] 查询最近事件: count={}", count);

        List<EdgeAIEventDTO> events = eventCacheManager.getRecentEvents(count);
        log.info("[WebSocket事件] 返回最近事件: count={}", events.size());

        return ResponseDTO.ok(events);
    }

    /**
     * 获取指定设备的最近事件（REST API）
     *
     * @param deviceId 设备ID
     * @param count    数量
     * @return 事件列表
     */
    @GetMapping("/device-events")
    @Operation(summary = "获取设备事件", description = "获取指定设备的最近AI事件列表")
    public ResponseDTO<List<EdgeAIEventDTO>> getDeviceEvents(
            @RequestParam String deviceId,
            @RequestParam(defaultValue = "10") int count) {
        log.info("[WebSocket事件] 查询设备事件: deviceId={}, count={}", deviceId, count);

        List<EdgeAIEventDTO> events = eventCacheManager.getDeviceEvents(deviceId, count);
        log.info("[WebSocket事件] 返回设备事件: deviceId={}, count={}", deviceId, events.size());

        return ResponseDTO.ok(events);
    }

    /**
     * 获取WebSocket会话统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/session-stats")
    @Operation(summary = "获取会话统计", description = "获取WebSocket连接会话的统计信息")
    public ResponseDTO<SessionStatsVO> getSessionStats() {
        SessionStatsVO stats = new SessionStatsVO();
        stats.setActiveSessions(sessionManager.getActiveSessionCount());
        stats.setOnlineUsers(sessionManager.getUserCount());
        stats.setDeviceSubscriptions(sessionManager.getDeviceSubscriptionCount());
        stats.setCachedEvents(eventCacheManager.getCacheSize());

        log.info("[WebSocket事件] 查询会话统计: {}", stats);
        return ResponseDTO.ok(stats);
    }

    /**
     * 会话统计信息VO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SessionStatsVO {
        private Integer activeSessions;
        private Integer onlineUsers;
        private Integer deviceSubscriptions;
        private Integer cachedEvents;
    }
}
