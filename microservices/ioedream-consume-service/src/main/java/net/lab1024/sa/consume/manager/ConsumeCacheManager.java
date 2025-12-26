package net.lab1024.sa.consume.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 消费缓存管理器
 * <p>
 * 提供消费分析相关的缓存清除功能
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-24
 */
@Slf4j
@Component
public class ConsumeCacheManager {

    private final CacheManager cacheManager;

    public ConsumeCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * 清除指定用户的所有分析缓存
     *
     * @param userId 用户ID
     */
    public void evictUserAnalysisCache(Long userId) {
        if (userId == null) {
            log.warn("[缓存管理] userId为空，跳过缓存清除");
            return;
        }

        log.debug("[缓存管理] 清除用户分析缓存: userId={}", userId);

        // 清除各种分析缓存
        evictCacheByKeyPattern("consume:analysis", userId);
        evictCacheByKeyPattern("consume:trend", userId);
        evictCacheByKeyPattern("consume:category", userId);
        evictCacheByKeyPattern("consume:habits", userId);
        evictCacheByKeyPattern("consume:recommendations", userId);

        log.info("[缓存管理] 用户分析缓存清除完成: userId={}", userId);
    }

    /**
     * 根据缓存名称和用户ID清除缓存
     *
     * @param cacheName 缓存名称
     * @param userId    用户ID
     */
    private void evictCacheByKeyPattern(String cacheName, Long userId) {
        try {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                // 清除该用户的所有period缓存（week, month, quarter）
                List<String> periods = List.of("week", "month", "quarter");
                for (String period : periods) {
                    String key = userId + ":" + period;
                    cache.evictIfPresent(key);
                    log.debug("[缓存管理] 清除缓存: cache={}, key={}", cacheName, key);
                }
            }
        } catch (Exception e) {
            log.error("[缓存管理] 清除缓存失败: cache={}, userId={}", cacheName, userId, e);
        }
    }
}
