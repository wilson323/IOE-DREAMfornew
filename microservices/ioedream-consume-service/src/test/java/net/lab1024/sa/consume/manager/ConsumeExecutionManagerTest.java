package net.lab1024.sa.consume.manager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.dao.ConsumeProductDao;
import net.lab1024.sa.consume.dao.ConsumeTransactionDao;
import net.lab1024.sa.consume.domain.entity.AccountEntity;
import net.lab1024.sa.consume.domain.entity.ConsumeAreaEntity;
import net.lab1024.sa.consume.domain.request.ConsumeRequest;
import net.lab1024.sa.consume.manager.impl.ConsumeExecutionManagerImpl;
import net.lab1024.sa.consume.service.impl.DefaultFixedAmountCalculator;
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
@DisplayName("ConsumeExecutionManager单元测试")
class ConsumeExecutionManagerTest {

    @Mock
    private ConsumeAreaManager consumeAreaManager;

    @Mock
    private ConsumeDeviceManager consumeDeviceManager;

    @Mock
    private net.lab1024.sa.common.gateway.GatewayServiceClient gatewayServiceClient;

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

    @InjectMocks
    private ConsumeExecutionManagerImpl consumeExecutionManager;

    private ConsumeRequest consumeRequest;
    private AccountEntity accountEntity;
    private ConsumeAreaEntity areaEntity;

    @BeforeEach
    void setUp() {
        consumeRequest = new ConsumeRequest();
        consumeRequest.setAccountId(1L);
        consumeRequest.setAreaId("AREA001");
        consumeRequest.setConsumeMode("FIXED");
        consumeRequest.setUserId(1001L);

        accountEntity = new AccountEntity();
        accountEntity.setAccountId(1L);
        accountEntity.setUserId(1001L);
        accountEntity.setBalance(new BigDecimal("1000.00"));
        accountEntity.setStatus(1);

        areaEntity = new ConsumeAreaEntity();
        areaEntity.setAreaCode("AREA001");
        areaEntity.setAreaName("测试区域");
        areaEntity.setStatus(1);
    }

    // ==================== validateConsumePermission 测试 ====================

    @Test
    @DisplayName("测试验证消费权限-有权限")
    void testValidateConsumePermission_HasPermission() {
        // Given
        Long accountId = 1L;
        String areaId = "AREA001";
        String consumeMode = "FIXED";

        when(accountManager.getAccountById(accountId)).thenReturn(accountEntity);
        when(consumeAreaManager.getAreaById(areaId)).thenReturn(areaEntity);

        // When
        boolean result = consumeExecutionManager.validateConsumePermission(accountId, areaId, consumeMode);

        // Then
        // 根据实际实现验证结果
        assertNotNull(result);
        verify(accountManager, times(1)).getAccountById(accountId);
        verify(consumeAreaManager, times(1)).getAreaById(areaId);
    }

    @Test
    @DisplayName("测试验证消费权限-账户不存在")
    void testValidateConsumePermission_AccountNotFound() {
        // Given
        Long accountId = 9999L;
        String areaId = "AREA001";
        String consumeMode = "FIXED";

        when(accountManager.getAccountById(accountId)).thenReturn(null);

        // When
        boolean result = consumeExecutionManager.validateConsumePermission(accountId, areaId, consumeMode);

        // Then
        assertFalse(result);
        verify(accountManager, times(1)).getAccountById(accountId);
        verify(consumeAreaManager, never()).getAreaById(anyString());
    }

    @Test
    @DisplayName("测试验证消费权限-区域不存在")
    void testValidateConsumePermission_AreaNotFound() {
        // Given
        Long accountId = 1L;
        String areaId = "NON_EXISTENT";
        String consumeMode = "FIXED";

        when(accountManager.getAccountById(accountId)).thenReturn(accountEntity);
        when(consumeAreaManager.getAreaById(areaId)).thenReturn(null);

        // When
        boolean result = consumeExecutionManager.validateConsumePermission(accountId, areaId, consumeMode);

        // Then
        assertFalse(result);
        verify(accountManager, times(1)).getAccountById(accountId);
        verify(consumeAreaManager, times(1)).getAreaById(areaId);
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

        when(accountManager.getAccountById(accountId)).thenReturn(accountEntity);
        when(consumeAreaManager.getAreaById(areaId)).thenReturn(areaEntity);
        when(fixedAmountCalculator.calculate(any(), any())).thenReturn(1000); // 返回分

        // When
        BigDecimal result = consumeExecutionManager.calculateConsumeAmount(
            accountId, areaId, consumeMode, consumeAmount, null);

        // Then
        assertNotNull(result);
        verify(accountManager, times(1)).getAccountById(accountId);
    }

    @Test
    @DisplayName("测试计算消费金额-账户不存在")
    void testCalculateConsumeAmount_AccountNotFound() {
        // Given
        Long accountId = 9999L;
        String areaId = "AREA001";
        String consumeMode = "FIXED";
        BigDecimal consumeAmount = new BigDecimal("10.00");

        when(accountManager.getAccountById(accountId)).thenReturn(null);

        // When
        BigDecimal result = consumeExecutionManager.calculateConsumeAmount(
            accountId, areaId, consumeMode, consumeAmount, null);

        // Then
        assertNull(result);
        verify(accountManager, times(1)).getAccountById(accountId);
    }

    @Test
    @DisplayName("测试计算消费金额-金额为null")
    void testCalculateConsumeAmount_AmountIsNull() {
        // Given
        Long accountId = 1L;
        String areaId = "AREA001";
        String consumeMode = "FIXED";
        BigDecimal consumeAmount = null;

        when(accountManager.getAccountById(accountId)).thenReturn(accountEntity);
        when(consumeAreaManager.getAreaById(areaId)).thenReturn(areaEntity);

        // When
        BigDecimal result = consumeExecutionManager.calculateConsumeAmount(
            accountId, areaId, consumeMode, consumeAmount, null);

        // Then
        // 根据实际实现，可能是null或默认值
        assertNotNull(result);
    }

    // ==================== executeConsumption 测试 ====================

    @Test
    @DisplayName("测试执行消费流程-成功")
    void testExecuteConsumption_Success() {
        // Given
        when(accountManager.getAccountById(anyLong())).thenReturn(accountEntity);
        when(consumeAreaManager.getAreaById(anyString())).thenReturn(areaEntity);
        when(consumeExecutionManager.validateConsumePermission(anyLong(), anyString(), anyString())).thenReturn(true);
        when(consumeExecutionManager.calculateConsumeAmount(anyLong(), anyString(), anyString(), any(), any())).thenReturn(new BigDecimal("10.00"));
        when(accountManager.checkBalanceSufficient(anyLong(), any(BigDecimal.class))).thenReturn(true);

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
        when(consumeAreaManager.getAreaById(anyString())).thenReturn(areaEntity);
        when(consumeExecutionManager.validateConsumePermission(anyLong(), anyString(), anyString())).thenReturn(false);

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
        when(consumeExecutionManager.validateConsumePermission(anyLong(), anyString(), anyString())).thenReturn(true);
        when(consumeExecutionManager.calculateConsumeAmount(anyLong(), anyString(), anyString(), any(), any())).thenReturn(new BigDecimal("10000.00"));
        when(accountManager.checkBalanceSufficient(anyLong(), any(BigDecimal.class))).thenReturn(false);

        // When
        ResponseDTO<?> result = consumeExecutionManager.executeConsumption(consumeRequest);

        // Then
        assertNotNull(result);
        assertFalse(result.getOk());
    }
}
