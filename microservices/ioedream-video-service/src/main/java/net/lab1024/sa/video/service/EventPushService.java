package net.lab1024.sa.video.service;

import java.util.List;

import jakarta.annotation.Resource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.video.constant.WebSocketConstants;
import net.lab1024.sa.video.domain.dto.EdgeAIEventDTO;
import net.lab1024.sa.video.manager.EventCacheManager;
import net.lab1024.sa.video.manager.WebSocketSessionManager;

/**
 * 事件推送服务
 * <p>
 * 负责将边缘AI事件推送到WebSocket订阅者
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Slf4j
@Service
public class EventPushService {

    @Resource
    private SimpMessagingTemplate messagingTemplate;

    @Resource
    private EventCacheManager eventCacheManager;

    @Resource
    private WebSocketSessionManager sessionManager;

    /**
     * 推送事件到所有订阅者
     * <p>
     * 将事件广播到 /topic/video-events 的所有订阅者
     * </p>
     *
     * @param event AI事件
     */
    public void pushEvent(EdgeAIEventDTO event) {
        if (event == null) {
            log.warn("[事件推送] 事件为空，跳过推送");
            return;
        }

        try {
            // 添加到缓存
            eventCacheManager.addEvent(event);

            // 广播到所有订阅者
            messagingTemplate.convertAndSend(WebSocketConstants.TOPIC_VIDEO_EVENTS, event);

            log.info("[事件推送] 广播事件成功: eventType={}, deviceId={}, eventId={}, confidence={}",
                    event.getEventType(), event.getDeviceId(), event.getEventId(), event.getConfidence());

        } catch (Exception e) {
            log.error("[事件推送] 推送事件失败: eventId={}, error={}",
                    event.getEventId(), e.getMessage(), e);
        }
    }

    /**
     * 推送事件到指定用户
     * <p>
     * 将事件推送到特定用户的私有队列
     * </p>
     *
     * @param userId 用户ID
     * @param event  AI事件
     */
    public void pushEventToUser(Long userId, EdgeAIEventDTO event) {
        if (userId == null || event == null) {
            String eventId = (event != null) ? event.getEventId() : "null";
            log.warn("[事件推送] 用户ID或事件为空，跳过推送: userId={}, eventId={}", userId, eventId);
            return;
        }

        try {
            // 检查用户是否在线
            if (!sessionManager.isUserOnline(userId)) {
                log.debug("[事件推送] 用户不在线，跳过推送: userId={}", userId);
                return;
            }

            // 推送到用户私有队列
            String destination = WebSocketConstants.USER_PREFIX + "/" + userId + WebSocketConstants.QUEUE_USER_EVENTS;
            messagingTemplate.convertAndSend(destination, event);

            log.info("[事件推送] 推送事件到用户成功: userId={}, eventType={}, eventId={}",
                    userId, event.getEventType(), event.getEventId());

        } catch (Exception e) {
            log.error("[事件推送] 推送事件到用户失败: userId={}, eventId={}, error={}",
                    userId, event.getEventId(), e.getMessage(), e);
        }
    }

    /**
     * 推送设备事件到订阅该设备的用户
     * <p>
     * 将事件推送到订阅了特定设备的所有会话
     * </p>
     *
     * @param deviceId 设备ID
     * @param event    AI事件
     */
    public void pushDeviceEvent(String deviceId, EdgeAIEventDTO event) {
        if (deviceId == null || event == null) {
            log.warn("[事件推送] 设备ID或事件为空，跳过推送: deviceId={}, eventId={}", deviceId, event != null ? event.getEventId() : "null");
            return;
        }

        try {
            // 添加到缓存
            eventCacheManager.addEvent(event);

            // 推送到设备主题
            String destination = WebSocketConstants.TOPIC_DEVICE_EVENTS + "/" + deviceId;
            messagingTemplate.convertAndSend(destination, event);

            log.info("[事件推送] 推送设备事件成功: deviceId={}, eventType={}, eventId={}",
                    deviceId, event.getEventType(), event.getEventId());

        } catch (Exception e) {
            log.error("[事件推送] 推送设备事件失败: deviceId={}, eventId={}, error={}",
                    deviceId, event.getEventId(), e.getMessage(), e);
        }
    }

    /**
     * 批量推送事件
     *
     * @param events 事件列表
     */
    public void pushEventBatch(List<EdgeAIEventDTO> events) {
        if (events == null || events.isEmpty()) {
            log.warn("[事件推送] 事件列表为空，跳过推送");
            return;
        }

        log.info("[事件推送] 批量推送事件: count={}", events.size());

        int successCount = 0;
        int failCount = 0;

        for (EdgeAIEventDTO event : events) {
            try {
                pushEvent(event);
                successCount++;
            } catch (Exception e) {
                log.error("[事件推送] 批量推送失败: eventId={}, error={}", event.getEventId(), e.getMessage());
                failCount++;
            }
        }

        log.info("[事件推送] 批量推送完成: total={}, success={}, failed={}", events.size(), successCount, failCount);
    }

    /**
     * 推送系统消息
     *
     * @param message 消息内容
     */
    public void pushSystemMessage(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }

        try {
            EdgeAIEventDTO systemEvent = EdgeAIEventDTO.builder()
                    .eventId("SYSTEM_" + System.currentTimeMillis())
                    .eventType("SYSTEM_MESSAGE")
                    .eventData(java.util.Map.of("message", message))
                    .confidence(java.math.BigDecimal.ONE)
                    .eventTime(java.time.LocalDateTime.now())
                    .build();

            pushEvent(systemEvent);
            log.info("[事件推送] 系统消息已推送: message={}", message);

        } catch (Exception e) {
            log.error("[事件推送] 推送系统消息失败: error={}", e.getMessage(), e);
        }
    }
}
