package net.lab1024.sa.consume.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.lab1024.sa.consume.domain.form.PaymentProcessForm;
import net.lab1024.sa.consume.domain.form.RefundApplyForm;
import net.lab1024.sa.consume.service.impl.PaymentServiceImpl;

/**
 * PaymentService 边界测试（最小可运行版本）
 * <p>
 * 说明：
 * - 该测试只覆盖“入参校验/错误返回”这类不依赖外部环境（DB/网关/第三方支付）的逻辑
 * - 避免使用模拟外部服务，确保在无环境依赖的情况下也能稳定运行
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-19
 */
@DisplayName("PaymentService边界测试")
class PaymentServiceBoundaryTest {

    /**
     * 测试：支付方式为空，应返回失败且不抛异常
     */
    @Test
    @DisplayName("processPayment: paymentMethod为空 -> 返回失败")
    void testProcessPayment_paymentMethodIsNull_shouldFail() {
        PaymentServiceImpl paymentService = new PaymentServiceImpl();

        PaymentProcessForm form = new PaymentProcessForm();
        form.setUserId(1001L);
        form.setAccountId(2001L);
        form.setPaymentMethod(null);
        form.setPaymentAmount(new BigDecimal("10.00"));

        Map<String, Object> result = paymentService.processPayment(form);

        assertNotNull(result);
        assertTrue(result.containsKey("success"));
        assertFalse(Boolean.TRUE.equals(result.get("success")));
    }

    /**
     * 测试：支付金额<=0，应返回失败
     */
    @Test
    @DisplayName("processPayment: paymentAmount<=0 -> 返回失败")
    void testProcessPayment_paymentAmountNotPositive_shouldFail() {
        PaymentServiceImpl paymentService = new PaymentServiceImpl();

        PaymentProcessForm form = new PaymentProcessForm();
        form.setUserId(1001L);
        form.setAccountId(2001L);
        form.setPaymentMethod(1);
        form.setPaymentAmount(BigDecimal.ZERO);

        Map<String, Object> result = paymentService.processPayment(form);

        assertNotNull(result);
        assertFalse(Boolean.TRUE.equals(result.get("success")));
        assertTrue(String.valueOf(result.get("message")).contains("支付金额"));
    }

    /**
     * 测试：退款申请缺少支付ID，应返回失败
     */
    @Test
    @DisplayName("applyRefund: paymentId为空 -> 返回失败")
    void testApplyRefund_paymentIdIsBlank_shouldFail() {
        PaymentServiceImpl paymentService = new PaymentServiceImpl();

        RefundApplyForm form = new RefundApplyForm();
        form.setPaymentId(null);
        form.setUserId(1001L);
        form.setRefundAmount(new BigDecimal("1.00"));

        Map<String, Object> result = paymentService.applyRefund(form);

        assertNotNull(result);
        assertFalse(Boolean.TRUE.equals(result.get("success")));
    }
}
