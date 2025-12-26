package net.lab1024.sa.attendance.engine.rule.cache;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.cache.RuleCacheManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 规则缓存管理服务测试类
 * <p>
 * P2-Batch4重构: 测试RuleCacheManagementService的4个核心方法
 * 遵循Given-When-Then测试模式
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("规则缓存管理服务测试")
@Slf4j
class RuleCacheManagementServiceTest {

    @Mock
    private RuleCacheManager cacheManager;

    private RuleCacheManagementService cacheManagementService;

    @BeforeEach
    void setUp() {
        log.info("[规则缓存管理测试] 初始化测试环境");
        cacheManagementService = new RuleCacheManagementService(cacheManager);
    }

    @Test
    @DisplayName("测试清除规则缓存-成功场景")
    void testClearRuleCache_Success() {
        log.info("[规则缓存管理测试] 测试场景: 清除规则缓存-成功");

        // Given: Mock缓存管理器行为
        doNothing().when(cacheManager).clearCache();

        // When: 清除规则缓存
        cacheManagementService.clearRuleCache();

        // Then: 验证缓存被清除
        verify(cacheManager, times(1)).clearCache();

        log.info("[规则缓存管理测试] 测试通过: 规则缓存清除成功");
    }

    @Test
    @DisplayName("测试预热规则缓存-成功场景")
    void testWarmUpRuleCache_Success() {
        log.info("[规则缓存管理测试] 测试场景: 预热规则缓存-成功");

        // Given: 准备测试数据
        List<Long> ruleIds = Arrays.asList(1L, 2L, 3L, 4L, 5L);

        // Mock缓存管理器行为
        doNothing().when(cacheManager).warmUp(anyList());

        // When: 预热规则缓存
        cacheManagementService.warmUpRuleCache(ruleIds);

        // Then: 验证缓存被预热
        verify(cacheManager, times(1)).warmUp(eq(ruleIds));

        log.info("[规则缓存管理测试] 测试通过: 规则缓存预热成功, 规则数={}", ruleIds.size());
    }

    @Test
    @DisplayName("测试获取缓存状态-有数据场景")
    void testGetCacheStatus_WithData() {
        log.info("[规则缓存管理测试] 测试场景: 获取缓存状态-有数据");

        // Given: Mock缓存管理器行为
        int cacheSize = 100;
        int hitCount = 80;
        int missCount = 20;

        when(cacheManager.getCacheSize()).thenReturn(cacheSize);
        when(cacheManager.getHitCount()).thenReturn(hitCount);
        when(cacheManager.getMissCount()).thenReturn(missCount);

        // When: 获取缓存状态
        RuleCacheManagementService.CacheStatus status = cacheManagementService.getCacheStatus();

        // Then: 验证缓存状态
        assertNotNull(status, "缓存状态不应为空");
        assertEquals(cacheSize, status.getCacheSize(), "缓存大小应该匹配");
        assertEquals(hitCount, status.getHitCount(), "命中次数应该匹配");
        assertEquals(missCount, status.getMissCount(), "未命中次数应该匹配");

        // 验证命中率计算: 80 / (80 + 20) = 0.8
        double expectedHitRate = 80.0 / (80 + 20);
        assertEquals(expectedHitRate, status.getHitRate(), 0.001, "命中率应该匹配");

        log.info("[规则缓存管理测试] 测试通过: 缓存状态获取成功, size={}, hitRate={}",
                status.getCacheSize(), status.getHitRate());
    }

    @Test
    @DisplayName("测试获取缓存状态-空缓存场景")
    void testGetCacheStatus_EmptyCache() {
        log.info("[规则缓存管理测试] 测试场景: 获取缓存状态-空缓存");

        // Given: Mock缓存管理器行为（空缓存）
        when(cacheManager.getCacheSize()).thenReturn(0);
        when(cacheManager.getHitCount()).thenReturn(0);
        when(cacheManager.getMissCount()).thenReturn(0);

        // When: 获取缓存状态
        RuleCacheManagementService.CacheStatus status = cacheManagementService.getCacheStatus();

        // Then: 验证缓存状态
        assertNotNull(status, "缓存状态不应为空");
        assertEquals(0, status.getCacheSize(), "缓存大小应该是0");
        assertEquals(0, status.getHitCount(), "命中次数应该是0");
        assertEquals(0, status.getMissCount(), "未命中次数应该是0");

        // 验证命中率: 0 / (0 + 0) = 0.0（特殊处理）
        assertEquals(0.0, status.getHitRate(), "命中率应该是0");

        log.info("[规则缓存管理测试] 测试通过: 空缓存状态获取成功");
    }
}
