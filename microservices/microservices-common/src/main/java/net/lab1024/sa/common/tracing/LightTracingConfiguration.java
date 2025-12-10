package net.lab1024.sa.common.tracing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.UUID;

/**
 * 轻量级分布式追踪配置
 * <p>
 * 避免过度工程化，只保留核心追踪功能：
 * - 简单的Trace ID生成
 * - 基本的调用链信息
 * - 日志关联标识
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.sleuth.enabled", havingValue = "true", matchIfMissing = false)
public class LightTracingConfiguration {

    /**
     * 初始化轻量级追踪
     */
    @PostConstruct
    public void initLightTracing() {
        log.info("[轻量追踪] 已启用 - 基础调用链追踪");
    }

    /**
     * 轻量级追踪工具类
     */
    public static class LightTracer {

        private static final ThreadLocal<String> traceIdHolder = new ThreadLocal<>();
        private static final ThreadLocal<Long> startTimeHolder = new ThreadLocal<>();

        /**
         * 开始追踪
         */
        public static String startTrace(String operation) {
            String traceId = generateTraceId();
            traceIdHolder.set(traceId);
            startTimeHolder.set(System.currentTimeMillis());

            log.info("[追踪] 开始 - traceId={}, operation={}", traceId, operation);
            return traceId;
        }

        /**
         * 结束追踪
         */
        public static void endTrace(String operation, boolean success) {
            String traceId = traceIdHolder.get();
            Long startTime = startTimeHolder.get();

            if (traceId != null && startTime != null) {
                long duration = System.currentTimeMillis() - startTime;
                log.info("[追踪] 结束 - traceId={}, operation={}, duration={}ms, success={}",
                    traceId, operation, duration, success);

                clearTrace();
            }
        }

        /**
         * 记录追踪事件
         */
        public static void traceEvent(String event, Object... params) {
            String traceId = traceIdHolder.get();
            if (traceId != null) {
                log.info("[追踪] 事件 - traceId={}, event={}, params={}",
                    traceId, event, java.util.Arrays.toString(params));
            }
        }

        /**
         * 获取当前追踪ID
         */
        public static String getCurrentTraceId() {
            return traceIdHolder.get();
        }

        /**
         * 生成追踪ID
         */
        private static String generateTraceId() {
            return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }

        /**
         * 清理追踪信息
         */
        private static void clearTrace() {
            traceIdHolder.remove();
            startTimeHolder.remove();
        }

        /**
         * 在MDC中设置追踪ID（用于日志）
         */
        public static void setTraceIdInMDC() {
            String traceId = getCurrentTraceId();
            if (traceId != null) {
                org.slf4j.MDC.put("traceId", traceId);
            }
        }
    }

    /**
     * 追踪注解
     */
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD})
    public @interface Trace {
        String value() default "";
    }

    /**
     * 追踪切面
     */
    @org.aspectj.lang.annotation.Aspect
    @org.springframework.stereotype.Component
    @ConditionalOnProperty(name = "spring.sleuth.enabled", havingValue = "true", matchIfMissing = false)
    public static class TraceAspect {

        @org.aspectj.lang.annotation.Around("@annotation(trace)")
        public Object traceMethod(org.aspectj.lang.ProceedingJoinPoint joinPoint, Trace trace) throws Throwable {
            String operation = trace.value().isEmpty() ?
                joinPoint.getSignature().toShortString() : trace.value();

            LightTracer.startTrace(operation);
            LightTracer.setTraceIdInMDC();
            // traceId已通过setTraceIdInMDC()设置到MDC中，无需额外变量

            try {
                Object result = joinPoint.proceed();
                LightTracer.endTrace(operation, true);
                return result;
            } catch (Exception e) {
                LightTracer.traceEvent("error", e.getMessage());
                LightTracer.endTrace(operation, false);
                throw e;
            } finally {
                org.slf4j.MDC.remove("traceId");
            }
        }
    }
}
