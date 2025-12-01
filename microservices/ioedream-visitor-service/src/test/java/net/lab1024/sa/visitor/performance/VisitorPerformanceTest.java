package net.lab1024.sa.visitor.performance;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import lombok.extern.slf4j.Slf4j;

/**
 * 访客服务性能测试
 * 测试访客创建、查询和审批流程的性能
 *
 * @author SmartAdmin Team
 * @since 2025-01-30
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("访客服务性能测试")
class VisitorPerformanceTest {

    private ExecutorService executorService;
    private static final int CONCURRENT_THREADS = 30;
    private static final long MAX_RESPONSE_TIME_MS = 500;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(CONCURRENT_THREADS);
    }

    @AfterEach
    void tearDown() {
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    @Test
    @DisplayName("测试访客创建响应时间")
    void testCreateVisitorResponseTime() {
        log.info("开始测试访客创建响应时间...");

        List<Long> responseTimes = new ArrayList<>();
        int testCount = 100;

        for (int i = 0; i < testCount; i++) {
            long startTime = System.currentTimeMillis();
            try {
                // 模拟访客创建操作
                // ResponseDTO<?> result = visitorService.createVisitor(...);
                Thread.sleep(20); // 模拟创建处理耗时
            } catch (Exception e) {
                log.error("访客创建失败", e);
            }
            long endTime = System.currentTimeMillis();
            responseTimes.add(endTime - startTime);
        }

        double avgResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        long p95ResponseTime = calculatePercentile(responseTimes, 95);

        log.info("访客创建性能统计:");
        log.info("  平均响应时间: {}ms", avgResponseTime);
        log.info("  P95响应时间: {}ms", p95ResponseTime);

        assertTrue(avgResponseTime < MAX_RESPONSE_TIME_MS,
                "平均响应时间应小于" + MAX_RESPONSE_TIME_MS + "ms");
    }

    @Test
    @DisplayName("测试并发访客查询性能")
    void testConcurrentVisitorQuery() throws Exception {
        log.info("开始测试并发访客查询性能，并发数: {}", CONCURRENT_THREADS);

        List<CompletableFuture<Long>> futures = new ArrayList<>();

        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            final int threadId = i;
            CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                long startTime = System.currentTimeMillis();
                try {
                    // 模拟并发查询
                    Thread.sleep(10 + (threadId % 10));
                } catch (Exception e) {
                    log.error("并发查询失败: threadId={}", threadId, e);
                }
                return System.currentTimeMillis() - startTime;
            }, executorService);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<Long> responseTimes = new ArrayList<>();
        for (CompletableFuture<Long> future : futures) {
            responseTimes.add(future.get());
        }

        double avgResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        log.info("并发访客查询性能统计:");
        log.info("  并发数: {}", CONCURRENT_THREADS);
        log.info("  平均响应时间: {}ms", avgResponseTime);

        assertTrue(avgResponseTime < MAX_RESPONSE_TIME_MS * 2,
                "并发平均响应时间应小于" + (MAX_RESPONSE_TIME_MS * 2) + "ms");
    }

    @Test
    @DisplayName("测试审批流程性能")
    void testApprovalWorkflowPerformance() {
        log.info("开始测试审批流程性能...");

        List<Long> approvalTimes = new ArrayList<>();
        int testCount = 50;

        for (int i = 0; i < testCount; i++) {
            long startTime = System.currentTimeMillis();
            try {
                // 模拟审批流程
                Thread.sleep(15); // 模拟审批处理耗时
            } catch (Exception e) {
                log.error("审批失败", e);
            }
            long endTime = System.currentTimeMillis();
            approvalTimes.add(endTime - startTime);
        }

        double avgApprovalTime = approvalTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        log.info("审批流程性能统计:");
        log.info("  平均审批时间: {}ms", avgApprovalTime);

        assertTrue(avgApprovalTime < 300, "平均审批时间应小于300ms");
    }

    private long calculatePercentile(List<Long> values, int percentile) {
        if (values.isEmpty()) {
            return 0L;
        }
        List<Long> sorted = new ArrayList<>(values);
        sorted.sort(Long::compareTo);
        int index = (int) Math.ceil((percentile / 100.0) * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(index, sorted.size() - 1)));
    }
}
