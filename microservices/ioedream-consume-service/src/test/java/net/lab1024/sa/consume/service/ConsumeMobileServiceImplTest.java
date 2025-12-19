package net.lab1024.sa.consume.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import net.lab1024.sa.consume.service.impl.ConsumeMobileServiceImpl;
import net.lab1024.sa.consume.dao.ConsumeTransactionDao;
import net.lab1024.sa.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.consume.dao.PaymentRecordDao;
import net.lab1024.sa.consume.entity.ConsumeTransactionEntity;
import net.lab1024.sa.consume.domain.form.ConsumeMobileQuickForm;
import net.lab1024.sa.consume.domain.form.ConsumeMobileScanForm;
import net.lab1024.sa.consume.domain.form.ConsumeMobileNfcForm;
import net.lab1024.sa.consume.domain.form.ConsumeMobileFaceForm;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileUserStatsVO;
import net.lab1024.sa.consume.domain.vo.MobileBillDetailVO;
import net.lab1024.sa.common.exception.BusinessException;

/**
 * ConsumeMobileServiceImpl单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：ConsumeMobileServiceImpl核心业务方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConsumeMobileServiceImpl单元测试")
@SuppressWarnings("unchecked")
class ConsumeMobileServiceImplTest {

    @Mock
    private ConsumeTransactionDao consumeTransactionDao;

    @Mock
    private ConsumeRecordDao consumeRecordDao;

    @Mock
    private PaymentRecordDao paymentRecordDao;

    @InjectMocks
    private ConsumeMobileServiceImpl consumeMobileServiceImpl;

    @BeforeEach
    void setUp() {
        // 准备测试数据
    }

    @Test
    @DisplayName("Test quickConsume - Not Implemented")
    void test_quickConsume_NotImplemented() {
        // Given
        ConsumeMobileQuickForm form = new ConsumeMobileQuickForm();

        // When & Then
        assertThrows(BusinessException.class, () -> {
            consumeMobileServiceImpl.quickConsume(form);
        });
    }

    @Test
    @DisplayName("Test scanConsume - Not Implemented")
    void test_scanConsume_NotImplemented() {
        // Given
        ConsumeMobileScanForm form = new ConsumeMobileScanForm();

        // When & Then
        assertThrows(BusinessException.class, () -> {
            consumeMobileServiceImpl.scanConsume(form);
        });
    }

    @Test
    @DisplayName("Test nfcConsume - Not Implemented")
    void test_nfcConsume_NotImplemented() {
        // Given
        ConsumeMobileNfcForm form = new ConsumeMobileNfcForm();

        // When & Then
        assertThrows(BusinessException.class, () -> {
            consumeMobileServiceImpl.nfcConsume(form);
        });
    }

    @Test
    @DisplayName("Test faceConsume - Not Implemented")
    void test_faceConsume_NotImplemented() {
        // Given
        ConsumeMobileFaceForm form = new ConsumeMobileFaceForm();

        // When & Then
        assertThrows(BusinessException.class, () -> {
            consumeMobileServiceImpl.faceConsume(form);
        });
    }

    @Test
    @DisplayName("Test quickUserInfo - Not Implemented")
    void test_quickUserInfo_NotImplemented() {
        // When & Then
        assertThrows(BusinessException.class, () -> {
            consumeMobileServiceImpl.quickUserInfo("phone", "13800138000");
        });
    }

    @Test
    @DisplayName("Test getUserConsumeInfo - Not Implemented")
    void test_getUserConsumeInfo_NotImplemented() {
        // When & Then
        assertThrows(BusinessException.class, () -> {
            consumeMobileServiceImpl.getUserConsumeInfo(1L);
        });
    }

    @Test
    @DisplayName("Test getDeviceConfig - Not Implemented")
    void test_getDeviceConfig_NotImplemented() {
        // When & Then
        assertThrows(BusinessException.class, () -> {
            consumeMobileServiceImpl.getDeviceConfig(1L);
        });
    }

    @Test
    @DisplayName("Test getDeviceTodayStats - Not Implemented")
    void test_getDeviceTodayStats_NotImplemented() {
        // When & Then
        assertThrows(BusinessException.class, () -> {
            consumeMobileServiceImpl.getDeviceTodayStats(1L);
        });
    }

    @Test
    @DisplayName("Test getTransactionSummary - Not Implemented")
    void test_getTransactionSummary_NotImplemented() {
        // When & Then
        assertThrows(BusinessException.class, () -> {
            consumeMobileServiceImpl.getTransactionSummary("AREA001");
        });
    }

    @Test
    @DisplayName("Test syncOfflineTransactions - Not Implemented")
    void test_syncOfflineTransactions_NotImplemented() {
        // Given
        net.lab1024.sa.consume.domain.form.ConsumeOfflineSyncForm form =
            new net.lab1024.sa.consume.domain.form.ConsumeOfflineSyncForm();

        // When & Then
        assertThrows(BusinessException.class, () -> {
            consumeMobileServiceImpl.syncOfflineTransactions(form);
        });
    }

    @Test
    @DisplayName("Test getSyncData - Not Implemented")
    void test_getSyncData_NotImplemented() {
        // When & Then
        assertThrows(BusinessException.class, () -> {
            consumeMobileServiceImpl.getSyncData(1L, "2025-01-01 00:00:00");
        });
    }

    @Test
    @DisplayName("Test validateConsumePermission - Not Implemented")
    void test_validateConsumePermission_NotImplemented() {
        // Given
        net.lab1024.sa.consume.domain.form.ConsumePermissionValidateForm form =
            new net.lab1024.sa.consume.domain.form.ConsumePermissionValidateForm();

        // When & Then
        assertThrows(BusinessException.class, () -> {
            consumeMobileServiceImpl.validateConsumePermission(form);
        });
    }

    @Test
    @DisplayName("Test getUserStats - Success Scenario")
    void test_getUserStats_Success() {
        // Given
        Long userId = 1001L;
        List<ConsumeTransactionEntity> totalTransactions = new ArrayList<>();
        ConsumeTransactionEntity tx1 = createMockTransaction(userId, "100.00", "SUCCESS");
        ConsumeTransactionEntity tx2 = createMockTransaction(userId, "50.00", "SUCCESS");
        totalTransactions.add(tx1);
        totalTransactions.add(tx2);

        List<ConsumeTransactionEntity> todayTransactions = new ArrayList<>();
        todayTransactions.add(tx1);

        List<ConsumeTransactionEntity> monthTransactions = new ArrayList<>();
        monthTransactions.add(tx1);
        monthTransactions.add(tx2);

        when(consumeTransactionDao.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(totalTransactions)  // First call for total
            .thenReturn(todayTransactions)  // Second call for today
            .thenReturn(monthTransactions); // Third call for month

        // When
        ConsumeMobileUserStatsVO result = consumeMobileServiceImpl.getUserStats(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(2, result.getTotalCount());
        assertEquals(new BigDecimal("150.00"), result.getTotalAmount());
        assertEquals(1, result.getTodayCount());
        assertEquals(new BigDecimal("100.00"), result.getTodayAmount());
        assertEquals(2, result.getMonthCount());
        assertEquals(new BigDecimal("150.00"), result.getMonthAmount());
        verify(consumeTransactionDao, times(3)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("Test getUserStats - Null UserId")
    void test_getUserStats_NullUserId() {
        // When & Then
        assertThrows(BusinessException.class, () -> {
            consumeMobileServiceImpl.getUserStats(null);
        });
    }

    @Test
    @DisplayName("Test getUserStats - Empty Transactions")
    void test_getUserStats_EmptyTransactions() {
        // Given
        Long userId = 1001L;
        when(consumeTransactionDao.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(new ArrayList<>());

        // When
        ConsumeMobileUserStatsVO result = consumeMobileServiceImpl.getUserStats(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(0, result.getTotalCount());
        assertEquals(BigDecimal.ZERO, result.getTotalAmount());
        assertEquals(0, result.getTodayCount());
        assertEquals(BigDecimal.ZERO, result.getTodayAmount());
        assertEquals(0, result.getMonthCount());
        assertEquals(BigDecimal.ZERO, result.getMonthAmount());
    }

    @Test
    @DisplayName("Test getBillDetail - Success Scenario")
    void test_getBillDetail_Success() {
        // Given
        String orderId = "ORDER001";
        net.lab1024.sa.consume.entity.ConsumeRecordEntity consumeRecord =
            new net.lab1024.sa.consume.entity.ConsumeRecordEntity();
        consumeRecord.setOrderNo(orderId);
        consumeRecord.setTransactionNo("TXN001");
        consumeRecord.setAmount(new BigDecimal("50.00"));
        consumeRecord.setConsumeTime(LocalDateTime.now());

        net.lab1024.sa.consume.entity.PaymentRecordEntity paymentRecord =
            new net.lab1024.sa.consume.entity.PaymentRecordEntity();
        paymentRecord.setTransactionNo("TXN001");
        paymentRecord.setPaymentAmount(new BigDecimal("50.00"));
        paymentRecord.setActualAmount(new BigDecimal("50.00"));
        paymentRecord.setPaymentStatus(3); // 支付成功
        paymentRecord.setPaymentMethod(3); // 支付宝
        paymentRecord.setPaymentChannel(3); // 移动端
        paymentRecord.setBusinessType(1); // 消费
        paymentRecord.setDeviceId("DEV001");
        paymentRecord.setUserId(1001L);
        paymentRecord.setAccountId(2001L);

        when(consumeRecordDao.selectByOrderNo(orderId)).thenReturn(consumeRecord);
        when(paymentRecordDao.selectByTransactionNo("TXN001")).thenReturn(paymentRecord);

        // When
        MobileBillDetailVO result = consumeMobileServiceImpl.getBillDetail(orderId);

        // Then
        assertNotNull(result);
        assertEquals(orderId, result.getOrderId());
        assertEquals(new BigDecimal("50.00"), result.getAmount());
        verify(consumeRecordDao, times(1)).selectByOrderNo(orderId);
        verify(paymentRecordDao, times(1)).selectByTransactionNo("TXN001");
    }

    @Test
    @DisplayName("Test getBillDetail - Consume Record Not Found")
    void test_getBillDetail_ConsumeRecordNotFound() {
        // Given
        String orderId = "NOT_EXIST";
        when(consumeRecordDao.selectByOrderNo(orderId)).thenReturn(null);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            consumeMobileServiceImpl.getBillDetail(orderId);
        });
        verify(consumeRecordDao, times(1)).selectByOrderNo(orderId);
    }

    @Test
    @DisplayName("Test getBillDetail - Null OrderId")
    void test_getBillDetail_NullOrderId() {
        // When & Then
        assertThrows(BusinessException.class, () -> {
            consumeMobileServiceImpl.getBillDetail(null);
        });
    }

    @Test
    @DisplayName("Test getBillDetail - Empty OrderId")
    void test_getBillDetail_EmptyOrderId() {
        // When & Then
        assertThrows(BusinessException.class, () -> {
            consumeMobileServiceImpl.getBillDetail("");
        });
    }

    // Helper method to create mock transaction
    private ConsumeTransactionEntity createMockTransaction(Long userId, String amount, String status) {
        ConsumeTransactionEntity entity = new ConsumeTransactionEntity();
        entity.setUserId(userId);
        entity.setAmount(new BigDecimal(amount));
        entity.setStatus(status);
        entity.setCreateTime(LocalDateTime.now());
        entity.setDeletedFlag(0);
        return entity;
    }

}




