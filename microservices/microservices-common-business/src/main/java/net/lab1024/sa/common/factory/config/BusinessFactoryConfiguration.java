package net.lab1024.sa.common.factory.config;

import net.lab1024.sa.common.factory.StrategyFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 业务工厂配置类
 * <p>
 * 注册策略工厂Bean，遵循CLAUDE.md架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 * @updated 从microservices-common迁移到细粒度模块
 */
@Configuration
public class BusinessFactoryConfiguration {

    @Bean
    @ConditionalOnMissingBean(StrategyFactory.class)
    @SuppressWarnings("rawtypes")
    public StrategyFactory strategyFactory(org.springframework.context.ApplicationContext applicationContext) {
        return new StrategyFactory<Object>(applicationContext);
    }
}