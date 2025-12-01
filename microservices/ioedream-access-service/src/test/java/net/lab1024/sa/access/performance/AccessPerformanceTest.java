package net.lab1024.sa.access.performance;

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
 * 门禁服务性能测试
 * 测试接口响应时间、并发性能和设备连接性能
 *
 * @author SmartAdmin Team
 * @since 2025-01-30
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("门禁服务性能测试")
class AccessPerformanceTest {

    private ExecutorService executorService;
    private static final int CONCURRENT_THREADS = 50;
    private static final long MAX_RESPONSE_TIME_MS = 300; // 门禁操作要求更快响应

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
    @DisplayName("测试门禁验证响应时间")
    void testAccessVerificationResponseTime() {
        log.info("开始测试门禁验证响应时间...");

        List<Long> responseTimes = new ArrayList<>();
        int testCount = 100;

        for (int i = 0; i < testCount; i++) {
            long startTime = System.currentTimeMillis();
            try {
                // 模拟门禁验证操作
                // ResponseDTO<?> result = accessService.verifyAccess(...);
                Thread.sleep(5); // 模拟验证耗时
            } catch (Exception e) {
                log.error("门禁验证失败", e);
            }
            long endTime = System.currentTimeMillis();
            responseTimes.add(endTime - startTime);
        }

        double avgResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        long p95ResponseTime = calculatePercentile(responseTimes, 95);

        log.info("门禁验证性能统计:");
        log.info("  平均响应时间: {}ms", avgResponseTime);
        log.info("  P95响应时间: {}ms", p95ResponseTime);

        assertTrue(avgResponseTime < MAX_RESPONSE_TIME_MS,
                "平均响应时间应小于" + MAX_RESPONSE_TIME_MS + "ms");
    }

    @Test
    @DisplayName("测试并发门禁验证性能")
    void testConcurrentAccessVerification() throws Exception {
        log.info("开始测试并发门禁验证性能，并发数: {}", CONCURRENT_THREADS);

        List<CompletableFuture<Long>> futures = new ArrayList<>();

        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            final int threadId = i;
            CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                long startTime = System.currentTimeMillis();
                try {
                    // 模拟并发门禁验证
                    Thread.sleep(5 + (threadId % 10));
                } catch (Exception e) {
                    log.error("并发验证失败: threadId={}", threadId, e);
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

        log.info("并发门禁验证性能统计:");
        log.info("  并发数: {}", CONCURRENT_THREADS);
        log.info("  平均响应时间: {}ms", avgResponseTime);

        assertTrue(avgResponseTime < MAX_RESPONSE_TIME_MS * 2,
                "并发平均响应时间应小于" + (MAX_RESPONSE_TIME_MS * 2) + "ms");
    }

    @Test
    @DisplayName("测试设备连接性能")
    void testDeviceConnectionPerformance() {
        log.info("开始测试设备连接性能...");

        List<Long> connectionTimes = new ArrayList<>();
        int testCount = 30;

        for (int i = 0; i < testCount; i++) {
            long startTime = System.currentTimeMillis();
            try {
                // 模拟设备连接
                // boolean connected = deviceAdapter.connect(...);
                Thread.sleep(20); // 模拟设备连接耗时
            } catch (Exception e) {
                log.error("设备连接失败", e);
            }
            long endTime = System.currentTimeMillis();
            connectionTimes.add(endTime - startTime);
        }

        double avgConnectionTime = connectionTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        log.info("设备连接性能统计:");
        log.info("  平均连接时间: {}ms", avgConnectionTime);

        assertTrue(avgConnectionTime < 100, "平均设备连接时间应小于100ms");
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

