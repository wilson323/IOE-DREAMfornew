package net.lab1024.sa.oa.workflow.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.oa.workflow.dao.ApprovalInstanceDao;
import net.lab1024.sa.oa.workflow.dao.ApprovalStatisticsDao;
import net.lab1024.sa.oa.workflow.dao.WorkflowTaskDao;
import net.lab1024.sa.oa.workflow.domain.form.ApprovalActionForm;
import net.lab1024.sa.oa.workflow.domain.form.ApprovalTaskQueryForm;
import net.lab1024.sa.oa.workflow.domain.vo.ApprovalInstanceVO;
import net.lab1024.sa.oa.workflow.domain.vo.ApprovalStatisticsVO;
import net.lab1024.sa.oa.workflow.domain.vo.ApprovalTaskVO;
import net.lab1024.sa.oa.domain.entity.WorkflowInstanceEntity;
import net.lab1024.sa.oa.domain.entity.WorkflowTaskEntity;
import net.lab1024.sa.oa.workflow.service.impl.ApprovalServiceImpl;

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
    private WorkflowTaskDao workflowTaskDao;

    @Mock
    private ApprovalInstanceDao approvalInstanceDao;

    @Mock
    private ApprovalStatisticsDao approvalStatisticsDao;

    @Mock
    private GatewayServiceClient gatewayServiceClient;

    @InjectMocks
    private ApprovalServiceImpl approvalServiceImpl;

    private ApprovalTaskQueryForm mockQueryForm;
    private WorkflowTaskEntity mockTaskEntity;
    private ApprovalActionForm mockActionForm;

    @BeforeEach
    void setUp() {
        // Prepare test data
        mockQueryForm = new ApprovalTaskQueryForm();
        mockQueryForm.setUserId(100L);
        mockQueryForm.setPageNum(1);
        mockQueryForm.setPageSize(10);
        mockQueryForm.setStatus("PENDING");

        mockTaskEntity = new WorkflowTaskEntity();
        mockTaskEntity.setId(1L);
        mockTaskEntity.setTaskName("Test Task");
        mockTaskEntity.setAssigneeId(100L);
        mockTaskEntity.setStatus(1);  // 修复：status是Integer类型，1表示待受理
        mockTaskEntity.setCreateTime(LocalDateTime.now());

        mockActionForm = new ApprovalActionForm();
        mockActionForm.setTaskId(1L);
        mockActionForm.setUserId(100L);
        mockActionForm.setActionType("APPROVE");  // 修复：使用setActionType而不是setAction
        mockActionForm.setComment("Approved");
    }

    @Test
    @DisplayName("Test getTodoTasks - Success Scenario")
    void test_getTodoTasks_Success() {
        // Given
        List<WorkflowTaskEntity> taskList = Arrays.asList(mockTaskEntity);
        when(workflowTaskDao.selectTodoTasks(any(ApprovalTaskQueryForm.class))).thenReturn(taskList);
        when(workflowTaskDao.countTodoTasks(any(ApprovalTaskQueryForm.class))).thenReturn(1L);

        // When
        PageResult<ApprovalTaskVO> result = approvalServiceImpl.getTodoTasks(mockQueryForm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        verify(workflowTaskDao, times(1)).selectTodoTasks(any(ApprovalTaskQueryForm.class));
    }

    @Test
    @DisplayName("Test getTodoTasks - Null UserId")
    void test_getTodoTasks_NullUserId() {
        // Given
        mockQueryForm.setUserId(null);

        // When
        PageResult<ApprovalTaskVO> result = approvalServiceImpl.getTodoTasks(mockQueryForm);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotal());
        verify(workflowTaskDao, never()).selectTodoTasks(any(ApprovalTaskQueryForm.class));
    }

    @Test
    @DisplayName("Test getCompletedTasks - Success Scenario")
    void test_getCompletedTasks_Success() {
        // Given
        List<WorkflowTaskEntity> taskList = Arrays.asList(mockTaskEntity);
        when(workflowTaskDao.selectCompletedTasks(any(ApprovalTaskQueryForm.class))).thenReturn(taskList);
        when(workflowTaskDao.countCompletedTasks(any(ApprovalTaskQueryForm.class))).thenReturn(1L);

        // When
        PageResult<ApprovalTaskVO> result = approvalServiceImpl.getCompletedTasks(mockQueryForm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        verify(workflowTaskDao, times(1)).selectCompletedTasks(any(ApprovalTaskQueryForm.class));
    }

    @Test
    @DisplayName("Test getMyApplications - Success Scenario")
    void test_getMyApplications_Success() {
        // Given
        mockQueryForm.setApplicantId(100L);
        List<WorkflowTaskEntity> taskList = Arrays.asList(mockTaskEntity);
        when(workflowTaskDao.selectMyApplications(any(ApprovalTaskQueryForm.class))).thenReturn(taskList);
        when(workflowTaskDao.countMyApplications(any(ApprovalTaskQueryForm.class))).thenReturn(1L);

        // When
        PageResult<ApprovalTaskVO> result = approvalServiceImpl.getMyApplications(mockQueryForm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        verify(workflowTaskDao, times(1)).selectMyApplications(any(ApprovalTaskQueryForm.class));
    }

    @Test
    @DisplayName("Test approveTask - Success Scenario")
    void test_approveTask_Success() {
        // Given
        when(workflowTaskDao.selectById(1L)).thenReturn(mockTaskEntity);
        when(workflowTaskDao.updateById(any(WorkflowTaskEntity.class))).thenReturn(1);

        // When
        String result = approvalServiceImpl.approveTask(mockActionForm);

        // Then
        assertNotNull(result);
        verify(workflowTaskDao, times(1)).selectById(1L);
        verify(workflowTaskDao, times(1)).updateById(any(WorkflowTaskEntity.class));
    }

    @Test
    @DisplayName("Test rejectTask - Success Scenario")
    void test_rejectTask_Success() {
        // Given
        mockActionForm.setActionType("REJECT");  // 修复：使用setActionType
        when(workflowTaskDao.selectById(1L)).thenReturn(mockTaskEntity);
        when(workflowTaskDao.updateById(any(WorkflowTaskEntity.class))).thenReturn(1);

        // When
        String result = approvalServiceImpl.rejectTask(mockActionForm);

        // Then
        assertNotNull(result);
        verify(workflowTaskDao, times(1)).selectById(1L);
        verify(workflowTaskDao, times(1)).updateById(any(WorkflowTaskEntity.class));
    }

    @Test
    @DisplayName("Test transferTask - Success Scenario")
    void test_transferTask_Success() {
        // Given
        mockActionForm.setActionType("TRANSFER");  // 修复：使用setActionType
        mockActionForm.setTargetUserId(200L);
        when(workflowTaskDao.selectById(1L)).thenReturn(mockTaskEntity);
        when(workflowTaskDao.updateById(any(WorkflowTaskEntity.class))).thenReturn(1);

        // When
        String result = approvalServiceImpl.transferTask(mockActionForm);

        // Then
        assertNotNull(result);
        verify(workflowTaskDao, times(1)).selectById(1L);
        verify(workflowTaskDao, times(1)).updateById(any(WorkflowTaskEntity.class));
    }

    @Test
    @DisplayName("Test delegateTask - Success Scenario")
    void test_delegateTask_Success() {
        // Given
        mockActionForm.setActionType("DELEGATE");  // 修复：使用setActionType
        mockActionForm.setTargetUserId(200L);
        when(workflowTaskDao.selectById(1L)).thenReturn(mockTaskEntity);
        when(workflowTaskDao.updateById(any(WorkflowTaskEntity.class))).thenReturn(1);

        // When
        String result = approvalServiceImpl.delegateTask(mockActionForm);

        // Then
        assertNotNull(result);
        verify(workflowTaskDao, times(1)).selectById(1L);
        verify(workflowTaskDao, times(1)).updateById(any(WorkflowTaskEntity.class));
    }

    @Test
    @DisplayName("Test getTaskDetail - Success Scenario")
    void test_getTaskDetail_Success() {
        // Given
        Long taskId = 1L;
        when(workflowTaskDao.selectById(taskId)).thenReturn(mockTaskEntity);

        // When
        ApprovalTaskVO result = approvalServiceImpl.getTaskDetail(taskId);

        // Then
        assertNotNull(result);
        verify(workflowTaskDao, times(1)).selectById(taskId);
    }

    @Test
    @DisplayName("Test getInstanceDetail - Success Scenario")
    void test_getInstanceDetail_Success() {
        // Given
        Long instanceId = 1L;
        WorkflowInstanceEntity instanceEntity = new WorkflowInstanceEntity();
        instanceEntity.setId(instanceId);
        instanceEntity.setProcessName("Test Instance");  // 修复：使用setProcessName而不是setInstanceName
        when(approvalInstanceDao.selectById(instanceId)).thenReturn(instanceEntity);

        // When
        ApprovalInstanceVO result = approvalServiceImpl.getInstanceDetail(instanceId);

        // Then
        assertNotNull(result);
        verify(approvalInstanceDao, times(1)).selectById(instanceId);
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
        mockStats.setTodoCount(20L);  // 修复：使用setTodoCount而不是setPendingCount
        mockStats.setApprovedCount(70L);
        mockStats.setRejectedCount(10L);

        when(approvalStatisticsDao.selectStatistics(anyLong(), anyLong(), anyString())).thenReturn(mockStats);  // 修复：使用selectStatistics而不是getStatistics

        // When
        ApprovalStatisticsVO result = approvalServiceImpl.getApprovalStatistics(userId, departmentId, statisticsType);

        // Then
        assertNotNull(result);
        assertEquals(100L, result.getTotalCount());
        verify(approvalStatisticsDao, times(1)).selectStatistics(anyLong(), anyLong(), anyString());  // 修复：使用selectStatistics
    }

    @Test
    @DisplayName("Test getBusinessTypes - Success Scenario")
    void test_getBusinessTypes_Success() {
        // When
        List<Map<String, Object>> result = approvalServiceImpl.getBusinessTypes();

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Test getPriorities - Success Scenario")
    void test_getPriorities_Success() {
        // When
        List<Map<String, Object>> result = approvalServiceImpl.getPriorities();

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Test batchProcessTasks - Success Scenario")
    void test_batchProcessTasks_Success() {
        // Given
        List<Long> taskIds = Arrays.asList(1L, 2L, 3L);
        String action = "APPROVE";
        Long userId = 100L;
        String comment = "Batch approved";

        when(workflowTaskDao.selectById(anyLong())).thenReturn(mockTaskEntity);
        when(workflowTaskDao.updateById(any(WorkflowTaskEntity.class))).thenReturn(1);

        // When
        Map<String, Object> result = approvalServiceImpl.batchProcessTasks(taskIds, action, userId, comment);

        // Then
        assertNotNull(result);
        verify(workflowTaskDao, atLeastOnce()).selectById(anyLong());
    }

    @Test
    @DisplayName("Test withdrawApplication - Success Scenario")
    void test_withdrawApplication_Success() {
        // Given
        Long instanceId = 1L;
        Long applicantId = 100L;
        String reason = "Need to modify";

        WorkflowInstanceEntity instanceEntity = new WorkflowInstanceEntity();
        instanceEntity.setId(instanceId);
        instanceEntity.setInitiatorId(applicantId);  // 修复：使用setInitiatorId而不是setApplicantId
        instanceEntity.setStatus(1);  // 修复：status是Integer类型，1表示运行中

        when(approvalInstanceDao.selectById(instanceId)).thenReturn(instanceEntity);
        when(approvalInstanceDao.updateById(any(WorkflowInstanceEntity.class))).thenReturn(1);

        // When
        String result = approvalServiceImpl.withdrawApplication(instanceId, applicantId, reason);

        // Then
        assertNotNull(result);
        verify(approvalInstanceDao, times(1)).selectById(instanceId);
        verify(approvalInstanceDao, times(1)).updateById(any(WorkflowInstanceEntity.class));
    }
}


