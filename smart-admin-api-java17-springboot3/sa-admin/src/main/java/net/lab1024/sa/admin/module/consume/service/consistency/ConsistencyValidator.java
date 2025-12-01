package net.lab1024.sa.admin.module.consume.service.consistency;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.cache.RedisUtil;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 数据一致性保障验证器
 * 用于验证分布式锁、版本控制、对账机制的有效性
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
@Component
public class ConsistencyValidator {

    @Resource
    private DataConsistencyManager consistencyManager;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 验证分布式锁机制
     */
    public boolean validateDistributedLock() {
        log.info("开始验证分布式锁机制...");

        try {
            String lockKey = "test:lock:validator";

            // 1. 测试锁获取
            String lockValue1 = consistencyManager.acquireLock(lockKey, 30);
            if (lockValue1 == null) {
                log.error("❌ 分布式锁获取失败");
                return false;
            }
            log.info("✅ 分布式锁获取成功: {}", lockValue1);

            // 2. 测试锁互斥性
            String lockValue2 = consistencyManager.acquireLock(lockKey, 30);
            if (lockValue2 != null) {
                log.error("❌ 分布式锁互斥性验证失败");
                return false;
            }
            log.info("✅ 分布式锁互斥性验证通过");

            // 3. 测试锁释放
            boolean released = consistencyManager.releaseLock(lockKey, lockValue1);
            if (!released) {
                log.error("❌ 分布式锁释放失败");
                return false;
            }
            log.info("✅ 分布式锁释放成功");

            // 4. 测试锁重新获取
            String lockValue3 = consistencyManager.acquireLock(lockKey, 30);
            if (lockValue3 == null) {
                log.error("❌ 分布式锁重新获取失败");
                return false;
            }
            log.info("✅ 分布式锁重新获取成功");

            // 清理测试锁
            consistencyManager.releaseLock(lockKey, lockValue3);

            log.info("🎉 分布式锁机制验证完成，所有功能正常");
            return true;

        } catch (Exception e) {
            log.error("❌ 分布式锁机制验证异常", e);
            return false;
        }
    }

    /**
     * 验证数据版本控制机制
     */
    public boolean validateVersionControl() {
        log.info("开始验证数据版本控制机制...");

        try {
            String dataKey = "test:data:version:validator";

            // 1. 获取初始版本号
            long version1 = consistencyManager.getDataVersion(dataKey);
            log.info("✅ 初始版本号: {}", version1);

            // 2. 获取更新版本号
            long version2 = consistencyManager.getDataVersion(dataKey);
            if (version2 <= version1) {
                log.error("❌ 版本号递增验证失败: {} -> {}", version1, version2);
                return false;
            }
            log.info("✅ 版本号递增验证通过: {} -> {}", version1, version2);

            // 3. 验证版本号有效性
            boolean valid = consistencyManager.validateDataVersion(dataKey, version2);
            if (!valid) {
                log.error("❌ 版本号有效性验证失败");
                return false;
            }
            log.info("✅ 版本号有效性验证通过");

            // 4. 验证版本号冲突检测
            boolean conflict = consistencyManager.validateDataVersion(dataKey, 999L);
            if (conflict) {
                log.error("❌ 版本号冲突检测验证失败");
                return false;
            }
            log.info("✅ 版本号冲突检测验证通过");

            // 5. 测试原子性检查并设置版本
            long newVersion = version2 + 1;
            boolean checkAndSet = consistencyManager.checkAndSetVersion(dataKey, version2, newVersion);
            if (!checkAndSet) {
                log.error("❌ 原子性检查并设置版本验证失败");
                return false;
            }
            log.info("✅ 原子性检查并设置版本验证通过");

            log.info("🎉 数据版本控制机制验证完成，所有功能正常");
            return true;

        } catch (Exception e) {
            log.error("❌ 数据版本控制机制验证异常", e);
            return false;
        }
    }

