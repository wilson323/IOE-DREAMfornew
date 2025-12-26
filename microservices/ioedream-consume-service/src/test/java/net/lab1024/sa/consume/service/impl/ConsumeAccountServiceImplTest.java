package net.lab1024.sa.consume.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.seata.spring.annotation.GlobalTransactional;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.consume.client.AccountServiceClient;
import net.lab1024.sa.consume.client.dto.BalanceChangeResult;
import net.lab1024.sa.consume.client.dto.BalanceDecreaseRequest;
import net.lab1024.sa.consume.client.dto.BalanceIncreaseRequest;
import net.lab1024.sa.consume.dao.ConsumeAccountDao;
import net.lab1024.sa.consume.dao.ConsumeAccountTransactionDao;
import net.lab1024.sa.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.consume.domain.entity.ConsumeAccountEntity;
import net.lab1024.sa.consume.domain.form.ConsumeAccountAddForm;
import net.lab1024.sa.consume.domain.form.ConsumeAccountQueryForm;
import net.lab1024.sa.consume.domain.form.ConsumeAccountRechargeForm;
import net.lab1024.sa.consume.domain.form.ConsumeAccountUpdateForm;
import net.lab1024.sa.consume.domain.vo.ConsumeAccountVO;
import net.lab1024.sa.consume.exception.ConsumeAccountException;
import net.lab1024.sa.consume.manager.ConsumeAccountManager;

