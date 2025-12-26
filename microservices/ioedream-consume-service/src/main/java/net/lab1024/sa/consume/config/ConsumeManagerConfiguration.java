package net.lab1024.sa.consume.config;

import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.dao.ConsumeAccountDao;
import net.lab1024.sa.consume.dao.ConsumeMealCategoryDao;
import net.lab1024.sa.consume.dao.ConsumeTransactionDao;
import net.lab1024.sa.consume.manager.ConsumeAccountManager;
import net.lab1024.sa.consume.manager.ConsumeDistributedLockManager;
import net.lab1024.sa.consume.manager.ConsumeMealCategoryManager;
import net.lab1024.sa.consume.manager.ConsumeTransactionManager;
import net.lab1024.sa.consume.monitor.ConsumeBusinessLogger;
import net.lab1024.sa.consume.monitor.ConsumeTransactionMonitor;

/**
 * 消费模块Manager配置类
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 负责将纯Java Manager类注册为Spring Bean
 * - 使用@ConditionalOnMissingBean避免重复注册
 * - 构造函数注入依赖，包含分布式锁支持
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Slf4j
@Configuration
public class ConsumeManagerConfiguration {

    /**
     * 注册消费分布式锁管理器
     * <p>
     * 仅在RedissonClient可用时创建（生产环境）
     * </p>
     *
     * @param redissonClient Redisson客户端
     * @return 分布式锁管理器Bean
     */
    @Bean
    @ConditionalOnBean(RedissonClient.class)
    @ConditionalOnMissingBean(ConsumeDistributedLockManager.class)
    public ConsumeDistributedLockManager consumeDistributedLockManager(RedissonClient redissonClient) {
        log.info("[配置] 注册消费分布式锁管理器Bean（生产模式）");
        return new ConsumeDistributedLockManager(redissonClient);
    }

    /**
     * 注册消费账户管理器（增强版，包含分布式锁）
     * <p>
     * 仅在分布式锁管理器可用时创建（生产环境）
     * </p>
     *
     * @param consumeAccountDao     账户数据访问对象
     * @param consumeTransactionDao 交易数据访问对象
     * @param lockManager           分布式锁管理器
     * @return 账户管理器Bean
     */
    @Bean
    @ConditionalOnBean(ConsumeDistributedLockManager.class)
    @ConditionalOnMissingBean(ConsumeAccountManager.class)
    public ConsumeAccountManager consumeAccountManagerWithLock(
            ConsumeAccountDao consumeAccountDao,
            ConsumeTransactionDao consumeTransactionDao,
            ConsumeDistributedLockManager lockManager) {

        log.info("[配置] 注册消费账户管理器Bean（生产模式，包含分布式锁）");
        return new ConsumeAccountManager(consumeAccountDao, consumeTransactionDao, lockManager);
    }

    /**
     * 注册消费账户管理器（基础版，不含分布式锁）
     * <p>
     * 测试环境专用，当分布式锁管理器不可用时创建
     * </p>
     *
     * @param consumeAccountDao     账户数据访问对象
     * @param consumeTransactionDao 交易数据访问对象
     * @return 账户管理器Bean
     */
    @Bean
    @ConditionalOnMissingBean({ConsumeDistributedLockManager.class, ConsumeAccountManager.class})
    public ConsumeAccountManager consumeAccountManagerNoLock(
            ConsumeAccountDao consumeAccountDao,
            ConsumeTransactionDao consumeTransactionDao) {

        log.info("[配置] 注册消费账户管理器Bean（测试模式，无分布式锁）");
        return new ConsumeAccountManager(consumeAccountDao, consumeTransactionDao, null);
    }

    /**
     * 注册消费交易管理器（包含完整事务管理和并发控制）
     * <p>
     * 仅在分布式锁管理器可用时创建（生产环境）
     * </p>
     *
     * @param consumeAccountDao     账户数据访问对象
     * @param consumeTransactionDao 交易数据访问对象
     * @param lockManager           分布式锁管理器
     * @param transactionMonitor     交易监控组件
     * @param businessLogger         业务日志记录器
     * @return 交易管理器Bean
     */
    @Bean
    @ConditionalOnBean(ConsumeDistributedLockManager.class)
    @ConditionalOnMissingBean(ConsumeTransactionManager.class)
    public ConsumeTransactionManager consumeTransactionManagerWithLock(
            ConsumeAccountDao consumeAccountDao,
            ConsumeTransactionDao consumeTransactionDao,
            ConsumeDistributedLockManager lockManager,
            ConsumeTransactionMonitor transactionMonitor,
            ConsumeBusinessLogger businessLogger) {

        log.info("[配置] 注册消费交易管理器Bean（生产模式，包含完整事务管理、并发控制、监控和日志）");
        return new ConsumeTransactionManager(consumeAccountDao, consumeTransactionDao, lockManager, transactionMonitor, businessLogger);
    }

    /**
     * 注册消费交易管理器（基础版，不含分布式锁）
     * <p>
     * 测试环境专用，当分布式锁管理器不可用时创建
     * </p>
     *
     * @param consumeAccountDao     账户数据访问对象
     * @param consumeTransactionDao 交易数据访问对象
     * @param transactionMonitor     交易监控组件
     * @param businessLogger         业务日志记录器
     * @return 交易管理器Bean
     */
    @Bean
    @ConditionalOnMissingBean({ConsumeDistributedLockManager.class, ConsumeTransactionManager.class})
    public ConsumeTransactionManager consumeTransactionManagerNoLock(
            ConsumeAccountDao consumeAccountDao,
            ConsumeTransactionDao consumeTransactionDao,
            ConsumeTransactionMonitor transactionMonitor,
            ConsumeBusinessLogger businessLogger) {

        log.info("[配置] 注册消费交易管理器Bean（测试模式，无分布式锁）");
        return new ConsumeTransactionManager(consumeAccountDao, consumeTransactionDao, null, transactionMonitor, businessLogger);
    }

    /**
     * 注册消费餐次分类管理器
     *
     * @param consumeMealCategoryDao 餐次分类数据访问对象
     * @param objectMapper JSON处理工具
     * @return 餐次分类管理器Bean
     */
    @Bean
    @ConditionalOnMissingBean(ConsumeMealCategoryManager.class)
    public ConsumeMealCategoryManager consumeMealCategoryManager(
            ConsumeMealCategoryDao consumeMealCategoryDao,
            ObjectMapper objectMapper) {

        log.info("[配置] 注册消费餐次分类管理器Bean");
        return new ConsumeMealCategoryManager(consumeMealCategoryDao, objectMapper);
    }
}