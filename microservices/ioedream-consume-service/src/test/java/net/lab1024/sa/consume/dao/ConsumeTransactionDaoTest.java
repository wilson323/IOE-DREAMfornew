package net.lab1024.sa.consume.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import net.lab1024.sa.consume.domain.entity.ConsumeTransactionEntity;

/**
 * ConsumeTransactionDao单元测试
 * <p>
 * 测试范围：消费交易记录数据访问操作
 * 目标：提升测试覆盖率至75%+
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ConsumeTransactionDao单元测试")
class ConsumeTransactionDaoTest {

    @Resource
    private ConsumeTransactionDao consumeTransactionDao;

    private ConsumeTransactionEntity testTransaction;

    @BeforeEach
    void setUp() {
        // 创建测试数据
        testTransaction = new ConsumeTransactionEntity();
        testTransaction.setTransactionNo("TXN" + System.currentTimeMillis());
        testTransaction.setUserId(1001L);
        testTransaction.setAccountId(1L);
        testTransaction.setAreaId(1L);
        testTransaction.setConsumeMoney(new BigDecimal("100.00"));
        testTransaction.setConsumeMode("FIXED");
        testTransaction.setStatus("SUCCESS"); // SUCCESS=成功
        testTransaction.setCreateTime(LocalDateTime.now());
    }

    // ==================== 基础CRUD测试 ====================

    @Test
    @DisplayName("测试插入交易记录-成功")
    void testInsert_Success() {
        // When
        int result = consumeTransactionDao.insert(testTransaction);

        // Then
        assertEquals(1, result);
        assertNotNull(testTransaction.getId());
    }

    @Test
    @DisplayName("测试根据ID查询交易记录-成功")
    void testSelectById_Success() {
        // Given
        consumeTransactionDao.insert(testTransaction);
        String id = testTransaction.getId();

        // When
        ConsumeTransactionEntity result = consumeTransactionDao.selectById(id);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(testTransaction.getTransactionNo(), result.getTransactionNo());
    }

    @Test
    @DisplayName("测试根据ID查询交易记录-不存在")
    void testSelectById_NotFound() {
        // When
        ConsumeTransactionEntity result = consumeTransactionDao.selectById("NON_EXISTENT");

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("测试更新交易记录-成功")
    void testUpdate_Success() {
        // Given
        consumeTransactionDao.insert(testTransaction);
        testTransaction.setStatus("SUCCESS"); // 更新状态

        // When
        int result = consumeTransactionDao.updateById(testTransaction);

        // Then
        assertEquals(1, result);

        // 验证更新
        ConsumeTransactionEntity updated = consumeTransactionDao.selectById(testTransaction.getId());
        assertNotNull(updated);
        assertEquals(2, updated.getStatus());
    }

    @Test
    @DisplayName("测试删除交易记录-成功")
    void testDelete_Success() {
        // Given
        consumeTransactionDao.insert(testTransaction);
        String id = testTransaction.getId();

        // When
        int result = consumeTransactionDao.deleteById(id);

        // Then
        assertEquals(1, result);

        // 验证删除
        ConsumeTransactionEntity deleted = consumeTransactionDao.selectById(id);
        assertNull(deleted);
    }

    // ==================== 业务查询测试 ====================

    @Test
    @DisplayName("测试根据交易流水号查询-成功")
    void testSelectByTransactionNo_Success() {
        // Given
        consumeTransactionDao.insert(testTransaction);
        String transactionNo = testTransaction.getTransactionNo();

        // When
        ConsumeTransactionEntity result = consumeTransactionDao.selectByTransactionNo(transactionNo);

        // Then
        assertNotNull(result);
        assertEquals(transactionNo, result.getTransactionNo());
    }

    @Test
    @DisplayName("测试根据交易流水号查询-不存在")
    void testSelectByTransactionNo_NotFound() {
        // When
        ConsumeTransactionEntity result = consumeTransactionDao.selectByTransactionNo("NON_EXISTENT");

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("测试根据用户ID查询-成功")
    void testSelectByUserId_Success() {
        // Given
        consumeTransactionDao.insert(testTransaction);
        Long userId = testTransaction.getUserId();
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now().plusDays(1);

        // When
        List<ConsumeTransactionEntity> result = consumeTransactionDao.selectByUserId(
            String.valueOf(userId), startTime, endTime);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(t -> userId.equals(t.getUserId())));
    }

    @Test
    @DisplayName("测试根据账户ID查询-成功")
    void testSelectByAccountId_Success() {
        // Given
        consumeTransactionDao.insert(testTransaction);
        Long accountId = testTransaction.getAccountId();
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now().plusDays(1);

        // When
        List<ConsumeTransactionEntity> result = consumeTransactionDao.selectByAccountId(
            String.valueOf(accountId), startTime, endTime);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(t -> accountId.equals(t.getAccountId())));
    }

    @Test
    @DisplayName("测试根据区域ID查询-成功")
    void testSelectByAreaId_Success() {
        // Given
        consumeTransactionDao.insert(testTransaction);
        Long areaId = testTransaction.getAreaId();
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now().plusDays(1);

        // When
        List<ConsumeTransactionEntity> result = consumeTransactionDao.selectByAreaId(
            String.valueOf(areaId), startTime, endTime);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(t -> areaId.equals(t.getAreaId())));
    }

    @Test
    @DisplayName("测试根据时间范围查询-成功")
    void testSelectByTimeRange_Success() {
        // Given
        consumeTransactionDao.insert(testTransaction);
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now().plusDays(1);

        // When
        List<ConsumeTransactionEntity> result = consumeTransactionDao.selectByTimeRange(
            startTime, endTime);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("测试根据时间范围查询-无结果")
    void testSelectByTimeRange_NoResult() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = LocalDateTime.now().plusDays(2);

        // When
        List<ConsumeTransactionEntity> result = consumeTransactionDao.selectByTimeRange(
            startTime, endTime);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试根据用户ID和时间范围查询-成功")
    void testSelectByUserIdAndTimeRange_Success() {
        // Given
        consumeTransactionDao.insert(testTransaction);
        Long userId = testTransaction.getUserId();
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now().plusDays(1);

        // When
        List<ConsumeTransactionEntity> result = consumeTransactionDao.selectByUserId(
            String.valueOf(userId), startTime, endTime);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(t -> userId.equals(t.getUserId())));
    }

    // ==================== 边界条件测试 ====================

    @Test
    @DisplayName("测试插入交易记录-交易流水号为null")
    void testInsert_TransactionNoIsNull() {
        // Given
        testTransaction.setTransactionNo(null);

        // When & Then
        // 根据业务规则，transactionNo可能是必填字段
        // 如果必填，应该抛出异常；如果可选，应该正常插入
        assertDoesNotThrow(() -> {
            consumeTransactionDao.insert(testTransaction);
        });
    }

    @Test
    @DisplayName("测试插入交易记录-金额为null")
    void testInsert_AmountIsNull() {
        // Given
        testTransaction.setConsumeMoney(null);

        // When & Then
        // 根据业务规则，amount可能是必填字段
        assertDoesNotThrow(() -> {
            consumeTransactionDao.insert(testTransaction);
        });
    }

    @Test
    @DisplayName("测试根据用户ID查询-用户ID为null")
    void testSelectByUserId_UserIdIsNull() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now().plusDays(1);

        // When & Then
        assertThrows(Exception.class, () -> {
            consumeTransactionDao.selectByUserId((String) null, startTime, endTime);
        });
    }
}


