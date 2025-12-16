package net.lab1024.sa.consume.service.payment.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.consume.dao.PaymentRecordDao;
import net.lab1024.sa.common.consume.entity.PaymentRecordEntity;
import net.lab1024.sa.consume.service.AccountService;

/**
 * PaymentRecordServiceImpl Unit Test
 * <p>
 * Target Coverage: >= 80%
 * Test Scope: Core business methods of PaymentRecordServiceImpl
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentRecordServiceImpl Unit Test")
class PaymentRecordServiceImplTest {

    @Mock
    private PaymentRecordDao paymentRecordDao;

    @Mock
    private AccountService accountService;

    @Mock
    private GatewayServiceClient gatewayServiceClient;

    @InjectMocks
    private PaymentRecordServiceImpl paymentRecordServiceImpl;

    private PaymentRecordEntity mockPaymentRecord;

    @BeforeEach
    void setUp() {
        // Prepare test data
        mockPaymentRecord = new PaymentRecordEntity();
        mockPaymentRecord.setPaymentId("PAY001");
        mockPaymentRecord.setTransactionNo("TXN001");
        mockPaymentRecord.setOrderNo("ORDER001");
        mockPaymentRecord.setUserId(100L);
        mockPaymentRecord.setAccountId(200L);
        mockPaymentRecord.setPaymentAmount(new BigDecimal("100.00"));
        mockPaymentRecord.setActualAmount(new BigDecimal("100.00"));
        mockPaymentRecord.setPaymentStatus(1); // 1=待支付
        mockPaymentRecord.setPaymentMethod(3); // 3=支付宝
        mockPaymentRecord.setPaymentChannel(3); // 3=移动端
        mockPaymentRecord.setBusinessType(1); // 1=消费
        mockPaymentRecord.setDeviceId("DEV001");
        mockPaymentRecord.setCreateTime(LocalDateTime.now());
        mockPaymentRecord.setDeletedFlag(0);
    }

    @Test
    @DisplayName("Test getPaymentRecord - Success Scenario")
    void test_getPaymentRecord_Success() {
        // Given
        String paymentId = "PAY001";
        when(paymentRecordDao.selectByPaymentId(paymentId)).thenReturn(mockPaymentRecord);

        // When
        PaymentRecordEntity result = paymentRecordServiceImpl.getPaymentRecord(paymentId);

        // Then
        assertNotNull(result);
        assertEquals(paymentId, result.getPaymentId());
        verify(paymentRecordDao, times(1)).selectByPaymentId(paymentId);
    }

    @Test
    @DisplayName("Test getPaymentRecord - Not Found")
    void test_getPaymentRecord_NotFound() {
        // Given
        String paymentId = "NON_EXIST";
        when(paymentRecordDao.selectByPaymentId(paymentId)).thenReturn(null);

        // When
        PaymentRecordEntity result = paymentRecordServiceImpl.getPaymentRecord(paymentId);

        // Then
        assertNull(result);
        verify(paymentRecordDao, times(1)).selectByPaymentId(paymentId);
    }

    @Test
    @DisplayName("Test savePaymentRecord - Success Scenario")
    void test_savePaymentRecord_Success() {
        // Given
        PaymentRecordEntity newRecord = new PaymentRecordEntity();
        newRecord.setPaymentId("PAY002");
        newRecord.setTransactionNo("TXN002");
        newRecord.setOrderNo("ORDER002");
        newRecord.setPaymentAmount(new BigDecimal("50.00"));
        newRecord.setActualAmount(new BigDecimal("50.00"));
        newRecord.setPaymentStatus(1); // 1=待支付
        newRecord.setPaymentMethod(3); // 3=支付宝
        newRecord.setPaymentChannel(3); // 3=移动端
        newRecord.setBusinessType(1); // 1=消费
        newRecord.setDeviceId("DEV001");
        newRecord.setUserId(1001L);
        newRecord.setAccountId(2001L);

        doAnswer(invocation -> {
            // PaymentRecordEntity的主键是paymentId (String)，不需要设置id
            return 1;
        }).when(paymentRecordDao).insert(any(PaymentRecordEntity.class));

        // When
        PaymentRecordEntity result = paymentRecordServiceImpl.savePaymentRecord(newRecord);

        // Then
        assertNotNull(result);
        assertEquals("PAY002", result.getPaymentId());
        verify(paymentRecordDao, times(1)).insert(any(PaymentRecordEntity.class));
    }

    @Test
    @DisplayName("Test updatePaymentStatus - Success Scenario")
    void test_updatePaymentStatus_Success() {
        // Given
        String paymentId = "PAY001";
        String status = "SUCCESS";
        String thirdPartyTransactionId = "THIRD_PARTY_001";

        when(paymentRecordDao.update(isNull(), any())).thenReturn(1);

        // When
        boolean result = paymentRecordServiceImpl.updatePaymentStatus(paymentId, status, thirdPartyTransactionId);

        // Then
        assertTrue(result);
        verify(paymentRecordDao, times(1)).update(isNull(), any());
    }

    @Test
    @DisplayName("Test updatePaymentStatus - Payment Record Not Found")
    void test_updatePaymentStatus_NotFound() {
        // Given
        String paymentId = "NON_EXIST";
        String status = "SUCCESS";
        String thirdPartyTransactionId = "THIRD_PARTY_001";

        when(paymentRecordDao.update(isNull(), any())).thenReturn(0);

        // When
        boolean result = paymentRecordServiceImpl.updatePaymentStatus(paymentId, status, thirdPartyTransactionId);

        // Then
        assertFalse(result);
        verify(paymentRecordDao, times(1)).update(isNull(), any());
    }

    @Test
    @DisplayName("Test handlePaymentSuccess - Success Scenario")
    void test_handlePaymentSuccess_Success() {
        // Given
        String paymentId = "PAY001";
        String transactionId = "TXN001";

        when(paymentRecordDao.selectByPaymentId(paymentId)).thenReturn(mockPaymentRecord);
        when(paymentRecordDao.update(isNull(), any())).thenReturn(1);

        // When
        paymentRecordServiceImpl.handlePaymentSuccess(paymentId, transactionId);

        // Then
        verify(paymentRecordDao, times(1)).selectByPaymentId(paymentId);
        verify(paymentRecordDao, times(1)).update(isNull(), any());
    }

    @Test
    @DisplayName("Test handlePaymentSuccess - Payment Record Not Found")
    void test_handlePaymentSuccess_NotFound() {
        // Given
        String paymentId = "NON_EXIST";
        String transactionId = "TXN001";

        when(paymentRecordDao.selectByPaymentId(paymentId)).thenReturn(null);

        // When
        assertThrows(Exception.class, () -> paymentRecordServiceImpl.handlePaymentSuccess(paymentId, transactionId));

        // Then
        verify(paymentRecordDao, times(1)).selectByPaymentId(paymentId);
        verify(paymentRecordDao, never()).update(isNull(), any());
    }
}


