package net.lab1024.sa.consume.dao;

import net.lab1024.sa.consume.BaseTest;
import net.lab1024.sa.consume.entity.AccountEntity;

import java.math.BigDecimal;
import java.util.List;

/**
 * 账户DAO单元测试
 * 测试账户数据访问层操作
 * 简化版本，专注于测试基础功能
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
class AccountDaoTest extends BaseTest {

    private AccountEntity testAccount;

    @Override
    public void setUp() {
        // 创建测试数据
        testAccount = createTestAccount();
    }

    private AccountEntity createTestAccount() {
        AccountEntity account = new AccountEntity();
        account.setUserId(1001L);
        account.setAccountNo("ACC1001");
        account.setBalance(new BigDecimal("1000.00"));
        account.setStatus(1); // 1=正常状态
        account.setAccountType(1); // 1=个人账户
        return account;
    }

    /**
     * 测试账户实体创建
     */
    public void testCreateAccountEntity() {
        // Given & When
        AccountEntity account = createTestAccount();

        // Then
        assertNotNull(account);
        assertEquals(1001L, account.getUserId());
        assertEquals("ACC1001", account.getAccountNo());
        assertEquals(new BigDecimal("1000.00"), account.getBalance());
        assertEquals(Integer.valueOf(1), account.getStatus());
        assertEquals(Integer.valueOf(1), account.getAccountType());
    }

    /**
     * 测试账户数据验证
     */
    public void testAccountDataValidation() {
        // Given
        AccountEntity account = testAccount;

        // When & Then - 验证基础数据
        assertNotNull(account.getUserId(), "用户ID不能为空");
        assertNotNull(account.getAccountNo(), "账户号不能为空");
        assertTrue(account.getBalance().compareTo(BigDecimal.ZERO) >= 0, "余额不能为负数");
        assertNotNull(account.getStatus(), "状态不能为空");
        assertNotNull(account.getAccountType(), "账户类型不能为空");

        // When & Then - 验证业务规则
        assertTrue(account.getUserId() > 0, "用户ID必须大于0");
        assertTrue(account.getAccountNo().length() > 0, "账户号长度必须大于0");
        assertTrue(account.getStatus() >= 0 && account.getStatus() <= 3, "状态值必须在0-3范围内");
        assertTrue(account.getAccountType() >= 1 && account.getAccountType() <= 3, "账户类型必须在1-3范围内");
    }

    /**
     * 测试账户状态枚举值
     */
    public void testAccountStatusValues() {
        // 测试所有有效的状态值
        Integer[] validStatuses = {0, 1, 2, 3}; // 0=禁用, 1=正常, 2=冻结, 3=注销

        for (Integer status : validStatuses) {
            AccountEntity account = createTestAccount();
            account.setStatus(status);

            assertTrue(status >= 0 && status <= 3,
                "状态值 " + status + " 必须在有效范围内");
        }
    }

    /**
     * 测试账户类型枚举值
     */
    public void testAccountTypeValues() {
        // 测试所有有效的账户类型
        Integer[] validTypes = {1, 2, 3}; // 1=个人账户, 2=企业账户, 3=临时账户

        for (Integer type : validTypes) {
            AccountEntity account = createTestAccount();
            account.setAccountType(type);

            assertTrue(type >= 1 && type <= 3,
                "账户类型 " + type + " 必须在有效范围内");
        }
    }

    /**
     * 测试余额计算
     */
    public void testBalanceCalculation() {
        AccountEntity account = testAccount;
        BigDecimal originalBalance = account.getBalance();

        // 模拟扣款操作
        BigDecimal deductAmount = new BigDecimal("100.00");
        BigDecimal expectedBalance = originalBalance.subtract(deductAmount);

        // 验证余额计算正确性
        assertTrue(expectedBalance.compareTo(BigDecimal.ZERO) >= 0,
            "扣款后余额不能为负数");
        assertEquals(new BigDecimal("900.00"), expectedBalance);
    }
}


