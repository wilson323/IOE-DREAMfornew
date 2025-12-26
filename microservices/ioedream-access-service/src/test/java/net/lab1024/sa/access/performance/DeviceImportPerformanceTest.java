package net.lab1024.sa.access.performance;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.service.DeviceImportService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 设备导入性能测试类
 * <p>
 * 测试批量导入的性能指标：
 * - 数据验证性能（1000条记录）
 * - 并发导入性能（10个并发）
 * - 内存占用
 * - 响应时间
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@SpringBootTest
@Disabled("性能测试默认禁用，需要时手动启用")
@DisplayName("设备导入性能测试")
public class DeviceImportPerformanceTest {

    @Autowired
    private DeviceImportService deviceImportService;

    /**
     * 测试数据验证性能 - 单线程
     * 目标：1000条记录 < 1秒
     */
    @Test
    @DisplayName("性能测试 - 数据验证性能（1000条记录）")
    public void testDataValidationPerformance() {
        int recordCount = 1000;
        List<JSONObject> testData = generateTestData(recordCount);

        long startTime = System.currentTimeMillis();

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        for (int i = 0; i < testData.size(); i++) {
            JSONObject rowData = testData.get(i);
            String errorMessage = deviceImportService.validateDeviceData(rowData, i + 2);

            if (errorMessage == null) {
                successCount.incrementAndGet();
            } else {
                errorCount.incrementAndGet();
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        log.info("[性能测试] 数据验证完成: recordCount={}, success={}, error={}, duration={}ms",
                recordCount, successCount.get(), errorCount.get(), duration);

        // 性能断言
        assertTrue(duration < 1000, "1000条记录验证应在1秒内完成，实际耗时: " + duration + "ms");
        assertTrue(successCount.get() > 0, "应该有验证成功的记录");
    }

    /**
     * 测试数据验证性能 - 大数据量
     * 目标：10000条记录 < 10秒
     */
    @Test
    @DisplayName("性能测试 - 数据验证性能（10000条记录）")
    public void testLargeDataValidationPerformance() {
        int recordCount = 10000;
        List<JSONObject> testData = generateTestData(recordCount);

        long startTime = System.currentTimeMillis();

        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < testData.size(); i++) {
            JSONObject rowData = testData.get(i);
            String errorMessage = deviceImportService.validateDeviceData(rowData, i + 2);

            if (errorMessage == null) {
                successCount.incrementAndGet();
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        log.info("[性能测试] 大数据量验证完成: recordCount={}, success={}, duration={}ms, avg={}ms/record",
                recordCount, successCount.get(), duration, (double) duration / recordCount);

        // 性能断言
        assertTrue(duration < 10000, "10000条记录验证应在10秒内完成，实际耗时: " + duration + "ms");
    }

    /**
     * 测试并发验证性能
     * 目标：10个线程，每个线程100条，总耗时 < 500ms
     */
    @Test
    @DisplayName("性能测试 - 并发数据验证性能")
    public void testConcurrentValidationPerformance() throws InterruptedException {
        int threadCount = 10;
        int recordsPerThread = 100;
        int totalRecords = threadCount * recordsPerThread;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger totalSuccess = new AtomicInteger(0);
        AtomicLong totalDuration = new AtomicLong(0);

        long startTime = System.currentTimeMillis();

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executorService.submit(() -> {
                try {
                    List<JSONObject> testData = generateTestData(recordsPerThread);
                    long threadStart = System.currentTimeMillis();

                    for (int i = 0; i < testData.size(); i++) {
                        JSONObject rowData = testData.get(i);
                        String errorMessage = deviceImportService.validateDeviceData(rowData, i + 2);

                        if (errorMessage == null) {
                            totalSuccess.incrementAndGet();
                        }
                    }

                    long threadEnd = System.currentTimeMillis();
                    totalDuration.addAndGet(threadEnd - threadStart);

                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        log.info("[性能测试] 并发验证完成: threads={}, totalRecords={}, success={}, duration={}ms, avgThreadDuration={}ms",
                threadCount, totalRecords, totalSuccess.get(), duration, totalDuration.get() / threadCount);

        // 性能断言
        assertTrue(duration < 500, "并发验证应在500ms内完成，实际耗时: " + duration + "ms");
    }

    /**
     * 测试内存占用
     * 目标：1000条记录内存占用 < 50MB
     */
    @Test
    @DisplayName("性能测试 - 内存占用测试")
    public void testMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();

        // 强制GC
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

        // 生成并验证1000条记录
        int recordCount = 1000;
        List<JSONObject> testData = generateTestData(recordCount);

        for (int i = 0; i < testData.size(); i++) {
            JSONObject rowData = testData.get(i);
            deviceImportService.validateDeviceData(rowData, i + 2);
        }

        // 强制GC
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = memoryAfter - memoryBefore;
        double memoryUsedMB = memoryUsed / (1024.0 * 1024.0);

        log.info("[性能测试] 内存占用: recordCount={}, memoryBefore={}MB, memoryAfter={}MB, memoryUsed={}MB",
                recordCount,
                String.format("%.2f", memoryBefore / (1024.0 * 1024.0)),
                String.format("%.2f", memoryAfter / (1024.0 * 1024.0)),
                String.format("%.2f", memoryUsedMB));

        // 性能断言
        assertTrue(memoryUsedMB < 50, "内存占用应小于50MB，实际占用: " + String.format("%.2f", memoryUsedMB) + "MB");
    }

    /**
     * 测试JSON解析性能
     * 目标：1000个JSON对象解析 < 100ms
     */
    @Test
    @DisplayName("性能测试 - JSON解析性能")
    public void testJsonParsingPerformance() {
        int recordCount = 1000;
        List<String> jsonStrings = new ArrayList<>();

        // 生成JSON字符串
        for (int i = 0; i < recordCount; i++) {
            JSONObject rowData = createValidRowData(i);
            jsonStrings.add(rowData.toJSONString());
        }

        // 测试JSON解析性能
        long startTime = System.currentTimeMillis();

        for (String jsonString : jsonStrings) {
            JSONObject obj = JSON.parseObject(jsonString);
            assertNotNull(obj, "解析后的JSON对象不应为null");
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        log.info("[性能测试] JSON解析完成: recordCount={}, duration={}ms, avg={}ms/record",
                recordCount, duration, (double) duration / recordCount);

        // 性能断言
        assertTrue(duration < 100, "1000个JSON解析应在100ms内完成，实际耗时: " + duration + "ms");
    }

    // ==================== 辅助方法 ====================

    /**
     * 生成测试数据
     */
    private List<JSONObject> generateTestData(int count) {
        List<JSONObject> dataList = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            dataList.add(createValidRowData(i));
        }

        return dataList;
    }

    /**
     * 创建有效的行数据
     */
    private JSONObject createValidRowData(int index) {
        JSONObject rowData = new JSONObject();
        rowData.put("deviceCode", "DEV" + String.format("%06d", index));
        rowData.put("deviceName", "测试设备" + index);
        rowData.put("deviceType", (index % 5) + 1);
        rowData.put("deviceModel", "MODEL-" + (index % 10));
        rowData.put("manufacturer", "厂商" + (index % 5));
        rowData.put("areaId", (index % 20) + 1);
        rowData.put("ipAddress", "192.168." + (index % 255) + "." + (index % 254 + 1));
        rowData.put("port", 8000 + (index % 1000));
        rowData.put("remark", "测试备注" + index);
        return rowData;
    }

    /**
     * 断言辅助方法
     */
    private void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new AssertionError(message);
        }
    }
}
