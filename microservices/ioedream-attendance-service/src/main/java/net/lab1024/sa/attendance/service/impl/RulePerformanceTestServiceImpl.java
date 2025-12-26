package net.lab1024.sa.attendance.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.lab1024.sa.attendance.dao.RulePerformanceTestDao;
import net.lab1024.sa.attendance.domain.form.RulePerformanceTestForm;
import net.lab1024.sa.attendance.domain.form.RuleTestRequest;
import net.lab1024.sa.attendance.domain.vo.RulePerformanceTestDetailVO;
import net.lab1024.sa.attendance.domain.vo.RulePerformanceTestResultVO;
import net.lab1024.sa.attendance.engine.rule.AttendanceRuleEngine;
import net.lab1024.sa.attendance.entity.RulePerformanceTestEntity;
import net.lab1024.sa.attendance.service.RulePerformanceTestService;
import net.lab1024.sa.attendance.service.RuleTestService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 规则性能测试服务实现类
 * <p>
 * 提供性能测试执行和统计功能
 * 支持并发测试和性能分析
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Service
public class RulePerformanceTestServiceImpl implements RulePerformanceTestService {

    @Resource
    private RulePerformanceTestDao rulePerformanceTestDao;

    @Resource
    private RuleTestService ruleTestService;

    @Resource
    private AttendanceRuleEngine attendanceRuleEngine;

    @Resource
    private ObjectMapper objectMapper;

    // 线程池（用于并发测试）
    private final ExecutorService executorService = Executors.newFixedThreadPool(50);

    // 存储正在运行的测试（用于取消）
    private final Map<Long, List<Future<?>>> runningTests = new ConcurrentHashMap<>();

