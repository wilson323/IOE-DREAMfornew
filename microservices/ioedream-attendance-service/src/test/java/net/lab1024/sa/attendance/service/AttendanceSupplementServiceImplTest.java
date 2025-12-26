package net.lab1024.sa.attendance.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.attendance.dao.AttendanceSupplementDao;
import net.lab1024.sa.common.entity.attendance.AttendanceSupplementEntity;
import net.lab1024.sa.attendance.domain.form.AttendanceSupplementForm;
import net.lab1024.sa.attendance.manager.AttendanceManager;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.workflow.manager.WorkflowApprovalManager;

/**
 * AttendanceSupplementServiceImpl Unit Test
 * <p>
 * Target Coverage: >= 80%
 * Test Scope: Core business methods of AttendanceSupplementServiceImpl
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceSupplementServiceImpl Unit Test")
class AttendanceSupplementServiceImplTest {

    @Mock
    private AttendanceSupplementDao attendanceSupplementDao;

    @Mock
    private WorkflowApprovalManager workflowApprovalManager;

    @Mock
    private AttendanceManager attendanceManager;

    @Spy
    @InjectMocks
    private net.lab1024.sa.attendance.service.impl.AttendanceSupplementServiceImpl attendanceSupplementServiceImpl;

    private AttendanceSupplementForm mockForm;
    private AttendanceSupplementEntity mockEntity;

    @BeforeEach
    void setUp() {
        // Prepare test data
        mockForm = new AttendanceSupplementForm();
        mockForm.setEmployeeId(100L);
        mockForm.setSupplementDate(LocalDate.now().minusDays(1));
        mockForm.setPunchTime(LocalTime.of(9, 0));
        mockForm.setPunchType("CHECK_IN");
        mockForm.setReason("Forgot to punch");
        mockForm.setRemark("Supplement application");

        mockEntity = new AttendanceSupplementEntity();
        mockEntity.setId(1L);
        mockEntity.setSupplementNo("SUP001");
        mockEntity.setEmployeeId(100L);
        mockEntity.setEmployeeName("Test Employee");
        mockEntity.setSupplementDate(LocalDate.now().minusDays(1));
        mockEntity.setPunchTime(LocalTime.of(9, 0));
        mockEntity.setPunchType("CHECK_IN");
        mockEntity.setReason("Forgot to punch");
        mockEntity.setStatus("PENDING");
        mockEntity.setCreateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("Test submitSupplementApplication - Success Scenario")
    void test_submitSupplementApplication_Success() {
        // Given
        Long workflowInstanceId = 1001L;

        when(attendanceManager.getUserName(100L)).thenReturn("Test Employee");
        doAnswer(invocation -> {
            AttendanceSupplementEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            entity.setSupplementNo("SUP001");
            return 1;
        }).when(attendanceSupplementDao).insert(any(AttendanceSupplementEntity.class));
        when(workflowApprovalManager.startApprovalProcess(anyLong(), anyString(), anyString(), anyLong(), anyString(),
                any(), any()))
                .thenReturn(workflowInstanceId);
        when(attendanceSupplementDao.updateById(any(AttendanceSupplementEntity.class))).thenReturn(1);

        // When
        AttendanceSupplementEntity result = attendanceSupplementServiceImpl.submitSupplementApplication(mockForm);

        // Then
        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        assertNotNull(result.getSupplementNo());
        assertEquals(1001L, result.getWorkflowInstanceId());
        verify(attendanceManager, times(1)).getUserName(100L);
        verify(attendanceSupplementDao, times(1)).insert(any(AttendanceSupplementEntity.class));
        verify(workflowApprovalManager, times(1)).startApprovalProcess(anyLong(), anyString(), anyString(), anyLong(),
                anyString(), any(), any());
    }

    @Test
    @DisplayName("Test submitSupplementApplication - Workflow Start Failed")
    void test_submitSupplementApplication_WorkflowFailed() {
        // Given
        when(attendanceManager.getUserName(100L)).thenReturn("Test Employee");
        doAnswer(invocation -> {
            AttendanceSupplementEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            entity.setSupplementNo("SUP001");
            return 1;
        }).when(attendanceSupplementDao).insert(any(AttendanceSupplementEntity.class));
        when(workflowApprovalManager.startApprovalProcess(anyLong(), anyString(), anyString(), anyLong(), anyString(),
                any(), any()))
                .thenThrow(new RuntimeException("Workflow start failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            attendanceSupplementServiceImpl.submitSupplementApplication(mockForm);
        });
        // 注意：submitSupplementApplication没有try-catch，异常直接传播
        assertTrue(exception.getMessage().contains("Workflow start failed"));
        verify(attendanceSupplementDao, times(1)).insert(any(AttendanceSupplementEntity.class));
    }

    @Test
    @DisplayName("Test submitSupplementApplicationFallback - Success Scenario")
    void test_submitSupplementApplicationFallback_Success() {
        // Given
        Exception mockException = new RuntimeException("Workflow service unavailable");

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            attendanceSupplementServiceImpl.submitSupplementApplicationFallback(mockForm, mockException);
        });
        assertTrue(exception.getMessage().contains("启动审批流程失败"));
    }

    @Test
    @DisplayName("Test updateSupplementStatus - Success Scenario (Approved)")
    void test_updateSupplementStatus_Approved() {
        // Given
        String supplementNo = "SUP001";
        String status = "APPROVED";
        String approvalComment = "Approved";

        when(attendanceSupplementDao.selectBySupplementNo(supplementNo)).thenReturn(mockEntity);
        when(attendanceSupplementDao.updateById(any(AttendanceSupplementEntity.class))).thenReturn(1);

        // When
        attendanceSupplementServiceImpl.updateSupplementStatus(supplementNo, status, approvalComment);

        // Then
        verify(attendanceSupplementDao, times(1)).selectBySupplementNo(supplementNo);
        verify(attendanceSupplementDao, times(1)).updateById(any(AttendanceSupplementEntity.class));
    }

    @Test
    @DisplayName("Test updateSupplementStatus - Supplement Not Found")
    void test_updateSupplementStatus_NotFound() {
        // Given
        String supplementNo = "NON_EXIST";
        String status = "APPROVED";
        String approvalComment = "Approved";

        when(attendanceSupplementDao.selectBySupplementNo(supplementNo)).thenReturn(null);

        // When
        attendanceSupplementServiceImpl.updateSupplementStatus(supplementNo, status, approvalComment);

        // Then
        verify(attendanceSupplementDao, times(1)).selectBySupplementNo(supplementNo);
        verify(attendanceSupplementDao, never()).updateById(any(AttendanceSupplementEntity.class));
    }

    @Test
    @DisplayName("Test updateSupplementStatus - Rejected")
    void test_updateSupplementStatus_Rejected() {
        // Given
        String supplementNo = "SUP001";
        String status = "REJECTED";
        String approvalComment = "Rejected";

        when(attendanceSupplementDao.selectBySupplementNo(supplementNo)).thenReturn(mockEntity);
        when(attendanceSupplementDao.updateById(any(AttendanceSupplementEntity.class))).thenReturn(1);

        // When
        attendanceSupplementServiceImpl.updateSupplementStatus(supplementNo, status, approvalComment);

        // Then
        verify(attendanceSupplementDao, times(1)).selectBySupplementNo(supplementNo);
        verify(attendanceSupplementDao, times(1)).updateById(any(AttendanceSupplementEntity.class));
    }
}
