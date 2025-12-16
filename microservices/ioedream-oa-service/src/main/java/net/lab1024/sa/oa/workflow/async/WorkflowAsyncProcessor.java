package net.lab1024.sa.oa.workflow.async;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 工作流异步处理器
 * <p>
 * 提供高性能异步处理能力，支持任务队列、批处理、失败重试
 * 集成线程池管理和性能监控
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@Component
public class WorkflowAsyncProcessor {

    private final ConcurrentHashMap<String, AsyncTask<?>> runningTasks = new ConcurrentHashMap<>();
    private final AsyncPerformanceMetrics performanceMetrics = new AsyncPerformanceMetrics();

    /**
     * 异步执行任务
     */
    @Async("workflowEventExecutor")
    public <T> CompletableFuture<T> executeAsync(Supplier<T> task, String taskName) {
        long startTime = System.nanoTime();
        String taskId = generateTaskId(taskName);

        log.debug("[异步处理] 开始执行任务: taskId={}, taskName={}", taskId, taskName);

        AsyncTask<T> asyncTask = new AsyncTask<>(taskId, taskName, startTime, task);
        runningTasks.put(taskId, asyncTask);

        return CompletableFuture.supplyAsync(() -> {
            try {
                T result = task.get();
                long duration = System.nanoTime() - startTime;
                performanceMetrics.recordTaskCompletion(taskName, duration, true);
                log.debug("[异步处理] 任务执行成功: taskId={}, duration={}ms", taskId, duration / 1_000_000);
                return result;
            } catch (Exception e) {
                long duration = System.nanoTime() - startTime;
                performanceMetrics.recordTaskCompletion(taskName, duration, false);
                log.error("[异步处理] 任务执行失败: taskId={}, duration={}ms", taskId, duration / 1_000_000, e);
                throw new RuntimeException(e);
            } finally {
                runningTasks.remove(taskId);
            }
        });
    }

    /**
     * 异步执行任务（带回调）
     */
    @Async("workflowEventExecutor")
    public <T> CompletableFuture<T> executeAsync(Supplier<T> task, String taskName, Consumer<T> onSuccess, Consumer<Exception> onError) {
        return executeAsync(task, taskName)
                .thenApply(result -> {
                    try {
                        onSuccess.accept(result);
                    } catch (Exception e) {
                        log.warn("[异步处理] 成功回调执行失败: taskName={}", taskName, e);
                    }
                    return result;
                })
                .exceptionally(throwable -> {
                    try {
                        onError.accept((Exception) throwable.getCause());
                    } catch (Exception e) {
                        log.warn("[异步处理] 失败回调执行失败: taskName={}", taskName, e);
                    }
                    throw new RuntimeException(throwable.getCause());
                });
    }

