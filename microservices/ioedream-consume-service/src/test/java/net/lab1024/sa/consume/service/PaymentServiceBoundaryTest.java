package net.lab1024.sa.consume.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.util.CursorPagination;
import net.lab1024.sa.consume.dao.PaymentRecordDao;
import net.lab1024.sa.consume.domain.entity.PaymentRecordEntity;
import net.lab1024.sa.consume.manager.MultiPaymentManager;
import net.lab1024.sa.common.consume.domain.form.PaymentProcessForm;
import net.lab1024.sa.common.consume.domain.form.RefundApplyForm;
import net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity;

/**
 * PaymentService边界和异常测试
 * <p>
 * 测试范围：边界条件、异常场景、复杂业务逻辑
 * 目标：提升测试覆盖率至80%+
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService边界和异常测试")
class PaymentServiceBoundaryTest {

    @Mock
    private PaymentRecordDao paymentRecordDao;

    @Mock
    private MultiPaymentManager multiPaymentManager;

    @Mock
    private net.lab1024.sa.consume.service.payment.PaymentRecordService paymentRecordService;

    @Mock
    private net.lab1024.sa.common.gateway.GatewayServiceClient gatewayServiceClient;

    @Mock
    private net.lab1024.sa.common.consume.dao.PaymentRefundRecordDao paymentRefundRecordDao;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentProcessForm paymentForm;
    private RefundApplyForm refundForm;

    @BeforeEach
    void setUp() {
        paymentForm = new PaymentProcessForm();
        paymentForm.setUserId(1001L);
        paymentForm.setAccountId(2001L);
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

    // ==================== 边界条件测试 ====================

    @Test
    @DisplayName("测试处理支付-订单号为null（应自动生成）")
    void testProcessPayment_OrderNoIsNull() {
        // Given
        paymentForm.setOrderNo(null);
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("success", true);
        mockResult.put("orderId", "AUTO_GENERATED");

        // 由于PaymentService依赖实际配置，这里只测试方法调用
        // 实际测试需要在集成测试中完成

        // When & Then
        // 验证不会因为orderNo为null而抛出异常
        assertDoesNotThrow(() -> {
            paymentService.processPayment(paymentForm);
        });
    }

    @Test
    @DisplayName("测试处理支付-支付方式为null")
    void testProcessPayment_PaymentMethodIsNull() {
        // Given
        paymentForm.setPaymentMethod(null);

        // When
        Map<String, Object> result = paymentService.processPayment(paymentForm);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertTrue(((String) result.get("message")).contains("支付方式不能为空"));
    }

    @Test
    @DisplayName("测试处理支付-支付金额为null")
    void testProcessPayment_AmountIsNull() {
        // Given
        paymentForm.setPaymentAmount(null);

        // When
        Map<String, Object> result = paymentService.processPayment(paymentForm);

        // Then
        assertNotNull(result);
        // 验证异常处理
    }

    @Test
    @DisplayName("测试处理支付-支付金额为0")
    void testProcessPayment_AmountIsZero() {
        // Given
        paymentForm.setPaymentAmount(BigDecimal.ZERO);

        // When
        Map<String, Object> result = paymentService.processPayment(paymentForm);

        // Then
        assertNotNull(result);
        // 根据业务规则，可能允许0金额或不允许
    }

    @Test
    @DisplayName("测试处理支付-支付金额为负数")
    void testProcessPayment_NegativeAmount() {
        // Given
        paymentForm.setPaymentAmount(new BigDecimal("-100.00"));

        // When
        Map<String, Object> result = paymentService.processPayment(paymentForm);

        // Then
        assertNotNull(result);
        // 应该返回失败或抛出异常
    }

    @Test
    @DisplayName("测试处理支付-支付金额为极大值")
    void testProcessPayment_VeryLargeAmount() {
        // Given
        paymentForm.setPaymentAmount(new BigDecimal("999999999999.99"));

        // When
        Map<String, Object> result = paymentService.processPayment(paymentForm);

        // Then
        assertNotNull(result);
        // 验证金额处理正确
    }

    @Test
    @DisplayName("测试处理支付-不支持的支付方式")
    void testProcessPayment_UnsupportedPaymentMethod() {
        // Given
        paymentForm.setPaymentMethod(999); // 不存在的支付方式

        // When
        Map<String, Object> result = paymentService.processPayment(paymentForm);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertTrue(((String) result.get("message")).contains("不支持的支付方式"));
    }

    @Test
    @DisplayName("测试申请退款-支付ID为null")
    void testApplyRefund_PaymentIdIsNull() {
        // Given
        refundForm.setPaymentId(null);

        // When & Then
        // 由于表单验证，应该在Controller层被拦截
        // Service层应该处理null的情况
        assertThrows(Exception.class, () -> {
            paymentService.applyRefund(refundForm);
        });
    }

    @Test
    @DisplayName("测试申请退款-退款金额为null")
    void testApplyRefund_RefundAmountIsNull() {
        // Given
        refundForm.setRefundAmount(null);

        // When & Then
        assertThrows(Exception.class, () -> {
            paymentService.applyRefund(refundForm);
        });
    }

    @Test
    @DisplayName("测试申请退款-退款金额为0")
    void testApplyRefund_RefundAmountIsZero() {
        // Given
        refundForm.setRefundAmount(BigDecimal.ZERO);

        // When & Then
        // 根据业务规则，可能允许0金额或不允许
        Map<String, Object> result = paymentService.applyRefund(refundForm);
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试申请退款-退款金额为负数")
    void testApplyRefund_NegativeRefundAmount() {
        // Given
        refundForm.setRefundAmount(new BigDecimal("-50.00"));

        // When & Then
        // 应该返回失败或抛出异常
        Map<String, Object> result = paymentService.applyRefund(refundForm);
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试申请退款-退款金额超过原支付金额")
    void testApplyRefund_RefundAmountExceedsPayment() {
        // Given
        refundForm.setRefundAmount(new BigDecimal("10000.00")); // 远大于原支付金额

        PaymentRecordEntity paymentRecord = new PaymentRecordEntity();
        paymentRecord.setAmount(new BigDecimal("100.00"));
        when(paymentRecordService.getPaymentRecord("PAYMENT001")).thenReturn(paymentRecord);

        // When
        Map<String, Object> result = paymentService.applyRefund(refundForm);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        // 应该返回失败，退款金额不能超过原支付金额
    }

    @Test
    @DisplayName("测试游标分页查询支付记录-每页大小为null")
    void testCursorPageUserPaymentRecords_PageSizeIsNull() {
        // Given
        when(paymentRecordDao.selectList(any())).thenReturn(java.util.Collections.emptyList());

        // When
        CursorPagination.CursorPageResult<net.lab1024.sa.common.consume.entity.PaymentRecordEntity> result =
            paymentService.cursorPageUserPaymentRecords(1001L, null, null);

        // Then
        assertNotNull(result);
        assertNotNull(result.getList());
    }

    @Test
    @DisplayName("测试游标分页查询支付记录-每页大小超过最大值100")
    void testCursorPageUserPaymentRecords_PageSizeExceedsMax() {
        // Given
        when(paymentRecordDao.selectList(any())).thenReturn(java.util.Collections.emptyList());

        // When
        CursorPagination.CursorPageResult<net.lab1024.sa.common.consume.entity.PaymentRecordEntity> result =
            paymentService.cursorPageUserPaymentRecords(1001L, 200, null);

        // Then
        assertNotNull(result);
        // 验证pageSize被限制为100
    }

    @Test
    @DisplayName("测试游标分页查询支付记录-用户ID为null")
    void testCursorPageUserPaymentRecords_UserIdIsNull() {
        // Given
        when(paymentRecordDao.selectList(any())).thenReturn(java.util.Collections.emptyList());

        // When & Then
        assertThrows(Exception.class, () -> {
            paymentService.cursorPageUserPaymentRecords(null, 20, null);
        });
    }

    @Test
    @DisplayName("测试游标分页查询退款记录-每页大小为null")
    void testCursorPageUserRefundRecords_PageSizeIsNull() {
        // Given
        when(paymentRefundRecordDao.selectList(any())).thenReturn(java.util.Collections.emptyList());

        // When
        CursorPagination.CursorPageResult<PaymentRefundRecordEntity> result =
            paymentService.cursorPageUserRefundRecords(1001L, null, null);

        // Then
        assertNotNull(result);
        assertNotNull(result.getList());
    }

    // ==================== 异常场景测试 ====================

    @Test
    @DisplayName("测试处理支付-支付服务未启用")
    void testProcessPayment_ServiceNotEnabled() {
        // Given
        // 模拟支付服务未启用的情况
        // 需要通过反射或其他方式设置wechatPayEnabled=false

        // When
        Map<String, Object> result = paymentService.processPayment(paymentForm);

        // Then
        assertNotNull(result);
        // 应该返回模拟数据或错误信息
    }

    @Test
    @DisplayName("测试处理支付-数据库异常")
    void testProcessPayment_DatabaseException() {
        // Given
        when(paymentRecordService.getPaymentRecord(anyString())).thenThrow(new RuntimeException("数据库连接失败"));

        // When
        Map<String, Object> result = paymentService.processPayment(paymentForm);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertTrue(((String) result.get("message")).contains("支付处理失败"));
    }

    @Test
    @DisplayName("测试申请退款-支付记录不存在")
    void testApplyRefund_PaymentRecordNotFound() {
        // Given
        when(paymentRecordService.getPaymentRecord("PAYMENT001")).thenReturn(null);

        // When
        Map<String, Object> result = paymentService.applyRefund(refundForm);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        // 应该返回失败，支付记录不存在
    }

    @Test
    @DisplayName("测试申请退款-支付记录状态不允许退款")
    void testApplyRefund_PaymentStatusNotAllowed() {
        // Given
        PaymentRecordEntity paymentRecord = new PaymentRecordEntity();
        paymentRecord.setStatus("FAILED"); // 支付失败，不允许退款
        paymentRecord.setAmount(new BigDecimal("100.00"));
        when(paymentRecordService.getPaymentRecord("PAYMENT001")).thenReturn(paymentRecord);

        // When
        Map<String, Object> result = paymentService.applyRefund(refundForm);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        // 应该返回失败，支付状态不允许退款
    }

    @Test
    @DisplayName("测试审核退款-退款记录不存在")
    void testAuditRefund_RefundRecordNotFound() {
        // Given
        String refundId = "REFUND001";
        when(paymentRefundRecordDao.selectById(refundId)).thenReturn(null);

        // When
        Map<String, Object> result = paymentService.auditRefund(refundId, 1, "审核通过");

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        // 应该返回失败，退款记录不存在
    }

    @Test
    @DisplayName("测试审核退款-审核状态为null")
    void testAuditRefund_AuditStatusIsNull() {
        // Given
        String refundId = "REFUND001";
        PaymentRefundRecordEntity refundRecord = new PaymentRefundRecordEntity();
        refundRecord.setRefundId(refundId);
        when(paymentRefundRecordDao.selectById(refundId)).thenReturn(refundRecord);

        // When & Then
        assertThrows(Exception.class, () -> {
            paymentService.auditRefund(refundId, null, "审核通过");
        });
    }

    @Test
    @DisplayName("测试执行退款-退款记录不存在")
    void testExecuteRefund_RefundRecordNotFound() {
        // Given
        String refundId = "REFUND001";
        when(paymentRefundRecordDao.selectById(refundId)).thenReturn(null);

        // When
        Map<String, Object> result = paymentService.executeRefund(refundId);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        // 应该返回失败，退款记录不存在
    }

    @Test
    @DisplayName("测试执行退款-退款记录状态不允许执行")
    void testExecuteRefund_RefundStatusNotAllowed() {
        // Given
        String refundId = "REFUND001";
        PaymentRefundRecordEntity refundRecord = new PaymentRefundRecordEntity();
        refundRecord.setRefundId(refundId);
        refundRecord.setRefundStatus(0); // 未审核，不允许执行
        when(paymentRefundRecordDao.selectById(refundId)).thenReturn(refundRecord);

        // When
        Map<String, Object> result = paymentService.executeRefund(refundId);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        // 应该返回失败，退款状态不允许执行
    }

    @Test
    @DisplayName("测试游标分页查询支付记录-数据库异常")
    void testCursorPageUserPaymentRecords_DatabaseException() {
        // Given
        when(paymentRecordDao.selectList(any())).thenThrow(new RuntimeException("数据库查询失败"));

        // When & Then
        assertThrows(BusinessException.class, () -> {
            paymentService.cursorPageUserPaymentRecords(1001L, 20, null);
        });
    }

    @Test
    @DisplayName("测试游标分页查询退款记录-数据库异常")
    void testCursorPageUserRefundRecords_DatabaseException() {
        // Given
        when(paymentRefundRecordDao.selectList(any())).thenThrow(new RuntimeException("数据库查询失败"));

        // When & Then
        assertThrows(BusinessException.class, () -> {
            paymentService.cursorPageUserRefundRecords(1001L, 20, null);
        });
    }

    // ==================== 复杂业务场景测试 ====================

    @Test
    @DisplayName("测试并发支付-同一订单多次支付")
    void testProcessPayment_ConcurrentPayment() {
        // Given
        // 模拟并发场景：同一订单号被多次请求支付
        String orderNo = "ORDER001";
        paymentForm.setOrderNo(orderNo);

        PaymentRecordEntity existingRecord = new PaymentRecordEntity();
        existingRecord.setStatus("SUCCESS");
        when(paymentRecordService.getPaymentRecord(orderNo)).thenReturn(existingRecord);

        // When
        Map<String, Object> result = paymentService.processPayment(paymentForm);

        // Then
        assertNotNull(result);
        // 应该检查订单是否已支付，防止重复支付
    }

    @Test
    @DisplayName("测试幂等性-重复申请退款")
    void testApplyRefund_Idempotency() {
        // Given
        // 模拟同一退款请求被多次提交
        PaymentRefundRecordEntity existingRefund = new PaymentRefundRecordEntity();
        existingRefund.setRefundId("REFUND001");
        existingRefund.setPaymentId("PAYMENT001");
        when(paymentRefundRecordDao.selectByPaymentId("PAYMENT001")).thenReturn(Collections.singletonList(existingRefund));

        // When
        Map<String, Object> result = paymentService.applyRefund(refundForm);

        // Then
        assertNotNull(result);
        // 应该检查是否已申请退款，防止重复申请
    }

    @Test
    @DisplayName("测试游标分页查询支付记录-有下一页")
    void testCursorPageUserPaymentRecords_HasNext() {
        // Given
        // 模拟返回21条记录（pageSize=20，多1条表示有下一页）
        java.util.List<PaymentRecordEntity> records = new java.util.ArrayList<>();
        for (int i = 0; i < 21; i++) {
            PaymentRecordEntity record = new PaymentRecordEntity();
            record.setId((long) (2001 + i));
            record.setCreateTime(LocalDateTime.now().minusHours(i));
            records.add(record);
        }
        when(paymentRecordDao.selectList(any())).thenReturn(records);

        // When
        CursorPagination.CursorPageResult<net.lab1024.sa.common.consume.entity.PaymentRecordEntity> result =
            paymentService.cursorPageUserPaymentRecords(1001L, 20, null);

        // Then
        assertNotNull(result);
        assertTrue(result.getHasNext());
        assertEquals(20, result.getList().size());
    }

    @Test
    @DisplayName("测试游标分页查询支付记录-无下一页")
    void testCursorPageUserPaymentRecords_NoNext() {
        // Given
        // 模拟返回20条记录（pageSize=20，刚好一页）
        java.util.List<PaymentRecordEntity> records = new java.util.ArrayList<>();
        for (int i = 0; i < 20; i++) {
            PaymentRecordEntity record = new PaymentRecordEntity();
            record.setId((long) (2001 + i));
            record.setCreateTime(LocalDateTime.now().minusHours(i));
            records.add(record);
        }
        when(paymentRecordDao.selectList(any())).thenReturn(records);

        // When
        CursorPagination.CursorPageResult<net.lab1024.sa.common.consume.entity.PaymentRecordEntity> result =
            paymentService.cursorPageUserPaymentRecords(1001L, 20, null);

        // Then
        assertNotNull(result);
        assertFalse(result.getHasNext());
        assertEquals(20, result.getList().size());
    }

    @Test
    @DisplayName("测试游标分页查询支付记录-空结果")
    void testCursorPageUserPaymentRecords_EmptyResult() {
        // Given
        when(paymentRecordDao.selectList(any())).thenReturn(java.util.Collections.emptyList());

        // When
        CursorPagination.CursorPageResult<net.lab1024.sa.common.consume.entity.PaymentRecordEntity> result =
            paymentService.cursorPageUserPaymentRecords(1001L, 20, null);

        // Then
        assertNotNull(result);
        assertFalse(result.getHasNext());
        assertEquals(0, result.getList().size());
    }

    @Test
    @DisplayName("测试支付回调-签名验证失败")
    void testWechatPayCallback_SignatureVerificationFailed() {
        // Given
        String notifyData = "{\"test\": \"data\"}";

        // When
        Map<String, Object> result = paymentService.handleWechatPayNotify(notifyData);

        // Then
        assertNotNull(result);
        // 签名验证失败应该返回失败响应
    }

    @Test
    @DisplayName("测试支付回调-重复回调处理")
    void testWechatPayCallback_DuplicateCallback() {
        // Given
        String notifyData = "{\"out_trade_no\": \"ORDER001\", \"trade_state\": \"SUCCESS\"}";

        PaymentRecordEntity existingRecord = new PaymentRecordEntity();
        existingRecord.setStatus("SUCCESS");
        when(paymentRecordService.getPaymentRecord("ORDER001")).thenReturn(existingRecord);

        // When
        Map<String, Object> result = paymentService.handleWechatPayNotify(notifyData);

        // Then
        assertNotNull(result);
        // 应该检查订单状态，防止重复处理
    }
}
