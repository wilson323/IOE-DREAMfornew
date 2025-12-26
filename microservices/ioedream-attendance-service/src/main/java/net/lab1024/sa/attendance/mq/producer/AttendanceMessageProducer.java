package net.lab1024.sa.attendance.mq.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.domain.vo.AttendanceEventVO;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * 考勤事件消息生产者
 * <p>
 * 发送考勤事件到RabbitMQ
 * 供其他服务消费（如数据统计、报表服务等）
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Service
@Slf4j
public class AttendanceMessageProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private ObjectMapper objectMapper;

    private static final String EXCHANGE = "attendance.event.exchange";
    private static final String ROUTING_KEY = "attendance.event.routing.key";

    /**
     * 发送考勤事件消息
     *
     * @param event 考勤事件
     */
    public void sendAttendanceEvent(AttendanceEventVO event) {
        try {
            log.info("[RabbitMQ] 发送考勤事件: userId={}, eventType={}",
                     event.getUserId(), event.getEventType());

            // 构建消息
            Message message = MessageBuilder
                .withBody(objectMapper.writeValueAsString(event).getBytes(StandardCharsets.UTF_8))
                .setContentType("application/json")
                .setMessageId(UUID.randomUUID().toString())
                .setTimestamp(new Date())
                .build();

            // 发送消息
            rabbitTemplate.send(EXCHANGE, ROUTING_KEY, message);

            log.info("[RabbitMQ] 考勤事件发送成功: messageId={}, userId={}",
                    message.getMessageProperties().getMessageId(), event.getUserId());

        } catch (Exception e) {
            log.error("[RabbitMQ] 发送考勤事件失败: userId={}, error={}",
                     event.getUserId(), e.getMessage(), e);
            // 消息发送失败不影响主流程
        }
    }

    /**
     * 发送考勤异常事件
     *
     * @param userId 用户ID
     * @param eventType 事件类型
     * @param errorMessage 错误信息
     */
    public void sendAttendanceErrorEvent(Long userId, String eventType, String errorMessage) {
        try {
            log.warn("[RabbitMQ] 发送考勤异常事件: userId={}, eventType={}",
                     userId, eventType);

            AttendanceEventVO event = new AttendanceEventVO();
            event.setUserId(userId);
            event.setEventType(eventType);
            event.setErrorMessage(errorMessage);
            event.setEventTime(new Date());

            sendAttendanceEvent(event);

        } catch (Exception e) {
            log.error("[RabbitMQ] 发送考勤异常事件失败: error={}", e.getMessage(), e);
        }
    }
}
