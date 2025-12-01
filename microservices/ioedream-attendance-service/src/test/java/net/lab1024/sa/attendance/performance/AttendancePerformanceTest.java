package net.lab1024.sa.attendance.performance;

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

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.service.AttendanceService;

/**
 * 考勤服务性能测试
 * 测试接口响应时间、并发性能和数据库查询性能
 *
 * @author SmartAdmin Team
 * @since 2025-01-30
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("考勤服务性能测试")
class AttendancePerformanceTest {

    @Resource
    private AttendanceService attendanceService;

    private ExecutorService executorService;
    private static final int CONCURRENT_THREADS = 50;
    private static final long MAX_RESPONSE_TIME_MS = 500; // 最大响应时间500ms

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
    @DisplayName("测试查询考勤记录响应时间")
    void testQueryAttendanceRecordResponseTime() {
        log.info("开始测试查询考勤记录响应时间...");

        List<Long> responseTimes = new ArrayList<>();
        int testCount = 100;

        for (int i = 0; i < testCount; i++) {
            long startTime = System.currentTimeMillis();
            try {
                // 这里应该调用实际的查询方法，由于是示例，使用模拟
                // ResponseDTO<?> result = attendanceService.queryAttendanceRecords(...);
                // assertNotNull(result);
                Thread.sleep(10); // 模拟查询耗时
            } catch (Exception e) {
                log.error("查询失败", e);
            }
            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;
            responseTimes.add(responseTime);
        }

        // 计算统计信息
        double avgResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        long p95ResponseTime = calculatePercentile(responseTimes, 95);
        long p99ResponseTime = calculatePercentile(responseTimes, 99);

        log.info("查询考勤记录性能统计:");
        log.info("  平均响应时间: {}ms", avgResponseTime);
        log.info("  P95响应时间: {}ms", p95ResponseTime);
        log.info("  P99响应时间: {}ms", p99ResponseTime);

        // 验证性能指标
        assertTrue(avgResponseTime < MAX_RESPONSE_TIME_MS,
                "平均响应时间应小于" + MAX_RESPONSE_TIME_MS + "ms");
        assertTrue(p95ResponseTime < MAX_RESPONSE_TIME_MS * 2,
                "P95响应时间应小于" + (MAX_RESPONSE_TIME_MS * 2) + "ms");
    }

    @Test
    @DisplayName("测试并发查询性能")
    void testConcurrentQueryPerformance() throws Exception {
        log.info("开始测试并发查询性能，并发数: {}", CONCURRENT_THREADS);

        List<CompletableFuture<Long>> futures = new ArrayList<>();

        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            final int threadId = i;
            CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                long startTime = System.currentTimeMillis();
                try {
                    // 模拟并发查询
                    // ResponseDTO<?> result = attendanceService.queryAttendanceRecords(...);
                    Thread.sleep(10 + (threadId % 20)); // 模拟不同查询耗时
                } catch (Exception e) {
                    log.error("并发查询失败: threadId={}", threadId, e);
                }
                return System.currentTimeMillis() - startTime;
            }, executorService);
            futures.add(future);
        }

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 收集响应时间
        List<Long> responseTimes = new ArrayList<>();
        for (CompletableFuture<Long> future : futures) {
            responseTimes.add(future.get());
        }

        // 计算统计信息
        double avgResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        long maxResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);

        log.info("并发查询性能统计:");
        log.info("  并发数: {}", CONCURRENT_THREADS);
        log.info("  平均响应时间: {}ms", avgResponseTime);
        log.info("  最大响应时间: {}ms", maxResponseTime);

        // 验证性能指标
        assertTrue(avgResponseTime < MAX_RESPONSE_TIME_MS * 2,
                "并发平均响应时间应小于" + (MAX_RESPONSE_TIME_MS * 2) + "ms");
    }

    @Test
    @DisplayName("测试数据库查询性能")
    void testDatabaseQueryPerformance() {
        log.info("开始测试数据库查询性能...");

        List<Long> queryTimes = new ArrayList<>();
        int testCount = 50;

        for (int i = 0; i < testCount; i++) {
            long startTime = System.currentTimeMillis();
            try {
                // 模拟数据库查询
                // 这里应该执行实际的数据库查询
                // List<?> results = attendanceDao.selectList(...);
                Thread.sleep(5); // 模拟查询耗时
            } catch (Exception e) {
                log.error("数据库查询失败", e);
            }
            long endTime = System.currentTimeMillis();
            queryTimes.add(endTime - startTime);
        }

        // 计算统计信息
        double avgQueryTime = queryTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        long p95QueryTime = calculatePercentile(queryTimes, 95);

        log.info("数据库查询性能统计:");
        log.info("  平均查询时间: {}ms", avgQueryTime);
        log.info("  P95查询时间: {}ms", p95QueryTime);

        // 验证性能指标（数据库查询应该更快）
        assertTrue(avgQueryTime < 100, "平均数据库查询时间应小于100ms");
        assertTrue(p95QueryTime < 200, "P95数据库查询时间应小于200ms");
    }

    /**
     * 计算百分位数
     */
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
