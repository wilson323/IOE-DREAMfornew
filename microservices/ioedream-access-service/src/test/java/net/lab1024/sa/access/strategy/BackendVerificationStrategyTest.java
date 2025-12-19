package net.lab1024.sa.access.strategy;

import net.lab1024.sa.access.domain.dto.AccessVerificationRequest;
import net.lab1024.sa.access.domain.dto.VerificationResult;
import net.lab1024.sa.access.manager.AccessVerificationManager;
import net.lab1024.sa.access.strategy.impl.BackendVerificationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 后台验证策略单元测试
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
class BackendVerificationStrategyTest {

    @Mock
    private AccessVerificationManager accessVerificationManager;

    @InjectMocks
    private BackendVerificationStrategy strategy;

    private AccessVerificationRequest request;

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
                .doorNumber(1)
                .build();
    }

    @Test
    void testVerify_Success() {
        // Given
        when(accessVerificationManager.verifyAntiPassback(anyLong(), anyLong(), anyInt(), anyLong())).thenReturn(true);
        when(accessVerificationManager.verifyInterlock(anyLong(), anyLong())).thenReturn(true);
        when(accessVerificationManager.verifyTimePeriod(anyLong(), anyLong(), any(), anyLong())).thenReturn(true);
        when(accessVerificationManager.isBlacklisted(anyLong())).thenReturn(false);
        when(accessVerificationManager.isMultiPersonRequired(anyLong())).thenReturn(false);

        // When
        VerificationResult result = strategy.verify(request);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("SUCCEED", result.getAuthStatus());
        assertNotNull(result.getControlCommand());
        assertEquals("backend", result.getVerificationMode());

        verify(accessVerificationManager).verifyAntiPassback(1001L, 2001L, 1, 3001L);
        verify(accessVerificationManager).verifyInterlock(2001L, 3001L);
        verify(accessVerificationManager).verifyTimePeriod(1001L, 2001L, request.getVerifyTime(), 3001L);
        verify(accessVerificationManager).isBlacklisted(1001L);
    }

    @Test
    void testVerify_AntiPassbackFailed() {
        // Given
        when(accessVerificationManager.verifyAntiPassback(anyLong(), anyLong(), anyInt(), anyLong())).thenReturn(false);

        // When
        VerificationResult result = strategy.verify(request);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("FAILED", result.getAuthStatus());
        assertEquals("ANTI_PASSBACK_VIOLATION", result.getErrorCode());

        verify(accessVerificationManager).verifyAntiPassback(1001L, 2001L, 1, 3001L);
        verify(accessVerificationManager, never()).verifyInterlock(anyLong(), anyLong());
    }

    @Test
    void testVerify_InterlockFailed() {
        // Given
        when(accessVerificationManager.verifyAntiPassback(anyLong(), anyLong(), anyInt(), anyLong())).thenReturn(true);
        when(accessVerificationManager.verifyInterlock(anyLong(), anyLong())).thenReturn(false);

        // When
        VerificationResult result = strategy.verify(request);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("FAILED", result.getAuthStatus());
        assertEquals("INTERLOCK_VIOLATION", result.getErrorCode());
    }

    @Test
    void testVerify_Blacklisted() {
        // Given
        when(accessVerificationManager.verifyAntiPassback(anyLong(), anyLong(), anyInt(), anyLong())).thenReturn(true);
        when(accessVerificationManager.verifyInterlock(anyLong(), anyLong())).thenReturn(true);
        when(accessVerificationManager.verifyTimePeriod(anyLong(), anyLong(), any(), anyLong())).thenReturn(true);
        when(accessVerificationManager.isBlacklisted(anyLong())).thenReturn(true);

        // When
        VerificationResult result = strategy.verify(request);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("FAILED", result.getAuthStatus());
        assertEquals("BLACKLIST", result.getErrorCode());
    }

    @Test
    void testSupports() {
        // When & Then
        assertTrue(strategy.supports("backend"));
        assertFalse(strategy.supports("edge"));
        assertFalse(strategy.supports("hybrid"));
    }

    @Test
    void testGetStrategyName() {
        // When
        String name = strategy.getStrategyName();

        // Then
        assertEquals("BackendVerificationStrategy", name);
    }
}
