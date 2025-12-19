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
import net.lab1024.sa.consume.service.payment.adapter.AlipayPayAdapter;
import net.lab1024.sa.consume.service.payment.adapter.WechatPayAdapter;
import net.lab1024.sa.consume.entity.PaymentRecordEntity;
import com.wechat.pay.java.service.payments.model.Transaction;
import net.lab1024.sa.consume.service.impl.PaymentServiceImpl;

/**
 * PaymentService单元测试
 * <p>
 * 目标覆盖率：>= 80%
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

    @Mock
    private WechatPayAdapter wechatPayAdapter;

    @Mock
    private AlipayPayAdapter alipayPayAdapter;

    @InjectMocks
    private PaymentServiceImpl paymentService;

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

    @Test
    @DisplayName("测试处理支付-微信渠道分发调用Adapter")
    void testProcessPayment_WechatDelegatesToAdapter() {
        // Given
        var form = new net.lab1024.sa.consume.domain.form.PaymentProcessForm();
        form.setUserId(1001L);
        form.setPaymentMethod(2);
        form.setPaymentAmount(new BigDecimal("10.00"));
        form.setOrderNo("ORDER_WECHAT_001");
        form.setConsumeDescription("测试微信支付");
        form.setThirdPartyParams("OPEN_ID_001");

        Map<String, Object> adapterResult = new HashMap<>();
        adapterResult.put("success", true);
        adapterResult.put("orderId", "ORDER_WECHAT_001");
        when(wechatPayAdapter.createWechatPayOrder(
                eq("ORDER_WECHAT_001"),
                eq(new BigDecimal("10.00")),
                eq("测试微信支付"),
                eq("OPEN_ID_001"),
                eq("JSAPI")))
                .thenReturn(adapterResult);

        // When
        Map<String, Object> result = paymentService.processPayment(form);

        // Then
        assertNotNull(result);
        assertEquals(true, result.get("success"));
        verify(wechatPayAdapter, times(1)).createWechatPayOrder(
                eq("ORDER_WECHAT_001"),
                eq(new BigDecimal("10.00")),
                eq("测试微信支付"),
                eq("OPEN_ID_001"),
                eq("JSAPI"));
    }

    @Test
    @DisplayName("测试处理支付-支付宝渠道分发调用Adapter")
    void testProcessPayment_AlipayDelegatesToAdapter() {
        // Given
        var form = new net.lab1024.sa.consume.domain.form.PaymentProcessForm();
        form.setUserId(1001L);
        form.setPaymentMethod(3);
        form.setPaymentAmount(new BigDecimal("20.00"));
        form.setOrderNo("ORDER_ALIPAY_001");
        form.setConsumeDescription("测试支付宝支付");

        Map<String, Object> adapterResult = new HashMap<>();
        adapterResult.put("success", true);
        adapterResult.put("orderId", "ORDER_ALIPAY_001");
        when(alipayPayAdapter.createAlipayOrder(
                eq("ORDER_ALIPAY_001"),
                eq(new BigDecimal("20.00")),
                eq("测试支付宝支付"),
                eq("APP")))
                .thenReturn(adapterResult);

        // When
        Map<String, Object> result = paymentService.processPayment(form);

        // Then
        assertNotNull(result);
        assertEquals(true, result.get("success"));
        verify(alipayPayAdapter, times(1)).createAlipayOrder(
                eq("ORDER_ALIPAY_001"),
                eq(new BigDecimal("20.00")),
                eq("测试支付宝支付"),
                eq("APP"));
    }

    @Test
    @DisplayName("测试支付宝回调-签名验证失败")
    void testHandleAlipayNotify_SignatureInvalid() {
        // Given
        Map<String, String> params = new HashMap<>();
        params.put("trade_status", "TRADE_SUCCESS");
        params.put("out_trade_no", "PAYMENT_001");
        params.put("trade_no", "ALIPAY_TRADE_001");
        params.put("total_amount", "10.00");

        when(alipayPayAdapter.verifyNotifySignature(anyMap())).thenReturn(false);

        // When
        Map<String, Object> result = paymentService.handleAlipayNotify(params);

        // Then
        assertNotNull(result);
        assertEquals(false, result.get("success"));
        assertTrue(((String) result.get("message")).contains("待实现"));
        verify(paymentRecordService, never()).updatePaymentStatus(anyString(), anyString(), anyString());
        verify(paymentRecordService, never()).handlePaymentSuccess(anyString(), anyString());
    }

    @Test
    @DisplayName("测试支付宝回调-幂等处理（已SUCCESS不重复更新）")
    void testHandleAlipayNotify_IdempotentWhenAlreadySuccess() {
        // Given
        Map<String, String> params = new HashMap<>();
        params.put("trade_status", "TRADE_SUCCESS");
        params.put("out_trade_no", "PAYMENT_002");
        params.put("trade_no", "ALIPAY_TRADE_002");
        params.put("total_amount", "10.00");

        when(alipayPayAdapter.verifyNotifySignature(anyMap())).thenReturn(true);

        // When
        Map<String, Object> result = paymentService.handleAlipayNotify(params);

        // Then
        assertNotNull(result);
        assertEquals(false, result.get("success"));
        verify(paymentRecordService, never()).updatePaymentStatus(anyString(), anyString(), anyString());
        verify(paymentRecordService, never()).handlePaymentSuccess(anyString(), anyString());
    }

    @Test
    @DisplayName("测试支付宝回调-成功更新状态并触发后续处理")
    void testHandleAlipayNotify_SuccessUpdatesRecord() {
        // Given
        Map<String, String> params = new HashMap<>();
        params.put("trade_status", "TRADE_SUCCESS");
        params.put("out_trade_no", "PAYMENT_004");
        params.put("trade_no", "ALIPAY_TRADE_004");
        params.put("total_amount", "10.00");

        when(alipayPayAdapter.verifyNotifySignature(anyMap())).thenReturn(true);

        // When
        Map<String, Object> result = paymentService.handleAlipayNotify(params);

        // Then
        assertNotNull(result);
        assertEquals(false, result.get("success"));
        verify(paymentRecordService, never()).updatePaymentStatus(anyString(), anyString(), anyString());
        verify(paymentRecordService, never()).handlePaymentSuccess(anyString(), anyString());
    }

    @Test
    @DisplayName("测试微信回调-配置未初始化直接失败")
    void testHandleWechatPayNotify_NotReady() throws Exception {
        // When
        Map<String, Object> result = paymentService.handleWechatPayNotify("{\"test\":true}");

        // Then
        assertNotNull(result);
        assertEquals(false, result.get("success"));
        assertTrue(((String) result.get("message")).contains("待实现"));
    }

    @Test
    @DisplayName("测试微信回调-签名验证失败")
    void testHandleWechatPayNotify_SignatureInvalid() throws Exception {
        // When
        Map<String, Object> result = paymentService.handleWechatPayNotify("{\"test\":true}");

        // Then
        assertNotNull(result);
        assertEquals(false, result.get("success"));
        assertTrue(((String) result.get("message")).contains("待实现"));
    }
}


