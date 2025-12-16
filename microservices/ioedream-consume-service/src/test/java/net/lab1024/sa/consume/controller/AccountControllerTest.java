package net.lab1024.sa.consume.controller;

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

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.domain.entity.AccountEntity;
import net.lab1024.sa.consume.domain.form.AccountAddForm;
import net.lab1024.sa.consume.domain.form.AccountUpdateForm;
import net.lab1024.sa.consume.service.AccountService;

/**
 * AccountController单元测试
 * <p>
 * 测试范围：账户管理REST API接口
 * 目标：提升测试覆盖率至70%+
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccountController单元测试")
class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    private AccountAddForm accountAddForm;
    private AccountUpdateForm accountUpdateForm;

    @BeforeEach
    void setUp() {
        accountAddForm = new AccountAddForm();
        accountAddForm.setUserId(1001L);
        accountAddForm.setAccountKindId(1L);
        accountAddForm.setInitialBalance(new BigDecimal("100.00"));

        accountUpdateForm = new AccountUpdateForm();
        accountUpdateForm.setAccountId(1L);
    }

    // ==================== 创建账户测试 ====================

    @Test
    @DisplayName("测试创建账户-成功")
    void testCreateAccount_Success() {
        // Given
        Long accountId = 1L;
        when(accountService.createAccount(any(AccountAddForm.class))).thenReturn(accountId);

        // When
        ResponseDTO<Long> result = accountController.createAccount(accountAddForm);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        assertEquals(accountId, result.getData());
        verify(accountService, times(1)).createAccount(any(AccountAddForm.class));
    }

    @Test
    @DisplayName("测试创建账户-参数验证失败")
    void testCreateAccount_ValidationFailed() {
        // Given
        accountAddForm.setUserId(null); // 缺少必填字段

        // When & Then
        // 由于使用@Valid，应该在Controller层被拦截
        // 这里测试Service层抛出异常的情况
        when(accountService.createAccount(any(AccountAddForm.class)))
            .thenThrow(new RuntimeException("参数验证失败"));

        ResponseDTO<Long> result = accountController.createAccount(accountAddForm);

        assertNotNull(result);
        assertFalse(result.getOk());
    }

    // ==================== 更新账户测试 ====================

    @Test
    @DisplayName("测试更新账户-成功")
    void testUpdateAccount_Success() {
        // Given
        when(accountService.updateAccount(any(AccountUpdateForm.class))).thenReturn(true);

        // When
        ResponseDTO<Boolean> result = accountController.updateAccount(accountUpdateForm);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        assertTrue(result.getData());
        verify(accountService, times(1)).updateAccount(any(AccountUpdateForm.class));
    }

    @Test
    @DisplayName("测试更新账户-账户不存在")
    void testUpdateAccount_AccountNotFound() {
        // Given
        when(accountService.updateAccount(any(AccountUpdateForm.class)))
            .thenThrow(new RuntimeException("账户不存在"));

        // When
        ResponseDTO<Boolean> result = accountController.updateAccount(accountUpdateForm);

        // Then
        assertNotNull(result);
        assertFalse(result.getOk());
    }

    // ==================== 删除账户测试 ====================

    @Test
    @DisplayName("测试删除账户-成功")
    void testDeleteAccount_Success() {
        // Given
        Long accountId = 1L;
        when(accountService.deleteAccount(accountId)).thenReturn(true);

        // When
        ResponseDTO<Boolean> result = accountController.deleteAccount(accountId);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        assertTrue(result.getData());
        verify(accountService, times(1)).deleteAccount(accountId);
    }

    @Test
    @DisplayName("测试删除账户-账户不存在")
    void testDeleteAccount_AccountNotFound() {
        // Given
        Long accountId = 9999L;
        when(accountService.deleteAccount(accountId))
            .thenThrow(new RuntimeException("账户不存在"));

        // When
        ResponseDTO<Boolean> result = accountController.deleteAccount(accountId);

        // Then
        assertNotNull(result);
        assertFalse(result.getOk());
    }

    // ==================== 查询账户测试 ====================

    @Test
    @DisplayName("测试根据ID查询账户-成功")
    void testGetAccountById_Success() {
        // Given
        Long accountId = 1L;
        AccountEntity account = new AccountEntity();
        account.setAccountId(accountId);
        account.setUserId(1001L);

        when(accountService.getById(accountId)).thenReturn(account);

        // When
        ResponseDTO<AccountEntity> result = accountController.getAccountById(accountId);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        assertEquals(accountId, result.getData().getAccountId());
        verify(accountService, times(1)).getById(accountId);
    }

    @Test
    @DisplayName("测试根据ID查询账户-不存在")
    void testGetAccountById_NotFound() {
        // Given
        Long accountId = 9999L;
        when(accountService.getById(accountId))
            .thenThrow(new RuntimeException("账户不存在"));

        // When
        ResponseDTO<AccountEntity> result = accountController.getAccountById(accountId);

        // Then
        assertNotNull(result);
        assertFalse(result.getOk());
    }

    // ==================== 分页查询测试 ====================

    @Test
    @DisplayName("测试分页查询账户-成功")
    void testQueryAccounts_Success() {
        // Given
        Integer pageNum = 1;
        Integer pageSize = 20;
        String keyword = "测试";
        Long accountKindId = 1L;
        Integer status = 1;

        PageResult<AccountEntity> pageResult = new PageResult<>();
        when(accountService.pageAccounts(eq(pageNum), eq(pageSize), eq(keyword), eq(accountKindId), eq(status)))
            .thenReturn(pageResult);

        // When
        ResponseDTO<PageResult<AccountEntity>> result = accountController.queryAccounts(
            pageNum, pageSize, keyword, null, accountKindId, status);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        verify(accountService, times(1)).pageAccounts(eq(pageNum), eq(pageSize), eq(keyword), eq(accountKindId), eq(status));
    }

    // ==================== 边界条件测试 ====================

    @Test
    @DisplayName("测试创建账户-表单为null")
    void testCreateAccount_FormIsNull() {
        // When & Then
        // 由于使用@Valid，应该在Controller层被拦截
        assertThrows(Exception.class, () -> {
            accountController.createAccount(null);
        });
    }

    @Test
    @DisplayName("测试更新账户-表单为null")
    void testUpdateAccount_FormIsNull() {
        // When & Then
        assertThrows(Exception.class, () -> {
            accountController.updateAccount(null);
        });
    }

    @Test
    @DisplayName("测试删除账户-账户ID为null")
    void testDeleteAccount_AccountIdIsNull() {
        // When
        ResponseDTO<Boolean> result = accountController.deleteAccount(null);

        // Then
        assertNotNull(result);
        assertFalse(result.getOk());
    }

    @Test
    @DisplayName("测试查询账户-账户ID为null")
    void testGetAccountById_AccountIdIsNull() {
        // When
        ResponseDTO<AccountEntity> result = accountController.getAccountById(null);

        // Then
        assertNotNull(result);
        assertFalse(result.getOk());
    }
}


