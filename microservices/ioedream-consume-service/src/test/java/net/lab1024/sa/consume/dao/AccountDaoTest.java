package net.lab1024.sa.consume.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.consume.BaseTest;
import net.lab1024.sa.consume.domain.entity.AccountEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import jakarta.annotation.Resource;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 账户DAO单元测试
 * 测试账户数据访问层操作
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@DisplayName("账户DAO测试")
class AccountDaoTest extends BaseTest {

    @Resource
    private AccountDao accountDao;

    private AccountEntity testAccount;

    @BeforeEach
    public void setUp() {
        // 清理测试数据
        accountDao.delete(new LambdaQueryWrapper<>());

        // 创建测试数据
        testAccount = new AccountEntity();
        testAccount.setUserId(1001L);
        testAccount.setAccountNo("ACC1001");
        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setStatus(1); // 1=正常状态
        testAccount.setAccountType(1); // 1=个人账户
    }

    @Test
    @DisplayName("测试插入账户 - 成功")
    void testInsert_Success() {
        // When
        int result = accountDao.insert(testAccount);

        // Then
        assertTrue(result > 0);
        assertNotNull(testAccount.getAccountId());
    }

    @Test
    @DisplayName("测试根据ID查询账户 - 成功")
    void testSelectById_Success() {
        // Given
        accountDao.insert(testAccount);

        // When
        AccountEntity result = accountDao.selectById(testAccount.getAccountId());

        // Then
        assertNotNull(result);
        assertEquals(testAccount.getUserId(), result.getUserId());
        assertEquals(testAccount.getAccountNo(), result.getAccountNo());
        assertEquals(testAccount.getBalance(), result.getBalance());
        assertEquals(testAccount.getStatus(), result.getStatus());
    }

    @Test
    @DisplayName("测试根据用户ID查询账户 - 成功")
    void testSelectByUserId_Success() {
        // Given
        accountDao.insert(testAccount);

        // When
        AccountEntity result = accountDao.selectByUserId(testAccount.getUserId());

        // Then
        assertNotNull(result);
        assertEquals(testAccount.getAccountId(), result.getAccountId());
        assertEquals(testAccount.getUserId(), result.getUserId());
    }

    @Test
    @DisplayName("测试更新账户余额 - 成功")
    void testUpdateBalance_Success() {
        // Given
        accountDao.insert(testAccount);
        BigDecimal updateAmount = new BigDecimal("500.00");
        BigDecimal expectedBalance = testAccount.getBalance().add(updateAmount);
        Long updateUserId = 1L; // 更新用户ID

        // When
        int result = accountDao.updateBalance(testAccount.getAccountId(), updateAmount, updateUserId);

        // Then
        assertTrue(result > 0);
        AccountEntity updatedAccount = accountDao.selectById(testAccount.getAccountId());
        assertEquals(expectedBalance, updatedAccount.getBalance());
    }

    @Test
    @DisplayName("测试更新账户状态 - 成功")
    void testUpdateStatus_Success() {
        // Given
        accountDao.insert(testAccount);
        Integer newStatus = 2; // 2=冻结状态
        Long updateUserId = 1L; // 更新用户ID

        // When
        int result = accountDao.updateStatus(testAccount.getAccountId(), newStatus, updateUserId);

        // Then
        assertTrue(result > 0);
        AccountEntity updatedAccount = accountDao.selectById(testAccount.getAccountId());
        assertEquals(newStatus, updatedAccount.getStatus());
    }

    @Test
    @DisplayName("测试分页查询账户 - 成功")
    void testSelectPage_Success() {
        // Given
        accountDao.insert(testAccount);

        // 创建更多测试数据
        for (int i = 2; i <= 5; i++) {
            AccountEntity account = new AccountEntity();
            account.setUserId((long) (1000 + i));
            account.setAccountNo("ACC" + (1000 + i));
            account.setBalance(new BigDecimal(String.valueOf(i * 100)));
            account.setStatus(1); // 1=正常状态
            account.setAccountType(1); // 1=个人账户
            accountDao.insert(account);
        }

        Page<AccountEntity> page = new Page<>(1, 3);
        LambdaQueryWrapper<AccountEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AccountEntity::getStatus, 1) // 1=正常状态
                .orderByDesc(AccountEntity::getCreateTime);

        // When
        IPage<AccountEntity> result = accountDao.selectPage(page, queryWrapper);

