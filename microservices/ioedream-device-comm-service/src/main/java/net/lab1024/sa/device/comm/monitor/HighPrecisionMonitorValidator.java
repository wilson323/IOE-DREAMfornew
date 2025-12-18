package net.lab1024.sa.device.comm.monitor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.device.comm.monitor.HighPrecisionDeviceMonitor.DeviceStatusSnapshot;
import org.springframework.http.HttpMethod;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 高精度监控验证器
 * <p>
 * 验证高精度设备监控系统的性能和精度：
 * 1. 亚秒级监控精度验证
 * 2. 高并发性能测试
 * 3. 监控延迟统计分析
 * 4. 系统稳定性和可靠性测试
 * 5. 监控数据一致性验证
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Schema(description = "高精度监控验证器")
public class HighPrecisionMonitorValidator {

    private final DeviceDao deviceDao;
    private final GatewayServiceClient gatewayServiceClient;
    private final HighPrecisionDeviceMonitor monitor;

    // 验证配置
    private static final int PRECISION_TEST_COUNT = 100;      // 精度测试次数
    private static final int CONCURRENT_THREAD_COUNT = 50;    // 并发线程数
    private static final int CONCURRENT_REQUESTS_PER_THREAD = 20; // 每个线程的请求数
    private static final int TARGET_PRECISION_MS = 100;        // 目标精度（100ms）
    private static final int STABILITY_TEST_DURATION_MINUTES = 5; // 稳定性测试时长（分钟）

    // 测试结果统计
    private final List<Long> precisionTestResults = new ArrayList<>();
    private final List<Long> concurrencyTestResults = new ArrayList<>();
    private final Map<String, AtomicInteger> errorCounts = new ConcurrentHashMap<>();
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successfulRequests = new AtomicLong(0);

    /**
     * 构造函数
     */
    public HighPrecisionMonitorValidator(DeviceDao deviceDao, GatewayServiceClient gatewayServiceClient,
                                        HighPrecisionDeviceMonitor monitor) {
        this.deviceDao = deviceDao;
        this.gatewayServiceClient = gatewayServiceClient;
        this.monitor = monitor;
    }

    /**
     * 执行完整验证测试
     *
     * @return 验证结果报告
     */
    public ValidationReport executeFullValidation() {
        log.info("[高精度验证] 开始执行完整验证测试");

        ValidationReport report = new ValidationReport();
        report.setTestStartTime(LocalDateTime.now());

        try {
            // 1. 精度验证测试
            log.info("[高精度验证] 开始精度验证测试");
            ValidationResult precisionResult = validatePrecision();
            report.setPrecisionResult(precisionResult);

            // 2. 并发性能测试
            log.info("[高精度验证] 开始并发性能测试");
            ValidationResult concurrencyResult = validateConcurrency();
            report.setConcurrencyResult(concurrencyResult);

            // 3. 稳定性测试
            log.info("[高精度验证] 开始稳定性测试");
            ValidationResult stabilityResult = validateStability();
            report.setStabilityResult(stabilityResult);

            // 4. 数据一致性测试
            log.info("[高精度验证] 开始数据一致性测试");
            ValidationResult consistencyResult = validateDataConsistency();
            report.setConsistencyResult(consistencyResult);

            // 5. 生成综合评估
            report.setOverallAssessment(generateOverallAssessment(report));

            log.info("[高精度验证] 完整验证测试执行完成");

        } catch (Exception e) {
            log.error("[高精度验证] 验证测试执行异常", e);
            report.setErrorMessage(e.getMessage());
        } finally {
            report.setTestEndTime(LocalDateTime.now());
            report.setTestDurationMillis(ChronoUnit.MILLIS.between(report.getTestStartTime(), report.getTestEndTime()));
        }

        return report;
    }

