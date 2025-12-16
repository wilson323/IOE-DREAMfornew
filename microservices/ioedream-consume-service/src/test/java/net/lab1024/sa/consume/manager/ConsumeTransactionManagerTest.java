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

import net.lab1024.sa.common.consume.entity.ConsumeRecordEntity;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.consume.dao.AccountDao;
import net.lab1024.sa.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.consume.domain.dto.ConsumeRequestDTO;
import net.lab1024.sa.common.consume.entity.AccountEntity;

/**
 * ConsumeTransactionManager单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：ConsumeTransactionManager核心业务方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-11
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConsumeTransactionManager单元测试")
class ConsumeTransactionManagerTest {
    @Mock
    private ConsumeRecordDao consumeRecordDao;
    @Mock
    private AccountDao accountDao;
    @Mock
    private GatewayServiceClient gatewayServiceClient;
    @InjectMocks
    private ConsumeTransactionManager consumeTransactionManager;

    @BeforeEach
    void setUp() {
        // 准备测试数据
    }

    @Test
    @DisplayName("executeConsumeTransaction - 成功场景")
    void test_executeConsumeTransaction_Success() {
        // Given
        ConsumeRequestDTO request = new ConsumeRequestDTO();
        request.setOrderId("ORDER001");
        request.setAccountId(1L);
        request.setUserId(1001L);
        request.setAmount(new BigDecimal("50.00"));

        AccountEntity account = new AccountEntity();
        account.setAccountId(1L);  // 修复：AccountEntity使用accountId字段
        account.setBalance(new BigDecimal("100.00"));
        account.setStatus(1);

        ConsumeRecordEntity record = new ConsumeRecordEntity();
        record.setId(100L);

        when(accountDao.selectById(1L)).thenReturn(account);
        when(accountDao.updateBalance(eq(1L), argThat(amount -> amount != null && amount.compareTo(new BigDecimal("-50.00")) == 0), eq(1001L))).thenReturn(1);
        when(consumeRecordDao.insert(any(ConsumeRecordEntity.class))).thenAnswer(invocation -> {
            ConsumeRecordEntity entity = invocation.getArgument(0);
            entity.setId(100L);
            return 1;
        });

        // When
        Long recordId = consumeTransactionManager.executeConsumeTransaction(request);

        // Then
        assertNotNull(recordId);
        assertEquals(100L, recordId);
        verify(accountDao).selectById(1L);
        verify(accountDao).updateBalance(eq(1L), argThat(amount -> amount != null && amount.compareTo(new BigDecimal("-50.00")) == 0), eq(1001L));
        verify(consumeRecordDao).insert(any(ConsumeRecordEntity.class));
    }

    @Test
    @DisplayName("executeConsumeTransaction - 账户不存在")
    void test_executeConsumeTransaction_AccountNotFound() {
        // Given
        ConsumeRequestDTO request = new ConsumeRequestDTO();
        request.setOrderId("ORDER001");
        request.setAccountId(999L);
        request.setAmount(new BigDecimal("50.00"));

        when(accountDao.selectById(999L)).thenReturn(null);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            consumeTransactionManager.executeConsumeTransaction(request);
        });
        verify(accountDao).selectById(999L);
    }

    @Test
    @DisplayName("executeConsumeTransaction - 余额不足")
    void test_executeConsumeTransaction_InsufficientBalance() {
        // Given
        ConsumeRequestDTO request = new ConsumeRequestDTO();
        request.setOrderId("ORDER001");
        request.setAccountId(1L);
        request.setAmount(new BigDecimal("150.00"));

        AccountEntity account = new AccountEntity();
        account.setAccountId(1L);  // 修复：AccountEntity使用accountId字段
        account.setBalance(new BigDecimal("100.00"));
        account.setStatus(1);

        when(accountDao.selectById(1L)).thenReturn(account);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            consumeTransactionManager.executeConsumeTransaction(request);
        });
        verify(accountDao).selectById(1L);
    }
}


