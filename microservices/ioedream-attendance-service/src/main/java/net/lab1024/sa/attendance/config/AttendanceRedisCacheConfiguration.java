package net.lab1024.sa.attendance.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

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

import lombok.extern.slf4j.Slf4j;

/**
 * 考勤服务Redis缓存配置
 * <p>
 * 配置多级缓存策略，优化考勤数据查询性能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Configuration
@EnableCaching
public class AttendanceRedisCacheConfiguration {


    /**
     * 缓存管理器配置
     * <p>
     * 针对不同业务场景配置不同的缓存过期时间：
     * - dashboard数据: 5分钟（实时性要求高）
     * - 排班数据: 30分钟（相对稳定）
     * - 班次数据: 1小时（基础数据，变化少）
     * - 统计数据: 10分钟（折中实时性和性能）
     * </p>
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        log.info("[Redis缓存] 初始化缓存管理器");

        // 默认缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // 默认10分钟过期
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues(); // 不缓存null值

        // 针对不同业务场景的缓存配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Dashboard缓存 - 5分钟过期（实时性要求高）
        cacheConfigurations.put("dashboard:overview", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("dashboard:personal", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("dashboard:department", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("dashboard:enterprise", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("dashboard:trend", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("dashboard:heatmap", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("dashboard:realtime", defaultConfig.entryTtl(Duration.ofMinutes(2))); // 实时数据2分钟

        // 排班数据缓存 - 30分钟过期
        cacheConfigurations.put("schedule:daily", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("schedule:template", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // 班次数据缓存 - 1小时过期（基础数据）
        cacheConfigurations.put("shift:info", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("shift:rules", defaultConfig.entryTtl(Duration.ofHours(1)));

        // 考勤记录缓存 - 15分钟过期
        cacheConfigurations.put("attendance:record", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put("attendance:daily", defaultConfig.entryTtl(Duration.ofMinutes(15)));

        // 统计数据缓存 - 10分钟过期
        cacheConfigurations.put("statistics:department", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put("statistics:enterprise", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // 用户信息缓存 - 30分钟过期
        cacheConfigurations.put("user:info", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // 设备信息缓存 - 20分钟过期
        cacheConfigurations.put("device:info", defaultConfig.entryTtl(Duration.ofMinutes(20)));
        cacheConfigurations.put("device:status", defaultConfig.entryTtl(Duration.ofMinutes(5))); // 设备状态5分钟

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware() // 支持事务
                .build();
    }
}
