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

import net.lab1024.sa.consume.dao.AccountDao;
import net.lab1024.sa.consume.domain.entity.AccountEntity;
import net.lab1024.sa.consume.manager.impl.AccountManagerImpl;

/**
 * AccountManager单元测试
 * <p>
 * 测试范围：账户管理业务逻辑
 * 目标：提升测试覆盖率至80%+
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccountManager单元测试")
class AccountManagerTest {

    @Mock
    private AccountDao accountDao;

    @InjectMocks
    private AccountManagerImpl accountManager;

    private AccountEntity testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new AccountEntity();
        testAccount.setAccountId(1L);
        testAccount.setUserId(1001L);
        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setFrozenAmount(new BigDecimal("100.00"));
        testAccount.setStatus(1); // 正常状态
    }

    // ==================== getAccountByUserId 测试 ====================

    @Test
    @DisplayName("测试根据用户ID获取账户-成功")
    void testGetAccountByUserId_Success() {
        // Given
        Long userId = 1001L;
        when(accountDao.selectByUserId(userId)).thenReturn(testAccount);

        // When
        AccountEntity result = accountManager.getAccountByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(new BigDecimal("1000.00"), result.getBalance());
        verify(accountDao, times(1)).selectByUserId(userId);
    }

    @Test
    @DisplayName("测试根据用户ID获取账户-账户不存在")
    void testGetAccountByUserId_NotFound() {
        // Given
        Long userId = 9999L;
        when(accountDao.selectByUserId(userId)).thenReturn(null);

        // When
        AccountEntity result = accountManager.getAccountByUserId(userId);

        // Then
        assertNull(result);
        verify(accountDao, times(1)).selectByUserId(userId);
    }

    @Test
    @DisplayName("测试根据用户ID获取账户-数据库异常")
    void testGetAccountByUserId_DatabaseException() {
        // Given
        Long userId = 1001L;
        when(accountDao.selectByUserId(userId)).thenThrow(new RuntimeException("数据库连接失败"));

        // When
        AccountEntity result = accountManager.getAccountByUserId(userId);

        // Then
        assertNull(result);
        verify(accountDao, times(1)).selectByUserId(userId);
    }

    // ==================== getAccountById 测试 ====================

    @Test
    @DisplayName("测试根据账户ID获取账户-成功")
    void testGetAccountById_Success() {
        // Given
        Long accountId = 1L;
        when(accountDao.selectById(accountId)).thenReturn(testAccount);

        // When
        AccountEntity result = accountManager.getAccountById(accountId);

        // Then
        assertNotNull(result);
        assertEquals(accountId, result.getAccountId());
        verify(accountDao, times(1)).selectById(accountId);
    }

    @Test
    @DisplayName("测试根据账户ID获取账户-账户不存在")
    void testGetAccountById_NotFound() {
        // Given
        Long accountId = 9999L;
        when(accountDao.selectById(accountId)).thenReturn(null);

        // When
        AccountEntity result = accountManager.getAccountById(accountId);

        // Then
        assertNull(result);
        verify(accountDao, times(1)).selectById(accountId);
    }

    // ==================== deductBalance 测试 ====================

    @Test
    @DisplayName("测试扣减账户余额-成功")
    void testDeductBalance_Success() {
        // Given
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("100.00");
        // BigDecimal originalBalance = testAccount.getBalance(); // 未使用

        when(accountDao.selectById(accountId)).thenReturn(testAccount);
        when(accountDao.updateById(any(AccountEntity.class))).thenReturn(1);

        // When
        boolean result = accountManager.deductBalance(accountId, amount);

        // Then
        assertTrue(result);
        verify(accountDao, times(1)).selectById(accountId);
        verify(accountDao, times(1)).updateById(any(AccountEntity.class));
    }

    @Test
    @DisplayName("测试扣减账户余额-账户不存在")
    void testDeductBalance_AccountNotFound() {
        // Given
        Long accountId = 9999L;
        BigDecimal amount = new BigDecimal("100.00");

        when(accountDao.selectById(accountId)).thenReturn(null);

        // When
        boolean result = accountManager.deductBalance(accountId, amount);

        // Then
        assertFalse(result);
        verify(accountDao, times(1)).selectById(accountId);
        verify(accountDao, never()).updateById(any(AccountEntity.class));
    }

    @Test
    @DisplayName("测试扣减账户余额-余额不足")
    void testDeductBalance_InsufficientBalance() {
        // Given
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("2000.00"); // 大于余额

        testAccount.setBalance(new BigDecimal("1000.00"));
        when(accountDao.selectById(accountId)).thenReturn(testAccount);

        // When
        boolean result = accountManager.deductBalance(accountId, amount);

        // Then
        assertFalse(result);
        verify(accountDao, times(1)).selectById(accountId);
        verify(accountDao, never()).updateById(any(AccountEntity.class));
    }

    @Test
    @DisplayName("测试扣减账户余额-金额为null")
    void testDeductBalance_AmountIsNull() {
        // Given
        Long accountId = 1L;
        BigDecimal amount = null;

        when(accountDao.selectById(accountId)).thenReturn(testAccount);

        // When
        boolean result = accountManager.deductBalance(accountId, amount);

        // Then
        assertFalse(result);
        verify(accountDao, times(1)).selectById(accountId);
        verify(accountDao, never()).updateById(any(AccountEntity.class));
    }

    @Test
    @DisplayName("测试扣减账户余额-金额为负数")
    void testDeductBalance_NegativeAmount() {
        // Given
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("-100.00");

        when(accountDao.selectById(accountId)).thenReturn(testAccount);

        // When
        boolean result = accountManager.deductBalance(accountId, amount);

        // Then
        assertFalse(result);
        verify(accountDao, times(1)).selectById(accountId);
        verify(accountDao, never()).updateById(any(AccountEntity.class));
    }

    @Test
    @DisplayName("测试扣减账户余额-余额为null")
    void testDeductBalance_BalanceIsNull() {
        // Given
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("100.00");

        testAccount.setBalance((BigDecimal) null);
        when(accountDao.selectById(accountId)).thenReturn(testAccount);

        // When
        boolean result = accountManager.deductBalance(accountId, amount);

        // Then
        assertFalse(result);
        verify(accountDao, times(1)).selectById(accountId);
        verify(accountDao, never()).updateById(any(AccountEntity.class));
    }

    // ==================== addBalance 测试 ====================

    @Test
    @DisplayName("测试增加账户余额-成功")
    void testAddBalance_Success() {
        // Given
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("200.00");
        // BigDecimal originalBalance = testAccount.getBalance(); // 未使用

        when(accountDao.selectById(accountId)).thenReturn(testAccount);
        when(accountDao.updateById(any(AccountEntity.class))).thenReturn(1);

        // When
        boolean result = accountManager.addBalance(accountId, amount);

        // Then
        assertTrue(result);
        verify(accountDao, times(1)).selectById(accountId);
        verify(accountDao, times(1)).updateById(any(AccountEntity.class));
    }

    @Test
    @DisplayName("测试增加账户余额-账户不存在")
    void testAddBalance_AccountNotFound() {
        // Given
        Long accountId = 9999L;
        BigDecimal amount = new BigDecimal("200.00");

        when(accountDao.selectById(accountId)).thenReturn(null);

        // When
        boolean result = accountManager.addBalance(accountId, amount);

        // Then
        assertFalse(result);
        verify(accountDao, times(1)).selectById(accountId);
        verify(accountDao, never()).updateById(any(AccountEntity.class));
    }

    @Test
    @DisplayName("测试增加账户余额-金额为null")
    void testAddBalance_AmountIsNull() {
        // Given
        Long accountId = 1L;
        BigDecimal amount = null;

        when(accountDao.selectById(accountId)).thenReturn(testAccount);

        // When
        boolean result = accountManager.addBalance(accountId, amount);

        // Then
        assertFalse(result);
        verify(accountDao, times(1)).selectById(accountId);
        verify(accountDao, never()).updateById(any(AccountEntity.class));
    }

    @Test
    @DisplayName("测试增加账户余额-余额为null")
    void testAddBalance_BalanceIsNull() {
        // Given
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("200.00");

        testAccount.setBalance((BigDecimal) null);
        when(accountDao.selectById(accountId)).thenReturn(testAccount);

        // When
        boolean result = accountManager.addBalance(accountId, amount);

        // Then
        assertTrue(result); // 余额为null时，应该设置为amount
        verify(accountDao, times(1)).selectById(accountId);
        verify(accountDao, times(1)).updateById(any(AccountEntity.class));
    }

    // ==================== checkBalanceSufficient 测试 ====================

    @Test
    @DisplayName("测试检查余额是否充足-余额充足")
    void testCheckBalanceSufficient_Sufficient() {
        // Given
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("500.00");

        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setFrozenAmount(new BigDecimal("100.00"));
        when(accountDao.selectById(accountId)).thenReturn(testAccount);

        // When
        boolean result = accountManager.checkBalanceSufficient(accountId, amount);

        // Then
        assertTrue(result);
        verify(accountDao, times(1)).selectById(accountId);
    }

    @Test
    @DisplayName("测试检查余额是否充足-余额不足")
    void testCheckBalanceSufficient_Insufficient() {
        // Given
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("1500.00"); // 大于可用余额

        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setFrozenAmount(new BigDecimal("100.00"));
        when(accountDao.selectById(accountId)).thenReturn(testAccount);

        // When
        boolean result = accountManager.checkBalanceSufficient(accountId, amount);

        // Then
        assertFalse(result);
        verify(accountDao, times(1)).selectById(accountId);
    }

    @Test
    @DisplayName("测试检查余额是否充足-账户不存在")
    void testCheckBalanceSufficient_AccountNotFound() {
        // Given
        Long accountId = 9999L;
        BigDecimal amount = new BigDecimal("500.00");

        when(accountDao.selectById(accountId)).thenReturn(null);

        // When
        boolean result = accountManager.checkBalanceSufficient(accountId, amount);

        // Then
        assertFalse(result);
        verify(accountDao, times(1)).selectById(accountId);
    }

    @Test
    @DisplayName("测试检查余额是否充足-金额为null")
    void testCheckBalanceSufficient_AmountIsNull() {
        // Given
        Long accountId = 1L;
        BigDecimal amount = null;

        when(accountDao.selectById(accountId)).thenReturn(testAccount);

        // When
        boolean result = accountManager.checkBalanceSufficient(accountId, amount);

        // Then
        assertFalse(result);
        verify(accountDao, times(1)).selectById(accountId);
    }

    @Test
    @DisplayName("测试检查余额是否充足-余额为null")
    void testCheckBalanceSufficient_BalanceIsNull() {
        // Given
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("100.00");

        testAccount.setBalance((BigDecimal) null);
        when(accountDao.selectById(accountId)).thenReturn(testAccount);

        // When
        boolean result = accountManager.checkBalanceSufficient(accountId, amount);

        // Then
        assertFalse(result);
        verify(accountDao, times(1)).selectById(accountId);
    }

    @Test
    @DisplayName("测试检查余额是否充足-精确计算可用余额")
    void testCheckBalanceSufficient_ExactCalculation() {
        // Given
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("900.00"); // 正好等于可用余额

        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setFrozenAmount(new BigDecimal("100.00"));
        // 可用余额 = 1000 - 100 = 900
        when(accountDao.selectById(accountId)).thenReturn(testAccount);

        // When
        boolean result = accountManager.checkBalanceSufficient(accountId, amount);

        // Then
        assertTrue(result); // 可用余额正好等于所需金额
        verify(accountDao, times(1)).selectById(accountId);
    }
}
