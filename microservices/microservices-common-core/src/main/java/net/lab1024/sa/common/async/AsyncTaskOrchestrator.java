package net.lab1024.sa.common.async;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 异步任务编排器
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 使用CompletableFuture实现异步任务编排，支持：
 * - 并行任务执行
 * - 任务依赖管理
 * - 异常处理和降级
 * - 超时控制
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
public class AsyncTaskOrchestrator {

    private final Executor executor;

    /**
     * 构造函数
     * <p>
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
     * Manager类在microservices-common中是纯Java类，不使用Spring注解
     * </p>
     *
     * @param executor 线程池执行器
     */
    public AsyncTaskOrchestrator(Executor executor) {
        this.executor = executor;
    }

    /**
     * 并行执行多个任务
     * <p>
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
     * 所有任务并行执行，等待所有任务完成
     * </p>
     *
     * @param tasks 任务列表
     * @param <T> 任务返回类型
     * @return 所有任务的执行结果
     */
    public <T> CompletableFuture<List<TaskResult<T>>> executeParallel(List<Supplier<T>> tasks) {
        List<CompletableFuture<TaskResult<T>>> futures = tasks.stream()
                .map(task -> CompletableFuture.supplyAsync(() -> {
                    long startTime = System.currentTimeMillis();
                    try {
                        T result = task.get();
                        long duration = System.currentTimeMillis() - startTime;
                        log.debug("[异步编排] 任务执行成功, duration={}ms", duration);
                        return TaskResult.success(result, duration);
                    } catch (Exception e) {
                        long duration = System.currentTimeMillis() - startTime;
                        log.error("[异步编排] 任务执行失败, duration={}ms", duration, e);
                        return TaskResult.failed(e.getMessage(), duration);
                    }
                }, executor))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));
    }

    /**
     * 顺序执行多个任务（前一个任务的结果作为后一个任务的输入）
     * <p>
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
     * 任务按顺序执行，支持任务间数据传递
     * </p>
     *
     * @param initialValue 初始值
     * @param tasks 任务列表（每个任务接收前一个任务的结果）
     * @param <T> 任务数据类型
     * @return 最后一个任务的执行结果
     */
    public <T> CompletableFuture<TaskResult<T>> executeSequential(
            T initialValue, List<java.util.function.Function<T, T>> tasks) {

        CompletableFuture<TaskResult<T>> future = CompletableFuture.completedFuture(
                TaskResult.success(initialValue, 0L));

        for (java.util.function.Function<T, T> task : tasks) {
            future = future.thenCompose(result -> {
                if (!result.isSuccess()) {
                    return CompletableFuture.completedFuture(result);
                }

                return CompletableFuture.supplyAsync(() -> {
                    long startTime = System.currentTimeMillis();
                    try {
                        T nextValue = task.apply(result.getData());
                        long duration = System.currentTimeMillis() - startTime;
                        log.debug("[异步编排] 顺序任务执行成功, duration={}ms", duration);
                        return TaskResult.success(nextValue, duration);
                    } catch (Exception e) {
                        long duration = System.currentTimeMillis() - startTime;
                        log.error("[异步编排] 顺序任务执行失败, duration={}ms", duration, e);
                        return TaskResult.failed(e.getMessage(), duration);
                    }
                }, executor);
            });
        }

        return future;
    }

    /**
     * 执行任务（带超时控制）
     * <p>
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
     * 任务执行超时后自动取消并返回失败结果
     * </p>
     *
     * @param task 任务
     * @param timeout 超时时间（毫秒）
     * @param <T> 任务返回类型
     * @return 任务执行结果
     */
    public <T> CompletableFuture<TaskResult<T>> executeWithTimeout(
            Supplier<T> task, long timeout) {

        CompletableFuture<TaskResult<T>> future = CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            try {
                T result = task.get();
                long duration = System.currentTimeMillis() - startTime;
                log.debug("[异步编排] 任务执行成功, duration={}ms", duration);
                return TaskResult.success(result, duration);
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                log.error("[异步编排] 任务执行失败, duration={}ms", duration, e);
                return TaskResult.failed(e.getMessage(), duration);
            }
        }, executor);

        // 超时控制
        CompletableFuture<TaskResult<T>> timeoutFuture = new CompletableFuture<>();
        future.orTimeout(timeout, TimeUnit.MILLISECONDS)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.warn("[异步编排] 任务执行超时, timeout={}ms", timeout);
                        timeoutFuture.complete(TaskResult.failed("任务执行超时", timeout));
                    } else {
                        timeoutFuture.complete(result);
                    }
                });

        return timeoutFuture;
    }

    /**
     * 执行任务（带降级处理）
     * <p>
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
     * 主任务失败时自动执行降级任务
     * </p>
     *
     * @param mainTask 主任务
     * @param fallbackTask 降级任务
     * @param <T> 任务返回类型
     * @return 任务执行结果
     */
    public <T> CompletableFuture<TaskResult<T>> executeWithFallback(
            Supplier<T> mainTask, Supplier<T> fallbackTask) {

        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            try {
                T result = mainTask.get();
                long duration = System.currentTimeMillis() - startTime;
                log.debug("[异步编排] 主任务执行成功, duration={}ms", duration);
                return TaskResult.success(result, duration);
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                log.warn("[异步编排] 主任务执行失败，执行降级任务, duration={}ms, error={}", duration, e.getMessage());

                // 执行降级任务
                try {
                    long fallbackStartTime = System.currentTimeMillis();
                    T fallbackResult = fallbackTask.get();
                    long fallbackDuration = System.currentTimeMillis() - fallbackStartTime;
                    log.info("[异步编排] 降级任务执行成功, duration={}ms", fallbackDuration);
                    return TaskResult.success(fallbackResult, duration + fallbackDuration);
                } catch (Exception fallbackException) {
                    long totalDuration = System.currentTimeMillis() - startTime;
                    log.error("[异步编排] 降级任务执行失败, duration={}ms", totalDuration, fallbackException);
                    return TaskResult.failed("主任务和降级任务均失败: " + fallbackException.getMessage(), totalDuration);
                }
            }
        }, executor);
    }

    /**
     * 任务执行结果
     */
    public static class TaskResult<T> {
        private final boolean success;
        private final T data;
        private final String errorMessage;
        private final long duration;

        private TaskResult(boolean success, T data, String errorMessage, long duration) {
            this.success = success;
            this.data = data;
            this.errorMessage = errorMessage;
            this.duration = duration;
        }

        public static <T> TaskResult<T> success(T data, long duration) {
            return new TaskResult<>(true, data, null, duration);
        }

        public static <T> TaskResult<T> failed(String errorMessage, long duration) {
            return new TaskResult<>(false, null, errorMessage, duration);
        }

        public boolean isSuccess() {
            return success;
        }

        public T getData() {
            return data;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public long getDuration() {
            return duration;
        }
    }
}
