package net.lab1024.sa.base.module.biometric.engine;

import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.Resource;
import net.lab1024.sa.base.module.device.entity.SmartDeviceEntity;
import net.lab1024.sa.base.module.biometric.entity.BiometricTemplateEntity;
import net.lab1024.sa.base.module.biometric.manager.BiometricCacheManager;
import net.lab1024.sa.base.common.device.DeviceAdapterInterface;
import net.lab1024.sa.base.common.device.DeviceDispatchResult;
import net.lab1024.sa.base.common.device.DeviceProtocolException;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 统一生物特征下发引擎
 * <p>
 * 负责统一管理和调度生物特征数据到各业务模块设备的下发工作
 * 确保生物特征数据的一致性和可靠性
 * 支持批量下发、异步处理、失败重试等功能
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
@Slf4j
public class UnifiedBiometricDispatchEngine {

    @Resource
    private BiometricCacheManager biometricCacheManager;

    @Resource
    private DeviceAdapterRegistry deviceAdapterRegistry;

    // 线程池用于异步下发
    private final ExecutorService dispatchExecutor;

    // 重试次数配置
    private static final int MAX_RETRY_COUNT = 3;

    // 批量下发批次大小
    private static final int BATCH_SIZE = 50;

    public UnifiedBiometricDispatchEngine() {
        this.dispatchExecutor = Executors.newFixedThreadPool(10);
    }

    /**
     * 统一生物特征下发请求
     */
    public static class BiometricDispatchRequest {
        private final Long personId;
        private final String personCode;
        private final String personName;
        private final List<BiometricTemplateEntity> biometricRecords;
        private final List<SmartDeviceEntity> targetDevices;
        private final Map<String, Object> dispatchOptions;
        private final String requestId;
        private final long timestamp;

        public BiometricDispatchRequest(Long personId, String personCode, String personName,
                                       List<BiometricTemplateEntity> biometricRecords,
                                       List<SmartDeviceEntity> targetDevices,
                                       Map<String, Object> dispatchOptions) {
            this.personId = personId;
            this.personCode = personCode;
            this.personName = personName;
            this.biometricRecords = new ArrayList<>(biometricRecords);
            this.targetDevices = new ArrayList<>(targetDevices);
            this.dispatchOptions = new HashMap<>(dispatchOptions != null ? dispatchOptions : new HashMap<>());
            this.requestId = UUID.randomUUID().toString();
            this.timestamp = System.currentTimeMillis();
        }

        // Getters
        public Long getPersonId() { return personId; }
        public String getPersonCode() { return personCode; }
        public String getPersonName() { return personName; }
        public List<BiometricTemplateEntity> getBiometricRecords() { return biometricRecords; }
        public List<SmartDeviceEntity> getTargetDevices() { return targetDevices; }
        public Map<String, Object> getDispatchOptions() { return dispatchOptions; }
        public String getRequestId() { return requestId; }
        public long getTimestamp() { return timestamp; }
    }

    /**
     * 生物特征下发结果
     */
    public static class BiometricDispatchResult {
        private final String requestId;
        private final Long personId;
        private final boolean success;
        private final String message;
        private final Map<String, DeviceDispatchResult> deviceResults;
        private final int successCount;
        private final int failureCount;
        private final long totalExecutionTime;
        private final long timestamp;

        public BiometricDispatchResult(String requestId, Long personId, boolean success, String message,
                                     Map<String, DeviceDispatchResult> deviceResults,
                                     int successCount, int failureCount, long totalExecutionTime) {
            this.requestId = requestId;
            this.personId = personId;
            this.success = success;
            this.message = message;
            this.deviceResults = new HashMap<>(deviceResults);
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.totalExecutionTime = totalExecutionTime;
            this.timestamp = System.currentTimeMillis();
        }

        // Getters
        public String getRequestId() { return requestId; }
        public Long getPersonId() { return personId; }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Map<String, DeviceDispatchResult> getDeviceResults() { return deviceResults; }
        public int getSuccessCount() { return successCount; }
        public int getFailureCount() { return failureCount; }
        public long getTotalExecutionTime() { return totalExecutionTime; }
        public long getTimestamp() { return timestamp; }
    }

