package net.lab1024.sa.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.lab1024.sa.common.filter.DirectCallAuthFilter;

/**
 * 直连调用自动配置
 */
@Configuration
@EnableConfigurationProperties(DirectCallProperties.class)
public class DirectCallAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "ioedream.direct-call", name = "enabled", havingValue = "true")
    public DirectCallAuthFilter directCallAuthFilter(DirectCallProperties properties) {
        return new DirectCallAuthFilter(properties);
    }
}

