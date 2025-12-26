package net.lab1024.sa.consume.config;

import net.lab1024.sa.consume.manager.ConsumeCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * 缓存测试配置
 * <p>
 * 提供测试环境专用的缓存配置
 * 使用 SimpleCacheManager，无需 Redis
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-25
 */
@Configuration
public class CacheTestConfiguration {

    /**
     * 创建测试专用的缓存管理器
     * 使用基于内存的 SimpleCacheManager
     */
    @Bean
    public CacheManager testCacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        // 创建消费分析相关的缓存
        cacheManager.setCaches(Arrays.asList(
                new ConcurrentMapCache("consume:analysis"),
                new ConcurrentMapCache("consume:trend"),
                new ConcurrentMapCache("consume:category"),
                new ConcurrentMapCache("consume:habits"),
                new ConcurrentMapCache("consume:recommendations")
        ));

        return cacheManager;
    }

    /**
     * 手动注册 ConsumeCacheManager
     * 避免扫描整个 manager 包导致其他 Manager 被加载
     */
    @Bean
    public ConsumeCacheManager consumeCacheManager(CacheManager cacheManager) {
        return new ConsumeCacheManager(cacheManager);
    }
}
