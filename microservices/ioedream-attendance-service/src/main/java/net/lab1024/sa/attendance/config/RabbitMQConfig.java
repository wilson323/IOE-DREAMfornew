package net.lab1024.sa.attendance.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 * <p>
 * 配置考勤事件交换机、队列和绑定关系
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Configuration
public class RabbitMQConfig {

    /**
     * 考勤事件交换机（Direct类型）
     */
    @Bean
    public DirectExchange attendanceEventExchange() {
        return new DirectExchange("attendance.event.exchange", true, false);
    }

    /**
     * 考勤事件队列（持久化）
     */
    @Bean
    public Queue attendanceEventQueue() {
        return QueueBuilder.durable("attendance.event.queue").build();
    }

    /**
     * 考勤事件绑定
     */
    @Bean
    public Binding attendanceEventBinding() {
        return BindingBuilder.bind(attendanceEventQueue())
                .to(attendanceEventExchange())
                .with("attendance.event.routing.key");
    }

    /**
     * JSON消息转换器
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate（使用JSON转换器）
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
