package net.lab1024.sa.attendance.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import net.lab1024.sa.attendance.manager.AttendanceManager;
import net.lab1024.sa.common.workflow.manager.WorkflowApprovalManager;
import net.lab1024.sa.attendance.dao.AttendanceTravelDao;
import net.lab1024.sa.attendance.domain.entity.AttendanceTravelEntity;
import net.lab1024.sa.attendance.domain.form.AttendanceTravelForm;
import net.lab1024.sa.attendance.service.impl.AttendanceTravelServiceImpl;

/**
 * AttendanceTravelServiceImpl Unit Test
 * <p>
 * Target Coverage: >= 80%
 * Test Scope: Core business methods of AttendanceTravelServiceImpl
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceTravelServiceImpl Unit Test")
class AttendanceTravelServiceImplTest {

    @Mock
    private AttendanceTravelDao attendanceTravelDao;

    @Mock
    private WorkflowApprovalManager workflowApprovalManager;

    @Mock
    private AttendanceManager attendanceManager;

    @InjectMocks
    private AttendanceTravelServiceImpl attendanceTravelServiceImpl;

    private AttendanceTravelForm mockForm;
    private AttendanceTravelEntity mockEntity;

    @BeforeEach
    void setUp() {
        // Prepare test data
        mockForm = new AttendanceTravelForm();
        mockForm.setEmployeeId(100L);
        mockForm.setDestination("Beijing");
        mockForm.setStartDate(LocalDate.now().plusDays(1));
        mockForm.setEndDate(LocalDate.now().plusDays(3));
        mockForm.setTravelDays(3);
        mockForm.setEstimatedCost(new BigDecimal("5000.00"));
        mockForm.setReason("Business meeting");
        mockForm.setRemark("Important client meeting");

        mockEntity = new AttendanceTravelEntity();
        mockEntity.setId(1L);
        mockEntity.setTravelNo("TRV001");
        mockEntity.setEmployeeId(100L);
        mockEntity.setEmployeeName("Test Employee");
        mockEntity.setDestination("Beijing");
        mockEntity.setStartDate(LocalDate.now().plusDays(1));
        mockEntity.setEndDate(LocalDate.now().plusDays(3));
        mockEntity.setTravelDays(3);
        mockEntity.setEstimatedCost(new BigDecimal("5000.00"));
        mockEntity.setReason("Business meeting");
        mockEntity.setStatus("PENDING");
        mockEntity.setCreateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("Test submitTravelApplication - Success Scenario")
    void test_submitTravelApplication_Success() {
        // Given
        ResponseDTO<Long> workflowResponse = ResponseDTO.ok(1001L);

        when(attendanceManager.getUserName(100L)).thenReturn("Test Employee");
        doAnswer(invocation -> {
            AttendanceTravelEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            entity.setTravelNo("TRV001");
            return 1;
        }).when(attendanceTravelDao).insert(any(AttendanceTravelEntity.class));
        when(workflowApprovalManager.startApprovalProcess(anyLong(), anyString(), anyString(), anyLong(), anyString(), any(), any()))
            .thenReturn(workflowResponse);
        when(attendanceTravelDao.updateById(any(AttendanceTravelEntity.class))).thenReturn(1);

        // When
        AttendanceTravelEntity result = attendanceTravelServiceImpl.submitTravelApplication(mockForm);

        // Then
        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        assertNotNull(result.getTravelNo());
        assertEquals(1001L, result.getWorkflowInstanceId());
        verify(attendanceManager, times(1)).getUserName(100L);
        verify(attendanceTravelDao, times(1)).insert(any(AttendanceTravelEntity.class));
        verify(workflowApprovalManager, times(1)).startApprovalProcess(anyLong(), anyString(), anyString(), anyLong(), anyString(), any(), any());
    }

    @Test
    @DisplayName("Test submitTravelApplication - Workflow Start Failed")
    void test_submitTravelApplication_WorkflowFailed() {
        // Given
        ResponseDTO<Long> workflowResponse = ResponseDTO.error("WORKFLOW_ERROR", "Workflow start failed");

        when(attendanceManager.getUserName(100L)).thenReturn("Test Employee");
        doAnswer(invocation -> {
            AttendanceTravelEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            entity.setTravelNo("TRV001");
            return 1;
        }).when(attendanceTravelDao).insert(any(AttendanceTravelEntity.class));
        when(workflowApprovalManager.startApprovalProcess(anyLong(), anyString(), anyString(), anyLong(), anyString(), any(), any()))
            .thenReturn(workflowResponse);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            attendanceTravelServiceImpl.submitTravelApplication(mockForm);
        });
        assertTrue(exception.getMessage().contains("启动审批流程失败"));
        verify(attendanceTravelDao, times(1)).insert(any(AttendanceTravelEntity.class));
    }

    @Test
    @DisplayName("Test submitTravelApplicationFallback - Success Scenario")
    void test_submitTravelApplicationFallback_Success() {
        // Given
        Exception mockException = new RuntimeException("Workflow service unavailable");

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            attendanceTravelServiceImpl.submitTravelApplicationFallback(mockForm, mockException);
        });
        assertTrue(exception.getMessage().contains("启动审批流程失败"));
    }

    @Test
    @DisplayName("Test updateTravelStatus - Success Scenario (Approved)")
    void test_updateTravelStatus_Approved() {
        // Given
        String travelNo = "TRV001";
        String status = "APPROVED";
        String approvalComment = "Approved";

        when(attendanceTravelDao.selectByTravelNo(travelNo)).thenReturn(mockEntity);
        when(attendanceTravelDao.updateById(any(AttendanceTravelEntity.class))).thenReturn(1);

        // When
        attendanceTravelServiceImpl.updateTravelStatus(travelNo, status, approvalComment);

        // Then
        verify(attendanceTravelDao, times(1)).selectByTravelNo(travelNo);
        verify(attendanceTravelDao, times(1)).updateById(any(AttendanceTravelEntity.class));
    }

    @Test
    @DisplayName("Test updateTravelStatus - Travel Not Found")
    void test_updateTravelStatus_NotFound() {
        // Given
        String travelNo = "NON_EXIST";
        String status = "APPROVED";
        String approvalComment = "Approved";

        when(attendanceTravelDao.selectByTravelNo(travelNo)).thenReturn(null);

        // When
        attendanceTravelServiceImpl.updateTravelStatus(travelNo, status, approvalComment);

        // Then
        verify(attendanceTravelDao, times(1)).selectByTravelNo(travelNo);
        verify(attendanceTravelDao, never()).updateById(any(AttendanceTravelEntity.class));
    }

    @Test
    @DisplayName("Test updateTravelStatus - Rejected")
    void test_updateTravelStatus_Rejected() {
        // Given
        String travelNo = "TRV001";
        String status = "REJECTED";
        String approvalComment = "Rejected";

        when(attendanceTravelDao.selectByTravelNo(travelNo)).thenReturn(mockEntity);
        when(attendanceTravelDao.updateById(any(AttendanceTravelEntity.class))).thenReturn(1);

        // When
        attendanceTravelServiceImpl.updateTravelStatus(travelNo, status, approvalComment);

        // Then
        verify(attendanceTravelDao, times(1)).selectByTravelNo(travelNo);
        verify(attendanceTravelDao, times(1)).updateById(any(AttendanceTravelEntity.class));
    }
}

