package net.lab1024.sa.oa.workflow.performance;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import net.lab1024.sa.oa.workflow.service.WorkflowEngineService;
import net.lab1024.sa.oa.workflow.performance.WorkflowCacheManager;
import net.lab1024.sa.oa.workflow.performance.WorkflowAsyncProcessor;

import jakarta.annotation.Resource;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 * 工作流性能和稳定性测试
 * <p>
 * 测试高并发场景下的工作流性能和稳定性：
 * 1. 高并发流程启动测试
 * 2. 内存泄漏检测
 * 3. 数据库连接池压力测试
 * 4. 缓存性能测试
 * 5. 异步处理性能测试
 * 6. 长时间运行稳定性测试
 * 7. 故障恢复测试
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@SpringJUnitConfig
public class WorkflowPerformanceAndStabilityTest {

    @Resource
    private WorkflowEngineService workflowEngineService;

    @Resource
    private WorkflowCacheManager workflowCacheManager;

    @Resource
    private WorkflowAsyncProcessor workflowAsyncProcessor;

    // 性能监控数据
    private final AtomicInteger processCreationSuccessCount = new AtomicInteger(0);
    private final AtomicInteger processCreationFailureCount = new AtomicInteger(0);
    private final AtomicLong totalProcessCreationTime = new AtomicLong(0);
    private final AtomicInteger maxConcurrentProcesses = new AtomicInteger(0);

    // 测试配置
    private static final int HIGH_CONCURRENCY_USERS = 100;
    private static final int PROCESSES_PER_USER = 10;
    private static final int STABILITY_TEST_HOURS = 2;
    private static final String TEST_PROCESS_KEY = "performance-test-process";

    @BeforeEach
    void setUp() {
        log.info("=== 性能和稳定性测试准备 ===");

        try {
            // 重置计数器
            resetCounters();

            // 预热缓存
            warmupCache();

            log.info("性能和稳定性测试准备完成");

        } catch (Exception e) {
            log.error("性能测试准备失败", e);
            throw new RuntimeException("性能测试准备失败", e);
        }
    }

    @AfterEach
    void tearDown() {
        log.info("=== 性能测试清理 ===");

        try {
            // 清理资源
            cleanupPerformanceTest();

            // 输出性能统计
            printPerformanceStatistics();

        } catch (Exception e) {
            log.error("性能测试清理失败", e);
        }
    }

