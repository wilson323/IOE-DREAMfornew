package net.lab1024.sa.admin.module.smart.biometric.engine;

import net.lab1024.sa.admin.module.smart.biometric.constant.BiometricTypeEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 并发认证压力测试(轻量化版本)。
 *
 * <p>利用轻量级的算法桩件模拟 200 次并发认证, 以验证统一引擎在
 * 1.5 秒内可以全部完成且全部成功, 满足 Phase4 的性能验收要求。</p>
 */
class ConcurrentAuthenticationLoadTest {

    private final BiometricRecognitionEngine engine;

    ConcurrentAuthenticationLoadTest() {
        BiometricRecognitionEngine.BiometricEngineConfig config = new BiometricRecognitionEngine.BiometricEngineConfig();
        config.setMaxConcurrentOperations(64);
        this.engine = new BiometricRecognitionEngine(config);
        this.engine.registerAlgorithm(new DeterministicAlgorithm());
    }

    @AfterEach
    void tearDown() {
        engine.shutdown();
    }

    @Test
    void shouldHandleTwoHundredConcurrentAuthenticationsUnderThreshold() {
        int totalRequests = 200;
        List<CompletableFuture<BiometricRecognitionEngine.BiometricAuthResult>> futures = new ArrayList<>();
        long start = System.nanoTime();
        for (int i = 0; i < totalRequests; i++) {
            BiometricRecognitionEngine.BiometricAuthRequest request =
                    buildRequest((long) i, ("payload-" + i).getBytes(StandardCharsets.UTF_8));
            futures.add(engine.authenticate(request));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        long durationMs = Duration.ofNanos(System.nanoTime() - start).toMillis();

        long success = futures.stream()
                .map(CompletableFuture::join)
                .filter(BiometricRecognitionEngine.BiometricAuthResult::isSuccess)
                .count();

        Assertions.assertEquals(totalRequests, success, "应全部认证成功");
        Assertions.assertTrue(durationMs < 1500,
                "批量认证应在1.5秒内完成, 实际耗时: " + durationMs + "ms");
    }

    private BiometricRecognitionEngine.BiometricAuthRequest buildRequest(Long userId, byte[] payload) {
        BiometricRecognitionEngine.BiometricAuthRequest request = new BiometricRecognitionEngine.BiometricAuthRequest();
        request.setUserId(userId);
        request.setDeviceId("DEV-LOAD");
        request.setBiometricType(BiometricTypeEnum.FACE);
        request.setBiometricData(payload);
        request.setTemplateId("TEMPLATE-" + userId);
        return request;
    }

    /**
     * 轻量化算法实现, 始终返回高置信度, 用于性能测试。
     */
    private static final class DeterministicAlgorithm implements BiometricAlgorithm {

        private final PerformanceMetrics metrics = new PerformanceMetrics();
        private final AtomicInteger totalCalls = new AtomicInteger();
        private AlgorithmStatus status = AlgorithmStatus.UNINITIALIZED;

        @Override
        public BiometricTypeEnum getAlgorithmType() {
            return BiometricTypeEnum.FACE;
        }

        @Override
        public boolean initialize(Map<String, Object> config) {
            status = AlgorithmStatus.READY;
            return true;
        }

        @Override
        public BiometricResult registerTemplate(Long userId, String deviceId, byte[] biometricData) {
            return successResult();
        }

        @Override
        public BiometricResult deleteTemplate(String templateId) {
            return successResult();
        }

        @Override
        public BiometricResult authenticate(Long userId, String deviceId, byte[] biometricData, String templateId) {
            totalCalls.incrementAndGet();
            simulateCpuLatency();
            return successResult();
        }

        @Override
        public BiometricBatchResult batchAuthenticate(Long userId, String deviceId, byte[][] biometricData) {
            BiometricBatchResult batchResult = new BiometricBatchResult();
            batchResult.setAllSuccess(true);
            batchResult.setSuccessCount(biometricData.length);
            batchResult.setFailureCount(0);
            return batchResult;
        }

        @Override
        public AlgorithmStatus getAlgorithmStatus() {
            return status;
        }

        @Override
        public PerformanceMetrics getPerformanceMetrics() {
            metrics.setTotalCalls(totalCalls.get());
            metrics.setSuccessCalls(totalCalls.get());
            metrics.setAvgProcessingTimeMs(5);
            return metrics;
        }

        @Override
        public void cleanup() {
            status = AlgorithmStatus.STOPPED;
        }

        private void simulateCpuLatency() {
            // 模拟 2-4ms 的随机耗时, 更贴近真实算法运行
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(2, 4));
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }

        private BiometricResult successResult() {
            return new BiometricResult(true, 0.98, 3, "OK", "TEMPLATE");
        }
    }
}

