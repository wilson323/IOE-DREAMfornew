package net.lab1024.sa.admin.module.consume;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.dao.AccountDao;
import net.lab1024.sa.admin.module.consume.dao.AccountTransactionDao;
import net.lab1024.sa.admin.module.consume.domain.entity.AccountEntity;
import net.lab1024.sa.admin.module.consume.domain.entity.AccountTransactionEntity;
import net.lab1024.sa.admin.module.consume.service.AccountService;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.tenant.TenantEnvironment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 账户服务单元测试
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
@SpringBootTest
@SpringJUnitConfig
@ExtendWith(SpringJUnitConfig.class)
class AccountServiceTest {

    @InjectMocks
    private AccountService accountService;

    @Mock
    private AccountDao accountDao;

    @Mock
    private AccountTransactionDao accountTransactionDao;

    @Mock
    private TenantEnvironment tenantEnvironment;

    private ObjectMapper objectMapper = new ObjectMapper();
    private AccountEntity testAccount;
    private AccountTransactionEntity testTransaction;

    @BeforeEach
    void setUp() {
        // 初始化测试账户
        testAccount = new AccountEntity();
        testAccount.setAccountId(1L);
        testAccount.setUserId(1001L);
        testAccount.setPersonId(2001L);
        testAccount.setAccountNo("ACC001");
        testAccount.setAccountType("CASH_CARD");
        testAccount.setStatus("ACTIVE");
        testAccount.setBalanceAmount(new BigDecimal("1000.00"));
        testAccount.setFrozenAmount(new BigDecimal("0.00"));
        testAccount.setTotalRechargeAmount(new BigDecimal("1000.00"));
        testAccount.setTotalConsumeAmount(new BigDecimal("0.00"));
        testAccount.setCreditLimit(new BigDecimal("0.00"));
        testAccount.setDailyLimit(new BigDecimal("5000.00"));
        testAccount.setMonthlyLimit(new BigDecimal("10000.00"));
        testAccount.setCreateTime(LocalDateTime.now());
        testAccount.setUpdateTime(LocalDateTime.now());

        // 初始化测试交易记录
        testTransaction = new AccountTransactionEntity();
        testTransaction.setTransactionId(12345L);
        testTransaction.setAccountId(1L);
        testTransaction.setTransactionType("RECHARGE");
        testTransaction.setTransactionAmount(new BigDecimal("100.00"));
        testTransaction.setBalanceBefore(new BigDecimal("900.00"));
        testTransaction.setBalanceAfter(new BigDecimal("1000.00"));
        testTransaction.setTransactionNo("TXN_" + System.currentTimeMillis());
        testTransaction.setOrderNo("ORDER_001");
        testTransaction.setTransactionStatus("SUCCESS");
        testTransaction.setTransactionTime(LocalDateTime.now());
        testTransaction.setDescription("测试充值");
    }

    @Test
    @DisplayName("测试获取账户列表")
    void testGetAccountList_Success() {
        // 准备测试数据
        List<AccountEntity> accountList = Arrays.asList(testAccount);
        Page<AccountEntity> accountPage = new PageImpl<>(accountList);
        Pageable pageable = PageRequest.of(0, 10);

        // 模拟依赖调用
        when(accountDao.findAll(pageable)).thenReturn(accountPage);

        // 执行测试
        Page<AccountEntity> result = accountService.getAccountList(pageable, null, null, null);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testAccount.getAccountId(), result.getContent().get(0).getAccountId());
        assertEquals(testAccount.getAccountNo(), result.getContent().get(0).getAccountNo());

