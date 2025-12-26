package net.lab1024.sa.video.config;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.extern.slf4j.Slf4j;

/**
 * 事件缓存配置
 * <p>
 * 配置Caffeine缓存用于WebSocket事件推送
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Slf4j
@Configuration
@EnableCaching
public class EventCacheConfig {

    /**
     * 事件缓存管理器
     * <p>
     * 使用Caffeine本地缓存，配置两个缓存区域：
     * - video-events: 全局视频事件缓存
     * - device-events: 按设备分类的事件缓存
     * </p>
     *
     * @return CacheManager
     */
    @Bean(name = "eventCacheManager")
    public CacheManager eventCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // 配置全局事件缓存
        cacheManager.registerCustomCache("video-events",
                Caffeine.newBuilder()
                        .maximumSize(100)
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .build()
        );

        // 配置设备事件缓存
        cacheManager.registerCustomCache("device-events",
                Caffeine.newBuilder()
                        .maximumSize(1000)
                        .expireAfterWrite(10, TimeUnit.MINUTES)
                        .build()
        );

        log.info("[事件缓存] 事件缓存管理器已配置: video-events(100条,5分钟), device-events(1000条,10分钟)");

        return cacheManager;
    }
}