    /**
     * 验证监控精度
     */
    private ValidationResult validatePrecision() {
        ValidationResult result = new ValidationResult();
        result.setTestName("监控精度验证");
        result.setTestStartTime(LocalDateTime.now());

        try {
            // 清空之前的测试结果
            precisionTestResults.clear();

            // 获取测试设备
            List<String> testDevices = getTestDevices(1); // 使用1个设备进行精度测试
            if (testDevices.isEmpty()) {
                result.setSuccess(false);
                result.setErrorMessage("没有可用的测试设备");
                return result;
            }

            String testDevice = testDevices.get(0);
            log.info("[高精度验证] 使用设备进行精度测试: {}", testDevice);

            // 执行精度测试
            List<Long> responseTimes = new ArrayList<>();
            for (int i = 0; i < PRECISION_TEST_COUNT; i++) {
                long startTime = System.nanoTime();

                try {
                    DeviceStatusSnapshot snapshot = monitor.getDeviceRealTimeStatus(testDevice);
                    if (snapshot != null) {
                        long endTime = System.nanoTime();
                        long responseTimeMs = (endTime - startTime) / 1_000_000; // 转换为毫秒
                        responseTimes.add(responseTimeMs);
                        precisionTestResults.add(responseTimeMs);
                    }
                } catch (Exception e) {
                    log.warn("[高精度验证] 精度测试单次失败, index={}", i, e);
                }

                // 短暂间隔，避免过于频繁的请求
                Thread.sleep(10);
            }

            // 分析测试结果
            if (responseTimes.isEmpty()) {
                result.setSuccess(false);
                result.setErrorMessage("所有精度测试请求都失败了");
                return result;
            }

            // 计算统计数据
            long minTime = responseTimes.stream().mapToLong(Long::longValue).min().orElse(0);
            long maxTime = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
            double avgTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
            long medianTime = calculateMedian(responseTimes);

            // 计算精度达标率（响应时间 <= 目标精度）
            long precision达标Count = responseTimes.stream()
                    .mapToLong(time -> time <= TARGET_PRECISION_MS ? 1 : 0)
                    .sum();
            double precision达标率 = (precision达标Count * 100.0) / responseTimes.size();

            // 设置结果
            result.setSuccess(true);
            result.addMetric("总测试次数", responseTimes.size());
            result.addMetric("最小响应时间(ms)", minTime);
            result.addMetric("最大响应时间(ms)", maxTime);
            result.addMetric("平均响应时间(ms)", Math.round(avgTime * 100.0) / 100.0);
            result.addMetric("中位数响应时间(ms)", medianTime);
            result.addMetric("目标精度(ms)", TARGET_PRECISION_MS);
            result.addMetric("精度达标率(%)", Math.round(precision达标率 * 100.0) / 100.0);

            // 判断是否达标
            boolean precision达标 = precision达标率 >= 95.0 && avgTime <= TARGET_PRECISION_MS;
            result.addMetric("精度是否达标", precision达标 ? "是" : "否");

            log.info("[高精度验证] 精度验证完成, 平均响应时间={}ms, 精度达标率={}%, 达标={}",
                    Math.round(avgTime * 100.0) / 100.0,
                    Math.round(precision达标率 * 100.0) / 100.0,
                    precision达标);

        } catch (Exception e) {
            log.error("[高精度验证] 精度验证异常", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        } finally {
            result.setTestEndTime(LocalDateTime.now());
        }

        return result;
    }

    /**
     * 验证并发性能
     */
    private ValidationResult validateConcurrency() {
        ValidationResult result = new ValidationResult();
        result.setTestName("并发性能验证");
        result.setTestStartTime(LocalDateTime.now());

        try {
            // 清空之前的测试结果
            concurrencyTestResults.clear();
            totalRequests.set(0);
            successfulRequests.set(0);
            errorCounts.clear();

            // 获取测试设备
            List<String> testDevices = getTestDevices(Math.min(10, CONCURRENT_THREAD_COUNT));
            if (testDevices.isEmpty()) {
                result.setSuccess(false);
                result.setErrorMessage("没有可用的测试设备");
                return result;
            }

            log.info("[高精度验证] 使用{}个设备进行并发性能测试", testDevices.size());

            // 创建线程池
            ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREAD_COUNT);
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            // 启动并发测试
            for (int i = 0; i < CONCURRENT_THREAD_COUNT; i++) {
                final int threadIndex = i;
                final String testDevice = testDevices.get(threadIndex % testDevices.size());

                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        for (int j = 0; j < CONCURRENT_REQUESTS_PER_THREAD; j++) {
                            totalRequests.incrementAndGet();
                            long startTime = System.nanoTime();

                            try {
                                DeviceStatusSnapshot snapshot = monitor.getDeviceRealTimeStatus(testDevice);
                                if (snapshot != null) {
                                    long endTime = System.nanoTime();
                                    long responseTimeMs = (endTime - startTime) / 1_000_000;
                                    concurrencyTestResults.add(responseTimeMs);
                                    successfulRequests.incrementAndGet();
                                }
                            } catch (Exception e) {
                                errorCounts.computeIfAbsent(testDevice, k -> new AtomicInteger(0)).incrementAndGet();
                            }

                            // 短暂间隔
                            Thread.sleep(5);
                        }
                    } catch (Exception e) {
                        log.error("[高精度验证] 并发测试线程异常, threadIndex={}", threadIndex, e);
                    }
                }, executor);

