package net.lab1024.sa.access.strategy;

import net.lab1024.sa.access.domain.dto.AccessVerificationRequest;
import net.lab1024.sa.access.domain.dto.VerificationResult;
import net.lab1024.sa.access.strategy.impl.EdgeVerificationStrategy;
import net.lab1024.sa.common.organization.dao.AccessRecordDao;
import net.lab1024.sa.common.organization.entity.AccessRecordEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 设备端验证策略单元测试
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
class EdgeVerificationStrategyTest {

    @Mock
    private AccessRecordDao accessRecordDao;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private EdgeVerificationStrategy strategy;

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
                .build();

        // Mock Redis操作
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(accessRecordDao.selectByCompositeKey(anyLong(), anyLong(), any())).thenReturn(null);
        when(accessRecordDao.insert(any(AccessRecordEntity.class))).thenReturn(1);
    }

    @Test
    void testVerify_Success() {
        // When
        VerificationResult result = strategy.verify(request);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("SUCCEED", result.getAuthStatus());
        assertEquals("edge", result.getVerificationMode());
    }

    @Test
    void testVerify_InvalidRecord() {
        // Given
        request.setUserId(null);

        // When
        VerificationResult result = strategy.verify(request);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("FAILED", result.getAuthStatus());
        assertEquals("INVALID_RECORD", result.getErrorCode());
    }

    @Test
    void testSupports() {
        // When & Then
        assertTrue(strategy.supports("edge"));
        assertFalse(strategy.supports("backend"));
        assertFalse(strategy.supports("hybrid"));
    }

    @Test
    void testGetStrategyName() {
        // When
        String name = strategy.getStrategyName();

        // Then
        assertEquals("EdgeVerificationStrategy", name);
    }
}
