package net.lab1024.sa.consume.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.config.CacheTestConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 消费缓存管理器测试
 * <p>
 * 测试缓存清除功能：
 * - 清除用户分析缓存
 * - 验证缓存确实被清除
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-24
 */
@Slf4j
@DisplayName("消费缓存管理器测试")
@SpringBootTest(classes = CacheTestConfiguration.class)
class ConsumeCacheManagerTest {

    @Autowired
    private ConsumeCacheManager consumeCacheManager;

    @Autowired
    private CacheManager cacheManager;

    @Test
    @DisplayName("测试清除用户分析缓存")
    void testEvictUserAnalysisCache() {
        log.info("[测试] 开始测试清除用户分析缓存");

        // Given: 准备测试数据
        Long userId = 1L;

        // 先填充缓存
        populateUserCache(userId);

        // 验证缓存已填充
        assertTrue(isCachePresent("consume:analysis", userId, "week"), "分析缓存应该存在");
        assertTrue(isCachePresent("consume:trend", userId, "month"), "趋势缓存应该存在");

        // When: 清除用户缓存
        consumeCacheManager.evictUserAnalysisCache(userId);

        // Then: 验证缓存已清除
        assertFalse(isCachePresent("consume:analysis", userId, "week"), "分析缓存应该被清除");
        assertFalse(isCachePresent("consume:trend", userId, "month"), "趋势缓存应该被清除");
        assertFalse(isCachePresent("consume:category", userId, "quarter"), "分类缓存应该被清除");
        assertFalse(isCachePresent("consume:habits", userId, "week"), "习惯缓存应该被清除");
        assertFalse(isCachePresent("consume:recommendations", userId, "month"), "推荐缓存应该被清除");

        log.info("[测试] 用户分析缓存清除测试通过");
    }

    @Test
    @DisplayName("测试清除空用户ID缓存")
    void testEvictUserAnalysisCacheWithNullUserId() {
        log.info("[测试] 开始测试清除空用户ID缓存");

        // When: 传入null userId
        consumeCacheManager.evictUserAnalysisCache(null);

        // Then: 不应抛出异常
        assertDoesNotThrow(() -> consumeCacheManager.evictUserAnalysisCache(null));

        log.info("[测试] 空用户ID缓存清除测试通过");
    }

    @Test
    @DisplayName("测试清除不存在的用户缓存")
    void testEvictNonExistentUserCache() {
        log.info("[测试] 开始测试清除不存在的用户缓存");

        // Given: 一个不存在的用户ID
        Long nonExistentUserId = 99999L;

        // When & Then: 清除不存在的缓存不应抛出异常
        assertDoesNotThrow(() -> consumeCacheManager.evictUserAnalysisCache(nonExistentUserId));

        log.info("[测试] 不存在用户缓存清除测试通过");
    }

    @Test
    @DisplayName("测试清除多个用户的缓存")
    void testEvictMultipleUserCache() {
        log.info("[测试] 开始测试清除多个用户的缓存");

        // Given: 准备多个用户的缓存
        Long userId1 = 1L;
        Long userId2 = 2L;
        Long userId3 = 3L;

        populateUserCache(userId1);
        populateUserCache(userId2);
        populateUserCache(userId3);

        // 验证缓存已填充
        assertTrue(isCachePresent("consume:analysis", userId1, "week"));
        assertTrue(isCachePresent("consume:analysis", userId2, "week"));
        assertTrue(isCachePresent("consume:analysis", userId3, "week"));

        // When: 清除其中一个用户的缓存
        consumeCacheManager.evictUserAnalysisCache(userId2);

        // Then: 只有被清除用户的缓存应该被删除
        assertTrue(isCachePresent("consume:analysis", userId1, "week"), "用户1的缓存应该存在");
        assertFalse(isCachePresent("consume:analysis", userId2, "week"), "用户2的缓存应该被清除");
        assertTrue(isCachePresent("consume:analysis", userId3, "week"), "用户3的缓存应该存在");

        log.info("[测试] 多用户缓存清除测试通过");
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据
        log.debug("[测试清理] 清理测试缓存");
    }

    // ==================== 辅助方法 ====================

    /**
     * 填充用户缓存
     */
    private void populateUserCache(Long userId) {
        String[] cacheNames = {"consume:analysis", "consume:trend", "consume:category", "consume:habits", "consume:recommendations"};
        String[] periods = {"week", "month", "quarter"};

        for (String cacheName : cacheNames) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                for (String period : periods) {
                    String key = userId + ":" + period;
                    cache.put(key, createMockCacheValue());
                }
            }
        }
    }

    /**
     * 检查缓存是否存在
     */
    private boolean isCachePresent(String cacheName, Long userId, String period) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return false;
        }

        String key = userId + ":" + period;
        Cache.ValueWrapper valueWrapper = cache.get(key);
        return valueWrapper != null && valueWrapper.get() != null;
    }

    /**
     * 创建模拟缓存值
     */
    private Object createMockCacheValue() {
        Map<String, Object> mockValue = new HashMap<>();
        mockValue.put("totalAmount", new BigDecimal("500.50"));
        mockValue.put("totalCount", 15);
        mockValue.put("test", true);
        return mockValue;
    }
}
