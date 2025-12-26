package net.lab1024.sa.common.transaction;

import lombok.extern.slf4j.Slf4j;

/**
 * Seata分布式事务管理器
 * <p>
 * 提供分布式事务的辅助方法
 * 注意：纯Java类，不使用Spring注解，符合microservices-common-core定位
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Slf4j
public class SeataTransactionManager {

    /**
     * 开启全局事务（示例方法）
     */
    public void begin() {
        log.info("[Seata] 开启全局事务");
        // 实际逻辑会调用 Seata API
    }

    /**
     * 提交全局事务
     */
    public void commit() {
        log.info("[Seata] 提交全局事务");
    }

    /**
     * 回滚全局事务
     */
    public void rollback() {
        log.warn("[Seata] 回滚全局事务");
    }
}
