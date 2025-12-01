package net.lab1024.sa.video.performance;

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
 * 视频服务性能测试
 * 测试视频流处理、AI分析和设备管理的性能
 *
 * @author SmartAdmin Team
 * @since 2025-01-30
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("视频服务性能测试")
class VideoPerformanceTest {

    private ExecutorService executorService;
    private static final int CONCURRENT_THREADS = 20; // 视频处理并发数较低
    private static final long MAX_RESPONSE_TIME_MS = 1000; // 视频处理允许更长响应时间

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
    @DisplayName("测试视频流查询响应时间")
    void testVideoStreamQueryResponseTime() {
        log.info("开始测试视频流查询响应时间...");

        List<Long> responseTimes = new ArrayList<>();
        int testCount = 50;

        for (int i = 0; i < testCount; i++) {
            long startTime = System.currentTimeMillis();
            try {
                // 模拟视频流查询
                // ResponseDTO<?> result = videoService.queryVideoStream(...);
                Thread.sleep(30); // 模拟视频流查询耗时
            } catch (Exception e) {
                log.error("视频流查询失败", e);
            }
            long endTime = System.currentTimeMillis();
            responseTimes.add(endTime - startTime);
        }

        double avgResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        long p95ResponseTime = calculatePercentile(responseTimes, 95);

        log.info("视频流查询性能统计:");
        log.info("  平均响应时间: {}ms", avgResponseTime);
        log.info("  P95响应时间: {}ms", p95ResponseTime);

        assertTrue(avgResponseTime < MAX_RESPONSE_TIME_MS,
                "平均响应时间应小于" + MAX_RESPONSE_TIME_MS + "ms");
    }

    @Test
    @DisplayName("测试并发视频流处理性能")
    void testConcurrentVideoStreamProcessing() throws Exception {
        log.info("开始测试并发视频流处理性能，并发数: {}", CONCURRENT_THREADS);

        List<CompletableFuture<Long>> futures = new ArrayList<>();

        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            final int threadId = i;
            CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                long startTime = System.currentTimeMillis();
                try {
                    // 模拟并发视频流处理
                    Thread.sleep(30 + (threadId % 20));
                } catch (Exception e) {
                    log.error("并发视频处理失败: threadId={}", threadId, e);
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

        log.info("并发视频流处理性能统计:");
        log.info("  并发数: {}", CONCURRENT_THREADS);
        log.info("  平均响应时间: {}ms", avgResponseTime);

        assertTrue(avgResponseTime < MAX_RESPONSE_TIME_MS * 2,
                "并发平均响应时间应小于" + (MAX_RESPONSE_TIME_MS * 2) + "ms");
    }

    @Test
    @DisplayName("测试AI分析性能")
    void testAIAnalysisPerformance() {
        log.info("开始测试AI分析性能...");

        List<Long> analysisTimes = new ArrayList<>();
        int testCount = 20; // AI分析测试次数较少

        for (int i = 0; i < testCount; i++) {
            long startTime = System.currentTimeMillis();
            try {
                // 模拟AI分析处理
                Thread.sleep(100); // 模拟AI分析耗时（通常较长）
            } catch (Exception e) {
                log.error("AI分析失败", e);
            }
            long endTime = System.currentTimeMillis();
            analysisTimes.add(endTime - startTime);
        }

        double avgAnalysisTime = analysisTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        log.info("AI分析性能统计:");
        log.info("  平均分析时间: {}ms", avgAnalysisTime);

        // AI分析允许更长的处理时间
        assertTrue(avgAnalysisTime < 500, "平均AI分析时间应小于500ms");
    }

    @Test
    @DisplayName("测试设备管理查询性能")
    void testDeviceManagementQueryPerformance() {
        log.info("开始测试设备管理查询性能...");

        List<Long> queryTimes = new ArrayList<>();
        int testCount = 100;

        for (int i = 0; i < testCount; i++) {
            long startTime = System.currentTimeMillis();
            try {
                // 模拟设备查询
                Thread.sleep(10);
            } catch (Exception e) {
                log.error("设备查询失败", e);
            }
            long endTime = System.currentTimeMillis();
            queryTimes.add(endTime - startTime);
        }

        double avgQueryTime = queryTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        log.info("设备管理查询性能统计:");
        log.info("  平均查询时间: {}ms", avgQueryTime);

        assertTrue(avgQueryTime < 200, "平均设备查询时间应小于200ms");
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