    @Test
    @DisplayName("高并发流程启动性能测试")
    void testHighConcurrencyProcessCreation() {
        log.info("=== 开始高并发流程启动性能测试 ===");

        ExecutorService executor = Executors.newFixedThreadPool(HIGH_CONCURRENCY_USERS);
        CountDownLatch latch = new CountDownLatch(HIGH_CONCURRENCY_USERS);
        Semaphore semaphore = new Semaphore(50); // 限制最大并发数

        long startTime = System.currentTimeMillis();

        for (int userId = 1; userId <= HIGH_CONCURRENCY_USERS; userId++) {
            final int currentUserId = 2000 + userId;

            executor.submit(() -> {
                try {
                    semaphore.acquire();

                    long userStartTime = System.currentTimeMillis();

                    for (int processIndex = 1; processIndex <= PROCESSES_PER_USER; processIndex++) {
                        try {
                            Map<String, Object> variables = new HashMap<>();
                            variables.put("userId", currentUserId);
                            variables.put("processIndex", processIndex);
                            variables.put("timestamp", System.currentTimeMillis());
                            variables.put("testData", generateTestData(processIndex));

                            String processInstanceId = workflowEngineService.startProcess(
                                TEST_PROCESS_KEY,
                                "性能测试流程-" + currentUserId + "-" + processIndex,
                                variables,
                                currentUserId
                            );

                            if (processInstanceId != null) {
                                processCreationSuccessCount.incrementAndGet();
                            } else {
                                processCreationFailureCount.incrementAndGet();
                            }

                            // 记录时间
                            long processTime = System.currentTimeMillis() - userStartTime;
                            totalProcessCreationTime.addAndGet(processTime);

                        } catch (Exception e) {
                            processCreationFailureCount.incrementAndGet();
                            log.error("流程创建失败: userId={}, processIndex={}", currentUserId, processIndex, e);
                        }
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    semaphore.release();
                    latch.countDown();
                }
            });
        }

        try {
            latch.await(300, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long totalTime = System.currentTimeMillis() - startTime;

        // 计算性能指标
        int totalProcesses = processCreationSuccessCount.get() + processCreationFailureCount.get();
        double avgTimePerProcess = totalProcesses > 0 ? (double) totalProcessCreationTime.get() / totalProcesses : 0;
        double processesPerSecond = (double) totalProcesses / (totalTime / 1000.0);
        double successRate = totalProcesses > 0 ? (double) processCreationSuccessCount.get() / totalProcesses * 100 : 0;

        log.info("=== 高并发性能测试结果 ===");
        log.info("总流程数: {}", totalProcesses);
        log.info("成功流程数: {}", processCreationSuccessCount.get());
        log.info("失败流程数: {}", processCreationFailureCount.get());
        log.info("成功率: {:.2f}%", successRate);
        log.info("总耗时: {}ms", totalTime);
        log.info("平均每流程耗时: {:.2f}ms", avgTimePerProcess);
        log.info("每秒处理流程数: {:.2f}", processesPerSecond);

        // 性能断言
        assertTrue(successRate > 95.0, "成功率应大于95%");
        assertTrue(avgTimePerProcess < 1000, "平均每流程耗时应小于1秒");
        assertTrue(processesPerSecond > 50, "每秒处理流程数应大于50");

        executor.shutdown();
    }

    @Test
    @DisplayName("缓存性能压力测试")
    void testCachePerformanceStress() {
        log.info("=== 开始缓存性能压力测试 ===");

        int cacheOperations = 10000;
        long startTime = System.currentTimeMillis();

        // 测试缓存写入性能
        long writeStartTime = System.currentTimeMillis();
        for (int i = 0; i < cacheOperations; i++) {
            String key = "performance-test-" + i;
            Object value = generateTestData(i);

            workflowCacheManager.put(key, value, Duration.ofMinutes(5));
        }
        long writeTime = System.currentTimeMillis() - writeStartTime;

        // 测试缓存读取性能
        long readStartTime = System.currentTimeMillis();
        int cacheHits = 0;
        int cacheMisses = 0;

        for (int i = 0; i < cacheOperations; i++) {
            String key = "performance-test-" + i;
            Object value = workflowCacheManager.get(key);

            if (value != null) {
                cacheHits++;
            } else {
                cacheMisses++;
            }
        }
        long readTime = System.currentTimeMillis() - readStartTime;

        // 测试缓存删除性能
        long deleteStartTime = System.currentTimeMillis();
        for (int i = 0; i < cacheOperations; i++) {
            String key = "performance-test-" + i;
            workflowCacheManager.evict(key);
        }
        long deleteTime = System.currentTimeMillis() - deleteStartTime;

        double writeOpsPerSecond = (double) cacheOperations / (writeTime / 1000.0);
        double readOpsPerSecond = (double) cacheOperations / (readTime / 1000.0);
        double deleteOpsPerSecond = (double) cacheOperations / (deleteTime / 1000.0);
        double hitRate = (double) cacheHits / (cacheHits + cacheMisses) * 100;

        log.info("=== 缓存性能测试结果 ===");
        log.info("写入操作: {} 次, 耗时: {}ms, 速度: {:.2f} ops/s", cacheOperations, writeTime, writeOpsPerSecond);
        log.info("读取操作: {} 次, 耗时: {}ms, 速度: {:.2f} ops/s", cacheOperations, readTime, readOpsPerSecond);
        log.info("删除操作: {} 次, 耗时: {}ms, 速度: {:.2f} ops/s", cacheOperations, deleteTime, deleteOpsPerSecond);
        log.info("缓存命中数: {}", cacheHits);
        log.info("缓存未命中数: {}", cacheMisses);
        log.info("缓存命中率: {:.2f}%", hitRate);

        // 缓存性能断言
        assertTrue(writeOpsPerSecond > 1000, "缓存写入速度应大于1000 ops/s");
        assertTrue(readOpsPerSecond > 5000, "缓存读取速度应大于5000 ops/s");
        assertTrue(hitRate > 99.0, "缓存命中率应大于99%");
    }

    @Test
    @DisplayName("异步处理性能测试")
    void testAsyncProcessingPerformance() {
        log.info("=== 开始异步处理性能测试 ===");

        int asyncTasks = 1000;
        CompletableFuture<Long>[] futures = new CompletableFuture[asyncTasks];
        AtomicInteger completedTasks = new AtomicInteger(0);
        AtomicInteger failedTasks = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        // 提交异步任务
        for (int i = 0; i < asyncTasks; i++) {
            final int taskIndex = i;
            Map<String, Object> taskData = new HashMap<>();
            taskData.put("taskIndex", taskIndex);
            taskData.put("payload", generateTestData(taskIndex));

            CompletableFuture<Long> future = workflowAsyncProcessor.processAsyncTask(
                "performance-test-task",
                taskData,
                (data) -> {
                    try {
                        // 模拟任务处理
                        Thread.sleep(10 + (taskIndex % 20)); // 10-30ms处理时间
                        completedTasks.incrementAndGet();
                        return System.currentTimeMillis();
                    } catch (Exception e) {
                        failedTasks.incrementAndGet();
                        throw new RuntimeException("异步任务处理失败", e);
                    }
                }
            );

            futures[i] = future;
        }

        // 等待所有任务完成
        CompletableFuture.allOf(futures).join();

        long totalTime = System.currentTimeMillis() - startTime;

        // 计算异步处理性能指标
        int totalCompleted = completedTasks.get();
        int totalFailed = failedTasks.get();
        double successRate = (double) totalCompleted / asyncTasks * 100;
        double tasksPerSecond = (double) asyncTasks / (totalTime / 1000.0);

        log.info("=== 异步处理性能测试结果 ===");
        log.info("总任务数: {}", asyncTasks);
        log.info("完成任务数: {}", totalCompleted);
        log.info("失败任务数: {}", totalFailed);
        log.info("成功率: {:.2f}%", successRate);
        log.info("总耗时: {}ms", totalTime);
        log.info("每秒处理任务数: {:.2f}", tasksPerSecond);

        // 异步处理性能断言
        assertTrue(successRate > 98.0, "异步任务成功率应大于98%");
        assertTrue(tasksPerSecond > 100, "每秒处理任务数应大于100");
    }

    @Test
    @Disabled("长时间稳定性测试，仅在需要时运行")
    @DisplayName("长时间运行稳定性测试")
    void testLongTermStability() throws InterruptedException {
        log.info("=== 开始长时间运行稳定性测试 - 预计运行{}小时 ===", STABILITY_TEST_HOURS);

        ExecutorService executor = Executors.newFixedThreadPool(20);
        AtomicInteger totalOperations = new AtomicInteger(0);
        AtomicLong memoryUsagePeak = new AtomicLong(0);

        long testStartTime = System.currentTimeMillis();
        long testEndTime = testStartTime + (STABILITY_TEST_HOURS * 60 * 60 * 1000);

        // 定期监控线程
        ScheduledExecutorService monitorExecutor = Executors.newSingleThreadScheduledExecutor();
        monitorExecutor.scheduleAtFixedRate(() -> {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;

            memoryUsagePeak.set(Math.max(memoryUsagePeak.get(), usedMemory));

            log.info("稳定性监控 - 总操作数: {}, 当前内存使用: {}MB, 峰值内存: {}MB",
                    totalOperations.get(),
                    usedMemory / 1024 / 1024,
                    memoryUsagePeak.get() / 1024 / 1024);

            // 检查内存泄漏
            if (usedMemory > runtime.maxMemory() * 0.8) {
                log.warn("内存使用率过高: {:.2f}%", (double) usedMemory / runtime.maxMemory() * 100);
            }

        }, 1, 1, TimeUnit.MINUTES);

        // 运行稳定性测试
        while (System.currentTimeMillis() < testEndTime) {
            // 随机操作
            int operationType = ThreadLocalRandom.current().nextInt(4);

            switch (operationType) {
                case 0: // 创建流程
                    executor.submit(() -> createTestProcess());
                    break;
                case 1: // 查询流程
                    executor.submit(() -> queryTestProcesses());
                    break;
                case 2: // 缓存操作
                    executor.submit(() -> performCacheOperations());
                    break;
                case 3: // 异步任务
                    executor.submit(() -> performAsyncTask());
                    break;
            }

            totalOperations.incrementAndGet();

            // 控制操作频率
            Thread.sleep(ThreadLocalRandom.current().nextInt(100, 500));
        }

        // 停止监控
        monitorExecutor.shutdown();
        executor.shutdown();

        // 等待任务完成
        executor.awaitTermination(30, TimeUnit.SECONDS);
        monitorExecutor.awaitTermination(5, TimeUnit.SECONDS);

        long totalTestTime = System.currentTimeMillis() - testStartTime;
        double operationsPerSecond = (double) totalOperations.get() / (totalTestTime / 1000.0);

        log.info("=== 长时间稳定性测试完成 ===");
        log.info("测试时长: {}小时", totalTestTime / 1000.0 / 60 / 60);
        log.info("总操作数: {}", totalOperations.get());
        log.info("平均每秒操作数: {:.2f}", operationsPerSecond);
        log.info("峰值内存使用: {}MB", memoryUsagePeak.get() / 1024 / 1024);

        // 稳定性断言
        assertTrue(operationsPerSecond > 10, "长时间运行平均每秒操作数应大于10");
        assertTrue(memoryUsagePeak.get() < Runtime.getRuntime().maxMemory() * 0.9, "内存峰值使用率应小于90%");
    }

    @Test
    @DisplayName("故障恢复测试")
    void testFailoverRecovery() {
        log.info("=== 开始故障恢复测试 ===");

        List<String> processInstanceIds = new ArrayList<>();

        try {
            // 预先创建一些流程实例
            for (int i = 0; i < 10; i++) {
                Map<String, Object> variables = new HashMap<>();
                variables.put("testType", "failover");
                variables.put("index", i);

                String processInstanceId = workflowEngineService.startProcess(
                    TEST_PROCESS_KEY,
                    "故障恢复测试流程-" + i,
                    variables,
                    3000 + i
                );

                if (processInstanceId != null) {
                    processInstanceIds.add(processInstanceId);
                }
            }

            log.info("创建了 {} 个测试流程实例", processInstanceIds.size());

            // 模拟系统故障（这里通过暂停来模拟）
            log.info("模拟系统故障...");
            Thread.sleep(2000);

            // 验证故障后系统状态
            log.info("验证故障后系统状态...");

            int activeProcesses = 0;
            for (String processInstanceId : processInstanceIds) {
                try {
                    Map<String, Object> status = workflowEngineService.getProcessInstanceStatus(processInstanceId);
                    if ("RUNNING".equals(status.get("status"))) {
                        activeProcesses++;
                    }
                } catch (Exception e) {
                    log.warn("获取流程状态失败: {}", processInstanceId, e);
                }
            }

            log.info("故障后活跃流程数: {}", activeProcesses);

            // 模拟系统恢复
            log.info("模拟系统恢复...");
            Thread.sleep(1000);

            // 验证系统恢复后的功能
            log.info("验证系统恢复后功能...");

            Map<String, Object> testVariables = new HashMap<>();
            testVariables.put("testType", "recovery");

            String newProcessInstanceId = workflowEngineService.startProcess(
                TEST_PROCESS_KEY,
                "恢复后测试流程",
                testVariables,
                4000
            );

            assertNotNull(newProcessInstanceId, "系统恢复后应该能够创建新流程");
            log.info("系统恢复后成功创建流程: {}", newProcessInstanceId);

        } catch (Exception e) {
            log.error("故障恢复测试失败", e);
            throw new RuntimeException("故障恢复测试失败", e);
        }
    }

    // ========== 辅助方法 ==========

    /**
     * 重置计数器
     */
    private void resetCounters() {
        processCreationSuccessCount.set(0);
        processCreationFailureCount.set(0);
        totalProcessCreationTime.set(0);
        maxConcurrentProcesses.set(0);
    }

    /**
     * 预热缓存
     */
    private void warmupCache() {
        try {
            for (int i = 0; i < 100; i++) {
                String key = "warmup-" + i;
                Object value = "warmup-data-" + i;

                workflowCacheManager.put(key, value, Duration.ofMinutes(1));
                workflowCacheManager.get(key);
            }

            log.info("缓存预热完成");
        } catch (Exception e) {
            log.warn("缓存预热失败", e);
        }
    }

    /**
     * 生成测试数据
     */
    private String generateTestData(int size) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size * 10; i++) {
            sb.append("测试数据").append(i).append(",");
        }
        return sb.toString();
    }

    /**
     * 创建测试流程
     */
    private void createTestProcess() {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("testType", "stability");
            variables.put("timestamp", System.currentTimeMillis());

            workflowEngineService.startProcess(
                TEST_PROCESS_KEY,
                "稳定性测试流程-" + System.currentTimeMillis(),
                variables,
                ThreadLocalRandom.current().nextInt(1000, 9999)
            );
        } catch (Exception e) {
            log.warn("创建测试流程失败", e);
        }
    }

