package net.lab1024.sa.common.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import jakarta.annotation.Resource;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import net.lab1024.sa.common.exception.SystemException;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * RabbitMQ 事件监听器
 *
 * @author IOE-DREAM Team
 * @date 2025-12-09
 * @description 统一的RabbitMQ事件监听和消息处理
 */
@Slf4j
@Component
public class RabbitMQEventListener {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private MessageConverter messageConverter;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private ApplicationEventPublisher eventPublisher;

    /**
     * 处理系统事件
     */
    @EventListener
    public void handleSystemEvent(SystemEvent event) {
        try {
            log.info("[RabbitMQ] 发送系统事件: type={}, data={}", event.getType(), event.getData());

            // 设置消息属性
            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            messageProperties.setMessageId(event.getId());
            messageProperties.setTimestamp(event.getTimestamp());
            messageProperties.setHeader("event-type", event.getType());
            messageProperties.setHeader("event-source", event.getSource());
            messageProperties.setHeader("version", "1.0");

            // 转换消息
            Message message = messageConverter.toMessage(event, messageProperties);

            // 发送消息
            rabbitTemplate.send("ioedream.system.exchange", "system.event", message);

            log.info("[RabbitMQ] 系统事件发送成功: eventId={}", event.getId());

        } catch (Exception e) {
            log.error("[RabbitMQ] 系统事件发送失败: type={}, error={}", event.getType(), e.getMessage(), e);
            throw new SystemException("SYSTEM_EVENT_SEND_ERROR", "系统事件发送失败", e);
        }
    }

    /**
     * 处理设备命令事件
     */
    @EventListener
    public void handleDeviceCommandEvent(DeviceCommandEvent event) {
        try {
            log.info("[RabbitMQ] 发送设备命令: deviceId={}, command={}", event.getDeviceId(), event.getCommand());

            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            messageProperties.setMessageId(event.getId());
            messageProperties.setTimestamp(event.getTimestamp());
            messageProperties.setHeader("device-id", event.getDeviceId());
            messageProperties.setHeader("command", event.getCommand());
            messageProperties.setHeader("timeout", event.getTimeout());
            messageProperties.setPriority(event.getPriority());

            Message message = messageConverter.toMessage(event.getData(), messageProperties);

            rabbitTemplate.send("ioedream.device.exchange", "device.command", message);

            log.info("[RabbitMQ] 设备命令发送成功: eventId={}, deviceId={}", event.getId(), event.getDeviceId());

        } catch (Exception e) {
            log.error("[RabbitMQ] 设备命令发送失败: deviceId={}, error={}", event.getDeviceId(), e.getMessage(), e);
            throw new SystemException("DEVICE_COMMAND_SEND_ERROR", "设备命令发送失败", e);
        }
    }

    /**
     * 处理门禁事件
     */
    @EventListener
    public void handleAccessEvent(AccessEvent event) {
        try {
            log.info("[RabbitMQ] 发送门禁事件: type={}, cardId={}", event.getEventType(), event.getCardId());

            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            messageProperties.setMessageId(event.getId());
            messageProperties.setTimestamp(event.getTimestamp());
            messageProperties.setHeader("event-type", event.getEventType());
            messageProperties.setHeader("card-id", event.getCardId());
            messageProperties.setHeader("device-id", event.getDeviceId());
            messageProperties.setHeader("user-id", event.getUserId());

            // 根据事件类型设置优先级
            if (event.getEventType().equals("ALARM")) {
                messageProperties.setPriority(10);
            }

            Message message = messageConverter.toMessage(event, messageProperties);

            String routingKey = "access." + event.getEventType().toLowerCase();
            rabbitTemplate.send("ioedream.access.exchange", routingKey, message);

            log.info("[RabbitMQ] 门禁事件发送成功: eventId={}, type={}", event.getId(), event.getEventType());

        } catch (Exception e) {
            log.error("[RabbitMQ] 门禁事件发送失败: type={}, error={}", event.getEventType(), e.getMessage(), e);
            throw new SystemException("ACCESS_EVENT_SEND_ERROR", "门禁事件发送失败", e);
        }
    }

    /**
     * 处理考勤事件
     */
    @EventListener
    public void handleAttendanceEvent(AttendanceEvent event) {
        try {
            log.info("[RabbitMQ] 发送考勤事件: type={}, userId={}", event.getEventType(), event.getUserId());

            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            messageProperties.setMessageId(event.getId());
            messageProperties.setTimestamp(event.getTimestamp());
            messageProperties.setHeader("event-type", event.getEventType());
            messageProperties.setHeader("user-id", event.getUserId());
            messageProperties.setHeader("device-id", event.getDeviceId());
            messageProperties.setHeader("check-time", event.getCheckTime());

            Message message = messageConverter.toMessage(event, messageProperties);

            String routingKey = "attendance." + event.getEventType().toLowerCase();
            rabbitTemplate.send("ioedream.attendance.exchange", routingKey, message);

            log.info("[RabbitMQ] 考勤事件发送成功: eventId={}, type={}", event.getId(), event.getEventType());

        } catch (Exception e) {
            log.error("[RabbitMQ] 考勤事件发送失败: type={}, error={}", event.getEventType(), e.getMessage(), e);
            throw new SystemException("ATTENDANCE_EVENT_SEND_ERROR", "考勤事件发送失败", e);
        }
    }

