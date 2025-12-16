package net.lab1024.sa.consume.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.domain.form.RefundQueryForm;
import net.lab1024.sa.consume.domain.form.RefundRequestForm;
import net.lab1024.sa.consume.domain.vo.RefundRecordVO;
import net.lab1024.sa.consume.service.ConsumeRefundService;

/**
 * ConsumeRefundController单元测试
 * <p>
 * 测试范围：消费退款管理REST API接口
 * 目标：提升测试覆盖率至70%+
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConsumeRefundController单元测试")
class ConsumeRefundControllerTest {

    @Mock
    private ConsumeRefundService refundService;

    @InjectMocks
    private ConsumeRefundController refundController;

    private RefundRequestForm refundRequestForm;

    @BeforeEach
    void setUp() {
        refundRequestForm = new RefundRequestForm();
        refundRequestForm.setTransactionNo("TXN001");
        refundRequestForm.setRefundAmount(new BigDecimal("50.00"));
        refundRequestForm.setRefundReason("测试退款");
    }

    // ==================== applyRefund 测试 ====================

    @Test
    @DisplayName("测试申请退款-成功")
    void testApplyRefund_Success() {
        // Given
        Long refundId = 1L;
        when(refundService.applyRefund(any(RefundRequestForm.class))).thenReturn(refundId);

        // When
        ResponseDTO<Long> result = refundController.applyRefund(refundRequestForm);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        assertEquals(refundId, result.getData());
        verify(refundService, times(1)).applyRefund(any(RefundRequestForm.class));
    }

    @Test
    @DisplayName("测试申请退款-参数验证失败")
    void testApplyRefund_ValidationFailed() {
        // Given
        refundRequestForm.setTransactionNo(null); // 缺少必填字段

        // When & Then
        // 由于使用@Valid，应该在Controller层被拦截
        assertThrows(Exception.class, () -> {
            when(refundService.applyRefund(any(RefundRequestForm.class)))
                    .thenThrow(new IllegalArgumentException("transactionNo不能为空"));
            refundController.applyRefund(refundRequestForm);
        });
    }

    // ==================== refundByTransactionNo 测试 ====================

    @Test
    @DisplayName("测试根据交易号申请退款-成功")
    void testRefundByTransactionNo_Success() {
        // Given
        String transactionNo = "TXN001";
        BigDecimal amount = new BigDecimal("50.00");
        String reason = "误操作";
        String description = "退款说明";

        Long refundId = 1L;
        when(refundService.applyRefund(any(RefundRequestForm.class))).thenReturn(refundId);

        // When
        ResponseDTO<Long> result = refundController.refundByTransactionNo(transactionNo, amount, reason, description);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        assertEquals(refundId, result.getData());
        verify(refundService, times(1)).applyRefund(any(RefundRequestForm.class));
    }

    @Test
    @DisplayName("测试根据交易号申请退款-交易号为空")
    void testRefundByTransactionNo_TransactionNoIsNull() {
        // When & Then
        assertThrows(Exception.class, () -> {
            when(refundService.applyRefund(any(RefundRequestForm.class)))
                    .thenThrow(new IllegalArgumentException("transactionNo不能为空"));
            refundController.refundByTransactionNo(null, new BigDecimal("50.00"), "原因", "");
        });
    }

    // ==================== getRefundDetail 测试 ====================

    @Test
    @DisplayName("测试获取退款记录详情-成功")
    void testGetRefundDetail_Success() {
        // Given
        Long refundId = 1L;
        RefundRecordVO mockVO = new RefundRecordVO();
        mockVO.setRefundId(refundId);

        when(refundService.getRefundDetail(refundId)).thenReturn(mockVO);

        // When
        ResponseDTO<RefundRecordVO> result = refundController.getRefundDetail(refundId);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        assertEquals(refundId, result.getData().getRefundId());
        verify(refundService, times(1)).getRefundDetail(refundId);
    }

    @Test
    @DisplayName("测试获取退款记录详情-退款ID为null")
    void testGetRefundDetail_RefundIdIsNull() {
        // When & Then
        assertThrows(Exception.class, () -> {
            when(refundService.getRefundDetail(isNull())).thenThrow(new IllegalArgumentException("refundId不能为空"));
            refundController.getRefundDetail(null);
        });
    }

    // ==================== getRefundList 测试 ====================

    @Test
    @DisplayName("测试分页查询退款记录-成功")
    void testGetRefundList_Success() {
        // Given
        RefundQueryForm queryForm = new RefundQueryForm();
        queryForm.setPageNum(1);
        queryForm.setPageSize(20);

        PageResult<RefundRecordVO> mockPageResult = new PageResult<>();
        when(refundService.getRefundPage(any(RefundQueryForm.class))).thenReturn(mockPageResult);

        // When
        ResponseDTO<PageResult<RefundRecordVO>> result = refundController.getRefundList(queryForm);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        verify(refundService, times(1)).getRefundPage(any(RefundQueryForm.class));
    }

    // ==================== approveRefund 测试 ====================

    @Test
    @DisplayName("测试审批退款申请-通过")
    void testApproveRefund_Approved() {
        // Given
        Long refundId = 1L;
        Boolean approved = true;
        String comment = "同意退款";

        when(refundService.approveRefund(refundId, approved, comment)).thenReturn(true);

        // When
        ResponseDTO<String> result = refundController.approveRefund(refundId, approved, comment);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        verify(refundService, times(1)).approveRefund(refundId, approved, comment);
    }

    @Test
    @DisplayName("测试审批退款申请-拒绝")
    void testApproveRefund_Rejected() {
        // Given
        Long refundId = 1L;
        Boolean approved = false;
        String comment = "不符合退款条件";

        when(refundService.approveRefund(refundId, approved, comment)).thenReturn(true);

        // When
        ResponseDTO<String> result = refundController.approveRefund(refundId, approved, comment);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        verify(refundService, times(1)).approveRefund(refundId, approved, comment);
    }

    // ==================== cancelRefund 测试 ====================

    @Test
    @DisplayName("测试取消退款申请-成功")
    void testCancelRefund_Success() {
        // Given
        Long refundId = 1L;
        String reason = "不需要退款了";

        when(refundService.cancelRefund(refundId, reason)).thenReturn(true);

        // When
        ResponseDTO<String> result = refundController.cancelRefund(refundId, reason);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        verify(refundService, times(1)).cancelRefund(refundId, reason);
    }

    // ==================== processRefund 测试 ====================

    @Test
    @DisplayName("测试处理退款-成功")
    void testProcessRefund_Success() {
        // Given
        Long refundId = 1L;
        when(refundService.processRefund(refundId)).thenReturn(true);

        // When
        ResponseDTO<String> result = refundController.processRefund(refundId);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        verify(refundService, times(1)).processRefund(refundId);
    }

    @Test
    @DisplayName("测试处理退款-退款ID为null")
    void testProcessRefund_RefundIdIsNull() {
        // When & Then
        assertThrows(Exception.class, () -> {
            when(refundService.processRefund(isNull())).thenThrow(new IllegalArgumentException("refundId不能为空"));
            refundController.processRefund(null);
        });
    }
}