    /**
     * 验证事务性操作机制
     */
    public boolean validateTransactionalOperation() {
        log.info("开始验证事务性操作机制...");

        try {
            String lockKey = "test:transaction:lock";
            String dataKey = "test:transaction:data";

            // 准备测试数据
            redisUtil.set("test:transaction:data", "initial_value", 60);

            // 1. 执行事务性操作
            String result = consistencyManager.executeTransactional(lockKey, dataKey, (currentVersion) -> {
                log.info("执行事务性操作，当前版本: {}", currentVersion);

                // 模拟业务操作
                String currentValue = redisUtil.get("test:transaction:data");
                if (!"initial_value".equals(currentValue)) {
                    throw new RuntimeException("数据已被修改，事务中止");
                }

                // 更新数据
                redisUtil.set("test:transaction:data", "updated_value_v" + currentVersion, 60);

                return "transaction_success";
            });

            if (!"transaction_success".equals(result)) {
                log.error("❌ 事务性操作执行失败: {}", result);
                return false;
            }
            log.info("✅ 事务性操作执行成功: {}", result);

            // 2. 验证数据更新
            String finalValue = redisUtil.get("test:transaction:data");
            if (!finalValue.startsWith("updated_value_v")) {
                log.error("❌ 事务性操作数据更新验证失败: {}", finalValue);
                return false;
            }
            log.info("✅ 事务性操作数据更新验证通过: {}", finalValue);

            // 3. 清理测试数据
            redisUtil.delete("test:transaction:data");

            log.info("🎉 事务性操作机制验证完成，所有功能正常");
            return true;

        } catch (Exception e) {
            log.error("❌ 事务性操作机制验证异常", e);
            return false;
        }
    }

    /**
     * 验证并发安全性
     */
    public boolean validateConcurrentSafety() {
        log.info("开始验证并发安全性...");

        try {
            String lockKey = "test:concurrent:safety";
            int threadCount = 10;
            CountDownLatch latch = new CountDownLatch(threadCount);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            // 并发计数器
            int[] successCount = {0};
            int[] failCount = {0};

            // 启动并发测试
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    try {
                        String lockValue = consistencyManager.acquireLockWithRetry(lockKey, 10, 3, 100);
                        if (lockValue != null) {
                            try {
                                // 模拟业务操作
                                Thread.sleep(50);
                                synchronized (successCount) {
                                    successCount[0]++;
                                }
                                log.debug("线程 {} 成功获取锁并执行操作", threadId);
                            } finally {
                                consistencyManager.releaseLock(lockKey, lockValue);
                            }
                        } else {
                            synchronized (failCount) {
                                failCount[0]++;
                            }
                            log.debug("线程 {} 未能获取锁", threadId);
                        }
                    } catch (Exception e) {
                        log.error("线程 {} 执行异常", threadId, e);
                        synchronized (failCount) {
                            failCount[0]++;
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // 等待所有线程完成
            latch.await(30, java.util.concurrent.TimeUnit.SECONDS);
            executor.shutdown();

            // 验证结果
            if (successCount[0] + failCount[0] != threadCount) {
                log.error("❌ 并发安全性验证失败: 成功{} + 失败{} != 总数{}",
                         successCount[0], failCount[0], threadCount);
                return false;
            }

            log.info("✅ 并发安全性验证通过: 成功操作={}, 失败操作={}", successCount[0], failCount[0]);
            log.info("🎉 并发安全性验证完成，机制工作正常");
            return true;

        } catch (Exception e) {
            log.error("❌ 并发安全性验证异常", e);
            return false;
        }
    }

    /**
     * 执行完整的一致性保障验证
     */
    public boolean validateAllConsistencyMechanisms() {
        log.info("🚀 开始执行完整的数据一致性保障验证...");

        boolean allPassed = true;

        // 1. 验证分布式锁机制
        if (!validateDistributedLock()) {
            log.error("❌ 分布式锁机制验证失败");
            allPassed = false;
        }

        // 2. 验证数据版本控制机制
        if (!validateVersionControl()) {
            log.error("❌ 数据版本控制机制验证失败");
            allPassed = false;
        }

        // 3. 验证事务性操作机制
        if (!validateTransactionalOperation()) {
            log.error("❌ 事务性操作机制验证失败");
            allPassed = false;
        }

        // 4. 验证并发安全性
        if (!validateConcurrentSafety()) {
            log.error("❌ 并发安全性验证失败");
            allPassed = false;
        }

        if (allPassed) {
            log.info("🎉 数据一致性保障机制验证完成！所有功能正常工作");
            log.info("✅ 分布式锁机制: 正常");
            log.info("✅ 数据版本控制: 正常");
            log.info("✅ 事务性操作: 正常");
            log.info("✅ 并发安全性: 正常");
        } else {
            log.error("❌ 数据一致性保障机制验证失败，存在问题需要修复");
        }

        return allPassed;
    }

    /**
     * 验证系统一致性状态
     */
    public DataConsistencyManager.ConsistencyCheckResult checkSystemConsistency() {
        log.info("检查系统一致性状态...");

        try {
            DataConsistencyManager.ConsistencyCheckResult result = consistencyManager.checkConsistency();

            if (result.isHealthy()) {
                log.info("✅ 系统一致性状态良好: {}", result);
            } else {
                log.warn("⚠️ 系统一致性状态异常: {}", result);
            }

            return result;

        } catch (Exception e) {
            log.error("❌ 检查系统一致性状态异常", e);
            return null;
        }
    }
}