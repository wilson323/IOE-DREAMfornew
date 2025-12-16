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
import net.lab1024.sa.common.consume.entity.PaymentRecordEntity;

/**
 * PaymentRecordDao单元测试
 * <p>
 * 测试范围：支付记录数据访问操作
 * 目标：提升测试覆盖率至75%+
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("PaymentRecordDao单元测试")
class PaymentRecordDaoTest {

    @Resource
    private PaymentRecordDao paymentRecordDao;

    private PaymentRecordEntity testPaymentRecord;

    @BeforeEach
    void setUp() {
        // 创建测试数据
        testPaymentRecord = new PaymentRecordEntity();
        testPaymentRecord.setPaymentId("PAY" + System.currentTimeMillis());
        testPaymentRecord.setUserId(1001L);
        testPaymentRecord.setAccountId(2001L);
        testPaymentRecord.setOrderNo("ORDER" + System.currentTimeMillis());
        testPaymentRecord.setTransactionNo("TXN" + System.currentTimeMillis());
        testPaymentRecord.setPaymentAmount(new BigDecimal("100.00"));
        testPaymentRecord.setActualAmount(new BigDecimal("100.00"));
        testPaymentRecord.setPaymentMethod(3); // 3=支付宝
        testPaymentRecord.setPaymentChannel(3); // 3=移动端
        testPaymentRecord.setPaymentStatus(3); // 3=支付成功
        testPaymentRecord.setBusinessType(1); // 1=消费
        testPaymentRecord.setDeviceId("DEV001");
        testPaymentRecord.setCreateTime(LocalDateTime.now());
    }

    // ==================== 基础CRUD测试 ====================

    @Test
    @DisplayName("测试插入支付记录-成功")
    void testInsert_Success() {
        // When
        int result = paymentRecordDao.insert(testPaymentRecord);

        // Then
        assertEquals(1, result);
        assertNotNull(testPaymentRecord.getPaymentId());
    }

    @Test
    @DisplayName("测试根据ID查询支付记录-成功")
    void testSelectById_Success() {
        // Given
        paymentRecordDao.insert(testPaymentRecord);
        String paymentId = testPaymentRecord.getPaymentId();

        // When
        PaymentRecordEntity result = paymentRecordDao.selectById(paymentId);

        // Then
        assertNotNull(result);
        assertEquals(paymentId, result.getPaymentId());
        assertEquals(testPaymentRecord.getPaymentId(), result.getPaymentId());
    }

    @Test
    @DisplayName("测试根据ID查询支付记录-不存在")
    void testSelectById_NotFound() {
        // When
        PaymentRecordEntity result = paymentRecordDao.selectById("NON_EXISTENT_ID");

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("测试根据支付订单号查询-成功")
    void testSelectByPaymentId_Success() {
        // Given
        paymentRecordDao.insert(testPaymentRecord);
        String paymentId = testPaymentRecord.getPaymentId();

        // When
        PaymentRecordEntity result = paymentRecordDao.selectByPaymentId(paymentId);

        // Then
        assertNotNull(result);
        assertEquals(paymentId, result.getPaymentId());
    }

    @Test
    @DisplayName("测试根据支付订单号查询-不存在")
    void testSelectByPaymentId_NotFound() {
        // When
        PaymentRecordEntity result = paymentRecordDao.selectByPaymentId("NON_EXISTENT");

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("测试根据用户ID查询-成功")
    void testSelectByUserId_Success() {
        // Given
        paymentRecordDao.insert(testPaymentRecord);
        Long userId = testPaymentRecord.getUserId();

        // When
        List<PaymentRecordEntity> result = paymentRecordDao.selectByUserId(userId, 10);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(p -> p.getUserId().equals(userId)));
    }

    @Test
    @DisplayName("测试根据用户ID查询-无结果")
    void testSelectByUserId_NoResult() {
        // When
        List<PaymentRecordEntity> result = paymentRecordDao.selectByUserId(99999L, 10);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试更新支付记录-成功")
    void testUpdate_Success() {
        // Given
        paymentRecordDao.insert(testPaymentRecord);
        testPaymentRecord.setPaymentStatus(4); // 更新状态为支付失败

        // When
        int result = paymentRecordDao.updateById(testPaymentRecord);

        // Then
        assertEquals(1, result);

        // 验证更新
        PaymentRecordEntity updated = paymentRecordDao.selectById(testPaymentRecord.getPaymentId());
        assertNotNull(updated);
        assertEquals(Integer.valueOf(4), updated.getPaymentStatus());
        assertEquals("FAILED", updated.getStatus()); // 使用兼容方法
    }

    @Test
    @DisplayName("测试删除支付记录-成功")
    void testDelete_Success() {
        // Given
        paymentRecordDao.insert(testPaymentRecord);
        String paymentId = testPaymentRecord.getPaymentId();

        // When
        int result = paymentRecordDao.deleteById(paymentId);

        // Then
        assertEquals(1, result);

        // 验证删除
        PaymentRecordEntity deleted = paymentRecordDao.selectById(paymentId);
        assertNull(deleted);
    }

    // ==================== 边界条件测试 ====================

    @Test
    @DisplayName("测试插入支付记录-支付订单号为null")
    void testInsert_PaymentIdIsNull() {
        // Given
        testPaymentRecord.setPaymentId(null);

        // When & Then
        // 根据业务规则，paymentId可能是必填字段
        // 如果必填，应该抛出异常；如果可选，应该正常插入
        assertDoesNotThrow(() -> {
            paymentRecordDao.insert(testPaymentRecord);
        });
    }

    @Test
    @DisplayName("测试插入支付记录-金额为null")
    void testInsert_AmountIsNull() {
        // Given
        testPaymentRecord.setPaymentAmount(null);
        testPaymentRecord.setActualAmount(null);

        // When & Then
        // 根据业务规则，amount可能是必填字段
        assertDoesNotThrow(() -> {
            paymentRecordDao.insert(testPaymentRecord);
        });
    }

    @Test
    @DisplayName("测试根据用户ID查询-用户ID为null")
    void testSelectByUserId_UserIdIsNull() {
        // When & Then
        assertThrows(Exception.class, () -> {
            paymentRecordDao.selectByUserId(null, 10);
        });
    }
}


