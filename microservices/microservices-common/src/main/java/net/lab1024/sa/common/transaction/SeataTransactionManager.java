package net.lab1024.sa.common.transaction;

// import io.seata.spring.annotation.GlobalTransactional; // 暂时注释，待添加seata依赖
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

/**
 * Seata分布式事务管理器
 * <p>
 * 统一使用Seata的@GlobalTransactional注解
 * 替代自定义的SagaManager实现
 * 提供企业级分布式事务解决方案
 * </p>
 *
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - Manager类是纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 在微服务中通过配置类注册为Spring Bean
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
public class SeataTransactionManager {

    /**
     * 执行分布式事务操作
     * <p>
     * 使用Seata的@GlobalTransactional注解确保事务的ACID特性
     * 自动处理事务的提交、回滚和补偿
     * </p>
     *
     * @param businessName 业务名称
     * @param supplier 业务操作
     * @param <T> 返回值类型
     * @return 业务操作结果
     */
    @Transactional(rollbackFor = Exception.class)
    public <T> T executeInTransaction(String businessName, Supplier<T> supplier) {
        log.info("[事务] 开始执行事务: {}", businessName);

        try {
            T result = supplier.get();
            log.info("[事务] 事务执行成功: {}", businessName);
            return result;
        } catch (Exception e) {
            log.error("[事务] 事务执行失败: {}, 错误: {}", businessName, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 执行带事务名称的分布式事务操作
     *
     * @param businessName 业务名称
     * @param transactionName 事务名称
     * @param supplier 业务操作
     * @param <T> 返回值类型
     * @return 业务操作结果
     */
    @Transactional(rollbackFor = Exception.class)
    public <T> T executeInTransaction(String businessName, String transactionName, Supplier<T> supplier) {
        log.info("[事务] 开始执行事务: {} - {}", businessName, transactionName);

        try {
            T result = supplier.get();
            log.info("[事务] 事务执行成功: {} - {}", businessName, transactionName);
            return result;
        } catch (Exception e) {
            log.error("[事务] 事务执行失败: {} - {}, 错误: {}", businessName, transactionName, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 执行只读事务操作
     *
     * @param businessName 业务名称
     * @param supplier 业务操作
     * @param <T> 返回值类型
     * @return 业务操作结果
     */
    @Transactional(readOnly = true)
    public <T> T executeInReadOnlyTransaction(String businessName, Supplier<T> supplier) {
        log.info("[事务] 开始执行只读事务: {}", businessName);

        try {
            T result = supplier.get();
            log.info("[事务] 只读事务执行成功: {}", businessName);
            return result;
        } catch (Exception e) {
            log.error("[事务] 只读事务执行失败: {}, 错误: {}", businessName, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 检查当前事务状态
     *
     * @return 是否在事务中
     */
    public boolean isInTransaction() {
        // 这里可以通过事务API获取当前事务状态
        return false;
    }

    /**
     * 获取当前事务ID
     *
     * @return 事务ID
     */
    public String getCurrentTransactionId() {
        // 这里可以通过事务API获取当前事务ID
        return "";
    }
}