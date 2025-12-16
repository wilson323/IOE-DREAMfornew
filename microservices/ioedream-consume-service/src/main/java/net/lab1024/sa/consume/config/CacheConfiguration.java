package net.lab1024.sa.consume.config;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.cache.CacheService;
import net.lab1024.sa.common.cache.SpringCacheServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 缓存配置类
 * <p>
 * 用于注册consume-service中的CacheService Bean
 * 严格遵循CLAUDE.md规范：
 * - 通过配置类注册Bean
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Slf4j
@Configuration
@EnableCaching
public class CacheConfiguration {

    /**
     * 注册 RedisTemplate Bean
     * 确保RedisTemplate被正确初始化
     */
    @Bean
    @ConditionalOnMissingBean(RedisTemplate.class)
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        log.info("[CacheConfiguration] 初始化RedisTemplate");
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 设置序列化器
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer();

        // 设置key和value的序列化规则
        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(jsonRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setHashValueSerializer(jsonRedisSerializer);

        // 设置默认序列化器
        template.setDefaultSerializer(jsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 注册 Redis CacheManager Bean
     * 使用Redis作为分布式缓存
     */
    @Bean
    @ConditionalOnMissingBean(name = "redisCacheManager")
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        log.info("[CacheConfiguration] 初始化RedisCacheManager");
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(java.time.Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .transactionAware()
                .build();
    }

    /**
     * 注册 Caffeine CacheManager Bean
     * 使用Caffeine作为本地缓存
     */
    @Bean
    @ConditionalOnMissingBean(name = "caffeineCacheManager")
    public CaffeineCacheManager caffeineCacheManager() {
        log.info("[CacheConfiguration] 初始化CaffeineCacheManager");
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(com.github.benmanes.caffeine.cache.Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(java.time.Duration.ofMinutes(5))
                .recordStats());
        return cacheManager;
    }

    /**
     * 注册 CacheService Bean
     * 优先使用Redis CacheManager
     */
    @Bean
    @ConditionalOnMissingBean(CacheService.class)
    public CacheService cacheService(org.springframework.context.ApplicationContext applicationContext,
                                   RedisTemplate<String, Object> redisTemplate) {
        log.info("[CacheConfiguration] 初始化CacheService");
        CacheManager cacheManager;

        // 优先使用RedisCacheManager，如果没有则使用CaffeineCacheManager
        try {
            cacheManager = applicationContext.getBean("redisCacheManager", CacheManager.class);
            log.info("[CacheConfiguration] 使用RedisCacheManager");
        } catch (Exception e) {
            cacheManager = applicationContext.getBean(CacheManager.class);
            log.info("[CacheConfiguration] 使用默认CacheManager: {}", cacheManager.getClass().getSimpleName());
        }

        return new SpringCacheServiceImpl(cacheManager, redisTemplate);
    }
}
