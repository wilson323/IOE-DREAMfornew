package net.lab1024.sa.video.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.video.domain.dto.EdgeAIEventDTO;
import net.lab1024.sa.video.service.EventProcessService;

/**
 * 边缘事件控制器
 * <p>
 * 接收边缘设备上报的AI识别事件
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/video/edge")
@Tag(name = "边缘事件上报", description = "边缘设备AI事件上报接口")
public class EdgeEventController {

    @Resource
    private EventProcessService eventProcessService;

    /**
     * 接收边缘设备上报的AI事件
     * <p>
     * 边缘设备完成AI识别后，通过此接口上报事件
     * 事件将被验证、缓存，并通过WebSocket推送到前端
     * </p>
     *
     * @param event AI事件
     * @return 响应
     */
    @PostMapping("/event")
    @Operation(summary = "上报AI事件", description = "边缘设备上报AI识别事件")
    public ResponseDTO<Void> receiveEdgeEvent(@Valid @RequestBody EdgeAIEventDTO event) {
        log.info("[边缘事件] 接收AI事件: eventType={}, deviceId={}, confidence={}",
                event.getEventType(), event.getDeviceId(), event.getConfidence());

        // 自动设置事件ID和事件时间
        if (event.getEventId() == null || event.getEventId().isEmpty()) {
            event.setEventId(generateEventId(event.getDeviceId(), event.getEventType()));
        }
        if (event.getEventTime() == null) {
            event.setEventTime(LocalDateTime.now());
        }

        // 处理事件
        eventProcessService.processEdgeEvent(event);

        return ResponseDTO.ok();
    }

    /**
     * 批量上报AI事件
     * <p>
     * 支持边缘设备批量上报多个事件
     * </p>
     *
     * @param events AI事件列表
     * @return 响应
     */
    @PostMapping("/events/batch")
    @Operation(summary = "批量上报AI事件", description = "边缘设备批量上报AI识别事件")
    public ResponseDTO<Void> receiveEdgeEventsBatch(@Valid @RequestBody EdgeAIEventDTO[] events) {
        log.info("[边缘事件] 批量接收AI事件: count={}", events.length);

        int successCount = 0;
        int failCount = 0;

        for (EdgeAIEventDTO event : events) {
            try {
                // 自动设置事件ID和事件时间
                if (event.getEventId() == null || event.getEventId().isEmpty()) {
                    event.setEventId(generateEventId(event.getDeviceId(), event.getEventType()));
                }
                if (event.getEventTime() == null) {
                    event.setEventTime(LocalDateTime.now());
                }

                eventProcessService.processEdgeEvent(event);
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.error("[边缘事件] 批量处理失败: eventId={}, error={}",
                        event.getEventId(), e.getMessage(), e);
            }
        }

        log.info("[边缘事件] 批量处理完成: total={}, success={}, failed={}", events.length, successCount, failCount);

        return ResponseDTO.ok();
    }

    /**
     * 生成事件ID
     *
     * @param deviceId  设备ID
     * @param eventType 事件类型
     * @return 事件ID
     */
    private String generateEventId(String deviceId, String eventType) {
        return String.format("%s_%s_%d", deviceId, eventType, System.currentTimeMillis());
    }
}
