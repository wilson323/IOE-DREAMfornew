package net.lab1024.sa.common.tracing;

import lombok.extern.slf4j.Slf4j;

import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.propagation.Propagator;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 分布式追踪工具类
 * <p>
 * 提供统一的分布式追踪操作工具，简化Span和Trace的使用
 * 支持手动创建Span、添加标签、记录事件等操作
 * 纯Java类设计，通过构造函数注入依赖
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 2.0.0
 * @since 2025-12-20
 */
@Slf4j
public class TracingUtils {

    private final Tracer tracer;
    private final Propagator propagator;

    /**
     * 构造函数注入依赖
     *
     * @param tracer 分布式追踪器
     * @param propagator 传播器
     */
    public TracingUtils(Tracer tracer, Propagator propagator) {
        this.tracer = tracer;
        this.propagator = propagator;
    }

    /**
     * 创建新的Span
     *
     * @param operationName 操作名称
     * @return 创建的Span
     */
    public Span startSpan(String operationName) {
        return tracer.nextSpan().name(operationName).start();
    }

    /**
     * 执行带追踪的操作
     *
     * @param operationName 操作名称
     * @param supplier 要执行的操作
     * @param <T> 返回值类型
     * @return 操作结果
     */
    public <T> T withTrace(String operationName, Supplier<T> supplier) {
        Span span = startSpan(operationName);
        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            return supplier.get();
        } finally {
            span.end();
        }
    }

    /**
     * 为Span添加标签
     *
     * @param span Span对象
     * @param key 标签键
     * @param value 标签值
     */
    public void addTag(Span span, String key, String value) {
        if (span != null) {
            span.tag(key, value);
        }
    }

    /**
     * 为当前Span添加标签
     *
     * @param key 标签键
     * @param value 标签值
     */
    public void addTag(String key, String value) {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            addTag(currentSpan, key, value);
        }
    }

    /**
     * 为Span添加事件
     *
     * @param span Span对象
     * @param eventName 事件名称
     */
    public void addEvent(Span span, String eventName) {
        if (span != null) {
            span.event(eventName);
        }
    }

    /**
     * 为当前Span添加事件
     *
     * @param eventName 事件名称
     */
    public void addEvent(String eventName) {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            addEvent(currentSpan, eventName);
        }
    }

    /**
     * 记录错误信息
     *
     * @param exception 异常对象
     */
    public void recordError(Exception exception) {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            currentSpan.tag("error", exception.getMessage());
            currentSpan.tag("error.type", exception.getClass().getSimpleName());
            currentSpan.event("error");
        }
    }

    /**
     * 记录业务指标
     *
     * @param metrics 指标信息
     */
    public void recordMetrics(Map<String, Object> metrics) {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            metrics.forEach((key, value) -> {
                addTag(currentSpan, "metric." + key, String.valueOf(value));
            });
        }
    }

    /**
     * 获取当前Trace ID
     *
     * @return Trace ID
     */
    public String getCurrentTraceId() {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            return currentSpan.context().traceId();
        }
        return "unknown";
    }

    /**
     * 获取当前Span ID
     *
     * @return Span ID
     */
    public String getCurrentSpanId() {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            return currentSpan.context().spanId();
        }
        return "unknown";
    }

    /**
     * 判断是否在追踪上下文中
     *
     * @return 是否在追踪上下文中
     */
    public boolean isTracing() {
        return tracer.currentSpan() != null;
    }

    /**
     * 创建业务操作Span
     *
     * @param moduleName 模块名称
     * @param operation 操作名称
     * @param userId 用户ID
     * @return 创建的Span
     */
    public Span createBusinessSpan(String moduleName, String operation, String userId) {
        Span span = startSpan(String.format("%s.%s", moduleName, operation));
        addTag(span, "module", moduleName);
        addTag(span, "operation", operation);
        addTag(span, "user.id", userId);
        addTag(span, "span.kind", "server");
        return span;
    }
}