    /**
     * 统一下发生物特征数据
     *
     * @param request 下发请求
     * @return 下发结果
     */
    public BiometricDispatchResult dispatchBiometricData(BiometricDispatchRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("开始统一生物特征下发: requestId={}, personId={}, personCode={}, 设备数={}, 生物特征数={}",
                request.getRequestId(), request.getPersonId(), request.getPersonCode(),
                request.getTargetDevices().size(), request.getBiometricRecords().size());

        try {
            // 1. 参数验证
            validateDispatchRequest(request);

            // 2. 获取生物特征数据
            Map<String, Object> unifiedBiometricData = buildUnifiedBiometricData(request);

            // 3. 执行下发
            Map<String, DeviceDispatchResult> deviceResults = new HashMap<>();
            int successCount = 0;
            int failureCount = 0;

            for (SmartDeviceEntity device : request.getTargetDevices()) {
                try {
                    // 获取设备适配器
                    DeviceAdapterInterface adapter = deviceAdapterRegistry.getAdapter(device);
                    if (adapter == null) {
                        log.warn("设备适配器不存在: deviceId={}, deviceType={}", device.getDeviceId(), device.getDeviceType());
                        deviceResults.put(device.getDeviceCode(), DeviceDispatchResult.deviceNotSupported(device.getDeviceType(), device.getManufacturer()));
                        failureCount++;
                        continue;
                    }

                    // 执行生物特征下发
                    DeviceDispatchResult result = adapter.dispatchBiometricData(device, unifiedBiometricData);
                    deviceResults.put(device.getDeviceCode(), result);

                    if (result.isSuccess()) {
                        successCount++;
                    } else {
                        failureCount++;
                        log.warn("生物特征下发失败: deviceId={}, error={}", device.getDeviceId(), result.getMessage());
                    }

                } catch (Exception e) {
                    log.error("生物特征下发异常: deviceId={}, personId={}", device.getDeviceId(), request.getPersonId(), e);
                    deviceResults.put(device.getDeviceCode(), DeviceDispatchResult.failure("下发异常: " + e.getMessage()));
                    failureCount++;
                }
            }

            // 4. 构建结果
            long executionTime = System.currentTimeMillis() - startTime;
            boolean overallSuccess = failureCount == 0;
            String message = overallSuccess ? "生物特征下发成功" : "部分设备下发失败";

            BiometricDispatchResult result = new BiometricDispatchResult(
                request.getRequestId(), request.getPersonId(), overallSuccess, message,
                deviceResults, successCount, failureCount, executionTime
            );

            log.info("生物特征下发完成: requestId={}, 成功数={}, 失败数={}, 耗时={}ms",
                    request.getRequestId(), successCount, failureCount, executionTime);

            return result;

        } catch (Exception e) {
            log.error("生物特征下发异常: requestId={}, personId={}", request.getRequestId(), request.getPersonId(), e);
            long executionTime = System.currentTimeMillis() - startTime;
            return new BiometricDispatchResult(
                request.getRequestId(), request.getPersonId(), false,
                "生物特征下发异常: " + e.getMessage(), new HashMap<>(), 0, 1, executionTime
            );
        }
    }

    /**
     * 异步下发生物特征数据
     *
     * @param request 下发请求
     * @return 异步结果
     */
    public CompletableFuture<BiometricDispatchResult> dispatchBiometricDataAsync(BiometricDispatchRequest request) {
        return CompletableFuture.supplyAsync(() -> dispatchBiometricData(request), dispatchExecutor)
                .exceptionally(throwable -> {
                    log.error("异步生物特征下发异常: requestId={}, personId={}", request.getRequestId(), request.getPersonId(), throwable);
                    return new BiometricDispatchResult(
                        request.getRequestId(), request.getPersonId(), false,
                        "异步下发异常: " + throwable.getMessage(), new HashMap<>(), 0, 1, 0L
                    );
                });
    }