    /**
     * 批量异步处理
     */
    @Async("workflowBatchExecutor")
    public <T> CompletableFuture<List<T>> processBatchAsync(List<T> items, Function<T, CompletableFuture<T>> processor, String batchName) {
        long startTime = System.nanoTime();
        String taskId = generateTaskId(batchName);

        log.debug("[异步处理] 开始批量处理: taskId={}, itemCount={}, batchName={}",
                taskId, items.size(), batchName);

        // 将所有任务转换为CompletableFuture
        List<CompletableFuture<T>> futures = items.stream()
                .map(item -> {
                    try {
                        return processor.apply(item);
                    } catch (Exception e) {
                        log.error("[异步处理] 批量任务处理异常: item={}", item, e);
                        return CompletableFuture.failedFuture(e);
                    }
                })
                .collect(java.util.stream.Collectors.toList());

        // 等待所有任务完成
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<T> results = new ArrayList<>();
                    List<Exception> exceptions = new ArrayList<>();

                    for (CompletableFuture<T> future : futures) {
                        try {
                            results.add(future.get());
                        } catch (Exception e) {
                            exceptions.add(e);
                        }
                    }

                    long duration = System.nanoTime() - startTime;
                    boolean success = exceptions.isEmpty();
                    performanceMetrics.recordBatchCompletion(batchName, duration, success, items.size(), exceptions.size());

                    if (success) {
                        log.debug("[异步处理] 批量处理成功: taskId={}, duration={}ms, itemCount={}",
                                taskId, duration / 1_000_000, items.size());
                    } else {
                        log.warn("[异步处理] 批量处理部分失败: taskId={}, duration={}ms, successCount={}, failCount={}",
                                taskId, duration / 1_000_000, results.size(), exceptions.size());
                    }

                    return results;
                });
    }

    /**
     * 带重试的异步执行
     */
    public <T> CompletableFuture<T> executeAsyncWithRetry(Supplier<T> task, String taskName, int maxRetries, long retryDelayMs) {
        return executeAsyncWithRetry(task, taskName, maxRetries, retryDelayMs, 0);
    }

    /**
     * 带重试的异步执行（内部实现）
     */
    private <T> CompletableFuture<T> executeAsyncWithRetry(Supplier<T> task, String taskName, int maxRetries, long retryDelayMs, int currentRetry) {
        return executeAsync(task, taskName + "_retry_" + currentRetry)
                .handle((result, throwable) -> {
                    if (throwable == null) {
                        return CompletableFuture.completedFuture(result);
                    }

                    if (currentRetry < maxRetries) {
                        log.warn("[异步处理] 任务执行失败，准备重试: taskName={}, retry={}/{}", taskName, currentRetry + 1, maxRetries);

                        return CompletableFuture
                                .delayedExecutor(retryDelayMs, java.util.concurrent.TimeUnit.MILLISECONDS)
                                .execute(() -> null)
                                .thenCompose(v -> executeAsyncWithRetry(task, taskName, maxRetries, retryDelayMs, currentRetry + 1));
                    } else {
                        log.error("[异步处理] 任务重试失败: taskName={}, maxRetries={}", taskName, maxRetries);
                        return CompletableFuture.failedFuture(throwable);
                    }
                });
    }

    /**
     * 并行执行多个任务
     */
    @SafeVarargs
    public final <T> CompletableFuture<List<T>> executeAll(Supplier<T>... tasks) {
        List<CompletableFuture<T>> futures = java.util.Arrays.stream(tasks)
                .map(task -> executeAsync(task, "parallel_task"))
                .collect(java.util.stream.Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(java.util.stream.Collectors.toList()));
    }

    /**
     * 获取运行中的任务信息
     */
    public List<AsyncTaskInfo> getRunningTasks() {
        return runningTasks.values().stream()
                .map(AsyncTask::getInfo)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取性能指标
     */
    public AsyncPerformanceMetrics getPerformanceMetrics() {
        return performanceMetrics.copy();
    }

    /**
     * 取消任务
     */
    public boolean cancelTask(String taskId) {
        AsyncTask<?> task = runningTasks.get(taskId);
        if (task != null) {
            boolean cancelled = task.getFuture().cancel(true);
            if (cancelled) {
                runningTasks.remove(taskId);
                log.info("[异步处理] 任务已取消: taskId={}", taskId);
            }
            return cancelled;
        }
        return false;
    }

    // ==================== 私有辅助方法 ====================

    private String generateTaskId(String taskName) {
        return taskName + "-" + System.currentTimeMillis() + "-" + Thread.currentThread().getId();
    }

    // ==================== 内部类定义 ====================

    /**
     * 异步任务包装器
     */
    private static class AsyncTask<T> {
        private final String taskId;
        private final String taskName;
        private final long startTime;
        private final CompletableFuture<T> future;

        public AsyncTask(String taskId, String taskName, long startTime, Supplier<T> supplier) {
            this.taskId = taskId;
            this.taskName = taskName;
            this.startTime = startTime;
            this.future = CompletableFuture.supplyAsync(supplier);
        }

        public String getTaskId() {
            return taskId;
        }

        public String getTaskName() {
            return taskName;
        }

        public long getStartTime() {
            return startTime;
        }

        public CompletableFuture<T> getFuture() {
            return future;
        }

        public AsyncTaskInfo getInfo() {
            return new AsyncTaskInfo(taskId, taskName, startTime, !future.isDone());
        }
    }

    /**
     * 异步任务信息
     */
    public static class AsyncTaskInfo {
        private final String taskId;
        private final String taskName;
        private final long startTime;
        private final boolean running;

        public AsyncTaskInfo(String taskId, String taskName, long startTime, boolean running) {
            this.taskId = taskId;
            this.taskName = taskName;
            this.startTime = startTime;
            this.running = running;
        }

        public String getTaskId() {
            return taskId;
        }

        public String getTaskName() {
            return taskName;
        }

        public long getStartTime() {
            return startTime;
        }

        public boolean isRunning() {
            return running;
        }

        public long getRunningTime() {
            return System.currentTimeMillis() - startTime;
        }
    }

    /**
     * 异步处理性能指标
     */
    public static class AsyncPerformanceMetrics {
        private final java.util.concurrent.atomic.AtomicLong totalTasks = new java.util.concurrent.atomic.AtomicLong(0);
        private final java.util.concurrent.atomic.AtomicLong successfulTasks = new java.util.concurrent.atomic.AtomicLong(0);
        private final java.util.concurrent.atomic.AtomicLong failedTasks = new java.util.concurrent.atomic.AtomicLong(0);
        private final java.util.concurrent.atomic.AtomicLong totalBatches = new java.util.concurrent.atomic.AtomicLong(0);
        private final java.util.concurrent.atomic.AtomicLong successfulBatches = new java.util.concurrent.atomic.AtomicLong(0);
        private final java.util.concurrent.atomic.AtomicLong totalExecutionTime = new java.util.concurrent.atomic.AtomicLong(0);
        private final java.util.concurrent.atomic.AtomicLong totalBatchSize = new java.util.concurrent.atomic.AtomicLong(0);
        private final java.util.concurrent.atomic.AtomicLong totalFailuresInBatch = new java.util.concurrent.atomic.AtomicLong(0);

        private final java.util.concurrent.ConcurrentHashMap<String, TaskMetrics> taskMetricsMap = new java.util.concurrent.ConcurrentHashMap<>();
        private final java.util.concurrent.ConcurrentHashMap<String, BatchMetrics> batchMetricsMap = new java.util.concurrent.ConcurrentHashMap<>();

        public void recordTaskCompletion(String taskName, long durationNanos, boolean success) {
            totalTasks.incrementAndGet();
            if (success) {
                successfulTasks.incrementAndGet();
            } else {
                failedTasks.incrementAndGet();
            }
            totalExecutionTime.addAndGet(durationNanos);

            taskMetricsMap.computeIfAbsent(taskName, k -> new TaskMetrics())
                    .recordCompletion(durationNanos, success);
        }

        public void recordBatchCompletion(String batchName, long durationNanos, boolean success, int batchSize, int failures) {
            totalBatches.incrementAndGet();
            if (success) {
                successfulBatches.incrementAndGet();
            }
            totalBatchSize.addAndGet(batchSize);
            totalFailuresInBatch.addAndGet(failures);

            batchMetricsMap.computeIfAbsent(batchName, k -> new BatchMetrics())
                    .recordCompletion(durationNanos, batchSize, failures);
        }

        public double getSuccessRate() {
            long total = totalTasks.get();
            return total > 0 ? (double) successfulTasks.get() / total : 0.0;
        }

        public double getAverageExecutionTime() {
            long total = totalTasks.get();
            return total > 0 ? (double) totalExecutionTime.get() / total / 1_000_000 : 0.0; // 转换为毫秒
        }

        public double getAverageBatchSize() {
            long total = totalBatches.get();
            return total > 0 ? (double) totalBatchSize.get() / total : 0.0;
        }

        public Map<String, Object> getMetrics() {
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("totalTasks", totalTasks.get());
            metrics.put("successfulTasks", successfulTasks.get());
            metrics.put("failedTasks", failedTasks.get());
            metrics.put("totalBatches", totalBatches.get());
            metrics.put("successfulBatches", successfulBatches.get());
            metrics.put("successRate", getSuccessRate());
            metrics.put("averageExecutionTimeMs", getAverageExecutionTime());
            metrics.put("averageBatchSize", getAverageBatchSize());

            // 任务级别指标
            Map<String, Object> taskMetrics = new HashMap<>();
            taskMetricsMap.forEach((name, tm) -> {
                Map<String, Object> tmData = new HashMap<>();
                tmData.put("completions", tm.getCompletions());
                tmData.put("successes", tm.getSuccesses());
                tmData.put("failures", tm.getFailures());
                tmData.put("successRate", tm.getSuccessRate());
                tmData.put("averageExecutionTimeMs", tm.getAverageExecutionTimeMs());
                taskMetrics.put(name, tmData);
            });
            metrics.put("taskMetrics", taskMetrics);

            // 批处理级别指标
            Map<String, Object> batchMetrics = new HashMap<>();
            batchMetricsMap.forEach((name, bm) -> {
                Map<String, Object> bmData = new HashMap<>();
                bmData.put("completions", bm.getCompletions());
                bmData.put("successes", bm.getSuccesses());
                bmData.put("totalItems", bm.getTotalItems());
                bmData.put("totalFailures", bm.getTotalFailures());
                bmData.put("successRate", bm.getSuccessRate());
                bmData.put("averageExecutionTimeMs", bm.getAverageExecutionTimeMs());
                batchMetrics.put(name, bmData);
            });
            metrics.put("batchMetrics", batchMetrics);

            return metrics;
        }

        public AsyncPerformanceMetrics copy() {
            AsyncPerformanceMetrics copy = new AsyncPerformanceMetrics();
            copy.totalTasks.set(this.totalTasks.get());
            copy.successfulTasks.set(this.successfulTasks.get());
            copy.failedTasks.set(this.failedTasks.get());
            copy.totalBatches.set(this.totalBatches.get());
            copy.successfulBatches.set(this.successfulBatches.get());
            copy.totalExecutionTime.set(this.totalExecutionTime.get());
            copy.totalBatchSize.set(this.totalBatchSize.get());
            copy.totalFailuresInBatch.set(this.totalFailuresInBatch.get());
            return copy;
        }
    }

    /**
     * 任务级别指标
     */
    private static class TaskMetrics {
        private final java.util.concurrent.atomic.AtomicLong completions = new java.util.concurrent.atomic.AtomicLong(0);
        private final java.util.concurrent.atomic.AtomicLong successes = new java.util.concurrent.atomic.AtomicLong(0);
        private final java.util.concurrent.atomic.AtomicLong failures = new java.util.concurrent.atomic.AtomicLong(0);
        private final java.util.concurrent.atomic.AtomicLong totalExecutionTime = new java.util.concurrent.atomic.AtomicLong(0);

        public void recordCompletion(long durationNanos, boolean success) {
            completions.incrementAndGet();
            if (success) {
                successes.incrementAndGet();
            } else {
                failures.incrementAndGet();
            }
            totalExecutionTime.addAndGet(durationNanos);
        }

        public long getCompletions() {
            return completions.get();
        }

        public long getSuccesses() {
            return successes.get();
        }

        public long getFailures() {
            return failures.get();
        }

        public double getSuccessRate() {
            long total = completions.get();
            return total > 0 ? (double) successes.get() / total : 0.0;
        }

        public double getAverageExecutionTimeMs() {
            long total = completions.get();
            return total > 0 ? (double) totalExecutionTime.get() / total / 1_000_000 : 0.0;
        }
    }

    /**
     * 批处理级别指标
     */
    private static class BatchMetrics {
        private final java.util.concurrent.atomic.AtomicLong completions = new java.util.concurrent.atomic.AtomicLong(0);
        private final java.util.concurrent.atomic.AtomicLong successes = new java.util.concurrent.atomic.AtomicLong(0);
        private final java.util.concurrent.atomic.AtomicLong totalItems = new java.util.concurrent.atomic.AtomicLong(0);
        private final java.util.concurrent.atomic.AtomicLong totalFailures = new java.util.concurrent.atomic.AtomicLong(0);
        private final java.util.concurrent.atomic.AtomicLong totalExecutionTime = new java.util.concurrent.atomic.AtomicLong(0);

        public void recordCompletion(long durationNanos, int batchSize, int failures) {
            completions.incrementAndGet();
            if (failures == 0) {
                successes.incrementAndGet();
            }
            totalItems.addAndGet(batchSize);
            totalFailures.addAndGet(failures);
            totalExecutionTime.addAndGet(durationNanos);
        }

        public long getCompletions() {
            return completions.get();
        }

        public long getSuccesses() {
            return successes.get();
        }

        public long getTotalItems() {
            return totalItems.get();
        }

        public long getTotalFailures() {
            return totalFailures.get();
        }

        public double getSuccessRate() {
            long total = completions.get();
            return total > 0 ? (double) successes.get() / total : 0.0;
        }

        public double getAverageExecutionTimeMs() {
            long total = completions.get();
            return total > 0 ? (double) totalExecutionTime.get() / total / 1_000_000 : 0.0;
        }
    }
}