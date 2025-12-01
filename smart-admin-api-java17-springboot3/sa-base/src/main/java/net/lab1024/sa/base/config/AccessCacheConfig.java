package net.lab1024.sa.base.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

/**
 * 门禁系统缓存配置
 *
 * @author SmartAdmin Team
 * @date 2025/01/13
 */
@Slf4j
@Configuration
@EnableCaching
public class AccessCacheConfig {

    /**
     * 缓存管理器 - 主要使用Caffeine
     */
    @Bean
    @Primary
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats());

        log.info("Caffeine缓存管理器初始化完成");
        return cacheManager;
    }

    /**
     * 设备信息缓存
     */
    @Bean("deviceCache")
    public Cache<String, Object> deviceCache() {
        return Caffeine.newBuilder()
                .initialCapacity(50)
                .maximumSize(500)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

    /**
     * 权限信息缓存
     */
    @Bean("permissionCache")
    public Cache<String, Object> permissionCache() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

    /**
     * 访问记录缓存
     */
    @Bean("accessRecordCache")
    public Cache<String, Object> accessRecordCache() {
        return Caffeine.newBuilder()
                .initialCapacity(200)
                .maximumSize(2000)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .recordStats()
                .build();
    }

    /**
     * 设备状态缓存
     */
    @Bean("deviceStatusCache")
    public Cache<String, Object> deviceStatusCache() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(500)
                .expireAfterWrite(2, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

    /**
     * 报警信息缓存
     */
    @Bean("alarmCache")
    public Cache<String, Object> alarmCache() {
        return Caffeine.newBuilder()
                .initialCapacity(50)
                .maximumSize(200)
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

    /**
     * 统计数据缓存
     */
    @Bean("statisticsCache")
    public Cache<String, Object> statisticsCache() {
        return Caffeine.newBuilder()
                .initialCapacity(20)
                .maximumSize(100)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

    /**
     * 用户会话缓存
     */
    @Bean("userSessionCache")
    public Cache<String, Object> userSessionCache() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(500)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

    /**
     * 系统配置缓存
     */
    @Bean("systemConfigCache")
    public Cache<String, Object> systemConfigCache() {
        return Caffeine.newBuilder()
                .initialCapacity(20)
                .maximumSize(50)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .recordStats()
                .build();
    }

    /**
     * 临时数据缓存（短期缓存）
     */
    @Bean("temporaryCache")
    public Cache<String, Object> temporaryCache() {
        return Caffeine.newBuilder()
                .initialCapacity(50)
                .maximumSize(200)
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .recordStats()
                .build();
    }

    /**
     * 长期数据缓存（长期缓存）
     */
    @Bean("longTermCache")
    public Cache<String, Object> longTermCache() {
        return Caffeine.newBuilder()
                .initialCapacity(30)
                .maximumSize(100)
                .expireAfterWrite(4, TimeUnit.HOURS)
                .recordStats()
                .build();
    }
}