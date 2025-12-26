package net.lab1024.sa.biometric.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.lab1024.sa.biometric.dao.BiometricTemplateDao;
import net.lab1024.sa.biometric.manager.BiometricTemplateManager;
import net.lab1024.sa.biometric.service.BiometricFeatureExtractionService;
import net.lab1024.sa.biometric.service.BiometricTemplateSyncService;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.client.RestTemplate;

/**
 * 生物识别服务测试配置类
 * <p>
 * 提供最小化的测试上下文，避免加载完整的Spring应用上下文。
 * 解决测试环境ApplicationContext加载失败问题。
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@TestConfiguration
public class BiometricTestConfiguration {

    /**
     * RestTemplate（测试环境）
     */
    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Redis模板（测试环境使用嵌入式Redis）
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Jackson ObjectMapper
     */
    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        return builder.createXmlMapper(false).build();
    }

    /**
     * Gateway服务客户端（测试环境）
     */
    @Bean
    @ConditionalOnMissingBean
    public GatewayServiceClient gatewayServiceClient(
            RestTemplate restTemplate,
            ObjectMapper objectMapper
    ) {
        // 测试环境使用默认的gateway URL
        String gatewayUrl = "http://localhost:8080";
        return new GatewayServiceClient(restTemplate, objectMapper, gatewayUrl);
    }
}
