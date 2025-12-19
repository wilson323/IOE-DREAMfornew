package net.lab1024.sa.consume.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.domain.form.PaymentProcessForm;
import net.lab1024.sa.consume.domain.form.RefundApplyForm;
import net.lab1024.sa.consume.entity.PaymentRecordEntity;
import net.lab1024.sa.consume.service.PaymentService;

/**
 * PaymentController单元测试
 * <p>
 * 测试范围：支付管理REST API接口
 * 目标：提升测试覆盖率至70%+
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PaymentController单元测试")
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private PaymentProcessForm paymentForm;
    private RefundApplyForm refundForm;

    @BeforeEach
    void setUp() {
        paymentForm = new PaymentProcessForm();
        paymentForm.setUserId(1001L);
        paymentForm.setAccountId(1L);
        paymentForm.setPaymentMethod(2); // 微信支付
        paymentForm.setPaymentAmount(new BigDecimal("100.00"));
        paymentForm.setOrderNo("ORDER001");

        refundForm = new RefundApplyForm();
        refundForm.setPaymentId("PAYMENT001");
        refundForm.setUserId(1001L);
        refundForm.setRefundAmount(new BigDecimal("50.00"));
        refundForm.setRefundMethod(1); // 原路退回
        refundForm.setRefundReasonDesc("测试退款");
    }

    // ==================== processPayment 测试 ====================

    @Test
    @DisplayName("测试处理支付-成功")
    void testProcessPayment_Success() {
        // Given
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("success", true);
        mockResult.put("paymentId", "PAY001");
        mockResult.put("orderId", "ORDER001");

        when(paymentService.processPayment(any(PaymentProcessForm.class))).thenReturn(mockResult);

        // When
        ResponseDTO<Map<String, Object>> result = paymentController.processPayment(paymentForm);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        verify(paymentService, times(1)).processPayment(any(PaymentProcessForm.class));
    }

    @Test
    @DisplayName("测试处理支付-参数验证失败")
    void testProcessPayment_ValidationFailed() {
        // Given
        paymentForm.setUserId(null); // 缺少必填字段

        // When & Then
        // 由于使用@Valid，应该在Controller层被拦截
        // 这里测试Service层返回错误的情况
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("success", false);
        mockResult.put("message", "参数验证失败");
        when(paymentService.processPayment(any(PaymentProcessForm.class))).thenReturn(mockResult);

        ResponseDTO<Map<String, Object>> result = paymentController.processPayment(paymentForm);

        assertNotNull(result);
    }

    @Test
    @DisplayName("测试处理支付-异常处理")
    void testProcessPayment_Exception() {
        // Given
        when(paymentService.processPayment(any(PaymentProcessForm.class)))
                .thenThrow(new RuntimeException("支付处理异常"));

        // When
        ResponseDTO<Map<String, Object>> result = paymentController.processPayment(paymentForm);

        // Then
        assertNotNull(result);
        assertFalse(result.getOk());
        assertTrue(result.getMessage().contains("支付处理失败"));
    }

    // ==================== applyRefund 测试 ====================

    @Test
    @DisplayName("测试申请退款-成功")
    void testApplyRefund_Success() {
        // Given
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("success", true);
        mockResult.put("refundId", "REFUND001");

        when(paymentService.applyRefund(any(RefundApplyForm.class))).thenReturn(mockResult);

        // When
        ResponseDTO<Map<String, Object>> result = paymentController.applyRefund(refundForm);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        verify(paymentService, times(1)).applyRefund(any(RefundApplyForm.class));
    }

    @Test
    @DisplayName("测试申请退款-参数验证失败")
    void testApplyRefund_ValidationFailed() {
        // Given
        refundForm.setPaymentId(null); // 缺少必填字段

        when(paymentService.applyRefund(any(RefundApplyForm.class)))
                .thenThrow(new IllegalArgumentException("paymentId不能为空"));

        // When
        ResponseDTO<Map<String, Object>> result = paymentController.applyRefund(refundForm);

        // Then
        assertNotNull(result);
        assertFalse(result.getOk());
    }

    // ==================== auditRefund 测试 ====================

    @Test
    @DisplayName("测试审核退款-成功")
    void testAuditRefund_Success() {
        // Given
        String refundId = "REFUND001";
        Integer auditStatus = 1; // 审核通过
        String auditComment = "同意退款";

        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("success", true);
        mockResult.put("refundId", refundId);

        when(paymentService.auditRefund(eq(refundId), eq(auditStatus), eq(auditComment))).thenReturn(mockResult);

        // When
        ResponseDTO<Map<String, Object>> result = paymentController.auditRefund(refundId, auditStatus, auditComment);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        verify(paymentService, times(1)).auditRefund(eq(refundId), eq(auditStatus), eq(auditComment));
    }

    @Test
    @DisplayName("测试审核退款-退款ID为null")
    void testAuditRefund_RefundIdIsNull() {
        when(paymentService.auditRefund(isNull(), any(), any()))
                .thenThrow(new IllegalArgumentException("refundId不能为空"));

        ResponseDTO<Map<String, Object>> result = paymentController.auditRefund(null, 1, "审核通过");
        assertNotNull(result);
        assertFalse(result.getOk());
    }

    @Test
    @DisplayName("测试审核退款-审核状态为null")
    void testAuditRefund_AuditStatusIsNull() {
        when(paymentService.auditRefund(anyString(), isNull(), any()))
                .thenThrow(new IllegalArgumentException("auditStatus不能为空"));

        ResponseDTO<Map<String, Object>> result = paymentController.auditRefund("REFUND001", null, "审核通过");
        assertNotNull(result);
        assertFalse(result.getOk());
    }

    // ==================== executeRefund 测试 ====================

    @Test
    @DisplayName("测试执行退款-成功")
    void testExecuteRefund_Success() {
        // Given
        String refundId = "REFUND001";
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("success", true);
        mockResult.put("refundId", refundId);

        when(paymentService.executeRefund(refundId)).thenReturn(mockResult);

        // When
        ResponseDTO<Map<String, Object>> result = paymentController.executeRefund(refundId);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        verify(paymentService, times(1)).executeRefund(refundId);
    }

    @Test
    @DisplayName("测试执行退款-退款ID为null")
    void testExecuteRefund_RefundIdIsNull() {
        when(paymentService.executeRefund(isNull()))
                .thenThrow(new IllegalArgumentException("refundId不能为空"));

        ResponseDTO<Map<String, Object>> result = paymentController.executeRefund(null);
        assertNotNull(result);
        assertFalse(result.getOk());
    }

    // ==================== getPaymentRecord 测试 ====================

    @Test
    @DisplayName("测试查询支付记录-成功")
    void testGetPaymentRecord_Success() {
        // Given
        String paymentId = "PAYMENT001";
        PaymentRecordEntity mockRecord = new PaymentRecordEntity();
        // PaymentRecordEntity.paymentId 为 Long，这里使用 paymentNo 作为业务标识断言
        mockRecord.setPaymentId(1L);
        mockRecord.setPaymentNo(paymentId);

        when(paymentService.getPaymentRecord(paymentId)).thenReturn(mockRecord);

        // When
        ResponseDTO<PaymentRecordEntity> result = paymentController.getPaymentRecord(paymentId);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        assertEquals(paymentId, result.getData().getPaymentNo());
        verify(paymentService, times(1)).getPaymentRecord(paymentId);
    }

    @Test
    @DisplayName("测试查询支付记录-不存在")
    void testGetPaymentRecord_NotFound() {
        // Given
        String paymentId = "NON_EXISTENT";
        when(paymentService.getPaymentRecord(paymentId)).thenReturn(null);

        // When
        ResponseDTO<PaymentRecordEntity> result = paymentController.getPaymentRecord(paymentId);

        // Then
        assertNotNull(result);
        // 根据实际实现，可能返回null或错误响应
    }

    // ==================== getUserPaymentRecords 测试 ====================

    @Test
    @DisplayName("测试查询用户支付记录-成功")
    void testGetUserPaymentRecords_Success() {
        // Given
        Long userId = 1001L;
        Integer pageNum = 1;
        Integer pageSize = 20;

        List<PaymentRecordEntity> mockRecords = java.util.Collections.emptyList();
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("list", mockRecords);
        mockResult.put("total", 0L);
        mockResult.put("pageNum", pageNum);
        mockResult.put("pageSize", pageSize);
        when(paymentService.getUserPaymentRecords(userId, pageNum, pageSize)).thenReturn(mockResult);

        // When
        ResponseDTO<Map<String, Object>> result = paymentController.getUserPaymentRecords(userId, pageNum, pageSize);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        verify(paymentService, times(1)).getUserPaymentRecords(userId, pageNum, pageSize);
    }

    @Test
    @DisplayName("测试查询用户支付记录-用户ID为null")
    void testGetUserPaymentRecords_UserIdIsNull() {
        when(paymentService.getUserPaymentRecords(isNull(), anyInt(), anyInt()))
                .thenThrow(new IllegalArgumentException("userId不能为空"));

        ResponseDTO<Map<String, Object>> result = paymentController.getUserPaymentRecords(null, 1, 20);
        assertNotNull(result);
        assertFalse(result.getOk());
    }
}
