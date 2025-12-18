package net.lab1024.sa.access.service;

import net.lab1024.sa.access.domain.dto.AccessVerificationRequest;
import net.lab1024.sa.access.domain.dto.VerificationResult;
import net.lab1024.sa.access.service.impl.AccessVerificationServiceImpl;
import net.lab1024.sa.access.strategy.VerificationModeStrategy;
import net.lab1024.sa.common.organization.dao.AreaAccessExtDao;
import net.lab1024.sa.common.organization.entity.AreaAccessExtEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 门禁验证服务单元测试
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
class AccessVerificationServiceTest {

    @Mock
    private AreaAccessExtDao areaAccessExtDao;

    @Mock
    private VerificationModeStrategy backendStrategy;

    @Mock
    private VerificationModeStrategy edgeStrategy;

    @InjectMocks
    private AccessVerificationServiceImpl service;

    private AccessVerificationRequest request;
    private AreaAccessExtEntity areaExt;

    @BeforeEach
    void setUp() {
        request = AccessVerificationRequest.builder()
                .userId(1001L)
                .deviceId(2001L)
                .areaId(3001L)
                .event(0)
                .verifyType(1)
                .inOutStatus(1)
                .verifyTime(LocalDateTime.now())
                .build();

        areaExt = new AreaAccessExtEntity();
        areaExt.setAreaId(3001L);
        areaExt.setVerificationMode("backend");

        // 设置策略列表
        List<VerificationModeStrategy> strategyList = Arrays.asList(backendStrategy, edgeStrategy);
        // 使用反射设置strategyList（实际实现中通过@Resource注入）
    }

    @Test
    void testVerifyAccess_BackendMode() {
        // Given
        when(areaAccessExtDao.selectByAreaId(3001L)).thenReturn(areaExt);
        when(backendStrategy.supports("backend")).thenReturn(true);
        when(backendStrategy.verify(any())).thenReturn(
                VerificationResult.success("验证通过", "0101000300", "backend")
        );

        // When
        VerificationResult result = service.verifyAccess(request);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("backend", result.getVerificationMode());

        verify(areaAccessExtDao).selectByAreaId(3001L);
        verify(backendStrategy).verify(any());
    }

    @Test
    void testVerifyAccess_EdgeMode() {
        // Given
        areaExt.setVerificationMode("edge");
        when(areaAccessExtDao.selectByAreaId(3001L)).thenReturn(areaExt);
        when(edgeStrategy.supports("edge")).thenReturn(true);
        when(edgeStrategy.verify(any())).thenReturn(
                VerificationResult.success("记录接收成功", null, "edge")
        );

        // When
        VerificationResult result = service.verifyAccess(request);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("edge", result.getVerificationMode());

        verify(areaAccessExtDao).selectByAreaId(3001L);
        verify(edgeStrategy).verify(any());
    }

    @Test
    void testVerifyAccess_UnsupportedMode() {
        // Given
        areaExt.setVerificationMode("unknown");
        when(areaAccessExtDao.selectByAreaId(3001L)).thenReturn(areaExt);

        // When
        VerificationResult result = service.verifyAccess(request);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("FAILED", result.getAuthStatus());
        assertEquals("UNSUPPORTED_MODE", result.getErrorCode());
    }

    @Test
    void testGetVerificationMode_WithConfig() {
        // Given
        when(areaAccessExtDao.selectByAreaId(3001L)).thenReturn(areaExt);

        // When
        String mode = service.getVerificationMode(3001L);

        // Then
        assertEquals("backend", mode);
        verify(areaAccessExtDao).selectByAreaId(3001L);
    }

    @Test
    void testGetVerificationMode_DefaultWhenNull() {
        // Given
        when(areaAccessExtDao.selectByAreaId(3001L)).thenReturn(null);

        // When
        String mode = service.getVerificationMode(3001L);

        // Then
        assertEquals("edge", mode); // 默认模式
    }

    @Test
    void testGetVerificationMode_DefaultWhenEmpty() {
        // Given
        areaExt.setVerificationMode("");
        when(areaAccessExtDao.selectByAreaId(3001L)).thenReturn(areaExt);

        // When
        String mode = service.getVerificationMode(3001L);

        // Then
        assertEquals("edge", mode); // 默认模式
    }
}