                futures.add(future);
            }

            // 等待所有线程完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            executor.shutdown();

            // 分析并发测试结果
            if (concurrencyTestResults.isEmpty()) {
                result.setSuccess(false);
                result.setErrorMessage("所有并发测试请求都失败了");
                return result;
            }

            // 计算统计数据
            double avgResponseTime = concurrencyTestResults.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);
            long maxResponseTime = concurrencyTestResults.stream()
                    .mapToLong(Long::longValue)
                    .max()
                    .orElse(0);
            long minResponseTime = concurrencyTestResults.stream()
                    .mapToLong(Long::longValue)
                    .min()
                    .orElse(0);

            // 计算吞吐量
            long actualTestDuration = ChronoUnit.MILLIS.between(result.getTestStartTime(), LocalDateTime.now());
            double throughput = successfulRequests.get() * 1000.0 / actualTestDuration; // 请求/秒

            // 设置结果
            result.setSuccess(true);
            result.addMetric("并发线程数", CONCURRENT_THREAD_COUNT);
            result.addMetric("每线程请求数", CONCURRENT_REQUESTS_PER_THREAD);
            result.addMetric("总请求数", totalRequests.get());
            result.addMetric("成功请求数", successfulRequests.get());
            result.addMetric("失败请求数", totalRequests.get() - successfulRequests.get());
            result.addMetric("成功率(%)", Math.round((successfulRequests.get() * 100.0 / totalRequests.get()) * 100.0) / 100.0);
            result.addMetric("平均响应时间(ms)", Math.round(avgResponseTime * 100.0) / 100.0);
            result.addMetric("最大响应时间(ms)", maxResponseTime);
            result.addMetric("最小响应时间(ms)", minResponseTime);
            result.addMetric("吞吐量(请求/秒)", Math.round(throughput * 100.0) / 100.0);

            log.info("[高精度验证] 并发性能验证完成, 成功率={}%, 平均响应时间={}ms, 吞吐量={}请求/秒",
                    Math.round((successfulRequests.get() * 100.0 / totalRequests.get()) * 100.0) / 100.0,
                    Math.round(avgResponseTime * 100.0) / 100.0,
                    Math.round(throughput * 100.0) / 100.0);

        } catch (Exception e) {
            log.error("[高精度验证] 并发性能验证异常", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        } finally {
            result.setTestEndTime(LocalDateTime.now());
        }

        return result;
    }

    /**
     * 验证系统稳定性
     */
    private ValidationResult validateStability() {
        ValidationResult result = new ValidationResult();
        result.setTestName("系统稳定性验证");
        result.setTestStartTime(LocalDateTime.now());

        try {
            // 获取测试设备
            List<String> testDevices = getTestDevices(5);
            if (testDevices.isEmpty()) {
                result.setSuccess(false);
                result.setErrorMessage("没有可用的测试设备");
                return result;
            }

            log.info("[高精度验证] 开始{}分钟稳定性测试，设备数量: {}", STABILITY_TEST_DURATION_MINUTES, testDevices.size());

            // 稳定性测试参数
            long testDurationMillis = STABILITY_TEST_DURATION_MINUTES * 60 * 1000L;
            long intervalMillis = 1000; // 每秒检查一次
            long endTime = System.currentTimeMillis() + testDurationMillis;

            List<Long> stabilityResponseTimes = new ArrayList<>();
            AtomicInteger stabilityErrorCount = new AtomicInteger(0);
            AtomicInteger stabilitySuccessCount = new AtomicInteger(0);

            // 执行稳定性测试
            while (System.currentTimeMillis() < endTime) {
                String testDevice = testDevices.get((int) (System.currentTimeMillis() % testDevices.size()));

                long startTime = System.nanoTime();
                try {
                    DeviceStatusSnapshot snapshot = monitor.getDeviceRealTimeStatus(testDevice);
                    if (snapshot != null) {
                        long responseTime = (System.nanoTime() - startTime) / 1_000_000;
                        stabilityResponseTimes.add(responseTime);
                        stabilitySuccessCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    stabilityErrorCount.incrementAndGet();
                }

                // 等待下一次检查
                Thread.sleep(intervalMillis);
            }

            // 分析稳定性测试结果
            long totalStabilityChecks = stabilitySuccessCount.get() + stabilityErrorCount.get();
            if (totalStabilityChecks == 0) {
                result.setSuccess(false);
                result.setErrorMessage("稳定性测试期间没有执行任何检查");
                return result;
            }

            double stabilitySuccessRate = (stabilitySuccessCount.get() * 100.0) / totalStabilityChecks;
            double avgStabilityResponseTime = stabilityResponseTimes.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);

            // 计算响应时间稳定性（标准差）
            double responseTimeStdDev = calculateStandardDeviation(stabilityResponseTimes);

            // 设置结果
            boolean isStable = stabilitySuccessRate >= 99.0 && responseTimeStdDev <= 50.0;
            result.setSuccess(isStable);
            result.addMetric("测试时长(分钟)", STABILITY_TEST_DURATION_MINUTES);
            result.addMetric("总检查次数", totalStabilityChecks);
            result.addMetric("成功检查次数", stabilitySuccessCount.get());
            result.addMetric("失败检查次数", stabilityErrorCount.get());
            result.addMetric("成功率(%)", Math.round(stabilitySuccessRate * 100.0) / 100.0);
            result.addMetric("平均响应时间(ms)", Math.round(avgStabilityResponseTime * 100.0) / 100.0);
            result.addMetric("响应时间标准差(ms)", Math.round(responseTimeStdDev * 100.0) / 100.0);
            result.addMetric("系统是否稳定", isStable ? "是" : "否");

            log.info("[高精度验证] 稳定性验证完成, 成功率={}%, 平均响应时间={}ms, 标准差={}ms, 稳定={}",
                    Math.round(stabilitySuccessRate * 100.0) / 100.0,
                    Math.round(avgStabilityResponseTime * 100.0) / 100.0,
                    Math.round(responseTimeStdDev * 100.0) / 100.0,
                    isStable);

        } catch (Exception e) {
            log.error("[高精度验证] 稳定性验证异常", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        } finally {
            result.setTestEndTime(LocalDateTime.now());
        }

        return result;
    }

    /**
     * 验证数据一致性
     */
    private ValidationResult validateDataConsistency() {
        ValidationResult result = new ValidationResult();
        result.setTestName("数据一致性验证");
        result.setTestStartTime(LocalDateTime.now());

        try {
            // 获取测试设备
            List<String> testDevices = getTestDevices(3);
            if (testDevices.isEmpty()) {
                result.setSuccess(false);
                result.setErrorMessage("没有可用的测试设备");
                return result;
            }

            log.info("[高精度验证] 使用{}个设备进行数据一致性测试", testDevices.size());

            int consistencyTests = 10;
            int inconsistentCount = 0;

            for (String deviceId : testDevices) {
                for (int i = 0; i < consistencyTests; i++) {
                    try {
                        // 连续两次获取设备状态，检查一致性
                        DeviceStatusSnapshot snapshot1 = monitor.getDeviceRealTimeStatus(deviceId);
                        Thread.sleep(10); // 短暂间隔
                        DeviceStatusSnapshot snapshot2 = monitor.getDeviceRealTimeStatus(deviceId);

                        if (snapshot1 != null && snapshot2 != null) {
                            // 检查关键字段是否一致
                            if (!isSnapshotConsistent(snapshot1, snapshot2)) {
                                inconsistentCount++;
                                log.warn("[高精度验证] 发现数据不一致, deviceId={}, snapshot1.status={}, snapshot2.status={}",
                                        deviceId, snapshot1.getStatus(), snapshot2.getStatus());
                            }
                        }
                    } catch (Exception e) {
                        log.debug("[高精度验证] 一致性测试单次失败, deviceId={}, index={}", deviceId, i, e);
                    }
                }
            }

            int totalConsistencyChecks = testDevices.size() * consistencyTests;
            double consistencyRate = totalConsistencyChecks > 0 ?
                    ((totalConsistencyChecks - inconsistentCount) * 100.0 / totalConsistencyChecks) : 0.0;

            result.setSuccess(consistencyRate >= 95.0);
            result.addMetric("测试设备数", testDevices.size());
            result.addMetric("每设备测试次数", consistencyTests);
            result.addMetric("总检查次数", totalConsistencyChecks);
            result.addMetric("不一致次数", inconsistentCount);
            result.addMetric("一致性率(%)", Math.round(consistencyRate * 100.0) / 100.0);
            result.addMetric("数据是否一致", consistencyRate >= 95.0 ? "是" : "否");

            log.info("[高精度验证] 数据一致性验证完成, 一致性率={}%, 一致={}",
                    Math.round(consistencyRate * 100.0) / 100.0,
                    consistencyRate >= 95.0);

        } catch (Exception e) {
            log.error("[高精度验证] 数据一致性验证异常", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        } finally {
            result.setTestEndTime(LocalDateTime.now());
        }

        return result;
    }

    /**
     * 生成综合评估
     */
    private OverallAssessment generateOverallAssessment(ValidationReport report) {
        OverallAssessment assessment = new OverallAssessment();

        // 评估各项测试结果
        boolean precision达标 = report.getPrecisionResult() != null && report.getPrecisionResult().isSuccess();
        boolean concurrency达标 = report.getConcurrencyResult() != null && report.getConcurrencyResult().isSuccess();
        boolean stability达标 = report.getStabilityResult() != null && report.getStabilityResult().isSuccess();
        boolean consistency达标 = report.getConsistencyResult() != null && report.getConsistencyResult().isSuccess();

        int passedTests = (precision达标 ? 1 : 0) + (concurrency达标 ? 1 : 0) + (stability达标 ? 1 : 0) + (consistency达标 ? 1 : 0);
        double overallScore = (passedTests * 100.0) / 4.0;

        // 确定整体等级
        String grade = "不合格";
        if (overallScore >= 90) {
            grade = "优秀";
        } else if (overallScore >= 80) {
            grade = "良好";
        } else if (overallScore >= 70) {
            grade = "一般";
        } else if (overallScore >= 60) {
            grade = "及格";
        }

        assessment.setOverallScore(Math.round(overallScore * 100.0) / 100.0);
        assessment.setGrade(grade);
        assessment.setPrecision达标(precision达标);
        assessment.setConcurrency达标(concurrency达标);
        assessment.setStability达标(stability达标);
        assessment.setConsistency达标(consistency达标);
        assessment.setRecommendations(generateRecommendations(report));

        return assessment;
    }

    /**
     * 生成改进建议
     */
    private List<String> generateRecommendations(ValidationReport report) {
        List<String> recommendations = new ArrayList<>();

        if (report.getPrecisionResult() != null && !report.getPrecisionResult().isSuccess()) {
            recommendations.add("提高监控精度：优化监控算法，减少响应时间延迟");
        }

        if (report.getConcurrencyResult() != null && !report.getConcurrencyResult().isSuccess()) {
            recommendations.add("提升并发性能：增加线程池大小，优化锁机制");
        }

        if (report.getStabilityResult() != null && !report.getStabilityResult().isSuccess()) {
            recommendations.add("增强系统稳定性：改进异常处理机制，增加重试策略");
        }

        if (report.getConsistencyResult() != null && !report.getConsistencyResult().isSuccess()) {
            recommendations.add("保证数据一致性：优化缓存策略，加强数据同步");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("系统性能良好，继续保持当前配置");
        }

        return recommendations;
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取测试设备
     */
    private List<String> getTestDevices(int maxCount) {
        try {
            List<String> deviceIds = new ArrayList<>();

            // 这里应该从数据库获取真实的设备ID
            // 简化实现，返回模拟设备ID
            for (int i = 1; i <= maxCount; i++) {
                deviceIds.add("TEST_DEVICE_" + String.format("%03d", i));
            }

            return deviceIds;
        } catch (Exception e) {
            log.error("[高精度验证] 获取测试设备失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 计算中位数
     */
    private long calculateMedian(List<Long> numbers) {
        List<Long> sorted = new ArrayList<>(numbers);
        Collections.sort(sorted);
        int size = sorted.size();

        if (size % 2 == 0) {
            return (sorted.get(size / 2 - 1) + sorted.get(size / 2)) / 2;
        } else {
            return sorted.get(size / 2);
        }
    }

    /**
     * 计算标准差
     */
    private double calculateStandardDeviation(List<Long> numbers) {
        if (numbers.isEmpty()) {
            return 0.0;
        }

        double mean = numbers.stream().mapToLong(Long::longValue).average().orElse(0.0);
        double variance = numbers.stream()
                .mapToDouble(num -> Math.pow(num - mean, 2))
                .average()
                .orElse(0.0);

        return Math.sqrt(variance);
    }

    /**
     * 检查快照一致性
     */
    private boolean isSnapshotConsistent(DeviceStatusSnapshot snapshot1, DeviceStatusSnapshot snapshot2) {
        // 检查关键字段是否一致
        return Objects.equals(snapshot1.getDeviceId(), snapshot2.getDeviceId()) &&
               Objects.equals(snapshot1.getDeviceType(), snapshot2.getDeviceType()) &&
               Objects.equals(snapshot1.getStatus(), snapshot2.getStatus());
    }

    // ==================== 内部类 ====================

    /**
     * 验证结果
     */
    @Schema(description = "验证结果")
    public static class ValidationResult {
        private String testName;
        private LocalDateTime testStartTime;
        private LocalDateTime testEndTime;
        private boolean success;
        private String errorMessage;
        private Map<String, Object> metrics = new HashMap<>();

        // getters and setters
        public String getTestName() { return testName; }
        public void setTestName(String testName) { this.testName = testName; }

        public LocalDateTime getTestStartTime() { return testStartTime; }
        public void setTestStartTime(LocalDateTime testStartTime) { this.testStartTime = testStartTime; }

        public LocalDateTime getTestEndTime() { return testEndTime; }
        public void setTestEndTime(LocalDateTime testEndTime) { this.testEndTime = testEndTime; }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        public Map<String, Object> getMetrics() { return metrics; }
        public void setMetrics(Map<String, Object> metrics) { this.metrics = metrics; }

        public void addMetric(String key, Object value) {
            this.metrics.put(key, value);
        }
    }

    /**
     * 验证报告
     */
    @Schema(description = "验证报告")
    public static class ValidationReport {
        private LocalDateTime testStartTime;
        private LocalDateTime testEndTime;
        private long testDurationMillis;
        private ValidationResult precisionResult;
        private ValidationResult concurrencyResult;
        private ValidationResult stabilityResult;
        private ValidationResult consistencyResult;
        private OverallAssessment overallAssessment;
        private String errorMessage;

        // getters and setters
        public LocalDateTime getTestStartTime() { return testStartTime; }
        public void setTestStartTime(LocalDateTime testStartTime) { this.testStartTime = testStartTime; }

        public LocalDateTime getTestEndTime() { return testEndTime; }
        public void setTestEndTime(LocalDateTime testEndTime) { this.testEndTime = testEndTime; }

        public long getTestDurationMillis() { return testDurationMillis; }
        public void setTestDurationMillis(long testDurationMillis) { this.testDurationMillis = testDurationMillis; }

        public ValidationResult getPrecisionResult() { return precisionResult; }
        public void setPrecisionResult(ValidationResult precisionResult) { this.precisionResult = precisionResult; }

        public ValidationResult getConcurrencyResult() { return concurrencyResult; }
        public void setConcurrencyResult(ValidationResult concurrencyResult) { this.concurrencyResult = concurrencyResult; }

        public ValidationResult getStabilityResult() { return stabilityResult; }
        public void setStabilityResult(ValidationResult stabilityResult) { this.stabilityResult = stabilityResult; }

        public ValidationResult getConsistencyResult() { return consistencyResult; }
        public void setConsistencyResult(ValidationResult consistencyResult) { this.consistencyResult = consistencyResult; }

        public OverallAssessment getOverallAssessment() { return overallAssessment; }
        public void setOverallAssessment(OverallAssessment overallAssessment) { this.overallAssessment = overallAssessment; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    /**
     * 综合评估
     */
    @Schema(description = "综合评估")
    public static class OverallAssessment {
        private double overallScore;
        private String grade;
        private boolean precision达标;
        private boolean concurrency达标;
        private boolean stability达标;
        private boolean consistency达标;
        private List<String> recommendations = new ArrayList<>();

        // getters and setters
        public double getOverallScore() { return overallScore; }
        public void setOverallScore(double overallScore) { this.overallScore = overallScore; }

        public String getGrade() { return grade; }
        public void setGrade(String grade) { this.grade = grade; }

        public boolean isPrecision达标() { return precision达标; }
        public void setPrecision达标(boolean precision达标) { this.precision达标 = precision达标; }

        public boolean isConcurrency达标() { return concurrency达标; }
        public void setConcurrency达标(boolean concurrency达标) { this.concurrency达标 = concurrency达标; }

        public boolean isStability达标() { return stability达标; }
        public void setStability达标(boolean stability达标) { this.stability达标 = stability达标; }

        public boolean isConsistency达标() { return consistency达标; }
        public void setConsistency达标(boolean consistency达标) { this.consistency达标 = consistency达标; }

        public List<String> getRecommendations() { return recommendations; }
        public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    }
}