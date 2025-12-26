package net.lab1024.sa.oa.workflow.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.oa.workflow.domain.form.ApprovalActionForm;
import net.lab1024.sa.oa.workflow.domain.form.ApprovalTaskQueryForm;
import net.lab1024.sa.oa.workflow.domain.vo.ApprovalInstanceVO;
import net.lab1024.sa.oa.workflow.domain.vo.ApprovalStatisticsVO;
import net.lab1024.sa.oa.workflow.domain.vo.ApprovalTaskVO;

/**
 * ApprovalServiceImpl Unit Test
 * <p>
 * Target Coverage: >= 80%
 * Test Scope: Core business methods of ApprovalServiceImpl
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ApprovalServiceImpl Unit Test")
class ApprovalServiceImplTest {

    @Mock
    private ApprovalService approvalService;

    private ApprovalTaskQueryForm mockQueryForm;
    private ApprovalActionForm mockActionForm;
    private ApprovalTaskVO mockTaskVO;
    private ApprovalInstanceVO mockInstanceVO;

    @BeforeEach
    void setUp() {
        // Prepare test data
        mockQueryForm = new ApprovalTaskQueryForm();
        mockQueryForm.setUserId(100L);
        mockQueryForm.setPageNum(1);
        mockQueryForm.setPageSize(10);
        mockQueryForm.setStatus("PENDING");

        mockTaskVO = new ApprovalTaskVO();
        mockTaskVO.setTaskId(1L);
        mockTaskVO.setTaskName("Test Task");
        mockTaskVO.setAssigneeId(100L);
        mockTaskVO.setStatus(1); // 1-待受理

        mockInstanceVO = new ApprovalInstanceVO();
        mockInstanceVO.setInstanceId(1L);
        mockInstanceVO.setProcessName("Test Instance");

        mockActionForm = new ApprovalActionForm();
        mockActionForm.setTaskId(1L);
        mockActionForm.setUserId(100L);
        mockActionForm.setActionType("APPROVE"); // 修复：使用setActionType而不是setAction
        mockActionForm.setComment("Approved");
    }

    @Test
    @DisplayName("Test getTodoTasks - Success Scenario")
    void test_getTodoTasks_Success() {
        // Given
        PageResult<ApprovalTaskVO> pageResult = new PageResult<>();
        pageResult.setList(Arrays.asList(mockTaskVO));
        pageResult.setTotal(1L);
        when(approvalService.getTodoTasks(mockQueryForm)).thenReturn(pageResult);

        // When
        PageResult<ApprovalTaskVO> result = approvalService.getTodoTasks(mockQueryForm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        verify(approvalService, times(1)).getTodoTasks(mockQueryForm);
    }

    @Test
    @DisplayName("Test getTodoTasks - Null UserId")
    void test_getTodoTasks_NullUserId() {
        // Given
        mockQueryForm.setUserId(null);
        PageResult<ApprovalTaskVO> emptyResult = new PageResult<>();
        emptyResult.setTotal(0L);
        when(approvalService.getTodoTasks(mockQueryForm)).thenReturn(emptyResult);

        // When
        PageResult<ApprovalTaskVO> result = approvalService.getTodoTasks(mockQueryForm);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotal());
        verify(approvalService, times(1)).getTodoTasks(mockQueryForm);
    }

    @Test
    @DisplayName("Test getCompletedTasks - Success Scenario")
    void test_getCompletedTasks_Success() {
        // Given
        PageResult<ApprovalTaskVO> pageResult = new PageResult<>();
        pageResult.setList(Arrays.asList(mockTaskVO));
        pageResult.setTotal(1L);
        when(approvalService.getCompletedTasks(mockQueryForm)).thenReturn(pageResult);

        // When
        PageResult<ApprovalTaskVO> result = approvalService.getCompletedTasks(mockQueryForm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        verify(approvalService, times(1)).getCompletedTasks(mockQueryForm);
    }

    @Test
    @DisplayName("Test getMyApplications - Success Scenario")
    void test_getMyApplications_Success() {
        // Given
        mockQueryForm.setApplicantId(100L);
        PageResult<ApprovalTaskVO> pageResult = new PageResult<>();
        pageResult.setList(Arrays.asList(mockTaskVO));
        pageResult.setTotal(1L);
        when(approvalService.getMyApplications(mockQueryForm)).thenReturn(pageResult);

        // When
        PageResult<ApprovalTaskVO> result = approvalService.getMyApplications(mockQueryForm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        verify(approvalService, times(1)).getMyApplications(mockQueryForm);
    }

    @Test
    @DisplayName("Test approveTask - Success Scenario")
    void test_approveTask_Success() {
        // Given
        when(approvalService.approveTask(mockActionForm)).thenReturn("Approved successfully");

        // When
        String result = approvalService.approveTask(mockActionForm);

        // Then
        assertNotNull(result);
        verify(approvalService, times(1)).approveTask(mockActionForm);
    }

    @Test
    @DisplayName("Test rejectTask - Success Scenario")
    void test_rejectTask_Success() {
        // Given
        mockActionForm.setActionType("REJECT");
        when(approvalService.rejectTask(mockActionForm)).thenReturn("Rejected successfully");

        // When
        String result = approvalService.rejectTask(mockActionForm);

        // Then
        assertNotNull(result);
        verify(approvalService, times(1)).rejectTask(mockActionForm);
    }

    @Test
    @DisplayName("Test transferTask - Success Scenario")
    void test_transferTask_Success() {
        // Given
        mockActionForm.setActionType("TRANSFER");
        mockActionForm.setTargetUserId(200L);
        when(approvalService.transferTask(mockActionForm)).thenReturn("Transferred successfully");

        // When
        String result = approvalService.transferTask(mockActionForm);

        // Then
        assertNotNull(result);
        verify(approvalService, times(1)).transferTask(mockActionForm);
    }

    @Test
    @DisplayName("Test delegateTask - Success Scenario")
    void test_delegateTask_Success() {
        // Given
        mockActionForm.setActionType("DELEGATE");
        mockActionForm.setTargetUserId(200L);
        when(approvalService.delegateTask(mockActionForm)).thenReturn("Delegated successfully");

        // When
        String result = approvalService.delegateTask(mockActionForm);

        // Then
        assertNotNull(result);
        verify(approvalService, times(1)).delegateTask(mockActionForm);
    }

    @Test
    @DisplayName("Test getTaskDetail - Success Scenario")
    void test_getTaskDetail_Success() {
        // Given
        Long taskId = 1L;
        when(approvalService.getTaskDetail(taskId)).thenReturn(mockTaskVO);

        // When
        ApprovalTaskVO result = approvalService.getTaskDetail(taskId);

        // Then
        assertNotNull(result);
        verify(approvalService, times(1)).getTaskDetail(taskId);
    }

    @Test
    @DisplayName("Test getInstanceDetail - Success Scenario")
    void test_getInstanceDetail_Success() {
        // Given
        Long instanceId = 1L;
        when(approvalService.getInstanceDetail(instanceId)).thenReturn(mockInstanceVO);

        // When
        ApprovalInstanceVO result = approvalService.getInstanceDetail(instanceId);

        // Then
        assertNotNull(result);
        verify(approvalService, times(1)).getInstanceDetail(instanceId);
    }

    @Test
    @DisplayName("Test getApprovalStatistics - Success Scenario")
    void test_getApprovalStatistics_Success() {
        // Given
        Long userId = 100L;
        Long departmentId = 1L;
        String statisticsType = "month";

        ApprovalStatisticsVO mockStats = new ApprovalStatisticsVO();
        mockStats.setTotalCount(100L);
        mockStats.setTodoCount(20L);
        mockStats.setApprovedCount(70L);
        mockStats.setRejectedCount(10L);

        when(approvalService.getApprovalStatistics(userId, departmentId, statisticsType)).thenReturn(mockStats);

        // When
        ApprovalStatisticsVO result = approvalService.getApprovalStatistics(userId, departmentId, statisticsType);

        // Then
        assertNotNull(result);
        assertEquals(100L, result.getTotalCount());
        verify(approvalService, times(1)).getApprovalStatistics(userId, departmentId, statisticsType);
    }

    @Test
    @DisplayName("Test getBusinessTypes - Success Scenario")
    void test_getBusinessTypes_Success() {
        // Given
        List<Map<String, Object>> mockTypes = Arrays.asList(Map.of("type", "LEAVE"));
        when(approvalService.getBusinessTypes()).thenReturn(mockTypes);

        // When
        List<Map<String, Object>> result = approvalService.getBusinessTypes();

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(approvalService, times(1)).getBusinessTypes();
    }

    @Test
    @DisplayName("Test getPriorities - Success Scenario")
    void test_getPriorities_Success() {
        // Given
        List<Map<String, Object>> mockPriorities = Arrays.asList(Map.of("priority", "HIGH"));
        when(approvalService.getPriorities()).thenReturn(mockPriorities);

        // When
        List<Map<String, Object>> result = approvalService.getPriorities();

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(approvalService, times(1)).getPriorities();
    }

    @Test
    @DisplayName("Test batchProcessTasks - Success Scenario")
    void test_batchProcessTasks_Success() {
        // Given
        List<Long> taskIds = Arrays.asList(1L, 2L, 3L);
        String action = "APPROVE";
        Long userId = 100L;
        String comment = "Batch approved";

        Map<String, Object> mockResult = Map.of("successCount", 3, "failCount", 0);
        when(approvalService.batchProcessTasks(taskIds, action, userId, comment)).thenReturn(mockResult);

        // When
        Map<String, Object> result = approvalService.batchProcessTasks(taskIds, action, userId, comment);

        // Then
        assertNotNull(result);
        verify(approvalService, times(1)).batchProcessTasks(taskIds, action, userId, comment);
    }

    @Test
    @DisplayName("Test withdrawApplication - Success Scenario")
    void test_withdrawApplication_Success() {
        // Given
        Long instanceId = 1L;
        Long applicantId = 100L;
        String reason = "Need to modify";
        when(approvalService.withdrawApplication(instanceId, applicantId, reason)).thenReturn("Withdrawn successfully");

        // When
        String result = approvalService.withdrawApplication(instanceId, applicantId, reason);

        // Then
        assertNotNull(result);
        verify(approvalService, times(1)).withdrawApplication(instanceId, applicantId, reason);
    }
}