package net.lab1024.sa.consume.manager;

/**
 * 数据一致性管理器接口
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
public interface DataConsistencyManager {

    /**
     * 确保消费数据一致性
     *
     * @param consumeId 消费ID
     * @return 是否一致
     */
    boolean ensureConsumeDataConsistency(Long consumeId);

    /**
     * 修复数据不一致问题
     *
     * @param consumeId 消费ID
     * @return 是否修复成功
     */
    boolean repairDataInconsistency(Long consumeId);

    /**
     * 验证交易完整性
     *
     * @param transactionId 交易ID
     * @return 是否完整
     */
    boolean validateTransactionIntegrity(String transactionId);

    /**
     * 同步分布式数据
     *
     * @param dataType 数据类型
     * @param dataId 数据ID
     * @return 是否同步成功
     */
    boolean syncDistributedData(String dataType, Long dataId);

    /**
     * 检查系统一致性状态
     *
     * @return 一致性检查结果
     */
    ConsistencyCheckResult checkConsistency();

    /**
     * 清理过期的锁和版本数据
     *
     * @return 清理结果
     */
    ConsistencyCleanupResult cleanupExpiredData();

    /**
     * 一致性检查结果
     */
    interface ConsistencyCheckResult {
        boolean isHealthy();

        int getActiveLocks();

        int getVersionEntries();

        boolean isRedisHealthy();

        String getErrorMessage();
    }

    /**
     * 一致性清理结果
     */
    interface ConsistencyCleanupResult {
        boolean isSuccess();

        int getCleanedLocks();

        int getCleanedVersions();

        String getErrorMessage();
    }
}