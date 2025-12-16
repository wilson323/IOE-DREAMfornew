package net.lab1024.sa.oa.workflow.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.oa.workflow.dao.WorkflowDefinitionDao;
import net.lab1024.sa.oa.workflow.dao.WorkflowInstanceDao;
import net.lab1024.sa.oa.workflow.dao.WorkflowTaskDao;
import net.lab1024.sa.oa.workflow.entity.WorkflowDefinitionEntity;
import net.lab1024.sa.oa.workflow.entity.WorkflowInstanceEntity;
import net.lab1024.sa.oa.workflow.entity.WorkflowTaskEntity;
import net.lab1024.sa.oa.workflow.service.impl.WorkflowEngineServiceImpl;
import net.lab1024.sa.oa.workflow.websocket.WorkflowWebSocketController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * WorkflowEngineServiceImpl单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：WorkflowEngineServiceImpl核心业务方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkflowEngineServiceImpl单元测试")
class WorkflowEngineServiceImplTest {

    @Mock
    private WorkflowDefinitionDao workflowDefinitionDao;

    @Mock
    private WorkflowInstanceDao workflowInstanceDao;

    @Mock
    private WorkflowTaskDao workflowTaskDao;

    @Mock
    private WorkflowWebSocketController webSocketController;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private WorkflowEngineServiceImpl workflowEngineServiceImpl;

    private WorkflowDefinitionEntity mockDefinition;
    private WorkflowInstanceEntity mockInstance;
    private WorkflowTaskEntity mockTask;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        mockDefinition = new WorkflowDefinitionEntity();
        mockDefinition.setId(1L);
        mockDefinition.setProcessKey("test_process");
        mockDefinition.setProcessName("测试流程");
        mockDefinition.setStatus("PUBLISHED");
        mockDefinition.setDeletedFlag(0);

        mockInstance = new WorkflowInstanceEntity();
        mockInstance.setId(1L);
        mockInstance.setProcessDefinitionId(1L);  // 修复：使用setProcessDefinitionId而不是setDefinitionId
        mockInstance.setBusinessId(1L);  // 修复：使用setBusinessId而不是setBusinessKey（业务ID是Long类型）
        mockInstance.setStatus(1);  // 修复：status是Integer类型，1表示运行中
        mockInstance.setInitiatorId(100L);  // 修复：使用setInitiatorId而不是setStartUserId
        mockInstance.setDeletedFlag(0);

        mockTask = new WorkflowTaskEntity();
        mockTask.setId(1L);
        mockTask.setInstanceId(1L);
        mockTask.setTaskName("测试任务");
        mockTask.setAssigneeId(200L);
        mockTask.setStatus(1);  // 修复：status是Integer类型，1表示待受理
        mockTask.setDeletedFlag(0);
    }

    @Test
    @DisplayName("Test deployProcess - Success")
    void test_deployProcess_Success() {
        // Given
        String bpmnXml = "<bpmn>...</bpmn>";
        String processName = "测试流程";
        String processKey = "test_process";
        String description = "测试描述";
        String category = "测试分类";

        when(workflowDefinitionDao.countByProcessKey(eq(processKey), isNull())).thenReturn(0);
        when(workflowDefinitionDao.insert(any(WorkflowDefinitionEntity.class))).thenReturn(1);

        // When
        ResponseDTO<String> result = workflowEngineServiceImpl.deployProcess(
                bpmnXml, processName, processKey, description, category);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        verify(workflowDefinitionDao, times(1)).insert(any(WorkflowDefinitionEntity.class));
    }

    @Test
    @DisplayName("Test startProcess - Success")
    void test_startProcess_Success() {
        // Given
        Long definitionId = 1L;
        String businessKey = "BUS001";
        String instanceName = "测试实例";
        Map<String, Object> variables = new HashMap<>();
        variables.put("initiatorId", "100");
        Map<String, Object> formData = new HashMap<>();

        when(workflowDefinitionDao.selectById(definitionId)).thenReturn(mockDefinition);
        when(workflowInstanceDao.insert(any(WorkflowInstanceEntity.class))).thenAnswer(invocation -> {
            WorkflowInstanceEntity instance = invocation.getArgument(0);
            instance.setId(10L);
            return 1;
        });

        // When
        ResponseDTO<Long> result = workflowEngineServiceImpl.startProcess(
                definitionId, businessKey, instanceName, variables, formData);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        verify(workflowInstanceDao, times(1)).insert(any(WorkflowInstanceEntity.class));
    }

    @Test
    @DisplayName("Test getTask - Success")
    void test_getTask_Success() {
        // Given
        Long taskId = 1L;
        when(workflowTaskDao.selectById(taskId)).thenReturn(mockTask);

        // When
        ResponseDTO<WorkflowTaskEntity> result = workflowEngineServiceImpl.getTask(taskId);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        assertEquals(taskId, result.getData().getId());
    }

    @Test
    @DisplayName("Test getTask - Not Found")
    void test_getTask_NotFound() {
        // Given
        Long taskId = 999L;
        when(workflowTaskDao.selectById(taskId)).thenReturn(null);

        // When & Then
        assertThrows(BusinessException.class, () -> workflowEngineServiceImpl.getTask(taskId));
    }

    @Test
    @DisplayName("Test completeTask - Success")
    void test_completeTask_Success() {
        // Given
        Long taskId = 1L;
        String outcome = "APPROVED";
        String comment = "审批通过";
        Map<String, Object> variables = new HashMap<>();
        Map<String, Object> formData = new HashMap<>();

        when(workflowTaskDao.selectById(taskId)).thenReturn(mockTask);
        when(workflowInstanceDao.selectById(mockTask.getInstanceId())).thenReturn(mockInstance);
        when(workflowTaskDao.completeTask(eq(taskId), anyInt(), anyString())).thenReturn(1);

        // When
        ResponseDTO<String> result = workflowEngineServiceImpl.completeTask(
                taskId, outcome, comment, variables, formData);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        verify(workflowTaskDao, times(1)).completeTask(eq(taskId), anyInt(), eq(comment));
    }

    @Test
    @DisplayName("Test pageMyTasks - Success")
    void test_pageMyTasks_Success() {
        // Given
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(1);
        pageParam.setPageSize(10);
        Long userId = 200L;
        String category = "测试分类";
        Integer priority = 1;
        String dueStatus = "PENDING";

        Page<WorkflowTaskEntity> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(mockTask));
        page.setTotal(1);

        @SuppressWarnings("unchecked")
        Page<WorkflowTaskEntity> typedPage = page;
        when(workflowTaskDao.selectMyTasksPage(any(Page.class), eq(userId), eq(category), eq(priority), eq(dueStatus))).thenReturn(typedPage);

        // When
        ResponseDTO<PageResult<WorkflowTaskEntity>> result = workflowEngineServiceImpl.pageMyTasks(
                pageParam, userId, category, priority, dueStatus);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getList().size());
    }

}