    /**
     * 执行性能测试
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RulePerformanceTestResultVO executePerformanceTest(RulePerformanceTestForm testForm) throws InterruptedException {
        log.info("[性能测试] 开始执行性能测试: testName={}, type={}, concurrentUsers={}, requestsPerUser={}",
                testForm.getTestName(), testForm.getTestType(),
                testForm.getConcurrentUsers(), testForm.getRequestsPerUser());

        long startTime = System.currentTimeMillis();

        try {
            // 创建测试实体
            RulePerformanceTestEntity testEntity = RulePerformanceTestEntity.builder()
                    .testName(testForm.getTestName())
                    .testType(testForm.getTestType())
                    .ruleIds(convertListToJson(testForm.getRuleIds()))
                    .ruleCount(testForm.getRuleIds() != null ? testForm.getRuleIds().size() : 0)
                    .concurrentUsers(testForm.getConcurrentUsers())
                    .testStatus("RUNNING")
                    .startTime(LocalDateTime.now())
                    .createdBy(getCurrentUserId())
                    .createdByName(getCurrentUserName())
                    .build();

            // 保存测试记录
            rulePerformanceTestDao.insert(testEntity);
            Long testId = testEntity.getTestId();

            // 执行并发测试
            PerformanceTestResult result = executeConcurrentTest(testId, testForm);

            // 更新测试结果
            updateTestResult(testEntity, result);
            rulePerformanceTestDao.updateById(testEntity);

            log.info("[性能测试] 测试完成: testId={}, totalRequests={}, successRate={}%, avgTime={}ms",
                    testId, result.totalRequests, result.successRate, result.avgResponseTime);

            return convertToResultVO(testEntity);

        } catch (Exception e) {
            log.error("[性能测试] 测试失败: testName={}, error={}", testForm.getTestName(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 执行并发测试（核心方法）
     */
    private PerformanceTestResult executeConcurrentTest(Long testId, RulePerformanceTestForm testForm) throws InterruptedException {
        int concurrentUsers = testForm.getConcurrentUsers();
        int requestsPerUser = testForm.getRequestsPerUser();
        int totalRequests = concurrentUsers * requestsPerUser;

        // 统计数据
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failedCount = new AtomicInteger(0);
        List<Long> responseTimes = new CopyOnWriteArrayList<>();
        List<SlowRequest> slowRequests = new CopyOnWriteArrayList<>();

        // 计数器（等待所有线程完成）
        CountDownLatch latch = new CountDownLatch(concurrentUsers);

        // 开始时间
        long testStartTime = System.currentTimeMillis();

        // 创建测试任务
        List<Future<?>> futures = new ArrayList<>();
        for (int userId = 0; userId < concurrentUsers; userId++) {
            final int currentUserId = userId;  // 创建final局部变量
            Future<?> future = executorService.submit(() -> {
                try {
                    // 每个用户执行多次请求
                    for (int reqId = 0; reqId < requestsPerUser; reqId++) {
                        try {
                            // 请求间隔
                            if (testForm.getRequestIntervalMs() != null && testForm.getRequestIntervalMs() > 0) {
                                Thread.sleep(testForm.getRequestIntervalMs());
                            }

                            // 执行规则测试
                            long requestStart = System.nanoTime();

                            // 模拟规则测试
                            boolean success = simulateRuleTest(testForm);

                            long requestEnd = System.nanoTime();
                            long responseTime = (requestEnd - requestStart) / 1_000_000; // 转换为毫秒

                            // 记录结果
                            responseTimes.add(responseTime);
                            if (success) {
                                successCount.incrementAndGet();
                            } else {
                                failedCount.incrementAndGet();
                                // 记录慢请求
                                if (responseTime > 1000) { // 超过1秒视为慢请求
                                    slowRequests.add(new SlowRequest(testForm.getTestName(), responseTime, false));
                                }
                            }

                        } catch (Exception e) {
                            log.error("[性能测试] 请求执行异常: userId={}, reqId={}, error={}",
                                    currentUserId, reqId, e.getMessage());
                            failedCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
            futures.add(future);
        }

        // 保存Future引用（用于取消）
        runningTests.put(testId, futures);

        // 等待所有线程完成（或超时）
        long timeoutSeconds = testForm.getTimeoutMs() != null ?
                testForm.getTimeoutMs() / 1000 + 60 : 600; // 默认10分钟超时
        boolean completed = latch.await(timeoutSeconds, TimeUnit.SECONDS);

        long testEndTime = System.currentTimeMillis();
        long duration = testEndTime - testStartTime;

        if (!completed) {
            log.warn("[性能测试] 测试超时: testId={}, timeout={}s", testId, timeoutSeconds);
            // 取消未完成的任务
            futures.forEach(f -> f.cancel(true));
        }

        // 从运行列表中移除
        runningTests.remove(testId);

        // 计算统计数据
        return calculateStatistics(testId, totalRequests, successCount.get(), failedCount.get(),
                responseTimes, slowRequests, duration, testForm);
    }

    /**
     * 计算统计数据
     */
    private PerformanceTestResult calculateStatistics(Long testId, int totalRequests,
            int successCount, int failedCount, List<Long> responseTimes,
            List<SlowRequest> slowRequests, long durationMs, RulePerformanceTestForm testForm) {

        PerformanceTestResult result = new PerformanceTestResult();
        result.testId = testId;
        result.totalRequests = totalRequests;
        result.successRequests = successCount;
        result.failedRequests = failedCount;
        result.successRate = totalRequests > 0 ? (successCount * 100.0 / totalRequests) : 0;
        result.durationSeconds = durationMs / 1000;

        // 响应时间统计
        if (!responseTimes.isEmpty()) {
            Collections.sort(responseTimes);

            result.minResponseTime = responseTimes.get(0);
            result.maxResponseTime = responseTimes.get(responseTimes.size() - 1);

            // 平均响应时间
            double avg = responseTimes.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0);
            result.avgResponseTime = avg;

            // 百分位数
            result.p50ResponseTime = getPercentile(responseTimes, 50);
            result.p95ResponseTime = getPercentile(responseTimes, 95);
            result.p99ResponseTime = getPercentile(responseTimes, 99);
        }

        // 吞吐量计算
        if (durationMs > 0) {
            result.throughput = (successCount * 1000.0) / durationMs; // TPS
            result.qps = (totalRequests * 1000.0) / durationMs;        // QPS
        }

        return result;
    }

    /**
     * 获取百分位数
     */
    private Long getPercentile(List<Long> sortedData, int percentile) {
        if (sortedData.isEmpty()) {
            return 0L;
        }
        int index = (int) Math.ceil(percentile / 100.0 * sortedData.size()) - 1;
        return sortedData.get(Math.max(0, Math.min(index, sortedData.size() - 1)));
    }

    /**
     * 模拟规则测试（简化实现）
     */
    private boolean simulateRuleTest(RulePerformanceTestForm testForm) {
        try {
            // 这里应该调用实际的规则测试逻辑
            // 为了性能测试，简化实现

            // 模拟一些计算延迟
            int delay = ThreadLocalRandom.current().nextInt(10, 100);
            Thread.sleep(delay);

            // 模拟成功率（95%成功）
            return ThreadLocalRandom.current().nextInt(100) < 95;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 更新测试结果
     */
    private void updateTestResult(RulePerformanceTestEntity entity, PerformanceTestResult result) {
        entity.setTestStatus("COMPLETED");
        entity.setEndTime(LocalDateTime.now());
        entity.setTotalRequests(result.totalRequests);
        entity.setSuccessRequests(result.successRequests);
        entity.setFailedRequests(result.failedRequests);
        entity.setSuccessRate(result.successRate);
        entity.setMinResponseTime(result.minResponseTime);
        entity.setMaxResponseTime(result.maxResponseTime);
        entity.setAvgResponseTime(result.avgResponseTime);
        entity.setP50ResponseTime(result.p50ResponseTime);
        entity.setP95ResponseTime(result.p95ResponseTime);
        entity.setP99ResponseTime(result.p99ResponseTime);
        entity.setThroughput(result.throughput);
        entity.setQps(result.qps);
        entity.setDurationSeconds(result.durationSeconds);
    }

    /**
     * 查询测试结果
     */
    @Override
    public RulePerformanceTestResultVO getTestResult(Long testId) {
        log.info("[性能测试] 查询测试结果: testId={}", testId);

        RulePerformanceTestEntity entity = rulePerformanceTestDao.selectById(testId);
        if (entity == null) {
            log.warn("[性能测试] 测试记录不存在: testId={}", testId);
            return null;
        }

        return convertToResultVO(entity);
    }

    /**
     * 查询测试详情
     */
    @Override
    public RulePerformanceTestDetailVO getTestDetail(Long testId) {
        log.info("[性能测试] 查询测试详情: testId={}", testId);

        RulePerformanceTestEntity entity = rulePerformanceTestDao.selectById(testId);
        if (entity == null) {
            log.warn("[性能测试] 测试记录不存在: testId={}", testId);
            return null;
        }

        RulePerformanceTestDetailVO detailVO = new RulePerformanceTestDetailVO();
        // 复制基本字段
        copyBasicFields(entity, detailVO);

        // 解析详细数据
        try {
            if (entity.getResponseTimeDistribution() != null) {
                Map<String, Integer> distribution = objectMapper.readValue(
                        entity.getResponseTimeDistribution(),
                        new TypeReference<Map<String, Integer>>() {}
                );
                detailVO.setResponseTimeDistribution(distribution);
            }

            // TODO: 解析其他详细字段

        } catch (Exception e) {
            log.error("[性能测试] 解析详细数据失败: testId={}", testId, e);
        }

        return detailVO;
    }

    /**
     * 查询最近的测试列表
     */
    @Override
    public List<RulePerformanceTestResultVO> getRecentTests(Integer limit) {
        log.info("[性能测试] 查询最近测试: limit={}", limit);

        List<RulePerformanceTestEntity> entities = rulePerformanceTestDao.queryRecentTests(limit);

        return entities.stream()
                .map(this::convertToResultVO)
                .collect(Collectors.toList());
    }

    /**
     * 取消正在运行的测试
     */
    @Override
    public void cancelTest(Long testId) {
        log.info("[性能测试] 取消测试: testId={}", testId);

        List<Future<?>> futures = runningTests.get(testId);
        if (futures != null) {
            // 取消所有任务
            futures.forEach(f -> f.cancel(true));
            runningTests.remove(testId);

            // 更新数据库状态
            RulePerformanceTestEntity entity = rulePerformanceTestDao.selectById(testId);
            if (entity != null && "RUNNING".equals(entity.getTestStatus())) {
                entity.setTestStatus("CANCELLED");
                entity.setEndTime(LocalDateTime.now());
                rulePerformanceTestDao.updateById(entity);
            }

            log.info("[性能测试] 测试已取消: testId={}", testId);
        } else {
            log.warn("[性能测试] 测试不在运行中: testId={}", testId);
        }
    }

    /**
     * 删除测试记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTest(Long testId) {
        log.info("[性能测试] 删除测试记录: testId={}", testId);

        // 如果测试正在运行，先取消
        cancelTest(testId);

        int count = rulePerformanceTestDao.deleteById(testId);
        if (count > 0) {
            log.info("[性能测试] 删除成功: testId={}", testId);
        } else {
            log.warn("[性能测试] 删除失败，记录不存在: testId={}", testId);
        }
    }

    /**
     * 批量删除测试记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteTests(List<Long> testIds) {
        log.info("[性能测试] 批量删除测试记录: count={}", testIds.size());

        int count = 0;
        for (Long testId : testIds) {
            count += rulePerformanceTestDao.deleteById(testId);
        }

        log.info("[性能测试] 批量删除成功: count={}", count);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 转换为ResultVO
     */
    private RulePerformanceTestResultVO convertToResultVO(RulePerformanceTestEntity entity) {
        return RulePerformanceTestResultVO.builder()
                .testId(entity.getTestId())
                .testName(entity.getTestName())
                .testType(entity.getTestType())
                .testStatus(entity.getTestStatus())
                .ruleCount(entity.getRuleCount())
                .concurrentUsers(entity.getConcurrentUsers())
                .totalRequests(entity.getTotalRequests())
                .successRequests(entity.getSuccessRequests())
                .failedRequests(entity.getFailedRequests())
                .successRate(entity.getSuccessRate())
                .minResponseTime(entity.getMinResponseTime())
                .maxResponseTime(entity.getMaxResponseTime())
                .avgResponseTime(entity.getAvgResponseTime())
                .p50ResponseTime(entity.getP50ResponseTime())
                .p95ResponseTime(entity.getP95ResponseTime())
                .p99ResponseTime(entity.getP99ResponseTime())
                .throughput(entity.getThroughput())
                .qps(entity.getQps())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .durationSeconds(entity.getDurationSeconds())
                .errorMessage(entity.getErrorMessage())
                .createTime(entity.getCreateTime())
                .build();
    }

    /**
     * 复制基本字段
     */
    private void copyBasicFields(RulePerformanceTestEntity entity, RulePerformanceTestDetailVO detailVO) {
        detailVO.setTestId(entity.getTestId());
        detailVO.setTestName(entity.getTestName());
        detailVO.setTestType(entity.getTestType());
        detailVO.setTestStatus(entity.getTestStatus());
        // ... 复制其他字段
    }

    /**
     * 转换列表为JSON
     */
    private String convertListToJson(List<?> list) {
        if (list == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            log.error("[性能测试] JSON转换失败", e);
            return null;
        }
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        // TODO: 从SecurityContext获取当前用户ID
        return 1L;
    }

    /**
     * 获取当前用户名
     */
    private String getCurrentUserName() {
        // TODO: 从SecurityContext获取当前用户名
        return "admin";
    }

    // ==================== 内部类 ====================

    /**
     * 性能测试结果（内部使用）
     */
    private static class PerformanceTestResult {
        Long testId;
        int totalRequests;
        int successRequests;
        int failedRequests;
        double successRate;
        Long minResponseTime;
        Long maxResponseTime;
        double avgResponseTime;
        Long p50ResponseTime;
        Long p95ResponseTime;
        Long p99ResponseTime;
        double throughput;
        double qps;
        long durationSeconds;
    }

    /**
     * 慢请求
     */
    private static class SlowRequest {
        String ruleName;
        long responseTime;
        boolean success;

        SlowRequest(String ruleName, long responseTime, boolean success) {
            this.ruleName = ruleName;
            this.responseTime = responseTime;
            this.success = success;
        }
    }
}