    /**
     * 批量下发多人的生物特征数据
     *
     * @param requests 批量请求列表
     * @return 批量结果列表
     */
    public List<BiometricDispatchResult> batchDispatchBiometricData(List<BiometricDispatchRequest> requests) {
        log.info("开始批量生物特征下发: 请求数={}", requests.size());

        List<BiometricDispatchResult> results = new ArrayList<>();

        // 分批处理
        for (int i = 0; i < requests.size(); i += BATCH_SIZE) {
            int endIndex = Math.min(i + BATCH_SIZE, requests.size());
            List<BiometricDispatchRequest> batch = requests.subList(i, endIndex);

            log.debug("处理批次: start={}, end={}, size={}", i, endIndex, batch.size());

            // 并发处理批次内的请求
            List<CompletableFuture<BiometricDispatchResult>> futures = new ArrayList<>();
            for (BiometricDispatchRequest request : batch) {
                futures.add(dispatchBiometricDataAsync(request));
            }

            // 等待批次完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .join();

            // 收集结果
            for (CompletableFuture<BiometricDispatchResult> future : futures) {
                try {
                    results.add(future.get());
                } catch (Exception e) {
                    log.error("批量下发结果获取异常", e);
                }
            }
        }

        log.info("批量生物特征下发完成: 总请求数={}, 总结果数={}", requests.size(), results.size());
        return results;
    }

    /**
     * 重新下发失败的生物特征数据
     *
     * @param originalResult 原始下发结果
     * @param request 原始请求
     * @return 重新下发结果
     */
    public BiometricDispatchResult retryFailedDispatch(BiometricDispatchResult originalResult,
                                                      BiometricDispatchRequest request) {
        log.info("重新下发失败的生物特征数据: requestId={}, 失败设备数={}",
                originalResult.getRequestId(), originalResult.getFailureCount());

        // 筛选失败的设备
        List<SmartDeviceEntity> failedDevices = new ArrayList<>();
        for (SmartDeviceEntity device : request.getTargetDevices()) {
            DeviceDispatchResult deviceResult = originalResult.getDeviceResults().get(device.getDeviceCode());
            if (deviceResult != null && !deviceResult.isSuccess()) {
                failedDevices.add(device);
            }
        }

        if (failedDevices.isEmpty()) {
            log.info("没有失败的设备需要重新下发: requestId={}", originalResult.getRequestId());
            return originalResult;
        }

        // 创建重试请求
        BiometricDispatchRequest retryRequest = new BiometricDispatchRequest(
            request.getPersonId(), request.getPersonCode(), request.getPersonName(),
            request.getBiometricRecords(), failedDevices, request.getDispatchOptions()
        );

        // 执行重新下发
        BiometricDispatchResult retryResult = dispatchBiometricData(retryRequest);

        // 合并结果
        Map<String, DeviceDispatchResult> combinedResults = new HashMap<>(originalResult.getDeviceResults());
        combinedResults.putAll(retryResult.getDeviceResults());

        int totalSuccessCount = originalResult.getSuccessCount() + retryResult.getSuccessCount();
        int totalFailureCount = retryResult.getFailureCount(); // 重试后失败的设备数
        boolean overallSuccess = totalFailureCount == 0;

        String message = overallSuccess ? "重试后生物特征下发成功" : "重试后仍有设备下发失败";

        return new BiometricDispatchResult(
            originalResult.getRequestId(), originalResult.getPersonId(), overallSuccess, message,
            combinedResults, totalSuccessCount, totalFailureCount,
            originalResult.getTotalExecutionTime() + retryResult.getTotalExecutionTime()
        );
    }

