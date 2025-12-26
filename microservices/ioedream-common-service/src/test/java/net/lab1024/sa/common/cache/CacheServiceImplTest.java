package net.lab1024.sa.common.cache;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * CacheServiceImpl单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：CacheServiceImpl核心业务方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CacheServiceImpl单元测试")
@SuppressWarnings("null")
class CacheServiceImplTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private Cache cache;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private CacheServiceImpl cacheServiceImpl;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        cacheServiceImpl = new CacheServiceImpl(cacheManager, redisTemplate);
    }

    @Test
    @DisplayName("测试get-成功场景")
    void test_get_Success() {
        // Given
        String key = "test:key:1";
        String expectedValue = "test-value";
        Cache.ValueWrapper wrapper = mock(Cache.ValueWrapper.class);
        when(cacheManager.getCache(anyString())).thenReturn(cache);
        when(cache.get(key)).thenReturn(wrapper);
        when(wrapper.get()).thenReturn(expectedValue);

        // When
        String result = cacheServiceImpl.get(key, String.class);

        // Then
        assertNotNull(result);
        assertEquals(expectedValue, result);
    }

    @Test
    @DisplayName("测试get-缓存不存在场景")
    void test_get_NotFound() {
        // Given
        String key = "test:key:1";
        when(cacheManager.getCache(anyString())).thenReturn(cache);
        when(cache.get(key)).thenReturn(null);
        when(valueOperations.get(anyString())).thenReturn(null);

        // When
        String result = cacheServiceImpl.get(key, String.class);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("测试put-成功场景")
    void test_put_Success() {
        // Given
        String key = "test:key:1";
        String value = "test-value";
        when(cacheManager.getCache(anyString())).thenReturn(cache);

        // When
        cacheServiceImpl.put(key, value);

        // Then
        verify(cache, times(1)).put(key, value);
        verify(valueOperations, times(1)).set(anyString(), eq(value));
    }

    @Test
    @DisplayName("测试put-带过期时间成功场景")
    void test_putWithTimeout_Success() {
        // Given
        String key = "test:key:1";
        String value = "test-value";
        long timeout = 300;
        TimeUnit unit = TimeUnit.SECONDS;
        when(cacheManager.getCache(anyString())).thenReturn(cache);

        // When
        cacheServiceImpl.put(key, value, timeout, unit);

        // Then
        verify(cache, times(1)).put(key, value);
        verify(valueOperations, times(1)).set(anyString(), eq(value), eq(timeout), eq(unit));
    }

    @Test
    @DisplayName("测试evict-成功场景")
    void test_evict_Success() {
        // Given
        String key = "test:key:1";
        when(cacheManager.getCache(anyString())).thenReturn(cache);
        when(redisTemplate.delete(anyString())).thenReturn(true);

        // When
        boolean result = cacheServiceImpl.evict(key);

        // Then
        assertTrue(result);
        verify(cache, times(1)).evict(key);
        verify(redisTemplate, times(1)).delete(anyString());
    }

    @Test
    @DisplayName("测试exists-成功场景")
    void test_exists_Success() {
        // Given
        String key = "test:key:1";
        when(redisTemplate.hasKey(anyString())).thenReturn(true);

        // When
        boolean result = cacheServiceImpl.exists(key);

        // Then
        assertTrue(result);
        verify(redisTemplate, times(1)).hasKey(anyString());
    }

    @Test
    @DisplayName("测试get-从Redis获取场景")
    void test_get_FromRedis() {
        // Given
        String key = "test:key:1";
        String expectedValue = "test-value";
        when(cacheManager.getCache(anyString())).thenReturn(cache);
        when(cache.get(key)).thenReturn(null);
        when(valueOperations.get(anyString())).thenReturn(expectedValue);

        // When
        String result = cacheServiceImpl.get(key, String.class);

        // Then
        assertNotNull(result);
        assertEquals(expectedValue, result);
        verify(valueOperations, times(1)).get(anyString());
    }

    @Test
    @DisplayName("测试put-异常场景")
    void test_put_Exception() {
        // Given
        String key = "test:key:1";
        String value = "test-value";
        when(cacheManager.getCache(anyString())).thenThrow(new RuntimeException("Cache error"));

        // When
        cacheServiceImpl.put(key, value);

        // Then
        // 方法应该处理异常而不抛出
        assertTrue(true);
    }

}
