package net.lab1024.sa.video.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 视频服务缓存配置
 * <p>
 * 提供多级缓存策略：
 * 1. L1本地缓存（Caffeine）：热点数据，毫秒级响应
 * 2. L2 Redis缓存：分布式缓存，数据一致性
 * 3. L3 数据库：持久化存储
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Configuration
@EnableCaching
public class VideoCacheConfig {

    /**
     * Redis缓存管理器
     * <p>
     * L2级分布式缓存，用于存储：
     * - 设备信息缓存
     * - 用户会话信息
     * - 配置数据缓存
     * - 分析结果缓存
     * </p>
     *
     * @param connectionFactory Redis连接工厂
     * @return Redis缓存管理器
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        log.info("[RedisCacheManager] 初始化Redis缓存管理器");

        // 默认缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                // 缓存过期时间：30分钟
                .entryTtl(Duration.ofMinutes(30))
                // 禁用缓存空值
                .disableCachingNullValues()
                // 键序列化方式
                .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
                // 值序列化方式
                .serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        // 设备信息缓存配置（高频访问）
        RedisCacheConfiguration deviceConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60)) // 1小时
                .disableCachingNullValues()
                .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        // 视频流会话缓存配置（临时数据）
        RedisCacheConfiguration streamConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // 10分钟
                .disableCachingNullValues()
                .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        // AI分析结果缓存配置（分析结果，可长期缓存）
        RedisCacheConfiguration aiConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(2)) // 2小时
                .disableCachingNullValues()
                .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        // 用户权限缓存配置（安全相关，短时间缓存）
        RedisCacheConfiguration authConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5)) // 5分钟
                .disableCachingNullValues()
                .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration("video:device", deviceConfig)
                .withCacheConfiguration("video:stream", streamConfig)
                .withCacheConfiguration("video:ai", aiConfig)
                .withCacheConfiguration("video:auth", authConfig)
                .build();
    }

    /**
     * 设备信息本地缓存
     * <p>
     * L1级本地缓存，存储热点设备信息：
     * - 在线设备列表
     * - 设备基本信息
     * - 设备状态信息
     * </p>
     *
     * @return 设备信息本地缓存
     */
    @Bean("deviceInfoCache")
    @ConditionalOnMissingBean(name = "deviceInfoCache")
    public Cache<String, Object> deviceInfoCache() {
        log.info("[DeviceInfoCache] 初始化设备信息本地缓存");

        return Caffeine.newBuilder()
                // 初始容量：100
                .initialCapacity(100)
                // 最大容量：1000
                .maximumSize(1000)
                // 过期时间：5分钟
                .expireAfterWrite(Duration.ofMinutes(5))
                // 刷新时间：2分钟
                .refreshAfterWrite(Duration.ofMinutes(2))
                // 记录访问统计
                .recordStats()
                .build();
    }

    /**
     * 视频流会话本地缓存
     * <p>
     * L1级本地缓存，存储活跃视频流会话：
     * - RTMP流会话
     * - HLS流会话
     * - WebRTC会话
     * </p>
     *
     * @return 视频流会话本地缓存
     */
    @Bean("streamSessionCache")
    @ConditionalOnMissingBean(name = "streamSessionCache")
    public Cache<String, Object> streamSessionCache() {
        log.info("[StreamSessionCache] 初始化视频流会话本地缓存");

        return Caffeine.newBuilder()
                // 初始容量：50
                .initialCapacity(50)
                // 最大容量：500
                .maximumSize(500)
                // 过期时间：2分钟（视频流会话较短）
                .expireAfterWrite(Duration.ofMinutes(2))
                // 刷新时间：1分钟
                .refreshAfterWrite(Duration.ofMinutes(1))
                // 记录访问统计
                .recordStats()
                .build();
    }

    /**
     * AI分析模型本地缓存
     * <p>
     * L1级本地缓存，存储AI分析模型和配置：
     * - 人脸识别模型
     * - 行为检测模型
     * - 分析参数配置
     * </p>
     *
     * @return AI分析模型本地缓存
     */
    @Bean("aiModelCache")
    @ConditionalOnMissingBean(name = "aiModelCache")
    public Cache<String, Object> aiModelCache() {
        log.info("[AIModelCache] 初始化AI分析模型本地缓存");

        return Caffeine.newBuilder()
                // 初始容量：20
                .initialCapacity(20)
                // 最大容量：100
                .maximumSize(100)
                // 过期时间：1小时（模型配置相对稳定）
                .expireAfterWrite(Duration.ofHours(1))
                // 刷新时间：30分钟
                .refreshAfterWrite(Duration.ofMinutes(30))
                // 记录访问统计
                .recordStats()
                .build();
    }

    /**
     * 用户权限本地缓存
     * <p>
     * L1级本地缓存，存储用户权限信息：
     * - 用户角色权限
     * - 设备访问权限
     * - 功能操作权限
     * </p>
     *
     * @return 用户权限本地缓存
     */
    @Bean("userPermissionCache")
    @ConditionalOnMissingBean(name = "userPermissionCache")
    public Cache<String, Object> userPermissionCache() {
        log.info("[UserPermissionCache] 初始化用户权限本地缓存");

        return Caffeine.newBuilder()
                // 初始容量：200
                .initialCapacity(200)
                // 最大容量：2000
                .maximumSize(2000)
                // 过期时间：3分钟（权限变化较快）
                .expireAfterWrite(Duration.ofMinutes(3))
                // 刷新时间：1分钟
                .refreshAfterWrite(Duration.ofMinutes(1))
                // 记录访问统计
                .recordStats()
                .build();
    }

    /**
     * 系统配置本地缓存
     * <p>
     * L1级本地缓存，存储系统配置信息：
     * - 视频编码配置
     * - 存储路径配置
     * - AI分析配置
     * </p>
     *
     * @return 系统配置本地缓存
     */
    @Bean("systemConfigCache")
    @ConditionalOnMissingBean(name = "systemConfigCache")
    public Cache<String, Object> systemConfigCache() {
        log.info("[SystemConfigCache] 初始化系统配置本地缓存");

        return Caffeine.newBuilder()
                // 初始容量：50
                .initialCapacity(50)
                // 最大容量：200
                .maximumSize(200)
                // 过期时间：30分钟（配置相对稳定）
                .expireAfterWrite(Duration.ofMinutes(30))
                // 刷新时间：10分钟
                .refreshAfterWrite(Duration.ofMinutes(10))
                // 记录访问统计
                .recordStats()
                .build();
    }
}