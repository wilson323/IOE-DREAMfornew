package net.lab1024.sa.attendance.mq.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.domain.vo.AttendanceEventVO;
import net.lab1024.sa.attendance.websocket.AttendanceWebSocketService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 考勤事件消息消费者
 * <p>
 * 监听考勤事件队列
 * 处理考勤事件并触发相关业务逻辑
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Component
@Slf4j
public class AttendanceEventConsumer {

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private AttendanceWebSocketService webSocketService;

    /**
     * 监听考勤事件队列
     *
     * @param messageJson 消息JSON
     */
    @RabbitListener(queues = "attendance.event.queue")
    public void handleAttendanceEvent(String messageJson) {
        try {
            log.info("[RabbitMQ消费] 收到考勤事件: message={}", messageJson);

            // 解析消息
            AttendanceEventVO event = objectMapper.readValue(messageJson, AttendanceEventVO.class);

            // 处理考勤事件
            processAttendanceEvent(event);

        } catch (Exception e) {
            log.error("[RabbitMQ消费] 处理考勤事件失败: message={}, error={}",
                     messageJson, e.getMessage(), e);
        }
    }

    /**
     * 处理考勤事件
     *
     * @param event 考勤事件
     */
    private void processAttendanceEvent(AttendanceEventVO event) {
        log.info("[RabbitMQ消费] 处理考勤事件: userId={}, eventType={}",
                 event.getUserId(), event.getEventType());

        switch (event.getEventType()) {
            case "PUNCH_SUCCESS":
                // 打卡成功，通过WebSocket通知用户
                handlePunchSuccess(event);
                break;

            case "PUNCH_FAILED":
                // 打卡失败，通过WebSocket通知用户
                handlePunchFailed(event);
                break;

            case "BIOMETRIC_FAILED":
                // 生物识别失败
                handleBiometricFailed(event);
                break;

            case "DEVICE_OFFLINE":
                // 设备离线
                handleDeviceOffline(event);
                break;

            default:
                log.warn("[RabbitMQ消费] 未知的事件类型: {}", event.getEventType());
                break;
        }
    }

    /**
     * 处理打卡成功事件
     */
    private void handlePunchSuccess(AttendanceEventVO event) {
        log.info("[RabbitMQ消费] 打卡成功: userId={}, punchTime={}",
                 event.getUserId(), event.getEventTime());

        // TODO: 根据需要添加业务逻辑
        // 例如：更新用户状态、触发统计任务等
    }

    /**
     * 处理打卡失败事件
     */
    private void handlePunchFailed(AttendanceEventVO event) {
        log.warn("[RabbitMQ消费] 打卡失败: userId={}, error={}",
                 event.getUserId(), event.getErrorMessage());

        // 通过WebSocket通知用户
        webSocketService.pushAttendanceError(
            event.getUserId(),
            event.getErrorMessage() != null ? event.getErrorMessage() : "打卡失败"
        );
    }

    /**
     * 处理生物识别失败事件
     */
    private void handleBiometricFailed(AttendanceEventVO event) {
        log.warn("[RabbitMQ消费] 生物识别失败: userId={}", event.getUserId());

        // 通过WebSocket通知用户
        webSocketService.pushAttendanceError(
            event.getUserId(),
            "生物识别失败，请重试或使用其他打卡方式"
        );
    }

    /**
     * 处理设备离线事件
     */
    private void handleDeviceOffline(AttendanceEventVO event) {
        log.error("[RabbitMQ消费] 设备离线: deviceId={}", event.getDeviceId());

        // TODO: 触发设备告警
    }
}
