package net.lab1024.sa.consume.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.consume.dao.PaymentRecordDao;
import net.lab1024.sa.consume.manager.MultiPaymentManager;

/**
 * PaymentService单元测试
 * <p>
 * 目标覆盖率：≥80%
 * 测试范围：支付相关核心业务方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService单元测试")
class PaymentServiceTest {

    @Mock
    private PaymentRecordDao paymentRecordDao;

    @Mock
    private MultiPaymentManager multiPaymentManager;

    @Mock
    private net.lab1024.sa.consume.service.payment.PaymentRecordService paymentRecordService;

    @Mock
    private net.lab1024.sa.common.gateway.GatewayServiceClient gatewayServiceClient;

    @Mock
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        // 准备测试数据
    }

    @Test
    @DisplayName("测试创建银行支付订单-成功场景")
    void testCreateBankPaymentOrder_Success() {
        // Given
        Long accountId = 1001L;
        BigDecimal amount = new BigDecimal("100.00");
        String orderId = "ORDER001";
        String description = "测试订单";
        String bankCardNo = "6222021234567890";

        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("success", true);
        expectedResult.put("tradeNo", "BANK001");
        expectedResult.put("message", "银行支付成功");

        when(multiPaymentManager.processBankPayment(
                eq(accountId), eq(amount), eq(orderId), eq(description), eq(bankCardNo)))
                .thenReturn(expectedResult);

        // When
        Map<String, Object> result = paymentService.createBankPaymentOrder(
                accountId, amount, orderId, description, bankCardNo);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("BANK001", result.get("tradeNo"));
        verify(multiPaymentManager, times(1)).processBankPayment(
                eq(accountId), eq(amount), eq(orderId), eq(description), eq(bankCardNo));
    }

    @Test
    @DisplayName("测试创建银行支付订单-失败场景")
    void testCreateBankPaymentOrder_Failure() {
        // Given
        Long accountId = 1001L;
        BigDecimal amount = new BigDecimal("100.00");
        String orderId = "ORDER001";
        String description = "测试订单";
        String bankCardNo = "6222021234567890";

        Map<String, Object> errorResult = new HashMap<>();
        errorResult.put("success", false);
        errorResult.put("message", "银行支付失败");

        when(multiPaymentManager.processBankPayment(any(), any(), any(), any(), any()))
                .thenReturn(errorResult);

        // When
        Map<String, Object> result = paymentService.createBankPaymentOrder(
                accountId, amount, orderId, description, bankCardNo);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertTrue(((String) result.get("message")).contains("银行支付失败"));
    }

    @Test
    @DisplayName("测试处理信用额度支付-成功场景")
    void testProcessCreditLimitPayment_Success() {
        // Given
        Long accountId = 1001L;
        BigDecimal amount = new BigDecimal("50.00");
        String orderId = "ORDER002";
        String reason = "测试信用额度支付";

        when(multiPaymentManager.deductCreditLimit(
                eq(accountId), eq(amount), eq(orderId), eq(reason)))
                .thenReturn(true);
        when(multiPaymentManager.isPaymentMethodEnabled("CREDIT"))
                .thenReturn(true);
        when(multiPaymentManager.checkCreditLimitSufficient(eq(accountId), eq(amount)))
                .thenReturn(true);

        // When
        Map<String, Object> result = paymentService.processCreditLimitPayment(accountId, amount, orderId, reason);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        verify(multiPaymentManager, times(1)).deductCreditLimit(
                eq(accountId), eq(amount), eq(orderId), eq(reason));
    }

    @Test
    @DisplayName("测试处理信用额度支付-失败场景")
    void testProcessCreditLimitPayment_Failure() {
        // Given
        Long accountId = 1001L;
        BigDecimal amount = new BigDecimal("50.00");
        String orderId = "ORDER002";
        String reason = "测试信用额度支付";

        when(multiPaymentManager.isPaymentMethodEnabled("CREDIT"))
                .thenReturn(true);
        when(multiPaymentManager.checkCreditLimitSufficient(eq(accountId), eq(amount)))
                .thenReturn(false);
        when(multiPaymentManager.getCreditLimit(eq(accountId)))
                .thenReturn(new BigDecimal("30.00"));

        // When
        Map<String, Object> result = paymentService.processCreditLimitPayment(accountId, amount, orderId, reason);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertTrue(((String) result.get("message")).contains("信用额度不足"));
    }
}