        // Then
        assertNotNull(result);
        assertTrue(result.getTotal() >= 4); // 至少包含插入的测试数据
        assertTrue(result.getRecords().size() <= 3); // 页面大小限制
    }

    @Test
    @DisplayName("测试根据条件查询账户 - 成功")
    void testSelectList_Success() {
        // Given
        accountDao.insert(testAccount);

        // 创建不同状态的测试数据
        AccountEntity inactiveAccount = new AccountEntity();
        inactiveAccount.setUserId(1002L);
        inactiveAccount.setAccountNo("ACC1002");
        inactiveAccount.setBalance(new BigDecimal("500.00"));
        inactiveAccount.setStatus(0); // 0=禁用状态
        inactiveAccount.setAccountType(1); // 1=个人账户
        accountDao.insert(inactiveAccount);

        LambdaQueryWrapper<AccountEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AccountEntity::getStatus, 1) // 1=正常状态
                .orderByDesc(AccountEntity::getCreateTime);

        // When
        List<AccountEntity> result = accountDao.selectList(queryWrapper);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(1), result.get(0).getStatus());
    }

    @Test
    @DisplayName("测试删除账户 - 成功")
    void testDeleteById_Success() {
        // Given
        accountDao.insert(testAccount);

        // When
        int result = accountDao.deleteById(testAccount.getAccountId());

        // Then
        assertTrue(result > 0);
        AccountEntity deletedAccount = accountDao.selectById(testAccount.getAccountId());
        assertNull(deletedAccount);
    }

    @Test
    @DisplayName("测试批量删除账户 - 成功")
    void testDeleteBatchIds_Success() {
        // Given
        List<Long> accountIds = Arrays.asList();

        // 创建多个测试账户
        for (int i = 1; i <= 3; i++) {
            AccountEntity account = new AccountEntity();
            account.setUserId((long) (2000 + i));
            account.setAccountNo("ACC" + (2000 + i));
            account.setBalance(new BigDecimal(String.valueOf(i * 100)));
            account.setStatus(1); // 1=正常状态
            account.setAccountType(1); // 1=个人账户
            accountDao.insert(account);
            accountIds.add(account.getAccountId());
        }

        // When - 使用新的批量删除方法
        int result = 0;
        for (Long accountId : accountIds) {
            result += accountDao.deleteById(accountId);
        }

        // Then
        assertEquals(3, result);

        for (Long accountId : accountIds) {
            AccountEntity deletedAccount = accountDao.selectById(accountId);
            assertNull(deletedAccount);
        }
    }

    @Test
    @DisplayName("测试查询账户总数 - 成功")
    void testSelectCount_Success() {
        // Given
        accountDao.insert(testAccount);

        LambdaQueryWrapper<AccountEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AccountEntity::getStatus, 1); // 1=正常状态

        // When
        Long result = accountDao.selectCount(queryWrapper);

        // Then
        assertEquals(1L, result);
    }

    // 注意：以下测试方法需要AccountDao中实现对应的方法
    // 目前AccountDao中没有这些方法，需要根据实际业务需求添加

    // @Test
    // @DisplayName("测试根据账户号查询 - 成功")
    // void testSelectByAccountNumber_Success() {
    //     // 需要AccountDao中添加selectByAccountNo方法
    // }

    // @Test
    // @DisplayName("测试检查账户号是否存在 - 存在")
    // void testExistsByAccountNumber_Exists() {
    //     // 需要AccountDao中添加existsByAccountNo方法
    // }

    // @Test
    // @DisplayName("测试根据用户ID和账户类型查询 - 成功")
    // void testSelectByUserIdAndAccountType_Success() {
    //     // 需要AccountDao中添加selectByUserIdAndAccountType方法
    // }

    // @Test
    // @DisplayName("测试查询余额大于指定值的账户 - 成功")
    // void testSelectByBalanceGreaterThan_Success() {
    //     // 需要使用LambdaQueryWrapper实现
    //     LambdaQueryWrapper<AccountEntity> queryWrapper = new LambdaQueryWrapper<>();
    //     queryWrapper.gt(AccountEntity::getBalance, threshold);
    //     List<AccountEntity> result = accountDao.selectList(queryWrapper);
    // }

    // @Test
    // @DisplayName("测试查询指定时间范围内创建的账户 - 成功")
    // void testSelectByCreateTimeBetween_Success() {
    //     // 需要使用LambdaQueryWrapper实现
    //     LambdaQueryWrapper<AccountEntity> queryWrapper = new LambdaQueryWrapper<>();
    //     queryWrapper.between(AccountEntity::getCreateTime, startTime, endTime);
    //     List<AccountEntity> result = accountDao.selectList(queryWrapper);
    // }
}


