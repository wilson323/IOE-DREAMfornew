package net.lab1024.sa.oa.workflow.performance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Tags;

import jakarta.annotation.Resource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Supplier;
import java.time.Duration;

/**
 * 工作流异步处理器
 * <p>
 * 提供企业级异步处理能力，包括任务异步化、事件驱动处理、
 * 批量异步操作、异步回调等高级功能
 * 集成性能监控和错误处理机制
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@Component
public class WorkflowAsyncProcessor {

    @Resource
    private MeterRegistry meterRegistry;

    // 性能指标
    private final Timer asyncProcessTimer;
    private final Counter asyncProcessCounter;
    private final Counter asyncErrorCounter;
    private final Counter asyncSuccessCounter;

    /**
     * 异步处理结果
     */
    public static class AsyncResult<T> {
        private final boolean success;
        private final T data;
        private final String error;
        private final long duration;
        private final String traceId;

        public AsyncResult(boolean success, T data, String error, long duration, String traceId) {
            this.success = success;
            this.data = data;
            this.error = error;
            this.duration = duration;
            this.traceId = traceId;
        }

        // Getters
        public boolean isSuccess() { return success; }
        public T getData() { return data; }
        public String getError() { return error; }
        public long getDuration() { return duration; }
        public String getTraceId() { return traceId; }
    }

    public WorkflowAsyncProcessor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // 初始化性能指标
        this.asyncProcessTimer = Timer.builder("workflow.async.process.duration")
                .description("异步处理耗时")
                .register(meterRegistry);

        this.asyncProcessCounter = Counter.builder("workflow.async.process.count")
                .description("异步处理次数")
                .register(meterRegistry);

        this.asyncErrorCounter = Counter.builder("workflow.async.error.count")
                .description("异步处理错误次数")
                .register(meterRegistry);

        this.asyncSuccessCounter = Counter.builder("workflow.async.success.count")
                .description("异步处理成功次数")
                .register(meterRegistry);

        log.info("[工作流异步处理器] 初始化完成");
    }

    /**
     * 异步执行任务
     *
     * @param taskName 任务名称
     * @param task 任务逻辑
     * @return 异步结果
     */
    @Async("workflowAsyncExecutor")
    public <T> CompletableFuture<AsyncResult<T>> executeAsync(String taskName, Supplier<T> task) {
        long startTime = System.currentTimeMillis();
        String traceId = generateTraceId();

        log.debug("[工作流异步] 开始执行异步任务: taskName={}, traceId={}", taskName, traceId);

        return CompletableFuture.supplyAsync(() -> {
            Timer.Sample sample = Timer.start(meterRegistry);

            try {
                asyncProcessCounter.increment();
                Counter.builder("workflow.async.task.process")
                        .tag("task", taskName)
                        .register(meterRegistry)
                        .increment();

                T result = task.get();
                long duration = System.currentTimeMillis() - startTime;

                asyncSuccessCounter.increment();
                Counter.builder("workflow.async.task.success")
                        .tag("task", taskName)
                        .register(meterRegistry)
                        .increment();

                log.debug("[工作流异步] 任务执行成功: taskName={}, traceId={}, duration={}ms",
                        taskName, traceId, duration);

                return new AsyncResult<>(true, result, null, duration, traceId);

            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;

                asyncErrorCounter.increment();
                Counter.builder("workflow.async.task.error")
                        .tag("task", taskName)
                        .register(meterRegistry)
                        .increment();

                log.error("[工作流异步] 任务执行失败: taskName={}, traceId={}, duration={}ms, error={}",
                        taskName, traceId, duration, e.getMessage(), e);

                return new AsyncResult<>(false, null, e.getMessage(), duration, traceId);

            } finally {
                sample.stop(Timer.builder("workflow.async.task.duration")
                        .tag("task", taskName)
                        .register(meterRegistry));
            }
        });
    }

    /**
     * 批量异步执行任务
     *
     * @param taskName 任务名称
     * @param tasks 任务列表
     * @return 异步结果列表
     */
    @Async("workflowAsyncExecutor")
    public <T> CompletableFuture<List<AsyncResult<T>>> executeBatchAsync(String taskName, List<Supplier<T>> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        long startTime = System.currentTimeMillis();
        String traceId = generateTraceId();

        log.debug("[工作流异步] 开始批量执行异步任务: taskName={}, count={}, traceId={}",
                taskName, tasks.size(), traceId);

        asyncProcessCounter.increment();
        Counter.builder("workflow.async.batch.process")
                .tag("task", taskName)
                .register(meterRegistry)
                .increment();

        List<CompletableFuture<AsyncResult<T>>> futures = new ArrayList<>();

        for (int i = 0; i < tasks.size(); i++) {
            final int taskIndex = i;
            Supplier<T> task = tasks.get(i);

            CompletableFuture<AsyncResult<T>> future = CompletableFuture.supplyAsync(() -> {
                String taskTraceId = traceId + "-" + taskIndex;
                long taskStartTime = System.currentTimeMillis();

                try {
                    T result = task.get();
                    long duration = System.currentTimeMillis() - taskStartTime;

                    log.debug("[工作流异步] 批量任务执行成功: taskName={}, index={}, traceId={}, duration={}ms",
                            taskName, taskIndex, taskTraceId, duration);

                    return new AsyncResult<>(true, result, null, duration, taskTraceId);

                } catch (Exception e) {
                    long duration = System.currentTimeMillis() - taskStartTime;

                    log.error("[工作流异步] 批量任务执行失败: taskName={}, index={}, traceId={}, duration={}ms, error={}",
                            taskName, taskIndex, taskTraceId, duration, e.getMessage(), e);

                    return new AsyncResult<>(false, null, e.getMessage(), duration, taskTraceId);
                }
            });

            futures.add(future);
        }

        // 等待所有任务完成
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<AsyncResult<T>> results = new ArrayList<>();
                    int successCount = 0;
                    int errorCount = 0;

                    for (CompletableFuture<AsyncResult<T>> future : futures) {
                        try {
                            AsyncResult<T> result = future.get();
                            results.add(result);

                            if (result.isSuccess()) {
                                successCount++;
                            } else {
                                errorCount++;
                            }
                        } catch (Exception e) {
                            log.error("[工作流异步] 获取批量任务结果失败: taskName={}, error={}",
                                    taskName, e.getMessage(), e);
                            errorCount++;
                        }
                    }

                    long totalDuration = System.currentTimeMillis() - startTime;

                    log.info("[工作流异步] 批量任务执行完成: taskName={}, total={}, success={}, error={}, duration={}ms",
                            taskName, tasks.size(), successCount, errorCount, totalDuration);

                    return results;
                });
    }

    /**
     * 异步执行带回调的任务
     *
     * @param taskName 任务名称
     * @param task 任务逻辑
     * @param onSuccess 成功回调
     * @param onError 失败回调
     * @return 异步结果
     */
    @Async("workflowAsyncExecutor")
    public <T> CompletableFuture<AsyncResult<T>> executeAsyncWithCallback(
            String taskName,
            Supplier<T> task,
            java.util.function.Consumer<T> onSuccess,
            java.util.function.Consumer<String> onError) {

        long startTime = System.currentTimeMillis();
        String traceId = generateTraceId();

        log.debug("[工作流异步] 开始执行带回调的异步任务: taskName={}, traceId={}", taskName, traceId);

        return CompletableFuture.supplyAsync(() -> {
            try {
                asyncProcessCounter.increment();
                Counter.builder("workflow.async.callback.process")
                        .tag("task", taskName)
                        .register(meterRegistry)
                        .increment();

                T result = task.get();
                long duration = System.currentTimeMillis() - startTime;

                // 执行成功回调
                if (onSuccess != null) {
                    try {
                        onSuccess.accept(result);
                        log.debug("[工作流异步] 成功回调执行完成: taskName={}, traceId={}", taskName, traceId);
                    } catch (Exception callbackException) {
                        log.error("[工作流异步] 成功回调执行失败: taskName={}, traceId={}, error={}",
                                taskName, traceId, callbackException.getMessage(), callbackException);
                    }
                }

                asyncSuccessCounter.increment();
                Counter.builder("workflow.async.callback.success")
                        .tag("task", taskName)
                        .register(meterRegistry)
                        .increment();

                log.debug("[工作流异步] 带回调任务执行成功: taskName={}, traceId={}, duration={}ms",
                        taskName, traceId, duration);

                return new AsyncResult<>(true, result, null, duration, traceId);

            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                String errorMessage = e.getMessage();

                // 执行失败回调
                if (onError != null) {
                    try {
                        onError.accept(errorMessage);
                        log.debug("[工作流异步] 失败回调执行完成: taskName={}, traceId={}", taskName, traceId);
                    } catch (Exception callbackException) {
                        log.error("[工作流异步] 失败回调执行失败: taskName={}, traceId={}, error={}",
                                taskName, traceId, callbackException.getMessage(), callbackException);
                    }
                }

                asyncErrorCounter.increment();
                Counter.builder("workflow.async.callback.error")
                        .tag("task", taskName)
                        .register(meterRegistry)
                        .increment();

                log.error("[工作流异步] 带回调任务执行失败: taskName={}, traceId={}, duration={}ms, error={}",
                        taskName, traceId, duration, e.getMessage(), e);

                return new AsyncResult<>(false, null, errorMessage, duration, traceId);
            }
        });
    }

    /**
     * 延迟异步执行任务
     *
     * @param taskName 任务名称
     * @param task 任务逻辑
     * @param delay 延迟时间
     * @return 异步结果
     */
    @Async("workflowAsyncExecutor")
    public <T> CompletableFuture<AsyncResult<T>> executeAsyncWithDelay(
            String taskName,
            Supplier<T> task,
            Duration delay) {

        long startTime = System.currentTimeMillis();
        String traceId = generateTraceId();

        log.debug("[工作流异步] 开始延迟执行异步任务: taskName={}, delay={}, traceId={}",
                taskName, delay.toMillis(), traceId);

        return CompletableFuture.supplyAsync(() -> {
                    // 先等待延迟时间
                    try {
                        Thread.sleep(delay.toMillis());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("[工作流异步] 延迟任务被中断: taskName={}, traceId={}", taskName, traceId);
                        return new AsyncResult<T>(false, null, "任务被中断", 0L, traceId);
                    }

                    long actualStartTime = System.currentTimeMillis();

                    log.debug("[工作流异步] 延迟时间结束，开始执行任务: taskName={}, traceId={}", taskName, traceId);

                    try {
                        asyncProcessCounter.increment();
                        Counter.builder("workflow.async.delayed.count")
                                .tag("task", taskName)
                                .register(meterRegistry)
                                .increment();

                        T result = task.get();
                        long duration = System.currentTimeMillis() - actualStartTime;
                        long totalDuration = System.currentTimeMillis() - startTime;

                        asyncSuccessCounter.increment();
                        Counter.builder("workflow.async.delayed.success")
                                .tag("task", taskName)
                                .register(meterRegistry)
                                .increment();

                        log.debug("[工作流异步] 延迟任务执行成功: taskName={}, delay={}, duration={}, total={}, traceId={}",
                                taskName, delay.toMillis(), duration, totalDuration, traceId);

                        return new AsyncResult<>(true, result, null, duration, traceId);

                    } catch (Exception e) {
                        long duration = System.currentTimeMillis() - actualStartTime;
                        long totalDuration = System.currentTimeMillis() - startTime;

                        asyncErrorCounter.increment();
                        Counter.builder("workflow.async.delayed.error")
                                .tag("task", taskName)
                                .register(meterRegistry)
                                .increment();

                        log.error("[工作流异步] 延迟任务执行失败: taskName={}, delay={}, duration={}, total={}, traceId={}, error={}",
                                taskName, delay.toMillis(), duration, totalDuration, traceId, e.getMessage(), e);

                        return new AsyncResult<>(false, null, e.getMessage(), duration, traceId);
                    }
                });
    }

    /**
     * 事务提交后异步执行
     *
     * @param taskName 任务名称
     * @param task 任务逻辑
     * @return 异步结果
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("workflowAsyncExecutor")
    public <T> CompletableFuture<AsyncResult<T>> executeAsyncAfterCommit(
            String taskName,
            Supplier<T> task) {

        long startTime = System.currentTimeMillis();
        String traceId = generateTraceId();

        log.debug("[工作流异步] 事务提交后开始执行异步任务: taskName={}, traceId={}", taskName, traceId);

        return CompletableFuture.supplyAsync(() -> {
            try {
                asyncProcessCounter.increment();
                Counter.builder("workflow.async.aftercommit.process")
                        .tag("task", taskName)
                        .register(meterRegistry)
                        .increment();

                T result = task.get();
                long duration = System.currentTimeMillis() - startTime;

                asyncSuccessCounter.increment();
                Counter.builder("workflow.async.aftercommit.success")
                        .tag("task", taskName)
                        .register(meterRegistry)
                        .increment();

                log.debug("[工作流异步] 事务提交后任务执行成功: taskName={}, traceId={}, duration={}ms",
                        taskName, traceId, duration);

                return new AsyncResult<>(true, result, null, duration, traceId);

            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;

                asyncErrorCounter.increment();
                Counter.builder("workflow.async.aftercommit.error")
                        .tag("task", taskName)
                        .register(meterRegistry)
                        .increment();

                log.error("[工作流异步] 事务提交后任务执行失败: taskName={}, traceId={}, duration={}ms, error={}",
                        taskName, traceId, duration, e.getMessage(), e);

                return new AsyncResult<>(false, null, e.getMessage(), duration, traceId);
            }
        });
    }

    /**
     * 获取异步处理统计信息
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // 基础统计（Counter.count()返回double类型）
        double processCount = asyncProcessCounter.count();
        double successCount = asyncSuccessCounter.count();
        double errorCount = asyncErrorCounter.count();

        stats.put("processCount", (long) processCount);
        stats.put("successCount", (long) successCount);
        stats.put("errorCount", (long) errorCount);

        // 成功率
        double total = successCount + errorCount;
        double successRate = total > 0 ? successCount / total * 100 : 0;
        stats.put("successRate", String.format("%.2f%%", successRate));

        // 错误率
        double errorRate = total > 0 ? errorCount / total * 100 : 0;
        stats.put("errorRate", String.format("%.2f%%", errorRate));

        return stats;
    }

    /**
     * 生成追踪ID
     */
    private String generateTraceId() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 创建标签
     */
    private io.micrometer.core.instrument.Tags Tags(String... keyValues) {
        return io.micrometer.core.instrument.Tags.of(keyValues);
    }
}
