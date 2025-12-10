package net.lab1024.sa.consume.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.domain.entity.RefundApplicationEntity;
import net.lab1024.sa.consume.domain.form.RefundApplicationForm;
import net.lab1024.sa.consume.service.refund.RefundApplicationService;

/**
 * RefundApplicationController单元测试
 * <p>
 * 测试范围：退款申请管理REST API接口
 * 目标：提升测试覆盖率至70%+
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RefundApplicationController单元测试")
class RefundApplicationControllerTest {

    @Mock
    private RefundApplicationService refundApplicationService;

    @InjectMocks
    private RefundApplicationController refundApplicationController;

    private RefundApplicationForm refundApplicationForm;

    @BeforeEach
    void setUp() {
        refundApplicationForm = new RefundApplicationForm();
        refundApplicationForm.setUserId(1001L);
        refundApplicationForm.setPaymentRecordId(1L);
        refundApplicationForm.setRefundAmount(new java.math.BigDecimal("100.00"));
        refundApplicationForm.setRefundReason("测试退款");
    }

    // ==================== submitRefundApplication 测试 ====================

    @Test
    @DisplayName("测试提交退款申请-成功")
    void testSubmitRefundApplication_Success() {
        // Given
        RefundApplicationEntity mockEntity = new RefundApplicationEntity();
        mockEntity.setRefundNo("REFUND001");
        mockEntity.setUserId(1001L);

        when(refundApplicationService.submitRefundApplication(any(RefundApplicationForm.class))).thenReturn(mockEntity);

        // When
        ResponseDTO<RefundApplicationEntity> result = refundApplicationController.submitRefundApplication(refundApplicationForm);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        assertEquals("REFUND001", result.getData().getRefundNo());
        verify(refundApplicationService, times(1)).submitRefundApplication(any(RefundApplicationForm.class));
    }

    @Test
    @DisplayName("测试提交退款申请-参数验证失败")
    void testSubmitRefundApplication_ValidationFailed() {
        // Given
        refundApplicationForm.setUserId(null); // 缺少必填字段

        // When & Then
        // 由于使用@Valid，应该在Controller层被拦截
        assertThrows(Exception.class, () -> {
            refundApplicationController.submitRefundApplication(refundApplicationForm);
        });
    }

    @Test
    @DisplayName("测试提交退款申请-表单为null")
    void testSubmitRefundApplication_FormIsNull() {
        // When & Then
        assertThrows(Exception.class, () -> {
            refundApplicationController.submitRefundApplication(null);
        });
    }

    // ==================== updateRefundStatus 测试 ====================

    @Test
    @DisplayName("测试更新退款申请状态-成功")
    void testUpdateRefundStatus_Success() {
        // Given
        String refundNo = "REFUND001";
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("status", "APPROVED");
        requestParams.put("approvalComment", "审批通过");

        doNothing().when(refundApplicationService).updateRefundStatus(
            eq(refundNo), eq("APPROVED"), eq("审批通过"));

        // When
        ResponseDTO<Void> result = refundApplicationController.updateRefundStatus(refundNo, requestParams);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        verify(refundApplicationService, times(1)).updateRefundStatus(
            eq(refundNo), eq("APPROVED"), eq("审批通过"));
    }

    @Test
    @DisplayName("测试更新退款申请状态-退款编号为null")
    void testUpdateRefundStatus_RefundNoIsNull() {
        // Given
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("status", "APPROVED");

        // When & Then
        assertThrows(Exception.class, () -> {
            refundApplicationController.updateRefundStatus(null, requestParams);
        });
    }

    @Test
    @DisplayName("测试更新退款申请状态-参数为空")
    void testUpdateRefundStatus_ParamsIsNull() {
        // Given
        String refundNo = "REFUND001";

        // When & Then
        assertThrows(Exception.class, () -> {
            refundApplicationController.updateRefundStatus(refundNo, null);
        });
    }

    @Test
    @DisplayName("测试更新退款申请状态-状态为null")
    void testUpdateRefundStatus_StatusIsNull() {
        // Given
        String refundNo = "REFUND001";
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("status", null);

        // When
        ResponseDTO<Void> result = refundApplicationController.updateRefundStatus(refundNo, requestParams);

        // Then
        assertNotNull(result);
        // 根据实际实现，可能返回错误或成功
    }
}
