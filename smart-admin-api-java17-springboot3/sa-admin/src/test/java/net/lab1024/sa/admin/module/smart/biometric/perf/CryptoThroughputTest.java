package net.lab1024.sa.admin.module.smart.biometric.perf;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.smart.biometric.service.BiometricDataEncryptionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 简易并发性能测试（无外部依赖）：
 * - 并发执行加解密，计算吞吐与耗时
 */
@Slf4j
class CryptoThroughputTest {

    @Test
    void concurrentEncryptDecrypt() throws Exception {
        BiometricDataEncryptionService service = new BiometricDataEncryptionService();
        int threads = 16;
        int tasks = 200;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        long start = System.nanoTime();

        for (int i = 0; i < tasks; i++) {
            final int idx = i;
            futures.add(CompletableFuture.runAsync(() -> {
                var r = service.encryptPayload("payload-" + idx, 1L, 2L);
                String plain = service.decryptPayload(r.getKeyId(), r.getCipherText());
                Assertions.assertTrue(plain.startsWith("payload-"));
            }, pool));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
        long costMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        log.info("[CryptoThroughputTest] tasks={}, threads={}, costMs={}", tasks, threads, costMs);
    }
}