    /**
     * 处理消费事件
     */
    @EventListener
    public void handleConsumeEvent(ConsumeEvent event) {
        try {
            log.info("[RabbitMQ] 发送消费事件: type={}, accountId={}, amount={}",
                    event.getEventType(), event.getAccountId(), event.getAmount());

            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            messageProperties.setMessageId(event.getId());
            messageProperties.setTimestamp(event.getTimestamp());
            messageProperties.setHeader("event-type", event.getEventType());
            messageProperties.setHeader("account-id", event.getAccountId());
            messageProperties.setHeader("amount", event.getAmount());
            messageProperties.setHeader("device-id", event.getDeviceId());
            messageProperties.setHeader("transaction-id", event.getTransactionId());

            // 消费事件设置高优先级
            messageProperties.setPriority(8);

            Message message = messageConverter.toMessage(event, messageProperties);

            String routingKey = "consume." + event.getEventType().toLowerCase();
            rabbitTemplate.send("ioedream.consume.exchange", routingKey, message);

            log.info("[RabbitMQ] 消费事件发送成功: eventId={}, type={}, amount={}",
                    event.getId(), event.getEventType(), event.getAmount());

        } catch (Exception e) {
            log.error("[RabbitMQ] 消费事件发送失败: type={}, error={}", event.getEventType(), e.getMessage(), e);
            throw new SystemException("CONSUME_EVENT_SEND_ERROR", "消费事件发送失败", e);
        }
    }

    /**
     * 处理访客事件
     */
    @EventListener
    public void handleVisitorEvent(VisitorEvent event) {
        try {
            log.info("[RabbitMQ] 发送访客事件: type={}, visitorId={}", event.getEventType(), event.getVisitorId());

            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            messageProperties.setMessageId(event.getId());
            messageProperties.setTimestamp(event.getTimestamp());
            messageProperties.setHeader("event-type", event.getEventType());
            messageProperties.setHeader("visitor-id", event.getVisitorId());
            messageProperties.setHeader("visit-id", event.getVisitId());

            Message message = messageConverter.toMessage(event, messageProperties);

            String routingKey = "visitor." + event.getEventType().toLowerCase();
            rabbitTemplate.send("ioedream.visitor.exchange", routingKey, message);

            log.info("[RabbitMQ] 访客事件发送成功: eventId={}, type={}", event.getId(), event.getEventType());

        } catch (Exception e) {
            log.error("[RabbitMQ] 访客事件发送失败: type={}, error={}", event.getEventType(), e.getMessage(), e);
            throw new SystemException("VISITOR_EVENT_SEND_ERROR", "访客事件发送失败", e);
        }
    }

    /**
     * 处理视频事件
     */
    @EventListener
    public void handleVideoEvent(VideoEvent event) {
        try {
            log.info("[RabbitMQ] 发送视频事件: type={}, deviceId={}", event.getEventType(), event.getDeviceId());

            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            messageProperties.setMessageId(event.getId());
            messageProperties.setTimestamp(event.getTimestamp());
            messageProperties.setHeader("event-type", event.getEventType());
            messageProperties.setHeader("device-id", event.getDeviceId());
            messageProperties.setHeader("video-url", event.getVideoUrl());

            // 设置视频消息TTL
            messageProperties.setExpiration("300000");  // 5分钟

            Message message = messageConverter.toMessage(event, messageProperties);

            String routingKey = "video." + event.getEventType().toLowerCase();
            rabbitTemplate.send("ioedream.video.exchange", routingKey, message);

            log.info("[RabbitMQ] 视频事件发送成功: eventId={}, type={}", event.getId(), event.getEventType());

        } catch (Exception e) {
            log.error("[RabbitMQ] 视频事件发送失败: type={}, error={}", event.getEventType(), e.getMessage(), e);
            throw new SystemException("VIDEO_EVENT_SEND_ERROR", "视频事件发送失败", e);
        }
    }

