package net.lab1024.sa.common.config;

import lombok.extern.slf4j.Slf4j;


import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redisson配置类
 * <p>
 * 配置Redisson客户端，用于分布式锁和缓存
 * 严格遵循CLAUDE.md规范：
 * - 使用@Configuration注解
 * - 使用@Bean注册RedissonClient
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Configuration
@Slf4j
public class RedissonConfig {


    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.database:0}")
    private int redisDatabase;

    /**
     * 配置Redisson客户端
     * <p>
     * 用于分布式锁和缓存操作
     * </p>
     *
     * @return RedissonClient实例
     */
    @Bean
    public RedissonClient redissonClient() {
        try {
            Config config = new Config();

            // 单节点模式配置
            String address = "redis://" + redisHost + ":" + redisPort;
            config.useSingleServer()
                    .setAddress(address)
                    .setDatabase(redisDatabase)
                    .setConnectionPoolSize(10)
                    .setConnectionMinimumIdleSize(5)
                    .setConnectTimeout(3000)
                    .setTimeout(3000)
                    .setRetryAttempts(3)
                    .setRetryInterval(1500);

            if (redisPassword != null && !redisPassword.isEmpty()) {
                config.useSingleServer().setPassword(redisPassword);
            }

            RedissonClient redissonClient = Redisson.create(config);
            log.info("Redisson客户端配置成功，地址：{}，数据库：{}", address, redisDatabase);
            return redissonClient;
        } catch (Exception e) {
            log.error("Redisson客户端配置失败", e);
            return null;
        }
    }

    /**
     * 配置RedisTemplate<String, Object>
     * <p>
     * 用于缓存服务的通用Redis操作
     * </p>
     *
     * @param connectionFactory Redis连接工厂
     * @return RedisTemplate实例
     */
    @Bean
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        log.info("RedisTemplate<String, Object>配置成功");
        return template;
    }
}
