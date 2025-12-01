package net.lab1024.sa.base.config;

import java.time.Duration;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Redisson本地配置 - 源源性解决Docker硬编码问题
 * 使用@Primary确保此配置优先级高于Redisson自动配置
 *
 * @author 老王
 * @date 2025-11-15
 */
@Configuration
public class LocalRedissonConfig {

    @Value("${spring.redis.host:127.0.0.1}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Value("${spring.redis.database:0}")
    private int redisDatabase;

    @Bean
    @Primary
    public RedissonClient redissonClient() {
        Config config = new Config();
        String redisAddress = "redis://" + redisHost + ":" + redisPort;

        config.useSingleServer()
                .setAddress(redisAddress)
                .setDatabase(redisDatabase)
                .setConnectTimeout(10000)
                .setTimeout(10000)
                .setRetryAttempts(3)
                .setRetryInterval((int) Duration.ofMillis(1500).toMillis())
                .setConnectionMinimumIdleSize(24)
                .setConnectionPoolSize(64);

        // 如果有密码，设置密码
        if (redisPassword != null && !redisPassword.trim().isEmpty()) {
            config.useSingleServer().setPassword(redisPassword);
        }

        return Redisson.create(config);
    }
}