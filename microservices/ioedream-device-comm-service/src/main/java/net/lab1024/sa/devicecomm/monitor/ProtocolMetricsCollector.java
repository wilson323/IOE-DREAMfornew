package net.lab1024.sa.devicecomm.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

/**
 * 协议监控指标收集器
 * <p>
 * 收集协议处理相关的业务指标，用于监控和告警
 * 严格遵循CLAUDE.md规范：
 * - 使用@Component注解
 * - 使用@Resource注入依赖
 * - 完整的函数级注释
 * </p>
 * <p>
 * 监控指标：
 * - 消息处理量（TPS）
 * - 消息处理成功率
 * - 消息处理延迟（P50/P90/P99）
 * - 各协议类型的处理统计
 * - 错误类型统计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class ProtocolMetricsCollector {

    /**
     * Micrometer指标注册表
     */
    @Resource
    private MeterRegistry meterRegistry;

    /**
     * 初始化监控指标
     * <p>
     * 在Spring容器启动后初始化所有监控指标
     * 注意：指标通过builder动态创建，不需要预先声明字段
     * </p>
     */
    @PostConstruct
    public void init() {
        log.info("[协议监控] 初始化协议监控指标收集器");
        log.info("[协议监控] 协议监控指标收集器初始化完成");
    }

    /**
     * 记录消息处理成功
     * <p>
     * 记录协议消息处理成功的指标
     * </p>
     *
     * @param protocolType 协议类型（ACCESS_ENTROPY_V4.8、ATTENDANCE_ENTROPY_V4.0、CONSUME_ZKTECO_V1.0）
     * @param duration 处理耗时（毫秒）
     */
    public void recordSuccess(String protocolType, long duration) {
        try {
            // 记录成功计数
            Counter.builder("protocol.message.process")
                    .tag("protocol_type", protocolType != null ? protocolType : "unknown")
                    .tag("status", "success")
                    .register(meterRegistry)
                    .increment();

            // 记录处理延迟
            Timer.builder("protocol.message.process.duration")
                    .tag("protocol_type", protocolType != null ? protocolType : "unknown")
                    .register(meterRegistry)
                    .record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);

            log.debug("[协议监控] 记录消息处理成功，protocolType={}, duration={}ms", protocolType, duration);
        } catch (Exception e) {
            log.warn("[协议监控] 记录成功指标异常，protocolType={}, 错误={}", protocolType, e.getMessage());
        }
    }

    /**
     * 记录消息处理失败
     * <p>
     * 记录协议消息处理失败的指标
     * </p>
     *
     * @param protocolType 协议类型
     * @param errorType 错误类型（PARSE_ERROR、PROCESS_ERROR、NETWORK_ERROR等）
     */
    public void recordError(String protocolType, String errorType) {
        try {
            // 记录失败计数
            Counter.builder("protocol.message.process")
                    .tag("protocol_type", protocolType != null ? protocolType : "unknown")
                    .tag("status", "error")
                    .register(meterRegistry)
                    .increment();

            // 记录错误类型
            Counter.builder("protocol.message.error")
                    .tag("protocol_type", protocolType != null ? protocolType : "unknown")
                    .tag("error_type", errorType != null ? errorType : "unknown")
                    .register(meterRegistry)
                    .increment();

            log.debug("[协议监控] 记录消息处理失败，protocolType={}, errorType={}", protocolType, errorType);
        } catch (Exception e) {
            log.warn("[协议监控] 记录错误指标异常，protocolType={}, errorType={}, 错误={}", 
                    protocolType, errorType, e.getMessage());
        }
    }

    /**
     * 记录消息处理延迟
     * <p>
     * 记录协议消息处理的延迟时间
     * </p>
     *
     * @param protocolType 协议类型
     * @param duration 处理耗时（毫秒）
     */
    public void recordDuration(String protocolType, long duration) {
        try {
            Timer.builder("protocol.message.process.duration")
                    .tag("protocol_type", protocolType != null ? protocolType : "unknown")
                    .register(meterRegistry)
                    .record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);

            log.debug("[协议监控] 记录消息处理延迟，protocolType={}, duration={}ms", protocolType, duration);
        } catch (Exception e) {
            log.warn("[协议监控] 记录延迟指标异常，protocolType={}, 错误={}", protocolType, e.getMessage());
        }
    }

    /**
     * 记录消息队列操作
     * <p>
     * 记录消息队列的发送和消费操作
     * </p>
     *
     * @param queueName 队列名称
     * @param operation 操作类型（send、consume、ack、nack）
     */
    public void recordQueueOperation(String queueName, String operation) {
        try {
            Counter.builder("protocol.queue.operation")
                    .tag("queue_name", queueName != null ? queueName : "unknown")
                    .tag("operation", operation != null ? operation : "unknown")
                    .register(meterRegistry)
                    .increment();

            log.debug("[协议监控] 记录队列操作，queueName={}, operation={}", queueName, operation);
        } catch (Exception e) {
            log.warn("[协议监控] 记录队列操作指标异常，queueName={}, operation={}, 错误={}", 
                    queueName, operation, e.getMessage());
        }
    }
}