        verify(accountDao, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("测试根据用户ID获取账户")
    void testGetAccountByUserId_Success() {
        // 模拟依赖调用
        when(accountDao.findByUserId(testAccount.getUserId()))
            .thenReturn(Optional.of(testAccount));

        // 执行测试
        AccountEntity result = accountService.getAccountByUserId(testAccount.getUserId());

        // 验证结果
        assertNotNull(result);
        assertEquals(testAccount.getAccountId(), result.getAccountId());
        assertEquals(testAccount.getUserId(), result.getUserId());
        assertEquals(testAccount.getAccountNo(), result.getAccountNo());

        verify(accountDao, times(1)).findByUserId(testAccount.getUserId());
    }

    @Test
    @DisplayName("测试根据用户ID获取账户 - 账户不存在")
    void testGetAccountByUserId_NotFound() {
        // 模拟账户不存在
        Long userId = 9999L;
        when(accountDao.findByUserId(userId))
            .thenReturn(Optional.empty());

        // 执行测试
        AccountEntity result = accountService.getAccountByUserId(userId);

        // 验证结果
        assertNull(result);

        verify(accountDao, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("测试创建账户")
    void testCreateAccount_Success() {
        // 准备新账户数据
        AccountEntity newAccount = new AccountEntity();
        newAccount.setUserId(1002L);
        newAccount.setPersonId(2002L);
        newAccount.setAccountNo("ACC002");
        newAccount.setAccountType("CASH_CARD");
        newAccount.setBalanceAmount(new BigDecimal("0.00"));
        newAccount.setDailyLimit(new BigDecimal("5000.00"));
        newAccount.setMonthlyLimit(new BigDecimal("10000.00"));

        // 模拟依赖调用
        when(accountDao.existsByAccountNo(newAccount.getAccountNo()))
            .thenReturn(false);
        when(accountDao.existsByUserId(newAccount.getUserId()))
            .thenReturn(false);
        when(accountDao.save(any(AccountEntity.class)))
            .thenReturn(testAccount);

        // 执行测试
        ResponseDTO<AccountEntity> result = accountService.createAccount(newAccount);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals("账户创建成功", result.getMsg());

        verify(accountDao, times(1)).existsByAccountNo(newAccount.getAccountNo());
        verify(accountDao, times(1)).existsByUserId(newAccount.getUserId());
        verify(accountDao, times(1)).save(any(AccountEntity.class));
    }

    @Test
    @DisplayName("测试创建账户 - 账号已存在")
    void testCreateAccount_AccountNoExists() {
        // 准备新账户数据
        AccountEntity newAccount = new AccountEntity();
        newAccount.setAccountNo("ACC001");
        newAccount.setUserId(1002L);

        // 模拟账号已存在
        when(accountDao.existsByAccountNo(newAccount.getAccountNo()))
            .thenReturn(true);

        // 执行测试
        ResponseDTO<AccountEntity> result = accountService.createAccount(newAccount);

        // 验证结果
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("账号已存在", result.getMsg());
        assertNull(result.getData());

        verify(accountDao, times(1)).existsByAccountNo(newAccount.getAccountNo());
        verify(accountDao, never()).save(any(AccountEntity.class));
    }

    @Test
    @DisplayName("测试创建账户 - 用户已存在")
    void testCreateAccount_UserExists() {
        // 准备新账户数据
        AccountEntity newAccount = new AccountEntity();
        newAccount.setAccountNo("ACC002");
        newAccount.setUserId(1001L);

        // 模拟用户已存在
        when(accountDao.existsByAccountNo(newAccount.getAccountNo()))
            .thenReturn(false);
        when(accountDao.existsByUserId(newAccount.getUserId()))
            .thenReturn(true);

        // 执行测试
        ResponseDTO<AccountEntity> result = accountService.createAccount(newAccount);

        // 验证结果
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("用户已存在账户", result.getMsg());
        assertNull(result.getData());

        verify(accountDao, times(1)).existsByAccountNo(newAccount.getAccountNo());
        verify(accountDao, times(1)).existsByUserId(newAccount.getUserId());
        verify(accountDao, never()).save(any(AccountEntity.class));
    }

    @Test
    @DisplayName("测试账户充值")
    void testRechargeAccount_Success() {
        // 准备充值数据
        BigDecimal rechargeAmount = new BigDecimal("100.00");
        String orderNo = "RECHARGE_001";
        String paymentMethod = "WECHAT_PAY";
        String remark = "测试充值";

        // 模拟依赖调用
        when(accountDao.findById(testAccount.getAccountId()))
            .thenReturn(Optional.of(testAccount));
        when(accountDao.save(any(AccountEntity.class)))
            .thenReturn(testAccount);
        when(accountTransactionDao.save(any(AccountTransactionEntity.class)))
            .thenReturn(testTransaction);

        // 执行测试
        ResponseDTO<AccountTransactionEntity> result = accountService.rechargeAccount(
            testAccount.getAccountId(), rechargeAmount, orderNo, paymentMethod, remark
        );

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals("充值成功", result.getMsg());

        verify(accountDao, times(1)).findById(testAccount.getAccountId());
        verify(accountDao, times(1)).save(any(AccountEntity.class));
        verify(accountTransactionDao, times(1)).save(any(AccountTransactionEntity.class));
    }

    @Test
    @DisplayName("测试账户充值 - 充值金额无效")
    void testRechargeAccount_InvalidAmount() {
        // 准备无效充值数据
        BigDecimal invalidAmount = new BigDecimal("-100.00");

        // 执行测试
        ResponseDTO<AccountTransactionEntity> result = accountService.rechargeAccount(
            testAccount.getAccountId(), invalidAmount, "ORDER_001", "WECHAT_PAY", "测试"
        );

        // 验证结果
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("充值金额必须大于0", result.getMsg());
        assertNull(result.getData());

        verify(accountDao, never()).findById(anyLong());
        verify(accountTransactionDao, never()).save(any(AccountTransactionEntity.class));
    }

    @Test
    @DisplayName("测试账户充值 - 账户不存在")
    void testRechargeAccount_AccountNotFound() {
        // 模拟账户不存在
        when(accountDao.findById(9999L))
            .thenReturn(Optional.empty());

        // 执行测试
        ResponseDTO<AccountTransactionEntity> result = accountService.rechargeAccount(
            9999L, new BigDecimal("100.00"), "ORDER_001", "WECHAT_PAY", "测试"
        );

        // 验证结果
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("账户不存在", result.getMsg());
        assertNull(result.getData());

        verify(accountDao, times(1)).findById(9999L);
        verify(accountTransactionDao, never()).save(any(AccountTransactionEntity.class));
    }

    @Test
    @DisplayName("测试账户扣款")
    void testDeductBalance_Success() {
        // 准备扣款数据
        BigDecimal deductAmount = new BigDecimal("50.00");
        String orderNo = "DEDUCT_001";
        String remark = "测试扣款";

        // 模拟依赖调用
        when(accountDao.findById(testAccount.getAccountId()))
            .thenReturn(Optional.of(testAccount));
        when(accountDao.save(any(AccountEntity.class)))
            .thenReturn(testAccount);
        when(accountTransactionDao.save(any(AccountTransactionEntity.class)))
            .thenReturn(testTransaction);

        // 执行测试
        ResponseDTO<AccountTransactionEntity> result = accountService.deductBalance(
            testAccount.getAccountId(), deductAmount, orderNo, remark
        );

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals("扣款成功", result.getMsg());

        verify(accountDao, times(1)).findById(testAccount.getAccountId());
        verify(accountDao, times(1)).save(any(AccountEntity.class));
        verify(accountTransactionDao, times(1)).save(any(AccountTransactionEntity.class));
    }

    @Test
    @DisplayName("测试账户扣款 - 余额不足")
    void testDeductBalance_InsufficientBalance() {
        // 准备扣款数据（超过余额）
        BigDecimal deductAmount = new BigDecimal("2000.00"); // 超过1000元余额

        // 模拟依赖调用
        when(accountDao.findById(testAccount.getAccountId()))
            .thenReturn(Optional.of(testAccount));

        // 执行测试
        ResponseDTO<AccountTransactionEntity> result = accountService.deductBalance(
            testAccount.getAccountId(), deductAmount, "DEDUCT_001", "测试扣款"
        );

        // 验证结果
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("余额不足", result.getMsg());
        assertNull(result.getData());

        verify(accountDao, times(1)).findById(testAccount.getAccountId());
        verify(accountDao, never()).save(any(AccountEntity.class));
        verify(accountTransactionDao, never()).save(any(AccountTransactionEntity.class));
    }

    @Test
    @DisplayName("测试检查余额是否充足")
    void testCheckBalanceSufficient_Success() {
        // 准备测试数据
        BigDecimal checkAmount = new BigDecimal("500.00");

        // 模拟依赖调用
        when(accountDao.findById(testAccount.getAccountId()))
            .thenReturn(Optional.of(testAccount));

        // 执行测试
        Boolean result = accountService.checkBalanceSufficient(testAccount.getAccountId(), checkAmount);

        // 验证结果
        assertTrue(result);

        verify(accountDao, times(1)).findById(testAccount.getAccountId());
    }

    @Test
    @DisplayName("测试检查余额是否充足 - 余额不足")
    void testCheckBalanceSufficient_Insufficient() {
        // 准备测试数据
        BigDecimal checkAmount = new BigDecimal("1500.00"); // 超过余额

        // 模拟依赖调用
        when(accountDao.findById(testAccount.getAccountId()))
            .thenReturn(Optional.of(testAccount));

        // 执行测试
        Boolean result = accountService.checkBalanceSufficient(testAccount.getAccountId(), checkAmount);

        // 验证结果
        assertFalse(result);

        verify(accountDao, times(1)).findById(testAccount.getAccountId());
    }

    @Test
    @DisplayName("测试检查余额是否充足 - 账户不存在")
    void testCheckBalanceSufficient_AccountNotFound() {
        // 模拟账户不存在
        when(accountDao.findById(9999L))
            .thenReturn(Optional.empty());

        // 执行测试
        Boolean result = accountService.checkBalanceSufficient(9999L, new BigDecimal("100.00"));

        // 验证结果
        assertFalse(result);

        verify(accountDao, times(1)).findById(9999L);
    }

    @Test
    @DisplayName("测试检查是否有冻结金额")
    void testHasFrozenAmount_Success() {
        // 准备测试数据
        BigDecimal checkAmount = new BigDecimal("50.00");

        // 模拟依赖调用
        when(accountDao.findById(testAccount.getAccountId()))
            .thenReturn(Optional.of(testAccount));

        // 执行测试
        Boolean result = accountService.hasFrozenAmount(testAccount.getAccountId(), checkAmount);

        // 验证结果（测试账户冻结金额为0，所以应该返回false）
        assertFalse(result);

        verify(accountDao, times(1)).findById(testAccount.getAccountId());
    }

    @Test
    @DisplayName("测试检查是否有冻结金额 - 有冻结金额")
    void testHasFrozenAmount_HasFrozenAmount() {
        // 准备测试数据
        BigDecimal checkAmount = new BigDecimal("50.00");
        testAccount.setFrozenAmount(new BigDecimal("100.00")); // 设置冻结金额

        // 模拟依赖调用
        when(accountDao.findById(testAccount.getAccountId()))
            .thenReturn(Optional.of(testAccount));

        // 执行测试
        Boolean result = accountService.hasFrozenAmount(testAccount.getAccountId(), checkAmount);

        // 验证结果
        assertTrue(result);

        verify(accountDao, times(1)).findById(testAccount.getAccountId());
    }

    @Test
    @DisplayName("测试冻结账户")
    void testFreezeAccount_Success() {
        // 模拟依赖调用
        when(accountDao.findById(testAccount.getAccountId()))
            .thenReturn(Optional.of(testAccount));
        when(accountDao.save(any(AccountEntity.class)))
            .thenReturn(testAccount);

        // 执行测试
        ResponseDTO<String> result = accountService.freezeAccount(testAccount.getAccountId(), "系统冻结");

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("账户冻结成功", result.getMsg());

        verify(accountDao, times(1)).findById(testAccount.getAccountId());
        verify(accountDao, times(1)).save(any(AccountEntity.class));
    }

    @Test
    @DisplayName("测试解冻账户")
    void testUnfreezeAccount_Success() {
        // 设置账户为冻结状态
        testAccount.setStatus("FROZEN");

        // 模拟依赖调用
        when(accountDao.findById(testAccount.getAccountId()))
            .thenReturn(Optional.of(testAccount));
        when(accountDao.save(any(AccountEntity.class)))
            .thenReturn(testAccount);

        // 执行测试
        ResponseDTO<String> result = accountService.unfreezeAccount(testAccount.getAccountId(), "系统解冻");

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("账户解冻成功", result.getMsg());

        verify(accountDao, times(1)).findById(testAccount.getAccountId());
        verify(accountDao, times(1)).save(any(AccountEntity.class));
    }

    @Test
    @DisplayName("测试获取账户交易记录")
    void testGetAccountTransactions_Success() {
        // 准备测试数据
        List<AccountTransactionEntity> transactionList = Arrays.asList(testTransaction);
        Page<AccountTransactionEntity> transactionPage = new PageImpl<>(transactionList);
        Pageable pageable = PageRequest.of(0, 10);

        // 模拟依赖调用
        when(accountTransactionDao.findByAccountIdOrderByTransactionTimeDesc(testAccount.getAccountId(), pageable))
            .thenReturn(transactionPage);

        // 执行测试
        Page<AccountTransactionEntity> result = accountService.getAccountTransactions(
            testAccount.getAccountId(), pageable, null, null
        );

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testTransaction.getTransactionId(), result.getContent().get(0).getTransactionId());

        verify(accountTransactionDao, times(1))
            .findByAccountIdOrderByTransactionTimeDesc(testAccount.getAccountId(), pageable);
    }

    @Test
    @DisplayName("测试获取账户余额")
    void testGetAccountBalance_Success() {
        // 模拟依赖调用
        when(accountDao.findById(testAccount.getAccountId()))
            .thenReturn(Optional.of(testAccount));

        // 执行测试
        BigDecimal result = accountService.getAccountBalance(testAccount.getAccountId());

        // 验证结果
        assertNotNull(result);
        assertEquals(testAccount.getBalanceAmount(), result);

        verify(accountDao, times(1)).findById(testAccount.getAccountId());
    }

    @Test
    @DisplayName("测试获取账户余额 - 账户不存在")
    void testGetAccountBalance_AccountNotFound() {
        // 模拟账户不存在
        when(accountDao.findById(9999L))
            .thenReturn(Optional.empty());

        // 执行测试
        BigDecimal result = accountService.getAccountBalance(9999L);

        // 验证结果
        assertNull(result);

        verify(accountDao, times(1)).findById(9999L);
    }

    @Test
    @DisplayName("测试异常处理")
    void testException() {
        // 模拟异常
        when(accountDao.findById(anyLong()))
            .thenThrow(new RuntimeException("数据库连接异常"));

        // 执行测试
        assertThrows(RuntimeException.class, () -> {
            accountService.getAccountByUserId(1001L);
        });
    }

    @Test
    @DisplayName("测试账户状态验证")
    void testAccountStatusValidation() {
        // 测试各种账户状态
        String[] validStatuses = {"ACTIVE", "FROZEN", "CLOSED"};
        String[] invalidStatuses = {"INVALID", "UNKNOWN", null};

        for (String status : validStatuses) {
            testAccount.setStatus(status);
            when(accountDao.findById(testAccount.getAccountId()))
                .thenReturn(Optional.of(testAccount));

            AccountEntity result = accountService.getAccountByUserId(testAccount.getUserId());
            assertNotNull(result);
            assertEquals(status, result.getStatus());
        }

        // 测试无效状态的处理逻辑
        for (String status : invalidStatuses) {
            if (status != null) {
                testAccount.setStatus(status);
                when(accountDao.findById(testAccount.getAccountId()))
                    .thenReturn(Optional.of(testAccount));

                // 这里应该有相应的状态验证逻辑
                // 具体验证规则取决于业务需求
            }
        }
    }

    @Test
    @DisplayName("测试分页参数验证")
    void testPageableValidation() {
        // 测试无效的分页参数
        Pageable invalidPageable = null;

        // 执行测试 - 应该能处理null参数
        assertDoesNotThrow(() -> {
            Page<AccountEntity> result = accountService.getAccountList(invalidPageable, null, null, null);
            assertNotNull(result);
        });
    }
}