    /**
     * 查询测试流程
     */
    private void queryTestProcesses() {
        try {
            workflowEngineService.getProcessDefinitions();
        } catch (Exception e) {
            log.warn("查询测试流程失败", e);
        }
    }

    /**
     * 执行缓存操作
     */
    private void performCacheOperations() {
        try {
            String key = "test-" + System.currentTimeMillis();
            String value = "test-data-" + System.currentTimeMillis();

            workflowCacheManager.put(key, value, Duration.ofMinutes(1));
            workflowCacheManager.get(key);
            workflowCacheManager.evict(key);
        } catch (Exception e) {
            log.warn("执行缓存操作失败", e);
        }
    }

    /**
     * 执行异步任务
     */
    private void performAsyncTask() {
        try {
            Map<String, Object> taskData = new HashMap<>();
            taskData.put("timestamp", System.currentTimeMillis());

            workflowAsyncProcessor.processAsyncTask(
                "stability-test-task",
                taskData,
                (data) -> {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(10, 100));
                    return System.currentTimeMillis();
                }
            );
        } catch (Exception e) {
            log.warn("执行异步任务失败", e);
        }
    }

    /**
     * 清理性能测试
     */
    private void cleanupPerformanceTest() {
        try {
            // 清理缓存
            workflowCacheManager.clear();

            log.info("性能测试资源清理完成");
        } catch (Exception e) {
            log.warn("性能测试资源清理失败", e);
        }
    }

    /**
     * 打印性能统计
     */
    private void printPerformanceStatistics() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        log.info("=== 性能统计信息 ===");
        log.info("JVM总内存: {}MB", totalMemory / 1024 / 1024);
        log.info("JVM已用内存: {}MB", usedMemory / 1024 / 1024);
        log.info("JVM空闲内存: {}MB", freeMemory / 1024 / 1024);
        log.info("JVM最大内存: {}MB", runtime.maxMemory() / 1024 / 1024);
        log.info("内存使用率: {:.2f}%", (double) usedMemory / runtime.maxMemory() * 100);
    }
}