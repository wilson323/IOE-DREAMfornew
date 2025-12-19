package net.lab1024.sa.consume.service.impl.reimbursement;

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
import net.lab1024.sa.consume.dao.ReimbursementApplicationDao;
import net.lab1024.sa.consume.entity.ReimbursementApplicationEntity;
import net.lab1024.sa.consume.domain.form.ReimbursementApplicationForm;
import net.lab1024.sa.consume.manager.AccountManager;

/**
 * ReimbursementApplicationServiceImpl Unit Test
 * <p>
 * Target Coverage: >= 80%
 * Test Scope: Core business methods of ReimbursementApplicationServiceImpl
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReimbursementApplicationServiceImpl Unit Test")
class ReimbursementApplicationServiceImplTest {

    @Mock
    private ReimbursementApplicationDao reimbursementApplicationDao;

    @Mock
    private WorkflowApprovalManager workflowApprovalManager;

    @Mock
    private AccountManager accountManager;

    @InjectMocks
    private ReimbursementApplicationServiceImpl reimbursementApplicationServiceImpl;

    private ReimbursementApplicationForm mockForm;
    private ReimbursementApplicationEntity mockEntity;

    @BeforeEach
    void setUp() {
        // Prepare test data
        mockForm = new ReimbursementApplicationForm();
        mockForm.setUserId(100L);
        mockForm.setReimbursementAmount(new BigDecimal("200.00"));
        mockForm.setReimbursementType("TRAVEL");
        mockForm.setReason("Business travel reimbursement");

        mockEntity = new ReimbursementApplicationEntity();
        mockEntity.setId(1L);
        mockEntity.setReimbursementNo("REIMB001");
        mockEntity.setUserId(100L);
        mockEntity.setReimbursementAmount(new BigDecimal("200.00"));
        mockEntity.setReimbursementType("TRAVEL");
        mockEntity.setReason("Business travel reimbursement");
        mockEntity.setStatus("PENDING");
        mockEntity.setCreateTime(LocalDateTime.now());
        mockEntity.setDeletedFlag(0);  // 修复：deletedFlag是Integer类型，0表示未删除
    }

    @Test
    @DisplayName("Test submitReimbursementApplication - Success Scenario")
    void test_submitReimbursementApplication_Success() {
        // Given
        ResponseDTO<Long> workflowResponse = ResponseDTO.ok(1001L);

        doAnswer(invocation -> {
            ReimbursementApplicationEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            entity.setReimbursementNo("REIMB001");
            return 1;
        }).when(reimbursementApplicationDao).insert(any(ReimbursementApplicationEntity.class));
        when(workflowApprovalManager.startApprovalProcess(anyLong(), anyString(), anyString(), anyLong(), anyString(), any(), any()))
            .thenReturn(workflowResponse);
        when(reimbursementApplicationDao.updateById(any(ReimbursementApplicationEntity.class))).thenReturn(1);

        // When
        ReimbursementApplicationEntity result = reimbursementApplicationServiceImpl.submitReimbursementApplication(mockForm);

        // Then
        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        assertNotNull(result.getReimbursementNo());
        verify(reimbursementApplicationDao, times(1)).insert(any(ReimbursementApplicationEntity.class));
        verify(workflowApprovalManager, times(1)).startApprovalProcess(anyLong(), anyString(), anyString(), anyLong(), anyString(), any(), any());
    }

    @Test
    @DisplayName("Test submitReimbursementApplication - Workflow Start Failed")
    void test_submitReimbursementApplication_WorkflowFailed() {
        // Given
        ResponseDTO<Long> workflowResponse = ResponseDTO.error("WORKFLOW_ERROR", "Workflow start failed");

        doAnswer(invocation -> {
            ReimbursementApplicationEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            entity.setReimbursementNo("REIMB001");
            return 1;
        }).when(reimbursementApplicationDao).insert(any(ReimbursementApplicationEntity.class));
        when(workflowApprovalManager.startApprovalProcess(anyLong(), anyString(), anyString(), anyLong(), anyString(), any(), any()))
            .thenReturn(workflowResponse);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reimbursementApplicationServiceImpl.submitReimbursementApplication(mockForm);
        });
        assertTrue(exception.getMessage().contains("启动审批流程失败"));
        verify(reimbursementApplicationDao, times(1)).insert(any(ReimbursementApplicationEntity.class));
    }

    @Test
    @DisplayName("Test submitReimbursementApplicationFallback - Success Scenario")
    void test_submitReimbursementApplicationFallback_Success() {
        // Given
        Exception mockException = new RuntimeException("Workflow service unavailable");

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reimbursementApplicationServiceImpl.submitReimbursementApplicationFallback(mockForm, mockException);
        });
        assertTrue(exception.getMessage().contains("启动审批流程失败"));
    }

    @Test
    @DisplayName("Test updateReimbursementStatus - Success Scenario (Approved)")
    void test_updateReimbursementStatus_Approved() {
        // Given
        String reimbursementNo = "REIMB001";
        String status = "APPROVED";
        String approvalComment = "Approved";

        when(reimbursementApplicationDao.selectByReimbursementNo(reimbursementNo)).thenReturn(mockEntity);
        when(reimbursementApplicationDao.updateById(any(ReimbursementApplicationEntity.class))).thenReturn(1);

        net.lab1024.sa.consume.entity.AccountEntity account = new net.lab1024.sa.consume.entity.AccountEntity();
        account.setAccountId(10L);
        when(accountManager.getAccountByUserId(mockEntity.getUserId())).thenReturn(account);
        when(accountManager.addBalance(eq(account.getId()), eq(mockEntity.getReimbursementAmount()))).thenReturn(true);

        // When
        reimbursementApplicationServiceImpl.updateReimbursementStatus(reimbursementNo, status, approvalComment);

        // Then
        verify(reimbursementApplicationDao, times(1)).selectByReimbursementNo(reimbursementNo);
        verify(reimbursementApplicationDao, times(1)).updateById(any(ReimbursementApplicationEntity.class));
        verify(accountManager).getAccountByUserId(mockEntity.getUserId());
        verify(accountManager).addBalance(eq(account.getId()), eq(mockEntity.getReimbursementAmount()));
    }

    @Test
    @DisplayName("Test updateReimbursementStatus - Reimbursement Not Found")
    void test_updateReimbursementStatus_NotFound() {
        // Given
        String reimbursementNo = "NON_EXIST";
        String status = "APPROVED";
        String approvalComment = "Approved";

        when(reimbursementApplicationDao.selectByReimbursementNo(reimbursementNo)).thenReturn(null);

        // When
        reimbursementApplicationServiceImpl.updateReimbursementStatus(reimbursementNo, status, approvalComment);

        // Then
        verify(reimbursementApplicationDao, times(1)).selectByReimbursementNo(reimbursementNo);
        verify(reimbursementApplicationDao, never()).updateById(any(ReimbursementApplicationEntity.class));
    }

    @Test
    @DisplayName("Test updateReimbursementStatus - Rejected")
    void test_updateReimbursementStatus_Rejected() {
        // Given
        String reimbursementNo = "REIMB001";
        String status = "REJECTED";
        String approvalComment = "Rejected";

        when(reimbursementApplicationDao.selectByReimbursementNo(reimbursementNo)).thenReturn(mockEntity);
        when(reimbursementApplicationDao.updateById(any(ReimbursementApplicationEntity.class))).thenReturn(1);

        // When
        reimbursementApplicationServiceImpl.updateReimbursementStatus(reimbursementNo, status, approvalComment);

        // Then
        verify(reimbursementApplicationDao, times(1)).selectByReimbursementNo(reimbursementNo);
        verify(reimbursementApplicationDao, times(1)).updateById(any(ReimbursementApplicationEntity.class));
    }
}