    /**
     * 获取人员的统一生物特征数据
     *
     * @param personId 人员ID
     * @return 统一生物特征数据
     */
    public Map<String, Object> getUnifiedBiometricData(Long personId) {
        try {
            // 从缓存获取
            Map<String, Object> cachedData = biometricCacheManager.getUnifiedBiometricData(personId);
            if (cachedData != null) {
                return new HashMap<>(cachedData);
            }

            // 从数据库获取
            Map<String, Object> unifiedData = new HashMap<>();
            List<BiometricTemplateEntity> records = biometricCacheManager.getPersonTemplates(personId);

            if (records.isEmpty()) {
                log.warn("人员生物特征数据为空: personId={}", personId);
                return unifiedData;
            }

            // 按类型分组
            Map<String, List<BiometricTemplateEntity>> biometricByType = new HashMap<>();
            for (BiometricTemplateEntity record : records) {
                String type = record.getBiometricType();
                biometricByType.computeIfAbsent(type, k -> new ArrayList<>()).add(record);
            }

            // 构建统一数据结构
            unifiedData.put("personId", personId);
            unifiedData.put("timestamp", System.currentTimeMillis());
            unifiedData.put("biometricData", biometricByType);
            unifiedData.put("biometricCount", records.size());

            // 缓存结果
            biometricCacheManager.cacheUnifiedBiometricData(personId, unifiedData);

            return unifiedData;

        } catch (Exception e) {
            log.error("获取统一生物特征数据异常: personId={}", personId, e);
            return new HashMap<>();
        }
    }

    /**
     * 验证下发请求
     */
    private void validateDispatchRequest(BiometricDispatchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("下发请求不能为空");
        }

        if (request.getPersonId() == null) {
            throw new IllegalArgumentException("人员ID不能为空");
        }

        if (request.getBiometricRecords() == null || request.getBiometricRecords().isEmpty()) {
            throw new IllegalArgumentException("生物特征数据不能为空");
        }

        if (request.getTargetDevices() == null || request.getTargetDevices().isEmpty()) {
            throw new IllegalArgumentException("目标设备不能为空");
        }

        // 验证生物特征数据完整性
        for (BiometricTemplateEntity record : request.getBiometricRecords()) {
            if (record.getBiometricType() == null || record.getBiometricType().trim().isEmpty()) {
                throw new IllegalArgumentException("生物特征类型不能为空");
            }
            if (record.getTemplateData() == null || record.getTemplateData().trim().isEmpty()) {
                throw new IllegalArgumentException("生物特征数据不能为空");
            }
        }
    }

    /**
     * 构建统一生物特征数据
     */
    private Map<String, Object> buildUnifiedBiometricData(BiometricDispatchRequest request) {
        Map<String, Object> unifiedData = new HashMap<>();

        // 基础信息
        unifiedData.put("personId", request.getPersonId());
        unifiedData.put("personCode", request.getPersonCode());
        unifiedData.put("personName", request.getPersonName());
        unifiedData.put("timestamp", request.getTimestamp());
        unifiedData.put("requestId", request.getRequestId());

        // 生物特征数据
        Map<String, List<Map<String, Object>>> biometricData = new HashMap<>();
        for (BiometricTemplateEntity record : request.getBiometricRecords()) {
            Map<String, Object> biometricInfo = new HashMap<>();
            biometricInfo.put("id", record.getId());
            biometricInfo.put("biometricType", record.getBiometricType());
            biometricInfo.put("biometricData", record.getTemplateData());
            biometricInfo.put("templateIndex", record.getTemplateIndex());
            biometricInfo.put("quality", record.getQualityScore());
            biometricInfo.put("templateVersion", record.getTemplateVersion());
            biometricInfo.put("captureTime", record.getCaptureTime());
            biometricInfo.put("algorithmVersion", record.getAlgorithmVersion());

            String type = record.getBiometricType();
            biometricData.computeIfAbsent(type, k -> new ArrayList<>()).add(biometricInfo);
        }

        unifiedData.put("biometricData", biometricData);
        unifiedData.put("biometricCount", request.getBiometricRecords().size());

        // 下发选项
        unifiedData.putAll(request.getDispatchOptions());

        return unifiedData;
    }

    /**
     * 清理资源
     */
    public void shutdown() {
        try {
            log.info("关闭生物特征下发引擎...");
            dispatchExecutor.shutdown();
            if (!dispatchExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                dispatchExecutor.shutdownNow();
            }
            log.info("生物特征下发引擎已关闭");
        } catch (InterruptedException e) {
            log.error("关闭生物特征下发引擎异常", e);
            dispatchExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}