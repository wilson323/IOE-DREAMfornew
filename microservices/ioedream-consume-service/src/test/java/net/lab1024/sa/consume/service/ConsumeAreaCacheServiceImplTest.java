package net.lab1024.sa.consume.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.consume.manager.ConsumeAreaManager;
import net.lab1024.sa.consume.service.impl.ConsumeAreaCacheServiceImpl;

/**
 * ConsumeAreaCacheServiceImpl单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：ConsumeAreaCacheServiceImpl核心业务方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConsumeAreaCacheServiceImpl单元测试")
class ConsumeAreaCacheServiceImplTest {

    @Mock
    private ConsumeAreaManager consumeAreaManager;

    @InjectMocks
    private ConsumeAreaCacheServiceImpl consumeAreaCacheServiceImpl;

    @BeforeEach
    void setUp() {
        // 准备测试数据
    }

    @Test
    @DisplayName("测试validateAreaPermission-成功场景")
    void test_validateAreaPermission_Success() {
        // Given
        Long accountId = 1L;
        String areaId = "area-001";
        when(consumeAreaManager.validateAreaPermission(accountId, areaId)).thenReturn(true);

        // When
        boolean result = consumeAreaCacheServiceImpl.validateAreaPermission(accountId, areaId);

        // Then
        assertTrue(result);
        verify(consumeAreaManager, times(1)).validateAreaPermission(accountId, areaId);
    }

    @Test
    @DisplayName("测试validateAreaPermission-无权限场景")
    void test_validateAreaPermission_NoPermission() {
        // Given
        Long accountId = 1L;
        String areaId = "area-001";
        when(consumeAreaManager.validateAreaPermission(accountId, areaId)).thenReturn(false);

        // When
        boolean result = consumeAreaCacheServiceImpl.validateAreaPermission(accountId, areaId);

        // Then
        assertFalse(result);
        verify(consumeAreaManager, times(1)).validateAreaPermission(accountId, areaId);
    }

    @Test
    @DisplayName("测试evictAreaPermissionCache-成功场景")
    void test_evictAreaPermissionCache_Success() {
        // Given
        Long accountId = 1L;
        String areaId = "area-001";

        // When
        consumeAreaCacheServiceImpl.evictAreaPermissionCache(accountId, areaId);

        // Then
        // Spring Cache会自动处理缓存清除，这里只验证方法执行无异常
        assertTrue(true);
    }

    @Test
    @DisplayName("测试evictAccountAreaPermissionCache-成功场景")
    void test_evictAccountAreaPermissionCache_Success() {
        // Given
        Long accountId = 1L;

        // When
        consumeAreaCacheServiceImpl.evictAccountAreaPermissionCache(accountId);

        // Then
        // Spring Cache会自动处理缓存清除，这里只验证方法执行无异常
        assertTrue(true);
    }

}


