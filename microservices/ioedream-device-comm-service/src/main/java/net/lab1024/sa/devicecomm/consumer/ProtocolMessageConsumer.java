package net.lab1024.sa.devicecomm.consumer;

import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
// import net.lab1024.sa.devicecomm.monitor.ProtocolMetricsCollector; // 已废弃，已移除

/**
 * 协议消息消费者
 * <p>
 * 消费RabbitMQ队列中的协议消息，调用业务服务处理
 * 严格遵循CLAUDE.md规范：
 * - 使用@Component注解
 * - 使用@Resource注入依赖
 * - 完整的函数级注释
 * </p>
 * <p>
 * 功能：
 * - 消费门禁记录队列
 * - 消费考勤记录队列
 * - 消费消费记录队列
 * - 消费设备状态更新队列
 * - 消费报警事件队列
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class ProtocolMessageConsumer {

    /**
     * 网关服务客户端（用于调用其他微服务）
     */
    @Resource
    private GatewayServiceClient gatewayServiceClient;

    /**
     * Micrometer指标注册表（用于编程式指标收集）
     */
    @Resource
    private MeterRegistry meterRegistry;

    /**
     * 消费门禁记录消息
     * <p>
     * 从门禁记录队列中消费消息，调用门禁服务保存通行记录
     * </p>
     *
     * @param request 门禁记录请求数据
     */
    @RabbitListener(queues = "protocol.access.record")
    public void consumeAccessRecord(Map<String, Object> request) {
        long startTime = System.currentTimeMillis();
        String protocolType = "ACCESS_ENTROPY_V4.8";

        try {
            log.info("[协议消费者] 消费门禁记录消息，request={}", request);

            // 记录队列消费操作
            // 记录队列消费操作（使用Micrometer编程式API）
            Counter.builder("protocol.queue.operation")
                    .tag("queue_name", "protocol.access.record")
                    .tag("operation", "consume")
                    .register(meterRegistry)
                    .increment();

            // 调用门禁服务保存通行记录
            ResponseDTO<Long> response = gatewayServiceClient.callAccessService(
                    "/api/v1/access/record/create",
                    HttpMethod.POST,
                    request,
                    Long.class
            );

            long duration = System.currentTimeMillis() - startTime;

            if (response != null && response.isSuccess()) {
                log.info("[协议消费者] 门禁记录保存成功，recordId={}, duration={}ms",
                        response.getData(), duration);
                // 记录成功指标（使用Micrometer编程式API）
                Counter.builder("protocol.message.process")
                        .tag("protocol_type", protocolType)
                        .tag("status", "success")
                        .register(meterRegistry)
                        .increment();
                Counter.builder("protocol.queue.operation")
                        .tag("queue_name", "protocol.access.record")
                        .tag("operation", "ack")
                        .register(meterRegistry)
                        .increment();
            } else {
                log.warn("[协议消费者] 门禁记录保存失败，错误={}, duration={}ms",
                        response != null ? response.getMessage() : "响应为空", duration);
                // 记录错误指标（使用Micrometer编程式API）
                Counter.builder("protocol.message.error")
                        .tag("protocol_type", protocolType)
                        .tag("error_type", "SAVE_ERROR")
                        .register(meterRegistry)
                        .increment();
                Counter.builder("protocol.queue.operation")
                        .tag("queue_name", "protocol.access.record")
                        .tag("operation", "nack")
                        .register(meterRegistry)
                        .increment();
                // 抛出异常，触发重试机制
                log.error("[协议消费者] 门禁记录保存失败, response={}", response);
                throw new SystemException("ACCESS_RECORD_SAVE_ERROR", "门禁记录保存失败：" +
                        (response != null ? response.getMessage() : "响应为空"));
            }

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[协议消费者] 消费门禁记录消息异常，request={}, 错误={}, duration={}ms",
                    request, e.getMessage(), duration, e);
            // 记录错误指标（使用Micrometer编程式API）
            Counter.builder("protocol.message.error")
                    .tag("protocol_type", protocolType)
                    .tag("error_type", "CONSUME_ERROR")
                    .register(meterRegistry)
                    .increment();
            Counter.builder("protocol.queue.operation")
                    .tag("queue_name", "protocol.access.record")
                    .tag("operation", "nack")
                    .register(meterRegistry)
                    .increment();
            // 重新抛出异常，触发重试机制
            throw e;
        }
    }

    /**
     * 消费考勤记录消息
     * <p>
     * 从考勤记录队列中消费消息，调用考勤服务保存考勤记录
     * </p>
     *
     * @param request 考勤记录请求数据
     */
    @RabbitListener(queues = "protocol.attendance.record")
    public void consumeAttendanceRecord(Map<String, Object> request) {
        long startTime = System.currentTimeMillis();
        String protocolType = "ATTENDANCE_ENTROPY_V4.0";

        try {
            log.info("[协议消费者] 消费考勤记录消息，request={}", request);

            // 记录队列消费操作
            // 记录队列消费操作（使用Micrometer编程式API）
            Counter.builder("protocol.queue.operation")
                    .tag("queue_name", "protocol.attendance.record")
                    .tag("operation", "consume")
                    .register(meterRegistry)
                    .increment();

            // 调用考勤服务保存考勤记录
            ResponseDTO<Long> response = gatewayServiceClient.callAttendanceService(
                    "/api/v1/attendance/record/create",
                    HttpMethod.POST,
                    request,
                    Long.class
            );

            long duration = System.currentTimeMillis() - startTime;

            if (response != null && response.isSuccess()) {
                log.info("[协议消费者] 考勤记录保存成功，recordId={}, duration={}ms",
                        response.getData(), duration);
                // 记录成功指标（使用Micrometer编程式API）
                Counter.builder("protocol.message.process")
                        .tag("protocol_type", protocolType)
                        .tag("status", "success")
                        .register(meterRegistry)
                        .increment();
                Counter.builder("protocol.queue.operation")
                        .tag("queue_name", "protocol.attendance.record")
                        .tag("operation", "ack")
                        .register(meterRegistry)
                        .increment();
            } else {
                log.warn("[协议消费者] 考勤记录保存失败，错误={}, duration={}ms",
                        response != null ? response.getMessage() : "响应为空", duration);
                // 记录错误指标（使用Micrometer编程式API）
                Counter.builder("protocol.message.error")
                        .tag("protocol_type", protocolType)
                        .tag("error_type", "SAVE_ERROR")
                        .register(meterRegistry)
                        .increment();
                Counter.builder("protocol.queue.operation")
                        .tag("queue_name", "protocol.attendance.record")
                        .tag("operation", "nack")
                        .register(meterRegistry)
                        .increment();
                // 抛出异常，触发重试机制
                log.error("[协议消费者] 考勤记录保存失败, response={}", response);
                throw new SystemException("ATTENDANCE_RECORD_SAVE_ERROR", "考勤记录保存失败：" +
                        (response != null ? response.getMessage() : "响应为空"));
            }

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[协议消费者] 消费考勤记录消息异常，request={}, 错误={}, duration={}ms",
                    request, e.getMessage(), duration, e);
            // 记录错误指标（使用Micrometer编程式API）
            Counter.builder("protocol.message.error")
                    .tag("protocol_type", protocolType)
                    .tag("error_type", "CONSUME_ERROR")
                    .register(meterRegistry)
                    .increment();
            Counter.builder("protocol.queue.operation")
                    .tag("queue_name", "protocol.attendance.record")
                    .tag("operation", "nack")
                    .register(meterRegistry)
                    .increment();
            // 重新抛出异常，触发重试机制
            throw e;
        }
    }

    /**
     * 消费消费记录消息
     * <p>
     * 从消费记录队列中消费消息，调用消费服务保存消费记录
     * </p>
     *
     * @param request 消费记录请求数据
     */
    @RabbitListener(queues = "protocol.consume.record")
    public void consumeConsumeRecord(Map<String, Object> request) {
        long startTime = System.currentTimeMillis();
        String protocolType = "CONSUME_ZKTECO_V1.0";

        try {
            log.info("[协议消费者] 消费消费记录消息，request={}", request);

            // 记录队列消费操作
            // 记录队列消费操作（使用Micrometer编程式API）
            Counter.builder("protocol.queue.operation")
                    .tag("queue_name", "protocol.consume.record")
                    .tag("operation", "consume")
                    .register(meterRegistry)
                    .increment();

            // 调用消费服务保存消费记录
            ResponseDTO<Long> response = gatewayServiceClient.callConsumeService(
                    "/api/v1/consume/record/create",
                    HttpMethod.POST,
                    request,
                    Long.class
            );

            long duration = System.currentTimeMillis() - startTime;

            if (response != null && response.isSuccess()) {
                log.info("[协议消费者] 消费记录保存成功，recordId={}, duration={}ms",
                        response.getData(), duration);
                // 记录成功指标（使用Micrometer编程式API）
                Counter.builder("protocol.message.process")
                        .tag("protocol_type", protocolType)
                        .tag("status", "success")
                        .register(meterRegistry)
                        .increment();
                Counter.builder("protocol.queue.operation")
                        .tag("queue_name", "protocol.consume.record")
                        .tag("operation", "ack")
                        .register(meterRegistry)
                        .increment();
            } else {
                log.warn("[协议消费者] 消费记录保存失败，错误={}, duration={}ms",
                        response != null ? response.getMessage() : "响应为空", duration);
                // 记录错误指标（使用Micrometer编程式API）
                Counter.builder("protocol.message.error")
                        .tag("protocol_type", protocolType)
                        .tag("error_type", "SAVE_ERROR")
                        .register(meterRegistry)
                        .increment();
                Counter.builder("protocol.queue.operation")
                        .tag("queue_name", "protocol.consume.record")
                        .tag("operation", "nack")
                        .register(meterRegistry)
                        .increment();
                // 抛出异常，触发重试机制
                log.error("[协议消费者] 消费记录保存失败, response={}", response);
                throw new SystemException("CONSUME_RECORD_SAVE_ERROR", "消费记录保存失败：" +
                        (response != null ? response.getMessage() : "响应为空"));
            }

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[协议消费者] 消费消费记录消息异常，request={}, 错误={}, duration={}ms",
                    request, e.getMessage(), duration, e);
            // 记录错误指标（使用Micrometer编程式API）
            Counter.builder("protocol.message.error")
                    .tag("protocol_type", protocolType)
                    .tag("error_type", "CONSUME_ERROR")
                    .register(meterRegistry)
                    .increment();
            Counter.builder("protocol.queue.operation")
                    .tag("queue_name", "protocol.consume.record")
                    .tag("operation", "nack")
                    .register(meterRegistry)
                    .increment();
            // 重新抛出异常，触发重试机制
            throw e;
        }
    }

    /**
     * 消费设备状态更新消息
     * <p>
     * 从设备状态更新队列中消费消息，调用公共服务更新设备状态
     * </p>
     *
     * @param request 设备状态更新请求数据
     */
    @RabbitListener(queues = "protocol.device.status")
    public void consumeDeviceStatus(Map<String, Object> request) {
        long startTime = System.currentTimeMillis();

        try {
            log.info("[协议消费者] 消费设备状态更新消息，request={}", request);

            // 记录队列消费操作
            // 记录队列消费操作（使用Micrometer编程式API）
            Counter.builder("protocol.queue.operation")
                    .tag("queue_name", "protocol.device.status")
                    .tag("operation", "consume")
                    .register(meterRegistry)
                    .increment();

            // 调用公共服务更新设备状态
            ResponseDTO<Void> response = gatewayServiceClient.callCommonService(
                    "/api/v1/device/status/update",
                    HttpMethod.PUT,
                    request,
                    Void.class
            );

            long duration = System.currentTimeMillis() - startTime;

            if (response != null && response.isSuccess()) {
                log.info("[协议消费者] 设备状态更新成功，duration={}ms", duration);
                // 记录队列确认操作（使用Micrometer编程式API）
                Counter.builder("protocol.queue.operation")
                        .tag("queue_name", "protocol.device.status")
                        .tag("operation", "ack")
                        .register(meterRegistry)
                        .increment();
            } else {
                log.warn("[协议消费者] 设备状态更新失败，错误={}, duration={}ms",
                        response != null ? response.getMessage() : "响应为空", duration);
                // 记录队列拒绝操作（使用Micrometer编程式API）
                Counter.builder("protocol.queue.operation")
                        .tag("queue_name", "protocol.device.status")
                        .tag("operation", "nack")
                        .register(meterRegistry)
                        .increment();
                // 抛出异常，触发重试机制
                log.error("[协议消费者] 设备状态更新失败, response={}", response);
                throw new SystemException("DEVICE_STATUS_UPDATE_ERROR", "设备状态更新失败：" +
                        (response != null ? response.getMessage() : "响应为空"));
            }

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[协议消费者] 消费设备状态更新消息异常，request={}, 错误={}, duration={}ms",
                    request, e.getMessage(), duration, e);
            // 记录队列拒绝操作（使用Micrometer编程式API）
            Counter.builder("protocol.queue.operation")
                    .tag("queue_name", "protocol.device.status")
                    .tag("operation", "nack")
                    .register(meterRegistry)
                    .increment();
            // 重新抛出异常，触发重试机制
            throw e;
        }
    }

    /**
     * 消费报警事件消息
     * <p>
     * 从报警事件队列中消费消息，调用公共服务保存报警记录
     * </p>
     *
     * @param request 报警事件请求数据
     */
    @RabbitListener(queues = "protocol.alarm.event")
    public void consumeAlarmEvent(Map<String, Object> request) {
        long startTime = System.currentTimeMillis();

        try {
            log.info("[协议消费者] 消费报警事件消息，request={}", request);

            // 记录队列消费操作
            // 记录队列消费操作（使用Micrometer编程式API）
            Counter.builder("protocol.queue.operation")
                    .tag("queue_name", "protocol.alarm.event")
                    .tag("operation", "consume")
                    .register(meterRegistry)
                    .increment();

            // 调用公共服务保存报警记录
            ResponseDTO<Long> response = gatewayServiceClient.callCommonService(
                    "/api/v1/alarm/record/create",
                    HttpMethod.POST,
                    request,
                    Long.class
            );

            long duration = System.currentTimeMillis() - startTime;

            if (response != null && response.isSuccess()) {
                log.info("[协议消费者] 报警事件保存成功，recordId={}, duration={}ms",
                        response.getData(), duration);
                // 记录队列确认操作（使用Micrometer编程式API）
                Counter.builder("protocol.queue.operation")
                        .tag("queue_name", "protocol.alarm.event")
                        .tag("operation", "ack")
                        .register(meterRegistry)
                        .increment();
            } else {
                log.warn("[协议消费者] 报警事件保存失败，错误={}, duration={}ms",
                        response != null ? response.getMessage() : "响应为空", duration);
                // 记录队列拒绝操作（使用Micrometer编程式API）
                Counter.builder("protocol.queue.operation")
                        .tag("queue_name", "protocol.alarm.event")
                        .tag("operation", "nack")
                        .register(meterRegistry)
                        .increment();
                // 抛出异常，触发重试机制
                log.error("[协议消费者] 报警事件保存失败, response={}", response);
                throw new SystemException("ALARM_EVENT_SAVE_ERROR", "报警事件保存失败：" +
                        (response != null ? response.getMessage() : "响应为空"));
            }

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[协议消费者] 消费报警事件消息异常，request={}, 错误={}, duration={}ms",
                    request, e.getMessage(), duration, e);
            // 记录队列拒绝操作（使用Micrometer编程式API）
            Counter.builder("protocol.queue.operation")
                    .tag("queue_name", "protocol.alarm.event")
                    .tag("operation", "nack")
                    .register(meterRegistry)
                    .increment();
            // 重新抛出异常，触发重试机制
            throw e;
        }
    }
}

