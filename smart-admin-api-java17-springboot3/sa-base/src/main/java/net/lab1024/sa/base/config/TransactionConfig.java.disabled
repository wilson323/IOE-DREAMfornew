package net.lab1024.sa.base.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;

import java.util.Collections;

/**
 * 事务管理配置
 *
 * @author SmartAdmin Team
 * @date 2025/01/13
 */
@Configuration
@EnableTransactionManagement
public class TransactionConfig implements TransactionManagementConfigurer {

    /**
     * 配置事务管理器
     */
    @Override
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return null; // Spring Boot 会自动配置
    }

    /**
     * 配置事务拦截器
     */
    @Bean("smartTransactionInterceptor")
    public TransactionInterceptor transactionInterceptor(PlatformTransactionManager transactionManager) {
        // 定义事务属性
        RuleBasedTransactionAttribute transactionAttribute = new RuleBasedTransactionAttribute();

        // 设置事务传播行为
        transactionAttribute.setPropagationBehaviorName("PROPAGATION_REQUIRED");

        // 设置事务隔离级别
        transactionAttribute.setIsolationLevelName("ISOLATION_READ_COMMITTED");

        // 设置回滚规则 - 所有异常都回滚
        transactionAttribute.setRollbackRules(Collections.singletonList(new RollbackRuleAttribute(Exception.class)));

        // 设置超时时间
        transactionAttribute.setTimeout(30); // 30秒超时

        // 创建事务属性源
        NameMatchTransactionAttributeSource transactionAttributeSource =
            new NameMatchTransactionAttributeSource();
        transactionAttributeSource.addTransactionalMethod("*", transactionAttribute);

        // Spring Boot 3.x 使用新的构造函数
        return new TransactionInterceptor(transactionManager, transactionAttributeSource);
    }
}