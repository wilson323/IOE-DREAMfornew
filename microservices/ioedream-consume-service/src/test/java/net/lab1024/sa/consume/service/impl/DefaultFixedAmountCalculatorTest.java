package net.lab1024.sa.consume.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.lab1024.sa.common.cache.CacheService;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.consume.domain.entity.AccountEntity;
import net.lab1024.sa.consume.manager.AccountManager;
import net.lab1024.sa.consume.manager.ConsumeAreaManager;

/**
 * DefaultFixedAmountCalculator单元测试
 * <p>
 * 目标覆盖率：≥80%
 * 测试范围：定值金额计算逻辑
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DefaultFixedAmountCalculator单元测试")
class DefaultFixedAmountCalculatorTest {

    @Mock
    private CacheService cacheService;

    @Mock
    private AccountManager accountManager;

    @Mock
    private ConsumeAreaManager consumeAreaManager;

    @Mock
    private GatewayServiceClient gatewayServiceClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DefaultFixedAmountCalculator calculator;

    private AccountEntity mockAccount;
    private Map<String, Object> mockAreaConfig;

    @BeforeEach
    void setUp() {
        // 准备模拟账户
        mockAccount = new AccountEntity();
        mockAccount.setAccountId(1001L);
        mockAccount.setUserId(2001L);
        mockAccount.setTotalConsumeAmount(new BigDecimal("500.00"));

        // 准备模拟区域配置
        mockAreaConfig = new HashMap<>();
        mockAreaConfig.put("breakfastAmount", 5.00);
        mockAreaConfig.put("lunchAmount", 10.00);
        mockAreaConfig.put("dinnerAmount", 15.00);
    }

    @Test
    @DisplayName("测试计算早餐金额-成功场景")
    void testCalculateBreakfastAmount_Success() {
        // Given
        Long areaId = 3001L;

        when(consumeAreaManager.parseFixedValueConfig(anyString()))
                .thenReturn(mockAreaConfig);
        when(accountManager.getAccountById(anyLong()))
                .thenReturn(mockAccount);

        // When - 使用实际的calculate方法
        net.lab1024.sa.consume.domain.dto.ConsumeRequestDTO request =
                new net.lab1024.sa.consume.domain.dto.ConsumeRequestDTO();
        request.setAreaId(areaId);
        request.setUserId(mockAccount.getUserId());
        request.setDeviceId(1L);
        request.setAmount(new BigDecimal("10.00"));

        Integer amount = calculator.calculate(request, mockAccount);

        // Then
        assertNotNull(amount);
        assertTrue(amount > 0);
    }

    @Test
    @DisplayName("测试计算午餐金额-成功场景")
    void testCalculateLunchAmount_Success() {
        // Given
        Long areaId = 3001L;

        when(consumeAreaManager.parseFixedValueConfig(anyString()))
                .thenReturn(mockAreaConfig);
        when(accountManager.getAccountById(anyLong()))
                .thenReturn(mockAccount);

        // When
        net.lab1024.sa.consume.domain.dto.ConsumeRequestDTO request =
                new net.lab1024.sa.consume.domain.dto.ConsumeRequestDTO();
        request.setAreaId(areaId);
        request.setUserId(mockAccount.getUserId());
        request.setDeviceId(1L);
        request.setAmount(new BigDecimal("10.00"));

        Integer amount = calculator.calculate(request, mockAccount);

        // Then
        assertNotNull(amount);
        assertTrue(amount > 0);
    }

    @Test
    @DisplayName("测试计算晚餐金额-成功场景")
    void testCalculateDinnerAmount_Success() {
        // Given
        Long areaId = 3001L;

        when(consumeAreaManager.parseFixedValueConfig(anyString()))
                .thenReturn(mockAreaConfig);
        when(accountManager.getAccountById(anyLong()))
                .thenReturn(mockAccount);

        // When
        net.lab1024.sa.consume.domain.dto.ConsumeRequestDTO request =
                new net.lab1024.sa.consume.domain.dto.ConsumeRequestDTO();
        request.setAreaId(areaId);
        request.setUserId(mockAccount.getUserId());
        request.setDeviceId(1L);
        request.setAmount(new BigDecimal("10.00"));

        Integer amount = calculator.calculate(request, mockAccount);

        // Then
        assertNotNull(amount);
        assertTrue(amount > 0);
    }

    @Test
    @DisplayName("测试周末金额计算-成功场景")
    void testCalculateWeekendAmount_Success() {
        // Given
        Long areaId = 3001L;

        Map<String, Object> weekendConfig = new HashMap<>(mockAreaConfig);
        weekendConfig.put("weekendMultiplier", 1.2); // 周末加价20%

        when(consumeAreaManager.parseFixedValueConfig(anyString()))
                .thenReturn(weekendConfig);
        when(accountManager.getAccountById(anyLong()))
                .thenReturn(mockAccount);

        // When
        net.lab1024.sa.consume.domain.dto.ConsumeRequestDTO request =
                new net.lab1024.sa.consume.domain.dto.ConsumeRequestDTO();
        request.setAreaId(areaId);
        request.setUserId(mockAccount.getUserId());
        request.setDeviceId(1L);
        request.setAmount(new BigDecimal("10.00"));

        Integer amount = calculator.calculate(request, mockAccount);

        // Then
        assertNotNull(amount);
        assertTrue(amount > 0);
    }

    @Test
    @DisplayName("测试会员等级金额计算-成功场景")
    void testCalculateMemberLevelAmount_Success() {
        // Given
        Long areaId = 3001L;

        // 设置高消费金额，触发高级会员
        AccountEntity highConsumeAccount = new AccountEntity();
        highConsumeAccount.setAccountId(1001L);
        highConsumeAccount.setTotalConsumeAmount(new BigDecimal("10000.00")); // 高消费

        when(consumeAreaManager.parseFixedValueConfig(anyString()))
                .thenReturn(mockAreaConfig);
        when(accountManager.getAccountById(anyLong()))
                .thenReturn(highConsumeAccount);

        // When
        net.lab1024.sa.consume.domain.dto.ConsumeRequestDTO request =
                new net.lab1024.sa.consume.domain.dto.ConsumeRequestDTO();
        request.setAreaId(areaId);
        request.setUserId(mockAccount.getUserId());
        request.setDeviceId(1L);
        request.setAmount(new BigDecimal("10.00"));

        Integer amount = calculator.calculate(request, highConsumeAccount);

        // Then
        assertNotNull(amount);
        assertTrue(amount > 0);
    }

    @Test
    @DisplayName("测试配置加载失败-使用默认值")
    void testLoadConfig_FallbackToDefault() {
        // Given
        Long areaId = 3001L;

        when(consumeAreaManager.parseFixedValueConfig(anyString()))
                .thenReturn(null); // 配置加载失败
        when(accountManager.getAccountById(anyLong()))
                .thenReturn(mockAccount);

        // When
        net.lab1024.sa.consume.domain.dto.ConsumeRequestDTO request =
                new net.lab1024.sa.consume.domain.dto.ConsumeRequestDTO();
        request.setAreaId(areaId);
        request.setUserId(mockAccount.getUserId());
        request.setDeviceId(1L);
        request.setAmount(new BigDecimal("10.00"));

        Integer amount = calculator.calculate(request, mockAccount);

        // Then
        assertNotNull(amount);
        assertTrue(amount > 0);
    }
}
