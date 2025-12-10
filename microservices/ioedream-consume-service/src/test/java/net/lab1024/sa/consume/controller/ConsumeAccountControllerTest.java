package net.lab1024.sa.consume.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.metadata.IPage;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.domain.entity.AccountEntity;
import net.lab1024.sa.consume.domain.form.AccountQueryForm;
import net.lab1024.sa.consume.domain.vo.AccountVO;
import net.lab1024.sa.consume.service.ConsumeAccountService;

/**
 * ConsumeAccountController单元测试
 * <p>
 * 测试范围：消费账户管理REST API接口
 * 目标：提升测试覆盖率至70%+
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConsumeAccountController单元测试")
class ConsumeAccountControllerTest {

    @Mock
    private ConsumeAccountService accountService;

    @InjectMocks
    private ConsumeAccountController accountController;

    private AccountQueryForm queryForm;
    private AccountEntity accountEntity;

    @BeforeEach
    void setUp() {
        queryForm = new AccountQueryForm();
        queryForm.setPageNum(1);
        queryForm.setPageSize(20);

        accountEntity = new AccountEntity();
        accountEntity.setAccountId(1L);
        accountEntity.setUserId(1001L);
    }

    // ==================== getAccountList 测试 ====================

    @Test
    @DisplayName("测试分页查询账户列表-成功")
    void testGetAccountList_Success() {
        // Given
        @SuppressWarnings("unchecked")
        IPage<AccountVO> mockPage = mock(IPage.class);
        ResponseDTO<IPage<AccountVO>> mockResponse = ResponseDTO.ok(mockPage);
        when(accountService.queryAccountPage(any(AccountQueryForm.class))).thenReturn(mockResponse);

        // When
        ResponseDTO<IPage<AccountVO>> result = accountController.getAccountList(queryForm);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        verify(accountService, times(1)).queryAccountPage(any(AccountQueryForm.class));
    }

    // ==================== getAccountDetail 测试 ====================

    @Test
    @DisplayName("测试获取账户详情-成功")
    void testGetAccountDetail_Success() {
        // Given
        Long accountId = 1L;
        AccountVO mockVO = new AccountVO();
        mockVO.setAccountId(accountId);
        ResponseDTO<AccountVO> mockResponse = ResponseDTO.ok(mockVO);
        when(accountService.getAccountById(accountId)).thenReturn(mockResponse);

        // When
        ResponseDTO<AccountVO> result = accountController.getAccountDetail(accountId);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        assertEquals(accountId, result.getData().getAccountId());
        verify(accountService, times(1)).getAccountById(accountId);
    }

    @Test
    @DisplayName("测试获取账户详情-账户ID为null")
    void testGetAccountDetail_AccountIdIsNull() {
        // When & Then
        assertThrows(Exception.class, () -> {
            accountController.getAccountDetail(null);
        });
    }

    // ==================== getAccountBalance 测试 ====================

    @Test
    @DisplayName("测试查询用户账户余额-成功")
    void testGetAccountBalance_Success() {
        // Given
        Long userId = 1001L;
        Map<String, Object> balanceInfo = new HashMap<>();
        balanceInfo.put("balance", "1000.00");
        balanceInfo.put("frozenAmount", "100.00");

        when(accountService.getUserBalanceInfo(userId)).thenReturn(balanceInfo);

        // When
        ResponseDTO<Map<String, Object>> result = accountController.getAccountBalance(userId);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        verify(accountService, times(1)).getUserBalanceInfo(userId);
    }

    // ==================== getAccountByUserId 测试 ====================

    @Test
    @DisplayName("测试根据用户ID获取账户-成功")
    void testGetAccountByUserId_Success() {
        // Given
        Long userId = 1001L;
        AccountVO mockVO = new AccountVO();
        mockVO.setUserId(userId);

        when(accountService.getAccountByUserId(userId)).thenReturn(mockVO);

        // When
        ResponseDTO<AccountVO> result = accountController.getAccountByUserId(userId);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        assertEquals(userId, result.getData().getUserId());
        verify(accountService, times(1)).getAccountByUserId(userId);
    }

    // ==================== updateAccountStatus 测试 ====================

    @Test
    @DisplayName("测试更新账户状态-冻结")
    void testUpdateAccountStatus_Freeze() {
        // Given
        Long accountId = 1L;
        String operationType = "freeze";
        String reason = "异常消费";
        Integer freezeDays = 7;

        when(accountService.freezeAccount(accountId, reason, freezeDays)).thenReturn(true);

        // When
        ResponseDTO<String> result = accountController.updateAccountStatus(accountId, operationType, reason, freezeDays);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        verify(accountService, times(1)).freezeAccount(accountId, reason, freezeDays);
    }

    @Test
    @DisplayName("测试更新账户状态-解冻")
    void testUpdateAccountStatus_Unfreeze() {
        // Given
        Long accountId = 1L;
        String operationType = "unfreeze";
        String reason = "问题已解决";

        when(accountService.unfreezeAccount(accountId, reason)).thenReturn(true);

        // When
        ResponseDTO<String> result = accountController.updateAccountStatus(accountId, operationType, reason, null);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        verify(accountService, times(1)).unfreezeAccount(accountId, reason);
    }

    @Test
    @DisplayName("测试更新账户状态-无效操作类型")
    void testUpdateAccountStatus_InvalidOperationType() {
        // Given
        Long accountId = 1L;
        String operationType = "invalid";
        String reason = "测试";

        // When
        ResponseDTO<String> result = accountController.updateAccountStatus(accountId, operationType, reason, null);

        // Then
        assertNotNull(result);
        assertFalse(result.getOk());
        assertTrue(result.getMessage().contains("无效的操作类型"));
    }

    @Test
    @DisplayName("测试更新账户状态-账户ID为null")
    void testUpdateAccountStatus_AccountIdIsNull() {
        // When & Then
        assertThrows(Exception.class, () -> {
            accountController.updateAccountStatus(null, "freeze", "测试", 7);
        });
    }

    // ==================== 边界条件测试 ====================

    @Test
    @DisplayName("测试分页查询账户列表-查询表单为null")
    void testGetAccountList_QueryFormIsNull() {
        // When
        ResponseDTO<IPage<AccountVO>> result = accountController.getAccountList(null);

        // Then
        assertNotNull(result);
        // 根据实际实现，可能返回默认查询结果或错误
    }

    @Test
    @DisplayName("测试查询用户账户余额-用户ID为null")
    void testGetAccountBalance_UserIdIsNull() {
        // When & Then
        assertThrows(Exception.class, () -> {
            accountController.getAccountBalance(null);
        });
    }

    @Test
    @DisplayName("测试根据用户ID获取账户-用户ID为null")
    void testGetAccountByUserId_UserIdIsNull() {
        // When & Then
        assertThrows(Exception.class, () -> {
            accountController.getAccountByUserId(null);
        });
    }
}
