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
    @DisplayName("测试set-成功场景")
    void test_set_Success() {
        // Given
        String key = "test:key:1";
        String value = "test-value";
        when(cacheManager.getCache(anyString())).thenReturn(cache);

        // When
        cacheServiceImpl.set(key, value);

        // Then
        verify(cache, times(1)).put(key, value);
        verify(valueOperations, times(1)).set(anyString(), eq(value));
    }

    @Test
    @DisplayName("测试set-带过期时间成功场景")
    void test_setWithTimeout_Success() {
        // Given
        String key = "test:key:1";
        String value = "test-value";
        long timeout = 300;
        TimeUnit unit = TimeUnit.SECONDS;
        when(cacheManager.getCache(anyString())).thenReturn(cache);

        // When
        cacheServiceImpl.set(key, value, timeout, unit);

        // Then
        verify(cache, times(1)).put(key, value);
        verify(valueOperations, times(1)).set(anyString(), eq(value), eq(timeout), eq(unit));
    }

    @Test
    @DisplayName("测试delete-成功场景")
    void test_delete_Success() {
        // Given
        String key = "test:key:1";
        when(cacheManager.getCache(anyString())).thenReturn(cache);
        when(redisTemplate.delete(anyString())).thenReturn(true);

        // When
        Boolean result = cacheServiceImpl.delete(key);

        // Then
        assertTrue(result);
        verify(cache, times(1)).evict(key);
        verify(redisTemplate, times(1)).delete(anyString());
    }

    @Test
    @DisplayName("测试hasKey-成功场景")
    void test_hasKey_Success() {
        // Given
        String key = "test:key:1";
        when(redisTemplate.hasKey(anyString())).thenReturn(true);

        // When
        Boolean result = cacheServiceImpl.hasKey(key);

        // Then
        assertTrue(result);
        verify(redisTemplate, times(1)).hasKey(anyString());
    }

    @Test
    @DisplayName("测试expire-成功场景")
    void test_expire_Success() {
        // Given
        String key = "test:key:1";
        long timeout = 300;
        TimeUnit unit = TimeUnit.SECONDS;
        when(redisTemplate.expire(anyString(), eq(timeout), eq(unit))).thenReturn(true);

        // When
        Boolean result = cacheServiceImpl.expire(key, timeout, unit);

        // Then
        assertTrue(result);
        verify(redisTemplate, times(1)).expire(anyString(), eq(timeout), eq(unit));
    }

    @Test
    @DisplayName("测试increment-成功场景")
    void test_increment_Success() {
        // Given
        String key = "test:key:1";
        Long expectedValue = 2L;
        when(valueOperations.increment(anyString())).thenReturn(expectedValue);

        // When
        Long result = cacheServiceImpl.increment(key);

        // Then
        assertNotNull(result);
        assertEquals(expectedValue, result);
        verify(valueOperations, times(1)).increment(anyString());
    }

    @Test
    @DisplayName("测试increment-带增量成功场景")
    void test_incrementWithDelta_Success() {
        // Given
        String key = "test:key:1";
        long delta = 5L;
        Long expectedValue = 6L;
        when(valueOperations.increment(anyString(), eq(delta))).thenReturn(expectedValue);

        // When
        Long result = cacheServiceImpl.increment(key, delta);

        // Then
        assertNotNull(result);
        assertEquals(expectedValue, result);
        verify(valueOperations, times(1)).increment(anyString(), eq(delta));
    }

    @Test
    @DisplayName("测试decrement-成功场景")
    void test_decrement_Success() {
        // Given
        String key = "test:key:1";
        Long expectedValue = 0L;
        when(valueOperations.decrement(anyString())).thenReturn(expectedValue);

        // When
        Long result = cacheServiceImpl.decrement(key);

        // Then
        assertNotNull(result);
        assertEquals(expectedValue, result);
        verify(valueOperations, times(1)).decrement(anyString());
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
    @DisplayName("测试set-异常场景")
    void test_set_Exception() {
        // Given
        String key = "test:key:1";
        String value = "test-value";
        when(cacheManager.getCache(anyString())).thenThrow(new RuntimeException("Cache error"));

        // When
        cacheServiceImpl.set(key, value);

        // Then
        // 方法应该处理异常而不抛出
        assertTrue(true);
    }

}

