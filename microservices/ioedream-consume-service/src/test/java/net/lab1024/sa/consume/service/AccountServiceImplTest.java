package net.lab1024.sa.consume.service;

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

import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.consume.dao.AccountDao;
import net.lab1024.sa.consume.entity.AccountEntity;
import net.lab1024.sa.consume.domain.form.AccountAddForm;
import net.lab1024.sa.consume.domain.form.AccountUpdateForm;
import net.lab1024.sa.consume.manager.AccountManager;
import net.lab1024.sa.consume.service.impl.AccountServiceImpl;

/**
 * AccountServiceImpl单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：账户服务核心业务方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccountServiceImpl单元测试")
class AccountServiceImplTest {

    @Mock
    private AccountDao accountDao;

    @Mock
    private AccountManager accountManager;

    @InjectMocks
    private AccountServiceImpl accountService;

    private AccountAddForm addForm;
    private AccountEntity accountEntity;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        addForm = new AccountAddForm();
        addForm.setUserId(1001L);
        addForm.setAccountKindId(1L);
        addForm.setInitialBalance(new BigDecimal("100.00"));

        accountEntity = new AccountEntity();
        accountEntity.setAccountId(2001L);
        accountEntity.setUserId(1001L);
        accountEntity.setAccountKindId(1L);
        accountEntity.setBalance(new BigDecimal("100.00")); // 使用BigDecimal类型
        accountEntity.setStatus(1); // 正常状态
        accountEntity.setVersion(0); // 乐观锁版本号，避免 NPE
    }

    @Test
    @DisplayName("测试创建账户-成功场景")
    void testCreateAccount_Success() {
        // Given
        when(accountDao.selectByUserId(1001L)).thenReturn(null);
        when(accountDao.insert(any(AccountEntity.class))).thenAnswer(invocation -> {
            AccountEntity entity = invocation.getArgument(0);
            entity.setAccountId(2001L);
            return 1;
        });

        // When
        Long accountId = accountService.createAccount(addForm);

        // Then
        assertNotNull(accountId);
        assertEquals(2001L, accountId);
        verify(accountDao, times(1)).selectByUserId(1001L);
        verify(accountDao, times(1)).insert(any(AccountEntity.class));
    }

    @Test
    @DisplayName("测试创建账户-账户已存在")
    void testCreateAccount_AlreadyExists() {
        // Given
        when(accountDao.selectByUserId(1001L)).thenReturn(accountEntity);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            accountService.createAccount(addForm);
        });
        verify(accountDao, times(1)).selectByUserId(1001L);
        verify(accountDao, never()).insert(any(AccountEntity.class));
    }

    @Test
    @DisplayName("测试更新账户-成功场景")
    void testUpdateAccount_Success() {
        // Given
        AccountUpdateForm updateForm = new AccountUpdateForm();
        updateForm.setAccountId(2001L);
        updateForm.setAccountKindId(2L);
        updateForm.setStatus(1);

        when(accountDao.selectById(2001L)).thenReturn(accountEntity);
        when(accountDao.updateById(any(AccountEntity.class))).thenReturn(1);

        // When
        boolean result = accountService.updateAccount(updateForm);

        // Then
        assertTrue(result);
        verify(accountDao, times(1)).selectById(2001L);
        verify(accountDao, times(1)).updateById(any(AccountEntity.class));
    }

    @Test
    @DisplayName("测试更新账户-账户不存在")
    void testUpdateAccount_NotFound() {
        // Given
        AccountUpdateForm updateForm = new AccountUpdateForm();
        updateForm.setAccountId(9999L);

        when(accountDao.selectById(9999L)).thenReturn(null);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            accountService.updateAccount(updateForm);
        });
        verify(accountDao, times(1)).selectById(9999L);
        verify(accountDao, never()).updateById(any(AccountEntity.class));
    }

    @Test
    @DisplayName("测试查询账户详情-成功场景")
    void testGetAccountDetail_Success() {
        // Given
        Long accountId = 2001L;
        when(accountManager.getAccountById(accountId)).thenReturn(accountEntity);

        // When
        AccountEntity result = accountService.getAccountDetail(accountId);

        // Then
        assertNotNull(result);
        assertEquals(accountId, result.getAccountId());
        assertEquals(1001L, result.getUserId());
        verify(accountManager, times(1)).getAccountById(accountId);
    }

    @Test
    @DisplayName("测试查询账户详情-账户不存在")
    void testGetAccountDetail_NotFound() {
        // Given
        Long accountId = 9999L;
        when(accountManager.getAccountById(accountId)).thenReturn(null);

        // When & Then
        assertThrows(BusinessException.class, () -> accountService.getAccountDetail(accountId));
        verify(accountManager, times(1)).getAccountById(accountId);
    }

    @Test
    @DisplayName("测试账户充值-成功场景")
    void testAddBalance_Success() {
        // Given
        Long accountId = 2001L;
        BigDecimal amount = new BigDecimal("50.00");
        String reason = "账户充值";
        when(accountManager.addBalance(accountId, amount)).thenReturn(true);

        // When
        boolean result = accountService.addBalance(accountId, amount, reason);

        // Then
        assertTrue(result);
        verify(accountManager, times(1)).addBalance(accountId, amount);
    }

    @Test
    @DisplayName("测试账户冻结-成功场景")
    void testFreezeAccount_Success() {
        // Given
        Long accountId = 2001L;
        String reason = "账户异常";
        when(accountDao.selectById(accountId)).thenReturn(accountEntity);
        when(accountDao.updateById(any(AccountEntity.class))).thenReturn(1);

        // When
        boolean result = accountService.freezeAccount(accountId, reason);

        // Then
        assertTrue(result);
        verify(accountDao, times(1)).selectById(accountId);
        verify(accountDao, times(1)).updateById(any(AccountEntity.class));
    }
}


