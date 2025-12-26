package net.lab1024.sa.common.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * 公共组件配置类
 * <p>
 * 注册纯Java类为Spring Bean，遵循CLAUDE.md架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-20
 */
@Configuration
public class CommonComponentsConfiguration {

    // 注意：ExceptionMetricsCollector已迁移到microservices-common-monitor模块
    //      StrategyFactory已迁移到microservices-common-business模块
    //      业务服务直接依赖对应的细粒度模块

    // 基础追踪工具配置 - 暂时注释，因为TracingUtils在细粒度模块中
    /*
    @Bean
    @ConditionalOnMissingBean(TracingUtils.class)
    public TracingUtils tracingUtils(Tracer tracer, Propagator propagator) {
        return new TracingUtils(tracer, propagator);
    }
    */

    // 暂时注释掉有依赖问题的组件，待后续修复
    /*
    @Bean
    @ConditionalOnMissingBean(BusinessMetricsCollector.class)
    public BusinessMetricsCollector businessMetricsCollector(MeterRegistry meterRegistry) {
        return new BusinessMetricsCollector(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(DatabasePerformanceOptimizer.class)
    public DatabasePerformanceOptimizer databasePerformanceOptimizer(DataSource dataSource, MeterRegistry meterRegistry) {
        return new DatabasePerformanceOptimizer(dataSource, meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(AdvancedCacheManager.class)
    public AdvancedCacheManager advancedCacheManager(org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate, MeterRegistry meterRegistry) {
        return new AdvancedCacheManager(redisTemplate, meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(CursorPaginationHelper.class)
    public CursorPaginationHelper cursorPaginationHelper(MeterRegistry meterRegistry) {
        return new CursorPaginationHelper(meterRegistry);
    }
    */
}
