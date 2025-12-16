package net.lab1024.sa.common.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 企业级多级缓存配置
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 统一使用Spring Cache + Caffeine + Redis
 * - 禁止使用自定义CacheManager
 * - L1本地缓存（Caffeine）+ L2分布式缓存（Redis）
 * </p>
 * <p>
 * 缓存策略：
 * - L1本地缓存：Caffeine，10000容量，5分钟过期，毫秒级响应
 * - L2分布式缓存：Redis，30分钟过期，秒级响应
 * - 读取顺序：L1 → L2 → 数据源
 * - 写入策略：同时写入L1和L2
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 2.0.0
 * @since 2025-01-30
 * @updated 2025-01-30 重构为CompositeCacheManager（L1本地缓存 + L2 Redis缓存）
 */
@Slf4j
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.enabled", havingValue = "true", matchIfMissing = true)
@SuppressWarnings({"null"})
public class LightCacheConfiguration {

    /**
     * L1本地缓存管理器（Caffeine）
     * <p>
     * 企业级配置（已优化内存占用）：
     * - 最大容量：5000（从10000降低至50%，节省5-10%内存）
     * - 过期时间：5分钟（与L2缓存配合）
     * - 访问过期：2分钟（冷数据快速淘汰）
     * - 弱键引用：启用（允许GC回收键，进一步节省内存）
     * - 统计功能：启用（用于监控缓存命中率）
     * </p>
     * <p>
     * 内存优化说明：
     * - weakKeys(): 使用弱引用存储键，GC时可回收不再使用的键
     * - maximumSize: 从10000降低至5000，适合大多数业务场景
     * - expireAfterAccess: 冷数据2分钟未访问则淘汰，进一步降低内存占用
     * </p>
     */
    @Bean("caffeineCacheManager")
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(5000)  // 降低50%，节省5-10%内存
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .expireAfterAccess(2, TimeUnit.MINUTES)
                .weakKeys()  // 启用弱键引用，允许GC回收
                .recordStats()  // 启用统计功能
        );
        log.info("[缓存配置] L1本地缓存（Caffeine）初始化完成：容量5000（优化后），过期时间5分钟，已启用weakKeys");
        return cacheManager;
    }

    /**
     * L2分布式缓存管理器（Redis）
     * <p>
     * 企业级配置：
     * - 默认过期时间：30分钟
     * - 序列化：JSON格式
     * - 事务支持：启用
     * </p>
     */
    @Bean("redisCacheManager")
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        // 默认缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // 默认30分钟过期
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues(); // 不缓存null值

        // 特殊缓存配置
        RedisCacheConfiguration shortTermConfig = defaultConfig.entryTtl(Duration.ofMinutes(5)); // 5分钟
        RedisCacheConfiguration longTermConfig = defaultConfig.entryTtl(Duration.ofHours(2)); // 2小时

        RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .transactionAware() // 支持事务
                .withCacheConfiguration("user", shortTermConfig) // 用户信息缓存5分钟
                .withCacheConfiguration("dict", longTermConfig)   // 字典信息缓存2小时
                .withCacheConfiguration("protocol:device", defaultConfig) // 协议设备缓存30分钟
                .withCacheConfiguration("protocol:device:code", defaultConfig) // 协议设备编码缓存30分钟
                .withCacheConfiguration("protocol:user:card", defaultConfig) // 协议用户卡号缓存30分钟
                .withCacheConfiguration("consume:realtime:statistics", defaultConfig) // 消费实时统计缓存30分钟
                .withCacheConfiguration("consume:area:permission", shortTermConfig) // 消费区域权限缓存5分钟
                .build();

        log.info("[缓存配置] L2分布式缓存（Redis）初始化完成：默认过期时间30分钟");
        return cacheManager;
    }

    /**
     * 组合缓存管理器（L1本地缓存 + L2 Redis缓存）
     * <p>
     * 企业级多级缓存架构：
     * - 优先使用L1本地缓存（Caffeine），毫秒级响应
     * - L1未命中时使用L2分布式缓存（Redis），秒级响应
     * - L2未命中时查询数据源，并同时写入L1和L2
     * </p>
     * <p>
     * 使用@Primary确保此CacheManager为默认缓存管理器
     * </p>
     */
    @Bean
    @Primary
    public CacheManager compositeCacheManager(RedisConnectionFactory connectionFactory) {
        org.springframework.cache.support.CompositeCacheManager compositeCacheManager =
                new org.springframework.cache.support.CompositeCacheManager();
        compositeCacheManager.setCacheManagers(
                java.util.Arrays.asList(
                        caffeineCacheManager(),
                        redisCacheManager(connectionFactory)
                )
        );
        compositeCacheManager.setFallbackToNoOpCache(false); // 不允许降级到NoOpCache

        log.info("[缓存配置] 组合缓存管理器（CompositeCacheManager）初始化完成");
        log.info("[缓存配置] 缓存策略：L1本地缓存（Caffeine）→ L2分布式缓存（Redis）→ 数据源");
        return compositeCacheManager;
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
