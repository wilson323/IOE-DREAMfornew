package net.lab1024.sa.consume.service.consistency;

import net.lab1024.sa.consume.manager.DataConsistencyManager;

/**
 * 一致性验证器接口
 * 用于验证数据一致性保障机制
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
public interface ConsistencyValidator {

    /**
     * 验证所有一致性机制
     *
     * @return 是否全部通过
     */
    boolean validateAllConsistencyMechanisms();

    /**
     * 验证分布式锁机制
     *
     * @return 是否通过
     */
    boolean validateDistributedLock();

    /**
     * 验证数据版本控制机制
     *
     * @return 是否通过
     */
    boolean validateVersionControl();

    /**
     * 验证事务性操作机制
     *
     * @return 是否通过
     */
    boolean validateTransactionalOperation();

    /**
     * 验证并发安全性
     *
     * @return 是否通过
     */
    boolean validateConcurrentSafety();

    /**
     * 检查系统一致性状态
     *
     * @return 一致性检查结果
     */
    DataConsistencyManager.ConsistencyCheckResult checkSystemConsistency();
}

