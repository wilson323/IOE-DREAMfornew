package net.lab1024.sa.common.config;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.listener.RabbitMQEventListener;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 消息队列配置类
 *
 * @author IOE-DREAM Team
 * @date 2025-12-09
 * @description 提供统一的RabbitMQ配置，包含队列、交换机、死信队列、延迟队列等
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class RabbitMQConfiguration {

    @Resource
    private ConnectionFactory connectionFactory;

    // ============================================================
    // 消息转换器
    // ============================================================

    @Bean
    public MessageConverter messageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        // 设置信任的包名，防止反序列化攻击
        converter.setTrustedPackages("net.lab1024.sa.common");
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());

        // 启用发布确认
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("[RabbitMQ] 消息发送确认成功: messageId={}",
                        correlationData != null ? correlationData.getId() : "unknown");
            } else {
                log.error("[RabbitMQ] 消息发送确认失败: messageId={}, cause={}",
                        correlationData != null ? correlationData.getId() : "unknown", cause);
            }
        });

        // 启用返回回调
        template.setReturnsCallback(returned -> {
            log.warn("[RabbitMQ] 消息返回: message={}, replyCode={}, replyText={}, exchange={}, routingKey={}",
                    returned.getMessage(), returned.getReplyCode(), returned.getReplyText(),
                    returned.getExchange(), returned.getRoutingKey());
        });

        // 强制启用消息路由检查
        template.setMandatory(true);

        return template;
    }

    // ============================================================
    // 监听器容器工厂
    // ============================================================

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());

        // 设置并发消费者
        factory.setConcurrentConsumers(2);
        factory.setMaxConcurrentConsumers(10);

        // 设置预取数量
        factory.setPrefetchCount(5);

        // 手动确认模式
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);

        // 重试配置
        factory.setAdviceChain(
                org.springframework.amqp.rabbit.config.RetryInterceptorBuilder
                        .stateless()
                        .retryPolicy(org.springframework.retry.policy.SimpleRetryPolicy
                                .builder()
                                .maxAttempts(3)
                                .build())
                        .backOffPolicy(org.springframework.retry.backoff.ExponentialBackOffPolicy
                                .builder()
                                .initialInterval(1000)
                                .maxInterval(10000)
                                .multiplier(2.0)
                                .build())
                        .build()
        );

        // 设置异常处理器
        factory.setErrorHandler(ex -> {
            log.error("[RabbitMQ] 消费异常: {}", ex.getMessage(), ex);
        });

        return factory;
    }

    // ============================================================
    // 死信队列配置
    // ============================================================

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("ioedream.dlx.queue")
                .withArgument("x-dead-letter-exchange", "ioedream.dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "dlx")
                .withArgument("x-message-ttl", 86400000)  // 24小时
                .build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("ioedream.dlx.exchange", true, false);
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("dlx");
    }

    // ============================================================
    // 延迟队列配置
    // ============================================================

    @Bean
    public CustomExchange delayedExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange("ioedream.delayed.exchange", "x-delayed-message", true, false, args);
    }

    @Bean
    public Queue delayedQueue() {
        return QueueBuilder.durable("ioedream.delayed.queue").build();
    }

    @Bean
    public Binding delayedBinding() {
        return BindingBuilder.bind(delayedQueue())
                .to(delayedExchange())
                .with("delayed");
    }

    // ============================================================
    // 系统事件队列
    // ============================================================

    @Bean
    public Queue systemEventQueue() {
        return QueueBuilder.durable("ioedream.system.event.queue")
                .withArgument("x-dead-letter-exchange", "ioedream.dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "system.dlx")
                .build();
    }

    @Bean
    public TopicExchange systemEventExchange() {
        return new TopicExchange("ioedream.system.exchange", true, false);
    }

    @Bean
    public Binding systemEventBinding() {
        return BindingBuilder.bind(systemEventQueue())
                .to(systemEventExchange())
                .with("system.event");
    }

    // ============================================================
    // 设备通讯队列
    // ============================================================

    @Bean
    public Queue deviceCommandQueue() {
        return QueueBuilder.durable("ioedream.device.command.queue")
                .withArgument("x-dead-letter-exchange", "ioedream.dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "device.command.dlx")
                .build();
    }

    @Bean
    public Queue deviceResponseQueue() {
        return QueueBuilder.durable("ioedream.device.response.queue")
                .withArgument("x-dead-letter-exchange", "ioedream.dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "device.response.dlx")
                .build();
    }

    @Bean
    public TopicExchange deviceExchange() {
        return new TopicExchange("ioedream.device.exchange", true, false);
    }

    @Bean
    public Binding deviceCommandBinding() {
        return BindingBuilder.bind(deviceCommandQueue())
                .to(deviceExchange())
                .with("device.command");
    }

    @Bean
    public Binding deviceResponseBinding() {
        return BindingBuilder.bind(deviceResponseQueue())
                .to(deviceExchange())
                .with("device.response");
    }

    // ============================================================
    // 门禁事件队列
    // ============================================================

    @Bean
    public Queue accessEventQueue() {
        return QueueBuilder.durable("ioedream.access.event.queue")
                .withArgument("x-dead-letter-exchange", "ioedream.dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "access.event.dlx")
                .build();
    }

    @Bean
    public Queue accessRecordQueue() {
        return QueueBuilder.durable("ioedream.access.record.queue")
                .withArgument("x-dead-letter-exchange", "ioedream.dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "access.record.dlx")
                .build();
    }

    @Bean
    public TopicExchange accessExchange() {
        return new TopicExchange("ioedream.access.exchange", true, false);
    }

    @Bean
    public Binding accessEventBinding() {
        return BindingBuilder.bind(accessEventQueue())
                .to(accessExchange())
                .with("access.event");
    }

    @Bean
    public Binding accessRecordBinding() {
        return BindingBuilder.bind(accessRecordQueue())
                .to(accessExchange())
                .with("access.record");
    }

    // ============================================================
    // 考勤事件队列
    // ============================================================

    @Bean
    public Queue attendanceRecordQueue() {
        return QueueBuilder.durable("ioedream.attendance.record.queue")
                .withArgument("x-dead-letter-exchange", "ioedream.dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "attendance.record.dlx")
                .build();
    }

    @Bean
    public TopicExchange attendanceExchange() {
        return new TopicExchange("ioedream.attendance.exchange", true, false);
    }

    @Bean
    public Binding attendanceRecordBinding() {
        return BindingBuilder.bind(attendanceRecordQueue())
                .to(attendanceExchange())
                .with("attendance.record");
    }

    // ============================================================
    // 消费事件队列
    // ============================================================

    @Bean
    public Queue consumeTransactionQueue() {
        return QueueBuilder.durable("ioedream.consume.transaction.queue")
                .withArgument("x-dead-letter-exchange", "ioedream.dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "consume.transaction.dlx")
                .build();
    }

    @Bean
    public TopicExchange consumeExchange() {
        return new TopicExchange("ioedream.consume.exchange", true, false);
    }

    @Bean
    public Binding consumeTransactionBinding() {
        return BindingBuilder.bind(consumeTransactionQueue())
                .to(consumeExchange())
                .with("consume.transaction");
    }

    // ============================================================
    // 访客事件队列
    // ============================================================

    @Bean
    public Queue visitorRegistrationQueue() {
        return QueueBuilder.durable("ioedream.visitor.registration.queue")
                .withArgument("x-dead-letter-exchange", "ioedream.dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "visitor.registration.dlx")
                .build();
    }

    @Bean
    public TopicExchange visitorExchange() {
        return new TopicExchange("ioedream.visitor.exchange", true, false);
    }

    @Bean
    public Binding visitorRegistrationBinding() {
        return BindingBuilder.bind(visitorRegistrationQueue())
                .to(visitorExchange())
                .with("visitor.registration");
    }

    // ============================================================
    // 视频事件队列
    // ============================================================

    @Bean
    public Queue videoStreamQueue() {
        return QueueBuilder.nonDurable("ioedream.video.stream.queue")
                .withArgument("x-message-ttl", 300000)  // 5分钟过期
                .autoDelete()
                .build();
    }

    @Bean
    public TopicExchange videoExchange() {
        return new TopicExchange("ioedream.video.exchange", true, false);
    }

    @Bean
    public Binding videoStreamBinding() {
        return BindingBuilder.bind(videoStreamQueue())
                .to(videoExchange())
                .with("video.stream");
    }

    // ============================================================
    // OA事件队列
    // ============================================================

    @Bean
    public Queue oaWorkflowQueue() {
        return QueueBuilder.durable("ioedream.oa.workflow.queue")
                .withArgument("x-dead-letter-exchange", "ioedream.dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "oa.workflow.dlx")
                .build();
    }

    @Bean
    public TopicExchange oaExchange() {
        return new TopicExchange("ioedream.oa.exchange", true, false);
    }

    @Bean
    public Binding oaWorkflowBinding() {
        return BindingBuilder.bind(oaWorkflowQueue())
                .to(oaExchange())
                .with("oa.workflow");
    }

    // ============================================================
    // 事件监听器
    // ============================================================

    @Bean
    public RabbitMQEventListener rabbitMQEventListener() {
        return new RabbitMQEventListener();
    }
}