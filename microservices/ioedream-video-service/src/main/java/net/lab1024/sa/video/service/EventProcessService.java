package net.lab1024.sa.video.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.video.constant.WebSocketConstants;
import net.lab1024.sa.video.domain.dto.EdgeAIEventDTO;

/**
 * 事件处理服务
 * <p>
 * 处理边缘设备上报的AI事件：
 * 1. 验证事件数据
 * 2. 存储到缓存
 * 3. 推送到WebSocket订阅者
 * 4. 触发告警（如需要）
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Slf4j
@Service
@Tag(name = "事件处理服务", description = "边缘AI事件处理服务")
public class EventProcessService {

    @Resource
    private EventPushService eventPushService;

    /**
     * 处理边缘AI事件
     *
     * @param event AI事件
     */
    public void processEdgeEvent(EdgeAIEventDTO event) {
        if (event == null) {
            log.warn("[事件处理] 事件为空，跳过处理");
            return;
        }

        try {
            // 1. 验证事件数据
            validateEvent(event);

            // 2. 推送到WebSocket订阅者
            pushEventToSubscribers(event);

            // 3. 如果是异常事件，触发告警
            if (isAbnormalEvent(event)) {
                handleAbnormalEvent(event);
            }

            log.info("[事件处理] 事件处理成功: eventId={}, eventType={}, deviceId={}",
                    event.getEventId(), event.getEventType(), event.getDeviceId());

        } catch (IllegalArgumentException e) {
            log.warn("[事件处理] 事件验证失败: eventId={}, error={}",
                    event.getEventId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[事件处理] 事件处理异常: eventId={}, error={}",
                    event.getEventId(), e.getMessage(), e);
            throw new RuntimeException("事件处理失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证事件数据
     *
     * @param event AI事件
     * @throws IllegalArgumentException 验证失败
     */
    private void validateEvent(EdgeAIEventDTO event) {
        if (event.getDeviceId() == null || event.getDeviceId().isEmpty()) {
            throw new IllegalArgumentException("设备ID不能为空");
        }

        if (event.getEventType() == null || event.getEventType().isEmpty()) {
            throw new IllegalArgumentException("事件类型不能为空");
        }

        if (event.getConfidence() == null) {
            throw new IllegalArgumentException("置信度不能为空");
        }

        // 验证置信度范围
        if (event.getConfidence().compareTo(java.math.BigDecimal.ZERO) < 0 ||
            event.getConfidence().compareTo(java.math.BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("置信度必须在0-1之间");
        }

        // 验证事件类型
        boolean isValidEventType = switch (event.getEventType()) {
            case WebSocketConstants.EVENT_FACE_DETECTED,
                 WebSocketConstants.EVENT_FACE_RECOGNIZED,
                 WebSocketConstants.EVENT_PERSON_COUNT,
                 WebSocketConstants.EVENT_ABNORMAL_BEHAVIOR,
                 WebSocketConstants.EVENT_AREA_INTRUSION,
                 WebSocketConstants.EVENT_LOITERING_DETECTED,
                 WebSocketConstants.EVENT_FALL_DETECTED,
                 WebSocketConstants.EVENT_FIGHT_DETECTED -> true;
            default -> false;
        };

        if (!isValidEventType) {
            throw new IllegalArgumentException("无效的事件类型: " + event.getEventType());
        }

        log.debug("[事件处理] 事件验证通过: eventId={}", event.getEventId());
    }

    /**
     * 推送事件到订阅者
     *
     * @param event AI事件
     */
    private void pushEventToSubscribers(EdgeAIEventDTO event) {
        // 广播到所有订阅者
        eventPushService.pushEvent(event);

        // 推送到设备特定订阅者
        eventPushService.pushDeviceEvent(event.getDeviceId(), event);
    }

    /**
     * 判断是否为异常事件
     *
     * @param event AI事件
     * @return 是否为异常事件
     */
    private boolean isAbnormalEvent(EdgeAIEventDTO event) {
        return switch (event.getEventType()) {
            case WebSocketConstants.EVENT_ABNORMAL_BEHAVIOR,
                 WebSocketConstants.EVENT_AREA_INTRUSION,
                 WebSocketConstants.EVENT_LOITERING_DETECTED,
                 WebSocketConstants.EVENT_FALL_DETECTED,
                 WebSocketConstants.EVENT_FIGHT_DETECTED -> true;
            default -> false;
        };
    }

    /**
     * 处理异常事件
     *
     * @param event 异常事件
     */
    private void handleAbnormalEvent(EdgeAIEventDTO event) {
        log.warn("[异常事件] 检测到异常行为: eventType={}, deviceId={}, eventId={}, confidence={}",
                event.getEventType(), event.getDeviceId(), event.getEventId(), event.getConfidence());

        // TODO: 触发告警流程
        // 1. 查找该区域的安保人员
        // 2. 发送实时通知
        // 3. 记录告警日志
        // 4. 视频联动（抓拍、录像）

        // 暂时只记录日志
        log.info("[异常事件] 告警处理完成: eventId={}", event.getEventId());
    }
}
