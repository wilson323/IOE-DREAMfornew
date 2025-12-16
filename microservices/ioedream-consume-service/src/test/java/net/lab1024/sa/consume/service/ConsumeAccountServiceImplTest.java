package net.lab1024.sa.consume.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.consume.dao.AccountDao;
import net.lab1024.sa.consume.domain.dto.RechargeRequestDTO;
import net.lab1024.sa.consume.domain.entity.AccountEntity;
import net.lab1024.sa.consume.domain.form.AccountQueryForm;
import net.lab1024.sa.consume.domain.vo.AccountVO;
import net.lab1024.sa.consume.service.impl.ConsumeAccountServiceImpl;

/**
 * ConsumeAccountServiceImpl Unit Test
 * <p>
 * Target Coverage: >= 80%
 * Test Scope: Core business methods of ConsumeAccountServiceImpl
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConsumeAccountServiceImpl Unit Test")
@SuppressWarnings("unchecked")
class ConsumeAccountServiceImplTest {

    @Mock
    private AccountDao accountDao;

    @InjectMocks
    private ConsumeAccountServiceImpl consumeAccountServiceImpl;

    @BeforeEach
    void setUp() {
        // Prepare test data
    }

    @Test
    @DisplayName("Test getAccountByUserId - Success Scenario")
    void test_getAccountByUserId_Success() {
        // Given
        Long userId = 1L;
        AccountEntity account = new AccountEntity();
        account.setAccountId(1L);
        account.setUserId(userId);
        account.setAccountNo("ACC001");
        account.setBalance(new BigDecimal("1000.00"));
        account.setStatus(1);
        when(accountDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(account);

        // When
        AccountVO result = consumeAccountServiceImpl.getAccountByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("ACC001", result.getAccountNo());
        verify(accountDao, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("Test getAccountByUserId - Account Not Found")
    void test_getAccountByUserId_NotFound() {
        // Given
        Long userId = 999L;
        when(accountDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // When
        AccountVO result = consumeAccountServiceImpl.getAccountByUserId(userId);

        // Then
        assertNull(result);
        verify(accountDao, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("Test getAccountById - Success Scenario")
    void test_getAccountById_Success() {
        // Given
        Long accountId = 1L;
        AccountEntity account = new AccountEntity();
        account.setAccountId(accountId);
        account.setAccountNo("ACC001");
        account.setBalance(new BigDecimal("1000.00"));
        account.setStatus(1);
        when(accountDao.selectById(accountId)).thenReturn(account);

        // When
        ResponseDTO<AccountVO> result = consumeAccountServiceImpl.getAccountById(accountId);

        // Then
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        assertEquals(accountId, result.getData().getAccountId());
        verify(accountDao, times(1)).selectById(accountId);
    }

    @Test
    @DisplayName("Test getAccountById - Account Not Found")
    void test_getAccountById_NotFound() {
        // Given
        Long accountId = 999L;
        when(accountDao.selectById(accountId)).thenReturn(null);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            consumeAccountServiceImpl.getAccountById(accountId);
        });
        verify(accountDao, times(1)).selectById(accountId);
    }

    @Test
    @DisplayName("Test getAccountByNo - Success Scenario")
    void test_getAccountByNo_Success() {
        // Given
        String accountNo = "ACC001";
        AccountEntity account = new AccountEntity();
        account.setAccountId(1L);
        account.setAccountNo(accountNo);
        account.setBalance(new BigDecimal("1000.00"));
        account.setStatus(1);
        when(accountDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(account);

        // When
        ResponseDTO<AccountVO> result = consumeAccountServiceImpl.getAccountByNo(accountNo);

        // Then
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        assertEquals(accountNo, result.getData().getAccountNo());
        verify(accountDao, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("Test getAccountByNo - Account Not Found")
    void test_getAccountByNo_NotFound() {
        // Given
        String accountNo = "NONEXISTENT";
        when(accountDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            consumeAccountServiceImpl.getAccountByNo(accountNo);
        });
        verify(accountDao, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("Test recharge - Success Scenario")
    void test_recharge_Success() {
        // Given
        RechargeRequestDTO request = new RechargeRequestDTO();
        request.setAccountId(1L);
        request.setAmount(new BigDecimal("100.00"));

        // When
        ResponseDTO<Void> result = consumeAccountServiceImpl.recharge(request);

        // Then
        assertTrue(result.getOk());
    }

    @Test
    @DisplayName("Test deduct - Success Scenario")
    void test_deduct_Success() {
        // Given
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("50.00");

        // When
        ResponseDTO<Void> result = consumeAccountServiceImpl.deduct(accountId, amount);

        // Then
        assertTrue(result.getOk());
    }

    @Test
    @DisplayName("Test refund - Success Scenario")
    void test_refund_Success() {
        // Given
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("30.00");
        String reason = "Test refund";

        // When
        ResponseDTO<Void> result = consumeAccountServiceImpl.refund(accountId, amount, reason);

        // Then
        assertTrue(result.getOk());
    }

    @Test
    @DisplayName("Test freeze - Success Scenario")
    void test_freeze_Success() {
        // Given
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("20.00");

        // When
        ResponseDTO<Void> result = consumeAccountServiceImpl.freeze(accountId, amount);

        // Then
        assertTrue(result.getOk());
    }

    @Test
    @DisplayName("Test freezeAccount - Success Scenario")
    void test_freezeAccount_Success() {
        // Given
        Long accountId = 1L;
        String reason = "Test freeze";
        Integer freezeDays = 7;

        // When
        boolean result = consumeAccountServiceImpl.freezeAccount(accountId, reason, freezeDays);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Test unfreezeAccount - Success Scenario")
    void test_unfreezeAccount_Success() {
        // Given
        Long accountId = 1L;
        String reason = "Test unfreeze";

        // When
        boolean result = consumeAccountServiceImpl.unfreezeAccount(accountId, reason);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Test rechargeAccount - Success Scenario")
    void test_rechargeAccount_Success() {
        // Given
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("200.00");
        String rechargeType = "MANUAL";
        String remark = "Test recharge";

        // When
        boolean result = consumeAccountServiceImpl.rechargeAccount(accountId, amount, rechargeType, remark);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Test setAccountLimit - Success Scenario")
    void test_setAccountLimit_Success() {
        // Given
        Long accountId = 1L;
        BigDecimal dailyLimit = new BigDecimal("500.00");
        BigDecimal monthlyLimit = new BigDecimal("10000.00");

        // When
        boolean result = consumeAccountServiceImpl.setAccountLimit(accountId, dailyLimit, monthlyLimit);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Test queryAccountPage - Success Scenario")
    void test_queryAccountPage_Success() {
        // Given
        AccountQueryForm queryForm = new AccountQueryForm();
        queryForm.setPageNum(1);
        queryForm.setPageSize(10);

        // When
        ResponseDTO<IPage<AccountVO>> result = consumeAccountServiceImpl.queryAccountPage(queryForm);

        // Then
        assertTrue(result.getOk());
        assertNotNull(result.getData());
    }

    @Test
    @DisplayName("Test existsAccount - Success Scenario")
    void test_existsAccount_Success() {
        // Given
        Long accountId = 1L;
        when(accountDao.selectById(accountId)).thenReturn(new AccountEntity());

        // When
        boolean result = consumeAccountServiceImpl.existsAccount(accountId);

        // Then
        assertTrue(result);
        verify(accountDao, times(1)).selectById(accountId);
    }

    @Test
    @DisplayName("Test existsAccount - Account Not Exists")
    void test_existsAccount_NotExists() {
        // Given
        Long accountId = 999L;
        when(accountDao.selectById(accountId)).thenReturn(null);

        // When
        boolean result = consumeAccountServiceImpl.existsAccount(accountId);

        // Then
        assertFalse(result);
        verify(accountDao, times(1)).selectById(accountId);
    }

    @Test
    @DisplayName("Test checkBalance - Success Scenario")
    void test_checkBalance_Success() {
        // Given
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("50.00");
        AccountEntity account = new AccountEntity();
        account.setAccountId(accountId);
        account.setBalance(new BigDecimal("1000.00"));
        account.setFrozenBalance(new BigDecimal("100.00"));
        when(accountDao.selectById(accountId)).thenReturn(account);

        // When
        boolean result = consumeAccountServiceImpl.checkBalance(accountId, amount);

        // Then
        assertTrue(result);
        verify(accountDao, times(1)).selectById(accountId);
    }

    @Test
    @DisplayName("Test checkBalance - Insufficient Balance")
    void test_checkBalance_Insufficient() {
        // Given
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("1000.00");
        AccountEntity account = new AccountEntity();
        account.setAccountId(accountId);
        account.setBalance(new BigDecimal("100.00"));
        account.setFrozenBalance(new BigDecimal("50.00"));
        when(accountDao.selectById(accountId)).thenReturn(account);

        // When
        boolean result = consumeAccountServiceImpl.checkBalance(accountId, amount);

        // Then
        assertFalse(result);
        verify(accountDao, times(1)).selectById(accountId);
    }

    @Test
    @DisplayName("Test getUserBalanceInfo - Success Scenario")
    void test_getUserBalanceInfo_Success() {
        // Given
        Long userId = 1L;
        AccountEntity account = new AccountEntity();
        account.setAccountId(1L);
        account.setUserId(userId);
        account.setBalance(new BigDecimal("1000.00"));
        account.setFrozenBalance(new BigDecimal("100.00"));
        when(accountDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(account);

        // When
        Map<String, Object> result = consumeAccountServiceImpl.getUserBalanceInfo(userId);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("balance"));
        assertTrue(result.containsKey("frozenBalance"));
        assertTrue(result.containsKey("availableBalance"));
        verify(accountDao, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("Test getUserBalanceInfo - Account Not Found")
    void test_getUserBalanceInfo_NotFound() {
        // Given
        Long userId = 999L;
        when(accountDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // When
        Map<String, Object> result = consumeAccountServiceImpl.getUserBalanceInfo(userId);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.get("balance"));
        verify(accountDao, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("Test getAccountBalance - Success Scenario")
    void test_getAccountBalance_Success() {
        // Given
        Long accountId = 1L;
        AccountEntity account = new AccountEntity();
        account.setAccountId(accountId);
        account.setBalance(new BigDecimal("1000.00"));
        when(accountDao.selectById(accountId)).thenReturn(account);

        // When
        ResponseDTO<BigDecimal> result = consumeAccountServiceImpl.getAccountBalance(accountId);

        // Then
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        assertEquals(new BigDecimal("1000.00"), result.getData());
        verify(accountDao, times(1)).selectById(accountId);
    }

    @Test
    @DisplayName("Test getAccountBalance - Account Not Found")
    void test_getAccountBalance_NotFound() {
        // Given
        Long accountId = 999L;
        when(accountDao.selectById(accountId)).thenReturn(null);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            consumeAccountServiceImpl.getAccountBalance(accountId);
        });
        verify(accountDao, times(1)).selectById(accountId);
    }

    @Test
    @DisplayName("Test existsAccount - Success Scenario (by accountId)")
    void test_existsAccount_ByAccountId_Success() {
        // Given
        Long accountId = 1L;
        when(accountDao.selectById(accountId)).thenReturn(new AccountEntity());

        // When
        Boolean result = consumeAccountServiceImpl.existsAccount(accountId);

        // Then
        assertTrue(result);
        verify(accountDao, times(1)).selectById(accountId);
    }

    @Test
    @DisplayName("Test existsAccount - Account Not Exists (by accountId)")
    void test_existsAccount_ByAccountId_NotExists() {
        // Given
        Long accountId = 999L;
        when(accountDao.selectById(accountId)).thenReturn(null);

        // When
        Boolean result = consumeAccountServiceImpl.existsAccount(accountId);

        // Then
        assertFalse(result);
        verify(accountDao, times(1)).selectById(accountId);
    }

}


