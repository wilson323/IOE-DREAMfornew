package net.lab1024.sa.attendance.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.attendance.attendance.manager.AttendanceManager;
import net.lab1024.sa.common.workflow.manager.WorkflowApprovalManager;
import net.lab1024.sa.attendance.dao.AttendanceOvertimeDao;
import net.lab1024.sa.attendance.domain.entity.AttendanceOvertimeEntity;
import net.lab1024.sa.attendance.domain.form.AttendanceOvertimeForm;
import net.lab1024.sa.attendance.service.impl.AttendanceOvertimeServiceImpl;

/**
 * AttendanceOvertimeServiceImpl Unit Test
 * <p>
 * Target Coverage: >= 80%
 * Test Scope: Core business methods of AttendanceOvertimeServiceImpl
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceOvertimeServiceImpl Unit Test")
class AttendanceOvertimeServiceImplTest {

    @Mock
    private AttendanceOvertimeDao attendanceOvertimeDao;

    @Mock
    private WorkflowApprovalManager workflowApprovalManager;

    @Mock
    private AttendanceManager attendanceManager;

    @InjectMocks
    private AttendanceOvertimeServiceImpl attendanceOvertimeServiceImpl;

    private AttendanceOvertimeForm mockForm;
    private AttendanceOvertimeEntity mockEntity;

    @BeforeEach
    void setUp() {
        // Prepare test data
        mockForm = new AttendanceOvertimeForm();
        mockForm.setEmployeeId(100L);
        mockForm.setOvertimeDate(LocalDate.now().plusDays(1));
        mockForm.setStartTime(LocalTime.of(18, 0));
        mockForm.setEndTime(LocalTime.of(22, 0));
        mockForm.setOvertimeHours(4.0);
        mockForm.setReason("Project deadline");
        mockForm.setRemark("Urgent project");

        mockEntity = new AttendanceOvertimeEntity();
        mockEntity.setId(1L);
        mockEntity.setOvertimeNo("OT001");
        mockEntity.setEmployeeId(100L);
        mockEntity.setEmployeeName("Test Employee");
        mockEntity.setOvertimeDate(LocalDate.now().plusDays(1));
        mockEntity.setStartTime(LocalTime.of(18, 0));
        mockEntity.setEndTime(LocalTime.of(22, 0));
        mockEntity.setOvertimeHours(4.0);
        mockEntity.setReason("Project deadline");
        mockEntity.setStatus("PENDING");
        mockEntity.setCreateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("Test submitOvertimeApplication - Success Scenario")
    void test_submitOvertimeApplication_Success() {
        // Given
        ResponseDTO<Long> workflowResponse = ResponseDTO.ok(1001L);

        when(attendanceManager.getUserName(100L)).thenReturn("Test Employee");
        doAnswer(invocation -> {
            AttendanceOvertimeEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            entity.setOvertimeNo("OT001");
            return 1;
        }).when(attendanceOvertimeDao).insert(any(AttendanceOvertimeEntity.class));
        when(workflowApprovalManager.startApprovalProcess(anyLong(), anyString(), anyString(), anyLong(), anyString(), any(), any()))
            .thenReturn(workflowResponse);
        when(attendanceOvertimeDao.updateById(any(AttendanceOvertimeEntity.class))).thenReturn(1);

        // When
        AttendanceOvertimeEntity result = attendanceOvertimeServiceImpl.submitOvertimeApplication(mockForm);

        // Then
        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        assertNotNull(result.getOvertimeNo());
        assertEquals(1001L, result.getWorkflowInstanceId());
        verify(attendanceManager, times(1)).getUserName(100L);
        verify(attendanceOvertimeDao, times(1)).insert(any(AttendanceOvertimeEntity.class));
        verify(workflowApprovalManager, times(1)).startApprovalProcess(anyLong(), anyString(), anyString(), anyLong(), anyString(), any(), any());
    }

    @Test
    @DisplayName("Test submitOvertimeApplication - Workflow Start Failed")
    void test_submitOvertimeApplication_WorkflowFailed() {
        // Given
        ResponseDTO<Long> workflowResponse = ResponseDTO.error("WORKFLOW_ERROR", "Workflow start failed");

        when(attendanceManager.getUserName(100L)).thenReturn("Test Employee");
        doAnswer(invocation -> {
            AttendanceOvertimeEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            entity.setOvertimeNo("OT001");
            return 1;
        }).when(attendanceOvertimeDao).insert(any(AttendanceOvertimeEntity.class));
        when(workflowApprovalManager.startApprovalProcess(anyLong(), anyString(), anyString(), anyLong(), anyString(), any(), any()))
            .thenReturn(workflowResponse);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            attendanceOvertimeServiceImpl.submitOvertimeApplication(mockForm);
        });
        assertTrue(exception.getMessage().contains("启动审批流程失败"));
        verify(attendanceOvertimeDao, times(1)).insert(any(AttendanceOvertimeEntity.class));
    }

    @Test
    @DisplayName("Test submitOvertimeApplicationFallback - Success Scenario")
    void test_submitOvertimeApplicationFallback_Success() {
        // Given
        Exception mockException = new RuntimeException("Workflow service unavailable");

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            attendanceOvertimeServiceImpl.submitOvertimeApplicationFallback(mockForm, mockException);
        });
        assertTrue(exception.getMessage().contains("启动审批流程失败"));
    }

    @Test
    @DisplayName("Test updateOvertimeStatus - Success Scenario (Approved)")
    void test_updateOvertimeStatus_Approved() {
        // Given
        String overtimeNo = "OT001";
        String status = "APPROVED";
        String approvalComment = "Approved";

        when(attendanceOvertimeDao.selectByOvertimeNo(overtimeNo)).thenReturn(mockEntity);
        when(attendanceOvertimeDao.updateById(any(AttendanceOvertimeEntity.class))).thenReturn(1);

        // When
        attendanceOvertimeServiceImpl.updateOvertimeStatus(overtimeNo, status, approvalComment);

        // Then
        verify(attendanceOvertimeDao, times(1)).selectByOvertimeNo(overtimeNo);
        verify(attendanceOvertimeDao, times(1)).updateById(any(AttendanceOvertimeEntity.class));
    }

    @Test
    @DisplayName("Test updateOvertimeStatus - Overtime Not Found")
    void test_updateOvertimeStatus_NotFound() {
        // Given
        String overtimeNo = "NON_EXIST";
        String status = "APPROVED";
        String approvalComment = "Approved";

        when(attendanceOvertimeDao.selectByOvertimeNo(overtimeNo)).thenReturn(null);

        // When
        attendanceOvertimeServiceImpl.updateOvertimeStatus(overtimeNo, status, approvalComment);

        // Then
        verify(attendanceOvertimeDao, times(1)).selectByOvertimeNo(overtimeNo);
        verify(attendanceOvertimeDao, never()).updateById(any(AttendanceOvertimeEntity.class));
    }

    @Test
    @DisplayName("Test updateOvertimeStatus - Rejected")
    void test_updateOvertimeStatus_Rejected() {
        // Given
        String overtimeNo = "OT001";
        String status = "REJECTED";
        String approvalComment = "Rejected";

        when(attendanceOvertimeDao.selectByOvertimeNo(overtimeNo)).thenReturn(mockEntity);
        when(attendanceOvertimeDao.updateById(any(AttendanceOvertimeEntity.class))).thenReturn(1);

        // When
        attendanceOvertimeServiceImpl.updateOvertimeStatus(overtimeNo, status, approvalComment);

        // Then
        verify(attendanceOvertimeDao, times(1)).selectByOvertimeNo(overtimeNo);
        verify(attendanceOvertimeDao, times(1)).updateById(any(AttendanceOvertimeEntity.class));
    }
}

