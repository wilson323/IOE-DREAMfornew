package net.lab1024.sa.base.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 缓存注解配置类
 *
 * 支持多级缓存架构：
 * - @Cacheable: 查询操作缓存
 * - @CacheEvict: 更新操作缓存清除
 * - @CachePut: 更新操作缓存更新
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Slf4j
@Configuration
@EnableCaching
public class CacheAnnotationConfig {

    /**
     * Caffeine本地缓存管理器 (L1缓存)
     */
    @Bean("caffeineCacheManager")
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                // 最大缓存数量
                .maximumSize(1000)
                // 写入后10分钟过期
                .expireAfterWrite(10, TimeUnit.MINUTES)
                // 访问后5分钟过期
                .expireAfterAccess(5, TimeUnit.MINUTES)
                // 初始容量
                .initialCapacity(50)
                // 记录统计信息
                .recordStats());

        log.info("Caffeine缓存管理器初始化完成");
        return cacheManager;
    }

    /**
     * Redis分布式缓存管理器 (L2缓存)
     */
    @Bean("redisCacheManager")
    @Primary
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        // Redis缓存配置
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                // 设置缓存过期时间为30分钟
                .entryTtl(Duration.ofMinutes(30))
                // 禁用缓存前缀（使用RedisTemplate的key序列化器）
                .disableCachingNullValues()
                // 设置key序列化器
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new org.springframework.data.redis.serializer.StringRedisSerializer()))
                // 设置value序列化器
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                // 允许缓存null值
                .transactionAware()
                .build();

        log.info("Redis缓存管理器初始化完成");
        return cacheManager;
    }

    /**
     * 创建自定义缓存管理器选择器
     * 根据缓存名称选择使用L1或L2缓存
     */
    @Bean
    public CacheManagerSelector cacheManagerSelector(
            org.springframework.cache.CacheManager caffeineCacheManager,
            org.springframework.cache.CacheManager redisCacheManager) {
        return new CacheManagerSelector(caffeineCacheManager, redisCacheManager);
    }

    /**
     * 缓存管理器选择器
     */
    public static class CacheManagerSelector {
        private final org.springframework.cache.CacheManager l1CacheManager;
        private final org.springframework.cache.CacheManager l2CacheManager;

        public CacheManagerSelector(org.springframework.cache.CacheManager l1CacheManager,
                                   org.springframework.cache.CacheManager l2CacheManager) {
            this.l1CacheManager = l1CacheManager;
            this.l2CacheManager = l2CacheManager;
        }

        /**
         * 根据缓存名称选择缓存管理器
         *
         * @param cacheName 缓存名称
         * @return 缓存管理器
         */
        public org.springframework.cache.CacheManager select(String cacheName) {
            // 根据缓存名称后缀决定使用哪种缓存
            if (cacheName.endsWith(":L1")) {
                return l1CacheManager;
            } else if (cacheName.endsWith(":L2")) {
                return l2CacheManager;
            } else {
                // 默认使用L2缓存
                return l2CacheManager;
            }
        }

        /**
         * 获取L1缓存管理器
         */
        public org.springframework.cache.CacheManager getL1CacheManager() {
            return l1CacheManager;
        }

        /**
         * 获取L2缓存管理器
         */
        public org.springframework.cache.CacheManager getL2CacheManager() {
            return l2CacheManager;
        }
    }
}