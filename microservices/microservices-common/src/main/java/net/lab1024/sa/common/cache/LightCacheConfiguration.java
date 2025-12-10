package net.lab1024.sa.common.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * 轻量级缓存配置
 * <p>
 * 避免过度复杂的缓存策略，只提供核心功能：
 * - Redis缓存支持
 * - 合理的默认过期时间
 * - JSON序列化
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.enabled", havingValue = "true", matchIfMissing = true)
public class LightCacheConfiguration {

    /**
     * Redis缓存管理器
     */
    @Bean
    @SuppressWarnings("null")
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 默认缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // 默认30分钟过期
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues(); // 不缓存null值

        // 特殊缓存配置
        RedisCacheConfiguration shortTermConfig = defaultConfig.entryTtl(Duration.ofMinutes(5)); // 5分钟
        RedisCacheConfiguration longTermConfig = defaultConfig.entryTtl(Duration.ofHours(2)); // 2小时

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .transactionAware() // 支持事务
                .withCacheConfiguration("user", shortTermConfig) // 用户信息缓存5分钟
                .withCacheConfiguration("dict", longTermConfig)   // 字典信息缓存2小时
                .build();
    }

    /**
     * 轻量级缓存工具类
     */
    public static class LightCacheUtil {

        /**
         * 简单的缓存键生成
         */
        public static String buildKey(String prefix, Object... params) {
            StringBuilder key = new StringBuilder(prefix);
            for (Object param : params) {
                key.append(":").append(param);
            }
            return key.toString();
        }

        /**
         * 生成用户缓存键
         */
        public static String userKey(Long userId) {
            return buildKey("user", userId);
        }

        /**
         * 生成字典缓存键
         */
        public static String dictKey(String typeCode) {
            return buildKey("dict", typeCode);
        }

        /**
         * 生成权限缓存键
         */
        public static String permissionKey(Long userId) {
            return buildKey("permission", userId);
        }
    }
}
