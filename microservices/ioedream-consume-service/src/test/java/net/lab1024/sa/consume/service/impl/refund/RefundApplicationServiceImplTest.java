package net.lab1024.sa.consume.service.impl.refund;

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

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.workflow.manager.WorkflowApprovalManager;
import net.lab1024.sa.consume.dao.PaymentRecordDao;
import net.lab1024.sa.consume.dao.RefundApplicationDao;
import net.lab1024.sa.common.consume.entity.PaymentRecordEntity;
import net.lab1024.sa.common.consume.entity.RefundApplicationEntity;
import net.lab1024.sa.consume.domain.form.RefundApplicationForm;
import net.lab1024.sa.consume.manager.AccountManager;

/**
 * RefundApplicationServiceImpl Unit Test
 * <p>
 * Target Coverage: >= 80%
 * Test Scope: Core business methods of RefundApplicationServiceImpl
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RefundApplicationServiceImpl Unit Test")
class RefundApplicationServiceImplTest {

    @Mock
    private RefundApplicationDao refundApplicationDao;

    @Mock
    private WorkflowApprovalManager workflowApprovalManager;

    @Mock
    private PaymentRecordDao paymentRecordDao;

    @Mock
    private AccountManager accountManager;

    @InjectMocks
    private RefundApplicationServiceImpl refundApplicationServiceImpl;

    private RefundApplicationForm mockForm;
    private RefundApplicationEntity mockEntity;
    private PaymentRecordEntity mockPaymentRecord;

    @BeforeEach
    void setUp() {
        // Prepare test data
        mockForm = new RefundApplicationForm();
        mockForm.setUserId(100L);
        mockForm.setPaymentRecordId(1L);
        mockForm.setRefundAmount(new BigDecimal("50.00"));
        mockForm.setRefundReason("Test refund");

        mockEntity = new RefundApplicationEntity();
        mockEntity.setId(1L);
        mockEntity.setRefundNo("RF001");
        mockEntity.setPaymentRecordId(1L);
        mockEntity.setUserId(100L);
        mockEntity.setRefundAmount(new BigDecimal("50.00"));
        mockEntity.setRefundReason("Test refund");
        mockEntity.setStatus("PENDING");
        mockEntity.setCreateTime(LocalDateTime.now());
        mockEntity.setDeletedFlag(0);  // 修复：deletedFlag是Integer类型，0表示未删除

        mockPaymentRecord = new PaymentRecordEntity();
        mockPaymentRecord.setPaymentId("PAY001");
        mockPaymentRecord.setOrderNo("ORDER001");
        mockPaymentRecord.setTransactionNo("TXN001");
        mockPaymentRecord.setPaymentAmount(new BigDecimal("100.00"));
        mockPaymentRecord.setActualAmount(new BigDecimal("100.00"));
        mockPaymentRecord.setPaymentStatus(3); // 3=支付成功
        mockPaymentRecord.setPaymentMethod(3); // 3=支付宝
        mockPaymentRecord.setPaymentChannel(3); // 3=移动端
        mockPaymentRecord.setBusinessType(1); // 1=消费
        mockPaymentRecord.setDeviceId("DEV001");
        mockPaymentRecord.setUserId(1001L);
        mockPaymentRecord.setAccountId(2001L);
    }

    @Test
    @DisplayName("Test submitRefundApplication - Success Scenario")
    void test_submitRefundApplication_Success() {
        // Given
        ResponseDTO<Long> workflowResponse = ResponseDTO.ok(1001L);

        doAnswer(invocation -> {
            RefundApplicationEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            entity.setRefundNo("RF001");
            return 1;
        }).when(refundApplicationDao).insert(any(RefundApplicationEntity.class));
        when(workflowApprovalManager.startApprovalProcess(anyLong(), anyString(), anyString(), anyLong(), anyString(), any(), any()))
            .thenReturn(workflowResponse);
        when(refundApplicationDao.updateById(any(RefundApplicationEntity.class))).thenReturn(1);

        // When
        RefundApplicationEntity result = refundApplicationServiceImpl.submitRefundApplication(mockForm);

        // Then
        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        assertNotNull(result.getRefundNo());
        verify(refundApplicationDao, times(1)).insert(any(RefundApplicationEntity.class));
        verify(workflowApprovalManager, times(1)).startApprovalProcess(anyLong(), anyString(), anyString(), anyLong(), anyString(), any(), any());
    }

    @Test
    @DisplayName("Test submitRefundApplication - Workflow Start Failed")
    void test_submitRefundApplication_WorkflowFailed() {
        // Given
        ResponseDTO<Long> workflowResponse = ResponseDTO.error("WORKFLOW_ERROR", "Workflow start failed");

        doAnswer(invocation -> {
            RefundApplicationEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            entity.setRefundNo("RF001");
            return 1;
        }).when(refundApplicationDao).insert(any(RefundApplicationEntity.class));
        when(workflowApprovalManager.startApprovalProcess(anyLong(), anyString(), anyString(), anyLong(), anyString(), any(), any()))
            .thenReturn(workflowResponse);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            refundApplicationServiceImpl.submitRefundApplication(mockForm);
        });
        assertTrue(exception.getMessage().contains("启动审批流程失败"));
        verify(refundApplicationDao, times(1)).insert(any(RefundApplicationEntity.class));
    }

    @Test
    @DisplayName("Test updateRefundStatus - Success Scenario (Approved)")
    void test_updateRefundStatus_Approved() {
        // Given
        String refundNo = "RF001";
        String status = "APPROVED";
        String approvalComment = "Approved";

        when(refundApplicationDao.selectByRefundNo(refundNo)).thenReturn(mockEntity);
        when(paymentRecordDao.selectById(anyString())).thenReturn(mockPaymentRecord);
        when(refundApplicationDao.updateById(any(RefundApplicationEntity.class))).thenReturn(1);

        // When
        refundApplicationServiceImpl.updateRefundStatus(refundNo, status, approvalComment);

        // Then
        verify(refundApplicationDao, times(1)).selectByRefundNo(refundNo);
        verify(refundApplicationDao, times(1)).updateById(any(RefundApplicationEntity.class));
    }

    @Test
    @DisplayName("Test updateRefundStatus - Refund Not Found")
    void test_updateRefundStatus_NotFound() {
        // Given
        String refundNo = "NON_EXIST";
        String status = "APPROVED";
        String approvalComment = "Approved";

        when(refundApplicationDao.selectByRefundNo(refundNo)).thenReturn(null);

        // When
        refundApplicationServiceImpl.updateRefundStatus(refundNo, status, approvalComment);

        // Then
        verify(refundApplicationDao, times(1)).selectByRefundNo(refundNo);
        verify(refundApplicationDao, never()).updateById(any(RefundApplicationEntity.class));
    }

    @Test
    @DisplayName("Test updateRefundStatus - Rejected")
    void test_updateRefundStatus_Rejected() {
        // Given
        String refundNo = "RF001";
        String status = "REJECTED";
        String approvalComment = "Rejected";

        when(refundApplicationDao.selectByRefundNo(refundNo)).thenReturn(mockEntity);
        when(refundApplicationDao.updateById(any(RefundApplicationEntity.class))).thenReturn(1);

        // When
        refundApplicationServiceImpl.updateRefundStatus(refundNo, status, approvalComment);

        // Then
        verify(refundApplicationDao, times(1)).selectByRefundNo(refundNo);
        verify(refundApplicationDao, times(1)).updateById(any(RefundApplicationEntity.class));
        verify(paymentRecordDao, never()).selectById(anyLong());
    }
}


