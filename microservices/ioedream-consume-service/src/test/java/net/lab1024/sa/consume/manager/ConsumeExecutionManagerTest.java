package net.lab1024.sa.consume.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.client.AccountKindConfigClient;
import net.lab1024.sa.consume.dao.ConsumeProductDao;
import net.lab1024.sa.consume.dao.ConsumeTransactionDao;
import net.lab1024.sa.consume.domain.request.ConsumeRequest;
import net.lab1024.sa.consume.entity.AccountEntity;
import net.lab1024.sa.consume.entity.ConsumeAreaEntity;
import net.lab1024.sa.consume.entity.ConsumeTransactionEntity;
import net.lab1024.sa.consume.manager.impl.ConsumeExecutionManagerImpl;
import net.lab1024.sa.consume.service.ConsumeAreaCacheService;
import net.lab1024.sa.consume.service.impl.DefaultFixedAmountCalculator;
import net.lab1024.sa.consume.strategy.ConsumeAmountCalculator;
import net.lab1024.sa.consume.strategy.ConsumeAmountCalculatorFactory;

/**
 * ConsumeExecutionManager单元测试
 * <p>
 * 测试范围：消费执行管理业务逻辑
 * 目标：提升测试覆盖率至80%+
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ConsumeExecutionManager单元测试")
class ConsumeExecutionManagerTest {

    @Mock
    private ConsumeAreaManager consumeAreaManager;

    @Mock
    private ConsumeAreaCacheService consumeAreaCacheService;

    @Mock
    private ConsumeDeviceManager consumeDeviceManager;

    @Mock
    private net.lab1024.sa.common.gateway.GatewayServiceClient gatewayServiceClient;

    @Mock
    private AccountKindConfigClient accountKindConfigClient;

    @Mock
    private AccountManager accountManager;

    @Mock
    private ConsumeTransactionDao consumeTransactionDao;

    @Mock
    private ConsumeProductDao consumeProductDao;

    @Mock
    private DefaultFixedAmountCalculator fixedAmountCalculator;

    @Mock
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Mock
    private ConsumeAmountCalculatorFactory calculatorFactory;

    @Mock
    private ConsumeAmountCalculator calculator;

    @Spy
    @InjectMocks
    private ConsumeExecutionManagerImpl consumeExecutionManager;

    private ConsumeRequest consumeRequest;
    private AccountEntity accountEntity;
    private ConsumeAreaEntity areaEntity;

    @BeforeEach
    void setUp() {
        consumeRequest = new ConsumeRequest();
        consumeRequest.setAccountId(1L);
        consumeRequest.setAreaId("1");
        consumeRequest.setConsumeMode("FIXED");
        consumeRequest.setUserId(1001L);

        accountEntity = new AccountEntity();
        accountEntity.setAccountId(1L);
        accountEntity.setUserId(1001L);
        accountEntity.setBalance(new BigDecimal("1000.00"));
        accountEntity.setStatus(1);
        accountEntity.setAccountKindId(1L);

        areaEntity = new ConsumeAreaEntity();
        areaEntity.setAreaCode("AREA001");
        areaEntity.setAreaName("测试区域");
        areaEntity.setStatus(1);
        areaEntity.setManageMode(1);
    }

    // ==================== validateConsumePermission 测试 ====================

    @Test
    @DisplayName("测试验证消费权限-有权限")
    void testValidateConsumePermission_HasPermission() {
        // Given
        Long accountId = 1L;
        String areaId = "AREA001";
        String consumeMode = "FIXED";

        when(consumeAreaCacheService.validateAreaPermission(accountId, areaId)).thenReturn(true);
        when(consumeAreaManager.getAreaById(areaId)).thenReturn(areaEntity);
        when(accountManager.getAccountById(accountId)).thenReturn(accountEntity);

        // When
        boolean result = consumeExecutionManager.validateConsumePermission(accountId, areaId, consumeMode);

        // Then
        assertTrue(result);
        verify(consumeAreaCacheService).validateAreaPermission(accountId, areaId);
        verify(consumeAreaManager).getAreaById(areaId);
        verify(accountManager).getAccountById(accountId);
    }

    @Test
    @DisplayName("测试验证消费权限-账户不存在")
    void testValidateConsumePermission_AccountNotFound() {
        // Given
        Long accountId = 9999L;
        String areaId = "AREA001";
        String consumeMode = "FIXED";

        when(consumeAreaCacheService.validateAreaPermission(accountId, areaId)).thenReturn(true);
        when(consumeAreaManager.getAreaById(areaId)).thenReturn(areaEntity);
        when(accountManager.getAccountById(accountId)).thenReturn(null);

        // When
        boolean result = consumeExecutionManager.validateConsumePermission(accountId, areaId, consumeMode);

        // Then
        assertFalse(result);
        verify(consumeAreaCacheService).validateAreaPermission(accountId, areaId);
        verify(consumeAreaManager).getAreaById(areaId);
        verify(accountManager).getAccountById(accountId);
    }

    @Test
    @DisplayName("测试验证消费权限-区域不存在")
    void testValidateConsumePermission_AreaNotFound() {
        // Given
        Long accountId = 1L;
        String areaId = "NON_EXISTENT";
        String consumeMode = "FIXED";

        when(consumeAreaCacheService.validateAreaPermission(accountId, areaId)).thenReturn(true);
        when(consumeAreaManager.getAreaById(areaId)).thenReturn(null);

        // When
        boolean result = consumeExecutionManager.validateConsumePermission(accountId, areaId, consumeMode);

        // Then
        assertFalse(result);
        verify(consumeAreaCacheService).validateAreaPermission(accountId, areaId);
        verify(consumeAreaManager).getAreaById(areaId);
        verify(accountManager, never()).getAccountById(anyLong());
    }

    // ==================== calculateConsumeAmount 测试 ====================

    @Test
    @DisplayName("测试计算消费金额-固定金额模式")
    void testCalculateConsumeAmount_FixedMode() {
        // Given
        Long accountId = 1L;
        String areaId = "AREA001";
        String consumeMode = "FIXED";
        BigDecimal consumeAmount = new BigDecimal("10.00");

        when(calculatorFactory.getCalculator(consumeMode)).thenReturn(calculator);
        when(accountManager.getAccountById(accountId)).thenReturn(accountEntity);
        when(calculator.isSupported(eq(accountId), eq(areaId), any(AccountEntity.class))).thenReturn(true);
        when(calculator.calculate(eq(accountId), eq(areaId), any(AccountEntity.class), any()))
                .thenReturn(new BigDecimal("10.00"));

        // When
        BigDecimal result = consumeExecutionManager.calculateConsumeAmount(
                accountId, areaId, consumeMode, consumeAmount, null);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("10.00"), result);
        verify(calculatorFactory).getCalculator(consumeMode);
        verify(accountManager).getAccountById(accountId);
    }

    @Test
    @DisplayName("测试计算消费金额-账户不存在")
    void testCalculateConsumeAmount_AccountNotFound() {
        // Given
        Long accountId = 9999L;
        String areaId = "AREA001";
        String consumeMode = "FIXED";
        BigDecimal consumeAmount = new BigDecimal("10.00");

        when(calculatorFactory.getCalculator(consumeMode)).thenReturn(calculator);
        when(accountManager.getAccountById(accountId)).thenReturn(null);

        // When
        BigDecimal result = consumeExecutionManager.calculateConsumeAmount(
                accountId, areaId, consumeMode, consumeAmount, null);

        // Then
        assertEquals(BigDecimal.ZERO, result);
        verify(calculatorFactory).getCalculator(consumeMode);
        verify(accountManager).getAccountById(accountId);
    }

    @Test
    @DisplayName("测试计算消费金额-金额为null")
    void testCalculateConsumeAmount_AmountIsNull() {
        // Given
        Long accountId = 1L;
        String areaId = "AREA001";
        String consumeMode = "FIXED";
        BigDecimal consumeAmount = null;

        when(calculatorFactory.getCalculator(consumeMode)).thenReturn(calculator);
        when(accountManager.getAccountById(accountId)).thenReturn(accountEntity);
        when(calculator.isSupported(eq(accountId), eq(areaId), any(AccountEntity.class))).thenReturn(true);
        when(calculator.calculate(eq(accountId), eq(areaId), any(AccountEntity.class), any()))
                .thenReturn(BigDecimal.ZERO);

        // When
        BigDecimal result = consumeExecutionManager.calculateConsumeAmount(
                accountId, areaId, consumeMode, consumeAmount, null);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result);
    }

    // ==================== executeConsumption 测试 ====================

    @Test
    @DisplayName("测试执行消费流程-成功")
    void testExecuteConsumption_Success() {
        // Given
        when(accountManager.getAccountById(anyLong())).thenReturn(accountEntity);
        when(consumeAreaManager.getAreaById(anyString())).thenReturn(areaEntity);
        doReturn(true).when(consumeExecutionManager).validateConsumePermission(anyLong(), anyString(), anyString());
        doReturn(new BigDecimal("10.00")).when(consumeExecutionManager)
                .calculateConsumeAmount(anyLong(), anyString(), anyString(), any(), any());
        when(accountManager.checkBalanceSufficient(anyLong(), any(BigDecimal.class))).thenReturn(true);
        when(accountManager.deductBalance(anyLong(), any(BigDecimal.class))).thenReturn(true);
        when(consumeTransactionDao.insert(any(ConsumeTransactionEntity.class))).thenReturn(1);

        // When
        ResponseDTO<?> result = consumeExecutionManager.executeConsumption(consumeRequest);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试执行消费流程-请求为null")
    void testExecuteConsumption_RequestIsNull() {
        // When
        ResponseDTO<?> result = consumeExecutionManager.executeConsumption(null);

        // Then
        assertNotNull(result);
        assertFalse(result.getOk());
    }

    @Test
    @DisplayName("测试执行消费流程-权限验证失败")
    void testExecuteConsumption_PermissionDenied() {
        // Given
        when(accountManager.getAccountById(anyLong())).thenReturn(accountEntity);
        doReturn(false).when(consumeExecutionManager).validateConsumePermission(anyLong(), anyString(), anyString());

        // When
        ResponseDTO<?> result = consumeExecutionManager.executeConsumption(consumeRequest);

        // Then
        assertNotNull(result);
        assertFalse(result.getOk());
    }

    @Test
    @DisplayName("测试执行消费流程-余额不足")
    void testExecuteConsumption_InsufficientBalance() {
        // Given
        when(accountManager.getAccountById(anyLong())).thenReturn(accountEntity);
        when(consumeAreaManager.getAreaById(anyString())).thenReturn(areaEntity);
        doReturn(true).when(consumeExecutionManager).validateConsumePermission(anyLong(), anyString(), anyString());
        doReturn(new BigDecimal("10000.00")).when(consumeExecutionManager)
                .calculateConsumeAmount(anyLong(), anyString(), anyString(), any(), any());
        when(accountManager.checkBalanceSufficient(anyLong(), any(BigDecimal.class))).thenReturn(false);

        // When
        ResponseDTO<?> result = consumeExecutionManager.executeConsumption(consumeRequest);

        // Then
        assertNotNull(result);
        assertFalse(result.getOk());
    }
}
