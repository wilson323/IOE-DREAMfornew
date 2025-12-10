package net.lab1024.sa.common.transaction.saga;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SAGA分布式事务配置
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Configuration
public class SagaConfiguration {

    /**
     * SAGA管理器Bean
     *
     * @return SAGA管理器
     */
    @Bean
    public SagaManager sagaManager() {
        return new SagaManager();
    }
}