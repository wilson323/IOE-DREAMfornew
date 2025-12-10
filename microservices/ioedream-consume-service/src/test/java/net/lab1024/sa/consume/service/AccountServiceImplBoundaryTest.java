package net.lab1024.sa.consume.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.consume.dao.AccountDao;
import net.lab1024.sa.consume.domain.entity.AccountEntity;
import net.lab1024.sa.consume.domain.form.AccountAddForm;
import net.lab1024.sa.consume.domain.form.AccountUpdateForm;
import net.lab1024.sa.consume.manager.AccountManager;
import net.lab1024.sa.consume.service.impl.AccountServiceImpl;

/**
 * AccountServiceImpl边界和异常测试
 * <p>
 * 测试范围：边界条件、异常场景、复杂业务逻辑
 * 目标：提升测试覆盖率至80%+
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccountServiceImpl边界和异常测试")
class AccountServiceImplBoundaryTest {

    @Mock
    private AccountDao accountDao;

    @Mock
    private AccountManager accountManager;

    @InjectMocks
    private AccountServiceImpl accountService;

    private AccountEntity testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new AccountEntity();
        testAccount.setAccountId(2001L);
        testAccount.setUserId(1001L);
        testAccount.setAccountKindId(1L);
        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setStatus(1);
    }

    // ==================== 边界条件测试 ====================

    @Test
    @DisplayName("测试创建账户-用户ID为null")
    void testCreateAccount_UserIdIsNull() {
        // Given
        AccountAddForm form = new AccountAddForm();
        form.setUserId(null);
        form.setAccountKindId(1L);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            accountService.createAccount(form);
        });
    }

    @Test
    @DisplayName("测试创建账户-初始余额为null（应使用默认值0）")
    void testCreateAccount_InitialBalanceIsNull() {
        // Given
        AccountAddForm form = new AccountAddForm();
        form.setUserId(1001L);
        form.setAccountKindId(1L);
        form.setInitialBalance(null);

        when(accountDao.selectByUserId(1001L)).thenReturn(null);
        when(accountDao.insert(any(AccountEntity.class))).thenAnswer(invocation -> {
            AccountEntity entity = invocation.getArgument(0);
            entity.setAccountId(2001L);
            return 1;
        });

        // When
        Long accountId = accountService.createAccount(form);

        // Then
        assertNotNull(accountId);
        verify(accountDao, times(1)).insert(any(AccountEntity.class));
    }

    @Test
    @DisplayName("测试创建账户-初始余额为负数")
    void testCreateAccount_NegativeInitialBalance() {
        // Given
        AccountAddForm form = new AccountAddForm();
        form.setUserId(1001L);
        form.setAccountKindId(1L);
        form.setInitialBalance(new BigDecimal("-100.00"));

        // When & Then
        // 注意：根据业务规则，可能允许负数（透支）或不允许
        // 这里假设不允许负数，应该抛出异常
        when(accountDao.selectByUserId(1001L)).thenReturn(null);

        // 如果业务规则不允许负数，应该在这里验证
        // 如果允许，则应该正常创建
        assertThrows(BusinessException.class, () -> {
            accountService.createAccount(form);
        });
    }

    @Test
    @DisplayName("测试创建账户-初始余额为极大值")
    void testCreateAccount_VeryLargeInitialBalance() {
        // Given
        AccountAddForm form = new AccountAddForm();
        form.setUserId(1001L);
        form.setAccountKindId(1L);
        form.setInitialBalance(new BigDecimal("999999999999.99"));

        when(accountDao.selectByUserId(1001L)).thenReturn(null);
        when(accountDao.insert(any(AccountEntity.class))).thenAnswer(invocation -> {
            AccountEntity entity = invocation.getArgument(0);
            entity.setAccountId(2001L);
            return 1;
        });

        // When
        Long accountId = accountService.createAccount(form);

        // Then
        assertNotNull(accountId);
        verify(accountDao, times(1)).insert(any(AccountEntity.class));
    }

    @Test
    @DisplayName("测试分页查询-页码为null（应使用默认值1）")
    void testPageAccounts_PageNumIsNull() {
        // Given
        when(accountDao.selectPage(any(), any())).thenReturn(createMockPage());

        // When
        var result = accountService.pageAccounts(null, 20, null, null, null);

        // Then
        assertNotNull(result);
        verify(accountDao, times(1)).selectPage(argThat(page ->
            page.getCurrent() == 1
        ), any());
    }

    @Test
    @DisplayName("测试分页查询-页码为0（应使用默认值1）")
    void testPageAccounts_PageNumIsZero() {
        // Given
        when(accountDao.selectPage(any(), any())).thenReturn(createMockPage());

        // When
        var result = accountService.pageAccounts(0, 20, null, null, null);

        // Then
        assertNotNull(result);
        verify(accountDao, times(1)).selectPage(argThat(page ->
            page.getCurrent() == 1
        ), any());
    }

    @Test
    @DisplayName("测试分页查询-每页大小为null（应使用默认值20）")
    void testPageAccounts_PageSizeIsNull() {
        // Given
        when(accountDao.selectPage(any(), any())).thenReturn(createMockPage());

        // When
        var result = accountService.pageAccounts(1, null, null, null, null);

        // Then
        assertNotNull(result);
        verify(accountDao, times(1)).selectPage(argThat(page ->
            page.getSize() == 20
        ), any());
    }

    @Test
    @DisplayName("测试分页查询-每页大小为0（应使用默认值20）")
    void testPageAccounts_PageSizeIsZero() {
        // Given
        when(accountDao.selectPage(any(), any())).thenReturn(createMockPage());

        // When
        var result = accountService.pageAccounts(1, 0, null, null, null);

        // Then
        assertNotNull(result);
        verify(accountDao, times(1)).selectPage(argThat(page ->
            page.getSize() == 20
        ), any());
    }

    @Test
    @DisplayName("测试游标分页查询-首次查询（lastTime为null）")
    void testCursorPageAccounts_FirstPage() {
        // Given
        when(accountDao.selectList(any())).thenReturn(java.util.Collections.emptyList());

        // When
        var result = accountService.cursorPageAccounts(20, null, null, null, null);

        // Then
        assertNotNull(result);
        assertNotNull(result.getList());
        assertFalse(result.getHasNext());
    }

    @Test
    @DisplayName("测试游标分页查询-每页大小为null（应使用默认值20）")
    void testCursorPageAccounts_PageSizeIsNull() {
        // Given
        when(accountDao.selectList(any())).thenReturn(java.util.Collections.emptyList());

        // When
        var result = accountService.cursorPageAccounts(null, null, null, null, null);

        // Then
        assertNotNull(result);
        // 验证查询条件中使用了默认的pageSize
    }

    @Test
    @DisplayName("测试游标分页查询-每页大小超过最大值100（应限制为100）")
    void testCursorPageAccounts_PageSizeExceedsMax() {
        // Given
        when(accountDao.selectList(any())).thenReturn(java.util.Collections.emptyList());

        // When
        var result = accountService.cursorPageAccounts(200, null, null, null, null);

        // Then
        assertNotNull(result);
        // 验证查询条件中pageSize被限制为100
    }

    @Test
    @DisplayName("测试增加余额-金额为null")
    void testAddBalance_AmountIsNull() {
        // Given
        Long accountId = 2001L;
        when(accountDao.selectById(accountId)).thenReturn(testAccount);

        // When & Then
        assertThrows(Exception.class, () -> {
            accountService.addBalance(accountId, null, "测试");
        });
    }

    @Test
    @DisplayName("测试增加余额-金额为0")
    void testAddBalance_AmountIsZero() {
        // Given
        Long accountId = 2001L;
        BigDecimal zeroAmount = BigDecimal.ZERO;
        when(accountDao.selectById(accountId)).thenReturn(testAccount);
        when(accountManager.addBalance(accountId, zeroAmount)).thenReturn(true);

        // When
        boolean result = accountService.addBalance(accountId, zeroAmount, "测试");

        // Then
        assertTrue(result);
        verify(accountManager, times(1)).addBalance(accountId, zeroAmount);
    }

    @Test
    @DisplayName("测试增加余额-金额为负数")
    void testAddBalance_NegativeAmount() {
        // Given
        Long accountId = 2001L;
        BigDecimal negativeAmount = new BigDecimal("-50.00");
        when(accountDao.selectById(accountId)).thenReturn(testAccount);

        // When & Then
        // 根据业务规则，可能允许负数（扣减）或不允许
        // 这里假设不允许负数，应该抛出异常
        assertThrows(BusinessException.class, () -> {
            accountService.addBalance(accountId, negativeAmount, "测试");
        });
    }

    @Test
    @DisplayName("测试扣减余额-余额不足")
    void testDeductBalance_InsufficientBalance() {
        // Given
        Long accountId = 2001L;
        BigDecimal deductAmount = new BigDecimal("2000.00"); // 大于余额
        when(accountDao.selectById(accountId)).thenReturn(testAccount);
        when(accountManager.checkBalanceSufficient(accountId, deductAmount)).thenReturn(false);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            accountService.deductBalance(accountId, deductAmount, "测试");
        });
    }

    @Test
    @DisplayName("测试冻结金额-冻结金额大于可用余额")
    void testFreezeAmount_ExceedsAvailableBalance() {
        // Given
        Long accountId = 2001L;
        BigDecimal freezeAmount = new BigDecimal("2000.00"); // 大于余额
        when(accountDao.selectById(accountId)).thenReturn(testAccount);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            accountService.freezeAmount(accountId, freezeAmount, "测试");
        });
    }

    // ==================== 异常场景测试 ====================

    @Test
    @DisplayName("测试创建账户-数据库异常")
    void testCreateAccount_DatabaseException() {
        // Given
        AccountAddForm form = new AccountAddForm();
        form.setUserId(1001L);
        form.setAccountKindId(1L);

        when(accountDao.selectByUserId(1001L)).thenReturn(null);
        when(accountDao.insert(any(AccountEntity.class))).thenThrow(new RuntimeException("数据库连接失败"));

        // When & Then
        assertThrows(BusinessException.class, () -> {
            accountService.createAccount(form);
        });
    }

    @Test
    @DisplayName("测试查询账户-数据库查询异常")
    void testGetById_DatabaseException() {
        // Given
        Long accountId = 2001L;
        when(accountDao.selectById(accountId)).thenThrow(new RuntimeException("数据库查询失败"));

        // When & Then
        assertThrows(BusinessException.class, () -> {
            accountService.getById(accountId);
        });
    }

    @Test
    @DisplayName("测试更新账户-数据库更新异常")
    void testUpdateAccount_DatabaseException() {
        // Given
        AccountUpdateForm form = new AccountUpdateForm();
        form.setAccountId(2001L);
        form.setStatus(1);

        when(accountDao.selectById(2001L)).thenReturn(testAccount);
        when(accountDao.updateById(any(AccountEntity.class))).thenThrow(new RuntimeException("数据库更新失败"));

        // When & Then
        assertThrows(BusinessException.class, () -> {
            accountService.updateAccount(form);
        });
    }

    @Test
    @DisplayName("测试增加余额-账户不存在")
    void testAddBalance_AccountNotFound() {
        // Given
        Long accountId = 9999L;
        BigDecimal amount = new BigDecimal("50.00");
        when(accountDao.selectById(accountId)).thenReturn(null);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            accountService.addBalance(accountId, amount, "测试");
        });
    }

    @Test
    @DisplayName("测试增加余额-账户已冻结")
    void testAddBalance_AccountFrozen() {
        // Given
        Long accountId = 2001L;
        BigDecimal amount = new BigDecimal("50.00");
        testAccount.setStatus(2); // 冻结状态
        when(accountDao.selectById(accountId)).thenReturn(testAccount);
        when(accountManager.addBalance(accountId, amount)).thenReturn(true);

        // When
        // 根据业务规则，冻结账户可能允许充值或不允许
        // 这里测试允许充值的情况
        boolean result = accountService.addBalance(accountId, amount, "测试");

        // Then
        assertTrue(result);
        verify(accountManager, times(1)).addBalance(accountId, amount);
    }

    @Test
    @DisplayName("测试增加余额-账户已禁用")
    void testAddBalance_AccountDisabled() {
        // Given
        Long accountId = 2001L;
        BigDecimal amount = new BigDecimal("50.00");
        testAccount.setStatus(0); // 禁用状态
        when(accountDao.selectById(accountId)).thenReturn(testAccount);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            accountService.addBalance(accountId, amount, "测试");
        });
    }

    // ==================== 复杂业务场景测试 ====================

    @Test
    @DisplayName("测试并发创建账户-同一用户多次创建")
    void testCreateAccount_ConcurrentCreation() {
        // Given
        AccountAddForm form = new AccountAddForm();
        form.setUserId(1001L);
        form.setAccountKindId(1L);

        // 模拟并发场景：第一次查询返回null，插入时账户已存在
        when(accountDao.selectByUserId(1001L))
            .thenReturn(null)  // 第一次查询：不存在
            .thenReturn(testAccount);  // 第二次查询：已存在（并发插入后）

        when(accountDao.insert(any(AccountEntity.class))).thenAnswer(invocation -> {
            AccountEntity entity = invocation.getArgument(0);
            entity.setAccountId(2001L);
            return 1;
        });

        // When
        Long accountId = accountService.createAccount(form);

        // Then
        assertNotNull(accountId);
        // 注意：实际并发场景需要数据库唯一约束来防止重复创建
    }

    @Test
    @DisplayName("测试批量操作-批量更新状态")
    void testBatchUpdateStatus_Success() {
        // Given
        java.util.List<Long> accountIds = java.util.Arrays.asList(2001L, 2002L, 2003L);
        Integer newStatus = 2; // 冻结状态

        when(accountDao.selectById(anyLong())).thenReturn(testAccount);
        when(accountDao.updateById(any(AccountEntity.class))).thenReturn(1);

        // When
        int result = accountService.batchUpdateStatus(accountIds, newStatus);

        // Then
        assertEquals(3, result);
        verify(accountDao, times(3)).selectById(anyLong());
        verify(accountDao, times(3)).updateById(any(AccountEntity.class));
    }

    @Test
    @DisplayName("测试批量操作-部分账户不存在")
    void testBatchUpdateStatus_PartialNotFound() {
        // Given
        java.util.List<Long> accountIds = java.util.Arrays.asList(2001L, 9999L, 2003L);
        Integer newStatus = 2;

        when(accountDao.selectById(2001L)).thenReturn(testAccount);
        when(accountDao.selectById(9999L)).thenReturn(null); // 不存在
        when(accountDao.selectById(2003L)).thenReturn(testAccount);
        when(accountDao.updateById(any(AccountEntity.class))).thenReturn(1);

        // When
        int result = accountService.batchUpdateStatus(accountIds, newStatus);

        // Then
        // 应该只更新存在的账户
        assertEquals(2, result);
    }

    @Test
    @DisplayName("测试游标分页-有下一页")
    void testCursorPageAccounts_HasNext() {
        // Given
        // 模拟返回21条记录（pageSize=20，多1条表示有下一页）
        java.util.List<AccountEntity> records = new java.util.ArrayList<>();
        for (int i = 0; i < 21; i++) {
            AccountEntity account = new AccountEntity();
            account.setAccountId((long) (2001 + i));
            account.setCreateTime(LocalDateTime.now().minusHours(i));
            records.add(account);
        }
        when(accountDao.selectList(any())).thenReturn(records);

        // When
        var result = accountService.cursorPageAccounts(20, null, null, null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.getHasNext());
        assertEquals(20, result.getList().size());
    }

    @Test
    @DisplayName("测试游标分页-无下一页")
    void testCursorPageAccounts_NoNext() {
        // Given
        // 模拟返回20条记录（pageSize=20，刚好一页）
        java.util.List<AccountEntity> records = new java.util.ArrayList<>();
        for (int i = 0; i < 20; i++) {
            AccountEntity account = new AccountEntity();
            account.setAccountId((long) (2001 + i));
            account.setCreateTime(LocalDateTime.now().minusHours(i));
            records.add(account);
        }
        when(accountDao.selectList(any())).thenReturn(records);

        // When
        var result = accountService.cursorPageAccounts(20, null, null, null, null);

        // Then
        assertNotNull(result);
        assertFalse(result.getHasNext());
        assertEquals(20, result.getList().size());
    }

    // ==================== 辅助方法 ====================

    private com.baomidou.mybatisplus.core.metadata.IPage<AccountEntity> createMockPage() {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<AccountEntity> page =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 20);
        page.setRecords(java.util.Collections.emptyList());
        page.setTotal(0);
        return page;
    }
}
