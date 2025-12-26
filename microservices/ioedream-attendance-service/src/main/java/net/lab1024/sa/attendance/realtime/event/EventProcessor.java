package net.lab1024.sa.attendance.realtime.event;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 考勤事件处理器接口
 * <p>
 * 定义事件处理的标准接口
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface EventProcessor {

    /**
     * 处理单个事件
     *
     * @param event 考勤事件
     * @return 处理结果
     */
    CompletableFuture<EventProcessingResult> processEvent(AttendanceEvent event);

    /**
     * 批量处理事件
     *
     * @param events 事件列表
     * @return 批量处理结果
     */
    CompletableFuture<BatchEventProcessingResult> processBatchEvents(List<AttendanceEvent> events);

    /**
     * 获取处理器支持的事件类型
     *
     * @return 支持的事件类型列表
     */
    List<AttendanceEvent.AttendanceEventType> getSupportedEventTypes();

    /**
     * 获取处理器优先级
     *
     * @return 优先级（数值越小优先级越高）
     */
    int getPriority();

    /**
     * 获取处理器名称
     *
     * @return 处理器名称
     */
    String getProcessorName();

    /**
     * 验证事件是否可处理
     *
     * @param event 考勤事件
     * @return 验证结果
     */
    EventValidationResult validateEvent(AttendanceEvent event);

    /**
     * 获取处理器状态
     *
     * @return 处理器状态
     */
    ProcessorStatus getProcessorStatus();

    /**
     * 停止处理器
     */
    void stop();

    /**
     * 启动处理器
     */
    void start();
}
