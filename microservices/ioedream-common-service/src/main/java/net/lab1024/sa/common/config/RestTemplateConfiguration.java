package net.lab1024.sa.common.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * RestTemplate配置类
 * <p>
 * 配置HTTP客户端RestTemplate
 * 严格遵循CLAUDE.md规范:
 * - 统一的HTTP客户端配置
 * - 合理的超时设置
 * - 完善的错误处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Configuration
public class RestTemplateConfiguration {

    /**
     * 配置RestTemplate Bean
     * <p>
     * 设置连接超时、读取超时等参数
     * </p>
     *
     * @param builder RestTemplateBuilder
     * @return RestTemplate实例
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        log.info("配置RestTemplate Bean");

        // Spring Boot 3.4.0+ 超时设置在requestFactory中配置，避免使用已弃用的方法
        return builder
                .requestFactory(this::httpRequestFactory)
                .build();
    }

    /**
     * 配置HTTP请求工厂
     *
     * @return ClientHttpRequestFactory
     */
    private ClientHttpRequestFactory httpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000); // 10秒
        factory.setReadTimeout(30000); // 30秒
        return factory;
    }
}
