package net.lab1024.sa.attendance.performance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.mobile.AttendanceMobileService;
import net.lab1024.sa.attendance.mobile.model.*;
import net.lab1024.sa.attendance.realtime.RealtimeCalculationEngine;
import net.lab1024.sa.attendance.rule.AttendanceRuleEngine;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 考勤系统性能压力测试
 * <p>
 * 全面测试考勤系统在高并发情况下的性能表现
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@RequiredArgsConstructor
@DirtiesContext(classMode = DirtiesContext.ClassMode)
@Disabled("性能压力测试需独立环境执行，默认不在单元测试阶段运行")
public class AttendancePerformanceTest {

    // ==================== 核心服务依赖 ====================

    private final AttendanceMobileService mobileService;
    private final RealtimeCalculationEngine realtimeEngine;
    private final AttendanceRuleEngine ruleEngine;

    // ==================== 测试配置 ====================

    private static final int CONCURRENT_USERS = 500;
    private static final int REQUESTS_PER_USER = 100;
    private static final int PERFORMANCE_TEST_TIMEOUT_SECONDS = 30;
    private static final String TEST_USERNAME_PREFIX = "perf_user_";
    private static final String TEST_PASSWORD = "Test@123456";

    private ExecutorService performanceExecutor;
    private List<String> testTokens;

    @BeforeEach
    void setUp() {
        log.info("[性能测试] 开始性能测试准备");

        // 初始化线程池
        performanceExecutor = Executors.newFixedThreadPool(CONCURRENT_USERS * 2);
        testTokens = new ArrayList<>();

        // 预热系统
        warmupSystem();

        log.info("[性能测试] 性能测试准备完成");
    }

