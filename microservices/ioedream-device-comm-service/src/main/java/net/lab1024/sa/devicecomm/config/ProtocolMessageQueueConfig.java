package net.lab1024.sa.devicecomm.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 协议消息队列配置
 * <p>
 * 配置RabbitMQ消息队列，用于协议消息的异步处理和缓冲
 * 严格遵循CLAUDE.md规范：
 * - 使用@Configuration注解
 * - 完整的函数级注释
 * </p>
 * <p>
 * 功能：
 * - 门禁记录队列
 * - 考勤记录队列
 * - 消费记录队列
 * - 设备状态更新队列
 * - 报警事件队列
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Configuration
public class ProtocolMessageQueueConfig {

    /**
     * 门禁记录队列
     * <p>
     * 用于缓冲门禁通行记录，异步处理，提高系统吞吐量
     * </p>
     *
     * @return 门禁记录队列
     */
    @Bean
    public Queue accessRecordQueue() {
        return QueueBuilder.durable("protocol.access.record")
                .withArgument("x-message-ttl", 3600000) // 消息TTL：1小时
                .withArgument("x-max-length", 10000) // 最大队列长度：10000
                .build();
    }

    /**
     * 考勤记录队列
     * <p>
     * 用于缓冲考勤打卡记录，异步处理，提高系统吞吐量
     * </p>
     *
     * @return 考勤记录队列
     */
    @Bean
    public Queue attendanceRecordQueue() {
        return QueueBuilder.durable("protocol.attendance.record")
                .withArgument("x-message-ttl", 3600000) // 消息TTL：1小时
                .withArgument("x-max-length", 10000) // 最大队列长度：10000
                .build();
    }

    /**
     * 消费记录队列
     * <p>
     * 用于缓冲消费记录，异步处理，提高系统吞吐量
     * </p>
     *
     * @return 消费记录队列
     */
    @Bean
    public Queue consumeRecordQueue() {
        return QueueBuilder.durable("protocol.consume.record")
                .withArgument("x-message-ttl", 3600000) // 消息TTL：1小时
                .withArgument("x-max-length", 10000) // 最大队列长度：10000
                .build();
    }

    /**
     * 设备状态更新队列
     * <p>
     * 用于缓冲设备状态更新消息，异步处理
     * </p>
     *
     * @return 设备状态更新队列
     */
    @Bean
    public Queue deviceStatusQueue() {
        return QueueBuilder.durable("protocol.device.status")
                .withArgument("x-message-ttl", 1800000) // 消息TTL：30分钟
                .withArgument("x-max-length", 5000) // 最大队列长度：5000
                .build();
    }

    /**
     * 报警事件队列
     * <p>
     * 用于缓冲报警事件，异步处理，确保报警及时处理
     * </p>
     *
     * @return 报警事件队列
     */
    @Bean
    public Queue alarmEventQueue() {
        return QueueBuilder.durable("protocol.alarm.event")
                .withArgument("x-message-ttl", 7200000) // 消息TTL：2小时
                .withArgument("x-max-length", 5000) // 最大队列长度：5000
                .build();
    }

    /**
     * 死信队列
     * <p>
     * 用于存储处理失败的消息，便于后续分析和重试
     * </p>
     *
     * @return 死信队列
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("protocol.dead.letter")
                .withArgument("x-message-ttl", 86400000) // 消息TTL：24小时
                .withArgument("x-max-length", 10000) // 最大队列长度：10000
                .build();
    }
}