/**
 * ConsumeAccountServiceImpl 单元测试
 * <p>
 * 测试消费账户服务的核心功能：
 * - 账户查询（分页、详情、用户账户）
 * - 账户管理（创建、更新、冻结、解冻、注销）
 * - 余额操作（充值、扣减、退款）
 * - 统计功能
 * - 与远程账户服务集成
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@ExtendWith(MockitoExtension.class)
class ConsumeAccountServiceImplTest {

    @Mock
    private ConsumeAccountManager accountManager;

    @Mock
    private ConsumeAccountDao accountDao;

    @Mock
    private ConsumeAccountTransactionDao transactionDao;

    @Mock
    private ConsumeRecordDao recordDao;

    @Mock
    private AccountServiceClient accountServiceClient;

    @InjectMocks
    private ConsumeAccountServiceImpl accountService;

    private ConsumeAccountEntity testAccount;
    private ConsumeAccountVO testAccountVO;
    private BalanceChangeResult successResult;

    @BeforeEach
    void setUp() {
        testAccount = createTestAccount();
        testAccountVO = createTestAccountVO();
        successResult = createSuccessResult();
    }

    /**
     * 创建测试账户实体
     */
    private ConsumeAccountEntity createTestAccount() {
        ConsumeAccountEntity account = new ConsumeAccountEntity();
        account.setAccountId(1001L);
        account.setUserId(10001L);
        account.setAccountCode("ACC20251223001");
        account.setAccountName("张三账户");
        account.setAccountType(1);  // 修复：使用Integer而非String
        account.setBalance(new BigDecimal("1000.00"));
        account.setFrozenAmount(BigDecimal.ZERO);
        account.setCreditLimit(BigDecimal.ZERO);
        account.setAccountStatus(1);  // 修复：使用setAccountStatus而非setStatus
        account.setTotalRecharge(BigDecimal.ZERO);
        account.setTotalConsume(BigDecimal.ZERO);
        account.setEnableAutoRecharge(0);
        account.setVersion(0);
        account.setDeletedFlag(0);  // 修复：使用Integer类型0而非boolean
        return account;
    }

    /**
     * 创建测试账户VO
     */
    private ConsumeAccountVO createTestAccountVO() {
        return ConsumeAccountVO.builder()
                .accountId(1001L)
                .userId(10001L)
                .accountType("1")  // 修复：使用accountType而非accountCode
                .accountTypeName("员工账户")  // 修复：使用accountTypeName而非accountName
                .balance(new BigDecimal("1000.00"))
                // frozenAmount不存在，已移除
                .creditLimit(BigDecimal.ZERO)
                .availableLimit(new BigDecimal("1000.00"))
                .status("ACTIVE")  // 修复：使用String类型状态
                .build();
    }

    /**
     * 创建成功的余额变更结果
     */
    private BalanceChangeResult createSuccessResult() {
        return BalanceChangeResult.success(
                "TXN20251223001",
                10001L,
                new BigDecimal("1000.00"),
                new BigDecimal("1025.50"),
                new BigDecimal("25.50")
        );
    }

    // ==================== 账户查询测试 ====================

    @Test
    void testQueryAccounts_Success() {
        // Given
        ConsumeAccountQueryForm queryForm = new ConsumeAccountQueryForm();
        PageResult<ConsumeAccountVO> expectedResult = PageResult.empty();  // 修复：empty()不接受参数

        // 修复：使用any()匹配器，避免Page对象参数不匹配
        when(accountDao.selectPage(any(), eq(queryForm))).thenReturn(
            new com.baomidou.mybatisplus.core.metadata.IPage<ConsumeAccountVO>() {
                @Override
                public List<ConsumeAccountVO> getRecords() { return new ArrayList<>(); }
                @Override
                public com.baomidou.mybatisplus.core.metadata.IPage<ConsumeAccountVO> setRecords(List<ConsumeAccountVO> records) { return this; }
                @Override
                public long getTotal() { return 0; }
                @Override
                public com.baomidou.mybatisplus.core.metadata.IPage<ConsumeAccountVO> setTotal(long total) { return this; }
                @Override
                public long getSize() { return 20; }
                @Override
                public com.baomidou.mybatisplus.core.metadata.IPage<ConsumeAccountVO> setSize(long size) { return this; }
                @Override
                public long getCurrent() { return 1; }
                @Override
                public com.baomidou.mybatisplus.core.metadata.IPage<ConsumeAccountVO> setCurrent(long current) { return this; }
                @Override
                public long getPages() { return 0; }
                @Override
                public java.util.List<com.baomidou.mybatisplus.core.metadata.OrderItem> orders() { return new ArrayList<>(); }
            }
        );

        // When
        PageResult<ConsumeAccountVO> result = accountService.queryAccounts(queryForm);

        // Then
        assertNotNull(result);
        verify(accountDao).selectPage(any(), eq(queryForm));
    }

    @Test
    void testGetAccountDetail_Success() {
        // Given
        Long accountId = 1001L;
        when(accountManager.getAccountDetail(accountId)).thenReturn(testAccountVO);

        // When
        ConsumeAccountVO result = accountService.getAccountDetail(accountId);

        // Then
        assertNotNull(result);
        assertEquals(testAccountVO, result);
        verify(accountManager).getAccountDetail(accountId);
    }

    @Test
    void testGetAccountByUserId_Success() {
        // Given
        Long userId = 10001L;
        when(accountManager.getAccountByUserId(userId)).thenReturn(testAccountVO);

        // When
        ConsumeAccountVO result = accountService.getAccountByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals(testAccountVO, result);
        verify(accountManager).getAccountByUserId(userId);
    }

    // ==================== 账户创建测试 ====================

    @Test
    void testCreateAccount_Success() {
        // Given
        ConsumeAccountAddForm addForm = new ConsumeAccountAddForm();
        addForm.setUserId(10001L);
        addForm.setAccountType("1");
        addForm.setCreditLimit(BigDecimal.ZERO);

        when(accountDao.selectByUserId(10001L)).thenReturn(null);
        when(accountDao.insert(any(ConsumeAccountEntity.class))).thenAnswer(invocation -> {
            ConsumeAccountEntity account = invocation.getArgument(0);
            account.setAccountId(1001L);
            return 1;
        });

        // When
        Long accountId = accountService.createAccount(addForm);

        // Then
        assertNotNull(accountId);
        assertEquals(1001L, accountId);
        verify(accountDao).selectByUserId(10001L);
        verify(accountDao).insert(any(ConsumeAccountEntity.class));
    }

    @Test
    void testCreateAccount_AccountAlreadyExists() {
        // Given
        ConsumeAccountAddForm addForm = new ConsumeAccountAddForm();
        addForm.setUserId(10001L);

        when(accountDao.selectByUserId(10001L)).thenReturn(testAccountVO);

        // When & Then
        ConsumeAccountException exception = assertThrows(ConsumeAccountException.class,
                () -> accountService.createAccount(addForm));
        assertTrue(exception.getMessage().contains("用户已有账户"));
        verify(accountDao).selectByUserId(10001L);
        verify(accountDao, never()).insert(any(ConsumeAccountEntity.class));
    }

    // ==================== 账户更新测试 ====================

    @Test
    void testUpdateAccount_Success() {
        // Given
        Long accountId = 1001L;
        ConsumeAccountUpdateForm updateForm = new ConsumeAccountUpdateForm();
        updateForm.setRemark("更新备注");

        when(accountDao.selectById(accountId)).thenReturn(testAccount);
        when(accountDao.updateById(any(ConsumeAccountEntity.class))).thenReturn(1);

        // When
        accountService.updateAccount(accountId, updateForm);

        // Then
        verify(accountDao).selectById(accountId);
        verify(accountDao).updateById(any(ConsumeAccountEntity.class));
    }

    @Test
    void testUpdateAccount_AccountNotFound() {
        // Given
        Long accountId = 999L;
        ConsumeAccountUpdateForm updateForm = new ConsumeAccountUpdateForm();

        when(accountDao.selectById(accountId)).thenReturn(null);

        // When & Then
        ConsumeAccountException exception = assertThrows(ConsumeAccountException.class,
                () -> accountService.updateAccount(accountId, updateForm));
        assertTrue(exception.getMessage().contains("账户不存在"));
        verify(accountDao).selectById(accountId);
        verify(accountDao, never()).updateById(any(ConsumeAccountEntity.class));
    }

    // ==================== 账户充值测试 ====================

    @Test
    void testRechargeAccount_Success() {
        // Given
        Long accountId = 1001L;
        ConsumeAccountRechargeForm rechargeForm = new ConsumeAccountRechargeForm();
        rechargeForm.setAmount(new BigDecimal("100.00"));
        rechargeForm.setRemark("测试充值");

        when(accountDao.selectById(accountId)).thenReturn(testAccount);
        when(accountServiceClient.increaseBalance(any(BalanceIncreaseRequest.class)))
                .thenReturn(ResponseDTO.ok(successResult));

        // When
        Boolean result = accountService.rechargeAccount(accountId, rechargeForm);

        // Then
        assertTrue(result);
        verify(accountDao).selectById(accountId);
        verify(accountServiceClient).increaseBalance(any(BalanceIncreaseRequest.class));
    }

    @Test
    void testRechargeAccount_AccountNotFound() {
        // Given
        Long accountId = 999L;
        ConsumeAccountRechargeForm rechargeForm = new ConsumeAccountRechargeForm();
        rechargeForm.setAmount(new BigDecimal("100.00"));

        when(accountDao.selectById(accountId)).thenReturn(null);

        // When & Then
        ConsumeAccountException exception = assertThrows(ConsumeAccountException.class,
                () -> accountService.rechargeAccount(accountId, rechargeForm));
        assertTrue(exception.getMessage().contains("账户不存在"));
        verify(accountDao).selectById(accountId);
        verify(accountServiceClient, never()).increaseBalance(any());
    }

    // ==================== 余额扣减测试（核心功能）====================

    @Test
    void testDeductAmount_Success() {
        // Given
        Long accountId = 1001L;
        BigDecimal amount = new BigDecimal("25.50");
        String description = "午餐消费";

        when(accountDao.selectById(accountId)).thenReturn(testAccount);
        BalanceChangeResult deductResult = BalanceChangeResult.success(
                "TXN20251223002",
                10001L,
                new BigDecimal("1000.00"),
                new BigDecimal("974.50"),
                new BigDecimal("-25.50")
        );
        when(accountServiceClient.decreaseBalance(any(BalanceDecreaseRequest.class)))
                .thenReturn(ResponseDTO.ok(deductResult));

        // When
        Boolean result = accountService.deductAmount(accountId, amount, description);

        // Then
        assertTrue(result);
        verify(accountDao).selectById(accountId);
        verify(accountServiceClient).decreaseBalance(any(BalanceDecreaseRequest.class));
    }

    @Test
    void testDeductAmount_AccountNotFound() {
        // Given
        Long accountId = 999L;
        BigDecimal amount = new BigDecimal("25.50");
        String description = "午餐消费";

        when(accountDao.selectById(accountId)).thenReturn(null);

        // When & Then
        ConsumeAccountException exception = assertThrows(ConsumeAccountException.class,
                () -> accountService.deductAmount(accountId, amount, description));
        assertTrue(exception.getMessage().contains("账户不存在"));
        verify(accountDao).selectById(accountId);
        verify(accountServiceClient, never()).decreaseBalance(any());
    }

    @Test
    void testDeductAmount_ServiceReturnsError() {
        // Given
        Long accountId = 1001L;
        BigDecimal amount = new BigDecimal("25.50");
        String description = "午餐消费";

        when(accountDao.selectById(accountId)).thenReturn(testAccount);
        BalanceChangeResult errorResult = BalanceChangeResult.failure("BALANCE_INSUFFICIENT", "余额不足");
        when(accountServiceClient.decreaseBalance(any(BalanceDecreaseRequest.class)))
                .thenReturn(ResponseDTO.ok(errorResult));

        // When & Then
        ConsumeAccountException exception = assertThrows(ConsumeAccountException.class,
                () -> accountService.deductAmount(accountId, amount, description));
        assertTrue(exception.getMessage().contains("扣款失败"));
        verify(accountDao).selectById(accountId);
        verify(accountServiceClient).decreaseBalance(any(BalanceDecreaseRequest.class));
    }

    // ==================== 余额退款测试 ====================

    @Test
    void testRefundAmount_Success() {
        // Given
        Long accountId = 1001L;
        BigDecimal amount = new BigDecimal("25.50");
        String reason = "菜品质量问题";

        when(accountDao.selectById(accountId)).thenReturn(testAccount);
        BalanceChangeResult refundResult = BalanceChangeResult.success(
                "REFUND20251223001",
                10001L,
                new BigDecimal("974.50"),
                new BigDecimal("1000.00"),
                new BigDecimal("25.50")
        );
        when(accountServiceClient.increaseBalance(any(BalanceIncreaseRequest.class)))
                .thenReturn(ResponseDTO.ok(refundResult));

        // When
        Boolean result = accountService.refundAmount(accountId, amount, reason);

        // Then
        assertTrue(result);
        verify(accountDao).selectById(accountId);
        verify(accountServiceClient).increaseBalance(any(BalanceIncreaseRequest.class));
    }

    @Test
    void testRefundAmount_AccountNotFound() {
        // Given
        Long accountId = 999L;
        BigDecimal amount = new BigDecimal("25.50");
        String reason = "菜品质量问题";

        when(accountDao.selectById(accountId)).thenReturn(null);

        // When & Then
        ConsumeAccountException exception = assertThrows(ConsumeAccountException.class,
                () -> accountService.refundAmount(accountId, amount, reason));
        assertTrue(exception.getMessage().contains("账户不存在"));
        verify(accountDao).selectById(accountId);
        verify(accountServiceClient, never()).increaseBalance(any());
    }

    // ==================== 账户冻结/解冻测试 ====================

    @Test
    void testFreezeAccount_Success() {
        // Given
        Long accountId = 1001L;
        String reason = "异常操作";

        when(accountDao.selectById(accountId)).thenReturn(testAccount);
        when(accountDao.updateById(any(ConsumeAccountEntity.class))).thenReturn(1);

        // When
        accountService.freezeAccount(accountId, reason);

        // Then
        verify(accountDao).selectById(accountId);
        verify(accountDao).updateById(any(ConsumeAccountEntity.class));
    }

    @Test
    void testUnfreezeAccount_Success() {
        // Given
        Long accountId = 1001L;
        String reason = "解除冻结";

        testAccount.setAccountStatus(0); // 冻结状态
        when(accountDao.selectById(accountId)).thenReturn(testAccount);
        when(accountDao.updateById(any(ConsumeAccountEntity.class))).thenReturn(1);

        // When
        accountService.unfreezeAccount(accountId, reason);

        // Then
        verify(accountDao).selectById(accountId);
        verify(accountDao).updateById(any(ConsumeAccountEntity.class));
    }

    // ==================== 账户注销测试 ====================

    @Test
    void testCloseAccount_Success() {
        // Given
        Long accountId = 1001L;
        String reason = "用户注销";

        testAccount.setBalance(BigDecimal.ZERO);
        when(accountDao.selectById(accountId)).thenReturn(testAccount);
        when(accountDao.updateById(any(ConsumeAccountEntity.class))).thenReturn(1);

        // When
        accountService.closeAccount(accountId, reason);

        // Then
        verify(accountDao).selectById(accountId);
        verify(accountDao).updateById(any(ConsumeAccountEntity.class));
    }

    @Test
    void testCloseAccount_BalanceNotZero() {
        // Given
        Long accountId = 1001L;
        String reason = "用户注销";

        when(accountDao.selectById(accountId)).thenReturn(testAccount);

        // When & Then
        ConsumeAccountException exception = assertThrows(ConsumeAccountException.class,
                () -> accountService.closeAccount(accountId, reason));
        assertTrue(exception.getMessage().contains("账户余额不为零"));
        verify(accountDao).selectById(accountId);
        verify(accountDao, never()).updateById(any(ConsumeAccountEntity.class));
    }

    // ==================== 余额查询测试 ====================

    @Test
    void testGetAccountBalance_Success() {
        // Given
        Long accountId = 1001L;
        when(accountDao.selectById(accountId)).thenReturn(testAccount);

        // When
        BigDecimal balance = accountService.getAccountBalance(accountId);

        // Then
        assertNotNull(balance);
        assertEquals(testAccount.getBalance(), balance);
        verify(accountDao).selectById(accountId);
    }

    // ==================== 批量创建测试 ====================

    @Test
    void testBatchCreateAccounts_Success() {
        // Given
        List<ConsumeAccountAddForm> addForms = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ConsumeAccountAddForm form = new ConsumeAccountAddForm();
            form.setUserId((long) (10001 + i));
            addForms.add(form);
        }

        when(accountDao.selectByUserId(anyLong())).thenReturn(null);
        when(accountDao.insert(any(ConsumeAccountEntity.class))).thenAnswer(invocation -> {
            ConsumeAccountEntity account = invocation.getArgument(0);
            account.setAccountId((long) (1001 + addForms.indexOf(account)));
            return 1;
        });

        // When
        Map<String, Object> result = accountService.batchCreateAccounts(addForms);

        // Then
        assertNotNull(result);
        assertEquals(3, result.get("total"));
        assertEquals(3, result.get("successCount"));
        assertEquals(0, result.get("failCount"));
    }
}