    @AfterEach
    void tearDown() {
        log.info("[性能测试] 开始清理性能测试资源");

        if (performanceExecutor != null && !performanceExecutor.isShutdown()) {
            performanceExecutor.shutdown();
            try {
                if (!performanceExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    performanceExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                performanceExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        testTokens.clear();
        log.info("[性能测试] 性能测试资源清理完成");
    }

    @Test
    @DisplayName("移动端登录性能压力测试")
    void testMobileLoginPerformance() {
        log.info("[性能测试] 开始移动端登录性能压力测试");

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicLong totalResponseTime = new AtomicLong(0);
        AtomicLong maxResponseTime = new AtomicLong(0);
        AtomicLong minResponseTime = new AtomicLong(Long.MAX_VALUE);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        // 并发登录测试
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            final int userId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    for (int j = 0; j < REQUESTS_PER_USER; j++) {
                        long requestStart = System.currentTimeMillis();

                        MobileLoginRequest loginRequest = new MobileLoginRequest();
                        loginRequest.setUsername(TEST_USERNAME_PREFIX + userId + "_" + j);
                        loginRequest.setPassword(TEST_PASSWORD);
                        loginRequest.setCaptchaCode("1234");

                        MobileLoginRequest.DeviceInfo deviceInfo = new MobileLoginRequest.DeviceInfo();
                        deviceInfo.setDeviceId("PERF_DEVICE_" + userId);
                        deviceInfo.setDeviceType("ANDROID");
                        loginRequest.setDeviceInfo(deviceInfo);

                        CompletableFuture<MobileLoginResult> loginFuture = mobileService.login(loginRequest);
                        MobileLoginResult loginResult = loginFuture.get(PERFORMANCE_TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

                        long responseTime = System.currentTimeMillis() - requestStart;
                        totalResponseTime.addAndGet(responseTime);

                        maxResponseTime.updateAndGet(current -> Math.max(current, responseTime));
                        minResponseTime.updateAndGet(current -> Math.min(current, responseTime));

                        if (loginResult.isSuccess()) {
                            successCount.incrementAndGet();
                            if (j == 0) {
                                synchronized (testTokens) {
                                    testTokens.add(loginResult.getToken());
                                }
                            }
                        } else {
                            failureCount.incrementAndGet();
                        }

                        // 模拟用户操作间隔
                        Thread.sleep(10);
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    log.warn("[性能测试] 登录请求异常", e);
                }
            }, performanceExecutor);

            futures.add(future);
        }

        // 等待所有请求完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .get(PERFORMANCE_TEST_TIMEOUT_SECONDS * 10, TimeUnit.SECONDS);

        long totalTime = System.currentTimeMillis() - startTime;
        int totalRequests = successCount.get() + failureCount.get();
        double averageResponseTime = totalResponseTime.get() / (double) totalRequests;
        double tps = (double) totalRequests / (totalTime / 1000.0);
        double successRate = (double) successCount.get() / totalRequests * 100;

        // 性能统计
        log.info("[性能测试] 登录性能统计:");
        log.info("  并发用户数: {}", CONCURRENT_USERS);
        log.info("  每用户请求数: {}", REQUESTS_PER_USER);
        log.info("  总请求数: {}", totalRequests);
        log.info("  成功请求数: {}", successCount.get());
        log.info("  失败请求数: {}", failureCount.get());
        log.info("  成功率: {:.2f}%", successRate);
        log.info("  总耗时: {}ms", totalTime);
        log.info("  TPS: {:.2f}", tps);
        log.info("  平均响应时间: {:.2f}ms", averageResponseTime);
        log.info("  最大响应时间: {}ms", maxResponseTime.get());
        log.info("  最小响应时间: {}ms", minResponseTime.get() == Long.MAX_VALUE ? 0 : minResponseTime.get());

        // 性能断言
        assert tps >= 100.0 : "登录TPS应该大于等于100，实际: " + tps;
        assert averageResponseTime <= 200.0 : "平均响应时间应该小于等于200ms，实际: " + averageResponseTime;
        assert successRate >= 95.0 : "成功率应该大于等于95%，实际: " + successRate;

        log.info("[性能测试] 移动端登录性能压力测试通过");
    }

    @Test
    @DisplayName("移动端打卡性能压力测试")
    void testMobileClockingPerformance() {
        log.info("[性能测试] 开始移动端打卡性能压力测试");

        // 准备测试Token
        prepareTestTokens();

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicLong totalResponseTime = new AtomicLong(0);
        AtomicLong maxResponseTime = new AtomicLong(0);
        AtomicLong minResponseTime = new AtomicLong(Long.MAX_VALUE);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        // 并发打卡测试
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            final int userId = i;
            final String token = getToken(userId);

            if (token != null) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        for (int j = 0; j < REQUESTS_PER_USER; j++) {
                            long requestStart = System.currentTimeMillis();

                            // 交替测试上班打卡和下班打卡
                            if (j % 2 == 0) {
                                MobileClockInRequest clockInRequest = new MobileClockInRequest();
                                clockInRequest.setDeviceCode("PERF_DEVICE_" + userId);
                                clockInRequest.setRemark("性能测试上班打卡");

                                MobileClockInRequest.Location location = new MobileClockInRequest.Location();
                                location.setLatitude(39.9042);
                                location.setLongitude(116.4074);
                                clockInRequest.setLocation(location);

                                CompletableFuture<MobileClockInResult> clockInFuture =
                                    mobileService.clockIn(clockInRequest, token);
                                MobileClockInResult clockInResult = clockInFuture.get(10, TimeUnit.SECONDS);

                                long responseTime = System.currentTimeMillis() - requestStart;
                                updatePerformanceMetrics(successCount, failureCount, totalResponseTime,
                                    maxResponseTime, minResponseTime, responseTime, clockInResult.isSuccess());
                            } else {
                                MobileClockOutRequest clockOutRequest = new MobileClockOutRequest();
                                clockOutRequest.setDeviceCode("PERF_DEVICE_" + userId);
                                clockOutRequest.setRemark("性能测试下班打卡");

                                CompletableFuture<MobileClockOutResult> clockOutFuture =
                                    mobileService.clockOut(clockOutRequest, token);
                                MobileClockOutResult clockOutResult = clockOutFuture.get(10, TimeUnit.SECONDS);

                                long responseTime = System.currentTimeMillis() - requestStart;
                                updatePerformanceMetrics(successCount, failureCount, totalResponseTime,
                                    maxResponseTime, minResponseTime, responseTime, clockOutResult.isSuccess());
                            }

                            // 模拟用户操作间隔
                            Thread.sleep(5);
                        }
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                        log.warn("[性能测试] 打卡请求异常", e);
                    }
                }, performanceExecutor);

                futures.add(future);
            }
        }

        // 等待所有请求完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .get(PERFORMANCE_TEST_TIMEOUT_SECONDS * 5, TimeUnit.SECONDS);

        long totalTime = System.currentTimeMillis() - startTime;
        int totalRequests = successCount.get() + failureCount.get();
        double averageResponseTime = totalResponseTime.get() / (double) totalRequests;
        double tps = (double) totalRequests / (totalTime / 1000.0);
        double successRate = (double) successCount.get() / totalRequests * 100;

        // 性能统计
        log.info("[性能测试] 打卡性能统计:");
        log.info("  有效用户数: {}", testTokens.size());
        log.info("  总请求数: {}", totalRequests);
        log.info("  成功请求数: {}", successCount.get());
        log.info("  失败请求数: {}", failureCount.get());
        log.info("  成功率: {:.2f}%", successRate);
        log.info("  总耗时: {}ms", totalTime);
        log.info("  TPS: {:.2f}", tps);
        log.info("  平均响应时间: {:.2f}ms", averageResponseTime);
        log.info("  最大响应时间: {}ms", maxResponseTime.get());
        log.info("  最小响应时间: {}ms", minResponseTime.get() == Long.MAX_VALUE ? 0 : minResponseTime.get());

        // 性能断言
        assert tps >= 200.0 : "打卡TPS应该大于等于200，实际: " + tps;
        assert averageResponseTime <= 150.0 : "平均响应时间应该小于等于150ms，实际: " + averageResponseTime;
        assert successRate >= 98.0 : "成功率应该大于等于98%，实际: " + successRate;

        log.info("[性能测试] 移动端打卡性能压力测试通过");
    }

    @Test
    @DisplayName("实时计算引擎性能压力测试")
    void testRealtimeCalculationEnginePerformance() {
        log.info("[性能测试] 开始实时计算引擎性能压力测试");

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicLong totalResponseTime = new AtomicLong(0);
        AtomicLong maxResponseTime = new AtomicLong(0);
        AtomicLong minResponseTime = new AtomicLong(Long.MAX_VALUE);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        // 并发事件处理测试
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            final int userId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    for (int j = 0; j < REQUESTS_PER_USER; j++) {
                        long requestStart = System.currentTimeMillis();

                        // 创建考勤事件
                        RealtimeCalculationEngine.AttendanceEvent event =
                            createTestEvent(userId + 1000, j % 2 == 0);

                        CompletableFuture<RealtimeCalculationEngine.RealtimeCalculationResult> processFuture =
                            realtimeEngine.processAttendanceEvent(event);
                        RealtimeCalculationEngine.RealtimeCalculationResult processResult =
                            processFuture.get(10, TimeUnit.SECONDS);

                        long responseTime = System.currentTimeMillis() - requestStart;
                        updatePerformanceMetrics(successCount, failureCount, totalResponseTime,
                            maxResponseTime, minResponseTime, responseTime, processResult.isSuccess());

                        // 模拟事件间隔
                        Thread.sleep(2);
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    log.warn("[性能测试] 实时计算请求异常", e);
                }
            }, performanceExecutor);

            futures.add(future);
        }

        // 等待所有请求完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .get(PERFORMANCE_TEST_TIMEOUT_SECONDS * 5, TimeUnit.SECONDS);

        long totalTime = System.currentTimeMillis() - startTime;
        int totalRequests = successCount.get() + failureCount.get();
        double averageResponseTime = totalResponseTime.get() / (double) totalRequests;
        double tps = (double) totalRequests / (totalTime / 1000.0);
        double successRate = (double) successCount.get() / totalRequests * 100;

        // 性能统计
        log.info("[性能测试] 实时计算引擎性能统计:");
        log.info("  总请求数: {}", totalRequests);
        log.info("  成功请求数: {}", successCount.get());
        log.info("  失败请求数: {}", failureCount.get());
        log.info("  成功率: {:.2f}%", successRate);
        log.info("  总耗时: {}ms", totalTime);
        log.info("  TPS: {:.2f}", tps);
        log.info("  平均响应时间: {:.2f}ms", averageResponseTime);
        log.info("  最大响应时间: {}ms", maxResponseTime.get());
        log.info("  最小响应时间: {}ms", minResponseTime.get() == Long.MAX_VALUE ? 0 : minResponseTime.get());

        // 性能断言
        assert tps >= 500.0 : "实时计算TPS应该大于等于500，实际: " + tps;
        assert averageResponseTime <= 100.0 : "平均响应时间应该小于等于100ms，实际: " + averageResponseTime;
        assert successRate >= 99.0 : "成功率应该大于等于99%，实际: " + successRate;

        log.info("[性能测试] 实时计算引擎性能压力测试通过");
    }

    @Test
    @DisplayName("考勤规则引擎性能压力测试")
    void testAttendanceRuleEnginePerformance() {
        log.info("[性能测试] 开始考勤规则引擎性能压力测试");

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicLong totalResponseTime = new AtomicLong(0);
        AtomicLong maxResponseTime = new AtomicLong(0);
        AtomicLong minResponseTime = new AtomicLong(Long.MAX_VALUE);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        // 并发规则执行测试
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            final int userId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    for (int j = 0; j < REQUESTS_PER_USER; j++) {
                        long requestStart = System.currentTimeMillis();

                        // 创建考勤规则上下文
                        RealtimeCalculationEngine.AttendanceRuleContext context =
                            createTestRuleContext(userId + 2000, j);

                        CompletableFuture<RealtimeCalculationEngine.RuleExecutionResult> executionFuture =
                            ruleEngine.executeRules("PERFORMANCE_TEST_RULE", context);
                        RealtimeCalculationEngine.RuleExecutionResult executionResult =
                            executionFuture.get(10, TimeUnit.SECONDS);

                        long responseTime = System.currentTimeMillis() - requestStart;
                        updatePerformanceMetrics(successCount, failureCount, totalResponseTime,
                            maxResponseTime, minResponseTime, responseTime, executionResult.isSuccess());

                        // 模拟规则执行间隔
                        Thread.sleep(1);
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    log.warn("[性能测试] 规则引擎执行异常", e);
                }
            }, performanceExecutor);

            futures.add(future);
        }

        // 等待所有请求完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .get(PERFORMANCE_TEST_TIMEOUT_SECONDS * 3, TimeUnit.SECONDS);

        long totalTime = System.currentTimeMillis() - startTime;
        int totalRequests = successCount.get() + failureCount.get();
        double averageResponseTime = totalResponseTime.get() / (double) totalRequests;
        double tps = (double) totalRequests / (totalTime / 1000.0);
        double successRate = (double) successCount.get() / totalRequests * 100;

        // 性能统计
        log.info("[性能测试] 考勤规则引擎性能统计:");
        log.info("  总请求数: {}", totalRequests);
        log.info("  成功请求数: {}", successCount.get());
        log.info("  失败请求数: {}", failureCount.get());
        log.info("  成功率: {:.2f}%", successRate);
        log.info("  总耗时: {}ms", totalTime);
        log.info("  TPS: {:.2f}", tps);
        log.info("  平均响应时间: {:.2f}ms", averageResponseTime);
        log.info("  最大响应时间: {}ms", maxResponseTime.get());
        log.info("  最小响应时间: {}ms", minResponseTime.get() == Long.MAX_VALUE ? 0 : minResponseTime.get());

        // 性能断言
        assert tps >= 1000.0 : "规则引擎TPS应该大于等于1000，实际: " + tps;
        assert averageResponseTime <= 50.0 : "平均响应时间应该小于等于50ms，实际: " + averageResponseTime;
        assert successRate >= 99.5 : "成功率应该大于等于99.5%，实际: " + successRate;

        log.info("[性能测试] 考勤规则引擎性能压力测试通过");
    }

    @Test
    @DisplayName("内存使用压力测试")
    void testMemoryUsageStress() {
        log.info("[性能测试] 开始内存使用压力测试");

        Runtime runtime = Runtime.getRuntime();

        // 记录初始内存状态
        long initialTotalMemory = runtime.totalMemory();
        long initialFreeMemory = runtime.freeMemory();
        long initialUsedMemory = initialTotalMemory - initialFreeMemory;

        log.info("[性能测试] 初始内存状态 - 总内存: {}MB, 已用: {}MB, 可用: {}MB",
                initialTotalMemory / 1024 / 1024,
                initialUsedMemory / 1024 / 1024,
                initialFreeMemory / 1024 / 1024);

        // 执行大量操作
        int largeOperationCount = CONCURRENT_USERS * REQUESTS_PER_USER * 5;
        List<CompletableFuture<Object>> futures = new ArrayList<>();

        for (int i = 0; i < largeOperationCount; i++) {
            final int operationId = i;
            CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> {
                try {
                    // 创建大量临时对象
                    List<byte[]> tempData = new ArrayList<>();
                    for (int j = 0; j < 100; j++) {
                        tempData.add(new byte[1024]); // 1KB临时对象
                    }

                    // 模拟业务处理
                    Thread.sleep(1);

                    // 释放临时对象
                    tempData.clear();

                    return operationId;
                } catch (Exception e) {
                    log.warn("[性能测试] 内存压力操作异常", e);
                    return null;
                }
            }, performanceExecutor);

            futures.add(future);
        }

        // 等待所有操作完成
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("[性能测试] 内存压力测试超时", e);
        }

        // 强制垃圾回收
        System.gc();
        Thread.sleep(1000);

        // 记录最终内存状态
        long finalTotalMemory = runtime.totalMemory();
        long finalFreeMemory = runtime.freeMemory();
        long finalUsedMemory = finalTotalMemory - finalFreeMemory;

        log.info("[性能测试] 最终内存状态 - 总内存: {}MB, 已用: {}MB, 可用: {}MB",
                finalTotalMemory / 1024 / 1024,
                finalUsedMemory / 1024 / 1024,
                finalFreeMemory / 1024 / 1024);

        // 内存增长分析
        long memoryIncrease = finalUsedMemory - initialUsedMemory;
        double memoryIncreasePercent = (double) memoryIncrease / initialUsedMemory * 100;
        long maxMemory = runtime.maxMemory();
        double memoryUsagePercent = (double) finalUsedMemory / maxMemory * 100;

        log.info("[性能测试] 内存使用分析:");
        log.info("  内存增长: {}MB ({:.2f}%)", memoryIncrease / 1024 / 1024, memoryIncreasePercent);
        log.info("  当前内存使用率: {:.2f}%", memoryUsagePercent);
        log.info("  最大可用内存: {}MB", maxMemory / 1024 / 1024);

        // 内存性能断言
        assert memoryUsagePercent < 85.0 : "内存使用率应该小于85%，实际: " + memoryUsagePercent;
        assert memoryIncreasePercent < 200.0 : "内存增长率应该小于200%，实际: " + memoryIncreasePercent;

        log.info("[性能测试] 内存使用压力测试通过");
    }

    // ==================== 辅助方法 ====================

    /**
     * 系统预热
     */
    private void warmupSystem() {
        try {
            log.info("[性能测试] 开始系统预热");

            // 预热移动服务
            for (int i = 0; i < 10; i++) {
                MobileLoginRequest loginRequest = new MobileLoginRequest();
                loginRequest.setUsername("warmup_user_" + i);
                loginRequest.setPassword(TEST_PASSWORD);
                mobileService.login(loginRequest).get(5, TimeUnit.SECONDS);
            }

            // 预热实时计算引擎
            for (int i = 0; i < 5; i++) {
                RealtimeCalculationEngine.AttendanceEvent event = createTestEvent(3000 + i, true);
                realtimeEngine.processAttendanceEvent(event).get(5, TimeUnit.SECONDS);
            }

            // 预热规则引擎
            for (int i = 0; i < 5; i++) {
                RealtimeCalculationEngine.AttendanceRuleContext context = createTestRuleContext(4000 + i, 0);
                ruleEngine.executeRules("WARMUP_RULE", context).get(5, TimeUnit.SECONDS);
            }

            log.info("[性能测试] 系统预热完成");

        } catch (Exception e) {
            log.warn("[性能测试] 系统预热出现异常，但继续执行测试", e);
        }
    }

    /**
     * 准备测试Token
     */
    private void prepareTestTokens() {
        try {
            log.info("[性能测试] 准备测试Token");

            for (int i = 0; i < Math.min(CONCURRENT_USERS, 50); i++) {
                MobileLoginRequest loginRequest = new MobileLoginRequest();
                loginRequest.setUsername(TEST_USERNAME_PREFIX + i);
                loginRequest.setPassword(TEST_PASSWORD);

                MobileLoginRequest.DeviceInfo deviceInfo = new MobileLoginRequest.DeviceInfo();
                deviceInfo.setDeviceId("PERF_DEVICE_" + i);
                deviceInfo.setDeviceType("ANDROID");
                loginRequest.setDeviceInfo(deviceInfo);

                CompletableFuture<MobileLoginResult> loginFuture = mobileService.login(loginRequest);
                MobileLoginResult loginResult = loginFuture.get(10, TimeUnit.SECONDS);

                if (loginResult.isSuccess() && loginResult.getToken() != null) {
                    testTokens.add(loginResult.getToken());
                }
            }

            log.info("[性能测试] 测试Token准备完成，数量: {}", testTokens.size());

        } catch (Exception e) {
            log.warn("[性能测试] 测试Token准备失败", e);
        }
    }

    /**
     * 获取测试Token
     */
    private String getToken(int userId) {
        if (testTokens.isEmpty()) {
            return null;
        }
        return testTokens.get(userId % testTokens.size());
    }

    /**
     * 创建测试考勤事件
     */
    private RealtimeCalculationEngine.AttendanceEvent createTestEvent(long employeeId, boolean isClockIn) {
        return RealtimeCalculationEngine.AttendanceEvent.builder()
                .employeeId(employeeId)
                .eventType(isClockIn ?
                    RealtimeCalculationEngine.AttendanceEvent.EventType.CLOCK_IN :
                    RealtimeCalculationEngine.AttendanceEvent.EventType.CLOCK_OUT)
                .eventTime(LocalDateTime.now())
                .deviceCode("PERF_DEVICE_" + employeeId)
                .location("北京市朝阳区建国门外大街1号")
                .build();
    }

    /**
     * 创建测试规则上下文
     */
    private RealtimeCalculationEngine.AttendanceRuleContext createTestRuleContext(long employeeId, int index) {
        return RealtimeCalculationEngine.AttendanceRuleContext.builder()
                .employeeId(employeeId)
                .attendanceDate(LocalDate.now())
                .clockInTime(index % 3 == 0 ?
                    LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 55)) :
                    LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 15)))
                .clockOutTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(18, 0)))
                .build();
    }

    /**
     * 更新性能指标
     */
    private void updatePerformanceMetrics(AtomicInteger successCount, AtomicInteger failureCount,
                                           AtomicLong totalResponseTime, AtomicLong maxResponseTime,
                                           AtomicLong minResponseTime, long responseTime,
                                           boolean success) {
        totalResponseTime.addAndGet(responseTime);
        maxResponseTime.updateAndGet(current -> Math.max(current, responseTime));
        minResponseTime.updateAndGet(current -> Math.min(current, responseTime));

        if (success) {
            successCount.incrementAndGet();
        } else {
            failureCount.incrementAndGet();
        }
    }
}