    /**
     * 处理OA事件
     */
    @EventListener
    public void handleOAEvent(OAEvent event) {
        try {
            log.info("[RabbitMQ] 发送OA事件: type={}, workflowId={}", event.getEventType(), event.getWorkflowId());

            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            messageProperties.setMessageId(event.getId());
            messageProperties.setTimestamp(event.getTimestamp());
            messageProperties.setHeader("event-type", event.getEventType());
            messageProperties.setHeader("workflow-id", event.getWorkflowId());
            messageProperties.setHeader("document-id", event.getDocumentId());
            messageProperties.setHeader("user-id", event.getUserId());

            Message message = messageConverter.toMessage(event, messageProperties);

            String routingKey = "oa." + event.getEventType().toLowerCase();
            rabbitTemplate.send("ioedream.oa.exchange", routingKey, message);

            log.info("[RabbitMQ] OA事件发送成功: eventId={}, type={}", event.getId(), event.getEventType());

        } catch (Exception e) {
            log.error("[RabbitMQ] OA事件发送失败: type={}, error={}", event.getEventType(), e.getMessage(), e);
            throw new SystemException("OA_EVENT_SEND_ERROR", "OA事件发送失败", e);
        }
    }

    /**
     * 发送延迟消息
     */
    public void sendDelayedMessage(String exchange, String routingKey, Object message, long delayMillis) {
        try {
            log.info("[RabbitMQ] 发送延迟消息: exchange={}, routingKey={}, delay={}ms",
                    exchange, routingKey, delayMillis);

            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            messageProperties.setHeader("x-delay", delayMillis);
            messageProperties.setTimestamp(new java.util.Date());

            Message msg = messageConverter.toMessage(message, messageProperties);

            rabbitTemplate.send(exchange, routingKey, msg);

            log.info("[RabbitMQ] 延迟消息发送成功: delay={}ms", delayMillis);

        } catch (Exception e) {
            log.error("[RabbitMQ] 延迟消息发送失败: error={}", e.getMessage(), e);
            throw new SystemException("RABBITMQ_DELAY_MESSAGE_SEND_ERROR", "延迟消息发送失败", e);
        }
    }

    /**
     * 发送消息到死信队列
     */
    public void sendToDeadLetterQueue(Message originalMessage, String reason) {
        try {
            log.info("[RabbitMQ] 发送消息到死信队列: reason={}", reason);

            MessageProperties messageProperties = originalMessage.getMessageProperties();
            messageProperties.setHeader("x-original-exchange", messageProperties.getReceivedExchange());
            messageProperties.setHeader("x-original-routing-key", messageProperties.getReceivedRoutingKey());
            messageProperties.setHeader("x-death-reason", reason);
            messageProperties.setHeader("x-death-timestamp", System.currentTimeMillis());

            rabbitTemplate.send("ioedream.dlx.exchange", "dlx", originalMessage);

            log.info("[RabbitMQ] 消息已发送到死信队列: reason={}", reason);

        } catch (Exception e) {
            log.error("[RabbitMQ] 发送死信消息失败: error={}", e.getMessage(), e);
        }
    }

    // ============================================================
    // 事件类定义
    // ============================================================

    /**
     * 系统事件
     */
    public static class SystemEvent {
        private String id;
        private String type;
        private String source;
        private Object data;
        private java.util.Date timestamp;

        // getters and setters
    }

    /**
     * 设备命令事件
     */
    public static class DeviceCommandEvent {
        private String id;
        private String deviceId;
        private String command;
        private Object data;
        private long timeout;
        private int priority;
        private java.util.Date timestamp;

        // getters and setters
    }

    /**
     * 门禁事件
     */
    public static class AccessEvent {
        private String id;
        private String eventType;
        private String deviceId;
        private String cardId;
        private Long userId;
        private Object data;
        private java.util.Date timestamp;

        // getters and setters
    }

    /**
     * 考勤事件
     */
    public static class AttendanceEvent {
        private String id;
        private String eventType;
        private Long userId;
        private String deviceId;
        private java.util.Date checkTime;
        private Object data;
        private java.util.Date timestamp;

        // getters and setters
    }

    /**
     * 消费事件
     */
    public static class ConsumeEvent {
        private String id;
        private String eventType;
        private String accountId;
        private Double amount;
        private String deviceId;
        private String transactionId;
        private Object data;
        private java.util.Date timestamp;

        // getters and setters
    }

    /**
     * 访客事件
     */
    public static class VisitorEvent {
        private String id;
        private String eventType;
        private String visitorId;
        private String visitId;
        private Object data;
        private java.util.Date timestamp;

        // getters and setters
    }

    /**
     * 视频事件
     */
    public static class VideoEvent {
        private String id;
        private String eventType;
        private String deviceId;
        private String videoUrl;
        private Object data;
        private java.util.Date timestamp;

        // getters and setters
    }

    /**
     * OA事件
     */
    public static class OAEvent {
        private String id;
        private String eventType;
        private String workflowId;
        private String documentId;
        private Long userId;
        private Object data;
        private java.util.Date timestamp;

        // getters and setters
    }
}
