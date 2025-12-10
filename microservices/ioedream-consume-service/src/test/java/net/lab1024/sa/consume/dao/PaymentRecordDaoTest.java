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
import net.lab1024.sa.consume.domain.entity.PaymentRecordEntity;

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
        testPaymentRecord.setUserId(1001L);
        testPaymentRecord.setAmount(new BigDecimal("100.00"));
        testPaymentRecord.setPaymentMethod("WECHAT"); // WECHAT=微信支付
        testPaymentRecord.setStatus("SUCCESS");
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
        assertNotNull(testPaymentRecord.getId());
    }

    @Test
    @DisplayName("测试根据ID查询支付记录-成功")
    void testSelectById_Success() {
        // Given
        paymentRecordDao.insert(testPaymentRecord);
        Long id = testPaymentRecord.getId();

        // When
        PaymentRecordEntity result = paymentRecordDao.selectById(id);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(testPaymentRecord.getPaymentId(), result.getPaymentId());
    }

    @Test
    @DisplayName("测试根据ID查询支付记录-不存在")
    void testSelectById_NotFound() {
        // When
        PaymentRecordEntity result = paymentRecordDao.selectById(99999L);

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
        List<PaymentRecordEntity> result = paymentRecordDao.selectByUserId(userId);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(p -> p.getUserId().equals(userId)));
    }

    @Test
    @DisplayName("测试根据用户ID查询-无结果")
    void testSelectByUserId_NoResult() {
        // When
        List<PaymentRecordEntity> result = paymentRecordDao.selectByUserId(99999L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试更新支付记录-成功")
    void testUpdate_Success() {
        // Given
        paymentRecordDao.insert(testPaymentRecord);
        testPaymentRecord.setStatus("FAILED"); // 更新状态

        // When
        int result = paymentRecordDao.updateById(testPaymentRecord);

        // Then
        assertEquals(1, result);

        // 验证更新
        PaymentRecordEntity updated = paymentRecordDao.selectById(testPaymentRecord.getId());
        assertNotNull(updated);
        assertEquals("FAILED", updated.getStatus());
    }

    @Test
    @DisplayName("测试删除支付记录-成功")
    void testDelete_Success() {
        // Given
        paymentRecordDao.insert(testPaymentRecord);
        Long id = testPaymentRecord.getId();

        // When
        int result = paymentRecordDao.deleteById(id);

        // Then
        assertEquals(1, result);

        // 验证删除
        PaymentRecordEntity deleted = paymentRecordDao.selectById(id);
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
        testPaymentRecord.setAmount(null);

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
            paymentRecordDao.selectByUserId(null);
        });
    }
}
