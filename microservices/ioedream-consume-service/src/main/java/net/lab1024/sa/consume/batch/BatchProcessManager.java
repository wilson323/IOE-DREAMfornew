package net.lab1024.sa.consume.batch;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 批量处理管理器
 *
 * 职责：支持高吞吐量的批量操作
 *
 * 使用场景：
 * 1. 批量消费记录查询
 * 2. 批量账户余额更新
 * 3. 批量补贴发放
 * 4. 批量数据导入导出
 *
 * 特性：
 * - 并发处理（线程池）
 * - 批量提交（减少数据库交互）
 * - 异步执行（非阻塞）
 * - 结果聚合
 *
 * 性能目标：
 * - 批量大小：100-1000条/批
 * - 并发线程：CPU核心数 * 2
 * - 吞吐量：≥10000 TPS
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
@Slf4j
public class BatchProcessManager<T, R> {

    /**
     * 默认批量大小
     */
    private static final int DEFAULT_BATCH_SIZE = 100;

    /**
     * 默认并发线程数
     */
    private static final int DEFAULT_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;

    /**
     * 线程池
     */
    private final ExecutorService executorService;

    /**
     * 批量大小
     */
    private final int batchSize;

    /**
     * 批量超时时间（秒）
     */
    private final long timeoutSeconds;

    /**
     * 构造函数（使用默认配置）
     */
    public BatchProcessManager() {
        this(DEFAULT_BATCH_SIZE, DEFAULT_THREAD_POOL_SIZE, 30);
    }

    /**
     * 构造函数
     *
     * @param batchSize 批量大小
     * @param threadPoolSize 线程池大小
     * @param timeoutSeconds 超时时间（秒）
     */
    public BatchProcessManager(int batchSize, int threadPoolSize, long timeoutSeconds) {
        this.batchSize = batchSize;
        this.timeoutSeconds = timeoutSeconds;

        // 创建线程池
        this.executorService = new ThreadPoolExecutor(
                threadPoolSize,
                threadPoolSize * 2,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        log.info("[批量处理] 初始化完成: batchSize={}, threadPoolSize={}, timeoutSeconds={}",
                batchSize, threadPoolSize, timeoutSeconds);
    }

    /**
     * 批量处理（同步）
     *
     * @param items 数据列表
     * @param processor 处理器（单条处理）
     * @return 处理结果列表
     */
    public List<R> processBatch(List<T> items, Function<T, R> processor) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }

        log.info("[批量处理] 开始处理: totalCount={}, batchSize={}", items.size(), batchSize);

        long startTime = System.currentTimeMillis();

        try {
            // 分批处理
            List<List<T>> batches = splitList(items, batchSize);
            List<Future<List<R>>> futures = new ArrayList<>();

            // 提交所有批次
            for (List<T> batch : batches) {
                Future<List<R>> future = executorService.submit(() -> processSingleBatch(batch, processor));
                futures.add(future);
            }

            // 等待所有批次完成
            List<R> results = new ArrayList<>();
            for (Future<List<R>> future : futures) {
                List<R> batchResults = future.get(timeoutSeconds, TimeUnit.SECONDS);
                results.addAll(batchResults);
            }

            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info("[批量处理] 处理完成: totalCount={}, successCount={}, elapsedTime={}ms, tps={}",
                    items.size(), results.size(), elapsedTime,
                    (items.size() * 1000L) / elapsedTime);

            return results;

        } catch (TimeoutException e) {
            log.error("[批量处理] 处理超时: totalCount={}, timeoutSeconds={}",
                    items.size(), timeoutSeconds);
            throw new RuntimeException("批量处理超时", e);
        } catch (Exception e) {
            log.error("[批量处理] 处理异常: totalCount={}, error={}",
                    items.size(), e.getMessage(), e);
            throw new RuntimeException("批量处理异常", e);
        }
    }

    /**
     * 批量处理（异步）
     *
     * @param items 数据列表
     * @param processor 处理器（单条处理）
     * @param callback 回调函数
     */
    public void processBatchAsync(List<T> items, Function<T, R> processor, Consumer<List<R>> callback) {
        CompletableFuture.runAsync(() -> {
            try {
                List<R> results = processBatch(items, processor);
                callback.accept(results);
            } catch (Exception e) {
                log.error("[批量处理] 异步处理异常: error={}", e.getMessage(), e);
                callback.accept(new ArrayList<>());
            }
        }, executorService);
    }

    /**
     * 批量处理（无返回值）
     *
     * @param items 数据列表
     * @param processor 处理器（单条处理）
     * @return 处理成功的数量
     */
    public int processBatch(List<T> items, Consumer<T> processor) {
        if (items == null || items.isEmpty()) {
            return 0;
        }

        log.info("[批量处理] 开始处理（无返回值）: totalCount={}, batchSize={}",
                items.size(), batchSize);

        long startTime = System.currentTimeMillis();

        try {
            // 分批处理
            List<List<T>> batches = splitList(items, batchSize);
            List<Future<Integer>> futures = new ArrayList<>();

            // 提交所有批次
            for (List<T> batch : batches) {
                Future<Integer> future = executorService.submit(() -> processSingleBatch(batch, processor));
                futures.add(future);
            }

            // 等待所有批次完成
            int successCount = 0;
            for (Future<Integer> future : futures) {
                successCount += future.get(timeoutSeconds, TimeUnit.SECONDS);
            }

            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info("[批量处理] 处理完成: totalCount={}, successCount={}, elapsedTime={}ms, tps={}",
                    items.size(), successCount, elapsedTime,
                    (items.size() * 1000L) / elapsedTime);

            return successCount;

        } catch (TimeoutException e) {
            log.error("[批量处理] 处理超时: totalCount={}, timeoutSeconds={}",
                    items.size(), timeoutSeconds);
            throw new RuntimeException("批量处理超时", e);
        } catch (Exception e) {
            log.error("[批量处理] 处理异常: totalCount={}, error={}",
                    items.size(), e.getMessage(), e);
            throw new RuntimeException("批量处理异常", e);
        }
    }

    /**
     * 处理单个批次（有返回值）
     */
    private List<R> processSingleBatch(List<T> batch, Function<T, R> processor) {
        List<R> results = new ArrayList<>(batch.size());

        for (T item : batch) {
            try {
                R result = processor.apply(item);
                if (result != null) {
                    results.add(result);
                }
            } catch (Exception e) {
                log.error("[批量处理] 单条处理异常: item={}, error={}", item, e.getMessage());
                // 继续处理下一条，不中断整个批次
            }
        }

        return results;
    }

    /**
     * 处理单个批次（无返回值）
     */
    private int processSingleBatch(List<T> batch, Consumer<T> processor) {
        int successCount = 0;

        for (T item : batch) {
            try {
                processor.accept(item);
                successCount++;
            } catch (Exception e) {
                log.error("[批量处理] 单条处理异常: item={}, error={}", item, e.getMessage());
                // 继续处理下一条，不中断整个批次
            }
        }

        return successCount;
    }

    /**
     * 分割列表
     */
    private List<List<T>> splitList(List<T> list, int batchSize) {
        List<List<T>> batches = new ArrayList<>();

        for (int i = 0; i < list.size(); i += batchSize) {
            int end = Math.min(i + batchSize, list.size());
            batches.add(list.subList(i, end));
        }

        return batches;
    }

    /**
     * 关闭批量处理器
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        log.info("[批量处理] 已关闭");
    }

    /**
     * 获取批量大小
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * 获取线程池活跃线程数
     */
    public int getActiveThreadCount() {
        if (executorService instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) executorService).getActiveCount();
        }
        return 0;
    }
}
